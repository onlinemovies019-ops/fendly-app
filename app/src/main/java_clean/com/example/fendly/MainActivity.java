package com.example.fendly;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Space;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.app.DatePickerDialog;

import java.util.Arrays;
import java.util.Calendar;
import android.os.Handler;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.SecureRandom;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.GetTokenResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.graphics.drawable.GradientDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import com.example.fendly.notifications.FcmRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.razorpay.Checkout;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.location.Location;
import android.location.LocationManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class MainActivity extends Activity implements PaymentResultWithDataListener {
    private static final int BACKGROUND = Color.rgb(18, 19, 25);
    private static final int SURFACE = Color.rgb(27, 28, 36);
    private static final int BORDER = Color.rgb(38, 39, 47);
    private static final int FIELD_BORDER = Color.rgb(42, 44, 54);
    private static final int GOLD = Color.rgb(232, 178, 74);
    private static final int LIGHT_TEXT = Color.BLACK;
    private static final int GOLD_ON = Color.rgb(43, 29, 5);
    private static final int LOST_GREEN = Color.rgb(11, 93, 69);
    private static final int LOST_GREEN_ON = Color.rgb(220, 239, 231);
    private static final int FOUND_GOLD = Color.rgb(201, 162, 76);
    private static final int FOUND_GOLD_ON = Color.rgb(51, 35, 5);
    private static final int TEXT_PRIMARY = Color.rgb(245, 243, 238);
    private static final int TEXT_MUTED = Color.rgb(110, 112, 128);
    private static final int TEXT_PRIMARY_LIGHT = Color.rgb(18, 18, 18);
    private static final int SILVER = Color.rgb(220, 218, 211);
    private TextView status;
    private SharedPreferences languagePreferences;
    private int selectedLanguage;
    private boolean darkMode;
    /** Re-renders the screen that is currently visible after a setting changes. */
    private Runnable screenRenderer;
    private LinearLayout activeContent;
    private Uri selectedImage;
    private Bitmap capturedImage;
    private final Uri[] reportImages = new Uri[3];
    private final Bitmap[] reportCameraImages = new Bitmap[3];
    private int pendingImageSlot = -1;
    private static final int REQUEST_IMAGE_PERMISSION = 704;
    private static final int REQUEST_CAMERA_PERMISSION = 705;
    private double currentLat;
    private double currentLng;
    private boolean hasLocation;
    private TextView locationStatus;
    private TextView locationToggleStatus;
    private String phoneVerificationId;
    private boolean phoneVerificationHandled;
    private String currentReportType;
    private Uri selectedProfileImage;
    private Bitmap capturedProfileImage;
    private static final int REQUEST_PROFILE_IMAGE = 706;
    private static final int REQUEST_PROFILE_CAMERA = 707;
    private static final int REQUEST_IMEI_SCAN = 708;
    private String draftItem = "";
    private String draftDescription = "";
    private String draftLocation = "";
    private String draftDate = "";
    private String draftImei = "";
    private String draftFullName = "";
    private String draftEmail = "";
    private String draftMobile = "";
    private String draftPin = "";
    private String draftState = "";
    private String draftCity = "";
    private String editingReportId;
    private String editingReportType;
    private String editingReportImageUrl;
    private EditText pendingPaymentItem;
    private EditText pendingPaymentDescription;
    private EditText pendingPaymentImei;
    private EditText pendingPaymentLocation;
    private EditText pendingPaymentDate;
    private TextView pendingPaymentButton;
    private EditText imeiScanTarget;
    private boolean inRenewalPaymentFlow;
    private int currentPage;
    private static final int PAGE_AUTH = 0;
    private static final int PAGE_HOME = 1;
    private static final int PAGE_REPORT = 2;
    private static final int PAGE_REPORTS = 3;
    private static final int PAGE_PROFILE = 4;
    private static final int PAGE_PROFILE_SETUP = 5;
    private static final int PAGE_SUBSCRIPTION = 6;
    private static final int PAGE_ADMIN = 7;
    private Handler adminPressHandler = new Handler();
    private boolean accountCreated;
    private boolean profileSetupVisible;
    private static final String API_BASE = "https://fendly-api.onrender.com";
    private final ExecutorService network = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean savedDarkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setTheme(savedDarkMode ? R.style.Theme_Fendly_Dark : R.style.Theme_Fendly);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        languagePreferences = getSharedPreferences("fendly_language", MODE_PRIVATE);
        selectedLanguage = languagePreferences.getInt("selected_language_index", 0);
        darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE).getBoolean("dark_mode", false);
        accountCreated = getSharedPreferences("fendly_account", MODE_PRIVATE).getBoolean("created", false);
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("state_current_page", PAGE_AUTH);
            currentReportType = savedInstanceState.getString("state_report_type", null);
            draftItem = savedInstanceState.getString("state_draft_item", "");
            draftDescription = savedInstanceState.getString("state_draft_description", "");
            draftLocation = savedInstanceState.getString("state_draft_location", "");
            draftDate = savedInstanceState.getString("state_draft_date", "");
            draftImei = savedInstanceState.getString("state_draft_imei", "");
            draftFullName = savedInstanceState.getString("state_draft_full_name", "");
            draftEmail = savedInstanceState.getString("state_draft_email", "");
            draftMobile = savedInstanceState.getString("state_draft_mobile", "");
            draftPin = savedInstanceState.getString("state_draft_pin", "");
            draftState = savedInstanceState.getString("state_draft_state", "");
            draftCity = savedInstanceState.getString("state_draft_city", "");
            profileSetupVisible = savedInstanceState.getBoolean("state_profile_setup_visible", false);
            inRenewalPaymentFlow = savedInstanceState.getBoolean("state_in_renewal_payment_flow", false);
        }
        applySystemBarColors();
        buildScreen();
        restoreScreenState();
        FcmRegistration.registerCurrentToken();
        syncProfileWithBackend();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((currentPage == PAGE_PROFILE || currentPage == PAGE_PROFILE_SETUP) && screenRenderer != null) {
            screenRenderer.run();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("state_current_page", currentPage);
        outState.putString("state_report_type", currentReportType);
        outState.putString("state_draft_item", draftItem);
        outState.putString("state_draft_description", draftDescription);
        outState.putString("state_draft_location", draftLocation);
        outState.putString("state_draft_date", draftDate);
        outState.putString("state_draft_imei", draftImei);
        outState.putString("state_draft_full_name", draftFullName);
        outState.putString("state_draft_email", draftEmail);
        outState.putString("state_draft_mobile", draftMobile);
        outState.putString("state_draft_pin", draftPin);
        outState.putString("state_draft_state", draftState);
        outState.putString("state_draft_city", draftCity);
        outState.putBoolean("state_profile_setup_visible", profileSetupVisible);
        outState.putBoolean("state_in_renewal_payment_flow", inRenewalPaymentFlow);
    }

    private void restoreScreenState() {
        if (currentPage == PAGE_HOME) {
            showHome();
            return;
        }
        if (currentPage == PAGE_REPORT) {
            if (currentReportType != null) {
                showReport(currentReportType);
            } else {
                showHome();
            }
            return;
        }
        if (currentPage == PAGE_REPORTS) {
            showReports();
            return;
        }
        if (currentPage == PAGE_PROFILE) {
            showProfile();
            return;
        }
        if (currentPage == PAGE_PROFILE_SETUP) {
            showProfileSetup();
            return;
        }
        if (currentPage == PAGE_SUBSCRIPTION) {
            if (inRenewalPaymentFlow) {
                showProfile();
            } else if (currentReportType != null) {
                showReport(currentReportType);
            } else {
                showHome();
            }
            return;
        }
        if (currentPage == PAGE_ADMIN) {
            showAdminLoginDialog();
            return;
        }
        buildScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (screenRenderer != null) {
            screenRenderer.run();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPage == PAGE_ADMIN) {
            buildScreen();
            return;
        }
        if (currentPage == PAGE_PROFILE_SETUP || profileSetupVisible) {
            profileSetupVisible = false;
            buildScreen();
            return;
        }
        if (currentPage == PAGE_SUBSCRIPTION) {
            if (inRenewalPaymentFlow) {
                showProfile();
                return;
            }
            showReport("LOST");
            return;
        }
        if (currentPage == PAGE_REPORT || currentPage == PAGE_REPORTS || currentPage == PAGE_PROFILE) {
            showHome();
            return;
        }
        if (currentPage == PAGE_HOME) {
            buildScreen();
            return;
        }
        super.onBackPressed();
    }

    private int responsiveHorizontalPadding() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        if (screenWidth >= 1200) return dp(44);
        if (screenWidth >= 900) return dp(32);
        if (screenWidth >= 600) return dp(24);
        return dp(18);
    }

    private boolean isLargeScreen() {
        int smallestWidth = getResources().getConfiguration().smallestScreenWidthDp;
        return smallestWidth >= 600 || getResources().getDisplayMetrics().widthPixels >= 900;
    }

    private void buildScreen() {
        currentPage = PAGE_AUTH;
        profileSetupVisible = false;
        screenRenderer = this::buildScreen;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(responsiveHorizontalPadding(), dp(16), responsiveHorizontalPadding(), dp(30));
        root.setBackgroundColor(backgroundColor());
        if (Build.VERSION.SDK_INT >= 29) root.setForceDarkAllowed(false);

        addAppControls(root, true);

        LinearLayout centerArea = new LinearLayout(this);
        centerArea.setOrientation(LinearLayout.VERTICAL);
        centerArea.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        if (Build.VERSION.SDK_INT >= 29) centerArea.setForceDarkAllowed(false);
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        ScrollView authScroll = new ScrollView(this);
        authScroll.setFillViewport(true);
        authScroll.addView(centerArea, new ScrollView.LayoutParams(-1, -2));
        root.addView(authScroll, centerParams);

        ImageView standaloneF = new ImageView(this);
        standaloneF.setImageResource(R.drawable.fendly_logo);
        standaloneF.setContentDescription("Fendly F icon");
        standaloneF.setScaleType(ImageView.ScaleType.FIT_CENTER);
        standaloneF.setAdjustViewBounds(true);
        LinearLayout.LayoutParams standaloneFParams = new LinearLayout.LayoutParams(dp(140), dp(100));
        standaloneFParams.gravity = Gravity.CENTER_HORIZONTAL;
        standaloneFParams.setMargins(0, 0, 0, dp(8));
        centerArea.addView(standaloneF, standaloneFParams);

        LinearLayout authCard = new LinearLayout(this);
        authCard.setOrientation(LinearLayout.VERTICAL);
        authCard.setPadding(dp(18), dp(16), dp(18), dp(18));
        authCard.setBackground(roundWithStroke(surfaceColor(), 30, borderColor()));
        authCard.setElevation(dp(16));
        authCard.setClipToOutline(true);
        if (Build.VERSION.SDK_INT >= 29) authCard.setForceDarkAllowed(false);

        LinearLayout emblemWrap = new LinearLayout(this);
        emblemWrap.setGravity(Gravity.CENTER);
        emblemWrap.setPadding(dp(10), dp(10), dp(10), dp(10));
        emblemWrap.setBackground(roundWithStroke(Color.argb(30, 232, 178, 74), 22, Color.argb(80, 232, 178, 74)));

        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.logo_final);
        emblem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        emblem.setAdjustViewBounds(true);
        emblem.setContentDescription("Fendly emblem");
        emblem.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                adminPressHandler.postDelayed(() -> showAdminLoginDialog(), 5000);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                adminPressHandler.removeCallbacksAndMessages(null);
                return true;
            }
            return true;
        });
        LinearLayout.LayoutParams emblemParams = new LinearLayout.LayoutParams(dp(180), dp(124));
        emblemParams.gravity = Gravity.CENTER_HORIZONTAL;
        emblemParams.setMargins(0, dp(4), 0, dp(10));
        emblemWrap.addView(emblem, emblemParams);
        LinearLayout.LayoutParams emblemWrapParams = new LinearLayout.LayoutParams(-1, dp(150));
        emblemWrapParams.setMargins(0, 0, 0, dp(14));
        authCard.addView(emblemWrap, emblemWrapParams);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, dp(10), 0, 0);
        centerArea.addView(authCard, cardParams);

        LinearLayout userLabelRow = new LinearLayout(this);
        if (Build.VERSION.SDK_INT >= 29) userLabelRow.setForceDarkAllowed(false);
        userLabelRow.setGravity(Gravity.CENTER_VERTICAL);
        userLabelRow.setAlpha(1f);
        userLabelRow.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ImageView userIcon = new ImageView(this);
        userIcon.setImageResource(R.drawable.ic_field_person);
        userIcon.setColorFilter(accentColor());
        LinearLayout.LayoutParams userIconParams = new LinearLayout.LayoutParams(dp(18), dp(18));
        userIconParams.setMargins(0, 0, dp(8), 0);
        userLabelRow.addView(userIcon, userIconParams);
        View usernameLabel = new AuthLabelView(this, loginText("username"));
        userLabelRow.addView(usernameLabel, new LinearLayout.LayoutParams(dp(160), dp(30)));
        LinearLayout.LayoutParams usernameLabelParams = new LinearLayout.LayoutParams(-1, dp(30));
        usernameLabelParams.setMargins(0, 0, 0, dp(8));
        authCard.addView(userLabelRow, usernameLabelParams);

        EditText username = field("");
        if (Build.VERSION.SDK_INT >= 29) username.setForceDarkAllowed(false);
        username.setTextColor(darkMode ? primaryTextColor() : LIGHT_TEXT);
        username.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        username.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        authCard.addView(username, new LinearLayout.LayoutParams(-1, dp(44)));

        LinearLayout pinLabelRow = new LinearLayout(this);
        if (Build.VERSION.SDK_INT >= 29) pinLabelRow.setForceDarkAllowed(false);
        pinLabelRow.setGravity(Gravity.CENTER_VERTICAL);
        pinLabelRow.setAlpha(1f);
        pinLabelRow.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ImageView pinIcon = new ImageView(this);
        pinIcon.setImageResource(R.drawable.ic_field_lock);
        pinIcon.setColorFilter(accentColor());
        LinearLayout.LayoutParams pinIconParams = new LinearLayout.LayoutParams(dp(18), dp(18));
        pinIconParams.setMargins(0, 0, dp(8), 0);
        pinLabelRow.addView(pinIcon, pinIconParams);
        View pinLabel = new AuthLabelView(this, loginText("pin"));
        pinLabelRow.addView(pinLabel, new LinearLayout.LayoutParams(dp(160), dp(30)));
        LinearLayout.LayoutParams pinLabelParams = new LinearLayout.LayoutParams(-1, dp(30));
        pinLabelParams.setMargins(0, dp(12), 0, dp(8));
        authCard.addView(pinLabelRow, pinLabelParams);
        EditText[] pinCells = pinCells();
        username.setNextFocusForwardId(pinCells[0].getId());
        pinCells[0].setNextFocusForwardId(pinCells[1].getId());
        pinCells[1].setNextFocusForwardId(pinCells[2].getId());
        pinCells[2].setNextFocusForwardId(pinCells[3].getId());
        LinearLayout pinRow = new LinearLayout(this);
        pinRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int index = 0; index < pinCells.length; index++) {
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
            if (index > 0) cellParams.setMargins(dp(8), 0, 0, 0);
            pinRow.addView(pinCells[index], cellParams);
        }
        ImageButton forgotPin = new ImageButton(this);
        forgotPin.setImageResource(R.drawable.ic_field_key);
        forgotPin.setContentDescription("Forgot PIN");
        forgotPin.setColorFilter(accentColor());
        forgotPin.setBackgroundColor(Color.TRANSPARENT);
        forgotPin.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        forgotPin.setPadding(dp(8), dp(8), dp(8), dp(8));
        forgotPin.setOnClickListener(view -> showForgotPinDialog(username));
        LinearLayout.LayoutParams forgotPinParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        forgotPinParams.setMargins(dp(8), 0, 0, 0);
        pinRow.addView(forgotPin, forgotPinParams);
        LinearLayout.LayoutParams pinParams = new LinearLayout.LayoutParams(-1, dp(44));
        pinParams.setMargins(0, 0, 0, dp(16));
        authCard.addView(pinRow, pinParams);

        TextView login = text(loginText("login"), 13, GOLD_ON, Typeface.NORMAL);
        login.setGravity(Gravity.CENTER);
        login.setPadding(dp(12), dp(8), dp(12), dp(8));
        login.setBackground(goldButton());
        login.setElevation(dp(8));
        login.setOnClickListener(view -> {
            String name = username.getText().toString().trim();
            String code = pinValue(pinCells);
            if (name.length() < 3 || code.length() != 4) {
                Toast.makeText(this, "Enter a valid username and 4-digit PIN", Toast.LENGTH_LONG).show();
                return;
            }
            login.setText("Signing in...");
            login.setEnabled(false);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(credentialEmail(name), credentialPassword(name, code))
                    .addOnSuccessListener(result -> {
                        accountCreated = true;
                        saveStoredAccountPin(code);
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", name).apply();
                        showHome();
                    })
                    .addOnFailureListener(error -> {
                        login.setText(loginText("login"));
                        login.setEnabled(true);
                        Toast.makeText(this, "Incorrect username or PIN", Toast.LENGTH_LONG).show();
                    });
        });
        authCard.addView(login, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView createAccount = text(loginText("create"), 12, Color.BLACK, Typeface.BOLD);
        createAccount.setGravity(Gravity.CENTER);
        createAccount.setIncludeFontPadding(true);
        createAccount.setMaxLines(2);
        createAccount.setEllipsize(null);
        createAccount.setPadding(dp(4), dp(6), dp(4), dp(6));
        if (Build.VERSION.SDK_INT >= 29) createAccount.setForceDarkAllowed(false);
        createAccount.setTextColor(darkMode ? primaryTextColor() : LIGHT_TEXT);
        createAccount.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        createAccount.setOnClickListener(view -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                FcmRegistration.registerCurrentToken();
                showProfileSetup();
                return;
            }
            auth.signInAnonymously()
                    .addOnSuccessListener(result -> {
                        FcmRegistration.registerCurrentToken();
                        showProfileSetup();
                    })
                    .addOnFailureListener(error -> Toast.makeText(this,
                            "Account setup unavailable: " + error.getMessage(), Toast.LENGTH_LONG).show());
        });
        authCard.addView(createAccount, new LinearLayout.LayoutParams(-1, dp(58)));

        setContentView(root);
        if (!darkMode) {
            forceLightModeText(root);
            root.post(() -> forceLightModeText(root));
            root.postDelayed(() -> forceLightModeText(root), 100);
        }
        root.postDelayed(() -> {
            int authTextColor = darkMode ? Color.WHITE : LIGHT_TEXT;
            username.setTextColor(authTextColor);
            for (EditText pinCell : pinCells) pinCell.setTextColor(authTextColor);
            createAccount.setTextColor(authTextColor);
        }, 150);
    }

    private boolean isPhoneVerified() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null && auth.getCurrentUser().getPhoneNumber() != null && !auth.getCurrentUser().getPhoneNumber().trim().isEmpty();
    }

    private void saveStoredAccountPin(String pin) {
        getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putString("account_pin", pin).apply();
    }

    private String getStoredAccountPin() {
        return getSharedPreferences("fendly_account", MODE_PRIVATE).getString("account_pin", "");
    }

    private void showProfileSetup() {
        currentPage = PAGE_PROFILE_SETUP;
        profileSetupVisible = true;
        screenRenderer = this::showProfileSetup;
        LinearLayout root = screenBase("");
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);

        TextView title = text("Complete your profile", 15, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        root.addView(title, contentParams(-1, dp(22), 0));
        TextView usernameText = text(accountCreated ? account.getString("username", "") : "Your Fendly account", 11, secondaryTextColor(), Typeface.NORMAL);
        usernameText.setGravity(Gravity.CENTER);
        root.addView(usernameText, contentParams(-1, dp(22), dp(16)));

        LinearLayout photoSection = new LinearLayout(this);
        photoSection.setOrientation(LinearLayout.VERTICAL);
        photoSection.setGravity(Gravity.CENTER_HORIZONTAL);
        photoSection.setPadding(0, dp(8), 0, dp(14));
        FrameLayout avatarWrap = new FrameLayout(this);
        avatarWrap.setBackground(roundWithStroke(surfaceColor(), 60, borderColor()));
        avatarWrap.setOnClickListener(view -> showProfilePhotoOptions());
        if (Build.VERSION.SDK_INT >= 21) avatarWrap.setClipToOutline(true);

        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setBackground(roundWithStroke(surfaceColor(), 60, fieldBorderColor()));
        avatar.setLayoutParams(new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER));
        avatar.setImageResource(R.drawable.ic_field_person);
        avatar.setColorFilter(accentColor());
        if (Build.VERSION.SDK_INT >= 21) avatar.setClipToOutline(true);

        String savedProfileUri = account.getString("profile_image_uri", null);
        if (savedProfileUri != null && !savedProfileUri.trim().isEmpty()
                && setImageFromUri(avatar, Uri.parse(savedProfileUri))) {
            avatar.clearColorFilter();
        }
        if (selectedProfileImage != null) {
            if (setImageFromUri(avatar, selectedProfileImage)) avatar.clearColorFilter();
        }
        if (capturedProfileImage != null) {
            avatar.clearColorFilter();
            avatar.setImageBitmap(capturedProfileImage);
        }

        avatarWrap.addView(avatar, new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER));
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(dp(118), dp(118));
        avatarLayout.gravity = Gravity.CENTER_HORIZONTAL;
        photoSection.addView(avatarWrap, avatarLayout);

        TextView photoHint = text("Tap to upload photo", 10, secondaryTextColor(), Typeface.NORMAL);
        photoHint.setGravity(Gravity.CENTER);
        photoSection.addView(photoHint, contentParams(-1, dp(20), dp(10)));
        root.addView(photoSection);

        EditText firstName = field("First name");
        EditText surname = field("Surname");
        String savedFullName = account.getString("full_name", draftFullName).trim();
        String[] nameParts = savedFullName.split("\\s+", 2);
        firstName.setText(nameParts.length > 0 ? nameParts[0] : draftFullName);
        surname.setText(nameParts.length > 1 ? nameParts[1] : "");
        firstName.addTextChangedListener(draftWatcher(value -> draftFullName = value));
        surname.addTextChangedListener(draftWatcher(value -> draftFullName = firstName.getText().toString().trim() + (value == null || value.trim().isEmpty() ? "" : " " + value.trim())));

        EditText email = field("Email address");
        EditText mobile = field("Mobile number");
        email.setText(account.getString("email", draftEmail));
        mobile.setText(account.getString("mobile", draftMobile));

        EditText[] pinCells = pinCells();
        if (!darkMode) {
            for (EditText pinCell : pinCells) pinCell.setTextColor(darkMode ? Color.WHITE : LIGHT_TEXT);
        }
        for (int index = 0; index < pinCells.length && index < draftPin.length(); index++) {
            pinCells[index].setText(String.valueOf(draftPin.charAt(index)));
        }

        email.addTextChangedListener(draftWatcher(value -> draftEmail = value));
        mobile.addTextChangedListener(draftWatcher(value -> draftMobile = value));
        for (EditText pinCell : pinCells) {
            pinCell.addTextChangedListener(draftWatcher(value -> draftPin = pinValue(pinCells)));
        }

        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
        mobile.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(10),
                (source, start, end, destination, destinationStart, destinationEnd) -> {
                    StringBuilder digits = new StringBuilder();
                    for (int index = start; index < end; index++) {
                        char character = source.charAt(index);
                        if (Character.isDigit(character)) digits.append(character);
                    }
                    return digits.toString();
                }
        });
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        email.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String emailValue = email.getText().toString().trim();
                if (!emailValue.isEmpty() && !validEmail(emailValue)) {
                    email.setError("Email invalid");
                } else {
                    email.setError(null);
                }
            }
        });

        mobile.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String mobileValue = mobile.getText().toString().trim();
                if (!mobileValue.isEmpty() && !mobileValue.matches("^\\d{10}$")) {
                    mobile.setError("Mobile invalid");
                } else {
                    mobile.setError(null);
                }
            }
        });

        EditText usernameField = field("Username for login");
        usernameField.setText(account.getString("username", ""));
        usernameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        usernameField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        root.addView(labeledField("Username", usernameField), contentParams(-1, dp(76), dp(4)));

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.addView(labeledField("First name", firstName), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams surnameParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        surnameParams.setMargins(dp(8), 0, 0, 0);
        nameRow.addView(labeledField("Surname", surname), surnameParams);
        root.addView(nameRow, contentParams(-1, dp(76), dp(4)));

        TextView emailVerify = filledButton("Verify", GOLD, GOLD_ON);
        emailVerify.setPadding(dp(12), 0, dp(12), 0);
        emailVerify.setOnClickListener(view -> {
            String emailValue = email.getText().toString().trim();
            if (!validEmail(emailValue)) {
                email.setError("Email invalid");
                email.requestFocus();
                return;
            }
            emailVerify.setEnabled(false);
            emailVerify.setText("Sending...");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                emailVerify.setText("Verify");
                emailVerify.setEnabled(true);
                Toast.makeText(this, "Sign in to verify your email", Toast.LENGTH_LONG).show();
                return;
            }
                FirebaseUser user = auth.getCurrentUser();
                com.google.android.gms.tasks.Task<Void> verificationTask = emailValue.equalsIgnoreCase(user.getEmail() == null ? "" : user.getEmail().trim())
                    ? user.sendEmailVerification()
                    : user.verifyBeforeUpdateEmail(emailValue);
                verificationTask
                    .addOnSuccessListener(result -> {
                        account.edit().putString("email", emailValue).apply();
                        emailVerify.setText("Sent");
                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(error -> {
                        emailVerify.setText("Verify");
                        emailVerify.setEnabled(true);
                        Toast.makeText(this, "Could not send verification email: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
        refreshEmailVerificationState(email, emailVerify);

        TextView mobileVerify = filledButton("Verify OTP", GOLD, GOLD_ON);
        mobileVerify.setPadding(dp(10), 0, dp(10), 0);
        mobileVerify.setOnClickListener(view -> verifyProfileMobile(mobile, mobileVerify));
        String verifiedMobile = account.getString("mobile", "").trim();
        boolean mobileAlreadyVerified = account.getBoolean("mobile_verified", false)
                && verifiedMobile.equals(mobile.getText().toString().trim());
        if (mobileAlreadyVerified) {
            mobileVerify.setText("Verified");
            mobileVerify.setEnabled(false);
        }
        mobile.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                if (!verifiedMobile.equals(value.toString().trim())) {
                    mobileVerify.setText("Verify OTP");
                    mobileVerify.setEnabled(true);
                }
            }
            @Override public void afterTextChanged(Editable value) { }
        });

        Map<String, String[]> stateCities = indiaStateCityMap();
        String[] states = stateCities.keySet().toArray(new String[0]);
        Arrays.sort(states, 1, states.length);
        AutoCompleteTextView stateSearch = new AutoCompleteTextView(this);
        AutoCompleteTextView citySearch = new AutoCompleteTextView(this);
        stateSearch.setText(account.getString("state", draftState));
        citySearch.setText(account.getString("city", draftCity));
        stateSearch.setHint("");
        citySearch.setHint("");
        stateSearch.setSingleLine(true);
        citySearch.setSingleLine(true);
        stateSearch.setInputType(InputType.TYPE_NULL);
        citySearch.setInputType(InputType.TYPE_NULL);
        stateSearch.setFocusable(false);
        citySearch.setFocusable(false);
        citySearch.setEnabled(false);

        stateSearch.setOnClickListener(clickedView -> showStatePicker(states, stateSearch, citySearch, stateCities));
        stateSearch.addTextChangedListener(draftWatcher(value -> draftState = value));
        citySearch.addTextChangedListener(draftWatcher(value -> draftCity = value));
        String[] draftCities = stateCities.getOrDefault(stateSearch.getText().toString().trim(), new String[0]);
        citySearch.setEnabled(draftCities.length > 0 && !"Select city".equals(draftCities[0]));
        citySearch.setOnClickListener(clickedView -> {
            String selectedState = stateSearch.getText().toString().trim();
            String[] cities = stateCities.getOrDefault(selectedState, new String[]{"Select city"});
            if (citySearch.isEnabled()) showCityPicker(selectedState, cities, citySearch);
        });

        LinearLayout locationRow = new LinearLayout(this);
        locationRow.setOrientation(LinearLayout.HORIZONTAL);
        locationRow.addView(labeledCitySearch("State", stateSearch), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams cityParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        cityParams.setMargins(dp(8), 0, 0, 0);
        locationRow.addView(labeledCitySearch("City", citySearch), cityParams);
        root.addView(locationRow, contentParams(-1, dp(76), dp(4)));

        addEditableProfileField(root, "Email", email, null);
        addEditableProfileField(root, "Mobile", mobile, mobileVerify);
        addLabeledPinField(root, "4-digit PIN", pinCells);

        TextView save = actionButton("Complete", true);
        save.setOnClickListener(view -> {
            String updatedFirstName = firstName.getText().toString().trim();
            String updatedSurname = surname.getText().toString().trim();
            String fullName = (updatedFirstName + " " + updatedSurname).trim();
            String usernameValue = usernameField.getText().toString().trim().toLowerCase(Locale.US);
            String mobileValue = mobile.getText().toString().trim();
            String emailValue = email.getText().toString().trim();
            String pinValue = pinValue(pinCells);
            String selectedState = stateSearch.getText().toString().trim();
            String selectedCity = citySearch.getText().toString().trim();
            String[] selectedStateCities = stateCities.getOrDefault(selectedState, new String[0]);
            boolean cityMatchesState = Arrays.asList(selectedStateCities).contains(selectedCity);

            if (fullName.isEmpty()) {
                firstName.setError("Enter first name");
                firstName.requestFocus();
            } else if (!usernameValue.matches("^[a-z0-9_]{3,32}$")) {
                usernameField.setError("Use 3-32 letters, numbers, or underscores");
                usernameField.requestFocus();
            } else if (!mobileValue.matches("^\\d{10}$")) {
                mobile.setError("Enter valid 10-digit mobile number");
                mobile.requestFocus();
            } else if (!validEmail(emailValue)) {
                email.setError("Enter a valid email like Gmail, Yahoo, Hotmail, Outlook, etc.");
                email.requestFocus();
            } else if (!pinValue.matches("^\\d{4}$")) {
                Toast.makeText(this, "Create a valid 4-digit PIN", Toast.LENGTH_LONG).show();
                pinCells[0].requestFocus();
            } else if (selectedState == null || selectedState.trim().isEmpty() || "Select state".equals(selectedState) || selectedCity.isEmpty() || "Select city".equals(selectedCity) || !cityMatchesState) {
                Toast.makeText(this, "Please select your state and city", Toast.LENGTH_LONG).show();
                stateSearch.requestFocus();
            } else {
                accountCreated = true;
                account.edit()
                        .putBoolean("created", true)
                        .putString("username", usernameValue)
                        .putString("full_name", fullName)
                        .putString("email", emailValue)
                        .putString("mobile", mobileValue)
                        .putString("state", selectedState)
                        .putString("city", selectedCity)
                        .putBoolean("mobile_verified", isPhoneVerified())
                        .apply();

                save.setText("Creating account...");
                save.setEnabled(false);
                if (isPhoneVerified()) {
                    saveFirebaseCredential(usernameValue, pinValue, save);
                } else {
                    startPhoneVerification(mobileValue, usernameValue, pinValue, save);
                }
            }
        });
        root.addView(save, contentParams(-1, dp(44), 0));
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
                    public void onVerificationFailed(FirebaseException error) {
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Verify your mobile")
                .setMessage("Enter the code sent to your mobile number.")
                .setView(code)
                .setPositiveButton("Verify", (dialogButton, which) -> {
                    if (phoneVerificationId == null || code.getText().toString().trim().length() != 6) {
                        save.setText("Invalid SMS code");
                        save.setEnabled(true);
                        return;
                    }
                    completePhoneVerification(PhoneAuthProvider.getCredential(phoneVerificationId, code.getText().toString().trim()), username, pin, save);
                })
                .setNegativeButton("Cancel", (dialogButton, which) -> {
                    save.setText("Save and continue");
                    save.setEnabled(true);
                })
                .create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
    }

    private void completePhoneVerification(PhoneAuthCredential credential, String username, String pin, TextView save) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                .addOnSuccessListener(result -> {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("mobile_verified", true).apply();
                    finishProfileSetup(username, pin, save);
                })
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
        saveStoredAccountPin(pin);
        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(EmailAuthProvider.getCredential(email, password))
                .addOnSuccessListener(result -> {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", username).apply();
                    accountCreated = true;
                    syncProfileWithBackend();
                    showHome();
                })
                .addOnFailureListener(error -> {
                    save.setText("Could not secure account");
                    save.setEnabled(true);
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void changeAccountPin(String username, String currentPin, String newPin, TextView saveButton) {
        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Account username not found", Toast.LENGTH_LONG).show();
            return;
        }
        if (newPin == null || !newPin.matches("^\\d{4}$")) {
            Toast.makeText(this, "Enter a valid 4-digit PIN", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_LONG).show();
            return;
        }
        String oldPassword = credentialPassword(username, currentPin);
        String newPassword = credentialPassword(username, newPin);
        saveButton.setText("Updating...");
        saveButton.setEnabled(false);

        auth.getCurrentUser().reauthenticate(EmailAuthProvider.getCredential(credentialEmail(username), oldPassword))
                .addOnSuccessListener(result -> auth.getCurrentUser().updatePassword(newPassword)
                        .addOnSuccessListener(updated -> {
                            saveStoredAccountPin(newPin);
                            saveButton.setText("Updated");
                            Toast.makeText(this, "PIN updated successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(error -> {
                            saveButton.setText("Change PIN");
                            saveButton.setEnabled(true);
                            Toast.makeText(this, "Could not update PIN: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }))
                .addOnFailureListener(error -> {
                    saveButton.setText("Change PIN");
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Current PIN is incorrect", Toast.LENGTH_LONG).show();
                });
    }

    private String credentialEmail(String username) {
        return username.trim().toLowerCase(Locale.US) + "@login.fendly.app";
    }

    private String credentialPassword(String username, String pin) {
        return "Fendly!" + username.trim().toLowerCase(Locale.US) + "#" + pin;
    }

    private void showPinLogin() {
        screenRenderer = this::showPinLogin;
        LinearLayout root = screenBase("Login with PIN");
        addHeading("Welcome back.", "Use the username and PIN from your Fendly profile.");
        EditText username = field("Username");
        EditText pin = field("4-digit PIN");
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pin.setPadding(dp(16), dp(8), dp(56), dp(8));
        addField(root, username);
        FrameLayout pinRow = new FrameLayout(this);
        pinRow.addView(pin, new FrameLayout.LayoutParams(-1, dp(48)));
        ImageButton forgotPin = new ImageButton(this);
        forgotPin.setImageResource(R.drawable.ic_field_key);
        forgotPin.setContentDescription("Forgot PIN");
        forgotPin.setColorFilter(accentColor());
        forgotPin.setBackgroundColor(Color.TRANSPARENT);
        forgotPin.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        forgotPin.setPadding(dp(12), dp(12), dp(12), dp(12));
        forgotPin.setOnClickListener(view -> showForgotPinDialog(username));
        FrameLayout.LayoutParams forgotPinParams = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.END | Gravity.CENTER_VERTICAL);
        pinRow.addView(forgotPin, forgotPinParams);
        addField(root, pinRow);
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

    private void showForgotPinDialog(EditText loginUsername) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset PIN?")
                .setMessage("A new temporary 4-digit PIN will be generated and sent after mobile verification.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", null)
                .create();

        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String username = loginUsername.getText().toString().trim().toLowerCase(Locale.US);
            String mobileValue = getSharedPreferences("fendly_account", MODE_PRIVATE).getString("mobile", "").trim();
            if (!username.matches("^[a-z0-9_]{3,32}$")) {
                loginUsername.setError("Enter your username first");
                dialog.dismiss();
                return;
            }
            if (!mobileValue.matches("^\\d{10}$")) {
                dialog.dismiss();
                Toast.makeText(this, "No verified mobile number is saved", Toast.LENGTH_LONG).show();
                return;
            }
            String temporaryPin = String.format(Locale.US, "%04d", new SecureRandom().nextInt(10000));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Sending...");
            startForgotPinPhoneVerification(username, mobileValue, temporaryPin, dialog);
        }));
        dialog.show();
    }

    private void startForgotPinPhoneVerification(String username, String mobile, String temporaryPin, AlertDialog parentDialog) {
        phoneVerificationHandled = false;
        PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+91" + mobile)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override public void onVerificationCompleted(PhoneAuthCredential credential) {
                        resetPinWithPhoneCredential(credential, username, mobile, temporaryPin, parentDialog);
                    }

                    @Override public void onVerificationFailed(FirebaseException error) {
                        parentDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Could not send OTP", Toast.LENGTH_LONG).show();
                    }

                    @Override public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        phoneVerificationId = verificationId;
                        showForgotOtpDialog(username, mobile, temporaryPin, parentDialog);
                    }
                }).build());
    }

    private void showForgotOtpDialog(String username, String mobile, String pin, AlertDialog parentDialog) {
        EditText code = field("6-digit OTP");
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("Enter OTP")
                .setView(code)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset PIN", (dialog, which) -> {
                    String value = code.getText().toString().trim();
                    if (phoneVerificationId == null || value.length() != 6) {
                        Toast.makeText(this, "Enter the 6-digit OTP", Toast.LENGTH_LONG).show();
                        return;
                    }
                    resetPinWithPhoneCredential(PhoneAuthProvider.getCredential(phoneVerificationId, value), username, mobile, pin, parentDialog);
                }).show();
    }

    private void resetPinWithPhoneCredential(PhoneAuthCredential credential, String username, String mobile, String pin, AlertDialog parentDialog) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    if (result.getUser() == null || !credentialEmail(username).equalsIgnoreCase(result.getUser().getEmail())) {
                        phoneVerificationHandled = false;
                        Toast.makeText(this, "This mobile is not linked to that username", Toast.LENGTH_LONG).show();
                        return;
                    }
                    result.getUser().updatePassword(credentialPassword(username, pin))
                            .addOnSuccessListener(updated -> {
                                getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                        .putBoolean("created", true)
                                        .putString("username", username)
                                        .putString("mobile", mobile)
                                        .apply();
                                saveStoredAccountPin(pin);
                                parentDialog.dismiss();
                                new AlertDialog.Builder(this)
                                    .setTitle("Temporary PIN")
                                    .setMessage("Your new 4-digit PIN is " + pin + ". Use it to log in, then change it from My Profile.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            })
                            .addOnFailureListener(error -> {
                                phoneVerificationHandled = false;
                                Toast.makeText(this, "Could not reset PIN", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(error -> {
                    phoneVerificationHandled = false;
                    Toast.makeText(this, "OTP verification failed", Toast.LENGTH_LONG).show();
                });
    }

    private boolean validEmail(String value) {
        String email = value == null ? "" : value.trim().toLowerCase(Locale.US);
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return false;
        }
        String[] allowedDomains = new String[]{
                "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "live.com",
                "icloud.com", "aol.com", "protonmail.com", "rediffmail.com", "ymail.com"
        };
        for (String domain : allowedDomains) {
            if (email.endsWith("@" + domain)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String[]> indiaStateCityMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        map.put("Select state", new String[]{"Select city"});
        map.put("Andaman and Nicobar Islands", new String[]{"Port Blair", "Diglipur", "Mayabunder", "Rangat", "Havelock Island", "Car Nicobar", "Nancowry", "Campbell Bay", "Little Andaman", "Baratang", "Neil Island"});
        map.put("Andhra Pradesh", new String[]{"Visakhapatnam", "Vijayawada", "Guntur", "Nellore", "Kurnool", "Rajahmundry", "Kadapa", "Tirupati", "Anantapur", "Ongole", "Eluru", "Srikakulam", "Machilipatnam", "Chittoor", "Vizianagaram", "Amaravati", "Kakinada", "Nandyal", "Sri Potti Sriramulu Nellore", "Bapatla", "Markapur", "Palnadu", "Parvathipuram", "Alluri Sitharama Raju", "Anakapalli", "Konaseema", "NTR", "Sri Sathya Sai", "Tadepalligudem", "Tenali", "Hindupur", "Kadiri", "Proddatur", "Rayachoti", "Srikalahasti", "Gudur"});
        map.put("Arunachal Pradesh", new String[]{"Itanagar", "Naharlagun", "Tawang", "Pasighat", "Ziro", "Bomdila", "Tezpur", "Along", "Roing", "Namsai", "Aalo", "Anini", "Changlang", "Daporijo", "Dibang Valley", "Diyun", "Khonsa", "Koloriang", "Kra Daadi", "Kurung Kumey", "Longding", "Seppa", "Tali", "Tawang", "Tezu", "Yingkiong", "Yupia"});
        map.put("Assam", new String[]{"Guwahati", "Silchar", "Dibrugarh", "Jorhat", "Nagaon", "Tinsukia", "Tezpur", "Sivasagar", "Bongaigaon", "Dhubri", "Diphu", "Goalpara", "Barpeta", "North Lakhimpur", "Amingaon", "Biswanath Chariali", "Charaideo", "Dhemaji", "Dhing", "Dima Hasao", "Haflong", "Hailakandi", "Hojai", "Karbi Anglong", "Kokrajhar", "Majuli", "Mangaldoi", "Marigaon", "Nalbari", "Sadiya", "Sonitpur", "South Salmara", "Tamulpur", "Udalguri"});
        map.put("Bihar", new String[]{"Patna", "Gaya", "Bhagalpur", "Muzaffarpur", "Purnia", "Darbhanga", "Arrah", "Begusarai", "Katihar", "Munger", "Chhapra", "Bettiah", "Saharsa", "Hajipur", "Bihar Sharif", "Araria", "Aurangabad", "Bagaha", "Banka", "Barh", "Bhabua", "Buxar", "Chapra", "Daudnagar", "Dehri", "Forbesganj", "Jamui", "Jehanabad", "Jhanjharpur", "Kaimur", "Khagaria", "Kishanganj", "Lakhisarai", "Madhepura", "Madhubani", "Mahua", "Masaurhi", "Nawada", "Piro", "Rajgir", "Raxaul", "Rohtas", "Samastipur", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan", "Supaul", "Vaishali"});
        map.put("Chandigarh", new String[]{"Chandigarh"});
        map.put("Chhattisgarh", new String[]{"Raipur", "Bhilai", "Bilaspur", "Korba", "Durg", "Rajnandgaon", "Jagdalpur", "Raigarh", "Ambikapur", "Dhamtari", "Mahasamund", "Kawardha", "Balod", "Baloda Bazar", "Balrampur", "Bastar", "Bemetara", "Bijapur", "Dantewada", "Gariaband", "Gaurela Pendra Marwahi", "Janjgir", "Jashpur", "Kanker", "Kondagaon", "Koriya", "Mungeli", "Narayanpur", "Sukma", "Surajpur", "Surguja", "Dongargarh", "Champa", "Chirmiri", "Dalli Rajhara", "Kumhari", "Ratanpur"});
        map.put("Dadra and Nagar Haveli and Daman and Diu", new String[]{"Daman", "Diu", "Silvassa", "Amli", "Naroli"});
        map.put("Delhi", new String[]{"New Delhi", "Delhi", "Dwarka", "Rohini", "Saket", "Karol Bagh", "Lajpat Nagar", "Vasant Kunj", "Janakpuri", "Shahdara", "Connaught Place", "Chandni Chowk", "Civil Lines", "Defence Colony", "Kalkaji", "Mayur Vihar", "Najafgarh", "Narela", "Patel Nagar", "Pitampura", "Preet Vihar", "Seelampur", "Vikaspuri", "Wazirabad"});
        map.put("Goa", new String[]{"Panaji", "Margao", "Mapusa", "Vasco da Gama", "Ponda", "Bicholim", "Curchorem", "Cuncolim", "Canacona", "Porvorim"});
        map.put("Gujarat", new String[]{"Ahmedabad", "Surat", "Vadodara", "Rajkot", "Gandhinagar", "Bhavnagar", "Jamnagar", "Junagadh", "Anand", "Bharuch", "Vapi", "Navsari", "Mehsana", "Morbi", "Bhuj", "Godhra", "Palanpur", "Amreli", "Aravalli", "Balasinor", "Botad", "Chhota Udaipur", "Dahod", "Dwarka", "Gandhidham", "Gir Somnath", "Himatnagar", "Kheda", "Kutch", "Mahisagar", "Mandvi", "Modasa", "Nadiad", "Patan", "Porbandar", "Rajpipla", "Surendranagar", "Valsad", "Veraval", "Wankaner", "Dholka", "Kalol", "Mundra", "Sanand", "Unjha"});
        map.put("Haryana", new String[]{"Gurugram", "Faridabad", "Panipat", "Hisar", "Rohtak", "Karnal", "Sonipat", "Ambala", "Yamunanagar", "Panchkula", "Bhiwani", "Sirsa", "Rewari", "Bahadurgarh", "Kurukshetra", "Charkhi Dadri", "Fatehabad", "Firozpur Jhirka", "Hansi", "Jhajjar", "Jind", "Kaithal", "Narnaul", "Palwal", "Pehowa", "Ratia", "Shahabad", "Tohana", "Nuh", "Kalanur", "Kalka", "Ladwa", "Radaur"});
        map.put("Himachal Pradesh", new String[]{"Shimla", "Manali", "Dharamshala", "Solan", "Mandi", "Kullu", "Baddi", "Una", "Hamirpur", "Kangra", "Chamba", "Bilaspur", "Nahan", "Kasauli", "Churah", "Dalhousie", "Kinnaur", "Lahaul", "Karsog", "Keylong", "Kullu", "Lahaul Spiti", "Paonta Sahib", "Palampur", "Rohru", "Sirmaur", "Sundernagar", "Theog", "Una", "Yol"});
        map.put("Jammu and Kashmir", new String[]{"Srinagar", "Jammu", "Anantnag", "Baramulla", "Kathua", "Udhampur", "Kupwara", "Pulwama", "Poonch", "Rajouri", "Sopore", "Kishtwar", "Doda", "Budgam", "Ganderbal", "Bandipora", "Bijbehara", "Kulgam", "Shopian", "Ramban", "Reasi", "Samba", "Tral", "Handwara", "Uri", "Banihal", "Chenani", "Bhadarwah", "Nowshera", "Thanamandi"});
        map.put("Jharkhand", new String[]{"Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Hazaribagh", "Deoghar", "Giridih", "Ramgarh", "Phusro", "Medininagar", "Dumka", "Chaibasa", "Sahibganj", "Gumla", "Chatra", "Garhwa", "Godda", "Jamtara", "Khunti", "Koderma", "Latehar", "Lohardaga", "Pakur", "Rajmahal", "Saraikela", "Simdega", "Adityapur", "Chakradharpur", "Chas", "Ghatshila", "Madhupur", "Mihijam", "Sahibganj"});
        map.put("Karnataka", new String[]{"Bengaluru", "Mysuru", "Hubballi", "Mangaluru", "Belagavi", "Kalaburagi", "Davangere", "Ballari", "Vijayapura", "Shivamogga", "Tumakuru", "Raichur", "Hassan", "Udupi", "Chitradurga", "Kolar", "Mandya", "Bidar", "Bagalkot", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga", "Dakshina Kannada", "Dharwad", "Gadag", "Haveri", "Kodagu", "Koppal", "Madikeri", "Ramanagara", "Uttara Kannada", "Yadgir", "Bantwal", "Bhadravati", "Channapatna", "Doddaballapur", "Gokak", "Hunsur", "Karwar", "Kundapura", "Madikeri", "Sirsi", "Sira", "Hospet", "Kolar Gold Fields"});
        map.put("Kerala", new String[]{"Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam", "Kannur", "Alappuzha", "Kottayam", "Palakkad", "Malappuram", "Kasaragod", "Idukki", "Pathanamthitta", "Varkala", "Aluva", "Attingal", "Changanassery", "Chavakkad", "Cherthala", "Kanhangad", "Kasaragod", "Kattappana", "Kayamkulam", "Koduvally", "Kondotty", "Kothamangalam", "Manjeri", "Mavelikkara", "Muvattupuzha", "Nedumangad", "Neyyattinkara", "Nilambur", "Ottapalam", "Paravur", "Perinthalmanna", "Ponnani", "Shoranur", "Taliparamba", "Thalassery", "Thiruvalla", "Tirur", "Wadakkanchery"});
        map.put("Ladakh", new String[]{"Leh", "Kargil", "Diskit", "Nubra", "Padum", "Drass"});
        map.put("Lakshadweep", new String[]{"Kavaratti", "Agatti", "Amini", "Andrott", "Kalpeni", "Minicoy"});
        map.put("Madhya Pradesh", new String[]{"Bhopal", "Indore", "Jabalpur", "Gwalior", "Ujjain", "Sagar", "Dewas", "Satna", "Ratlam", "Rewa", "Murwara", "Singrauli", "Burhanpur", "Khandwa", "Chhindwara", "Vidisha", "Shivpuri", "Agar", "Alirajpur", "Anuppur", "Ashoknagar", "Balaghat", "Barwani", "Betul", "Bhind", "Dhar", "Dindori", "Guna", "Harda", "Hoshangabad", "Jhabua", "Katni", "Mandla", "Mandsaur", "Morena", "Narsinghpur", "Neemuch", "Panna", "Raisen", "Rajgarh", "Sehore", "Shahdol", "Sheopur", "Shajapur", "Sivni", "Tikamgarh", "Umaria", "Datia", "Maihar", "Niwari"});
        map.put("Maharashtra", new String[]{"Mumbai", "Pune", "Nagpur", "Nashik", "Aurangabad", "Thane", "Navi Mumbai", "Kolhapur", "Solapur", "Amravati", "Nanded", "Sangli", "Jalgaon", "Akola", "Latur", "Dhule", "Ahmednagar", "Satara", "Ratnagiri", "Beed", "Bhandara", "Buldhana", "Chandrapur", "Gadchiroli", "Gondia", "Hingoli", "Jalna", "Karad", "Lonavala", "Malegaon", "Malkapur", "Nandurbar", "Osmanabad", "Palghar", "Parbhani", "Raigad", "Sangamner", "Sindhudurg", "Wardha", "Washim", "Yavatmal", "Baramati", "Bhiwandi", "Kalyan", "Mira Bhayandar", "Panvel", "Vasai Virar", "Ichalkaranji", "Ulhasnagar"});
        map.put("Manipur", new String[]{"Imphal", "Thoubal", "Kakching", "Bishnupur", "Ukhrul", "Churachandpur", "Senapati", "Tamenglong", "Jiribam"});
        map.put("Meghalaya", new String[]{"Shillong", "Tura", "Jowai", "Nongstoin", "Baghmara", "Nongpoh", "Williamnagar", "Mairang", "Khliehriat"});
        map.put("Mizoram", new String[]{"Aizawl", "Lunglei", "Champhai", "Serchhip", "Kolasib", "Saiha", "Lawngtlai", "Mamit", "Khawzawl"});
        map.put("Nagaland", new String[]{"Kohima", "Dimapur", "Chumoukedima", "Mokokchung", "Wokha", "Tuensang", "Mon", "Phek", "Zunheboto", "Kiphire", "Longleng"});
        map.put("Odisha", new String[]{"Bhubaneswar", "Cuttack", "Rourkela", "Puri", "Sambalpur", "Berhampur", "Balasore", "Baripada", "Jharsuguda", "Bhadrak", "Jeypore", "Bargarh", "Angul", "Dhenkanal", "Rayagada", "Koraput", "Balangir", "Boudh", "Deogarh", "Gajapati", "Ganjam", "Jagatsinghpur", "Jajpur", "Kalahandi", "Kandhamal", "Kendrapara", "Kendujhar", "Khordha", "Malkangiri", "Mayurbhanj", "Nabarangpur", "Nayagarh", "Nuapada", "Sonepur", "Sundargarh", "Athagarh", "Barbil", "Bhanjanagar", "Boudhgarh", "Jajpur Road", "Paradip", "Phulbani"});
        map.put("Puducherry", new String[]{"Puducherry", "Karaikal", "Mahe", "Yanam"});
        map.put("Punjab", new String[]{"Chandigarh", "Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda", "Mohali", "Hoshiarpur", "Batala", "Pathankot", "Moga", "Abohar", "Sangrur", "Barnala", "Firozpur", "Kapurthala", "Fazilka", "Faridkot", "Fatehgarh Sahib", "Gurdaspur", "Mansa", "Malerkotla", "Muktsar", "Nawanshahr", "Rupnagar", "Tarn Taran", "Zirakpur", "Khanna", "Kharar", "Rajpura", "Sunam", "Phagwara"});
        map.put("Rajasthan", new String[]{"Jaipur", "Jodhpur", "Udaipur", "Kota", "Ajmer", "Bikaner", "Alwar", "Bharatpur", "Sikar", "Bhilwara", "Sri Ganganagar", "Pali", "Chittorgarh", "Barmer", "Tonk", "Kishangarh", "Jaisalmer", "Bundi", "Anupgarh", "Balotra", "Banswara", "Baran", "Beawar", "Bharatpur", "Bikaner", "Churu", "Dausa", "Dholpur", "Didwana", "Dungarpur", "Hanumangarh", "Jhalawar", "Jhunjhunu", "Karauli", "Khairthal", "Nagaur", "Neem Ka Thana", "Phalodi", "Rajsamand", "Salumbar", "Sawai Madhopur", "Shahpura", "Sirohi", "Jalore", "Jodhpur", "Kekri", "Kotputli", "Makrana", "Mount Abu", "Nathdwara", "Sujangarh"});
        map.put("Sikkim", new String[]{"Gangtok", "Namchi", "Gyalshing", "Mangan", "Rangpo", "Singtam", "Jorethang", "Naya Bazar"});
        map.put("Tamil Nadu", new String[]{"Chennai", "Coimbatore", "Madurai", "Salem", "Tiruchirappalli", "Tiruppur", "Erode", "Tirunelveli", "Vellore", "Thoothukudi", "Dindigul", "Thanjavur", "Hosur", "Nagercoil", "Kanchipuram", "Cuddalore", "Kumbakonam", "Karur", "Namakkal", "Ariyalur", "Chengalpattu", "Dharmapuri", "Kallakurichi", "Krishnagiri", "Mayiladuthurai", "Nagapattinam", "Perambalur", "Pudukkottai", "Ramanathapuram", "Ranipet", "Sivaganga", "Tenkasi", "Theni", "The Nilgiris", "Thiruvarur", "Tiruvallur", "Tiruvannamalai", "Viluppuram", "Virudhunagar", "Avadi", "Ambattur", "Hosur", "Karaikudi", "Kovilpatti", "Pollachi", "Rajapalayam", "Sivakasi", "Tiruchengode", "Udumalaipettai"});
        map.put("Telangana", new String[]{"Hyderabad", "Warangal", "Nizamabad", "Khammam", "Karimnagar", "Ramagundam", "Mahbubnagar", "Nalgonda", "Adilabad", "Suryapet", "Siddipet", "Miryalaguda", "Jagtial", "Mancherial", "Kamareddy"});
        map.put("Tripura", new String[]{"Agartala", "Udaipur", "Khowai", "Belonia", "Kailashahar", "Dharmanagar", "Ambassa", "Sabroom", "Sonamura"});
        map.put("Uttar Pradesh", new String[]{"Lucknow", "Kanpur", "Agra", "Varanasi", "Noida", "Prayagraj", "Ghaziabad", "Meerut", "Bareilly", "Aligarh", "Moradabad", "Saharanpur", "Gorakhpur", "Mathura", "Ayodhya", "Jhansi", "Firozabad", "Muzaffarnagar", "Rampur", "Mirzapur", "Etawah", "Hapur", "Amroha", "Azamgarh", "Bahraich", "Ballia", "Banda", "Barabanki", "Basti", "Bhadohi", "Bijnor", "Budaun", "Bulandshahr", "Chandauli", "Chitrakoot", "Deoria", "Etah", "Farrukhabad", "Fatehpur", "Ghazipur", "Gonda", "Hamirpur", "Hardoi", "Hathras", "Jalaun", "Jaunpur", "Kannauj", "Kanpur Dehat", "Kasganj", "Kaushambi", "Kushinagar", "Lakhimpur", "Lalitpur", "Maharajganj", "Mahoba", "Mainpuri", "Mau", "Pilibhit", "Pratapgarh", "Raebareli", "Sambhal", "Sant Kabir Nagar", "Shahjahanpur", "Shamli", "Shravasti", "Siddharthnagar", "Sitapur", "Sonbhadra", "Sultanpur", "Unnao", "Vrindavan", "Robertsganj", "Sikandrabad", "Kasganj"});
        map.put("Uttarakhand", new String[]{"Dehradun", "Haridwar", "Rishikesh", "Haldwani", "Roorkee", "Nainital", "Rudrapur", "Kashipur", "Almora", "Pithoragarh", "Mussoorie", "Srinagar", "Kotdwar", "Chamoli"});
        map.put("West Bengal", new String[]{"Kolkata", "Durgapur", "Asansol", "Siliguri", "Howrah", "Darjeeling", "Kharagpur", "Haldia", "Malda", "Bardhaman", "Baharampur", "Jalpaiguri", "Cooch Behar", "Raiganj", "Krishnanagar", "Serampore", "Dum Dum", "Alipurduar", "Arambagh", "Bankura", "Bangaon", "Barasat", "Barrackpore", "Basirhat", "Bidhannagar", "Bishnupur", "Bolpur", "Chakdaha", "Contai", "Diamond Harbour", "Egra", "English Bazar", "Gangarampur", "Ghatal", "Habra", "Halisahar", "Jangipur", "Jhargram", "Kalna", "Katwa", "Kalyani", "Kamarhati", "Kanchrapara", "Kandi", "Kharagpur", "Kulti", "Madhyamgram", "Murarai", "Nabadwip", "Naihati", "Purulia", "Raghunathganj", "Rampurhat", "Shantiniketan", "Suri", "Tamluk", "Tarakeswar", "Uluberia"});
        return map;
    }

    private LinearLayout labeledSpinner(String label, Spinner spinner) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        TextView fieldLabel = text(label, 10, secondaryTextColor(), Typeface.BOLD);
        fieldLabel.setLetterSpacing(.04f);
        fieldLabel.setGravity(Gravity.START);
        fieldLabel.setIncludeFontPadding(false);
        group.addView(fieldLabel, new LinearLayout.LayoutParams(-1, dp(20)));
        spinner.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        spinner.setPadding(dp(12), dp(8), dp(12), dp(8));
        group.addView(spinner, new LinearLayout.LayoutParams(-1, dp(46)));
        return group;
    }

    private LinearLayout labeledCitySearch(String label, AutoCompleteTextView citySearch) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));
        citySearch.setTextColor(darkMode ? Color.WHITE : LIGHT_TEXT);
        citySearch.setHintTextColor(darkMode ? Color.WHITE : LIGHT_TEXT);
        citySearch.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        citySearch.setPadding(dp(12), dp(8), dp(12), dp(8));
        group.addView(citySearch, new LinearLayout.LayoutParams(-1, dp(46)));
        return group;
    }

    private void showStatePicker(String[] states, AutoCompleteTextView stateSearch,
                                 AutoCompleteTextView citySearch,
                                 Map<String, String[]> stateCities) {
        LinearLayout picker = new LinearLayout(this);
        picker.setOrientation(LinearLayout.VERTICAL);
        picker.setPadding(dp(4), 0, dp(4), 0);

        EditText search = new EditText(this);
        search.setHint("Search state");
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        search.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        applyIcon(search, R.drawable.ic_field_search);
        picker.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        ListView stateList = new ListView(this);
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, states);
        stateList.setAdapter(stateAdapter);
        picker.addView(stateList, new LinearLayout.LayoutParams(-1, dp(320)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select state")
                .setView(picker)
                .setNegativeButton("Cancel", null)
                .create();

        search.setOnClickListener(view -> {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                stateAdapter.getFilter().filter(value);
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        stateList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = stateAdapter.getItem(position);
            stateSearch.setText(selectedState);
            citySearch.setText("");
            String[] cities = stateCities.getOrDefault(selectedState, new String[0]);
            citySearch.setEnabled(cities.length > 0 && !"Select city".equals(cities[0]));
            dialog.dismiss();
            hideKeyboardAfterLocationSelection(stateSearch, citySearch);
        });
        dialog.setOnShowListener(shown -> {
            stateList.requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        });
        dialog.show();
    }

    private void showCityPicker(String state, String[] cities, AutoCompleteTextView citySearch) {
        LinearLayout picker = new LinearLayout(this);
        picker.setOrientation(LinearLayout.VERTICAL);
        picker.setPadding(dp(4), 0, dp(4), 0);

        EditText search = new EditText(this);
        search.setHint("Search city");
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        search.setShowSoftInputOnFocus(false);
        search.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        applyIcon(search, R.drawable.ic_field_search);
        picker.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        ListView cityList = new ListView(this);
        String[] sortedCities = cities.clone();
        Arrays.sort(sortedCities, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sortedCities);
        cityList.setAdapter(cityAdapter);
        picker.addView(cityList, new LinearLayout.LayoutParams(-1, dp(320)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(state)
                .setView(picker)
                .setNegativeButton("Cancel", null)
                .create();

        search.setOnClickListener(view -> {
            search.setShowSoftInputOnFocus(true);
            search.requestFocus();
            InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                cityAdapter.getFilter().filter(value);
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        cityList.setOnItemClickListener((parent, view, position, id) -> {
            citySearch.setText(cityAdapter.getItem(position));
            dialog.dismiss();
            hideKeyboardAfterLocationSelection(citySearch);
        });
        dialog.setOnShowListener(shown -> {
            cityList.requestFocus();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        });
        dialog.show();
    }

    private void hideKeyboardAfterLocationSelection(View... selectedFields) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (keyboard != null && focusedView != null) {
            keyboard.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
        if (focusedView != null) focusedView.clearFocus();
        for (View field : selectedFields) field.clearFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void showHome() {
        currentPage = PAGE_HOME;
        screenRenderer = this::showHome;
        LinearLayout root = screenBase("Home");
        addHeading("Find what matters.", "Lost nearby? Found something? Start here.");
        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.HORIZONTAL);
        TextView lost = actionButton("LOST", true);
        lost.setBackground(round(Color.rgb(11, 93, 69), 18));
        lost.setTextColor(Color.WHITE);
        lost.setOnClickListener(view -> showReport("LOST"));
        choices.addView(lost, new LinearLayout.LayoutParams(0, 72, 1));
        TextView found = actionButton("FOUND", false);
        found.setTextColor(Color.WHITE);
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
        String username = getSharedPreferences("fendly_account", MODE_PRIVATE).getString("username", "your account");
        TextView account = text("Signed in as " + username, 14, secondaryTextColor(), Typeface.NORMAL);
        account.setGravity(Gravity.CENTER);
        addField(root, account);
    }

    private void showReport(String type) {
        inRenewalPaymentFlow = false;
        currentPage = PAGE_REPORT;
        screenRenderer = () -> showReport(type);
        currentReportType = type;
        LinearLayout root = screenBase("");
        root.addView(text("Report an item", 15, primaryTextColor(), Typeface.NORMAL), contentParams(-1, dp(22), dp(14)));
        LinearLayout typeToggle = new LinearLayout(this);
        typeToggle.setOrientation(LinearLayout.HORIZONTAL);
        TextView lostToggle = reportTypeToggle("Lost", "LOST".equals(type), LOST_GREEN, LOST_GREEN_ON);
        TextView foundToggle = reportTypeToggle("Found", "FOUND".equals(type), FOUND_GOLD, FOUND_GOLD_ON);
        lostToggle.setOnClickListener(view -> showReport("LOST"));
        foundToggle.setOnClickListener(view -> showReport("FOUND"));
        typeToggle.addView(lostToggle, new LinearLayout.LayoutParams(0, dp(36), 1));
        LinearLayout.LayoutParams foundToggleParams = new LinearLayout.LayoutParams(0, dp(36), 1);
        foundToggleParams.setMargins(dp(8), 0, 0, 0);
        typeToggle.addView(foundToggle, foundToggleParams);
        root.addView(typeToggle, contentParams(-1, dp(36), dp(16)));

        EditText item = field("");
        EditText description = field("");
        EditText imei = field("");
        imei.setText(draftImei);
        imei.setInputType(InputType.TYPE_CLASS_NUMBER);
        imei.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        TextView imeiHint = text("Enter 15-digit IMEI number", 10, secondaryTextColor(), Typeface.NORMAL);
        imeiHint.setVisibility(View.GONE);
        imei.addTextChangedListener(new TextWatcher() {
            private boolean selfUpdating = false;
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable value) {
                if (selfUpdating || value == null) return;
                String digits = value.toString().replaceAll("\\D", "");
                draftImei = digits;
                if (!digits.equals(value.toString())) {
                    selfUpdating = true;
                    value.clear();
                    value.append(digits);
                    selfUpdating = false;
                }
                if (value.length() > 0 && value.length() < 15) {
                    imei.setError(null);
                    imeiHint.setVisibility(View.GONE);
                }
                if (value.length() == 15) {
                    imei.setError(null);
                    imeiHint.setVisibility(View.GONE);
                }
            }
        });
        imei.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String imeiValue = imei.getText().toString().trim();
                if (!imeiValue.isEmpty() && !imeiValue.matches("\\d{15}")) {
                    imei.setError("Invalid IMEI number");
                    imeiHint.setVisibility(View.VISIBLE);
                } else {
                    imei.setError(null);
                    imeiHint.setVisibility(View.GONE);
                }
            }
        });
        EditText location = field("");
        EditText date = field("");
        item.setText(draftItem);
        description.setText(draftDescription);
        location.setText(draftLocation);
        date.setText(draftDate);
        final LinearLayout imeiGroup = new LinearLayout(this);
        imeiGroup.setOrientation(LinearLayout.VERTICAL);
        imeiGroup.addView(fieldLabel("IMEI Number"), new LinearLayout.LayoutParams(-1, dp(20)));
        FrameLayout imeiFrame = new FrameLayout(this);
        imeiFrame.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        imei.setBackgroundColor(Color.TRANSPARENT);
        imei.setPadding(dp(16), dp(8), dp(48), dp(8));
        imeiFrame.addView(imei, new FrameLayout.LayoutParams(-1, dp(46)));
        ImageView imeiCamera = new ImageView(this);
        imeiCamera.setImageResource(R.drawable.ic_field_camera);
        imeiCamera.setColorFilter(accentColor());
        imeiCamera.setPadding(dp(11), dp(11), dp(11), dp(11));
        imeiCamera.setContentDescription("Scan IMEI");
        imeiCamera.setOnClickListener(view -> openImeiScanner(imei));
        FrameLayout.LayoutParams imeiIconParams = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.END | Gravity.CENTER_VERTICAL);
        imeiFrame.addView(imeiCamera, imeiIconParams);
        imeiFrame.setOnClickListener(view -> openImeiScanner(imei));
        imeiGroup.addView(imeiFrame, new LinearLayout.LayoutParams(-1, dp(46)));
        LinearLayout.LayoutParams imeiParams = contentParams(-1, dp(76), dp(8));
        imeiParams.topMargin = dp(2);
        imeiGroup.setLayoutParams(imeiParams);
        imeiGroup.setVisibility(View.GONE);
        Runnable refreshImeiField = () -> {
            boolean shouldShow = !draftImei.isEmpty()
                    || containsMobileKeyword(item.getText().toString())
                    || containsMobileKeyword(description.getText().toString());
            imeiGroup.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            if (!shouldShow) {
                draftImei = "";
                imei.setText("");
                imei.setError(null);
            } else if (!draftImei.equals(imei.getText().toString())) {
                imei.setText(draftImei);
                imei.setSelection(draftImei.length());
            }
        };
        item.addTextChangedListener(draftWatcher(value -> {
            draftItem = value;
            refreshImeiField.run();
        }));
        description.addTextChangedListener(draftWatcher(value -> {
            draftDescription = value;
            refreshImeiField.run();
        }));
        location.addTextChangedListener(draftWatcher(value -> draftLocation = value));
        date.addTextChangedListener(draftWatcher(value -> draftDate = value));
        configureDateField(date);
        addLabeledField(root, "Item name", item);
        addLabeledField(root, "Description", description);
        root.addView(imeiGroup, root.getChildCount(), imeiParams);
        refreshImeiField.run();
        addLabeledDateField(root, "Date " + ("FOUND".equals(type) ? "found" : "lost"), date);
        addLabeledField(root, "FOUND".equals(type) ? "Place found" : "Last seen at", location);

        TextView addLocation = text("Precise location  OFF", 11, secondaryTextColor(), Typeface.BOLD);
        addLocation.setGravity(Gravity.CENTER);
        addLocation.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(-1, dp(42));
        locationParams.setMargins(0, 0, 0, dp(12));
        root.addView(addLocation, locationParams);
        addLocation.setOnClickListener(view -> {
            if (hasLocation) {
                hasLocation = false;
                currentLat = 0.0;
                currentLng = 0.0;
                addLocation.setText("Precise location  OFF");
                addLocation.setTextColor(secondaryTextColor());
                addLocation.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            } else {
                requestLocation(addLocation, addLocation);
            }
        });
        locationStatus = addLocation;
        locationToggleStatus = addLocation;

        root.addView(imageSlots(), contentParams(-1, dp(104), dp(14)));

        if (editingReportId != null && type.equalsIgnoreCase(editingReportType)) {
            TextView save = actionButton("Save changes", true);
            save.setOnClickListener(view -> submitItem(type, item, description, imei, location, date, save, null));
            root.addView(save, contentParams(-1, dp(44), 0));
        } else if ("FOUND".equals(type)) {
            TextView publish = actionButton("Submit report — free", true);
            publish.setOnClickListener(view -> submitItem(type, item, description, imei, location, date, publish, null));
            root.addView(publish, contentParams(-1, dp(44), 0));
        } else {
            boolean unlocked = hasActiveAnnualSubscription();
            if (!unlocked) {
                root.addView(subscriptionCard(), contentParams(-1, dp(74), dp(14)));
            }
            TextView action = unlocked
                    ? actionButton("Submit report — free", true)
                    : filledButton("Continue to payment", LOST_GREEN, LOST_GREEN_ON);
            if (unlocked) {
                String subscriptionPaymentId = getSharedPreferences("fendly_account", MODE_PRIVATE)
                    .getString("annual_subscription_payment_id", null);
                action.setOnClickListener(view -> submitItem(type, item, description, imei, location, date, action, subscriptionPaymentId));
            } else {
                action.setOnClickListener(view -> showPaymentOptions(item, description, imei, location, date));
            }
            root.addView(action, contentParams(-1, dp(44), 0));
        }
    }

    private void showPaymentOptions(EditText item, EditText description, EditText imei, EditText location, EditText date) {
        inRenewalPaymentFlow = false;
        currentPage = PAGE_SUBSCRIPTION;
        LinearLayout root = screenBase("Payment");
        addHeading("Choose payment method", "Complete your lost report payment");

        TextView upi = filledButton("Pay with UPI", LOST_GREEN, LOST_GREEN_ON);
        upi.setOnClickListener(view -> startPayment(item, description, imei, location, date, upi));
        addField(root, upi);

        TextView card = actionButton("Debit / Credit card", false);
        card.setOnClickListener(view -> startPayment(item, description, imei, location, date, card));
        addField(root, card);

        TextView wallet = actionButton("Wallet / Paytm", false);
        wallet.setOnClickListener(view -> startPayment(item, description, imei, location, date, wallet));
        addField(root, wallet);

        TextView back = actionButton("Back to report", false);
        back.setOnClickListener(view -> showReport("LOST"));
        addField(root, back);
    }

    private void showRenewPlanOptions() {
        inRenewalPaymentFlow = true;
        currentPage = PAGE_SUBSCRIPTION;
        LinearLayout root = screenBase("Renew plan");
        addHeading("Choose payment method", "Complete your annual plan renewal");

        TextView upi = filledButton("Pay with UPI", LOST_GREEN, LOST_GREEN_ON);
        upi.setOnClickListener(view -> startAnnualSubscriptionPayment(upi));
        addField(root, upi);

        TextView card = actionButton("Debit / Credit card", false);
        card.setOnClickListener(view -> startAnnualSubscriptionPayment(card));
        addField(root, card);

        TextView wallet = actionButton("Wallet / Paytm", false);
        wallet.setOnClickListener(view -> startAnnualSubscriptionPayment(wallet));
        addField(root, wallet);

        TextView back = actionButton("Back to profile", false);
        back.setOnClickListener(view -> showProfile());
        addField(root, back);
    }

    private void startAnnualSubscriptionPayment(TextView button) {
        pendingPaymentItem = null;
        pendingPaymentDescription = null;
        pendingPaymentImei = null;
        pendingPaymentLocation = null;
        pendingPaymentDate = null;
        pendingPaymentButton = button;
        button.setText("Opening checkout...");
        button.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(tokenResult -> {
            if (tokenResult == null || tokenResult.getToken() == null) {
                paymentFailed("Authentication unavailable");
                return;
            }
            network.execute(() -> {
                String response = createPaymentOrder(tokenResult.getToken());
                runOnUiThread(() -> {
                    if (response == null) {
                        paymentFailed("Payment gateway unavailable");
                        return;
                    }
                    try {
                        JSONObject order = new JSONObject(response);
                        Checkout checkout = new Checkout();
                        checkout.setKeyID(order.getString("key_id"));
                        JSONObject options = new JSONObject();
                        options.put("key", order.getString("key_id"));
                        options.put("order_id", order.getString("order_id"));
                        options.put("amount", order.getInt("amount"));
                        options.put("currency", order.getString("currency"));
                        options.put("name", "Fendly");
                        options.put("description", "Annual subscription renewal");
                        options.put("theme.color", "#E8B24A");
                        options.put("method", new JSONObject().put("upi", true));
                        checkout.open(this, options);
                    } catch (Exception error) {
                        paymentFailed("Could not open payment checkout");
                    }
                });
            });
        }).addOnFailureListener(error -> paymentFailed("Authentication failed"));
    }

    private void startPayment(EditText item, EditText description, EditText imei, EditText location, EditText date, TextView button) {
        pendingPaymentItem = item;
        pendingPaymentDescription = description;
        pendingPaymentImei = imei;
        pendingPaymentLocation = location;
        pendingPaymentDate = date;
        pendingPaymentButton = button;
        button.setText("Opening checkout...");
        button.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(tokenResult -> {
            if (tokenResult == null || tokenResult.getToken() == null) {
                paymentFailed("Authentication unavailable");
                return;
            }
            network.execute(() -> {
                String response = createPaymentOrder(tokenResult.getToken());
                runOnUiThread(() -> {
                    if (response == null) {
                        paymentFailed("Payment gateway unavailable");
                        return;
                    }
                    try {
                        JSONObject order = new JSONObject(response);
                        Checkout checkout = new Checkout();
                        checkout.setKeyID(order.getString("key_id"));
                        JSONObject options = new JSONObject();
                        options.put("key", order.getString("key_id"));
                        options.put("order_id", order.getString("order_id"));
                        options.put("amount", order.getInt("amount"));
                        options.put("currency", order.getString("currency"));
                        options.put("name", "Fendly");
                        options.put("description", "Lost item report");
                        options.put("theme.color", "#E8B24A");
                        options.put("method", new JSONObject().put("upi", true));
                        checkout.open(this, options);
                    } catch (Exception error) {
                        paymentFailed("Could not open payment checkout");
                    }
                });
            });
        }).addOnFailureListener(error -> paymentFailed("Authentication failed"));
    }

    private String createPaymentOrder(String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + "/api/payments/orders").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.getOutputStream().write("{}".getBytes(StandardCharsets.UTF_8));
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return null;
            return readStream(connection.getInputStream());
        } catch (Exception error) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void paymentFailed(String message) {
        if (pendingPaymentButton != null) {
            pendingPaymentButton.setText("Try payment again");
            pendingPaymentButton.setEnabled(true);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean hasActiveAnnualSubscription() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        boolean active = account.getBoolean("annual_subscription_active", false);
        long expiresAt = account.getLong("annual_subscription_expires_at", 0L);
        boolean stillValid = active && expiresAt > 0L && System.currentTimeMillis() < expiresAt;
        if (!stillValid && active) {
            account.edit()
                .putBoolean("annual_subscription_active", false)
                .remove("annual_subscription_payment_id")
                .apply();
        }
        return stillValid;
    }

    private String annualSubscriptionStatusText() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        long expiresAt = account.getLong("annual_subscription_expires_at", 0L);
        if (!account.getBoolean("annual_subscription_active", false) || expiresAt <= 0L) {
            return "Renew plan";
        }
        if (System.currentTimeMillis() >= expiresAt) {
            account.edit().putBoolean("annual_subscription_active", false).remove("annual_subscription_payment_id").apply();
            return "Renew plan";
        }
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd MMM yyyy", Locale.US);
        return "Active subscription until " + format.format(new java.util.Date(expiresAt));
    }

    private void activateAnnualSubscription(String paymentId) {
        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
            .putBoolean("annual_subscription_active", true)
            .putLong("annual_subscription_expires_at", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365L))
            .putString("annual_subscription_payment_id", paymentId)
            .apply();
    }

    @Override
    public void onPaymentSuccess(String paymentId, PaymentData paymentData) {
        if (paymentData == null || paymentData.getOrderId() == null || paymentData.getSignature() == null) {
            paymentFailed("Payment response was incomplete");
            return;
        }
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(tokenResult -> network.execute(() -> {
            boolean verified = verifyPayment(tokenResult.getToken(), paymentData.getOrderId(), paymentId, paymentData.getSignature());
            runOnUiThread(() -> {
                if (!verified) {
                    paymentFailed("Payment could not be verified");
                    return;
                }
                activateAnnualSubscription(paymentId);
                if (pendingPaymentItem == null && pendingPaymentDescription == null && pendingPaymentImei == null && pendingPaymentLocation == null && pendingPaymentDate == null) {
                    showProfile();
                    return;
                }
                currentPage = PAGE_REPORTS;
                showReports();
                submitItem("LOST", pendingPaymentItem, pendingPaymentDescription, pendingPaymentImei, pendingPaymentLocation, pendingPaymentDate, pendingPaymentButton, paymentId);
            });
        })).addOnFailureListener(error -> paymentFailed("Authentication failed"));
    }

    @Override
    public void onPaymentError(int code, String description, PaymentData paymentData) {
        paymentFailed("Payment cancelled or failed");
    }

    private boolean verifyPayment(String idToken, String orderId, String paymentId, String signature) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + "/api/payments/verify").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String body = "{\"razorpay_order_id\":\"" + escapeJson(orderId) + "\",\"razorpay_payment_id\":\"" + escapeJson(paymentId) + "\",\"razorpay_signature\":\"" + escapeJson(signature) + "\"}";
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            return connection.getResponseCode() >= 200 && connection.getResponseCode() < 300;
        } catch (Exception error) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void submitItem(String type, EditText item, EditText description, EditText imei, EditText location, EditText date, TextView publish, String paymentId) {
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
        if (containsMobileKeyword(title) || containsMobileKeyword(details)) {
            String imeiValue = imei == null ? "" : imei.getText().toString().trim();
            if (!imeiValue.matches("\\d{15}")) {
                if (imei != null) imei.setError("Enter a valid 15-digit IMEI number");
                return;
            }
        }
        String dateValue = date.getText().toString().trim();
        if (!dateValue.isEmpty() && !validDate(dateValue)) {
            date.setError("Use a valid date in DD/MM/YYYY format");
            return;
        }
        if (editingReportId != null && type.equalsIgnoreCase(editingReportType)) {
            updateItem(type, item, description, location, date, publish);
            return;
        }
        publish.setText("Submitting...");
        publish.setEnabled(false);
        Uri image = selectedImage;
        Bitmap cameraImage = capturedImage;
        double latitude = hasLocation ? currentLat : 0.0;
        double longitude = hasLocation ? currentLng : 0.0;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            publish.setText("Sign in to submit");
            publish.setEnabled(true);
            return;
        }
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> {
            if (token == null || token.getToken() == null) {
                publish.setText("Authentication unavailable");
                publish.setEnabled(true);
                return;
            }
            network.execute(() -> {
                int code = postItem(type, title, details, location.getText().toString().trim(), date.getText().toString().trim(), latitude, longitude, image, cameraImage, token.getToken(), paymentId);
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

    private int postItem(String type, String title, String description, String location, String date, double latitude, double longitude, Uri image, Bitmap cameraImage, String idToken, String paymentId) {
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
            String imageJson = imageUrl == null ? "null" : "\"" + escapeJson(imageUrl) + "\"";
            String paymentJson = paymentId == null ? "null" : "\"" + escapeJson(paymentId) + "\"";
            String body = "{\"title\":\"" + escapeJson(title) + "\",\"description\":\"" + escapeJson(details) + "\",\"report_location\":\"" + escapeJson(location) + "\",\"report_date\":\"" + escapeJson(date) + "\",\"category\":\"other\",\"lat\":" + latitude + ",\"lng\":" + longitude + ",\"image_url\":" + imageJson + ",\"payment_id\":" + paymentJson + "}";
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

    private void updateItem(String type, EditText item, EditText description, EditText location, EditText date, TextView saveButton) {
        saveButton.setText("Saving...");
        saveButton.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String imageUrl = editingReportImageUrl;
            if (selectedImage != null || capturedImage != null) imageUrl = uploadImage(selectedImage, capturedImage, token.getToken());
            String details = description.getText().toString().trim();
            String locationValue = location.getText().toString().trim();
            String dateValue = date.getText().toString().trim();
            int code = putItem(type, editingReportId, item.getText().toString().trim(), details, locationValue, dateValue, imageUrl, token.getToken());
            runOnUiThread(() -> {
                saveButton.setEnabled(true);
                if (code >= 200 && code < 300) {
                    editingReportId = null;
                    editingReportType = null;
                    editingReportImageUrl = null;
                    Toast.makeText(this, "Report updated", Toast.LENGTH_SHORT).show();
                    showReports();
                } else {
                    saveButton.setText("Retry update");
                        String message = code == 404
                            ? "Report update API is not deployed yet"
                            : "Could not update report (" + code + ")";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            });
        })).addOnFailureListener(error -> {
            saveButton.setText("Retry update");
            saveButton.setEnabled(true);
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show();
        });
    }

    private int putItem(String type, String id, String title, String description, String location, String date, String imageUrl, String idToken) {
        HttpURLConnection connection = null;
        try {
            String endpoint = API_BASE + "/api/items/" + type.toLowerCase(Locale.US) + "/" + id;
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("PUT");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String imageJson = imageUrl == null || imageUrl.trim().isEmpty() ? "null" : "\"" + escapeJson(imageUrl) + "\"";
            String body = "{\"title\":\"" + escapeJson(title) + "\",\"description\":\"" + escapeJson(description) + "\",\"report_location\":\"" + escapeJson(location) + "\",\"report_date\":\"" + escapeJson(date) + "\",\"category\":\"other\",\"lat\":0.0,\"lng\":0.0,\"image_url\":" + imageJson + "}";
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
        DatePickerDialog picker = new DatePickerDialog(this, (dialog, year, month, day) -> target.setText(String.format("%02d/%02d/%04d", day, month + 1, year)), now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }

    private void showProfilePhotoOptions() {
        new AlertDialog.Builder(this)
                .setTitle("Update profile photo")
                .setItems(new String[]{"Choose from gallery", "Take photo"}, (dialog, which) -> {
                    if (which == 0) {
                        openProfilePhotoPicker();
                    } else {
                        openProfilePhotoCamera();
                    }
                })
                .show();
    }

    private void openProfilePhotoPicker() {
        String permission = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PROFILE_IMAGE);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_PROFILE_IMAGE);
    }

    private void openProfilePhotoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PROFILE_CAMERA);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_PROFILE_CAMERA);
    }

    private void refreshEmailVerificationState(EditText email, TextView verifyButton) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            verifyButton.setText("Verify");
            verifyButton.setEnabled(true);
            return;
        }
        verifyButton.setText("Checking...");
        verifyButton.setEnabled(false);
        user.reload().addOnCompleteListener(task -> {
            FirebaseUser refreshedUser = auth.getCurrentUser();
            boolean verified = task.isSuccessful() && refreshedUser != null && refreshedUser.isEmailVerified();
            if (verified) {
                String verifiedEmail = refreshedUser.getEmail();
                if (verifiedEmail != null && !verifiedEmail.trim().isEmpty()) {
                    email.setText(verifiedEmail);
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                            .putString("email", verifiedEmail)
                            .putBoolean("email_verified", true)
                            .apply();
                }
                verifyButton.setText("Verified");
                verifyButton.setEnabled(false);
            } else {
                verifyButton.setText("Verify");
                verifyButton.setEnabled(true);
            }
        });
    }

    private void verifyProfileMobile(EditText mobile, TextView verifyButton) {
        String mobileValue = mobile.getText().toString().trim();
        if (!mobileValue.matches("^\\d{10}$")) {
            mobile.setError("Enter a valid 10-digit mobile number");
            mobile.requestFocus();
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sign in to verify your mobile number", Toast.LENGTH_LONG).show();
            return;
        }
        verifyButton.setText("Sending...");
        verifyButton.setEnabled(false);
        PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91" + mobileValue)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        auth.getCurrentUser().linkWithCredential(credential)
                                .addOnSuccessListener(result -> {
                                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                            .putString("mobile", mobileValue)
                                            .putBoolean("mobile_verified", true)
                                            .apply();
                                    verifyButton.setText("Verified");
                                    Toast.makeText(MainActivity.this, "Mobile verified", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(error -> {
                                    verifyButton.setText("Verify OTP");
                                    verifyButton.setEnabled(true);
                                    Toast.makeText(MainActivity.this, "Could not verify mobile: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException error) {
                        verifyButton.setText("Verify OTP");
                        verifyButton.setEnabled(true);
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        phoneVerificationId = verificationId;
                        verifyButton.setText("Enter OTP");
                        verifyButton.setEnabled(true);
                        showOtpDialogForProfile(mobileValue, mobile, verifyButton);
                    }
                }).build());
    }

    private void showOtpDialogForProfile(String mobileValue, EditText mobile, TextView verifyButton) {
        EditText code = field("6-digit OTP");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Verify mobile")
                .setMessage("Enter the code sent to +91" + mobileValue)
                .setView(code)
                .setPositiveButton("Verify", (dialogButton, which) -> {
                    if (phoneVerificationId == null || code.getText().toString().trim().length() != 6) {
                        verifyButton.setText("Verify OTP");
                        verifyButton.setEnabled(true);
                        Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_LONG).show();
                        return;
                    }
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code.getText().toString().trim());
                    FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                            .addOnSuccessListener(result -> {
                                getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                        .putString("mobile", mobileValue)
                                        .putBoolean("mobile_verified", true)
                                        .apply();
                                verifyButton.setText("Verified");
                                mobile.setText(mobileValue);
                                Toast.makeText(this, "Mobile verified", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(error -> {
                                verifyButton.setText("Verify OTP");
                                verifyButton.setEnabled(true);
                                Toast.makeText(this, "Could not verify mobile: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", (dialogButton, which) -> {
                    verifyButton.setText("Verify OTP");
                    verifyButton.setEnabled(true);
                })
                .create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
    }

    private LinearLayout imageSlots() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        for (int slot = 0; slot < 3; slot++) {
            final int imageSlot = slot;
            FrameLayout slotView = new FrameLayout(this);
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setPadding(dp(6), dp(6), dp(6), dp(6));
            image.setImageResource(R.drawable.ic_field_image);
            image.setColorFilter(accentColor());
            image.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            if (reportImages[slot] != null) {
                image.clearColorFilter();
                if (!setImageFromUri(image, reportImages[slot])) image.setImageResource(R.drawable.ic_field_image);
            }
            if (reportCameraImages[slot] != null) {
                image.clearColorFilter();
                image.setImageBitmap(reportCameraImages[slot]);
            }
            image.setOnClickListener(view -> showImageOptions(imageSlot));
            slotView.addView(image, new FrameLayout.LayoutParams(-1, dp(96)));

            if (reportImages[slot] != null || reportCameraImages[slot] != null) {
                ImageView remove = new ImageView(this);
                remove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                remove.setColorFilter(Color.WHITE);
                remove.setBackground(roundWithStroke(Color.argb(180, 0, 0, 0), 18, Color.WHITE));
                remove.setPadding(dp(4), dp(4), dp(4), dp(4));
                FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.TOP | Gravity.END);
                removeParams.setMargins(dp(4), dp(4), dp(4), 0);
                remove.setOnClickListener(view -> clearImageSlot(imageSlot));
                slotView.addView(remove, removeParams);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(96), 1f);
            if (slot > 0) params.setMargins(dp(8), 0, 0, 0);
            row.addView(slotView, params);
        }
        return row;
    }

    private void clearImageSlot(int slot) {
        reportImages[slot] = null;
        reportCameraImages[slot] = null;
        if (currentReportType != null) showReport(currentReportType);
    }

    private void showImageOptions(int slot) {
        pendingImageSlot = slot;
        new AlertDialog.Builder(this)
                .setTitle("Add image")
                .setItems(new String[]{"Choose from gallery", "Take photo"}, (dialog, which) -> {
                    if (which == 0) openGalleryForSlot(slot);
                    else openCameraForSlot(slot);
                })
                .show();
    }

    private void openGalleryForSlot(int slot) {
        pendingImageSlot = slot;
        String permission = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_IMAGE_PERMISSION);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 710 + slot);
    }

    private void openCameraForSlot(int slot) {
        pendingImageSlot = slot;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 720 + slot);
    }

    private void configureDateField(EditText date) {
        date.setHint("");
        date.setInputType(InputType.TYPE_CLASS_NUMBER);
        date.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        date.setOnTouchListener((view, event) -> {
            return false;
        });
        date.addTextChangedListener(new TextWatcher() {
            private boolean formatting;

            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable value) {
                if (formatting) return;
                String digits = value.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 8) digits = digits.substring(0, 8);
                StringBuilder formatted = new StringBuilder(digits);
                if (digits.length() > 2) formatted.insert(2, '/');
                if (digits.length() > 4) formatted.insert(5, '/');
                String result = formatted.toString();
                if (!result.equals(value.toString())) {
                    formatting = true;
                    date.setText(result);
                    date.setSelection(result.length());
                    formatting = false;
                }
                String[] parts = result.split("/", -1);
                boolean invalidDay = parts.length > 0 && parts[0].length() == 2 && !validRange(parts[0], 1, 31);
                boolean invalidMonth = parts.length > 1 && parts[1].length() == 2 && !validRange(parts[1], 1, 12);
                boolean invalid = !result.isEmpty() && result.length() < 10
                    || invalidDay || invalidMonth || (result.length() == 10 && !validDate(result));
                date.setError(invalid ? "Use a valid date in DD/MM/YYYY format" : null);
            }
        });
    }

    private void openImeiScanner(EditText imeiField) {
        imeiScanTarget = imeiField;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMEI_SCAN);
            return;
        }
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(PortraitCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan IMEI");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private void addLabeledDateField(LinearLayout parent, String label, EditText date) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));

        FrameLayout dateFrame = new FrameLayout(this);
        dateFrame.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        date.setBackgroundColor(Color.TRANSPARENT);
        date.setPadding(dp(16), dp(8), dp(48), dp(8));
        dateFrame.addView(date, new FrameLayout.LayoutParams(-1, dp(46)));
        ImageView calendar = new ImageView(this);
        calendar.setImageResource(R.drawable.ic_field_calendar);
        calendar.setColorFilter(accentColor());
        calendar.setPadding(dp(11), dp(11), dp(11), dp(11));
        calendar.setContentDescription("Choose date");
        calendar.setOnClickListener(view -> pickDate(date));
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.END | Gravity.CENTER_VERTICAL);
        dateFrame.addView(calendar, iconParams);
        dateFrame.setOnClickListener(view -> pickDate(date));
        group.addView(dateFrame, new LinearLayout.LayoutParams(-1, dp(46)));
        parent.addView(group, contentParams(-1, dp(76), dp(4)));
    }

    private TextWatcher draftWatcher(Consumer<String> update) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { update.accept(value.toString()); }
            @Override public void afterTextChanged(Editable value) { }
        };
    }

    private boolean validRange(String value, int minimum, int maximum) {
        try {
            int number = Integer.parseInt(value);
            return number >= minimum && number <= maximum;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    private boolean validDate(String value) {
        if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) return false;
        String[] parts = value.split("/");
        try {
            Calendar date = Calendar.getInstance();
            date.setLenient(false);
            date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            date.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            date.set(Calendar.YEAR, Integer.parseInt(parts[2]));
            int year = Integer.parseInt(parts[2]);
            if (year < 1900 || year > 2099) return false;
            date.getTime();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            today.set(Calendar.MILLISECOND, 999);
            return !date.after(today);
        } catch (Exception error) {
            return false;
        }
    }

    private void showSubscription(EditText item, EditText description, EditText imei, EditText location, EditText date) {
        currentPage = PAGE_SUBSCRIPTION;
        LinearLayout root = screenBase("Fendly Plus");
        addHeading("Report an item", "Silver wristwatch");
        addField(root, premiumCard("Annual subscription", "Rs 99 / year"));
        TextView pay = actionButton("Pay and submit lost report", true);
        pay.setOnClickListener(view -> startPayment(item, description, imei, location, date, pay));
        addField(root, pay);
        TextView back = actionButton("Back to report", false);
        back.setOnClickListener(view -> showReport("LOST"));
        addField(root, back);
    }

    private void showReports() {
        currentPage = PAGE_REPORTS;
        screenRenderer = this::showReports;
        LinearLayout root = screenBase("My reports");
        addHeading("Your reports", "Keep track of items you are helping to reunite.");
        TextView loading = text("Loading reports...", 16, secondaryTextColor(), Typeface.NORMAL);
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
                        String reportId = report.optString("id", "");
                        String reportImageUrl = report.optString("image_url", null);
                        boolean canEdit = report.optInt("edit_count", 0) == 0;
                        addField(activeContent, reportRow(title, type + "  ·  " + detail, () -> {
                            editingReportId = reportId;
                            editingReportType = type;
                            editingReportImageUrl = reportImageUrl;
                            draftItem = title;
                            draftDescription = report.optString("description", "");
                            draftLocation = report.optString("report_location", "");
                            draftDate = report.optString("report_date", "");
                            selectedImage = null;
                            capturedImage = null;
                            Arrays.fill(reportImages, null);
                            Arrays.fill(reportCameraImages, null);
                            showReport(type);
                        }, canEdit, () -> deleteReport(reportId, type)));
                    }
                    if (reports.length() == 0) {
                        addField(activeContent, text("No reports yet.", 16, secondaryTextColor(), Typeface.NORMAL));
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
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return null;
            return readStream(connection.getInputStream());
        } catch (Exception error) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void deleteReport(String reportId, String type) {
        new AlertDialog.Builder(this)
                .setTitle("Delete report?")
                .setMessage("This report will be permanently removed.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                    int code = deleteItem(type, reportId, token.getToken());
                    runOnUiThread(() -> {
                        if (code >= 200 && code < 300) {
                            Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show();
                            showReports();
                        } else {
                            Toast.makeText(this, "Could not delete report (" + code + ")", Toast.LENGTH_LONG).show();
                        }
                    });
                })))
                .show();
    }

    private int deleteItem(String type, String id, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + "/api/items/" + type.toLowerCase(Locale.US) + "/" + id).openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            return connection.getResponseCode();
        } catch (Exception error) {
            return -1;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void syncProfileWithBackend() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        auth.getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(API_BASE + "/api/users/profile").openConnection();
                connection.setRequestMethod("PUT");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Bearer " + token.getToken());
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body = "{\"username\":\"" + escapeJson(account.getString("username", "")) + "\",\"full_name\":\"" + escapeJson(account.getString("full_name", "")) + "\",\"email\":\"" + escapeJson(account.getString("email", "")) + "\",\"mobile\":\"" + escapeJson(account.getString("mobile", "")) + "\"}";
                try (OutputStream output = connection.getOutputStream()) { output.write(body.getBytes(StandardCharsets.UTF_8)); }
                connection.getResponseCode();
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        }));
    }

    private boolean setImageFromUri(ImageView image, Uri uri) {
        if (image == null || uri == null) return false;
        try {
            Bitmap bitmap;
            if ("content".equals(uri.getScheme())) {
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    bitmap = input == null ? null : BitmapFactory.decodeStream(input);
                }
            } else {
                String path = "file".equals(uri.getScheme()) ? uri.getPath() : uri.toString();
                bitmap = BitmapFactory.decodeFile(path);
            }
            if (bitmap == null) return false;
            image.setImageBitmap(bitmap);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void showProfile() {
        inRenewalPaymentFlow = false;
        currentPage = PAGE_PROFILE;
        screenRenderer = this::showProfile;
        LinearLayout root = screenBase("My profile");
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        addCenteredHeading(account.getString("full_name", account.getString("username", "Your profile")), "Your account details and preferences.");

        LinearLayout photoSection = new LinearLayout(this);
        photoSection.setOrientation(LinearLayout.VERTICAL);
        photoSection.setGravity(Gravity.CENTER_HORIZONTAL);
        photoSection.setPadding(0, dp(8), 0, dp(14));

        FrameLayout avatarWrap = new FrameLayout(this);
        avatarWrap.setBackground(roundWithStroke(surfaceColor(), 60, borderColor()));
        avatarWrap.setPadding(0, 0, 0, 0);
        avatarWrap.setOnClickListener(view -> showProfilePhotoOptions());
        if (Build.VERSION.SDK_INT >= 21) {
            avatarWrap.setClipToOutline(true);
        }

        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setBackground(roundWithStroke(surfaceColor(), 60, fieldBorderColor()));
        avatar.setPadding(0, 0, 0, 0);
        avatar.setLayoutParams(new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER));
        avatar.setImageResource(R.drawable.ic_field_person);
        avatar.setColorFilter(accentColor());
        if (Build.VERSION.SDK_INT >= 21) {
            avatar.setClipToOutline(true);
        }

        String savedProfileUri = account.getString("profile_image_uri", null);
        if (savedProfileUri != null && !savedProfileUri.trim().isEmpty()
                && setImageFromUri(avatar, Uri.parse(savedProfileUri))) {
            avatar.clearColorFilter();
        }
        if (selectedProfileImage != null) {
            if (setImageFromUri(avatar, selectedProfileImage)) avatar.clearColorFilter();
        }
        if (capturedProfileImage != null) {
            avatar.clearColorFilter();
            avatar.setImageBitmap(capturedProfileImage);
        }

        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER);
        avatarWrap.addView(avatar, avatarParams);
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(dp(118), dp(118));
        avatarLayout.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(avatarWrap, avatarLayout);

        TextView photoHint = text("Tap to upload photo", 10, secondaryTextColor(), Typeface.NORMAL);
        photoHint.setGravity(Gravity.CENTER);
        root.addView(photoHint, contentParams(-1, dp(20), dp(10)));

        String subscriptionStatus = annualSubscriptionStatusText();
        TextView subscriptionMeta = text(subscriptionStatus, 11, secondaryTextColor(), Typeface.NORMAL);
        subscriptionMeta.setGravity(Gravity.CENTER);
        subscriptionMeta.setPadding(dp(8), 0, dp(8), 0);
        root.addView(subscriptionMeta, contentParams(-1, dp(20), dp(10)));

        EditText firstName = field("First name");
        EditText surname = field("Surname");
        String savedFullName = account.getString("full_name", "").trim();
        String[] nameParts = savedFullName.split("\\s+", 2);
        firstName.setText(nameParts.length > 0 ? nameParts[0] : "");
        surname.setText(nameParts.length > 1 ? nameParts[1] : "");

        addProfileField(root, "Username (login)", account.getString("username", ""));

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.addView(labeledField("First name", firstName), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams surnameParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        surnameParams.setMargins(dp(8), 0, 0, 0);
        nameRow.addView(labeledField("Surname", surname), surnameParams);
        root.addView(nameRow, contentParams(-1, dp(76), dp(4)));

        EditText email = field("Email address");
        email.setText(account.getString("email", ""));
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String emailValue = email.getText().toString().trim();
                if (!emailValue.isEmpty() && !validEmail(emailValue)) {
                    email.setError("Email invalid");
                } else {
                    email.setError(null);
                }
            }
        });

        EditText mobile = field("Mobile number");
        mobile.setText(account.getString("mobile", ""));
        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
        mobile.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(10),
                (source, start, end, destination, destinationStart, destinationEnd) -> {
                    StringBuilder digits = new StringBuilder();
                    for (int index = start; index < end; index++) {
                        char character = source.charAt(index);
                        if (Character.isDigit(character)) digits.append(character);
                    }
                    return digits.toString();
                }
        });
        mobile.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String mobileValue = mobile.getText().toString().trim();
                if (!mobileValue.isEmpty() && !mobileValue.matches("^\\d{10}$")) {
                    mobile.setError("Mobile invalid");
                } else {
                    mobile.setError(null);
                }
            }
        });

        TextView emailVerify = filledButton("Verify", GOLD, GOLD_ON);
        emailVerify.setPadding(dp(12), 0, dp(12), 0);
        emailVerify.setOnClickListener(view -> {
            String emailValue = email.getText().toString().trim();
            if (!validEmail(emailValue)) {
                email.setError("Email invalid");
                email.requestFocus();
                return;
            }
            emailVerify.setEnabled(false);
            emailVerify.setText("Sending...");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                emailVerify.setText("Verify");
                emailVerify.setEnabled(true);
                Toast.makeText(this, "Sign in to verify your email", Toast.LENGTH_LONG).show();
                return;
            }
                FirebaseUser user = auth.getCurrentUser();
                com.google.android.gms.tasks.Task<Void> verificationTask = emailValue.equalsIgnoreCase(user.getEmail() == null ? "" : user.getEmail().trim())
                    ? user.sendEmailVerification()
                    : user.verifyBeforeUpdateEmail(emailValue);
                verificationTask
                    .addOnSuccessListener(result -> {
                        account.edit().putString("email", emailValue).apply();
                        emailVerify.setText("Sent");
                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(error -> {
                        emailVerify.setText("Verify");
                        emailVerify.setEnabled(true);
                        Toast.makeText(this, "Could not send verification email: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
        refreshEmailVerificationState(email, emailVerify);

        TextView mobileVerify = filledButton("Verify OTP", GOLD, GOLD_ON);
        mobileVerify.setPadding(dp(10), 0, dp(10), 0);
        mobileVerify.setOnClickListener(view -> verifyProfileMobile(mobile, mobileVerify));
        String verifiedMobile = account.getString("mobile", "").trim();
        boolean mobileAlreadyVerified = account.getBoolean("mobile_verified", false)
                && verifiedMobile.equals(mobile.getText().toString().trim());
        if (mobileAlreadyVerified) {
            mobileVerify.setText("Verified");
            mobileVerify.setEnabled(false);
        }
        mobile.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                if (!verifiedMobile.equals(value.toString().trim())) {
                    mobileVerify.setText("Verify OTP");
                    mobileVerify.setEnabled(true);
                }
            }
            @Override public void afterTextChanged(Editable value) { }
        });

        Map<String, String[]> stateCities = indiaStateCityMap();
        String[] states = stateCities.keySet().toArray(new String[0]);
        Arrays.sort(states, 1, states.length);
        AutoCompleteTextView stateSearch = new AutoCompleteTextView(this);
        AutoCompleteTextView citySearch = new AutoCompleteTextView(this);
        stateSearch.setText(account.getString("state", ""));
        citySearch.setText(account.getString("city", ""));
        stateSearch.setHint("");
        citySearch.setHint("");
        stateSearch.setSingleLine(true);
        citySearch.setSingleLine(true);
        stateSearch.setInputType(InputType.TYPE_NULL);
        citySearch.setInputType(InputType.TYPE_NULL);
        stateSearch.setFocusable(false);
        citySearch.setFocusable(false);
        citySearch.setEnabled(false);

        stateSearch.setOnClickListener(clickedView -> showStatePicker(states, stateSearch, citySearch, stateCities));
        citySearch.setOnClickListener(clickedView -> {
            String selectedState = stateSearch.getText().toString().trim();
            String[] cities = stateCities.getOrDefault(selectedState, new String[]{"Select city"});
            if (citySearch.isEnabled()) showCityPicker(selectedState, cities, citySearch);
        });

        String[] selectedCities = stateCities.getOrDefault(stateSearch.getText().toString().trim(), new String[0]);
        citySearch.setEnabled(selectedCities.length > 0 && !"Select city".equals(selectedCities[0]));

        LinearLayout locationRow = new LinearLayout(this);
        locationRow.setOrientation(LinearLayout.HORIZONTAL);
        locationRow.addView(labeledCitySearch("State", stateSearch), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams cityParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        cityParams.setMargins(dp(8), 0, 0, 0);
        locationRow.addView(labeledCitySearch("City", citySearch), cityParams);
        root.addView(locationRow, contentParams(-1, dp(76), dp(4)));

        addEditableProfileField(root, "Email", email, emailVerify);
        addEditableProfileField(root, "Mobile", mobile, mobileVerify);
        EditText[] changePinCells = pinCells();
        addLabeledPinField(root, "New PIN", changePinCells);
        TextView changePinButton = actionButton("Change PIN", false);
        changePinButton.setOnClickListener(view -> {
            String newPin = pinValue(changePinCells);
            changeAccountPin(account.getString("username", ""), getStoredAccountPin(), newPin, changePinButton);
        });
        addField(root, changePinButton);

        TextView save = actionButton("Save changes", true);
        save.setOnClickListener(view -> {
            String updatedFirstName = firstName.getText().toString().trim();
            String updatedSurname = surname.getText().toString().trim();
            String updatedFullName = (updatedFirstName + " " + updatedSurname).trim();
            String updatedEmail = email.getText().toString().trim();
            String updatedMobile = mobile.getText().toString().trim();
            String selectedState = stateSearch.getText().toString().trim();
            String selectedCity = citySearch.getText().toString().trim();
            String[] selectedStateCities = stateCities.getOrDefault(selectedState, new String[0]);
            boolean cityMatchesState = Arrays.asList(selectedStateCities).contains(selectedCity);
            if (updatedFullName.isEmpty()) {
                firstName.setError("Enter first name");
                firstName.requestFocus();
                return;
            }
            if (!updatedEmail.isEmpty() && !validEmail(updatedEmail)) {
                email.setError("Email invalid");
                email.requestFocus();
                return;
            }
            if (!updatedMobile.isEmpty() && !updatedMobile.matches("^\\d{10}$")) {
                mobile.setError("Mobile invalid");
                mobile.requestFocus();
                return;
            }
            if (selectedState == null || selectedState.trim().isEmpty() || "Select state".equals(selectedState) || selectedCity.isEmpty() || "Select city".equals(selectedCity) || !cityMatchesState) {
                Toast.makeText(this, "Please select your state and city", Toast.LENGTH_LONG).show();
                stateSearch.requestFocus();
                return;
            }
            if (selectedProfileImage != null) {
                account.edit().putString("profile_image_uri", selectedProfileImage.toString()).apply();
            }
            account.edit()
                    .putString("full_name", updatedFullName)
                    .putString("email", updatedEmail)
                    .putString("mobile", updatedMobile)
                        .putBoolean("mobile_verified", account.getBoolean("mobile_verified", false)
                            && updatedMobile.equals(account.getString("mobile", "").trim()))
                    .putString("state", selectedState)
                    .putString("city", selectedCity)
                    .apply();
                    syncProfileWithBackend();
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });
        root.addView(save, contentParams(-1, dp(44), dp(10)));

        TextView home = actionButton("Back home", false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
    }

    private LinearLayout screenBase(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(responsiveHorizontalPadding(), dp(16), responsiveHorizontalPadding(), dp(30));
        root.setBackgroundColor(backgroundColor());
        if (Build.VERSION.SDK_INT >= 29) root.setForceDarkAllowed(false);
        addAppControls(root, false);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        activeContent = new LinearLayout(this);
        activeContent.setOrientation(LinearLayout.VERTICAL);
        activeContent.setPadding(0, dp(26), 0, 0);
        scroll.addView(activeContent, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        if (!darkMode) root.post(() -> forceLightModeText(root));
        return activeContent;
    }

    private void addHeading(String title, String subtitle) {
        TextView titleView = text(translate(title), 20, primaryTextColor(), Typeface.NORMAL);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        activeContent.addView(titleView, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitleView = text(translate(subtitle), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        addField(activeContent, subtitleView);
    }

    private void addCenteredHeading(String title, String subtitle) {
        TextView titleView = text(translate(title), 20, primaryTextColor(), Typeface.NORMAL);
        titleView.setGravity(Gravity.CENTER);
        activeContent.addView(titleView, new LinearLayout.LayoutParams(-1, dp(32)));
        TextView subtitleView = text(translate(subtitle), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitleView.setGravity(Gravity.CENTER);
        addField(activeContent, subtitleView);
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        if (Build.VERSION.SDK_INT >= 29) input.setForceDarkAllowed(false);
        input.setId(View.generateViewId());
        input.setHint("");
        float fontScale = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        input.setTextSize(14 * Math.max(1.0f, Math.min(1.2f, fontScale)));
        input.setTag(Float.valueOf(14));
        input.setTextColor(!darkMode ? LIGHT_TEXT : primaryTextColor());
        input.setHintTextColor(!darkMode ? LIGHT_TEXT : Color.WHITE);
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        input.setPadding(dp(16), dp(8), dp(16), dp(8));
        input.setIncludeFontPadding(false);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        input.setOnFocusChangeListener((view, focused) ->
            input.setBackground(roundWithStroke(surfaceColor(), 10, focused ? accentColor() : fieldBorderColor())));
        enableAutoCapitalize(input, hint);
        return input;
    }

    private void enableAutoCapitalize(EditText input, String hint) {
        if (hint == null || hint.toLowerCase(Locale.US).contains("email")) return;
        input.addTextChangedListener(new TextWatcher() {
            private boolean selfUpdating = false;
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable value) {
                if (selfUpdating || value == null || value.length() == 0) return;
                String current = value.toString();
                String trimmed = current.trim();
                if (trimmed.isEmpty()) return;
                char first = trimmed.charAt(0);
                if (Character.isLetter(first) && Character.isLowerCase(first)) {
                    String capitalized = Character.toUpperCase(first) + trimmed.substring(1);
                    if (!capitalized.equals(current)) {
                        selfUpdating = true;
                        input.setText(capitalized);
                        input.setSelection(capitalized.length());
                        selfUpdating = false;
                    }
                }
            }
        });
    }

    private void applyFieldIcon(EditText input, String label) {
        String value = label == null ? "" : label.toLowerCase(Locale.US);
        int icon = R.drawable.ic_field_info;
        if (value.contains("user") || value.contains("name")) {
            icon = R.drawable.ic_field_person;
        } else if (value.contains("pin") || value.contains("code") || value.contains("password")) {
            icon = R.drawable.ic_field_lock;
        } else if (value.contains("email")) {
            icon = R.drawable.ic_field_email;
        } else if (value.contains("mobile") || value.contains("phone")) {
            icon = R.drawable.ic_field_phone;
        } else if (value.contains("state") || value.contains("city") || value.contains("location") || value.contains("place")) {
            icon = R.drawable.ic_field_location;
        } else if (value.contains("item")) {
            icon = R.drawable.ic_field_search;
        } else if (value.contains("date") || value.contains("time")) {
            icon = R.drawable.ic_field_calendar;
        }
        applyIcon(input, icon);
    }

    private void applyIcon(TextView view, int icon) {
        Drawable fieldIcon = getDrawable(icon);
        if (fieldIcon != null) {
            fieldIcon = fieldIcon.mutate();
            fieldIcon.setTint(accentColor());
            fieldIcon.setBounds(0, 0, dp(24), dp(24));
        }
        view.setCompoundDrawables(fieldIcon, null, null, null);
        view.setCompoundDrawablePadding(dp(12));
        view.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        view.setIncludeFontPadding(false);
        view.setAlpha(1.0f);
    }

    private TextView actionButton(String label, boolean primary) {
        return primary ? filledButton(label, GOLD, GOLD_ON) : outlinedButton(label);
    }

    private LinearLayout premiumCard(String title, String detail) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(12), dp(18), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 18, borderColor()));

        TextView titleView = text(title, 18, secondaryTextColor(), Typeface.NORMAL);
        titleView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        titleView.setIncludeFontPadding(false);
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

        TextView detailView = text("", 20, primaryTextColor(), Typeface.NORMAL);
        detailView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        detailView.setIncludeFontPadding(false);
        detailView.setLineSpacing(0f, 1f);
        detailView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

        SpannableString detailText = new SpannableString("₹99/year");
        int priceLength = "₹99".length();
        detailText.setSpan(new StyleSpan(Typeface.BOLD), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailText.setSpan(new ForegroundColorSpan(primaryTextColor()), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailText.setSpan(new ForegroundColorSpan(secondaryTextColor()), priceLength, detailText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailView.setText(detailText);

        card.addView(titleView, new LinearLayout.LayoutParams(0, -2, 1f));
        card.addView(detailView, new LinearLayout.LayoutParams(-2, -2));
        return card;
    }

    private TextView chip(String label, int background, int foreground) {
        TextView chip = text(label, 12, foreground, Typeface.NORMAL);
        chip.setMinWidth(0);
        chip.setGravity(Gravity.CENTER);
        chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        if (background == LOST_GREEN) {
            chip.setTextColor(Color.WHITE);
            chip.setBackground(round(background, 24));
        } else {
            chip.setTextColor(primaryTextColor());
            chip.setBackground(roundWithStroke(background, 24, borderColor()));
        }
        return chip;
    }

    private LinearLayout reportRow(String title, String detail) {
        return reportRow(title, detail, (Runnable) null);
    }
    private LinearLayout reportRow(String title, String detail, Runnable editAction, boolean showEdit) {
        return showEdit ? reportRow(title, detail, editAction) : reportRow(title, detail);
    }

    private LinearLayout reportRow(String title, String detail, Runnable editAction, boolean showEdit, Runnable deleteAction) {
        LinearLayout row = reportRow(title, detail, editAction, showEdit);
        TextView delete = actionButton("Delete", false);
        delete.setOnClickListener(view -> deleteAction.run());
        row.addView(delete, new LinearLayout.LayoutParams(dp(82), dp(40)));
        return row;
    }

    private LinearLayout reportRow(String title, String detail, Runnable editAction) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(18, 8, 18, 8);
        row.setBackground(round(surfaceColor(), 18));
        row.setTag("reportRow");
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = text(title, 15, primaryTextColor(), Typeface.BOLD);
        titleView.setIncludeFontPadding(false);
        TextView detailView = text(detail, 10, secondaryTextColor(), Typeface.NORMAL);
        detailView.setIncludeFontPadding(false);
        content.addView(titleView);
        content.addView(detailView);
        row.addView(content, new LinearLayout.LayoutParams(0, -2, 1f));
        if (editAction != null) {
            TextView edit = actionButton("Edit", false);
            edit.setOnClickListener(view -> editAction.run());
            row.addView(edit, new LinearLayout.LayoutParams(dp(76), dp(40)));
        }
        return row;
    }

    private void addProfileField(LinearLayout parent, String label, String value) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));

        TextView valueView = text(value, 14, primaryTextColor(), Typeface.NORMAL);
        valueView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        valueView.setPadding(dp(16), dp(8), dp(16), dp(8));
        valueView.setSingleLine(true);
        valueView.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        group.addView(valueView, new LinearLayout.LayoutParams(-1, dp(46)));
        parent.addView(group, contentParams(-1, dp(76), dp(4)));
    }

    private void addEditableProfileField(LinearLayout parent, String label, EditText input, TextView verifyButton) {
        if (input != null && input.getParent() instanceof ViewGroup) {
            ((ViewGroup) input.getParent()).removeView(input);
        }
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        input.setLayoutParams(new LinearLayout.LayoutParams(0, dp(46), 1f));
        input.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        input.setPadding(dp(16), dp(8), dp(16), dp(8));
        input.setSingleLine(true);
        input.setEnabled(!"Location".equals(label));
        if (verifyButton != null) {
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-2, dp(34));
            buttonParams.setMargins(dp(8), 0, 0, 0);
            verifyButton.setLayoutParams(buttonParams);
            row.addView(input);
            row.addView(verifyButton);
        } else {
            row.addView(input);
        }
        group.addView(row, new LinearLayout.LayoutParams(-1, dp(46)));
        parent.addView(group, contentParams(-1, dp(76), dp(4)));
    }

    private void addField(LinearLayout parent, View child) {
        int height = "reportRow".equals(child.getTag()) ? dp(82) : dp(48);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, height);
        params.setMargins(0, 0, 0, dp(14));
        parent.addView(child, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams contentParams(int width, int height, int bottomMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        if (bottomMargin > 0) params.setMargins(0, 0, 0, bottomMargin);
        return params;
    }

    private LinearLayout labeledField(String label, EditText input) {
        if (input != null && input.getParent() instanceof ViewGroup) {
            ((ViewGroup) input.getParent()).removeView(input);
        }
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, dp(46));
        inputParams.topMargin = dp(4);
        if (input != null) {
            if (input.getParent() instanceof ViewGroup) {
                ((ViewGroup) input.getParent()).removeView(input);
            }
            group.addView(input, inputParams);
        }
        return group;
    }

    private LinearLayout fieldLabel(String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(getIconForLabel(label));
        icon.setColorFilter(accentColor());
        icon.setContentDescription(label + " icon");
        row.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(20)));

        TextView labelView = text(label, 10, secondaryTextColor(), Typeface.BOLD);
        labelView.setLetterSpacing(.04f);
        labelView.setGravity(Gravity.CENTER_VERTICAL);
        labelView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, dp(20));
        labelParams.setMargins(dp(8), 0, 0, 0);
        row.addView(labelView, labelParams);
        return row;
    }

    private int getIconForLabel(String label) {
        String value = label == null ? "" : label.toLowerCase(Locale.US);
        if (value.contains("user") || value.contains("name")) return R.drawable.ic_field_person;
        if (value.contains("pin") || value.contains("code") || value.contains("password")) return R.drawable.ic_field_lock;
        if (value.contains("email")) return R.drawable.ic_field_email;
        if (value.contains("mobile") || value.contains("phone")) return R.drawable.ic_field_phone;
        if (value.contains("state") || value.contains("city") || value.contains("location") || value.contains("place")) return R.drawable.ic_field_location;
        if (value.contains("item")) return R.drawable.ic_field_search;
        if (value.contains("date") || value.contains("time")) return R.drawable.ic_field_calendar;
        return R.drawable.ic_field_info;
    }

    private void addLabeledField(LinearLayout parent, String label, EditText input) {
        if (input != null && input.getParent() instanceof ViewGroup) {
            ((ViewGroup) input.getParent()).removeView(input);
        }
        parent.addView(labeledField(label, input), contentParams(-1, dp(76), dp(4)));
    }

    private boolean containsMobileKeyword(String value) {
        return value != null && value.toLowerCase(Locale.US).contains("mobile");
    }

    private void addLabeledPinField(LinearLayout parent, String label, EditText[] cells) {
        for (EditText cell : cells) {
            if (cell != null && cell.getParent() instanceof ViewGroup) {
                ((ViewGroup) cell.getParent()).removeView(cell);
            }
        }
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int index = 0; index < cells.length; index++) {
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
            if (index > 0) cellParams.setMargins(dp(8), 0, 0, 0);
            row.addView(cells[index], cellParams);
        }
        group.addView(row, new LinearLayout.LayoutParams(-1, dp(46)));
        parent.addView(group, contentParams(-1, dp(76), dp(4)));
    }

    private EditText[] pinCells() {
        EditText[] cells = new EditText[4];
        for (int index = 0; index < cells.length; index++) {
            EditText cell = new EditText(this);
            if (Build.VERSION.SDK_INT >= 29) cell.setForceDarkAllowed(false);
            cell.setId(View.generateViewId());
            cell.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            cell.setTextColor(primaryTextColor());
            cell.setHintTextColor(secondaryTextColor());
            cell.setTextColor(!darkMode ? LIGHT_TEXT : primaryTextColor());
            cell.setHintTextColor(!darkMode ? LIGHT_TEXT : secondaryTextColor());
            cell.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            cell.setTextSize(14);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, 0, 0, 0);
            cell.setSingleLine(true);
            cell.setSelectAllOnFocus(true);
            cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            cell.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            cell.setImeOptions(index == cells.length - 1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            final int cellIndex = index;
            cell.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }

                @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                    if (value.length() == 1) {
                        if (cellIndex < cells.length - 1) {
                            cells[cellIndex + 1].requestFocus();
                        } else {
                            InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            if (keyboard != null) {
                                keyboard.hideSoftInputFromWindow(cell.getWindowToken(), 0);
                            }
                        }
                    }
                }

                @Override public void afterTextChanged(Editable value) { }
            });
            cell.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT && cellIndex < cells.length - 1) {
                    cells[cellIndex + 1].requestFocus();
                    cells[cellIndex + 1].setSelection(cells[cellIndex + 1].length());
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (keyboard != null) {
                        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            });
            cell.setOnKeyListener((view, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && cell.getText().length() == 0
                        && cellIndex > 0) {
                    EditText previous = cells[cellIndex - 1];
                    previous.requestFocus();
                    previous.setSelection(previous.length());
                    return true;
                }
                return false;
            });
            cells[index] = cell;
        }
        return cells;
    }

    private String pinValue(EditText[] cells) {
        StringBuilder value = new StringBuilder(4);
        for (EditText cell : cells) value.append(cell.getText());
        return value.toString();
    }

    private TextView filledButton(String label, int background, int foreground) {
        TextView button = text(label, 12, foreground, Typeface.NORMAL);
        button.setTextColor(background == LOST_GREEN ? Color.WHITE : foreground);
        button.setGravity(Gravity.CENTER);
        button.setBackground(round(background, 24));
        return button;
    }

    private TextView outlinedButton(String label) {
        TextView button = text(label, 12, primaryTextColor(), Typeface.NORMAL);
        button.setGravity(Gravity.CENTER);
        button.setBackground(roundWithStroke(surfaceColor(), 24, borderColor()));
        return button;
    }

    private TextView reportTypeToggle(String label, boolean selected, int selectedBackground, int selectedForeground) {
        TextView toggle = text(label, 11, selected ? selectedForeground : secondaryTextColor(), Typeface.NORMAL);
        if (selected && selectedBackground == LOST_GREEN) toggle.setTextColor(Color.WHITE);
        toggle.setGravity(Gravity.CENTER);
        toggle.setBackground(selected
                ? round(selectedBackground, 24)
                : roundWithStroke(surfaceColor(), 24, fieldBorderColor()));
        return toggle;
    }

    private LinearLayout subscriptionCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(12), dp(18), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 18, borderColor()));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.HORIZONTAL);
        copy.setAlpha(1.0f);
        copy.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

        TextView subscriptionLabel = text("Annual subscription", 18, secondaryTextColor(), Typeface.NORMAL);
        subscriptionLabel.setIncludeFontPadding(false);
        subscriptionLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        copy.addView(subscriptionLabel, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView subscriptionPrice = text("", 22, primaryTextColor(), Typeface.NORMAL);
        subscriptionPrice.setIncludeFontPadding(false);
        subscriptionPrice.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        SpannableString priceText = new SpannableString("₹99/year");
        int priceLength = "₹99".length();
        priceText.setSpan(new StyleSpan(Typeface.BOLD), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceText.setSpan(new ForegroundColorSpan(primaryTextColor()), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceText.setSpan(new ForegroundColorSpan(secondaryTextColor()), priceLength, priceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        subscriptionPrice.setText(priceText);

        copy.addView(subscriptionPrice, new LinearLayout.LayoutParams(-2, -2));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1f));

        ImageView lock = new ImageView(this);
        lock.setImageResource(R.drawable.ic_premium_lock);
        lock.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        lock.setColorFilter(GOLD);
        card.addView(lock, new LinearLayout.LayoutParams(dp(28), dp(28)));
        return card;
    }

    private LinearLayout adminItemCard(String type, String item, int accent, boolean showIndicator) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(roundWithStroke(surfaceColor(), 16, borderColor()));
        TextView label = text(type + (showIndicator ? "  •" : ""), 9, accent, Typeface.NORMAL);
        label.setGravity(Gravity.CENTER);
        card.addView(label, new LinearLayout.LayoutParams(-1, dp(20)));
        TextView title = text(item, 11, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        card.addView(title, new LinearLayout.LayoutParams(-1, dp(26)));
        return card;
    }

    private LinearLayout aiMatchCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(roundWithStroke(surfaceColor(), 16, borderColor()));

        LinearLayout badge = new LinearLayout(this);
        badge.setGravity(Gravity.CENTER_VERTICAL);
        badge.setPadding(dp(8), 0, dp(8), 0);
        badge.setBackground(round(Color.rgb(19, 42, 30), 8));

        ImageView spark = new ImageView(this);
        spark.setImageResource(R.drawable.ic_premium_spark);
        spark.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        spark.setColorFilter(SuccessMintColor());
        badge.addView(spark, new LinearLayout.LayoutParams(dp(24), dp(24)));

        TextView chip = text("AI match — 94%", 10, SuccessMintColor(), Typeface.NORMAL);
        chip.setTextColor(SuccessMintColor());
        chip.setGravity(Gravity.CENTER);
        chip.setIncludeFontPadding(false);
        chip.setSingleLine(true);
        chip.setEllipsize(android.text.TextUtils.TruncateAt.END);
        chip.setPadding(dp(6), 0, 0, 0);
        badge.addView(chip, new LinearLayout.LayoutParams(0, dp(32), 1f));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-1, dp(36));
        badgeParams.gravity = Gravity.CENTER_HORIZONTAL;
        card.addView(badge, badgeParams);

        TextView note = text("Both users' contact details visible to you only", 11, secondaryTextColor(), Typeface.NORMAL);
        note.setGravity(Gravity.CENTER);
        note.setSingleLine(false);
        note.setMaxLines(2);
        note.setEllipsize(null);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
        noteParams.setMargins(0, dp(8), 0, dp(8));
        card.addView(note, noteParams);

        LinearLayout notify = new LinearLayout(this);
        notify.setOrientation(LinearLayout.HORIZONTAL);
        notify.setGravity(Gravity.CENTER);
        notify.setBackground(round(GOLD, 24));
        notify.setOnClickListener(view -> Toast.makeText(this, "User notification queued", Toast.LENGTH_SHORT).show());
        ImageView notifyIcon = new ImageView(this);
        notifyIcon.setImageResource(R.drawable.ic_premium_spark);
        notifyIcon.setColorFilter(GOLD_ON);
        notify.addView(notifyIcon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView notifyLabel = text("Notify user", 12, GOLD_ON, Typeface.NORMAL);
        notifyLabel.setTextColor(GOLD_ON);
        notifyLabel.setGravity(Gravity.CENTER_VERTICAL);
        notifyLabel.setIncludeFontPadding(false);
        LinearLayout.LayoutParams notifyLabelParams = new LinearLayout.LayoutParams(-2, dp(44));
        notifyLabelParams.setMargins(dp(6), 0, 0, 0);
        notify.addView(notifyLabel, notifyLabelParams);
        card.addView(notify, new LinearLayout.LayoutParams(-1, dp(44)));
        return card;
    }

    private int SuccessMintColor() {
        return Color.rgb(95, 201, 152);
    }

    private void requestLocation(TextView button, TextView toggle) {
        locationStatus = button;
        locationToggleStatus = toggle;
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 702);
            if (button != null) button.setText("Location permission requested");
            return;
        }
        enableLocationServicesIfNeeded(button, toggle);
    }

    private void enableLocationServicesIfNeeded(TextView button, TextView toggle) {
        try {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (manager == null) {
                if (button != null) button.setText("Location unavailable");
                return;
            }

            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gpsEnabled && !networkEnabled) {
                if (button != null) {
                    button.setText("Turn on location");
                    button.setTextColor(secondaryTextColor());
                    button.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
                }
                Toast.makeText(this, "Please turn on device location to use precise location.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            updateLocation(button, toggle);
        } catch (Exception error) {
            if (button != null) button.setText("Location unavailable");
        }
    }

    private void updateLocation(TextView button, TextView toggle) {
        try {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (manager == null) {
                if (button != null) button.setText("Precise location  OFF");
                return;
            }

            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gpsEnabled && !networkEnabled) {
                if (button != null) {
                    button.setText("Precise location  OFF");
                    button.setTextColor(secondaryTextColor());
                    button.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
                }
                Toast.makeText(this, "Please turn on your mobile location to continue.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }

            Location best = null;
            for (String provider : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
                Location candidate = manager.getLastKnownLocation(provider);
                if (candidate != null && (best == null || candidate.getTime() > best.getTime())) best = candidate;
            }
            if (best == null) {
                button.setText("Precise location  OFF");
                button.setTextColor(secondaryTextColor());
                button.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
                return;
            }
            currentLat = best.getLatitude();
            currentLng = best.getLongitude();
            hasLocation = true;
            button.setText(String.format(Locale.US, "Location ready: %.4f, %.4f", currentLat, currentLng));
            if (toggle != null) {
                toggle.setText("Precise location  ON");
                toggle.setTextColor(Color.WHITE);
                toggle.setBackground(roundWithStroke(LOST_GREEN, 10, LOST_GREEN));
            }
        } catch (SecurityException error) {
            if (button != null) button.setText("Location permission required");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 702 && locationStatus != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationServicesIfNeeded(locationStatus, locationToggleStatus);
            } else {
                locationStatus.setText("Location permission denied");
            }
        }
        if (requestCode == REQUEST_IMAGE_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingImageSlot >= 0) {
            openGalleryForSlot(pendingImageSlot);
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingImageSlot >= 0) {
            openCameraForSlot(pendingImageSlot);
        }
        if (requestCode == REQUEST_PROFILE_IMAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openProfilePhotoPicker();
        }
        if (requestCode == REQUEST_PROFILE_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openProfilePhotoCamera();
        }
        if (requestCode == REQUEST_IMEI_SCAN && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && imeiScanTarget != null) {
            openImeiScanner(imeiScanTarget);
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
        if (requestCode >= 710 && requestCode <= 712 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            int slot = requestCode - 710;
            reportImages[slot] = data.getData();
            reportCameraImages[slot] = null;
            selectedImage = reportImages[0];
            if (currentReportType != null) showReport(currentReportType);
        }
        if (requestCode >= 720 && requestCode <= 722 && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            int slot = requestCode - 720;
            reportCameraImages[slot] = (Bitmap) data.getExtras().get("data");
            reportImages[slot] = null;
            capturedImage = reportCameraImages[0];
            if (currentReportType != null) showReport(currentReportType);
        }
        if (requestCode == REQUEST_PROFILE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedProfileImage = data.getData();
            capturedProfileImage = null;
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .putString("profile_image_uri", selectedProfileImage.toString())
                    .apply();
            if (currentPage == PAGE_PROFILE) showProfile();
        }
        if (requestCode == REQUEST_PROFILE_CAMERA && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            capturedProfileImage = (Bitmap) data.getExtras().get("data");
            selectedProfileImage = null;
            if (capturedProfileImage != null) {
                String savedPath = saveProfileBitmap(capturedProfileImage);
                if (savedPath != null) {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                            .putString("profile_image_uri", savedPath)
                            .apply();
                }
            }
            if (currentPage == PAGE_PROFILE) showProfile();
        }

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            String normalized = "";
            if (scanResult != null && scanResult.getContents() != null) {
                String scanned = scanResult.getContents().trim().replaceAll("\\D", "");
                if (!scanned.isEmpty()) {
                    normalized = scanned.length() > 15 ? scanned.substring(0, 15) : scanned;
                }
            }
            if (!normalized.isEmpty()) {
                draftImei = normalized;
                if (imeiScanTarget != null) {
                    imeiScanTarget.setText(normalized);
                    imeiScanTarget.setSelection(normalized.length());
                    imeiScanTarget.setError(null);
                }
            }
            imeiScanTarget = null;
            if (currentReportType != null) {
                showReport(currentReportType);
            }
        }
    }

    private String saveProfileBitmap(Bitmap bitmap) {
        try {
            File folder = new File(getFilesDir(), "profile_images");
            if (!folder.exists() && !folder.mkdirs()) return null;
            File imageFile = new File(folder, "profile_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream output = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);
            }
            return imageFile.getAbsolutePath();
        } catch (Exception error) {
            return null;
        }
    }

    private void showAdminDashboard() {
        currentPage = PAGE_ADMIN;
        screenRenderer = this::showAdminDashboard;
        LinearLayout root = screenBase("");
        root.addView(text("Search user reports", 15, primaryTextColor(), Typeface.NORMAL), contentParams(-1, dp(22), dp(14)));
        EditText search = field("Name, surname, mobile, username, or email");
        root.addView(search, contentParams(-1, dp(52), dp(8)));
        TextView searchButton = actionButton("Search", true);
        root.addView(searchButton, contentParams(-1, dp(44), dp(12)));
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results, contentParams(-1, -2, 0));
        searchButton.setOnClickListener(view -> searchAdminUsers(search.getText().toString().trim(), results, searchButton));
    }

    private void searchAdminUsers(String query, LinearLayout results, TextView button) {
        if (query.length() < 2) {
            Toast.makeText(this, "Enter at least 2 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        button.setText("Searching...");
        button.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = fetchAdminSearch(query, token.getToken());
            runOnUiThread(() -> {
                button.setText("Search");
                button.setEnabled(true);
                results.removeAllViews();
                if (response == null) {
                    addField(results, text("Search unavailable.", 14, secondaryTextColor(), Typeface.NORMAL));
                    return;
                }
                try {
                    JSONArray users = new JSONArray(response);
                    if (users.length() == 0) {
                        addField(results, text("No matching users.", 14, secondaryTextColor(), Typeface.NORMAL));
                        return;
                    }
                    for (int index = 0; index < users.length(); index++) {
                        JSONObject result = users.getJSONObject(index);
                        JSONObject user = result.optJSONObject("user");
                        String identity = user == null ? "User" : user.optString("full_name", "User") + " · " + user.optString("username", "") + " · " + user.optString("mobile", "");
                        addField(results, text(identity, 14, primaryTextColor(), Typeface.BOLD));
                        JSONArray reports = result.optJSONArray("reports");
                        if (reports == null || reports.length() == 0) {
                            addField(results, text("No reports.", 12, secondaryTextColor(), Typeface.NORMAL));
                        } else {
                            for (int reportIndex = 0; reportIndex < reports.length(); reportIndex++) {
                                JSONObject report = reports.getJSONObject(reportIndex);
                                String imageState = report.optString("image_url", "").isEmpty() ? "No image" : "Image attached";
                                String reportDetails = report.optString("type", "ITEM") + "  ·  " + report.optString("description", "")
                                        + "  ·  " + report.optString("category", "other")
                                        + "  ·  location: " + report.optDouble("lat", 0.0) + ", " + report.optDouble("lng", 0.0)
                                        + "  ·  " + imageState;
                                addField(results, reportRow(report.optString("title", "Untitled"), reportDetails));
                            }
                        }
                    }
                } catch (Exception error) {
                    addField(results, text("Search results could not be read.", 14, secondaryTextColor(), Typeface.NORMAL));
                }
            });
        }));
    }

    private String fetchAdminSearch(String query, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + "/api/admin/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.name())).openConnection();
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

    private void showAdminLoginDialog() {
        EditText username = field("Admin username");
        EditText[] adminPinCells = pinCells();
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(4), 0, dp(4), 0);
        form.addView(username, new LinearLayout.LayoutParams(-1, dp(48)));
        TextView pinLabel = text("4-digit PIN", 11, secondaryTextColor(), Typeface.BOLD);
        pinLabel.setPadding(0, dp(8), 0, dp(6));
        form.addView(pinLabel, new LinearLayout.LayoutParams(-1, dp(30)));
        LinearLayout pinRow = new LinearLayout(this);
        pinRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int index = 0; index < adminPinCells.length; index++) {
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
            if (index > 0) cellParams.setMargins(dp(8), 0, 0, 0);
            pinRow.addView(adminPinCells[index], cellParams);
        }
        form.addView(pinRow, new LinearLayout.LayoutParams(-1, dp(46)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Admin login")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Login", null)
                .create();
        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String adminUsername = username.getText().toString().trim().toLowerCase(Locale.US);
            String adminPin = pinValue(adminPinCells);
            if (!adminUsername.matches("^[a-z0-9_]{3,32}$") || !adminPin.matches("^\\d{4}$")) {
                Toast.makeText(this, "Enter a valid username and 4-digit PIN", Toast.LENGTH_LONG).show();
                return;
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Checking...");
            FirebaseAuth.getInstance().signInWithEmailAndPassword(credentialEmail(adminUsername), credentialPassword(adminUsername, adminPin))
                    .addOnSuccessListener(result -> result.getUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                        String adminItems = fetchAdminItems(token.getToken());
                        runOnUiThread(() -> {
                            if (adminItems == null) {
                                FirebaseAuth.getInstance().signOut();
                                dialog.dismiss();
                                Toast.makeText(this, "Admin access denied", Toast.LENGTH_LONG).show();
                                return;
                            }
                            dialog.dismiss();
                            showAdminDashboard();
                        });
                    })).addOnFailureListener(error -> {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Login");
                        Toast.makeText(this, "Admin verification unavailable", Toast.LENGTH_LONG).show();
                    }))
                    .addOnFailureListener(error -> {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Login");
                        Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_LONG).show();
                    });
        }));
        dialog.show();
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

    private final class AuthLabelView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String label;

        AuthLabelView(Context context, String label) {
            super(context);
            this.label = label;
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(dp(12));
            paint.setColor(Color.BLACK);
            paint.setAlpha(255);
            setAlpha(1f);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(darkMode ? Color.WHITE : LIGHT_TEXT);
            Paint.FontMetrics metrics = paint.getFontMetrics();
            float baseline = (getHeight() - metrics.bottom - metrics.top) / 2f;
            canvas.drawText(label, 0, baseline, paint);
        }
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(translate(value));
        float fontScale = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        view.setTextSize(size * Math.max(1.0f, Math.min(1.2f, fontScale)));
        view.setTag(Float.valueOf(size));
        if (darkMode) {
            view.setTextColor(Color.WHITE);
        } else {
            view.setTextColor(LIGHT_TEXT);
        }
        view.setTypeface(null, style);
        return view;
    }


    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private GradientDrawable roundWithStroke(int color, int radius, int strokeColor) {
        GradientDrawable drawable = round(color, radius);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private void applySystemBarColors() {
        if (Build.VERSION.SDK_INT >= 29) getWindow().getDecorView().setForceDarkAllowed(false);
        getWindow().setStatusBarColor(backgroundColor());
        getWindow().setNavigationBarColor(backgroundColor());
        getWindow().getDecorView().setSystemUiVisibility(darkMode ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void applyThemeInPlace() {
        applySystemBarColors();
        View root = getWindow().getDecorView().getRootView();
        root.setBackgroundColor(backgroundColor());
        updateThemeView(root);
    }

    private void updateThemeView(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(primaryTextColor());
            if (textView instanceof EditText) {
                textView.setHintTextColor(secondaryTextColor());
                textView.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            if ("reportRow".equals(group.getTag())) group.setBackground(round(surfaceColor(), 18));
            for (int index = 0; index < group.getChildCount(); index++) updateThemeView(group.getChildAt(index));
        }
    }

    private int backgroundColor() {
        return darkMode ? BACKGROUND : Color.rgb(250, 249, 246);
    }

    private int surfaceColor() {
        return darkMode ? SURFACE : Color.rgb(255, 255, 255);
    }

    private int primaryTextColor() {
        return darkMode ? Color.WHITE : LIGHT_TEXT;
    }

    private int secondaryTextColor() {
        return darkMode ? Color.WHITE : LIGHT_TEXT;
    }

    private int borderColor() {
        return darkMode ? BORDER : Color.rgb(218, 215, 207);
    }

    private int fieldBorderColor() {
        return darkMode ? FIELD_BORDER : Color.rgb(204, 201, 194);
    }

    private int accentColor() {
        return GOLD;
    }

    private void addAppControls(LinearLayout parent, boolean authScreen) {
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);

        TextView language = text("", 18, secondaryTextColor(), Typeface.NORMAL);
        language.setGravity(Gravity.CENTER);
        language.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        language.setPadding(dp(8), dp(8), dp(8), dp(8));
        language.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_language, 0, 0, 0);
        language.setCompoundDrawablePadding(0);
        language.setElevation(dp(2));
        if (Build.VERSION.SDK_INT >= 21) {
            language.setCompoundDrawableTintList(ColorStateList.valueOf(accentColor()));
        }
        language.setContentDescription("Select language");
        language.setOnClickListener(view -> showLanguagePicker());
        if (authScreen) {
            controls.addView(language, new LinearLayout.LayoutParams(dp(42), dp(42)));
        }

        if (!authScreen) {
            controls.setGravity(Gravity.CENTER_VERTICAL);
            controls.setPadding(0, 0, 0, 0);

            TextView font = text("A", 17, secondaryTextColor(), Typeface.BOLD);
            font.setGravity(Gravity.CENTER);
            font.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            font.setPadding(dp(8), dp(8), dp(8), dp(8));
            font.setContentDescription("Adjust text size");
            font.setOnClickListener(view -> showFontScaleDialog());
            controls.addView(font, new LinearLayout.LayoutParams(dp(42), dp(42)));

            Space leftSpacer = new Space(this);
            controls.addView(leftSpacer, new LinearLayout.LayoutParams(0, 1, 1));

            ImageView logo = new ImageView(this);
            logo.setImageResource(R.drawable.fendly_logo);
            logo.setContentDescription("Fendly logo");
            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
            logo.setAdjustViewBounds(true);
            logo.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(36), dp(36));
            logoParams.topMargin = dp(8);
            controls.addView(logo, logoParams);

            Space rightSpacer = new Space(this);
            controls.addView(rightSpacer, new LinearLayout.LayoutParams(0, 1, 1));

            FrameLayout themeButton = new FrameLayout(this);
            int themeBg = darkMode ? surfaceColor() : Color.rgb(244, 239, 232);
            int themeBorder = darkMode ? borderColor() : Color.rgb(214, 211, 204);
            themeButton.setBackground(roundWithStroke(themeBg, 14, themeBorder));
            themeButton.setPadding(dp(6), dp(6), dp(6), dp(6));
            themeButton.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));
            themeButton.setOnClickListener(view -> {
                darkMode = !darkMode;
                getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
                applySystemBarColors();
                applyThemeInPlace();
            });
            themeButton.setContentDescription(darkMode ? "Switch to light mode" : "Switch to dark mode");

            ImageView themeIcon = new ImageView(this);
            themeIcon.setImageResource(darkMode ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
            themeIcon.setColorFilter(darkMode ? accentColor() : LIGHT_TEXT);
            themeIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            FrameLayout.LayoutParams themeIconParams = new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER);
            themeButton.addView(themeIcon, themeIconParams);

            controls.addView(themeButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
            parent.addView(controls, new LinearLayout.LayoutParams(-1, -2));
            return;
        }

        Space spacer = new Space(this);
        controls.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        FrameLayout themeButton = new FrameLayout(this);
        int themeBg = darkMode ? surfaceColor() : Color.rgb(244, 239, 232);
        int themeBorder = darkMode ? borderColor() : Color.rgb(214, 211, 204);
        themeButton.setBackground(roundWithStroke(themeBg, 14, themeBorder));
        themeButton.setPadding(dp(6), dp(6), dp(6), dp(6));
        themeButton.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));
        themeButton.setOnClickListener(view -> {
            darkMode = !darkMode;
            getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
            applySystemBarColors();
            applyThemeInPlace();
        });
        themeButton.setContentDescription(darkMode ? "Switch to light mode" : "Switch to dark mode");

        ImageView themeIcon = new ImageView(this);
        themeIcon.setImageResource(darkMode ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
        themeIcon.setColorFilter(darkMode ? accentColor() : LIGHT_TEXT);
        themeIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams themeIconParams = new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER);
        themeButton.addView(themeIcon, themeIconParams);

        controls.addView(themeButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
        parent.addView(controls, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showLanguagePicker() {
        String[] languages = {"English", "हिन्दी", "मराठी", "اردو", "ಕನ್ನಡ", "తెలుగు", "বাংলা", "മലയാളം"};
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(20), dp(22), dp(16));

        TextView title = text(localized("language"), 25, primaryTextColor(), Typeface.BOLD);
        title.setIncludeFontPadding(false);
        title.setPadding(dp(2), 0, dp(2), dp(16));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .create();

        for (int index = 0; index < languages.length; index++) {
            final int languageIndex = index;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), 0, dp(12), 0);
            row.setMinimumHeight(dp(54));
            row.setBackground(index == selectedLanguage
                    ? roundWithStroke(Color.argb(darkMode ? 28 : 18, 232, 178, 74), 14, Color.TRANSPARENT)
                    : round(Color.TRANSPARENT, 14));
            row.setOnClickListener(view -> {
                selectedLanguage = languageIndex;
                languagePreferences.edit().putInt("selected_language_index", languageIndex).apply();
                if (screenRenderer != null) screenRenderer.run();
                dialog.dismiss();
            });

            RadioButton indicator = new RadioButton(this);
            indicator.setChecked(index == selectedLanguage);
            indicator.setClickable(false);
            indicator.setFocusable(false);
            indicator.setButtonTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{accentColor(), darkMode ? Color.WHITE : Color.rgb(150, 147, 140)}));
            row.addView(indicator, new LinearLayout.LayoutParams(dp(30), dp(30)));

            TextView label = text(languages[index], 19, primaryTextColor(), Typeface.NORMAL);
            label.setIncludeFontPadding(false);
            label.setGravity(Gravity.CENTER_VERTICAL);
            label.setTextAlignment(languages[index].equals("اردو") ? View.TEXT_ALIGNMENT_VIEW_END : View.TEXT_ALIGNMENT_VIEW_START);
            row.addView(label, new LinearLayout.LayoutParams(-1, -2));
            content.addView(row, new LinearLayout.LayoutParams(-1, dp(58)));
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(Math.min(dp(360), getResources().getDisplayMetrics().widthPixels - dp(32)), -2);
            dialog.getWindow().setDimAmount(0.42f);
        }
        content.setBackground(roundWithStroke(surfaceColor(), 24, borderColor()));
    }

    private void showFontScaleDialog() {
        float current = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(16), dp(20), dp(12));
        content.setBackground(roundWithStroke(surfaceColor(), 20, borderColor()));
        TextView value = text(String.format(Locale.US, "Text size: %d%%", Math.round(current * 100)), 14, primaryTextColor(), Typeface.NORMAL);
        SeekBar slider = new SeekBar(this);
        if (Build.VERSION.SDK_INT >= 21) {
            int sliderTrackColor = darkMode ? Color.WHITE : accentColor();
            slider.setProgressTintList(ColorStateList.valueOf(sliderTrackColor));
            slider.setProgressBackgroundTintList(ColorStateList.valueOf(sliderTrackColor));
            slider.setThumbTintList(ColorStateList.valueOf(accentColor()));
        }
        slider.setMax(20);
        slider.setProgress(Math.round((current - 1.0f) * 100));
        content.addView(value);
        content.addView(slider);
        final boolean[] applied = {false};
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                value.setText(String.format(Locale.US, "Text size: %d%%", 100 + progress));
                applyLiveFontScale(getWindow().getDecorView(), 1.0f + progress / 100.0f);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(8), 0, 0);
        TextView cancelButton = text("Cancel", 14, darkMode ? Color.WHITE : Color.BLACK, Typeface.BOLD);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setMinWidth(dp(92));
        cancelButton.setMinHeight(dp(44));
        cancelButton.setPadding(dp(14), 0, dp(14), 0);
        cancelButton.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        TextView applyButton = text("Apply", 14, darkMode ? Color.BLACK : Color.WHITE, Typeface.BOLD);
        applyButton.setGravity(Gravity.CENTER);
        applyButton.setMinWidth(dp(92));
        applyButton.setMinHeight(dp(44));
        applyButton.setPadding(dp(14), 0, dp(14), 0);
        applyButton.setBackground(round(GOLD, 10));
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(-2, dp(44));
        cancelParams.setMargins(0, 0, dp(8), 0);
        actions.addView(cancelButton, cancelParams);
        actions.addView(applyButton, new LinearLayout.LayoutParams(-2, dp(44)));
        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(52)));
        AlertDialog fontDialog = new AlertDialog.Builder(this)
                .setView(content)
                .create();
        applyButton.setOnClickListener(view -> {
            float scale = 1.0f + slider.getProgress() / 100.0f;
            getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putFloat("font_scale", scale).apply();
            applied[0] = true;
            fontDialog.dismiss();
        });
        cancelButton.setOnClickListener(view -> fontDialog.dismiss());
        fontDialog.setOnShowListener(dialog -> {
            if (fontDialog.getWindow() != null) {
                fontDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
        fontDialog.show();
        fontDialog.setOnDismissListener(dialog -> {
            if (!applied[0]) {
                applyLiveFontScale(getWindow().getDecorView(), current);
            }
        });
    }

    private void applyLiveFontScale(View view, float scale) {
        Object originalSize = view.getTag();
        if (view instanceof TextView && originalSize instanceof Float) {
            ((TextView) view).setTextSize(((Float) originalSize) * scale);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                applyLiveFontScale(group.getChildAt(index), scale);
            }
        }
    }

    private void forceLightModeText(View view) {
        if (view instanceof EditText) {
            EditText input = (EditText) view;
            if (Build.VERSION.SDK_INT >= 29) input.setForceDarkAllowed(false);
            input.setTextColor(LIGHT_TEXT);
            input.setHintTextColor(LIGHT_TEXT);
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (Build.VERSION.SDK_INT >= 29) textView.setForceDarkAllowed(false);
            int current = textView.getCurrentTextColor();
            if (current != Color.WHITE && current != GOLD_ON && current != LOST_GREEN_ON && current != FOUND_GOLD_ON && current != SuccessMintColor()) {
                textView.setTextColor(LIGHT_TEXT);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                forceLightModeText(group.getChildAt(index));
            }
        }
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

    private String loginText(String key) {
        String[][] values = {
                {"Username", "4-digit PIN", "Login", "New here?  Create account"},
                {"उपयोगकर्ता नाम", "4-अंकीय पिन", "लॉग इन", "नए हैं?  खाता बनाएं"},
                {"वापरकर्तानाव", "4-अंकी पिन", "लॉग इन", "नवीन आहात?  खाते तयार करा"},
                {"صارف نام", "4 ہندسوں کا پن", "لاگ ان", "نئے ہیں؟  اکاؤنٹ بنائیں"},
                {"ಬಳಕೆದಾರ ಹೆಸರು", "4-ಅಂಕಿಯ ಪಿನ್", "ಲಾಗ್ ಇನ್", "ಹೊಸಬರೇ?  ಖಾತೆ ರಚಿಸಿ"},
                {"వినియోగదారు పేరు", "4-అంకెల పిన్", "లాగిన్", "కొత్తగా ఉన్నారా?  ఖాతా సృష్టించండి"},
                {"ব্যবহারকারীর নাম", "৪-অঙ্কের পিন", "লগ ইন", "নতুন এখানে?  অ্যাকাউন্ট তৈরি করুন"},
                {"ഉപയോക്തൃനാമം", "4-അക്ക പിൻ", "ലോഗിൻ", "പുതിയ ആളാണോ?  അക്കൗണ്ട് സൃഷ്ടിക്കുക"}
        };
        int language = Math.max(0, Math.min(selectedLanguage, values.length - 1));
        if ("username".equals(key)) return localizeDigits(values[language][0]);
        if ("pin".equals(key)) return localizeDigits(values[language][1]);
        if ("login".equals(key)) return localizeDigits(values[language][2]);
        return localizeDigits(values[language][3]);
    }

    private String translate(String value) {
        String extraTranslation = translateExtra(value);
        if (extraTranslation != null) return localizeDigits(extraTranslation);
        String[] english = {
                "Complete your profile", "A little about you", "This helps neighbours know who they are helping.", "Save and continue",
                "Login with PIN", "Welcome back.", "Use the username and PIN from your Fendly profile.", "Log in",
                "Home", "Find what matters.", "Lost nearby? Found something? Start here.", "LOST", "FOUND", "My reports", "My profile",
                "Post found item", "Report lost item", "Help it get home.", "Let's find it.", "Add clear details so the right person can recognise it.",
                "Item name", "Description and identifying details", "Location or landmark", "Date and time", "Upload item image", "Image selected",
                "Take photo with camera", "Use current location", "Publish found item", "Publish lost item", "Fendly Plus",
                "Unlock lost-item submissions.", "Found-item reports stay free forever. Lost-item submissions are Rs 99 per year.",
                "Pay and submit lost report", "Back to report", "Your reports", "Keep track of items you are helping to reunite.", "Back home",
                "Dummy user", "Your account details and preferences.", "Admin dashboard", "Private moderation workspace", "English only · confidential user details",
                "Loading live admin data...", "Review AI match", "Confirm and notify owner", "Exit admin"
        };
        String[][] translations = {
                english,
                {"अपनी प्रोफ़ाइल पूरी करें", "आपके बारे में थोड़ा", "इससे पड़ोसियों को पता चलेगा कि वे किसकी मदद कर रहे हैं।", "सहेजें और जारी रखें", "पिन से लॉगिन", "वापसी पर स्वागत है।", "अपने Fendly उपयोगकर्ता नाम और पिन का उपयोग करें।", "लॉगिन", "होम", "जो महत्वपूर्ण है उसे खोजें।", "पास में कुछ खोया? कुछ मिला? यहां से शुरू करें।", "खोया", "मिला", "मेरी रिपोर्ट", "मेरी प्रोफ़ाइल", "मिली वस्तु पोस्ट करें", "खोई वस्तु रिपोर्ट करें", "इसे घर पहुंचाने में मदद करें।", "आइए इसे खोजें।", "स्पष्ट विवरण जोड़ें ताकि सही व्यक्ति पहचान सके।", "वस्तु का नाम", "विवरण और पहचान की जानकारी", "स्थान या पहचान चिन्ह", "दिनांक और समय", "वस्तु की तस्वीर अपलोड करें", "तस्वीर चुनी गई", "कैमरे से तस्वीर लें", "वर्तमान स्थान उपयोग करें", "मिली वस्तु प्रकाशित करें", "खोई वस्तु प्रकाशित करें", "Fendly Plus", "खोई वस्तु की रिपोर्ट अनलॉक करें।", "मिली वस्तु की रिपोर्ट हमेशा निःशुल्क है। खोई वस्तु की रिपोर्ट Rs 99 प्रति वर्ष है।", "भुगतान करें और खोई रिपोर्ट भेजें", "रिपोर्ट पर वापस जाएं", "मेरी रिपोर्ट", "जिन वस्तुओं को मिलाने में मदद कर रहे हैं उनका रिकॉर्ड रखें।", "होम पर वापस जाएं", "डमी उपयोगकर्ता", "आपके खाते का विवरण और प्राथमिकताएं।", "एडमिन डैशबोर्ड", "निजी मॉडरेशन कार्यक्षेत्र", "केवल अंग्रेज़ी · गोपनीय उपयोगकर्ता विवरण", "लाइव एडमिन डेटा लोड हो रहा है...", "AI मिलान देखें", "पुष्टि करें और मालिक को सूचित करें", "एडमिन से बाहर निकलें"},
                {"पूर्ण प्रोफाइल", "तुमच्याबद्दल थोडे", "यामुळे शेजाऱ्यांना ते कोणाला मदत करत आहेत हे समजेल.", "जतन करा आणि पुढे जा", "पिनने लॉगिन", "पुन्हा स्वागत आहे.", "तुमचे Fendly वापरकर्तानाव आणि पिन वापरा.", "लॉगिन", "मुख्यपृष्ठ", "महत्त्वाचे शोधा.", "जवळ काही हरवले? काही सापडले? इथून सुरुवात करा.", "हरवले", "सापडले", "माझे अहवाल", "माझे प्रोफाइल", "सापडलेली वस्तू पोस्ट करा", "हरवलेली वस्तू नोंदवा", "ते घरी पोहोचवण्यास मदत करा.", "चला ते शोधूया.", "योग्य व्यक्ती ओळखू शकेल असे स्पष्ट तपशील जोडा.", "वस्तूचे नाव", "वर्णन आणि ओळख तपशील", "ठिकाण किंवा खूण", "दिनांक आणि वेळ", "वस्तूचा फोटो अपलोड करा", "फोटो निवडला", "कॅमेऱ्याने फोटो घ्या", "सध्याचे स्थान वापरा", "सापडलेली वस्तू प्रकाशित करा", "हरवलेली वस्तू प्रकाशित करा", "Fendly Plus", "हरवलेल्या वस्तूंचे अहवाल सुरू करा.", "सापडलेल्या वस्तूंचे अहवाल कायम विनामूल्य आहेत. हरवलेल्या वस्तूंचे अहवाल वर्षाला Rs 99 आहेत.", "भरणा करून हरवलेला अहवाल पाठवा", "अहवालाकडे परत जा", "माझे अहवाल", "तुम्ही पुन्हा जोडण्यास मदत करत असलेल्या वस्तूंचा मागोवा ठेवा.", "मुख्यपृष्ठावर परत जा", "डमी वापरकर्ता", "तुमच्या खात्याचे तपशील आणि प्राधान्ये.", "अॅडमिन डॅशबोर्ड", "खासगी मॉडरेशन कार्यक्षेत्र", "फक्त इंग्रजी · गोपनीय वापरकर्ता तपशील", "लाइव्ह अॅडमिन डेटा लोड होत आहे...", "AI जुळणी पाहा", "पुष्टी करून मालकाला कळवा", "अॅडमिनमधून बाहेर पडा"},
                {"اپنی پروفائل مکمل کریں", "آپ کے بارے میں کچھ", "اس سے پڑوسیوں کو معلوم ہوگا کہ وہ کس کی مدد کر رہے ہیں۔", "محفوظ کریں اور جاری رکھیں", "پن سے لاگ ان", "خوش آمدید۔", "اپنا Fendly صارف نام اور پن استعمال کریں۔", "لاگ ان", "ہوم", "اہم چیز تلاش کریں۔", "قریب کچھ گم ہوا؟ کچھ ملا؟ یہاں سے شروع کریں۔", "گمشدہ", "ملا", "میری رپورٹس", "میری پروفائل", "ملی ہوئی چیز پوسٹ کریں", "گمشدہ چیز رپورٹ کریں", "اسے گھر پہنچانے میں مدد کریں۔", "آئیے اسے تلاش کریں۔", "واضح تفصیلات شامل کریں تاکہ صحیح شخص شناخت کر سکے۔", "چیز کا نام", "تفصیل اور شناختی معلومات", "مقام یا نشانی", "تاریخ اور وقت", "چیز کی تصویر اپ لوڈ کریں", "تصویر منتخب ہے", "کیمرے سے تصویر لیں", "موجودہ مقام استعمال کریں", "ملی ہوئی چیز شائع کریں", "گمشدہ چیز شائع کریں", "Fendly Plus", "گمشدہ چیز کی رپورٹس کھولیں۔", "ملی ہوئی چیز کی رپورٹس ہمیشہ مفت ہیں۔ گمشدہ چیز کی رپورٹس سالانہ Rs 99 ہیں۔", "ادائیگی کریں اور گمشدہ رپورٹ بھیجیں", "رپورٹ پر واپس جائیں", "میری رپورٹس", "جن چیزوں کو ملانے میں مدد کر رہے ہیں ان کا ریکارڈ رکھیں۔", "ہوم پر واپس جائیں", "نمونہ صارف", "آپ کے اکاؤنٹ کی تفصیلات اور ترجیحات۔", "ایڈمن ڈیش بورڈ", "نجی نگرانی کا ورک اسپیس", "صرف انگریزی · خفیہ صارف کی تفصیلات", "لائیو ایڈمن ڈیٹا لوڈ ہو رہا ہے...", "AI میچ دیکھیں", "تصدیق کریں اور مالک کو اطلاع دیں", "ایڈمن سے باہر نکلیں"},
                {"Complete o seu perfil", "Sobre você", "Isto ajuda os vizinhos a saberem quem estão ajudando.", "Salvar e continuar", "Entrar com PIN", "Bem-vindo de volta.", "Use seu nome de usuário e PIN Fendly.", "Entrar", "Início", "Encontre o que importa.", "Perdeu algo perto? Encontrou algo? Comece aqui.", "PERDIDO", "ENCONTRADO", "Meus relatórios", "Meu perfil", "Publicar item encontrado", "Relatar item perdido", "Ajude-o a voltar para casa.", "Vamos encontrá-lo.", "Adicione detalhes claros para que a pessoa certa o reconheça.", "Nome do item", "Descrição e detalhes de identificação", "Local ou referência", "Data e hora", "Enviar imagem do item", "Imagem selecionada", "Tirar foto com câmera", "Usar localização atual", "Publicar item encontrado", "Publicar item perdido", "Fendly Plus", "Desbloqueie relatórios de itens perdidos.", "Relatórios de itens encontrados são sempre gratuitos. Itens perdidos custam Rs 99 por ano.", "Pagar e enviar relatório", "Voltar ao relatório", "Meus relatórios", "Acompanhe os itens que você ajuda a reunir.", "Voltar ao início", "Usuário de teste", "Detalhes e preferências da sua conta.", "Painel administrativo", "Área privada de moderação", "Somente inglês · dados confidenciais", "Carregando dados administrativos...", "Ver correspondência de IA", "Confirmar e notificar proprietário", "Sair do administrador"},
                {"ನಿಮ್ಮ ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಳಿಸಿ", "ನಿಮ್ಮ ಬಗ್ಗೆ ಸ್ವಲ್ಪ ಮಾಹಿತಿ", "ಇದು ನೆರವಿನವರನ್ನು ಯಾರು ಸಹಾಯ ಮಾಡುತ್ತಿದ್ದಾರೆಂದು ತಿಳಿಸಲು ಸಹಾಯ ಮಾಡುತ್ತದೆ.", "ಸೇವ್ ಮಾಡಿ ಮತ್ತು ಮುಂದುವರಿಸಿ", "ಪಿನ್ ಮೂಲಕ ಲಾಗಿನ್", "ಮರಳಿ ಸ್ವಾಗತ", "ನಿಮ್ಮ Fendly ಬಳಕೆದಾರಹೆಸರು ಮತ್ತು ಪಿನ್ ಬಳಸಿ.", "ಲಾಗಿನ್", "ಹೋಮ್", "ಪ್ರಮುಖವಾದ್ದನ್ನು ಹುಡುಕಿ.", "ಹತ್ತಿರದಲ್ಲಿ ಯಾವುದೋ ಕಳೆದುಹೋಗಿದೆಯೇ? ಏನಾದರೂ ಸಿಕ್ಕಿದೆಯೇ? ಇಲ್ಲಿಂದ ಪ್ರಾರಂಭಿಸಿ.", "ಕಳೆದುಹೋಗಿದೆ", "ಸಿಕ್ಕಿದೆ", "ನನ್ನ ವರದಿಗಳು", "ನನ್ನ ಪ್ರೊಫೈಲ್", "ಕಂಡ ವಸ್ತು ಪೋಸ್ಟ್ ಮಾಡಿ", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿ ಮಾಡಿ", "ಅದನ್ನು ಮನೆಗೆ ಸೇರಿಸಲು ಸಹಾಯ ಮಾಡಿ.", "ಮುತ್ತಲಿನವರೊಂದಿಗೆ ಹುಡುಕೋಣ.", "ಸರಿಯಾದ ವ್ಯಕ್ತಿ ಗುರುತಿಸಿಕೊಳ್ಳಲು ಸ್ಪಷ್ಟ ವಿವರಗಳನ್ನು ಸೇರಿಸಿ.", "ವಸ್ತುವಿನ ಹೆಸರು", "ವಿವರಣೆ ಮತ್ತು ಗುರುತಿಸುವ ವಿವರಗಳು", "ಸ್ಥಳ ಅಥವಾ ಗುರುತು", "ದಿನಾಂಕ ಮತ್ತು ಸಮಯ", "ವಸ್ತುವಿನ ಚಿತ್ರ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ", "ಚಿತ್ರ ಆಯ್ಕೆಮಾಡಲಾಗಿದೆ", "ಕ್ಯಾಮರಾದಿಂದ ಫೋಟೋ ತೆಗೆದುಕೊಳ್ಳಿ", "ಪ್ರಸ್ತುತ ಸ್ಥಳವನ್ನು ಬಳಸಿ", "ಕಂಡ ವಸ್ತು ಪ್ರಕಟಿಸಿ", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ಪ್ರಕಟಿಸಿ", "Fendly Plus", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿಗಳನ್ನು desbloಕ್ ಮಾಡಿ.", "ಕಂಡ ವಸ್ತು ವರದಿಗಳು ಶಾಶ್ವತವಾಗಿ ಉಚಿತವಾಗಿರುತ್ತವೆ. ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿಗಳು ವರ್ಷಕ್ಕೆ Rs 99.", "ಚెలಾಯಿಸಿ ಮತ್ತು ಕಳೆದುಹೋಗಿದ ವರದಿಯನ್ನು ಸಲ್ಲಿಸಿ", "ವರದಿಗೆ ಹಿಂತಿರುಗಿ", "ನನ್ನ ವರದಿಗಳು", "ನೀವು ಒಟ್ಟುಗೂಡಿಸಲು ಸಹಾಯ ಮಾಡುವ ವಸ್ತುಗಳ ರೆಕಾರ್ಡ್ ಅನ್ನು ನಿರ್ವಹಿಸಿ.", "ಮನೆಯತ್ತ ಹಿಂತಿರುಗಿ", "ಡಮ್ಮಿ ಬಳಕೆದಾರ", "ನಿಮ್ಮ ಖಾತೆ ವಿವರಗಳು ಮತ್ತು ಆದ್ಯತೆಗಳು.", "ಅಡ್ಮಿನ್ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್", "ಖಾಸಗಿ मॉಡರೇಶನ್ ಕಾರ್ಯಕ್ಷೇತ್ರ", "ಇಂಗ್ಲಿಷ್ ಮಾತ್ರ · ರಹಸ್ಯ ಬಳಕೆದಾರ ವಿವರಗಳು", "ಲೈವ್ ಅಡ್ಮಿನ್ ಡೇಟಾವನ್ನು ಲೋಡ್ ಮಾಡಲಾಗುತ್ತಿದೆ...", "AI ಪಂದ್ಯವನ್ನು ವೀಕ್ಷಿಸಿ", "ನಿಶ್ಚಿತಪಡಿಸಿ ಮತ್ತು ಮಾಲೀಕನಿಗೆ ತಿಳಿಸಿ", "ಅಡ್ಮಿನ್ ನಿಂದ ನಿರ್ಗಮಿಸಿ"},
                {"మీ ప్రొఫైల్‌ను పూర్తి చేయండి", "మీ గురించి కొద్దిపాటి సమాచారం", "ఇది పొరుగు వారికి ఎవరికి సహాయం చేస్తున్నారో తెలుసుకోవడంలో సహాయపడుతుంది.", "సేవ్ చేసి కొనసాగించండి", "పిన్‌తో లాగిన్", "మళ్ళీ స్వాగతం", "మీ Fendly వినియోగదారు పేరు మరియు పిన్‌ను ఉపయోగించండి.", "లాగిన్", "హోమ్", "ముఖ్యమైన వాటిని కనుగొనండి.", "ఇక్కడకు దగ్గరలో ఏదైనా పోయిందా? ఏదైనా దొరికిందా? ఇక్కడ ప్రారంభించండి.", "కోల్పోయినవి", "కనుగొన్నది", "నా రిపోర్ట్లు", "నా ప్రొఫైల్", "కనుగొన్న అంశాన్ని పోస్ట్ చేయండి", "కోల్పోయిన అంశాన్ని రిపోర్ట్ చేయండి", "దానిని ఇంటికి చేర్చడానికి సహాయం చేయండి.", "వెతుకుదాం.", "సరైన వ్యక్తి గుర్తించగలిగే స్పష్టమైన వివరాలను జోడించండి.", "అంశం పేరు", "వివరణ మరియు గుర్తింపు వివరాలు", "స్థలం లేదా పరిశీలన", "తేదీ మరియు సమయం", "అంశపు ఫోటో అప్లోడ్ చేయండి", "ఫోటో ఎంపికైంది", "కెమెరా నుండి ఫోటో తీయండి", "ప్రస్తుత స్థానం ఉపయోగించండి", "కనుగొన్న అంశాన్ని ప్రచురించండి", "కోల్పోయిన అంశాన్ని ప్రచురించండి", "Fendly Plus", "కోల్పోయిన వస్తువుల రిపోర్ట్లను అన్‌లాక్ చేయండి.", "కనుగొన్న వస్తువుల రిపోర్ట్లు ఎల్లప్పుడూ ఉచితం. కోల్పోయిన వస్తువుల రిపోర్ట్లు సంవత్సరానికి Rs 99.", "చెల్లించి కోల్పోయిన రిపోర్టును సమర్పించండి", "రిపోర్టుకు తిరిగి వెళ్లండి", "నా రిపోర్ట్లు", "మీరు పునరుద్ధరించడానికి సహాయం చేస్తున్న వస్తువుల రికార్డ్‌ను పర్యవేక్షించండి.", "హోమ్కి తిరిగి వెళ్లండి", "డమ్మీ యూజర్", "మీ అకౌంట్ వివరాలు మరియు ప్రాధాన్యతలు.", "అడ్మిన్ డాష్‌బోర్డ్", "ప్రైవేట్ మోడరేషన్ వర్క్‌స్పేస్", "ఇంగ్లీష్ మాత్రమే · గోప్య వినియోగదారు వివరాలు", "లైవ్ అడ్మిన్ డేటాను లోడ్ చేస్తున్నారు...", "AI మ్యాచ్ చూసుకోండి", "నిర్ధారించండి మరియు యజమానికి తెలియజేయండి", "అడ్మిన్ నుండి నిష్క్రమించండి"},
                {"আপনার প্রোফাইল সম্পূর্ণ করুন", "আপনার সম্পর্কে কিছু", "এটি প্রতিবেশীদের জানতে সাহায্য করে তারা কার সাহায্য করছে।", "সংরক্ষণ করুন এবং চালিয়ে যান", "পিন দিয়ে লগইন", "ফিরে আসার জন্য স্বাগতম।", "আপনার Fendly ব্যবহারকারীর নাম এবং পিন ব্যবহার করুন।", "লগইন", "হোম", "গুরুত্বপূর্ণ জিনিস খুঁজুন।", "কাছাকাছি কিছু হারিয়েছে? কিছু পেয়েছেন? এখান থেকে শুরু করুন।", "হারিয়ে গেছে", "পাওয়া গেছে", "আমার রিপোর্ট", "আমার প্রোফাইল", "পাওয়া আইটেম পোস্ট করুন", "হারানো আইটেম রিপোর্ট করুন", "এটিকে বাড়িতে ফিরিয়ে দিতে সাহায্য করুন।", "চলো এটি খুঁজে বের করি।", "সঠিক ব্যক্তি শনাক্ত করার জন্য স্পষ্ট বিবরণ যোগ করুন।", "আইটেমের নাম", "বর্ণনা ও শনাক্তকরণ তথ্য", "অবস্থান বা চিহ্ন", "তারিখ ও সময়", "আইটেমের ছবি আপলোড করুন", "চিত্র নির্বাচন করা হয়েছে", "ক্যামেরা দিয়ে ছবি নিন", "বর্তমান অবস্থান ব্যবহার করুন", "পাওয়া আইটেম প্রকাশ করুন", "হারানো আইটেম প্রকাশ করুন", "Fendly Plus", "হারানো আইটেম রিপোর্ট আনলক করুন।", "পাওয়া আইটেম রিপোর্ট সবসময় বিনামূল্যে। হারানো আইটেম রিপোর্ট বছরে Rs 99.", "পেমেন্ট করুন এবং হারানো রিপোর্ট জমা দিন", "রিপোর্টে ফিরে যান", "আমার রিপোর্ট", "আপনি কী কী আইটেম আবার একত্রিত করতে সাহায্য করছেন তার রেকর্ড রাখুন।", "হোমে ফিরে যান", "ডামি ব্যবহারকারী", "আপনার অ্যাকাউন্টের বিবরণ ও পছন্দসমূহ।", "অ্যাডমিন ড্যাশবোর্ড", "ব্যক্তিগত মডারেশন ওয়ার্কস্পেস", "শুধু ইংরেজি · গোপন ব্যবহারকারী বিবরণ", "লাইভ অ্যাডমিন ডেটা লোড হচ্ছে...", "AI ম্যাচ দেখুন", "নিশ্চিত করুন এবং মালিককে অবহিত করুন", "অ্যাডমিন থেকে বের হন"},
                {"നിങ്ങളുടെ പ്രൊഫൈൽ പൂർത്തിയാക്കുക", "നിങ്ങളെക്കുറിച്ച് കുറച്ച്", "ഇത് അയൽവാസികൾക്ക് ആരെ സഹായിക്കുന്നുവെന്ന് അറിയാൻ സഹായിക്കുന്നു.", "സേവ് ചെയ്ത് ಮುಂದോട്ടു തുടരുക", "പിൻ ഉപയോഗിച്ച് ലോഗിൻ", "വീണ്ടും സ്വാഗതം", "നിങ്ങളുടെ Fendly യൂസർനെയം, പിൻ ഉപയോഗിക്കുക.", "ലോഗിൻ", "ഹോം", "പ്രധാനമായ വസ്തുക്കൾ കണ്ടെത്തുക.", "സമീപത്ത് തന്നെ നഷ്ടപ്പെട്ടോ? എന്തെങ്കിലും കണ്ടെത്തിയോ? ഇവിടെ ആരംഭിക്കുക.", "കുറച്ചു പോയി", "കണ്ടെത്തി", "എന്റെ റിപ്പോർട്ടുകൾ", "എന്റെ പ്രൊഫൈൽ", "കണ്ടെത്തിയ ഇനം പോസ്റ്റുചെയ്യുക", "കുറച്ചു പോയ ഇനം റിപ്പോർട്ട് ചെയ്യുക", "അത് വീട്ടിലേക്ക് എത്തിക്കാൻ സഹായിക്കുക.", "കണ്ടുപിടിക്കാം.", "ശരിയായ വ്യക്തിയെ തിരിച്ചറിയാൻ വ്യക്തമായ വിശദാംശങ്ങൾ ചേർക്കുക.", "ഇനത്തിന്റെ പേര്", "വിവരണം, തിരിച്ചറിയൽ വിശദാംശങ്ങൾ", "സ്ഥലം അല്ലെങ്കിൽ അടയാളം", "തീയതിയും സമയവും", "ഇനത്തിന്റെ ചിത്രം അപ്‌ലോഡ് ചെയ്യുക", "ചിത്രം തിരഞ്ഞെടുത്തു", "ക്യാമറയിൽ നിന്ന് ഫോട്ടോ എടുക്കുക", "നിലവിലെ സ്ഥലം ഉപയോഗിക്കുക", "കണ്ടെത്തിയ ഇനം പ്രസിദ്ധീകരിക്കുക", "കുറച്ചു പോയ ഇനം പ്രസിദ്ധീകരിക്കുക", "Fendly Plus", "കുറച്ചു പോയ ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ അൺലോക്ക് ചെയ്യുക.", "കണ്ടെത്തിയ ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ എല്ലായ്പ്പോഴും സൗജന്യമാണ്. കുറച്ചു പോയ ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ പ്രതിവർഷം Rs 99.", "പേയ്‌മെന്റ് ചെയ്യുകയും കുറച്ചു പോയ റിപ്പോർട്ട് സമർപ്പിക്കുകയും ചെയ്യുക", "റിപ്പോർട്ടിലേക്ക് തിരികെ പോകുക", "എന്റെ റിപ്പോർട്ടുകൾ", "നിങ്ങൾ വീണ്ടും കൂട്ടിച്ചേർക്കാൻ സഹായിക്കുന്ന ഇനങ്ങളുടെ രേഖ സൂക്ഷിക്കുക.", "ഹോമിലേക്ക് മടങ്ങുക", "ഡമ്മി ഉപയോക്താവ്", "നിങ്ങളുടെ അക്കൗണ്ട് വിശദാംശങ്ങളും മുൻനിര‌സ്ഥിതികളും.", "അഡ്മിൻ ഡാഷ്‌ബോർഡ്", "സ്വകാര്യ മോർഡറേഷൻ വേർക്ക്‌സ്പേസ്", "ഇംഗ്ലീഷ് മാത്രം · രഹസ്യ ഉപയോക്തൃ വിശദാംശങ്ങൾ", "ലൈവ് അഡ്മിൻ ഡാറ്റ ലോഡ് ചെയ്യുകയാണ്...", "AI മാച്ച് കാണുക", "നിശ്ചിതപ്പെടുത്തുകയും مالکയ്ക്ക് അറിയിക്കുകയും ചെയ്യുക", "അഡ്മിൻ നിന്ന് പുറത്തുകടക്കുക"}
        };
        int language = Math.max(0, Math.min(selectedLanguage, translations.length - 1));
        for (int index = 0; index < english.length; index++) {
            if (english[index].equals(value)) return localizeDigits(translations[language][index]);
        }
        return localizeDigits(value);
    }

    private String translateExtra(String value) {
        String[][] values = {
                {"Tap to upload photo", "First name", "Surname", "Email address", "Mobile number", "State", "City", "Email", "Mobile", "Verify", "Verify OTP", "4-digit PIN", "Complete", "Save changes"},
                {"फ़ोटो अपलोड करने के लिए टैप करें", "पहला नाम", "उपनाम", "ईमेल पता", "मोबाइल नंबर", "राज्य", "शहर", "ईमेल", "मोबाइल", "सत्यापित करें", "OTP सत्यापित करें", "4 अंकों का पिन", "पूरा करें", "बदलाव सहेजें"},
                {"फोटो अपलोड करण्यासाठी टॅप करा", "पहिले नाव", "आडनाव", "ईमेल पत्ता", "मोबाइल नंबर", "राज्य", "शहर", "ईमेल", "मोबाइल", "पडताळा", "OTP पडताळा", "4 अंकी पिन", "पूर्ण करा", "बदल जतन करा"},
                {"تصویر اپ لوڈ کرنے کے لیے ٹیپ کریں", "پہلا نام", "مستقل نام", "ای میل پتہ", "موبائل نمبر", "ریاست", "شہر", "ای میل", "موبائل", "تصدیق کریں", "OTP کی تصدیق کریں", "4 ہندسوں کا پن", "مکمل کریں", "تبدیلیاں محفوظ کریں"},
                {"ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಲು ಟ್ಯಾಪ್ ಮಾಡಿ", "ಮೊದಲ ಹೆಸರು", "ಉಪನಾಮ", "ಇಮೇಲ್ ವಿಳಾಸ", "ಮೊಬೈಲ್ ಸಂಖ್ಯೆ", "ರಾಜ್ಯ", "ನಗರ", "ಇಮೇಲ್", "ಮೊಬೈಲ್", "ಪರಿಶೀಲಿಸಿ", "OTP ಪರಿಶೀಲಿಸಿ", "4 ಅಂಕಿಯ ಪಿನ್", "ಪೂರ್ಣಗೊಳಿಸಿ", "ಬದಲಾವಣೆಗಳನ್ನು ಉಳಿಸಿ"},
                {"ఫోటోను అప్‌లోడ్ చేయడానికి నొక్కండి", "మొదటి పేరు", "ఇంటి పేరు", "ఇమెయిల్ చిరునామా", "మొబైల్ నంబర్", "రాష్ట్రం", "నగరం", "ఇమెయిల్", "మొబైల్", "ధృవీకరించండి", "OTP ధృవీకరించండి", "4 అంకెల పిన్", "పూర్తి చేయండి", "మార్పులను సేవ్ చేయండి"},
                {"ছবি আপলোড করতে ট্যাপ করুন", "প্রথম নাম", "পদবি", "ইমেল ঠিকানা", "মোবাইল নম্বর", "রাজ্য", "শহর", "ইমেল", "মোবাইল", "যাচাই করুন", "OTP যাচাই করুন", "৪-অঙ্কের পিন", "সম্পূর্ণ করুন", "পরিবর্তন সংরক্ষণ করুন"},
                {"ഫോട്ടോ അപ്‌ലോഡ് ചെയ്യാൻ ടാപ്പ് ചെയ്യുക", "പേര്", "കുടുംബപ്പേര്", "ഇമെയിൽ വിലാസം", "മൊബൈൽ നമ്പർ", "സംസ്ഥാനം", "നഗരം", "ഇമെയിൽ", "മൊബൈൽ", "പരിശോധിക്കുക", "OTP പരിശോധിക്കുക", "4 അക്ക പിൻ", "പൂർത്തിയാക്കുക", "മാറ്റങ്ങൾ സംരക്ഷിക്കുക"}
        };
        String[] keys = values[0];
        int language = Math.max(0, Math.min(selectedLanguage, values.length - 1));
        for (int index = 0; index < keys.length; index++) {
            if (keys[index].equals(value)) return values[language][index];
        }
        return null;
    }

    private String localizeDigits(String value) {
        String[] digits = {
                "0123456789", "०१२३४५६७८९", "०१२३४५६७८९", "٠١٢٣٤٥٦٧٨٩",
                "೦೧೨೩೪೫೬೭೮೯", "౦౧౨౩౪౫౬౭౮౯", "০১২৩৪৫৬৭৮৯", "൦൧൨൩൪൫൬൭൮൯"
        };
        int language = Math.max(0, Math.min(selectedLanguage, digits.length - 1));
        if (language == 0 || value == null || value.isEmpty()) return value;

        String sourceDigits = digits[0];
        String targetDigits = digits[language];
        StringBuilder localized = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            int digitIndex = sourceDigits.indexOf(character);
            localized.append(digitIndex >= 0 ? targetDigits.charAt(digitIndex) : character);
        }
        return localized.toString();
    }

    private GradientDrawable goldButton() {
        GradientDrawable button = new GradientDrawable();
        button.setColor(GOLD);
        button.setCornerRadius(dp(24));
        button.setStroke(dp(1), Color.argb(120, 255, 255, 255));
        return button;
    }

    private GradientDrawable outlineButton() {
        GradientDrawable button = new GradientDrawable();
        button.setColor(surfaceColor());
        button.setCornerRadius(dp(24));
        button.setStroke(dp(1), borderColor());
        return button;
    }
}
