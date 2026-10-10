package com.example.fendly;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.content.res.ColorStateList;
import android.graphics.drawable.LayerDrawable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.activity.OnBackPressedCallback;
import androidx.core.os.LocaleListCompat;
import androidx.core.widget.TextViewCompat;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import androidx.compose.ui.platform.ComposeView;
import android.content.res.Configuration;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.CheckBox;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.SecureRandom;

import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.FirebaseException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import java.util.Collections;
import android.graphics.drawable.GradientDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.exifinterface.media.ExifInterface;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import com.example.fendly.notifications.FcmRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import java.util.concurrent.TimeUnit;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.location.Location;
import android.location.LocationListener;
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
    private int profileScrollY;
    private Uri selectedImage;
    private Bitmap capturedImage;
    private final Uri[] reportImages = new Uri[3];
    private final Bitmap[] reportCameraImages = new Bitmap[3];
    private int pendingImageSlot = -1;
    private static final int REQUEST_CAMERA_PERMISSION = 705;
    private double currentLat;
    private double currentLng;
    private boolean hasLocation;
    private String activeLocationReportType;
    private boolean lostReportHasLocation;
    private double lostReportLat;
    private double lostReportLng;
    private boolean foundReportHasLocation;
    private double foundReportLat;
    private double foundReportLng;
    private String locationRequestReportType;
    private long locationRequestGeneration;
    private TextView locationStatus;
    private TextView locationToggleStatus;
    private LocationListener activeLocationListener;
    private String phoneVerificationMobile;
    private boolean phoneVerificationHandled;
    private String currentReportType;
    private boolean adminEnglishUi;

    private String normalizePhoneNumber(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) return "";
        if (digits.length() == 10) return "+91" + digits;
        if (digits.startsWith("91") && digits.length() > 10) return "+" + digits;
        return "+" + digits;
    }

    private String normalizeIndianMobileDigits(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            return digits.substring(2);
        }
        return digits;
    }

    private void stopActiveLocationUpdates() {
        LocationListener listener = activeLocationListener;
        activeLocationListener = null;
        if (listener == null) return;
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) return;
        try {
            manager.removeUpdates(listener);
        } catch (SecurityException error) {
            Log.w("LOCATION", "Location access was revoked before updates could be stopped", error);
        }
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
    private Uri pendingProfileCameraUri;
    private Bitmap capturedProfileImage;
    private boolean profileImageExplicitlyRemoved = false;
    private String profilePhotoCacheDownloadUrl = "";
    private static final int REQUEST_PROFILE_IMAGE = 706;
    private static final int REQUEST_PROFILE_CAMERA = 707;
    private static final long EMAIL_VERIFICATION_COOLDOWN_MS = 60000L;
    private Runnable emailVerificationCooldownRunnable;
    private String draftItem = "";
    private String draftDescription = "";
    private String draftLocation = "";
    private String draftCityTag = "";
    private String draftDate = "";
    private String draftIdentifier = "";
    private String draftReportCategory = "item";
    private int reportWizardStep = 1;
    private String draftImei = "";
    private boolean draftSocialShareConsent;
    private boolean draftGuidelinesAccepted;
    private String localEmailOtp = "";
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
    private EditText pendingPaymentLocation;
    private EditText pendingPaymentDate;
    private TextView pendingPaymentButton;
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
    private static final int PAGE_ADMIN_SUBSCRIPTIONS = 8;
    private boolean adminAlertsAutoShownThisVisit;
    private boolean adminSocialPageOpen;
    private boolean adminContentReportsPageOpen;
    private boolean socialAuthorizationPending;
    private int pendingNotificationCount = 0;
    private TextView pendingNotificationBadge;
    private String adminReportFilter = "";
    private String adminReportCategory = "items";
    private String discoveryCity = "";
    private int discoveryRequestGeneration;
    private String myReportsCategory = "items";
    private boolean discoveryReportsLoaded;
    private final List<JSONObject> discoveryReports = new ArrayList<>();
    private int unreadUserNotificationCount = 0;
    private TextView userNotificationBadge;
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
    private AutoCompleteTextView visibleStateSearch;
    private AutoCompleteTextView visibleCitySearch;
    private TextView visibleEmailVerify;
    private TextView visibleMobileVerify;
    private ImageView visibleAvatar;
    private TextToSpeech ttsEngine;
    private boolean ttsReady;
    private String pendingGuideSpeech;
    private ImageView pendingGuidePlayButton;
    private String guideSpeechText;
    private volatile int guideSpeechPosition;
    private int guideSpeechGeneration;
    private ImageView guideSpeechPlayButton;
    private EditText[] visibleMobileCells;
    private static final int REQUEST_SMS_USER_CONSENT = 913;
    private BroadcastReceiver smsUserConsentReceiver;
    private boolean smsUserConsentReceiverRegistered;
    private Intent pendingSmsUserConsentIntent;
    private EditText[] activeSmsOtpCells;
    private Dialog activeSmsOtpDialog;
    private final Handler realtimeProfileHandler = new Handler(Looper.getMainLooper());
    private Runnable realtimeProfileSave;
    private boolean applyingCloudProfile;
    private boolean visibleNameDirty;
    private final List<Runnable> pendingProfileHydrationCallbacks = new ArrayList<>();
    private long lastProfileHydrationAttemptMs = 0L;
    private static final long PROFILE_HYDRATION_RETRY_WINDOW_MS = 10000L;
    private static final String API_BASE = "https://fendly-api.onrender.com";
    private static final String TEST_PHONE_PIN_RECOVERY_MOBILE = "8657111989";
    /**
     * Temporary testing toggle: set to true to restore the mobile OTP requirement later.
     * Keep the old validation logic in place while it is disabled for the current testing phase.
     */
    private static final boolean REQUIRE_MOBILE_OTP_FOR_PROFILE_SAVE = true;
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private volatile String lastSubmissionError;

    private String normalizeDocumentKey(String rawUsername) {
        if (rawUsername == null) return "";
        String username = rawUsername.trim().toLowerCase(Locale.US);
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        username = username.replaceAll("[^a-z0-9_]", "_");
        username = username.replaceAll("_+", "_");
        return username.replaceAll("^_+|_+$", "");
    }

    private String getProfileDocumentKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String username = normalizeDocumentKey(account.getString("username", ""));
        if (!username.isEmpty() && username.length() >= 3) {
            return username;
        }
        return "anonymous";
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean savedDarkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false);
        setTheme(savedDarkMode ? R.style.Theme_Fendly_Dark : R.style.Theme_Fendly);
        LanguageManager.restoreSavedLanguage(this);
        int savedLanguage = LanguageManager.getSavedLanguageIndex(this);
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleAppBack();
            }
        });
        initializeCloudinary();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        languagePreferences = getSharedPreferences("fendly_language", MODE_PRIVATE);
        selectedLanguage = languagePreferences.getInt("selected_language_index", 0);
        darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE).getBoolean("dark_mode", false);
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit().remove("imei_number").remove("mobile_serial_number").apply();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Always reset fresh account drafts when there's no current Firebase user,
        // even if the 'created' flag persists from a previous session.
        accountCreated = firebaseUser != null;
        if (firebaseUser == null) {
            resetFreshAccountDrafts();
        } else {
            accountCreated = true;
        }
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("state_current_page", PAGE_AUTH);
            currentReportType = savedInstanceState.getString("state_report_type", null);
            draftItem = savedInstanceState.getString("state_draft_item", "");
            draftDescription = savedInstanceState.getString("state_draft_description", "");
            draftLocation = savedInstanceState.getString("state_draft_location", "");
            draftCityTag = savedInstanceState.getString("state_draft_city_tag", "");
            draftDate = savedInstanceState.getString("state_draft_date", "");
            draftIdentifier = savedInstanceState.getString("state_draft_identifier", "");
            draftReportCategory = savedInstanceState.getString("state_draft_report_category", "item");
            reportWizardStep = savedInstanceState.getInt("state_report_wizard_step", 1);
            draftImei = savedInstanceState.getString("state_draft_imei", "");
            draftSocialShareConsent = savedInstanceState.getBoolean("state_draft_social_share_consent", false);
            draftGuidelinesAccepted = savedInstanceState.getBoolean("state_draft_guidelines_accepted", false);
            draftFullName = savedInstanceState.getString("state_draft_full_name", "");
            draftEmail = savedInstanceState.getString("state_draft_email", "");
            draftMobile = savedInstanceState.getString("state_draft_mobile", "");
            draftPin = savedInstanceState.getString("state_draft_pin", "");
            draftState = savedInstanceState.getString("state_draft_state", "");
            draftCity = savedInstanceState.getString("state_draft_city", "");
            profileSetupVisible = savedInstanceState.getBoolean("state_profile_setup_visible", false);
            profileScrollY = savedInstanceState.getInt("state_profile_scroll_y", 0);
            inRenewalPaymentFlow = savedInstanceState.getBoolean("state_in_renewal_payment_flow", false);
        }
        boolean languageSelected = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .contains("selected_language_index");
        if (!languageSelected) {
            Intent intent = new Intent(this, LanguageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
            return;
        }

        if (firebaseUser == null
                && !getSharedPreferences("fendly_onboarding", MODE_PRIVATE)
                        .getBoolean("welcome_seen", false)) {
            showWelcomeOnboarding(() -> {
                getSharedPreferences("fendly_onboarding", MODE_PRIVATE)
                        .edit()
                        .putBoolean("welcome_seen", true)
                        .apply();
                restoreProfileDrafts();
                applySystemBarColors();
                restoreScreenState();
                FcmRegistration.registerCurrentToken();
            });
            return;
        }

        restoreProfileDrafts();
        applySystemBarColors();
        restoreScreenState();
        FcmRegistration.registerCurrentToken();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            hydrateProfileFromBackend(null);
            hydrateCloudProfile(null);
            checkAndReloadUserVerification();
            refreshAnnualSubscription(null);
        }
    }

    private void showWelcomeOnboarding() {
        showWelcomeOnboarding(() -> {});
    }

    private void showWelcomeOnboarding(Runnable onContinue) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(14), dp(14), dp(14), dp(14));
        screen.setBackgroundColor(backgroundColor());
        WindowInsetsHelper.applySafeArea(screen);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(24), dp(22), dp(20));
        card.setBackground(roundWithStroke(surfaceColor(), 28, borderColor()));
        card.setElevation(dp(8));
        screen.addView(card, new LinearLayout.LayoutParams(-1, 0, 1f));

        TextView title = text(onboardingText("title"), 23, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        title.setPadding(0, 0, 0, dp(18));
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        addWelcomeSection(content, "purpose_heading", "purpose_body");
        addWelcomeHeading(content, "how_heading");
        addWelcomeItem(content, "lost_lead", "lost_body");
        addWelcomeItem(content, "found_lead", "found_body");
        addWelcomeHeading(content, "steps_heading");
        addWelcomeItem(content, "step1_lead", "step1_body");
        addWelcomeItem(content, "step2_lead", "step2_body");
        addWelcomeItem(content, "step3_lead", "step3_body");

        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView continueButton = actionButton(onboardingText("continue"), true);
        continueButton.setOnClickListener(view -> {
            dialog.dismiss();
            onContinue.run();
        });
        actionsRow.addView(continueButton, new LinearLayout.LayoutParams(0, dp(48), 1f));

        ImageView voiceButton = new ImageView(this);
        voiceButton.setImageResource(android.R.drawable.ic_media_play);
        voiceButton.setColorFilter(primaryTextColor());
        voiceButton.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        voiceButton.setPadding(dp(10), dp(10), dp(10), dp(10));
        voiceButton.setContentDescription(onboardingText("listen"));
        voiceButton.setOnClickListener(view -> speakOrStop(buildWelcomeSpeechText(), voiceButton));
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        voiceParams.setMargins(dp(8), 0, 0, 0);
        actionsRow.addView(voiceButton, voiceParams);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, -2);
        actionsParams.setMargins(0, dp(8), 0, 0);
        card.addView(actionsRow, actionsParams);

        dialog.setContentView(screen);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(ignored -> stopGuideSpeech());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setStatusBarColor(backgroundColor());
            window.setNavigationBarColor(backgroundColor());
        }
    }

    private String onboardingText(String key) {
        return LanguageManager.onboardingText(this, key);
    }

    private void addWelcomeHeading(LinearLayout parent, String key) {
        TextView sectionHeading = text(onboardingText(key), 16, primaryTextColor(), Typeface.BOLD);
        sectionHeading.setPadding(0, dp(8), 0, dp(8));
        parent.addView(sectionHeading, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addWelcomeSection(LinearLayout parent, String headingKey, String bodyKey) {
        addWelcomeHeading(parent, headingKey);
        TextView body = text(onboardingText(bodyKey), 14, secondaryTextColor(), Typeface.NORMAL);
        body.setLineSpacing(dp(3), 1.12f);
        body.setPadding(0, 0, 0, dp(10));
        parent.addView(body, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addWelcomeItem(LinearLayout parent, String leadKey, String bodyKey) {
        String leadText = onboardingText(leadKey);
        if ("lost_lead".equals(leadKey) || "found_lead".equals(leadKey)) {
            leadText = "• " + leadText;
        }
        TextView lead = text(leadText, 14, primaryTextColor(), Typeface.BOLD);
        lead.setPadding(0, dp(4), 0, dp(2));
        parent.addView(lead, new LinearLayout.LayoutParams(-1, -2));

        TextView body = text(onboardingText(bodyKey), 14, secondaryTextColor(), Typeface.NORMAL);
        body.setLineSpacing(dp(3), 1.12f);
        body.setPadding(dp(10), 0, 0, dp(8));
        parent.addView(body, new LinearLayout.LayoutParams(-1, -2));
    }

    private String buildWelcomeSpeechText() {
        return onboardingText("title") + ". "
                + onboardingText("purpose_heading") + " " + onboardingText("purpose_body") + " "
                + onboardingText("how_heading") + " "
                + onboardingText("lost_lead") + " " + onboardingText("lost_body") + " "
                + onboardingText("found_lead") + " " + onboardingText("found_body") + " "
                + onboardingText("steps_heading") + " "
                + onboardingText("step1_lead") + " " + onboardingText("step1_body") + " "
                + onboardingText("step2_lead") + " " + onboardingText("step2_body") + " "
                + onboardingText("step3_lead") + " " + onboardingText("step3_body");
    }

    private void checkAndReloadUserVerification() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        currentUser.reload().addOnCompleteListener(task -> {
            boolean firebaseVerified = currentUser.isEmailVerified();
            if (firebaseVerified) {
                getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                        .putBoolean("email_verified", true)
                        .apply();
                if (currentPage == PAGE_PROFILE && visibleEmail != null && visibleEmailVerify != null) {
                    runOnUiThread(() -> refreshEmailVerificationState(visibleEmail, visibleEmailVerify));
                }
            }

            currentUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
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
                            boolean apiVerified = profile.optBoolean("email_verified", false);
                            if (apiVerified) {
                                getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                        .putBoolean("email_verified", true)
                                        .apply();
                                if (currentPage == PAGE_PROFILE && visibleEmail != null && visibleEmailVerify != null) {
                                    runOnUiThread(() -> refreshEmailVerificationState(visibleEmail, visibleEmailVerify));
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }));
        }).addOnFailureListener(e -> {
            Log.d("USER_RELOAD", "User reload failed due to network issues: ", e);
        });
    }

    private void checkLanguageAndRedirectIfNeeded() {
        boolean languageSelected = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .contains("selected_language_index");
        if (!languageSelected) {
            Intent intent = new Intent(this, LanguageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        stopActiveLocationUpdates();
        stopSmsUserConsent();
        if (cloudProfileListener != null) cloudProfileListener.remove();
        if (realtimeProfileSave != null) realtimeProfileHandler.removeCallbacks(realtimeProfileSave);
        if (ttsEngine != null) {
            stopGuideSpeech();
            ttsEngine.shutdown();
            ttsEngine = null;
            ttsReady = false;
        }
        network.shutdownNow();
        super.onDestroy();
    }

    private void initTts() {
        if (ttsEngine == null) {
            ttsEngine = new TextToSpeech(this, status -> {
                if (isFinishing() || isDestroyed()) return;
                if (status == TextToSpeech.SUCCESS) {
                    ttsReady = true;
                    String pendingText = pendingGuideSpeech;
                    ImageView pendingButton = pendingGuidePlayButton;
                    pendingGuideSpeech = null;
                    pendingGuidePlayButton = null;
                    if (pendingText != null && pendingButton != null) {
                        speakGuideNow(pendingText, pendingButton);
                    }
                } else {
                    ttsReady = false;
                    stopGuideSpeech();
                    if (ttsEngine != null) {
                        ttsEngine.shutdown();
                        ttsEngine = null;
                    }
                }
            });
        }
    }

    private Locale getTtsLocaleForSelectedLanguage() {
        int lang = Math.max(0, Math.min(selectedLanguage, 8));
        switch (lang) {
            case 1: return new Locale("hi", "IN"); // Hindi
            case 2: return new Locale("mr", "IN"); // Marathi
            case 3: return new Locale("gu", "IN"); // Gujarati
            case 4: return new Locale("bn", "IN"); // Bengali
            case 5: return new Locale("ta", "IN"); // Tamil
            case 6: return new Locale("te", "IN"); // Telugu
            case 7: return new Locale("kn", "IN"); // Kannada
            case 8: return new Locale("ml", "IN"); // Malayalam
            default: return Locale.ENGLISH;        // English
        }
    }

    private void speakOrStop(String textToSpeak, ImageView playPauseIcon) {
        if (!ttsReady) {
            if (pendingGuidePlayButton == playPauseIcon) {
                cancelPendingGuideSpeech();
                return;
            }
            if (guideSpeechPlayButton != playPauseIcon || !textToSpeak.equals(guideSpeechText)) {
                resetGuideSpeechPosition();
            }
            guideSpeechText = textToSpeak;
            guideSpeechPlayButton = playPauseIcon;
            pendingGuideSpeech = textToSpeak;
            pendingGuidePlayButton = playPauseIcon;
            playPauseIcon.setImageResource(android.R.drawable.ic_media_pause);
            initTts();
            return;
        }
        if (ttsEngine.isSpeaking()) {
            if (guideSpeechPlayButton != playPauseIcon || !textToSpeak.equals(guideSpeechText)) {
                ttsEngine.stop();
                resetGuideSpeechPosition();
                guideSpeechText = textToSpeak;
                guideSpeechPlayButton = playPauseIcon;
                speakGuideNow(textToSpeak, playPauseIcon);
                return;
            }
            guideSpeechGeneration++;
            ttsEngine.stop();
            playPauseIcon.setImageResource(android.R.drawable.ic_media_play);
            return;
        }
        if (guideSpeechPlayButton != playPauseIcon || !textToSpeak.equals(guideSpeechText)) {
            resetGuideSpeechPosition();
            guideSpeechText = textToSpeak;
            guideSpeechPlayButton = playPauseIcon;
        }
        speakGuideNow(textToSpeak, playPauseIcon);
    }

    private void speakGuideNow(String textToSpeak, ImageView playPauseIcon) {
        if (ttsEngine == null || !ttsReady) return;
        if (!textToSpeak.equals(guideSpeechText)) {
            guideSpeechText = textToSpeak;
            guideSpeechPosition = 0;
        }
        int startPosition = Math.max(0, Math.min(guideSpeechPosition, textToSpeak.length()));
        int generation = ++guideSpeechGeneration;
        String utteranceId = "GuideVoice-" + generation;
        Locale targetLocale = getTtsLocaleForSelectedLanguage();
        int result = ttsEngine.setLanguage(targetLocale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            ttsEngine.setLanguage(Locale.ENGLISH);
        }
        ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}

            @Override public void onDone(String id) {
                runOnUiThread(() -> {
                    if (generation != guideSpeechGeneration) return;
                    playPauseIcon.setImageResource(android.R.drawable.ic_media_play);
                    resetGuideSpeechPosition();
                });
            }

            @Override public void onError(String id) {
                runOnUiThread(() -> {
                    if (generation != guideSpeechGeneration) return;
                    playPauseIcon.setImageResource(android.R.drawable.ic_media_play);
                    resetGuideSpeechPosition();
                });
            }

            @Override public void onRangeStart(String id, int start, int end, int frame) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && generation == guideSpeechGeneration) {
                    guideSpeechPosition = Math.min(textToSpeak.length(), startPosition + start);
                }
            }
        });
        String remainingText = textToSpeak.substring(startPosition);
        int speakResult = ttsEngine.speak(remainingText, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        playPauseIcon.setImageResource(speakResult == TextToSpeech.ERROR
                ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);
        if (speakResult == TextToSpeech.ERROR) resetGuideSpeechPosition();
    }

    private void stopGuideSpeech() {
        ImageView activeButton = pendingGuidePlayButton != null
                ? pendingGuidePlayButton : guideSpeechPlayButton;
        if (activeButton != null) {
            activeButton.setImageResource(android.R.drawable.ic_media_play);
        }
        guideSpeechGeneration++;
        pendingGuidePlayButton = null;
        pendingGuideSpeech = null;
        resetGuideSpeechPosition();
        if (ttsEngine != null) ttsEngine.stop();
    }

    private void cancelPendingGuideSpeech() {
        if (pendingGuidePlayButton != null) {
            pendingGuidePlayButton.setImageResource(android.R.drawable.ic_media_play);
        }
        pendingGuidePlayButton = null;
        pendingGuideSpeech = null;
        guideSpeechGeneration++;
        resetGuideSpeechPosition();
    }

    private void resetGuideSpeechPosition() {
        guideSpeechText = null;
        guideSpeechPosition = 0;
        guideSpeechPlayButton = null;
    }

    private String translateHowToReportTitle() {
        String[] titles = {
                "How to Report Lost & Found",
                "खोया और पाया रिपोर्ट कैसे करें",
                "हरवलेले आणि सापडलेले अहवाल कसा नोंदवावा",
                "ખોવાયેલ અને મળેલ રિપોર્ટ કેવી રીતે કરવો",
                "হারানো এবং পাওয়া রিপোর্ট কীভাবে করবেন",
                "தொலைந்தது மற்றும் கிடைத்ததை எவ்வாறு புகாரளிப்பது",
                "పోగొట్టుకున్న మరియు దొరికిన వివరాలు ఎలా రిపోర్ట్ చేయాలి",
                "ಕಳೆದುಹೋದ ಮತ್ತು ಸಿಕ್ಕ ವಸ್ತುಗಳನ್ನು ವರದಿ ಮಾಡುವುದು ಹೇಗೆ",
                "നഷ്ടപ്പെട്ടതും കണ്ടെത്തിയതും എങ്ങനെ റിപ്പോർട്ട് ചെയ്യാം"
        };
        int lang = Math.max(0, Math.min(selectedLanguage, titles.length - 1));
        return titles[lang];
    }

    private String translateHowToReportText() {
        String[] texts = {
                "1. Start a report:\n• On Home, tap LOST/THEFT for something missing or FOUND for something you found.\n• Choose a category, then complete the four steps: category, location/photo, details, and review.\n\n2. Add the details:\n• Enter the city and location, identifying details, and date. Add a photo if available; precise location is optional.\n\n3. Review and submit:\n• Check the summary before submitting. A LOST report may require payment unless you have an active subscription.\n\n4. Follow updates:\n• Open My Reports to view your report and its status.",
                "1. रिपोर्ट शुरू करें:\n• खोई वस्तु के लिए होम पर LOST/THEFT या मिली वस्तु के लिए FOUND चुनें।\n• श्रेणी चुनकर चार चरण पूरे करें: श्रेणी, स्थान/फ़ोटो, विवरण और समीक्षा।\n\n2. जानकारी भरें:\n• शहर, स्थान, पहचान के विवरण और तारीख दें। फ़ोटो उपलब्ध हो तो जोड़ें; सटीक लोकेशन वैकल्पिक है।\n\n3. समीक्षा करके जमा करें:\n• सारांश जांचें। सक्रिय सदस्यता न होने पर LOST रिपोर्ट के लिए भुगतान करना पड़ सकता है।\n\n4. स्थिति देखें:\n• अपनी रिपोर्ट और अपडेट देखने के लिए My Reports खोलें।",
                "1. हरवलेल्या वस्तूंची नोंद:\n• होम स्क्रीनवरील हिरव्या LOST बटणावर टॅप करा.\n• वस्तूचे तपशील, वर्ग, वर्णन आणि फोटो प्रविष्ट करा.\n\n2. सापडलेल्या वस्तूंची नोंद:\n• वस्तू सापडल्यावर होम स्क्रीनवरील सोनेरी FOUND बटणावर टॅप करा.\n• सापडलेल्या वस्तूचा तपशील भरा.\n\n3. अहवाल ट्रॅक करा:\n• तुमचे सादर केलेले अहवाल आणि अपडेट्स पाहण्यासाठी कधीही My Reports ला भेट द्या.",
                "1. ખોવાયેલ વસ્તુઓની જાણ કરો:\n• હોમ સ્ક્રીન પર લીલા LOST બટન પર ટેપ કરો.\n• વસ્તુની વિગતો, શ્રેણી, વર્ણન અને ફોટો દાખલ કરો.\n\n2. મળેલ વસ્તુઓની જાણ કરો:\n• વસ્તુ મળે ત્યારે હોમ સ્ક્રીન પર સોનેરી FOUND બટન પર ટેપ કરો.\n• મળેલ વસ્તુની વિગતો ભરો.\n\n3. તમારા રિપોર્ટ ટ્રેક કરો:\n• તમારા સબમિટ કરેલા રિપોર્ટ અને અપડેટ્સ જોવા માટે ગમે ત્યારે My Reports ની મુલાકાત લો.",
                "1. হারানো আইটেম রিপোর্ট করা:\n• হোম স্ক্রিনে সবুজ LOST বোতামে ট্যাপ করুন।\n• আইটেমের বিবরণ, বিভাগ, বর্ণনা এবং ছবি লিখুন।\n\n2. পাওয়া আইটেম রিপোর্ট করা:\n• কোনো আইটেম পেলে হোম স্ক্রিনে সোনালী FOUND বোতামে ট্যাপ করুন।\n• পাওয়া আইটেমের বিবরণ পূরণ করুন।\n\n3. আপনার রিপোর্ট ট্র্যাক করা:\n• আপনার জমা দেওয়া রিপোর্ট এবং আপডেট দেখতে যেকোনো সময় My Reports দেখুন।",
                "1. தொலைந்த பொருட்களை அறிக்கையிடல்:\n• முகப்புத் திரையில் உள்ள பச்சை நிற LOST பொத்தானைத் தட்டவும்.\n• பொருளின் விவரங்கள், வகை, விளக்கம் மற்றும் புகைப்படத்தை உள்ளிடவும்.\n\n2. கிடைத்த பொருட்களை அறிக்கையிடல்:\n• ஒரு பொருள் கிடைக்கும்போது முகப்புத் திரையில் உள்ள தங்க நிற FOUND பொத்தானைத் தட்டவும்.\n• கிடைத்த பொருளின் விவரங்களை நிரப்பவும்.\n\n3. உங்கள் அறிக்கைகளைக் கண்காணித்தல்:\n• சமர்ப்பிக்கப்பட்ட அறிக்கைகள் மற்றும் புதுப்பிப்புகளைப் பார்க்க எப்போதும் My Reports பகுதிக்குச் செல்லவும்.",
                "1. పోగొట్టుకున్న వస్తువులను రిపోర్ట్ చేయడం:\n• హోమ్ స్క్రీన్‌పై ఉన్న ఆకుపచ్చ LOST బటన్‌ను నొక్కండి.\n• వస్తువు వివరాలు, వర్గం, వివరణ మరియు ఫోటోను నమోదు చేయండి.\n\n2. దొరికిన వస్తువులను రిపోర్ట్ చేయడం:\n• మీకు వస్తువు దొరికినప్పుడు హోమ్ స్క్రీన్‌పై ఉన్న బంగారు FOUND బటన్‌ను నొక్కండి.\n• దొరికిన వస్తువు వివరాలను నింపండి.\n\n3. మీ రిపోర్టులను ట్రాక్ చేయడం:\n• సమర్పించిన రిపోర్టులు మరియు అప్‌డేట్‌లను చూడటానికి ఎప్పుడైనా My Reportsని సందర్శించండి.",
                "1. ಕಳೆದುಹೋದ ವಸ್ತುಗಳನ್ನು ವರದಿ ಮಾಡುವುದು:\n• ಹೋಮ್ స్క್ರೀನ್‌ನಲ್ಲಿರುವ ಹಸಿರು LOST ಬಟನ್ ಟ್ಯಾಪ್ ಮಾಡಿ.\n• ವಸ್ತುವಿನ ವಿವರಗಳು, ವರ್ಗ, ವಿವರಣೆ ಮತ್ತು ಫೋಟೋ ನಮೂದಿಸಿ.\n\n2. ಸಿಕ್ಕ ವಸ್ತುಗಳನ್ನು ವರದಿ ಮಾಡುವುದು:\n• ವಸ್ತು ಸಿಕ್ಕಾಗ ಹೋಮ್ స్క್ರೀನ್‌ನಲ್ಲಿರುವ ಚಿನ್ನದ ಬಣ್ಣದ FOUND ಬಟನ್ ಟ್ಯಾಪ್ ಮಾಡಿ.\n• ಸಿಕ್ಕ ವಸ್ತುವಿನ ವಿವರಗಳನ್ನು ಭರ್ತಿ ಮಾಡಿ.\n\n3. ನಿಮ್ಮ ವರದಿಗಳನ್ನು ಟ್ರ್ಯಾಕ್ ಮಾಡುವುದು:\n• ಸಲ್ಲಿಸಿದ ವರದಿಗಳು ಮತ್ತು ಅಪ್‌ಡೇಟ್‌ಗಳನ್ನು ವೀಕ್ಷಿಸಲು ಯಾವಾಗ ಬೇಕಾದರೂ My Reports ಗೆ ಭೇಟಿ ನೀಡಿ.",
                "1. നഷ്ടപ്പെട്ടവ റിപ്പോർട്ട് ചെയ്യൽ:\n• ഹോം സ്‌ക്രീനിലെ പച്ച LOST ബട്ടണിൽ ടാപ്പ് ചെയ്യുക.\n• ഇനത്തിന്റെ വിശദാംശങ്ങൾ, വിഭാഗം, വിവരണം, ഫോട്ടോ എന്നിവ നൽകുക.\n\n2. കണ്ടെത്തിയവ റിപ്പോർട്ട് ചെയ്യൽ:\n• ഒരു ഇനം കണ്ടെത്തുമ്പോൾ ഹോം സ്‌ക്രീനിലെ സുവർണ്ണ FOUND ബട്ടണിൽ ടാപ്പ് ചെയ്യുക.\n• കണ്ടെത്തിയ ഇനത്തിന്റെ വിവരങ്ങൾ നൽകുക.\n\n3. റിപ്പോർട്ടുകൾ ട്രാക്ക് ചെയ്യൽ:\n• സമർപ്പിച്ച റിപ്പോർട്ടുകളും അപ്‌ഡേറ്റുകളും കാണാൻ എപ്പോൾ വേണമെങ്കിലും My Reports സന്ദർശിക്കുക."
        };
        int lang = Math.max(0, Math.min(selectedLanguage, texts.length - 1));
        if (lang == 0) return texts[lang];

        String[] lostButtonLabels = {
            "LOST", "खोया", "हरवले/चोरी", "ખોવાયેલું", "হারানো",
            "தொலைந்தது", "పోగొట్టుకున్న", "ಕಳೆದುಹೋದ", "നഷ്ടപ്പെട്ട"
        };
        String[] foundButtonLabels = {
            "FOUND", "मिला", "सापडले", "મળેલ", "পাওয়া",
            "கிடைத்தது", "దొరికిన", "ಸಿಕ್ಕಿದ", "കണ്ടെത്തിയത്"
        };
        String[] myReportsLabels = {
            "My Reports", "मेरी रिपोर्टें", "माझे अहवाल", "મારા અહેવાલો", "আমার রিপোর্ট",
            "எனது அறிக்கைகள்", "నా నివేదికలు", "ನನ್ನ ವರದಿಗಳು", "എന്റെ റിപ്പോർട്ടുകൾ"
        };
        return texts[lang]
            .replace("LOST", lostButtonLabels[lang])
            .replace("FOUND", foundButtonLabels[lang])
            .replace("My Reports", myReportsLabels[lang]);
    }

    private String translateMyReportsTitle() {
        String[] titles = {
                "How to Use My Reports",
                "My Reports का उपयोग कैसे करें",
                "माझे अहवाल कसे वापरावे",
                "My Reports નો ઉપયોગ કેવી રીતે કરવો",
                "My Reports কীভাবে ব্যবহার করবেন",
                "My Reports ஐ எவ்வாறு பயன்படுத்துவது",
                "My Reports ఎలా ఉపయోగించాలి",
                "My Reports ಅನ್ನು ಹೇಗೆ ಬಳಸುವುದು",
                "My Reports എങ്ങനെ ഉപയോഗിക്കാം"
        };
        int lang = Math.max(0, Math.min(selectedLanguage, titles.length - 1));
        return titles[lang];
    }

    private String translateMyReportsText() {
        String[] texts = {
                "1. Browse reports:\n• Choose Valuables & Items, Pets & Animals, or Missing Persons / Loved Ones to filter both sections.\n• Discover active reports by city (all Indian cities by default), then scroll to Your reports for your submissions.\n\n2. Open a report:\n• Tap a report card for details. Your reports also show a live status tracker from review through publication and verification to reunion.\n\n3. Manage your reports:\n• Use Edit once within 5 hours of creating a report.",
                "1. जमा की गई रिपोर्ट देखें:\n• अपनी सभी सक्रिय खोई और मिली हुई रिपोर्ट एक ही सूची में देखें।\n\n2. स्थिति और AI मिलान ट्रैक करें:\n• रिपोर्ट की स्थिति पर नज़र रखें और किसी भी संभावित AI मिलान की जांच करें।\n\n3. रिपोर्ट संपादित करें:\n• आपकी अपलोड की गई रिपोर्ट निर्माण के 5 घंटे के भीतर केवल एक बार संपादित की जा सकती है; उसके बाद इसे संपादित नहीं किया जा सकता।",
                "1. सादर केलेले अहवाल पाहा:\n• तुमचे सर्व सक्रिय हरवलेले आणि सापडलेले अहवाल एकाच यादीत पाहा.\n\n2. स्थिती आणि जुळणी ट्रॅक करा:\n• अहवाल स्थितीवर लक्ष ठेवा आणि संभाव्य जुळण्या तपासा.\n\n3. अहवाल संपादित करा:\n• तुमचा अपलोड केलेला अहवाल तयार केल्यापासून ५ तासांच्या आत फक्त एकदाच संपादित केला जाऊ शकतो; त्यानंतर तो संपादित केला जाऊ शकत नाही.",
                "1. સબમિટ કરેલા રિપોર્ટ જુઓ:\n• તમારા બધા સક્રિય ખોવાયેલ અને મળેલ રિપોર્ટ એક જ યાદીમાં જુઓ.\n\n2. સ્ટેટસ અને AI મેચ ટ્રેક કરો:\n• રિપોર્ટ સ્ટેટસ પર નજર રાખો અને સંભવિત AI મેચ તપાસો.\n\n3. રિપોર્ટ એડિટ કરો:\n• તમારો અપલોડ કરેલો રિપોર્ટ બનાવ્યાના 5 કલાકની અંદર માત્ર એક જ વાર એડિટ કરી શકાય છે; ત્યારબાદ તેને એડિટ કરી શકાશે નહીં.",
                "1. জমা দেওয়া রিপোর্ট দেখুন:\n• একটি তালিকায় আপনার সমস্ত সক্রিয় হারানো এবং পাওয়া রিপোর্ট দেখুন।\n\n2. স্থিতি এবং AI ম্যাচ ট্র্যাক করুন:\n• রিপোর্টের স্থিতি ট্র্যাক করুন এবং যেকোনো সম্ভাব্য AI ম্যাচ দেখুন।\n\n3. রিপোর্ট সম্পাদনা করুন:\n• আপনার আপলোড করা রিপোর্ট তৈরির ৫ ঘণ্টার মধ্যে শুধুমাত্র একবার সম্পাদনা করা যেতে পারে; তারপরে এটি সম্পাদনা করা যাবে না।",
                "1. சமர்ப்பிக்கப்பட்ட அறிக்கைகளைப் பார்க்கவும்:\n• உங்கள் செயலில் உள்ள அனைத்து அறிக்கைகளையும் ஒரே பட்டியலில் பார்க்கவும்.\n\n2. நிலை மற்றும் AI பொருத்தங்களைக் கண்காணிக்கவும்:\n• அறிக்கைகளின் நிலையைக் கண்காணித்து சாத்தியமான AI பொருத்தங்களைச் சரிபார்க்கவும்.\n\n3. அறிக்கைகளைத் திருத்துதல்:\n• நீங்கள் பதிவேற்றிய அறிக்கையை உருவாக்கிய 5 மணிநேரத்திற்குள் ஒரு முறை மட்டுமே திருத்த முடியும்; அதற்குப் பிறகு திருத்த முடியாது.",
                "1. సమర్పించిన రిపోర్టులను చూడండి:\n• మీ క్రియాశీల రిపోర్టులన్నింటినీ ఒకే జాబితాలో చూడండి.\n\n2. స్థితి మరియు AI మ్యాచ్‌లను ట్రాక్ చేయండి:\n• రిపోర్టుల స్థితిని పర్యవేక్షించండి మరియు ఏవైనా AI మ్యాచ్‌లను తనిఖీ చేయండి.\n\n3. రిపోర్టులను సవరించడం:\n• మీరు అప్‌లోడ్ చేసిన రిపోర్ట్‌ను సృష్టించిన 5 గంటలలోపు ఒకసారి మాత్రమే సవరించవచ్చు; ఆ తర్వాత సవరించలేరు.",
                "1. ಸಲ್ಲಿಸಿದ ವರದಿಗಳನ್ನು ವೀಕ್ಷಿಸಿ:\n• ನಿಮ್ಮ ಎಲ್ಲಾ ಸಕ್ರಿಯ ವರದಿಗಳನ್ನು ಒಂದೇ ಪಟ್ಟಿಯಲ್ಲಿ ಪರಿಶೀಲಿಸಿ.\n\n2. ಸ್ಥಿತಿ ಮತ್ತು AI ಪಂದ್ಯಗಳನ್ನು ಟ್ರ್ಯಾಕ್ ಮಾಡಿ:\n• ವರದಿಗಳ ಸ್ಥಿತಿಯನ್ನು ಮೇಲ್ವಿಚಾರಣೆ ಮಾಡಿ ಮತ್ತು ಸಂಭಾವ್ಯ AI ಪಂದ್ಯಗಳನ್ನು ಪರಿಶೀಲಿಸಿ.\n\n3. ವರದಿಗಳನ್ನು ಸಂಪಾದಿಸುವುದು:\n• ನೀವು ಅಪ್‌ಲೋಡ್ ಮಾಡಿದ ವರದಿಯನ್ನು ರಚಿಸಿದ 5 ಗಂಟೆಗಳ ಒಳಗೆ ಒಮ್ಮೆ ಮಾತ್ರ ಸಂಪಾದಿಸಬಹುದು; ಅದರ ನಂತರ ಅದನ್ನು ಸಂಪಾದಿಸಲು ಸಾಧ್ಯವಿಲ್ಲ.",
                "1. സമർപ്പിച്ച റിപ്പോർട്ടുകൾ കാണുക:\n• നിങ്ങളുടെ എല്ലാ റിപ്പോർട്ടുകളും ഒറ്റ ലിസ്റ്റിൽ കാണുക.\n\n2. സ്റ്റാറ്റസും AI മാച്ചുകളും ട്രാക്ക് ചെയ്യുക:\n• റിപ്പോർട്ട് സ്റ്റാറ്റസ് നിരീക്ഷിക്കുകയും സാധ്യമായ AI മാച്ചുകൾ പരിശോധിക്കുകയും ചെയ്യുക.\n\n3. റിപ്പോർട്ടുകൾ എഡിറ്റ് ചെയ്യൽ:\n• നിങ്ങൾ അപ്‌ലോഡ് ചെയ്ത റിപ്പോർട്ട് സൃഷ്ടിച്ച് 5 മണിക്കൂറിനുള്ളിൽ ഒരു തവണ മാത്രമേ എഡിറ്റ് ചെയ്യാൻ സാധിക്കൂ; അതിനുശേഷം എഡിറ്റ് ചെയ്യാൻ കഴിയില്ല."
        };
        int lang = Math.max(0, Math.min(selectedLanguage, texts.length - 1));
        String[] completedReportsNotes = {
                "Completed reports stay collapsed. Tap the section heading to expand or collapse the list.",
                "पूरी हो चुकी रिपोर्टें बंद रहती हैं। सूची देखने या छिपाने के लिए पूरी रिपोर्टें शीर्षक पर टैप करें।",
                "पूर्ण झालेले अहवाल बंद राहतात. यादी उघडण्यासाठी किंवा बंद करण्यासाठी पूर्ण झालेले अहवाल या शीर्षकावर टॅप करा.",
                "પૂર્ણ થયેલા રિપોર્ટ બંધ રહે છે. યાદી ખોલવા અથવા બંધ કરવા પૂર્ણ થયેલા રિપોર્ટના શીર્ષક પર ટેપ કરો.",
                "সম্পন্ন রিপোর্টগুলো বন্ধ থাকে। তালিকা খুলতে বা বন্ধ করতে সম্পন্ন রিপোর্ট শিরোনামে ট্যাপ করুন।",
                "நிறைவு செய்யப்பட்ட அறிக்கைகள் சுருக்கப்பட்டிருக்கும். பட்டியலைத் திறக்க அல்லது மூட நிறைவு செய்யப்பட்ட அறிக்கைகள் தலைப்பைத் தட்டவும்.",
                "పూర్తయిన రిపోర్టులు మూసి ఉంటాయి. జాబితాను తెరవడానికి లేదా మూసివేయడానికి పూర్తయిన రిపోర్టుల శీర్షికను నొక్కండి.",
                "ಪೂರ್ಣಗೊಂಡ ವರದಿಗಳು ಮುಚ್ಚಿರುತ್ತವೆ. ಪಟ್ಟಿಯನ್ನು ತೆರೆಯಲು ಅಥವಾ ಮುಚ್ಚಲು ಪೂರ್ಣಗೊಂಡ ವರದಿಗಳ ಶೀರ್ಷಿಕೆಯನ್ನು ಟ್ಯಾಪ್ ಮಾಡಿ.",
                "പൂർത്തിയായ റിപ്പോർട്ടുകൾ അടച്ചിരിക്കും. പട്ടിക തുറക്കാനോ അടയ്ക്കാനോ പൂർത്തിയായ റിപ്പോർട്ടുകൾ എന്ന തലക്കെട്ടിൽ ടാപ്പ് ചെയ്യുക."
        };
        return texts[lang].replace("AI ", "").replace("AI", "")
                + "\n\n" + completedReportsNotes[lang];
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Don't restore profile drafts when there's no current Firebase user,
        // as this would re-populate old email/phone from a previous session.
        // Only restore if user is authenticated.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                    .getInt("selected_language_index", 0);
            restoreProfileDrafts();
            hydrateProfileFromBackend(null);
            hydrateCloudProfile(null);
            checkAndReloadUserVerification();
        } else {
            // User is not logged in - ensure drafts are fresh
            selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                    .getInt("selected_language_index", 0);
            // Clear any stale draft data since user is not authenticated
            draftFullName = "";
            draftEmail = "";
            draftMobile = "";
            draftPin = "";
            draftState = "";
            draftCity = "";
            SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
            account.edit()
                    .remove("draft_full_name")
                    .remove("draft_email")
                    .remove("draft_mobile")
                    .remove("draft_pin")
                    .remove("draft_state")
                    .remove("draft_city")
                    .apply();
        }
        if (socialAuthorizationPending && adminSocialPageOpen && currentPage == PAGE_ADMIN) {
            socialAuthorizationPending = false;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (adminSocialPageOpen && currentPage == PAGE_ADMIN) {
                    showAdminSocialPublishingPage();
                }
            }, 500);
        }
        if (currentPage == PAGE_PROFILE) {
            if (visibleFirstName != null || visibleAvatar != null || visibleEmail != null) {
                refreshProfileViewInPlace(false);
            } else {
                showProfile();
            }
            return;
        }
        if (currentPage == PAGE_PROFILE_SETUP && screenRenderer != null) {
            screenRenderer.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopActiveLocationUpdates();
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
        outState.putString("state_draft_city_tag", draftCityTag);
        outState.putString("state_draft_date", draftDate);
        outState.putString("state_draft_identifier", draftIdentifier);
        outState.putString("state_draft_report_category", draftReportCategory);
        outState.putInt("state_report_wizard_step", reportWizardStep);
        outState.putString("state_draft_imei", draftImei);
        outState.putBoolean("state_draft_social_share_consent", draftSocialShareConsent);
        outState.putBoolean("state_draft_guidelines_accepted", draftGuidelinesAccepted);
        outState.putString("state_draft_full_name", draftFullName);
        outState.putString("state_draft_email", draftEmail);
        outState.putString("state_draft_mobile", draftMobile);
        outState.putString("state_draft_pin", draftPin);
        outState.putString("state_draft_state", draftState);
        outState.putString("state_draft_city", draftCity);
        outState.putBoolean("state_profile_setup_visible", profileSetupVisible);
        outState.putInt("state_profile_scroll_y", profileScrollY);
        outState.putBoolean("state_in_renewal_payment_flow", inRenewalPaymentFlow);
    }

    private void restoreScreenState() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        if (FirebaseAuth.getInstance().getCurrentUser() != null
                && account.getBoolean("pin_setup_pending", false)) {
            showProfileSetup();
            return;
        }
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

    private void handleAppBack() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null
                && getSharedPreferences("fendly_account", MODE_PRIVATE).getBoolean("pin_setup_pending", false)) {
            showProfileSetup();
            return;
        }
        if (currentPage == PAGE_ADMIN_SUBSCRIPTIONS) {
            showAdminDashboard();
            return;
        }
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
        if (currentPage == PAGE_REPORT && reportWizardStep > 1) {
            reportWizardStep--;
            showReport(currentReportType == null ? "LOST" : currentReportType);
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
        if (currentPage == PAGE_AUTH) {
            showLogoutConfirmationDialog();
            return;
        }
        showLogoutConfirmationDialog();
    }

    private void showLogoutConfirmationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_lock,
            LanguageManager.profileText(this, "logout"),
            LanguageManager.profileText(this, "logout_confirm")
        );
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        TextView no = filledButton(LanguageManager.profileText(this, "no"), LOST_GREEN, LOST_GREEN_ON);
        no.setOnClickListener(view -> dialog.dismiss());
        LinearLayout.LayoutParams noParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        actions.addView(no, noParams);

        TextView yes = filledButton(LanguageManager.profileText(this, "yes"), FOUND_GOLD, FOUND_GOLD_ON);
        LinearLayout.LayoutParams yesParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        yesParams.setMargins(dp(12), 0, 0, 0);
        actions.addView(yes, yesParams);

        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        yes.setOnClickListener(view -> {
            dialog.dismiss();
            clearAllLocalAccountData();
            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception ignored) {}
            Toast.makeText(MainActivity.this, translate("Signed out successfully"), Toast.LENGTH_SHORT).show();
            finishAffinity();
        });

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void confirmDeleteReport(JSONObject report, Dialog detailsDialog) {
        Dialog confirmDialog = new Dialog(this);
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_lock,
                LanguageManager.profileText(this, "delete_report_title"),
                LanguageManager.profileText(this, "delete_report_body")
        );
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        TextView cancel = filledButton(
                LanguageManager.profileText(this, "cancel"),
                LOST_GREEN,
                LOST_GREEN_ON
        );
        cancel.setOnClickListener(view -> confirmDialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(44), 1f));

        TextView delete = filledButton(
                LanguageManager.profileText(this, "delete_report"),
                Color.rgb(180, 45, 45),
                Color.WHITE
        );
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        deleteParams.setMargins(dp(12), 0, 0, 0);
        actions.addView(delete, deleteParams);
        delete.setOnClickListener(view -> {
            confirmDialog.dismiss();
            deleteReport(report, detailsDialog);
        });

        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        confirmDialog.setContentView(content);
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.show();
        sizeThemedDialog(confirmDialog);
    }

    private void deleteReport(JSONObject report, Dialog detailsDialog) {
        String reportId = report.optString("id", "").trim();
        String reportType = report.optString("type", "").trim().toLowerCase(Locale.ROOT);
        if (reportId.isEmpty() || !(reportType.equals("lost") || reportType.equals("found"))) {
            Toast.makeText(this, LanguageManager.profileText(this, "report_delete_failed"), Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, LanguageManager.profileText(this, "delete_sign_in_required"), Toast.LENGTH_LONG).show();
            return;
        }
        user.getIdToken(false).addOnSuccessListener(tokenResult -> network.execute(() -> {
            HttpURLConnection connection = null;
            int responseCode = -1;
            try {
                connection = (HttpURLConnection) new URL(
                        API_BASE + "/api/items/" + reportType + "/" + Uri.encode(reportId)
                ).openConnection();
                connection.setRequestMethod("DELETE");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Authorization", "Bearer " + tokenResult.getToken());
                responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    runOnUiThread(() -> {
                        detailsDialog.dismiss();
                        Toast.makeText(this, LanguageManager.profileText(this, "report_deleted"), Toast.LENGTH_LONG).show();
                        showReports();
                    });
                    return;
                }
                String errorBody = readStream(connection.getErrorStream());
                Log.w("REPORT_DELETE", "Delete failed with HTTP " + responseCode + ": " + errorBody);
            } catch (Exception error) {
                Log.e("REPORT_DELETE", "Could not delete report " + reportId, error);
            } finally {
                if (connection != null) connection.disconnect();
            }
            runOnUiThread(() -> Toast.makeText(
                    this,
                    LanguageManager.profileText(this, "report_delete_failed"),
                    Toast.LENGTH_LONG
            ).show());
        })).addOnFailureListener(error -> {
            Log.e("REPORT_DELETE", "Could not authenticate report deletion", error);
            Toast.makeText(this, LanguageManager.profileText(this, "report_delete_failed"), Toast.LENGTH_LONG).show();
        });
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_lock,
                LanguageManager.profileText(this, "delete_account_title"),
                LanguageManager.profileText(this, "delete_account_body")
        );
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        TextView cancel = filledButton(LanguageManager.profileText(this, "cancel"), LOST_GREEN, LOST_GREEN_ON);
        cancel.setOnClickListener(view -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(44), 1f));

        TextView delete = filledButton(LanguageManager.profileText(this, "delete_account"), Color.rgb(180, 45, 45), Color.WHITE);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        deleteParams.setMargins(dp(12), 0, 0, 0);
        actions.addView(delete, deleteParams);
        delete.setOnClickListener(view -> deleteAccount(dialog, delete));

        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void deleteAccount(Dialog dialog, TextView deleteButton) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, LanguageManager.profileText(this, "delete_sign_in_required"), Toast.LENGTH_LONG).show();
            return;
        }
        deleteButton.setEnabled(false);
        deleteButton.setText(LanguageManager.profileText(this, "deleting_account"));
        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            HttpURLConnection connection = null;
            int responseCode = -1;
            try {
                connection = (HttpURLConnection) new URL(API_BASE + "/api/users/account").openConnection();
                connection.setRequestMethod("DELETE");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);
                connection.setRequestProperty("Authorization", "Bearer " + token.getToken());
                responseCode = connection.getResponseCode();
            } catch (Exception error) {
                Log.e("ACCOUNT_DELETE", "Account deletion request failed", error);
            } finally {
                if (connection != null) connection.disconnect();
            }
            final int finalResponseCode = responseCode;
            runOnUiThread(() -> {
                if (finalResponseCode == 204) {
                    dialog.dismiss();
                    clearAllLocalAccountData();
                    try {
                        FirebaseAuth.getInstance().signOut();
                    } catch (Exception ignored) {}
                    Toast.makeText(MainActivity.this, LanguageManager.profileText(MainActivity.this, "account_deleted"), Toast.LENGTH_LONG).show();
                    buildScreen();
                } else {
                    deleteButton.setEnabled(true);
                    deleteButton.setText(LanguageManager.profileText(MainActivity.this, "delete_account"));
                    Toast.makeText(this, LanguageManager.profileText(this, "account_delete_failed"), Toast.LENGTH_LONG).show();
                }
            });
        })).addOnFailureListener(error -> {
            deleteButton.setEnabled(true);
            deleteButton.setText(LanguageManager.profileText(MainActivity.this, "delete_account"));
            Toast.makeText(this, LanguageManager.profileText(this, "account_delete_auth_failed"), Toast.LENGTH_LONG).show();
        });
    }

    private int responsiveHorizontalPadding() {
        int widthDp = WindowInsetsHelper.windowWidthDp(this);
        int maxContentWidthDp = widthDp >= 600 ? 720 : 520;
        return widthDp > maxContentWidthDp
                ? dp((widthDp - maxContentWidthDp) / 2)
                : dp(18);
    }

    private void scrollAuthFieldIntoView(ScrollView scroll, View field) {
        scroll.post(() -> {
            if (!field.isFocused() || scroll.getHeight() == 0) return;

            Rect fieldBounds = new Rect(0, 0, field.getWidth(), field.getHeight());
            scroll.offsetDescendantRectToMyCoords(field, fieldBounds);
            int visibleTop = scroll.getScrollY();
            int visibleBottom = visibleTop + scroll.getHeight();
            if (fieldBounds.bottom > visibleBottom) {
                scroll.smoothScrollBy(0, fieldBounds.bottom - visibleBottom + dp(20));
            } else if (fieldBounds.top < visibleTop) {
                scroll.smoothScrollBy(0, fieldBounds.top - visibleTop - dp(20));
            }
        });
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
        WindowInsetsHelper.applySafeArea(root);

        addAppControls(root, true);

        LinearLayout centerArea = new LinearLayout(this);
        centerArea.setOrientation(LinearLayout.VERTICAL);
        centerArea.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        if (Build.VERSION.SDK_INT >= 29) centerArea.setForceDarkAllowed(false);
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        ScrollView authScroll = new ScrollView(this);
        authScroll.setFillViewport(true);
        authScroll.addView(centerArea, new ScrollView.LayoutParams(-1, -2));
        authScroll.addOnLayoutChangeListener((view, left, top, right, bottom,
                                               oldLeft, oldTop, oldRight, oldBottom) -> {
            View focused = authScroll.findFocus();
            if (focused != null) scrollAuthFieldIntoView(authScroll, focused);
        });
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

        FrameLayout emblemWrap = new FrameLayout(this);
        emblemWrap.setPadding(dp(10), 0, dp(10), 0);
        emblemWrap.setBackground(roundWithStroke(Color.argb(30, 232, 178, 74), 22, Color.argb(80, 232, 178, 74)));
        LinearLayout emblemContent = new LinearLayout(this);
        emblemContent.setOrientation(LinearLayout.VERTICAL);
        emblemContent.setGravity(Gravity.CENTER_HORIZONTAL);
        emblemWrap.addView(emblemContent, new FrameLayout.LayoutParams(-1, -1));

        TextView authTaglineTop = text(
                LanguageManager.profileText(this, "home_tagline_top"),
                16,
                primaryTextColor(),
                Typeface.BOLD
        );
        authTaglineTop.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        authTaglineTop.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        authTaglineTop.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        authTaglineTop.setIncludeFontPadding(false);
        authTaglineTop.setMaxLines(2);
        authTaglineTop.setTextSize(responsiveTextSize(16));
        emblemContent.addView(authTaglineTop, new LinearLayout.LayoutParams(-1, dp(48)));

        ImageView emblem = new ImageView(this);
        emblem.setImageResource(R.drawable.logo_final);
        emblem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        emblem.setAdjustViewBounds(true);
        emblem.setContentDescription("Fendly emblem. Long press for administrator sign-in.");
        emblem.setOnLongClickListener(view -> {
            showAdminLoginDialog();
            return true;
        });
        LinearLayout.LayoutParams emblemParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        emblem.setMinimumHeight(dp(88));
        emblemContent.addView(emblem, emblemParams);

        TextView authTaglineBottom = text(
                LanguageManager.profileText(this, "home_tagline_bottom"),
                16,
                primaryTextColor(),
                Typeface.BOLD
        );
        authTaglineBottom.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        authTaglineBottom.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        authTaglineBottom.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        authTaglineBottom.setIncludeFontPadding(false);
        authTaglineBottom.setMaxLines(2);
        authTaglineBottom.setTextSize(responsiveTextSize(16));
        emblemContent.addView(authTaglineBottom, new LinearLayout.LayoutParams(-1, dp(48)));

        LinearLayout.LayoutParams emblemWrapParams = new LinearLayout.LayoutParams(-1, dp(200));
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
        View.OnFocusChangeListener revealAuthField = (view, hasFocus) -> {
            if (hasFocus) {
                scrollAuthFieldIntoView(authScroll, view);
                authScroll.postDelayed(() -> scrollAuthFieldIntoView(authScroll, view), 250);
            }
        };
        username.setOnFocusChangeListener(revealAuthField);
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
        for (EditText pinCell : pinCells) {
            pinCell.setOnFocusChangeListener(revealAuthField);
        }
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
                        handleSuccessfulLogin(name, code, () -> {
                            login.setText(loginText("login"));
                            login.setEnabled(true);
                            showHome();
                        });
                    })
                    .addOnFailureListener(error -> {
                        login.setText(loginText("login"));
                        login.setEnabled(true);
                        Toast.makeText(
                                this,
                                firebaseLoginFailureMessage(error),
                                Toast.LENGTH_LONG
                        ).show();
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

        TextView createAccount = text(loginText("create"), 12, primaryTextColor(), Typeface.NORMAL);
        createAccount.setGravity(Gravity.CENTER);
        createAccount.setIncludeFontPadding(true);
        createAccount.setMaxLines(2);
        createAccount.setEllipsize(null);
        createAccount.setPadding(dp(4), dp(6), dp(4), dp(6));
        if (Build.VERSION.SDK_INT >= 29) createAccount.setForceDarkAllowed(false);
        createAccount.setTextColor(primaryTextColor());
        createAccount.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        username.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                username.setError(null);
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        createAccount.setOnClickListener(view -> {
            String name = username.getText().toString().trim();
            if (!name.matches("^[a-zA-Z0-9_]{3,32}$")) {
                username.setError("Enter a valid username (3-32 letters, numbers, or underscores)");
                username.requestFocus();
                return;
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            createAccount.setEnabled(false);
            createAccount.setText(translate("Creating account..."));
            checkRegistrationUsername(name, (available, checked) -> {
                String currentName = username.getText().toString().trim();
                if (!name.equalsIgnoreCase(currentName)) {
                    createAccount.setEnabled(true);
                    createAccount.setText(loginText("create"));
                    return;
                }
                if (checked && !available) {
                    createAccount.setEnabled(true);
                    createAccount.setText(loginText("create"));
                    username.setError("User already exists");
                    username.requestFocus();
                    return;
                }
                if (!checked) {
                    createAccount.setEnabled(true);
                    createAccount.setText(loginText("create"));
                    username.setError("Could not check username. Please try again.");
                    username.requestFocus();
                    return;
                }
                resetFreshAccountDrafts();
                auth.signOut();
                profileHydrated = false;
                String initialPassword = generateTemporaryFirebasePassword();
                auth.createUserWithEmailAndPassword(credentialEmail(name), initialPassword)
                    .addOnSuccessListener(result -> {
                        clearAllLocalAccountData();
                        accountCreated = true;
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                .putBoolean("created", true)
                                .putBoolean("pin_setup_pending", true)
                                .putString("username", name)
                                .apply();
                        FcmRegistration.registerCurrentToken();
                        showProfileSetup();
                    })
                    .addOnFailureListener(error -> {
                        createAccount.setEnabled(true);
                        createAccount.setText(loginText("create"));
                        String errorCode = error instanceof FirebaseAuthException
                                ? ((FirebaseAuthException) error).getErrorCode()
                                : error.getClass().getSimpleName();
                        String diagnostic = error.getMessage();
                        if (diagnostic != null) {
                            java.util.regex.Matcher firebaseCode =
                                    java.util.regex.Pattern.compile("\\[\\s*([A-Z0-9_]+)\\s*\\]")
                                            .matcher(diagnostic);
                            if (firebaseCode.find()
                                    && (!(error instanceof FirebaseAuthException)
                                    || "ERROR_INTERNAL_ERROR".equalsIgnoreCase(errorCode))) {
                                errorCode = firebaseCode.group(1);
                            }
                        }
                        Log.w("AUTH", "Firebase account creation failed: " + errorCode
                                + (diagnostic == null ? "" : " - " + diagnostic));
                        if (isUsernameAlreadyTaken(error)) {
                            username.setError("User already exists");
                            username.requestFocus();
                        } else {
                            username.setError(accountCreationErrorMessage(errorCode));
                            username.requestFocus();
                        }
                    });
            });
        });
        authCard.addView(createAccount, new LinearLayout.LayoutParams(-1, dp(58)));

        setContentView(root);
        root.post(() -> {
            if (currentPage == PAGE_AUTH) fetchUserNotificationCount();
        });
        root.postDelayed(() -> {
            int authTextColor = authTextColor();
            username.setTextColor(authTextColor);
            for (EditText pinCell : pinCells) pinCell.setTextColor(authTextColor);
            createAccount.setTextColor(authTextColor);
        }, 150);
    }

    private boolean isUsernameAlreadyTaken(Exception error) {
        if (error instanceof FirebaseAuthUserCollisionException) return true;
        if (error instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) error).getErrorCode();
            if ("ERROR_EMAIL_ALREADY_IN_USE".equalsIgnoreCase(errorCode)
                    || "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL".equalsIgnoreCase(errorCode)) {
                return true;
            }
        }
        String message = error == null ? "" : String.valueOf(error.getMessage()).toLowerCase(Locale.US);
        return message.contains("email-already-in-use")
                || message.contains("email_exists")
                || message.contains("email exists")
                || message.contains("account-exists-with-different-credential")
                || message.contains("credential already in use")
                || message.contains("email address is already in use")
                || message.contains("already in use");
    }

    private String accountCreationErrorMessage(String errorCode) {
        if ("ERROR_OPERATION_NOT_ALLOWED".equalsIgnoreCase(errorCode)) {
            return "Account creation is temporarily unavailable. Please try again later.";
        }
        if ("CONFIGURATION_NOT_FOUND".equalsIgnoreCase(errorCode)) {
            return "Account creation is temporarily unavailable. Please try again later.";
        }
        if ("ERROR_NETWORK_REQUEST_FAILED".equalsIgnoreCase(errorCode)) {
            return "Network error. Check your connection and try again.";
        }
        if ("ERROR_TOO_MANY_REQUESTS".equalsIgnoreCase(errorCode)) {
            return "Too many attempts. Please wait and try again.";
        }
        if ("ERROR_INVALID_EMAIL".equalsIgnoreCase(errorCode)) {
            return "Check your username and try again.";
        }
        if ("ERROR_APP_NOT_AUTHORIZED".equalsIgnoreCase(errorCode)
                || "ERROR_INVALID_API_KEY".equalsIgnoreCase(errorCode)) {
            return "Account creation is temporarily unavailable. Please try again later.";
        }
        return "Could not create your account right now. Please try again.";
    }

    private interface UsernameAvailabilityCallback {
        void onResult(boolean available, boolean checked);
    }

    private void checkRegistrationUsername(String username, UsernameAvailabilityCallback callback) {
        network.execute(() -> {
            HttpURLConnection connection = null;
            boolean available = false;
            boolean checked = false;
            try {
                String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
                connection = (HttpURLConnection) new URL(
                        API_BASE + "/api/users/username-availability/" + encoded).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                    String response = readStream(connection.getInputStream());
                    if (response != null) {
                        available = new JSONObject(response).optBoolean("available", false);
                        checked = true;
                    }
                }
            } catch (Exception error) {
                Log.w("AUTH", "Could not check username availability before registration", error);
            } finally {
                if (connection != null) connection.disconnect();
            }
            boolean finalAvailable = available;
            boolean finalChecked = checked;
            runOnUiThread(() -> callback.onResult(finalAvailable, finalChecked));
        });
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

    private void clearAllLocalAccountData() {
        clearProfileDrafts();
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit().clear().apply();
        cloudProfileLoaded = false;
        profileHydrated = false;
        selectedProfileImage = null;
        capturedProfileImage = null;
        profileImageExplicitlyRemoved = false;
        visibleFirstName = null;
        visibleSurname = null;
        visibleEmail = null;
        visibleStateSearch = null;
        visibleCitySearch = null;
        visibleMobileVerify = null;
        visibleMobileCells = null;
        visibleAvatar = null;
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
        boolean emailVerified = account.getBoolean("email_verified", false);
        boolean mobileVerified = account.getBoolean("mobile_verified", false);

        draftFullName = account.getString("draft_full_name", draftFullName);
        if (!emailVerified) {
            draftEmail = account.getString("draft_email", draftEmail);
        } else {
            draftEmail = "";
            account.edit().remove("draft_email").apply();
        }
        if (!mobileVerified) {
            draftMobile = account.getString("draft_mobile", draftMobile);
        } else {
            draftMobile = "";
            account.edit().remove("draft_mobile").apply();
        }
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
                .remove("draft_full_name")
                .remove("draft_email")
                .remove("draft_mobile")
                .remove("draft_pin")
                .remove("draft_state")
                .remove("draft_city")
                .apply();
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
        avatarWrap.setClipToOutline(true);

        ImageView avatar = new ImageView(this);
        avatar.setBackground(roundWithStroke(surfaceColor(), 60, fieldBorderColor()));
        avatar.setLayoutParams(new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER));
        avatar.setImageResource(R.drawable.ic_field_person);
        avatar.setClipToOutline(true);

        bindProfilePhoto(avatar, account);

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
        String savedFirstName = account.getString("profile_first_name", "").trim();
        if (savedFirstName.isEmpty() && nameParts.length > 0) savedFirstName = nameParts[0];
        String savedSurname = account.getString("profile_surname", "").trim();
        if (savedSurname.isEmpty() && nameParts.length > 1) savedSurname = nameParts[1];
        firstName.setText(localizeProfileName(savedFirstName));
        surname.setText(localizeProfileName(savedSurname));
        visibleFirstName = firstName;
        visibleSurname = surname;
        visibleNameDirty = false;
        firstName.addTextChangedListener(draftWatcher(value -> {
            if (applyingCloudProfile) return;
            String fn = getCanonicalEnglishName(value);
            String sn = getCanonicalEnglishName(surname.getText().toString().trim());
            draftFullName = (fn + " " + sn).trim();
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .putString("profile_first_name", fn)
                    .putString("profile_surname", sn)
                    .apply();
            visibleNameDirty = true;
            saveProfileDrafts();
        }));
        surname.addTextChangedListener(draftWatcher(value -> {
            if (applyingCloudProfile) return;
            String fn = getCanonicalEnglishName(firstName.getText().toString().trim());
            String sn = getCanonicalEnglishName(value);
            draftFullName = (fn + " " + sn).trim();
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .putString("profile_first_name", fn)
                    .putString("profile_surname", sn)
                    .apply();
            visibleNameDirty = true;
            saveProfileDrafts();
        }));

        EditText email = field(getString(R.string.profile_email_address));
        EditText[] mobileCells = mobileCells();
        visibleEmail = email;
        visibleMobileCells = mobileCells;
        String profileEmail = account.getString("email", "");
        if (profileEmail.endsWith("@login.fendly.app")) profileEmail = "";
        email.setText(profileEmail);
        String savedMobileValue = normalizeLocalizedDigits(account.getString("mobile", ""));
        draftMobile = savedMobileValue;
        setMobileCells(mobileCells, savedMobileValue);

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
        for (EditText cell : mobileCells) {
            cell.addTextChangedListener(draftWatcher(value -> {
                draftMobile = normalizeLocalizedDigits(mobileValue(mobileCells));
                saveProfileDrafts();
            }));
        }
        for (EditText pinCell : pinCells) {
            pinCell.addTextChangedListener(draftWatcher(value -> {
                draftPin = pinValue(pinCells);
                saveProfileDrafts();
            }));
        }

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

        EditText mobileField = mobileCells[0];
        mobileField.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String mobileValue = normalizeLocalizedDigits(mobileField.getText().toString().trim());
                if (!mobileValue.isEmpty() && !mobileValue.matches("^\\d{10}$")) {
                    mobileField.setError("Mobile number must be 10 digits");
                } else {
                    mobileField.setError(null);
                }
            }
        });

        EditText usernameField = field(getString(R.string.profile_username_login));
        usernameField.setText(localizeProfileDisplayValue("username", account.getString("username", "")));
        usernameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        usernameField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});

        TextView usernameStatus = text("", 10, secondaryTextColor(), Typeface.NORMAL);
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
                String candidate = editable == null ? "" : editable.toString().trim();
                if (candidate.isEmpty()) {
                    usernameStatus.setVisibility(View.INVISIBLE);
                    usernameStatus.setText("");
                    return;
                }
                if (!candidate.matches("^[a-zA-Z0-9_]{3,32}$")) {
                    usernameStatus.setVisibility(View.VISIBLE);
                    usernameStatus.setText(translate("Invalid"));
                    usernameStatus.setTextColor(Color.RED);
                    return;
                }
                usernameStatus.setVisibility(View.VISIBLE);
                usernameStatus.setText(translate("Checking..."));
                usernameStatus.setTextColor(Color.rgb(201, 162, 76));
                checkUsernameAvailability(candidate.toLowerCase(Locale.US), usernameField, usernameStatus);
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
        visibleMobileVerify = mobileVerify;
        mobileVerify.setPadding(dp(10), 0, dp(10), 0);
        mobileVerify.setOnClickListener(view -> verifyProfileMobileTarget(mobileCells, mobileVerify));
        String verifiedMobile = account.getString("mobile", "").trim();
        for (EditText cell : mobileCells) {
            cell.setEnabled(true);
            cell.setFocusable(true);
            cell.setFocusableInTouchMode(true);
            cell.setCursorVisible(true);
        }
        setMobileCells(mobileCells, verifiedMobile);
        String currentMobile = normalizeLocalizedDigits(mobileValue(mobileCells));
        boolean isMobileVerified = isMobileVerifiedFor(currentMobile, account);
        if (isMobileVerified && !verifiedMobile.isEmpty() && verifiedMobile.equals(currentMobile)) {
            mobileVerify.setOnClickListener(null);
            lockVerifiedMobileField(mobileCells, mobileVerify);
        }
        for (EditText cell : mobileCells) {
            cell.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                    String currentMobile = normalizeLocalizedDigits(mobileValue(mobileCells));
                    boolean verifiedNow = currentMobile.matches("^\\d{10}$")
                        && isMobileVerifiedFor(currentMobile, getSharedPreferences("fendly_account", MODE_PRIVATE));
                    if (verifiedNow) {
                        mobileVerify.setOnClickListener(null);
                        lockVerifiedMobileField(mobileCells, mobileVerify);
                        return;
                    }
                    if (!verifiedMobile.equals(currentMobile)) {
                        mobileVerify.setOnClickListener(view -> verifyProfileMobileTarget(mobileCells, mobileVerify));
                        mobileVerify.setText(translate("Verify OTP"));
                        mobileVerify.setEnabled(true);
                        mobileVerify.setClickable(true);
                        mobileVerify.setFocusable(true);
                        mobileVerify.setBackground(round(GOLD, 24));
                        mobileVerify.setTextColor(GOLD_ON);
                        for (EditText c : mobileCells) {
                            c.setEnabled(true);
                            c.setFocusable(true);
                            c.setFocusableInTouchMode(true);
                            c.setCursorVisible(true);
                        }
                    }
                }
                @Override public void afterTextChanged(Editable value) { }
            });
        }

        Map<String, String[]> stateCities = indiaStateCityMap();
        String[] states = stateCities.keySet().toArray(new String[0]);
        Arrays.sort(states, 1, states.length);
        AutoCompleteTextView stateSearch = new AutoCompleteTextView(this);
        AutoCompleteTextView citySearch = new AutoCompleteTextView(this);
        visibleStateSearch = stateSearch;
        visibleCitySearch = citySearch;
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
        addLabeledMobileField(root, LanguageManager.profileText(this, "profile_mobile"), mobileCells, mobileVerify);
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
            String usernameValue = usernameField.getText().toString().trim();
            String mobileValue = normalizeLocalizedDigits(mobileValue(mobileCells));
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
            } else if (!usernameValue.matches("^[a-zA-Z0-9_]{3,32}$")) {
                usernameField.setError("Use 3-32 letters, numbers, or underscores");
                usernameField.requestFocus();
            } else if (!mobileValue.matches("^\\d{10}$")) {
                Toast.makeText(this, "Enter valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
                mobileCells[0].requestFocus();
            } else if (!emailValue.isEmpty() && !validEmail(emailValue)) {
                email.setError("Enter a valid email address or leave this field blank");
                email.requestFocus();
            } else if (!pinValue.matches("^\\d{4}$")) {
                Toast.makeText(this, "Create a valid 4-digit PIN", Toast.LENGTH_LONG).show();
                pinCells[0].requestFocus();
            } else if (selectedState == null || selectedState.trim().isEmpty() || isSelectStatePlaceholder(selectedState) || selectedCity.isEmpty() || isSelectCityPlaceholder(selectedCity) || !cityMatchesState) {
                Toast.makeText(this, "Please select your state and city", Toast.LENGTH_LONG).show();
                stateSearch.requestFocus();
            } else {
                boolean currentMobileVerified = isMobileVerifiedFor(mobileValue, account);
                if (REQUIRE_MOBILE_OTP_FOR_PROFILE_SAVE && !currentMobileVerified) {
                    Toast.makeText(this, "Verify mobile OTP before continuing", Toast.LENGTH_SHORT).show();
                    blinkVerificationRequired(mobileVerify);
                    mobileCells[0].requestFocus();
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
                        .putBoolean("mobile_verified", currentMobileVerified)
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
        phoneVerificationMobile = normalizeIndianMobileDigits(mobile);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", phoneVerificationMobile);
                JSONObject response = postJson("/api/auth/send-otp", payload.toString(), null);
                boolean sent = response != null && response.optBoolean("success", false);
                if (!sent) {
                    String failureMessage = response == null
                            ? translate("Could not send OTP")
                            : response.optString("detail", response.optString("message", translate("Could not send OTP")));
                    runOnUiThread(() -> {
                        save.setText(translate("SMS verification unavailable"));
                        save.setEnabled(true);
                        Toast.makeText(MainActivity.this, failureMessage, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
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

    private interface OtpVerificationHandler {
        void onVerify(String otpValue, Dialog dialog, TextView verifyInDialog, EditText[] codeCells);
    }

    private void showOtpDialog(String username, String pin, TextView save) {
        showThemedOtpDialog(translate("Verify your mobile"), translate("Enter the code sent to your mobile number."), translate("6-digit SMS code"),
            (value, dialog, verifyInDialog, codeCells) -> {
                if (phoneVerificationMobile == null || value.length() != 6) {
                    save.setText(translate("Invalid SMS code"));
                    save.setEnabled(true);
                    Toast.makeText(this, translate("Enter a valid 6-digit code"), Toast.LENGTH_LONG).show();
                    return;
                }
                verifyInDialog.setText(translate("Verifying..."));
                verifyInDialog.setEnabled(false);
                completePhoneVerification(phoneVerificationMobile, username, pin, save, value, dialog, verifyInDialog, codeCells);
            }, () -> {
                save.setText(translate("Save and continue"));
                save.setEnabled(true);
            });
    }

    private void showThemedOtpDialog(String titleText, String subtitleText, String hintText,
                                     OtpVerificationHandler verifyHandler, Runnable cancelAction) {
        showThemedOtpDialog(titleText, subtitleText, hintText, verifyHandler, cancelAction, null);
    }

    private void showThemedOtpDialog(String titleText, String subtitleText, String hintText,
                                     OtpVerificationHandler verifyHandler, Runnable cancelAction, Runnable resendAction) {
        showThemedOtpDialog(titleText, subtitleText, hintText, verifyHandler, cancelAction, resendAction, false);
    }

    private void showThemedOtpDialog(String titleText, String subtitleText, String hintText,
                                     OtpVerificationHandler verifyHandler, Runnable cancelAction,
                                     Runnable resendAction, boolean smsConsent) {
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

        TextView title = text(translate(titleText), 18, primaryTextColor(), Typeface.NORMAL);
        title.setTypeface(localizedScriptTypeface(title.getText(), Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text(translate(subtitleText), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setTypeface(localizedScriptTypeface(subtitle.getText(), Typeface.NORMAL));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setMaxLines(2);
        content.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(34)));

        EditText[] codeCells = otpCells();
        if (smsConsent) {
            activeSmsOtpCells = codeCells;
            activeSmsOtpDialog = dialog;
            dialog.setOnDismissListener(ignored -> stopSmsUserConsent());
        }
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

        if (resendAction != null) {
            TextView resend = text(translate("Resend"), 11, accentColor(), Typeface.NORMAL);
            resend.setTypeface(localizedScriptTypeface(resend.getText(), Typeface.NORMAL));
            resend.setGravity(Gravity.CENTER);
            resend.setPadding(dp(4), 0, dp(8), 0);
            resend.setOnClickListener(view -> {
                dialog.dismiss();
                resendAction.run();
            });
            actions.addView(resend, new LinearLayout.LayoutParams(dp(72), dp(44)));
        }

        TextView cancel = text(translate("Cancel"), 12, secondaryTextColor(), Typeface.NORMAL);
        cancel.setTypeface(localizedScriptTypeface(cancel.getText(), Typeface.NORMAL));
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
            if (cancelAction != null) cancelAction.run();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(72), dp(44)));

        TextView verify = text(translate("Verify"), 12, GOLD_ON, Typeface.NORMAL);
        verify.setTypeface(localizedScriptTypeface(verify.getText(), Typeface.NORMAL));
        verify.setGravity(Gravity.CENTER);
        verify.setBackground(goldButton());
        LinearLayout.LayoutParams verifyParams = new LinearLayout.LayoutParams(dp(90), dp(44));
        verifyParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(verify, verifyParams);
        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));

        verify.setOnClickListener(view -> {
            String value = pinValue(codeCells).trim();
            if (value.length() != 6) {
                Toast.makeText(this, translate("Enter a valid 6-digit code"), Toast.LENGTH_LONG).show();
                return;
            }
            if (verifyHandler != null) {
                verifyHandler.onVerify(value, dialog, verify, codeCells);
            }
        });

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        if (smsConsent && pendingSmsUserConsentIntent != null) {
            Intent consentIntent = pendingSmsUserConsentIntent;
            pendingSmsUserConsentIntent = null;
            launchSmsUserConsent(consentIntent);
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
    }

    private void completePhoneVerification(String mobile, String username, String pin, TextView save, String otpValue,
                                           Dialog dialog, TextView verifyInDialog, EditText[] codeCells) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        save.setText(translate("Verifying OTP..."));
        save.setEnabled(false);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", mobile);
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-otp", payload.toString(), null);
                boolean verified = response != null && (response.optBoolean("success", false) || "success".equalsIgnoreCase(response.optString("status", "")));
                String verificationToken = response == null ? "" : response.optString("verification_token", "");
                runOnUiThread(() -> {
                    if (verified) {
                        confirmVerifiedMobileOnBackend(mobile, verificationToken, confirmed -> {
                            if (!confirmed) {
                                phoneVerificationHandled = false;
                                save.setText(translate("Could not save mobile verification"));
                                save.setEnabled(true);
                                Toast.makeText(this, translate("Could not save mobile verification. Please request a new code and try again."), Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
                            account.edit()
                                    .putString("mobile", mobile)
                                    .putString("verified_mobile", mobile)
                                    .putBoolean("mobile_verified", true)
                                    .apply();
                            saveVerifiedMobileToCloud(mobile);
                            finishProfileSetup(username, pin, save);
                        });
                    } else {
                        phoneVerificationHandled = false;
                        if (verifyInDialog != null) {
                            verifyInDialog.setText(translate("Verify"));
                            verifyInDialog.setEnabled(true);
                        }
                        if (codeCells != null) {
                            for (EditText cell : codeCells) {
                                if (cell != null) cell.setText("");
                            }
                            if (codeCells.length > 0 && codeCells[0] != null) {
                                codeCells[0].requestFocus();
                            }
                        }
                        save.setText(translate("SMS verification failed"));
                        save.setEnabled(true);
                        Toast.makeText(this, translate("Could not verify mobile number. Check the code and try again."), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    phoneVerificationHandled = false;
                    if (verifyInDialog != null) {
                        verifyInDialog.setText(translate("Verify"));
                        verifyInDialog.setEnabled(true);
                    }
                    save.setText(translate("SMS verification failed"));
                    save.setEnabled(true);
                    Toast.makeText(this, translate("Could not verify mobile number"), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void finishProfileSetup(String username, String pin, TextView save) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            save.setText(translate("Sign in first"));
            return;
        }
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        if (account.getBoolean("pin_setup_pending", false)) {
            saveFirebaseCredential(username, pin, save);
            return;
        }
        save.setText(translate("Checking username..."));
        save.setEnabled(false);
        currentUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            save.setText(translate("Could not secure account"));
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
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        boolean pinSetupPending = account.getBoolean("pin_setup_pending", false);
        if (passwordProviderLinked && pinSetupPending) {
            currentUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                JSONObject payload = new JSONObject();
                try {
                    payload.put("username", username);
                    payload.put("pin", pin);
                    JSONObject response = postJson(
                            "/api/auth/complete-registration",
                            payload.toString(),
                            token.getToken()
                    );
                    boolean completed = response.optBoolean("success", false);
                    boolean alreadyCompleted = response.optBoolean("already_completed", false);
                    String detail = response.optString(
                            "detail",
                            "Could not secure account credentials; please retry"
                    );
                    runOnUiThread(() -> {
                        if (completed) {
                            if (alreadyCompleted) {
                                account.edit()
                                        .putBoolean("created", true)
                                        .putBoolean("pin_setup_pending", false)
                                        .putString("username", formatUsernameDisplay(username))
                                        .apply();
                                accountCreated = false;
                                FirebaseAuth.getInstance().signOut();
                                buildScreen();
                                Toast.makeText(
                                        this,
                                        "This account is already registered. Log in from the Auth screen.",
                                        Toast.LENGTH_LONG
                                ).show();
                            } else {
                                finishFirebaseCredentialSetup(username, pin, save, account);
                            }
                        } else {
                            save.setText(translate("Could not secure account"));
                            save.setEnabled(true);
                            Toast.makeText(this, detail, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception error) {
                    runOnUiThread(() -> {
                        save.setText(translate("Could not secure account"));
                        save.setEnabled(true);
                        Toast.makeText(this, "Could not reach account security service. Please retry.", Toast.LENGTH_LONG).show();
                    });
                }
            })).addOnFailureListener(error -> {
                save.setText(translate("Could not secure account"));
                save.setEnabled(true);
                Toast.makeText(this, "Your account session expired. Please sign in again and retry.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        Task<?> accountTask = passwordProviderLinked
                ? Tasks.forResult(null)
                : currentUser.linkWithCredential(EmailAuthProvider.getCredential(email, password));
        accountTask
                .addOnSuccessListener(result -> finishFirebaseCredentialSetup(username, pin, save, account))
                .addOnFailureListener(error -> {
                    save.setText(translate("Could not secure account"));
                    save.setEnabled(true);
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void finishFirebaseCredentialSetup(
            String username,
            String pin,
            TextView save,
            SharedPreferences account
    ) {
        saveStoredAccountPin(pin);
        String formattedUsername = formatUsernameDisplay(username);
        account.edit()
                .putBoolean("created", true)
                .putBoolean("pin_setup_pending", false)
                .putString("username", formattedUsername)
                .apply();
        accountCreated = true;
        saveCloudProfile(formattedUsername, account.getString("full_name", ""), account.getString("email", ""),
                account.getString("mobile", ""), account.getString("state", ""), account.getString("city", ""), null);
        syncProfileWithBackend();
        showHome();
        hydrateProfileFromBackend(null);
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

    private String generateTemporaryFirebasePassword() {
        SecureRandom secureRandom = new SecureRandom();
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnopqrstuvwxyz";
        String digits = "23456789";
        String symbols = "!@#$%&*+-_";
        String all = upper + lower + digits + symbols;
        char[] password = new char[24];
        password[0] = upper.charAt(secureRandom.nextInt(upper.length()));
        password[1] = lower.charAt(secureRandom.nextInt(lower.length()));
        password[2] = digits.charAt(secureRandom.nextInt(digits.length()));
        password[3] = symbols.charAt(secureRandom.nextInt(symbols.length()));
        for (int i = 4; i < password.length; i++) {
            password[i] = all.charAt(secureRandom.nextInt(all.length()));
        }
        for (int i = password.length - 1; i > 0; i--) {
            int swapIndex = secureRandom.nextInt(i + 1);
            char current = password[i];
            password[i] = password[swapIndex];
            password[swapIndex] = current;
        }
        return new String(password);
    }

    private String credentialPassword(String username, String pin) {
        return "Fendly!" + username.trim().toLowerCase(Locale.US) + "#" + pin;
    }

    private String firebaseLoginFailureMessage(Exception error) {
        if (!(error instanceof FirebaseAuthException)) {
            Log.w("AUTH", "Firebase sign-in failed: " + error.getClass().getSimpleName());
            return "Could not contact the sign-in service. Check your connection and try again.";
        }

        String code = ((FirebaseAuthException) error).getErrorCode();
        String normalizedCode = code == null ? "" : code.toUpperCase(Locale.US);
        Log.w("AUTH", "Firebase sign-in failed: " + normalizedCode);
        if (normalizedCode.contains("WRONG_PASSWORD")
                || normalizedCode.contains("USER_NOT_FOUND")
                || normalizedCode.contains("INVALID_CREDENTIAL")
                || normalizedCode.contains("INVALID_LOGIN_CREDENTIALS")) {
            return "Incorrect username or PIN. Check your details, or tap Forgot PIN to recover your account.";
        }
        if (normalizedCode.contains("NETWORK_REQUEST_FAILED")) {
            return "Could not sign you in. Check your internet connection and try again.";
        }
        if (normalizedCode.contains("TOO_MANY_REQUESTS")) {
            return "Too many sign-in attempts. Wait a while, then try again.";
        }
        if (normalizedCode.contains("USER_DISABLED")) {
            return "This account is disabled. Contact Fendly support.";
        }
        return "We could not sign you in right now. Please try again.";
    }

    private void showPinLogin() {
        screenRenderer = this::showPinLogin;
        LinearLayout root = screenBase(translate("Login with PIN"));
        addHeading(translate("Welcome back."), translate("Use the username and PIN from your Fendly profile."));
        EditText username = field(localizedFieldLabel("Username"));
        String savedUsername = getSharedPreferences("fendly_account", MODE_PRIVATE)
                .getString("username", "");
        username.setText(savedUsername);
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
                        handleSuccessfulLogin(name, code, () -> {
                            login.setText(localizedFieldLabel("Log in"));
                            login.setEnabled(true);
                            showHome();
                        });
                    })
                    .addOnFailureListener(error -> {
                        login.setText(translate("Try again"));
                        login.setEnabled(true);
                        Toast.makeText(
                                this,
                                firebaseLoginFailureMessage(error),
                                Toast.LENGTH_LONG
                        ).show();
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

        TextView title = text(translate("Fingerprint login"), 18, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text(translate("Confirm your identity to continue"), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(28)));

        String localizedUsername = localizeProfileName(username);
        TextView usernameLabel = text(localizedUsername, 15, primaryTextColor(), Typeface.NORMAL);
        usernameLabel.setGravity(Gravity.CENTER);
        usernameLabel.setBackground(roundWithStroke(backgroundColor(), 12, fieldBorderColor()));
        LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(-1, dp(46));
        usernameParams.setMargins(0, dp(10), 0, dp(14));
        content.addView(usernameLabel, usernameParams);

        TextView login = text(translate("Log in"), 13, GOLD_ON, Typeface.NORMAL);
        login.setGravity(Gravity.CENTER);
        login.setBackground(goldButton());
        login.setOnClickListener(view -> {
            dialog.dismiss();
            authenticateWithBiometric(username, pin);
        });
        content.addView(login, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView another = text(translate("Use another account"), 11, secondaryTextColor(), Typeface.NORMAL);
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
                                    handleSuccessfulLogin(username, pin, MainActivity.this::showHome);
                                })
                                .addOnFailureListener(error -> Toast.makeText(
                                        MainActivity.this,
                                        firebaseLoginFailureMessage(error),
                                        Toast.LENGTH_LONG
                                ).show());
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
        String savedMobile = getSharedPreferences("fendly_account", MODE_PRIVATE)
                .getString("mobile", "").trim();
        boolean usesFirebaseTestCode = TEST_PHONE_PIN_RECOVERY_MOBILE.equals(
                normalizeIndianMobileDigits(savedMobile)
        );
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_key,
                translate("Reset PIN?"),
                translate(usesFirebaseTestCode
                        ? "A temporary PIN will be created after your Firebase test code, or by SMS if Firebase billing is unavailable."
                        : "A temporary 4-digit PIN will be sent after mobile verification.")
        );
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView cancel = text(translate("Cancel"), 12, secondaryTextColor(), Typeface.NORMAL);
        cancel.setTypeface(localizedScriptTypeface(cancel.getText(), Typeface.NORMAL));
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(view -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(88), dp(44)));
        TextView confirm = text(translate("Confirm"), 12, GOLD_ON, Typeface.NORMAL);
        confirm.setTypeface(localizedScriptTypeface(confirm.getText(), Typeface.NORMAL));
        confirm.setGravity(Gravity.CENTER);
        confirm.setBackground(goldButton());
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(dp(100), dp(44));
        confirmParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(confirm, confirmParams);
        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        confirm.setOnClickListener(view -> {
            String username = loginUsername.getText().toString().trim();
            String mobileValue = getSharedPreferences("fendly_account", MODE_PRIVATE).getString("mobile", "").trim();
            if (!username.matches("^[a-zA-Z0-9_]{3,32}$")) {
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
        if (iconResource != 0) {
            ImageView icon = new ImageView(this);
            icon.setImageResource(iconResource);
            icon.setColorFilter(accentColor());
            icon.setPadding(dp(12), dp(12), dp(12), dp(12));
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(60), dp(60));
            iconParams.gravity = Gravity.CENTER_HORIZONTAL;
            content.addView(icon, iconParams);
        }
        TextView title = text(translate(titleText), 18, primaryTextColor(), Typeface.NORMAL);
        title.setTypeface(localizedScriptTypeface(title.getText(), Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, iconResource == 0 ? 0 : dp(8), 0, dp(6));
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView subtitle = text(translate(subtitleText), 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setTypeface(localizedScriptTypeface(subtitle.getText(), Typeface.NORMAL));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLineSpacing(dp(2), 1f);
        subtitle.setPadding(0, dp(4), 0, dp(10));
        content.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));
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
        phoneVerificationMobile = normalizeIndianMobileDigits(mobile);
        if (TEST_PHONE_PIN_RECOVERY_MOBILE.equals(phoneVerificationMobile)) {
            startFirebaseTestPhonePinRecovery(username, mobile, temporaryPin, parentDialog);
            return;
        }
        startFast2SmsForgotPinVerification(username, mobile, temporaryPin, parentDialog);
    }

    private void startFast2SmsForgotPinVerification(
            String username,
            String mobile,
            String temporaryPin,
            Dialog parentDialog
    ) {
        phoneVerificationHandled = false;
        phoneVerificationMobile = normalizeIndianMobileDigits(mobile);
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", phoneVerificationMobile);
                JSONObject response = postJson("/api/auth/send-otp", payload.toString(), null);
                boolean sent = response != null && response.optBoolean("success", false);
                if (!sent) {
                    String failureMessage = response == null
                            ? translate("Could not send OTP")
                            : response.optString("detail", response.optString("message", translate("Could not send OTP")));
                    runOnUiThread(() -> {
                        parentDialog.dismiss();
                        Toast.makeText(MainActivity.this, failureMessage, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                runOnUiThread(() -> showForgotOtpDialog(username, mobile, temporaryPin, parentDialog));
            } catch (Exception error) {
                runOnUiThread(() -> {
                    parentDialog.dismiss();
                    Toast.makeText(MainActivity.this, translate("Could not send OTP"), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startFirebaseTestPhonePinRecovery(
            String username,
            String mobile,
            String temporaryPin,
            Dialog parentDialog
    ) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber("+91" + TEST_PHONE_PIN_RECOVERY_MOBILE)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        recoverPinWithTestPhoneCredential(
                                username, mobile, temporaryPin, parentDialog, null, credential
                        );
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException error) {
                        String diagnostic = error.getMessage();
                        if (diagnostic != null && diagnostic.contains("BILLING_NOT_ENABLED")) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Firebase phone verification requires billing. Sending a Fast2SMS recovery code instead.",
                                    Toast.LENGTH_LONG
                            ).show();
                            startFast2SmsForgotPinVerification(
                                    username,
                                    mobile,
                                    temporaryPin,
                                    parentDialog
                            );
                        } else {
                            parentDialog.dismiss();
                            Toast.makeText(
                                    MainActivity.this,
                                    "Firebase phone verification failed: " + error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onCodeSent(
                            String verificationId,
                            PhoneAuthProvider.ForceResendingToken resendToken
                    ) {
                        showThemedOtpDialog(
                                translate("Enter Firebase test code"),
                                translate("Enter the test code configured for your phone in Firebase Console."),
                                translate("6-digit test code"),
                                (value, otpDialog, verifyButton, codeCells) -> {
                                    if (value.length() != 6) {
                                        Toast.makeText(
                                                MainActivity.this,
                                                translate("Enter the 6-digit OTP"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                        return;
                                    }
                                    verifyButton.setText(translate("Verifying..."));
                                    verifyButton.setEnabled(false);
                                    PhoneAuthCredential credential =
                                            PhoneAuthProvider.getCredential(verificationId, value);
                                    recoverPinWithTestPhoneCredential(
                                            username,
                                            mobile,
                                            temporaryPin,
                                            parentDialog,
                                            otpDialog,
                                            credential
                                    );
                                },
                                parentDialog::dismiss
                        );
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void recoverPinWithTestPhoneCredential(
            String username,
            String mobile,
            String pin,
            Dialog parentDialog,
            Dialog otpDialog,
            PhoneAuthCredential credential
    ) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser verifiedPhoneUser = result.getUser();
                    if (verifiedPhoneUser == null) {
                        auth.signOut();
                        showPinRecoveryFailure(parentDialog, otpDialog, "Phone verification did not return an account");
                        return;
                    }
                    verifiedPhoneUser.getIdToken(false)
                            .addOnSuccessListener(token -> network.execute(() -> {
                                boolean recovered = false;
                                String detail = "Could not reset this account PIN; please retry";
                                try {
                                    JSONObject payload = new JSONObject();
                                    payload.put("username", username);
                                    payload.put("mobile", TEST_PHONE_PIN_RECOVERY_MOBILE);
                                    payload.put("pin", pin);
                                    JSONObject response = postJson(
                                            "/api/auth/recover-pin-test-phone",
                                            payload.toString(),
                                            token.getToken()
                                    );
                                    recovered = response.optBoolean("success", false);
                                    detail = response.optString("detail", detail);
                                } catch (Exception error) {
                                    detail = "Could not reach account recovery service. Please retry.";
                                }
                                boolean recoverySucceeded = recovered;
                                String recoveryDetail = detail;
                                runOnUiThread(() -> {
                                    auth.signOut();
                                    if (recoverySucceeded) {
                                        if (otpDialog != null && otpDialog.isShowing()) {
                                            otpDialog.dismiss();
                                        }
                                        saveRecoveredPinLocally(username, mobile, pin, parentDialog);
                                    } else {
                                        showPinRecoveryFailure(
                                                parentDialog, otpDialog, recoveryDetail
                                        );
                                    }
                                });
                            }))
                            .addOnFailureListener(error -> {
                                auth.signOut();
                                showPinRecoveryFailure(
                                        parentDialog,
                                        otpDialog,
                                        "Could not verify the Firebase phone session"
                                );
                            });
                })
                .addOnFailureListener(error -> showPinRecoveryFailure(
                        parentDialog,
                        otpDialog,
                        "The Firebase test code was not accepted"
                ));
    }

    private void showPinRecoveryFailure(Dialog parentDialog, Dialog otpDialog, String message) {
        if (otpDialog != null && otpDialog.isShowing()) {
            otpDialog.dismiss();
        }
        if (parentDialog != null && parentDialog.isShowing()) {
            parentDialog.dismiss();
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void saveRecoveredPinLocally(String username, String mobile, String pin, Dialog parentDialog) {
        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                .putBoolean("created", true)
                .putString("username", username)
                .putString("mobile", mobile)
                .putString("account_pin", pin)
                .apply();
        saveStoredAccountPin(pin);
        if (parentDialog != null && parentDialog.isShowing()) {
            parentDialog.dismiss();
        }
        new AlertDialog.Builder(this)
                .setTitle(translate("Temporary PIN"))
                .setMessage(translate("Your new 4-digit PIN is ") + pin + ". " + translate("Use it to log in, then change it from My Profile."))
                .setPositiveButton(translate("OK"), null)
                .show();
    }

    private void showForgotOtpDialog(String username, String mobile, String pin, Dialog parentDialog) {
        showThemedOtpDialog(translate("Enter OTP"), translate("Enter the 6-digit code sent to your mobile number."), translate("6-digit OTP"),
            (value, dialog, verifyInDialog, codeCells) -> {
                if (phoneVerificationMobile == null || value.length() != 6) {
                    Toast.makeText(this, translate("Enter the 6-digit OTP"), Toast.LENGTH_LONG).show();
                    return;
                }
                verifyInDialog.setText(translate("Verifying..."));
                verifyInDialog.setEnabled(false);
                verifyForgotOtp(username, mobile, pin, parentDialog, value, dialog, verifyInDialog, codeCells);
            }, () -> { }, null);
    }

    private void verifyForgotOtp(String username, String mobile, String pin, Dialog parentDialog, String otpValue,
                                Dialog dialog, TextView verifyInDialog, EditText[] codeCells) {
        if (phoneVerificationHandled) return;
        phoneVerificationHandled = true;
        network.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", phoneVerificationMobile);
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-otp", payload.toString(), null);
                boolean verified = response != null && (response.optBoolean("success", false) || "success".equalsIgnoreCase(response.optString("status", "")));
                String verificationToken = response == null
                        ? ""
                        : response.optString("verification_token", "");
                if (verified && !verificationToken.isEmpty()) {
                    finishSmsVerifiedPinRecovery(
                            username,
                            mobile,
                            pin,
                            parentDialog,
                            dialog,
                            verificationToken
                    );
                    return;
                }
                runOnUiThread(() -> {
                    phoneVerificationHandled = false;
                    if (verifyInDialog != null) {
                        verifyInDialog.setText(translate("Verify"));
                        verifyInDialog.setEnabled(true);
                    }
                    if (codeCells != null) {
                        for (EditText cell : codeCells) {
                            if (cell != null) cell.setText("");
                        }
                        if (codeCells.length > 0 && codeCells[0] != null) {
                            codeCells[0].requestFocus();
                        }
                    }
                    String message = verified
                            ? "Mobile verification did not return a recovery token"
                            : translate("Incorrect OTP. Please try again.");
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    phoneVerificationHandled = false;
                    if (verifyInDialog != null) {
                        verifyInDialog.setText(translate("Verify"));
                        verifyInDialog.setEnabled(true);
                    }
                    Toast.makeText(this, "Could not reset PIN", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void finishSmsVerifiedPinRecovery(
            String username,
            String mobile,
            String pin,
            Dialog parentDialog,
            Dialog otpDialog,
            String verificationToken
    ) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("mobile", phoneVerificationMobile);
            payload.put("pin", pin);
            JSONObject response = postJson(
                    "/api/auth/recover-pin",
                    payload.toString(),
                    verificationToken
            );
            boolean recovered = response != null && response.optBoolean("success", false);
            String detail = response == null
                    ? "Could not reset this account PIN; please retry"
                    : response.optString("detail", "Could not reset this account PIN; please retry");
            runOnUiThread(() -> {
                if (recovered) {
                    if (otpDialog != null && otpDialog.isShowing()) {
                        otpDialog.dismiss();
                    }
                    saveRecoveredPinLocally(username, mobile, pin, parentDialog);
                } else {
                    showPinRecoveryFailure(parentDialog, otpDialog, detail);
                }
            });
        } catch (Exception error) {
            runOnUiThread(() -> showPinRecoveryFailure(
                    parentDialog,
                    otpDialog,
                    "Could not reach account recovery service. Please retry."
            ));
        }
    }

    private boolean validEmail(String value) {
        String email = value == null ? "" : value.trim().toLowerCase(Locale.US);
        return !email.isEmpty() && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
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
        TextView fieldLabel = text(label, 10, secondaryTextColor(), Typeface.NORMAL);
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
        styleLocationPickerSearch(search);
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
            styleLocationPickerDialog(dialog);
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
        styleLocationPickerSearch(search);
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
            styleLocationPickerDialog(dialog);
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

    private void styleLocationPickerDialog(AlertDialog dialog) {
        if (Build.VERSION.SDK_INT >= 29 && dialog.getWindow() != null) {
            dialog.getWindow().getDecorView().setForceDarkAllowed(false);
        }
        TextView title = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
        if (title != null) {
            title.setTextColor(primaryTextColor());
            title.setTypeface(localizedScriptTypeface(title.getText(), Typeface.NORMAL));
        }
        TextView negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) negativeButton.setTextColor(primaryTextColor());
    }

    private void styleLocationPickerSearch(EditText search) {
        search.setTextColor(primaryTextColor());
        search.setHintTextColor(secondaryTextColor());
        search.setTypeface(localizedScriptTypeface(search.getText(), Typeface.NORMAL));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                search.setTypeface(localizedScriptTypeface(value, Typeface.NORMAL));
            }
            @Override public void afterTextChanged(Editable value) { }
        });
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

    private void clearReportDraftState() {
        editingReportId = null;
        editingReportType = null;
        editingReportImageUrl = null;
        draftItem = "";
        draftDescription = "";
        draftLocation = "";
        draftCityTag = "";
        draftDate = "";
        draftIdentifier = "";
        draftReportCategory = "item";
        reportWizardStep = 1;
        draftImei = "";
        draftSocialShareConsent = false;
        draftGuidelinesAccepted = false;
        selectedImage = null;
        capturedImage = null;
        Arrays.fill(reportImages, null);
        Arrays.fill(reportCameraImages, null);
        stopActiveLocationUpdates();
        hasLocation = false;
        currentLat = 0.0;
        currentLng = 0.0;
        activeLocationReportType = null;
        lostReportHasLocation = false;
        lostReportLat = 0.0;
        lostReportLng = 0.0;
        foundReportHasLocation = false;
        foundReportLat = 0.0;
        foundReportLng = 0.0;
        locationRequestGeneration++;
    }

    private void showHome() {
        currentPage = PAGE_HOME;
        profileScrollY = 0;
        screenRenderer = this::showHome;
        LinearLayout root = screenBase("Home");
        addHeading("Find what matters.", LanguageManager.profileText(this, "home_tagline"));

        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.HORIZONTAL);
        TextView lost = actionButton(LanguageManager.profileText(this, "lost_theft_button"), true);
        lost.setBackground(round(Color.rgb(11, 93, 69), 18));
        lost.setTextColor(Color.WHITE);
        lost.setOnClickListener(view -> {
            clearReportDraftState();
            showReport("LOST");
        });
        choices.addView(lost, new LinearLayout.LayoutParams(0, 72, 1));
        TextView found = actionButton("FOUND", false);
        found.setTextColor(Color.WHITE);
        found.setBackground(round(Color.rgb(201, 162, 76), 18));
        found.setOnClickListener(view -> {
            clearReportDraftState();
            showReport("FOUND");
        });
        choices.addView(found, new LinearLayout.LayoutParams(0, 72, 1));
        addField(root, choices);
        TextView reports = actionButton("My reports", false);
        reports.setOnClickListener(view -> showReports());
        addField(root, reports);
        TextView vault = actionButton(LanguageManager.profileText(this, "vault_title"), false);
        vault.setOnClickListener(view -> startActivity(new Intent(this, VaultActivity.class)));
        addField(root, vault);
        TextView profile = actionButton("My profile", false);
        profile.setOnClickListener(view -> showProfile());
        addField(root, profile);
        TextView safeTrade = actionButton(LanguageManager.profileText(this, "safe_trade_check"), false);
        safeTrade.setOnClickListener(view -> startActivity(new Intent(this, SafeTradeCheckActivity.class)));
        addField(root, safeTrade);
        String username = getSharedPreferences("fendly_account", MODE_PRIVATE).getString("username", "your account");
        TextView signedInText = text(translate("Signed in as") + " " + localizeProfileName(username), 14, secondaryTextColor(), Typeface.NORMAL);
        signedInText.setGravity(Gravity.CENTER);
        addField(root, signedInText);
    }

    private void loadDiscoveryReports(
            String city,
            LinearLayout resultsContainer,
            TextView statusView,
            List<JSONObject> ownerReports,
            int requestGeneration
    ) {
        network.execute(() -> {
            List<JSONObject> reports = new ArrayList<>();
            String loadError = null;
            HttpURLConnection connection = null;
            try {
                String endpoint = API_BASE + "/api/reports";
                if (city != null && !city.trim().isEmpty()) {
                    endpoint += "?city=" + URLEncoder.encode(city.trim(), StandardCharsets.UTF_8.name());
                }
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(10000);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String idToken = Tasks.await(currentUser.getIdToken(false)).getToken();
                    if (idToken == null || idToken.trim().isEmpty()) {
                        throw new java.io.IOException("Could not authenticate blocked-user filtering");
                    }
                    connection.setRequestProperty("Authorization", "Bearer " + idToken);
                }
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    throw new java.io.IOException("Public reports returned HTTP " + responseCode);
                }
                JSONArray response = new JSONArray(readStream(connection.getInputStream()));
                for (int index = 0; index < response.length(); index++) {
                    JSONObject report = response.optJSONObject(index);
                    if (report != null) reports.add(report);
                }
            } catch (Exception error) {
                Log.w("DISCOVERY_REPORTS", "Could not load discovery reports", error);
                loadError = "Could not load reports. Try again.";
            } finally {
                if (connection != null) connection.disconnect();
            }

            List<JSONObject> loadedReports = reports;
            String errorMessage = loadError;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || requestGeneration != discoveryRequestGeneration) return;
                resultsContainer.removeAllViews();
                if (errorMessage != null) {
                    discoveryReports.clear();
                    discoveryReportsLoaded = false;
                    statusView.setText(localizeReportsText(errorMessage));
                    statusView.setVisibility(View.VISIBLE);
                    return;
                }
                discoveryReports.clear();
                discoveryReports.addAll(loadedReports);
                discoveryReportsLoaded = true;
                renderDiscoveryReports(resultsContainer, statusView, ownerReports);
            });
        });
    }

    private void populateReportCategoryTabs(
            LinearLayout tabs,
            String selectedCategory,
            java.util.function.Consumer<String> onCategorySelected
    ) {
        tabs.removeAllViews();
        String[] categories = {"items", "animals", "people"};
        String[] icons = {"🔍", "🐾", "❤️"};
        String[] labels = {
                "Valuables & Items",
                "Pets & Animals",
                "Missing Persons / Loved Ones"
        };
        for (int index = 0; index < categories.length; index++) {
            String category = categories[index];
            boolean emergency = "people".equals(category);
            LinearLayout tab = createDiscoveryCategoryTab(
                    category,
                    icons[index],
                    labels[index],
                    emergency,
                    selectedCategory.equals(category)
            );
            tab.setOnClickListener(view -> {
                if (selectedCategory.equals(category)) return;
                onCategorySelected.accept(category);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(112), 1);
            params.setMargins(dp(3), 0, dp(3), 0);
            tabs.addView(tab, params);
        }
    }

    private LinearLayout createDiscoveryCategoryTab(
            String category,
            String icon,
            String label,
            boolean emergency,
            boolean selected
    ) {
        int urgentColor = Color.rgb(190, 45, 55);
        int backgroundColor = selected
                ? emergency ? urgentColor : Color.rgb(24, 112, 82)
                : emergency ? Color.argb(30, 220, 50, 60) : surfaceColor();
        int strokeColor = emergency ? urgentColor : selected ? Color.rgb(24, 112, 82) : borderColor();
        int foregroundColor = selected ? Color.WHITE : primaryTextColor();

        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(dp(4), dp(5), dp(4), dp(5));
        tab.setBackground(roundWithStroke(backgroundColor, 16, strokeColor));
        tab.setClickable(true);
        tab.setFocusable(true);
        tab.setContentDescription(localizeReportsText(label));

        TextView badge = text(
                emergency ? localizeReportsText("URGENT") : " ",
                8,
                selected && emergency ? urgentColor : Color.WHITE,
                Typeface.BOLD
        );
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(6), dp(1), dp(6), dp(1));
        badge.setBackground(round(
                emergency
                        ? selected ? Color.WHITE : urgentColor
                        : Color.TRANSPARENT,
                8
        ));
        tab.addView(badge, new LinearLayout.LayoutParams(-2, dp(14)));

        TextView iconView = text(icon, 23, foregroundColor, Typeface.NORMAL);
        iconView.setGravity(Gravity.CENTER);
        tab.addView(iconView, new LinearLayout.LayoutParams(-1, dp(32)));

        TextView labelView = text(
                localizeReportsText(label).replace(" / ", "\n").replace(" & ", " &\n"),
                9,
                foregroundColor,
                Typeface.BOLD
        );
        labelView.setGravity(Gravity.CENTER);
        labelView.setMaxLines(3);
        tab.addView(labelView, new LinearLayout.LayoutParams(-1, -2));
        return tab;
    }

    private void renderDiscoveryReports(
            LinearLayout resultsContainer,
            TextView statusView,
            List<JSONObject> ownerReports
    ) {
        resultsContainer.removeAllViews();
        if (!discoveryReportsLoaded) return;
        List<JSONObject> visibleReports = new ArrayList<>();
        for (JSONObject report : discoveryReports) {
            if (discoveryReportMatchesCategory(report, myReportsCategory)) {
                visibleReports.add(report);
            }
        }
        if (visibleReports.isEmpty()) {
            statusView.setText(localizeReportsText(
                    discoveryReports.isEmpty()
                            ? "No active reports found."
                            : "No reports in this category."
            ));
            statusView.setVisibility(View.VISIBLE);
            return;
        }
        statusView.setVisibility(View.GONE);
        for (JSONObject report : visibleReports) {
            JSONObject ownerReport = findOwnedReport(report, ownerReports);
            resultsContainer.addView(createDiscoveryReportCard(
                    report,
                    ownerReport == null ? report : ownerReport
            ));
        }
    }

    private JSONObject findOwnedReport(JSONObject publicReport, List<JSONObject> ownerReports) {
        if (publicReport == null || ownerReports == null || ownerReports.isEmpty()) return null;
        String publicId = publicReport.optString("id", publicReport.optString("_id", "")).trim();
        if (publicId.isEmpty()) return null;
        for (JSONObject ownerReport : ownerReports) {
            String ownerId = ownerReport.optString("id", ownerReport.optString("_id", "")).trim();
            if (publicId.equals(ownerId)) return ownerReport;
        }
        return null;
    }

    private boolean discoveryReportMatchesCategory(JSONObject report, String category) {
        String reportCategory = report.optString("category", "").trim().toLowerCase(Locale.US);
        if ("people".equals(category)) {
            if (reportCategory.contains("people") || reportCategory.contains("person")) return true;
            if (!reportCategory.isEmpty() && !"other".equals(reportCategory)) return false;
            return containsDiscoveryTerm(
                    report.optString("title", "").toLowerCase(Locale.US),
                    "missing person", "person", "people", "loved one", "child", "children",
                    "toddler", "boy", "girl", "woman", "man"
            );
        }
        if ("animals".equals(category)) {
            if (reportCategory.contains("animal") || reportCategory.contains("pet")) return true;
            if (!reportCategory.isEmpty() && !"other".equals(reportCategory)) return false;
            return containsDiscoveryTerm(
                    report.optString("title", "").toLowerCase(Locale.US),
                    "animal", "pet", "dog", "puppy", "cat", "kitten", "cow", "goat",
                    "sheep", "horse", "bird", "parrot", "rabbit", "fish", "snake"
            );
        }
        return !discoveryReportMatchesCategory(report, "people")
                && !discoveryReportMatchesCategory(report, "animals");
    }

    private boolean containsDiscoveryTerm(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) return true;
        }
        return false;
    }

    private View createDiscoveryReportCard(JSONObject report, JSONObject detailsReport) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        boolean emergency = discoveryReportMatchesCategory(report, "people");
        int emergencyColor = Color.rgb(190, 45, 55);
        card.setBackground(roundWithStroke(
                emergency ? Color.argb(24, 220, 50, 60) : surfaceColor(),
                16,
                emergency ? emergencyColor : borderColor()
        ));
        card.setElevation(dp(2));
        card.setTag("reportRow");

        String type = report.optString("type", "FOUND").toUpperCase(Locale.US);
        if (emergency) {
            TextView urgentBadge = text(
                    localizeReportsText("URGENT"),
                    9,
                    Color.WHITE,
                    Typeface.BOLD
            );
            urgentBadge.setGravity(Gravity.CENTER);
            urgentBadge.setPadding(dp(7), dp(2), dp(7), dp(2));
            urgentBadge.setBackground(round(emergencyColor, 8));
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
            badgeParams.setMargins(0, 0, 0, dp(5));
            card.addView(urgentBadge, badgeParams);
        }
        TextView typeLabel = text(
                "LOST".equals(type) ? translate("LOST") : translate("FOUND"),
                11,
                "LOST".equals(type) ? Color.rgb(110, 205, 161) : accentColor(),
                Typeface.BOLD
        );
        card.addView(typeLabel, new LinearLayout.LayoutParams(-1, -2));

        TextView title = text(report.optString("title", ""), 16, primaryTextColor(), Typeface.BOLD);
        title.setPadding(0, dp(3), 0, dp(4));
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));

        String location = report.optString("report_location", "").trim();
        String category = report.optString("category", "").trim();
        String detail = location;
        if (!category.isEmpty()) {
            detail = detail.isEmpty() ? category : category + " · " + detail;
        }
        if (!detail.isEmpty()) {
            TextView details = text(detail, 13, secondaryTextColor(), Typeface.NORMAL);
            card.addView(details, new LinearLayout.LayoutParams(-1, -2));
        }

        String reportDate = report.optString("report_date", "").trim();
        if (!reportDate.isEmpty()) {
            TextView date = text(reportDate, 12, secondaryTextColor(), Typeface.NORMAL);
            date.setPadding(0, dp(4), 0, 0);
            card.addView(date, new LinearLayout.LayoutParams(-1, -2));
        }

        if ("LOST".equals(type) && isActiveCommunityReport(report)) {
            addCommunityPosterAction(card, detailsReport, 0, dp(10));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);
        card.setOnClickListener(view -> showReportDetailsDialog(detailsReport, false));
        card.setFocusable(true);
        return card;
    }

    private void showReport(String type) {
        inRenewalPaymentFlow = false;
        currentPage = PAGE_REPORT;
        screenRenderer = () -> showReport(type);
        currentReportType = type;
        restoreReportLocationState(type);
        LinearLayout root = screenBase("");
        AnimationSet transition = new AnimationSet(true);
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        TranslateAnimation slide = new TranslateAnimation(0f, 0f, dp(10), 0f);
        transition.addAnimation(fade);
        transition.addAnimation(slide);
        transition.setDuration(180);
        root.startAnimation(transition);
        addHeading(
                localizeReportWizardText("Report wizard"),
                localizeReportWizardText("Step " + reportWizardStep + " of 4")
        );
        addReportWizardProgress(root);

        EditText item = field("");
        EditText description = field("");
        EditText location = field("");
        EditText city = field("");
        EditText date = field("");
        EditText identifier = field("");
        EditText imei = field("");
        imei.setInputType(InputType.TYPE_CLASS_NUMBER);
        imei.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        imei.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        bindReportDraftField(item, draftItem, value -> draftItem = value);
        bindReportDraftField(description, draftDescription, value -> draftDescription = value);
        bindReportDraftField(location, draftLocation, value -> draftLocation = value);
        bindReportDraftField(city, draftCityTag, value -> draftCityTag = value);
        bindReportDraftField(date, draftDate, value -> draftDate = value);
        bindReportDraftField(identifier, draftIdentifier, value -> draftIdentifier = value);
        bindReportDraftField(imei, draftImei, value -> draftImei = value);
        configureDateField(date);

        if (reportWizardStep == 1) {
            renderReportWizardCategoryStep(root, type);
            return;
        }
        if (reportWizardStep == 2) {
            renderReportWizardLocationStep(root, type, location, city);
            return;
        }
        if (reportWizardStep == 3) {
            renderReportWizardDetailsStep(root, type, item, identifier, description, date, imei);
            return;
        }
        renderReportWizardReviewStep(root, type, item, description, location, city, date, identifier, imei);
    }

    private void addReportWizardProgress(LinearLayout root) {
        String[] labels = {"Category", "Location", "Details", "Review"};
        LinearLayout progress = new LinearLayout(this);
        progress.setOrientation(LinearLayout.HORIZONTAL);
        for (int index = 0; index < labels.length; index++) {
            boolean current = reportWizardStep == index + 1;
            TextView step = text(
                    (index + 1) + ". " + localizeReportWizardText(labels[index]),
                    10,
                    current ? Color.WHITE : secondaryTextColor(),
                    current ? Typeface.BOLD : Typeface.NORMAL
            );
            step.setGravity(Gravity.CENTER);
            step.setPadding(dp(4), dp(8), dp(4), dp(8));
            step.setBackground(round(
                    current ? LOST_GREEN : surfaceColor(),
                    12
            ));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(38), 1);
            params.setMargins(dp(2), 0, dp(2), 0);
            progress.addView(step, params);
        }
        addField(root, progress);
    }

    private void bindReportDraftField(EditText field, String value, java.util.function.Consumer<String> update) {
        field.setText(value);
        field.addTextChangedListener(draftWatcher(update::accept));
    }

    private void renderReportWizardCategoryStep(LinearLayout root, String type) {
        addWizardSectionTitle(root, "Choose what you are reporting");
        String[] categories = {"item", "pet", "person"};
        String[] categoryLabels = {"Item / Valuables", "Pet / Animal", "Missing Person"};
        String[] categoryIcons = {"📦", "🐾", "❤️"};
        for (int index = 0; index < categories.length; index++) {
            String category = categories[index];
            TextView option = actionButton(
                    categoryIcons[index] + "\n" + localizeReportWizardText(categoryLabels[index]),
                    draftReportCategory.equals(category)
            );
            option.setGravity(Gravity.CENTER);
            option.setCompoundDrawablePadding(dp(12));
            option.setTextSize(16);
            option.setMinHeight(dp(62));
            option.setOnClickListener(view -> {
                draftReportCategory = category;
                showReport(type);
            });
            root.addView(option, contentParams(-1, dp(68), dp(8)));
        }

        addWizardSectionTitle(root, "Report type");
        LinearLayout typeChoices = new LinearLayout(this);
        typeChoices.setOrientation(LinearLayout.HORIZONTAL);
        TextView lost = reportTypeToggle(
                LanguageManager.profileText(this, "lost_theft_button"),
                "LOST".equalsIgnoreCase(type),
                LOST_GREEN,
                LOST_GREEN_ON
        );
        TextView found = reportTypeToggle(
                translate("FOUND"),
                "FOUND".equalsIgnoreCase(type),
                FOUND_GOLD,
                FOUND_GOLD_ON
        );
        if (editingReportId != null) {
            lost.setEnabled(false);
            found.setEnabled(false);
            lost.setAlpha("LOST".equalsIgnoreCase(type) ? 1f : 0.55f);
            found.setAlpha("FOUND".equalsIgnoreCase(type) ? 1f : 0.55f);
        }
        lost.setOnClickListener(view -> showReport("LOST"));
        found.setOnClickListener(view -> showReport("FOUND"));
        typeChoices.addView(lost, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams foundParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        foundParams.setMargins(dp(8), 0, 0, 0);
        typeChoices.addView(found, foundParams);
        addField(root, typeChoices);
        addWizardNavigation(root, type, 2);
    }

    private void renderReportWizardLocationStep(
            LinearLayout root,
            String type,
            EditText location,
            EditText city
    ) {
        addWizardSectionTitle(root, "Add a photo and location");
        addLabeledField(root, translate("FOUND".equalsIgnoreCase(type) ? "Place found" : "Last seen at"), location);
        addLabeledField(root, localizeReportWizardText("City / area tag"), city);
        city.setHint(localizeReportWizardText("e.g. Nagpur"));

        boolean locationEnabled = isReportLocationEnabled(type);
        TextView preciseLocation = text(
                locationEnabled ? localizeReportWizardText("Precise location ready")
                        : localizeReportWizardText("Use precise location (optional)"),
                12,
                locationEnabled ? Color.WHITE : secondaryTextColor(),
                Typeface.BOLD
        );
        preciseLocation.setGravity(Gravity.CENTER);
        preciseLocation.setBackground(roundWithStroke(
                locationEnabled ? LOST_GREEN : surfaceColor(),
                12,
                locationEnabled ? LOST_GREEN : fieldBorderColor()
        ));
        root.addView(preciseLocation, contentParams(-1, dp(44), dp(10)));
        preciseLocation.setOnClickListener(view -> {
            if (isReportLocationEnabled(type)) {
                setReportLocationState(type, false, 0.0, 0.0);
                locationRequestGeneration++;
                stopActiveLocationUpdates();
                hasLocation = false;
                activeLocationReportType = null;
                currentLat = 0.0;
                currentLng = 0.0;
                showReport(type);
            } else {
                locationStatus = preciseLocation;
                locationToggleStatus = preciseLocation;
                requestLocation(preciseLocation, preciseLocation);
            }
        });
        root.addView(imageSlots(), contentParams(-1, dp(104), dp(12)));
        addWizardNavigation(root, type, 3);
    }

    private void renderReportWizardDetailsStep(
            LinearLayout root,
            String type,
            EditText item,
            EditText identifier,
            EditText description,
            EditText date,
            EditText imei
    ) {
        addWizardSectionTitle(root, "Add identifying details");
        addLabeledField(root, localizeReportWizardText("Report title"), item);
        item.setHint(reportCategoryHint("e.g. Blue backpack", "e.g. Brown dog", "e.g. Missing person"));
        addLabeledField(root, reportIdentifierLabel(), identifier);
        identifier.setHint(reportIdentifierHint());
        addLabeledField(root, localizeReportWizardText("Other identifying details"), description);
        description.setMinLines(3);
        description.setHint(localizeReportWizardText(
                "Describe color, brand, appearance, or other helpful details"
        ));
        addLabeledDateField(root, translate("FOUND".equalsIgnoreCase(type) ? "Date found" : "Date lost"), date);
        if ("item".equals(draftReportCategory) && "LOST".equalsIgnoreCase(type) && editingReportId == null) {
            addLabeledField(root, localizeReportWizardText("Optional 15-digit IMEI"), imei);
            TextView disclosure = text(
                    "IMEI is stored as a keyed hash for SafeTrade checks.",
                    11,
                    secondaryTextColor(),
                    Typeface.NORMAL
            );
            addField(root, disclosure);
        }
        addWizardNavigation(root, type, 4);
    }

    private void renderReportWizardReviewStep(
            LinearLayout root,
            String type,
            EditText item,
            EditText description,
            EditText location,
            EditText city,
            EditText date,
            EditText identifier,
            EditText imei
    ) {
        String submissionDescription = reportDetailsForSubmission(description.getText().toString(), identifier.getText().toString());
        String locationValue = reportLocationForSubmission(location.getText().toString(), city.getText().toString());
        addWizardSectionTitle(root, "Review your report");
        addWizardSummaryRow(root, "Category", reportCategoryDisplayName());
        addWizardSummaryRow(root, "Report type", translate(type));
        addWizardSummaryRow(root, "Title", item.getText().toString().trim());
        addWizardSummaryRow(root, "Photo", localizeReportWizardText(
                reportHasPhoto() ? "Photo attached" : "No photo attached"
        ));
        addWizardPhotoPreview(root);
        addWizardSummaryRow(root, "Location", locationValue);
        addWizardSummaryRow(root, "Date", date.getText().toString().trim());
        addWizardSummaryRow(root, "Identifying details", submissionDescription);
        if (editingReportId == null) {
            CheckBox shareConsent = new CheckBox(this);
            shareConsent.setText(localizeReportWizardText(
                    "Allow Fendly to share this report on its Facebook and Instagram accounts"
            ));
            shareConsent.setTextColor(primaryTextColor());
            shareConsent.setChecked(draftSocialShareConsent);
            shareConsent.setOnCheckedChangeListener((button, checked) -> draftSocialShareConsent = checked);
            addField(root, shareConsent);
            TextView shareDisclosure = text(
                    localizeReportWizardText(
                            "Optional. Report type and title may be public. Facebook/Instagram may receive the generated poster and selected photo. Details, location, contact info and IMEI stay private. Instagram needs a public JPEG poster; Reels are not supported."
                    ),
                    12,
                    secondaryTextColor(),
                    Typeface.NORMAL
            );
            addField(root, shareDisclosure);
        }

        CheckBox guidelinesConsent = new CheckBox(this);
        guidelinesConsent.setText(localizeReportWizardText(
                "I agree to follow Fendly's Community Guidelines"
        ));
        guidelinesConsent.setTextColor(primaryTextColor());
        guidelinesConsent.setChecked(draftGuidelinesAccepted);
        guidelinesConsent.setOnCheckedChangeListener(
                (button, checked) -> draftGuidelinesAccepted = checked
        );
        addField(root, guidelinesConsent);
        TextView guidelinesLink = actionButton(
                localizeReportWizardText("Read Community Guidelines"),
                false
        );
        guidelinesLink.setOnClickListener(view -> openCommunityGuidelines());
        addField(root, guidelinesLink);

        TextView editCategory = actionButton(localizeReportWizardText("Edit category"), false);
        editCategory.setOnClickListener(view -> {
            reportWizardStep = 1;
            showReport(type);
        });
        addField(root, editCategory);
        TextView editLocation = actionButton(localizeReportWizardText("Edit photo and location"), false);
        editLocation.setOnClickListener(view -> {
            reportWizardStep = 2;
            showReport(type);
        });
        addField(root, editLocation);
        TextView editDetails = actionButton(localizeReportWizardText("Edit identifying details"), false);
        editDetails.setOnClickListener(view -> {
            reportWizardStep = 3;
            showReport(type);
        });
        addField(root, editDetails);

        if (editingReportId != null && type.equalsIgnoreCase(editingReportType)) {
            TextView save = actionButton(translate("Save changes"), true);
            save.setOnClickListener(view -> {
                if (!validateWizardReport(item, submissionDescription, locationValue, date)) return;
                location.setText(locationValue);
                description.setText(submissionDescription);
                updateItem(type, item, description, location, date, save, draftGuidelinesAccepted);
            });
            addField(root, save);
            return;
        }
        if ("LOST".equalsIgnoreCase(type) && !hasActiveAnnualSubscription()) {
            addField(root, subscriptionCard());
            TextView payment = filledButton(
                    translate("Continue to payment"),
                    LOST_GREEN,
                    LOST_GREEN_ON
            );
            payment.setOnClickListener(view -> {
                if (!validateWizardReport(item, submissionDescription, locationValue, date)) return;
                location.setText(locationValue);
                description.setText(submissionDescription);
                draftImei = imei.getText().toString().trim();
                showPaymentOptions(item, description, location, date);
            });
            addField(root, payment);
            return;
        }

        TextView submit = actionButton(translate("Submit report — free"), true);
        submit.setOnClickListener(view -> {
            if (!validateWizardReport(item, submissionDescription, locationValue, date)) return;
            location.setText(locationValue);
            description.setText(submissionDescription);
            draftImei = imei.getText().toString().trim();
            String paymentId = "LOST".equalsIgnoreCase(type)
                    ? getSharedPreferences("fendly_account", MODE_PRIVATE)
                            .getString("annual_subscription_payment_id", null)
                    : null;
            submitItem(type, item, description, location, date, submit, paymentId);
        });
        addField(root, submit);
    }

    private void addWizardNavigation(LinearLayout root, String type, int nextStep) {
        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        if (reportWizardStep > 1) {
            TextView back = actionButton(localizeReportWizardText("Back"), false);
            back.setOnClickListener(view -> {
                reportWizardStep--;
                showReport(type);
            });
            navigation.addView(back, new LinearLayout.LayoutParams(0, dp(46), 1));
        }
        TextView next = actionButton(localizeReportWizardText("Continue"), true);
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        if (reportWizardStep > 1) nextParams.setMargins(dp(8), 0, 0, 0);
        navigation.addView(next, nextParams);
        next.setOnClickListener(view -> {
            if (editingReportId == null && reportWizardStep == 2
                    && (draftCityTag.trim().isEmpty() || draftLocation.trim().isEmpty())) {
                Toast.makeText(this, localizeReportWizardText("Add a location and city tag."), Toast.LENGTH_LONG).show();
                return;
            }
            if (reportWizardStep == 3 && draftItem.trim().isEmpty()) {
                Toast.makeText(this, localizeReportWizardText("Enter a report title to continue."), Toast.LENGTH_LONG).show();
                return;
            }
            if (reportWizardStep == 3 && reportDetailsForSubmission(
                    draftDescription,
                    draftIdentifier
            ).trim().isEmpty()) {
                Toast.makeText(this, localizeReportWizardText("Add at least one identifying detail."), Toast.LENGTH_LONG).show();
                return;
            }
            reportWizardStep = nextStep;
            showReport(type);
        });
        addField(root, navigation);
    }

    private void addWizardSectionTitle(LinearLayout root, String title) {
        TextView heading = text(
                localizeReportWizardText(title),
                17,
                primaryTextColor(),
                Typeface.BOLD
        );
        addField(root, heading);
    }

    private void addWizardSummaryRow(LinearLayout root, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(roundWithStroke(surfaceColor(), 12, borderColor()));
        TextView labelView = text(
                localizeReportWizardText(label),
                10,
                secondaryTextColor(),
                Typeface.BOLD
        );
        row.addView(labelView, new LinearLayout.LayoutParams(-1, -2));
        String summary = value == null || value.trim().isEmpty() ? "—" : value.trim();
        TextView valueView = text(summary, 13, primaryTextColor(), Typeface.NORMAL);
        row.addView(valueView, new LinearLayout.LayoutParams(-1, -2));
        addField(root, row);
    }

    private void addWizardPhotoPreview(LinearLayout root) {
        Bitmap cameraPhoto = reportCameraImages.length > 0 ? reportCameraImages[0] : null;
        Uri selectedPhoto = reportImages.length > 0 ? reportImages[0] : null;
        String existingPhoto = editingReportImageUrl == null ? "" : editingReportImageUrl.trim();
        if (cameraPhoto == null && selectedPhoto == null && existingPhoto.isEmpty()) return;
        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        if (cameraPhoto != null) {
            preview.setImageBitmap(cameraPhoto);
        } else if (selectedPhoto != null) {
            Glide.with(this).load(selectedPhoto).centerCrop().into(preview);
        } else {
            Glide.with(this).load(existingPhoto).centerCrop().into(preview);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(132), dp(132));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.setMargins(0, dp(5), 0, dp(8));
        root.addView(preview, params);
    }

    private String reportDetailsForSubmission(String details, String identifier) {
        String cleanDetails = details == null ? "" : details.trim();
        String cleanIdentifier = identifier == null ? "" : identifier.trim();
        if (cleanIdentifier.isEmpty()) return cleanDetails;
        if (cleanDetails.isEmpty()) return "Identifying details: " + cleanIdentifier;
        return cleanDetails + "\nIdentifying details: " + cleanIdentifier;
    }

    private String reportLocationForSubmission(String location, String city) {
        String cleanLocation = location == null ? "" : location.trim();
        String cleanCity = city == null ? "" : city.trim();
        if (cleanCity.isEmpty()) return cleanLocation;
        if (cleanLocation.isEmpty()) return cleanCity;
        if (cleanLocation.toLowerCase(Locale.US).contains(cleanCity.toLowerCase(Locale.US))) {
            return cleanLocation;
        }
        return cleanLocation + ", " + cleanCity;
    }

    private boolean reportHasPhoto() {
        for (int index = 0; index < reportImages.length; index++) {
            if (reportImages[index] != null || reportCameraImages[index] != null) return true;
        }
        return editingReportImageUrl != null && !editingReportImageUrl.trim().isEmpty();
    }

    private boolean validateWizardReport(EditText item, String details, String location, EditText date) {
        if (!draftGuidelinesAccepted) {
            Toast.makeText(
                    this,
                    localizeReportWizardText("Please accept the Community Guidelines to continue."),
                    Toast.LENGTH_LONG
            ).show();
            return false;
        }
        if (item.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, localizeReportWizardText("Enter a report title."), Toast.LENGTH_LONG).show();
            return false;
        }
        if (details.trim().isEmpty()) {
            Toast.makeText(this, localizeReportWizardText("Add at least one identifying detail."), Toast.LENGTH_LONG).show();
            return false;
        }
        if (editingReportId == null && (draftCityTag.trim().isEmpty() || location.trim().isEmpty())) {
            Toast.makeText(this, localizeReportWizardText("Add a location and city tag."), Toast.LENGTH_LONG).show();
            return false;
        }
        String dateValue = date.getText().toString().trim();
        if (!dateValue.isEmpty() && !validDate(dateValue)) {
            date.setError("Use a valid date in DD/MM/YYYY format");
            return false;
        }
        if ("item".equals(draftReportCategory) && "LOST".equalsIgnoreCase(currentReportType)
                && !draftImei.isEmpty() && !draftImei.matches("[0-9]{15}")) {
            Toast.makeText(this, "IMEI must contain exactly 15 digits", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private String backendReportCategory() {
        if ("pet".equals(draftReportCategory)) return "Animals";
        if ("person".equals(draftReportCategory)) return "People";
        return "Items";
    }

    private String reportCategoryDisplayName() {
        if ("pet".equals(draftReportCategory)) return localizeReportWizardText("Pet / Animal");
        if ("person".equals(draftReportCategory)) return localizeReportWizardText("Missing Person");
        return localizeReportWizardText("Item / Valuables");
    }

    private String reportIdentifierLabel() {
        if ("pet".equals(draftReportCategory)) return localizeReportWizardText("Pet identifiers");
        if ("person".equals(draftReportCategory)) return localizeReportWizardText("Person identifiers");
        return localizeReportWizardText("Serial number or other identifier");
    }

    private String reportIdentifierHint() {
        if ("pet".equals(draftReportCategory)) return localizeReportWizardText("Collar color, tag, breed, or microchip");
        if ("person".equals(draftReportCategory)) return localizeReportWizardText("Distinct clothing, appearance, or accessories");
        return localizeReportWizardText("Serial number, brand, model, or distinguishing mark");
    }

    private String reportCategoryHint(String itemHint, String petHint, String personHint) {
        if ("pet".equals(draftReportCategory)) return localizeReportWizardText(petHint);
        if ("person".equals(draftReportCategory)) return localizeReportWizardText(personHint);
        return localizeReportWizardText(itemHint);
    }

    private void showPaymentOptions(EditText item, EditText description, EditText location, EditText date) {
        inRenewalPaymentFlow = false;
        currentPage = PAGE_SUBSCRIPTION;
        LinearLayout root = screenBase("Payment");
        addHeading("Choose payment method", "Complete your lost report payment");

        TextView upi = filledButton("Pay with UPI", LOST_GREEN, LOST_GREEN_ON);
        upi.setOnClickListener(view -> startPayment(item, description, location, date, upi));
        addField(root, upi);

        TextView card = actionButton("Debit / Credit card", false);
        card.setOnClickListener(view -> startPayment(item, description, location, date, card));
        addField(root, card);

        TextView wallet = actionButton("Wallet / Paytm", false);
        wallet.setOnClickListener(view -> startPayment(item, description, location, date, wallet));
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
        pendingPaymentLocation = null;
        pendingPaymentDate = null;
        pendingPaymentButton = button;
        button.setText(getString(R.string.payment_opening_checkout));
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
                        options.put("description", "Subscription renewal");
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

    private void startPayment(EditText item, EditText description, EditText location, EditText date, TextView button) {
        pendingPaymentItem = item;
        pendingPaymentDescription = description;
        pendingPaymentLocation = location;
        pendingPaymentDate = date;
        pendingPaymentButton = button;
        button.setText(getString(R.string.payment_opening_checkout));
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
            pendingPaymentButton.setText(getString(R.string.payment_retry));
            pendingPaymentButton.setEnabled(true);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean hasActiveAnnualSubscription() {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean active = account.getBoolean("annual_subscription_active", false);
        long expiresAt = account.getLong("annual_subscription_expires_at", 0L);
        boolean belongsToCurrentUser = user != null
            && user.getUid().equals(account.getString("annual_subscription_firebase_uid", ""));
        boolean stillValid = belongsToCurrentUser && active && expiresAt > 0L && System.currentTimeMillis() < expiresAt;
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        long expiresAt = account.getLong("annual_subscription_expires_at", 0L);
        boolean belongsToCurrentUser = user != null
            && user.getUid().equals(account.getString("annual_subscription_firebase_uid", ""));
        if (!belongsToCurrentUser || !account.getBoolean("annual_subscription_active", false) || expiresAt <= 0L) {
            return translate("Renew plan");
        }
        if (System.currentTimeMillis() >= expiresAt) {
            account.edit().putBoolean("annual_subscription_active", false).remove("annual_subscription_payment_id").apply();
            return translate("Renew plan");
        }
        Locale dateLocale = Locale.forLanguageTag(LanguageManager.getSavedLanguage(this));
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy", dateLocale);
        return translate("Active subscription until") + " " + format.format(new Date(expiresAt));
    }

    private void activateAnnualSubscription(String paymentId) {
        SharedPreferences.Editor editor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
            .putBoolean("annual_subscription_active", true)
            .putLong("annual_subscription_expires_at", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365L))
            .putString("annual_subscription_payment_id", paymentId);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) editor.putString("annual_subscription_firebase_uid", user.getUid());
        editor.apply();
    }

    private void refreshAnnualSubscription(Runnable onComplete) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        user.getIdToken(false).addOnSuccessListener(tokenResult -> network.execute(() -> {
            String response = getAuthorized("/api/payments/subscription", tokenResult.getToken());
            boolean loaded = false;
            boolean active = false;
            long expiresAt = 0L;
            String paymentId = "";
            try {
                if (response != null) {
                    JSONObject subscription = new JSONObject(response);
                    active = subscription.optBoolean("active", false);
                    expiresAt = subscription.optLong("expires_at", 0L);
                    paymentId = subscription.optString("payment_id", "");
                    loaded = true;
                }
            } catch (Exception ignored) {
            }

            final boolean subscriptionActive = active;
            final boolean subscriptionLoaded = loaded;
            if (loaded) {
                SharedPreferences.Editor editor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                        .putString("annual_subscription_firebase_uid", user.getUid())
                        .putBoolean("annual_subscription_active", active)
                        .putLong("annual_subscription_expires_at", expiresAt);
                if (active && !paymentId.isEmpty()) {
                    editor.putString("annual_subscription_payment_id", paymentId);
                } else {
                    editor.remove("annual_subscription_payment_id");
                }
                editor.apply();
            }

            runOnUiThread(() -> {
                if (subscriptionLoaded && subscriptionActive && currentPage == PAGE_REPORT && screenRenderer != null) {
                    screenRenderer.run();
                }
                if (onComplete != null) onComplete.run();
            });
        })).addOnFailureListener(error -> {
            if (onComplete != null) runOnUiThread(onComplete);
        });
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
                if (pendingPaymentItem == null && pendingPaymentDescription == null && pendingPaymentLocation == null && pendingPaymentDate == null) {
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show();
                    showProfile();
                    return;
                }
                submitItem("LOST", pendingPaymentItem, pendingPaymentDescription, pendingPaymentLocation, pendingPaymentDate, pendingPaymentButton, paymentId);
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

    private void submitItem(String type, EditText item, EditText description, EditText location, EditText date, TextView publish, String paymentId) {
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
        String dateValue = date.getText().toString().trim();
        if (!dateValue.isEmpty() && !validDate(dateValue)) {
            date.setError("Use a valid date in DD/MM/YYYY format");
            return;
        }
        if ("LOST".equalsIgnoreCase(type) && "item".equals(draftReportCategory) && !draftImei.isEmpty()
                && !draftImei.matches("[0-9]{15}")) {
            Toast.makeText(this, "IMEI must contain exactly 15 digits", Toast.LENGTH_LONG).show();
            return;
        }
        if (editingReportId != null && type.equalsIgnoreCase(editingReportType)) {
            updateItem(type, item, description, location, date, publish, draftGuidelinesAccepted);
            return;
        }
        publish.setText(getString(R.string.report_submitting));
        publish.setEnabled(false);
        Uri[] images = reportImages.clone();
        Bitmap[] cameraImages = reportCameraImages.clone();
        boolean reportHasLocation = isReportLocationEnabled(type);
        double latitude = reportHasLocation ? reportLocationLatitude(type) : 0.0;
        double longitude = reportHasLocation ? reportLocationLongitude(type) : 0.0;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            publish.setText(getString(R.string.report_sign_in_required));
            publish.setEnabled(true);
            return;
        }
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> {
            if (token == null || token.getToken() == null) {
                publish.setText(getString(R.string.auth_unavailable));
                publish.setEnabled(true);
                return;
            }
            boolean shareConsent = draftSocialShareConsent;
            network.execute(() -> {
                ItemSubmissionResult submission = postItem(
                        type, title, details, location.getText().toString().trim(),
                        date.getText().toString().trim(), latitude, longitude,
                        images, cameraImages, token.getToken(), paymentId,
                        "LOST".equalsIgnoreCase(type) && "item".equals(draftReportCategory)
                                ? draftImei
                                : "",
                        shareConsent);
                runOnUiThread(() -> {
                    publish.setEnabled(true);
                    int code = submission.statusCode;
                    if (code >= 200 && code < 300) {
                        boolean paidLostReport = paymentId != null && "LOST".equalsIgnoreCase(type);
                        String message = ReportSubmissionMessages.buildSubmissionSuccessMessage(paidLostReport, submission.errorMessage, null);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        clearReportDraftState();
                        showReports();
                    } else {
                        if (code == 402 && "LOST".equalsIgnoreCase(type)) {
                            publish.setText(getString(R.string.report_continue_payment));
                            publish.setOnClickListener(view ->
                                    showPaymentOptions(item, description, location, date));
                        } else {
                            publish.setText(getString(R.string.report_retry_submission));
                        }
                        String detail = submission.errorMessage == null || submission.errorMessage.trim().isEmpty()
                                ? "Could not save report (" + code + ")"
                                : submission.errorMessage;
                        Toast.makeText(this, detail, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }).addOnFailureListener(error -> {
            publish.setText(getString(R.string.report_retry_submission));
            publish.setEnabled(true);
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show();
        });
    }

    private static final class ItemSubmissionResult {
        final int statusCode;
        final String errorMessage;

        ItemSubmissionResult(int statusCode, String errorMessage) {
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }
    }

    private ItemSubmissionResult postItem(String type, String title, String description, String location, String date, double latitude, double longitude, Uri[] images, Bitmap[] cameraImages, String idToken, String paymentId, String imeiNumber, boolean socialShareConsent) {
        lastSubmissionError = null;
        try {
            List<String> imageUrls = new ArrayList<>();
            for (int slot = 0; slot < Math.min(images.length, cameraImages.length); slot++) {
                if (images[slot] == null && cameraImages[slot] == null) continue;
                String uploadedUrl = uploadImage(images[slot], cameraImages[slot], idToken);
                if (uploadedUrl == null || uploadedUrl.trim().isEmpty()) {
                    lastSubmissionError = "one or more images could not be uploaded";
                } else {
                    imageUrls.add(uploadedUrl);
                }
            }
            String imageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);

            AiMatchService.ApiResponse response = AiMatchService.createItem(
                    title, description, imageUrl, imageUrls, type, latitude, longitude,
                    location, date, paymentId,
                    getTtsLocaleForSelectedLanguage().getLanguage(), idToken, imeiNumber,
                    backendReportCategory(), socialShareConsent,
                    draftGuidelinesAccepted);
            if (!response.isSuccessful()) {
                lastSubmissionError = "Could not save report (" + response.getStatusCode() + "): " + response.getErrorMessage();
                return new ItemSubmissionResult(response.getStatusCode(), lastSubmissionError);
            }

            return new ItemSubmissionResult(response.getStatusCode(), lastSubmissionError);
        } catch (Exception error) {
            lastSubmissionError = "Could not save report: " + error.getClass().getSimpleName();
            return new ItemSubmissionResult(-1, lastSubmissionError);
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

    private void updateItem(
            String type,
            EditText item,
            EditText description,
            EditText location,
            EditText date,
            TextView saveButton,
            boolean guidelinesAccepted
    ) {
        if (!guidelinesAccepted) {
            Toast.makeText(
                    this,
                    localizeReportWizardText("Please accept the Community Guidelines to continue."),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        saveButton.setText(translate("Saving..."));
        saveButton.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String imageUrl = editingReportImageUrl;
            if (selectedImage != null || capturedImage != null) imageUrl = uploadImage(selectedImage, capturedImage, token.getToken());
            String details = description.getText().toString().trim();
            String locationValue = location.getText().toString().trim();
            String dateValue = date.getText().toString().trim();
            int code = putItem(
                    type,
                    editingReportId,
                    item.getText().toString().trim(),
                    details,
                    locationValue,
                    dateValue,
                    imageUrl,
                    token.getToken(),
                    guidelinesAccepted
            );
            runOnUiThread(() -> {
                saveButton.setEnabled(true);
                if (code >= 200 && code < 300) {
                    editingReportId = null;
                    editingReportType = null;
                    editingReportImageUrl = null;
                    Toast.makeText(this, "Report updated", Toast.LENGTH_SHORT).show();
                    showReports();
                } else {
                    saveButton.setText(translate("Retry update"));
                        String message = code == 404
                            ? "Report update API is not deployed yet"
                            : "Could not update report (" + code + ")";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            });
        })).addOnFailureListener(error -> {
            saveButton.setText(translate("Retry update"));
            saveButton.setEnabled(true);
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show();
        });
    }

    private int putItem(
            String type,
            String id,
            String title,
            String description,
            String location,
            String date,
            String imageUrl,
            String idToken,
            boolean guidelinesAccepted
    ) {
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
            String body = "{\"title\":\"" + escapeJson(title) + "\",\"description\":\"" + escapeJson(description) + "\",\"source_language\":\"" + getTtsLocaleForSelectedLanguage().getLanguage() + "\",\"report_location\":\"" + escapeJson(location) + "\",\"report_date\":\"" + escapeJson(date) + "\",\"category\":\"" + escapeJson(backendReportCategory()) + "\",\"community_guidelines_accepted\":" + guidelinesAccepted + ",\"lat\":0.0,\"lng\":0.0,\"image_url\":" + imageJson + "}";
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
        DatePickerDialog picker = new DatePickerDialog(this, (dialog, year, month, day) -> target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)), now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }

    private void uploadProfilePhotoBackground(Uri imageUri, Bitmap cameraBitmap) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String imageUrl = uploadImage(imageUri, cameraBitmap, token.getToken());
            if (imageUrl == null || imageUrl.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_LONG).show());
                return;
            }

            final String finalImageUrl = imageUrl;
            SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
            account.edit().putString("profile_image_url", finalImageUrl).apply();

            selectedProfileImage = null;
            capturedProfileImage = null;

            String username = account.getString("username", "");
            String fullName = account.getString("full_name", "");
            String email = account.getString("email", "");
            String mobile = account.getString("mobile", "");
            String state = account.getString("state", "");
            String city = account.getString("city", "");

            saveCloudProfileDocument(user, username, fullName, email, mobile, state, city, finalImageUrl, null);

            runOnUiThread(() -> {
                if (currentPage == PAGE_PROFILE) showProfile();
            });
        })).addOnFailureListener(error -> {
            Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_LONG).show();
        });
    }

    private void showProfilePhotoOptions() {
        boolean hasPicture = hasProfilePicture();
        Dialog dialog = new Dialog(this);
        LinearLayout content = themedDialogContent(R.drawable.ic_field_person, "Profile photo", "Choose an action for your profile picture");

        TextView seePhoto = actionButton("See profile picture", false);
        seePhoto.setOnClickListener(v -> {
            dialog.dismiss();
            showEnlargedProfilePicture();
        });
        addFieldToDialog(content, seePhoto);

        TextView choosePhoto = actionButton("Choose profile picture", false);
        choosePhoto.setOnClickListener(v -> {
            dialog.dismiss();
            showChoosePhotoSourceOptions();
        });
        addFieldToDialog(content, choosePhoto);

        if (hasPicture) {
            TextView removePhoto = actionButton("Remove profile picture", false);
            removePhoto.setTextColor(Color.RED);
            removePhoto.setOnClickListener(v -> {
                dialog.dismiss();
                removeProfilePicture();
            });
            addFieldToDialog(content, removePhoto);
        }

        TextView cancel = actionButton("Cancel", true);
        cancel.setOnClickListener(v -> dialog.dismiss());
        addFieldToDialog(content, cancel);

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void clearProfileImageSelectionState(boolean explicitlyRemoved) {
        selectedProfileImage = null;
        capturedProfileImage = null;
        profileImageExplicitlyRemoved = explicitlyRemoved;
    }

    private void removeProfilePicture() {
        clearProfileImageSelectionState(true);
        try {
            File dir = getFilesDir();
            File[] files = dir.listFiles((dir1, name) -> name != null && name.startsWith("profile_photo"));
            if (files != null) {
                for (File f : files) f.delete();
            }
        } catch (Exception ignored) {}

        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit()
                .remove("profile_image_uri")
                .remove("profile_image_url")
            .remove("profile_image_cache_url")
                .apply();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> update = new LinkedHashMap<>();
            update.put("imageUrl", "");
            update.put("profile_image_url", "");
            update.put("profile_photo_url", "");
            update.put("profile_image_removed", true);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String docKey = getProfileDocumentKey();
            db.collection("users").document(docKey).set(update, SetOptions.merge());
            if (!user.getUid().equals(docKey)) {
                db.collection("users").document(user.getUid()).set(update, SetOptions.merge());
            }
            syncProfileToBackendApi(account.getString("username", ""), account.getString("full_name", ""), account.getString("email", ""), account.getString("mobile", ""), account.getString("state", ""), account.getString("city", ""), "");
        }
        if (currentPage == PAGE_PROFILE) showProfile();
        else if (currentPage == PAGE_PROFILE_SETUP) showProfileSetup();
        Toast.makeText(this, translate("Profile picture removed"), Toast.LENGTH_SHORT).show();
    }

    private void showChoosePhotoSourceOptions() {
        Dialog dialog = new Dialog(this);
        LinearLayout content = themedDialogContent(R.drawable.ic_field_person, "Choose profile picture", "Select source for your profile picture");

        TextView gallery = actionButton("Choose from gallery", false);
        gallery.setOnClickListener(v -> {
            dialog.dismiss();
            openProfilePhotoPicker();
        });
        addFieldToDialog(content, gallery);

        TextView camera = actionButton("Take photo", false);
        camera.setOnClickListener(v -> {
            dialog.dismiss();
            openProfilePhotoCamera();
        });
        addFieldToDialog(content, camera);

        TextView cancel = actionButton("Cancel", true);
        cancel.setOnClickListener(v -> dialog.dismiss());
        addFieldToDialog(content, cancel);

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void addFieldToDialog(LinearLayout parent, View child) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(44));
        params.setMargins(0, 0, 0, dp(10));
        parent.addView(child, params);
    }

    private void showEnlargedProfilePicture() {
        Dialog imageDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);
        layout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (profileImageExplicitlyRemoved) {
            imageView.setImageResource(R.drawable.ic_field_person);
            imageView.setColorFilter(Color.WHITE);
            imageView.setPadding(dp(80), dp(80), dp(80), dp(80));
            imageView.setOnClickListener(v -> imageDialog.dismiss());
            layout.setOnClickListener(v -> imageDialog.dismiss());
            layout.addView(imageView);
            imageDialog.setContentView(layout);
            imageDialog.show();
            return;
        }

        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String savedProfileUri = account.getString("profile_image_uri", null);
        String cloudImageUrl = account.getString("profile_image_url", "").trim();

        if (capturedProfileImage != null) {
            imageView.setImageBitmap(capturedProfileImage);
        } else if (selectedProfileImage != null) {
            Glide.with(this).load(selectedProfileImage).into(imageView);
        } else if (savedProfileUri != null && !savedProfileUri.trim().isEmpty()) {
            if (savedProfileUri.startsWith("content://") || savedProfileUri.startsWith("file://") || savedProfileUri.startsWith("http://") || savedProfileUri.startsWith("https://")) {
                Glide.with(this).load(Uri.parse(savedProfileUri)).into(imageView);
            } else {
                Glide.with(this).load(new File(savedProfileUri)).into(imageView);
            }
        } else if (!cloudImageUrl.isEmpty()) {
            Glide.with(this).load(cloudImageUrl).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_field_person);
        }

        imageView.setOnClickListener(v -> imageDialog.dismiss());
        layout.setOnClickListener(v -> imageDialog.dismiss());
        layout.addView(imageView);
        imageDialog.setContentView(layout);
        imageDialog.show();
    }

    private boolean hasProfilePicture() {
        if (profileImageExplicitlyRemoved) return false;
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String savedProfileUri = account.getString("profile_image_uri", null);
        String cloudImageUrl = account.getString("profile_image_url", "").trim();
        return (selectedProfileImage != null) ||
               (capturedProfileImage != null) ||
               (savedProfileUri != null && !savedProfileUri.trim().isEmpty()) ||
               (!cloudImageUrl.isEmpty());
    }

    private boolean bindProfilePhoto(ImageView avatar, SharedPreferences account) {
        if (avatar == null) return false;
        if (profileImageExplicitlyRemoved) {
            avatar.setImageResource(R.drawable.ic_field_person);
            avatar.setColorFilter(accentColor());
            avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int pad = dp(24);
            avatar.setPadding(pad, pad, pad, pad);
            return false;
        }
        boolean hasCustomPhoto = false;
        String savedProfileUri = account.getString("profile_image_uri", null);
        String cloudImageUrl = account.getString("profile_image_url", "").trim();
        if ((savedProfileUri == null || savedProfileUri.trim().isEmpty()) && !profileImageExplicitlyRemoved) {
            File cachedPhoto = new File(getFilesDir(), "profile_photo_cache.jpg");
            if (cachedPhoto.isFile()) {
                savedProfileUri = cachedPhoto.getAbsolutePath();
                account.edit().putString("profile_image_uri", savedProfileUri).apply();
            }
        }

        if (selectedProfileImage != null) {
            if (setImageFromUri(avatar, selectedProfileImage)) {
                hasCustomPhoto = true;
            }
        } else if (capturedProfileImage != null) {
            avatar.setImageBitmap(capturedProfileImage);
            hasCustomPhoto = true;
        }
        if (!hasCustomPhoto && savedProfileUri != null && !savedProfileUri.trim().isEmpty()) {
            Uri uri = savedProfileUri.startsWith("/") ? Uri.fromFile(new File(savedProfileUri)) : Uri.parse(savedProfileUri);
            if (setImageFromUri(avatar, uri)) {
                hasCustomPhoto = true;
                String cachedImageUrl = account.getString("profile_image_cache_url", "").trim();
                if (savedProfileUri.endsWith("profile_photo_cache.jpg") && !cloudImageUrl.isEmpty()
                        && !cloudImageUrl.equals(cachedImageUrl)) {
                    cacheProfilePhotoFromCloud(cloudImageUrl);
                }
            }
        }
        if (!hasCustomPhoto && !cloudImageUrl.isEmpty()) {
            loadCloudProfileImage(avatar);
            hasCustomPhoto = true;
        }

        if (hasCustomPhoto) {
            avatar.clearColorFilter();
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatar.setPadding(0, 0, 0, 0);
        } else {
            avatar.setImageResource(R.drawable.ic_field_person);
            avatar.setColorFilter(accentColor());
            avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int pad = dp(24);
            avatar.setPadding(pad, pad, pad, pad);
        }
        return hasCustomPhoto;
    }

    private void openProfilePhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PROFILE_IMAGE);
    }

    private void openProfilePhotoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PROFILE_CAMERA);
            return;
        }
        try {
            File imageDir = new File(getFilesDir(), "profile_photos");
            if (!imageDir.exists() && !imageDir.mkdirs()) {
                Toast.makeText(this, "Could not prepare camera capture", Toast.LENGTH_SHORT).show();
                return;
            }
            File imageFile = new File(imageDir, "profile_capture_" + System.currentTimeMillis() + ".jpg");
            pendingProfileCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pendingProfileCameraUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PROFILE_CAMERA);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap decodeProfileCameraBitmap(Uri uri, int maxDimension) {
        if (uri == null) return null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) return null;
                BitmapFactory.decodeStream(input, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
            int sample = 1;
            while ((bounds.outWidth / sample) > maxDimension || (bounds.outHeight / sample) > maxDimension) {
                sample *= 2;
            }
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = sample;
            decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap decoded;
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) return null;
                decoded = BitmapFactory.decodeStream(input, null, decodeOptions);
            }
            if (decoded == null) return null;

            int orientation = ExifInterface.ORIENTATION_NORMAL;
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input != null) {
                    ExifInterface exif = new ExifInterface(input);
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                }
            }
            return rotateBitmapToExifOrientation(decoded, orientation);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Bitmap rotateBitmapToExifOrientation(Bitmap bitmap, int orientation) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270);
                matrix.postScale(-1, 1);
                break;
            default:
                return bitmap;
        }
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return bitmap;
        }
    }

    private void refreshEmailVerificationState(EditText email, TextView verifyButton) {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Check Firestore-synced verification status first
        boolean cloudEmailVerified = account.getBoolean("email_verified", false);
        boolean firebaseEmailVerified = currentUser != null && currentUser.isEmailVerified();
        
        if (cloudEmailVerified || firebaseEmailVerified) {
            account.edit().putBoolean("email_verified", true).apply();
            cancelEmailVerificationCooldown();
            lockVerifiedEmailField(email, verifyButton);
        } else {
            long cooldownUntil = account.getLong("email_verify_cooldown_until", 0L);
            if (cooldownUntil > System.currentTimeMillis()) {
                startEmailVerificationCooldown(verifyButton, cooldownUntil, false);
                return;
            }
            verifyButton.setText(translate("Verify OTP"));
            verifyButton.setTextColor(GOLD_ON);
            verifyButton.setEnabled(true);
            verifyButton.setClickable(true);
            verifyButton.setFocusable(true);
            verifyButton.setBackground(round(GOLD, 24));
        }

        String savedEmail = account.getString("email", "").trim();
        if ((savedEmail == null || savedEmail.isEmpty()) && currentUser != null && currentUser.getEmail() != null) {
            String firebaseEmail = currentUser.getEmail().trim();
            if (!firebaseEmail.endsWith("@login.fendly.app")) {
                savedEmail = firebaseEmail;
                account.edit().putString("email", savedEmail).apply();
            }
        }
        if (savedEmail != null && !savedEmail.isEmpty() && !savedEmail.endsWith("@login.fendly.app") && email.getText().toString().trim().isEmpty()) {
            email.setText(savedEmail);
        }

        email.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                if (applyingCloudProfile || !email.hasFocus() || !email.isEnabled()) {
                    return;
                }
                String currentEmail = value.toString().trim().toLowerCase(Locale.US);
                SharedPreferences prefs = getSharedPreferences("fendly_account", MODE_PRIVATE);
                boolean isVerified = prefs.getBoolean("email_verified", false);
                String verifiedEmail = prefs.getString("email", "").trim().toLowerCase(Locale.US);

                if (isVerified && !currentEmail.isEmpty() && !verifiedEmail.isEmpty() && !verifiedEmail.equalsIgnoreCase(currentEmail)) {
                    prefs.edit().putBoolean("email_verified", false).apply();
                    verifyButton.setText(translate("Verify OTP"));
                    verifyButton.setEnabled(true);
                    verifyButton.setClickable(true);
                    verifyButton.setFocusable(true);
                    verifyButton.setBackground(round(GOLD, 24));
                    verifyButton.setTextColor(GOLD_ON);
                    verifyButton.setOnClickListener(v -> sendEmailOtpFlow(email, verifyButton));
                }
            }
            @Override public void afterTextChanged(Editable value) { }
        });
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
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        if (account.getBoolean("email_verified", false)) {
            cancelEmailVerificationCooldown();
            setVerifiedButtonState(verifyButton);
            return;
        }
        long remainingMs = cooldownUntil - System.currentTimeMillis();
        if (remainingMs <= 0L) {
            verifyButton.setEnabled(true);
            verifyButton.setClickable(true);
            verifyButton.setFocusable(true);
            verifyButton.setText(translate("Verify OTP"));
            verifyButton.setTextColor(GOLD_ON);
            verifyButton.setBackground(round(GOLD, 24));
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .remove("email_verify_cooldown_until")
                    .apply();
            emailVerificationCooldownRunnable = null;
            return;
        }

        verifyButton.setEnabled(false);
        int seconds = (int) Math.ceil(remainingMs / 1000.0D);
        verifyButton.setText(String.format(
                Locale.ROOT,
                "%1$s %2$ss",
                translate("Resend in"),
                localizeDigits(String.valueOf(seconds))
        ));
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
            // An active OTP exists within its 5-minute validity window. Re-open the OTP entry dialog!
            showEmailOtpDialog(emailValue, emailField, verifyButton);
            return;
        }

        verifyButton.setEnabled(false);
        verifyButton.setText(translate("Sending..."));
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            verifyButton.setText(translate("Enter OTP"));
            verifyButton.setEnabled(true);
            Toast.makeText(this, "Sign in required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String idToken = tokenResult != null ? tokenResult.getToken() : null;
            network.execute(() -> {
                boolean sent = false;
                String errorMessage = "Could not send email OTP";
                try {
                    if (idToken == null || idToken.isEmpty()) {
                        throw new IllegalStateException("Authentication token unavailable");
                    }
                    JSONObject payload = new JSONObject();
                    payload.put("email", emailValue);
                    JSONObject response = postJson("/api/auth/send-email-otp", payload.toString(), idToken);
                    if (response != null) {
                        sent = response.optBoolean("success", false);
                        if (!sent) {
                            errorMessage = response.optString("detail", response.optString("message", errorMessage));
                        }
                    }
                } catch (Exception error) {
                    errorMessage = error.getMessage() != null ? error.getMessage() : "Could not send email OTP";
                }
                final boolean finalSent = sent;
                final String finalErrorMessage = errorMessage;
                runOnUiThread(() -> {
                    verifyButton.setText(translate("Enter OTP"));
                    verifyButton.setEnabled(true);
                    if (!finalSent) {
                        Toast.makeText(this, finalErrorMessage, Toast.LENGTH_LONG).show();
                        return;
                    }
                    getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                            .putString("email", emailValue)
                            .apply();
                    long newCooldownUntil = System.currentTimeMillis() + EMAIL_VERIFICATION_COOLDOWN_MS;
                    startEmailVerificationCooldown(verifyButton, newCooldownUntil, true);
                    showEmailOtpDialog(emailValue, emailField, verifyButton);
                    Toast.makeText(this, "Verification code sent to your email", Toast.LENGTH_LONG).show();
                });
            });
        }).addOnFailureListener(error -> {
            verifyButton.setText(translate("Enter OTP"));
            verifyButton.setEnabled(true);
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void showEmailOtpDialog(String emailValue, EditText emailField, TextView verifyButton) {
        showThemedOtpDialog("Verify email", "Enter the 6-digit code sent to " + emailValue, "6-digit OTP",
            (otpValue, dialog, verifyInDialog, codeCells) -> {
                verifyEmailOtpCode(emailValue, otpValue, emailField, verifyButton, dialog, verifyInDialog, codeCells);
            }, () -> {
                verifyButton.setText(translate("Enter OTP"));
                verifyButton.setEnabled(true);
            }, () -> {
                sendEmailOtpFlow(emailField, verifyButton);
            });
    }

    private void verifyEmailOtpCode(String emailValue, String otpValue, EditText emailField, TextView verifyButton,
                                    Dialog dialog, TextView verifyInDialog, EditText[] codeCells) {
        verifyButton.setText(translate("Verifying..."));
        verifyButton.setEnabled(false);
        if (verifyInDialog != null) {
            verifyInDialog.setText(translate("Verifying..."));
            verifyInDialog.setEnabled(false);
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            verifyButton.setText(translate("Enter OTP"));
            verifyButton.setEnabled(true);
            if (verifyInDialog != null) {
                verifyInDialog.setText(translate("Verify"));
                verifyInDialog.setEnabled(true);
            }
            Toast.makeText(this, "Sign in required", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String idToken = tokenResult != null ? tokenResult.getToken() : null;
            network.execute(() -> {
                boolean verified = false;
                String errorMessage = "Could not verify email";
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("email", emailValue);
                    payload.put("otp", otpValue);
                    JSONObject response = postJson("/api/auth/verify-email-otp", payload.toString(), idToken);
                    if (response != null) {
                        verified = response.optBoolean("success", false);
                        if (!verified) {
                            errorMessage = response.optString("detail", response.optString("message", errorMessage));
                        }
                    }
                } catch (Exception error) {
                    errorMessage = error.getMessage() != null ? error.getMessage() : "Could not verify email";
                }

                final boolean finalVerified = verified;
                final String finalErrorMessage = errorMessage;
                runOnUiThread(() -> {
                    if (finalVerified) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        cancelEmailVerificationCooldown();
                        getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                .putString("email", emailValue)
                                .putBoolean("email_verified", true)
                                .remove("email_verify_cooldown_until")
                                .apply();
                        saveVerifiedEmailToCloud(emailValue);
                        lockVerifiedEmailField(emailField, verifyButton);
                        Toast.makeText(this, "Email verified successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Keep dialog open on failure & clear cells so user can re-enter correct OTP immediately
                        if (verifyInDialog != null) {
                            verifyInDialog.setText(translate("Verify"));
                            verifyInDialog.setEnabled(true);
                        }
                        if (codeCells != null) {
                            for (EditText cell : codeCells) {
                                if (cell != null) cell.setText("");
                            }
                            if (codeCells.length > 0 && codeCells[0] != null) {
                                codeCells[0].requestFocus();
                            }
                        }
                        verifyButton.setText(translate("Enter OTP"));
                        verifyButton.setEnabled(true);
                        verifyButton.setClickable(true);
                        verifyButton.setFocusable(true);
                        verifyButton.setBackground(round(GOLD, 24));
                        verifyButton.setTextColor(GOLD_ON);
                        Toast.makeText(this, finalErrorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }).addOnFailureListener(error -> {
            verifyButton.setText(translate("Enter OTP"));
            verifyButton.setEnabled(true);
            if (verifyInDialog != null) {
                verifyInDialog.setText(translate("Verify"));
                verifyInDialog.setEnabled(true);
            }
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
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
        sendBackendMobileOtp(mobileValue, mobile, verifyButton);
    }

    private void sendBackendMobileOtp(String mobileValue, Object mobileTarget, TextView verifyButton) {
        prepareSmsUserConsent();
        try {
            SmsRetriever.getClient(this).startSmsUserConsent(null)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("AUTH", "SMS User Consent could not start; OTP autofill may be unavailable",
                                    task.getException());
                        }
                        sendBackendMobileOtpRequest(mobileValue, mobileTarget, verifyButton);
                    });
        } catch (RuntimeException error) {
            Log.w("AUTH", "SMS User Consent is unavailable; continuing without OTP autofill", error);
            sendBackendMobileOtpRequest(mobileValue, mobileTarget, verifyButton);
        }
    }

    private void prepareSmsUserConsent() {
        stopSmsUserConsent();
        smsUserConsentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) return;
                Bundle extras = intent.getExtras();
                if (extras == null) return;
                com.google.android.gms.common.api.Status status =
                        extras.getParcelable(SmsRetriever.EXTRA_STATUS);
                if (status == null || status.getStatusCode() != com.google.android.gms.common.api.CommonStatusCodes.SUCCESS) {
                    if (status != null && status.getStatusCode() == com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT) {
                        Log.i("AUTH", "Timed out waiting for OTP SMS consent");
                    }
                    return;
                }
                Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                if (consentIntent == null) return;
                if (activeSmsOtpDialog != null && activeSmsOtpDialog.isShowing()) {
                    launchSmsUserConsent(consentIntent);
                } else {
                    pendingSmsUserConsentIntent = consentIntent;
                }
            }
        };
        try {
            ContextCompat.registerReceiver(
                    this,
                    smsUserConsentReceiver,
                    new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                    SmsRetriever.SEND_PERMISSION,
                    null,
                    ContextCompat.RECEIVER_EXPORTED);
            smsUserConsentReceiverRegistered = true;
        } catch (RuntimeException error) {
            smsUserConsentReceiver = null;
            Log.w("AUTH", "Could not register SMS User Consent receiver; OTP autofill may be unavailable", error);
        }
    }

    private void launchSmsUserConsent(Intent consentIntent) {
        try {
            startActivityForResult(consentIntent, REQUEST_SMS_USER_CONSENT);
        } catch (ActivityNotFoundException | SecurityException error) {
            Log.w("AUTH", "Could not open SMS User Consent prompt", error);
        }
    }

    private void stopSmsUserConsent() {
        if (smsUserConsentReceiverRegistered && smsUserConsentReceiver != null) {
            try {
                unregisterReceiver(smsUserConsentReceiver);
            } catch (IllegalArgumentException error) {
                Log.w("AUTH", "SMS User Consent receiver was already unregistered", error);
            }
        }
        smsUserConsentReceiver = null;
        smsUserConsentReceiverRegistered = false;
        pendingSmsUserConsentIntent = null;
        activeSmsOtpCells = null;
        activeSmsOtpDialog = null;
    }

    private void sendBackendMobileOtpRequest(String mobileValue, Object mobileTarget, TextView verifyButton) {
        network.execute(() -> {
            String errorMessage = "Could not send SMS verification code";
            boolean sent = false;
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", normalizeIndianMobileDigits(mobileValue));
                JSONObject response = postJson("/api/auth/send-otp", payload.toString(), null);
                if (response != null) {
                    sent = response.optBoolean("success", false);
                    errorMessage = response.optString("detail", response.optString("message", errorMessage));
                }
            } catch (Exception error) {
                Log.e("AUTH", "Could not request SMS verification code", error);
            }

            final boolean finalSent = sent;
            final String finalErrorMessage = errorMessage;
            runOnUiThread(() -> {
                verifyButton.setText(translate("Enter OTP"));
                verifyButton.setEnabled(true);
                if (!finalSent) {
                    phoneVerificationMobile = null;
                    stopSmsUserConsent();
                    Toast.makeText(MainActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show();
                    return;
                }
                phoneVerificationMobile = normalizeIndianMobileDigits(mobileValue);
                showOtpDialogForProfile(mobileValue, mobileTarget, verifyButton);
            });
        });
    }

    private void showOtpDialogForProfile(String mobileValue, Object mobileTarget, TextView verifyButton) {
        showThemedOtpDialog(
            "Verify mobile",
            "Enter 6-digit code sent to " + normalizePhoneNumber(mobileValue),
            "6-digit OTP",
            (otpValue, dialog, verifyInDialog, codeCells) -> {
                if (phoneVerificationMobile == null || otpValue.length() != 6) {
                    Toast.makeText(this, "Enter a valid 6-digit code", Toast.LENGTH_LONG).show();
                    return;
                }
                verifyProfileOtp(mobileValue, otpValue, mobileTarget, verifyButton, dialog, verifyInDialog, codeCells);
            }, () -> {
                verifyButton.setText(translate("Enter OTP"));
                verifyButton.setEnabled(true);
            }, () -> {
                verifyProfileMobileTarget(mobileTarget, verifyButton);
            }, true);
    }

    private void verifyProfileOtp(String mobileValue, String otpValue, Object mobileTarget, TextView verifyButton,
                                   Dialog dialog, TextView verifyInDialog, EditText[] codeCells) {
        verifyButton.setText(translate("Verifying..."));
        verifyButton.setEnabled(false);
        if (verifyInDialog != null) {
            verifyInDialog.setText(translate("Verifying..."));
            verifyInDialog.setEnabled(false);
        }

        verifyBackendMobileOtp(mobileValue, otpValue, mobileTarget, verifyButton, dialog, verifyInDialog, codeCells);
    }

    private void verifyBackendMobileOtp(String mobileValue, String otpValue, Object mobileTarget, TextView verifyButton,
                                         Dialog dialog, TextView verifyInDialog, EditText[] codeCells) {
        network.execute(() -> {
            boolean verified = false;
            String verificationToken = "";
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", normalizeIndianMobileDigits(mobileValue));
                payload.put("otp", otpValue);
                JSONObject response = postJson("/api/auth/verify-otp", payload.toString(), null);
                if (response != null) {
                    verified = response.optBoolean("success", false) || "success".equalsIgnoreCase(response.optString("status", ""));
                    verificationToken = response.optString("verification_token", "");
                }
            } catch (Exception error) {
                Log.e("MOBILE_VERIFICATION", "OTP verification request failed", error);
            }

            final boolean finalVerified = verified;
            final String finalVerificationToken = verificationToken;
            runOnUiThread(() -> {
                if (finalVerified) {
                    confirmVerifiedMobileOnBackend(mobileValue, finalVerificationToken, confirmed -> {
                        if (confirmed) {
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                                    .putString("mobile", mobileValue)
                                    .putString("verified_mobile", mobileValue)
                                    .putBoolean("mobile_verified", true)
                                    .apply();
                            saveVerifiedMobileToCloud(mobileValue);
                            lockVerifiedMobileField(mobileTarget, verifyButton);
                            if (mobileTarget instanceof EditText) {
                                ((EditText) mobileTarget).setText(mobileValue);
                            } else if (mobileTarget instanceof EditText[]) {
                                setMobileCells((EditText[]) mobileTarget, mobileValue);
                            }
                            Toast.makeText(this, "Mobile verified", Toast.LENGTH_SHORT).show();
                        } else {
                            resetMobileVerificationControls(verifyButton, verifyInDialog, codeCells);
                            Toast.makeText(this, "Could not save mobile verification. Please request a new code and try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    resetMobileVerificationControls(verifyButton, verifyInDialog, codeCells);
                    Toast.makeText(this, "Could not verify mobile. Check the code and try again.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private interface MobileVerificationCallback {
        void onComplete(boolean confirmed);
    }

    private void confirmVerifiedMobileOnBackend(String mobileValue, String verificationToken,
                                                MobileVerificationCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || verificationToken == null || verificationToken.trim().isEmpty()) {
            callback.onComplete(false);
            return;
        }
        user.getIdToken(false).addOnSuccessListener(idToken -> network.execute(() -> {
            boolean confirmed = false;
            try {
                JSONObject payload = new JSONObject();
                payload.put("mobile", normalizeIndianMobileDigits(mobileValue));
                payload.put("verification_token", verificationToken);
                JSONObject response = postJson("/api/auth/confirm-mobile", payload.toString(), idToken.getToken());
                confirmed = response != null && response.optBoolean("success", false);
            } catch (Exception error) {
                Log.e("MOBILE_VERIFICATION", "Could not persist verified mobile", error);
            }
            boolean finalConfirmed = confirmed;
            runOnUiThread(() -> callback.onComplete(finalConfirmed));
        })).addOnFailureListener(error -> {
            Log.e("MOBILE_VERIFICATION", "Could not get Firebase token to persist mobile verification", error);
            callback.onComplete(false);
        });
    }

    private void resetMobileVerificationControls(TextView verifyButton, TextView verifyInDialog,
                                                 EditText[] codeCells) {
        if (verifyInDialog != null) {
            verifyInDialog.setText(translate("Verify"));
            verifyInDialog.setEnabled(true);
        }
        if (codeCells != null) {
            for (EditText cell : codeCells) {
                if (cell != null) cell.setText("");
            }
            if (codeCells.length > 0 && codeCells[0] != null) {
                codeCells[0].requestFocus();
            }
        }
        verifyButton.setText(translate("Enter OTP"));
        verifyButton.setEnabled(true);
        verifyButton.setClickable(true);
        verifyButton.setFocusable(true);
        verifyButton.setBackground(round(GOLD, 24));
        verifyButton.setTextColor(GOLD_ON);
    }


    private void saveVerifiedEmailToCloud(String emailValue) {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String cleanEmail = emailValue == null ? "" : emailValue.trim().toLowerCase(Locale.US);
        account.edit()
                .putString("email", cleanEmail)
                .putBoolean("email_verified", true)
                .remove("email_verify_cooldown_until")
                .apply();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("uid", user.getUid());
        update.put("email", cleanEmail);
        update.put("emailVerified", true);
        update.put("isEmailVerified", true);
        update.put("email_verified", true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(getProfileDocumentKey())
                .set(update, SetOptions.merge())
                .addOnFailureListener(error -> Log.e("FIREBASE_ERROR", "Profile email save failed: ", error));

        syncProfileToBackendApi(account.getString("username", ""), account.getString("full_name", ""),
                cleanEmail, account.getString("mobile", ""), account.getString("state", ""), account.getString("city", ""),
                account.getString("profile_image_url", ""));
    }

    private void saveVerifiedMobileToCloud(String mobileValue) {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit()
                .putString("mobile", mobileValue)
                .putString("verified_mobile", normalizeIndianMobileDigits(mobileValue))
                .putBoolean("mobile_verified", true)
                .apply();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("uid", user.getUid());
        update.put("mobile", mobileValue);
        update.put("mobileVerified", true);
        update.put("mobile_verified", true);
        FirebaseFirestore.getInstance().collection("users").document(getProfileDocumentKey())
                .set(update, SetOptions.merge())
                .addOnFailureListener(error -> Log.e("FIREBASE_ERROR", "Mobile verification cloud save failed: ", error));
        syncProfileToBackendApi(account.getString("username", ""), account.getString("full_name", ""),
                account.getString("email", ""), mobileValue, account.getString("state", ""), account.getString("city", ""),
                account.getString("profile_image_url", ""));
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
            image.setBackgroundColor(Color.TRANSPARENT);
            boolean hasImage = reportImages[slot] != null || reportCameraImages[slot] != null;
            if (hasImage) {
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setPadding(0, 0, 0, 0);
                image.clearColorFilter();
                image.setScaleX(1f);
                image.setScaleY(1f);
                if (reportImages[slot] != null) {
                    Bitmap existing = bitmapFromUri(reportImages[slot]);
                    if (existing != null) {
                        image.setImageBitmap(existing);
                    } else {
                        image.setImageResource(R.drawable.add_image);
                    }
                }
                if (reportCameraImages[slot] != null) {
                    image.setImageBitmap(reportCameraImages[slot]);
                }
                image.setOnClickListener(view -> showFullImagePreview(imageSlot));
            } else {
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int pad = dp(22);
                image.setPadding(pad, pad, pad, pad);
                image.setImageResource(R.drawable.add_image);
                image.setColorFilter(accentColor());
                image.setScaleX(1f);
                image.setScaleY(1f);
                image.setOnClickListener(view -> showImageOptions(imageSlot));
            }

            slotView.addView(image, new FrameLayout.LayoutParams(-1, -1));

            if (hasImage) {
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
        Dialog dialog = new Dialog(this);
        LinearLayout content = themedDialogContent(R.drawable.add_image, "Add image", "Choose source for item image");

        TextView gallery = actionButton("Choose from gallery", false);
        gallery.setOnClickListener(v -> {
            dialog.dismiss();
            openGalleryForSlot(slot);
        });
        addFieldToDialog(content, gallery);

        TextView camera = actionButton("Take photo", false);
        camera.setOnClickListener(v -> {
            dialog.dismiss();
            openCameraForSlot(slot);
        });
        addFieldToDialog(content, camera);

        TextView cancel = actionButton("Cancel", true);
        cancel.setOnClickListener(v -> dialog.dismiss());
        addFieldToDialog(content, cancel);

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void openGalleryForSlot(int slot) {
        pendingImageSlot = slot;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
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
            private String lastValue = "";
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                String current = value.toString();
                if (!current.equals(lastValue)) {
                    lastValue = current;
                    update.accept(current);
                }
            }
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

    private void showReports() {
        currentPage = PAGE_REPORTS;
        screenRenderer = this::showReports;
        LinearLayout root = screenBase(translate("My reports"));
        addHeading(translate("Your reports"), translate("Keep track of items you are helping to reunite."));
        TextView loading = text(localizeReportsText("Loading reports..."), 16, secondaryTextColor(), Typeface.NORMAL);
        addField(root, loading);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            loading.setText(localizeReportsText("Sign in to view reports."));
            TextView home = actionButton(translate("Back home"), false);
            home.setOnClickListener(v -> showHome());
            addField(root, home);
            return;
        }

        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String idToken = (tokenResult != null) ? tokenResult.getToken() : null;

            network.execute(() -> {
                List<JSONObject> backendReports = new ArrayList<>();
                if (idToken != null) {
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(API_BASE + "/api/items/mine").openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(10000);
                        connection.setRequestProperty("Authorization", "Bearer " + idToken);
                        if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                            String response = readStream(connection.getInputStream());
                            if (response != null && !response.trim().isEmpty()) {
                                JSONArray arr = new JSONArray(response);
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject item = arr.optJSONObject(i);
                                    if (item != null) backendReports.add(item);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    } finally {
                        if (connection != null) connection.disconnect();
                    }
                }

                runOnUiThread(() -> renderMergedReports(backendReports, Collections.emptyList()));
            });
        }).addOnFailureListener(error -> {
            loading.setText(localizeReportsText("Your reports could not be loaded."));
            TextView home = actionButton(translate("Back home"), false);
            home.setOnClickListener(v -> showHome());
            addField(root, home);
        });
    }

    private void renderMergedReports(List<JSONObject> backendReports, List<JSONObject> firestoreReports) {
        if (isFinishing() || isDestroyed()) return;

        currentPage = PAGE_REPORTS;
        screenRenderer = this::showReports;
        LinearLayout root = screenBase(translate("My reports"));
        addHeading(translate("Your reports"), translate("Keep track of items you are helping to reunite."));
        addField(activeContent, fieldLabel(localizeReportsText("Browse by category")));
        LinearLayout categoryTabs = new LinearLayout(this);
        categoryTabs.setOrientation(LinearLayout.HORIZONTAL);
        populateReportCategoryTabs(
                categoryTabs,
                myReportsCategory,
                category -> {
                    myReportsCategory = category;
                    renderMergedReports(backendReports, firestoreReports);
                }
        );
        activeContent.addView(categoryTabs, contentParams(-1, dp(112), dp(12)));

        TextView discoveryLabel = text(
                localizeReportsText("Discover active reports"),
                15,
                primaryTextColor(),
                Typeface.BOLD
        );
        addField(activeContent, discoveryLabel);

        Map<String, String[]> stateCities = indiaStateCityMap();
        LinkedHashSet<String> uniqueCities = new LinkedHashSet<>();
        for (String[] cities : stateCities.values()) {
            uniqueCities.addAll(Arrays.asList(cities));
        }
        List<String> discoveryCities = new ArrayList<>(uniqueCities);
        Collections.sort(discoveryCities, String.CASE_INSENSITIVE_ORDER);
        String[] localizedCities = localizedCityChoices(discoveryCities.toArray(new String[0]));
        String[] cityOptions = new String[localizedCities.length + 1];
        cityOptions[0] = localizeReportsText("All cities");
        System.arraycopy(localizedCities, 0, cityOptions, 1, localizedCities.length);

        LinearLayout cityFilterLabel = fieldLabel(localizeReportsText("Filter reports by city"));
        addField(activeContent, cityFilterLabel);
        AutoCompleteTextView cityFilter = new AutoCompleteTextView(this);
        cityFilter.setHint(localizeReportsText("Search any city or town in India"));
        cityFilter.setThreshold(0);
        cityFilter.setSingleLine(true);
        cityFilter.setAdapter(localizedLocationAdapter(cityOptions));
        applyLocationFieldStyle(cityFilter);
        cityFilter.setText(
                discoveryCity.isEmpty()
                        ? cityOptions[0]
                        : localizeProfileDisplayValue("city", discoveryCity),
                false
        );
        cityFilter.setOnClickListener(view -> cityFilter.showDropDown());
        cityFilter.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = String.valueOf(parent.getItemAtPosition(position));
            if (cityOptions[0].equals(selectedOption)) {
                discoveryCity = "";
            } else {
                for (int index = 0; index < localizedCities.length; index++) {
                    if (selectedOption.equals(localizedCities[index])) {
                        discoveryCity = discoveryCities.get(index);
                        break;
                    }
                }
            }
            renderMergedReports(backendReports, firestoreReports);
        });
        addField(activeContent, cityFilter);
        TextView searchCity = actionButton(localizeReportsText("Search city"), false);
        searchCity.setOnClickListener(view -> {
            String cityQuery = cityFilter.getText().toString().trim();
            if (cityQuery.equalsIgnoreCase(cityOptions[0])) {
                discoveryCity = "";
            } else {
                discoveryCity = cityQuery;
                for (int index = 0; index < localizedCities.length; index++) {
                    if (cityQuery.equalsIgnoreCase(localizedCities[index])) {
                        discoveryCity = discoveryCities.get(index);
                        break;
                    }
                }
            }
            renderMergedReports(backendReports, firestoreReports);
        });
        addField(activeContent, searchCity);

        TextView discoveryStatus = text(
                localizeReportsText("Loading reports..."),
                14,
                secondaryTextColor(),
                Typeface.NORMAL
        );
        discoveryStatus.setGravity(Gravity.CENTER);
        LinearLayout discoveryResults = new LinearLayout(this);
        discoveryResults.setOrientation(LinearLayout.VERTICAL);
        activeContent.addView(discoveryStatus, contentParams(-1, -2, dp(8)));
        activeContent.addView(discoveryResults, new LinearLayout.LayoutParams(-1, -2));
        discoveryReportsLoaded = false;
        discoveryStatus.setOnClickListener(view -> {
            int retryGeneration = ++discoveryRequestGeneration;
            discoveryReportsLoaded = false;
            discoveryStatus.setText(localizeReportsText("Loading reports..."));
            discoveryStatus.setVisibility(View.VISIBLE);
            loadDiscoveryReports(discoveryCity, discoveryResults, discoveryStatus, backendReports, retryGeneration);
        });
        int requestGeneration = ++discoveryRequestGeneration;
        loadDiscoveryReports(discoveryCity, discoveryResults, discoveryStatus, backendReports, requestGeneration);

        TextView userReportsLabel = text(
                localizeReportsText("Your reports"),
                15,
                primaryTextColor(),
                Typeface.BOLD
        );
        LinearLayout.LayoutParams userReportsLabelParams = new LinearLayout.LayoutParams(-1, -2);
        userReportsLabelParams.setMargins(0, dp(16), 0, dp(8));
        activeContent.addView(userReportsLabel, userReportsLabelParams);
        LinearLayout activeUserReports = new LinearLayout(this);
        activeUserReports.setOrientation(LinearLayout.VERTICAL);
        activeContent.addView(activeUserReports, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout completedUserReports = new LinearLayout(this);
        completedUserReports.setOrientation(LinearLayout.VERTICAL);

        Map<String, JSONObject> reportMap = new LinkedHashMap<>();
        try {
            if (backendReports != null) {
                for (JSONObject r : backendReports) {
                    String id = r.optString("id", r.optString("_id", ""));
                    if (!id.isEmpty()) {
                        reportMap.put(id, r);
                    } else {
                        reportMap.put("be_" + reportMap.size(), r);
                    }
                }
            }
            if (firestoreReports != null) {
                for (JSONObject r : firestoreReports) {
                    String id = r.optString("id", "");
                    if (!id.isEmpty() && !reportMap.containsKey(id)) {
                        reportMap.put(id, r);
                    } else if (id.isEmpty()) {
                        reportMap.put("fs_" + reportMap.size(), r);
                    }
                }
            }

            List<JSONObject> allReports = new ArrayList<>(reportMap.values());
            Collections.sort(allReports, (a, b) -> Long.compare(parseReportCreatedAtMillis(b), parseReportCreatedAtMillis(a)));

            int visibleReportCount = 0;
            int activeReportCount = 0;
            int completedReportCount = 0;
            for (JSONObject report : allReports) {
                if (!discoveryReportMatchesCategory(report, myReportsCategory)) continue;
                visibleReportCount++;
                String type = report.optString("type", "ITEM");
                String title = report.optString("title", "Untitled item");
                String detail = report.optString("description", "");
                if (detail.length() > 90) detail = detail.substring(0, 90) + "...";
                String displayType = "FOUND".equalsIgnoreCase(type) ? translate("FOUND") : "LOST".equalsIgnoreCase(type) ? translate("LOST") : translate("Item");
                String reportId = report.optString("id", report.optString("_id", ""));
                String reportImageUrl = report.optString("image_url", null);
                long createdAtMs = parseReportCreatedAtMillis(report);
                long ageMs = System.currentTimeMillis() - createdAtMs;
                boolean isWithin5Hours = createdAtMs > 0 && ageMs >= 0 && ageMs <= 5 * 3600 * 1000L;
                boolean isCompleted = resolveReportWorkflowStage(report) == 4;
                boolean canEdit = !isCompleted && report.optInt("edit_count", 0) == 0 && isWithin5Hours;
                LinearLayout reportCard = reportRow(title, displayType + "  ·  " + detail, () -> {
                    editingReportId = reportId;
                    editingReportType = type;
                    editingReportImageUrl = reportImageUrl;
                    draftItem = title;
                    draftDescription = report.optString("description", "");
                    draftLocation = report.optString("report_location", "");
                    draftCityTag = report.optString("report_location", "");
                    draftDate = report.optString("report_date", "");
                    String savedCategory = report.optString("category", "").toLowerCase(Locale.US);
                    draftReportCategory = savedCategory.contains("animal") || savedCategory.contains("pet")
                            ? "pet"
                            : savedCategory.contains("people") || savedCategory.contains("person")
                                    ? "person"
                                    : "item";
                    draftIdentifier = "";
                    reportWizardStep = 1;
                    selectedImage = null;
                    capturedImage = null;
                    Arrays.fill(reportImages, null);
                    Arrays.fill(reportCameraImages, null);
                    showReport(type);
                }, canEdit);
                LinearLayout reportCardContainer = new LinearLayout(this);
                reportCardContainer.setOrientation(LinearLayout.VERTICAL);
                reportCardContainer.setPadding(dp(8), dp(6), dp(8), dp(10));
                reportCardContainer.setBackground(roundWithStroke(surfaceColor(), 18, borderColor()));
                reportCard.setBackgroundColor(Color.TRANSPARENT);
                reportCard.setOnClickListener(view -> showReportDetailsDialog(report, false, true));
                reportCardContainer.addView(reportCard, new LinearLayout.LayoutParams(-1, -2));
                reportCardContainer.addView(createReportStatusTracker(report));
                reportCardContainer.setOnClickListener(view -> showReportDetailsDialog(report, false, true));
                if (!isCompleted && "LOST".equalsIgnoreCase(type)) {
                    addCommunityPosterAction(reportCardContainer, report, dp(8), dp(12));
                }
                if (isHighPriorityReport(report)) {
                    TextView shareAlert = actionButton(
                            localizeReportsText("Share Alert Card"),
                            false
                    );
                    shareAlert.setTextColor(Color.WHITE);
                    shareAlert.setBackground(round(Color.rgb(190, 45, 55), 14));
                    LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(-1, dp(44));
                    shareParams.setMargins(dp(12), dp(8), dp(12), 0);
                    reportCardContainer.addView(shareAlert, shareParams);
                    shareAlert.setOnClickListener(view -> showShareAlertCardDialog(report));
                }
                LinearLayout.LayoutParams reportCardParams = new LinearLayout.LayoutParams(-1, -2);
                reportCardParams.setMargins(0, 0, 0, dp(12));
                if (isCompleted) {
                    completedReportCount++;
                    completedUserReports.addView(reportCardContainer, reportCardParams);
                } else {
                    activeReportCount++;
                    activeUserReports.addView(reportCardContainer, reportCardParams);
                }
            }

            if (allReports.isEmpty()) {
                addField(activeContent, text(localizeReportsText("No reports yet."), 16, secondaryTextColor(), Typeface.NORMAL));
            } else if (visibleReportCount == 0) {
                addField(activeContent, text(localizeReportsText("No reports in this category."), 16, secondaryTextColor(), Typeface.NORMAL));
            } else {
                if (activeReportCount == 0) {
                    addField(activeUserReports, text(
                            localizeReportsText("No active reports."),
                            14,
                            secondaryTextColor(),
                            Typeface.NORMAL
                    ));
                }
                if (completedReportCount > 0) {
                    int completedCount = completedReportCount;
                    TextView completedToggle = actionButton(
                            localizeReportsText("Completed reports") + " (" + localizeDigits(String.valueOf(completedCount)) + ")  ▼",
                            false
                    );
                    completedToggle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                    completedToggle.setPadding(dp(14), 0, dp(14), 0);
                    LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(-1, dp(48));
                    toggleParams.setMargins(0, dp(8), 0, dp(8));
                    activeContent.addView(completedToggle, toggleParams);
                    activeContent.addView(completedUserReports, new LinearLayout.LayoutParams(-1, -2));
                    completedUserReports.setVisibility(View.GONE);
                    completedToggle.setOnClickListener(view -> {
                        boolean expand = completedUserReports.getVisibility() != View.VISIBLE;
                        completedUserReports.setVisibility(expand ? View.VISIBLE : View.GONE);
                        completedToggle.setText(String.format(
                                Locale.ROOT,
                                "%1$s (%2$s)  %3$s",
                                localizeReportsText("Completed reports"),
                                localizeDigits(String.valueOf(completedCount)),
                                expand ? "▲" : "▼"
                        ));
                    });
                }
            }
        } catch (Exception error) {
            addField(activeContent, text(localizeReportsText("No reports yet."), 16, secondaryTextColor(), Typeface.NORMAL));
        }

        TextView home = actionButton(translate("Back home"), false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
    }

    private View createReportStatusTracker(JSONObject report) {
        LinearLayout tracker = new LinearLayout(this);
        tracker.setOrientation(LinearLayout.VERTICAL);
        tracker.setPadding(dp(12), dp(8), dp(8), dp(2));

        TextView heading = text(
                localizeReportsText("Live Status Tracker"),
                13,
                primaryTextColor(),
                Typeface.BOLD
        );
        tracker.addView(heading, new LinearLayout.LayoutParams(-1, -2));

        String[] titles = {
                "Submitted & Under Review",
                "Published & Broadcasting",
                "Match Found / Verification in Progress",
                "Successfully Reunited"
        };
        String[] descriptions = {
                "Admin validating details",
                "Live on network",
                "Admin verifying ownership",
                "Closed with a success badge"
        };
        int currentStage = resolveReportWorkflowStage(report);
        int completeColor = Color.rgb(24, 132, 91);
        int activeColor = currentStage == 3 ? Color.rgb(190, 45, 55) : Color.rgb(201, 145, 40);

        for (int index = 0; index < titles.length; index++) {
            int stage = index + 1;
            boolean complete = stage < currentStage || currentStage == 4;
            boolean active = stage == currentStage && currentStage < 4;
            int markerColor = complete ? completeColor : active ? activeColor : borderColor();

            LinearLayout step = new LinearLayout(this);
            step.setOrientation(LinearLayout.HORIZONTAL);
            step.setGravity(Gravity.TOP);
            step.setPadding(0, dp(8), 0, 0);

            LinearLayout track = new LinearLayout(this);
            track.setOrientation(LinearLayout.VERTICAL);
            track.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView marker = text(complete ? "✓" : active ? "•" : "", active ? 18 : 13,
                    complete ? Color.WHITE : active ? Color.WHITE : secondaryTextColor(),
                    Typeface.BOLD);
            marker.setGravity(Gravity.CENTER);
            marker.setBackground(roundWithStroke(
                    complete || active ? markerColor : Color.TRANSPARENT,
                    20,
                    markerColor
            ));
            track.addView(marker, new LinearLayout.LayoutParams(dp(22), dp(22)));
            if (stage < titles.length) {
                View connector = new View(this);
                connector.setBackgroundColor(complete ? completeColor : borderColor());
                track.addView(connector, new LinearLayout.LayoutParams(dp(2), dp(28)));
            }
            step.addView(track, new LinearLayout.LayoutParams(dp(26), -2));

            LinearLayout textColumn = new LinearLayout(this);
            textColumn.setOrientation(LinearLayout.VERTICAL);
            TextView title = text(
                    localizeReportsText(titles[index]),
                    12,
                    complete ? completeColor : active ? activeColor : secondaryTextColor(),
                    complete || active ? Typeface.BOLD : Typeface.NORMAL
            );
            TextView description = text(
                    localizeReportsText(descriptions[index]),
                    10,
                    secondaryTextColor(),
                    Typeface.NORMAL
            );
            textColumn.addView(title, new LinearLayout.LayoutParams(-1, -2));
            textColumn.addView(description, new LinearLayout.LayoutParams(-1, -2));
            step.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));
            tracker.addView(step, new LinearLayout.LayoutParams(-1, -2));

            if (active) {
                ScaleAnimation pulse = new ScaleAnimation(
                        0.78f, 1.12f, 0.78f, 1.12f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                pulse.setDuration(750);
                pulse.setRepeatMode(Animation.REVERSE);
                pulse.setRepeatCount(Animation.INFINITE);
                marker.startAnimation(pulse);
            }
        }

        if (currentStage == 4) {
            TextView successBadge = text(
                    localizeReportsText("SUCCESSFULLY REUNITED"),
                    10,
                    Color.WHITE,
                    Typeface.BOLD
            );
            successBadge.setGravity(Gravity.CENTER);
            successBadge.setPadding(dp(10), dp(5), dp(10), dp(5));
            successBadge.setBackground(round(completeColor, 12));
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
            badgeParams.setMargins(dp(26), dp(8), 0, 0);
            tracker.addView(successBadge, badgeParams);
        }
        return tracker;
    }

    private int resolveReportWorkflowStage(JSONObject report) {
        int stage = report.optInt("workflow_stage", 0);
        if (stage >= 1 && stage <= 4) return stage;
        String status = report.optString("status", "").trim().toLowerCase(Locale.US);
        if (containsDiscoveryTerm(status, "recovered", "reunited", "closed")) return 4;
        if (containsDiscoveryTerm(status, "match", "verification")) return 3;
        return 2;
    }

    private boolean isHighPriorityReport(JSONObject report) {
        return discoveryReportMatchesCategory(report, "people")
                || resolveReportWorkflowStage(report) >= 3;
    }

    private void showShareAlertCardDialog(JSONObject report) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_info,
                "Share alert flyer",
                "Preview the branded alert, then share it with your community."
        );

        ImageView preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setBackground(roundWithStroke(backgroundColor(), 16, borderColor()));
        preview.setPadding(dp(4), dp(4), dp(4), dp(4));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(-1, dp(350));
        previewParams.setMargins(0, dp(8), 0, dp(12));
        content.addView(preview, previewParams);

        TextView preparing = text(
                localizeReportsText("Preparing flyer..."),
                13,
                secondaryTextColor(),
                Typeface.NORMAL
        );
        preparing.setGravity(Gravity.CENTER);
        content.addView(preparing, new LinearLayout.LayoutParams(-1, dp(30)));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView close = actionButton(translate("Close"), false);
        close.setOnClickListener(view -> dialog.dismiss());
        actions.addView(close, new LinearLayout.LayoutParams(0, dp(44), 1));

        TextView share = actionButton(localizeReportsText("Share Alert Card"), true);
        share.setEnabled(false);
        share.setAlpha(0.55f);
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        shareParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(share, shareParams);
        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));

        final File[] flyerFile = {null};
        share.setOnClickListener(view -> {
            if (flyerFile[0] == null) return;
            shareAlertFlyer(report, flyerFile[0]);
        });

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);

        network.execute(() -> {
            Bitmap flyer;
            File flyerFileResult;
            try {
                flyer = createAlertFlyerBitmap(report);
                File flyerDirectory = new File(getCacheDir(), "share_flyers");
                if (!flyerDirectory.exists() && !flyerDirectory.mkdirs()) {
                    throw new java.io.IOException("Could not create alert flyer cache");
                }
                String safeReportId = report.optString("id", "report")
                        .replaceAll("[^A-Za-z0-9_-]", "_");
                flyerFileResult = new File(
                        flyerDirectory,
                        "fendly_alert_" + safeReportId + "_" + System.currentTimeMillis() + ".jpg"
                );
                try (FileOutputStream output = new FileOutputStream(flyerFileResult)) {
                    if (!flyer.compress(Bitmap.CompressFormat.JPEG, 92, output)) {
                        throw new java.io.IOException("Could not encode alert flyer");
                    }
                    output.flush();
                }
            } catch (Exception error) {
                Log.e("REPORT_FLYER", "Could not prepare alert flyer", error);
                runOnUiThread(() -> {
                    if (!dialog.isShowing()) return;
                    preparing.setText(localizeReportsText("Could not prepare flyer. Try again."));
                    preparing.setTextColor(Color.rgb(190, 45, 55));
                });
                return;
            }

            runOnUiThread(() -> {
                if (!dialog.isShowing()) return;
                flyerFile[0] = flyerFileResult;
                preview.setImageBitmap(flyer);
                preparing.setVisibility(View.GONE);
                share.setEnabled(true);
                share.setAlpha(1f);
            });
        });
    }

    private Bitmap createAlertFlyerBitmap(JSONObject report) throws Exception {
        final int width = 1080;
        final int height = 1600;
        Bitmap photo = loadAlertFlyerPhoto(report.optString("image_url", "").trim());
        Bitmap flyer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(flyer);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int deepGreen = Color.rgb(14, 67, 53);
        int brandGreen = Color.rgb(25, 112, 82);
        int cream = Color.rgb(248, 246, 238);
        int urgentRed = Color.rgb(190, 45, 55);

        canvas.drawColor(cream);
        paint.setShader(new LinearGradient(
                0, 0, width, 300,
                deepGreen, brandGreen, Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, width, 300, paint);
        paint.setShader(null);

        Drawable logo = AppCompatResources.getDrawable(this, R.drawable.fendly_logo);
        if (logo != null) {
            logo.setBounds(70, 70, 190, 190);
            logo.draw(canvas);
        }
        drawFlyerText(canvas, "FENDLY", 235, 136, 54, Color.WHITE, Typeface.BOLD);
        drawFlyerText(canvas, "REUNITE WHAT MATTERS", 238, 190, 25,
                Color.rgb(220, 237, 228), Typeface.NORMAL);

        RectF photoBounds = new RectF(70, 330, 1010, 1055);
        drawFlyerRoundRect(canvas, photoBounds.left, photoBounds.top,
                photoBounds.right, photoBounds.bottom, 34, Color.rgb(239, 237, 229));
        if (photo != null) {
            Path photoClip = new Path();
            photoClip.addRoundRect(75, 335, 1005, 1050, 28, 28, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(photoClip);
            float photoScale = Math.min(
                    930f / photo.getWidth(),
                    715f / photo.getHeight()
            );
            float photoWidth = photo.getWidth() * photoScale;
            float photoHeight = photo.getHeight() * photoScale;
            RectF photoDestination = new RectF(
                    540f - photoWidth / 2f,
                    692.5f - photoHeight / 2f,
                    540f + photoWidth / 2f,
                    692.5f + photoHeight / 2f
            );
            canvas.drawBitmap(photo, null, photoDestination, paint);
            canvas.restore();
            if (!photo.isRecycled()) photo.recycle();
        } else {
            paint.setColor(Color.rgb(229, 237, 231));
            canvas.drawRoundRect(75, 335, 1005, 1050, 28, 28, paint);
            drawFlyerText(canvas, "PHOTO NOT AVAILABLE", 540, 700, 28,
                    brandGreen, Typeface.BOLD, Paint.Align.CENTER);
        }

        boolean reunited = resolveReportWorkflowStage(report) == 4;
        String alertLabel = reunited ? "SUCCESSFULLY REUNITED" : "COMMUNITY ALERT";
        drawFlyerRoundRect(canvas, 70, 1080, 520, 1148, 28, reunited ? brandGreen : urgentRed);
        drawFlyerText(canvas, alertLabel, 295, 1126, 25, Color.WHITE,
                Typeface.BOLD, Paint.Align.CENTER);

        String title = report.optString("title", "").trim();
        if (title.isEmpty()) title = "Fendly community report";
        drawFlyerWrappedText(canvas, title, 72, 1215, 900, 54,
                Color.rgb(28, 43, 36), Typeface.BOLD, 2);

        drawFlyerText(canvas, "LAST-SEEN LOCATION", 75, 1360, 22,
                Color.rgb(98, 111, 103), Typeface.BOLD);
        String location = report.optString("report_location", "").trim();
        if (location.isEmpty()) location = "Location shared with the Fendly team";
        drawFlyerWrappedText(canvas, location, 75, 1405, 900, 34,
                Color.rgb(48, 63, 54), Typeface.NORMAL, 2);

        paint.setColor(Color.rgb(216, 226, 217));
        canvas.drawRect(70, 1525, 1010, 1528, paint);
        drawFlyerText(canvas, "FENDLY  ·  LOST & FOUND COMMUNITY", 75, 1570, 22,
                deepGreen, Typeface.BOLD);
        return flyer;
    }

    private Bitmap loadAlertFlyerPhoto(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.isEmpty()) return null;
        URL url = new URL(imageUrl);
        String protocol = url.getProtocol();
        if (!"https".equalsIgnoreCase(protocol) && !"http".equalsIgnoreCase(protocol)) {
            throw new java.io.IOException("Unsupported report photo URL");
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new java.io.IOException("Report photo returned HTTP " + responseCode);
            }
            int contentLength = connection.getContentLength();
            if (contentLength > 15 * 1024 * 1024) {
                throw new java.io.IOException("Report photo is too large to share");
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int total = 0;
            int count;
            try (InputStream input = connection.getInputStream()) {
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > 15 * 1024 * 1024) {
                        throw new java.io.IOException("Report photo is too large to share");
                    }
                    bytes.write(buffer, 0, count);
                }
            }
            byte[] imageBytes = bytes.toByteArray();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw new java.io.IOException("Report photo is not a valid image");
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = Math.max(
                    1,
                    Math.min(bounds.outWidth / 930, bounds.outHeight / 715)
            );
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
        } finally {
            connection.disconnect();
        }
    }

    private void drawFlyerRoundRect(Canvas canvas, float left, float top, float right,
                                    float bottom, float radius, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint);
    }

    private void drawFlyerText(Canvas canvas, String value, float x, float baseline,
                               float size, int color, int style) {
        drawFlyerText(canvas, value, x, baseline, size, color, style, Paint.Align.LEFT);
    }

    private void drawFlyerText(Canvas canvas, String value, float x, float baseline,
                               float size, int color, int style, Paint.Align alignment) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(Typeface.create("sans-serif", style));
        paint.setTextAlign(alignment);
        canvas.drawText(value, x, baseline, paint);
    }

    private void drawFlyerWrappedText(Canvas canvas, String value, float x, float baseline,
                                      float maxWidth, float size, int color, int style,
                                      int maxLines) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(Typeface.create("sans-serif", style));
        String[] words = value.split("\\s+");
        String line = "";
        int lineNumber = 0;
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (paint.measureText(candidate) > maxWidth && !line.isEmpty()) {
                drawFlyerText(canvas, line, x, baseline + lineNumber * (size + 10),
                        size, color, style);
                line = word;
                lineNumber++;
                if (lineNumber >= maxLines) return;
            } else {
                line = candidate;
            }
        }
        if (!line.isEmpty() && lineNumber < maxLines) {
            drawFlyerText(canvas, line, x, baseline + lineNumber * (size + 10),
                    size, color, style);
        }
    }

    private void shareAlertFlyer(JSONObject report, File flyerFile) {
        try {
            Uri flyerUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    flyerFile
            );
            String title = report.optString("title", "Fendly community alert");
            String location = report.optString("report_location", "").trim();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, flyerUri);
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    location.isEmpty()
                            ? title + " · Shared from Fendly"
                            : title + " · Last seen: " + location + " · Shared from Fendly"
            );
            shareIntent.setClipData(android.content.ClipData.newUri(
                    getContentResolver(),
                    "Fendly alert flyer",
                    flyerUri
            ));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(
                    shareIntent,
                    localizeReportsText("Share alert flyer")
            ));
        } catch (Exception error) {
            Log.e("REPORT_FLYER", "Could not share alert flyer", error);
            Toast.makeText(this, localizeReportsText("Could not share flyer."), Toast.LENGTH_LONG).show();
        }
    }

    private void hydrateProfileFromBackend() {
        hydrateProfileFromBackend(null);
    }

    private String languageCodeForIndex(int languageIndex) {
        String[] languageCodes = {"en", "hi", "mr", "gu", "bn", "ta", "te", "kn", "ml"};
        return languageCodes[Math.max(0, Math.min(languageIndex, languageCodes.length - 1))];
    }

    public static List<JSONObject> parseReportsFromBackendJson(String responseBody) throws Exception {
        List<JSONObject> reports = new ArrayList<>();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return reports;
        }

        JSONArray response = new JSONArray(responseBody);
        for (int i = 0; i < response.length(); i++) {
            JSONObject item = response.optJSONObject(i);
            if (item != null) {
                reports.add(item);
            }
        }

        Collections.sort(reports, (a, b) -> Long.compare(parseReportCreatedAtMillis(b), parseReportCreatedAtMillis(a)));
        return reports;
    }

    private static long parseReportCreatedAtMillis(JSONObject report) {
        if (report == null) return 0L;
        Object createdAt = report.opt("created_at");
        if (createdAt instanceof Number) {
            return ((Number) createdAt).longValue();
        }
        if (createdAt instanceof String) {
            String value = ((String) createdAt).trim();
            if (value.isEmpty()) return 0L;

            String[] patterns = {
                    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                    "yyyy-MM-dd'T'HH:mm:ssX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
            };
            for (String pattern : patterns) {
                try {
                    Date parsedDate = new SimpleDateFormat(pattern, Locale.US).parse(value);
                    if (parsedDate != null) return parsedDate.getTime();
                } catch (Exception ignored) {
                }
            }
        }
        return 0L;
    }

    private void initializeCloudinary() {
        String cloudName = getString(R.string.cloudinary_cloud_name).trim();
        if (cloudName.isEmpty()) {
            cloudName = "fendly";
        }
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
        cloudProfileListener = FirebaseFirestore.getInstance().collection("users").document(getProfileDocumentKey())
                .addSnapshotListener((document, error) -> {
                    boolean firstLoad = cloudProfileHydrationInFlight;
                    if (error != null) {
                        Log.e("FIREBASE_ERROR", "Data fetch failed: ", error);
                        cloudProfileLoaded = true;
                        cloudProfileHydrationInFlight = false;
                        if (firstLoad && onComplete != null) runOnUiThread(onComplete);
                        return;
                    }
                    if (document != null && document.exists()) {
                        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
                        String previousImageUrl = account.getString("profile_image_url", "").trim();
                        SharedPreferences.Editor editor = account.edit();
                        String cloudUsername = document.getString("username");
                        if (cloudUsername != null && !cloudUsername.trim().isEmpty()) {
                            editor.putString("username", formatUsernameDisplay(cloudUsername));
                        }
                        String cloudFirstName = document.getString("first_name");
                        if (cloudFirstName == null || cloudFirstName.trim().isEmpty()) cloudFirstName = document.getString("profile_first_name");
                        String cloudSurname = document.getString("surname");
                        if (cloudSurname == null || cloudSurname.trim().isEmpty()) cloudSurname = document.getString("profile_surname");
                        putProfileNameFields(editor, document.getString("full_name"), cloudFirstName, cloudSurname);
                        putIfPresent(editor, "email", document.getString("email"));
                        putIfPresent(editor, "mobile", document.getString("mobile"));
                        putIfPresent(editor, "state", document.getString("state"));
                        putIfPresent(editor, "city", document.getString("city"));
                        String cloudImage = syncProfileImageFromDocument(editor, account, document,
                                "imageUrl", "profile_image_url");
                        boolean cloudImageChanged = !cloudImage.equals(previousImageUrl);

                        boolean cloudEmailVerified = parseBooleanValue(document.get("emailVerified"))
                                || parseBooleanValue(document.get("isEmailVerified"))
                                || parseBooleanValue(document.get("email_verified"));
                        String cloudEmail = document.getString("email");
                        SharedPreferences accountPrefs = getSharedPreferences("fendly_account", MODE_PRIVATE);
                        String localEmail = accountPrefs.getString("email", "").trim();
                        boolean localEmailVerified = accountPrefs.getBoolean("email_verified", false);
                        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        boolean firebaseEmailVerified = currentFirebaseUser != null && currentFirebaseUser.isEmailVerified();

                        boolean emailVerifiedFinal = localEmailVerified || cloudEmailVerified || firebaseEmailVerified;
                        if (!emailVerifiedFinal && !localEmail.isEmpty() && cloudEmail != null && !cloudEmail.isEmpty() && localEmail.equalsIgnoreCase(cloudEmail)) {
                            emailVerifiedFinal = localEmailVerified;
                        }
                        editor.putBoolean("email_verified", emailVerifiedFinal);

                        editor.apply();
                        if (visibleAvatar != null && cloudImageChanged) {
                            runOnUiThread(() -> bindProfilePhoto(visibleAvatar, account));
                        }
                        if (currentPage == PAGE_PROFILE) {
                            runOnUiThread(() -> refreshProfileViewInPlace(false));
                        }
                    }
                    cloudProfileLoaded = true;
                    cloudProfileHydrationInFlight = false;
                    if (firstLoad && onComplete != null) {
                        runOnUiThread(onComplete);
                    } else if (currentPage == PAGE_PROFILE && visibleEmail != null && visibleEmailVerify != null) {
                        runOnUiThread(() -> refreshEmailVerificationState(visibleEmail, visibleEmailVerify));
                    }
                });
    }

    private void putIfPresent(SharedPreferences.Editor editor, String key, String value) {
        if (value != null && !value.trim().isEmpty()) editor.putString(key, value.trim());
    }

    private void putProfileNameFields(SharedPreferences.Editor editor, String fullName,
                                      String firstName, String surname) {
        String normalizedFullName = fullName == null ? "" : getCanonicalEnglishName(fullName.trim());
        if (!normalizedFullName.isEmpty()) editor.putString("full_name", normalizedFullName);

        String[] nameParts = normalizedFullName.isEmpty()
                ? new String[0]
                : normalizedFullName.split("\\s+", 2);
        String resolvedFirstName = firstName == null ? "" : firstName.trim();
        String resolvedSurname = surname == null ? "" : surname.trim();
        if (resolvedFirstName.isEmpty() && nameParts.length > 0) resolvedFirstName = nameParts[0];
        if (resolvedSurname.isEmpty() && nameParts.length > 1) resolvedSurname = nameParts[1];

        if (!resolvedFirstName.isEmpty()) editor.putString("profile_first_name", resolvedFirstName);
        if (!resolvedSurname.isEmpty() || !normalizedFullName.isEmpty()) {
            editor.putString("profile_surname", resolvedSurname);
        }
    }

    private String syncProfileImageFromDocument(SharedPreferences.Editor editor, SharedPreferences account,
                                                DocumentSnapshot document, String... fields) {
        if (profileImageExplicitlyRemoved || (document != null && parseBooleanValue(document.get("profile_image_removed")))) {
            clearProfileImageCache(editor);
            return "";
        }
        if (document != null && document.exists()) {
            for (String field : fields) {
                Object value = document.get(field);
                if (value instanceof String) {
                    String imageUrl = ((String) value).trim();
                    if (!imageUrl.isEmpty()) {
                        editor.putString("profile_image_url", imageUrl);
                        return imageUrl;
                    }
                }
            }
        }
        return account.getString("profile_image_url", "").trim();
    }

    private String syncProfileImageFromJson(SharedPreferences.Editor editor, SharedPreferences account,
                                            JSONObject profile, String... fields) {
        if (profileImageExplicitlyRemoved || (profile != null && profile.optBoolean("profile_image_removed", false))) {
            clearProfileImageCache(editor);
            return "";
        }
        if (profile != null) {
            for (String field : fields) {
                if (profile.has(field) && !profile.isNull(field)) {
                    String imageUrl = profile.optString(field, "").trim();
                    if (!imageUrl.isEmpty()) {
                        editor.putString("profile_image_url", imageUrl);
                        return imageUrl;
                    }
                }
            }
        }
        return account.getString("profile_image_url", "").trim();
    }

    private void clearProfileImageCache(SharedPreferences.Editor editor) {
        editor.remove("profile_image_url");
        editor.remove("profile_image_uri");
        editor.remove("profile_image_cache_url");
        File cachedPhoto = new File(getFilesDir(), "profile_photo_cache.jpg");
        if (cachedPhoto.exists()) cachedPhoto.delete();
    }

    private boolean parseBooleanValue(Object val) {
        if (val instanceof Boolean) {
            return (Boolean) val;
        } else if (val instanceof String) {
            return Boolean.parseBoolean((String) val);
        } else if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        return false;
    }

    private void applyCloudProfileToVisibleFields(DocumentSnapshot document) {
        if (applyingCloudProfile) return;
        if (visibleFirstName == null || visibleFirstName.hasFocus()) return;
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        applyingCloudProfile = true;
        String fullName = document.getString("full_name");
        if (fullName != null && !visibleNameDirty) {
            String cloudFirstName = document.getString("first_name");
            String cloudSurname = document.getString("surname");
            String targetFirst = (cloudFirstName != null && !cloudFirstName.trim().isEmpty()) ? cloudFirstName : fullName.split("\\s+", 2)[0];
            String targetSur = (cloudSurname != null && !cloudSurname.trim().isEmpty()) ? cloudSurname : (fullName.split("\\s+", 2).length > 1 ? fullName.split("\\s+", 2)[1] : "");
            String finalFirst = selectedLanguage == 0 ? targetFirst : localizeProfileName(targetFirst);
            String finalSur = selectedLanguage == 0 ? targetSur : localizeProfileName(targetSur);
            if (!finalFirst.equals(visibleFirstName.getText().toString())) {
                setVisibleText(visibleFirstName, finalFirst);
            }
            if (visibleSurname != null && !finalSur.equals(visibleSurname.getText().toString())) {
                setVisibleText(visibleSurname, finalSur);
            }
        }
        String emailVal = document.getString("email");
        if (visibleEmail != null && emailVal != null && !emailVal.endsWith("@login.fendly.app") && !visibleEmail.hasFocus()) {
            setVisibleText(visibleEmail, emailVal);
        }
        String mobileVal = document.getString("mobile");
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        boolean localMobileVerified = isMobileVerifiedFor(
                account.getString("mobile", ""),
                account
        );
        boolean cloudMobileMatchesVerified = normalizeIndianMobileDigits(mobileVal == null ? "" : mobileVal)
                .equals(normalizeIndianMobileDigits(account.getString("mobile", "")));
        if (visibleMobileCells != null
                && mobileVal != null
                && !mobileVal.trim().isEmpty()
                && (!localMobileVerified || cloudMobileMatchesVerified)
                && !hasMobileCellsFocus()) {
            setMobileCells(visibleMobileCells, mobileVal);
        } else if (visibleMobileCells != null && !localMobileVerified && !hasMobileCellsFocus()
                && mobileVal != null && mobileVal.trim().isEmpty()) {
            setMobileCells(visibleMobileCells, "");
        }
        applyingCloudProfile = false;
    }

    private void setVisibleText(EditText field, String value) {
        if (field == null || value == null || field.hasFocus() || value.equals(field.getText().toString())) return;
        int cursor = Math.min(field.getSelectionStart(), value.length());
        field.setText(value);
        if (cursor >= 0 && cursor <= value.length()) {
            field.setSelection(cursor);
        }
    }

    private void scheduleRealtimeProfileSave() {
        // Disabled realtime background save during text editing to prevent premature snapshot loops
    }

    private void saveVisibleProfileToCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || visibleFirstName == null) return;
        Map<String, Object> update = new LinkedHashMap<>();
        String canonicalFirst = getCanonicalEnglishName(visibleFirstName.getText().toString());
        String canonicalSur = getCanonicalEnglishName(visibleSurname != null ? visibleSurname.getText().toString() : "");
        String canonicalFull = (canonicalFirst + " " + canonicalSur).trim();
        if (!canonicalFull.isEmpty()) {
            update.put("full_name", canonicalFull);
            update.put("first_name", canonicalFirst);
            update.put("surname", canonicalSur);
            update.put("profile_first_name", canonicalFirst);
            update.put("profile_surname", canonicalSur);
        }
        if (visibleEmail != null) {
            String emailText = visibleEmail.getText().toString().trim();
            if (!emailText.endsWith("@login.fendly.app")) {
                update.put("email", emailText);
            }
        }
        if (visibleMobileCells != null) {
            String mobileText = normalizeLocalizedDigits(mobileValue(visibleMobileCells));
            if (mobileText.length() == 10) {
                update.put("mobile", mobileText);
            }
        }
        String currentImageUrl = getSharedPreferences("fendly_account", MODE_PRIVATE).getString("profile_image_url", "").trim();
        if (!currentImageUrl.isEmpty()) {
            update.put("imageUrl", currentImageUrl);
            update.put("profile_image_url", currentImageUrl);
            update.put("profile_photo_url", currentImageUrl);
            update.put("profile_image_removed", false);
        }
        if (update.isEmpty()) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docKey = getProfileDocumentKey();
        db.collection("users").document(docKey)
                .set(update, SetOptions.merge())
                .addOnFailureListener(error -> Log.e("FIREBASE_ERROR", "Data fetch failed: ", error));
        if (!user.getUid().equals(docKey)) {
            db.collection("users").document(user.getUid())
                    .set(update, SetOptions.merge())
                    .addOnFailureListener(error -> Log.e("FIREBASE_ERROR", "Data fetch failed: ", error));
        }
    }

    private void loadCloudProfileImage(ImageView avatar) {
        if (avatar == null) return;
        String imageUrl = getSharedPreferences("fendly_account", MODE_PRIVATE)
                .getString("profile_image_url", "").trim();
        if (imageUrl.isEmpty()) return;

        avatar.clearColorFilter();
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setPadding(0, 0, 0, 0);

        if (imageUrl.startsWith("data:image/") || (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://") && !imageUrl.startsWith("content://") && !imageUrl.startsWith("file://"))) {
            try {
                String cleanB64 = imageUrl.contains(",") ? imageUrl.substring(imageUrl.indexOf(",") + 1) : imageUrl;
                byte[] decodedBytes = Base64.decode(cleanB64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    avatar.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_field_person)
                .error(R.drawable.ic_field_person)
                .into(avatar);
        cacheProfilePhotoFromCloud(imageUrl);
    }

    private void cacheProfilePhotoFromCloud(String imageUrl) {
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) return;
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String existingUri = account.getString("profile_image_uri", "").trim();
        String cachedUrl = account.getString("profile_image_cache_url", "").trim();
        if (imageUrl.equals(cachedUrl) && !existingUri.isEmpty() && new File(existingUri).isFile()) return;
        if (imageUrl.equals(profilePhotoCacheDownloadUrl)) return;
        profilePhotoCacheDownloadUrl = imageUrl;
        network.execute(() -> {
            HttpURLConnection connection = null;
            Bitmap bitmap = null;
            String savedPath = null;
            try {
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                    try (InputStream input = connection.getInputStream()) {
                        bitmap = BitmapFactory.decodeStream(input);
                    }
                    if (bitmap != null) {
                        savedPath = saveProfileBitmapToInternalStorage(bitmap, "profile_photo_cache.jpg");
                    }
                }
            } catch (Exception error) {
                Log.w("PROFILE_PHOTO_CACHE", "Cloud avatar cache download failed");
            } finally {
                if (connection != null) connection.disconnect();
                if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            }
            String cachedPath = savedPath;
            runOnUiThread(() -> {
                if (imageUrl.equals(profilePhotoCacheDownloadUrl)) profilePhotoCacheDownloadUrl = "";
                if (cachedPath == null) return;
                SharedPreferences currentAccount = getSharedPreferences("fendly_account", MODE_PRIVATE);
                String currentImageUrl = currentAccount.getString("profile_image_url", "").trim();
                if (imageUrl.equals(currentImageUrl)) {
                    currentAccount.edit()
                            .putString("profile_image_uri", cachedPath)
                            .putString("profile_image_cache_url", imageUrl)
                            .apply();
                    if (currentPage == PAGE_PROFILE && visibleAvatar != null) {
                        bindProfilePhoto(visibleAvatar, currentAccount);
                    }
                } else {
                    Log.d("PROFILE_PHOTO_CACHE", "Skipped stale avatar cache after cloud URL changed");
                }
            });
        });
    }

    private void saveCloudProfile(String username, String fullName, String email, String mobile,
                                  String state, String city, TextView saveButton) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (saveButton != null) Toast.makeText(this, "Sign in before saving your profile", Toast.LENGTH_LONG).show();
            return;
        }

        String rawExisting = getSharedPreferences("fendly_account", MODE_PRIVATE)
                .getString("profile_image_url", "").trim();
        String imageUrl = (rawExisting.startsWith("http://") || rawExisting.startsWith("https://")) ? rawExisting : "";

        if (capturedProfileImage != null || selectedProfileImage != null) {
            user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                String uploadedUrl = uploadImage(selectedProfileImage, capturedProfileImage, token.getToken());
                if (uploadedUrl == null || uploadedUrl.isEmpty()) {
                    runOnUiThread(() -> {
                        if (saveButton != null) profileSaveFailed(saveButton, "Profile photo upload failed");
                        else Toast.makeText(this, "Profile photo upload failed", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                runOnUiThread(() -> saveCloudProfileDocument(user, username, fullName, email, mobile, state, city, uploadedUrl, saveButton));
            })).addOnFailureListener(error -> {
                if (saveButton != null) profileSaveFailed(saveButton, "Authentication failed while uploading your photo");
            });
            return;
        }

        saveCloudProfileDocument(user, username, fullName, email, mobile, state, city, imageUrl, saveButton);
    }

    private String bitmapToBase64(Bitmap sourceBitmap) {
        if (sourceBitmap == null) return "";
        try {
            int width = sourceBitmap.getWidth();
            int height = sourceBitmap.getHeight();
            int maxDimension = 400;
            Bitmap bitmap = sourceBitmap;
            if (width > maxDimension || height > maxDimension) {
                float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
                width = Math.round(width * ratio);
                height = Math.round(height * ratio);
                bitmap = Bitmap.createScaledBitmap(sourceBitmap, width, height, true);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
            byte[] bytes = outputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    private String uriToBase64(Uri uri) {
        if (uri == null) return "";
        try {
            Bitmap bitmap;
            if ("content".equals(uri.getScheme())) {
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    bitmap = BitmapFactory.decodeStream(input);
                }
            } else {
                String path = "file".equals(uri.getScheme()) ? uri.getPath() : uri.toString();
                bitmap = BitmapFactory.decodeFile(path);
            }
            return bitmapToBase64(bitmap);
        } catch (Exception e) {
            return "";
        }
    }

    private Bitmap loadBitmapFromUri(Uri uri) {
        if (uri == null) return null;
        try {
            return Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .submit(800, 800)
                    .get();
        } catch (Exception e) {
            try {
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    if (input != null) return BitmapFactory.decodeStream(input);
                }
            } catch (Exception ignored) {}
            return null;
        }
    }

    private String saveProfileFileToInternalStorage(Uri uri) {
        if (uri == null) return null;
        try {
            Bitmap bitmap = loadBitmapFromUri(uri);
            if (bitmap == null) return null;
            return saveProfileBitmapToInternalStorage(bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    private String saveProfileBitmapToInternalStorage(Bitmap sourceBitmap) {
        clearOldProfilePhotoFiles();
        String fileName = "profile_photo_" + System.currentTimeMillis() + ".jpg";
        return saveProfileBitmapToInternalStorage(sourceBitmap, fileName);
    }

    private void clearOldProfilePhotoFiles() {
        try {
            File dir = getFilesDir();
            File[] files = dir.listFiles((d, name) -> name != null && (name.startsWith("profile_photo") || name.startsWith("profile_photo_")));
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String saveProfileBitmapToInternalStorage(Bitmap sourceBitmap, String fileName) {
        if (sourceBitmap == null) return null;
        try {
            File file = new File(getFilesDir(), fileName);
            int width = sourceBitmap.getWidth();
            int height = sourceBitmap.getHeight();
            int maxDim = 400;
            Bitmap bitmap = sourceBitmap;
            if (width > maxDim || height > maxDim) {
                float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
                width = Math.round(width * ratio);
                height = Math.round(height * ratio);
                bitmap = Bitmap.createScaledBitmap(sourceBitmap, width, height, true);
            }
            try (OutputStream output = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void saveCloudProfileDocument(FirebaseUser user, String username, String fullName, String email,
                                          String mobile, String state, String city, String imageUrl,
                                          TextView saveButton) {
        if (user == null) {
            profileSaveFailed(saveButton, "Sign in before saving profile");
            return;
        }

        if (imageUrl != null && imageUrl.startsWith("data:image/")) {
            imageUrl = "";
        }

        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String fn = account.getString("profile_first_name", "");
        String sn = account.getString("profile_surname", "");
        if (fn.isEmpty() || sn.isEmpty()) {
            String[] parts = fullName.split("\\s+", 2);
            if (fn.isEmpty() && parts.length > 0) fn = parts[0];
            if (sn.isEmpty() && parts.length > 1) sn = parts[1];
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("uid", user.getUid());
        profile.put("username", username);
        profile.put("full_name", fullName);
        profile.put("first_name", fn);
        profile.put("surname", sn);
        profile.put("profile_first_name", fn);
        profile.put("profile_surname", sn);
        profile.put("email", email);
        profile.put("mobile", mobile);
        profile.put("state", state);
        profile.put("city", city);
        String savedPhotoUrl = imageUrl == null ? "" : imageUrl.trim();
        if (!savedPhotoUrl.isEmpty()) {
            profile.put("imageUrl", savedPhotoUrl);
            profile.put("profile_image_url", savedPhotoUrl);
            profile.put("profile_photo_url", savedPhotoUrl);
            profile.put("profile_image_removed", false);
        }

        syncProfileToBackendApi(username, fullName, email, mobile, state, city, savedPhotoUrl, saveButton);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String docKey = getProfileDocumentKey();
        db.collection("users").document(docKey)
                .set(profile, SetOptions.merge());
        if (!user.getUid().equals(docKey)) {
            db.collection("users").document(user.getUid())
                    .set(profile, SetOptions.merge());
        }

        SharedPreferences.Editor accountEditor = account.edit();
        if (!savedPhotoUrl.isEmpty()) accountEditor.putString("profile_image_url", savedPhotoUrl);
        accountEditor
                .putString("profile_first_name", fn)
                .putString("profile_surname", sn)
                .putString("full_name", fullName)
                .putString("email", email)
                .putString("mobile", mobile)
                .putString("state", state)
                .putString("city", city);
            accountEditor.apply();

        profileImageExplicitlyRemoved = false;
        cloudProfileLoaded = true;
    }

    private void syncProfileToBackendApi(String username, String fullName, String email, String mobile,
                                       String state, String city, String profilePhotoUrl) {
        syncProfileToBackendApi(username, fullName, email, mobile, state, city, profilePhotoUrl, null);
    }

    private void syncProfileToBackendApi(String username, String fullName, String email, String mobile,
                                       String state, String city, String profilePhotoUrl, TextView saveButton) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (saveButton != null) profileSaveFailed(saveButton, "Sign in before saving profile");
            return;
        }
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String photoUrl = profilePhotoUrl == null ? "" : profilePhotoUrl.trim();
        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            HttpURLConnection putConnection = null;
            try {
                putConnection = (HttpURLConnection) new URL(API_BASE + "/api/users/profile").openConnection();
                putConnection.setRequestMethod("PUT");
                putConnection.setConnectTimeout(15000);
                putConnection.setReadTimeout(30000);
                putConnection.setDoOutput(true);
                putConnection.setRequestProperty("Authorization", "Bearer " + token.getToken());
                putConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                String body = "{\"username\":\"" + escapeJson(username) + "\",\"full_name\":\"" + escapeJson(fullName) + "\",\"email\":\"" + escapeJson(email) + "\",\"mobile\":\"" + escapeJson(mobile) + "\",\"state\":\"" + escapeJson(state) + "\",\"city\":\"" + escapeJson(city) + "\"";
                if (!photoUrl.isEmpty() || profileImageExplicitlyRemoved) {
                    body += ",\"profile_photo_url\":\"" + escapeJson(photoUrl) + "\",\"profile_image_removed\":" + profileImageExplicitlyRemoved;
                }
                body += "}";
                try (OutputStream output = putConnection.getOutputStream()) {
                    output.write(body.getBytes(StandardCharsets.UTF_8));
                }
                int statusCode = putConnection.getResponseCode();
                if (statusCode < 200 || statusCode >= 300) {
                    Log.e("PROFILE_SYNC", "Backend profile sync failed with HTTP " + statusCode);
                    if (saveButton != null) {
                        runOnUiThread(() -> profileSaveFailed(saveButton,
                                "Profile sync failed (HTTP " + statusCode + "). Please try again."));
                    }
                } else if (saveButton != null) {
                    runOnUiThread(() -> profileSaveSucceeded(saveButton));
                }
            } catch (Exception error) {
                Log.e("PROFILE_SYNC", "Backend profile sync failed", error);
                if (saveButton != null) {
                    runOnUiThread(() -> profileSaveFailed(saveButton, "Profile sync failed. Please try again."));
                }
            } finally {
                if (putConnection != null) putConnection.disconnect();
            }
        })).addOnFailureListener(error -> {
            Log.e("PROFILE_SYNC", "Could not get authentication token for profile sync", error);
            if (saveButton != null) {
                profileSaveFailed(saveButton, "Profile sync failed. Please try again.");
            }
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
        if (saveButton != null) {
            saveButton.setEnabled(true);
            saveButton.setText(localizedFieldLabel("Save changes"));
        }
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

    private void handleSuccessfulLogin(String username, String pin, Runnable onSuccess) {
        accountCreated = true;
        if (pin != null && !pin.isEmpty()) {
            saveStoredAccountPin(pin);
        }
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        account.edit()
                .putBoolean("created", true)
                .putString("username", formatUsernameDisplay(username))
                .apply();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (onSuccess != null) onSuccess.run();
            return;
        }

        if (onSuccess != null) onSuccess.run();
        refreshAnnualSubscription(null);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(getProfileDocumentKey())
                .get(Source.SERVER)
                .addOnSuccessListener(document -> {
                    SharedPreferences.Editor editor = account.edit();
                    String locallySavedEmail = account.getString("email", "").trim();
                    boolean emailVerified = account.getBoolean("email_verified", false);
                    boolean mobileVerified = false;
                    String email = "";
                    String mobile = "";
                    String fullName = "";
                    String firstName = "";
                    String surname = "";
                    String state = "";
                    String city = "";
                    boolean hasMobileVerificationState = false;

                    if (document != null && document.exists()) {
                        String cloudEmailValue = document.getString("email");
                        boolean cloudEmailVerified = parseBooleanValue(document.get("emailVerified"))
                                || parseBooleanValue(document.get("isEmailVerified"))
                                || parseBooleanValue(document.get("email_verified"));
                        boolean sameSavedEmail = locallySavedEmail.isEmpty()
                                || cloudEmailValue == null
                                || cloudEmailValue.trim().isEmpty()
                                || locallySavedEmail.equalsIgnoreCase(cloudEmailValue.trim());
                        emailVerified = cloudEmailVerified
                                || (emailVerified && sameSavedEmail);
                        hasMobileVerificationState = document.contains("mobileVerified")
                                || document.contains("isMobileVerified")
                                || document.contains("mobile_verified");
                        mobileVerified = parseBooleanValue(document.get("mobileVerified"))
                                || parseBooleanValue(document.get("isMobileVerified"))
                                || parseBooleanValue(document.get("mobile_verified"));
                        email = cloudEmailValue;
                        mobile = document.getString("mobile");
                        fullName = document.getString("full_name");
                        firstName = document.getString("first_name");
                        surname = document.getString("surname");
                        state = document.getString("state");
                        city = document.getString("city");
                    }

                    if (!hasMobileVerificationState) {
                        mobileVerified = isMobileVerifiedFor(
                                mobile == null || mobile.trim().isEmpty()
                                        ? account.getString("mobile", "")
                                        : mobile,
                                account
                        );
                    }
                    editor.putBoolean("email_verified", emailVerified);
                    editor.putBoolean("mobile_verified", mobileVerified);
                    if (mobileVerified) {
                        if (mobile != null && !mobile.trim().isEmpty()) {
                            editor.putString("verified_mobile", normalizeIndianMobileDigits(mobile));
                        }
                    } else if (hasMobileVerificationState) {
                        editor.remove("verified_mobile");
                    }
                    if (email != null && !email.trim().isEmpty() && !email.endsWith("@login.fendly.app")) {
                        editor.putString("email", email.trim());
                    }
                    if (mobile != null && !mobile.trim().isEmpty()) {
                        editor.putString("mobile", mobile.trim());
                    }
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        editor.putString("full_name", fullName.trim());
                    }
                    if (firstName != null && !firstName.trim().isEmpty()) {
                        editor.putString("profile_first_name", firstName.trim());
                    }
                    if (surname != null && !surname.trim().isEmpty()) {
                        editor.putString("profile_surname", surname.trim());
                    }
                    if (state != null && !state.trim().isEmpty()) {
                        editor.putString("state", state.trim());
                    }
                    if (city != null && !city.trim().isEmpty()) {
                        editor.putString("city", city.trim());
                    }
                    syncProfileImageFromDocument(editor, account, document,
                            "profile_photo_url", "imageUrl", "profile_image_url");
                    editor.putBoolean("created", true);
                    editor.putString("username", formatUsernameDisplay(username));
                    editor.apply();

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
                                    boolean remoteEmailVerified = profile.optBoolean("email_verified", false);
                                    boolean remoteMobileVerified = profile.optBoolean("mobile_verified", false);
                                    String remoteEmail = profile.optString("email", "").trim();
                                    String remoteMobile = profile.optString("mobile", "").trim();
                                    String remoteFullName = profile.optString("full_name", "").trim();
                                    String remoteState = profile.optString("state", "").trim();
                                    String remoteCity = profile.optString("city", "").trim();
                                    SharedPreferences.Editor backendEditor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit();
                                    if (remoteEmailVerified) backendEditor.putBoolean("email_verified", true);
                                    if (!remoteMobile.isEmpty()) {
                                        backendEditor.putBoolean("mobile_verified", remoteMobileVerified);
                                        if (remoteMobileVerified) {
                                            backendEditor.putString("verified_mobile", normalizeIndianMobileDigits(remoteMobile));
                                        } else {
                                            backendEditor.remove("verified_mobile");
                                        }
                                    }
                                    if (!remoteEmail.isEmpty() && !remoteEmail.endsWith("@login.fendly.app")) backendEditor.putString("email", remoteEmail);
                                    if (!remoteMobile.isEmpty()) backendEditor.putString("mobile", remoteMobile);
                                    putProfileNameFields(backendEditor, remoteFullName, "", "");
                                    if (!remoteState.isEmpty()) backendEditor.putString("state", remoteState);
                                    if (!remoteCity.isEmpty()) backendEditor.putString("city", remoteCity);
                                    syncProfileImageFromJson(backendEditor,
                                            getSharedPreferences("fendly_account", MODE_PRIVATE), profile,
                                            "profile_photo_url", "imageUrl", "image_url");
                                    backendEditor.apply();
                                    runOnUiThread(() -> {
                                        if (currentPage == PAGE_PROFILE) {
                                            refreshProfileViewInPlace(false);
                                        } else if (currentPage == PAGE_PROFILE_SETUP) {
                                            if (visibleMobileCells != null && !remoteMobile.isEmpty()) {
                                                setMobileCells(visibleMobileCells, remoteMobile);
                                            }
                                            refreshMobileVerificationUi();
                                        }
                                    });
                                }
                            }
                        } catch (Exception ignored) {
                        } finally {
                            if (connection != null) connection.disconnect();
                        }
                    }));

                    profileHydrated = true;
                    cloudProfileLoaded = true;
                })
                .addOnFailureListener(e -> {
                    SharedPreferences.Editor editor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit();
                    editor.putBoolean("created", true);
                    editor.putString("username", formatUsernameDisplay(username));
                    editor.apply();
                });
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

        String primaryKey = getProfileDocumentKey();
        String fallbackKey = user.getUid();

        OnSuccessListener<DocumentSnapshot> docHandler = document -> {
            if (document != null && document.exists()) {
                SharedPreferences.Editor editor = account.edit();
                String username = document.getString("username");
                if (username != null && !username.trim().isEmpty()) {
                    editor.putString("username", formatUsernameDisplay(username));
                }
                String cloudFirstName = document.getString("first_name");
                if (cloudFirstName == null || cloudFirstName.trim().isEmpty()) cloudFirstName = document.getString("profile_first_name");
                String cloudSurname = document.getString("surname");
                if (cloudSurname == null || cloudSurname.trim().isEmpty()) cloudSurname = document.getString("profile_surname");
                putProfileNameFields(editor, document.getString("full_name"), cloudFirstName, cloudSurname);
                putIfPresent(editor, "email", document.getString("email"));
                putIfPresent(editor, "mobile", document.getString("mobile"));
                putIfPresent(editor, "state", document.getString("state"));
                putIfPresent(editor, "city", document.getString("city"));
                syncProfileImageFromDocument(editor, account, document,
                        "profile_photo_url", "imageUrl", "profile_image_url");

                boolean cloudEmailVerified = parseBooleanValue(document.get("emailVerified"))
                        || parseBooleanValue(document.get("isEmailVerified"))
                        || parseBooleanValue(document.get("email_verified"));
                String cloudEmail = document.getString("email");
                String localEmail = account.getString("email", "").trim();
                boolean localEmailVerified = account.getBoolean("email_verified", false);
                FirebaseUser firebaseUser = auth.getCurrentUser();
                boolean firebaseEmailVerified = firebaseUser != null && firebaseUser.isEmailVerified();

                boolean emailVerifiedFinal = localEmailVerified || cloudEmailVerified || firebaseEmailVerified;
                if (!emailVerifiedFinal && !localEmail.isEmpty() && cloudEmail != null && !cloudEmail.isEmpty() && localEmail.equalsIgnoreCase(cloudEmail)) {
                    emailVerifiedFinal = localEmailVerified;
                }
                editor.putBoolean("email_verified", emailVerifiedFinal);

                boolean cloudMobileVerified = parseBooleanValue(document.get("mobileVerified"))
                        || parseBooleanValue(document.get("isMobileVerified"))
                        || parseBooleanValue(document.get("mobile_verified"));
                String cloudMobile = document.getString("mobile");
                String localMobile = account.getString("mobile", "").trim();
                boolean localMobileVerified = account.getBoolean("mobile_verified", false);

                boolean mobileVerifiedFinal = localMobileVerified || cloudMobileVerified;
                if (!mobileVerifiedFinal && !localMobile.isEmpty() && cloudMobile != null && !cloudMobile.isEmpty() && localMobile.equals(cloudMobile)) {
                    mobileVerifiedFinal = localMobileVerified;
                }
                editor.putBoolean("mobile_verified", mobileVerifiedFinal);
                editor.apply();
            }
            profileHydrated = true;
            cloudProfileLoaded = true;
            profileHydrationInFlight = false;
            flushPendingProfileHydrationCallbacks();
            if (onComplete != null) onComplete.run();
        };

        FirebaseFirestore.getInstance().collection("users").document(primaryKey)
                .get(Source.SERVER)
                .addOnSuccessListener(document -> {
                    if (document != null && document.exists()) {
                        docHandler.onSuccess(document);
                    } else if (!fallbackKey.equals(primaryKey)) {
                        FirebaseFirestore.getInstance().collection("users").document(fallbackKey)
                                .get(Source.SERVER)
                                .addOnSuccessListener(docHandler)
                                .addOnFailureListener(e -> fetchProfileFromBackendApi(user, account, onComplete));
                    } else {
                        fetchProfileFromBackendApi(user, account, onComplete);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!fallbackKey.equals(primaryKey)) {
                        FirebaseFirestore.getInstance().collection("users").document(fallbackKey)
                                .get(Source.SERVER)
                                .addOnSuccessListener(docHandler)
                                .addOnFailureListener(err -> fetchProfileFromBackendApi(user, account, onComplete));
                    } else {
                        fetchProfileFromBackendApi(user, account, onComplete);
                    }
                });
    }

    private void fetchProfileFromBackendApi(FirebaseUser user, SharedPreferences account, Runnable onComplete) {
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
                        boolean emailVerified = profile.optBoolean("email_verified", account.getBoolean("email_verified", false));
                        boolean mobileVerified = profile.optBoolean("mobile_verified", account.getBoolean("mobile_verified", false));

                        SharedPreferences.Editor editor = account.edit();
                        if (!username.isEmpty()) editor.putString("username", username);
                        putProfileNameFields(editor, fullName, "", "");
                        if (!email.isEmpty()) editor.putString("email", email);
                        if (!mobile.isEmpty()) editor.putString("mobile", mobile);
                        if (!state.isEmpty()) editor.putString("state", state);
                        if (!city.isEmpty()) editor.putString("city", city);
                        syncProfileImageFromJson(editor, account, profile,
                                "profile_photo_url", "imageUrl", "image_url");
                        if (emailVerified) editor.putBoolean("email_verified", true);
                        if (!mobile.isEmpty()) {
                            editor.putBoolean("mobile_verified", mobileVerified);
                            if (mobileVerified) {
                                editor.putString("verified_mobile", normalizeIndianMobileDigits(mobile));
                            } else {
                                editor.remove("verified_mobile");
                            }
                        }
                        editor.apply();
                    }
                }
            } catch (Exception exception) {
                Log.e("FIREBASE_ERROR", "Data fetch failed: ", exception);
            } finally {
                if (connection != null) connection.disconnect();
                profileHydrated = true;
                profileHydrationInFlight = false;
                flushPendingProfileHydrationCallbacks();
                runOnUiThread(() -> {
                    if (currentPage == PAGE_PROFILE) refreshProfileViewInPlace(false);
                    if (onComplete != null) onComplete.run();
                });
            }
        })).addOnFailureListener(error -> {
            profileHydrated = true;
            profileHydrationInFlight = false;
            flushPendingProfileHydrationCallbacks();
            runOnUiThread(() -> {
                if (currentPage == PAGE_PROFILE) refreshProfileViewInPlace(false);
                if (onComplete != null) onComplete.run();
            });
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
        WindowInsetsHelper.applySafeArea(loading);
        ProgressBar progress = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.CENTER);
        loading.addView(progress, progressParams);
        setContentView(loading);
    }

    private void processDocumentToAccount(DocumentSnapshot document, SharedPreferences account) {
        if (document == null || !document.exists()) return;
        SharedPreferences.Editor editor = account.edit();
        boolean isVerified = parseBooleanValue(document.get("emailVerified"))
                || parseBooleanValue(document.get("isEmailVerified"))
                || parseBooleanValue(document.get("email_verified"));
        String cloudEmail = document.getString("email");
        String cloudMobile = document.getString("mobile");
        String fullName = document.getString("full_name");
        String cloudFirstName = document.getString("first_name");
        if (cloudFirstName == null || cloudFirstName.trim().isEmpty()) {
            cloudFirstName = document.getString("profile_first_name");
        }
        String cloudSurname = document.getString("surname");
        if (cloudSurname == null || cloudSurname.trim().isEmpty()) {
            cloudSurname = document.getString("profile_surname");
        }
        String cloudState = document.getString("state");
        String cloudCity = document.getString("city");
        putProfileNameFields(editor, fullName, cloudFirstName, cloudSurname);
        if (isVerified) editor.putBoolean("email_verified", true);
        if (cloudEmail != null && !cloudEmail.trim().isEmpty() && !cloudEmail.endsWith("@login.fendly.app")) {
            editor.putString("email", cloudEmail.trim());
        }
        if (cloudMobile != null && !cloudMobile.trim().isEmpty()) {
            editor.putString("mobile", cloudMobile.trim());
        }
        if (cloudState != null && !cloudState.trim().isEmpty()) {
            editor.putString("state", cloudState.trim());
        }
        if (cloudCity != null && !cloudCity.trim().isEmpty()) {
            editor.putString("city", cloudCity.trim());
        }
        syncProfileImageFromDocument(editor, account, document,
                "profile_photo_url", "imageUrl", "profile_image_url");
        editor.apply();
    }

    private void showProfile() {
        if (currentPage == PAGE_PROFILE && (visibleFirstName != null || visibleAvatar != null || visibleEmail != null)) {
            refreshProfileViewInPlace(true);
            return;
        }

        profileScrollY = 0;
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        currentPage = PAGE_PROFILE;
        screenRenderer = this::showProfile;

        if (currentUser == null) {
            renderProfileContent();
            return;
        }

        renderProfileContent();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String primaryKey = getProfileDocumentKey();
        String fallbackKey = currentUser.getUid();

        Runnable proceedWithProfile = () -> {
            currentUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
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
                            boolean remoteEmailVerified = profile.optBoolean("email_verified", false);
                            boolean remoteMobileVerified = profile.optBoolean("mobile_verified", false);
                            String remoteEmail = profile.optString("email", "").trim();
                            String remoteMobile = profile.optString("mobile", "").trim();
                            String remoteFullName = profile.optString("full_name", "").trim();
                            String remoteState = profile.optString("state", "").trim();
                            String remoteCity = profile.optString("city", "").trim();
                            String previousPhotoUrl = account.getString("profile_image_url", "").trim();
                            SharedPreferences.Editor backendEditor = account.edit();
                            if (remoteEmailVerified) backendEditor.putBoolean("email_verified", true);
                            if (!remoteMobile.isEmpty()) {
                                backendEditor.putBoolean("mobile_verified", remoteMobileVerified);
                                if (remoteMobileVerified) {
                                    backendEditor.putString("verified_mobile", normalizeIndianMobileDigits(remoteMobile));
                                } else {
                                    backendEditor.remove("verified_mobile");
                                }
                            }
                            if (!remoteEmail.isEmpty() && !remoteEmail.endsWith("@login.fendly.app")) backendEditor.putString("email", remoteEmail);
                            if (!remoteMobile.isEmpty()) backendEditor.putString("mobile", remoteMobile);
                            putProfileNameFields(backendEditor, remoteFullName, "", "");
                            if (!remoteState.isEmpty()) backendEditor.putString("state", remoteState);
                            if (!remoteCity.isEmpty()) backendEditor.putString("city", remoteCity);
                            String updatedPhotoUrl = syncProfileImageFromJson(backendEditor, account, profile,
                                    "profile_photo_url", "imageUrl", "image_url");
                            backendEditor.apply();
                            if (!updatedPhotoUrl.equals(previousPhotoUrl)) {
                                runOnUiThread(() -> {
                                    if (currentPage == PAGE_PROFILE && visibleAvatar != null) {
                                        bindProfilePhoto(visibleAvatar, account);
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    if (connection != null) connection.disconnect();
                    runOnUiThread(() -> {
                        if (currentPage == PAGE_PROFILE) refreshProfileViewInPlace(false);
                    });
                }
            })).addOnFailureListener(err -> {
                runOnUiThread(() -> {
                    if (currentPage == PAGE_PROFILE) refreshProfileViewInPlace(false);
                });
            });
        };

        db.collection("users").document(primaryKey)
                .get(Source.SERVER)
                .addOnSuccessListener(document -> {
                    if ((document == null || !document.exists()) && !fallbackKey.equals(primaryKey)) {
                        db.collection("users").document(fallbackKey)
                                .get(Source.SERVER)
                                .addOnSuccessListener(fallbackDoc -> {
                                    processDocumentToAccount(fallbackDoc, account);
                                    proceedWithProfile.run();
                                })
                                .addOnFailureListener(e -> proceedWithProfile.run());
                        return;
                    }
                    processDocumentToAccount(document, account);
                    proceedWithProfile.run();
                })
                .addOnFailureListener(e -> {
                    if (!fallbackKey.equals(primaryKey)) {
                        db.collection("users").document(fallbackKey)
                                .get(Source.SERVER)
                                .addOnSuccessListener(fallbackDoc -> {
                                    processDocumentToAccount(fallbackDoc, account);
                                    proceedWithProfile.run();
                                })
                                .addOnFailureListener(err -> proceedWithProfile.run());
                    } else {
                        proceedWithProfile.run();
                    }
                });
    }

    private void refreshProfileViewInPlace(boolean refreshAvatar) {
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        if (refreshAvatar && visibleAvatar != null) {
            if (profileImageExplicitlyRemoved) {
                visibleAvatar.setImageResource(R.drawable.ic_field_person);
                visibleAvatar.setColorFilter(accentColor());
                visibleAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int pad = dp(24);
                visibleAvatar.setPadding(pad, pad, pad, pad);
            } else {
                bindProfilePhoto(visibleAvatar, account);
            }
        }

        String firstName = account.getString("profile_first_name", "");
        String surname = account.getString("profile_surname", "");
        String fullName = account.getString("full_name", "");
        String[] nameParts = getCanonicalEnglishName(fullName).split("\\s+", 2);
        if (firstName.trim().isEmpty() && nameParts.length > 0) firstName = nameParts[0];
        if (surname.trim().isEmpty() && nameParts.length > 1) surname = nameParts[1];
        if (!firstName.isEmpty() && visibleFirstName != null) {
            setVisibleText(visibleFirstName, localizeProfileName(firstName));
        }
        if (visibleSurname != null) {
            if (!surname.isEmpty()) {
                setVisibleText(visibleSurname, localizeProfileName(surname));
            }
        }
        String state = account.getString("state", "").trim();
        String city = account.getString("city", "").trim();
        if (visibleStateSearch != null && visibleStateSearch.getText().toString().trim().isEmpty() && !state.isEmpty()) {
            visibleStateSearch.setText(localizeProfileDisplayValue("state", state));
            visibleStateSearch.setTag(state);
        }
        if (visibleCitySearch != null && visibleCitySearch.getText().toString().trim().isEmpty() && !city.isEmpty()) {
            visibleCitySearch.setText(localizeProfileDisplayValue("city", city));
            visibleCitySearch.setTag(city);
        }
        if (visibleCitySearch != null) {
            String selectedState = visibleStateSearch == null
                    ? state
                    : String.valueOf(visibleStateSearch.getTag() == null ? state : visibleStateSearch.getTag());
            String[] cities = indiaStateCityMap().getOrDefault(selectedState, new String[0]);
            visibleCitySearch.setEnabled(cities.length > 0 && !isSelectCityPlaceholder(cities[0]));
        }
        if (visibleEmail != null) {
            String email = account.getString("email", "");
            if (!email.endsWith("@login.fendly.app")) {
                setVisibleText(visibleEmail, email);
            }
        }
        if (visibleMobileCells != null) {
            String mobile = account.getString("mobile", "").trim();
            if (!mobile.isEmpty() && visibleMobileCells.length > 0) {
                setMobileCells(visibleMobileCells, mobile);
            }
        }
        refreshMobileVerificationUi();
    }

    private void renderProfileContent() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        profileHydrated = true;
        cloudProfileLoaded = true;
        inRenewalPaymentFlow = false;
        currentPage = PAGE_PROFILE;
        screenRenderer = this::showProfile;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        LinearLayout root = screenBase(getString(R.string.profile_title));
        String profileHeadingName = account.getString("full_name", account.getString("username", "Your profile"));
        LinearLayout profileHeader = new LinearLayout(this);
        profileHeader.setOrientation(LinearLayout.HORIZONTAL);
        profileHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView profileTitle = text(
                localizeProfileName(profileHeadingName),
                20,
                primaryTextColor(),
                Typeface.NORMAL
        );
        profileTitle.setGravity(Gravity.CENTER);
        profileTitle.setTypeface(localizedScriptTypeface(profileTitle.getText(), Typeface.NORMAL));
        profileHeader.addView(profileTitle, new LinearLayout.LayoutParams(0, dp(44), 1f));
        activeContent.addView(profileHeader, new LinearLayout.LayoutParams(-1, dp(48)));
        TextView profileSubtitle = text(
                translate(getString(R.string.profile_account_details)),
                11,
                secondaryTextColor(),
                Typeface.NORMAL
        );
        profileSubtitle.setGravity(Gravity.CENTER);
        addField(activeContent, profileSubtitle);

        LinearLayout photoSection = new LinearLayout(this);
        photoSection.setOrientation(LinearLayout.VERTICAL);
        photoSection.setGravity(Gravity.CENTER_HORIZONTAL);
        photoSection.setPadding(0, dp(8), 0, dp(14));

        FrameLayout avatarWrap = new FrameLayout(this);
        avatarWrap.setBackground(roundWithStroke(surfaceColor(), 60, borderColor()));
        avatarWrap.setPadding(0, 0, 0, 0);
        avatarWrap.setOnClickListener(view -> showProfilePhotoOptions());
        avatarWrap.setClipToOutline(true);

        ImageView avatar = new ImageView(this);
        avatar.setBackground(roundWithStroke(surfaceColor(), 60, fieldBorderColor()));
        avatar.setLayoutParams(new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER));
        avatar.setImageResource(R.drawable.ic_field_person);
        avatar.setClipToOutline(true);

        visibleAvatar = avatar;
        bindProfilePhoto(avatar, account);

        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(dp(118), dp(118), Gravity.CENTER);
        avatarWrap.addView(avatar, avatarParams);
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
        String savedFirstName = account.getString("profile_first_name", "").trim();
        if (savedFirstName.isEmpty() && nameParts.length > 0) savedFirstName = nameParts[0];
        String savedSurname = account.getString("profile_surname", "").trim();
        if (savedSurname.isEmpty() && nameParts.length > 1) savedSurname = nameParts[1];
        firstName.setText(localizeProfileName(savedFirstName));
        surname.setText(localizeProfileName(savedSurname));
        firstName.setTag(savedFirstName);
        surname.setTag(savedSurname);
        visibleFirstName = firstName;
        visibleSurname = surname;
        visibleNameDirty = false;
        firstName.addTextChangedListener(draftWatcher(value -> {
            if (applyingCloudProfile) return;
            String fn = getCanonicalEnglishName(value);
            String sn = getCanonicalEnglishName(surname.getText().toString().trim());
            draftFullName = (fn + " " + sn).trim();
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .putString("profile_first_name", fn)
                    .putString("profile_surname", sn)
                    .apply();
            visibleNameDirty = true;
            saveProfileDrafts();
            scheduleRealtimeProfileSave();
        }));
        surname.addTextChangedListener(draftWatcher(value -> {
            if (applyingCloudProfile) return;
            String fn = getCanonicalEnglishName(firstName.getText().toString().trim());
            String sn = getCanonicalEnglishName(value);
            draftFullName = (fn + " " + sn).trim();
            getSharedPreferences("fendly_account", MODE_PRIVATE).edit()
                    .putString("profile_first_name", fn)
                    .putString("profile_surname", sn)
                    .apply();
            visibleNameDirty = true;
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
        String profileEmail = account.getString("email", "");
        if (profileEmail.endsWith("@login.fendly.app")) profileEmail = "";
        email.setText(profileEmail);
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

        EditText[] mobileCells = mobileCells();
        visibleMobileCells = mobileCells;
        String savedMobile = normalizeLocalizedDigits(account.getString("mobile", ""));
        draftMobile = savedMobile;
        setMobileCells(mobileCells, savedMobile);
        for (EditText cell : mobileCells) {
            cell.addTextChangedListener(draftWatcher(value -> {
                draftMobile = normalizeLocalizedDigits(mobileValue(mobileCells));
                saveProfileDrafts();
                scheduleRealtimeProfileSave();
            }));
        }
        EditText mobileField = mobileCells[0];
        mobileField.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String currentMobile = normalizeLocalizedDigits(mobileField.getText().toString().trim());
                if (!currentMobile.isEmpty() && !currentMobile.matches("^\\d{10}$")) {
                    mobileField.setError("Mobile number must be 10 digits");
                } else {
                    mobileField.setError(null);
                }
            }
        });

        TextView emailVerify = filledButton(getString(R.string.profile_verify_otp), GOLD, GOLD_ON);
        emailVerify.setPadding(dp(12), 0, dp(12), 0);
        emailVerify.setOnClickListener(view -> sendEmailOtpFlow(email, emailVerify));
        visibleEmail = email;
        visibleEmailVerify = emailVerify;
        refreshEmailVerificationState(email, emailVerify);

        TextView mobileVerify = filledButton(getString(R.string.profile_verify_otp), GOLD, GOLD_ON);
        visibleMobileVerify = mobileVerify;
        mobileVerify.setPadding(dp(10), 0, dp(10), 0);
        mobileVerify.setOnClickListener(view -> verifyProfileMobileTarget(mobileCells, mobileVerify));
        String verifiedMobile = account.getString("mobile", "").trim();
        for (EditText cell : mobileCells) {
            cell.setEnabled(true);
            cell.setFocusable(true);
            cell.setFocusableInTouchMode(true);
            cell.setCursorVisible(true);
        }
        boolean isMobileVerified = isMobileVerifiedFor(savedMobile, account);
        if (isMobileVerified) lockVerifiedMobileField(mobileCells, mobileVerify);
        for (EditText cell : mobileCells) {
            cell.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                    String currentMobile = normalizeLocalizedDigits(mobileValue(mobileCells));
                    boolean verifiedNow = currentMobile.matches("^\\d{10}$")
                        && isMobileVerifiedFor(currentMobile, getSharedPreferences("fendly_account", MODE_PRIVATE));
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
                    }
                }
                @Override public void afterTextChanged(Editable value) { }
            });
        }

        Map<String, String[]> stateCities = indiaStateCityMap();
        String[] states = stateCities.keySet().toArray(new String[0]);
        Arrays.sort(states, 1, states.length);
        AutoCompleteTextView stateSearch = new AutoCompleteTextView(this);
        AutoCompleteTextView citySearch = new AutoCompleteTextView(this);
        visibleStateSearch = stateSearch;
        visibleCitySearch = citySearch;
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
        addLabeledMobileField(root, LanguageManager.profileText(this, "profile_mobile"), mobileCells, mobileVerify);

        EditText[] changePinCells = pinCells();
        LinearLayout pinGroup = new LinearLayout(this);
        pinGroup.setOrientation(LinearLayout.VERTICAL);
        pinGroup.setVisibility(View.GONE);

        for (EditText cell : changePinCells) {
            if (cell != null && cell.getParent() instanceof ViewGroup) {
                ((ViewGroup) cell.getParent()).removeView(cell);
            }
        }
        pinGroup.addView(fieldLabel(getString(R.string.profile_new_pin)), new LinearLayout.LayoutParams(-1, dp(20)));
        LinearLayout pinRow = new LinearLayout(this);
        pinRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int index = 0; index < changePinCells.length; index++) {
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
            if (index > 0) cellParams.setMargins(dp(8), 0, 0, 0);
            pinRow.addView(changePinCells[index], cellParams);
        }
        pinGroup.addView(pinRow, new LinearLayout.LayoutParams(-1, dp(46)));
        root.addView(pinGroup, contentParams(-1, dp(76), dp(4)));

        TextView changePinButton = actionButton(getString(R.string.profile_change_pin), false);
        changePinButton.setOnClickListener(view -> {
            if (pinGroup.getVisibility() == View.GONE) {
                pinGroup.setVisibility(View.VISIBLE);
                changePinCells[0].requestFocus();

                pinGroup.postDelayed(() -> {
                    ViewParent parent = pinGroup.getParent();
                    while (parent != null) {
                        if (parent instanceof ScrollView) {
                            ScrollView scroll = (ScrollView) parent;
                            scroll.smoothScrollTo(0, pinGroup.getTop() - dp(16));
                            break;
                        }
                        if (parent instanceof View) {
                            parent = ((View) parent).getParent();
                        } else {
                            break;
                        }
                    }
                }, 150L);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(changePinCells[0], InputMethodManager.SHOW_IMPLICIT);
            } else {
                String newPin = pinValue(changePinCells);
                if (newPin.length() != 4) {
                    Toast.makeText(this, "Enter a valid 4-digit PIN", Toast.LENGTH_LONG).show();
                    changePinCells[0].requestFocus();
                    return;
                }
                changeAccountPin(account.getString("username", ""), getStoredAccountPin(), newPin, changePinButton);
            }
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
            String updatedMobile = normalizeLocalizedDigits(mobileValue(mobileCells));
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
                Toast.makeText(this, "Mobile invalid", Toast.LENGTH_SHORT).show();
                mobileCells[0].requestFocus();
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
                mobileCells[0].requestFocus();
                return;
            }
            String lastSavedEmail = account.getString("email", "").trim();
            boolean verifiedEmailAddress;
            if (account.getBoolean("email_verified", false)) {
                if (lastSavedEmail.isEmpty() || updatedEmail.equalsIgnoreCase(lastSavedEmail)) {
                    verifiedEmailAddress = true;
                } else {
                    verifiedEmailAddress = false;
                }
            } else {
                verifiedEmailAddress = false;
            }
            account.edit()
                    .putString("full_name", updatedFullName)
                    .putString("email", updatedEmail)
                    .putString("mobile", updatedMobile)
                    .putBoolean("email_verified", verifiedEmailAddress)
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

        TextView deleteAccount = actionButton(LanguageManager.profileText(this, "delete_account"), false);
        deleteAccount.setTextColor(Color.rgb(190, 55, 55));
        deleteAccount.setOnClickListener(view -> showDeleteAccountDialog());
        root.addView(deleteAccount, contentParams(-1, dp(44), dp(4)));

        TextView privacyPolicy = actionButton(LanguageManager.profileText(this, "privacy_data_deletion"), false);
        privacyPolicy.setOnClickListener(view -> showPrivacyPolicyDialog());
        root.addView(privacyPolicy, contentParams(-1, dp(44), dp(4)));

        TextView home = actionButton(getString(R.string.profile_back_home), false);
        home.setOnClickListener(view -> showHome());
        addField(root, home);
        applyProfileFont(root);
    }

    private void showPrivacyPolicyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                0,
                LanguageManager.profileText(this, "privacy_data_deletion"),
                LanguageManager.profileText(this, "privacy_subtitle")
        );

        FrameLayout policyContainer = new FrameLayout(this);
        policyContainer.setBackground(roundWithStroke(surfaceColor(), 12, borderColor()));
        WebView policyView = new WebView(this);
        policyView.setBackgroundColor(surfaceColor());
        WebSettings settings = policyView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        policyContainer.addView(policyView, new FrameLayout.LayoutParams(-1, -1));

        TextView loadError = text(
                LanguageManager.profileText(this, "privacy_load_failed"),
                13,
                secondaryTextColor(),
                Typeface.NORMAL
        );
        loadError.setGravity(Gravity.CENTER);
        loadError.setVisibility(View.GONE);
        policyContainer.addView(loadError, new FrameLayout.LayoutParams(-1, -1));
        policyView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handlePrivacyPolicyNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handlePrivacyPolicyNavigation(Uri.parse(url));
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    android.webkit.WebResourceError error
            ) {
                if (request.isForMainFrame()) {
                    policyView.setVisibility(View.GONE);
                    loadError.setVisibility(View.VISIBLE);
                }
            }
        });

        int availableHeight = getResources().getDisplayMetrics().heightPixels - dp(250);
        int policyHeight = Math.max(dp(280), Math.min(dp(520), availableHeight));
        LinearLayout.LayoutParams policyParams = new LinearLayout.LayoutParams(-1, policyHeight);
        policyParams.setMargins(0, dp(8), 0, dp(10));
        content.addView(policyContainer, policyParams);

        TextView close = actionButton(LanguageManager.profileText(this, "close"), true);
        close.setOnClickListener(view -> dialog.dismiss());
        content.addView(close, new LinearLayout.LayoutParams(-1, dp(44)));

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
        policyView.loadDataWithBaseURL(
                API_BASE + "/static/",
                LanguageManager.privacyInformationHtml(this),
                "text/html",
                "UTF-8",
                null
        );
    }

    private void openCommunityGuidelines() {
        Uri uri = Uri.parse(BuildConfig.API_BASE_URL + "/static/community-guidelines.html");
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException error) {
            Log.e("COMMUNITY_GUIDELINES", "No app is available to open the guidelines", error);
            Toast.makeText(
                    this,
                    LanguageManager.profileText(this, "privacy_open_link_failed"),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private boolean handlePrivacyPolicyNavigation(Uri uri) {
        if ("https".equalsIgnoreCase(uri.getScheme())
                && "fendly-api.onrender.com".equalsIgnoreCase(uri.getHost())) {
            return false;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())
                && !"mailto".equalsIgnoreCase(uri.getScheme())
                && !"tel".equalsIgnoreCase(uri.getScheme())) {
            Log.w("PRIVACY_POLICY", "Blocked unsupported policy link scheme");
            return true;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException error) {
            Log.e("PRIVACY_POLICY", "No app is available to open the policy link", error);
            Toast.makeText(this, LanguageManager.profileText(this, "privacy_open_link_failed"), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private LinearLayout screenBase(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(responsiveHorizontalPadding(), dp(16), responsiveHorizontalPadding(), dp(30));
        root.setBackgroundColor(backgroundColor());
        if (Build.VERSION.SDK_INT >= 29) root.setForceDarkAllowed(false);
        WindowInsetsHelper.applySafeArea(root);
        addAppControls(root, false);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setHorizontalScrollBarEnabled(false);
        if (currentPage == PAGE_PROFILE) {
            scroll.setOnScrollChangeListener((View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> profileScrollY = scrollY);
        }
        if (currentPage == PAGE_PROFILE || currentPage == PAGE_PROFILE_SETUP) {
            scroll.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                View focused = getCurrentFocus();
                if (focused != null) {
                    ensureFocusedProfileFieldVisible(scroll, focused);
                }
            });
        }
        activeContent = new LinearLayout(this);
        activeContent.setOrientation(LinearLayout.VERTICAL);
        activeContent.setPadding(0, dp(26), 0, 0);
        activeContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(activeContent, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        return activeContent;
    }

    private void ensureFocusedProfileFieldVisible(ScrollView scroll, View focused) {
        ViewParent parent = focused.getParent();
        while (parent != null && parent != scroll) {
            parent = parent.getParent();
        }
        if (parent != scroll) return;

        scroll.post(() -> {
            if (!focused.isAttachedToWindow()) return;
            int[] scrollLocation = new int[2];
            int[] focusedLocation = new int[2];
            scroll.getLocationOnScreen(scrollLocation);
            focused.getLocationOnScreen(focusedLocation);

            int viewportTop = scrollLocation[1] + scroll.getPaddingTop();
            int viewportBottom = scrollLocation[1] + scroll.getHeight() - scroll.getPaddingBottom();
            int fieldTop = focusedLocation[1];
            int fieldBottom = fieldTop + focused.getHeight();
            int margin = dp(12);
            if (fieldBottom + margin > viewportBottom) {
                scroll.scrollBy(0, fieldBottom + margin - viewportBottom);
            } else if (fieldTop - margin < viewportTop) {
                scroll.scrollBy(0, fieldTop - margin - viewportTop);
            }
        });
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

    private void addAdminEnglishHeading(String title, String subtitle) {
        TextView titleView = text(title, 20, primaryTextColor(), Typeface.NORMAL);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(localizedScriptTypeface(titleView.getText(), Typeface.NORMAL));
        activeContent.addView(titleView, new LinearLayout.LayoutParams(-1, dp(32)));
        TextView subtitleView = text(subtitle, 11, secondaryTextColor(), Typeface.NORMAL);
        subtitleView.setGravity(Gravity.CENTER);
        addField(activeContent, subtitleView);
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        if (Build.VERSION.SDK_INT >= 29) input.setForceDarkAllowed(false);
        input.setId(View.generateViewId());
        input.setHint("");
        input.setTextSize(responsiveTextSize(14));
        input.setTag(Float.valueOf(14));
        input.setTextColor(primaryTextColor());
        input.setHintTextColor(secondaryTextColor());
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        input.setPadding(dp(16), dp(8), dp(16), dp(8));
        input.setIncludeFontPadding(false);
        input.setSingleLine(false);
        input.setMaxLines(2);
        input.setHorizontallyScrolling(false);
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
        Drawable fieldIcon = AppCompatResources.getDrawable(this, icon);
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
        boolean isCompact = getResources().getConfiguration().screenWidthDp < 410
                || getResources().getConfiguration().fontScale > 1.1f;
        card.setOrientation(isCompact ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        card.setGravity(isCompact ? Gravity.START : Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 18, borderColor()));

        TextView titleView = text(title, isCompact ? 15 : 18, secondaryTextColor(), Typeface.NORMAL);
        titleView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        titleView.setIncludeFontPadding(false);
        titleView.setSingleLine(false);
        titleView.setMaxLines(2);

        TextView detailView = text("", isCompact ? 18 : 20, primaryTextColor(), Typeface.NORMAL);
        detailView.setGravity(isCompact ? Gravity.END : Gravity.CENTER_VERTICAL);
        detailView.setIncludeFontPadding(false);

        String priceText = detail != null && !detail.trim().isEmpty() ? detail : localizedAnnualPriceText();
        SpannableString detailText = new SpannableString(priceText);
        int priceLength = Math.min(3, detailText.length());
        detailText.setSpan(new StyleSpan(Typeface.NORMAL), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailText.setSpan(new ForegroundColorSpan(primaryTextColor()), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailText.setSpan(new ForegroundColorSpan(secondaryTextColor()), priceLength, detailText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailView.setText(detailText);

        if (isCompact) {
            card.addView(titleView, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout bottomRow = new LinearLayout(this);
            bottomRow.setOrientation(LinearLayout.HORIZONTAL);
            bottomRow.setGravity(Gravity.CENTER_VERTICAL);
            TextView space = text("", 1, Color.TRANSPARENT, Typeface.NORMAL);
            bottomRow.addView(space, new LinearLayout.LayoutParams(0, -2, 1f));
            bottomRow.addView(detailView, new LinearLayout.LayoutParams(-2, -2));
            LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(-1, -2);
            bottomParams.setMargins(0, dp(6), 0, 0);
            card.addView(bottomRow, bottomParams);
        } else {
            card.addView(titleView, new LinearLayout.LayoutParams(0, -2, 1f));
            card.addView(detailView, new LinearLayout.LayoutParams(-2, -2));
        }
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
        TextView titleView = text(title, 15, primaryTextColor(), Typeface.NORMAL);
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
        if (verifyButton != null && verifyButton.getParent() instanceof ViewGroup) {
            ((ViewGroup) verifyButton.getParent()).removeView(verifyButton);
        }
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.addView(fieldLabel(label), new LinearLayout.LayoutParams(-1, dp(20)));

        input.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(46)));
        input.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        input.setPadding(dp(16), dp(8), dp(16), dp(8));
        input.setSingleLine(true);
        input.setEnabled(!"Location".equals(label));
        group.addView(input);

        if (verifyButton != null) {
            verifyButton.setClickable(true);
            verifyButton.setFocusable(true);
            verifyButton.setEnabled(true);
            verifyButton.setIncludeFontPadding(false);
            verifyButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            verifyButton.setSingleLine(true);
            verifyButton.setPadding(dp(16), dp(8), dp(16), dp(8));
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(40));
            buttonParams.setMargins(0, dp(8), 0, 0);
            verifyButton.setLayoutParams(buttonParams);
            group.addView(verifyButton);
            parent.addView(group, contentParams(-1, dp(120), dp(8)));
        } else {
            parent.addView(group, contentParams(-1, dp(76), dp(4)));
        }
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
        TextView labelView = text(displayLabel, 10, secondaryTextColor(), Typeface.NORMAL);
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

    private EditText[] mobileCells() {
        EditText mobileField = field(LanguageManager.profileText(this, "profile_mobile_number"));
        mobileField.setKeyListener(DigitsKeyListener.getInstance("0123456789" + localizedProfileDigits()));
        mobileField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mobileField.addTextChangedListener(new TextWatcher() {
            private boolean normalizing;

            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }

            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { }

            @Override public void afterTextChanged(Editable value) {
                if (normalizing) return;
                String normalized = normalizeLocalizedDigits(value.toString()).replaceAll("\\D+", "");
                String localized = localizeDigits(normalized);
                if (localized.equals(value.toString())) return;

                int selection = mobileField.getSelectionStart();
                normalizing = true;
                mobileField.setText(localized);
                if (selection >= 0) {
                    mobileField.setSelection(Math.min(selection, localized.length()));
                }
                normalizing = false;
            }
        });
        mobileField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return new EditText[]{mobileField};
    }

    private String localizedProfileDigits() {
        String[] digitSets = {
                "0123456789", "०१२३४५६७८९", "०१२३४५६७८९", "૦૧૨૩૪૫૬૭૮૯",
                "০১২৩৪৫৬৭৮৯", "௦௧௨௩௪௫௬௭௮௯", "౦౧౨౩౪౫౬౭౮౯", "೦೧೨೩೪೫೬೭೮೯",
                "൦൧൨൩൪൫൬൭൮൯"
        };
        int language = Math.max(0, Math.min(selectedLanguage, digitSets.length - 1));
        return digitSets[language];
    }

    private String mobileValue(EditText[] cells) {
        StringBuilder builder = new StringBuilder();
        if (cells != null) {
            for (EditText cell : cells) {
                if (cell != null) builder.append(cell.getText().toString());
            }
        }
        return builder.toString();
    }

    private void setMobileCells(EditText[] cells, String value) {
        if (cells == null) return;
        String clean = normalizeLocalizedDigits(value != null ? value : "").replaceAll("\\D+", "");
        if (cells.length != 1) {
            throw new IllegalArgumentException("Mobile profile field must contain one text input");
        }
        cells[0].setText(clean);
    }

    private boolean hasMobileCellsFocus() {
        if (visibleMobileCells == null) return false;
        for (EditText cell : visibleMobileCells) {
            if (cell != null && cell.hasFocus()) return true;
        }
        return false;
    }

    private void addLabeledMobileField(LinearLayout parent, String label, EditText[] cells, TextView verifyButton) {
        if (cells == null || cells.length != 1) {
            throw new IllegalArgumentException("Mobile profile field must contain one text input");
        }
        addEditableProfileField(parent, label, cells[0], verifyButton);
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

    private void lockVerifiedEmailField(EditText email, TextView verifyButton) {
        if (email != null) {
            email.setEnabled(false);
            email.setFocusable(false);
            email.setFocusableInTouchMode(false);
            email.setCursorVisible(false);
        }
        setVerifiedButtonState(verifyButton);
    }

    private boolean isMobileVerifiedFor(String mobileValue, SharedPreferences account) {
        String mobile = normalizeIndianMobileDigits(mobileValue);
        String savedMobile = normalizeIndianMobileDigits(account.getString("mobile", ""));
        String verifiedMobile = normalizeIndianMobileDigits(
                account.getString("verified_mobile", savedMobile)
        );
        return account.getBoolean("mobile_verified", false)
                && mobile.matches("^\\d{10}$")
                && mobile.equals(savedMobile)
                && mobile.equals(verifiedMobile);
    }

    private void refreshMobileVerificationUi() {
        if (visibleMobileCells == null || visibleMobileVerify == null) return;
        SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
        String mobile = normalizeLocalizedDigits(mobileValue(visibleMobileCells));
        if (isMobileVerifiedFor(mobile, account)) {
            lockVerifiedMobileField(visibleMobileCells, visibleMobileVerify);
            return;
        }
        if (!visibleMobileVerify.isEnabled()) {
            visibleMobileVerify.setOnClickListener(view ->
                    verifyProfileMobileTarget(visibleMobileCells, visibleMobileVerify));
            visibleMobileVerify.setText(translate("Verify OTP"));
            visibleMobileVerify.setEnabled(true);
            visibleMobileVerify.setClickable(true);
            visibleMobileVerify.setFocusable(true);
            visibleMobileVerify.setBackground(round(GOLD, 24));
            visibleMobileVerify.setTextColor(GOLD_ON);
        }
        for (EditText cell : visibleMobileCells) {
            if (cell == null) continue;
            cell.setEnabled(true);
            cell.setFocusable(true);
            cell.setFocusableInTouchMode(true);
            cell.setCursorVisible(true);
        }
    }

    private void lockVerifiedMobileField(Object mobileTarget, TextView verifyButton) {
        if (mobileTarget instanceof EditText) {
            EditText mobile = (EditText) mobileTarget;
            mobile.setEnabled(false);
            mobile.setFocusable(false);
            mobile.setFocusableInTouchMode(false);
            mobile.setCursorVisible(false);
        } else if (mobileTarget instanceof EditText[]) {
            for (EditText cell : (EditText[]) mobileTarget) {
                if (cell != null) {
                    cell.setEnabled(false);
                    cell.setFocusable(false);
                    cell.setFocusableInTouchMode(false);
                    cell.setCursorVisible(false);
                }
            }
        }
        setVerifiedButtonState(verifyButton);
    }

    private void verifyProfileMobileTarget(Object mobileTarget, TextView verifyButton) {
        String mobileValue = "";
        if (mobileTarget instanceof EditText) {
            mobileValue = normalizeLocalizedDigits(((EditText) mobileTarget).getText().toString().trim());
        } else if (mobileTarget instanceof EditText[]) {
            mobileValue = normalizeLocalizedDigits(mobileValue((EditText[]) mobileTarget));
        }
        if (!mobileValue.matches("^\\d{10}$")) {
            Toast.makeText(this, "Enter a valid 10-digit mobile number", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sign in to verify your mobile number", Toast.LENGTH_LONG).show();
            return;
        }
        verifyButton.setText(translate("Sending..."));
        verifyButton.setEnabled(false);
        sendBackendMobileOtp(mobileValue, mobileTarget, verifyButton);
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
        button.setTextSize(responsiveTextSize(12));
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
        button.setTextSize(responsiveTextSize(12));
        button.setGravity(Gravity.CENTER);
        button.setBackground(roundWithStroke(surfaceColor(), 24, borderColor()));
        return button;
    }

    private float responsiveTextSize(float baseSize) {
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        float widthFactor = screenWidthDp < 360 ? 0.90f : screenWidthDp < 480 ? 0.94f : screenWidthDp < 720 ? 1.0f : 1.08f;
        float fontScale = getSharedPreferences("fendly_settings", MODE_PRIVATE).getFloat("font_scale", 1.0f);
        float safeFontScale = Math.max(0.85f, Math.min(1.25f, fontScale));
        return baseSize * widthFactor * safeFontScale;
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
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 18, borderColor()));

        TextView subscriptionLabel = text(translate("Subscription"), 15, secondaryTextColor(), Typeface.NORMAL);
        subscriptionLabel.setIncludeFontPadding(false);
        subscriptionLabel.setSingleLine(true);

        TextView subscriptionPrice = text("", 16, primaryTextColor(), Typeface.NORMAL);
        subscriptionPrice.setIncludeFontPadding(false);
        subscriptionPrice.setSingleLine(true);

        String priceTextValue = localizedAnnualPriceText();
        SpannableString priceText = new SpannableString(priceTextValue);
        int priceLength = Math.min(3, priceText.length());
        priceText.setSpan(new StyleSpan(Typeface.NORMAL), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceText.setSpan(new ForegroundColorSpan(primaryTextColor()), 0, priceLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceText.setSpan(new ForegroundColorSpan(secondaryTextColor()), priceLength, priceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        subscriptionPrice.setText(priceText);

        ImageView lock = new ImageView(this);
        lock.setImageResource(R.drawable.ic_premium_lock);
        lock.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        lock.setColorFilter(GOLD);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.HORIZONTAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-2, -2);
        copy.addView(subscriptionLabel, labelParams);

        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(-2, -2);
        priceParams.setMargins(dp(8), 0, 0, 0);
        copy.addView(subscriptionPrice, priceParams);

        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout.LayoutParams lockParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        lockParams.setMargins(dp(12), 0, 0, 0);
        card.addView(lock, lockParams);

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

    private LinearLayout matchCard() {
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

        TextView chip = text("Match — 94%", 10, SuccessMintColor(), Typeface.NORMAL);
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
            button.setText(translate("Location ready"));
            button.setTextColor(Color.WHITE);
            button.setBackground(roundWithStroke(LOST_GREEN, 10, LOST_GREEN));
        } else {
            button.setText(translate("Precise location  OFF"));
            button.setTextColor(secondaryTextColor());
            button.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        }
        if (toggle != null) {
            if (enabled) {
                toggle.setText(translate("Precise location  ON"));
                toggle.setTextColor(Color.WHITE);
                toggle.setBackground(roundWithStroke(LOST_GREEN, 10, LOST_GREEN));
            } else {
                toggle.setText(translate("Precise location  OFF"));
                toggle.setTextColor(secondaryTextColor());
                toggle.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
            }
        }
    }

    private boolean isReportLocationEnabled(String reportType) {
        return "LOST".equalsIgnoreCase(reportType) ? lostReportHasLocation : foundReportHasLocation;
    }

    private double reportLocationLatitude(String reportType) {
        return "LOST".equalsIgnoreCase(reportType) ? lostReportLat : foundReportLat;
    }

    private double reportLocationLongitude(String reportType) {
        return "LOST".equalsIgnoreCase(reportType) ? lostReportLng : foundReportLng;
    }

    private void setReportLocationState(String reportType, boolean enabled, double latitude, double longitude) {
        if ("LOST".equalsIgnoreCase(reportType)) {
            lostReportHasLocation = enabled;
            lostReportLat = enabled ? latitude : 0.0;
            lostReportLng = enabled ? longitude : 0.0;
        } else if ("FOUND".equalsIgnoreCase(reportType)) {
            foundReportHasLocation = enabled;
            foundReportLat = enabled ? latitude : 0.0;
            foundReportLng = enabled ? longitude : 0.0;
        }
    }

    private void restoreReportLocationState(String reportType) {
        hasLocation = isReportLocationEnabled(reportType);
        currentLat = hasLocation ? reportLocationLatitude(reportType) : 0.0;
        currentLng = hasLocation ? reportLocationLongitude(reportType) : 0.0;
        activeLocationReportType = hasLocation ? reportType : null;
    }

    private void requestLocation(TextView button, TextView toggle) {
        locationRequestReportType = currentReportType;
        long requestGeneration = ++locationRequestGeneration;
        locationStatus = button;
        locationToggleStatus = toggle;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 702);
            if (button != null) button.setText(translate("Location permission requested"));
            return;
        }
        enableLocationServicesIfNeeded(button, toggle, locationRequestReportType, requestGeneration);
    }

    private void enableLocationServicesIfNeeded(TextView button, TextView toggle, String reportType, long requestGeneration) {
        try {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (manager == null) {
                if (button != null) button.setText(translate("Location unavailable"));
                return;
            }

            boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!gpsEnabled && !networkEnabled) {
                if (button != null) {
                    button.setText(translate("Turn on location"));
                    button.setTextColor(secondaryTextColor());
                    button.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
                }
                Toast.makeText(this, "Please turn on device location to use precise location.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            updateLocation(button, toggle, reportType, requestGeneration);
        } catch (Exception error) {
            if (button != null) button.setText(translate("Location unavailable"));
        }
    }

    private void updateLocation(TextView button, TextView toggle, String reportType, long requestGeneration) {
        try {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (manager == null) {
                if (button != null) button.setText(translate("Precise location  OFF"));
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
                if (candidate != null && candidate.getLatitude() != 0.0 && candidate.getLongitude() != 0.0
                        && (best == null || candidate.getTime() > best.getTime())) {
                    best = candidate;
                }
            }

            if (best != null) {
                setReportLocationState(reportType, true, best.getLatitude(), best.getLongitude());
                if (reportType != null && reportType.equalsIgnoreCase(currentReportType)) {
                    currentLat = best.getLatitude();
                    currentLng = best.getLongitude();
                    hasLocation = true;
                    activeLocationReportType = reportType;
                    applyLocationToggleVisualState(button, toggle, true);
                }
            } else if (toggle != null) {
                toggle.setText(translate("Locating..."));
                toggle.setTextColor(secondaryTextColor());
            }

            if (activeLocationListener != null) {
                try {
                    manager.removeUpdates(activeLocationListener);
                } catch (Exception ignored) {}
            }

            activeLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (requestGeneration != locationRequestGeneration) {
                        try {
                            manager.removeUpdates(this);
                        } catch (Exception ignored) {}
                        return;
                    }
                    if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                        setReportLocationState(reportType, true, location.getLatitude(), location.getLongitude());
                        if (reportType != null && reportType.equalsIgnoreCase(currentReportType)) {
                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();
                            hasLocation = true;
                            activeLocationReportType = reportType;
                            runOnUiThread(() -> applyLocationToggleVisualState(button, toggle, true));
                        }
                        try {
                            manager.removeUpdates(this);
                        } catch (Exception ignored) {}
                    }
                }

                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
            };

            Looper looper = Looper.getMainLooper();
            if (gpsEnabled) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, activeLocationListener, looper);
            }
            if (networkEnabled) {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1f, activeLocationListener, looper);
            }
        } catch (SecurityException error) {
            if (button != null) button.setText(translate("Location permission required"));
        } catch (Exception ignored) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 702 && locationStatus != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationServicesIfNeeded(locationStatus, locationToggleStatus,
                        locationRequestReportType, locationRequestGeneration);
            } else {
                locationStatus.setText(translate("Location permission denied"));
            }
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingImageSlot >= 0) {
            openCameraForSlot(pendingImageSlot);
        }
        if (requestCode == REQUEST_PROFILE_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openProfilePhotoCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SMS_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                String smsMessage = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                fillOtpFromSms(smsMessage);
            }
            return;
        }
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
            Uri imageUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            String savedPath = saveProfileFileToInternalStorage(imageUri);
            if (savedPath != null) {
                SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
                account.edit()
                    .putString("profile_image_uri", savedPath)
                    .remove("profile_image_cache_url")
                    .apply();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                        String uploadedUrl = uploadImage(imageUri, null, token.getToken());
                        if (uploadedUrl == null || uploadedUrl.isEmpty()) {
                            runOnUiThread(() -> Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_LONG).show());
                            return;
                        }
                        SharedPreferences.Editor editor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit();
                        editor.putString("profile_image_url", uploadedUrl);
                        editor.putString("profile_image_cache_url", uploadedUrl);
                        editor.apply();

                        String username = account.getString("username", "");
                        String fullName = account.getString("full_name", "");
                        String email = account.getString("email", "");
                        String mobile = account.getString("mobile", "");
                        String state = account.getString("state", "");
                        String city = account.getString("city", "");
                        saveCloudProfileDocument(user, username, fullName, email, mobile, state, city, uploadedUrl, null);
                        runOnUiThread(() -> {
                            if (currentPage == PAGE_PROFILE) showProfile();
                            Toast.makeText(this, translate("Profile picture updated"), Toast.LENGTH_SHORT).show();
                        });
                    })).addOnFailureListener(error -> {
                        Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_LONG).show();
                    });
                }
            } else {
                Toast.makeText(this, "Could not load selected image", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_PROFILE_CAMERA && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if (pendingProfileCameraUri != null) {
                bitmap = decodeProfileCameraBitmap(pendingProfileCameraUri, 1600);
            }
            if (bitmap == null && data != null && data.getExtras() != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            if (bitmap != null) {
                final Bitmap captureBitmap = bitmap;
                capturedProfileImage = captureBitmap;
                selectedProfileImage = null;
                profileImageExplicitlyRemoved = false;
                String savedPath = saveProfileBitmapToInternalStorage(captureBitmap);
                if (savedPath != null) {
                    SharedPreferences account = getSharedPreferences("fendly_account", MODE_PRIVATE);
                    account.edit()
                        .putString("profile_image_uri", savedPath)
                        .remove("profile_image_cache_url")
                        .apply();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                            String uploadedUrl = uploadImage(null, captureBitmap, token.getToken());
                            if (uploadedUrl == null || uploadedUrl.isEmpty()) {
                                runOnUiThread(() -> Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_LONG).show());
                                return;
                            }
                            SharedPreferences.Editor editor = getSharedPreferences("fendly_account", MODE_PRIVATE).edit();
                            editor.putString("profile_image_url", uploadedUrl);
                            editor.putString("profile_image_cache_url", uploadedUrl);
                            editor.apply();

                            String username = account.getString("username", "");
                            String fullName = account.getString("full_name", "");
                            String email = account.getString("email", "");
                            String mobile = account.getString("mobile", "");
                            String state = account.getString("state", "");
                            String city = account.getString("city", "");
                            saveCloudProfileDocument(user, username, fullName, email, mobile, state, city, uploadedUrl, null);
                            runOnUiThread(() -> {
                                if (currentPage == PAGE_PROFILE) showProfile();
                                Toast.makeText(this, translate("Profile picture updated"), Toast.LENGTH_SHORT).show();
                            });
                        })).addOnFailureListener(error -> {
                            Toast.makeText(this, "Profile picture upload failed", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            } else {
                Toast.makeText(this, "Could not capture a clear profile photo", Toast.LENGTH_SHORT).show();
            }
            pendingProfileCameraUri = null;
        }

    }

    private void fillOtpFromSms(String smsMessage) {
        if (smsMessage == null || activeSmsOtpCells == null || activeSmsOtpCells.length != 6) return;
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("(?<!\\d)\\d{6}(?!\\d)").matcher(smsMessage);
        if (!matcher.find()) {
            Log.w("AUTH", "SMS User Consent message did not contain a standalone six-digit OTP");
            return;
        }
        String otp = matcher.group();
        for (int index = 0; index < activeSmsOtpCells.length; index++) {
            activeSmsOtpCells[index].setText(String.valueOf(otp.charAt(index)));
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
        adminSocialPageOpen = false;
        adminContentReportsPageOpen = false;
        adminEnglishUi = true;
        screenRenderer = this::showAdminDashboard;
        LinearLayout root = screenBase("");
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView adminTitle = text("Admin workspace", 20, primaryTextColor(), Typeface.NORMAL);
        adminTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        adminTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        root.addView(adminTitle, contentParams(-1, dp(28), dp(4)));

        TextView socialPublishingButton = actionButton("Manage social accounts and posts", false);
        socialPublishingButton.setOnClickListener(view -> showAdminSocialPublishingPage());
        root.addView(socialPublishingButton, contentParams(-1, dp(44), dp(10)));

        TextView contentReportsButton = actionButton("Review user content reports", false);
        contentReportsButton.setOnClickListener(view -> showAdminContentReportsPage());
        root.addView(contentReportsButton, contentParams(-1, dp(44), dp(8)));

        LinearLayout overview = new LinearLayout(this);
        overview.setOrientation(LinearLayout.HORIZONTAL);
        TextView lostCount = adminSummary("LOST", LOST_GREEN);
        TextView foundCount = adminSummary("FOUND", FOUND_GOLD);
        overview.addView(lostCount, new LinearLayout.LayoutParams(0, dp(62), 1f));
        LinearLayout.LayoutParams foundCountParams = new LinearLayout.LayoutParams(0, dp(62), 1f);
        foundCountParams.setMargins(dp(8), 0, 0, 0);
        overview.addView(foundCount, foundCountParams);
        root.addView(overview, contentParams(-1, dp(62), dp(16)));

        root.addView(text("Find users or reports", 13, primaryTextColor(), Typeface.NORMAL), contentParams(-1, dp(20), dp(6)));
        EditText search = field("Name, item, description, mobile, username, or email");
        root.addView(search, contentParams(-1, dp(52), dp(8)));
        TextView searchButton = actionButton("Search", true);
        root.addView(searchButton, contentParams(-1, dp(44), dp(12)));

        String reportsHeadingText = adminReportFilter.isEmpty() ? "Live reports" : adminReportFilter + " reports";
        TextView reportsHeading = text(reportsHeadingText, 15, primaryTextColor(), Typeface.NORMAL);
        root.addView(reportsHeading, contentParams(-1, dp(22), dp(8)));
        root.addView(text("Browse by category", 12, secondaryTextColor(), Typeface.BOLD),
                contentParams(-1, dp(20), dp(6)));
        LinearLayout reportCategoryTabs = new LinearLayout(this);
        reportCategoryTabs.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(reportCategoryTabs, contentParams(-1, dp(112), dp(10)));
        LinearLayout reports = new LinearLayout(this);
        reports.setOrientation(LinearLayout.VERTICAL);
        root.addView(reports, contentParams(-1, -2, 0));
        String[] liveReportsResponse = {null};
        boolean[] liveReportsLoaded = {false};

        TextView matchesHeading = text("Match review", 15, primaryTextColor(), Typeface.NORMAL);
        matchesHeading.setVisibility(View.GONE);
        root.addView(matchesHeading, contentParams(-1, dp(22), dp(12)));
        LinearLayout matches = new LinearLayout(this);
        matches.setOrientation(LinearLayout.VERTICAL);
        root.addView(matches, contentParams(-1, -2, 0));

        Runnable restoreLiveReports = () -> {
            if (liveReportsLoaded[0]) {
                renderAdminReports(liveReportsResponse[0], reports, matchesHeading, matches, lostCount, foundCount);
            }
        };
        populateAdminReportCategoryTabs(reportCategoryTabs, search, restoreLiveReports);
        updateAdminReportFilterStyles(lostCount, foundCount);
        lostCount.setOnClickListener(view -> selectAdminReportFilter(
            "LOST", lostCount, foundCount, search, reportsHeading, restoreLiveReports));
        foundCount.setOnClickListener(view -> selectAdminReportFilter(
            "FOUND", lostCount, foundCount, search, reportsHeading, restoreLiveReports));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable value) {
                if (value.toString().trim().isEmpty()) {
                    searchButton.setText(translate("Search"));
                    searchButton.setEnabled(true);
                    reportsHeading.setText(adminReportFilter.isEmpty()
                            ? translate("Live reports")
                            : String.format(Locale.ROOT, "%s reports", adminReportFilter));
                    restoreLiveReports.run();
                }
            }
        });
        searchButton.setOnClickListener(view -> {
            String query = search.getText().toString().trim();
            if (query.isEmpty()) {
                restoreLiveReports.run();
                return;
            }
            reportsHeading.setText(translate("Search results"));
            searchAdminUsers(query, reports, searchButton, search, matchesHeading, matches);
        });
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser != null) {
            adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
                String response = fetchAdminItems(token.getToken());
                runOnUiThread(() -> {
                    liveReportsResponse[0] = response;
                    liveReportsLoaded[0] = true;
                    if (search.getText().toString().trim().isEmpty()) {
                        renderAdminReports(response, reports, matchesHeading, matches, lostCount, foundCount);
                    }
                });
            }));
        }
        root.post(() -> {
            if (currentPage == PAGE_ADMIN) fetchPendingNotificationCount();
        });
    }

    private void showAdminContentReportsPage() {
        currentPage = PAGE_ADMIN;
        adminSocialPageOpen = false;
        adminContentReportsPageOpen = true;
        adminEnglishUi = true;
        screenRenderer = this::showAdminContentReportsPage;
        LinearLayout root = screenBase("");
        TextView back = actionButton("Back to admin workspace", false);
        back.setOnClickListener(view -> showAdminDashboard());
        addField(root, back);
        addHeading("User content reports", "Review reports from the Fendly community");
        TextView loading = text("Loading reports...", 13, secondaryTextColor(), Typeface.NORMAL);
        addField(root, loading);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            loading.setText(translate("Sign in as an administrator to review content reports."));
            return;
        }
        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = getAuthorized("/api/admin/content-reports", token.getToken());
            runOnUiThread(() -> {
                if (currentPage != PAGE_ADMIN || !adminContentReportsPageOpen) {
                    return;
                }
                root.removeView(loading);
                if (response == null) {
                    addField(root, text(
                            "Could not load reports. Check admin access and try again.",
                            13,
                            secondaryTextColor(),
                            Typeface.NORMAL
                    ));
                    return;
                }
                try {
                    JSONArray reports = new JSONArray(response);
                    if (reports.length() == 0) {
                        addField(root, text("No pending content reports.", 13, secondaryTextColor(), Typeface.NORMAL));
                        return;
                    }
                    for (int index = 0; index < reports.length(); index++) {
                        JSONObject report = reports.getJSONObject(index);
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setPadding(dp(12), dp(10), dp(12), dp(10));
                        card.setBackground(roundWithStroke(surfaceColor(), 12, borderColor()));
                        card.addView(text(
                                report.optString("report_type", "REPORT") + " · " +
                                        report.optString("title", "Untitled"),
                                14,
                                primaryTextColor(),
                                Typeface.BOLD
                        ));
                        card.addView(text(
                                "Reason: " + report.optString("reason", "other") +
                                        (report.optString("details", "").isEmpty()
                                                ? ""
                                                : "\nDetails: " + report.optString("details")),
                                12,
                                secondaryTextColor(),
                                Typeface.NORMAL
                        ));
                        card.addView(text(
                                report.optString("description", ""),
                                12,
                                primaryTextColor(),
                                Typeface.NORMAL
                        ));
                        LinearLayout actions = new LinearLayout(this);
                        actions.setOrientation(LinearLayout.HORIZONTAL);
                        addContentReportDecisionButton(actions, "Dismiss", "dismiss", report, token.getToken());
                        addContentReportDecisionButton(actions, "Reviewed", "reviewed", report, token.getToken());
                        addContentReportDecisionButton(actions, "Hide", "hide", report, token.getToken());
                        card.addView(actions);
                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
                        cardParams.setMargins(0, 0, 0, dp(8));
                        root.addView(card, cardParams);
                    }
                } catch (Exception error) {
                    Log.e("CONTENT_REPORTS", "Could not parse administrator content reports", error);
                    addField(root, text(
                            "The content report response could not be read.",
                            13,
                            secondaryTextColor(),
                            Typeface.NORMAL
                    ));
                }
            });
        })).addOnFailureListener(error -> runOnUiThread(() ->
                loading.setText(translate("Could not authenticate administrator access."))));
    }

    private void addContentReportDecisionButton(
            LinearLayout actions,
            String label,
            String decision,
            JSONObject report,
            String idToken
    ) {
        TextView button = actionButton(label, "hide".equals(decision));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(40), 1f);
        if (actions.getChildCount() > 0) params.setMargins(dp(5), 0, 0, 0);
        actions.addView(button, params);
        button.setOnClickListener(view -> {
            button.setEnabled(false);
            String reportId = Uri.encode(report.optString("id", ""));
            String body = "{\"decision\":\"" + decision + "\"}";
            network.execute(() -> {
                boolean saved = postAuthorized(
                        "/api/admin/content-reports/" + reportId + "/review",
                        idToken,
                        body
                );
                runOnUiThread(() -> {
                    if (saved) {
                        Toast.makeText(this, "Content report reviewed.", Toast.LENGTH_SHORT).show();
                        showAdminContentReportsPage();
                    } else {
                        button.setEnabled(true);
                        Toast.makeText(this, "Could not save the review decision.", Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    private void showAdminSocialPublishingPage() {
        currentPage = PAGE_ADMIN;
        adminSocialPageOpen = true;
        adminContentReportsPageOpen = false;
        adminEnglishUi = true;
        screenRenderer = this::showAdminSocialPublishingPage;
        LinearLayout root = screenBase("");

        TextView backButton = actionButton("Back to admin workspace", false);
        backButton.setOnClickListener(view -> showAdminDashboard());
        root.addView(backButton, contentParams(-1, dp(42), dp(10)));

        TextView title = text("Social publishing", 20, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        root.addView(title, contentParams(-1, dp(30), dp(6)));
        TextView note = text(
                "Connect Fendly brand accounts here. Reporters still opt in separately for each report.",
                12, secondaryTextColor(), Typeface.NORMAL);
        note.setGravity(Gravity.CENTER);
        root.addView(note, contentParams(-1, -2, dp(12)));

        TextView message = text("", 12, secondaryTextColor(), Typeface.NORMAL);
        root.addView(message, contentParams(-1, -2, dp(8)));
        LinearLayout channels = new LinearLayout(this);
        channels.setOrientation(LinearLayout.VERTICAL);
        root.addView(channels, contentParams(-1, -2, dp(16)));

        TextView historyTitle = text("Recent publication jobs", 15, primaryTextColor(), Typeface.NORMAL);
        root.addView(historyTitle, contentParams(-1, dp(24), dp(8)));
        LinearLayout history = new LinearLayout(this);
        history.setOrientation(LinearLayout.VERTICAL);
        root.addView(history, contentParams(-1, -2, dp(12)));

        TextView refreshButton = actionButton("Refresh status", true);
        refreshButton.setOnClickListener(view ->
                loadAdminSocialPublishing(channels, history, message, true));
        root.addView(refreshButton, contentParams(-1, dp(44), dp(8)));
        loadAdminSocialPublishing(channels, history, message, true);
    }

    private void loadAdminSocialPublishing(
            LinearLayout channels,
            LinearLayout history,
            TextView message,
            boolean refreshPlatformStatus
    ) {
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) {
            message.setText(translate("Sign in to manage Fendly's social accounts."));
            return;
        }
        message.setText(translate("Loading social account status…"));
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            AuthorizedResponse statusResponse = getAuthorizedResponse(
                    "/api/social/status", token.getToken());
            AuthorizedResponse historyResponse = refreshPlatformStatus
                    ? postAuthorizedResponse("/api/social/publications/refresh?limit=20", token.getToken())
                    : getAuthorizedResponse("/api/social/publications?limit=20", token.getToken());
            runOnUiThread(() -> {
                if (!adminSocialPageOpen || currentPage != PAGE_ADMIN) return;
                if (statusResponse.statusCode != 200) {
                    message.setText(socialPublishingRequestError(statusResponse));
                    return;
                }
                channels.removeAllViews();
                history.removeAllViews();
                try {
                    JSONObject status = new JSONObject(statusResponse.body);
                    boolean facebookConnected = socialProviderConnected(status, "facebook");
                    boolean instagramConnected = socialProviderConnected(status, "instagram");
                    addSocialMetaProviderRow(
                            channels,
                            facebookConnected,
                            socialProviderName(status, "facebook"),
                            instagramConnected,
                            socialProviderName(status, "instagram")
                    );
                    message.setText(translate(
                            "Generated posters can be sent to Facebook/Instagram. "
                                    + "Failed or interrupted jobs are not automatically retried."
                    ));
                } catch (Exception error) {
                    Log.e("SOCIAL_PUBLISHING", "Could not parse social account status", error);
                    message.setText(translate("The server returned invalid social account status."));
                    return;
                }

                if (historyResponse.statusCode != 200) {
                    addSocialHistoryEntry(history, socialPublishingRequestError(historyResponse));
                    return;
                }
                try {
                    JSONArray publications = new JSONArray(historyResponse.body);
                    Map<String, ArrayList<JSONObject>> groupedPublications = new LinkedHashMap<>();
                    for (int index = 0; index < publications.length(); index++) {
                        JSONObject item = publications.getJSONObject(index);
                        String provider = item.optString("provider", "")
                                .toLowerCase(Locale.ROOT);
                        if (!"facebook".equals(provider) && !"instagram".equals(provider)) {
                            continue;
                        }
                        String reportId = item.optString("report_id", "").trim();
                        String groupKey = reportId.isEmpty() ? "unavailable-" + index : reportId;
                        if (!groupedPublications.containsKey(groupKey)) {
                            groupedPublications.put(groupKey, new ArrayList<>());
                        }
                        groupedPublications.get(groupKey).add(item);
                    }
                    if (groupedPublications.isEmpty()) {
                        addSocialHistoryEntry(history, "No Facebook or Instagram publication jobs yet.");
                    } else {
                        final LinearLayout[] expandedPanel = {null};
                        for (Map.Entry<String, ArrayList<JSONObject>> entry
                                : groupedPublications.entrySet()) {
                            addSocialPublicationGroup(
                                    history,
                                    entry.getValue(),
                                    expandedPanel
                            );
                        }
                    }
                } catch (Exception error) {
                    Log.e("SOCIAL_PUBLISHING", "Could not parse publication history", error);
                    addSocialHistoryEntry(history, "The server returned invalid publication history.");
                }
            });
        })).addOnFailureListener(error -> runOnUiThread(() -> {
            Log.e("SOCIAL_PUBLISHING", "Could not retrieve Firebase admin token", error);
            message.setText(translate("Could not verify your Fendly sign-in. Please try again."));
        }));
    }

    private void addSocialPublicationGroup(
            LinearLayout parent,
            List<JSONObject> publications,
            LinearLayout[] expandedPanel
    ) {
        JSONObject firstPublication = publications.get(0);
        String reportId = firstPublication.optString("report_id", "unavailable");
        String reportType = firstPublication.optString("report_type", "report")
                .toUpperCase(Locale.ROOT);
        String reportTitle = firstPublication.optString("report_title", "").trim();
        String reportCategory = firstPublication.optString("report_category", "").trim();
        String subject = socialPublicationSubject(reportCategory, reportTitle);
        String panelTitle = "found".equalsIgnoreCase(reportType)
                ? "Found " + lowercaseSocialSubject(subject) + " report"
                : subject + " lost report";

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(roundWithStroke(surfaceColor(), 12, borderColor()));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(8);
        parent.addView(card, cardParams);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(header, new LinearLayout.LayoutParams(-1, -2));
        TextView title = text(
                panelTitle,
                13,
                primaryTextColor(),
                Typeface.BOLD
        );
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView expandIcon = text("＋", 18, secondaryTextColor(), Typeface.NORMAL);
        expandIcon.setGravity(Gravity.CENTER);
        header.addView(expandIcon, new LinearLayout.LayoutParams(dp(28), dp(28)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setVisibility(View.GONE);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(-1, -2);
        detailsParams.topMargin = dp(8);
        card.addView(details, detailsParams);

        TextView reportIdRow = text(
                "Report ID: " + reportId,
                11,
                secondaryTextColor(),
                Typeface.NORMAL
        );
        details.addView(reportIdRow, new LinearLayout.LayoutParams(-1, -2));

        String posterUrl = "";
        String reportImageUrl = "";
        for (JSONObject publication : publications) {
            String provider = publication.optString("provider", "").toLowerCase(Locale.ROOT);
            String platformStatus = publication.optString("platform_status", "not_checked");
            String jobStatus = publication.optString("status", "unknown");
            if ("available".equals(platformStatus)) {
                jobStatus += " (present in platform feed)";
            } else if ("unavailable".equals(platformStatus)) {
                jobStatus = "not found (deleted or inaccessible)";
            } else if ("check_failed".equals(platformStatus)) {
                jobStatus = "platform status unknown";
            }
            String rowText = provider + " · " + jobStatus;
            String error = publication.optString("last_error", "");
            if (!error.isEmpty()) rowText += "\n" + error;
            TextView providerRow = text(rowText, 12, secondaryTextColor(), Typeface.NORMAL);
            providerRow.setPadding(dp(8), dp(7), dp(8), dp(7));
            providerRow.setBackground(roundWithStroke(backgroundColor(), 8, borderColor()));
            LinearLayout.LayoutParams providerParams = new LinearLayout.LayoutParams(-1, -2);
            providerParams.topMargin = dp(6);
            details.addView(providerRow, providerParams);
            if (posterUrl.isEmpty()) {
                posterUrl = publication.optString("poster_url", "").trim();
            }
            if (posterUrl.isEmpty()) {
                posterUrl = publication.optString("published_image_url", "").trim();
            }
            if (reportImageUrl.isEmpty()) {
                reportImageUrl = publication.optString("report_image_url", "").trim();
            }
        }

        final boolean isPublishedPosterAvailable = !posterUrl.isEmpty();
        final String previewUrl = isPublishedPosterAvailable ? posterUrl : reportImageUrl;
        if (!previewUrl.isEmpty()) {
            TextView previewLabel = text(
                    isPublishedPosterAvailable
                            ? "Published post image · tap to enlarge"
                            : "Report photo preview · tap to enlarge",
                    11,
                    secondaryTextColor(),
                    Typeface.NORMAL
            );
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
            labelParams.topMargin = dp(10);
            details.addView(previewLabel, labelParams);

            ImageView thumbnail = new ImageView(this);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setContentDescription(isPublishedPosterAvailable
                    ? "Preview the published social post image"
                    : "Preview the report photo");
            thumbnail.setBackground(roundWithStroke(backgroundColor(), 8, borderColor()));
            thumbnail.setClipToOutline(true);
            LinearLayout.LayoutParams thumbnailParams =
                    new LinearLayout.LayoutParams(dp(112), dp(144));
            thumbnailParams.topMargin = dp(6);
            details.addView(thumbnail, thumbnailParams);
            Glide.with(this)
                    .load(previewUrl)
                    .placeholder(new ColorDrawable(Color.argb(100, 190, 190, 190)))
                    .error(new ColorDrawable(Color.argb(100, 190, 190, 190)))
                    .into(thumbnail);
            thumbnail.setOnClickListener(view -> showSocialPosterPreview(previewUrl));
        }

        View.OnClickListener togglePanel = view -> {
            if (expandedPanel[0] == details) {
                details.setVisibility(View.GONE);
                expandIcon.setText("＋");
                expandedPanel[0] = null;
                return;
            }
            if (expandedPanel[0] != null) {
                expandedPanel[0].setVisibility(View.GONE);
                View oldCard = (View) expandedPanel[0].getParent();
                View oldHeader = oldCard instanceof LinearLayout
                        ? ((LinearLayout) oldCard).getChildAt(0) : null;
                if (oldHeader instanceof LinearLayout) {
                    View oldIcon = ((LinearLayout) oldHeader).getChildAt(1);
                    if (oldIcon instanceof TextView) ((TextView) oldIcon).setText("＋");
                }
            }
            details.setVisibility(View.VISIBLE);
            expandIcon.setText("−");
            expandedPanel[0] = details;
        };
        header.setOnClickListener(togglePanel);
    }

    private String socialPublicationSubject(String category, String title) {
        String normalizedTitle = title.toLowerCase(Locale.ROOT);
        String[][] namedSubjects = {
                {"cat", "cat"}, {"kitten", "cat"}, {"dog", "dog"}, {"puppy", "dog"},
                {"cow", "cow"}, {"goat", "goat"}, {"horse", "horse"},
                {"bird", "bird"}, {"parrot", "parrot"}, {"rabbit", "rabbit"},
                {"fish", "fish"}, {"people", "People"}, {"person", "People"},
                {"woman", "People"}, {"man", "People"}, {"child", "People"},
                {"phone", "phone"}, {"wallet", "wallet"}, {"bag", "bag"},
                {"bicycle", "bicycle"}, {"cycle", "bicycle"}, {"car", "car"},
                {"laptop", "laptop"}
        };
        for (String[] subject : namedSubjects) {
            if (containsSocialSubject(normalizedTitle, subject[0])) {
                return "People".equals(subject[1])
                        ? subject[1] : capitalizeSocialSubject(subject[1]);
            }
        }
        String normalizedCategory = category.trim().toLowerCase(Locale.ROOT);
        if (normalizedCategory.contains("people") || normalizedCategory.contains("person")) {
            return "People";
        }
        if (normalizedCategory.contains("animal")) return "Animal";
        if (!normalizedCategory.isEmpty() && !"other".equals(normalizedCategory)) {
            String categorySubject = normalizedCategory.replaceAll("\\s+", " ").trim();
            if (categorySubject.contains("accessories")) return "Item";
            return capitalizeSocialSubject(categorySubject);
        }
        String cleanTitle = title.trim()
                .replaceAll("(?i)\\b(lost|found|missing|report|item)\\b", " ")
                .replaceAll("[^\\p{L}\\p{N} ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleanTitle.isEmpty()) return "Item";
        String[] words = cleanTitle.split(" ");
        return capitalizeSocialSubject(words[0]);
    }

    private boolean containsSocialSubject(String value, String subject) {
        return value.matches("(?s).*\\b" + java.util.regex.Pattern.quote(subject) + "\\b.*");
    }

    private String capitalizeSocialSubject(String subject) {
        if (subject.isEmpty()) return subject;
        return subject.substring(0, 1).toUpperCase(Locale.ROOT) + subject.substring(1);
    }

    private String lowercaseSocialSubject(String subject) {
        if (subject.isEmpty()) return subject;
        if ("People".equals(subject)) return "people";
        return subject.substring(0, 1).toLowerCase(Locale.ROOT) + subject.substring(1);
    }

    private void showSocialPosterPreview(String posterUrl) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(true);

        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(Color.BLACK);
        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        container.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        Glide.with(this)
                .load(posterUrl)
                .placeholder(new ColorDrawable(Color.BLACK))
                .error(new ColorDrawable(Color.BLACK))
                .into(preview);
        preview.setOnClickListener(view -> dialog.dismiss());

        ImageView close = new ImageView(this);
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setColorFilter(Color.WHITE);
        close.setBackground(roundWithStroke(Color.argb(180, 0, 0, 0), 18, Color.WHITE));
        close.setPadding(dp(4), dp(4), dp(4), dp(4));
        FrameLayout.LayoutParams closeParams =
                new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.TOP | Gravity.END);
        closeParams.setMargins(dp(12), dp(12), dp(12), 0);
        close.setOnClickListener(view -> dialog.dismiss());
        container.addView(close, closeParams);

        dialog.setContentView(container);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -1);
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private boolean socialProviderConnected(JSONObject status, String provider) {
        return status.optJSONObject(provider) != null
                && status.optJSONObject(provider).optBoolean("connected", false);
    }

    private String socialProviderName(JSONObject status, String provider) {
        JSONObject account = status.optJSONObject(provider);
        return account == null ? "" : account.optString("account_name", "");
    }

    private void addSocialMetaProviderRow(
            LinearLayout parent,
            boolean facebookConnected,
            String facebookName,
            boolean instagramConnected,
            String instagramName
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(roundWithStroke(surfaceColor(), 16, borderColor()));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(header, new LinearLayout.LayoutParams(-1, -2));
        TextView title = text("Meta accounts", 15, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setIncludeFontPadding(false);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView expandIcon = text("＋", 18, secondaryTextColor(), Typeface.NORMAL);
        expandIcon.setGravity(Gravity.CENTER);
        header.addView(expandIcon, new LinearLayout.LayoutParams(dp(28), dp(28)));

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setVisibility(View.GONE);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(-1, -2);
        detailsParams.topMargin = dp(4);
        card.addView(details, detailsParams);

        LinearLayout accounts = new LinearLayout(this);
        accounts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams accountsParams = new LinearLayout.LayoutParams(-1, -2);
        accountsParams.topMargin = dp(12);
        details.addView(accounts, accountsParams);

        addSocialMetaAccountRow(
                accounts,
                "Facebook Page",
                facebookConnected,
                facebookConnected && !facebookName.isEmpty()
                        ? "Connected as " + facebookName : null,
                false
        );
        addSocialMetaAccountRow(
                accounts,
                "Instagram",
                instagramConnected,
                instagramConnected && !instagramName.isEmpty()
                        ? "Connected as @" + instagramName : null,
                true
        );

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        boolean compact = getResources().getConfiguration().screenWidthDp < 410
                || getResources().getConfiguration().fontScale > 1.1f;
        actions.setOrientation(compact ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        boolean anyMetaAccountConnected = facebookConnected || instagramConnected;
        if (anyMetaAccountConnected) {
            if (!facebookConnected || !instagramConnected) {
                String missingAccount = facebookConnected ? "Instagram" : "Facebook";
                TextView connectMissing = actionButton("Connect " + missingAccount, true);
                connectMissing.setOnClickListener(view -> startSocialAuthorization("meta"));
                LinearLayout.LayoutParams connectParams = new LinearLayout.LayoutParams(
                        compact ? -1 : -2, dp(44)
                );
                if (compact) {
                    connectParams.bottomMargin = dp(8);
                }
                actions.addView(connectMissing, connectParams);
            }
            TextView disconnect = actionButton("Disconnect Meta", false);
            disconnect.setOnClickListener(view -> confirmSocialDisconnect("facebook"));
            LinearLayout.LayoutParams disconnectParams = new LinearLayout.LayoutParams(
                    compact ? -1 : -2, dp(44)
            );
            if (!compact && (!facebookConnected || !instagramConnected)) {
                disconnectParams.setMargins(dp(8), 0, 0, 0);
            }
            actions.addView(disconnect, disconnectParams);
        } else {
            TextView connect = actionButton("Connect Meta", true);
            connect.setOnClickListener(view -> startSocialAuthorization("meta"));
            actions.addView(connect, new LinearLayout.LayoutParams(
                    compact ? -1 : -2, dp(44)
            ));
        }

        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, -2);
        actionsParams.topMargin = dp(16);
        details.addView(actions, actionsParams);
        header.setOnClickListener(view -> {
            boolean isExpanded = details.getVisibility() == View.VISIBLE;
            details.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            expandIcon.setText(isExpanded ? "＋" : "−");
        });
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(8);
        parent.addView(card, cardParams);
    }

    private void addSocialMetaAccountRow(
            LinearLayout parent,
            String provider,
            boolean connected,
            String connectedAccount,
            boolean addTopMargin
    ) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.START);

        TextView providerLabel = text(provider, 13, primaryTextColor(), Typeface.BOLD);
        providerLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        providerLabel.setIncludeFontPadding(false);
        row.addView(providerLabel, new LinearLayout.LayoutParams(-1, -2));

        String status = connected
                ? (connectedAccount == null ? "Connected" : connectedAccount)
                : "Not connected";
        TextView accountStatus = text(status, 13, secondaryTextColor(), Typeface.NORMAL);
        accountStatus.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        accountStatus.setIncludeFontPadding(false);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(3);
        row.addView(accountStatus, statusParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
        if (addTopMargin) {
            rowParams.topMargin = dp(12);
        }
        parent.addView(row, rowParams);
    }

    private void startSocialAuthorization(String provider) {
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) {
            Toast.makeText(this, "Sign in to connect a social account.", Toast.LENGTH_LONG).show();
            return;
        }
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            AuthorizedResponse response = socialAuthorizedRequest(
                    "POST", "/api/social/connect/" + provider, token.getToken());
            runOnUiThread(() -> {
                if (response.statusCode < 200 || response.statusCode >= 300) {
                    Toast.makeText(this, socialPublishingRequestError(response), Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    String authorizationUrl = new JSONObject(response.body).optString("authorization_url", "");
                    Uri uri = Uri.parse(authorizationUrl);
                    String host = uri.getHost();
                    boolean permittedHost = "meta".equals(provider)
                            ? "facebook.com".equalsIgnoreCase(host)
                                    || (host != null && host.endsWith(".facebook.com"))
                            : "x.com".equalsIgnoreCase(host)
                                    || (host != null && host.endsWith(".x.com"));
                    if (!"https".equalsIgnoreCase(uri.getScheme()) || !permittedHost) {
                        throw new IllegalArgumentException("Unexpected authorization URL");
                    }
                    socialAuthorizationPending = true;
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception error) {
                    Log.e("SOCIAL_PUBLISHING", "Could not open provider authorization", error);
                    Toast.makeText(
                            this,
                            "The server returned an invalid authorization link.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        })).addOnFailureListener(error -> {
            Log.e("SOCIAL_PUBLISHING", "Could not retrieve Firebase admin token", error);
            runOnUiThread(() -> Toast.makeText(
                    this, "Could not verify your Fendly sign-in.", Toast.LENGTH_LONG).show());
        });
    }

    private void confirmSocialDisconnect(String provider) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(
                R.drawable.ic_field_lock,
                "Disconnect social account?",
                "Fendly will stop using the stored authorization. Revoke Fendly in the provider settings to remove platform access too."
        );
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        TextView cancel = filledButton("Cancel", LOST_GREEN, LOST_GREEN_ON);
        cancel.setOnClickListener(view -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(44), 1f));

        TextView disconnect = filledButton("Disconnect", Color.rgb(180, 45, 45), Color.WHITE);
        LinearLayout.LayoutParams disconnectParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        disconnectParams.setMargins(dp(12), 0, 0, 0);
        actions.addView(disconnect, disconnectParams);
        disconnect.setOnClickListener(view -> {
            dialog.dismiss();
            disconnectSocialAccount(provider);
        });

        content.addView(actions, new LinearLayout.LayoutParams(-1, dp(44)));
        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void disconnectSocialAccount(String provider) {
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) return;
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            AuthorizedResponse response = socialAuthorizedRequest(
                    "DELETE", "/api/social/account/" + provider, token.getToken());
            runOnUiThread(() -> {
                if (response.statusCode < 200 || response.statusCode >= 300) {
                    Toast.makeText(this, socialPublishingRequestError(response), Toast.LENGTH_LONG).show();
                    return;
                }
                showAdminSocialPublishingPage();
            });
        })).addOnFailureListener(error -> {
            Log.e("SOCIAL_PUBLISHING", "Could not retrieve Firebase admin token", error);
            runOnUiThread(() -> Toast.makeText(
                    this, "Could not verify your Fendly sign-in.", Toast.LENGTH_LONG).show());
        });
    }

    private AuthorizedResponse socialAuthorizedRequest(String method, String path, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = stream == null ? "" : readStream(stream);
            return new AuthorizedResponse(statusCode, body);
        } catch (Exception error) {
            Log.e("SOCIAL_PUBLISHING", "Social account request failed", error);
            return new AuthorizedResponse(-1, "");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String socialPublishingRequestError(AuthorizedResponse response) {
        if (response.statusCode < 0) {
            return "Could not reach the Fendly server. Check your connection and try again.";
        }
        try {
            String detail = new JSONObject(response.body).optString("detail", "").trim();
            if (!detail.isEmpty()) return detail;
        } catch (Exception ignored) {
            Log.w("SOCIAL_PUBLISHING", "Social endpoint returned a non-JSON error response");
        }
        return "Social account request failed (HTTP " + response.statusCode + ").";
    }

    private void addSocialHistoryEntry(LinearLayout parent, String value) {
        TextView row = text(value, 12, secondaryTextColor(), Typeface.NORMAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackground(roundWithStroke(surfaceColor(), 10, borderColor()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(6);
        parent.addView(row, params);
    }

    private void populateAdminReportCategoryTabs(
            LinearLayout tabs,
            EditText search,
            Runnable restoreLiveReports
    ) {
        populateReportCategoryTabs(tabs, adminReportCategory, category -> {
            adminReportCategory = category;
            populateAdminReportCategoryTabs(tabs, search, restoreLiveReports);
            if (!search.getText().toString().trim().isEmpty()) {
                search.setText("");
            } else {
                restoreLiveReports.run();
            }
        });
    }

    private TextView adminSummary(String label, int accent) {
        TextView summary = text(label + "  0", 12, accent, Typeface.NORMAL);
        summary.setGravity(Gravity.CENTER);
        summary.setPadding(dp(14), 0, dp(14), 0);
        summary.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        return summary;
    }

    private void updateAdminReportFilterStyles(TextView lostCount, TextView foundCount) {
        lostCount.setBackground(roundWithStroke(surfaceColor(), 14,
                "LOST".equals(adminReportFilter) ? LOST_GREEN : borderColor()));
        foundCount.setBackground(roundWithStroke(surfaceColor(), 14,
                "FOUND".equals(adminReportFilter) ? FOUND_GOLD : borderColor()));
    }

    private void selectAdminReportFilter(String filter, TextView lostCount, TextView foundCount,
                                         EditText search, TextView reportsHeading, Runnable restoreLiveReports) {
        adminReportFilter = filter.equals(adminReportFilter) ? "" : filter;
        updateAdminReportFilterStyles(lostCount, foundCount);
        boolean hadSearch = !search.getText().toString().trim().isEmpty();
        if (hadSearch) {
            search.setText("");
        } else {
            restoreLiveReports.run();
        }
        reportsHeading.setText(adminReportFilter.isEmpty() ? "Live reports" : adminReportFilter + " reports");
    }

    private boolean isDummyAlert(JSONObject alert) {
        if (alert == null) return true;
        String foundTitle = alert.optString("found_title", "").toLowerCase(Locale.US);
        String lostTitle = alert.optString("lost_title", "").toLowerCase(Locale.US);
        return foundTitle.contains("dummy") || foundTitle.contains("test") || foundTitle.contains("sample")
                || lostTitle.contains("dummy") || lostTitle.contains("test") || lostTitle.contains("sample");
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
            int filteredLost = 0;
            int filteredFound = 0;
            LinearLayout lostSection = adminReportSection("LOST REPORTS", LOST_GREEN);
            LinearLayout foundSection = adminReportSection("FOUND REPORTS", FOUND_GOLD);
            for (int index = 0; index < items.length(); index++) {
                JSONObject item = items.getJSONObject(index);
                if ("FOUND".equalsIgnoreCase(item.optString("type"))) {
                    found++;
                    if (discoveryReportMatchesCategory(item, adminReportCategory)) {
                        filteredFound++;
                        foundSection.addView(adminReportCard(item, true, matchesHeading, matches));
                    }
                } else {
                    lost++;
                    if (discoveryReportMatchesCategory(item, adminReportCategory)) {
                        filteredLost++;
                        lostSection.addView(adminReportCard(item, false, matchesHeading, matches));
                    }
                }
            }
            lostCount.setText(String.format(
                    Locale.ROOT,
                    "%1$s  %2$s",
                    translate("LOST"),
                    localizeDigits(String.valueOf(lost))
            ));
            foundCount.setText(String.format(
                    Locale.ROOT,
                    "%1$s  %2$s",
                    translate("FOUND"),
                    localizeDigits(String.valueOf(found))
            ));
            updateAdminReportFilterStyles(lostCount, foundCount);
            if ("LOST".equals(adminReportFilter)) {
                if (filteredLost > 0) {
                    reports.addView(lostSection, contentParams(-1, -2, 0));
                } else {
                    reports.addView(text("No LOST reports in this category currently available.", 13, secondaryTextColor(), Typeface.NORMAL));
                }
            } else if ("FOUND".equals(adminReportFilter)) {
                if (filteredFound > 0) {
                    reports.addView(foundSection, contentParams(-1, -2, 0));
                } else {
                    reports.addView(text("No FOUND reports in this category currently available.", 13, secondaryTextColor(), Typeface.NORMAL));
                }
            } else if (lost == 0 && found == 0) {
                reports.addView(text("No live reports currently available.", 13, secondaryTextColor(), Typeface.NORMAL));
            } else {
                reports.addView(text("Select LOST or FOUND to view reports.", 13, secondaryTextColor(), Typeface.NORMAL));
            }
        } catch (Exception error) {
            reports.addView(text("Reports could not be read.", 13, secondaryTextColor(), Typeface.NORMAL));
        }
    }

    private LinearLayout adminReportSection(String title, int accent) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        TextView heading = text(title, 11, accent, Typeface.NORMAL);
        heading.setPadding(dp(2), 0, 0, dp(6));
        section.addView(heading, new LinearLayout.LayoutParams(-1, dp(24)));
        return section;
    }

    private LinearLayout adminReportCard(JSONObject item, boolean isFound, TextView matchesHeading, LinearLayout matches) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        TextView title = text(item.optString("title", "Untitled"), 14, primaryTextColor(), Typeface.NORMAL);
        card.addView(title, new LinearLayout.LayoutParams(-1, dp(22)));
        String details = item.optString("category", "other") + "  ·  " + item.optString("description", "No description");
        TextView detail = text(details, 11, secondaryTextColor(), Typeface.NORMAL);
        detail.setMaxLines(2);
        card.addView(detail, new LinearLayout.LayoutParams(-1, dp(38)));
        double lat = item.optDouble("lat", item.optDouble("latitude", 0.0));
        double lng = item.optDouble("lng", item.optDouble("longitude", 0.0));
        String locStr = item.optString("report_location", item.optString("location", "")).trim();
        String locDisplay;
        if (lat != 0.0 || lng != 0.0) {
            locDisplay = String.format(Locale.US, "Location: %.6f, %.6f", lat, lng);
            if (!locStr.isEmpty()) locDisplay += " (" + locStr + ")";
        } else if (!locStr.isEmpty()) {
            locDisplay = "Location: " + locStr;
        } else {
            locDisplay = "Location: 0.0000, 0.0000";
        }
        TextView location = text(locDisplay, 10, secondaryTextColor(), Typeface.NORMAL);
        card.addView(location, new LinearLayout.LayoutParams(-1, dp(20)));
        if (isFound) {
            TextView review = text("Review matches  ›", 11, GOLD_ON, Typeface.NORMAL);
            review.setGravity(Gravity.CENTER);
            review.setBackground(roundWithStroke(GOLD, 10, GOLD));
            review.setOnClickListener(view -> loadAdminMatches(item, matchesHeading, matches));
            LinearLayout.LayoutParams reviewParams = new LinearLayout.LayoutParams(-1, dp(36));
            reviewParams.setMargins(0, dp(8), 0, 0);
            card.addView(review, reviewParams);
        }
        card.setOnClickListener(view -> showReportDetailsDialog(item, true));
        return card;
    }

    private void showReportDetailsDialog(JSONObject report, boolean adminView) {
        showReportDetailsDialog(report, adminView, false);
    }

    private void showReportDetailsDialog(JSONObject report, boolean adminView, boolean allowDelete) {
        String type = report.optString("type", "ITEM");
        String title = report.optString("original_title", report.optString("title", "Untitled item")).trim();
        String reportTypeLabel = "FOUND".equalsIgnoreCase(type) ? "Found" : "Lost";
        Dialog dialog = new Dialog(this);
        LinearLayout content = themedDialogContent(
            0,
            "Report details",
            reportTypeLabel + " report · " + title);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(0, dp(8), 0, dp(8));

        List<String> imageUrls = reportImageUrls(report);
        if (!imageUrls.isEmpty()) {
            FrameLayout imageFrame = new FrameLayout(this);
            imageFrame.setBackground(roundWithStroke(backgroundColor(), 10, borderColor()));
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setImageDrawable(new ColorDrawable(Color.argb(100, 190, 190, 190)));
            imageFrame.addView(image, new FrameLayout.LayoutParams(-1, -1));
            details.addView(imageFrame, new LinearLayout.LayoutParams(-1, dp(210)));

            final int[] imageIndex = {0};
            Runnable showSelectedImage = () -> Glide.with(this)
                    .load(imageUrls.get(imageIndex[0]))
                    .placeholder(new ColorDrawable(Color.argb(100, 190, 190, 190)))
                    .error(new ColorDrawable(Color.argb(100, 190, 190, 190)))
                    .into(image);
            showSelectedImage.run();

            if (imageUrls.size() > 1) {
                TextView previous = text("‹", 30, Color.WHITE, Typeface.NORMAL);
                previous.setGravity(Gravity.CENTER);
                previous.setBackground(roundWithStroke(Color.argb(190, 0, 0, 0), 22, Color.WHITE));
                FrameLayout.LayoutParams previousParams = new FrameLayout.LayoutParams(dp(40), dp(48), Gravity.START | Gravity.CENTER_VERTICAL);
                previousParams.setMargins(dp(8), 0, 0, 0);
                imageFrame.addView(previous, previousParams);

                TextView next = text("›", 30, Color.WHITE, Typeface.NORMAL);
                next.setGravity(Gravity.CENTER);
                next.setBackground(roundWithStroke(Color.argb(190, 0, 0, 0), 22, Color.WHITE));
                FrameLayout.LayoutParams nextParams = new FrameLayout.LayoutParams(dp(40), dp(48), Gravity.END | Gravity.CENTER_VERTICAL);
                nextParams.setMargins(0, 0, dp(8), 0);
                imageFrame.addView(next, nextParams);

                TextView imageCount = text("1 / " + imageUrls.size(), 11, Color.WHITE, Typeface.NORMAL);
                imageCount.setGravity(Gravity.CENTER);
                imageCount.setBackground(roundWithStroke(Color.argb(190, 0, 0, 0), 12, Color.WHITE));
                FrameLayout.LayoutParams countParams = new FrameLayout.LayoutParams(dp(56), dp(28), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                countParams.setMargins(0, 0, 0, dp(8));
                imageFrame.addView(imageCount, countParams);
                previous.bringToFront();
                next.bringToFront();
                imageCount.bringToFront();

                Runnable updateSelectedImage = () -> {
                    if (imageUrls.isEmpty()) return;
                    showSelectedImage.run();
                    imageCount.setText(String.format(
                            Locale.ROOT,
                            "%1$s / %2$s",
                            localizeDigits(String.valueOf(imageIndex[0] + 1)),
                            localizeDigits(String.valueOf(imageUrls.size()))
                    ));
                };
                previous.setOnClickListener(view -> {
                    imageIndex[0] = (imageIndex[0] - 1 + imageUrls.size()) % imageUrls.size();
                    updateSelectedImage.run();
                });
                next.setOnClickListener(view -> {
                    imageIndex[0] = (imageIndex[0] + 1) % imageUrls.size();
                    updateSelectedImage.run();
                });
            }
        }

        addReportDetail(details, "Report type", type);
        addReportDetail(details, "Title", title);
        addTranslatedReportDetail(details, "English title", report.optString("title", ""), title, adminView);
        String category = report.optString("original_category", report.optString("category", "")).trim();
        addReportDetail(details, "Category", category);
        addTranslatedReportDetail(details, "English category", report.optString("category", ""), category, adminView);
        String description = report.optString("original_description", report.optString("description", "")).trim();
        addReportDetail(details, "Description", description);
        addTranslatedReportDetail(details, "English description", report.optString("description", ""), description, adminView);
        addReportDetail(details, "Date", report.optString("report_date", ""));
        String location = report.optString("original_report_location", report.optString("report_location", report.optString("location", ""))).trim();
        addReportDetail(details, "Location", location);
        addTranslatedReportDetail(details, "English location", report.optString("report_location", ""), location, adminView);
        addReportDetail(details, "Status", report.optString("status", ""));
        double latitude = report.optDouble("lat", report.optDouble("latitude", 0.0));
        double longitude = report.optDouble("lng", report.optDouble("longitude", 0.0));
        String preciseLocation;
        if (latitude != 0.0 || longitude != 0.0) {
            preciseLocation = String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
        } else {
            preciseLocation = "No location shared";
        }
        if (latitude != 0.0 || longitude != 0.0) {
            addClickableMapReportDetail(details, "Precise location", preciseLocation, latitude, longitude);
        } else {
            addReportDetail(details, "Precise location", preciseLocation);
        }
        if (adminView) {
            addReportDetail(details, "Posted", report.optString("created_at", ""));
            addReportDetail(details, "Reporter ID", report.optString("created_by", ""));
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(details, new ScrollView.LayoutParams(-1, -2));
        int maxContentHeight = Math.max(dp(180), getResources().getDisplayMetrics().heightPixels - dp(300));
        content.addView(scroll, new LinearLayout.LayoutParams(-1, Math.min(dp(390), maxContentHeight)));

        if (!adminView
                && "LOST".equalsIgnoreCase(type)
                && isActiveCommunityReport(report)) {
            addCommunityPosterAction(content, report, 0, dp(8));
        }

        if (allowDelete && !adminView) {
            TextView delete = actionButton(LanguageManager.profileText(this, "delete_report"), false);
            delete.setTextColor(Color.rgb(190, 45, 55));
            addFieldToDialog(content, delete);
            delete.setOnClickListener(view -> confirmDeleteReport(report, dialog));
        }

        String contentId = report.optString("id", "").trim();
        FirebaseUser signedInUser = FirebaseAuth.getInstance().getCurrentUser();
        String authorId = report.optString("created_by", "").trim();
        if (!adminView && !contentId.isEmpty()
                && (authorId.isEmpty() || signedInUser == null
                || !authorId.equals(signedInUser.getUid()))) {
            TextView reportContent = actionButton("Report this content", false);
            addFieldToDialog(content, reportContent);
            reportContent.setOnClickListener(view -> promptReportContent(report));
        }

        TextView close = actionButton("Close", true);
        close.setOnClickListener(view -> dialog.dismiss());
        addFieldToDialog(content, close);

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void promptReportContent(JSONObject report) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sign in to report content.", Toast.LENGTH_LONG).show();
            return;
        }
        String[] reasons = {
                "Inappropriate content",
                "Spam or misleading",
                "Personal information",
                "Fraud or unsafe activity",
                "Other concern"
        };
        String[] reasonCodes = {
                "inappropriate",
                "spam",
                "personal_information",
                "fraud",
                "other"
        };
        Dialog dialog = new Dialog(this);
        LinearLayout content = themedDialogContent(
                0,
                "Report this content",
                "Choose the reason that best describes your concern."
        );
        for (int index = 0; index < reasons.length; index++) {
            final int selected = index;
            TextView reason = actionButton(reasons[index], false);
            reason.setOnClickListener(view -> {
                dialog.dismiss();
                user.getIdToken(false)
                        .addOnSuccessListener(token -> network.execute(() -> {
                            String reportType = report.optString("type", "").toLowerCase(Locale.US);
                            String reportId = report.optString("id", "");
                            String body = "{\"reason\":\"" + reasonCodes[selected] + "\"}";
                            boolean submitted = postAuthorized(
                                    "/api/items/" + reportType + "/" + Uri.encode(reportId) + "/reports",
                                    token.getToken(),
                                    body
                            );
                            runOnUiThread(() -> Toast.makeText(
                                    this,
                                    submitted
                                            ? "Thanks. Your report was sent for review."
                                            : "Could not send your report. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show());
                        }))
                        .addOnFailureListener(error -> Toast.makeText(
                                this,
                                "Could not authenticate your content report.",
                                Toast.LENGTH_LONG
                        ).show());
            });
            addFieldToDialog(content, reason);
        }
        TextView cancel = actionButton("Cancel", true);
        cancel.setOnClickListener(view -> dialog.dismiss());
        addFieldToDialog(content, cancel);

        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private boolean isActiveCommunityReport(JSONObject report) {
        String status = report.optString("status", "").trim().toLowerCase(Locale.ROOT);
        return resolveReportWorkflowStage(report) != 4
                && !status.equals("completed")
                && !status.equals("reunited")
                && !status.equals("resolved")
                && !status.equals("closed")
                && !status.equals("removed")
                && !status.equals("deleted");
    }

    private void addCommunityPosterAction(LinearLayout parent, JSONObject report, int horizontalMargin, int topMargin) {
        TextView poster = actionButton("Generate Community Poster", false);
        LinearLayout.LayoutParams posterParams = new LinearLayout.LayoutParams(-1, dp(46));
        posterParams.setMargins(horizontalMargin, topMargin, horizontalMargin, 0);
        parent.addView(poster, posterParams);
        poster.setOnClickListener(view -> generateAndShareCommunityPoster(report));
    }

    private void generateAndShareCommunityPoster(JSONObject report) {
        String title = report.optString("original_title", report.optString("title", "Lost item")).trim();
        String location = report.optString(
                "original_report_location",
                report.optString("report_location", report.optString("location", ""))
        ).trim();
        String date = report.optString("report_date", "").trim();
        String category = report.optString("original_category", report.optString("category", "")).trim();
        String type = report.optString("type", "");
        String itemToken = report.optString("share_token", report.optString("token", report.optString("id", ""))).trim();
        if (itemToken.isEmpty()) {
            Toast.makeText(this, "This report cannot be shared because its item link is unavailable.", Toast.LENGTH_LONG).show();
            return;
        }
        String itemUrl = "https://fendly.app/item/" + Uri.encode(itemToken);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sign in to share a community poster.", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Preparing community poster…", Toast.LENGTH_SHORT).show();
        user.getIdToken(false)
                .addOnSuccessListener(tokenResult -> network.execute(() -> {
                    try {
                        String profileResponse = getAuthorized("/api/users/profile", tokenResult.getToken());
                        if (profileResponse == null) {
                            throw new java.io.IOException("Could not load your saved profile links.");
                        }
                        JSONObject profile = new JSONObject(profileResponse);
                        Map<String, String> links = new LinkedHashMap<>();
                        addNonBlankSocialLink(links, "Instagram", profile.optString("instagram_url", ""));
                        addNonBlankSocialLink(links, "Facebook", profile.optString("facebook_url", ""));
                        runOnUiThread(() -> loadPosterPhotoAndShare(
                                reportImageUrls(report), title, location, date, type, category, itemUrl, links
                        ));
                    } catch (Exception error) {
                        Log.e("COMMUNITY_POSTER", "Could not load profile links for poster sharing", error);
                        runOnUiThread(() -> Toast.makeText(
                                this,
                                "Could not prepare sharing details. Please try again.",
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }))
                .addOnFailureListener(error -> {
                    Log.e("COMMUNITY_POSTER", "Could not authenticate poster sharing", error);
                    Toast.makeText(this, "Could not authenticate. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

    private void addNonBlankSocialLink(Map<String, String> links, String label, String url) {
        String normalized = url == null ? "" : url.trim();
        if (!normalized.isEmpty()) links.put(label, normalized);
    }

    private void loadPosterPhotoAndShare(
            List<String> imageUrls,
            String title,
            String location,
            String date,
            String itemType,
            String category,
            String itemUrl,
            Map<String, String> profileLinks
    ) {
        if (imageUrls.isEmpty()) {
            renderAndShareCommunityPoster(null, title, location, date, itemType, category, itemUrl, profileLinks);
            return;
        }

        Glide.with(this)
                .asBitmap()
                .load(imageUrls.get(0))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        renderAndShareCommunityPoster(resource, title, location, date, itemType, category, itemUrl, profileLinks);
                    }

                    @Override
                    public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
                        Log.w("COMMUNITY_POSTER", "Could not load the item photo; generating a poster without it");
                        renderAndShareCommunityPoster(null, title, location, date, itemType, category, itemUrl, profileLinks);
                    }
                });
    }

    private void renderAndShareCommunityPoster(
            Bitmap photo,
            String title,
            String location,
            String date,
            String itemType,
            String category,
            String itemUrl,
            Map<String, String> profileLinks
    ) {
        renderCommunityPoster(photo, title, location, date, itemType, category, itemUrl, profileLinks);
    }

    private void renderCommunityPoster(
            Bitmap photo,
            String title,
            String location,
            String date,
            String itemType,
            String category,
            String itemUrl,
            Map<String, String> profileLinks
    ) {
        Bitmap poster = null;
        try {
            poster = CommunityPoster.render(title, location, date, itemType, category, itemUrl, photo);
            String subject = CommunityPosterSubject.homeSubject(title, category);
            String homeMessage = "Help bring this "
                    + ("Item".equals(subject) ? "item" : subject) + " home";
            SharePosterUtil.sharePoster(
                    this,
                    poster,
                    homeMessage + ": " + title + "\n" + itemUrl,
                    profileLinks
            );
        } catch (Exception error) {
            Log.e("COMMUNITY_POSTER", "Could not generate or share the community poster", error);
            Toast.makeText(this, "Could not generate the poster. Please try again.", Toast.LENGTH_LONG).show();
        } finally {
            if (poster != null && !poster.isRecycled()) poster.recycle();
        }
    }

    private List<String> reportImageUrls(JSONObject report) {
        List<String> imageUrls = new ArrayList<>();
        JSONArray images = report.optJSONArray("image_urls");
        if (images != null) {
            for (int index = 0; index < images.length(); index++) {
                String imageUrl = images.optString(index, "").trim();
                if (!imageUrl.isEmpty() && !imageUrls.contains(imageUrl)) imageUrls.add(imageUrl);
            }
        }
        String primaryImageUrl = report.optString("image_url", report.optString("imageUrl", "")).trim();
        if (!primaryImageUrl.isEmpty() && !imageUrls.contains(primaryImageUrl)) imageUrls.add(0, primaryImageUrl);
        return imageUrls;
    }

    private void addTranslatedReportDetail(LinearLayout parent, String label, String value, String source, boolean adminView) {
        String normalizedValue = value == null ? "" : value.trim();
        String normalizedSource = source == null ? "" : source.trim();
        if (adminView && !normalizedValue.isEmpty() && !normalizedValue.equalsIgnoreCase(normalizedSource)) {
            addReportDetail(parent, label, normalizedValue);
        }
    }

    private void addClickableMapReportDetail(LinearLayout parent, String label, String value, double latitude, double longitude) {
        String content = value == null ? "" : value.trim();
        if (content.isEmpty() || content.equalsIgnoreCase("null")) return;
        TextView labelView = text(label, 11, secondaryTextColor(), Typeface.NORMAL);
        labelView.setPadding(0, dp(10), 0, dp(2));
        parent.addView(labelView, new LinearLayout.LayoutParams(-1, -2));

        TextView valueView = text(content, 14, accentColor(), Typeface.NORMAL);
        valueView.setClickable(true);
        valueView.setFocusable(true);
        valueView.setTextColor(accentColor());
        valueView.setOnClickListener(view -> openReportLocationInMaps(latitude, longitude));
        parent.addView(valueView, new LinearLayout.LayoutParams(-1, -2));
    }

    private void openReportLocationInMaps(double latitude, double longitude) {
        String uriString = "https://www.google.com/maps/search/?api=1&query="
                + latitude + "%2C" + longitude;
        Uri mapUri = Uri.parse(uriString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        mapIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void addReportDetail(LinearLayout parent, String label, String value) {
        String content = value == null ? "" : value.trim();
        if (content.isEmpty() || content.equalsIgnoreCase("null")) return;
        TextView labelView = text(label, 11, secondaryTextColor(), Typeface.NORMAL);
        labelView.setPadding(0, dp(10), 0, dp(2));
        parent.addView(labelView, new LinearLayout.LayoutParams(-1, -2));
        TextView valueView = text(content, 14, primaryTextColor(), Typeface.NORMAL);
        parent.addView(valueView, new LinearLayout.LayoutParams(-1, -2));
    }

    private void loadAdminMatches(JSONObject foundItem, TextView heading, LinearLayout matches) {
        heading.setVisibility(View.VISIBLE);
        matches.removeAllViews();
        matches.addView(text("Loading saved AI matches...", 12, secondaryTextColor(), Typeface.NORMAL));
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) return;
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String alertsResponse = getAuthorized("/api/admin/alerts", token.getToken());
            if (alertsResponse != null) {
                try {
                    JSONArray alerts = new JSONArray(alertsResponse);
                    JSONArray linkedAlerts = new JSONArray();
                    for (int index = 0; index < alerts.length(); index++) {
                        JSONObject alert = alerts.getJSONObject(index);
                        if (alertMatchesFoundReport(alert, foundItem)) linkedAlerts.put(alert);
                    }
                    if (linkedAlerts.length() > 0) {
                        runOnUiThread(() -> renderSavedAdminMatches(linkedAlerts, matches));
                        return;
                    }
                } catch (Exception ignored) {
                }
            }

            runOnUiThread(() -> {
                matches.removeAllViews();
                matches.addView(text("No saved AI alert linked. Searching other reports...", 12,
                        secondaryTextColor(), Typeface.NORMAL));
            });
            String response = null;
            try {
                AiMatchService.ApiResponse matchResponse = AiMatchService.findMatches(
                    foundItem.optString("id", ""), "found", token.getToken());
                if (matchResponse.isSuccessful()) response = matchResponse.getBody();
            } catch (Exception ignored) {
            }
            String result = response;
            runOnUiThread(() -> renderAdminMatches(foundItem, result, matches));
        }));
    }

    private boolean alertMatchesFoundReport(JSONObject alert, JSONObject foundItem) {
        String alertId = alert.optString("found_item_id", "").trim();
        String reportId = foundItem.optString("id", "").trim();
        if (!alertId.isEmpty() && alertId.equals(reportId)) return true;

        String alertImage = alert.optString("found_image_url", "").trim();
        String reportImage = foundItem.optString("image_url", foundItem.optString("imageUrl", "")).trim();
        if (!alertImage.isEmpty() && !reportImage.isEmpty()) return alertImage.equals(reportImage);

        String alertTitle = alert.optString("found_title", "").trim();
        String originalTitle = foundItem.optString("original_title", "").trim();
        String displayTitle = foundItem.optString("title", "").trim();
        return !alertTitle.isEmpty() && (alertTitle.equalsIgnoreCase(originalTitle)
                || alertTitle.equalsIgnoreCase(displayTitle));
    }

    private void renderSavedAdminMatches(JSONArray alerts, LinearLayout matches) {
        matches.removeAllViews();
        for (int index = 0; index < alerts.length(); index++) {
            JSONObject alert = alerts.optJSONObject(index);
            if (alert == null) continue;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            card.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));

            double confidence = alert.optDouble("confidence", 0.0);
            TextView score = text(String.format(Locale.US, "SAVED AI MATCH  ·  %d%%", Math.round(confidence * 100)),
                    11, GOLD_ON, Typeface.BOLD);
            card.addView(score, new LinearLayout.LayoutParams(-1, dp(24)));

            LinearLayout comparison = new LinearLayout(this);
            comparison.setOrientation(LinearLayout.HORIZONTAL);
            comparison.addView(adminAlertComparisonColumn(
                    "FOUND", alert.optString("found_title", "Found item"),
                    alert.optString("found_description", ""), alert.optString("found_image_url", ""), FOUND_GOLD),
                    new LinearLayout.LayoutParams(0, -2, 1f));
            LinearLayout.LayoutParams lostParams = new LinearLayout.LayoutParams(0, -2, 1f);
            lostParams.setMargins(dp(8), 0, 0, 0);
            comparison.addView(adminAlertComparisonColumn(
                    "LOST", alert.optString("lost_title", "Lost report"),
                    alert.optString("lost_description", ""), alert.optString("lost_image_url", ""), LOST_GREEN),
                    lostParams);
            card.addView(comparison, new LinearLayout.LayoutParams(-1, -2));
            matches.addView(card, contentParams(-1, -2, dp(8)));
        }
        if (matches.getChildCount() == 0) {
            matches.addView(text("No saved AI matches found for this report.", 12,
                    secondaryTextColor(), Typeface.NORMAL));
        }
    }

    private void renderAdminMatches(JSONObject foundItem, String response, LinearLayout matches) {
        matches.removeAllViews();
        if (response == null) {
            matches.addView(text("Could not search matches. Try again.", 12, secondaryTextColor(), Typeface.NORMAL));
            return;
        }
        try {
            JSONArray results = new JSONArray(response);
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
            matches.addView(text("Matches unavailable.", 12, secondaryTextColor(), Typeface.NORMAL));
        }
    }

    private LinearLayout adminMatchCard(JSONObject foundItem, JSONObject lostItem, double score) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        TextView scoreLabel = text(String.format(Locale.US, "MATCH  %d%%", Math.round(score * 100)), 11, GOLD_ON, Typeface.NORMAL);
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

    private void showReportMatchDialog(String itemType, String title, String description, JSONArray results) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(18), dp(12), dp(18), dp(12));

        int added = 0;
        for (int index = 0; index < Math.min(results.length(), 5); index++) {
            JSONObject result = results.optJSONObject(index);
            if (result == null) continue;

            JSONObject matchedItem = result.optJSONObject("item");
            if (matchedItem == null) {
                matchedItem = result;
            }
            if (matchedItem == null) continue;

            String matchType = result.optString("matchType", matchedItem.optString("matchType", ""));
            double scoreForCard = result.optDouble("score", matchedItem.optDouble("score", 0.0));

            LinearLayout card = matchResultCard(matchedItem, scoreForCard, matchType);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.setMargins(0, 0, 0, dp(10));
            rows.addView(card, cardParams);
            added++;
        }

        if (added == 0) {
            TextView empty = text("No possible matches found yet", 13, secondaryTextColor(), Typeface.NORMAL);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(12), dp(24), dp(12), dp(24));
            rows.addView(empty, new LinearLayout.LayoutParams(-1, -2));
        }

        scroll.addView(rows);
        new AlertDialog.Builder(this)
                .setTitle("Possible matches")
                .setView(scroll)
                .setPositiveButton("Close", null)
                .show();
    }

    private LinearLayout matchResultCard(JSONObject matchItem, double score) {
        String type = matchItem.optString("matchType", "");
        return matchResultCard(matchItem, score, type);
    }

    private LinearLayout matchResultCard(JSONObject matchItem, double score, String matchType) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));

        String badgeText;
        int badgeColor = GOLD;
        int percent = Math.max(0, Math.min(100, Math.round((float) score * 100f)));
        badgeText = percent + "% Visual Match";

        TextView scoreLabel = text(badgeText, 12, badgeColor == LOST_GREEN ? LOST_GREEN_ON : GOLD_ON, Typeface.NORMAL);
        scoreLabel.setGravity(Gravity.CENTER);
        scoreLabel.setBackground(round(badgeColor, 10));
        scoreLabel.setPadding(dp(10), dp(6), dp(10), dp(6));
        card.addView(scoreLabel, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout infoRow = new LinearLayout(this);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);
        infoRow.setPadding(0, dp(10), 0, 0);

        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int imageSize = dp(84);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        imageParams.setMargins(0, 0, dp(10), 0);
        imageView.setLayoutParams(imageParams);
        imageView.setBackground(roundWithStroke(backgroundColor(), 10, borderColor()));
        imageView.setImageDrawable(new ColorDrawable(Color.argb(120, 232, 178, 74)));
        String photoUrl = matchItem.optString("image_url", matchItem.optString("imageUrl", "")).trim();
        if (!photoUrl.isEmpty()) {
            Glide.with(this).load(photoUrl).placeholder(new ColorDrawable(Color.argb(120, 232, 178, 74))).error(new ColorDrawable(Color.argb(120, 200, 200, 200))).into(imageView);
        }
        infoRow.addView(imageView);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView title = text(matchItem.optString("title", "Untitled"), 14, primaryTextColor(), Typeface.NORMAL);
        title.setMaxLines(2);
        textColumn.addView(title, new LinearLayout.LayoutParams(-1, -2));

        String description = matchItem.optString("description", "").trim();
        if (!description.isEmpty()) {
            TextView desc = text(description, 11, secondaryTextColor(), Typeface.NORMAL);
            desc.setMaxLines(2);
            textColumn.addView(desc, new LinearLayout.LayoutParams(-1, -2));
        } else {
            TextView details = text("Full details are visible only to authorized Fendly administrators.",
                    10, secondaryTextColor(), Typeface.NORMAL);
            details.setMaxLines(2);
            textColumn.addView(details, new LinearLayout.LayoutParams(-1, -2));
        }

        String category = matchItem.optString("category", "").trim();
        if (!category.isEmpty()) {
            textColumn.addView(text("Category: " + category, 10, secondaryTextColor(), Typeface.NORMAL),
                    new LinearLayout.LayoutParams(-1, -2));
        }
        String reportDate = matchItem.optString("report_date", "").trim();
        if (!reportDate.isEmpty()) {
            textColumn.addView(text("Reported: " + reportDate, 10, secondaryTextColor(), Typeface.NORMAL),
                    new LinearLayout.LayoutParams(-1, -2));
        }

        infoRow.addView(textColumn);
        card.addView(infoRow, new LinearLayout.LayoutParams(-1, -2));
        return card;
    }

    private LinearLayout adminComparisonColumn(String label, JSONObject item, int accent) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(10), dp(10), dp(10), dp(10));
        column.setBackground(roundWithStroke(backgroundColor(), 10, accent));
        column.addView(text(label, 10, accent, Typeface.NORMAL), new LinearLayout.LayoutParams(-1, dp(18)));
        column.addView(text(item.optString("title", "Untitled"), 13, primaryTextColor(), Typeface.NORMAL), new LinearLayout.LayoutParams(-1, dp(24)));
        TextView description = text(item.optString("description", "No description"), 10, secondaryTextColor(), Typeface.NORMAL);
        description.setMaxLines(3);
        column.addView(description, new LinearLayout.LayoutParams(-1, dp(48)));
        column.addView(text(item.optString("category", "other"), 10, secondaryTextColor(), Typeface.NORMAL), new LinearLayout.LayoutParams(-1, dp(18)));
        return column;
    }

    private void searchAdminUsers(
            String query,
            LinearLayout results,
            TextView button,
            EditText queryField,
            TextView matchesHeading,
            LinearLayout matches
    ) {
        if (query.length() < 2) {
            Toast.makeText(this, "Enter at least 2 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        button.setText(translate("Searching..."));
        button.setEnabled(false);
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String userResponse = fetchAdminSearch(query, token.getToken());
            String reportResponse = fetchAdminReportSearch(query, token.getToken());
            runOnUiThread(() -> {
                button.setText(translate("Search"));
                button.setEnabled(true);
                if (!query.equals(queryField.getText().toString().trim())) return;
                results.removeAllViews();
                java.util.HashSet<String> displayedReportIds = new java.util.HashSet<>();
                boolean hasResults = false;

                if (userResponse != null) {
                    try {
                        JSONArray users = new JSONArray(userResponse);
                        if (users.length() > 0) {
                            addField(results, text("Matching users", 13, primaryTextColor(), Typeface.BOLD));
                            hasResults = true;
                        }
                        for (int index = 0; index < users.length(); index++) {
                            JSONObject result = users.getJSONObject(index);
                            JSONObject user = result.optJSONObject("user");
                            String identity = user == null ? "User" : user.optString("full_name", "User") + " · " + user.optString("username", "") + " · " + user.optString("mobile", "");
                            addField(results, text(identity, 14, primaryTextColor(), Typeface.NORMAL));
                            JSONArray userReports = result.optJSONArray("reports");
                            if (userReports == null || userReports.length() == 0) {
                                addField(results, text("No reports.", 12, secondaryTextColor(), Typeface.NORMAL));
                            } else {
                                for (int reportIndex = 0; reportIndex < userReports.length(); reportIndex++) {
                                    JSONObject report = userReports.getJSONObject(reportIndex);
                                    String reportId = report.optString("id", "");
                                    if (!reportId.isEmpty()) displayedReportIds.add(reportId);
                                    String imageState = report.optString("image_url", "").isEmpty() ? "No image" : "Image attached";
                                    String reportDetails = report.optString("type", "ITEM") + "  ·  " + report.optString("description", "")
                                            + "  ·  " + report.optString("category", "other")
                                            + "  ·  location: " + report.optDouble("lat", 0.0) + ", " + report.optDouble("lng", 0.0)
                                            + "  ·  " + imageState;
                                        LinearLayout reportRow = reportRow(report.optString("title", "Untitled"), reportDetails);
                                        reportRow.setOnClickListener(view -> showReportDetailsDialog(report, true));
                                        addField(results, reportRow);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (reportResponse != null) {
                    try {
                        JSONArray reportResults = new JSONArray(reportResponse);
                        LinearLayout reportSection = adminReportSection("Matching reports", primaryTextColor());
                        for (int index = 0; index < reportResults.length(); index++) {
                            JSONObject report = reportResults.getJSONObject(index);
                            String reportId = report.optString("id", "");
                            if (!reportId.isEmpty() && !displayedReportIds.add(reportId)) continue;
                            boolean isFound = "FOUND".equalsIgnoreCase(report.optString("type"));
                            reportSection.addView(adminReportCard(report, isFound, matchesHeading, matches));
                            hasResults = true;
                        }
                        if (reportSection.getChildCount() > 1) {
                            results.addView(reportSection, contentParams(-1, -2, dp(8)));
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (!hasResults) {
                    String message = userResponse == null && reportResponse == null
                            ? "Search unavailable."
                            : "No matching users or reports.";
                    addField(results, text(message, 14, secondaryTextColor(), Typeface.NORMAL));
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

    private String fetchAdminReportSearch(String query, String idToken) {
        HttpURLConnection connection = null;
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            connection = (HttpURLConnection) new URL(API_BASE + "/api/admin/reports/search?q=" + encodedQuery).openConnection();
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
        adminEnglishUi = true;
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnDismissListener(dialogInterface -> adminEnglishUi = false);
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

        TextView title = text("Admin login", 18, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(2));
        form.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = text("Authorized access only", 11, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setGravity(Gravity.CENTER);
        form.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(28)));

        LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(-1, dp(48));
        usernameParams.setMargins(0, dp(10), 0, 0);
        form.addView(username, usernameParams);
        TextView pinLabel = text("4-digit PIN", 11, secondaryTextColor(), Typeface.NORMAL);
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
        TextView cancel = text("Cancel", 12, secondaryTextColor(), Typeface.NORMAL);
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(view -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(88), dp(44)));
        TextView login = text("Login", 12, GOLD_ON, Typeface.NORMAL);
        login.setGravity(Gravity.CENTER);
        login.setBackground(goldButton());
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(dp(100), dp(44));
        loginParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(login, loginParams);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, dp(44));
        actionsParams.setMargins(0, dp(16), 0, 0);
        form.addView(actions, actionsParams);

        login.setOnClickListener(view -> {
            String adminUsername = username.getText().toString().trim();
            String adminPin = pinValue(adminPinCells);

            AdminViewModel adminViewModel = new AdminViewModel();
            if (adminViewModel.verifyAdminCredentials(adminUsername, adminPin)) {
                dialog.dismiss();
                adminAlertsAutoShownThisVisit = false;
                adminReportFilter = "";
                adminReportCategory = "items";
                showAdminDashboard();
            } else {
                Toast.makeText(this, "Invalid Admin Name or PIN", Toast.LENGTH_LONG).show();
            }
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

    private boolean isPendingAlert(JSONObject alert) {
        if (alert == null) return false;
        String reviewStatus = alert.optString("review_status", "").trim();
        if (reviewStatus.isEmpty()) return true;
        String normalized = reviewStatus.toLowerCase(Locale.US);
        return !("confirmed".equals(normalized) || "rejected".equals(normalized));
    }

    private void fetchPendingNotificationCount() {
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) return;
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = getAuthorized("/api/admin/alerts", token.getToken());
            if (response == null) return;
            try {
                JSONArray alerts = new JSONArray(response);
                int pendingCount = 0;
                for (int index = 0; index < alerts.length(); index++) {
                    JSONObject alert = alerts.getJSONObject(index);
                    if (isDummyAlert(alert)) continue;
                    if (isPendingAlert(alert)) {
                        pendingCount++;
                    }
                }
                int newCount = pendingCount;
                runOnUiThread(() -> updatePendingNotificationCount(newCount));
            } catch (Exception ignored) {}
        }));
    }

    private void fetchUserNotificationCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            updateUserNotificationCount(0);
            return;
        }
        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = getAuthorized("/api/users/notifications", token.getToken());
            if (response == null) return;
            try {
                int unreadCount = new JSONObject(response).optInt("unread_count", 0);
                runOnUiThread(() -> updateUserNotificationCount(unreadCount));
            } catch (Exception ignored) {}
        }));
    }

    private void loadUserNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sign in to view your notifications", Toast.LENGTH_SHORT).show();
            return;
        }
        user.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = getAuthorized("/api/users/notifications", token.getToken());
            runOnUiThread(() -> {
                if (response == null) {
                    Toast.makeText(this, "Could not load your notifications", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    JSONObject payload = new JSONObject(response);
                    JSONArray notifications = payload.optJSONArray("notifications");
                    if (notifications == null) notifications = new JSONArray();
                    int unreadCount = payload.optInt("unread_count", 0);
                    updateUserNotificationCount(unreadCount);
                    if (notifications.length() == 0) {
                        Toast.makeText(this, LanguageManager.profileText(this, "no_notifications"), Toast.LENGTH_SHORT).show();
                    } else {
                        showUserNotificationsDialog(notifications);
                    }
                    if (unreadCount > 0) {
                        network.execute(() -> {
                            if (postAuthorized("/api/users/notifications/read", token.getToken())) {
                                runOnUiThread(() -> updateUserNotificationCount(0));
                            }
                        });
                    }
                } catch (Exception error) {
                    Toast.makeText(this, "Your notifications could not be read", Toast.LENGTH_LONG).show();
                }
            });
        }));
    }

    private void showUserNotificationsDialog(JSONArray notifications) throws Exception {
        StringBuilder message = new StringBuilder();
        for (int index = 0; index < notifications.length(); index++) {
            JSONObject notification = notifications.getJSONObject(index);
            if (message.length() > 0) message.append("\n\n");
            message.append(notification.optString("title", "Fendly notification"));
            String body = notification.optString("body", "");
            if (!body.isEmpty()) message.append("\n").append(body);
        }
        new AlertDialog.Builder(this)
                .setTitle("Your notifications")
                .setMessage(message.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private void updatePendingNotificationCount(int count) {
        pendingNotificationCount = Math.max(0, count);
        updateNotificationBadge(pendingNotificationBadge, pendingNotificationCount);
    }

    private void updateUserNotificationCount(int count) {
        unreadUserNotificationCount = Math.max(0, count);
        updateNotificationBadge(userNotificationBadge, unreadUserNotificationCount);
    }

    private void updateNotificationBadge(TextView badge, int count) {
        if (badge == null) return;
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        badge.setContentDescription(count + " notifications");
    }

    private View createNotificationIconButton(int count, boolean adminNotifications, View.OnClickListener onClickListener) {
        FrameLayout frame = new FrameLayout(this);
        frame.setClipChildren(false);
        frame.setClipToPadding(false);

        TextView notificationButton = text("🔔", 16, secondaryTextColor(), Typeface.NORMAL);
        notificationButton.setGravity(Gravity.CENTER);
        notificationButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        notificationButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        notificationButton.setElevation(dp(2));
        notificationButton.setContentDescription(adminNotifications ? "Admin notifications" : "Match notifications");
        if (onClickListener != null) {
            notificationButton.setOnClickListener(onClickListener);
            frame.setClickable(true);
            frame.setFocusable(true);
            frame.setContentDescription(adminNotifications ? "Admin notifications" : "Match notifications");
            frame.setOnClickListener(onClickListener);
        }

        frame.addView(notificationButton, new FrameLayout.LayoutParams(dp(42), dp(42)));

        TextView badge = new TextView(this);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setIncludeFontPadding(false);
        badge.setMinWidth(dp(22));
        badge.setPadding(dp(5), 0, dp(5), 0);

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(Color.rgb(220, 38, 38));
        badge.setBackground(badgeBg);
        badge.setElevation(dp(4));

        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(-2, dp(22));
        badgeParams.gravity = Gravity.TOP | Gravity.END;
        badgeParams.setMargins(0, dp(1), dp(1), 0);
        frame.addView(badge, badgeParams);
        if (adminNotifications) {
            pendingNotificationBadge = badge;
        } else {
            userNotificationBadge = badge;
        }
        updateNotificationBadge(badge, count);

        return frame;
    }

    private void loadAdminAlerts(boolean pendingOnly) {
        if (pendingOnly && adminAlertsAutoShownThisVisit) return;
        if (pendingOnly) adminAlertsAutoShownThisVisit = true;
        FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();
        if (adminUser == null) {
            showAdminNotificationsEmptyState(
                    "Admin notifications",
                    "The Admin PIN opens this dashboard, but notifications require a signed-in Firebase admin account."
            );
            if (pendingOnly) adminAlertsAutoShownThisVisit = false;
            return;
        }
        adminUser.getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            if (token == null || token.getToken() == null || token.getToken().trim().isEmpty()) {
                runOnUiThread(() -> {
                    showAdminNotificationsEmptyState(
                            "Admin sign-in required",
                            "Could not get a Firebase sign-in token for this admin account. Please sign in again."
                    );
                    if (pendingOnly) adminAlertsAutoShownThisVisit = false;
                });
                return;
            }
            AuthorizedResponse apiResponse = getAuthorizedResponse("/api/admin/alerts", token.getToken());
            runOnUiThread(() -> {
                if (apiResponse.statusCode < 200 || apiResponse.statusCode >= 300) {
                    Log.e(
                            "ADMIN_NOTIFICATIONS",
                            "Admin alerts request failed with HTTP " + apiResponse.statusCode
                                    + (apiResponse.body.isEmpty() ? "" : ": " + apiResponse.body)
                    );
                    showAdminNotificationsEmptyState(
                            "Could not load notifications",
                            adminAlertsRequestError(apiResponse)
                    );
                    if (pendingOnly) adminAlertsAutoShownThisVisit = false;
                    return;
                }
                try {
                    JSONArray alerts = new JSONArray(apiResponse.body);
                    JSONArray visibleAlerts = new JSONArray();
                    int pendingCount = 0;
                    for (int index = 0; index < alerts.length(); index++) {
                        JSONObject alert = alerts.getJSONObject(index);
                        if (isDummyAlert(alert)) continue;
                        if (isPendingAlert(alert)) {
                            pendingCount++;
                        }
                        if (!pendingOnly || isPendingAlert(alert)) {
                            visibleAlerts.put(alert);
                        }
                    }
                    updatePendingNotificationCount(pendingCount);
                    if (visibleAlerts.length() == 0) {
                        showAdminNotificationsEmptyState(
                                "Admin notifications",
                                pendingOnly ? "No pending notifications right now." : "No notifications right now."
                        );
                        return;
                    }
                    showAdminAlertsDialog(visibleAlerts, token.getToken());
                } catch (Exception error) {
                    Log.e("ADMIN_NOTIFICATIONS", "Could not parse admin notifications", error);
                    showAdminNotificationsEmptyState(
                            "Could not read notifications",
                            "Please try again."
                    );
                    if (pendingOnly) adminAlertsAutoShownThisVisit = false;
                }
            });
        })).addOnFailureListener(error -> runOnUiThread(() -> {
            Log.e("ADMIN_NOTIFICATIONS", "Could not get admin authentication token", error);
            showAdminNotificationsEmptyState(
                    "Admin sign-in required",
                    "Firebase could not verify this admin account. Please sign in again."
            );
            if (pendingOnly) adminAlertsAutoShownThisVisit = false;
        }));
    }

    private void showAdminNotificationsEmptyState(String title, String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout content = themedDialogContent(R.drawable.ic_field_lock, title, message);
        TextView close = filledButton("Close", GOLD, GOLD_ON);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(-1, dp(44));
        closeParams.setMargins(0, dp(12), 0, 0);
        content.addView(close, closeParams);
        close.setOnClickListener(view -> dialog.dismiss());
        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private void showAdminAlertsDialog(JSONArray alerts, String idToken) throws Exception {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(22), dp(22), dp(18));
        form.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));

        TextView bellIcon = text("🔔", 24, primaryTextColor(), Typeface.NORMAL);
        bellIcon.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        form.addView(bellIcon, iconParams);

        TextView title = text("Admin alerts", 18, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(4), 0, dp(12));
        form.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView instructions = text("Review report matches.",
            12, secondaryTextColor(), Typeface.NORMAL);
        instructions.setGravity(Gravity.CENTER);
        instructions.setPadding(0, 0, 0, dp(12));
        form.addView(instructions, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);

        for (int index = 0; index < alerts.length(); index++) {
            JSONObject alert = alerts.getJSONObject(index);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(12), dp(12), dp(12), dp(12));
            row.setBackground(roundWithStroke(surfaceColor(), 12, fieldBorderColor()));

            double confidence = alert.optDouble("confidence", 0.0);
            String reviewStatus = alert.optString("review_status", "pending");
            TextView matchLabel = text(
                    String.format(Locale.US, "AI VISUAL SIMILARITY  ·  %d%%", Math.round(confidence * 100)),
                11, GOLD_ON, Typeface.BOLD);
            row.addView(matchLabel, new LinearLayout.LayoutParams(-1, dp(22)));

            LinearLayout comparison = new LinearLayout(this);
            comparison.setOrientation(LinearLayout.HORIZONTAL);
            comparison.addView(adminAlertComparisonColumn(
                "FOUND ITEM",
                alert.optString("found_title", "Found item"),
                alert.optString("found_description", ""),
                alert.optString("found_image_url", ""),
                FOUND_GOLD), new LinearLayout.LayoutParams(0, -2, 1f));
            LinearLayout.LayoutParams lostColumnParams = new LinearLayout.LayoutParams(0, -2, 1f);
            lostColumnParams.setMargins(dp(8), 0, 0, 0);
            comparison.addView(adminAlertComparisonColumn(
                "LOST REPORT",
                alert.optString("lost_title", "Lost report"),
                alert.optString("lost_description", ""),
                alert.optString("lost_image_url", ""),
                LOST_GREEN), lostColumnParams);
            row.addView(comparison, new LinearLayout.LayoutParams(-1, -2));

            boolean reviewed = "confirmed".equalsIgnoreCase(reviewStatus) || "rejected".equalsIgnoreCase(reviewStatus);
            String statusLabel = "confirmed".equalsIgnoreCase(reviewStatus)
                ? "Confirmed match" : "rejected".equalsIgnoreCase(reviewStatus) ? "Not a match" : "Needs review";
            TextView status = text(statusLabel, 11,
                reviewed ? secondaryTextColor() : GOLD_ON, Typeface.NORMAL);
            status.setPadding(0, dp(8), 0, dp(4));
            row.addView(status, new LinearLayout.LayoutParams(-1, -2));

            String alertId = alert.optString("id", "");
            if (!reviewed && !alertId.isEmpty()) {
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            TextView confirm = actionButton("Confirm match", true);
            TextView reject = actionButton("Not a match", false);
            actions.addView(confirm, new LinearLayout.LayoutParams(0, dp(42), 1f));
            LinearLayout.LayoutParams rejectParams = new LinearLayout.LayoutParams(0, dp(42), 1f);
            rejectParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(reject, rejectParams);
            confirm.setOnClickListener(view -> saveAdminAlertReview(
                alertId, "confirmed", idToken, status, actions));
            reject.setOnClickListener(view -> saveAdminAlertReview(
                alertId, "rejected", idToken, status, actions));
            row.addView(actions, new LinearLayout.LayoutParams(-1, dp(42)));
            }

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.setMargins(0, 0, 0, dp(8));
            rows.addView(row, rowParams);

        }

        scroll.addView(rows);
        int maxReviewHeight = Math.max(dp(220), getResources().getDisplayMetrics().heightPixels - dp(300));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, Math.min(dp(460), maxReviewHeight));
        scrollParams.setMargins(0, 0, 0, dp(16));
        form.addView(scroll, scrollParams);

        TextView close = text(translate("Close"), 14, GOLD_ON, Typeface.NORMAL);
        close.setGravity(Gravity.CENTER);
        close.setBackground(goldButton());
        close.setOnClickListener(v -> dialog.dismiss());
        form.addView(close, new LinearLayout.LayoutParams(-1, dp(44)));

        dialog.setContentView(form);
        dialog.show();
        sizeThemedDialog(dialog);
    }

    private LinearLayout adminAlertComparisonColumn(String label, String title, String description,
                                                    String imageUrl, int accent) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);

        TextView heading = text(label, 10, accent, Typeface.BOLD);
        column.addView(heading, new LinearLayout.LayoutParams(-1, dp(20)));

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(roundWithStroke(backgroundColor(), 8, borderColor()));
        image.setImageDrawable(new ColorDrawable(Color.argb(100, 190, 190, 190)));
        column.addView(image, new LinearLayout.LayoutParams(-1, dp(112)));
        if (!imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(new ColorDrawable(Color.argb(100, 190, 190, 190)))
                    .error(new ColorDrawable(Color.argb(100, 190, 190, 190)))
                    .into(image);
        }

        TextView itemTitle = text(title, 12, primaryTextColor(), Typeface.BOLD);
        itemTitle.setMaxLines(2);
        itemTitle.setPadding(0, dp(5), 0, 0);
        column.addView(itemTitle, new LinearLayout.LayoutParams(-1, -2));

        TextView itemDescription = text(description.isEmpty() ? "No description" : description,
                10, secondaryTextColor(), Typeface.NORMAL);
        itemDescription.setMaxLines(3);
        column.addView(itemDescription, new LinearLayout.LayoutParams(-1, -2));
        return column;
    }

    private void saveAdminAlertReview(String alertId, String decision, String idToken,
                                      TextView status, LinearLayout actions) {
        status.setText(translate("Saving decision..."));
        actions.setEnabled(false);
        for (int index = 0; index < actions.getChildCount(); index++) {
            actions.getChildAt(index).setEnabled(false);
        }
        String body = "{\"decision\":\"" + decision + "\"}";
        network.execute(() -> {
            boolean saved = postAuthorized("/api/admin/alerts/" + alertId + "/review", idToken, body);
            runOnUiThread(() -> {
                if (saved) {
                    status.setText(translate(
                            "confirmed".equals(decision) ? "Confirmed match" : "Not a match"
                    ));
                    actions.setVisibility(View.GONE);
                } else {
                    status.setText(translate("Could not save decision. Try again."));
                    actions.setEnabled(true);
                    for (int index = 0; index < actions.getChildCount(); index++) {
                        actions.getChildAt(index).setEnabled(true);
                    }
                }
            });
        });
    }

    private boolean postAuthorized(String path, String idToken) {
        return postAuthorized(path, idToken, null);
    }

    private boolean postAuthorized(String path, String idToken, String body) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            return connection.getResponseCode() >= 200 && connection.getResponseCode() < 300;
        } catch (Exception error) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private boolean deleteAuthorized(String path, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 300;
        } catch (Exception error) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
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

    private static final class AuthorizedResponse {
        final int statusCode;
        final String body;

        AuthorizedResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    private AuthorizedResponse getAuthorizedResponse(String path, String idToken) {
        return authorizedResponse("GET", path, idToken);
    }

    private AuthorizedResponse postAuthorizedResponse(String path, String idToken) {
        return authorizedResponse("POST", path, idToken);
    }

    private AuthorizedResponse authorizedResponse(String method, String path, String idToken) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_BASE + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Authorization", "Bearer " + idToken);
            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = stream == null ? "" : readStream(stream);
            return new AuthorizedResponse(statusCode, body);
        } catch (Exception error) {
            Log.e("ADMIN_NOTIFICATIONS", "Admin alerts network request failed", error);
            return new AuthorizedResponse(-1, "");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String adminAlertsRequestError(AuthorizedResponse response) {
        if (response.statusCode < 0) {
            return "Could not reach the Fendly server. Check your connection and try again.";
        }
        String detail = "";
        try {
            detail = new JSONObject(response.body).optString("detail", "").trim();
        } catch (Exception ignored) {
        }
        if (response.statusCode == 401) {
            return "Firebase sign-in was rejected. Sign out and sign in again.";
        }
        if (response.statusCode == 403) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user == null ? "" : user.getUid();
            String reason = detail.isEmpty()
                    ? "This account is not authorized. Verify the configured admin email by OTP in your Fendly profile, or check ADMIN_FIREBASE_UIDS."
                    : detail;
            return uid.isEmpty() ? reason : reason + "\n\nCurrent app Firebase UID: " + uid;
        }
        if (response.statusCode == 503) {
            return "The admin notifications service is unavailable."
                    + (detail.isEmpty() ? "" : " " + detail);
        }
        return "The server returned HTTP " + response.statusCode
                + (detail.isEmpty() ? " while loading notifications." : ": " + detail);
    }

    private final class AuthLabelView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String label;

        AuthLabelView(Context context, String label) {
            super(context);
            this.label = label;
            paint.setTypeface(languageTypeface(Typeface.NORMAL));
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

    private boolean shouldKeepAdminUiInEnglish() {
        return adminEnglishUi || currentPage == PAGE_ADMIN || currentPage == PAGE_ADMIN_SUBSCRIPTIONS;
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(this);
        String resolvedText = shouldKeepAdminUiInEnglish() ? value : translate(value);
        view.setText(resolvedText);
        view.setTextSize(responsiveTextSize(size));
        view.setTag(Float.valueOf(size));
        view.setTextColor(color != 0 ? color : (darkMode ? Color.WHITE : LIGHT_TEXT));
        Typeface tf = typefaceForTextValue(resolvedText, Typeface.NORMAL);
        view.setTypeface(tf, Typeface.NORMAL);
        view.setLineSpacing(0, 1.1f);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
        if (view == null || selectedLanguage <= 0) return;
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int style = textView.getTypeface() != null && textView.getTypeface().isBold()
                    ? Typeface.NORMAL : Typeface.NORMAL;
            Typeface tf = languageTypeface(style);
            if (tf != null) {
                textView.setTypeface(tf);
            }
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
        int systemUiVisibility = darkMode ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (!darkMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
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

    private void removeParent(View v) {
        if (v != null && v.getParent() instanceof ViewGroup) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
    }

    private void addAppControls(LinearLayout parent, boolean authScreen) {
        removeParent(parent);
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);

        if (authScreen) {
            // Top-left: Small expand right/down arrow icon that toggles language and theme icons
            final boolean[] isExpanded = {false};

            TextView expandButton = text("", 18, secondaryTextColor(), Typeface.NORMAL);
            expandButton.setGravity(Gravity.CENTER);
            expandButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            expandButton.setPadding(dp(8), dp(8), dp(8), dp(8));
            expandButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gear, 0, 0, 0);
            expandButton.setElevation(dp(2));
            TextViewCompat.setCompoundDrawableTintList(expandButton, ColorStateList.valueOf(accentColor()));
            expandButton.setContentDescription("Expand menu");

            LinearLayout expandedIconsContainer = new LinearLayout(this);
            expandedIconsContainer.setGravity(Gravity.CENTER_VERTICAL);
            expandedIconsContainer.setVisibility(View.GONE);

            TextView languageIconView = text("", 18, secondaryTextColor(), Typeface.NORMAL);
            languageIconView.setGravity(Gravity.CENTER);
            languageIconView.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            languageIconView.setPadding(dp(8), dp(8), dp(8), dp(8));
            languageIconView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_language, 0, 0, 0);
            languageIconView.setElevation(dp(2));
            TextViewCompat.setCompoundDrawableTintList(languageIconView, ColorStateList.valueOf(accentColor()));
            languageIconView.setContentDescription("Select language");
            languageIconView.setOnClickListener(view -> showLanguagePicker());
            LinearLayout.LayoutParams langParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            langParams.setMargins(dp(6), 0, 0, 0);
            expandedIconsContainer.addView(languageIconView, langParams);

            TextView welcomeInfoIcon = text("i", 20, accentColor(), Typeface.BOLD);
            welcomeInfoIcon.setGravity(Gravity.CENTER);
            welcomeInfoIcon.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            welcomeInfoIcon.setElevation(dp(2));
            welcomeInfoIcon.setContentDescription(onboardingText("info"));
            welcomeInfoIcon.setOnClickListener(view -> showWelcomeOnboarding());
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            infoParams.setMargins(dp(6), 0, 0, 0);
            expandedIconsContainer.addView(welcomeInfoIcon, infoParams);

            FrameLayout themeIconView = new FrameLayout(this);
            int themeBg = darkMode ? surfaceColor() : Color.rgb(244, 239, 232);
            int themeBorder = darkMode ? borderColor() : Color.rgb(214, 211, 204);
            themeIconView.setBackground(roundWithStroke(themeBg, 14, themeBorder));
            themeIconView.setPadding(dp(6), dp(6), dp(6), dp(6));
            ImageView themeImage = new ImageView(this);
            themeImage.setImageResource(darkMode ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
            themeImage.setColorFilter(darkMode ? accentColor() : LIGHT_TEXT);
            themeImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            FrameLayout.LayoutParams themeImgParams = new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER);
            themeIconView.addView(themeImage, themeImgParams);
            themeIconView.setContentDescription(darkMode ? "Switch to light mode" : "Switch to dark mode");
            themeIconView.setOnClickListener(view -> {
                darkMode = !darkMode;
                getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
                applySystemBarColors();
                if (screenRenderer != null) screenRenderer.run();
            });
            LinearLayout.LayoutParams themeParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            themeParams.setMargins(dp(6), 0, 0, 0);
            expandedIconsContainer.addView(themeIconView, themeParams);

            expandButton.setOnClickListener(view -> {
                isExpanded[0] = !isExpanded[0];
                if (isExpanded[0]) {
                    expandButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.material_ic_keyboard_arrow_left_black_24dp, 0, 0, 0);
                    expandedIconsContainer.setVisibility(View.VISIBLE);
                } else {
                    expandButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gear, 0, 0, 0);
                    expandedIconsContainer.setVisibility(View.GONE);
                }
            });

            controls.addView(expandButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
            controls.addView(expandedIconsContainer, new LinearLayout.LayoutParams(-2, -2));

            Space spacer = new Space(this);
            controls.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

            // Top-right: Notification button for user match pings
            View notificationIconButton = createNotificationIconButton(
                    unreadUserNotificationCount, false, view -> loadUserNotifications());
            controls.addView(notificationIconButton, new LinearLayout.LayoutParams(-2, dp(42)));

            parent.addView(controls, new LinearLayout.LayoutParams(-1, -2));
            return;
        }

        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(0, 0, 0, 0);

        if (currentPage == PAGE_ADMIN) {
            final boolean[] isExpanded = {false};
            TextView gearButton = text("", 18, secondaryTextColor(), Typeface.NORMAL);
            gearButton.setGravity(Gravity.CENTER);
            gearButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            gearButton.setPadding(dp(8), dp(8), dp(8), dp(8));
            gearButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gear, 0, 0, 0);
            gearButton.setContentDescription("Admin display settings");
            TextViewCompat.setCompoundDrawableTintList(gearButton, ColorStateList.valueOf(accentColor()));

            LinearLayout adminSettings = new LinearLayout(this);
            adminSettings.setGravity(Gravity.CENTER_VERTICAL);
            adminSettings.setVisibility(View.GONE);

            FrameLayout themeButton = new FrameLayout(this);
            themeButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            ImageView themeImage = new ImageView(this);
            themeImage.setImageResource(darkMode ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
            themeImage.setColorFilter(darkMode ? accentColor() : LIGHT_TEXT);
            themeImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            themeButton.addView(themeImage, new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER));
            themeButton.setContentDescription(darkMode ? "Switch to light mode" : "Switch to dark mode");
            themeButton.setOnClickListener(view -> {
                darkMode = !darkMode;
                getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putBoolean("dark_mode", darkMode).apply();
                applySystemBarColors();
                if (screenRenderer != null) screenRenderer.run();
            });
            LinearLayout.LayoutParams adminThemeParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            adminThemeParams.setMargins(dp(6), 0, 0, 0);
            adminSettings.addView(themeButton, adminThemeParams);

            TextView subscriptionOverrideButton = text("±", 18, secondaryTextColor(), Typeface.NORMAL);
            subscriptionOverrideButton.setGravity(Gravity.CENTER);
            subscriptionOverrideButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            subscriptionOverrideButton.setIncludeFontPadding(false);
            subscriptionOverrideButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            subscriptionOverrideButton.setContentDescription("Manage subscription override");
            subscriptionOverrideButton.setOnClickListener(view -> showAdminSubscriptionOverridePage());
            LinearLayout.LayoutParams adminOverrideParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            adminOverrideParams.setMargins(dp(6), 0, 0, 0);
            adminSettings.addView(subscriptionOverrideButton, adminOverrideParams);

            gearButton.setOnClickListener(view -> {
                isExpanded[0] = !isExpanded[0];
                gearButton.setCompoundDrawablesWithIntrinsicBounds(
                        isExpanded[0] ? R.drawable.material_ic_keyboard_arrow_left_black_24dp : R.drawable.ic_gear, 0, 0, 0);
                adminSettings.setVisibility(isExpanded[0] ? View.VISIBLE : View.GONE);
            });
            controls.addView(gearButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
            controls.addView(adminSettings, new LinearLayout.LayoutParams(-2, -2));
        } else if (currentPage == PAGE_ADMIN_SUBSCRIPTIONS) {
            ImageView backButton = new ImageView(this);
            backButton.setImageResource(R.drawable.material_ic_keyboard_arrow_left_black_24dp);
            backButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            backButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            backButton.setPadding(dp(10), dp(10), dp(10), dp(10));
            backButton.setColorFilter(accentColor());
            backButton.setContentDescription("Back to admin dashboard");
            backButton.setOnClickListener(view -> showAdminDashboard());
            controls.addView(backButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
        } else {
            TextView font = text("A", 17, secondaryTextColor(), Typeface.NORMAL);
            font.setGravity(Gravity.CENTER);
            font.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            font.setIncludeFontPadding(false);
            font.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            font.setPadding(dp(6), 0, dp(6), 0);
            font.setContentDescription("Adjust text size");
            font.setOnClickListener(view -> showFontScaleDialog());
            controls.addView(font, new LinearLayout.LayoutParams(dp(42), dp(42)));
        }

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

        if (currentPage == PAGE_PROFILE) {
            // Top-right link icon for profile page only, expands Instagram and Facebook icons.
            final boolean[] isSocialExpanded = {false};

            LinearLayout rightContainer = new LinearLayout(this);
            rightContainer.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout socialIconsContainer = new LinearLayout(this);
            socialIconsContainer.setGravity(Gravity.CENTER_VERTICAL);
            socialIconsContainer.setVisibility(View.GONE);

            // Instagram button (vector icon only)
            TextView instaButton = text("", 18, secondaryTextColor(), Typeface.NORMAL);
            instaButton.setGravity(Gravity.CENTER);
            instaButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            instaButton.setPadding(dp(8), dp(8), dp(8), dp(8));
            instaButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_instagram, 0, 0, 0);
            instaButton.setElevation(dp(2));
            TextViewCompat.setCompoundDrawableTintList(instaButton, ColorStateList.valueOf(accentColor()));
            instaButton.setContentDescription("Instagram");
            instaButton.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/fendly_community/"));
                    startActivity(intent);
                } catch (Exception ignored) {}
            });
            LinearLayout.LayoutParams instaParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            instaParams.setMarginEnd(dp(6));
            socialIconsContainer.addView(instaButton, instaParams);

            // Facebook button (vector icon only)
            TextView fbButton = text("", 18, secondaryTextColor(), Typeface.NORMAL);
            fbButton.setGravity(Gravity.CENTER);
            fbButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            fbButton.setPadding(dp(8), dp(8), dp(8), dp(8));
            fbButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_facebook, 0, 0, 0);
            fbButton.setElevation(dp(2));
            TextViewCompat.setCompoundDrawableTintList(fbButton, ColorStateList.valueOf(accentColor()));
            fbButton.setContentDescription("Facebook");
            fbButton.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/profile.php?id=61594623491021"));
                    startActivity(intent);
                } catch (Exception ignored) {}
            });
            LinearLayout.LayoutParams fbParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            fbParams.setMarginEnd(dp(6));
            socialIconsContainer.addView(fbButton, fbParams);

            rightContainer.addView(socialIconsContainer, new LinearLayout.LayoutParams(-2, -2));

            // Link icon button (vector icon only)
            TextView linkButton = text("", 18, secondaryTextColor(), Typeface.NORMAL);
            linkButton.setGravity(Gravity.CENTER);
            linkButton.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
            linkButton.setPadding(dp(8), dp(8), dp(8), dp(8));
            linkButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link, 0, 0, 0);
            linkButton.setElevation(dp(2));
            TextViewCompat.setCompoundDrawableTintList(linkButton, ColorStateList.valueOf(accentColor()));
            linkButton.setContentDescription("Community links");
            linkButton.setOnClickListener(view -> {
                isSocialExpanded[0] = !isSocialExpanded[0];
                socialIconsContainer.setVisibility(isSocialExpanded[0] ? View.VISIBLE : View.GONE);
            });
            rightContainer.addView(linkButton, new LinearLayout.LayoutParams(dp(42), dp(42)));

            controls.addView(rightContainer, new LinearLayout.LayoutParams(-2, -2));
        } else if (currentPage == PAGE_ADMIN || currentPage == PAGE_ADMIN_SUBSCRIPTIONS) {
                View notificationIconButton = createNotificationIconButton(
                    pendingNotificationCount, true, view -> loadAdminAlerts(false));
            controls.addView(notificationIconButton, new LinearLayout.LayoutParams(-2, dp(42)));
        } else if (currentPage == PAGE_REPORTS) {
            ImageView guideButton = createGuideButton("My Reports voice guide", this::showReportsGuideDialog);
            controls.addView(guideButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
        } else {
            ImageView guideButton = createGuideButton("Lost and found voice guide", this::showHowToReportDialog);
            controls.addView(guideButton, new LinearLayout.LayoutParams(dp(42), dp(42)));
        }

        parent.addView(controls, new LinearLayout.LayoutParams(-1, -2));
    }

    private ImageView createGuideButton(String description, Runnable action) {
        ImageView button = new ImageView(this);
        button.setImageResource(R.drawable.ic_volume_up);
        button.setColorFilter(secondaryTextColor());
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        button.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        button.setElevation(dp(2));
        button.setContentDescription(description);
        button.setClickable(true);
        button.setFocusable(true);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    private void showReportsGuideDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(24), dp(24), dp(20));
        form.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));

        String titleStr = translateMyReportsTitle();
        String guideText = translateMyReportsText();

        TextView title = text(titleStr, 18, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(16));
        form.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView body = text(guideText, 13, secondaryTextColor(), Typeface.NORMAL);
        body.setPadding(0, 0, 0, dp(20));
        form.addView(body, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView close = text(translate("Got it"), 14, GOLD_ON, Typeface.NORMAL);
        close.setGravity(Gravity.CENTER);
        close.setBackground(goldButton());
        close.setOnClickListener(view -> {
            stopGuideSpeech();
            dialog.dismiss();
        });
        actionsRow.addView(close, new LinearLayout.LayoutParams(0, dp(44), 1f));

        ImageView voiceButton = new ImageView(this);
        voiceButton.setImageResource(android.R.drawable.ic_media_play);
        voiceButton.setColorFilter(primaryTextColor());
        voiceButton.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        voiceButton.setPadding(dp(10), dp(10), dp(10), dp(10));
        voiceButton.setContentDescription("Listen to guide");
        voiceButton.setOnClickListener(v -> speakOrStop(titleStr + ". " + guideText, voiceButton));
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        voiceParams.setMargins(dp(8), 0, 0, 0);
        actionsRow.addView(voiceButton, voiceParams);

        form.addView(actionsRow, new LinearLayout.LayoutParams(-1, -2));

        dialog.setContentView(form);
        dialog.setOnDismissListener(d -> stopGuideSpeech());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
    }

    private void showAdminSubscriptionOverridePage() {
        currentPage = PAGE_ADMIN_SUBSCRIPTIONS;
        screenRenderer = this::showAdminSubscriptionOverridePage;
        LinearLayout root = screenBase("");
        addAdminEnglishHeading("Manage paid access", "Search registered users by mobile number or username");

        EditText queryField = field("Mobile number or username");
        root.addView(queryField, contentParams(-1, dp(48), dp(10)));

        TextView searchButton = actionButton("Search", true);
        root.addView(searchButton, contentParams(-1, dp(44), dp(14)));

        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results, contentParams(-1, -2, 0));
        searchButton.setOnClickListener(view -> searchAdminSubscriptionUsers(
                queryField.getText().toString().trim(), results));
        queryField.setOnEditorActionListener((textView, actionId, event) -> {
            searchButton.performClick();
            return true;
        });
    }

    private void searchAdminSubscriptionUsers(String query, LinearLayout results) {
        if (query.length() < 2) {
            Toast.makeText(this, "Enter at least 2 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        results.removeAllViews();
        addField(results, text("Searching...", 13, secondaryTextColor(), Typeface.NORMAL));

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(token -> network.execute(() -> {
            String response = fetchAdminSearch(query, token.getToken());
            runOnUiThread(() -> {
                results.removeAllViews();
                if (response == null) {
                    addField(results, text("Search unavailable.", 14, secondaryTextColor(), Typeface.NORMAL));
                    return;
                }
                try {
                    JSONArray users = new JSONArray(response);
                    boolean phoneSearch = query.matches("[+0-9()\\s-]+");
                    String phoneDigits = query.replaceAll("\\D", "");
                    if (phoneSearch && phoneDigits.isEmpty()) {
                        addField(results, text("Enter a valid phone number or username.", 14, secondaryTextColor(), Typeface.NORMAL));
                        return;
                    }
                    String usernameQuery = query.startsWith("@") ? query.substring(1) : query;
                    Map<String, JSONObject> matchingUsers = new LinkedHashMap<>();
                    for (int index = 0; index < users.length(); index++) {
                        JSONObject userResult = users.optJSONObject(index);
                        JSONObject user = userResult == null ? null : userResult.optJSONObject("user");
                        if (user == null) continue;

                        String uid = user.isNull("uid") ? "" : user.optString("uid", "");
                        String mobile = user.isNull("mobile") ? "" : user.optString("mobile", "");
                        String username = user.isNull("username") ? "" : user.optString("username", "");
                        String mobileDigits = normalizeIndianMobileDigits(mobile);
                        boolean matches;
                        String resultKey;
                        if (phoneSearch) {
                            matches = phoneDigits.length() >= 10
                                    ? mobileDigits.equals(phoneDigits.substring(phoneDigits.length() - 10))
                                    : mobileDigits.contains(phoneDigits);
                            resultKey = mobileDigits.isEmpty() ? uid : mobileDigits;
                        } else {
                            matches = username.toLowerCase(Locale.ROOT)
                                    .contains(usernameQuery.toLowerCase(Locale.ROOT));
                            resultKey = uid;
                        }
                        if (!matches || resultKey.isEmpty()) continue;

                        JSONObject previous = matchingUsers.get(resultKey);
                        if (previous == null || adminSubscriptionProfileRank(userResult)
                                > adminSubscriptionProfileRank(previous)) {
                            matchingUsers.put(resultKey, userResult);
                        }
                    }

                    if (matchingUsers.isEmpty()) {
                        addField(results, text("No matching users found.", 14, secondaryTextColor(), Typeface.NORMAL));
                        return;
                    }

                    for (JSONObject result : matchingUsers.values()) {
                        JSONObject user = result.optJSONObject("user");
                        if (user == null) continue;
                        boolean verified = user.optBoolean("mobile_verified", false)
                                || user.optBoolean("email_verified", false)
                                || user.optBoolean("is_verified", false);

                        String userId = user.optString("uid", "");
                        boolean paidSubscriber = user.optBoolean("subscription_active", false);
                        String fullName = user.isNull("full_name") ? "" : user.optString("full_name", "");
                        String mobile = user.isNull("mobile") ? "" : user.optString("mobile", "");
                        String username = user.isNull("username") ? "" : user.optString("username", "");
                        String displayName = !fullName.isEmpty() ? fullName
                                : !username.isEmpty() ? username : "User";
                        String label = displayName + (username.isEmpty() ? "" : " · @" + username)
                                + (mobile.isEmpty() ? "" : " · " + mobile)
                                + (verified ? " · Verified" : " · Not verified");

                        TextView userRow = text(label, 14, primaryTextColor(), Typeface.NORMAL);
                        userRow.setPadding(dp(12), dp(12), dp(12), dp(12));
                        userRow.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
                        if (verified && !userId.isEmpty()) {
                            userRow.setOnClickListener(view -> showAdminSubscriptionActionDialog(
                                    userId, label, paidSubscriber, token.getToken()));
                        } else {
                            userRow.setAlpha(0.65f);
                        }
                        addField(results, userRow);
                    }

                    if (results.getChildCount() == 0) {
                        addField(results, text("No matching users found.", 14, secondaryTextColor(), Typeface.NORMAL));
                    }
                } catch (Exception ignored) {
                    addField(results, text("Search failed. Try again.", 14, secondaryTextColor(), Typeface.NORMAL));
                }
            });
        }));
    }

    private int adminSubscriptionProfileRank(JSONObject result) {
        JSONObject user = result.optJSONObject("user");
        if (user == null) return Integer.MIN_VALUE;
        int rank = 0;
        if (user.optBoolean("mobile_verified", false) || user.optBoolean("email_verified", false)) rank += 16;
        if (!user.isNull("username") && !user.optString("username", "").isEmpty()) rank += 8;
        if (!user.isNull("full_name") && !user.optString("full_name", "").isEmpty()) rank += 4;
        if (!user.isNull("email") && !user.optString("email", "").isEmpty()) rank += 2;
        if (user.optBoolean("subscription_active", false)) rank += 1;
        return rank;
    }

    private void showAdminSubscriptionActionDialog(String userId, String label, boolean paidSubscriber, String idToken) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(20), dp(22), dp(20));
        root.setBackground(roundWithStroke(surfaceColor(), 24, borderColor()));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(paidSubscriber ? "Paid subscriber" : "Unpaid subscriber",
            18, primaryTextColor(), Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));

        ImageView closeButton = new ImageView(this);
        closeButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setColorFilter(primaryTextColor());
        closeButton.setPadding(dp(8), dp(8), dp(8), dp(8));
        closeButton.setBackground(roundWithStroke(backgroundColor(), 18, borderColor()));
        closeButton.setContentDescription("Close");
        closeButton.setOnClickListener(view -> dialog.dismiss());
        header.addView(closeButton, new LinearLayout.LayoutParams(dp(38), dp(38)));
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView details = text("Manage paid access for " + label, 14, secondaryTextColor(), Typeface.NORMAL);
        details.setPadding(0, dp(12), 0, dp(20));
        root.addView(details, new LinearLayout.LayoutParams(-1, -2));

        String action = paidSubscriber ? "cancel" : "add";
        String actionLabel = paidSubscriber ? "Cancel subscription" : "Add subscription";
        TextView subscriptionButton = actionButton(actionLabel, true);
        subscriptionButton.setOnClickListener(view -> {
            dialog.dismiss();
            updateAdminSubscriptionOverride(userId, action, idToken);
        });
        root.addView(subscriptionButton, new LinearLayout.LayoutParams(-1, dp(48)));

        dialog.setContentView(root);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
    }

    private void updateAdminSubscriptionOverride(String userId, String action, String idToken) {
        final String encodedUid = Uri.encode(userId);
        final String body = "{\"action\":\"" + action + "\"}";
        network.execute(() -> {
            boolean success = postAuthorized("/api/admin/users/" + encodedUid + "/subscription", idToken, body);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "Subscription " + ("add".equals(action) ? "added" : "cancelled") + ".", Toast.LENGTH_LONG).show();
                    showAdminSubscriptionOverridePage();
                } else {
                    Toast.makeText(this, "Unable to update subscription.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void showHowToReportDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(24), dp(24), dp(20));
        form.setBackground(roundWithStroke(surfaceColor(), 26, borderColor()));

        String titleStr = translateHowToReportTitle();
        String guideText = translateHowToReportText();

        TextView title = text(titleStr, 18, primaryTextColor(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(16));
        form.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView body = text(guideText, 13, secondaryTextColor(), Typeface.NORMAL);
        body.setPadding(0, 0, 0, dp(20));
        form.addView(body, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView close = text(translate("Got it"), 14, GOLD_ON, Typeface.NORMAL);
        close.setGravity(Gravity.CENTER);
        close.setBackground(goldButton());
        close.setOnClickListener(view -> {
            stopGuideSpeech();
            dialog.dismiss();
        });
        actionsRow.addView(close, new LinearLayout.LayoutParams(0, dp(44), 1f));

        ImageView voiceButton = new ImageView(this);
        voiceButton.setImageResource(android.R.drawable.ic_media_play);
        voiceButton.setColorFilter(primaryTextColor());
        voiceButton.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        voiceButton.setPadding(dp(10), dp(10), dp(10), dp(10));
        voiceButton.setContentDescription("Listen to guide");
        voiceButton.setOnClickListener(v -> speakOrStop(titleStr + ". " + guideText, voiceButton));
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        voiceParams.setMargins(dp(8), 0, 0, 0);
        actionsRow.addView(voiceButton, voiceParams);

        form.addView(actionsRow, new LinearLayout.LayoutParams(-1, -2));

        dialog.setContentView(form);
        dialog.setOnDismissListener(d -> stopGuideSpeech());
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels - dp(36), dp(360)), -2);
        }
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
        LanguageManager.setAppLanguage(this, languageCode);
        recreate();
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

        TextView title = text(localized("language"), 20, primaryTextColor(), Typeface.NORMAL);
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

            TextView label = text(languages[index][0], languages[index][0].length() > 6 ? 14 : 16, primaryTextColor(), Typeface.NORMAL);
            label.setIncludeFontPadding(false);
            label.setSingleLine(true);
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
        int sliderTrackColor = darkMode ? Color.WHITE : accentColor();
        slider.setProgressTintList(ColorStateList.valueOf(sliderTrackColor));
        slider.setProgressBackgroundTintList(ColorStateList.valueOf(sliderTrackColor));
        slider.setThumbTintList(ColorStateList.valueOf(accentColor()));
        slider.setMax(20);
        slider.setProgress(Math.round((current - 1.0f) * 100));
        content.addView(value);
        content.addView(slider);
        final boolean[] applied = {false};
        View mainContentView = findViewById(android.R.id.content);
        final View liveScaleTarget = mainContentView != null ? mainContentView : (activeContent != null ? activeContent : getWindow().getDecorView());
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                value.setText(localizedTextSize(100 + progress));
                applyLiveFontScale(liveScaleTarget, 1.0f + progress / 100.0f);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(8), 0, 0);
        TextView cancelButton = text("Cancel", 14, darkMode ? Color.WHITE : Color.BLACK, Typeface.NORMAL);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setMinWidth(dp(92));
        cancelButton.setMinHeight(dp(44));
        cancelButton.setPadding(dp(14), 0, dp(14), 0);
        cancelButton.setBackground(roundWithStroke(surfaceColor(), 10, fieldBorderColor()));
        TextView applyButton = text("Apply", 14, darkMode ? Color.BLACK : Color.WHITE, Typeface.NORMAL);
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
        if (fontDialog.getWindow() != null) {
            fontDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            fontDialog.getWindow().setGravity(Gravity.CENTER);
        }
        applyButton.setOnClickListener(view -> {
            float scale = 1.0f + slider.getProgress() / 100.0f;
            getSharedPreferences("fendly_settings", MODE_PRIVATE).edit().putFloat("font_scale", scale).apply();
            applied[0] = true;
            fontDialog.dismiss();
        });
        cancelButton.setOnClickListener(view -> fontDialog.dismiss());
        fontDialog.show();
        fontDialog.setOnDismissListener(dialog -> {
            if (!applied[0]) {
                applyLiveFontScale(liveScaleTarget, current);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void applyLiveFontScale(View view, float scale) {
        if (view instanceof TextView) {
            float baseSize = 14f;
            Object originalSize = view.getTag();
            if (originalSize instanceof Float) {
                baseSize = (Float) originalSize;
            } else {
                float currentSize = ((TextView) view).getTextSize() / getResources().getDisplayMetrics().scaledDensity;
                if (currentSize > 0) {
                    baseSize = Math.max(10f, Math.min(28f, currentSize));
                    view.setTag(Float.valueOf(baseSize));
                }
            }
            ((TextView) view).setTextSize(baseSize * scale);
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
                "Home", "Find what matters.", "Lost or Found? Will Connect the Dots..", "LOST", "FOUND", "My reports", "My profile",
                "Post found item", "Report lost item", "Help it get home.", "Let's find it.", "Add clear details so the right person can recognise it.",
                "Item name", "Description and identifying details", "Location or landmark", "Date and time", "Upload item image", "Image selected",
                "Take photo with camera", "Use current location", "Publish found item", "Publish lost item", "Fendly Plus",
                "Unlock lost-item submissions.", "Found-item reports stay free forever. Lost-item submissions are Rs 99 per year.",
                "Pay and submit lost report", "Back to report", "Your reports", "Keep track of items you are helping to reunite.", "Back home",
                "Dummy user", "Your account details and preferences.", "Admin dashboard", "Private moderation workspace", "English only · confidential user details",
                "Loading live admin data...", "Review match", "Confirm and notify owner", "Exit admin"
        };
        String[][] translations = {
                english, // 0: en
                {"अपनी प्रोफ़ाइल पूरी करें", "आपके बारे में थोड़ा", "इससे पड़ोसियों को पता चलेगा कि वे किसकी मदद कर रहे हैं।", "सहेजें और जारी रखें", "पिन से लॉगिन", "वापसी पर स्वागत है।", "अपने Fendly उपयोगकर्ता नाम और पिन का उपयोग करें।", "लॉगिन", "होम", "जो महत्वपूर्ण है उसे खोजें।", "पास में कुछ खोया? कुछ मिला? यहां से शुरू करें।", "खोया", "मिला", "मेरी रिपोर्ट", "मेरी प्रोफ़ाइल", "मिली वस्तु पोस्ट करें", "खोई वस्तु रिपोर्ट करें", "इसे घर पहुंचाने में मदद करें।", "आइए इसे खोजें।", "स्पष्ट विवरण जोड़ें ताकि सही व्यक्ति पहचान सके।", "वस्तु का नाम", "विवरण और पहचान की जानकारी", "स्थान या पहचान चिन्ह", "दिनांक और समय", "वस्तु की तस्वीर अपलोड करें", "तस्वीर चुनी गई", "कैमरे से तस्वीर लें", "वर्तमान स्थान उपयोग करें", "मिली वस्तु प्रकाशित करें", "खोई वस्तु प्रकाशित करें", "Fendly Plus", "खोई वस्तु की रिपोर्ट अनलॉक करें।", "मिली वस्तु की रिपोर्ट हमेशा निःशुल्क है। खोई वस्तु की रिपोर्ट Rs 99 प्रति वर्ष है।", "भुगतान करें और खोई रिपोर्ट भेजें", "रिपोर्ट पर वापस जाएं", "मेरी रिपोर्ट", "जिन वस्तुओं को मिलाने में मदद कर रहे हैं उनका रिकॉर्ड रखें।", "होम पर वापस जाएं", "डमी उपयोगकर्ता", "आपके खाते का विवरण और प्राथमिकताएं।", "एडमिन डैशबोर्ड", "निजी मॉडरेशन कार्यक्षेत्र", "केवल अंग्रेज़ी · गोपनीय उपयोगकर्ता विवरण", "लाइव एडमिन डेटा लोड हो रहा है...", "मिलान देखें", "पुष्टि करें और मालिक को सूचित करें", "एडमिन से बाहर निकलें"}, // 1: hi
                {"पूर्ण प्रोफाइल", "तुमच्याबद्दल थोडे", "यामुळे शेजाऱ्यांना ते कोणाला मदत करत आहेत हे समजेल.", "जतन करा आणि पुढे जा", "पिनने लॉगिन", "पुन्हा स्वागत आहे.", "तुमचे Fendly वापरकर्तानाव आणि पिन वापरा.", "लॉगिन", "मुख्यपृष्ठ", "महत्त्वाचे शोधा.", "जवळ काही हरवले? काही सापडले? इथून सुरुवात करा.", "हरवले", "सापडले", "माझे अहवाल", "माझे प्रोफाइल", "सापडलेली वस्तू पोस्ट करा", "हरवलेली वस्तू नोंदवा", "ते घरी पोहोचवण्यास मदत करा.", "चला ते शोधूया.", "योग्य व्यक्ती ओळखू शकेल असे स्पष्ट तपशील जोडा.", "वस्तूचे नाव", "वर्णन आणि ओळख तपशील", "ठिकाण किंवा खूण", "दिनांक आणि वेळ", "वस्तूचा फोटो अपलोड करा", "फोटो निवडला", "कॅमेऱ्याने फोटो घ्या", "सध्याचे स्थान वापरा", "सापडलेली वस्तू प्रकाशित करा", "हरवलेली वस्तू प्रकाशित करा", "Fendly Plus", "हरवलेल्या वस्तूंचे अहवाल सुरू करा.", "सापडलेल्या वस्तूंचे अहवाल कायम विनामूल्य आहेत. हरवलेल्या वस्तूंचे अहवाल वर्षाला Rs 99 आहेत.", "भरणा करून हरवलेला अहवाल पाठवा", "अहवालाकडे परत जा", "माझे अहवाल", "तुम्ही पुन्हा जोडण्यास मदत करत असलेल्या वस्तूंचा मागोवा ठेवा.", "मुख्यपृष्ठावर परत जा", "डमी वापरकर्ता", "तुमच्या खात्याचे तपशील आणि प्राधान्ये.", "अॅडमिन डॅशबोर्ड", "खासगी मॉडरेशन कार्यक्षेत्र", "फक्त इंग्रजी · गोपनीय वापरकर्ता तपशील", "लाइव्ह अॅडमिन डेटा लोड होत आहे...", "जुळणी पाहा", "पुष्टी करून मालकाला कळवा", "अॅडमिनमधून बाहेर पडा"}, // 2: mr
                {"તમારી પ્રોફાઇલ પૂર્ણ કરો", "તમારા વિશે થોડું", "આ પડોશીઓને જાણવામાં મદદ કરે છે કે તેઓ કોને મદદ કરી રહ્યા છે.", "સાચવો અને આગળ વધો", "PIN વડે લોગિન", "પાછા સ્વાગત છે.", "તમારી ફ્રેન્ડલી પ્રોફાઇલમાંથી યુઝરનામ અને PIN નો ઉપયોગ કરો.", "લોગિન", "હોમ", "જે મહત્ત્વનું છે તે શોધો.", "આસપાસ કંઈ ખોવાયું? કંઈ મળ્યું? અહીંથી શરૂ કરો.", "ખોવાયેલ", "મળેલ", "મારા અહેવાલો", "મારી પ્રોફાઇલ", "મળેલ વસ્તુ પોસ્ટ કરો", "ખોવાયેલ વસ્તુની જાણ કરો", "તેને ઘરે પહોંચાડવામાં મદદ કરો.", "ચાલો તે શોધીએ.", "સ્પષ્ટ વિગતો ઉમેરો જેથી સાચી વ્યક્તિ તેને ઓળખી શકે.", "વસ્તુનું નામ", "વર્ણન અને ઓળખની વિગતો", "સ્થળ અથવા ઓળખચિહ્ન", "તારીખ અને સમય", "વસ્તુની છબી અપલોડ કરો", "છબી પસંદ કરી", "કેમેરાથી ફોટો લો", "વર્તમાન સ્થાનનો ઉપયોગ કરો", "મળેલ વસ્તુ પ્રકાશિત કરો", "ખોવાયેલ વસ્તુ પ્રકાશિત કરો", "Fendly Plus", "ખોવાયેલ વસ્તુના રિપોર્ટ અનલોક કરો.", "મળેલ વસ્તુના રિપોર્ટ હંમેશા મફત છે. ખોવાયેલ વસ્તુના રિપોર્ટ વર્ષે Rs 99 છે.", "ચૂકવણી કરો અને ખોવાયેલ રિપોર્ટ સબમિટ કરો", "રિપોર્ટ પર પાછા જાઓ", "તમારા અહેવાલો", "તમે જેને ફરી મેળવવામાં મદદ કરી રહ્યા છો તે વસ્તુઓનો ટ્રેક રાખો.", "હોમ પર પાછા જાઓ", "ડમી યુઝર", "તમારા ખાતાની વિગતો અને પસંદગીઓ.", "એડમિન ડેશબોર્ડ", "ખાનગી મોડરેશન વર્કસ્પેસ", "માત્ર અંગ્રેજી · ગોપનીય વપરાશકર્તા વિગતો", "લાઈવ એડમિન ડેટા લોડ થઈ રહ્યો છે...", "મેચ સમીક્ષા કરો", "પુષ્ટિ કરો અને માલિકને જાણ કરો", "એડમિનમાંથી બહાર નીકળો"}, // 3: gu
                {"আপনার প্রোফাইল সম্পূর্ণ করুন", "আপনার সম্পর্কে কিছু", "এটি প্রতিবেশীদের জানতে সাহায্য করে তারা কার সাহায্য করছে।", "সংরক্ষণ করুন এবং চালিয়ে যান", "পিন দিয়ে লগইন", "ফিরে আসার জন্য স্বাগতম।", "আপনার Fendly ব্যবহারকারীর নাম এবং পিন ব্যবহার করুন।", "লগইন", "হোম", "গুরুত্বপূর্ণ জিনিস খুঁজুন।", "কাছাকাছি কিছু হারিয়েছে? কিছু পেয়েছেন? এখান থেকে শুরু করুন।", "হারিয়ে গেছে", "পাওয়া গেছে", "আমার রিপোর্ট", "আমার প্রোফাইল", "পাওয়া আইটেম পোস্ট করুন", "হারানো আইটেম রিপোর্ট করুন", "এটিকে বাড়িতে ফিরিয়ে দিতে সাহায্য করুন।", "চলো এটি খুঁজে বের করি।", "সঠিক ব্যক্তি শনাক্ত করার জন্য স্পষ্ট বিবরণ যোগ করুন।", "আইটেমের নাম", "বর্ণনা ও শনাক্তকরণ তথ্য", "অবস্থান বা চিহ্ন", "তারিখ ও সময়", "আইটেমের ছবি আপলোড করুন", "চিত্র নির্বাচন করা হয়েছে", "ক্যামেরা দিয়ে ছবি নিন", "বর্তমান অবস্থান ব্যবহার করুন", "পাওয়া আইটেম প্রকাশ করুন", "হারানো আইটেম প্রকাশ করুন", "Fendly Plus", "হারানো আইটেম রিপোর্ট আনলক করুন।", "পাওয়া আইটেম রিপোর্ট সবসময় বিনামূল্যে। হারানো আইটেম রিপোর্ট বছরে Rs 99।", "পেমেন্ট করুন এবং হারানো রিপোর্ট জমা দিন", "রিপোর্টে ফিরে যান", "আমার রিপোর্ট", "আপনি কী কী আইটেম আবার একত্রিত করতে সাহায্য করছেন তার রেকর্ড রাখুন।", "হোমে ফিরে যান", "ডামি ব্যবহারকারী", "আপনার অ্যাকাউন্টে বিবরণ ও পছন্দসমূহ।", "অ্যাডমিন ড্যাশবোর্ড", "ব্যক্তিগত মডারেশন ওয়ার্কস্পেস", "শুধু ইংরেজি · গোপন ব্যবহারকারী বিবরণ", "লাইভ অ্যাডমিন ডেটা লোড হচ্ছে...", "ম্যাচ দেখুন", "নিশ্চিত করুন এবং মালিককে অবহিত করুন", "অ্যাডমিন থেকে বের হন"}, // 4: bn
                {"உங்கள் சுயவிவரத்தை முழுமையாக்குங்கள்", "உங்களைப் பற்றி சிறிது", "இது யாருக்கு உதவுகிறார்கள் என்பதை அண்டை வீட்டாருக்குத் தெரியப்படுத்த உதவுகிறது.", "சேமித்து தொடரவும்", "PIN மூலம் உள்நுழைவு", "மீண்டும் வருக.", "உங்கள் Fendly சுயவிவரத்தின் பயனர்பெயர் மற்றும் PIN ஐப் பயன்படுத்தவும்.", "உள்நுழை", "முகப்பு", "முக்கியமானவற்றைக் கண்டறியவும்.", "அருகில் தொலைந்ததா? ஏதேனும் கிடைத்ததா? இங்கிருந்து தொடங்குங்கள்.", "தொலைந்தது", "கிடைத்தது", "என் அறிக்கைகள்", "என் சுயவிவரம்", "கிடைத்த பொருளைப் பதிவிடவும்", "தொலைந்த பொருளை அறிக்கை செய்யவும்", "அதை வீட்டிற்குச் சேர்க்க உதவுங்கள்.", "அதைக் கண்டுபிடிப்போம்.", "சரியான நபர் அடையாளம் காண தெளிவான விவரங்களைச் சேர்க்கவும்.", "பொருளின் பெயர்", "விளக்கம் மற்றும் அடையாள விவரங்கள்", "இடம் அல்லது அடையாளம்", "தேதி மற்றும் நேரம்", "பொருளின் படத்தைப் பதிவேற்றவும்", "படம் தேர்ந்தெடுக்கப்பட்டது", "கேமரா மூலம் படம் எடுக்கவும்", "தற்போதைய இடத்தைப் பயன்படுத்தவும்", "கிடைத்த பொருளை வெளியிடுங்கள்", "தொலைந்த பொருளை வெளியிடுங்கள்", "Fendly Plus", "தொலைந்த பொருள் அறிக்கைகளைத் திறக்கவும்.", "கிடைத்த பொருள் அறிக்கைகள் எப்போதும் இலவசம். தொலைந்த பொருள் அறிக்கைகள் ஆண்டிற்கு Rs 99.", "பணம் செலுத்தி அறிக்கையைச் சமர்ப்பிக்கவும்", "அறிக்கைக்குத் திரும்புக", "உங்கள் அறிக்கைகள்", "நீங்கள் மீண்டும் சேர்க்க உதவும் பொருட்களைக் கண்காணிக்கவும்.", "முகப்பிற்குத் திரும்புக", "மாதிரி பயனர்", "உங்கள் கணக்கு விவரங்கள் மற்றும் முன்னுரிமைகள்.", "நிர்வாகி குழு", "தனியார் மிதமான பணிப்பகுதி", "ஆங்கிலம் மட்டும் · இரகசிய பயனர் விவரங்கள்", "நிர்வாகி தரவு ஏற்றப்படுகிறது...", "பொருத்தத்தை மதிப்பாய்வு செய்க", "உறுதிசெய்து உரிமையாளருக்கு அறிவிக்கவும்", "நிர்வாகியிலிருந்து வெளியேறு"}, // 5: ta
                {"మీ ప్రొఫైల్‌ను పూర్తి చేయండి", "మీ గురించి కొద్దిపాటి సమాచారం", "ఇది పొరుగు వారికి ఎవరికి సహాయం చేస్తున్నారో తెలుసుకోవడంలో సహాయపడుతుంది.", "సేవ్ చేసి కొనసాగించండి", "పిన్‌తో లాగిన్", "మళ్ళీ స్వాగతం", "మీ Fendly వినియోగదారు పేరు మరియు పిన్‌ను ఉపయోగించండి.", "లాగిన్", "హోమ్", "ముఖ్యమైన వాటిని కనుగొనండి.", "ఇక్కడకు దగ్గరలో ఏదైనా పోయిందా? ఏదైనా దొరికిందా? ఇక్కడ ప్రారంభించండి.", "కోల్పోయినవి", "కనుగొన్నది", "నా రిపోర్ట్లు", "నా ప్రొఫైల్", "కనుగొన్న అంశాన్ని పోస్ట్ చేయండి", "కోల్పోయిన అంశాన్ని రిపోర్ట్ చేయండి", "దానిని ఇంటికి చేర్చడానికి సహాయం చేయండి.", "వెతుకుదాం.", "సరైన వ్యక్తి గుర్తించగలిగే స్పష్టమైన వివరాలను జోడించండి.", "అంశం పేరు", "వివరణ మరియు గుర్తింపు వివరాలు", "స్థలం లేదా పరిశీలన", "తేదీ మరియు సమయం", "అంశపు ఫోటో అప్లోడ్ చేయండి", "ఫోటో ఎంపికైంది", "కెమెరా నుండి ఫోటో తీయండి", "ప్రస్తుత స్థానం ఉపయోగించండి", "కనుగొన్న అంశాన్ని ప్రచురించండి", "కోల్పోయిన అంశాన్ని ప్రచురించండి", "Fendly Plus", "కోల్పోయిన వస్తువుల రిపోర్ట్లను అన్‌లాక్ చేయండి.", "కనుగొన్న వస్తువుల రిపోర్ట్లు ఎల్లప్పుడూ ఉచితం. కోల్పోయిన వస్తువుల రిపోర్ట్లు సంవత్సరానికి Rs 99.", "చెల్లించి కోల్పోయిన రిపోర్టును సమర్పించండి", "రిపోర్టుకు తిరిగి వెళ్లండి", "నా రిపోర్ట్లు", "మీరు పునరుద్ధరించడానికి సహాయం చేస్తున్న వస్తువుల రికార్డ్‌ను పర్యవేక్షించండి.", "హోమ్కి తిరిగి వెళ్లండి", "డమ్మీ యూజర్", "మీ అకౌంట్ వివరాలు మరియు ప్రాధాన్యతలు.", "అడ్మిన్ డాష్‌బోర్డ్", "ప్రైవేట్ మోడరేషన్ వర్క్‌స్పేస్", "ఇంగ్లీష్ మాత్రమే · గోప్య వినియోగదారు వివరాలు", "లైవ్ అడ్మిన్ డేటాను లోడ్ చేస్తున్నారు...", "మ్యాచ్ చూసుకోండి", "నిర్ధారించండి మరియు యజమానికి తెలియజేయండి", "అడ్మిన్ నుండి నిష్క్రమించండి"}, // 6: te
                {"ನಿಮ್ಮ ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಳಿಸಿ", "ನಿಮ್ಮ ಬಗ್ಗೆ ಸ್ವಲ್ಪ ಮಾಹಿತಿ", "ಇದು ನೆರವಿನವರನ್ನು ಯಾರು ಸಹಾಯ ಮಾಡುತ್ತಿದ್ದಾರೆಂದು ತಿಳಿಸಲು ಸಹಾಯ ಮಾಡುತ್ತದೆ.", "ಸೇವ್ ಮಾಡಿ ಮತ್ತು ಮುಂದುವರಿಸಿ", "ಪಿನ್ ಮೂಲಕ ಲಾಗಿನ್", "ಮರಳಿ ಸ್ವಾಗತ", "ನಿಮ್ಮ Fendly ಬಳಕೆದಾರಹೆಸರು ಮತ್ತು ಪಿನ್ ಬಳಸಿ.", "ಲಾಗಿನ್", "ಹೋಮ್", "ಪ್ರಮುಖವಾದ್ದನ್ನು ಹುಡುಕಿ.", "ಹತ್ತಿರದಲ್ಲಿ ಯಾವುದೋ ಕಳೆದುಹೋಗಿದೆಯೇ? ಏನಾದರೂ ಸಿಕ್ಕಿದೆಯೇ? ಇಲ್ಲಿಂದ ಪ್ರಾರಂಭಿಸಿ.", "ಕಳೆದುಹೋಗಿದೆ", "ಸಿಕ್ಕಿದೆ", "ನನ್ನ ವರದಿಗಳು", "ನನ್ನ ಪ್ರೊಫೈಲ್", "ಕಂಡ ವಸ್ತು ಪೋಸ್ಟ್ ಮಾಡಿ", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿ ಮಾಡಿ", "ಅದನ್ನು ಮನೆಗೆ ಸೇರಿಸಲು ಸಹಾಯ ಮಾಡಿ.", "ಮುತ್ತಲಿನವರೊಂದಿಗೆ ಹುಡುಕೋಣ.", "ಸರಿಯಾದ ವ್ಯಕ್ತಿ ಗುರುತಿಸಿಕೊಳ್ಳಲು ಸ್ಪಷ್ಟ ವಿವರಗಳನ್ನು ಸೇರಿಸಿ.", "ವಸ್ತುವಿನ ಹೆಸರು", "ವಿವರಣೆ ಮತ್ತು ಗುರುತಿಸುವ ವಿವರಗಳು", "ಸ್ಥಳ ಅಥವಾ ಗುರುತು", "ದಿನಾಂಕ ಮತ್ತು ಸಮಯ", "ವಸ್ತುವಿನ ಚಿತ್ರ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ", "ಚಿತ್ರ ಆಯ್ಕೆಮಾಡಲಾಗಿದೆ", "ಕ್ಯಾಮರಾದಿಂದ ಫೋಟೋ ತೆಗೆದುಕೊಳ್ಳಿ", "ಪ್ರಸ್ತುತ ಸ್ಥಳವನ್ನು ಬಳಸಿ", "ಕಂಡ ವಸ್ತು ಪ್ರಕಟಿಸಿ", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ಪ್ರಕಟಿಸಿ", "Fendly Plus", "ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿಗಳನ್ನು desbloಕ್ ಮಾಡಿ.", "ಕಂಡ ವಸ್ತು ವರದಿಗಳು ಶಾಶ್ವತವಾಗಿ ಉಚಿತವಾಗಿರುತ್ತವೆ. ಕಳೆದುಹೋಗಿದ ವಸ್ತು ವರದಿಗಳು ವರ್ಷಕ್ಕೆ Rs 99.", "ಚెలಾಯಿಸಿ ಮತ್ತು ಕಳೆದುಹೋಗಿದ ವರದಿಯನ್ನು ಸಲ್ಲಿಸಿ", "ವರದಿಗೆ ಹಿಂತಿರುಗಿ", "ನನ್ನ ವರದಿಗಳು", "ನೀವು ಒಟ್ಟುಗೂಡಿಸಲು ಸಹಾಯ ಮಾಡುವ ವಸ್ತುಗಳ ರೆಕಾರ್ಡ್ ಅನ್ನು ನಿರ್ವಹಿಸಿ.", "ಮನೆಯತ್ತ ಹಿಂತಿರುಗಿ", "ಡಮ್ಮি ಬಳಕೆದಾರ", "ನಿಮ್ಮ ಖಾತೆ ವಿವರಗಳು ಮತ್ತು ಆದ್ಯತೆಗಳು.", "ಅಡ್ಮಿನ್ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್", "ಖಾಸಗಿ मॉಡರೇಶನ್ ಕಾರ್ಯಕ್ಷೇತ್ರ", "ಇಂಗ್ಲಿಷ್ ಮಾತ್ರ · ರಹಸ್ಯ ಬಳಕೆದಾರ ವಿವರಗಳು", "ಲೈವ್ ಅಡ್ಮಿನ್ ಡೇಟಾವನ್ನು ಲೋಡ್ ಮಾಡಲಾಗುತ್ತಿದೆ...", "ಪಂದ್ಯವನ್ನು ವೀಕ್ಷಿಸಿ", "ನಿಶ್ಚಿತಪಡಿಸಿ ಮತ್ತು ಮಾಲೀಕನಿಗೆ ತಿಳಿಸಿ", "ಅಡ್ಮಿನ್ ನಿಂದ ನಿರ್ಗಮಿಸಿ"}, // 7: kn
                {"നിങ്ങളുടെ പ്രൊഫൈൽ പൂർത്തിയാക്കുക", "നിങ്ങളെക്കുറിച്ച് കുറച്ച്", "ഇത് അയൽവാസികൾക്ക് ആരെ സഹായിക്കുന്നുവെന്ന് അറിയാൻ സഹായിക്കുന്നു.", "സേവ് ചെയ്ത് മുന്നോട്ട് പോകുക", "പിൻ ഉപയോഗിച്ച് ലോഗിൻ", "വീണ്ടും സ്വാഗതം", "നിങ്ങളുടെ Fendly യൂസർനെയം, പിൻ ഉപയോഗിക്കുക.", "ലോഗിൻ", "ഹോം", "പ്രധാനമായ വസ്തുക്കൾ കണ്ടെത്തുക.", "സമീപത്ത് തന്നെ നഷ്ടപ്പെട്ടോ? എന്തെങ്കിലും കണ്ടെത്തിയോ? ഇവിടെ ആരംഭിക്കുക.", "നഷ്ടപ്പെട്ടു", "കണ്ടെത്തി", "എന്റെ റിപ്പോർട്ടുകൾ", "എന്റെ പ്രൊഫൈൽ", "കണ്ടെത്തിയ ഇനം പോസ്റ്റുചെയ്യുക", "നഷ്ടപ്പെട്ട ഇനം റിപ്പോർട്ട് ചെയ്യുക", "അത് വീട്ടിലേക്ക് എത്തിക്കാൻ സഹായിക്കുക.", "കണ്ടുപിടിക്കാം.", "ശരിയായ വ്യക്തിയെ തിരിച്ചറിയാൻ വ്യക്തമായ വിശദാംശങ്ങൾ ചേർക്കുക.", "ഇനത്തിന്റെ പേര്", "വിവരണം, തിരിച്ചറിയൽ വിശദാംശങ്ങൾ", "സ്ഥലം അല്ലെങ്കിൽ അടയാളം", "തീയതിയും സമയവും", "ഇനത്തിന്റെ ചിത്രം അപ്‌ಲೋഡ് ചെയ്യുക", "ചിത്രം തിരഞ്ഞെടുത്തു", "ക്യാമറയിൽ നിന്ന് ഫോട്ടോ എടുക്കുക", "നിലവിലെ സ്ഥലം ഉപയോഗിക്കുക", "കണ്ടെത്തിയ ഇനം പ്രസിദ്ധീകരിക്കുക", "നഷ്ടപ്പെട്ട ഇനം പ്രസിദ്ധീകരിക്കുക", "Fendly Plus", "നഷ്ടപ്പെട്ട ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ അൺലോക്ക് ചെയ്യുക.", "കണ്ടെത്തിയ ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ എല്ലായ്പ്പോഴും സൗജന്യമാണ്. നഷ്ടപ്പെട്ട ഇനങ്ങളുടെ റിപ്പോർട്ടുകൾ പ്രതിവർഷം Rs 99.", "പേയ്‌മെന്റ് ചെയ്യുകയും നഷ്ടപ്പെട്ട റിപ്പോർട്ട് സമർപ്പിക്കുകയും ചെയ്യുക", "റിപ്പോർട്ടിലേക്ക് തിരികെ പോകുക", "എന്റെ റിപ്പോർട്ടുകൾ", "നിങ്ങൾ വീണ്ടും കൂട്ടിച്ചേർക്കാൻ സഹായിക്കുന്ന ഇനങ്ങളുടെ രേഖ സൂക്ഷിക്കുക.", "ഹോമിലേക്ക് മടങ്ങുക", "ഡമ്മി ഉപയോക്താവ്", "നിങ്ങളുടെ അക്കൗണ്ട് വിശദാംശങ്ങളും മുൻഗണനകളും.", "അഡ്മിൻ ഡാഷ്‌ബോർഡ്", "സ്വകാര്യ മോഡറേഷൻ വർക്ക്‌സ്പേസ്", "ഇംഗ്ലീഷ് മാത്രം · രഹസ്യ ഉപയോക്തൃ വിശദാംശങ്ങൾ", "ലൈവ് അഡ്മിൻ ഡാറ്റ ലോഡ് ചെയ്യുകയാണ്...", "മാച്ച് കാണുക", "സ്ഥിരീകരിക്കുകയും ഉടമയെ അറിയിക്കുകയും ചെയ്യുക", "അഡ്മിനിൽ നിന്ന് പുറത്തുകടക്കുക"} // 8: ml
        };
        int language = Math.max(0, Math.min(selectedLanguage, translations.length - 1));
        for (int index = 0; index < english.length; index++) {
            if (english[index].equals(value)) return localizeDigits(translations[language][index]);
        }
        return localizeDigits(value);
    }

    private String translateUi(String value) {
        if (value == null) return null;
        if ("Got it".equalsIgnoreCase(value)) {
            String[] labels = {"Got it", "ठीक है", "समजले", "બરાબર", "বুঝেছি", "சரி", "సరే", "ಸರಿ", "ശരി"};
            int language = Math.max(0, Math.min(selectedLanguage, labels.length - 1));
            return labels[language];
        }
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
            if ("Subscription".equalsIgnoreCase(value)) return "વાર્ષિક સબ્સ્ક્રિપ્શન";
            if ("Continue to payment".equalsIgnoreCase(value)) return "ચુકવણી ચાલુ રાખો";
            if ("Save changes".equalsIgnoreCase(value)) return "ફેરફારો સાચવો";
            if ("Back home".equalsIgnoreCase(value)) return "હોમ પર પાછા જાઓ";
            if ("My reports".equalsIgnoreCase(value)) return "મારા રિપોર્ટ";
            if ("My profile".equalsIgnoreCase(value)) return "મારી પ્રોફાઇલ";
            if ("LOST".equalsIgnoreCase(value) || "Lost".equalsIgnoreCase(value)) return "ખોવાયેલ";
            if ("FOUND".equalsIgnoreCase(value) || "Found".equalsIgnoreCase(value)) return "મળેલ";
            if ("Report an item".equalsIgnoreCase(value) || "Click Lost/Found button to report".equalsIgnoreCase(value)) return "વસ્તુની રિપોર્ટ કરો";
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
        if ("Profile photo".equals(value)) {
            String[] text = {"Profile photo", "प्रोफ़ाइल फोटो", "प्रोफाइल फोटो", "پروفাইল تصویر", "ಪ್ರೊಫೈಲ್ ಫೋಟೋ", "ప్రొఫైల్ ఫోటో", "প্রোফাইল ছবি", "പ്രൊഫൈൽ ഫോട്ടോ"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        if ("See profile picture".equals(value)) {
            String[] text = {"See profile picture", "प्रोफ़ाइल पिक्चर देखें", "प्रोफाइल चित्र पहा", "پروفাইল تصویر دیکھیں", "ಪ್ರೊಫೈಲ್ ಚಿತ್ರವನ್ನು ನೋಡಿ", "ప్రొఫైల్ చిత్రాన్ని చూడండి", "প্রোফাইল ছবি দেখুন", "പ്രൊഫൈൽ ചിത്രം കാണുക"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        if ("Choose profile picture".equals(value)) {
            String[] text = {"Choose profile picture", "प्रोफ़ाइल पिक्चर चुनें", "प्रोफाइल चित्र निवडा", "پروفাইল تصویر منتخب کریں", "ಪ್ರೊఫೈಲ್ ಚಿತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ", "ప్రొఫైల్ చిత్రాన్ని ఎంచుకోండి", "প্রোফাইল ছবি নির্বাচন করুন", "പ്രൊഫൈൽ ചിത്രം തിരഞ്ഞെടുക്കുക"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        if ("Choose from gallery".equals(value)) {
            String[] text = {"Choose from gallery", "गैलरी से चुनें", "गॅलरीतून निवडा", "گیلری سے منتخب کریں", "ಗ್ಯಾಲರಿಯಿಂದ ಆಯ್ಕೆಮಾಡಿ", "గ్యాలరీ నుండి ఎంచుకోండి", "গ্যালারি থেকে বেছে নিন", "ഗാലറിയിൽ നിന്ന് തിരഞ്ഞെടുക്കുക"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
        }
        if ("Take photo".equals(value)) {
            String[] text = {"Take photo", "फोटो लें", "फोटो घ्या", "تصویر لیں", "ಫೋಟೋ ತೆಗೆದುಕೊಳ್ಳಿ", "ఫోటో తీయండి", "ছবি তুলুন", "ഫോട്ടോ എടുക്കുക"};
            return text[Math.max(0, Math.min(selectedLanguage, text.length - 1))];
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
                {"Subscription", "वार्षिक सदस्यता", "वार्षिक सदस्यत्व", "سالانہ رکنیت", "ವಾರ್ಷಿಕ ಚಂದಾದಾರಿಕೆ", "వార్షిక సభ్యత్వం", "বার্ষিক সাবস্ক্রিপশন", "വാർഷിക സബ്സ്ക്രിപ്ഷൻ"},
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
                {"Authentication unavailable.", "प्रमाणीकरण उपलब्ध नहीं है।", "प्रमाणीकरण उपलब्ध नाही.", "પ્રમાણીકરણ અનુપલબ્ધ છે.", "প্রমাণীকরণ উপলব্ধ নয়।", "அடையாள அங்கீகாரம் கிடைக்கவில்லை.", "ప్రామాణీకరణ అందుబాటులో లేదు.", "ಪ್ರಮಾಣೀಕರಣ ಲಭ್ಯವಿಲ್ಲ.", "ആധികാരികത ലഭ്യമല്ല."},
                {"Discover active reports", "सक्रिय रिपोर्ट खोजें", "सक्रिय अहवाल शोधा", "સક્રિય અહેવાલો શોધો", "সক্রিয় রিপোর্ট খুঁজুন", "செயலில் உள்ள புகார்களைக் கண்டறியுங்கள்", "యాక్టివ్ నివేదికలను కనుగొనండి", "ಸಕ್ರಿಯ ವರದಿಗಳನ್ನು ಅನ್ವೇಷಿಸಿ", "സജീവ റിപ്പോർട്ടുകൾ കണ്ടെത്തുക"},
                {"Filter reports by city", "शहर के अनुसार रिपोर्ट फ़िल्टर करें", "शहरानुसार अहवाल फिल्टर करा", "શહેર પ્રમાણે અહેવાલો ફિલ્ટર કરો", "শহর অনুযায়ী রিপোর্ট ফিল্টার করুন", "நகரத்தின் அடிப்படையில் புகார்களை வடிகட்டுங்கள்", "నగరం ఆధారంగా నివేదికలను ఫిల్టర్ చేయండి", "ನಗರದ ಪ್ರಕಾರ ವರದಿಗಳನ್ನು ಫಿಲ్టర్ ಮಾಡಿ", "നഗരം അനുസരിച്ച് റിപ്പോർട്ടുകൾ ഫിൽട്ടർ ചെയ്യുക"},
                {"Search any city or town in India", "भारत में कोई भी शहर या कस्बा खोजें", "भारतातील कोणतेही शहर किंवा गाव शोधा", "ભારતનું કોઈપણ શહેર અથવા નગર શોધો", "ভারতের যেকোনো শহর বা ছোট শহর খুঁজুন", "இந்தியாவில் உள்ள எந்த நகரம் அல்லது சிற்றூரையும் தேடுங்கள்", "భారతదేశంలోని ఏ నగరం లేదా పట్టణాన్నైనా వెతకండి", "ಭಾರತದ ಯಾವುದೇ ನಗರ ಅಥವಾ ಪಟ್ಟಣವನ್ನು ಹುಡುಕಿ", "ഇന്ത്യയിലെ ഏത് നഗരമോ പട്ടണമോ തിരയുക"},
                {"Search city", "शहर खोजें", "शहर शोधा", "શહેર શોધો", "শহর খুঁজুন", "நகரத்தைத் தேடுங்கள்", "నగరాన్ని వెతకండి", "ನಗರವನ್ನು ಹುಡುಕಿ", "നഗരം തിരയുക"},
                {"All cities", "सभी शहर", "सर्व शहरे", "બધા શહેરો", "সব শহর", "அனைத்து நகரங்களும்", "అన్ని నగరాలు", "ಎಲ್ಲಾ ನಗರಗಳು", "എല്ലാ നഗരങ്ങളും"},
                {"No active reports found.", "कोई सक्रिय रिपोर्ट नहीं मिली।", "कोणताही सक्रिय अहवाल आढळला नाही.", "કોઈ સક્રિય અહેવાલ મળ્યો નથી.", "কোনো সক্রিয় রিপোর্ট পাওয়া যায়নি।", "செயலில் உள்ள புகார்கள் எதுவும் இல்லை.", "యాక్టివ్ నివేదికలు ఏవీ కనుగొనబడలేదు.", "ಸಕ್ರಿಯ ವರದಿಗಳು ಕಂಡುಬಂದಿಲ್ಲ.", "സജീവ റിപ്പോർട്ടുകളൊന്നും കണ്ടെത്തിയില്ല."},
                {"No active reports.", "कोई सक्रिय रिपोर्ट नहीं है।", "सक्रिय अहवाल नाहीत.", "કોઈ સક્રિય રિપોર્ટ નથી.", "কোনো সক্রিয় রিপোর্ট নেই।", "செயலில் உள்ள அறிக்கைகள் இல்லை.", "యాక్టివ్ రిపోర్టులు లేవు.", "ಸಕ್ರಿಯ ವರದಿಗಳಿಲ್ಲ.", "സജീവ റിപ്പോർട്ടുകൾ ഇല്ല."},
                {"Completed reports", "पूर्ण रिपोर्टें", "पूर्ण झालेले अहवाल", "પૂર્ણ થયેલા રિપોર્ટ", "সম্পন্ন রিপোর্ট", "நிறைவு செய்யப்பட்ட அறிக்கைகள்", "పూర్తయిన రిపోర్టులు", "ಪೂರ್ಣಗೊಂಡ ವರದಿಗಳು", "പൂർത്തിയായ റിപ്പോർട്ടുകൾ"},
                {"Could not load reports. Try again.", "रिपोर्ट लोड नहीं हो सकीं। फिर से कोशिश करें।", "अहवाल लोड करता आले नाहीत. पुन्हा प्रयत्न करा.", "અહેવાલો લોડ થઈ શક્યા નથી. ફરી પ્રયાસ કરો.", "রিপোর্ট লোড করা যায়নি। আবার চেষ্টা করুন।", "புகார்களை ஏற்ற முடியவில்லை. மீண்டும் முயற்சிக்கவும்.", "నివేదికలను లోడ్ చేయలేకపోయాము. మళ్లీ ప్రయత్నించండి.", "ವರದಿಗಳನ್ನು ಲೋಡ್ ಮಾಡಲಾಗಲಿಲ್ಲ. ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.", "റിപ്പോർട്ടുകൾ ലോഡ് ചെയ്യാനായില്ല. വീണ്ടും ശ്രമിക്കുക."},
                {"Browse by category", "श्रेणी के अनुसार देखें", "श्रेणीनुसार पहा", "શ્રેણી પ્રમાણે જુઓ", "বিভাগ অনুযায়ী দেখুন", "வகையின்படி பார்க்கவும்", "వర్గం ద్వారా చూడండి", "ವರ್ಗದ ಪ್ರಕಾರ ವೀಕ್ಷಿಸಿ", "വിഭാഗം തിരിച്ച് കാണുക"},
                {"Valuables & Items", "कीमती सामान और वस्तुएँ", "मौल्यवान वस्तू", "કીમતી વસ્તુઓ", "মূল্যবান জিনিসপত্র", "மதிப்புமிக்க பொருட்கள்", "విలువైన వస్తువులు", "ಬೆಲೆಬಾಳುವ ವಸ್ತುಗಳು", "വിലപിടിപ്പുള്ള വസ്തുക്കൾ"},
                {"Pets & Animals", "पालतू जानवर और पशु", "पाळीव प्राणी आणि इतर प्राणी", "પાળતુ પ્રાણી અને પ્રાણીઓ", "পোষা প্রাণী ও অন্যান্য প্রাণী", "செல்லப்பிராணிகள் மற்றும் விலங்குகள்", "పెంపుడు జంతువులు మరియు ఇతర జంతువులు", "ಸಾಕುಪ್ರಾಣಿಗಳು ಮತ್ತು ಇತರ ಪ್ರಾಣಿಗಳು", "വളർത്തുമൃഗങ്ങളും മറ്റ് മൃഗങ്ങളും"},
                {"Missing Persons / Loved Ones", "लापता व्यक्ति / प्रियजन", "बेपत्ता व्यक्ती / प्रियजन", "ગુમ થયેલ વ્યક્તિ / પ્રિયજનો", "নিখোঁজ ব্যক্তি / প্রিয়জন", "காணாமல் போனவர்கள் / அன்புக்குரியவர்கள்", "కనిపించని వ్యక్తులు / ఆత్మీయులు", "ಕಾಣೆಯಾದ ವ್ಯಕ್ತಿಗಳು / ಪ್ರೀತಿಪಾತ್ರರು", "കാണാതായവർ / പ്രിയപ്പെട്ടവർ"},
                {"URGENT", "अत्यावश्यक", "तातडीचे", "તાત્કાલિક", "জরুরি", "அவசரம்", "అత్యవసరం", "ತುರ್ತು", "അടിയന്തിരം"},
                {"No reports in this category.", "इस श्रेणी में कोई रिपोर्ट नहीं है।", "या श्रेणीमध्ये कोणतेही अहवाल नाहीत.", "આ શ્રેણીમાં કોઈ અહેવાલ નથી.", "এই বিভাগে কোনো রিপোর্ট নেই।", "இந்த வகையில் புகார்கள் எதுவும் இல்லை.", "ఈ వర్గంలో నివేదికలు లేవు.", "ಈ ವರ್ಗದಲ್ಲಿ ಯಾವುದೇ ವರದಿಗಳಿಲ್ಲ.", "ഈ വിഭാഗത്തിൽ റിപ്പോർട്ടുകളൊന്നുമില്ല."},
                {"Live Status Tracker", "लाइव स्थिति ट्रैकर", "लाइव्ह स्थिती ट्रॅकर", "લાઇવ સ્થિતિ ટ્રેકર", "লাইভ স্ট্যাটাস ট্র্যাকার", "நேரடி நிலை கண்காணிப்பு", "లైవ్ స్టేటస్ ట్రాకర్", "ಲೈವ್ ಸ್ಥಿತಿ ಟ್ರ್ಯಾಕರ್", "ലൈവ് സ്റ്റാറ്റസ് ട്രാക്കർ"},
                {"Submitted & Under Review", "जमा किया गया और समीक्षा जारी", "सबमिट केले आणि पुनरावलोकन सुरू", "સબમિટ કર્યું અને સમીક્ષા હેઠળ", "জমা দেওয়া হয়েছে ও পর্যালোচনাধীন", "சமர்ப்பிக்கப்பட்டது மற்றும் மதிப்பாய்வில் உள்ளது", "సమర్పించబడింది మరియు సమీక్షలో ఉంది", "ಸಲ್ಲಿಸಲಾಗಿದೆ ಮತ್ತು ಪರಿಶೀಲನೆಯಲ್ಲಿದೆ", "സമർപ്പിച്ചു, അവലോകനത്തിലാണ്"},
                {"Admin validating details", "एडमिन विवरण सत्यापित कर रहा है", "अॅडमिन तपशील पडताळत आहे", "એડમિન વિગતો ચકાસી રહ્યું છે", "অ্যাডমিন বিবরণ যাচাই করছে", "நிர்வாகி விவரங்களைச் சரிபார்க்கிறார்", "అడ్మిన్ వివరాలను ధృవీకరిస్తున్నారు", "ನಿರ್ವಾಹಕರು ವಿವರಗಳನ್ನು ಪರಿಶೀಲಿಸುತ್ತಿದ್ದಾರೆ", "അഡ്മിൻ വിവരങ്ങൾ പരിശോധിക്കുന്നു"},
                {"Published & Broadcasting", "प्रकाशित और प्रसारित", "प्रकाशित आणि प्रसारित", "પ્રકાશિત અને પ્રસારિત", "প্রকাশিত ও সম্প্রচারিত", "வெளியிடப்பட்டு ஒளிபரப்பப்படுகிறது", "ప్రచురించబడింది మరియు ప్రసారం అవుతోంది", "ಪ್ರಕಟಿಸಲಾಗಿದೆ ಮತ್ತು ಪ್ರಸಾರವಾಗುತ್ತಿದೆ", "പ്രസിദ്ധീകരിച്ചു, പ്രക്ഷേപണം ചെയ്യുന്നു"},
                {"Live on network", "नेटवर्क पर लाइव", "नेटवर्कवर लाइव्ह", "નેટવર્ક પર લાઇવ", "নেটওয়ার্কে লাইভ", "நெட்வொர்க்கில் நேரலையில்", "నెట్‌వర్క్‌లో ప్రత్యక్షంగా", "ನೆಟ್‌ವರ್ಕ್‌ನಲ್ಲಿ ಲೈವ್", "നെറ്റ്‌വർക്കിൽ ലൈവ്"},
                {"Match Found / Verification in Progress", "मैच मिला / सत्यापन जारी", "जुळणी आढळली / पडताळणी सुरू", "મેચ મળ્યો / ચકાસણી ચાલુ", "মিল পাওয়া গেছে / যাচাই চলছে", "பொருத்தம் கண்டறியப்பட்டது / சரிபார்ப்பு நடைபெறுகிறது", "మ్యాచ్ కనుగొనబడింది / ధృవీకరణలో ఉంది", "ಹೊಂದಾಣಿಕೆ ಕಂಡುಬಂದಿದೆ / ಪರಿಶೀಲನೆ ನಡೆಯುತ್ತಿದೆ", "പൊരുത്തം കണ്ടെത്തി / പരിശോധന പുരോഗമിക്കുന്നു"},
                {"Admin verifying ownership", "एडमिन स्वामित्व सत्यापित कर रहा है", "अॅडमिन मालकीची पडताळणी करत आहे", "એડમિન માલિકી ચકાસી રહ્યું છે", "অ্যাডমিন মালিকানা যাচাই করছে", "நிர்வாகி உரிமையைச் சரிபார்க்கிறார்", "అడ్మిన్ యాజమాన్యాన్ని ధృవీకరిస్తున్నారు", "ನಿರ್ವಾಹಕರು ಮಾಲೀಕತ್ವವನ್ನು ಪರಿಶೀಲಿಸುತ್ತಿದ್ದಾರೆ", "അഡ്മിൻ ഉടമസ്ഥാവകാശം പരിശോധിക്കുന്നു"},
                {"Successfully Reunited", "सफलतापूर्वक मिल गया", "यशस्वीरित्या पुन्हा मिळाले", "સફળતાપૂર્વક ફરી મળ્યું", "সফলভাবে পুনর্মিলিত", "வெற்றிகரமாக மீண்டும் இணைக்கப்பட்டது", "విజయవంతంగా తిరిగి కలిశారు", "ಯಶಸ್ವಿಯಾಗಿ ಮತ್ತೆ ಒಂದಾಗಿದೆ", "വിജയകരമായി വീണ്ടും ഒന്നിച്ചു"},
                {"Closed with a success badge", "सफलता बैज के साथ बंद", "यशस्वी बॅजसह बंद", "સફળતા બેજ સાથે બંધ", "সাফল্যের ব্যাজসহ বন্ধ", "வெற்றி அடையாளத்துடன் முடிக்கப்பட்டது", "విజయ బ్యాడ్జ్‌తో మూసివేయబడింది", "ಯಶಸ್ಸಿನ ಬ್ಯಾಡ್ಜ್‌ನೊಂದಿಗೆ ಮುಚ್ಚಲಾಗಿದೆ", "വിജയ ബാഡ്ജോടെ അടച്ചു"},
                {"SUCCESSFULLY REUNITED", "सफलतापूर्वक मिल गया", "यशस्वीरित्या पुन्हा मिळाले", "સફળતાપૂર્વક ફરી મળ્યું", "সফলভাবে পুনর্মিলিত", "வெற்றிகரமாக மீண்டும் இணைக்கப்பட்டது", "విజయవంతంగా తిరిగి కలిశారు", "ಯಶಸ್ವಿಯಾಗಿ ಮತ್ತೆ ಒಂದಾಗಿದೆ", "വിജയകരമായി വീണ്ടും ഒന്നിച്ചു"},
                {"Share Alert Card", "अलर्ट कार्ड साझा करें", "अलर्ट कार्ड शेअर करा", "એલર્ટ કાર્ડ શેર કરો", "সতর্কতা কার্ড শেয়ার করুন", "எச்சரிக்கை அட்டையைப் பகிரவும்", "అలర్ట్ కార్డ్‌ను షేర్ చేయండి", "ಎಚ್ಚರಿಕೆ ಕಾರ್ಡ್ ಹಂಚಿಕೊಳ್ಳಿ", "അലർട്ട് കാർഡ് പങ്കിടുക"},
                {"Share alert flyer", "अलर्ट फ्लायर साझा करें", "अलर्ट फ्लायर शेअर करा", "એલર્ટ ફ્લાયર શેર કરો", "সতর্কতার ফ্লায়ার শেয়ার করুন", "எச்சரிக்கை ஃப்ளையரைப் பகிரவும்", "అలర్ట్ ఫ్లయర్‌ను షేర్ చేయండి", "ಎಚ್ಚರಿಕೆ ಫ್ಲೈಯರ್ ಹಂಚಿಕೊಳ್ಳಿ", "അലർട്ട് ഫ്ലയർ പങ്കിടുക"},
                {"Preview the branded alert, then share it with your community.", "ब्रांडेड अलर्ट का पूर्वावलोकन करें और समुदाय के साथ साझा करें।", "ब्रँडेड अलर्टचे पूर्वावलोकन करा आणि समुदायासोबत शेअर करा.", "બ્રાન્ડેડ એલર્ટનું પૂર્વાવલોકન કરો અને સમુદાય સાથે શેર કરો.", "ব্র্যান্ডেড সতর্কতা দেখুন, তারপর সম্প্রদায়ের সঙ্গে শেয়ার করুন।", "பிராண்டட் எச்சரிக்கையை முன்னோட்டமிட்டு சமூகத்துடன் பகிரவும்.", "బ్రాండెడ్ అలర్ట్‌ను ప్రివ్యూ చేసి కమ్యూనిటీతో షేర్ చేయండి.", "ಬ್ರಾಂಡೆಡ್ ಎಚ್ಚರಿಕೆಯನ್ನು ಪೂರ್ವವೀಕ್ಷಿಸಿ, ನಂತರ ಸಮುದಾಯದೊಂದಿಗೆ ಹಂಚಿಕೊಳ್ಳಿ.", "ബ്രാൻഡഡ് അലർട്ട് പ്രിവ്യൂ ചെയ്ത് സമൂഹവുമായി പങ്കിടുക."},
                {"Preparing flyer...", "फ्लायर तैयार हो रहा है...", "फ्लायर तयार होत आहे...", "ફ્લાયર તૈયાર થઈ રહ્યું છે...", "ফ্লায়ার তৈরি হচ্ছে...", "ஃப்ளையர் தயாராகிறது...", "ఫ్లయర్ సిద్ధమవుతోంది...", "ಫ್ಲೈಯರ್ ಸಿದ್ಧವಾಗುತ್ತಿದೆ...", "ഫ്ലയർ തയ്യാറാക്കുന്നു..."},
                {"Could not prepare flyer. Try again.", "फ्लायर तैयार नहीं हो सका। फिर से कोशिश करें।", "फ्लायर तयार करता आला नाही. पुन्हा प्रयत्न करा.", "ફ્લાયર તૈયાર થઈ શક્યું નથી. ફરી પ્રયાસ કરો.", "ফ্লায়ার তৈরি করা যায়নি। আবার চেষ্টা করুন।", "ஃப்ளையரைத் தயாரிக்க முடியவில்லை. மீண்டும் முயற்சிக்கவும்.", "ఫ్లయర్‌ను సిద్ధం చేయలేకపోయాము. మళ్లీ ప్రయత్నించండి.", "ಫ್ಲೈಯರ್ ಸಿದ್ಧಪಡಿಸಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ. ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.", "ഫ്ലയർ തയ്യാറാക്കാനായില്ല. വീണ്ടും ശ്രമിക്കുക."},
                {"Could not share flyer.", "फ्लायर साझा नहीं हो सका।", "फ्लायर शेअर करता आला नाही.", "ફ્લાયર શેર થઈ શક્યો નથી.", "ফ্লায়ার শেয়ার করা যায়নি।", "ஃப்ளையரைப் பகிர முடியவில்லை.", "ఫ్లయర్‌ను షేర్ చేయలేకపోయాము.", "ಫ್ಲೈಯರ್ ಹಂಚಿಕೊಳ್ಳಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ.", "ഫ്ലയർ പങ്കിടാനായില്ല."}
        };
        int language = Math.max(0, Math.min(selectedLanguage, entries[0].length - 1));
        for (String[] entry : entries) {
            if (entry[0].equals(value)) return localizeDigits(entry[language]);
        }
        return localizeDigits(value);
    }

    private String localizeReportWizardText(String value) {
        if (value == null) return null;
        if (value.startsWith("Step ") && value.endsWith(" of 4")) {
            return localizeDigits(value);
        }
        String[][] entries = {
                {"Category", "श्रेणी", "श्रेणी", "શ્રેણી", "বিভাগ", "வகை", "వర్గం", "ವರ್ಗ", "വിഭാഗം"},
                {"Location", "स्थान", "ठिकाण", "સ્થાન", "অবস্থান", "இடம்", "స్థానం", "ಸ್ಥಳ", "സ്ഥലം"},
                {"Details", "विवरण", "तपशील", "વિગતો", "বিবরণ", "விவரங்கள்", "వివరాలు", "ವಿವರಗಳು", "വിശദാംശങ്ങൾ"},
                {"Review", "समीक्षा", "पुनरावलोकन", "સમીક્ષા", "পর্যালোচনা", "மதிப்பாய்வு", "సమీక్ష", "ಪರಿಶೀಲನೆ", "അവലോകനം"},
                {"Allow Fendly to share this report on its Facebook and Instagram accounts", "Fendly को इस रिपोर्ट को अपने Facebook और Instagram खातों पर साझा करने दें", "Fendly ला हा अहवाल त्याच्या Facebook आणि Instagram खात्यांवर शेअर करू द्या", "Fendly ને આ અહેવાલ તેના Facebook અને Instagram એકાઉન્ટ્સ પર શેર કરવા દો", "Fendly-কে এই প্রতিবেদনটি তার Facebook ও Instagram অ্যাকাউন্টে শেয়ার করতে দিন", "இந்த அறிக்கையை Fendly தனது Facebook மற்றும் Instagram கணக்குகளில் பகிர அனுமதிக்கவும்", "ఈ నివేదికను Fendly తన Facebook మరియు Instagram ఖాతాల్లో పంచుకోవడానికి అనుమతించండి", "ಈ ವರದಿಯನ್ನು Fendly ತನ್ನ Facebook ಮತ್ತು Instagram ಖಾತೆಗಳಲ್ಲಿ ಹಂಚಿಕೊಳ್ಳಲು ಅನುಮತಿಸಿ", "ഈ റിപ്പോർട്ട് Fendly-യുടെ Facebook, Instagram അക്കൗണ്ടുകളിൽ പങ്കിടാൻ അനുവദിക്കുക"},
                {"Optional. Report type and title may be public. Facebook/Instagram may receive the generated poster and selected photo. Details, location, contact info and IMEI stay private. Instagram needs a public JPEG poster; Reels are not supported.", "वैकल्पिक। रिपोर्ट का प्रकार और शीर्षक सार्वजनिक हो सकते हैं। Facebook/Instagram को जनरेट किया गया पोस्टर और चुनी गई फोटो मिल सकती है। विवरण, स्थान, संपर्क जानकारी और IMEI निजी रहेंगे। Instagram के लिए सार्वजनिक JPEG पोस्टर जरूरी है; Reels समर्थित नहीं हैं।", "ऐच्छिक. अहवालाचा प्रकार आणि शीर्षक सार्वजनिक होऊ शकतात. Facebook/Instagram ला तयार केलेले पोस्टर आणि निवडलेला फोटो मिळू शकतो. तपशील, ठिकाण, संपर्क माहिती आणि IMEI खाजगी राहतील. Instagram साठी सार्वजनिक JPEG पोस्टर आवश्यक आहे; Reels समर्थित नाहीत.", "વૈકલ્પિક. રિપોર્ટનો પ્રકાર અને શીર્ષક જાહેર થઈ શકે છે. Facebook/Instagram ને બનાવેલું પોસ્ટર અને પસંદ કરેલો ફોટો મળી શકે છે. વિગતો, સ્થાન, સંપર્ક માહિતી અને IMEI ખાનગી રહેશે. Instagram માટે જાહેર JPEG પોસ્ટર જરૂરી છે; Reels સપોર્ટેડ નથી.", "ঐচ্ছিক। প্রতিবেদনের ধরন ও শিরোনাম প্রকাশ্যে যেতে পারে। Facebook/Instagram তৈরি করা পোস্টার ও নির্বাচিত ছবি পেতে পারে। বিবরণ, অবস্থান, যোগাযোগের তথ্য ও IMEI ব্যক্তিগত থাকবে। Instagram-এর জন্য সর্বজনীন JPEG পোস্টার দরকার; Reels সমর্থিত নয়।", "விருப்பத்தேர்வு. அறிக்கை வகையும் தலைப்பும் பொதுவாகலாம். Facebook/Instagram உருவாக்கிய போஸ்டரையும் தேர்ந்தெடுத்த படத்தையும் பெறலாம். விவரங்கள், இடம், தொடர்புத் தகவல், IMEI தனிப்பட்டவை. Instagram-க்கு பொதுவில் கிடைக்கும் JPEG போஸ்டர் தேவை; Reels ஆதரிக்கப்படவில்லை.", "ఐచ్ఛికం. నివేదిక రకం మరియు శీర్షిక పబ్లిక్ కావచ్చు. Facebook/Instagram రూపొందించిన పోస్టర్ మరియు ఎంచుకున్న ఫోటోను పొందవచ్చు. వివరాలు, స్థానం, సంప్రదింపు సమాచారం, IMEI ప్రైవేట్‌గా ఉంటాయి. Instagram‌కు పబ్లిక్ JPEG పోస్టర్ అవసరం; Reels‌కు మద్దతు లేదు.", "ಐಚ್ಛಿಕ. ವರದಿ ಪ್ರಕಾರ ಮತ್ತು ಶೀರ್ಷಿಕೆ ಸಾರ್ವಜನಿಕವಾಗಬಹುದು. Facebook/Instagram ರಚಿಸಿದ ಪೋಸ್ಟರ್ ಮತ್ತು ಆಯ್ಕೆಮಾಡಿದ ಫೋಟೋ ಪಡೆಯಬಹುದು. ವಿವರಗಳು, ಸ್ಥಳ, ಸಂಪರ್ಕ ಮಾಹಿತಿ ಮತ್ತು IMEI ಖಾಸಗಿ. Instagram ಗೆ ಸಾರ್ವಜನಿಕ JPEG ಪೋಸ್ಟರ್ ಬೇಕು; Reels ಬೆಂಬಲಿತವಲ್ಲ.", "ഐച്ഛികം. റിപ്പോർട്ടിന്റെ തരവും തലക്കെട്ടും പൊതുവാകാം. Facebook/Instagram സൃഷ്ടിച്ച പോസ്റ്ററും തിരഞ്ഞെടുത്ത ചിത്രവും ലഭിക്കും. വിശദാംശങ്ങൾ, സ്ഥലം, ബന്ധപ്പെടാനുള്ള വിവരങ്ങൾ, IMEI എന്നിവ സ്വകാര്യമാണ്. Instagram-ന് പൊതുവായി ലഭ്യമായ JPEG പോസ്റ്റർ വേണം; Reels പിന്തുണയ്ക്കുന്നില്ല."},
                {"Choose what you are reporting", "आप किसकी रिपोर्ट कर रहे हैं चुनें", "कशाची नोंद करायची ते निवडा", "તમે શું નોંધવા માંગો છો તે પસંદ કરો", "আপনি কী রিপোর্ট করছেন তা বেছে নিন", "எதைப் புகாரளிக்கிறீர்கள் என்பதைத் தேர்ந்தெடுக்கவும்", "మీరు దేనిని నివేదిస్తున్నారో ఎంచుకోండి", "ನೀವು ಏನನ್ನು ವರದಿ ಮಾಡುತ್ತಿದ್ದೀರಿ ಆಯ್ಕೆಮಾಡಿ", "എന്താണ് റിപ്പോർട്ട് ചെയ്യുന്നതെന്ന് തിരഞ്ഞെടുക്കുക"},
                {"Item / Valuables", "वस्तु / कीमती सामान", "वस्तू / मौल्यवान वस्तू", "વસ્તુ / કીમતી ચીજ", "জিনিস / মূল্যবান সামগ্রী", "பொருள் / மதிப்புமிக்கவை", "వస్తువు / విలువైనవి", "ವಸ್ತು / ಬೆಲೆಬಾಳುವವು", "വസ്തു / വിലപിടിപ്പുള്ളവ"},
                {"Pet / Animal", "पालतू / पशु", "पाळीव प्राणी / प्राणी", "પાળતુ પ્રાણી / પ્રાણી", "পোষা প্রাণী / পশু", "செல்லப்பிராணி / விலங்கு", "పెంపుడు జంతువు / జంతువు", "ಸಾಕುಪ್ರಾಣಿ / ಪ್ರಾಣಿ", "വളർത്തുമൃഗം / മൃഗം"},
                {"Missing Person", "लापता व्यक्ति", "बेपत्ता व्यक्ती", "ગુમ થયેલ વ્યક્તિ", "নিখোঁজ ব্যক্তি", "காணாமல் போனவர்", "కనిపించని వ్యక్తి", "ಕಾಣೆಯಾದ ವ್ಯಕ್ತಿ", "കാണാതായ വ്യക്തി"},
                {"Report type", "रिपोर्ट का प्रकार", "अहवालाचा प्रकार", "રિપોર્ટનો પ્રકાર", "রিপোর্টের ধরন", "புகார் வகை", "నివేదిక రకం", "ವರದಿ ಪ್ರಕಾರ", "റിപ്പോർട്ട് തരം"},
                {"Add a photo and location", "फोटो और स्थान जोड़ें", "फोटो आणि ठिकाण जोडा", "ફોટો અને સ્થાન ઉમેરો", "ছবি ও অবস্থান যোগ করুন", "படம் மற்றும் இடத்தைச் சேர்க்கவும்", "ఫోటో మరియు స్థానాన్ని జోడించండి", "ಫೋಟೋ ಮತ್ತು ಸ್ಥಳ ಸೇರಿಸಿ", "ഫോട്ടോയും സ്ഥലവും ചേർക്കുക"},
                {"City / area tag", "शहर / क्षेत्र टैग", "शहर / परिसर टॅग", "શહેર / વિસ્તાર ટૅગ", "শহর / এলাকার ট্যাগ", "நகரம் / பகுதி குறிச்சொல்", "నగరం / ప్రాంతం ట్యాగ్", "ನಗರ / ಪ್ರದೇಶ ಟ್ಯಾಗ್", "നഗരം / പ്രദേശ ടാഗ്"},
                {"Precise location ready", "सटीक स्थान तैयार", "अचूक ठिकाण तयार", "ચોક્કસ સ્થાન તૈયાર", "সুনির্দিষ্ট অবস্থান প্রস্তুত", "துல்லியமான இடம் தயார்", "ఖచ్చితమైన స్థానం సిద్ధం", "ನಿಖರ ಸ್ಥಳ ಸಿದ್ಧ", "കൃത്യമായ സ്ഥലം തയ്യാറാണ്"},
                {"Use precise location (optional)", "सटीक स्थान उपयोग करें (वैकल्पिक)", "अचूक ठिकाण वापरा (ऐच्छिक)", "ચોક્કસ સ્થાન વાપરો (વૈકલ્પિક)", "সুনির্দিষ্ট অবস্থান ব্যবহার করুন (ঐচ্ছিক)", "துல்லியமான இடத்தைப் பயன்படுத்தவும் (விருப்பம்)", "ఖచ్చితమైన స్థానాన్ని ఉపయోగించండి (ఐచ్ఛికం)", "ನಿಖರ ಸ್ಥಳ ಬಳಸಿ (ಐಚ್ಛಿಕ)", "കൃത്യമായ സ്ഥലം ഉപയോഗിക്കുക (ഐച്ഛികം)"},
                {"Add identifying details", "पहचान का विवरण जोड़ें", "ओळख तपशील जोडा", "ઓળખની વિગતો ઉમેરો", "শনাক্তকরণের বিবরণ যোগ করুন", "அடையாள விவரங்களைச் சேர்க்கவும்", "గుర్తింపు వివరాలను జోడించండి", "ಗುರುತಿನ ವಿವರಗಳನ್ನು ಸೇರಿಸಿ", "തിരിച്ചറിയൽ വിവരങ്ങൾ ചേർക്കുക"},
                {"Report title", "रिपोर्ट का शीर्षक", "अहवालाचे शीर्षक", "રિપોર્ટનું શીર્ષક", "রিপোর্টের শিরোনাম", "புகார் தலைப்பு", "నివేదిక శీర్షిక", "ವರದಿ ಶೀರ್ಷಿಕೆ", "റിപ്പോർട്ട് തലക്കെട്ട്"},
                {"Pet identifiers", "पालतू जानवर की पहचान", "पाळीव प्राण्याची ओळख", "પાળતુ પ્રાણીની ઓળખ", "পোষা প্রাণীর পরিচয়", "செல்லப்பிராணி அடையாளங்கள்", "పెంపుడు జంతువు గుర్తింపులు", "ಸಾಕುಪ್ರಾಣಿ ಗುರುತುಗಳು", "വളർത്തുമൃഗത്തിന്റെ തിരിച്ചറിയൽ"},
                {"Person identifiers", "व्यक्ति की पहचान", "व्यक्तीची ओळख", "વ્યક્તિની ઓળખ", "ব্যক্তির পরিচয়", "நபர் அடையாளங்கள்", "వ్యక్తి గుర్తింపులు", "ವ್ಯಕ್ತಿ ಗುರುತುಗಳು", "വ്യക്തിയുടെ തിരിച്ചറിയൽ"},
                {"Serial number or other identifier", "सीरियल नंबर या अन्य पहचान", "सीरियल क्रमांक किंवा इतर ओळख", "સીરીયલ નંબર અથવા અન્ય ઓળખ", "সিরিয়াল নম্বর বা অন্য পরিচয়", "வரிசை எண் அல்லது பிற அடையாளம்", "సీరియల్ నంబర్ లేదా ఇతర గుర్తింపు", "ಸೀರಿಯಲ್ ಸಂಖ್ಯೆ ಅಥವಾ ಇತರ ಗುರುತು", "സീരിയൽ നമ്പർ അല്ലെങ്കിൽ മറ്റ് തിരിച്ചറിയൽ"},
                {"Other identifying details", "अन्य पहचान विवरण", "इतर ओळख तपशील", "અન્ય ઓળખ વિગતો", "অন্যান্য শনাক্তকরণ বিবরণ", "பிற அடையாள விவரங்கள்", "ఇతర గుర్తింపు వివరాలు", "ಇತರ ಗುರುತಿನ ವಿವರಗಳು", "മറ്റ് തിരിച്ചറിയൽ വിവരങ്ങൾ"},
                {"Review your report", "अपनी रिपोर्ट की समीक्षा करें", "तुमच्या अहवालाचे पुनरावलोकन करा", "તમારા રિપોર્ટની સમીક્ષા કરો", "আপনার রিপোর্ট পর্যালোচনা করুন", "உங்கள் புகாரை மதிப்பாய்வு செய்யவும்", "మీ నివేదికను సమీక్షించండి", "ನಿಮ್ಮ ವರದಿಯನ್ನು ಪರಿಶೀಲಿಸಿ", "നിങ്ങളുടെ റിപ്പോർട്ട് അവലോകനം ചെയ്യുക"},
                {"Photo", "फोटो", "फोटो", "ફોટો", "ছবি", "படம்", "ఫోటో", "ಫೋಟೋ", "ഫോട്ടോ"},
                {"Photo attached", "फोटो संलग्न", "फोटो जोडला", "ફોટો જોડ્યો", "ছবি সংযুক্ত", "படம் இணைக்கப்பட்டது", "ఫోటో జోడించబడింది", "ಫೋಟೋ ಸೇರಿಸಲಾಗಿದೆ", "ഫോട്ടോ ചേർത്തു"},
                {"No photo attached", "कोई फोटो संलग्न नहीं", "फोटो जोडलेला नाही", "કોઈ ફોટો જોડ્યો નથી", "কোনো ছবি সংযুক্ত নয়", "படம் இணைக்கப்படவில்லை", "ఫోటో జోడించలేదు", "ಫೋಟೋ ಸೇರಿಸಲಾಗಿಲ್ಲ", "ഫോട്ടോ ചേർത്തിട്ടില്ല"},
                {"Identifying details", "पहचान का विवरण", "ओळख तपशील", "ઓળખની વિગતો", "শনাক্তকরণের বিবরণ", "அடையாள விவரங்கள்", "గుర్తింపు వివరాలు", "ಗುರುತಿನ ವಿವರಗಳು", "തിരിച്ചറിയൽ വിവരങ്ങൾ"},
                {"Edit category", "श्रेणी बदलें", "श्रेणी बदला", "શ્રેણી બદલો", "বিভাগ সম্পাদনা", "வகையைத் திருத்தவும்", "వర్గాన్ని సవరించండి", "ವರ್ಗ ಸಂಪಾದಿಸಿ", "വിഭാഗം തിരുത്തുക"},
                {"Edit photo and location", "फोटो और स्थान बदलें", "फोटो आणि ठिकाण बदला", "ફોટો અને સ્થાન બદલો", "ছবি ও অবস্থান সম্পাদনা", "படம் மற்றும் இடத்தைத் திருத்தவும்", "ఫోటో మరియు స్థానాన్ని సవరించండి", "ಫೋಟೋ ಮತ್ತು ಸ್ಥಳ ಸಂಪಾದಿಸಿ", "ഫോട്ടോയും സ്ഥലവും തിരുത്തുക"},
                {"Edit identifying details", "पहचान विवरण बदलें", "ओळख तपशील बदला", "ઓળખની વિગતો બદલો", "শনাক্তকরণের বিবরণ সম্পাদনা", "அடையாள விவரங்களைத் திருத்தவும்", "గుర్తింపు వివరాలను సవరించండి", "ಗುರುತಿನ ವಿವರ ಸಂಪಾದಿಸಿ", "തിരിച്ചറിയൽ വിവരങ്ങൾ തിരുത്തുക"},
                {"Back", "वापस", "मागे", "પાછળ", "পেছনে", "பின்", "వెనుకకు", "ಹಿಂದೆ", "പിന്നിലേക്ക്"},
                {"Continue", "जारी रखें", "पुढे जा", "ચાલુ રાખો", "চালিয়ে যান", "தொடரவும்", "కొనసాగించండి", "ಮುಂದುವರಿಸಿ", "തുടരുക"},
                {"Add a city or area tag to continue.", "जारी रखने के लिए शहर या क्षेत्र टैग जोड़ें।", "पुढे जाण्यासाठी शहर किंवा परिसर टॅग जोडा.", "ચાલુ રાખવા માટે શહેર અથવા વિસ્તાર ટૅગ ઉમેરો.", "চালিয়ে যেতে শহর বা এলাকার ট্যাগ যোগ করুন।", "தொடர நகரம் அல்லது பகுதி குறிச்சொல்லைச் சேர்க்கவும்.", "కొనసాగించడానికి నగరం లేదా ప్రాంతం ట్యాగ్ జోడించండి.", "ಮುಂದುವರಿಸಲು ನಗರ ಅಥವಾ ಪ್ರದೇಶ ಟ್ಯಾಗ್ ಸೇರಿಸಿ.", "തുടരാൻ നഗരമോ പ്രദേശമോ ടാഗ് ചേർക്കുക."},
                {"Enter a report title to continue.", "जारी रखने के लिए रिपोर्ट का शीर्षक दर्ज करें।", "पुढे जाण्यासाठी अहवालाचे शीर्षक भरा.", "ચાલુ રાખવા માટે રિપોર્ટનું શીર્ષક દાખલ કરો.", "চালিয়ে যেতে রিপোর্টের শিরোনাম লিখুন।", "தொடர புகார் தலைப்பை உள்ளிடவும்.", "కొనసాగించడానికి నివేదిక శీర్షిక నమోదు చేయండి.", "ಮುಂದುವರಿಸಲು ವರದಿ ಶೀರ್ಷಿಕೆ ನಮೂದಿಸಿ.", "തുടരാൻ റിപ്പോർട്ട് തലക്കെട്ട് നൽകുക."},
                {"Enter a report title.", "रिपोर्ट का शीर्षक दर्ज करें।", "अहवालाचे शीर्षक भरा.", "રિપોર્ટનું શીર્ષક દાખલ કરો.", "রিপোর্টের শিরোনাম লিখুন।", "புகார் தலைப்பை உள்ளிடவும்.", "నివేదిక శీర్షిక నమోదు చేయండి.", "ವರದಿ ಶೀರ್ಷಿಕೆ ನಮೂದಿಸಿ.", "റിപ്പോർട്ട് തലക്കെട്ട് നൽകുക."},
                {"Add at least one identifying detail.", "कम से कम एक पहचान विवरण जोड़ें।", "किमान एक ओळख तपशील जोडा.", "ઓછામાં ઓછી એક ઓળખની વિગત ઉમેરો.", "অন্তত একটি শনাক্তকরণ বিবরণ যোগ করুন।", "குறைந்தது ஒரு அடையாள விவரத்தைச் சேர்க்கவும்.", "కనీసం ఒక గుర్తింపు వివరాన్ని జోడించండి.", "ಕನಿಷ್ಠ ಒಂದು ಗುರುತಿನ ವಿವರ ಸೇರಿಸಿ.", "കുറഞ്ഞത് ഒരു തിരിച്ചറിയൽ വിവരം ചേർക്കുക."},
                {"Add a location and city tag.", "स्थान और शहर टैग जोड़ें।", "ठिकाण आणि शहर टॅग जोडा.", "સ્થાન અને શહેર ટૅગ ઉમેરો.", "অবস্থান ও শহরের ট্যাগ যোগ করুন।", "இடம் மற்றும் நகரக் குறிச்சொல்லைச் சேர்க்கவும்.", "స్థానం మరియు నగరం ట్యాగ్ జోడించండి.", "ಸ್ಥಳ ಮತ್ತು ನಗರ ಟ್ಯಾಗ್ ಸೇರಿಸಿ.", "സ്ഥലവും നഗര ടാഗും ചേർക്കുക."},
                {"Title", "शीर्षक", "शीर्षक", "શીર્ષક", "শিরোনাম", "தலைப்பு", "శీర్షిక", "ಶೀರ್ಷಿಕೆ", "തലക്കെട്ട്"},
                {"Date", "तारीख", "तारीख", "તારીખ", "তারিখ", "தேதி", "తేదీ", "ದಿನಾಂಕ", "തീയതി"},
                {"Collar color, tag, breed, or microchip", "कॉलर का रंग, टैग, नस्ल या माइक्रोचिप", "कॉलरचा रंग, टॅग, जात किंवा मायक्रोचिप", "કોલરનો રંગ, ટૅગ, જાતિ અથવા માઇક્રોચિપ", "কলারের রং, ট্যাগ, জাত বা মাইক্রোচিপ", "காலர் நிறம், குறிச்சொல், இனம் அல்லது மைக்ரோசிப்", "కాలర్ రంగు, ట్యాగ్, జాతి లేదా మైక్రోచిప్", "ಕಾಲರ್ ಬಣ್ಣ, ಟ್ಯಾಗ್, ತಳಿ ಅಥವಾ ಮೈಕ್ರೋಚಿಪ್", "കോളറിന്റെ നിറം, ടാഗ്, ഇനം അല്ലെങ്കിൽ മൈക്രോചിപ്പ്"},
                {"Distinct clothing, appearance, or accessories", "विशिष्ट कपड़े, रूप-रंग या सहायक वस्तुएँ", "वेगळे कपडे, रूप किंवा अॅक्सेसरीज", "અલગ કપડાં, દેખાવ અથવા એસેસરીઝ", "স্বতন্ত্র পোশাক, চেহারা বা আনুষঙ্গিক", "தனித்துவமான ஆடை, தோற்றம் அல்லது அணிகலன்கள்", "ప్రత్యేక దుస్తులు, రూపం లేదా ఉపకరణాలు", "ವಿಶಿಷ್ಟ ಉಡುಪು, ರೂಪ ಅಥವಾ ಪರಿಕರಗಳು", "പ്രത്യേക വസ്ത്രം, രൂപം അല്ലെങ്കിൽ അനുബന്ധങ്ങൾ"},
                {"Serial number, brand, model, or distinguishing mark", "सीरियल नंबर, ब्रांड, मॉडल या पहचान चिह्न", "सीरियल क्रमांक, ब्रँड, मॉडेल किंवा ओळखचिन्ह", "સીરીયલ નંબર, બ્રાન્ડ, મોડેલ અથવા ઓળખચિહ્ન", "সিরিয়াল নম্বর, ব্র্যান্ড, মডেল বা শনাক্তকারী চিহ্ন", "வரிசை எண், பிராண்ட், மாடல் அல்லது அடையாளக் குறி", "సీరియల్ నంబర్, బ్రాండ్, మోడల్ లేదా ప్రత్యేక గుర్తు", "ಸೀರಿಯಲ್ ಸಂಖ್ಯೆ, ಬ್ರ್ಯಾಂಡ್, ಮಾದರಿ ಅಥವಾ ಗುರುತಿನ ಚಿಹ್ನೆ", "സീരിയൽ നമ്പർ, ബ്രാൻഡ്, മോഡൽ അല്ലെങ്കിൽ തിരിച്ചറിയൽ അടയാളം"},
                {"e.g. Blue backpack", "उदाहरण: नीला बैकपैक", "उदा. निळी बॅकपॅक", "દા.ત. વાદળી બેકપેક", "যেমন: নীল ব্যাকপ্যাক", "உதா: நீல நிற பை", "ఉదా: నీలం బ్యాక్‌ప్యాక్", "ಉದಾ: ನೀಲಿ ಬ್ಯಾಕ್‌ಪ್ಯಾಕ್", "ഉദാ: നീല ബാക്ക്പാക്ക്"},
                {"e.g. Brown dog", "उदाहरण: भूरा कुत्ता", "उदा. तपकिरी कुत्रा", "દા.ત. ભૂરા રંગનો કૂતરો", "যেমন: বাদামি কুকুর", "உதா: பழுப்பு நாய்", "ఉదా: గోధుమ రంగు కుక్క", "ಉದಾ: ಕಂದು ನಾಯಿ", "ഉദാ: തവിട്ടുനിറമുള്ള നായ"},
                {"e.g. Missing person", "उदाहरण: लापता व्यक्ति", "उदा. बेपत्ता व्यक्ती", "દા.ત. ગુમ થયેલ વ્યક્તિ", "যেমন: নিখোঁজ ব্যক্তি", "உதா: காணாமல் போனவர்", "ఉదా: కనిపించని వ్యక్తి", "ಉದಾ: ಕಾಣೆಯಾದ ವ್ಯಕ್ತಿ", "ഉദാ: കാണാതായ വ്യക്തി"},
                {"e.g. Nagpur", "उदाहरण: नागपुर", "उदा. नागपूर", "દા.ત. નાગપુર", "যেমন: নাগপুর", "உதா: நாக்பூர்", "ఉదా: నాగ్‌పూర్", "ಉದಾ: ನಾಗಪುರ", "ഉദാ: നാഗ്പൂർ"},
                {"Optional 15-digit IMEI", "वैकल्पिक 15-अंकीय IMEI", "ऐच्छिक 15-अंकी IMEI", "વૈકલ્પિક 15-અંકનો IMEI", "ঐচ্ছিক ১৫-অঙ্কের IMEI", "விருப்பமான 15 இலக்க IMEI", "ఐచ్ఛిక 15 అంకెల IMEI", "ಐಚ್ಛಿಕ 15-ಅಂಕಿಯ IMEI", "ഐച്ഛിക 15 അക്ക IMEI"},
                {"Report wizard", "रिपोर्ट विज़ार्ड", "अहवाल विझार्ड", "રિપોર્ટ વિઝાર્ડ", "রিপোর্ট উইজার্ড", "புகார் வழிகாட்டி", "నివేదిక విజార్డ్", "ವರದಿ ವಿಝಾರ್ಡ್", "റിപ്പോർട്ട് വിസാർഡ്"},
                {"Describe color, brand, appearance, or other helpful details", "रंग, ब्रांड, रूप-रंग या अन्य उपयोगी विवरण बताएँ", "रंग, ब्रँड, रूप किंवा इतर उपयुक्त तपशील द्या", "રંગ, બ્રાન્ડ, દેખાવ અથવા અન્ય ઉપયોગી વિગતો આપો", "রং, ব্র্যান্ড, চেহারা বা অন্যান্য সহায়ক বিবরণ দিন", "நிறம், பிராண்ட், தோற்றம் அல்லது பிற பயனுள்ள விவரங்களை விவரிக்கவும்", "రంగు, బ్రాండ్, రూపం లేదా ఇతర ఉపయోగకరమైన వివరాలను వివరించండి", "ಬಣ್ಣ, ಬ್ರ್ಯಾಂಡ್, ರೂಪ ಅಥವಾ ಇತರ ಉಪಯುಕ್ತ ವಿವರಗಳನ್ನು ವಿವರಿಸಿ", "നിറം, ബ്രാൻഡ്, രൂപം അല്ലെങ്കിൽ മറ്റ് സഹായകരമായ വിശദാംശങ്ങൾ വിവരിക്കുക"}
        };
        int language = Math.max(0, Math.min(selectedLanguage, entries[0].length - 1));
        for (String[] entry : entries) {
            if (entry[0].equals(value)) return localizeDigits(entry[language]);
        }
        return localizeReportsText(value);
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

    private String formatUsernameDisplay(String username) {
        if (username == null || username.trim().isEmpty()) return "";
        String trimmed = username.trim();
        if (trimmed.length() > 0 && Character.isLowerCase(trimmed.charAt(0))) {
            return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
        }
        return trimmed;
    }

    private String localizeProfileDisplayValue(String key, String value) {
        if (value == null || value.trim().isEmpty()) return value;
        if ("username".equalsIgnoreCase(key)) {
            return formatUsernameDisplay(value);
        }
        if ("email".equalsIgnoreCase(key) || "mobile".equalsIgnoreCase(key)) return value;
        if ("state".equalsIgnoreCase(key) || key.toLowerCase(Locale.US).contains("state")) {
            String localized = localizedStateName(value);
            return localized != null ? localized : value;
        }
        if ("city".equalsIgnoreCase(key) || key.toLowerCase(Locale.US).contains("city")) {
            String localized = localizedCityName(value);
            return localized != null ? localized : localizeProfileName(value);
        }
        return localizeProfileName(value);
    }

    private String localizeProfileName(String value) {
        if (value == null || value.trim().isEmpty()) return value;
        selectedLanguage = getSharedPreferences("fendly_language", MODE_PRIVATE)
                .getInt("selected_language_index", 0);
        int lang = Math.max(0, Math.min(selectedLanguage, 8));
        if (lang == 0) {
            return getCanonicalEnglishName(value);
        }
        return transliterateToSelectedScript(value, lang);
    }

    private String getCanonicalEnglishName(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String trimmed = text.trim();
        if (isAlreadyLocalizedScript(trimmed)) {
            return reverseDevanagariToEnglish(trimmed);
        }
        if (!trimmed.isEmpty() && Character.isLowerCase(trimmed.charAt(0))) {
            return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
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

        String[] parts = canonical.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (parts[index].isEmpty()) continue;
            if (index > 0) result.append(" ");
            String wordLower = parts[index].toLowerCase(Locale.US);
            String wordExact = exactLocalizedName(wordLower, lang);
            if (wordExact != null) {
                result.append(wordExact);
            } else {
                result.append(phoneticTransliterate(parts[index], lang));
            }
        }
        String transliterated = result.toString();
        return transliterated.isEmpty() ? canonical : localizeDigits(transliterated);
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

        // lang: 0=English, 1=Hindi, 2=Marathi, 3=Gujarati, 4=Bengali, 5=Tamil, 6=Telugu, 7=Kannada, 8=Malayalam
        int offset = 0;
        switch (lang) {
            case 3: offset = 0x0180; break; // Gujarati (0x0A80)
            case 4: offset = 0x0080; break; // Bengali (0x0980)
            case 5: offset = 0x0280; break; // Tamil (0x0B80)
            case 6: offset = 0x0300; break; // Telugu (0x0C00)
            case 7: offset = 0x0380; break; // Kannada (0x0C80)
            case 8: offset = 0x0400; break; // Malayalam (0x0D00)
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

        String trimmed = stateName.trim();
        String[][] translations = {
                {"Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat", "Haryana",
                        "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
                        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
                        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
                        "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
                        "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"}, // 0: English
                {"आंध्र प्रदेश", "अरुणाचल प्रदेश", "असम", "बिहार", "छत्तीसगढ़", "गोवा", "गुजरात", "हरियाणा",
                        "हिमाचल प्रदेश", "झारखंड", "कर्नाटक", "केरल", "मध्य प्रदेश", "महाराष्ट्र", "मणिपुर",
                        "मेघालय", "मिज़ोरम", "नागालैंड", "ओडिशा", "पंजाब", "राजस्थान", "सिक्किम", "तमिलनाडु",
                        "तेलंगाना", "त्रिपुरा", "उत्तर प्रदेश", "उत्तराखंड", "पश्चिम बंगाल",
                        "अंडमान और निकोबार द्वीपसमूह", "चंडीगढ़", "दादरा और नगर हवेली तथा दमन और दीव", "दिल्ली",
                        "जम्मू और कश्मीर", "लद्दाख", "लक्षद्वीप", "पुदुचेरी"}, // 1: Hindi
                {"आंध्र प्रदेश", "अरुणाचल प्रदेश", "असम", "बिहार", "छत्तीसगढ़", "गोवा", "गुजरात", "हरियाणा",
                        "हिमाचल प्रदेश", "झारखंड", "कर्नाटक", "केरल", "मध्य प्रदेश", "महाराष्ट्र", "मणिपुर",
                        "मेघालय", "मिज़ोरम", "नागालैंड", "ओडिशा", "पंजाब", "राजस्थान", "सिक्किम", "तमिलनाडु",
                        "तेलंगाना", "त्रिपुरा", "उत्तर प्रदेश", "उत्तराखंड", "पश्चिम बंगाल",
                        "अंडमान और निकोबार द्वीपसमूह", "चंडीगढ़", "दादरा और नगर हवेली तथा दमन और दीव", "दिल्ली",
                        "जम्मू और कश्मीर", "लद्दाख", "लक्षद्वीप", "पुदुचेरी"}, // 2: Marathi
                {"આંધ્ર પ્રદેશ", "અరుణાચલ પ્રદેશ", "અસમ", "બિહાર", "છત્તીસગઢ", "ગોવા", "ગુજરાત", "હરિયાણા",
                        "હિમાચલ પ્રદેશ", "ઝારખંડ", "કર્ણાટક", "કેરળ", "મધ્યપ્રદેશ", "મહારાષ્ટ્ર", "મણિপুর",
                        "મેઘાલય", "મિઝોરમ", "નાગાલેન્ડ", "ઓડિશા", "પંજાબ", "રાજસ્થાન", "સિક્કિમ", "તમિલનાડુ",
                        "તેલંગાણા", "ત્રિપુરા", "ઉત્તર પ્રદેશ", "ઉત્તરાખંડ", "પશ્ચિમ બંગાળ",
                        "અંદમાન અને નિકોબાર ટાપુઓ", "ચંદીગઢ", "દાદરા અને નગર હવેલી અને દમણ અને દીવ", "દિલ્હી",
                        "જમ્મુ અને કાશ્મીર", "લદ્દાખ", "લક્ષદ્વીપ", "પુડુચેરી"}, // 3: Gujarati
                {"অন্ধ্র প্রদেশ", "অরুণাচল প্রদেশ", "আসাম", "বিহার", "ছত্তিশগড়", "গোয়া", "গুজরাত", "হরিয়ানা",
                        "হিমাচল প্রদেশ", "ঝাড়খণ্ড", "কর্ণাটক", "কেরালা", "মধ্যপ্রদেশ", "মহারাষ্ট্র", "মণিপুর",
                        "মেঘালয়", "মিজোরাম", "নাগাল্যান্ড", "ওড়িশা", "পাঞ্জাব", "রাজস্থান", "সিকিম", "তামিলনাড়ু",
                        "তেলেঙ্গানা", "ত্রিপুরা", "উত্তর প্রদেশ", "উত্তরাখণ্ড", "পশ্চিমবঙ্গ",
                        "আন্দামান ও নিকোবর দ্বীপপুঞ্জ", "চণ্ডীগড়", "দাদরা ও নগর হাভেলি ও দমন ও দিউ", "দিল্লি",
                        "জম্মু ও কাশ্মীর", "লাদাখ", "লাক্ষাদ্বীপ", "পুদুচেরি"}, // 4: Bengali
                {"தமிழ்நாடு", "அருணாச்சலப் பிரதேசம்", "அசாம்", "பீகார்", "சத்தீஸ்கர்", "கோவா", "குஜராத்", "அரியானா",
                        "இமாச்சலப் பிரதேசம்", "ஜார்கண்ட்", "கர்நாடகா", "கேரளா", "மத்தியப் பிரதேசம்", "மகாராஷ்டிரா", "மணிப்பூர்",
                        "மேகாலயா", "மிசோரம்", "நாகாலாந்து", "ஒடிசா", "பஞ்சாப்", "இராஜஸ்தான்", "சிக்கிம்", "தமிழ்நாடு",
                        "தெலங்கானா", "திரிபுரா", "உத்தரப் பிரதேசம்", "உத்தராகண்ட்டு", "மேற்கு வங்காளம்",
                        "அந்தமான் மற்றும் நிக்கோபார் தீவுகள்", "சண்டிகர்", "தாத்ரா மற்றும் நகர் ஹவேலி மற்றும் தாமன் மற்றும் தியூ", "தில்லி",
                        "ஜம்மு காஷ்மீர்", "லடாக்", "லட்சத்தீவு", "புதுச்சேரி"}, // 5: Tamil
                {"ఆంధ్ర ప్రదేశ్", "అరుణాచల్ ప్రదేశ్", "అస్సాం", "బీహార్", "చత్తీస్‌గఢ్", "గోవా", "గుజరాత్", "హర్యానా",
                        "హిమాచల్ ప్రదేశ్", "జార్ఖండ్", "కర్నాటక", "కేరళ", "మధ్య ప్రదేశ్", "మహారాష్ట్ర", "మనిపూర్",
                        "మేఘాలయ", "మిజోరం", "నాగాలాండ్", "ఒడిషా", "పంజాబ్", "రాజస్థాన్", "సిక్కిం", "తమిళనాడు",
                        "తెలంగాణ", "త్రిపుర", "ఉత్తరప్రదేశ్", "ఉత్తరాఖండ్", "పశ్చిమ బెంగాల్",
                        "అండమాన్ మరియు నికోబార్ ద్వీపాలు", "చండీఘర్", "దాద్రా మరియు నగర్ హవేలీ మరియు దామన్ మరియు డయ్యు", "ఢిల్లీ",
                        "జమ్మూ మరియు కాశ్మీర్", "లడఖ్", "లక్షద్వీప్", "పుదుచ్చేరి"}, // 6: Telugu
                {"ಆಂಧ್ರಪ್ರದೇಶ", "ಅರುಣಾಚಲ ಪ್ರದೇಶ", "ಅಸ್ಸಾಂ", "ಬಿಹಾರ", "ಛತ್ತೀಸ್‌ಗಢ", "ಗೋವಾ", "ಗುಜರಾತ್", "ಹರಿಯಾಣ",
                        "ಹಿಮಾಚಲ ಪ್ರದೇಶ", "ಜಾರ್ಖಂಡ್", "ಕರ್ನಾಟಕ", "ಕೇರಳ", "ಮಧ್ಯಪ್ರದೇಶ", "ಮಹಾರಾಷ್ಟ್ರ", "ಮಣಿಪುರ",
                        "ಮೇಘಾಲಯ", "ಮಿಜೋರಾಂ", "ನಾಗಾಲ್ಯಾಂಡ್", "ಒಡಿಸ್ಸಾ", "ಪಂಜಾಬ್", "ರಾಜಸ್ಥಾನ", "ಸಿಕ್ಕಿಂ", "ತಮಿಳುನಾಡು",
                        "ತೆಲಂಗಾಣ", "ತ್ರಿಪುರ", "ಉತ್ತರಪ್ರದೇಶ", "ಉತ್ತರಾಖಂಡ", "ಪಶ್ಚಿಮ ಬಂಗಾಳ",
                        "ಅಂಡಮಾನ್ ಮತ್ತು ನಿಕೋಬಾರ್ ದ್ವೀಪಗಳು", "ಚಂಡೀಘಢ", "ದಾದ್ರಾ ಮತ್ತು ನಗರ್ ಹವೇಲಿ ಮತ್ತು ದಾಮನ್ ಮತ್ತು ದಿಯು", "ದೆಹಲಿ",
                        "ಜಮ್ಮು ಮತ್ತು ಕಾಶ್ಮೀರ", "ಲಡಾಖ್", "ಲಕ್ಷದ್ವೀಪ", "ಪುದುಚೇರಿ"}, // 7: Kannada
                {"ആന്ധ്രാപ്രദേശ്", "അരുണാചൽ പ്രദേശ്", "അസ്സാം", "ബീഹാർ", "ഛത്തീസ്ഗഡ്", "ഗോവ", "ഗുജരാത്ത്", "ഹരിയാന",
                        "ഹിമാചൽ പ്രദേശ്", "ജാർഖണ്ഡ്", "കർണാടക", "കേരളം", "മധ്യപ്രദേശ്", "മഹാരാഷ്ട്ര", "മണിപൂർ",
                        "മേഘാലയ", "മിസോറാം", "നാഗാലാൻഡ്", "ഒഡീഷ", "പഞ്ചാബ്", "രാജസ്ഥാൻ", "സിക്കിം", "തമിഴ്നാട്",
                        "തെലങ്കാന", "ത്രിപുര", "ഉത്തർപ്രദേശ്", "ഉത്തരാഖണ്ഡ്", "പശ്ചിമ ബംഗാൾ",
                        "ആൻഡമാൻ നിക്കോബാർ ദ്വീപുകൾ", "ചണ്ഡിഗഡ്", "ദാദ്ര നഗർ ഹവേലി ദാമൻ ദിയു", "ഡൽഹി",
                        "ജമ്മു കാശ്മീർ", "ലഡാക്ക്", "ലക്ഷദ്വീപ്", "പുതുച്ചേരി"} // 8: Malayalam
        };

        int targetRow = selectedLanguage;
        if (targetRow >= translations.length) targetRow = 1;

        for (int index = 0; index < translations[0].length; index++) {
            if (trimmed.equalsIgnoreCase(translations[0][index])) {
                return translations[targetRow][index];
            }
        }
        return stateName;
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
                "൦൧൨൩൪൫൬൭൮൯", "੦੧੨੩੪੫੬੭੮੯", "୦୧୨୩૪୫୬୭୮୯", "০১২৩৪৫৬৭৮৯",
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
                "൦൧൨൩൪൫൬൭൮൯", "੦੧੨੩੪੫੬੭੮੯", "୦୧୨୩૪୫୬୭୮୯", "০১২৩৪৫६৭৮৯",
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
