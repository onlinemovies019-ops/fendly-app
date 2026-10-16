package com.example.fendly;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class OfflineReportSyncWorker extends Worker {
    private static final String WORK_NAME = "fendly_offline_lost_report_sync";

    public OfflineReportSyncWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParameters
    ) {
        super(context, workerParameters);
    }

    static void enqueue(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(OfflineReportSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return Result.success();
        try (OfflineReportDraftStore store = new OfflineReportDraftStore(getApplicationContext())) {
            List<OfflineReportDraftStore.Draft> drafts = store.getDrafts(user.getUid());
            if (drafts.isEmpty()) return Result.success();
            GetTokenResult tokenResult = Tasks.await(user.getIdToken(true));
            String token = tokenResult.getToken();
            if (token == null || token.trim().isEmpty()) {
                Log.w("OFFLINE_REPORT", "Firebase returned an empty token for the signed-in user");
                return Result.retry();
            }
            for (OfflineReportDraftStore.Draft draft : drafts) {
                try {
                    List<String> imageUrls = new ArrayList<>();
                    if (draft.uploadedImageUrls.size() > draft.imagePaths.size()) {
                        throw new IllegalStateException("Saved report has invalid image upload state");
                    }
                    for (int index = 0; index < draft.imagePaths.size(); index++) {
                        String imageUrl;
                        if (index < draft.uploadedImageUrls.size()) {
                            imageUrl = draft.uploadedImageUrls.get(index);
                        } else {
                            imageUrl = uploadImage(draft.imagePaths.get(index), token);
                            store.saveUploadedImageUrl(draft.id, index, imageUrl);
                        }
                        imageUrls.add(imageUrl);
                    }
                    String imageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);
                    AiMatchService.ApiResponse response = AiMatchService.createItem(
                            draft.title,
                            draft.description,
                            imageUrl,
                            imageUrls,
                            "LOST",
                            draft.latitude,
                            draft.longitude,
                            draft.location,
                            draft.date,
                            draft.paymentId,
                            draft.sourceLanguage,
                            token,
                            draft.category,
                            true,
                            draft.id
                    );
                    if (!response.isSuccessful()) {
                        int code = response.getStatusCode();
                        if (code == 408 || code == 429 || code >= 500) {
                            Log.w("OFFLINE_REPORT", "Temporary server error syncing local report: " + code);
                            return Result.retry();
                        }
                        throw new PermanentSyncException("Server rejected queued report with HTTP " + code);
                    }
                    store.deleteDraft(getApplicationContext(), draft);
                } catch (RetryableSyncException error) {
                    Log.w("OFFLINE_REPORT", "Temporary image upload failure while syncing", error);
                    return Result.retry();
                } catch (PermanentSyncException error) {
                    Log.e("OFFLINE_REPORT", "Saved report draft requires correction before it can sync", error);
                    return Result.success();
                } catch (Exception error) {
                    Log.e("OFFLINE_REPORT", "Could not sync a saved report draft", error);
                    return Result.retry();
                }
            }
            return Result.success();
        } catch (Exception error) {
            Log.e("OFFLINE_REPORT", "Offline report synchronization failed", error);
            return Result.retry();
        }
    }

    private String uploadImage(String imagePath, String token) throws Exception {
        String boundary = "FendlyOffline" + System.currentTimeMillis();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(BuildConfig.API_BASE_URL + "/api/upload").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60_000);
            connection.setReadTimeout(60_000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream output = connection.getOutputStream();
                 InputStream input = new FileInputStream(new File(imagePath))) {
                output.write(("--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"image\"; filename=\"offline-report.jpg\"\r\n"
                        + "Content-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            int code = connection.getResponseCode();
            InputStream response = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readStream(response);
            if (code == 408 || code == 429 || code >= 500) {
                throw new RetryableSyncException("Image upload returned HTTP " + code);
            }
            if (code < 200 || code >= 300) {
                throw new PermanentSyncException("Image upload was rejected with HTTP " + code + ": " + body);
            }
            String url = new JSONObject(body).optString("url", "");
            if (url.trim().isEmpty()) throw new IllegalStateException("Image upload response did not contain a URL");
            return url;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String readStream(InputStream input) throws Exception {
        if (input == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            return body.toString();
        }
    }

    private static final class RetryableSyncException extends Exception {
        RetryableSyncException(String message) {
            super(message);
        }
    }

    private static final class PermanentSyncException extends Exception {
        PermanentSyncException(String message) {
            super(message);
        }
    }
}
