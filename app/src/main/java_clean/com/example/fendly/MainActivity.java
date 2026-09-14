package com.example.fendly;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Space;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.text.InputType;
import android.app.DatePickerDialog;
import java.util.Calendar;
import android.os.Handler;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.firebase.auth.GetTokenResult;
import android.graphics.drawable.GradientDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import com.example.fendly.notifications.FcmRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import android.net.Uri;
import android.location.Location;
import android.location.LocationManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(13, 12, 9);
    private static final int SURFACE = Color.rgb(25, 22, 17);
    private static final int GOLD = Color.rgb(212, 175, 55);
    private static final int SILVER = Color.rgb(199, 201, 204);
    private TextView status;
    private SharedPreferences languagePreferences;
    private int selectedLanguage;
    private boolean darkMode;
    private LinearLayout activeContent;
    private Uri selectedImage;
    private Bitmap capturedImage;
    private double currentLat;
    private double currentLng;
    private boolean hasLocation;
    private String phoneVerificationId;
    private boolean phoneVerificationHandled;
    private String currentReportType;
    private Handler adminPressHandler = new Handler();
    private boolean accountCreated;
    private static final String API_BASE = "https://fendly-api.onrender.com";
    private final ExecutorService network = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        languagePreferences = getSharedPreferences("fendly_language", MODE_PRIVATE);
        selectedLanguage = languagePreferences.getInt("selected_language_index", 0);
        darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE).getBoolean("dark_mode", true);
        accountCreated = getSharedPreferences("fendly_account", MODE_PRIVATE).getBoolean("created", false);
        applySystemBarColors();
        buildScreen();
        FcmRegistration.registerCurrentToken();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(42, 24, 42, 24);
        root.setBackgroundColor(backgroundColor());
        if (Build.VERSION.SDK_INT >= 29) root.setForceDarkAllowed(false);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        TextView languageButton = new TextView(this);
        languageButton.setText("A");
        languageButton.setTextColor(accentColor());
        languageButton.setTextSize(22);
        languageButton.setGravity(Gravity.CENTER);
        languageButton.setContentDescription("Select language");
        languageButton.setBackground(outlineButton());
        languageButton.setClickable(false);
        languageButton.setFocusable(false);
        topBar.addView(languageButton, new LinearLayout.LayoutParams(54, 54));
        Space topSpacer = new Space(this);
        topBar.addView(topSpacer, new LinearLayout.LayoutParams(0, 1, 1));
        TextView modeButton = new TextView(this);
        modeButton.setText(darkMode ? "L" : "D");
        modeButton.setTextColor(accentColor());
        modeButton.setTextSize(22);
        modeButton.setGravity(Gravity.CENTER);
        modeButton.setContentDescription(darkMode ? "Switch to light mode" : "Switch to dark mode");
        modeButton.setBackground(outlineButton());
        modeButton.setOnClickListener(view -> {
            darkMode = !darkMode;
            getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
            applySystemBarColors();
            buildScreen();
        });
        topBar.addView(modeButton, new LinearLayout.LayoutParams(54, 54));
        topBar.setOnClickListener(view -> showLanguagePicker());
        root.addView(topBar, new LinearLayout.LayoutParams(-1, 82));

        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.splash_emblem);
        emblem.setAdjustViewBounds(true);
        emblem.setContentDescription("Fendly emblem");
        emblem.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                adminPressHandler.postDelayed(() -> showAdminDashboard(), 5000);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                adminPressHandler.removeCallbacksAndMessages(null);
                return true;
            }
            return true;
        });
        root.addView(emblem, new LinearLayout.LayoutParams(190, 190));

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(0, 112, 83));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(250, 5);
        dividerParams.setMargins(0, 8, 0, 14);
        root.addView(divider, dividerParams);

        TextView wordmark = new TextView(this);
        wordmark.setText("Fendly");
        wordmark.setTextColor(accentColor());
        wordmark.setTextSize(38);
        wordmark.setGravity(Gravity.CENTER);
        wordmark.setTypeface(android.graphics.Typeface.create("cursive", android.graphics.Typeface.BOLD));
        wordmark.setContentDescription("Fendly");
        root.addView(wordmark, new LinearLayout.LayoutParams(250, 104));

        TextView tagline = new TextView(this);
        tagline.setText(localized("tagline"));
        tagline.setTextColor(darkMode ? SILVER : Color.BLACK);
        tagline.setTextSize(16);
        tagline.setGravity(Gravity.CENTER);
        root.addView(tagline, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText(localized("status"));
        status.setTextSize(17);
        status.setTextColor(darkMode ? SILVER : Color.BLACK);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, 92, 0, 42);
        root.addView(status, statusParams);

        TextView createProfile = new TextView(this);
        createProfile.setText(accountCreated ? "Open my Fendly" : localized("create_profile"));
        createProfile.setTextColor(Color.WHITE);
        createProfile.setTextSize(16);
        createProfile.setTypeface(null, android.graphics.Typeface.BOLD);
        createProfile.setGravity(Gravity.CENTER);
        createProfile.setBackground(goldButton());
        createProfile.setOnClickListener(view -> FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(result -> {
                    status.setText("Profile started. Push notifications are enabled.");
                    FcmRegistration.registerCurrentToken();
                    if (accountCreated) showHome(); else showProfileSetup();
                })
                .addOnFailureListener(error -> status.setText("Connection failed: " + error.getMessage())));
        root.addView(createProfile, new LinearLayout.LayoutParams(-1, 56));

        TextView pinLogin = new TextView(this);
        pinLogin.setText(localized("pin_login"));
        pinLogin.setTextColor(darkMode ? Color.WHITE : Color.BLACK);
        pinLogin.setTextSize(16);
        pinLogin.setGravity(Gravity.CENTER);
        pinLogin.setBackground(outlineButton());
        pinLogin.setOnClickListener(view -> showPinLogin());
        LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams(-1, 56);
        pinParams.setMargins(0, 28, 0, 0);
        root.addView(pinLogin, pinParams);
        setContentView(root);
    }

    private void showProfileSetup() {
        LinearLayout root = screenBase("Complete your profile");
        addHeading("A little about you", "This helps neighbours know who they are helping.");
        EditText username = field("Username");
        EditText pin = field("Create 4-digit PIN");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        EditText name = field("Full name");
        EditText dob = field("Date of birth (DD/MM/YYYY)");
        EditText email = field("Email address");
        EditText mobile = field("Mobile number");
        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
        mobile.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(10)});
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        addField(root, username);
        addField(root, pin);
        addField(root, name);
        addField(root, dob);
        addField(root, email);
        addField(root, mobile);
        TextView save = actionButton("Save and continue", true);
        save.setOnClickListener(view -> {
            if (username.getText().toString().trim().length() < 3) {
                username.setError("Use at least 3 characters");
            } else if (pin.getText().toString().length() != 4) {
                pin.setError("PIN must be exactly 4 digits");
            } else if (mobile.getText().toString().length() != 10) {
                mobile.setError("Enter exactly 10 digits");
            } else if (!validEmail(email.getText().toString())) {
                email.setError("Use Gmail, Yahoo, or Hotmail");
            } else {
                startPhoneVerification(mobile.getText().toString().trim(), username.getText().toString().trim(), pin.getText().toString(), save);
            }
        });
        addField(root, save);
    }

    private void startPhoneVerification(String mobile, String username, String pin, TextView save) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            save.setText("Sign in first");
            return;
        }
        phoneVerificationHandled = false;
        save.setText("Sending verification code...");
        save.setEnabled(false);
        PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91" + mobile)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        completePhoneVerification(credential, username, pin, save);
                    }

                    @Override
                    public void onVerificationFailed(com.google.firebase.FirebaseException error) {
                        save.setText("SMS verification unavailable");
                        save.setEnabled(true);
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        phoneVerificationId = verificationId;
                        save.setText("Enter SMS code");
                        showOtpDialog(username, pin, save);
                    }
                }).build());
    }

    private void showOtpDialog(String username, String pin, TextView save) {
        EditText code = field("6-digit SMS code");
        new AlertDialog.Builder(this)
                .setTitle("Verify your mobile")
                .setMessage("Enter the code sent to your mobile number.")
                .setView(code)
                .setPositiveButton("Verify", (dialog, which) -> {
                    if (phoneVerificationId == null || code.getText().toString().trim().length() != 6) {
                        save.setText("Invalid SMS code");
                        save.setEnabled(true);
                        return;
                    }
                    completePhoneVerification(PhoneAuthProvider.getCredential(phoneVerificationId, code.getText().toString().trim()), username, pin, save);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    save.setText("Save and continue");
                    save.setEnabled(true);
                })
                .show();
    }

    private void completePhoneVerification(PhoneAuthCredential credential, String username, String pin, TextView save) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                .addOnSuccessListener(result -> finishProfileSetup(username, pin, save))
                .addOnFailureListener(error -> {
                    phoneVerificationHandled = false;
                    save.setText("SMS verification failed");
                    save.setEnabled(true);
                    Toast.makeText(this, "Could not verify mobile number", Toast.LENGTH_LONG).show();
                });
    }

    private void finishProfileSetup(String username, String pin, TextView save) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            save.setText("Sign in first");
            return;
        }
        save.setText("Checking username...");
        save.setEnabled(false);
        auth.getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            int reservationCode = reserveUsername(username, token.getToken());
            runOnUiThread(() -> {
                if (reservationCode != 201 && reservationCode != 200) {
                    save.setText("Username unavailable");
                    save.setEnabled(true);
                    Toast.makeText(this, "Choose another username", Toast.LENGTH_LONG).show();
                    return;
                }
                saveFirebaseCredential(username, pin, save);
            });
        })).addOnFailureListener(error -> {
            save.setText("Authentication unavailable");
            save.setEnabled(true);
        });
    }

    private int reserveUsername(String username, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + "/api/users/username").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String body = "{\"username\":\"" + escapeJson(username) + "\"}";
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
            }
            return connection.getResponseCode();
        } catch (Exception error) {
            return -1;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void saveFirebaseCredential(String username, String pin, TextView save) {
        String email = credentialEmail(username);
        String password = credentialPassword(username, pin);
        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(EmailAuthProvider.getCredential(email, password))
                .addOnSuccessListener(result -> {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", username).apply();
                    accountCreated = true;
                    showHome();
                })
                .addOnFailureListener(error -> {
                    save.setText("Could not secure account");
                    save.setEnabled(true);
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String credentialEmail(String username) {
        return username.trim().toLowerCase(Locale.US) + "@login.fendly.app";
    }

    private String credentialPassword(String username, String pin) {
        return "Fendly!" + username.trim().toLowerCase(Locale.US) + "#" + pin;
    }

    private void showPinLogin() {
        LinearLayout root = screenBase("Login with PIN");
        addHeading("Welcome back.", "Use the username and PIN from your Fendly profile.");
        EditText username = field("Username");
        EditText pin = field("4-digit PIN");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        addField(root, username);
        addField(root, pin);
        TextView login = actionButton("Log in", true);
        login.setOnClickListener(view -> {
            String name = username.getText().toString().trim();
            String code = pin.getText().toString();
            if (name.length() < 3 || code.length() != 4) {
                Toast.makeText(this, "Enter a valid username and 4-digit PIN", Toast.LENGTH_LONG).show();
                return;
            }
            login.setText("Signing in...");
            login.setEnabled(false);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(credentialEmail(name), credentialPassword(name, code))
                    .addOnSuccessListener(result -> {
                        accountCreated = true;
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", name).apply();
                        showHome();
                    })
                    .addOnFailureListener(error -> {
                        login.setText("Try again");
                        login.setEnabled(true);
                        Toast.makeText(this, "Incorrect username or PIN", Toast.LENGTH_LONG).show();
                    });
        });
        addField(root, login);
    }

    private boolean validEmail(String value) {
        String email = value.trim().toLowerCase();
        return email.matches("^[^@\\s]+@(gmail|yahoo|hotmail)\\.com$");
    }

    private void showHome() {
        LinearLayout root = screenBase("Home");
        addHeading("Find what matters.", "Lost nearby? Found something? Start here.");
        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.HORIZONTAL);
        TextView lost = actionButton("LOST", true);
        lost.setBackground(round(Color.rgb(11, 93, 69), 18));
        lost.setOnClickListener(view -> showReport("LOST"));
        choices.addView(lost, new LinearLayout.LayoutParams(0, 72, 1));
        TextView found = actionButton("FOUND", false);
        found.setTextColor(Color.rgb(18, 18, 18));
        found.setBackground(round(Color.rgb(201, 162, 76), 18));
        found.setOnClickListener(view -> showReport("FOUND"));
        choices.addView(found, new LinearLayout.LayoutParams(0, 72, 1));
        addField(root, choices);
        TextView reports = actionButton("My reports", false);
        reports.setOnClickListener(view -> showReports());
        addField(root, reports);
        TextView profile = actionButton("My profile", false);
        profile.setOnClickListener(view -> showProfile());
        addField(root, profile);
        TextView account = text("Signed in as dummy user", 14, secondaryTextColor(), android.graphics.Typeface.NORMAL);
        account.setGravity(Gravity.CENTER);
        addField(root, account);
    }

    private void showReport(String type) {
        currentReportType = type;
        LinearLayout root = screenBase(type.equals("FOUND") ? "Post found item" : "Report lost item");
        addHeading(type.equals("FOUND") ? "Help it get home." : "Let's find it.", "Add clear details so the right person can recognise it.");
        EditText item = field("Item name");
        EditText description = field("Description and identifying details");
        description.setMinLines(3);
        EditText location = field("Location or landmark");
        EditText date = field("Date and time");
        date.setFocusable(false);
        date.setOnClickListener(view -> pickDate(date));
        addField(root, item);
        addField(root, description);
        addField(root, location);
        addField(root, date);
        TextView upload = actionButton(selectedImage == null && capturedImage == null ? "Upload item image" : "Image selected", false);
        upload.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(intent, 701);
        });
        addField(root, upload);
        TextView camera = actionButton("Take photo with camera", false);
        camera.setOnClickListener(view -> {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 703);
        });
        addField(root, camera);
        TextView useLocation = actionButton("Use current location", false);
        useLocation.setOnClickListener(view -> requestLocation(useLocation));
        addField(root, useLocation);
        TextView publish = actionButton(type.equals("FOUND") ? "Publish found item" : "Publish lost item", true);
        publish.setOnClickListener(view -> {
            if ("LOST".equals(type)) {
                showSubscription(item, description, location, date);
                return;
            }
            submitItem(type, item, description, location, date, publish);
        });
        addField(root, publish);
    }

    private void submitItem(String type, EditText item, EditText description, EditText location, EditText date, TextView publish) {
        String title = item.getText().toString().trim();
        String details = description.getText().toString().trim();
        if (title.isEmpty()) {
            item.setError("Enter an item name");
            return;
        }
        if (details.isEmpty()) {
            description.setError("Describe the item");
            return;
        }
        publish.setText("Submitting...");
        publish.setEnabled(false);
        Uri image = selectedImage;
        Bitmap cameraImage = capturedImage;
        double latitude = hasLocation ? currentLat : 0.0;
        double longitude = hasLocation ? currentLng : 0.0;
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> {
            if (token == null || token.getToken() == null) {
                publish.setText("Authentication unavailable");
                publish.setEnabled(true);
                return;
            }
            network.execute(() -> {
                int code = postItem(type, title, details, location.getText().toString().trim(), date.getText().toString().trim(), latitude, longitude, image, cameraImage, token.getToken());
                runOnUiThread(() -> {
                    publish.setEnabled(true);
                    if (code >= 200 && code < 300) {
                        Toast.makeText(this, "Report saved securely", Toast.LENGTH_SHORT).show();
                        showReports();
                    } else {
                        publish.setText("Retry submission");
                        Toast.makeText(this, "Could not save report (" + code + ")", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }).addOnFailureListener(error -> {
            publish.setText("Retry submission");
            publish.setEnabled(true);
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show();
        });
    }

    private int postItem(String type, String title, String description, String location, String date, double latitude, double longitude, Uri image, Bitmap cameraImage, String idToken) {
        HttpURLConnection connection = null;
        try {
            String endpoint = API_BASE + ("FOUND".equals(type) ? "/api/items/found" : "/api/items/lost");
            String imageUrl = null;
            if (image != null || cameraImage != null) {
                imageUrl = uploadImage(image, cameraImage, idToken);
                if (imageUrl == null) return -2;
            }
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String details = description;
            if (!location.isEmpty()) details += " Location: " + location;
            if (!date.isEmpty()) details += " Date: " + date;
            String imageJson = imageUrl == null ? "null" : "\"" + escapeJson(imageUrl) + "\"";
            String body = "{\"title\":\"" + escapeJson(title) + "\",\"description\":\"" + escapeJson(details) + "\",\"category\":\"other\",\"lat\":" + latitude + ",\"lng\":" + longitude + ",\"image_url\":" + imageJson + "}";
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
            }
            return connection.getResponseCode();
        } catch (Exception error) {
            return -1;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String uploadImage(Uri image, Bitmap cameraImage, String idToken) {
        HttpURLConnection connection = null;
        String boundary = "FendlyBoundary" + System.currentTimeMillis();
        try {
            byte[] data;
            String contentType;
            String filename;
            if (image != null) {
                try (InputStream input = getContentResolver().openInputStream(image); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    if (input == null) return null;
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                    data = output.toByteArray();
                }
                contentType = getContentResolver().getType(image);
                if (contentType == null) contentType = "image/jpeg";
                filename = "item-image";
            } else {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                cameraImage.compress(Bitmap.CompressFormat.JPEG, 90, output);
                data = output.toByteArray();
                contentType = "image/jpeg";
                filename = "camera-image.jpg";
            }
            connection = (HttpURLConnection) new URL(API_BASE + "/api/upload").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                output.write(("Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
                output.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                output.write(data);
                output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return null;
            return new JSONObject(readStream(connection.getInputStream())).optString("url", null);
        } catch (Exception error) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String readStream(InputStream input) throws Exception {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private void pickDate(EditText target) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (picker, year, month, day) -> target.setText(String.format("%02d/%02d/%04d", day, month + 1, year)), now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showSubscription(EditText item, EditText description, EditText location, EditText date) {
        LinearLayout root = screenBase("Fendly Plus");
        addHeading("Unlock lost-item submissions.", "Found-item reports stay free forever. Lost-item submissions are Rs 99 per year.");
        TextView price = text("Rs 99 / year", 30, accentColor(), android.graphics.Typeface.BOLD);
        price.setGravity(Gravity.CENTER);
        addField(root, price);
        TextView pay = actionButton("Pay and submit lost report", true);
        pay.setOnClickListener(view -> {
            submitItem("LOST", item, description, location, date, pay);
        });
        addField(root, pay);
        TextView back = actionButton("Back to report", false);
        back.setOnClickListener(view -> showReport("LOST"));
        addField(root, back);
    }

    private void showReports() {
        LinearLayout root = screenBase("My reports");
        addHeading("Your reports", "Keep track of items you are helping to reunite.");
        TextView loading = text("Loading reports...", 16, secondaryTextColor(), android.graphics.Typeface.NORMAL);
        addField(root, loading);
        TextView home = actionButton("Back home", false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
        FirebaseAuth user = FirebaseAuth.getInstance();
        if (user.getCurrentUser() == null) {
            loading.setText("Sign in to view reports.");
            return;
        }
        user.getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = fetchReports(token.getToken());
            runOnUiThread(() -> {
                if (response == null) {
                    loading.setText("Reports are temporarily unavailable.");
                    return;
                }
                activeContent.removeView(loading);
                try {
                    JSONArray reports = new JSONArray(response);
                    for (int index = 0; index < reports.length(); index++) {
                        JSONObject report = reports.getJSONObject(index);
                        String type = report.optString("type", "ITEM");
                        String title = report.optString("title", "Untitled item");
                        String detail = report.optString("description", "");
                        if (detail.length() > 90) detail = detail.substring(0, 90) + "...";
                        addField(activeContent, reportRow(title, type + "  ·  " + detail));
                    }
                    if (reports.length() == 0) {
                        addField(activeContent, text("No reports yet.", 16, secondaryTextColor(), android.graphics.Typeface.NORMAL));
                    }
                } catch (Exception error) {
                    loading.setText("Reports could not be read.");
                }
            });
        })).addOnFailureListener(error -> loading.setText("Authentication unavailable."));
    }

    private String fetchReports(String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + "/api/items/mine").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return null;
            return readStream(connection.getInputStream());
        } catch (Exception error) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void showProfile() {
        LinearLayout root = screenBase("My profile");
        addHeading("Dummy user", "Your account details and preferences.");
        addField(root, reportRow("Email", "dummy.user@fendly.app"));
        addField(root, reportRow("Mobile", "+91 90000 00000"));
        addField(root, reportRow("Date of birth", "01/01/1995"));
        TextView home = actionButton("Back home", false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
    }

    private LinearLayout screenBase(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 26, 28, 28);
        root.setBackgroundColor(backgroundColor());
        if (Build.VERSION.SDK_INT >= 29) root.setForceDarkAllowed(false);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = text(title, 22, darkMode ? Color.WHITE : Color.BLACK, android.graphics.Typeface.BOLD);
        bar.addView(heading, new LinearLayout.LayoutParams(0, 58, 1));
        TextView mode = text(darkMode ? "L" : "D", 22, accentColor(), android.graphics.Typeface.BOLD);
        mode.setGravity(Gravity.CENTER);
        mode.setBackground(outlineButton());
        mode.setOnClickListener(view -> {
            darkMode = !darkMode;
            getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
            applySystemBarColors();
            if (currentReportType != null) showReport(currentReportType); else showHome();
        });
        bar.addView(mode, new LinearLayout.LayoutParams(58, 58));
        root.addView(bar);
        ScrollView scroll = new ScrollView(this);
        activeContent = new LinearLayout(this);
        activeContent.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(activeContent, new ScrollView.LayoutParams(-1, -1));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        return activeContent;
    }

    private void addHeading(String title, String subtitle) {
        activeContent.addView(text(title, 31, darkMode ? Color.WHITE : Color.BLACK, android.graphics.Typeface.BOLD));
        addField(activeContent, text(subtitle, 16, secondaryTextColor(), android.graphics.Typeface.NORMAL));
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setTextColor(darkMode ? Color.WHITE : Color.BLACK);
        input.setHintTextColor(secondaryTextColor());
        input.setPadding(18, 0, 18, 0);
        input.setBackground(round(surfaceColor(), 18));
        return input;
    }

    private TextView actionButton(String label, boolean primary) {
        TextView button = text(label, 16, primary || darkMode ? Color.WHITE : Color.BLACK, android.graphics.Typeface.BOLD);
        button.setText(label);
        button.setGravity(Gravity.CENTER);
        button.setBackground(primary ? goldButton() : outlineButton());
        return button;
    }

    private LinearLayout reportRow(String title, String detail) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(18, 8, 18, 8);
        row.setBackground(round(surfaceColor(), 18));
        row.setTag("reportRow");
        TextView titleView = text(title, 15, darkMode ? Color.WHITE : Color.BLACK, android.graphics.Typeface.BOLD);
        titleView.setIncludeFontPadding(false);
        TextView detailView = text(detail, 10, secondaryTextColor(), android.graphics.Typeface.NORMAL);
        detailView.setIncludeFontPadding(false);
        row.addView(titleView);
        row.addView(detailView);
        return row;
    }

    private void addField(LinearLayout parent, View child) {
        int height = "reportRow".equals(child.getTag()) ? 128 : 62;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, height);
        params.setMargins(0, 0, 0, 14);
        parent.addView(child, params);
    }

    private void requestLocation(TextView button) {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 702);
            button.setText("Location permission requested");
        } else {
            updateLocation(button);
        }
    }

    private void updateLocation(TextView button) {
        try {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location best = null;
            for (String provider : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
                Location candidate = manager.getLastKnownLocation(provider);
                if (candidate != null && (best == null || candidate.getTime() > best.getTime())) best = candidate;
            }
            if (best == null) {
                button.setText("Location unavailable");
                return;
            }
            currentLat = best.getLatitude();
            currentLng = best.getLongitude();
            hasLocation = true;
            button.setText(String.format(Locale.US, "Location ready: %.4f, %.4f", currentLat, currentLng));
        } catch (SecurityException error) {
            button.setText("Location permission required");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 702 && currentReportType != null && activeContent != null) {
            for (int index = 0; index < activeContent.getChildCount(); index++) {
                View child = activeContent.getChildAt(index);
                if (child instanceof TextView && ((TextView) child).getText().toString().startsWith("Location")) {
                    updateLocation((TextView) child);
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 701 && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            if (currentReportType != null) showReport(currentReportType);
        }
        if (requestCode == 703 && resultCode == RESULT_OK) {
            capturedImage = data == null || data.getExtras() == null ? null : (Bitmap) data.getExtras().get("data");
            selectedImage = null;
            if (currentReportType != null) showReport(currentReportType);
        }
    }

    private void showAdminDashboard() {
        LinearLayout root = screenBase("Admin dashboard");
        addHeading("Private moderation workspace", "English only · confidential user details");
        TextView loading = text("Loading live admin data...", 16, secondaryTextColor(), android.graphics.Typeface.NORMAL);
        addField(root, loading);
        String[] firstFoundId = new String[1];
        TextView match = actionButton("Review AI match", true);
        match.setOnClickListener(view -> {
            if (firstFoundId[0] == null) {
                Toast.makeText(this, "No found item available", Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                String response = fetchAdminMatches(firstFoundId[0], token.getToken());
                runOnUiThread(() -> Toast.makeText(this, response == null ? "Admin access unavailable" : "AI match results loaded", Toast.LENGTH_LONG).show());
            }));
        });
        addField(root, match);
        TextView notify = actionButton("Confirm and notify owner", false);
        notify.setOnClickListener(view -> Toast.makeText(this, "Select a match before notifying an owner", Toast.LENGTH_LONG).show());
        addField(root, notify);
        TextView exit = actionButton("Exit admin", false);
        exit.setOnClickListener(view -> buildScreen());
        addField(root, exit);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = fetchAdminItems(token.getToken());
            runOnUiThread(() -> {
                if (response == null) {
                    loading.setText("Admin access unavailable.");
                    return;
                }
                activeContent.removeView(loading);
                try {
                    JSONArray items = new JSONArray(response);
                    int lost = 0;
                    int found = 0;
                    for (int index = 0; index < items.length(); index++) {
                        JSONObject item = items.getJSONObject(index);
                        String type = item.optString("type", "ITEM");
                        if ("FOUND".equals(type)) {
                            found++;
                            if (firstFoundId[0] == null) firstFoundId[0] = item.optString("id", null);
                        } else {
                            lost++;
                        }
                    }
                    addField(activeContent, reportRow("Lost reports", lost + " live requests"));
                    addField(activeContent, reportRow("Found reports", found + " live items"));
                } catch (Exception error) {
                    loading.setText("Admin data could not be read.");
                }
            });
        }));
    }

    private String fetchAdminItems(String idToken) {
        return getAuthorized("/api/admin/items", idToken);
    }

    private String fetchAdminMatches(String foundItemId, String idToken) {
        return getAuthorized("/api/admin/matches/" + foundItemId, idToken);
    }

    private String getAuthorized(String path, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return null;
            return readStream(connection.getInputStream());
        } catch (Exception error) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(null, style);
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private void applySystemBarColors() {
        if (Build.VERSION.SDK_INT >= 29) getWindow().getDecorView().setForceDarkAllowed(false);
        getWindow().setStatusBarColor(backgroundColor());
        getWindow().setNavigationBarColor(backgroundColor());
        getWindow().getDecorView().setSystemUiVisibility(darkMode ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private int backgroundColor() {
        return darkMode ? BACKGROUND : Color.rgb(248, 246, 238);
    }

    private int surfaceColor() {
        return darkMode ? SURFACE : Color.WHITE;
    }

    private int accentColor() {
        return darkMode ? GOLD : Color.rgb(139, 101, 12);
    }

    private int secondaryTextColor() {
        return darkMode ? SILVER : Color.rgb(28, 34, 30);
    }

    private void showLanguagePicker() {
        String[] languages = {"English", "हिन्दी", "मराठी", "اردو", "ಕನ್ನಡ", "తెలుగు", "বাংলা", "മലയാളം"};
        new AlertDialog.Builder(this)
                .setTitle(localized("language"))
                .setSingleChoiceItems(languages, selectedLanguage, (dialog, which) -> {
                    selectedLanguage = which;
                    languagePreferences.edit().putInt("selected_language_index", which).apply();
                    dialog.dismiss();
                    buildScreen();
                })
                .show();
    }

    private String localized(String key) {
        String[][] values = {
                {"Lost & Found across India", "Help is right here—get started.", "Create my profile", "Login with PIN", "Language"},
                {"भारत में खोया और पाया", "मदद यहीं है—शुरू करें।", "मेरी प्रोफ़ाइल बनाएं", "पिन से लॉगिन", "भाषा"},
                {"संपूर्ण भारतातील हरवलेले आणि सापडलेले", "मदत इथेच आहे—सुरुवात करा.", "माझे प्रोफाइल तयार करा", "पिनने लॉगिन करा", "भाषा"},
                {"پورے ہندوستان میں گمشدہ اور ملی اشیاء", "مدد یہیں ہے—شروع کریں۔", "میرا پروفائل بنائیں", "پن سے لاگ ان", "زبان"},
                {"ಭಾರತದಾದ್ಯಂತ ಕಳೆದುಹೋದ ಮತ್ತು ಸಿಕ್ಕ ವಸ್ತುಗಳು", "ಸಹಾಯ ಇಲ್ಲಿದೆ—ಪ್ರಾರಂಭಿಸಿ.", "ನನ್ನ ಪ್ರೊಫೈಲ್ ರಚಿಸಿ", "ಪಿನ್ ಮೂಲಕ ಲಾಗಿನ್", "ಭಾಷೆ"},
                {"భారతదేశం అంతటా పోయినవి మరియు దొరికినవి", "సహాయం ఇక్కడే ఉంది—ప్రారంభించండి.", "నా ప్రొఫైల్ సృష్టించండి", "పిన్‌తో లాగిన్", "భాష"},
                {"ভারত জুড়ে হারানো এবং পাওয়া", "সাহায্য এখানেই—শুরু করুন।", "আমার প্রোফাইল তৈরি করুন", "পিন দিয়ে লগইন", "ভাষা"},
                {"ഇന്ത്യയിലുടനീളം നഷ്ടപ്പെട്ടതും കണ്ടെത്തിയതും", "സഹായം ഇവിടെയുണ്ട്—തുടങ്ങാം.", "എന്റെ പ്രൊഫൈൽ സൃഷ്ടിക്കുക", "പിൻ ഉപയോഗിച്ച് ലോഗിൻ", "ഭാഷ"}
        };
        int language = Math.max(0, Math.min(selectedLanguage, values.length - 1));
        if ("tagline".equals(key)) return values[language][0];
        if ("status".equals(key)) return values[language][1];
        if ("create_profile".equals(key)) return values[language][2];
        if ("pin_login".equals(key)) return values[language][3];
        return values[language][4];
    }

    private GradientDrawable goldButton() {
        GradientDrawable button = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.rgb(169, 129, 31), GOLD, GOLD});
        button.setCornerRadius(28);
        button.setStroke(3, Color.rgb(255, 237, 157));
        return button;
    }

    private GradientDrawable outlineButton() {
        GradientDrawable button = new GradientDrawable();
        button.setColor(surfaceColor());
        button.setCornerRadius(28);
        button.setStroke(3, darkMode ? Color.rgb(128, 116, 75) : Color.rgb(166, 137, 64));
        return button;
    }
}