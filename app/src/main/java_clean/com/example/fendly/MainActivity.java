package com.example.fendly;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.util.Log;
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
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Space;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import android.app.Dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
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

import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.graphics.drawable.GradientDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import com.example.fendly.notifications.FcmRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.razorpay.Checkout;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.bumptech.glide.Glide;
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
import java.util.function.Function;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class MainActivity extends FragmentActivity implements PaymentResultWithDataListener {
    private static final int LIGHT_BACKGROUND = Color.rgb(247, 243, 238);
    private static final int LIGHT_SURFACE = Color.rgb(253, 251, 248);
    private static final int LIGHT_BORDER = Color.rgb(216, 208, 196);
    private static final int LIGHT_FIELD_BORDER = Color.rgb(205, 197, 184);
    private static final int DARK_BACKGROUND = Color.rgb(18, 19, 25);
    private static final int DARK_SURFACE = Color.rgb(24, 29, 36);
    private static final int DARK_BORDER = Color.rgb(50, 57, 67);
    private static final int DARK_FIELD_BORDER = Color.rgb(68, 77, 90);
    private static final int DARK_TEXT_PRIMARY = Color.rgb(247, 249, 252);
    private static final int DARK_TEXT_MUTED = Color.rgb(170, 177, 188);
    private static final int LIGHT_TEXT = Color.BLACK;
    private static final int LIGHT_TEXT_MUTED = Color.rgb(77, 80, 90);
    private static final int GOLD = Color.rgb(232, 178, 74);
    private static final int GOLD_ON = Color.rgb(43, 29, 5);
    private static final int LOST_GREEN = Color.rgb(11, 93, 69);
    private static final int LOST_GREEN_ON = Color.rgb(220, 239, 231);
    private static final int FOUND_GOLD = Color.rgb(201, 162, 76);
    private static final int FOUND_GOLD_ON = Color.rgb(51, 35, 5);
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
    private String activeLocationReportType;
    private TextView locationStatus;
    private TextView locationToggleStatus;
    private String phoneVerificationId;
    private boolean phoneVerificationHandled;
    private String currentReportType;

    private String normalizePhoneNumber(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) return "";
        if (digits.length() == 10) return "+91" + digits;
        if (digits.startsWith("91") && digits.length() > 10) return "+" + digits;
        return "+" + digits;
    }

    private boolean isValidMobileNumber(String raw) {
        return raw != null && raw.replaceAll("\\D", "").length() == 10;
    }

    private String formatPhoneNumberForDisplay(String phoneNumber) {
        String normalizedPhoneNumber = normalizeLocalizedDigits(phoneNumber);
        if (normalizedPhoneNumber == null || normalizedPhoneNumber.trim().isEmpty()) return "";
        return localizeDigits(normalizedPhoneNumber);
    }
    private Uri selectedProfileImage;
    private Bitmap capturedProfileImage;
    private static final int REQUEST_PROFILE_IMAGE = 706;
    private static final int REQUEST_PROFILE_CAMERA = 707;
    private static final int REQUEST_IMEI_SCAN = 708;
    private static final long EMAIL_VERIFICATION_COOLDOWN_MS = 60000L;
    private Runnable emailVerificationCooldownRunnable;
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
    private boolean profileHydrationInFlight = false;
    private boolean profileHydrated = false;
    private boolean cloudProfileHydrationInFlight = false;
    private boolean cloudProfileLoaded;
    private ListenerRegistration cloudProfileListener;
    private EditText visibleFirstName;
    private EditText visibleSurname;
    private EditText visibleEmail;
    private EditText visibleMobile;
    private final Handler realtimeProfileHandler = new Handler(Looper.getMainLooper());
    private Runnable realtimeProfileSave;
    private boolean applyingCloudProfile;
    private final List<Runnable> pendingProfileHydrationCallbacks = new ArrayList<>();
    private long lastProfileHydrationAttemptMs = 0L;
    private static final long PROFILE_HYDRATION_RETRY_WINDOW_MS = 10000L;
    private static final String API_BASE = "https://fendly-api.onrender.com";
    /**
     * Temporary testing toggle: set to true to restore the mobile OTP requirement later.
     * Keep the old validation logic in place while it is disabled for the current testing phase.
     */
    private static final boolean REQUIRE_MOBILE_OTP_FOR_PROFILE_SAVE = false;
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private volatile String lastSubmissionError;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int savedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        LanguageManager.setAppLanguage(languageCodeForIndex(savedLanguage));
        boolean savedDarkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false);
        super.onCreate(savedInstanceState);
        initializeCloudinary();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setTheme(savedDarkMode ? R.style.Theme_Fendly_Dark : R.style.Theme_Fendly);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        languagePreferences = getSharedPreferences("fendly_language", MODE_PRIVATE);
        selectedLanguage = languagePreferences.getInt("selected_language_index", 0);
        darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE).getBoolean("dark_mode", false);
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        accountCreated = account.getBoolean("created", false) || firebaseUser != null;
        if (!accountCreated && !hasProfileDrafts(account)) {
            resetFreshAccountDrafts();
        }
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
        restoreProfileDrafts();
        applySystemBarColors();
        restoreScreenState();
        FcmRegistration.registerCurrentToken();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) hydrateProfileFromBackend(null);
    }

    @Override
    protected void onDestroy() {
        if (cloudProfileListener != null) cloudProfileListener.remove();
        if (realtimeProfileSave != null) realtimeProfileHandler.removeCallbacks(realtimeProfileSave);
        network.shutdownNow();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        restoreProfileDrafts();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            hydrateProfileFromBackend(null);
            return;
        }
        if ((currentPage == PAGE_PROFILE || currentPage == PAGE_PROFILE_SETUP) && screenRenderer != null) {
            screenRenderer.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProfileDrafts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveProfileDrafts();
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

        LinearLayout fingerprintRow = new LinearLayout(this);
        fingerprintRow.setGravity(Gravity.END);
        ImageButton fingerprint = new ImageButton(this);
        fingerprint.setImageResource(R.drawable.ic_fingerprint);
        fingerprint.setContentDescription("Sign in with fingerprint");
        fingerprint.setColorFilter(accentColor());
        fingerprint.setBackgroundColor(Color.TRANSPARENT);
        fingerprint.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        fingerprint.setPadding(dp(8), dp(8), dp(8), dp(8));
        fingerprint.setOnClickListener(view -> showBiometricLoginDialog());
        fingerprintRow.addView(fingerprint, new LinearLayout.LayoutParams(dp(44), dp(44)));

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
            login.setText(translate("Signing in..."));
            login.setEnabled(false);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(credentialEmail(name), credentialPassword(name, code))
                    .addOnSuccessListener(result -> {
                        accountCreated = true;
                        saveStoredAccountPin(code);
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", name).apply();
                        showHome();
                        hydrateProfileFromBackend(null);
                    })
                    .addOnFailureListener(error -> {
                        login.setText(loginText("login"));
                        login.setEnabled(true);
                        Toast.makeText(this, "Incorrect username or PIN", Toast.LENGTH_LONG).show();
                    });
        });
        LinearLayout loginRow = new LinearLayout(this);
        loginRow.setOrientation(LinearLayout.HORIZONTAL);
        loginRow.setGravity(Gravity.END);
        loginRow.addView(login, new LinearLayout.LayoutParams(0, dp(44), 1f));
        LinearLayout.LayoutParams fingerprintRowParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        fingerprintRowParams.setMargins(dp(8), 0, 0, 0);
        loginRow.addView(fingerprintRow, fingerprintRowParams);
        authCard.addView(loginRow, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView createAccount = text(loginText("create"), 12, primaryTextColor(), Typeface.BOLD);
        createAccount.setGravity(Gravity.CENTER);
        createAccount.setIncludeFontPadding(true);
        createAccount.setMaxLines(2);
        createAccount.setEllipsize(null);
        createAccount.setPadding(dp(4), dp(6), dp(4), dp(6));
        if (Build.VERSION.SDK_INT >= 29) createAccount.setForceDarkAllowed(false);
        createAccount.setTextColor(primaryTextColor());
        createAccount.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        createAccount.setOnClickListener(view -> {
            resetFreshAccountDrafts();
            String name = username.getText().toString().trim();
            String code = pinValue(pinCells);
            if (name.length() < 3 || code.length() != 4) {
                Toast.makeText(this, "Enter a valid username and 4-digit PIN", Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            profileHydrated = false;
            createAccount.setEnabled(false);
            createAccount.setText(translate("Creating account..."));
            auth.createUserWithEmailAndPassword(credentialEmail(name), credentialPassword(name, code))
                    .addOnSuccessListener(result -> {
                        accountCreated = true;
                        saveStoredAccountPin(code);
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                .putBoolean("created", true)
                                .putString("username", name)
                                .apply();
                        FcmRegistration.registerCurrentToken();
                        showProfileSetup();
                    })
                    .addOnFailureListener(error -> {
                        createAccount.setEnabled(true);
                        createAccount.setText(loginText("create"));
                        String message = error.getMessage();
                        if (message != null && message.toLowerCase(Locale.US).contains("already in use")) {
                            message = "Username already exists";
                        } else {
                            message = "Could not create account";
                        }
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    });
        });
        authCard.addView(createAccount, new LinearLayout.LayoutParams(-1, dp(58)));

        setContentView(root);
        root.postDelayed(() -> {
            int authTextColor = authTextColor();
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

    private boolean hasProfileDrafts(SharedPreferences account) {
        return !account.getString("draft_full_name", "").trim().isEmpty()
                || !account.getString("draft_email", "").trim().isEmpty()
                || !account.getString("draft_mobile", "").trim().isEmpty()
                || !account.getString("draft_pin", "").trim().isEmpty()
                || !account.getString("draft_state", "").trim().isEmpty()
                || !account.getString("draft_city", "").trim().isEmpty();
    }

    private void clearProfileDrafts() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit()
                .remove("draft_full_name")
                .remove("draft_email")
                .remove("draft_mobile")
                .remove("draft_pin")
                .remove("draft_state")
                .remove("draft_city")
                .apply();
        draftFullName = "";
        draftEmail = "";
        draftMobile = "";
        draftPin = "";
        draftState = "";
        draftCity = "";
    }

    private void saveProfileDrafts() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit()
                .putString("draft_full_name", draftFullName)
                .putString("draft_email", draftEmail)
                .putString("draft_mobile", draftMobile)
                .putString("draft_pin", draftPin)
                .putString("draft_state", draftState)
                .putString("draft_city", draftCity)
                .apply();
    }

    private void restoreProfileDrafts() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        draftFullName = account.getString("draft_full_name", draftFullName);
        draftEmail = account.getString("draft_email", draftEmail);
        draftMobile = account.getString("draft_mobile", draftMobile);
        draftPin = account.getString("draft_pin", draftPin);
        draftState = account.getString("draft_state", draftState);
        draftCity = account.getString("draft_city", draftCity);
    }

    private void resetFreshAccountDrafts() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return;
        }

        draftFullName = "";
        draftEmail = "";
        draftMobile = "";
        draftPin = "";
        draftState = "";
        draftCity = "";

        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit()
                .remove("created")
                .remove("username")
                .remove("full_name")
                .remove("email")
                .remove("mobile")
                .remove("state")
                .remove("city")
                .remove("profile_image_uri")
                .remove("account_pin")
                .remove("mobile_verified")
                .remove("email_verified")
                .remove("email_verify_cooldown_until")
                .apply();

        if (hasProfileDrafts(account)) {
            saveProfileDrafts();
        } else {
            account.edit()
                    .remove("draft_full_name")
                    .remove("draft_email")
                    .remove("draft_mobile")
                    .remove("draft_pin")
                    .remove("draft_state")
                    .remove("draft_city")
                    .apply();
        }
    }

    private void showProfileSetup() {
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        currentPage = PAGE_PROFILE_SETUP;
        profileSetupVisible = true;
        screenRenderer = this::showProfileSetup;
        LinearLayout root = screenBase("");
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);

        TextView title = text(getString(R.string.profile_complete_title), 15, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        root.addView(title, contentParams(-1, dp(22), 0));
        String rawHeaderUsername = account.getString("username", "");
        String displayHeaderUsername = accountCreated
            ? (!rawHeaderUsername.isEmpty() ? localizeProfileDisplayValue("username", rawHeaderUsername) : "")
            : translate("Your Fendly account");
        TextView usernameText = text(displayHeaderUsername, 11, secondaryTextColor(), Typeface.NORMAL);
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

        TextView photoHint = text(getString(R.string.profile_tap_upload_photo), 10, secondaryTextColor(), Typeface.NORMAL);
        photoHint.setGravity(Gravity.CENTER);
        photoSection.addView(photoHint, contentParams(-1, dp(20), dp(10)));
        root.addView(photoSection);

        EditText firstName = field(getString(R.string.profile_first_name));
        EditText surname = field(getString(R.string.profile_surname));
        String rawSavedFullName = (!draftFullName.trim().isEmpty() ? draftFullName : account.getString("full_name", "")).trim();
        String savedFullName = getCanonicalEnglishName(rawSavedFullName);
        String[] nameParts = savedFullName.split("\\s+", 2);
        firstName.setText(localizeProfileName(nameParts.length > 0 ? nameParts[0] : ""));
        surname.setText(localizeProfileName(nameParts.length > 1 ? nameParts[1] : ""));
        visibleFirstName = firstName;
        visibleSurname = surname;
        firstName.addTextChangedListener(draftWatcher(value -> {
            String fn = getCanonicalEnglishName(value);
            String sn = getCanonicalEnglishName(surname.getText().toString().trim());
            draftFullName = (fn + " " + sn).trim();
            saveProfileDrafts();
        }));
        surname.addTextChangedListener(draftWatcher(value -> {
            String fn = getCanonicalEnglishName(firstName.getText().toString().trim());
            String sn = getCanonicalEnglishName(value);
            draftFullName = (fn + " " + sn).trim();
            saveProfileDrafts();
        }));

        EditText email = field(getString(R.string.profile_email_address));
        EditText mobile = field(getString(R.string.profile_mobile_number));
        email.setText(!draftEmail.trim().isEmpty() ? draftEmail : account.getString("email", ""));
        mobile.setText(formatPhoneNumberForDisplay(
            !draftMobile.trim().isEmpty() ? draftMobile : account.getString("mobile", "")));

        EditText[] pinCells = pinCells();
        if (!darkMode) {
            for (EditText pinCell : pinCells) pinCell.setTextColor(darkMode ? Color.WHITE : LIGHT_TEXT);
        }
        for (int index = 0; index < pinCells.length && index < draftPin.length(); index++) {
            pinCells[index].setText(String.valueOf(draftPin.charAt(index)));
        }

        email.addTextChangedListener(draftWatcher(value -> {
            draftEmail = value;
            saveProfileDrafts();
        }));
        mobile.addTextChangedListener(draftWatcher(value -> {
            draftMobile = value;
            saveProfileDrafts();
        }));
        for (EditText pinCell : pinCells) {
            pinCell.addTextChangedListener(draftWatcher(value -> {
                draftPin = pinValue(pinCells);
                saveProfileDrafts();
            }));
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

        EditText usernameField = field(getString(R.string.profile_username_login));
        usernameField.setText(localizeProfileDisplayValue("username", account.getString("username", "")));
        usernameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        usernameField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});

        TextView usernameStatus = text("", 10, secondaryTextColor(), Typeface.BOLD);
        usernameStatus.setGravity(Gravity.CENTER);
        usernameStatus.setPadding(dp(10), dp(4), dp(10), dp(4));
        usernameStatus.setVisibility(View.INVISIBLE);

        FrameLayout usernameContainer = new FrameLayout(this);
        usernameContainer.addView(usernameField, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL);
        statusParams.rightMargin = dp(12);
        statusParams.topMargin = dp(0);
        usernameContainer.addView(usernameStatus, statusParams);

        LinearLayout usernameGroup = new LinearLayout(this);
        usernameGroup.setOrientation(LinearLayout.VERTICAL);
        usernameGroup.addView(fieldLabel(getString(R.string.profile_username)), new LinearLayout.LayoutParams(-1, dp(20)));
        usernameGroup.addView(usernameContainer, contentParams(-1, dp(46), dp(0)));
        root.addView(usernameGroup, contentParams(-1, dp(76), dp(4)));

        usernameField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable editable) {
                String candidate = editable == null ? "" : editable.toString().trim().toLowerCase(Locale.US);
                if (candidate.isEmpty()) {
                    usernameStatus.setVisibility(View.INVISIBLE);
                    usernameStatus.setText("");
                    return;
                }
                if (!candidate.matches("^[a-z0-9_]{3,32}$")) {
                    usernameStatus.setVisibility(View.VISIBLE);
                    usernameStatus.setText(translate("Invalid"));
                    usernameStatus.setTextColor(Color.RED);
                    return;
                }
                usernameStatus.setVisibility(View.VISIBLE);
                usernameStatus.setText(translate("Checking..."));
                usernameStatus.setTextColor(Color.rgb(201, 162, 76));
                checkUsernameAvailability(candidate, usernameField, usernameStatus);
            }
        });

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.addView(labeledField(getString(R.string.profile_first_name), firstName), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams surnameParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        surnameParams.setMargins(dp(8), 0, 0, 0);
        nameRow.addView(labeledField(getString(R.string.profile_surname), surname), surnameParams);
        root.addView(nameRow, contentParams(-1, dp(76), dp(4)));

        TextView emailVerify = filledButton(getString(R.string.profile_verify_otp), GOLD, GOLD_ON);
        emailVerify.setPadding(dp(12), 0, dp(12), 0);
        emailVerify.setOnClickListener(view -> sendEmailOtpFlow(email, emailVerify));
        refreshEmailVerificationState(email, emailVerify);

        TextView mobileVerify = filledButton(getString(R.string.profile_verify_otp), GOLD, GOLD_ON);
        mobileVerify.setPadding(dp(10), 0, dp(10), 0);
        mobileVerify.setOnClickListener(view -> verifyProfileMobile(mobile, mobileVerify));
        String verifiedMobile = account.getString("mobile", "").trim();
        String enteredMobile = normalizeLocalizedDigits(mobile.getText().toString().trim());
        boolean mobileAlreadyVerified = verifiedMobile.matches("^\\d{10}$")
            && enteredMobile.matches("^\\d{10}$")
            && account.getBoolean("mobile_verified", false)
            && verifiedMobile.equals(enteredMobile);
        if (mobileAlreadyVerified) {
            lockVerifiedMobileField(mobile, mobileVerify);
        } else {
            mobile.setEnabled(true);
            mobile.setFocusable(true);
            mobile.setFocusableInTouchMode(true);
            mobile.setCursorVisible(true);
        }
        mobile.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                String currentMobile = normalizeLocalizedDigits(value.toString().trim());
                boolean verifiedNow = currentMobile.matches("^\\d{10}$")
                    && getSharedPreferences("fendly_account", MODE_PRIVATE).getBoolean("mobile_verified", false)
                    && currentMobile.equals(getSharedPreferences("fendly_account", MODE_PRIVATE).getString("mobile", "").trim());
                if (verifiedNow) {
                    mobileVerify.setOnClickListener(null);
                    lockVerifiedMobileField(mobile, mobileVerify);
                    return;
                }
                if (!verifiedMobile.equals(currentMobile)) {
                    mobileVerify.setOnClickListener(view -> verifyProfileMobile(mobile, mobileVerify));
                    mobileVerify.setText(translate("Verify OTP"));
                    mobileVerify.setEnabled(true);
                    mobileVerify.setClickable(true);
                    mobileVerify.setFocusable(true);
                    mobileVerify.setBackground(round(GOLD, 24));
                    mobileVerify.setTextColor(GOLD_ON);
                    mobile.setEnabled(true);
                    mobile.setFocusable(true);
                    mobile.setFocusableInTouchMode(true);
                    mobile.setCursorVisible(true);
                }
            }
            @Override public void afterTextChanged(Editable value) { }
        });

        Map<String, String[]> stateCities = indiaStateCityMap();
        String[] states = stateCities.keySet().toArray(new String[0]);
        Arrays.sort(states, 1, states.length);
        AutoCompleteTextView stateSearch = new AutoCompleteTextView(this);
        AutoCompleteTextView citySearch = new AutoCompleteTextView(this);
        String savedState = !draftState.trim().isEmpty() ? draftState : account.getString("state", "");
        String savedCity = !draftCity.trim().isEmpty() ? draftCity : account.getString("city", "");
        stateSearch.setText(localizeProfileDisplayValue("state", savedState));
        citySearch.setText(localizeProfileDisplayValue("city", savedCity));
        stateSearch.setTag(savedState);
        citySearch.setTag(savedCity);
        stateSearch.setHint("");
        citySearch.setHint("");
        stateSearch.setSingleLine(true);
        citySearch.setSingleLine(true);
        stateSearch.setInputType(InputType.TYPE_NULL);
        citySearch.setInputType(InputType.TYPE_NULL);
        stateSearch.setFocusable(false);
        citySearch.setFocusable(false);
        citySearch.setEnabled(false);
        applyLocationFieldStyle(stateSearch);
        applyLocationFieldStyle(citySearch);

        stateSearch.setOnClickListener(clickedView -> showStatePicker(states, stateSearch, citySearch, stateCities));
        stateSearch.addTextChangedListener(draftWatcher(value -> {
            draftState = value;
            saveProfileDrafts();
        }));
        citySearch.addTextChangedListener(draftWatcher(value -> {
            draftCity = value;
            saveProfileDrafts();
        }));
        String[] draftCities = stateCities.getOrDefault(savedState, new String[0]);
        citySearch.setEnabled(draftCities.length > 0 && !isSelectCityPlaceholder(draftCities[0]));
        citySearch.setOnClickListener(clickedView -> {
            String selectedState = stateSearch.getText().toString().trim();
            String[] cities = stateCities.getOrDefault(selectedState, new String[]{defaultSelectCityText()});
            if (citySearch.isEnabled()) showCityPicker(selectedState, cities, citySearch);
        });

        LinearLayout locationRow = new LinearLayout(this);
        locationRow.setOrientation(LinearLayout.HORIZONTAL);
        locationRow.addView(labeledCitySearch(getString(R.string.profile_state), stateSearch), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams cityParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        cityParams.setMargins(dp(8), 0, 0, 0);
        locationRow.addView(labeledCitySearch(getString(R.string.profile_city), citySearch), cityParams);
        root.addView(locationRow, contentParams(-1, dp(76), dp(4)));

        addEditableProfileField(root, getString(R.string.profile_email), email, null);
        addEditableProfileField(root, getString(R.string.profile_mobile), mobile, mobileVerify);
        addLabeledPinField(root, getString(R.string.profile_four_digit_pin), pinCells);

        TextView save = actionButton(getString(R.string.profile_complete), true);
        save.setOnClickListener(view -> {
            String updatedFirstName = getCanonicalEnglishName(firstName.getText().toString());
            String updatedSurname = getCanonicalEnglishName(surname.getText().toString());
            String originalFirstName = getCanonicalEnglishName(String.valueOf(firstName.getTag() == null ? "" : firstName.getTag()));
            String originalSurname = getCanonicalEnglishName(String.valueOf(surname.getTag() == null ? "" : surname.getTag()));
            if (updatedFirstName.equalsIgnoreCase(originalFirstName) || updatedFirstName.equals(localizeProfileName(originalFirstName))) updatedFirstName = originalFirstName;
            if (updatedSurname.equalsIgnoreCase(originalSurname) || updatedSurname.equals(localizeProfileName(originalSurname))) updatedSurname = originalSurname;
            String fullName = (updatedFirstName + " " + updatedSurname).trim();
            String usernameValue = usernameField.getText().toString().trim().toLowerCase(Locale.US);
            String mobileValue = mobile.getText().toString().trim();
            String emailValue = email.getText().toString().trim();
            String pinValue = pinValue(pinCells);
            String selectedState = reverseLocalizedProfileValue("state", stateSearch.getText().toString().trim());
            String selectedCity = reverseLocalizedProfileValue("city", citySearch.getText().toString().trim());
            String originalState = String.valueOf(stateSearch.getTag() == null ? "" : stateSearch.getTag());
            if (selectedState.equals(localizeProfileDisplayValue("state", originalState))) selectedState = originalState;
            String originalCity = String.valueOf(citySearch.getTag() == null ? "" : citySearch.getTag());
            if (selectedCity.equals(localizeProfileDisplayValue("city", originalCity))) selectedCity = originalCity;
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
            } else if (selectedState == null || selectedState.trim().isEmpty() || isSelectStatePlaceholder(selectedState) || selectedCity.isEmpty() || isSelectCityPlaceholder(selectedCity) || !cityMatchesState) {
                Toast.makeText(this, "Please select your state and city", Toast.LENGTH_LONG).show();
                stateSearch.requestFocus();
            } else {
                boolean currentMobileVerified = account.getBoolean("mobile_verified", false)
                        && mobileValue.equals(account.getString("mobile", "").trim());
                if (REQUIRE_MOBILE_OTP_FOR_PROFILE_SAVE && !currentMobileVerified) {
                    Toast.makeText(this, "Verify mobile OTP before continuing", Toast.LENGTH_SHORT).show();
                    blinkVerificationRequired(mobileVerify);
                    mobile.requestFocus();
                    return;
                }

                accountCreated = true;
                account.edit()
                        .putBoolean("created", true)
                        .putString("username", usernameValue)
                        .putString("full_name", fullName)
                        .putString("email", emailValue)
                        .putString("mobile", mobileValue)
                        .putString("state", selectedState)
                        .putString("city", selectedCity)
                        .putBoolean("mobile_verified", true)
                        .apply();
                clearProfileDrafts();

                save.setText(translate("Creating account..."));
                save.setEnabled(false);
                saveFirebaseCredential(usernameValue, pinValue, save);
            }
        });
        root.addView(save, contentParams(-1, dp(44), 0));
        applyProfileFont(root);
    }

    private void startPhoneVerification(String mobile, String username, String pin, TextView save) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            save.setText(translate("Sign in first"));
            return;
        }
        phoneVerificationHandled = false;
        save.setText(translate("Sending verification code..."));
        save.setEnabled(false);
        String phoneNumber = normalizePhoneNumber(mobile);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phone_number", phoneNumber);
                JSONObject response = postJson("/api/auth/send-otp", payload.toString(), null);
                String sessionId = response != null ? response.optString("session_id", "") : "";
                if (sessionId == null || sessionId.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        save.setText(translate("SMS verification unavailable"));
                        save.setEnabled(true);
                        Toast.makeText(MainActivity.this, translate("Could not send OTP"), Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                phoneVerificationId = sessionId;
                runOnUiThread(() -> {
                    save.setText(translate("Enter SMS code"));
                    showOtpDialog(username, pin, save);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    save.setText(translate("SMS verification unavailable"));
                    save.setEnabled(true);
                    Toast.makeText(MainActivity.this, translate("Could not send OTP: ") + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showOtpDialog(String username, String pin, TextView save) {
        showThemedOtpDialog(translate("Verify your mobile"), translate("Enter the code sent to your mobile number."), translate("6-digit SMS code"), value -> {
            if (phoneVerificationId == null || value.length() != 6) {
                save.setText(translate("Invalid SMS code"));
                save.setEnabled(true);
                return false;
            }
            completePhoneVerification(phoneVerificationId, username, pin, save, value);
            return true;
        }, () -> {
                    save.setText(translate("Save and continue"));
                    save.setEnabled(true);
                });
    }

    private void showThemedOtpDialog(String titleText, String subtitleText, String hintText,
                                     Function<String, Boolean> verifyAction, Runnable cancelAction) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(22), dp(22), dp(18));
        content.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_field_lock);
        icon.setColorFilter(accentColor());
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(60), dp(60));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(icon, iconParams);

        TextView title = text(translate(titleText), 18, primaryTextColor(), Typeface.BOLD);
        title.setTypeface(localizedScriptTypeface(title.getText(), Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text(translate(subtitleText), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setTypeface(localizedScriptTypeface(subtitle.getText(), Typeface.NORMAL));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setMaxLines(2);
        content.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(34)));

        EditText[] codeCells = otpCells();
        LinearLayout codeRow = new LinearLayout(this);
        codeRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int index = 0; index < codeCells.length; index++) {
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
            if (index > 0) cellParams.setMargins(dp(6), 0, 0, 0);
            codeRow.addView(codeCells[index], cellParams);
        }
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(-1, dp(46));
        codeParams.setMargins(0, dp(10), 0, dp(16));
        content.addView(codeRow, codeParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView cancel = text(translate("Cancel"), 12, secondaryTextColor(), Typeface.BOLD);
        cancel.setTypeface(localizedScriptTypeface(cancel.getText(), Typeface.BOLD));
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
            cancelAction.run();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(88), dp(44)));
        TextView verify = text(translate("Verify"), 12, GOLD_ON, Typeface.BOLD);
        verify.setTypeface(localizedScriptTypeface(verify.getText(), Typeface.BOLD));
        verify.setGravity(Gravity.CENTER);
        verify.setBackground(goldButton());
        LinearLayout.LayoutParams verifyParams = new LinearLayout.LayoutParams(dp(100), dp(44));
        verifyParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(verify, verifyParams);
        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        verify.setOnClickListener(view -> {
            if (Boolean.TRUE.equals(verifyAction.apply(pinValue(codeCells).trim()))) {
                dialog.dismiss();
            }
        });

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
    }

    private void completePhoneVerification(String sessionId, String username, String pin, TextView save, String otpValue) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        save.setText(translate("Verifying OTP..."));
        save.setEnabled(false);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("session_id", sessionId);
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-otp", payload.toString(), null);
                boolean verified = response != null && response.optBoolean("success", false);
                runOnUiThread(() -> {
                    if (verified) {
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("mobile_verified", true).apply();
                        finishProfileSetup(username, pin, save);
                    } else {
                        phoneVerificationHandled = false;
                        save.setText(translate("SMS verification failed"));
                        save.setEnabled(true);
                        Toast.makeText(this, translate("Could not verify mobile number"), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    phoneVerificationHandled = false;
                    save.setText(translate("SMS verification failed"));
                    save.setEnabled(true);
                    Toast.makeText(this, translate("Could not verify mobile number"), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void finishProfileSetup(String username, String pin, TextView save) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            save.setText(translate("Sign in first"));
            return;
        }
        save.setText(translate("Checking username..."));
        save.setEnabled(false);
        auth.getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            int reservationCode = reserveUsername(username, token.getToken());
            runOnUiThread(() -> {
                if (reservationCode != 201 && reservationCode != 200) {
                    save.setText(translate("Username unavailable"));
                    save.setEnabled(true);
                    Toast.makeText(this, translate("Choose another username"), Toast.LENGTH_LONG).show();
                    return;
                }
                saveFirebaseCredential(username, pin, save);
            });
        })).addOnFailureListener(error -> {
            save.setText(translate("Authentication unavailable"));
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

    private String suggestAlternateUsername(String baseUsername) {
        String base = baseUsername == null ? "" : baseUsername.trim().toLowerCase(Locale.US);
        base = base.replaceAll("[^a-z0-9_]", "_");
        base = base.replaceAll("_+", "_");
        base = base.replaceAll("^_+|_+$", "");
        if (base.length() < 3) return "";
        String[] candidates = new String[]{
                base + "01",
                base + "12",
                base + "_01",
                base + "_12",
                base + "1",
                base + "2"
        };
        for (String candidate : candidates) {
            if (candidate.length() >= 3 && candidate.length() <= 32 && candidate.matches("^[a-z0-9_]+$")) {
                return candidate;
            }
        }
        return base + "1";
    }

    private void checkUsernameAvailability(String candidate, EditText usernameField, TextView usernameStatus) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            usernameStatus.setText(translate("Login"));
            usernameStatus.setTextColor(Color.RED);
            return;
        }

        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String encoded = URLEncoder.encode(candidate, StandardCharsets.UTF_8.toString());
                URL url = new URL(API_BASE + "/api/users/username/" + encoded);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Authorization", "Bearer " + token.getToken());

                int responseCode = connection.getResponseCode();
                String response = responseCode >= 200 && responseCode < 300
                        ? readStream(connection.getInputStream())
                        : readStream(connection.getErrorStream());
                boolean available = false;
                if (response != null && !response.trim().isEmpty()) {
                    try {
                        JSONObject json = new JSONObject(response);
                        available = json.optBoolean("available", false);
                    } catch (Exception ignored) {
                        available = false;
                    }
                }
                final boolean isAvailable = available;
                final String currentValue = usernameField.getText() == null ? "" : usernameField.getText().toString().trim().toLowerCase(Locale.US);
                runOnUiThread(() -> {
                    if (!candidate.equals(currentValue)) {
                        return;
                    }
                    if (isAvailable) {
                        usernameStatus.setText(translate("Available"));
                        usernameStatus.setTextColor(Color.rgb(19, 128, 56));
                    } else {
                        usernameStatus.setText(translate("Exists"));
                        usernameStatus.setTextColor(Color.RED);
                        String suggestion = suggestAlternateUsername(candidate);
                        if (!suggestion.isEmpty() && !suggestion.equals(candidate)) {
                            Toast.makeText(MainActivity.this, translate("Username exists. Try:") + " " + suggestion, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception ignored) {
                runOnUiThread(() -> {
                    String currentValue = usernameField.getText() == null ? "" : usernameField.getText().toString().trim().toLowerCase(Locale.US);
                    if (!candidate.equals(currentValue)) {
                        return;
                    }
                    usernameStatus.setText(translate("Try again"));
                    usernameStatus.setTextColor(Color.rgb(201, 162, 76));
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        })).addOnFailureListener(error -> runOnUiThread(() -> {
            String currentValue = usernameField.getText() == null ? "" : usernameField.getText().toString().trim().toLowerCase(Locale.US);
            if (!candidate.equals(currentValue)) {
                return;
            }
            usernameStatus.setText(translate("Try again"));
            usernameStatus.setTextColor(Color.rgb(201, 162, 76));
        }));
    }

    private void saveFirebaseCredential(String username, String pin, TextView save) {
        String email = credentialEmail(username);
        String password = credentialPassword(username, pin);
        saveStoredAccountPin(pin);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            save.setText("Could not secure account");
            save.setEnabled(true);
            Toast.makeText(this, "Please create the account again", Toast.LENGTH_LONG).show();
            return;
        }
        boolean passwordProviderLinked = false;
        for (UserInfo provider : currentUser.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(provider.getProviderId())) {
                passwordProviderLinked = true;
                break;
            }
        }
        Task<AuthResult> accountTask = passwordProviderLinked
                ? Tasks.forResult(null)
                : currentUser.linkWithCredential(EmailAuthProvider.getCredential(email, password));
        accountTask
                .addOnSuccessListener(result -> {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", username).apply();
                    accountCreated = true;
                    syncProfileWithBackend();
                    showHome();
                    hydrateProfileFromBackend(null);
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
        saveButton.setText(translate("Updating..."));
        saveButton.setEnabled(false);

        auth.getCurrentUser().reauthenticate(EmailAuthProvider.getCredential(credentialEmail(username), oldPassword))
                .addOnSuccessListener(result -> auth.getCurrentUser().updatePassword(newPassword)
                        .addOnSuccessListener(updated -> {
                            saveStoredAccountPin(newPin);
                            saveButton.setText(translate("Updated"));
                            Toast.makeText(this, "PIN updated successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(error -> {
                            saveButton.setText(translate("Change PIN"));
                            saveButton.setEnabled(true);
                            Toast.makeText(this, "Could not update PIN: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }))
                .addOnFailureListener(error -> {
                    saveButton.setText(translate("Change PIN"));
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
        LinearLayout root = screenBase(translate("Login with PIN"));
        addHeading(translate("Welcome back."), translate("Use the username and PIN from your Fendly profile."));
        EditText username = field(localizedFieldLabel("Username"));
        EditText pin = field(localizedFieldLabel("4-digit PIN"));
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pin.setPadding(dp(16), dp(8), dp(56), dp(8));
        addField(root, username);
        FrameLayout pinRow = new FrameLayout(this);
        pinRow.addView(pin, new FrameLayout.LayoutParams(-1, dp(48)));
        ImageButton forgotPin = new ImageButton(this);
        forgotPin.setImageResource(R.drawable.ic_field_key);
        forgotPin.setContentDescription(translate("Forgot PIN"));
        forgotPin.setColorFilter(accentColor());
        forgotPin.setBackgroundColor(Color.TRANSPARENT);
        forgotPin.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        forgotPin.setPadding(dp(12), dp(12), dp(12), dp(12));
        forgotPin.setOnClickListener(view -> showForgotPinDialog(username));
        FrameLayout.LayoutParams forgotPinParams = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.END | Gravity.CENTER_VERTICAL);
        pinRow.addView(forgotPin, forgotPinParams);
        addField(root, pinRow);
        TextView login = actionButton(localizedFieldLabel("Log in"), true);
        login.setOnClickListener(view -> {
            String name = username.getText().toString().trim();
            String code = pin.getText().toString();
            if (name.length() < 3 || code.length() != 4) {
                Toast.makeText(this, translate("Enter a valid username and 4-digit PIN"), Toast.LENGTH_LONG).show();
                return;
            }
            login.setText(translate("Signing in..."));
            login.setEnabled(false);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(credentialEmail(name), credentialPassword(name, code))
                    .addOnSuccessListener(result -> {
                        accountCreated = true;
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit().putBoolean("created", true).putString("username", name).apply();
                        showHome();
                        hydrateProfileFromBackend(null);
                    })
                    .addOnFailureListener(error -> {
                        login.setText(translate("Try again"));
                        login.setEnabled(true);
                        Toast.makeText(this, translate("Incorrect username or PIN"), Toast.LENGTH_LONG).show();
                    });
        });
        addField(root, login);
    }

    private void showBiometricLoginDialog() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String username = account.getString("username", "").trim();
        String pin = getStoredAccountPin();
        if (!account.getBoolean("created", false) || username.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, translate("Create an account and log in once first"), Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(22), dp(22), dp(18));
        content.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_fingerprint);
        icon.setColorFilter(accentColor());
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(68), dp(68));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(icon, iconParams);

        TextView title = text(translate("Fingerprint login"), 18, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text(translate("Confirm your identity to continue"), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(28)));

        String localizedUsername = localizeProfileName(username);
        TextView usernameLabel = text(localizedUsername, 15, primaryTextColor(), Typeface.BOLD);
        usernameLabel.setGravity(Gravity.CENTER);
        usernameLabel.setBackground(roundWithStroke(backgroundColor(), 12, fieldBorderColor()));
        LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(-1, dp(46));
        usernameParams.setMargins(0, dp(10), 0, dp(14));
        content.addView(usernameLabel, usernameParams);

        TextView login = text(translate("Log in"), 13, GOLD_ON, Typeface.BOLD);
        login.setGravity(Gravity.CENTER);
        login.setBackground(goldButton());
        login.setOnClickListener(view -> {
            dialog.dismiss();
            authenticateWithBiometric(username, pin);
        });
        content.addView(login, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView another = text(translate("Use another account"), 11, secondaryTextColor(), Typeface.BOLD);
        another.setGravity(Gravity.CENTER);
        another.setPadding(0, dp(10), 0, 0);
        another.setOnClickListener(view -> dialog.dismiss());
        content.addView(another, new LinearLayout.LayoutParams(-1, dp(34)));

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
    }

    private void authenticateWithBiometric(String username, String pin) {
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG;
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, translate("Fingerprint login is not available on this device"), Toast.LENGTH_LONG).show();
            return;
        }
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                                        credentialEmail(username), credentialPassword(username, pin))
                                .addOnSuccessListener(authResult -> {
                                    showHome();
                                    hydrateProfileFromBackend(null);
                                })
                                .addOnFailureListener(error -> Toast.makeText(MainActivity.this,
                                        translate("Could not sign in with fingerprint"), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errorString) {
                        Toast.makeText(MainActivity.this, errorString, Toast.LENGTH_SHORT).show();
                    }
                });
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(translate("Sign in to Fendly"))
                .setSubtitle(translate("Confirm your identity"))
                .setAllowedAuthenticators(authenticators)
                .setNegativeButtonText(translate("Cancel"))
                .build();
        prompt.authenticate(promptInfo);
    }

    private void showForgotPinDialog(EditText loginUsername) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_key,
                translate("Reset PIN?"),
                translate("A temporary 4-digit PIN will be sent after mobile verification.")
        );
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView cancel = text(translate("Cancel"), 12, secondaryTextColor(), Typeface.BOLD);
        cancel.setTypeface(localizedScriptTypeface(cancel.getText(), Typeface.BOLD));
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(view -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(88), dp(44)));
        TextView confirm = text(translate("Confirm"), 12, GOLD_ON, Typeface.BOLD);
        confirm.setTypeface(localizedScriptTypeface(confirm.getText(), Typeface.BOLD));
        confirm.setGravity(Gravity.CENTER);
        confirm.setBackground(goldButton());
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(dp(100), dp(44));
        confirmParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(confirm, confirmParams);
        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        confirm.setOnClickListener(view -> {
            String username = loginUsername.getText().toString().trim().toLowerCase(Locale.US);
            String mobileValue = getSharedPreferences("fendly_account", MODE_PRIVATE).getString("mobile", "").trim();
            if (!username.matches("^[a-z0-9_]{3,32}$")) {
                loginUsername.setError(translate("Enter your username first"));
                dialog.dismiss();
                return;
            }
            if (!mobileValue.matches("^\\d{10}$")) {
                dialog.dismiss();
                Toast.makeText(this, translate("No verified mobile number is saved"), Toast.LENGTH_LONG).show();
                return;
            }
            String temporaryPin = String.format(Locale.US, "%04d", new SecureRandom().nextInt(10000));
            confirm.setEnabled(false);
            confirm.setText(translate("Sending..."));
            startForgotPinPhoneVerification(username, mobileValue, temporaryPin, dialog);
        });
        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private LinearLayout themedDialogContent(int iconResource, String titleText, String subtitleText) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(22), dp(22), dp(18));
        content.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconResource);
        icon.setColorFilter(accentColor());
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(60), dp(60));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(icon, iconParams);
        TextView title = text(translate(titleText), 18, primaryTextColor(), Typeface.BOLD);
        title.setTypeface(localizedScriptTypeface(title.getText(), Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView subtitle = text(translate(subtitleText), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setTypeface(localizedScriptTypeface(subtitle.getText(), Typeface.NORMAL));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setMaxLines(3);
        content.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(42)));
        return content;
    }

    private void sizeThemedDialog(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
    }

    private void startForgotPinPhoneVerification(String username, String mobile, String temporaryPin, Dialog parentDialog) {
        phoneVerificationHandled = false;
        String phoneNumber = normalizePhoneNumber(mobile);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phone_number", phoneNumber);
                JSONObject response = postJson("/api/auth/send-otp", payload.toString(), null);
                String sessionId = response != null ? response.optString("session_id", "") : "";
                if (sessionId == null || sessionId.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        parentDialog.dismiss();
                        Toast.makeText(MainActivity.this, translate("Could not send OTP"), Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                phoneVerificationId = sessionId;
                runOnUiThread(() -> showForgotOtpDialog(username, mobile, temporaryPin, parentDialog));
            } catch (Exception error) {
                runOnUiThread(() -> {
                    parentDialog.dismiss();
                    Toast.makeText(MainActivity.this, translate("Could not send OTP"), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showForgotOtpDialog(String username, String mobile, String pin, Dialog parentDialog) {
        showThemedOtpDialog(translate("Enter OTP"), translate("Enter the 6-digit code sent to your mobile number."), translate("6-digit OTP"), value -> {
            if (phoneVerificationId == null || value.length() != 6) {
                Toast.makeText(this, translate("Enter the 6-digit OTP"), Toast.LENGTH_LONG).show();
                return false;
            }
            verifyForgotOtp(username, mobile, pin, parentDialog, value);
            return true;
        }, () -> { });
    }

    private void verifyForgotOtp(String username, String mobile, String pin, Dialog parentDialog, String otpValue) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("session_id", phoneVerificationId);
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-otp", payload.toString(), null);
                boolean verified = response != null && response.optBoolean("success", false);
                runOnUiThread(() -> {
                    if (verified) {
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                .putBoolean("created", true)
                                .putString("username", username)
                                .putString("mobile", mobile)
                                .putString("account_pin", pin)
                                .apply();
                        saveStoredAccountPin(pin);
                        parentDialog.dismiss();
                        new AlertDialog.Builder(this)
                                .setTitle(translate("Temporary PIN"))
                                .setMessage(translate("Your new 4-digit PIN is ") + pin + ". " + translate("Use it to log in, then change it from My Profile."))
                                .setPositiveButton(translate("OK"), null)
                                .show();
                    } else {
                        phoneVerificationHandled = false;
                        Toast.makeText(this, translate("This mobile is not linked to that username"), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    phoneVerificationHandled = false;
                    Toast.makeText(this, "Could not reset PIN", Toast.LENGTH_LONG).show();
                });
            }
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

    private void applyLocationFieldStyle(AutoCompleteTextView input) {
        float fontScale = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        input.setTextSize(14 * Math.max(1.0f, Math.min(1.2f, fontScale)));
        input.setTag(Float.valueOf(14));
        input.setTypeface(localizedScriptTypeface(input.getText(), Typeface.NORMAL));
        input.setTextColor(primaryTextColor());
        input.setHintTextColor(secondaryTextColor());
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        input.setIncludeFontPadding(false);
        input.setSingleLine(true);
        input.setPadding(dp(16), dp(6), dp(16), dp(6));
        input.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        input.setOnFocusChangeListener((view, focused) ->
                input.setBackground(roundWithStroke(surfaceColor(), 10, focused ? accentColor() : fieldBorderColor())));
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                input.setTypeface(localizedScriptTypeface(value, Typeface.NORMAL));
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    }

    private LinearLayout labeledCitySearch(String label, AutoCompleteTextView citySearch) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));
        applyLocationFieldStyle(citySearch);

        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(-1, dp(42));
        fieldParams.topMargin = dp(4);
        group.addView(citySearch, fieldParams);
        return group;
    }

    private String[] localizedStateChoices(String[] states) {
        if (states == null) return null;
        String[] localized = new String[states.length];
        for (int index = 0; index < states.length; index++) {
            String state = states[index];
            localized[index] = state == null ? null : (localizedStateName(state) != null ? localizedStateName(state) : state);
        }
        return localized;
    }

    private String[] localizedCityChoices(String[] cities) {
        if (cities == null) return null;
        String[] localized = new String[cities.length];
        for (int index = 0; index < cities.length; index++) {
            String city = cities[index];
            localized[index] = city == null ? null : (localizeProfileDisplayValue("city", city) != null ? localizeProfileDisplayValue("city", city) : city);
        }
        return localized;
    }

    private void showStatePicker(String[] states, AutoCompleteTextView stateSearch,
                                 AutoCompleteTextView citySearch,
                                 Map<String, String[]> stateCities) {
        LinearLayout picker = new LinearLayout(this);
        picker.setOrientation(LinearLayout.VERTICAL);
        picker.setPadding(dp(4), 0, dp(4), 0);

        EditText search = new EditText(this);
        search.setHint(translateUi("State") != null ? translateUi("State") : "Search state");
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        search.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        applyIcon(search, R.drawable.ic_field_search);
        picker.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        String[] displayStates = localizedStateChoices(states);
        ListView stateList = new ListView(this);
        stateList.setBackgroundColor(surfaceColor());
        stateList.setCacheColorHint(surfaceColor());
        stateList.setDivider(new ColorDrawable(fieldBorderColor()));
        stateList.setDividerHeight(1);
        ArrayAdapter<String> stateAdapter = localizedLocationAdapter(displayStates);
        stateList.setAdapter(stateAdapter);
        picker.addView(stateList, new LinearLayout.LayoutParams(-1, dp(320)));

        picker.setBackgroundColor(surfaceColor());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(localizedFieldLabel("State"))
                .setView(picker)
                .setNegativeButton(translateUi("Cancel") != null ? translateUi("Cancel") : "Cancel", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(surfaceColor()));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0f);
        }

        search.setOnClickListener(view -> {
            search.setFocusable(true);
            search.setFocusableInTouchMode(true);
            search.setShowSoftInputOnFocus(true);
            search.requestFocus();
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
            String selectedDisplayState = stateAdapter.getItem(position);
            String selectedState = states[position];
            for (int index = 0; index < displayStates.length; index++) {
                if (selectedDisplayState != null && selectedDisplayState.equals(displayStates[index])) {
                    selectedState = states[index];
                    break;
                }
            }
            stateSearch.setText(localizeProfileDisplayValue("state", selectedState));
            stateSearch.setTag(selectedState);
            citySearch.setText("");
            citySearch.setTag("");
            String[] cities = stateCities.getOrDefault(selectedState, new String[0]);
                citySearch.setEnabled(cities.length > 0 && !isSelectCityPlaceholder(cities[0]));
            dialog.dismiss();
            hideKeyboardAfterLocationSelection(stateSearch, citySearch);
        });
        dialog.setOnShowListener(shown -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
            search.setFocusable(true);
            search.setFocusableInTouchMode(true);
            search.post(() -> {
                search.requestFocus();
                InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (keyboard != null) keyboard.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
            });
        });
        dialog.show();
    }

    private void showCityPicker(String state, String[] cities, AutoCompleteTextView citySearch) {
        LinearLayout picker = new LinearLayout(this);
        picker.setOrientation(LinearLayout.VERTICAL);
        picker.setPadding(dp(4), 0, dp(4), 0);

        EditText search = new EditText(this);
        search.setHint(localizedFieldLabel("City"));
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        search.setShowSoftInputOnFocus(false);
        search.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        applyIcon(search, R.drawable.ic_field_search);
        picker.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        String[] originalCities = cities == null ? new String[0] : cities.clone();
        String[] sortedCities = originalCities.clone();
        Arrays.sort(sortedCities, String.CASE_INSENSITIVE_ORDER);
        String[] displayCities = localizedCityChoices(sortedCities);
        ListView cityList = new ListView(this);
        cityList.setBackgroundColor(surfaceColor());
        cityList.setCacheColorHint(surfaceColor());
        cityList.setDivider(new ColorDrawable(fieldBorderColor()));
        cityList.setDividerHeight(1);
        ArrayAdapter<String> cityAdapter = localizedLocationAdapter(displayCities);
        cityList.setAdapter(cityAdapter);
        picker.addView(cityList, new LinearLayout.LayoutParams(-1, dp(320)));

        picker.setBackgroundColor(surfaceColor());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(localizeProfileDisplayValue("state", state) != null ? localizeProfileDisplayValue("state", state) : state)
                .setView(picker)
                .setNegativeButton(translateUi("Cancel") != null ? translateUi("Cancel") : "Cancel", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(surfaceColor()));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0f);
        }

        search.setOnClickListener(view -> {
            search.setFocusable(true);
            search.setFocusableInTouchMode(true);
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
            String selectedDisplayCity = cityAdapter.getItem(position);
            String selectedCity = sortedCities[position];
            for (int index = 0; index < displayCities.length; index++) {
                if (selectedDisplayCity != null && selectedDisplayCity.equals(displayCities[index])) {
                    selectedCity = sortedCities[index];
                    break;
                }
            }
            citySearch.setText(localizeProfileDisplayValue("city", selectedCity));
            citySearch.setTag(selectedCity);
            dialog.dismiss();
            hideKeyboardAfterLocationSelection(citySearch);
        });
        dialog.setOnShowListener(shown -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
            search.setFocusable(true);
            search.setFocusableInTouchMode(true);
            search.post(() -> {
                search.requestFocus();
                InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (keyboard != null) keyboard.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
            });
        });
        dialog.show();
    }

    private ArrayAdapter<String> localizedLocationAdapter(String[] values) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                row.setTypeface(localizedScriptTypeface(row.getText(), Typeface.NORMAL));
                row.setTextColor(darkMode ? DARK_TEXT_PRIMARY : LIGHT_TEXT);
                row.setBackgroundColor(surfaceColor());
                row.setIncludeFontPadding(true);
                return row;
            }
        };
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
        TextView signedInText = text(translate("Signed in as") + " " + localizeProfileName(username), 14, secondaryTextColor(), Typeface.NORMAL);
        signedInText.setGravity(Gravity.CENTER);
        addField(root, signedInText);
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
        addLabeledDateField(root, translate("FOUND".equals(type) ? "Date found" : "Date lost"), date);
        addLabeledField(root, translate("FOUND".equals(type) ? "Place found" : "Last seen at"), location);

        boolean locationVisibleOnThisScreen = hasLocation && currentReportType != null && currentReportType.equalsIgnoreCase(activeLocationReportType);
        TextView addLocation = text(locationVisibleOnThisScreen
            ? "Location ready"
                : translate("Precise location  OFF"), 11, locationVisibleOnThisScreen ? Color.WHITE : secondaryTextColor(), Typeface.BOLD);
        addLocation.setGravity(Gravity.CENTER);
        addLocation.setBackground(locationVisibleOnThisScreen
                ? roundWithStroke(LOST_GREEN, 10, LOST_GREEN)
                : roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(-1, dp(42));
        locationParams.setMargins(0, 0, 0, dp(12));
        root.addView(addLocation, locationParams);
        addLocation.setOnClickListener(view -> {
            if (hasLocation && currentReportType != null && currentReportType.equalsIgnoreCase(activeLocationReportType)) {
                hasLocation = false;
                activeLocationReportType = null;
                currentLat = 0.0;
                currentLng = 0.0;
                applyLocationToggleVisualState(addLocation, addLocation, false);
            } else {
                requestLocation(addLocation, addLocation);
            }
        });
        locationStatus = addLocation;
        locationToggleStatus = addLocation;

        root.addView(imageSlots(), contentParams(-1, dp(104), dp(14)));

        if (editingReportId != null && type.equalsIgnoreCase(editingReportType)) {
            TextView save = actionButton(translate("Save changes"), true);
            save.setOnClickListener(view -> submitItem(type, item, description, imei, location, date, save, null));
            root.addView(save, contentParams(-1, dp(44), 0));
        } else if ("FOUND".equals(type)) {
            TextView publish = actionButton(translate("Submit report — free"), true);
            publish.setOnClickListener(view -> submitItem(type, item, description, imei, location, date, publish, null));
            root.addView(publish, contentParams(-1, dp(44), 0));
        } else {
            boolean unlocked = hasActiveAnnualSubscription();
            if (!unlocked) {
                root.addView(subscriptionCard(), contentParams(-1, dp(74), dp(14)));
            }
            TextView action = unlocked
                    ? actionButton(translate("Submit report — free"), true)
                    : filledButton(translate("Continue to payment"), LOST_GREEN, LOST_GREEN_ON);
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
            return translate("Renew plan");
        }
        if (System.currentTimeMillis() >= expiresAt) {
            account.edit().putBoolean("annual_subscription_active", false).remove("annual_subscription_payment_id").apply();
            return translate("Renew plan");
        }
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        return translate("Active subscription until") + " " + format.format(new Date(expiresAt));
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
                        String message = lastSubmissionError == null || lastSubmissionError.trim().isEmpty()
                                ? "Report saved securely"
                                : "Report saved without image: " + lastSubmissionError;
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        showReports();
                    } else {
                        publish.setText("Retry submission");
                        String detail = lastSubmissionError == null || lastSubmissionError.trim().isEmpty()
                                ? "Could not save report (" + code + ")"
                                : lastSubmissionError;
                        Toast.makeText(this, detail, Toast.LENGTH_LONG).show();
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
        lastSubmissionError = null;
        try {
            String endpoint = API_BASE + ("FOUND".equals(type) ? "/api/items/found" : "/api/items/lost");
            String imageUrl = null;
            if (image != null || cameraImage != null) {
                imageUrl = uploadImage(image, cameraImage, idToken);
                if (imageUrl == null) {
                    lastSubmissionError = "image storage unavailable";
                }
            }
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
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
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                lastSubmissionError = "Could not save report (" + responseCode + "): " + readErrorResponse(connection, responseCode);
            }
            return responseCode;
        } catch (Exception error) {
            lastSubmissionError = "Could not save report: " + error.getClass().getSimpleName();
            return -1;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private JSONObject postJson(String endpoint, String body, String idToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setDoOutput(true);
            if (idToken != null && !idToken.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + idToken);
            }
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();
            InputStream responseStream = (statusCode >= 200 && statusCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String response = readStream(responseStream);

            if (response == null || response.trim().isEmpty()) {
                if (statusCode >= 200 && statusCode < 300) {
                    return new JSONObject();
                }
                JSONObject emptyError = new JSONObject();
                emptyError.put("success", false);
                emptyError.put("detail", "Server request failed with status " + statusCode);
                return emptyError;
            }

            try {
                return new JSONObject(response);
            } catch (Exception jsonError) {
                JSONObject fallback = new JSONObject();
                fallback.put("success", statusCode >= 200 && statusCode < 300);
                fallback.put("detail", response.trim());
                return fallback;
            }
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
            String contentType = "image/jpeg";
            String filename = "item-image.jpg";

            if (image != null) {
                Bitmap bitmap = null;
                try (InputStream input = getContentResolver().openInputStream(image)) {
                    if (input != null) {
                        bitmap = BitmapFactory.decodeStream(input);
                    }
                }
                if (bitmap != null) {
                    int maxDim = 1280;
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (width > maxDim || height > maxDim) {
                        float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
                        int newWidth = Math.round(width * ratio);
                        int newHeight = Math.round(height * ratio);
                        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    }
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
                    data = output.toByteArray();
                } else {
                    return null;
                }
            } else if (cameraImage != null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                cameraImage.compress(Bitmap.CompressFormat.JPEG, 80, output);
                data = output.toByteArray();
                filename = "camera-image.jpg";
            } else {
                return null;
            }

            connection = (HttpURLConnection) new URL(API_BASE + "/api/upload").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
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
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                lastSubmissionError = "Image upload failed (" + responseCode + "): " + readErrorResponse(connection, responseCode);
                return null;
            }
            return new JSONObject(readStream(connection.getInputStream())).optString("url", null);
        } catch (Exception error) {
            lastSubmissionError = "Image upload failed: " + error.getClass().getSimpleName();
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String readErrorResponse(HttpURLConnection connection, int responseCode) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream == null) return formatDefaultError(responseCode);
            String response = readStream(errorStream);
            if (response == null || response.trim().isEmpty()) return formatDefaultError(responseCode);
            try {
                JSONObject json = new JSONObject(response);
                if (json.has("detail")) return json.optString("detail");
                if (json.has("message")) return json.optString("message");
            } catch (Exception ignored) {
                if (responseCode == 429) return "Too many requests (429). Please wait a few seconds and try again.";
                if (responseCode == 502) return "Server is temporarily busy or unreachable (502). Please try again.";
                if (responseCode == 504) return "Server request timed out (504). Please try again.";
                if (responseCode == 503) return "Server service temporarily unavailable (503).";
            }
            String trimmed = response.trim();
            if (trimmed.startsWith("<") || trimmed.toLowerCase(Locale.US).contains("<html")) {
                return formatDefaultError(responseCode);
            }
            return trimmed;
        } catch (Exception error) {
            return "Server error details unavailable";
        }
    }

    private String formatDefaultError(int responseCode) {
        if (responseCode == 429) return "Too many requests (429). Please wait a few seconds and try again.";
        if (responseCode == 502) return "Server is temporarily busy or unreachable (502). Please try again.";
        if (responseCode == 504) return "Server request timed out (504). Please try again.";
        if (responseCode == 503) return "Server service temporarily unavailable (503).";
        return "Server did not provide details";
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
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        long cooldownUntil = account.getLong("email_verify_cooldown_until", 0L);
        if (cooldownUntil > System.currentTimeMillis()) {
            startEmailVerificationCooldown(verifyButton, cooldownUntil, false);
            return;
        }

        boolean verified = account.getBoolean("email_verified", false);
        if (verified) {
            cancelEmailVerificationCooldown();
            setVerifiedButtonState(verifyButton);
            return;
        }

        String savedEmail = account.getString("email", "").trim();
        if (savedEmail != null && !savedEmail.isEmpty()) {
            email.setText(savedEmail);
        }
        verifyButton.setText(translate("Verify OTP"));
        verifyButton.setTextColor(GOLD_ON);
        verifyButton.setEnabled(true);
        verifyButton.setClickable(true);
        verifyButton.setFocusable(true);
        verifyButton.setBackground(round(GOLD, 24));
    }

    private void cancelEmailVerificationCooldown() {
        if (emailVerificationCooldownRunnable != null) {
            new Handler(getMainLooper()).removeCallbacks(emailVerificationCooldownRunnable);
            emailVerificationCooldownRunnable = null;
        }
    }

    private void startEmailVerificationCooldown(TextView verifyButton, long cooldownUntil, boolean persist) {
        cancelEmailVerificationCooldown();
        if (persist) {
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .putLong("email_verify_cooldown_until", cooldownUntil)
                    .apply();
        }
        updateEmailVerificationCountdown(verifyButton, cooldownUntil);
    }

    private void updateEmailVerificationCountdown(TextView verifyButton, long cooldownUntil) {
        if (verifyButton == null) {
            return;
        }
        long remainingMs = cooldownUntil - System.currentTimeMillis();
        if (remainingMs <= 0L) {
            verifyButton.setEnabled(true);
            verifyButton.setText(translate("Verify OTP"));
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .remove("email_verify_cooldown_until")
                    .apply();
            emailVerificationCooldownRunnable = null;
            return;
        }

        verifyButton.setEnabled(false);
        int seconds = (int) Math.ceil(remainingMs / 1000.0D);
        verifyButton.setText("Resend in " + seconds + "s");
        emailVerificationCooldownRunnable = new Runnable() {
            @Override
            public void run() {
                updateEmailVerificationCountdown(verifyButton, cooldownUntil);
            }
        };
        new Handler(getMainLooper()).postDelayed(emailVerificationCooldownRunnable, 1000L);
    }

    private void sendEmailOtpFlow(EditText emailField, TextView verifyButton) {
        String emailValue = emailField.getText().toString().trim();
        if (!validEmail(emailValue)) {
            emailField.setError("Email invalid");
            emailField.requestFocus();
            return;
        }

        long cooldownUntil = getSharedPreferences("fendly_account", MODE_PRIVATE).getLong("email_verify_cooldown_until", 0L);
        if (cooldownUntil > System.currentTimeMillis()) {
            startEmailVerificationCooldown(verifyButton, cooldownUntil, false);
            return;
        }

        verifyButton.setEnabled(false);
        verifyButton.setText(translate("Sending..."));
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("email", emailValue);
                JSONObject response = postJson("/api/auth/send-email-otp", payload.toString(), null);
                boolean sent = response != null && response.optBoolean("success", false);
                String errorMessage = response != null ? response.optString("detail", response.optString("message", "Could not send email OTP")) : "Could not send email OTP";
                runOnUiThread(() -> {
                    if (!sent) {
                        verifyButton.setText(translate("Verify OTP"));
                        verifyButton.setEnabled(true);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        return;
                    }
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                            .putString("email", emailValue)
                            .apply();
                    long newCooldownUntil = System.currentTimeMillis() + EMAIL_VERIFICATION_COOLDOWN_MS;
                    startEmailVerificationCooldown(verifyButton, newCooldownUntil, true);
                    showEmailOtpDialog(emailValue, verifyButton);
                    Toast.makeText(this, "Verification code sent to your email", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                    Toast.makeText(this, "Could not send email OTP", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showEmailOtpDialog(String emailValue, TextView verifyButton) {
        showThemedOtpDialog("Verify email", "Enter the 6-digit code sent to " + emailValue, "6-digit OTP", value -> {
            if (value.length() != 6) {
                verifyButton.setText(translate("Verify OTP"));
                verifyButton.setEnabled(true);
                Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_LONG).show();
                return false;
            }
            verifyEmailOtpCode(emailValue, value, verifyButton);
            return true;
        }, () -> {
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                });
    }

    private void verifyEmailOtpCode(String emailValue, String otpValue, TextView verifyButton) {
        verifyButton.setText(translate("Verifying..."));
        verifyButton.setEnabled(false);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("email", emailValue);
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-email-otp", payload.toString(), null);
                boolean verified = response != null && response.optBoolean("success", false);
                String errorMessage = response != null ? response.optString("detail", response.optString("message", "Could not verify email")) : "Could not verify email";
                runOnUiThread(() -> {
                    if (verified) {
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                .putString("email", emailValue)
                                .putBoolean("email_verified", true)
                                .apply();
                        setVerifiedButtonState(verifyButton);
                        Toast.makeText(this, "Email verified", Toast.LENGTH_SHORT).show();
                    } else {
                        verifyButton.setText(translate("Verify OTP"));
                        verifyButton.setEnabled(true);
                        verifyButton.setClickable(true);
                        verifyButton.setFocusable(true);
                        verifyButton.setBackground(round(GOLD, 24));
                        verifyButton.setTextColor(GOLD_ON);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                    verifyButton.setClickable(true);
                    verifyButton.setFocusable(true);
                    verifyButton.setBackground(round(GOLD, 24));
                    verifyButton.setTextColor(GOLD_ON);
                    Toast.makeText(this, "Could not verify email", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void verifyProfileMobile(EditText mobile, TextView verifyButton) {
        String mobileValue = normalizeLocalizedDigits(mobile.getText().toString().trim());
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
        verifyButton.setText(translate("Sending..."));
        verifyButton.setEnabled(false);
        String phoneNumber = normalizePhoneNumber(mobileValue);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("phone_number", phoneNumber);
                JSONObject response = postJson("/api/auth/send-otp", payload.toString(), null);
                String sessionId = response != null ? response.optString("session_id", "") : "";
                if (sessionId == null || sessionId.trim().isEmpty()) {
                    runOnUiThread(() -> {
                        verifyButton.setText(translate("Verify OTP"));
                        verifyButton.setEnabled(true);
                        Toast.makeText(MainActivity.this, "Could not send OTP", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                phoneVerificationId = sessionId;
                runOnUiThread(() -> {
                    verifyButton.setText(translate("Enter OTP"));
                    verifyButton.setEnabled(true);
                    showOtpDialogForProfile(mobileValue, mobile, verifyButton);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Could not send OTP", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showOtpDialogForProfile(String mobileValue, EditText mobile, TextView verifyButton) {
        showThemedOtpDialog("Verify mobile", "Enter the code sent to " + normalizePhoneNumber(mobileValue), "6-digit OTP", value -> {
            if (phoneVerificationId == null || value.length() != 6) {
                verifyButton.setText(translate("Verify OTP"));
                verifyButton.setEnabled(true);
                Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_LONG).show();
                return false;
            }
            verifyProfileOtp(phoneVerificationId, value, mobileValue, mobile, verifyButton);
            return true;
        }, () -> {
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                });
    }

    private void verifyProfileOtp(String sessionId, String otpValue, String mobileValue, EditText mobile, TextView verifyButton) {
        verifyButton.setText(translate("Verifying..."));
        verifyButton.setEnabled(false);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("session_id", sessionId);
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-otp", payload.toString(), null);
                boolean verified = response != null && response.optBoolean("success", false);
                runOnUiThread(() -> {
                    if (verified) {
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                .putString("mobile", mobileValue)
                                .putBoolean("mobile_verified", true)
                                .apply();
                        saveVerifiedMobileToCloud(mobileValue);
                        lockVerifiedMobileField(mobile, verifyButton);
                        mobile.setText(mobileValue);
                        Toast.makeText(this, "Mobile verified", Toast.LENGTH_SHORT).show();
                    } else {
                        verifyButton.setText(translate("Verify OTP"));
                        verifyButton.setEnabled(true);
                        verifyButton.setClickable(true);
                        verifyButton.setFocusable(true);
                        verifyButton.setBackground(round(GOLD, 24));
                        verifyButton.setTextColor(GOLD_ON);
                        Toast.makeText(this, "Could not verify mobile", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                    verifyButton.setClickable(true);
                    verifyButton.setFocusable(true);
                    verifyButton.setBackground(round(GOLD, 24));
                    verifyButton.setTextColor(GOLD_ON);
                    Toast.makeText(this, "Could not verify mobile", Toast.LENGTH_LONG).show();
                });
            }
        });
    }


    private void saveVerifiedMobileToCloud(String mobileValue) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("uid", user.getUid());
        update.put("mobile", mobileValue);
        update.put("mobileVerified", true);
        update.put("isMobileVerified", true);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(update, SetOptions.merge())
                .addOnFailureListener(error -> Log.e("FIREBASE_ERROR", "Data fetch failed: ", error));
    }
    private Bitmap bitmapFromUri(Uri uri) {
        if (uri == null) return null;
        try {
            if ("content".equals(uri.getScheme())) {
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    return input == null ? null : BitmapFactory.decodeStream(input);
                }
            }
            String path = "file".equals(uri.getScheme()) ? uri.getPath() : uri.toString();
            return BitmapFactory.decodeFile(path);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void showFullImagePreview(int slot) {
        if (slot < 0 || slot >= reportImages.length) return;
        Bitmap bitmap = reportCameraImages[slot];
        if (bitmap == null) {
            bitmap = bitmapFromUri(reportImages[slot]);
        }
        if (bitmap == null) return;

        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(true);

        FrameLayout container = new FrameLayout(this);
        container.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        container.setBackgroundColor(Color.BLACK);

        ImageView preview = new ImageView(this);
        preview.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setImageBitmap(bitmap);
        preview.setOnClickListener(view -> dialog.dismiss());
        container.addView(preview);

        ImageView close = new ImageView(this);
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setColorFilter(Color.WHITE);
        close.setBackground(roundWithStroke(Color.argb(180, 0, 0, 0), 18, Color.WHITE));
        close.setPadding(dp(4), dp(4), dp(4), dp(4));
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.TOP | Gravity.END);
        closeParams.setMargins(dp(12), dp(12), dp(12), 0);
        close.setOnClickListener(view -> dialog.dismiss());
        container.addView(close, closeParams);

        dialog.setContentView(container);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -1);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        dialog.show();
    }

    private LinearLayout imageSlots() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        for (int slot = 0; slot < 3; slot++) {
            final int imageSlot = slot;
            FrameLayout slotView = new FrameLayout(this);
            slotView.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            slotView.setClipToOutline(true);

            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setPadding(0, 0, 0, 0);
            image.setBackgroundColor(Color.TRANSPARENT);
            image.setImageResource(R.drawable.add_image);
            image.clearColorFilter();
            if (reportImages[slot] != null) {
                Bitmap existing = bitmapFromUri(reportImages[slot]);
                if (existing != null) {
                    image.setImageBitmap(existing);
                } else {
                    image.setImageResource(R.drawable.add_image);
                }
            }
            if (reportCameraImages[slot] != null) {
                image.clearColorFilter();
                image.setImageBitmap(reportCameraImages[slot]);
            }
            boolean hasImage = reportImages[slot] != null || reportCameraImages[slot] != null;
            image.setScaleX(hasImage ? 1f : 0.5f);
            image.setScaleY(hasImage ? 1f : 0.5f);
            if (hasImage) {
                image.setOnClickListener(view -> showFullImagePreview(imageSlot));
            } else {
                image.setOnClickListener(view -> showImageOptions(imageSlot));
            }
            slotView.addView(image, new FrameLayout.LayoutParams(-1, -1));

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
        addField(root, premiumCard(translate("Annual subscription"), localizedAnnualPriceText()));
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
        LinearLayout root = screenBase(translate("My reports"));
        addHeading(translate("Your reports"), translate("Keep track of items you are helping to reunite."));
        TextView loading = text(localizeReportsText("Loading reports..."), 16, secondaryTextColor(), Typeface.NORMAL);
        addField(root, loading);
        TextView home = actionButton(translate("Back home"), false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
        FirebaseAuth user = FirebaseAuth.getInstance();
        if (user.getCurrentUser() == null) {
            loading.setText(localizeReportsText("Sign in to view reports."));
            return;
        }
        user.getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = fetchReports(token.getToken());
            runOnUiThread(() -> {
                if (response == null) {
                    loading.setText(localizeReportsText("Reports are temporarily unavailable."));
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
                        addField(activeContent, text(localizeReportsText("No reports yet."), 16, secondaryTextColor(), Typeface.NORMAL));
                    }
                } catch (Exception error) {
                    loading.setText(localizeReportsText("Reports could not be read."));
                }
            });
        })).addOnFailureListener(error -> loading.setText(localizeReportsText("Authentication unavailable.")));
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

    private void hydrateProfileFromBackend() {
        hydrateProfileFromBackend(null);
    }

    private String languageCodeForIndex(int languageIndex) {
        String[] languageCodes = {"en", "hi", "mr", "gu", "bn", "ta", "te", "kn", "ml"};
        return languageCodes[Math.max(0, Math.min(languageIndex, languageCodes.length - 1))];
    }

    private void initializeCloudinary() {
        String cloudName = getString(R.string.cloudinary_cloud_name).trim();
        if (cloudName.isEmpty()) return;
        try {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("cloud_name", cloudName);
            MediaManager.init(this, config);
        } catch (IllegalStateException ignored) {
            // The process may already have initialized Cloudinary.
        }
    }

    private void hydrateCloudProfile(Runnable onComplete) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || cloudProfileHydrationInFlight) {
            if (onComplete != null) onComplete.run();
            return;
        }
        cloudProfileHydrationInFlight = true;
        if (cloudProfileListener != null) cloudProfileListener.remove();
        cloudProfileListener = FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .addSnapshotListener((document, error) -> {
                    if (error != null) {
                        Log.e("FIREBASE_ERROR", "Data fetch failed: ", error);
                        cloudProfileLoaded = true;
                        cloudProfileHydrationInFlight = false;
                        if (onComplete != null) runOnUiThread(onComplete);
                        return;
                    }
                    if (document != null && document.exists()) {
                        SharedPreferences.Editor editor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit();
                        putIfPresent(editor, "username", document.getString("username"));
                        putIfPresent(editor, "full_name", document.getString("full_name"));
                        putIfPresent(editor, "email", document.getString("email"));
                        putIfPresent(editor, "mobile", document.getString("mobile"));
                        putIfPresent(editor, "state", document.getString("state"));
                        putIfPresent(editor, "city", document.getString("city"));
                        putIfPresent(editor, "profile_image_url", document.getString("imageUrl"));
                        Boolean mobileVerified = document.getBoolean("mobileVerified");
                        if (mobileVerified == null) mobileVerified = document.getBoolean("mobile_verified");
                        if (mobileVerified != null) editor.putBoolean("mobile_verified", mobileVerified);
                        editor.apply();
                        runOnUiThread(() -> applyCloudProfileToVisibleFields(document));
                    }
                    cloudProfileLoaded = true;
                    boolean firstLoad = cloudProfileHydrationInFlight;
                    cloudProfileHydrationInFlight = false;
                    if (firstLoad && onComplete != null) runOnUiThread(onComplete);
                });
    }

    private void putIfPresent(SharedPreferences.Editor editor, String key, String value) {
        if (value != null && !value.trim().isEmpty()) editor.putString(key, value.trim());
    }

    private void applyCloudProfileToVisibleFields(DocumentSnapshot document) {
        if (applyingCloudProfile) return;
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        applyingCloudProfile = true;
        String fullName = document.getString("full_name");
        if (fullName != null && visibleFirstName != null) {
            String canonicalFull = getCanonicalEnglishName(fullName);
            String[] parts = canonicalFull.trim().split("\\s+", 2);
            setVisibleText(visibleFirstName, localizeProfileName(parts.length > 0 ? parts[0] : ""));
            if (visibleSurname != null) setVisibleText(visibleSurname, localizeProfileName(parts.length > 1 ? parts[1] : ""));
        }
        setVisibleText(visibleEmail, document.getString("email"));
        setVisibleText(visibleMobile, formatPhoneNumberForDisplay(document.getString("mobile")));
        applyingCloudProfile = false;
    }

    private void setVisibleText(EditText field, String value) {
        if (field == null || value == null || value.equals(field.getText().toString())) return;
        int cursor = Math.min(field.getSelectionStart(), value.length());
        field.setText(value);
        field.setSelection(Math.max(0, cursor));
    }

    private void scheduleRealtimeProfileSave() {
        if (applyingCloudProfile || visibleFirstName == null) return;
        if (realtimeProfileSave != null) realtimeProfileHandler.removeCallbacks(realtimeProfileSave);
        realtimeProfileSave = () -> saveVisibleProfileToCloud();
        realtimeProfileHandler.postDelayed(realtimeProfileSave, 350L);
    }

    private void saveVisibleProfileToCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || visibleFirstName == null) return;
        Map<String, Object> update = new LinkedHashMap<>();
        String canonicalFirst = getCanonicalEnglishName(visibleFirstName.getText().toString());
        String canonicalSur = getCanonicalEnglishName(visibleSurname != null ? visibleSurname.getText().toString() : "");
        String canonicalFull = (canonicalFirst + " " + canonicalSur).trim();
        update.put("full_name", canonicalFull);
        if (visibleEmail != null) update.put("email", visibleEmail.getText().toString().trim());
        if (visibleMobile != null) update.put("mobile", normalizeLocalizedDigits(visibleMobile.getText().toString().trim()));
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(update, SetOptions.merge())
                .addOnFailureListener(error -> Log.e("FIREBASE_ERROR", "Data fetch failed: ", error));
    }

    private void loadCloudProfileImage(ImageView avatar) {
        String imageUrl = getSharedPreferences("fendly_account", MODE_PRIVATE)
                .getString("profile_image_url", "").trim();
        if (imageUrl.isEmpty()) return;
        avatar.clearColorFilter();
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_field_person)
                .error(R.drawable.ic_field_person)
                .into(avatar);
    }

    private void saveCloudProfile(String username, String fullName, String email, String mobile,
                                  String state, String city, TextView saveButton) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sign in before saving your profile", Toast.LENGTH_LONG).show();
            return;
        }
        String existingImageUrl = getSharedPreferences("fendly_account", MODE_PRIVATE)
                .getString("profile_image_url", "").trim();
        Uri imageUri = selectedProfileImage;
        String uploadPreset = getString(R.string.cloudinary_upload_preset).trim();
        if (imageUri != null && uploadPreset.isEmpty()) {
            profileSaveFailed(saveButton, "Image upload is not configured");
            return;
        }
        if (imageUri != null) {
            try {
                MediaManager.get().upload(imageUri)
                        .unsigned(uploadPreset)
                        .callback(new UploadCallback() {
                            @Override public void onStart(String requestId) { }
                            @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
                            @Override public void onReschedule(String requestId, ErrorInfo error) { }
                            @Override public void onError(String requestId, ErrorInfo error) {
                                Log.e("FIREBASE_ERROR", "Data fetch failed: " + error.getDescription());
                                runOnUiThread(() -> profileSaveFailed(saveButton, "Image upload failed"));
                            }
                            @Override public void onSuccess(String requestId, Map resultData) {
                                Object secureUrl = resultData == null ? null : resultData.get("secure_url");
                                saveCloudProfileDocument(user, username, fullName, email, mobile, state, city,
                                        secureUrl == null ? existingImageUrl : String.valueOf(secureUrl), saveButton);
                            }
                        })
                        .dispatch();
                return;
            } catch (Exception exception) {
                Log.e("FIREBASE_ERROR", "Data fetch failed: ", exception);
                profileSaveFailed(saveButton, "Image upload unavailable");
                return;
            }
        }
        saveCloudProfileDocument(user, username, fullName, email, mobile, state, city, existingImageUrl, saveButton);
    }

    private void saveCloudProfileDocument(FirebaseUser user, String username, String fullName, String email,
                                          String mobile, String state, String city, String imageUrl,
                                          TextView saveButton) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("uid", user.getUid());
        profile.put("username", username);
        profile.put("full_name", fullName);
        profile.put("email", email);
        profile.put("mobile", mobile);
        profile.put("state", state);
        profile.put("city", city);
        profile.put("mobileVerified", getSharedPreferences("fendly_account", MODE_PRIVATE)
            .getBoolean("mobile_verified", false));
        profile.put("imageUrl", imageUrl == null ? "" : imageUrl);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(profile, SetOptions.merge())
                .addOnSuccessListener(unused -> runOnUiThread(() -> {
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                            .putString("profile_image_url", imageUrl == null ? "" : imageUrl).apply();
                    cloudProfileLoaded = true;
                    profileSaveSucceeded(saveButton);
                }))
                .addOnFailureListener(exception -> {
                    Log.e("FIREBASE_ERROR", "Data fetch failed: ", exception);
                    String reason = exception.getMessage();
                    runOnUiThread(() -> profileSaveFailed(saveButton,
                            reason == null || reason.trim().isEmpty()
                                    ? "Profile could not be saved"
                                    : "Profile save failed: " + reason));
                });
    }

    private void profileSaveSucceeded(TextView saveButton) {
        if (saveButton != null) {
            saveButton.setEnabled(true);
            saveButton.setText(localizedFieldLabel("Save changes"));
        }
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
    }

    private void profileSaveFailed(TextView saveButton, String message) {
        if (saveButton != null) saveButton.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void flushPendingProfileHydrationCallbacks() {
        List<Runnable> callbacks = new ArrayList<>(pendingProfileHydrationCallbacks);
        pendingProfileHydrationCallbacks.clear();
        if (!callbacks.isEmpty()) {
            runOnUiThread(() -> {
                for (Runnable callback : callbacks) {
                    if (callback != null) callback.run();
                }
            });
        }
    }

    private void hydrateProfileFromBackend(Runnable onComplete) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        if (profileHydrated) {
            if (onComplete != null) onComplete.run();
            return;
        }
        if (profileHydrationInFlight) {
            if (onComplete != null) pendingProfileHydrationCallbacks.add(onComplete);
            return;
        }
        if (onComplete != null) pendingProfileHydrationCallbacks.add(onComplete);
        profileHydrationInFlight = true;
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(API_BASE + "/api/users/profile").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Authorization", "Bearer " + token.getToken());
                if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                    String payload = readStream(connection.getInputStream());
                    if (payload != null && !payload.trim().isEmpty()) {
                        JSONObject profile = new JSONObject(payload);
                        String username = profile.optString("username", account.getString("username", "")).trim();
                        String fullName = profile.optString("full_name", account.getString("full_name", "")).trim();
                        String email = profile.optString("email", account.getString("email", "")).trim();
                        String mobile = profile.optString("mobile", account.getString("mobile", "")).trim();
                        String state = profile.optString("state", account.getString("state", "")).trim();
                        String city = profile.optString("city", account.getString("city", "")).trim();

                        SharedPreferences.Editor editor = account.edit();
                        if (!username.isEmpty()) editor.putString("username", username);
                        if (!fullName.isEmpty()) editor.putString("full_name", fullName);
                        if (!email.isEmpty()) editor.putString("email", email);
                        if (!mobile.isEmpty()) editor.putString("mobile", mobile);
                        if (!state.isEmpty()) editor.putString("state", state);
                        if (!city.isEmpty()) editor.putString("city", city);
                        editor.apply();
                    }
                }
            } catch (Exception exception) {
                Log.e("FIREBASE_ERROR", "Data fetch failed: ", exception);
                // Keep the current local profile data when the backend is temporarily unavailable;
                // a failed hydration should never wipe an already saved account on the device.
            } finally {
                if (connection != null) connection.disconnect();
                profileHydrationInFlight = false;
                profileHydrated = true;
                flushPendingProfileHydrationCallbacks();
            }
        })).addOnFailureListener(error -> {
            profileHydrationInFlight = false;
            profileHydrated = true;
            flushPendingProfileHydrationCallbacks();
        });
    }

    private void syncProfileWithBackend() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String localUsername = account.getString("username", "").trim();
        if (localUsername.isEmpty()) return;

        currentUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            HttpURLConnection connection = null;
            String remotePayload = null;
            try {
                connection = (HttpURLConnection) new URL(API_BASE + "/api/users/profile").openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Authorization", "Bearer " + token.getToken());
                if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                    remotePayload = readStream(connection.getInputStream());
                }
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }

            String username = localUsername;
            String fullName = account.getString("full_name", "").trim();
            String email = account.getString("email", "").trim();
            String mobile = account.getString("mobile", "").trim();
            String state = account.getString("state", "").trim();
            String city = account.getString("city", "").trim();

            try {
                if (remotePayload != null && !remotePayload.trim().isEmpty()) {
                    JSONObject remoteProfile = new JSONObject(remotePayload);
                    if (fullName.isEmpty()) fullName = remoteProfile.optString("full_name", "").trim();
                    if (email.isEmpty()) email = remoteProfile.optString("email", "").trim();
                    if (mobile.isEmpty()) mobile = remoteProfile.optString("mobile", "").trim();
                    if (state.isEmpty()) state = remoteProfile.optString("state", "").trim();
                    if (city.isEmpty()) city = remoteProfile.optString("city", "").trim();
                }
            } catch (Exception ignored) {
            }

            if (fullName.isEmpty() && email.isEmpty() && mobile.isEmpty() && state.isEmpty() && city.isEmpty()) {
                return;
            }

            HttpURLConnection putConnection = null;
            try {
                putConnection = (HttpURLConnection) new URL(API_BASE + "/api/users/profile").openConnection();
                putConnection.setRequestMethod("PUT");
                putConnection.setConnectTimeout(15000);
                putConnection.setReadTimeout(30000);
                putConnection.setDoOutput(true);
                putConnection.setRequestProperty("Authorization", "Bearer " + token.getToken());
                putConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body = "{\"username\":\"" + escapeJson(username) + "\",\"full_name\":\"" + escapeJson(fullName) + "\",\"email\":\"" + escapeJson(email) + "\",\"mobile\":\"" + escapeJson(mobile) + "\",\"state\":\"" + escapeJson(state) + "\",\"city\":\"" + escapeJson(city) + "\"}";
                try (OutputStream output = putConnection.getOutputStream()) {
                    output.write(body.getBytes(StandardCharsets.UTF_8));
                }
                putConnection.getResponseCode();
            } catch (Exception ignored) {
            } finally {
                if (putConnection != null) putConnection.disconnect();
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

    private void showProfileLoading() {
        currentPage = PAGE_PROFILE;
        screenRenderer = this::showProfile;
        FrameLayout loading = new FrameLayout(this);
        loading.setBackgroundColor(backgroundColor());
        ProgressBar progress = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.CENTER);
        loading.addView(progress, progressParams);
        setContentView(loading);
    }

    private void showProfile() {
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        currentPage = PAGE_PROFILE;
        if (currentUser != null) {
            if (!cloudProfileLoaded && !cloudProfileHydrationInFlight) hydrateCloudProfile(null);
            if (!profileHydrated && !profileHydrationInFlight) hydrateProfileFromBackend(null);
        }
        inRenewalPaymentFlow = false;
        currentPage = PAGE_PROFILE;
        screenRenderer = this::showProfile;
        LinearLayout root = screenBase(getString(R.string.profile_title));
        String profileHeadingName = account.getString("full_name", account.getString("username", "Your profile"));
        addCenteredHeading(localizeProfileName(profileHeadingName), getString(R.string.profile_account_details));

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
        loadCloudProfileImage(avatar);
        LinearLayout.LayoutParams avatarLayout = new LinearLayout.LayoutParams(dp(118), dp(118));
        avatarLayout.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(avatarWrap, avatarLayout);

        TextView photoHint = text(getString(R.string.profile_tap_upload_photo), 10, secondaryTextColor(), Typeface.NORMAL);
        photoHint.setGravity(Gravity.CENTER);
        root.addView(photoHint, contentParams(-1, dp(20), dp(10)));

        String subscriptionStatus = annualSubscriptionStatusText();
        TextView subscriptionMeta = text(subscriptionStatus, 11, secondaryTextColor(), Typeface.NORMAL);
        subscriptionMeta.setGravity(Gravity.CENTER);
        subscriptionMeta.setPadding(dp(8), 0, dp(8), 0);
        root.addView(subscriptionMeta, contentParams(-1, dp(20), dp(10)));

        restoreProfileDrafts();

        EditText firstName = field(getString(R.string.profile_first_name));
        EditText surname = field(getString(R.string.profile_surname));
        String rawSavedFullName = (!draftFullName.trim().isEmpty() ? draftFullName : account.getString("full_name", "")).trim();
        String savedFullName = getCanonicalEnglishName(rawSavedFullName);
        String[] nameParts = savedFullName.split("\\s+", 2);
        String savedFirstName = nameParts.length > 0 ? nameParts[0] : "";
        String savedSurname = nameParts.length > 1 ? nameParts[1] : "";
        firstName.setText(localizeProfileName(savedFirstName));
        surname.setText(localizeProfileName(savedSurname));
        firstName.setTag(savedFirstName);
        surname.setTag(savedSurname);
        visibleFirstName = firstName;
        visibleSurname = surname;
        firstName.addTextChangedListener(draftWatcher(value -> {
            String fn = getCanonicalEnglishName(value);
            String sn = getCanonicalEnglishName(surname.getText().toString().trim());
            draftFullName = (fn + " " + sn).trim();
            saveProfileDrafts();
            scheduleRealtimeProfileSave();
        }));
        surname.addTextChangedListener(draftWatcher(value -> {
            String fn = getCanonicalEnglishName(firstName.getText().toString().trim());
            String sn = getCanonicalEnglishName(value);
            draftFullName = (fn + " " + sn).trim();
            saveProfileDrafts();
            scheduleRealtimeProfileSave();
        }));

        addProfileField(root, getString(R.string.profile_username_login), account.getString("username", ""));

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.addView(labeledField(getString(R.string.profile_first_name), firstName), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams surnameParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        surnameParams.setMargins(dp(8), 0, 0, 0);
        nameRow.addView(labeledField(getString(R.string.profile_surname), surname), surnameParams);
        root.addView(nameRow, contentParams(-1, dp(76), dp(4)));

        EditText email = field(getString(R.string.profile_email_address));
        visibleEmail = email;
        email.setText(!draftEmail.trim().isEmpty() ? draftEmail : account.getString("email", ""));
        email.addTextChangedListener(draftWatcher(value -> {
            draftEmail = value;
            saveProfileDrafts();
            scheduleRealtimeProfileSave();
        }));
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

        EditText mobile = field(getString(R.string.profile_mobile_number));
        visibleMobile = mobile;
        String savedMobile = !draftMobile.trim().isEmpty() ? draftMobile : account.getString("mobile", "");
        mobile.setText(formatPhoneNumberForDisplay(savedMobile));
        mobile.setTag(savedMobile);
        mobile.addTextChangedListener(draftWatcher(value -> {
            draftMobile = value;
            saveProfileDrafts();
            scheduleRealtimeProfileSave();
        }));
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

        TextView emailVerify = filledButton(getString(R.string.profile_verify_otp), GOLD, GOLD_ON);
        emailVerify.setPadding(dp(12), 0, dp(12), 0);
        emailVerify.setOnClickListener(view -> sendEmailOtpFlow(email, emailVerify));
        refreshEmailVerificationState(email, emailVerify);

        TextView mobileVerify = filledButton(getString(R.string.profile_verify_otp), GOLD, GOLD_ON);
        mobileVerify.setPadding(dp(10), 0, dp(10), 0);
        mobileVerify.setOnClickListener(view -> verifyProfileMobile(mobile, mobileVerify));
        String verifiedMobile = account.getString("mobile", "").trim();
        String enteredMobile = normalizeLocalizedDigits(mobile.getText().toString().trim());
        boolean mobileAlreadyVerified = verifiedMobile.matches("^\\d{10}$")
            && enteredMobile.matches("^\\d{10}$")
            && account.getBoolean("mobile_verified", false)
            && verifiedMobile.equals(enteredMobile);
        if (mobileAlreadyVerified) {
            lockVerifiedMobileField(mobile, mobileVerify);
        } else {
            mobile.setEnabled(true);
            mobile.setFocusable(true);
            mobile.setFocusableInTouchMode(true);
            mobile.setCursorVisible(true);
        }
        mobile.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                String currentMobile = normalizeLocalizedDigits(value.toString().trim());
                boolean verifiedNow = currentMobile.matches("^\\d{10}$")
                    && getSharedPreferences("fendly_account", MODE_PRIVATE).getBoolean("mobile_verified", false)
                    && currentMobile.equals(getSharedPreferences("fendly_account", MODE_PRIVATE).getString("mobile", "").trim());
                if (verifiedNow) {
                    setVerifiedButtonState(mobileVerify);
                    return;
                }
                if (!verifiedMobile.equals(currentMobile)) {
                    mobileVerify.setText(translate("Verify OTP"));
                    mobileVerify.setEnabled(true);
                    mobileVerify.setClickable(true);
                    mobileVerify.setFocusable(true);
                    mobileVerify.setBackground(round(GOLD, 24));
                    mobileVerify.setTextColor(GOLD_ON);
                    mobile.setEnabled(true);
                    mobile.setFocusable(true);
                    mobile.setFocusableInTouchMode(true);
                    mobile.setCursorVisible(true);
                }
            }
            @Override public void afterTextChanged(Editable value) { }
        });

        Map<String, String[]> stateCities = indiaStateCityMap();
        String[] states = stateCities.keySet().toArray(new String[0]);
        Arrays.sort(states, 1, states.length);
        AutoCompleteTextView stateSearch = new AutoCompleteTextView(this);
        AutoCompleteTextView citySearch = new AutoCompleteTextView(this);
        String savedState = !draftState.trim().isEmpty() ? draftState : account.getString("state", "");
        String savedCity = !draftCity.trim().isEmpty() ? draftCity : account.getString("city", "");
        stateSearch.setText(localizeProfileDisplayValue("state", savedState));
        citySearch.setText(localizeProfileDisplayValue("city", savedCity));
        stateSearch.setTag(savedState);
        citySearch.setTag(savedCity);
        stateSearch.setHint("");
        citySearch.setHint("");
        stateSearch.setSingleLine(true);
        citySearch.setSingleLine(true);
        stateSearch.setInputType(InputType.TYPE_NULL);
        citySearch.setInputType(InputType.TYPE_NULL);
        stateSearch.setFocusable(false);
        citySearch.setFocusable(false);
        citySearch.setEnabled(false);
        applyLocationFieldStyle(stateSearch);
        applyLocationFieldStyle(citySearch);

        stateSearch.setOnClickListener(clickedView -> showStatePicker(states, stateSearch, citySearch, stateCities));
        stateSearch.addTextChangedListener(draftWatcher(value -> {
            draftState = value;
            saveProfileDrafts();
        }));
        citySearch.addTextChangedListener(draftWatcher(value -> {
            draftCity = value;
            saveProfileDrafts();
        }));
        citySearch.setOnClickListener(clickedView -> {
            String selectedState = reverseLocalizedProfileValue("state", stateSearch.getText().toString().trim());
            String[] cities = stateCities.getOrDefault(selectedState, new String[]{defaultSelectCityText()});
            if (citySearch.isEnabled()) showCityPicker(selectedState, cities, citySearch);
        });

        String[] selectedCities = stateCities.getOrDefault(savedState, new String[0]);
        citySearch.setEnabled(selectedCities.length > 0 && !isSelectCityPlaceholder(selectedCities[0]));

        LinearLayout locationRow = new LinearLayout(this);
        locationRow.setOrientation(LinearLayout.HORIZONTAL);
        locationRow.addView(labeledCitySearch(getString(R.string.profile_state), stateSearch), new LinearLayout.LayoutParams(0, dp(76), 1f));
        LinearLayout.LayoutParams cityParams = new LinearLayout.LayoutParams(0, dp(76), 1f);
        cityParams.setMargins(dp(8), 0, 0, 0);
        locationRow.addView(labeledCitySearch(getString(R.string.profile_city), citySearch), cityParams);
        root.addView(locationRow, contentParams(-1, dp(76), dp(4)));

        addEditableProfileField(root, getString(R.string.profile_email), email, emailVerify);
        addEditableProfileField(root, getString(R.string.profile_mobile), mobile, mobileVerify);
        EditText[] changePinCells = pinCells();
        addLabeledPinField(root, getString(R.string.profile_new_pin), changePinCells);
        TextView changePinButton = actionButton(getString(R.string.profile_change_pin), false);
        changePinButton.setOnClickListener(view -> {
            String newPin = pinValue(changePinCells);
            changeAccountPin(account.getString("username", ""), getStoredAccountPin(), newPin, changePinButton);
        });
        addField(root, changePinButton);

        TextView save = actionButton(getString(R.string.profile_save_changes), true);
        save.setOnClickListener(view -> {
            String updatedFirstName = getCanonicalEnglishName(firstName.getText().toString());
            String updatedSurname = getCanonicalEnglishName(surname.getText().toString());
            String originalFirstName = getCanonicalEnglishName(String.valueOf(firstName.getTag() == null ? "" : firstName.getTag()));
            String originalSurname = getCanonicalEnglishName(String.valueOf(surname.getTag() == null ? "" : surname.getTag()));
            if (updatedFirstName.equalsIgnoreCase(originalFirstName) || updatedFirstName.equals(localizeProfileName(originalFirstName))) updatedFirstName = originalFirstName;
            if (updatedSurname.equalsIgnoreCase(originalSurname) || updatedSurname.equals(localizeProfileName(originalSurname))) updatedSurname = originalSurname;
            String updatedFullName = (updatedFirstName + " " + updatedSurname).trim();
            String updatedEmail = email.getText().toString().trim();
            String updatedMobile = normalizeLocalizedDigits(mobile.getText().toString().trim());
            String originalMobile = String.valueOf(mobile.getTag() == null ? "" : mobile.getTag());
            if (updatedMobile.equals(localizeProfileDisplayValue("mobile", originalMobile))) updatedMobile = originalMobile;
            String selectedState = reverseLocalizedProfileValue("state", stateSearch.getText().toString().trim());
            String selectedCity = reverseLocalizedProfileValue("city", citySearch.getText().toString().trim());
            String originalState = String.valueOf(stateSearch.getTag() == null ? "" : stateSearch.getTag());
            if (selectedState.equals(localizeProfileDisplayValue("state", originalState))) selectedState = originalState;
            String originalCity = String.valueOf(citySearch.getTag() == null ? "" : citySearch.getTag());
            if (selectedCity.equals(localizeProfileDisplayValue("city", originalCity))) selectedCity = originalCity;
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
            if (selectedState == null || selectedState.trim().isEmpty() || isSelectStatePlaceholder(selectedState) || selectedCity.isEmpty() || isSelectCityPlaceholder(selectedCity) || !cityMatchesState) {
                Toast.makeText(this, "Please select your state and city", Toast.LENGTH_LONG).show();
                stateSearch.requestFocus();
                return;
            }
            boolean verifiedMobileNumber = account.getBoolean("mobile_verified", false)
                    && updatedMobile.equals(account.getString("mobile", "").trim());
            if (REQUIRE_MOBILE_OTP_FOR_PROFILE_SAVE && !verifiedMobileNumber) {
                Toast.makeText(this, "Verify mobile OTP before saving changes", Toast.LENGTH_SHORT).show();
                blinkVerificationRequired(mobileVerify);
                mobile.requestFocus();
                return;
            }
            account.edit()
                    .putString("full_name", updatedFullName)
                    .putString("email", updatedEmail)
                    .putString("mobile", updatedMobile)
                    .putBoolean("mobile_verified", verifiedMobileNumber)
                    .putString("state", selectedState)
                    .putString("city", selectedCity)
                    .apply();
            clearProfileDrafts();
                save.setEnabled(false);
                save.setText(getString(R.string.profile_saving));
                saveCloudProfile(account.getString("username", "").trim(), updatedFullName, updatedEmail,
                    updatedMobile, selectedState, selectedCity, save);
        });
        root.addView(save, contentParams(-1, dp(44), dp(10)));

        TextView home = actionButton(getString(R.string.profile_back_home), false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
        applyProfileFont(root);
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
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setHorizontalScrollBarEnabled(false);
        activeContent = new LinearLayout(this);
        activeContent.setOrientation(LinearLayout.VERTICAL);
        activeContent.setPadding(0, dp(26), 0, 0);
        activeContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(activeContent, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
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
        TextView titleView = text(title, 20, primaryTextColor(), Typeface.NORMAL);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(localizedScriptTypeface(titleView.getText(), Typeface.NORMAL));
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
        input.setTextColor(primaryTextColor());
        input.setHintTextColor(secondaryTextColor());
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        input.setPadding(dp(16), dp(8), dp(16), dp(8));
        input.setIncludeFontPadding(false);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        input.setTypeface(typefaceForTextValue(input.getText(), Typeface.NORMAL));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        input.setOnFocusChangeListener((view, focused) ->
            input.setBackground(roundWithStroke(surfaceColor(), 10, focused ? accentColor() : fieldBorderColor())));
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                input.setTypeface(typefaceForTextValue(value, Typeface.NORMAL));
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        return input;
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

        String priceText = detail != null && !detail.trim().isEmpty() ? detail : localizedAnnualPriceText();
        SpannableString detailText = new SpannableString(priceText);
        int priceLength = localizeDigits("₹99").length();
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

    private boolean isUsernameLabel(String label) {
        if (label == null) return false;
        String lower = label.toLowerCase(Locale.US);
        return lower.contains("user") || lower.contains("username") || lower.contains("login")
                || lower.contains("वापरकर्ता") || lower.contains("उपयोगकर्ता") || lower.contains("صارف")
                || lower.contains("ಬಳಕೆದಾರ") || lower.contains("వినియోగదారు") || lower.contains("ব্যবহারকারী")
                || label.equalsIgnoreCase(getString(R.string.profile_username_login))
                || label.equalsIgnoreCase(getString(R.string.profile_username));
    }

    private void addProfileField(LinearLayout parent, String label, String value) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));

        String displayValue = isUsernameLabel(label)
                ? localizeProfileDisplayValue("username", value)
                : localizeProfileDisplayValue(label, value);
        TextView valueView = text(displayValue, 14, primaryTextColor(), Typeface.NORMAL);
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
            verifyButton.setClickable(true);
            verifyButton.setFocusable(true);
            verifyButton.setEnabled(true);
            verifyButton.setMinWidth(dp(92));
            verifyButton.setPadding(dp(12), dp(6), dp(12), dp(6));
            verifyButton.setIncludeFontPadding(false);
            verifyButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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

        String displayLabel = localizedFieldLabel(label);
        if (displayLabel == null) displayLabel = label;
        TextView labelView = text(displayLabel, 10, secondaryTextColor(), Typeface.BOLD);
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
        return digitCells(4, true);
    }

    private EditText[] otpCells() {
        return digitCells(6, false);
    }

    private EditText[] digitCells(int count, boolean password) {
        EditText[] cells = new EditText[count];
        for (int index = 0; index < cells.length; index++) {
            EditText cell = new EditText(this);
            if (Build.VERSION.SDK_INT >= 29) cell.setForceDarkAllowed(false);
            cell.setId(View.generateViewId());
                cell.setInputType(password
                    ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    : InputType.TYPE_CLASS_NUMBER);
            cell.setTextColor(primaryTextColor());
            cell.setHintTextColor(secondaryTextColor());
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

    private void lockVerifiedMobileField(EditText mobile, TextView verifyButton) {
        if (mobile == null) return;
        mobile.setEnabled(false);
        mobile.setFocusable(false);
        mobile.setFocusableInTouchMode(false);
        mobile.setCursorVisible(false);
        mobile.setKeyListener(null);
        setVerifiedButtonState(verifyButton);
    }

    private void blinkVerificationRequired(TextView verifyButton) {
        if (verifyButton == null) return;
        verifyButton.setEnabled(true);
        verifyButton.setClickable(true);
        verifyButton.setFocusable(true);
        verifyButton.setText(translate("Verify OTP"));
        final Handler handler = new Handler(getMainLooper());
        final int[] blinkStep = {0};
        Runnable blink = new Runnable() {
            @Override
            public void run() {
                if (blinkStep[0] >= 6) {
                    verifyButton.setBackground(round(GOLD, 24));
                    verifyButton.setTextColor(GOLD_ON);
                    return;
                }
                boolean isRed = (blinkStep[0] % 2 == 0);
                verifyButton.setBackground(round(isRed ? Color.rgb(217, 71, 71) : GOLD, 24));
                verifyButton.setTextColor(Color.WHITE);
                blinkStep[0]++;
                handler.postDelayed(this, 180L);
            }
        };
        handler.post(blink);
    }

    private void setVerifiedButtonState(TextView button) {
        if (button == null) return;
        button.setText(translate("Verified"));
        button.setTextColor(Color.WHITE);
        button.setBackground(round(LOST_GREEN, 24));
        button.setEnabled(false);
        button.setClickable(false);
        button.setFocusable(false);
        button.setOnClickListener(null);
    }

    private TextView filledButton(String label, int background, int foreground) {
        TextView button = text(label, 12, foreground, Typeface.NORMAL);
        button.setTextColor(background == LOST_GREEN ? Color.WHITE : foreground);
        button.setGravity(Gravity.CENTER);
        button.setBackground(round(background, 24));
        button.setClickable(true);
        button.setFocusable(true);
        button.setEnabled(true);
        button.setMinHeight(dp(34));
        button.setMinWidth(dp(92));
        button.setPadding(dp(12), dp(6), dp(12), dp(6));
        button.setIncludeFontPadding(false);
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

        TextView subscriptionLabel = text(translate("Annual subscription"), 18, secondaryTextColor(), Typeface.NORMAL);
        subscriptionLabel.setIncludeFontPadding(false);
        subscriptionLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        copy.addView(subscriptionLabel, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView subscriptionPrice = text("", 22, primaryTextColor(), Typeface.NORMAL);
        subscriptionPrice.setIncludeFontPadding(false);
        subscriptionPrice.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        String priceTextValue = localizedAnnualPriceText();
        SpannableString priceText = new SpannableString(priceTextValue);
        int priceLength = localizeDigits("₹99").length();
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
        chip.setEllipsize(TextUtils.TruncateAt.END);
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

    private void applyLocationToggleVisualState(TextView button, TextView toggle, boolean enabled) {
        if (button == null) return;
        if (enabled) {
            button.setText("Location ready");
            button.setTextColor(Color.WHITE);
            button.setBackground(roundWithStroke(LOST_GREEN, 10, LOST_GREEN));
        } else {
            button.setText("Precise location  OFF");
            button.setTextColor(secondaryTextColor());
            button.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        }
        if (toggle != null) {
            if (enabled) {
                toggle.setText("Precise location  ON");
                toggle.setTextColor(Color.WHITE);
                toggle.setBackground(roundWithStroke(LOST_GREEN, 10, LOST_GREEN));
            } else {
                toggle.setText("Precise location  OFF");
                toggle.setTextColor(secondaryTextColor());
                toggle.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            }
        }
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
                    applyLocationToggleVisualState(button, toggle, false);
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
                applyLocationToggleVisualState(button, toggle, false);
                return;
            }
            currentLat = best.getLatitude();
            currentLng = best.getLongitude();
            hasLocation = true;
            activeLocationReportType = currentReportType;
            applyLocationToggleVisualState(button, toggle, true);
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
                    selectedProfileImage = Uri.fromFile(new File(savedPath));
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
        root.addView(text("Admin workspace", 20, primaryTextColor(), Typeface.BOLD), contentParams(-1, dp(28), dp(4)));
        root.addView(text("Review reports and verify possible matches", 11, secondaryTextColor(), Typeface.NORMAL), contentParams(-1, dp(22), dp(14)));

        LinearLayout overview = new LinearLayout(this);
        overview.setOrientation(LinearLayout.HORIZONTAL);
        TextView lostCount = adminSummary("LOST", LOST_GREEN);
        TextView foundCount = adminSummary("FOUND", FOUND_GOLD);
        overview.addView(lostCount, new LinearLayout.LayoutParams(0, dp(62), 1f));
        LinearLayout.LayoutParams foundCountParams = new LinearLayout.LayoutParams(0, dp(62), 1f);
        foundCountParams.setMargins(dp(8), 0, 0, 0);
        overview.addView(foundCount, foundCountParams);
        root.addView(overview, contentParams(-1, dp(62), dp(16)));

        root.addView(text("Find a user", 13, primaryTextColor(), Typeface.BOLD), contentParams(-1, dp(20), dp(6)));
        EditText search = field("Name, surname, mobile, username, or email");
        root.addView(search, contentParams(-1, dp(52), dp(8)));
        TextView searchButton = actionButton("Search", true);
        root.addView(searchButton, contentParams(-1, dp(44), dp(12)));

        TextView reportsHeading = text("Live reports", 15, primaryTextColor(), Typeface.BOLD);
        root.addView(reportsHeading, contentParams(-1, dp(22), dp(8)));
        LinearLayout reports = new LinearLayout(this);
        reports.setOrientation(LinearLayout.VERTICAL);
        root.addView(reports, contentParams(-1, -2, 0));

        TextView matchesHeading = text("AI match review", 15, primaryTextColor(), Typeface.BOLD);
        matchesHeading.setVisibility(View.GONE);
        root.addView(matchesHeading, contentParams(-1, dp(22), dp(12)));
        LinearLayout matches = new LinearLayout(this);
        matches.setOrientation(LinearLayout.VERTICAL);
        root.addView(matches, contentParams(-1, -2, 0));

        searchButton.setOnClickListener(view -> searchAdminUsers(search.getText().toString().trim(), reports, searchButton));
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser != null) {
            adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                String response = fetchAdminItems(token.getToken());
                runOnUiThread(() -> renderAdminReports(response, reports, matchesHeading, matches, lostCount, foundCount));
            }));
        }
    }

    private TextView adminSummary(String label, int accent) {
        TextView summary = text(label + "  0", 12, accent, Typeface.BOLD);
        summary.setGravity(Gravity.CENTER_VERTICAL);
        summary.setPadding(dp(14), 0, dp(14), 0);
        summary.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        return summary;
    }

    private void renderAdminReports(String response, LinearLayout reports, TextView matchesHeading,
                                    LinearLayout matches, TextView lostCount, TextView foundCount) {
        reports.removeAllViews();
        if (response == null) {
            reports.addView(text("Reports unavailable.", 13, secondaryTextColor(), Typeface.NORMAL));
            return;
        }
        try {
            JSONArray items = new JSONArray(response);
            int lost = 0;
            int found = 0;
            LinearLayout lostSection = adminReportSection("LOST REPORTS", LOST_GREEN);
            LinearLayout foundSection = adminReportSection("FOUND REPORTS", FOUND_GOLD);
            for (int index = 0; index < items.length(); index++) {
                JSONObject item = items.getJSONObject(index);
                if ("FOUND".equalsIgnoreCase(item.optString("type"))) {
                    found++;
                    foundSection.addView(adminReportCard(item, true, matchesHeading, matches));
                } else {
                    lost++;
                    lostSection.addView(adminReportCard(item, false, matchesHeading, matches));
                }
            }
            lostCount.setText("LOST  " + lost);
            foundCount.setText("FOUND  " + found);
            reports.addView(lostSection, contentParams(-1, -2, 0));
            reports.addView(foundSection, contentParams(-1, -2, dp(12)));
        } catch (Exception error) {
            reports.addView(text("Reports could not be read.", 13, secondaryTextColor(), Typeface.NORMAL));
        }
    }

    private LinearLayout adminReportSection(String title, int accent) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        TextView heading = text(title, 11, accent, Typeface.BOLD);
        heading.setPadding(dp(2), 0, 0, dp(6));
        section.addView(heading, new LinearLayout.LayoutParams(-1, dp(24)));
        return section;
    }

    private LinearLayout adminReportCard(JSONObject item, boolean isFound, TextView matchesHeading, LinearLayout matches) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        TextView title = text(item.optString("title", "Untitled"), 14, primaryTextColor(), Typeface.BOLD);
        card.addView(title, new LinearLayout.LayoutParams(-1, dp(22)));
        String details = item.optString("category", "other") + "  ·  " + item.optString("description", "No description");
        TextView detail = text(details, 11, secondaryTextColor(), Typeface.NORMAL);
        detail.setMaxLines(2);
        card.addView(detail, new LinearLayout.LayoutParams(-1, dp(38)));
        TextView location = text(String.format(Locale.US, "Location  %.4f, %.4f", item.optDouble("lat", 0.0), item.optDouble("lng", 0.0)), 10, secondaryTextColor(), Typeface.NORMAL);
        card.addView(location, new LinearLayout.LayoutParams(-1, dp(20)));
        if (isFound) {
            TextView review = text("Review AI matches  ›", 11, GOLD_ON, Typeface.BOLD);
            review.setGravity(Gravity.CENTER);
            review.setBackground(roundWithStroke(GOLD, 10, GOLD));
            LinearLayout.LayoutParams reviewParams = new LinearLayout.LayoutParams(-1, dp(36));
            reviewParams.setMargins(0, dp(8), 0, 0);
            card.addView(review, reviewParams);
            card.setOnClickListener(view -> loadAdminMatches(item, matchesHeading, matches));
        }
        return card;
    }

    private void loadAdminMatches(JSONObject foundItem, TextView heading, LinearLayout matches) {
        heading.setVisibility(View.VISIBLE);
        matches.removeAllViews();
        matches.addView(text("Comparing against lost reports...", 12, secondaryTextColor(), Typeface.NORMAL));
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) return;
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = fetchAdminMatches(foundItem.optString("id", ""), token.getToken());
            runOnUiThread(() -> renderAdminMatches(foundItem, response, matches));
        }));
    }

    private void renderAdminMatches(JSONObject foundItem, String response, LinearLayout matches) {
        matches.removeAllViews();
        try {
            JSONArray results = new JSONArray(response == null ? "[]" : response);
            if (results.length() == 0) {
                matches.addView(text("No possible matches found.", 12, secondaryTextColor(), Typeface.NORMAL));
                return;
            }
            for (int index = 0; index < Math.min(results.length(), 5); index++) {
                JSONObject result = results.getJSONObject(index);
                JSONObject lostItem = result.optJSONObject("item");
                if (lostItem != null) matches.addView(adminMatchCard(foundItem, lostItem, result.optDouble("score", 0.0)), contentParams(-1, -2, dp(8)));
            }
        } catch (Exception error) {
            matches.addView(text("AI matches unavailable.", 12, secondaryTextColor(), Typeface.NORMAL));
        }
    }

    private LinearLayout adminMatchCard(JSONObject foundItem, JSONObject lostItem, double score) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        TextView scoreLabel = text(String.format(Locale.US, "AI MATCH  %d%%", Math.round(score * 100)), 11, GOLD_ON, Typeface.BOLD);
        scoreLabel.setGravity(Gravity.CENTER);
        scoreLabel.setBackground(round(GOLD, 10));
        card.addView(scoreLabel, new LinearLayout.LayoutParams(-1, dp(30)));
        LinearLayout comparison = new LinearLayout(this);
        comparison.setOrientation(LinearLayout.HORIZONTAL);
        comparison.addView(adminComparisonColumn("FOUND", foundItem, FOUND_GOLD), new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams lostParams = new LinearLayout.LayoutParams(0, -2, 1f);
        lostParams.setMargins(dp(8), 0, 0, 0);
        comparison.addView(adminComparisonColumn("LOST", lostItem, LOST_GREEN), lostParams);
        card.addView(comparison, new LinearLayout.LayoutParams(-1, -2));
        return card;
    }

    private LinearLayout adminComparisonColumn(String label, JSONObject item, int accent) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(10), dp(10), dp(10), dp(10));
        column.setBackground(roundWithStroke(backgroundColor(), 10, accent));
        column.addView(text(label, 10, accent, Typeface.BOLD), new LinearLayout.LayoutParams(-1, dp(18)));
        column.addView(text(item.optString("title", "Untitled"), 13, primaryTextColor(), Typeface.BOLD), new LinearLayout.LayoutParams(-1, dp(24)));
        TextView description = text(item.optString("description", "No description"), 10, secondaryTextColor(), Typeface.NORMAL);
        description.setMaxLines(3);
        column.addView(description, new LinearLayout.LayoutParams(-1, dp(48)));
        column.addView(text(item.optString("category", "other"), 10, secondaryTextColor(), Typeface.NORMAL), new LinearLayout.LayoutParams(-1, dp(18)));
        return column;
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
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        EditText username = field("Admin username");
        EditText[] adminPinCells = pinCells();
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(22), dp(22), dp(18));
        form.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_field_lock);
        icon.setColorFilter(accentColor());
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(60), dp(60));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        form.addView(icon, iconParams);

        TextView title = text("Admin login", 18, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        form.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text("Authorized access only", 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setGravity(Gravity.CENTER);
        form.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(28)));

        LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(-1, dp(48));
        usernameParams.setMargins(0, dp(10), 0, 0);
        form.addView(username, usernameParams);
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

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView cancel = text("Cancel", 12, secondaryTextColor(), Typeface.BOLD);
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(view -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(88), dp(44)));
        TextView login = text("Login", 12, GOLD_ON, Typeface.BOLD);
        login.setGravity(Gravity.CENTER);
        login.setBackground(goldButton());
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(dp(100), dp(44));
        loginParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(login, loginParams);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, dp(44));
        actionsParams.setMargins(0, dp(16), 0, 0);
        form.addView(actions, actionsParams);

        login.setOnClickListener(view -> {
            String adminUsername = username.getText().toString().trim().toLowerCase(Locale.US);
            String adminPin = pinValue(adminPinCells);
            if (!adminUsername.matches("^[a-z0-9_]{3,32}$") || !adminPin.matches("^\\d{4}$")) {
                Toast.makeText(this, "Enter a valid username and 4-digit PIN", Toast.LENGTH_LONG).show();
                return;
            }
            login.setEnabled(false);
            login.setText("Checking...");
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
                        login.setEnabled(true);
                        login.setText("Login");
                        Toast.makeText(this, "Admin verification unavailable", Toast.LENGTH_LONG).show();
                    }))
                    .addOnFailureListener(error -> {
                        login.setEnabled(true);
                        login.setText("Login");
                        Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_LONG).show();
                    });
        });
        dialog.setContentView(form);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
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
            paint.setTypeface(languageTypeface(Typeface.BOLD));
            paint.setTextSize(dp(12));
            paint.setColor(authTextColor());
            paint.setAlpha(255);
            setAlpha(1f);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(authTextColor());
            Paint.FontMetrics metrics = paint.getFontMetrics();
            float baseline = (getHeight() - metrics.bottom - metrics.top) / 2f;
            canvas.drawText(label, 0, baseline, paint);
        }
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(this);
        String resolvedText = translate(value);
        view.setText(resolvedText);
        float fontScale = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        view.setTextSize(size * Math.max(1.0f, Math.min(1.2f, fontScale)));
        view.setTag(Float.valueOf(size));
        view.setTextColor(color != 0 ? color : (darkMode ? Color.WHITE : LIGHT_TEXT));
        view.setTypeface(typefaceForTextValue(resolvedText, style));
        return view;
    }

    private Typeface languageTypeface(int style) {
        String assetPath = getSelectedLanguageFontAsset();
        if (assetPath == null) {
            return Typeface.create(Typeface.SANS_SERIF, style);
        }
        try {
            Typeface base = Typeface.createFromAsset(getAssets(), assetPath);
            return Typeface.create(base, style);
        } catch (RuntimeException e) {
            return Typeface.create(Typeface.SANS_SERIF, style);
        }
    }

    private void applyProfileFont(View view) {
        if (view == null || selectedLanguage != 2) return;
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int style = textView.getTypeface() != null && textView.getTypeface().isBold()
                    ? Typeface.BOLD : Typeface.NORMAL;
            Typeface devanagari = ResourcesCompat.getFont(this, R.font.noto_sans_devanagari);
            if (devanagari != null) textView.setTypeface(Typeface.create(devanagari, style));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                applyProfileFont(group.getChildAt(index));
            }
        }
    }

    private String getSelectedLanguageFontAsset() {
        int lang = Math.max(0, Math.min(selectedLanguage, 8));
        switch (lang) {
            case 1:
            case 2:
                return "fonts/NotoSansDevanagari[wdth,wght].ttf";
            case 4:
                return "fonts/NotoSansBengali[wdth,wght].ttf";
            case 5:
                return "fonts/NotoSansTamil[wdth,wght].ttf";
            case 6:
                return "fonts/NotoSansTelugu[wdth,wght].ttf";
            case 7:
                return "fonts/NotoSansKannada[wdth,wght].ttf";
            case 8:
                return "fonts/NotoSansMalayalam[wdth,wght].ttf";
            default:
                return null;
        }
    }

    private Typeface typefaceForTextValue(CharSequence value, int style) {
        String text = value == null ? "" : value.toString();
        String assetPath = resolveAssetPathForText(text);
        if (assetPath == null) {
            return Typeface.create(Typeface.SANS_SERIF, style);
        }
        try {
            Typeface base = Typeface.createFromAsset(getAssets(), assetPath);
            return Typeface.create(base, style);
        } catch (RuntimeException e) {
            return Typeface.create(Typeface.SANS_SERIF, style);
        }
    }

    private Typeface localizedScriptTypeface(CharSequence value, int style) {
        if (selectedLanguage >= 1 && selectedLanguage <= 8) {
            return languageTypeface(style);
        }
        return typefaceForTextValue(value, style);
    }

    private String resolveAssetPathForText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return getSelectedLanguageFontAsset();
        }
        if (containsUnicodeRange(text, 0x0900, 0x097F)) {
            return "fonts/NotoSansDevanagari[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0980, 0x09FF)) {
            return "fonts/NotoSansBengali[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0C80, 0x0CFF)) {
            return "fonts/NotoSansKannada[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0C00, 0x0C7F)) {
            return "fonts/NotoSansTelugu[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0D00, 0x0D7F)) {
            return "fonts/NotoSansMalayalam[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0B80, 0x0BFF)) {
            return "fonts/NotoSansTamil[wdth,wght].ttf";
        }
        return getSelectedLanguageFontAsset();
    }

    private boolean containsUnicodeRange(String text, int start, int end) {
        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            if (codePoint >= start && codePoint <= end) {
                return true;
            }
            index += Character.charCount(codePoint);
        }
        return false;
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

    private void refreshCurrentScreenTheme() {
        applySystemBarColors();
        if (screenRenderer != null) screenRenderer.run();
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

    private int authTextColor() {
        return darkMode ? Color.WHITE : LIGHT_TEXT;
    }

    private int backgroundColor() {
        return darkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }

    private int surfaceColor() {
        return darkMode ? DARK_SURFACE : LIGHT_SURFACE;
    }

    private int primaryTextColor() {
        return darkMode ? DARK_TEXT_PRIMARY : LIGHT_TEXT;
    }

    private int secondaryTextColor() {
        return darkMode ? DARK_TEXT_MUTED : LIGHT_TEXT_MUTED;
    }

    private int borderColor() {
        return darkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    private int fieldBorderColor() {
        return darkMode ? DARK_FIELD_BORDER : LIGHT_FIELD_BORDER;
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
            font.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            font.setIncludeFontPadding(false);
            font.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            font.setPadding(dp(6), 0, dp(6), 0);
            font.setContentDescription("Adjust text size");
            font.setOnClickListener(view -> showFontScaleDialog());
            controls.addView(font, new LinearLayout.LayoutParams(dp(42), dp(42)));

            Space leftSpacer = new Space(this);
            controls.addView(leftSpacer, new LinearLayout.LayoutParams(0, 1, 1));

            ImageView logo = new ImageView(this);
            logo.setImageResource(R.drawable.fendly_logo);
            logo.setContentDescription("Fendly logo");
            logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            logo.setAdjustViewBounds(true);
            logo.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(40), dp(40));
            logoParams.gravity = Gravity.CENTER_VERTICAL;
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
                if (screenRenderer != null) screenRenderer.run();
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
            if (screenRenderer != null) screenRenderer.run();
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

    private Drawable createRadioButtonDrawable(boolean checked, int primaryColor, int strokeColor) {
        GradientDrawable outer = new GradientDrawable();
        outer.setShape(GradientDrawable.OVAL);
        outer.setSize(dp(20), dp(20));
        if (checked) {
            outer.setColor(Color.TRANSPARENT);
            outer.setStroke(dp(2), primaryColor);
        } else {
            outer.setColor(Color.TRANSPARENT);
            outer.setStroke(dp(2), strokeColor);
        }

        if (!checked) return outer;

        GradientDrawable inner = new GradientDrawable();
        inner.setShape(GradientDrawable.OVAL);
        inner.setColor(primaryColor);
        inner.setSize(dp(10), dp(10));

        LayerDrawable layer = new LayerDrawable(new Drawable[]{outer, inner});
        int margin = dp(5);
        layer.setLayerInset(1, margin, margin, margin, margin);
        return layer;
    }

    private void applyLanguageSelection(int languageIndex, String languageCode) {
        selectedLanguage = languageIndex;
        getSharedPreferences("fendly_language", MODE_PRIVATE)
                .edit()
                .putInt("selected_language_index", languageIndex)
                .commit();
        Configuration configuration = new Configuration(getResources().getConfiguration());
        Locale locale = Locale.forLanguageTag(languageCode);
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        applySystemBarColors();
        if (screenRenderer != null) screenRenderer.run();
    }

    private void showLanguagePicker() {
        String[][] languages = {
            {"English", "en"},
            {"हिन्दी", "hi"},
            {"मराठी", "mr"},
            {"ગુજરાતી", "gu"},
            {"বাংলা", "bn"},
            {"தமிழ்", "ta"},
            {"తెలుగు", "te"},
            {"ಕನ್ನಡ", "kn"},
            {"മലയാളം", "ml"}
        };
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(14));

        TextView title = text(localized("language"), 20, primaryTextColor(), Typeface.BOLD);
        title.setIncludeFontPadding(false);
        title.setPadding(dp(4), 0, dp(4), dp(10));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .create();

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(false);
        grid.setPadding(0, dp(4), 0, 0);

        for (int index = 0; index < languages.length; index++) {
            final int languageIndex = index;
            final String languageCode = languages[index][1];
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(8), dp(12), dp(8));
            row.setBackground(index == selectedLanguage
                    ? roundWithStroke(Color.argb(darkMode ? 35 : 22, 232, 178, 74), 12, GOLD)
                    : round(Color.TRANSPARENT, 12));
            row.setOnClickListener(view -> {
                dialog.dismiss();
                applyLanguageSelection(languageIndex, languageCode);
            });

            ImageView indicator = new ImageView(this);
            indicator.setImageDrawable(createRadioButtonDrawable(
                    index == selectedLanguage,
                    accentColor(),
                    darkMode ? Color.rgb(160, 155, 145) : Color.rgb(180, 175, 165)
            ));
            LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(dp(20), dp(20));
            indicatorParams.gravity = Gravity.CENTER_VERTICAL;
            row.addView(indicator, indicatorParams);

            TextView label = text(languages[index][0], 16, primaryTextColor(), index == selectedLanguage ? Typeface.BOLD : Typeface.NORMAL);
            label.setIncludeFontPadding(false);
            label.setGravity(Gravity.CENTER_VERTICAL);
            label.setPadding(dp(10), 0, 0, 0);
            label.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            row.addView(label, new LinearLayout.LayoutParams(-1, -2));

            GridLayout.LayoutParams rowParams = new GridLayout.LayoutParams(
                    GridLayout.spec(index / 2, 1f),
                    GridLayout.spec(index % 2, 1f));
            rowParams.width = 0;
            rowParams.height = dp(48);
            rowParams.setMargins(dp(4), dp(4), dp(4), dp(4));
            grid.addView(row, rowParams);
        }

        content.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(Math.min(dp(320), getResources().getDisplayMetrics().widthPixels - dp(40)), -2);
            dialog.getWindow().setDimAmount(0.42f);
        }
        content.setBackground(roundWithStroke(surfaceColor(), 20, borderColor()));
    }

    private void showFontScaleDialog() {
        float current = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(16), dp(20), dp(12));
        content.setBackground(roundWithStroke(surfaceColor(), 20, borderColor()));
        TextView value = text(localizedTextSize(Math.round(current * 100)), 14, primaryTextColor(), Typeface.NORMAL);
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
                value.setText(localizedTextSize(100 + progress));
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

    private String localizedTextSize(int percent) {
        return translate("Text size") + ": " + localizeDigits(String.valueOf(percent)) + "%";
    }

    private String localized(String key) {
        String[][] values = {
                {"Lost & Found across India", "Help is right here—get started.", "Create my profile", "Login with PIN", "Language"},
                {"भारत में खोया और पाया", "मदद यहीं है—शुरू करें।", "मेरी प्रोफ़ाइल बनाएं", "पिन से लॉगिन", "भाषा"},
                {"संपूर्ण भारतातील हरवलेले आणि सापडलेले", "मदत इथेच आहे—सुरुवात करा.", "माझे प्रोफाइल तयार करा", "पिनने लॉगिन करा", "भाषा"},
                {"સમગ્ર ભારતમાં ખોવાયેલ અને મળેલ", "મદદ અહીં જ છે—શરૂ કરો.", "મારી પ્રોફાઇલ બનાવો", "PIN વડે લોગિન", "ભાષા"},
                {"ভারত জুড়ে হারানো এবং পাওয়া", "সাহায্য এখানেই—শুরু করুন।", "আমার প্রোফাইল তৈরি করুন", "পিন দিয়ে লগইন", "ভাষা"},
                {"இந்தியா முழுவதும் தொலைந்தது மற்றும் கிடைத்தது", "உதவி இங்கே உள்ளது—தொடங்குங்கள்.", "என் சுயவிவரத்தை உருவாக்கவும்", "PIN மூலம் உள்நுழைவு", "மொழி"},
                {"భారతదేశం అంతటా పోయినవి మరియు దొరికినవి", "సహాయం ఇక్కడే ఉంది—ప్రారంభించండి.", "నా ప్రొఫైల్ సృష్టించండి", "పిన్‌తో లాగిన్", "భాష"},
                {"ಭಾರತದಾದ್ಯಂತ ಕಳೆದುಹೋದ ಮತ್ತು ಸಿಕ್ಕ ವಸ್ತುಗಳು", "ಸಹಾಯ ಇಲ್ಲಿದೆ—ಪ್ರಾರಂಭಿಸಿ.", "ನನ್ನ ಪ್ರೊಫೈಲ್ ರಚಿಸಿ", "ಪಿನ್ ಮೂಲಕ ಲಾಗಿನ್", "ಭಾಷೆ"},
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
                {"વપરાશકર્તા નામ", "4-અંકનો PIN", "લોગિન", "નવા છો?  ખાતું બનાવો"},
                {"ব্যবহারকারীর নাম", "৪-অঙ্কের পিন", "লগ ইন", "নতুন এখানে?  অ্যাকাউন্ট তৈরি করুন"},
                {"பயனர்பெயர்", "4-இலக்க PIN", "உள்நுழைவு", "புதியவரா?  கணக்கை உருவாக்கவும்"},
                {"వినియోగదారు పేరు", "4-అంకెల పిన్", "లాగిన్", "కొత్తగా ఉన్నారా?  ఖాతా సృష్టించండి"},
                {"ಬಳಕೆದಾರ ಹೆಸರು", "4-ಅಂಕಿಯ ಪಿನ್", "ಲಾಗ್ ಇನ್", "ಹೊಸಬರೇ?  ಖಾತೆ ರಚಿಸಿ"},
                {"ഉപയോക്തൃനാമം", "4-അക്ക പിൻ", "ലോഗിൻ", "പുതിയ ആളാണോ?  അക്കൗണ്ട് സൃഷ്ടിക്കുക"}
        };
        int language = Math.max(0, Math.min(selectedLanguage, values.length - 1));
        if ("username".equals(key)) return localizeDigits(values[language][0]);
        if ("pin".equals(key)) return localizeDigits(values[language][1]);
        if ("login".equals(key)) return localizeDigits(values[language][2]);
        return localizeDigits(values[language][3]);
    }

    private String translate(String value) {
        String targetedTranslation = translateUi(value);
        if (targetedTranslation != null) return localizeDigits(targetedTranslation);
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
                english, // 0: en
                {"अपनी प्रोफ़ाइल पूरी करें", "आपके बारे में थोड़ा", "इससे पड़ोसियों को पता चलेगा कि वे किसकी मदद कर रहे हैं।", "सहेजें और जारी रखें", "पिन से लॉगिन", "वापसी पर स्वागत है।", "अपने Fendly उपयोगकर्ता नाम और पिन का उपयोग करें।", "लॉगिन", "होम", "जो महत्वपूर्ण है उसे खोजें।", "पास में कुछ खोया? कुछ मिला? यहां से शुरू करें।", "खोया", "मिला", "मेरी रिपोर्ट", "मेरी प्रोफ़ाइल", "मिली वस्तु पोस्ट करें", "खोई वस्तु रिपोर्ट करें", "इसे घर पहुंचाने में मदद करें।", "आइए इसे खोजें।", "स्पष्ट विवरण जोड़ें ताकि सही व्यक्ति पहचान सके।", "वस्तु का नाम", "विवरण और पहचान की जानकारी", "स्थान या पहचान चिन्ह", "दिनांक और समय", "वस्तु की तस्वीर अपलोड करें", "तस्वीर चुनी गई", "कैमरे से तस्वीर लें", "वर्तमान स्थान उपयोग करें", "मिली वस्तु प्रकाशित करें", "खोई वस्तु प्रकाशित करें", "Fendly Plus", "खोई वस्तु की रिपोर्ट अनलॉक करें।", "मिली वस्तु की रिपोर्ट हमेशा निःशुल्क है। खोई वस्तु की रिपोर्ट Rs 99 प्रति वर्ष है।", "भुगतान करें और खोई रिपोर्ट भेजें", "रिपोर्ट पर वापस जाएं", "मेरी रिपोर्ट", "जिन वस्तुओं को मिलाने में मदद कर रहे हैं उनका रिकॉर्ड रखें।", "होम पर वापस जाएं", "डमी उपयोगकर्ता", "आपके खाते का विवरण और प्राथमिकताएं।", "एडमिन डैशबोर्ड", "निजी मॉडरेशन कार्यक्षेत्र", "केवल अंग्रेज़ी · गोपनीय उपयोगकर्ता विवरण", "लाइव एडमिन डेटा लोड हो रहा है...", "AI मिलान देखें", "पुष्टि करें और मालिक को सूचित करें", "एडमिन से बाहर निकलें"}, // 1: hi
                {"पूर्ण प्रोफाइल", "तुमच्याबद्दल थोडे", "यामुळे शेजाऱ्यांना ते कोणाला मदत करत आहेत हे समजेल.", "जतन करा आणि पुढे जा", "पिनने लॉगिन", "पुन्हा स्वागत आहे.", "तुमचे Fendly वापरकर्तानाव आणि पिन वापरा.", "लॉगिन", "मुख्यपृष्ठ", "महत्त्वाचे शोधा.", "जवळ काही हरवले? काही सापडले? इथून सुरुवात करा.", "हरवले", "सापडले", "माझे अहवाल", "माझे प्रोफाइल", "सापडलेली वस्तू पोस्ट करा", "हरवलेली वस्तू नोंदवा", "ते घरी पोहोचवण्यास मदत करा.", "चला ते शोधूया.", "योग्य व्यक्ती ओळखू शकेल असे स्पष्ट तपशील जोडा.", "वस्तूचे नाव", "वर्णन आणि ओळख तपशील", "ठिकाण किंवा खूण", "दिनांक आणि वेळ", "वस्तूचा फोटो अपलोड करा", "फोटो निवडला", "कॅमेऱ्याने फोटो घ्या", "सध्याचे स्थान वापरा", "सापडलेली वस्तू प्रकाशित करा", "हरवलेली वस्तू प्रकाशित करा", "Fendly Plus", "हरवलेल्या वस्तूंचे अहवाल सुरू करा.", "सापडलेल्या वस्तूंचे अहवाल कायम विनामूल्य आहेत. हरवलेल्या वस्तूंचे अहवाल वर्षाला Rs 99 आहेत.", "भरणा करून हरवलेला अहवाल पाठवा", "अहवालाकडे परत जा", "माझे अहवाल", "तुम्ही पुन्हा जोडण्यास मदत करत असलेल्या वस्तूंचा मागोवा ठेवा.", "मुख्यपृष्ठावर परत जा", "डमी वापरकर्ता", "तुमच्या खात्याचे तपशील आणि प्राधान्ये.", "अॅडमिन डॅशबोर्ड", "खासगी मॉडरेशन कार्यक्षेत्र", "फक्त इंग्रजी · गोपनीय वापरकर्ता तपशील", "लाइव्ह अॅडमिन डेटा लोड होत आहे...", "AI जुळणी पाहा", "पुष्टी करून मालकाला कळवा", "अॅडमिनमधून बाहेर पडा"}, // 2: mr
                {"તમારી પ્રોફાઇલ પૂર્ણ કરો", "તમારા વિશે થોડું", "આ પડોશીઓને જાણવામાં મદદ કરે છે કે તેઓ કોને મદદ કરી રહ્યા છે.", "સાચવો અને આગળ વધો", "PIN વડે લોગિન", "પાછા સ્વાગત છે.", "તમારી Fendly પ્રોફાઇલમાંથી યુઝરનામ અને PIN નો ઉપયોગ કરો.", "લોગિન", "હોમ", "જે મહત્ત્વનું છે તે શોધો.", "આસપાસ કંઈ ખોવાયું? કંઈ મળ્યું? અહીંથી શરૂ કરો.", "ખોવાયેલ", "મળેલ", "મારા અહેવાલો", "મારી પ્રોફાઇલ", "મળેલ વસ્તુ પોસ્ટ કરો", "ખોવાયેલ વસ્તુની જાણ કરો", "તેને ઘરે પહોંચાડવામાં મદદ કરો.", "ચાલો તે શોધીએ.", "સ્પષ્ટ વિગતો ઉમેરો જેથી સાચી વ્યક્તિ તેને ઓળખી શકે.", "વસ્તુનું નામ", "વર્ણન અને ઓળખની વિગતો", "સ્થળ અથવા ઓળખચિહ્ન", "તારીખ અને સમય", "વસ્તુની છબી અપલોડ કરો", "છબી પસંદ કરી", "કેમેરાથી ફોટો લો", "વર્તમાન સ્થાનનો ઉપયોગ કરો", "મળેલ વસ્તુ પ્રકાશિત કરો", "ખોવાયેલ વસ્તુ પ્રકાશિત કરો", "Fendly Plus", "ખોવાયેલ વસ્તુના રિપોર્ટ અનલોક કરો.", "મળેલ વસ્તુના રિપોર્ટ હંમેશા મફત છે. ખોવાયેલ વસ્તુના રિપોર્ટ વર્ષે Rs 99 છે.", "ચૂકવણી કરો અને ખોવાયેલ રિપોર્ટ સબમિટ કરો", "રિપોર્ટ પર પાછા જાઓ", "તમારા અહેવાલો", "તમે જેને ફરી મેળવવામાં મદદ કરી રહ્યા છો તે વસ્તુઓનો ટ્રેક રાખો.", "હોમ પર પાછા જાઓ", "ડમી યુઝર", "તમારા ખાતાની વિગતો અને પસંદગીઓ.", "એડમિન ડેશબોર્ડ", "ખાનગી મોડરેશન વર્કસ્પેસ", "માત્ર અંગ્રેજી · ગોપનીય વપરાશકર્તા વિગતો", "લાઈવ એડમિન ડેટા લોડ થઈ રહ્યો છે...", "AI મેચ સમીક્ષા કરો", "પુષ્ટિ કરો અને માલિકને જાણ કરો", "એડમિનમાંથી બહાર નીકળો"}, // 3: gu
                {"আপনার প্রোফাইল সম্পূর্ণ করুন", "আপনার সম্পর্কে কিছু", "এটি প্রতিবেশীদের জানতে সাহায্য করে তারা কার সাহায্য করছে।", "সংরক্ষণ করুন এবং চালিয়ে যান", "পিন দিয়ে লগইন", "ফিরে আসার জন্য স্বাগতম।", "আপনার Fendly ব্যবহারকারীর নাম এবং পিন ব্যবহার করুন।", "লগইন", "হোম", "গুরুত্বপূর্ণ জিনিস খুঁজুন।", "কাছাকাছি কিছু হারিয়েছে? কিছু পেয়েছেন? এখান থেকে শুরু করুন।", "হারিয়ে গেছে", "পাওয়া গেছে", "আমার রিপোর্ট", "আমার প্রোফাইল", "পাওয়া আইটেম পোস্ট করুন", "হারানো আইটেম রিপোর্ট করুন", "এটিকে বাড়িতে ফিরিয়ে দিতে সাহায্য করুন।", "চলো এটি খুঁজে বের করি।", "সঠিক ব্যক্তি শনাক্ত করার জন্য স্পষ্ট বিবরণ যোগ করুন।", "আইটেমের নাম", "বর্ণনা ও শনাক্তকরণ তথ্য", "অবস্থান বা চিহ্ন", "তারিখ ও সময়", "আইটেমের ছবি আপলোড করুন", "চিত্র নির্বাচন করা হয়েছে", "ক্যামেরা দিয়ে ছবি নিন", "বর্তমান অবস্থান ব্যবহার করুন", "পাওয়া আইটেম প্রকাশ করুন", "হারানো আইটেম প্রকাশ করুন", "Fendly Plus", "হারানো আইটেম রিপোর্ট আনলক করুন।", "পাওয়া আইটেম রিপোর্ট সবসময় বিনামূল্যে। হারানো আইটেম রিপোর্ট বছরে Rs 99।", "পেমেন্ট করুন এবং হারানো রিপোর্ট জমা দিন", "রিপোর্টে ফিরে যান", "আমার রিপোর্ট", "আপনি কী কী আইটেম আবার একত্রিত করতে সাহায্য করছেন তার রেকর্ড রাখুন।", "হোমে ফিরে যান", "ডামি ব্যবহারকারী", "আপনার অ্যাকাউন্টের বিবরণ ও পছন্দসমূহ।", "অ্যাডমিন ড্যাশবোর্ড", "ব্যক্তিগত মডারেশন ওয়ার্কস্পেস", "শুধু ইংরেজি · গোপন ব্যবহারকারী বিবরণ", "লাইভ অ্যাডমিন ডেটা লোড হচ্ছে...", "AI ম্যাচ দেখুন", "নিশ্চিত করুন এবং মালিককে অবহিত করুন", "অ্যাডমিন থেকে বের হন"}, // 4: bn
                {"உங்கள் சுயவிவரத்தை முழுமையாக்குங்கள்", "உங்களைப் பற்றி சிறிது", "இது யாருக்கு உதவுகிறார்கள் என்பதை அண்டை வீட்டாருக்குத் தெரியப்படுத்த உதவுகிறது.", "சேமித்து தொடரவும்", "PIN மூலம் உள்நுழைவு", "மீண்டும் வருக.", "உங்கள் Fendly சுயவிவரத்தின் பயனர்பெயர் மற்றும் PIN ஐப் பயன்படுத்தவும்.", "உள்நுழை", "முகப்பு", "முக்கியமானவற்றைக் கண்டறியவும்.", "அருகில் தொலைந்ததா? ஏதேனும் கிடைத்ததா? இங்கிருந்து தொடங்குங்கள்.", "தொலைந்தது", "கிடைத்தது", "என் அறிக்கைகள்", "என் சுயவிவரம்", "கிடைத்த பொருளைப் பதிவிடவும்", "தொலைந்த பொருளை அறிக்கை செய்யவும்", "அதை வீட்டிற்குச் சேர்க்க உதவுங்கள்.", "அதைக் கண்டுபிடிப்போம்.", "சரியான நபர் அடையாளம் காண தெளிவான விவரங்களைச் சேர்க்கவும்.", "பொருளின் பெயர்", "விளக்கம் மற்றும் அடையாள விவரங்கள்", "இடம் அல்லது அடையாளம்", "தேதி மற்றும் நேரம்", "பொருளின் படத்தைப் பதிவேற்றவும்", "படம் தேர்ந்தெடுக்கப்பட்டது", "கேமரா மூலம் படம் எடுக்கவும்", "தற்போதைய இடத்தைப் பயன்படுத்தவும்", "கிடைத்த பொருளை வெளியிடுங்கள்", "தொலைந்த பொருளை வெளியிடுங்கள்", "Fendly Plus", "தொலைந்த பொருள் அறிக்கைகளைத் திறக்கவும்.", "கிடைத்த பொருள் அறிக்கைகள் எப்போதும் இலவசம். தொலைந்த பொருள் அறிக்கைகள் ஆண்டிற்கு Rs 99.", "பணம் செலுத்தி அறிக்கையைச் சமர்ப்பிக்கவும்", "அறிக்கைக்குத் திரும்புக", "உங்கள் அறிக்கைகள்", "நீங்கள் மீண்டும் சேர்க்க உதவும் பொருட்களைக் கண்காணிக்கவும்.", "முகப்பிற்குத் திரும்புக", "மாதிரி பயனர்", "உங்கள் கணக்கு விவரங்கள் மற்றும் முன்னுரிமைகள்.", "நிர்வாகி குழு", "தனியார் மிதமான பணிப்பகுதி", "ஆங்கிலம் மட்டும் · இரகசிய பயனர் விவரங்கள்", "நிர்வாகி தரவு ஏற்றப்படுகிறது...", "AI பொருத்தத்தை மதிப்பாய்வு செய்க", "உறுதிசெய்து உரிமையாளருக்கு அறிவிக்கவும்", "நிர்வாகியிலிருந்து வெளியேறு"}, // 5: ta
                {"మీ ప్రొఫైల్‌ను పూర్తి చేయండి", "మీ గురించి కొద్దిపాటి సమాచారం", "ఇది పొరుగు వారికి ఎవరికి సహాయం చేస్తున్నారో తెలుసుకోవడంలో సహాయపడుతుంది.", "సేవ్ చేసి కొనసాగించండి", "పిన్‌తో లాగిన్", "మళ్ళీ స్వాగతం", "మీ Fendly వినియోగదారు పేరు మరియు పిన్‌ను ఉపయోగించండి.", "లాగిన్", "హోమ్", "ముఖ్యమైన వాటిని కనుగొనండి.", "ఇక్కడకు దగ్గరలో ఏదైనా పోయిందా? ఏదైనా దొరికిందా? ఇక్కడ ప్రారంభించండి.", "కోల్పోయినవి", "కనుగొన్నది", "నా రిపోర్ట్లు", "నా ప్రొఫైల్", "కనుగొన్న అంశాన్ని పోస్ట్ చేయండి", "కోల్పోయిన అంశాన్ని రిపోర్ట్ చేయండి", "దానిని ఇంటికి చేర్చడానికి సహాయం చేయండి.", "వెతుకుదాం.", "సరైన వ్యక్తి గుర్తించగలిగే స్పష్టమైన వివరాలను జోడించండి.", "అంశం పేరు", "వివరణ మరియు గుర్తింపు వివరాలు", "స్థలం లేదా పరిశీలన", "తేదీ మరియు సమయం", "అంశపు ఫోటో అప్లోడ్ చేయండి", "ఫోటో ఎంపికైంది", "కెమెరా నుండి ఫోటో తీయండి", "ప్రస్తుత స్థానం ఉపయోగించండి", "కనుగొన్న అంశాన్ని ప్రచురించండి", "కోల్పోయిన అంశాన్ని ప్రచురించండి", "Fendly Plus", "కోల్పోయిన వస్తువుల రిపోర్ట్లను అన్‌లాక్ చేయండి.", "కనుగొన్న వస్తువుల రిపోర్ట్లు ఎల్లప్పుడూ ఉచితం. కోల్పోయిన వస్తువుల రిపోర్ట్లు సంవత్సరానికి Rs 99.", "చెల్లించి కోల్పోయిన రిపోర్టును సమర్పించండి", "రిపోర్టుకు తిరిగి వెళ్లండి", "నా రిపోర్ట్లు", "మీరు పునరుద్ధరించడానికి సహాయం చేస్తున్న వస్తువుల రికార్డ్‌ను పర్యవేక్షించండి.", "హోమ్కి తిరిగి వెళ్లండి", "డమ్మీ యూజర్", "మీ అకౌంట్ వివరాలు మరియు ప్రాధాన్యతలు.", "అడ్మిన్ డాష్‌బోర్డ్", "ప్రైవేట్ మోడరేషన్ వర్క్‌స్పేస్", "ఇంగ్లీష్ మాత్రమే · గోప్య వినియోగదారు వివరాలు", "లైవ్ అడ్మిన్ డేటాను లోడ్ చేస్తున్నారు...", "AI మ్యాచ్ చూసుకోండి", "నిర్ధారించండి మరియు యజమానికి తెలియజేయండి", "అడ్మిన్ నుండి నిష్క్రమించండి"}, // 6: te
                {"ನಿಮ್ಮ ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಳಿಸಿ", "ನಿಮ್ಮ ಬಗ್ಗೆ ಸ್ವಲ್ಪ ಮಾಹಿತಿ", "ಇದು ನೆರವಿನವರನ್ನು ಯಾರು ಸಹಾಯ ಮಾಡುತ್ತಿದ್ದಾರೆಂದು ತಿಳಿಸಲು ಸಹಾಯ ಮಾಡುತ್ತದೆ.", "ಸೇವ್ ಮಾಡಿ ಮತ್ತು ಮುಂದುವರಿಸಿ", "ಪಿನ್ ಮೂಲಕ ಲಾಗಿನ್", "ಮರಳಿ ಸ್ವಾಗತ", "ನಿಮ್ಮ Fendly ಬಳಕೆದಾರಹೆಸರು ಮತ್ತು ಪಿನ್ ಬಳಸಿ.", "ಲಾಗಿನ್", "ಹೋಮ್", "ಪ್ರಮುಖವಾದ್ದನ್ನು ಹುಡುಕಿ.", "ಹತ್ತಿರದಲ್ಲಿ ಯಾವುದೋ ಕಳೆದುಹೋಗಿದೆಯೇ? ಏನಾದರೂ ಸಿಕ್ಕಿದೆಯೇ? ಇಲ್ಲಿಂದ ಪ್ರಾರಂಭಿಸಿ.", "ಕಳೆದುಹೋಗಿದೆ", "ಸಿಕ್ಕಿದೆ", "ನನ್ನ ವರದಿಗಳು", "ನನ್ನ ಪ್ರೊಫೈಲ್", "ಕಂಡ ವಸ್ತು ಪೋಸ್ಟ್ ಮಾಡಿ", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿ ಮಾಡಿ", "ಅದನ್ನು ಮನೆಗೆ ಸೇರಿಸಲು ಸಹಾಯ ಮಾಡಿ.", "ಮುತ್ತಲಿನವರೊಂದಿಗೆ ಹುಡುಕೋಣ.", "ಸರಿಯಾದ ವ್ಯಕ್ತಿ ಗುರುತಿಸಿಕೊಳ್ಳಲು ಸ್ಪಷ್ಟ ವಿವರಗಳನ್ನು ಸೇರಿಸಿ.", "ವಸ್ತುವಿನ ಹೆಸರು", "ವಿವರಣೆ ಮತ್ತು ಗುರುತಿಸುವ ವಿವರಗಳು", "ಸ್ಥಳ ಅಥವಾ ಗುರುತು", "ದಿನಾಂಕ ಮತ್ತು ಸಮಯ", "ವಸ್ತುವಿನ ಚಿತ್ರ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ", "ಚಿತ್ರ ಆಯ್ಕೆಮಾಡಲಾಗಿದೆ", "ಕ್ಯಾಮರಾದಿಂದ ಫೋಟೋ ತೆಗೆದುಕೊಳ್ಳಿ", "ಪ್ರಸ್ತುತ ಸ್ಥಳವನ್ನು ಬಳಸಿ", "ಕಂಡ ವಸ್ತು ಪ್ರಕಟಿಸಿ", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ಪ್ರಕಟಿಸಿ", "Fendly Plus", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿಗಳನ್ನು desbloಕ್ ಮಾಡಿ.", "ಕಂಡ ವಸ್ತು ವರದಿಗಳು ಶಾಶ್ವತವಾಗಿ ಉಚಿತವಾಗಿರುತ್ತವೆ. ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿಗಳು ವರ್ಷಕ್ಕೆ Rs 99.", "ಚెలಾಯಿಸಿ ಮತ್ತು ಕಳೆದುಹೋಗಿದ ವರದಿಯನ್ನು ಸಲ್ಲಿಸಿ", "ವರದಿಗೆ ಹಿಂತಿರುಗಿ", "ನನ್ನ ವರದಿಗಳು", "ನೀವು ಒಟ್ಟುಗೂಡಿಸಲು ಸಹಾಯ ಮಾಡುವ ವಸ್ತುಗಳ ರೆಕಾರ್ಡ್ ಅನ್ನು ನಿರ್ವಹಿಸಿ.", "ಮನೆಯತ್ತ ಹಿಂತಿರುಗಿ", "ಡಮ್ಮಿ ಬಳಕೆದಾರ", "ನಿಮ್ಮ ಖಾತೆ ವಿವರಗಳು ಮತ್ತು ಆದ್ಯತೆಗಳು.", "ಅಡ್ಮಿನ್ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್", "ಖಾಸಗಿ मॉಡರೇಶನ್ ಕಾರ್ಯಕ್ಷೇತ್ರ", "ಇಂಗ್ಲಿಷ್ ಮಾತ್ರ · ರಹಸ್ಯ ಬಳಕೆದಾರ ವಿವರಗಳು", "ಲೈವ್ ಅಡ್ಮಿನ್ ಡೇಟಾವನ್ನು ಲೋಡ್ ಮಾಡಲಾಗುತ್ತಿದೆ...", "AI ಪಂದ್ಯವನ್ನು ವೀಕ್ಷಿಸಿ", "ನಿಶ್ಚಿತಪಡಿಸಿ ಮತ್ತು ಮಾಲೀಕನಿಗೆ ತಿಳಿಸಿ", "ಅಡ್ಮಿನ್ ನಿಂದ ನಿರ್ಗಮಿಸಿ"}, // 7: kn
                {"നിങ്ങളുടെ പ്രൊഫൈൽ പൂർത്തിയാക്കുക", "നിങ്ങളെക്കുറിച്ച് കുറച്ച്", "ഇത് അയൽവാസികൾക്ക് ആരെ സഹായിക്കുന്നുവെന്ന് അറിയാൻ സഹായിക്കുന്നു.", "സേവ് ചെയ്ത് മുന്നോട്ട് പോകുക", "പിൻ ഉപയോഗിച്ച് ലോഗിൻ", "വീണ്ടും സ്വാഗതം", "നിങ്ങളുടെ Fendly യൂസർനെയം, പിൻ ഉപയോഗിക്കുക.", "ലോഗിൻ", "ഹോം", "പ്രധാനമായ വസ്തുക്കൾ കണ്ടെത്തുക.", "സമീപത്ത് തന്നെ നഷ്ടപ്പെട്ടോ? എന്തെങ്കിലും കണ്ടെത്തിയോ? ഇവിടെ ആരംഭിക്കുക.", "നഷ്ടപ്പെട്ടു", "കണ്ടെത്തി", "എന്റെ റിപ്പോർട്ടുകൾ", "എന്റെ പ്രൊഫൈൽ", "കണ്ടെത്തിയ ഇനം പോസ്റ്റുചെയ്യുക", "നഷ്ടപ്പെട്ട ഇനം റിപ്പോർട്ട് ചെയ്യുക", "അത് വീട്ടിലേക്ക് എത്തിക്കാൻ സഹായിക്കുക.", "കണ്ടുപിടിക്കാം.", "ശരിയായ വ്യക്തിയെ തിരിച്ചറിയാൻ വ്യക്തമായ വിശദാംശങ്ങൾ ചേർക്കുക.", "ഇനത്തിന്റെ പേര്", "വിവരണം, തിരിച്ചറിയൽ വിശദാംശങ്ങൾ", "സ്ഥലം അല്ലെങ്കിൽ അടയാളം", "തീയതിയും സമയവും", "ഇനത്തിന്റെ ചിത്രം അപ്‌ലോഡ് ചെയ്യുക", "ചിത്രം തിരഞ്ഞെടുത്തു", "ക്യാമറയിൽ നിന്ന് ഫോട്ടോ എടുക്കുക", "നിലവിലെ സ്ഥലം ഉപയോഗിക്കുക", "കണ്ടെത്തിയ ഇനം പ്രസിദ്ധീകരിക്കുക", "നഷ്ടപ്പെട്ട ഇനം പ്രസിദ്ധീകരിക്കുക", "Fendly Plus", "നഷ്ടപ്പെട്ട ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ അൺലോക്ക് ചെയ്യുക.", "കണ്ടെത്തിയ ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ എല്ലായ്പ്പോഴും സൗജന്യമാണ്. നഷ്ടപ്പെട്ട ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ പ്രതിവർഷം Rs 99.", "പേയ്‌മെന്റ് ചെയ്യുകയും നഷ്ടപ്പെട്ട റിപ്പോർട്ട് സമർപ്പിക്കുകയും ചെയ്യുക", "റിപ്പോർട്ടിലേക്ക് തിരികെ പോകുക", "എന്റെ റിപ്പോർട്ടുകൾ", "നിങ്ങൾ വീണ്ടും കൂട്ടിച്ചേർക്കാൻ സഹായിക്കുന്ന ഇനങ്ങളുടെ രേഖ സൂക്ഷിക്കുക.", "ഹോമിലേക്ക് മടങ്ങുക", "ഡമ്മി ഉപയോക്താവ്", "നിങ്ങളുടെ അക്കൗണ്ട് വിശദാംശങ്ങളും മുൻഗണനകളും.", "അഡ്മിൻ ഡാഷ്‌ബോർഡ്", "സ്വകാര്യ മോഡറേഷൻ വർക്ക്‌സ്പേസ്", "ഇംഗ്ലീഷ് മാത്രം · രഹസ്യ ഉപയോക്തൃ വിശദാംശങ്ങൾ", "ലൈവ് അഡ്മിൻ ഡാറ്റ ലോഡ് ചെയ്യുകയാണ്...", "AI മാച്ച് കാണുക", "സ്ഥിരീകരിക്കുകയും ഉടമയെ അറിയിക്കുകയും ചെയ്യുക", "അഡ്മിനിൽ നിന്ന് പുറത്തുകടക്കുക"} // 8: ml
        };
        int language = Math.max(0, Math.min(selectedLanguage, translations.length - 1));
        for (int index = 0; index < english.length; index++) {
            if (english[index].equals(value)) return localizeDigits(translations[language][index]);
        }
        return localizeDigits(value);
    }

    private String translateUi(String value) {
        if (value == null) return null;
        if (selectedLanguage == 3) {
            if ("Reset PIN?".equalsIgnoreCase(value)) return "PIN રીસેટ કરવું છે?";
            if ("A temporary 4-digit PIN will be sent after mobile verification.".equalsIgnoreCase(value)) return "મોબાઇલ ચકાસણી પછી અસ્થાયી 4-અંકનો PIN મોકલવામાં આવશે.";
            if ("Enter your username first".equalsIgnoreCase(value)) return "પ્રથમ તમારું વપરાશકર્તા નામ દાખલ કરો";
            if ("No verified mobile number is saved".equalsIgnoreCase(value)) return "કોઈ ચકાસાયેલ મોબાઇલ નંબર સાચવેલ નથી";
            if ("Verify OTP".equalsIgnoreCase(value)) return "OTP ચકાસો";
            if ("Verified".equalsIgnoreCase(value)) return "ચકાસાયેલ";
            if ("Change PIN".equalsIgnoreCase(value)) return "PIN બદલો";
            if ("New PIN".equalsIgnoreCase(value)) return "નવો PIN";
            if ("State".equalsIgnoreCase(value)) return "રાજ્ય";
            if ("City".equalsIgnoreCase(value)) return "શહેર";
            if ("Select state".equalsIgnoreCase(value)) return "રાજ્ય પસંદ કરો";
            if ("Select city".equalsIgnoreCase(value)) return "શહેર પસંદ કરો";
            if ("Description".equalsIgnoreCase(value)) return "વર્ણન";
            if ("Date lost".equalsIgnoreCase(value)) return "ખોવાયાની તારીખ";
            if ("Date found".equalsIgnoreCase(value)) return "મળ્યાની તારીખ";
            if ("Last seen at".equalsIgnoreCase(value)) return "છેલ્લે અહીં જોયું";
            if ("Place found".equalsIgnoreCase(value)) return "મળવાનું સ્થળ";
            if ("Precise location  OFF".equalsIgnoreCase(value)) return "ચોક્કસ સ્થાન બંધ";
            if ("Location ready".equalsIgnoreCase(value)) return "સ્થાન તૈયાર છે";
            if ("Annual subscription".equalsIgnoreCase(value)) return "વાર્ષિક સબ્સ્ક્રિપ્શન";
            if ("Continue to payment".equalsIgnoreCase(value)) return "ચુકવણી ચાલુ રાખો";
            if ("Save changes".equalsIgnoreCase(value)) return "ફેરફારો સાચવો";
            if ("Back home".equalsIgnoreCase(value)) return "હોમ પર પાછા જાઓ";
            if ("My reports".equalsIgnoreCase(value)) return "મારા રિપોર્ટ";
            if ("My profile".equalsIgnoreCase(value)) return "મારી પ્રોફાઇલ";
            if ("LOST".equalsIgnoreCase(value)) return "ખોવાયેલ";
            if ("FOUND".equalsIgnoreCase(value)) return "મળેલ";
            if ("Report an item".equalsIgnoreCase(value)) return "વસ્તુની રિપોર્ટ કરો";
            if ("Item name".equalsIgnoreCase(value)) return "વસ્તુનું નામ";
            if ("IMEI Number".equalsIgnoreCase(value)) return "IMEI નંબર";
            if ("Description and identifying details".equalsIgnoreCase(value)) return "વર્ણન અને ઓળખ વિગતો";
            if ("Location or landmark".equalsIgnoreCase(value)) return "સ્થાન અથવા સીમાચિહ્ન";
            if ("Date and time".equalsIgnoreCase(value)) return "તારીખ અને સમય";
            if ("Use current location".equalsIgnoreCase(value)) return "વર્તમાન સ્થાન વાપરો";
            if ("Submit report — free".equalsIgnoreCase(value)) return "રિપોર્ટ સબમિટ કરો — મફત";
            if ("Updating...".equalsIgnoreCase(value)) return "અપડેટ થઈ રહ્યું છે...";
            if ("Saving...".equalsIgnoreCase(value)) return "સાચવવામાં આવી રહ્યું છે...";
            if ("Text size".equalsIgnoreCase(value)) return "ટેક્સ્ટ કદ";
            if ("Cancel".equalsIgnoreCase(value)) return "રદ કરો";
            if ("Apply".equalsIgnoreCase(value)) return "લાગુ કરો";
            if ("Confirm".equalsIgnoreCase(value)) return "કન્ફર્મ કરો";
            if ("Fingerprint login".equalsIgnoreCase(value)) return "ફિંગરપ્રિન્ટ લોગિન";
            if ("Confirm your identity to continue".equalsIgnoreCase(value)) return "ચાલુ રાખવા માટે તમારી ઓળખની પુષ્ટિ કરો";
            if ("Use another account".equalsIgnoreCase(value)) return "બીજું એકાઉન્ટ વાપરો";
            if ("Sign in to Fendly".equalsIgnoreCase(value)) return "Fendly માં સાઇન ઇન કરો";
            if ("Confirm your identity".equalsIgnoreCase(value)) return "તમારી ઓળખની પુષ્ટિ કરો";
            if ("Sending...".equalsIgnoreCase(value)) return "મોકલાઈ રહ્યું છે...";
            if ("Verifying...".equalsIgnoreCase(value)) return "ચકાસણી થઈ રહી છે...";
            if ("Enter OTP".equalsIgnoreCase(value)) return "OTP દાખલ કરો";
            if ("Updated".equalsIgnoreCase(value)) return "અપડેટ કર્યું";
            if ("Signed in as".equalsIgnoreCase(value)) return "તરીકે સાઇન ઇન કર્યું: ";
            if ("Renew plan".equalsIgnoreCase(value)) return "પ્લાન રિન્યૂ કરો";
            if ("Active subscription until".equalsIgnoreCase(value)) return "સુધી સક્રિય સબ્સ્ક્રિપ્શન";
            if ("Username (login)".equalsIgnoreCase(value)) return "વપરાશકર્તા નામ (લૉગિન)";
            if ("Signing in...".equalsIgnoreCase(value)) return "સાઇમ ઇન થઈ રહ્યું છે...";
            if ("Creating account...".equalsIgnoreCase(value)) return "એકાઉન્ટ બનાવવામાં આવી રહ્યું છે...";
        }
        if ("Reset PIN?".equals(value)) {
            String[] resetPin = {"Reset PIN?", "पिन रीसेट करें?", "पिन रीसेट करा?", "پن ری سیٹ کریں؟", "ಪಿನ್ ಮರುಹೊಂದಿಸಿ?", "పిన్‌ను రీసెట్ చేయండి?", "পিন রিসেট করুন?", "പിൻ റീസെറ്റ് ചെയ്യണോ?"};
            return resetPin[Math.max(0, Math.min(selectedLanguage, resetPin.length - 1))];
        }
        if ("A temporary 4-digit PIN will be sent after mobile verification.".equals(value)) {
            String[] text = {"A temporary 4-digit PIN will be sent after mobile verification.", "मोबाइल सत्यापन के बाद अस्थायी 4-अंकीय पिन भेजा जाएगा।", "मोबाइल पडताळणी नंतर तात्पुरता 4-अंकी पिन पाठवला जाईल.", "موبائل توثیق کے بعد عارضی 4 ہندسوں کا پن بھیجا جائے گا۔", "ಮೊಬೈಲ್ ಪರಿಶೀಲನೆ ಬಳಿಕ ತಾತ್ಕಾಲಿಕ 4-ಅಂಕಿಯ ಪಿನ್ ಕಳುಹಿಸಲಾಗುತ್ತದೆ.", "మొబైల్ ధృవీకరణ తర్వాత తాత్కాలిక 4-అంకెల పిన్ పంపబడుతుంది.", "মোবাইল যাচাইয়ের পরে অস্থায়ী 4-অঙ্কের পিন পাঠানো হবে।", "മൊബൈൽ വെരിഫിക്കേഷന jälkeen താൽക്കാലിക 4-അക്ക പിൻ അയയ്ക്കും."};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        if ("Enter your username first".equals(value)) {
            String[] text = {"Enter your username first", "पहले अपना उपयोगकर्ता नाम दर्ज करें", "प्रथम तुमचे वापरकर्तानाव प्रविष्ट करा", "سب سے پہلے اپنا صارف نام درج کریں", "ಮೊದಲು ನಿಮ್ಮ ಬಳಕೆದಾರಹೆಸರು ನಮೂದಿಸಿ", "మొదట మీ వినియోగదారు పేరు నమోదు చేయండి", "প্রথমে আপনার ব্যবহারকারীর নাম লিখুন", "മൊതまず നിങ്ങളുടെ ഉപയോക്തൃനാമം നൽകുക"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        if ("No verified mobile number is saved".equals(value)) {
            String[] text = {"No verified mobile number is saved", "कोई सत्यापित मोबाइल नंबर सेव नहीं है", "कोणताही पडताळलेला मोबाइल नंबर सेव्ह केलेला नाही", "کوئی تصدیق شدہ موبائل نمبر محفوظ نہیں ہے", "ಪರिशೀಲಿಸಿದ ಮೊಬೈಲ್ ಸಂಖ್ಯೆಯು ಸೇವ್ ಆಗಿಲ್ಲ", "ధృవీకరించబడిన మొబైల్ నంబర్ సేవ్ చేయబడలేదు", "কোনো যাচাইকৃত মোবাইল নম্বর সংরক্ষিত নেই", "വെരിഫൈഡ് മൊബൈൽ നമ്പർ 저장ിച്ചിട്ടില്ല"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        String[][] entries = {
                {"Verify OTP", "OTP सत्यापित करें", "OTP पडताळा", "OTP کی تصدیق کریں", "OTP ಪರಿಶೀಲಿಸಿ", "OTP ధృవీకరించండి", "OTP যাচাই করুন", "OTP പരിശോധിക്കുക"},
                {"Verified", "सत्यापित", "पडताळले", "تصدیق شدہ", "ಪರಿಶೀಲಿಸಲಾಗಿದೆ", "ధృవీకరించబడింది", "যাচাই করা হয়েছে", "പരിശോധിച്ചു"},
                {"Change PIN", "पिन बदलें", "पिन बदला", "پن تبدیل کریں", "ಪಿನ್ ಬದಲಾಯಿಸಿ", "పిన్ మార్చండి", "পিন পরিবর্তন করুন", "പിൻ മാറ്റുക"},
                {"New PIN", "नया पिन", "नवीन पिन", "نیا پن", "ಹೊಸ ಪಿನ್", "కొత్త పిన్", "নতুন পিন", "പുതിയ പിൻ"},
                {"State", "राज्य", "राज्य", "ریاست", "ರಾಜ್ಯ", "రాష్ట్రం", "রাজ্য", "സംസ്ഥാനം"},
                {"City", "शहर", "शहर", "شہر", "ನಗರ", "నగరం", "শহর", "നഗരം"},
                {"Select state", "राज्य चुनें", "राज्य निवडा", "ریاست منتخب کریں", "ರಾಜ್ಯವನ್ನು ಆಯ್ಕೆಮಾಡಿ", "రాష్ట్రాన్ని ఎంచుకోండి", "রাজ্য নির্বাচন করুন", "സംസ്ഥാനം തിരഞ്ഞെടുക്കുക"},
                {"Select city", "शहर चुनें", "शहर निवडा", "شہر منتخب کریں", "ನಗರವನ್ನು ಆಯ್ಕೆಮಾಡಿ", "నగరాన్ని ఎంచుకోండి", "শহর নির্বাচন করুন", "നగരം തിരഞ്ഞെടുക്കുക"},
                {"Description", "विवरण", "वर्णन", "تفصیل", "ವಿವರಣೆ", "వివరణ", "বর্ণনা", "വിവരണം"},
                {"Date lost", "खोने की तारीख", "हरवल्याची तारीख", "گم ہونے کی تاریخ", "ಕಳೆದುಹೋದ ದಿನಾಂಕ", "కోల్పోయిన తేదీ", "হারানোর তারিখ", "നഷ്ടപ്പെട്ട തീയതി"},
                {"Date found", "मिलने की तारीख", "सापडल्याची तारीख", "ملنے کی تاریخ", "ಸಿಕ್ಕಿದ ದಿನಾಂಕ", "కనుగొన్న తేదీ", "পাওয়ার তারিখ", "കണ്ടെത്തിയ തീയതി"},
                {"Last seen at", "आखिरी बार यहां देखा गया", "शेवटचे येथे दिसले", "آخری بار یہاں دیکھا گیا", "ಕೊನೆಯದಾಗಿ ಇಲ್ಲಿ ಕಂಡುಬಂದಿದೆ", "చివరిగా ఇక్కడ కనిపించింది", "শেষবার এখানে দেখা গেছে", "അവസാനം ഇവിടെ കണ്ടു"},
                {"Place found", "मिलने का स्थान", "सापडलेले ठिकाण", "ملنے کی جگہ", "ಸಿಕ್ಕ ಸ್ಥಳ", "కనుగొన్న ప్రదేశం", "পাওয়ার স্থান", "കണ്ടെത്തിയ സ്ഥലം"},
                {"Precise location  OFF", "सटीक स्थान बंद", "अचूक स्थान बंद", "درست مقام بند", "ನಿಖರ ಸ್ಥಳ ಆಫ್", "ఖచ్చితమైన స్థానం ఆఫ్", "সঠিক অবস্থান বন্ধ", "കൃത്യമായ സ്ഥാനം ഓഫ്"},
                {"Location ready", "स्थान तैयार है", "स्थान तयार आहे", "مقام تیار ہے", "ಸ್ಥಳ ಸಿದ್ಧವಾಗಿದೆ", "స్థానం సిద్ధంగా ఉంది", "অবস্থান প্রস্তুত", "സ്ഥാനം തയ്യാറാണ്"},
                {"Annual subscription", "वार्षिक सदस्यता", "वार्षिक सदस्यत्व", "سالانہ رکنیت", "ವಾರ್ಷಿಕ ಚಂದಾದಾರಿಕೆ", "వార్షిక సభ్యత్వం", "বার্ষিক সাবস্ক্রিপশন", "വാർഷിക സബ്സ്ക്രിപ്ഷൻ"},
                {"Continue to payment", "भुगतान जारी रखें", "भरणा सुरू ठेवा", "ادائیگی جاری رکھیں", "ಪಾವತಿಗೆ ಮುಂದುವರಿಯಿರಿ", "చెల్లింపుకు కొనసాగండి", "পেমেন্টে এগিয়ে যান", "പേയ്മെന്റിലേക്ക് തുടരുക"},
                {"Save changes", "बदलाव सहेजें", "बदल जतन करा", "تبدیلیاں محفوظ کریں", "ಬದಲಾವಣೆಗಳನ್ನು ಉಳಿಸಿ", "మార్పులను సేవ్ చేయండి", "পরিবর্তন সংরক্ষণ করুন", "മാറ്റങ്ങൾ സംരക്ഷിക്കുക"},
                {"Back home", "होम पर वापस जाएं", "मुख्यपृष्ठावर परत जा", "ہوم پر واپس جائیں", "ಹೋಮ್‌ಗೆ ಹಿಂತಿರುಗಿ", "హోమ్‌కు తిరిగి వెళ్లండి", "হোমে ফিরে যান", "ഹോമിലേക്ക് മടങ്ങുക"},
                {"My reports", "मेरी रिपोर्ट", "माझे अहवाल", "میری رپورٹس", "ನನ್ನ ವರದಿಗಳು", "నా రిపోర్ట్లు", "আমার রিপোর্ট", "എന്റെ റിപ്പോർട്ടുകൾ"},
                {"My profile", "मेरी प्रोफ़ाइल", "माझे प्रोफाइल", "میری پروفائل", "ನನ್ನ ಪ್ರೊಫೈಲ್", "నా ప్రొఫైల్", "আমার প্রোফাইল", "എന്റെ പ്രൊഫൈൽ"},
                {"LOST", "खोया", "हरवले", "گمشدہ", "ಕಳೆದುಹೋಗಿದೆ", "కోల్పోయినవి", "হারিয়ে গেছে", "കുറച്ചു പോയി"},
                {"FOUND", "मिला", "सापडले", "ملا", "ಸಿಕ್ಕಿದೆ", "కనుగొన్నది", "পাওয়া গেছে", "കണ്ടെത്തി"},
                {"Report an item", "वस्तु की रिपोर्ट करें", "वस्तूचा अहवाल द्या", "ایک چیز کی رپورٹ کریں", "ವಸ್ತುವನ್ನು ವರದಿ ಮಾಡಿ", "వస్తువును రిపోర్ట్ చేయండి", "একটি আইটেম রিপোর্ট করুন", "ഒരു ഇനം റിപ്പോർട്ട് ചെയ്യുക"},
                {"Item name", "वस्तु का नाम", "वस्तूचे नाव", "چیز کا نام", "ವಸ್ತುವಿನ ಹೆಸರು", "వస్తువు పేరు", "আইটেমের নাম", "ഇനത്തിന്റെ പേര്"},
                {"IMEI Number", "IMEI नंबर", "IMEI क्रमांक", "IMEI نمبر", "IMEI ಸಂಖ್ಯೆ", "IMEI నంబర్", "IMEI নম্বর", "IMEI നമ്പർ"},
                {"Description and identifying details", "विवरण और पहचान की जानकारी", "वर्णन आणि ओळख तपशील", "تفصیل اور شناختی معلومات", "ವಿವರಣೆ ಮತ್ತು ಗುರುತಿಸುವ ವಿವರಗಳು", "వివరణ మరియు గుర్తింపు వివరాలు", "বর্ণনা ও শনাক্তকরণ তথ্য", "വിവരണം, തിരിച്ചറിയൽ വിശദാംശങ്ങൾ"},
                {"Location or landmark", "स्थान या पहचान चिन्ह", "ठिकाण किंवा खूण", "مقام یا نشانی", "ಸ್ಥಳ ಅಥವಾ ಗುರುತು", "స్థలం లేదా గుర్తు", "অবস্থান বা চিহ্ন", "സ്ഥലം അല്ലെങ്കിൽ അടയാളം"},
                {"Date and time", "दिनांक और समय", "दिनांक आणि वेळ", "تاریخ اور وقت", "ದಿನಾಂಕ ಮತ್ತು ಸಮಯ", "తేదీ మరియు సమయం", "তারিখ ও সময়", "തീയതിയും സമയവും"},
                {"Use current location", "वर्तमान स्थान उपयोग करें", "सध्याचे स्थान वापरा", "موجودہ مقام استعمال کریں", "ಪ್ರಸ್ತುತ ಸ್ಥಳವನ್ನು ಬಳಸಿ", "ప్రస్తుత స్థానాన్ని ఉపయోగించండి", "বর্তমান অবস্থান ব্যবহার করুন", "നിലവിലെ സ്ഥലം ഉപയോഗിക്കുക"},
                {"Submit report — free", "रिपोर्ट भेजें — निःशुल्क", "अहवाल पाठवा — विनामूल्य", "رپورٹ جمع کریں — مفت", "ವರದಿ ಸಲ್ಲಿಸಿ — ಉಚಿತ", "రిపోర్ట్ సమర్పించండి — ఉచితం", "রিপোর্ট জমা দিন — বিনামূল্যে", "റിപ്പോർട്ട് സമർപ്പിക്കുക — സൗജന്യം"},
                {"Updating...", "अपडेट हो रहा है...", "अपडेट होत आहे...", "اپ ڈیٹ ہو رہا ہے...", "ನವೀಕರಿಸಲಾಗುತ್ತಿದೆ...", "అప్‌డేట్ అవుతోంది...", "আপডেট হচ্ছে...", "അപ്‌ഡേറ്റ് ചെയ്യുന്നു..."},
                {"Saving...", "सहेजा जा रहा है...", "जतन होत आहे...", "محفوظ کیا جا رہا ہے...", "ಉಳಿಸಲಾಗುತ್ತಿದೆ...", "సేవ్ అవుతోంది...", "সংরক্ষণ করা হচ্ছে...", "സംരക്ഷിക്കുന്നു..."}
                ,{"Text size", "टेक्स्ट आकार", "मजकूर आकार", "متن کا سائز", "ಪಠ್ಯ ಗಾತ್ರ", "వచన పరిమాణం", "টেক্সটের আকার", "ടെക്സ്റ്റ് വലുപ്പം"}
                ,{"Cancel", "रद्द करें", "रद्द करा", "منسوخ کریں", "ರದ್ದುಮಾಡಿ", "రద్దు చేయండి", "বাতিল করুন", "റദ്ദാക്കുക"}
                ,{"Apply", "लागू करें", "लागू करा", "لاگو کریں", "ಅನ್ವಯಿಸಿ", "వర్తింపజేయండి", "প্রয়োগ করুন", "പ്രയോഗിക്കുക"}
                ,{"Confirm", "पुष्टि करें", "पुष्टी करा", "تصدیق کریں", "ದೃಢೀಕರಿಸಿ", "నిర్ధారించండి", "নিশ্চিত করুন", "സ്ഥിരീകരിക്കുക"}
                ,{"Fingerprint login", "फिंगरप्रिंट लॉगिन", "फिंगरप्रिंट लॉगिन", "فنگر پرنٹ لاگ ان", "ಫಿಂಗರ್‌ಪ್ರಿಂಟ್ ಲಾಗಿನ್", "ఫింగర్‌ప్రింట్ లాగిన్", "ফিঙ্গারপ্রিন্ট লগইন", "ഫിംഗർപ്രിന്റ് ലോഗിൻ"}
                ,{"Confirm your identity to continue", "जारी रखने के लिए अपनी पहचान की पुष्टि करें", "पुढे जाण्यासाठी तुमची ओळख पुष्टी करा", "جاری رکھنے کے لیے اپنی شناخت کی تصدیق کریں", "ಮುಂದುವರಿಯಲು ನಿಮ್ಮ ಗುರುತನ್ನು ದೃಢೀಕರಿಸಿ", "కొనసాగడానికి మీ గుర్తింపును నిర్ధారించండి", "চালিয়ে যেতে আপনার পরিচয় নিশ্চিত করুন", "തുടരാൻ നിങ്ങളുടെ ഐഡന്റിറ്റി സ്ഥിരീകരിക്കുക"}
                ,{"Use another account", "दूसरे खाते का उपयोग करें", "दुसरे खाते वापरा", "دوسرا اکاؤنٹ استعمال کریں", "ಮತ್ತೊಂದು ಖಾತೆಯನ್ನು ಬಳಸಿ", "మరొక ఖాతాను ఉపయోగించండి", "অন্য অ্যাকাউন্ট ব্যবহার করুন", "മറ്റൊരു അക്കൗണ്ട് ഉപയോഗിക്കുക"}
                ,{"Sign in to Fendly", "Fendly में साइन इन करें", "Fendly मध्ये साइन इन करा", "Fendly میں سائن ان کریں", "Fendly ಗೆ ಸೈನ್ ಇನ್ ಮಾಡಿ", "Fendly లో సైన్ ఇన్ చేయండి", "Fendly-তে সাইন ইন করুন", "Fendly-യിൽ സൈൻ ഇൻ ചെയ്യുക"}
                ,{"Confirm your identity", "अपनी पहचान की पुष्टि करें", "तुमची ओळख पुष्टी करा", "اپنی شناخت کی تصدیق کریں", "ನಿಮ್ಮ ಗುರುತನ್ನು ದೃಢೀಕರಿಸಿ", "మీ గుర్తింపును నిర్ధారించండి", "আপনার পরিচয় নিশ্চিত করুন", "നിങ്ങളുടെ ഐഡന്റിറ്റി സ്ഥിരീകരിക്കുക"}
                ,{"Sending...", "भेजा जा रहा है...", "पाठवत आहे...", "بھیجا جا رہا ہے...", "ಕಳುಹಿಸಲಾಗುತ್ತಿದೆ...", "పంపుతోంది...", "পাঠানো হচ্ছে...", "അയയ്ക്കുന്നു..."}
                ,{"Verifying...", "सत्यापन हो रहा है...", "पडताळणी होत आहे...", "تصدیق ہو رہی ہے...", "ಪರಿಶೀಲಿಸಲಾಗುತ್ತಿದೆ...", "ధృవీకరిస్తోంది...", "যাচাই হচ্ছে...", "പരിശോധിക്കുന്നു..."}
                ,{"Enter OTP", "OTP दर्ज करें", "OTP प्रविष्ट करा", "OTP درج کریں", "OTP ನಮೂದಿಸಿ", "OTP నమోదు చేయండి", "OTP লিখুন", "OTP നൽകുക"}
                ,{"Updated", "अपडेट किया गया", "अपडेट केले", "اپ ڈیٹ ہو گیا", "ನವೀಕರಿಸಲಾಗಿದೆ", "నవీకరించబడింది", "আপডেট হয়েছে", "അപ്‌ഡേറ്റ് ചെയ്തു"}
                ,{"Signed in as", "इस खाते में साइन इन: ", "साइन इन केलेले खाते: ", "سائن ان اکاؤنٹ: ", "ಸೈನ್ ಇನ್ ಮಾಡಿದ ಖಾತೆ: ", "సైన్ ఇన్ చేసిన ఖాతా: ", "সাইন ইন করা হয়েছে: ", "സൈൻ ഇൻ ചെയ്ത അക്കൗണ്ട്: "}
                ,{"Renew plan", "प्लान नवीनीकृत करें", "प्लॅन नूतनीकरण करा", "پلان کی تجدید کریں", "ಯೋಜನೆಯನ್ನು ನವೀಕರಿಸಿ", "ప్లాన్‌ను పునరుద్ధరించండి", "প্ল্যান নবায়ন করুন", "പ്ലാൻ പുതുക്കുക"}
                ,{"Active subscription until", "सक्रिय सदस्यता समाप्ति", "सक्रिय सदस्यत्व समाप्ती", "فعال رکنیت کی میعاد", "ಸಕ್ರಿಯ ಚಂದಾದಾರಿಕೆ ಮುಕ್ತಾಯ", "క్రియాశీల సభ్యత్వం ముగింపు", "সক্রিয় সাবস্ক্রিপশন শেষ", "സജീവ സബ്സ്ക്രിപ്ഷൻ അവസാനിക്കുന്നത്"}
                ,{"Username (login)", "उपयोगकर्ता नाम (लॉगिन)", "वापरकर्तानाव (लॉगिन)", "صارف نام (لاگ ان)", "ಬಳಕೆದಾರ ಹೆಸರು (ಲಾಗಿನ್)", "వినియోగదారు పేరు (లాగిన్)", "ব্যবহারকারীর নাম (লগইন)", "ഉപയോക്തൃനാമം (ലോഗിൻ)"}
                ,{"Signing in...", "साइन इन हो रहा है...", "साइन इन होत आहे...", "سائن ان ہو رہا ہے...", "ಸೈನ್ ಇನ್ ಆಗುತ್ತಿದೆ...", "సైన్ ఇన్ అవుతోంది...", "সাইন ইন হচ্ছে...", "സൈൻ ഇൻ ചെയ്യുന്നു..."}
                ,{"Creating account...", "खाता बनाया जा रहा है...", "खाते तयार होत आहे...", "اکاؤنٹ بنایا جا رہا ہے...", "ಖಾತೆಯನ್ನು ರಚಿಸಲಾಗುತ್ತಿದೆ...", "ఖాతా సృష్టించబడుతోంది...", "অ্যাকাউন্ট তৈরি হচ্ছে...", "അക്കൗണ്ട് സൃഷ്ടിക്കുന്നു..."}
        };
        int language = Math.max(0, Math.min(selectedLanguage, 12));
        for (String[] entry : entries) {
            if (entry[0].equalsIgnoreCase(value)) {
                int col = Math.min(language, entry.length - 1);
                return entry[col];
            }
        }
        return null;
    }

    private String localizedFieldLabel(String value) {
        if (value == null) return null;
        if ("Username".equalsIgnoreCase(value)) return loginText("username");
        if ("4-digit PIN".equalsIgnoreCase(value)) return loginText("pin");
        if ("Login".equalsIgnoreCase(value) || "Log in".equalsIgnoreCase(value)) return loginText("login");
        if ("Username (login)".equalsIgnoreCase(value)) return translateUi("Username (login)");
        String translated = translateExtra(value);
        if (translated != null) return translated;
        String ui = translateUi(value);
        if (ui != null) return ui;
        String fallback = translate(value);
        return fallback != null ? fallback : value;
    }

    private String localizeReportsText(String value) {
        if (value == null) return null;
        String[][] entries = {
                {"Loading reports...", "रिपोर्ट लोड हो रही हैं...", "अहवाल लोड होत आहेत...", "રિપોર્ટ લોડ થઈ રહ્યા છે...", "রিপোর্ট লোড হচ্ছে...", "அறிக்கைகள் ஏற்றப்படுகின்றன...", "రిపోర్టులు లోడ్ అవుతున్నాయి...", "ವರದಿಗಳನ್ನು ಲೋಡ್ ಮಾಡಲಾಗುತ್ತಿದೆ...", "റിപ്പോർട്ടുകൾ ലോഡ് ചെയ്യുന്നു..."},
                {"No reports yet.", "अभी तक कोई रिपोर्ट नहीं है।", "अद्याप कोणतेही अहवाल नाहीत.", "હજુ સુધી કોઈ રિપોર્ટ નથી.", "এখনও কোনো রিপোর্ট নেই।", "இதுவரை அறிக்கைகள் எதுவும் இல்லை.", "ఇంకా ఎటువంటి రిపోర్ట్లు లేవు.", "ಇನ್ನೂ ಯಾವುದೇ ವರದಿಗಳಿಲ್ಲ.", "ഇതുവരെ റിപ്പോർട്ടുകൾ ഇല്ല."},
                {"Sign in to view reports.", "रिपोर्ट देखने के लिए साइन इन करें।", "अहवाल पाहण्यासाठी साइन इन करा.", "રિપોર્ટ જોવા માટે સાઇન ઇન કરો.", "রিপোর্ট দেখতে সাইন ইন করুন।", "அறிக்கைகளைப் பார்க்க உள்நுழைவும்.", "రిపోర్టులు చూసేందుకు సైన్ ఇన్ చేయండి.", "ವರದಿಗಳನ್ನು ವೀಕ್ಷಿಸಲು ಸೈನ್ ઇન ಮಾಡಿ.", "റിപ്പോർട്ടുകൾ കാണാൻ സൈൻ ഇൻ ചെയ്യുക."},
                {"Reports are temporarily unavailable.", "रिपोर्ट्स अस्थायी रूप से उपलब्ध नहीं हैं।", "अहवाल तात्पुरते उपलब्ध नाहीत.", "રિપોર્ટ્સ અસ્થાયી રૂપે અનુપલબ્ધ છે.", "রিপোর্ট সাময়িকভাবে পাওয়া যাচ্ছে না।", "அறிக்கைகள் தற்காலிகமாகக் கிடைக்கவில்லை.", "రిపోర్టులు తాత్కాలికంగా అందుబాటులో లేవు.", "ವರದಿಗಳು ತಾತ್ಕಾಲಿಕವಾಗಿ ಲಭ್ಯವಾಗಿಲ್ಲ.", "റിപ്പോർട്ടുകൾ താൽకాలികമായി ലഭ്യമല്ല."},
                {"Reports could not be read.", "रिपोर्ट पढ़ी नहीं जा सकीं।", "अहवाल वाचता आले नाहीत.", "રિપોર્ટ વાંચી શકાયા નથી.", "রিপোর্ট পড়া সম্ভব হয়নি।", "அறிக்கைகளைப் படிக்க முடியவில்லை.", "రిపోర్టులు చదవలేకపోయాము.", "ವರದಿಗಳನ್ನು ಓದಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ.", "റിപ്പോർട്ടുകൾ വായിക്കാൻ കഴിഞ്ഞില്ല."},
                {"Authentication unavailable.", "प्रमाणीकरण उपलब्ध नहीं है।", "प्रमाणीकरण उपलब्ध नाही.", "પ્રમાણીકરણ અનુપલબ્ધ છે.", "প্রমাণীকরণ উপলব্ধ নয়।", "அடையாள அங்கீகாரம் கிடைக்கவில்லை.", "ప్రామాణీకరణ అందుబాటులో లేదు.", "ಪ್ರಮಾಣೀಕರಣ ಲಭ್ಯವಿಲ್ಲ.", "ആധികാരികത ലഭ്യമല്ല."}
        };
        int language = Math.max(0, Math.min(selectedLanguage, entries[0].length - 1));
        for (String[] entry : entries) {
            if (entry[0].equals(value)) return localizeDigits(entry[language]);
        }
        return localizeDigits(value);
    }

    private String translateExtra(String value) {
        String[][] values = {
                {"Tap to upload photo", "First name", "Surname", "Email address", "Mobile number", "State", "City", "Email", "Mobile", "Verify", "Verify OTP", "4-digit PIN", "Complete", "Save changes", "Username", "Username (login)", "New PIN", "Change PIN"},
                {"फ़ोटो अपलोड करने के लिए टैप करें", "पहला नाम", "उपनाम", "ईमेल पता", "मोबाइल नंबर", "राज्य", "शहर", "ईमेल", "मोबाइल", "सत्यापित करें", "OTP सत्यापित करें", "4 अंकों का पिन", "पूरा करें", "बदलाव सहेजें", "उपयोगकर्ता नाम", "उपयोगकर्ता नाम (लॉगिन)", "नया पिन", "पिन बदलें"},
                {"फोटो अपलोड करण्यासाठी टॅप करा", "पहिले नाव", "आडनाव", "ईमेल पत्ता", "मोबाइल नंबर", "राज्य", "शहर", "ईमेल", "मोबाइल", "पडताळा", "OTP पडताळा", "4 अंकी पिन", "पूर्ण करा", "बदल जतन करा", "वापरकर्तानाव", "वापरकर्तानाव (लॉगिन)", "नवीन पिन", "पिन बदला"},
                {"ફોટો અપલોડ કરવા ટેપ કરો", "પ્રથમ નામ", "અટક", "ઈમેલ સરનામું", "મોબાઈલ નંબર", "રાજ્ય", "શહેર", "ઈમેલ", "મોબાઈલ", "ચકાસો", "OTP ચકાસો", "4-અંકનો PIN", "પૂર્ણ કરો", "ફેરફારો સાચવો", "વપરાશકર્તા નામ", "વપરાશકર્તા નામ (લૉગિન)", "નવો PIN", "PIN બદલો"},
                {"ছবি আপলোড করতে ট্যাপ করুন", "প্রথম নাম", "পদবি", "ইমেল ঠিকানা", "মোবাইল নম্বর", "রাজ্য", "শহর", "ইমেল", "মোবাইল", "যাচাই করুন", "OTP যাচাই করুন", "৪-অঙ্কের পিন", "সম্পূর্ণ করুন", "পরিবর্তন সংরক্ষণ করুন", "ব্যবহারকারীর নাম", "ব্যবহারকারীর নাম (লগইন)", "নতুন পিন", "পিন পরিবর্তন করুন"},
                {"புகைப்படத்தைப் பதிவேற்ற தட்டவும்", "முதல் பெயர்", "குடும்பப் பெயர்", "மின்னஞ்சல் முகவரி", "கைபேசி எண்", "மாநிலம்", "நகரம்", "மின்னஞ்சல்", "கைபேசி", "சரிபார்க்கவும்", "OTP சரிபார்க்கவும்", "4-இலக்க PIN", "முடிக்கவும்", "மாற்றங்களைச் சேமிக்கவும்", "பயனர்பெயர்", "பயனர்பெயர் (உள்நுழைவு)", "புதிய PIN", "PIN மாற்றவும்"},
                {"ఫోటోను అప్‌లోడ్ చేయడానికి నొక్కండి", "మొదటి పేరు", "ఇంటి పేరు", "ఇమెయిల్ చిరునామా", "మొబైల్ నంబర్", "రాష్ట్రం", "నగరం", "ఇమెయిల్", "మొబైల్", "ధృవీకరించండి", "OTP ధృవీకరించండి", "4 అంకెల పిన్", "పూర్తి చేయండి", "మార్పులను సేవ్ చేయండి", "వినియోగదారు పేరు", "వినియోగదారు పేరు (లాగిన్)", "కొత్త పిన్", "పిన్ మార్చండి"},
                {"ಫೋಟೋ ಅಪ್‌ಲೋಡ್ ಮಾಡಲು ಟ್ಯಾಪ್ ಮಾಡಿ", "ಮೊದಲ ಹೆಸರು", "ಉಪನಾಮ", "ಇಮೇಲ್ ವಿಳಾಸ", "ಮೊಬೈಲ್ ಸಂಖ್ಯೆ", "ರಾಜ್ಯ", "ನಗರ", "ಇಮೇಲ್", "ಮೊಬೈಲ್", "ಪರಿಶೀಲಿಸಿ", "OTP ಪರಿಶೀಲಿಸಿ", "4 ಅಂಕಿಯ ಪಿನ್", "ಪೂರ್ಣಗೊಳಿಸಿ", "ಬದಲಾವಣೆಗಳನ್ನು ಉಳಿಸಿ", "ಬಳಕೆದಾರ ಹೆಸರು", "ಬಳಕೆದಾರ ಹೆಸರು (ಲಾಗಿನ್)", "ಹೊಸ ಪಿನ್", "ಪಿನ್ ಬದಲಾಯಿಸಿ"},
                {"ഫോട്ടോ അപ്‌ലോഡ് ചെയ്യാൻ ടാപ്പ് ചെയ്യുക", "പേര്", "കുടുംബപ്പേര്", "ഇമെയിൽ വിലാസം", "മൊബൈൽ നമ്പർ", "സംസ്ഥാനം", "നഗരം", "ഇമെയിൽ", "മൊബൈൽ", "പരിശോധിക്കുക", "OTP പരിശോധിക്കുക", "4 അക്ക പിൻ", "പൂർത്തിയാക്കുക", "മാറ്റങ്ങൾ സംരക്ഷിക്കുക", "ഉപയോക്തൃനാമം", "ഉപയോക്തൃനാമം (ലോഗിൻ)", "പുതിയ പിൻ", "പിൻ മാറ്റുക"}
        };
        String[] keys = values[0];
        int language = Math.max(0, Math.min(selectedLanguage, values.length - 1));
        for (int index = 0; index < keys.length; index++) {
            if (keys[index].equalsIgnoreCase(value)) return values[language][index];
        }
        return null;
    }

    private String localizeProfileDisplayValue(String key, String value) {
        if (value == null || value.trim().isEmpty()) return value;
        if ("email".equalsIgnoreCase(key)) return value;
        if ("state".equalsIgnoreCase(key) || key.toLowerCase(Locale.US).contains("state")) {
            String localized = localizedStateName(value);
            return localized != null ? localized : value;
        }
        if ("city".equalsIgnoreCase(key) || key.toLowerCase(Locale.US).contains("city")) {
            String localized = localizedCityName(value);
            return localized != null ? localized : localizeProfileName(value);
        }
        if ("mobile".equalsIgnoreCase(key) || key.toLowerCase(Locale.US).contains("mobile") || key.toLowerCase(Locale.US).contains("phone")) {
            return formatPhoneNumberForDisplay(value);
        }
        return localizeProfileName(value);
    }

    private String localizeProfileName(String value) {
        if (value == null || value.trim().isEmpty()) return value;
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        int lang = Math.max(0, Math.min(selectedLanguage, 12));
        String canonical = getCanonicalEnglishName(value);
        if (lang == 0) {
            return canonical;
        }
        return transliterateToSelectedScript(canonical, lang);
    }

    private String getCanonicalEnglishName(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String trimmed = text.trim();
        if (isAlreadyLocalizedScript(trimmed)) {
            return reverseDevanagariToEnglish(trimmed);
        }
        return trimmed;
    }

    private String defaultSelectStateText() {
        String translated = translateUi("Select state");
        return translated != null ? translated : "Select state";
    }

    private String defaultSelectCityText() {
        String translated = translateUi("Select city");
        return translated != null ? translated : "Select city";
    }

    private boolean isSelectStatePlaceholder(String value) {
        if (value == null) return false;
        String normalized = value.trim();
        return "Select state".equals(normalized)
                || defaultSelectStateText().equals(normalized)
                || "राज्य चुनें".equals(normalized)
                || "राज्य निवडा".equals(normalized)
                || "રાજ્ય પસંદ કરો".equals(normalized)
                || "ರಾಜ್ಯವನ್ನು ಆಯ್ಕೆಮಾಡಿ".equals(normalized)
                || "రాష్ట్రాన్ని ఎంచుకోండి".equals(normalized)
                || "রাজ্য নির্বাচন করুন".equals(normalized)
                || "സംസ്ഥാനം തിരഞ്ഞെടുക്കുക".equals(normalized);
    }

    private boolean isSelectCityPlaceholder(String value) {
        if (value == null) return false;
        String normalized = value.trim();
        return "Select city".equals(normalized)
                || defaultSelectCityText().equals(normalized)
                || "शहर चुनें".equals(normalized)
                || "शहर निवडा".equals(normalized)
                || "શહેર પસંદ કરો".equals(normalized)
                || "ನಗರವನ್ನು ಆಯ್ಕೆಮಾಡಿ".equals(normalized)
                || "నగరాన్ని ఎంచుకోండి".equals(normalized)
                || "শহর নির্বাচন করুন".equals(normalized)
                || "നഗരം തിരഞ്ഞെടുക്കുക".equals(normalized);
    }

    private String exactLocalizedName(String value, int language) {
        if (value == null || value.trim().isEmpty()) return null;
        String normalized = value.trim().toLowerCase(Locale.US);

        String[][] namesTable = {
            {"ashok", "अशोक", "अशोक", "અશોક", "অশোক", "அசோக்", "అశోక్", "ಅಶೋಕ್", "അശോക്", "ਅਸ਼ੋਕ", "ଅଶୋକ", "অশোক", "اشوک"},
            {"rohan", "रोहन", "रोहन", "રોહિત", "রোহান", "ரோஹன்", "రోహన్", "ರೋಹನ್", "രോഹൻ", "ਰੋਹਨ", "<ctrl42>ରୋହନ", "ৰোহন", "روہن"},
            {"bagmare", "बगमारे", "बगमारे", "બગમારે", "বগমারে", "பக்மாரே", "బాగ్మారే", "ಬಗ್ಮਾਰੇ", "ബഗ്മാരെ", "ਬਗਮਾਰੇ", "ବଗମାରେ", "বগমারে", "بگمارے"},
            {"rahul", "राहुल", "राहुल", "રાહુલ", "রাহুল", "ராகுல்", "రాహుల్", "ರಾಹುಲ್", "രാഹുൽ", "ਰਾਹੁਲ", "ରାହୁଲ", "ৰাহুল", "راہول"},
            {"priya", "प्रिया", "प्रिया", "પ્રિયા", "প্রিয়া", "ப்ரியா", "ప్రియ", "ಪ್ರಿಯಾ", "പ്രിയ", "ਪ੍ਰਿਆ", "ପ୍ରିୟା", "প্ৰিয়া", "پریا"},
            {"kumar", "कुमार", "कुमार", "કુમાર", "কুমার", "குமார்", "కుమార్", "ಕುಮಾರ್", "കുമാർ", "ਕੁਮਾਰ", "କୁମାର", "কুমাৰ", "کمار"},
            {"singh", "सिंह", "सिंह", "સિંહ", "সিংহ", "சிங்", "సింగ్", "సింగ్", "സിംഗ്", "ਸਿੰਘ", "ସିଂ", "সিং", "سنگھ"},
            {"sharma", "शर्मा", "शर्मा", "શર્મા", "শর্মা", "சர்மா", "శర్మ", "ಶರ್ಮಾ", "ശർമ്മ", "ਸ਼ਰਮਾ", "ଶର୍ମା", "শৰ্মা", "شرما"},
            {"verma", "वर्मा", "वर्मा", "વર્મા", "বর্মা", "வர்மா", "వర్మ", "వర్మా", "വർമ്മ", "ਵਰਮਾ", "ବର୍ମା", "বৰ্মা", "ورما"},
            {"patil", "पाटिल", "पाटील", "પાટીલ", "પાટીલ", "பாட்டீல்", "పాటిల్", "పాటిల్", "പാട്ടീൽ", "ਪਾਟਿਲ", "ପାଟିଲ", "পাটিল", "پاٹل"},
            {"pawar", "पवार", "पवार", "પવાર", "પવાર", "பવાર", "పవార్", "ಪವಾರ್", "പവാർ", "ਪਵਾਰ", "ପୱାର", "পাৱাৰ", "پوار"},
            {"joshi", "जोशी", "जोशी", "જોશી", "જોશી", "ஜோஷி", "జోషి", "ಜೋಷಿ", "ജോഷി", "ਜੋਸ਼ੀ", "ଜୋଷୀ", "জোশী", "جوشی"},
            {"deshmukh", "देशमुख", "देशमुख", "દેશમુખ", "દેશમુખ", "தேஷ்முக்", "దేశ్‌ముఖ్", "ದೇಶ್‌ಮುಖ್", "ദേശ്മുഖ്", "ਦੇਸ਼ਮੁਖ", "ଦେଶମୁଖ", "দেশমুখ", "دیشمکھ"},
            {"kulkarni", "कुलकर्णी", "कुलकर्णी", "કુલકર્ણી", "કુલકર્ણી", "குல்கர்னி", "కులకర్ణి", "ಕುಲಕರ್ಣಿ", "കുൽക്കർണി", "<ctrl42>ਕੁਲਕਰਣੀ", "କୁଲକର୍ଣ୍ଣୀ", "কুলকার্ণী", "کلکرنی"},
            {"shinde", "शिंदे", "शिंदे", "શિંદે", "શિંદે", "ஷிண்டே", "షిండే", "ಶಿಂಧೆ", "ഷിൻഡെ", "ਸ਼ਿੰਦੇ", "ଶିନ୍ଦେ", "শিন্দে", "شندے"},
            {"gaikwad", "गायकवाड़", "गायकवाड", "ગાયકવાડ", "গায়কোয়াড়", "காயக்வாட்", "గాయక్వాడ్", "ಗಾಯಕ್ವಾಡ್", "ഗായക്‌വാഡ്", "ਗਾਇਕਵਾੜ", "ଗାୟକୱାଡ", "গায়কোৱাড়", "گائیکواڑ"},
            {"raut", "राउत", "राऊत", "રાઉત", "রাউত", "ராவுத்", "రావుత్", "ರಾವುತ್", "റാവുത്ത്", "ਰਾਉਤ", "ରାଉତ", "ৰাউত", "راؤت"},
            {"jadhav", "जाधव", "जाधव", "જાદવ", "যাদব", "ஜாதவ்", "జాధవ్", "ಜಾಧವ್", "ജാധവ്", "ਜਾਧਵ", "ଜାଧବ", "যাদৱ", "جادھو"},
            {"more", "मोरे", "मोरे", "મોરે", "মোরে", "મોરે", "మోరే", "ಮೋರೆ", "മോരെ", "ਮੋਰੇ", "ମୋରେ", "মোৰে", "مورے"},
            {"bhosale", "भोसले", "भोसले", "ભોસલે", "ভোসাલે", "போசலே", "భోసలే", "ಭೋಸಲೆ", "ഭോസലെ", "ਭੋਸਲੇ", "ଭୋସଲେ", "ভোচালে", "بھوسلے"},
            {"chavan", "चव्हाण", "चव्हाण", "ચવ્હાણ", "চভন", "சவான்", "చవాన్", "చవాన్", "ചവാൻ", "ਚਵਾਨ", "ଚଭାଣ", "চৱান", "چوہان"},
            {"mane", "माने", "माने", "માને", "মানে", "மானே", "మానే", "ಮಾನೆ", "മാനെ", "ਮਾਨੇ", "ମାନେ", "মানে", "مانے"},
            {"kadam", "कदम", "कदम", "કદમ", "কদম", "கடம்", "కదమ్", "ಕದಮ್", "കദം", "ਕਦਮ", "<ctrl42>କଦମ", "কদম", "قدم"},
            {"wagh", "वाघ", "वाघ", "વાઘ", "ওয়াঘ", "வாக்", "వాఘ్", "ವಾಘ್", "വാഘ്", "ਵਾਘ", "ୱାଘ", "ৱাঘ", "واگھ"},
            {"kamble", "कांबले", "कांबळे", "કાંબળે", "কাম্বলে", "காம்ப்ளே", "కాంబ్లే", "ಕಾಂಬಳೆ", "കാമ്പ്ളേ", "ਕਾਂਬਲੇ", "କାମ୍ବଳେ", "কাম্বলে", "کامبلے"},
            {"thakur", "ठाकुर", "ठाकूर", "ઠાકુર", "ঠাকুর", "தாக்கூர்", "ఠాకూర్", "ಠಾಕೂರ್", "താക്കൂർ", "ਠਾਕੁਰ", "ଠାକୁର", "ঠাকুৰ", "ٹھاکر"},
            {"gupta", "गुप्ता", "गुप्ता", "ગુપ્તા", "গুপ্তা", "குப்தா", "గుప్తా", "గుప్తా", "ഗുപ്ത", "ਗੁਪਤਾ", "ଗୁପ୍ତା", "গুপ্তা", "گپتا"},
            {"khan", "खान", "खान", "ખાન", "খান", "கான்", "ఖాన్", "ಖಾನ್", "ഖാൻ", "ਖਾਨ", "ଖାନ", "খান", "خان"},
            {"shaikh", "शेख", "शेख", "શેખ", "শেখ", "ஷேக்", "షేక్", "షేక్", "ഷെയ്ഖ്", "ਸ਼ੇਖ", "ଶେଖ", "শ্বেখ", "شیخ"},
            {"patel", "पटेल", "पटेल", "પટેલ", "પટેલ", "பட்டேલ", "పటేల్", "పటేల్", "പട്ടേൽ", "ਪਟੇਲ", "ପଟେଲ", "প্যাটেল", "پٹیل"},
            {"shah", "शाह", "शाह", "શાહ", "શાહ", "ஷா", "షా", "షా", "ഷാ", "ਸ਼ਾਹ", "ଶାହ", "শ্বাহ", "شاہ"},
            {"mehta", "मेहता", "महता", "મહેતા", "મેહતા", "மேத்தா", "మెహతా", "ಮೆಹ್ತಾ", "മേഹ്ത", "ਮਹਿਤਾ", "ମେହତା", "মেহতা", "مہتا"},
            {"nagpur", "नागपुर", "नागपूर", "નાગપુર", "નાગપુર", "நாக்பூர்", "నాగపూర్", "నాగపూర్", "നാഗ്പൂർ", "ਨਾਗਪੁਰ", "ନାଗପୁର", "নাগপুৰ", "ناگپور"},
            {"maharashtra", "महाराष्ट्र", "महाराष्ट्र", "મહારાષ્ટ્ર", "মহারাষ্ট্র", "மகாராஷ்டிரா", "మహారాష్ట్ర", "మహారాష్ట్ర", "മഹാരാഷ്ട്ര", "ਮਹਾਰਾਸ਼ਟਰ", "ମହାରାଷ୍ଟ୍ର", "মহাৰાষ্ট্ৰ", "مہاراشٹرا"},
            {"mumbai", "मुंबई", "मुंबई", "મુંબઈ", "મุม્બાઈ", "மும்பை", "ముంబై", "ముంబై", "മുംബൈ", "ਮੁੰਬਈ", "ମୁମ୍ବାଇ", "মুম্বাই", "ممبئی"},
            {"pune", "पुणे", "पुणे", "પુણે", "પુણે", "புனே", "పుణే", "పుణే", "പൂനെ", "ਪੁਣੇ", "ପୁଣେ", "পুণে", "پونے"}
        };

        for (String[] entry : namesTable) {
            if (entry[0].equals(normalized)) {
                int langIndex = Math.max(1, Math.min(language, entry.length - 1));
                return entry[langIndex];
            }
        }
        return null;
    }

    private String transliterateToSelectedScript(String text, int lang) {
        if (text == null || text.trim().isEmpty() || lang == 0) return text;
        String canonical = getCanonicalEnglishName(text);
        if (lang == 0) return canonical;

        String lower = canonical.toLowerCase(Locale.US);
        String exact = exactLocalizedName(lower, lang);
        if (exact != null) return localizeDigits(exact);

        return phoneticTransliterate(canonical, lang);
    }

    private String phoneticTransliterate(String text, int lang) {
        if (text == null || text.trim().isEmpty()) return text;
        if (lang == 0) return text;

        String devanagari = transliterateToDevanagari(text);
        if (lang == 1 || lang == 2) {
            return devanagari;
        }

        return devanagariToScript(devanagari, lang);
    }

    private String devanagariToScript(String devanagari, int lang) {
        if (devanagari == null || devanagari.isEmpty()) return devanagari;

        int offset = 0;
        switch (lang) {
            case 3: offset = 0x0180; break; // Gujarati (0x0A80)
            case 4:                         // Bengali (0x0980)
            case 11: offset = 0x0080; break; // Assamese
            case 5: offset = 0x0280; break; // Tamil (0x0B80)
            case 6: offset = 0x0300; break; // Telugu (0x0C00)
            case 7: offset = 0x0380; break; // Kannada (0x0C80)
            case 8: offset = 0x0400; break; // Malayalam (0x0D00)
            case 9: offset = 0x0100; break; // Punjabi (0x0A00)
            case 10: offset = 0x0200; break; // Odia (0x0B00)
            default: return devanagari;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < devanagari.length(); i++) {
            char c = devanagari.charAt(i);
            if (c >= 0x0901 && c <= 0x097F) {
                char converted = (char) (c + offset);
                sb.append(converted);
            } else {
                sb.append(c);
            }
        }
        return localizeDigits(sb.toString());
    }

    private String transliterateToDevanagari(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder result = new StringBuilder();
        String[] words = text.split("(?<=\\s)|(?=\\s)");
        for (String word : words) {
            if (word.trim().isEmpty()) {
                result.append(word);
                continue;
            }
            String lower = word.toLowerCase(Locale.US);
            if (isAlreadyLocalizedScript(word)) {
                result.append(localizeDigits(word));
                continue;
            }

            String exact = exactLocalizedName(lower, 2);
            if (exact != null) {
                result.append(localizeDigits(exact));
                continue;
            }

            StringBuilder wordResult = new StringBuilder();
            int index = 0;
            int length = word.length();
            while (index < length) {
                char character = word.charAt(index);
                if (Character.isDigit(character)) {
                    wordResult.append(localizeDigits(String.valueOf(character)));
                    index++;
                    continue;
                }
                if (!Character.isLetter(character)) {
                    wordResult.append(character);
                    index++;
                    continue;
                }

                if (index + 2 <= length) {
                    String sub2 = lower.substring(index, index + 2);
                    if ("sh".equals(sub2)) { wordResult.append("श"); index += 2; continue; }
                    if ("ch".equals(sub2)) { wordResult.append("च"); index += 2; continue; }
                    if ("dh".equals(sub2)) { wordResult.append("ध"); index += 2; continue; }
                    if ("th".equals(sub2)) { wordResult.append("थ"); index += 2; continue; }
                    if ("kh".equals(sub2)) { wordResult.append("ख"); index += 2; continue; }
                    if ("gh".equals(sub2)) { wordResult.append("घ"); index += 2; continue; }
                    if ("ph".equals(sub2)) { wordResult.append("फ"); index += 2; continue; }
                    if ("bh".equals(sub2)) { wordResult.append("भ"); index += 2; continue; }
                    if ("jh".equals(sub2)) { wordResult.append("झ"); index += 2; continue; }
                    if ("ee".equals(sub2)) { wordResult.append(wordResult.length() == 0 ? "ई" : "ी"); index += 2; continue; }
                    if ("oo".equals(sub2)) { wordResult.append(wordResult.length() == 0 ? "ऊ" : "ू"); index += 2; continue; }
                    if ("aa".equals(sub2)) { wordResult.append(wordResult.length() == 0 ? "आ" : "ा"); index += 2; continue; }
                    if ("ai".equals(sub2)) { wordResult.append(wordResult.length() == 0 ? "ऐ" : "ै"); index += 2; continue; }
                    if ("au".equals(sub2)) { wordResult.append(wordResult.length() == 0 ? "औ" : "ौ"); index += 2; continue; }
                }

                char charLower = Character.toLowerCase(character);
                boolean isStart = wordResult.length() == 0;
                switch (charLower) {
                    case 'a': wordResult.append(isStart ? "अ" : ""); break;
                    case 'b': wordResult.append("ब"); break;
                    case 'c': wordResult.append("क"); break;
                    case 'd': wordResult.append("द"); break;
                    case 'e': wordResult.append(isStart ? "ए" : "े"); break;
                    case 'f': wordResult.append("फ"); break;
                    case 'g': wordResult.append("ग"); break;
                    case 'h': wordResult.append("ह"); break;
                    case 'i': wordResult.append(isStart ? "इ" : "ि"); break;
                    case 'j': wordResult.append("ज"); break;
                    case 'k': wordResult.append("क"); break;
                    case 'l': wordResult.append("ल"); break;
                    case 'm': wordResult.append("म"); break;
                    case 'n': wordResult.append("न"); break;
                    case 'o': wordResult.append(isStart ? "ओ" : "ो"); break;
                    case 'p': wordResult.append("प"); break;
                    case 'q': wordResult.append("क"); break;
                    case 'r': wordResult.append("र"); break;
                    case 's': wordResult.append("स"); break;
                    case 't': wordResult.append("त"); break;
                    case 'u': wordResult.append(isStart ? "उ" : "ु"); break;
                    case 'v': case 'w': wordResult.append("व"); break;
                    case 'x': wordResult.append("क्स"); break;
                    case 'y': wordResult.append("य"); break;
                    case 'z': wordResult.append("ज़"); break;
                    default: wordResult.append(character); break;
                }
                index++;
            }
            result.append(wordResult);
        }
        return result.toString();
    }

    private String normalizeIndicToDevanagari(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x0980 && c <= 0x09FF) { // Bengali / Assamese
                sb.append((char) (c - 0x0080));
            } else if (c >= 0x0A00 && c <= 0x0A7F) { // Gurmukhi / Punjabi
                sb.append((char) (c - 0x0100));
            } else if (c >= 0x0A80 && c <= 0x0AFF) { // Gujarati
                sb.append((char) (c - 0x0180));
            } else if (c >= 0x0B00 && c <= 0x0B7F) { // Odia
                sb.append((char) (c - 0x0200));
            } else if (c >= 0x0B80 && c <= 0x0BFF) { // Tamil
                sb.append((char) (c - 0x0280));
            } else if (c >= 0x0C00 && c <= 0x0C7F) { // Telugu
                sb.append((char) (c - 0x0300));
            } else if (c >= 0x0C80 && c <= 0x0CFF) { // Kannada
                sb.append((char) (c - 0x0380));
            } else if (c >= 0x0D00 && c <= 0x0D7F) { // Malayalam
                sb.append((char) (c - 0x0400));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String reverseExactLocalizedName(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String devanagari = normalizeIndicToDevanagari(text);
        String normalized = devanagari.trim().toLowerCase(Locale.US);

        String[][] namesTable = {
            {"ashok", "अशोक", "अशोक", "અશોક", "অশোক"},
            {"rohan", "रोहन", "रोहन", "રોહિત", "রোহান"},
            {"bagmare", "बगमारे", "बागमारे", "બગમારે", "বগমারে"},
            {"rahul", "राहुल", "राहुल", "રાહુલ", "রাহুল"},
            {"priya", "प्रिया", "प्रिया", "પ્રિયા", "প্রিয়া"},
            {"kumar", "कुमार", "कुमार", "કુમાર", "কুমার"},
            {"singh", "सिंह", "सिंह", "સિંહ", "সিংহ"},
            {"sharma", "शर्मा", "शर्मा", "શર્મા", "শর্মা"},
            {"verma", "वर्मा", "वर्मा", "વર્મા", "বর্মা"},
            {"patil", "पाटिल", "पाटील", "પાટીલ", "પાટીલ"},
            {"pawar", "पवार", "पवार", "પવાર", "પવાર"},
            {"joshi", "जोशी", "जोशी", "જોશી", "જોશી"},
            {"deshmukh", "देशमुख", "देशमुख", "દેશમુખ", "દેશમુખ"},
            {"kulkarni", "कुलकर्णी", "कुलकर्णी", "કુલકર્ણી", "કુલકર્ણી"},
            {"shinde", "शिंदे", "शिंदे", "શિંદે", "શિંદે"},
            {"gaikwad", "गायकवाड़", "गायकवाड", "ગાયકવાડ", "গায়কোয়াড়"},
            {"raut", "राउत", "राऊत", "રાઉત", "রাউত"},
            {"jadhav", "जाधव", "जाधव", "જાદવ", "যাদব"},
            {"more", "मोरे", "मोरे", "મોરે", "মোরে"},
            {"bhosale", "भोसले", "भोसले", "ભોસલે", "ভোસાલે"},
            {"chavan", "चव्हाण", "चव्हाण", "ચવ્હાણ", "চভন"},
            {"mane", "माने", "माने", "માને", "মানে"},
            {"kadam", "कदम", "कदम", "કદમ", "কদম"},
            {"wagh", "वाघ", "वाघ", "વાઘ", "ওয়াঘ"},
            {"kamble", "कांबले", "कांबळे", "કાંબળે", "কাম্বলে"},
            {"thakur", "ठाकुर", "ठाकूर", "ઠાકુર", "ঠাকুর"},
            {"gupta", "गुप्ता", "गुप्ता", "ગુપ્તા", "গুপ্তা"},
            {"khan", "खान", "खान", "ખાન", "খান"},
            {"shaikh", "शेख", "शेख", "શેખ", "শেখ"},
            {"patel", "पटेल", "पटेल", "પટેલ", "પટેલ"},
            {"shah", "शाह", "शाह", "શાહ", "શાહ"},
            {"mehta", "मेहता", "महता", "મહેતા", "મેહતા"}
        };

        for (String[] entry : namesTable) {
            for (int index = 1; index < entry.length; index++) {
                if (entry[index].equalsIgnoreCase(normalized)) {
                    String english = entry[0];
                    return english.substring(0, 1).toUpperCase(Locale.US) + english.substring(1);
                }
            }
        }
        return null;
    }

    private String reverseDevanagariToEnglish(String text) {
        if (text == null || text.trim().isEmpty()) return text;
        String devanagari = normalizeIndicToDevanagari(text);
        String normalizedDigits = normalizeLocalizedDigits(devanagari);
        StringBuilder result = new StringBuilder();
        String[] words = normalizedDigits.split("(?<=\\s)|(?=\\s)");
        for (String word : words) {
            if (word.trim().isEmpty()) {
                result.append(word);
                continue;
            }

            String exact = reverseExactLocalizedName(word);
            if (exact != null) {
                result.append(exact);
                continue;
            }

            StringBuilder wordResult = new StringBuilder();
            int index = 0;
            int length = word.length();
            while (index < length) {
                char character = word.charAt(index);
                if (Character.isDigit(character) || !isAlreadyLocalizedScript(String.valueOf(character))) {
                    wordResult.append(character);
                    index++;
                    continue;
                }

                switch (character) {
                    case 'अ': wordResult.append(wordResult.length() == 0 ? "A" : "a"); break;
                    case 'आ': wordResult.append(wordResult.length() == 0 ? "A" : "a"); break;
                    case 'इ': case 'ई': wordResult.append(wordResult.length() == 0 ? "I" : "i"); break;
                    case 'उ': case 'ऊ': wordResult.append(wordResult.length() == 0 ? "U" : "u"); break;
                    case 'ए': case 'ऐ': wordResult.append(wordResult.length() == 0 ? "E" : "e"); break;
                    case 'ओ': case 'औ': wordResult.append(wordResult.length() == 0 ? "O" : "o"); break;
                    case 'ा': wordResult.append("a"); break;
                    case 'ि': case 'ी': wordResult.append("i"); break;
                    case 'ु': case 'ू': wordResult.append("u"); break;
                    case 'े': wordResult.append("e"); break;
                    case 'ै': wordResult.append("ai"); break;
                    case 'ो': wordResult.append("o"); break;
                    case 'ौ': wordResult.append("au"); break;
                    case 'ब': wordResult.append(wordResult.length() == 0 ? "B" : "b"); break;
                    case 'क': wordResult.append(wordResult.length() == 0 ? "K" : "k"); break;
                    case 'द': case 'ड': wordResult.append(wordResult.length() == 0 ? "D" : "d"); break;
                    case 'ग': wordResult.append(wordResult.length() == 0 ? "G" : "g"); break;
                    case 'ह': wordResult.append(wordResult.length() == 0 ? "H" : "h"); break;
                    case 'ज': wordResult.append(wordResult.length() == 0 ? "J" : "j"); break;
                    case 'ल': wordResult.append(wordResult.length() == 0 ? "L" : "l"); break;
                    case 'म': wordResult.append(wordResult.length() == 0 ? "M" : "m"); break;
                    case 'न': wordResult.append(wordResult.length() == 0 ? "N" : "n"); break;
                    case 'प': wordResult.append(wordResult.length() == 0 ? "P" : "p"); break;
                    case 'र': wordResult.append(wordResult.length() == 0 ? "R" : "r"); break;
                    case 'स': wordResult.append(wordResult.length() == 0 ? "S" : "s"); break;
                    case 'श': wordResult.append(wordResult.length() == 0 ? "Sh" : "sh"); break;
                    case 'त': case 'ट': wordResult.append(wordResult.length() == 0 ? "T" : "t"); break;
                    case 'व': wordResult.append(wordResult.length() == 0 ? "V" : "v"); break;
                    case 'य': wordResult.append(wordResult.length() == 0 ? "Y" : "y"); break;
                    case '़': break;
                    default: wordResult.append(character); break;
                }
                index++;
            }

            if (wordResult.length() > 0) {
                String firstChar = String.valueOf(wordResult.charAt(0)).toUpperCase(Locale.US);
                String rest = wordResult.length() > 1 ? wordResult.substring(1) : "";
                result.append(firstChar).append(rest);
            } else {
                result.append(word);
            }
        }
        return result.toString();
    }

    private boolean isAlreadyLocalizedScript(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        return containsUnicodeRange(value, 0x0900, 0x097F)
                || containsUnicodeRange(value, 0x0980, 0x09FF)
                || containsUnicodeRange(value, 0x0A00, 0x0A7F)
                || containsUnicodeRange(value, 0x0A80, 0x0AFF)
                || containsUnicodeRange(value, 0x0B00, 0x0B7F)
                || containsUnicodeRange(value, 0x0B80, 0x0BFF)
                || containsUnicodeRange(value, 0x0C00, 0x0C7F)
                || containsUnicodeRange(value, 0x0C80, 0x0CFF)
                || containsUnicodeRange(value, 0x0D00, 0x0D7F);
    }

    private String reverseLocalizedProfileValue(String key, String value) {
        if (value == null || value.trim().isEmpty()) return value;
        if ("email".equalsIgnoreCase(key)) return value;
        if ("state".equalsIgnoreCase(key)) {
            String english = englishStateName(value);
            return english != null ? english : value;
        }
        if ("city".equalsIgnoreCase(key)) {
            String english = englishCityName(value);
            return english != null ? english : value;
        }
        return value;
    }

    private String localizedCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) return cityName;
        String normalized = cityName.trim();
        if (selectedLanguage != 1 && selectedLanguage != 2) return null;

        switch (normalized.toLowerCase(Locale.US)) {
            case "baghmara": return "बाघमारा";
            case "nagpur": return "नागपूर";
            case "mumbai": return "मुंबई";
            case "pune": return "पुणे";
            case "nashik": return "नाशिक";
            case "aurangabad": return "औरंगाबाद";
            case "thane": return "ठाणे";
            case "navi mumbai": return "नवी मुंबई";
            case "kolhapur": return "कोल्हापूर";
            case "solapur": return "सोलापूर";
            case "amravati": return "अमरावती";
            case "nanded": return "नांदेड";
            case "sangli": return "सांगली";
            case "jalgaon": return "जळगाव";
            case "akola": return "अकोला";
            case "latur": return "लातूर";
            case "dhule": return "धुळे";
            case "ahmednagar": return "अहमदनगर";
            case "satara": return "सतारा";
            case "beed": return "बीड";
            case "ratnagiri": return "रत्नागिरी";
            case "bhandara": return "भंडारा";
            case "buldhana": return "बुलढाणा";
            case "chandrapur": return "चंद्रपूर";
            case "gadchiroli": return "गडचिरोली";
            case "gondia": return "गोंदिया";
            case "hingoli": return "हिंगोली";
            case "jalna": return "जालना";
            case "karad": return "कराड";
            case "lonavala": return "लोणावळा";
            case "malegaon": return "मालेगाव";
            case "malkapur": return "मलकापूर";
            case "nandurbar": return "नंदुरबार";
            case "osmanabad": return "उस्मानाबाद";
            case "palghar": return "पालघर";
            case "parbhani": return "परभणी";
            case "raigad": return "रायगड";
            case "sangamner": return "संगमनेर";
            case "sindhudurg": return "सिंधुदुर्ग";
            case "wardha": return "वर्धा";
            case "washim": return "वाशिम";
            case "yavatmal": return "यवतमाळ";
            case "baramati": return "बारामती";
            case "bhiwandi": return "भिवंडी";
            case "kalyan": return "कल्याण";
            case "mira bhayandar": return "मीरा भाईंदर";
            case "panvel": return "पनवेल";
            case "vasai virar": return "वसई विरार";
            case "ichalkaranji": return "इचलकरंजी";
            case "ulhasnagar": return "उल्हासनगर";
            default: return null;
        }
    }

    private String englishCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) return cityName;
        String value = cityName.trim();
        switch (value) {
            case "बाघमारा": return "Baghmara";
            case "नागपूर": return "Nagpur";
            case "मुंबई": return "Mumbai";
            case "पुणे": return "Pune";
            case "नाशिक": return "Nashik";
            case "औरंगाबाद": return "Aurangabad";
            case "ठाणे": return "Thane";
            case "नवी मुंबई": return "Navi Mumbai";
            case "कोल्हापूर": return "Kolhapur";
            case "सोलापूर": return "Solapur";
            case "अमरावती": return "Amravati";
            case "नांदेड": return "Nanded";
            case "सांगली": return "Sangli";
            case "जळगाव": return "Jalgaon";
            case "अकोला": return "Akola";
            case "लातूर": return "Latur";
            case "धुळे": return "Dhule";
            case "अहमदनगर": return "Ahmednagar";
            case "सतारा": return "Satara";
            case "बीड": return "Beed";
            case "रत्नागिरी": return "Ratnagiri";
            case "भंडारा": return "Bhandara";
            case "बुलढाणा": return "Buldhana";
            case "चंद्रपूर": return "Chandrapur";
            case "गडचिरोली": return "Gadchiroli";
            case "गोंदिया": return "Gondia";
            case "हिंगोली": return "Hingoli";
            case "जालना": return "Jalna";
            case "कराड": return "Karad";
            case "लोणावळा": return "Lonavala";
            case "मालेगाव": return "Malegaon";
            case "मलकापूर": return "Malkapur";
            case "नंदुरबार": return "Nandurbar";
            case "उस्मानाबाद": return "Osmanabad";
            case "पालघर": return "Palghar";
            case "परभणी": return "Parbhani";
            case "रायगड": return "Raigad";
            case "संगमनेर": return "Sangamner";
            case "सिंधुदुर्ग": return "Sindhudurg";
            case "वर्धा": return "Wardha";
            case "वाशिम": return "Washim";
            case "यवतमाळ": return "Yavatmal";
            case "बारामती": return "Baramati";
            case "भिवंडी": return "Bhiwandi";
            case "कल्याण": return "Kalyan";
            case "मीरा भाईंदर": return "Mira Bhayandar";
            case "पनवेल": return "Panvel";
            case "वसई विरार": return "Vasai Virar";
            case "इचलकरंजी": return "Ichalkaranji";
            case "उल्हासनगर": return "Ulhasnagar";
            default: return null;
        }
    }

    private String localizedStateName(String stateName) {
        if (stateName == null || stateName.trim().isEmpty()) return stateName;
        if (selectedLanguage == 0) return stateName;
        if (selectedLanguage == 1 || selectedLanguage == 2) {
            switch (stateName.trim()) {
                case "Maharashtra": return "महाराष्ट्र";
                case "Nagpur": return "नागपूर";
                case "Baghmara": return "बगमारे";
                case "Andhra Pradesh": return "आंध्र प्रदेश";
                case "Arunachal Pradesh": return "अरुणाचल प्रदेश";
                case "Assam": return "असम";
                case "Bihar": return "बिहार";
                case "Chhattisgarh": return "छत्तीसगढ़";
                case "Goa": return "गोवा";
                case "Gujarat": return "गुजरात";
                case "Haryana": return "हरियाणा";
                case "Himachal Pradesh": return "हिमाचल प्रदेश";
                case "Jharkhand": return "झारखंड";
                case "Karnataka": return "कर्नाटक";
                case "Kerala": return "केरल";
                case "Madhya Pradesh": return "मध्य प्रदेश";
                case "Manipur": return "मणिपुर";
                case "Meghalaya": return "मेघालय";
                case "Mizoram": return "मिज़ोरम";
                case "Nagaland": return "नागालैंड";
                case "Odisha": return "ओडिशा";
                case "Punjab": return "पंजाब";
                case "Rajasthan": return "राजस्थान";
                case "Sikkim": return "सिक्किम";
                case "Tamil Nadu": return "तमिलनाडु";
                case "Telangana": return "तेलंगाना";
                case "Tripura": return "त्रिपुरा";
                case "Uttar Pradesh": return "उत्तर प्रदेश";
                case "Uttarakhand": return "उत्तराखंड";
                case "West Bengal": return "पश्चिम बंगाल";
                case "Andaman and Nicobar Islands": return "अंडमान और निकोबार द्वीपसमूह";
                case "Chandigarh": return "चंडीगढ़";
                case "Dadra and Nagar Haveli and Daman and Diu": return "दादरा और नगर हवेली तथा दमन और दीव";
                case "Delhi": return "दिल्ली";
                case "Jammu and Kashmir": return "जम्मू और कश्मीर";
                case "Ladakh": return "लद्दाख";
                case "Lakshadweep": return "लक्षद्वीप";
                case "Puducherry": return "पुदुचेरी";
                default:
                    break;
            }
        }

        String[][] translations = {
                {"Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat", "Haryana",
                        "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
                        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
                        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
                        "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
                        "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"},
                {"आंध्र प्रदेश", "अरुणाचल प्रदेश", "असम", "बिहार", "छत्तीसगढ़", "गोवा", "गुजरात", "हरियाणा",
                        "हिमाचल प्रदेश", "झारखंड", "कर्नाटक", "केरल", "मध्य प्रदेश", "महाराष्ट्र", "मणिपुर",
                        "मेघालय", "मिज़ोरम", "नागालैंड", "ओडिशा", "पंजाब", "राजस्थान", "सिक्किम", "तमिलनाडु",
                        "तेलंगाना", "त्रिपुरा", "उत्तर प्रदेश", "उत्तराखंड", "पश्चिम बंगाल",
                        "अंडमान और निकोबार द्वीपसमूह", "चंडीगढ़", "दादरा और नगर हवेली तथा दमन और दीव", "दिल्ली",
                        "जम्मू और कश्मीर", "लद्दाख", "लक्षद्वीप", "पुदुचेरी"},
                {"ఆంధ్ర ప్రదేశ్", "అరుణాచల్ ప్రదేశ్", "అస్సాం", "బీహార్", "చత్తీస్‌గఢ్", "గోవా", "గుజరాత్", "హర్యానా",
                        "హిమాచల్ ప్రదేశ్", "జార్ఖండ్", "కర్నాటక", "కేరళ", "మధ్య ప్రదేశ్", "మహారాష్ట్ర", "మనిపూర్",
                        "మేఘాలయ", "మిజోరం", "నాగాలాండ్", "ఒడిషా", "పంజాబ్", "రాజస్థాన్", "సిక్కిం", "తమిళనాడు",
                        "తెలంగాణ", "త్రిపుర", "ఉత్తరప్రదేశ్", "ఉత్తరాఖండ్", "పశ్చిమ బెంగాల్",
                        "అండమాన్ మరియు నికోబార్ ద్వీపాలు", "చండీఘర్", "దాద్రా మరియు నగర్ హవేలీ మరియు దామన్ మరియు డయ్యు", "ఢిల్లీ",
                        "జమ్మూ మరియు కాశ్మీర్", "లడఖ్", "లక్షద్వీప్", "పుదుచ్చేరి"},
                {"আন্দামান ও নিকোবর দ্বীপপুঞ্জ", "চণ্ডীগড়", "দাদরা ও নগর হাভেলি ও দমন ও দিউ", "দিল্লি", "জম্মু ও কাশ্মীর", "লাদাখ", "লাক্ষাদ্বীপ", "পুদুচেরি"},
                {"ആൻഡമാൻ വലക്യങ്ങളും നിക്കോബാർ ദ്വീപുകളും", "ചണ്ഡിഗഡ്", "ദാദ്രാ നഗർ ഹവേലി, ദാമൻ-ദിയു", "ദില്ലി", "ജമ്മു കശ്മീർ", "ലഡാക്ക്", "ലക്ഷദ്വീപ്", "പുതുച്ചേരി"}
        };
        for (int languageIndex = 0; languageIndex < translations.length; languageIndex++) {
            String[] values = translations[languageIndex];
            for (int index = 0; index < values.length; index++) {
                if (stateName.equalsIgnoreCase(translations[0][index])) {
                    return values[index];
                }
            }
        }
        return null;
    }

    private String englishStateName(String stateName) {
        String[][] translations = {
                {"Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat", "Haryana",
                        "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
                        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
                        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
                        "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
                        "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"},
                {"आंध्र प्रदेश", "अरुणाचल प्रदेश", "असम", "बिहार", "छत्तीसगढ़", "गोवा", "गुजरात", "हरियाणा",
                        "हिमाचल प्रदेश", "झारखंड", "कर्नाटक", "केरल", "मध्य प्रदेश", "महाराष्ट्र", "मणिपुर",
                        "मेघालय", "मिज़ोरम", "नागालैंड", "ओडिशा", "पंजाब", "राजस्थान", "सिक्किम", "तमिलनाडु",
                        "तेलंगाना", "त्रिपुरा", "उत्तर प्रदेश", "उत्तराखंड", "पश्चिम बंगाल",
                        "अंडमान और निकोबार द्वीपसमूह", "चंडीगढ़", "दादरा और नगर हवेली तथा दमन और दीव", "दिल्ली",
                        "जम्मू और कश्मीर", "लद्दाख", "लक्षद्वीप", "पुदुचेरी"},
                {"ఆంధ్ర ప్రదేశ్", "అరుణాచల్ ప్రదేశ్", "అస్సాం", "బీహార్", "చత్తీస్‌గఢ్", "గోవా", "గుజరాత్", "హర్యానా",
                        "హిమాచల్ ప్రదేశ్", "జార్ఖండ్", "కర్నాటక", "కేరళ", "మధ్య ప్రదేశ్", "మహారాష్ట్ర", "మనిపూర్",
                        "మేఘాలయ", "మిజోరం", "నాగాలాండ్", "ఒడిషా", "పంజాబ్", "రాజస్థాన్", "సిక్కిం", "తమిళనాడు",
                        "తెలంగాణ", "త్రిపుర", "ఉత్తరప్రదేశ్", "ఉత్తరాఖండ్", "పశ్చిమ బెంగాల్",
                        "అండమాన్ మరియు నికోబార్ ద్వీపాలు", "చండీఘర్", "దాద్రా మరియు నగర్ హవేలీ మరియు దామన్ మరియు డయ్యు", "ఢిల్లీ",
                        "జమ్మూ మరియు కాశ్మీర్", "లడఖ్", "లక్షద్వీప్", "పుదుచ్చేరి"},
                {"আন্দামান ও নিকোবর দ্বীপপুঞ্জ", "চণ্ডীগড়", "দাদরা ও নগর হাভেলি ও দমন ও দিউ", "দিল্লি", "জম্মু ও কাশ্মীর", "লাদাখ", "লাক্ষাদ্বীপ", "পুদুচেরি"},
                {"ആൻഡമാൻ വലക്യങ്ങളും നിക്കോബാർ ദ്വീപുകളും", "ചണ്ഡിഗഡ്", "ദാദ്രാ നഗർ ഹവേലി, ദാമൻ-ദിയു", "ദില്ലി", "ജമ്മു കശ്മീർ", "ലഡാക്ക്", "ലക്ഷദ്വീപ്", "പുതുച്ചേരി"}
        };
        for (int languageIndex = 0; languageIndex < translations.length; languageIndex++) {
            String[] values = translations[languageIndex];
            for (int index = 0; index < values.length; index++) {
                if (stateName.equalsIgnoreCase(values[index])) {
                    return translations[0][index];
                }
            }
        }
        return null;
    }

    private String localizedAnnualPriceText() {
        String[] yearUnits = {"year", "वर्ष", "वर्ष", "વર્ષ", "বছর", "வருடம்", "సంవత్సరం", "ವರ್ಷ", "വർഷം"};
        int language = Math.max(0, Math.min(selectedLanguage, yearUnits.length - 1));
        String price = localizeDigits("₹99");
        String unit = yearUnits[language];
        return price + "/" + unit;
    }

    private String localizeDigits(String value) {
        String[] digits = {
                "0123456789", "०१२३४५६७८९", "०१२३४५६७८९", "૦૧૨૩૪૫૬૭૮૯",
                "০১২৩৪৫৬৭৮৯", "௦௧௨௩௪௫௬௭௮௯", "౦౧౨౩౪౫౬౭౮౯", "೦೧೨೩೪೫೬೭೮೯",
                "൦<ctrl42>൨൩൪൫൬൭൮൯", "੦੧੨੩੪੫੬੭੮੯", "୦୧୨୩૪୫୬୭୮୯", "০১২৩৪৫৬৭৮৯",
                "٠١٢٣٤٥٦٧٨٩"
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

    private String normalizeLocalizedDigits(String value) {
        if (value == null || value.isEmpty()) return value;
        String[] digits = {
                "0123456789", "०१२३४५६७८९", "०१२३४५६७८९", "૦૧૨૩૪૫૬૭૮૯",
                "০১২৩৪৫৬৭৮৯", "௦௧௨௩௪௫௬௭௮௯", "౦౧౨౩౪౫౬౭౮౯", "೦೧೨೩೪೫೬೭೮೯",
                "൦<ctrl42>൨൩൪൫൬൭൮൯", "੦੧੨੩੪੫੬੭੮੯", "୦୧୨୩૪୫୬୭୮୯", "০১২৩৪৫৬৭৮৯",
                "٠١٢٣٤٥٦٧٨٩"
        };
        StringBuilder normalized = new StringBuilder(value.length());
        for (int characterIndex = 0; characterIndex < value.length(); characterIndex++) {
            char character = value.charAt(characterIndex);
            boolean replaced = false;
            for (String digitSet : digits) {
                int digitIndex = digitSet.indexOf(character);
                if (digitIndex >= 0) {
                    normalized.append(digitIndex);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) normalized.append(character);
        }
        return normalized.toString();
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
