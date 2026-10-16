package com.example.fendly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

final class OfflineReportDraftStore extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "fendly_offline_reports.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE = "report_drafts";

    static final class Draft {
        final String id;
        final String ownerUid;
        final String title;
        final String description;
        final String location;
        final String date;
        final double latitude;
        final double longitude;
        final String category;
        final String sourceLanguage;
        final String paymentId;
        final List<String> imagePaths;
        final List<String> uploadedImageUrls;

        Draft(Cursor cursor) throws Exception {
            id = cursor.getString(cursor.getColumnIndexOrThrow("draft_id"));
            ownerUid = cursor.getString(cursor.getColumnIndexOrThrow("owner_uid"));
            title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            date = cursor.getString(cursor.getColumnIndexOrThrow("report_date"));
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
            category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            sourceLanguage = cursor.getString(cursor.getColumnIndexOrThrow("source_language"));
            paymentId = cursor.getString(cursor.getColumnIndexOrThrow("payment_id"));
            JSONArray paths = new JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("image_paths")));
            imagePaths = new ArrayList<>();
            for (int index = 0; index < paths.length(); index++) imagePaths.add(paths.getString(index));
            JSONArray uploaded = new JSONArray(
                    cursor.getString(cursor.getColumnIndexOrThrow("uploaded_image_urls"))
            );
            uploadedImageUrls = new ArrayList<>();
            for (int index = 0; index < uploaded.length(); index++) {
                uploadedImageUrls.add(uploaded.getString(index));
            }
        }
    }

    OfflineReportDraftStore(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE + " ("
                + "draft_id TEXT PRIMARY KEY NOT NULL, owner_uid TEXT NOT NULL, title TEXT NOT NULL, "
                + "description TEXT NOT NULL, location TEXT, report_date TEXT, latitude REAL NOT NULL, "
                + "longitude REAL NOT NULL, category TEXT NOT NULL, source_language TEXT NOT NULL, "
                + "payment_id TEXT, image_paths TEXT NOT NULL, uploaded_image_urls TEXT NOT NULL, "
                + "created_at INTEGER NOT NULL)");
        database.execSQL("CREATE INDEX report_drafts_owner_created ON " + TABLE
                + " (owner_uid, created_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            database.execSQL(
                    "ALTER TABLE " + TABLE
                            + " ADD COLUMN uploaded_image_urls TEXT NOT NULL DEFAULT '[]'"
            );
        }
    }

    synchronized String saveLostDraft(
            Context context,
            String draftId,
            String ownerUid,
            String title,
            String description,
            String location,
            String date,
            double latitude,
            double longitude,
            String category,
            String sourceLanguage,
            String paymentId,
            Uri[] images,
            Bitmap[] cameraImages
    ) throws Exception {
        JSONArray paths = new JSONArray();
        List<File> savedFiles = new ArrayList<>();
        File directory = new File(context.getFilesDir(), "offline_report_drafts");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Could not create local draft image storage");
        }
        for (int index = 0; index < Math.min(images.length, cameraImages.length); index++) {
            if (images[index] == null && cameraImages[index] == null) continue;
            File file = new File(directory, draftId + "_" + index + ".jpg");
            savedFiles.add(file);
            try (FileOutputStream output = new FileOutputStream(file)) {
                if (images[index] != null) {
                    try (InputStream input = context.getContentResolver().openInputStream(images[index])) {
                        if (input == null) throw new IllegalStateException("Could not read selected report image");
                        byte[] bytes = readImage(input);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bitmap == null) throw new IllegalStateException("Selected report image is not supported");
                        bitmap = resizeForUpload(bitmap);
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)) {
                            throw new IllegalStateException("Could not encode selected report image");
                        }
                    }
                } else if (!resizeForUpload(cameraImages[index]).compress(Bitmap.CompressFormat.JPEG, 80, output)) {
                    throw new IllegalStateException("Could not save captured report image");
                }
            }
            paths.put(file.getAbsolutePath());
        }

        ContentValues values = new ContentValues();
        values.put("draft_id", draftId);
        values.put("owner_uid", ownerUid);
        values.put("title", title);
        values.put("description", description);
        values.put("location", location);
        values.put("report_date", date);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("category", category);
        values.put("source_language", sourceLanguage);
        values.put("payment_id", paymentId);
        values.put("image_paths", paths.toString());
        values.put("uploaded_image_urls", new JSONArray().toString());
        values.put("created_at", System.currentTimeMillis());
        try {
            getWritableDatabase().insertOrThrow(TABLE, null, values);
            return draftId;
        } catch (Exception error) {
            for (File file : savedFiles) {
                if (file.exists() && !file.delete()) {
                    error.addSuppressed(new IllegalStateException("Could not remove incomplete local image copy"));
                }
            }
            throw error;
        }
    }

    private static byte[] readImage(InputStream input) throws Exception {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }

    private static Bitmap resizeForUpload(Bitmap bitmap) {
        int maxDimension = 1280;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxDimension && height <= maxDimension) return bitmap;
        float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);
        return Bitmap.createScaledBitmap(
                bitmap,
                Math.round(width * scale),
                Math.round(height * scale),
                true
        );
    }

    synchronized List<Draft> getDrafts(String ownerUid) throws Exception {
        List<Draft> drafts = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE,
                null,
                "owner_uid = ?",
                new String[]{ownerUid},
                null,
                null,
                "created_at ASC")) {
            while (cursor.moveToNext()) drafts.add(new Draft(cursor));
        }
        return drafts;
    }

    synchronized void saveUploadedImageUrl(String draftId, int imageIndex, String imageUrl)
            throws Exception {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE,
                new String[]{"uploaded_image_urls"},
                "draft_id = ?",
                new String[]{draftId},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) throw new IllegalStateException("Offline report draft no longer exists");
            JSONArray urls = new JSONArray(cursor.getString(0));
            if (imageIndex != urls.length()) {
                throw new IllegalStateException("Offline report image upload order changed");
            }
            urls.put(imageUrl);
            ContentValues values = new ContentValues();
            values.put("uploaded_image_urls", urls.toString());
            int updated = getWritableDatabase().update(
                    TABLE,
                    values,
                    "draft_id = ?",
                    new String[]{draftId}
            );
            if (updated != 1) throw new IllegalStateException("Could not save uploaded report image URL");
        }
    }

    synchronized void deleteDraft(Context context, Draft draft) throws Exception {
        int deleted = getWritableDatabase().delete(TABLE, "draft_id = ?", new String[]{draft.id});
        if (deleted != 1) throw new IllegalStateException("Could not remove synced local report draft");
        for (String path : draft.imagePaths) {
            File file = new File(path);
            if (file.exists() && !file.delete()) {
                Log.w("OFFLINE_REPORT", "Synced report image could not be removed: " + path);
            }
        }
    }

    synchronized void deleteDraftsForOwner(String ownerUid) throws Exception {
        for (Draft draft : getDrafts(ownerUid)) {
            for (String path : draft.imagePaths) {
                File file = new File(path);
                if (file.exists() && !file.delete()) {
                    throw new IllegalStateException("Could not remove a locally queued report image");
                }
            }
            int deleted = getWritableDatabase().delete(
                    TABLE,
                    "draft_id = ? AND owner_uid = ?",
                    new String[]{draft.id, ownerUid}
            );
            if (deleted != 1) throw new IllegalStateException("Could not remove a locally queued report");
        }
    }
}
