package com.example.fendly;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LanguageActivity extends AppCompatActivity {
    private static final String STATE_SELECTED_LANGUAGE = "selected_language";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    private static final int GOLD = Color.rgb(232, 178, 74);
    private static final int GOLD_ON = Color.rgb(43, 29, 5);

    private boolean darkMode;

    private final String[][] languages = {
            {"English", "English", "en"},
            {"हिन्दी", "Hindi", "hi"},
            {"मराठी", "Marathi", "mr"},
            {"ગુજરાતી", "Gujarati", "gu"},
            {"বাংলা", "Bengali", "bn"},
            {"தமிழ்", "Tamil", "ta"},
            {"తెలుగు", "Telugu", "te"},
            {"ಕನ್ನಡ", "Kannada", "kn"},
            {"മലയാളം", "Malayalam", "ml"}
    };

    private final List<LinearLayout> languageCards = new ArrayList<>();
    private int selectedLanguage = 0;
    private GridLayout languageGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);

        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setStatusBarColor(backgroundColor());
        getWindow().setNavigationBarColor(backgroundColor());
        getWindow().getDecorView().setSystemUiVisibility(
                darkMode ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int windowWidthDp = WindowInsetsHelper.windowWidthDp(this);
        int windowHeightDp = WindowInsetsHelper.windowHeightDp(this);
        boolean compactHeight = windowHeightDp > 0 && windowHeightDp < 500;
        int horizontalPadding = windowWidthDp > 760
                ? dp((windowWidthDp - 760) / 2)
                : dp(22);
        root.setPadding(
                horizontalPadding,
                dp(compactHeight ? 12 : 22),
                horizontalPadding,
                dp(compactHeight ? 12 : 20));
        root.setBackgroundColor(backgroundColor());
        if (Build.VERSION.SDK_INT >= 29) root.setForceDarkAllowed(false);
        WindowInsetsHelper.applySafeArea(root);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.fendly_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setContentDescription("Fendly logo");
        int logoSize = dp(compactHeight ? 72 : 112);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.setMargins(0, 0, 0, dp(compactHeight ? 4 : 12));
        root.addView(logo, logoParams);

        TextView title = text("Choose your language", 23, primaryTextColor(), Typeface.NORMAL);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView subtitle = text("Make Fendly feel like home.", 12, secondaryTextColor(), Typeface.NORMAL);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLetterSpacing(.04f);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, dp(28));
        subtitleParams.setMargins(0, 0, 0, dp(16));
        root.addView(subtitle, subtitleParams);

        EditText search = new EditText(this);
        search.setHint("Search language");
        search.setHintTextColor(secondaryTextColor());
        search.setTextColor(primaryTextColor());
        search.setTextSize(14);
        search.setSingleLine(true);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setPadding(dp(16), 0, dp(16), 0);
        search.setBackground(roundWithStroke(surfaceColor(), 14, borderColor()));
        search.clearFocus();
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(48));
        searchParams.setMargins(0, 0, 0, dp(16));
        root.addView(search, searchParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        languageGrid = new GridLayout(this);
        languageGrid.setColumnCount(2);
        languageGrid.setUseDefaultMargins(false);
        scroll.addView(languageGrid, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        // Load saved language index
        if (savedInstanceState != null) {
            selectedLanguage = savedInstanceState.getInt(STATE_SELECTED_LANGUAGE, 0);
        } else {
            String currentCode = LanguageManager.getSavedLanguage(this);
            for (int i = 0; i < languages.length; i++) {
                if (languages[i][2].equalsIgnoreCase(currentCode)) {
                    selectedLanguage = i;
                    break;
                }
            }
        }

        for (int index = 0; index < languages.length; index++) {
            addLanguageCard(index);
        }
        updateSelection();

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                filterLanguages(value.toString());
            }
            @Override public void afterTextChanged(Editable value) { }
        });

        TextView continueButton = text("Continue", 14, GOLD_ON, Typeface.NORMAL);
        continueButton.setGravity(Gravity.CENTER);
        continueButton.setBackground(round(GOLD, 24));
        continueButton.setElevation(dp(5));
        continueButton.setOnClickListener(view -> continueToApp());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(52));
        buttonParams.setMargins(0, dp(16), 0, 0);
        root.addView(continueButton, buttonParams);

        setContentView(root);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_LANGUAGE, selectedLanguage);
        super.onSaveInstanceState(outState);
    }

    private void addLanguageCard(int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(12), dp(10), dp(10));

        // Selection update on card tap (No premature recreate)
        card.setOnClickListener(view -> {
            selectedLanguage = index;
            updateSelection();
        });

        languageCards.add(card);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(index / 2, 1), GridLayout.spec(index % 2, 1, 1f));
        params.width = 0;
        params.height = dp(100);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        languageGrid.addView(card, params);

        TextView nativeName = text(languages[index][0], languages[index][0].length() > 6 ? 16 : 19, primaryTextColor(), Typeface.NORMAL);
        nativeName.setGravity(Gravity.CENTER);
        nativeName.setSingleLine(true);
        nativeName.setIncludeFontPadding(false);
        card.addView(nativeName, new LinearLayout.LayoutParams(-1, dp(36)));

        TextView englishName = text(languages[index][1], 11, secondaryTextColor(), Typeface.NORMAL);
        englishName.setGravity(Gravity.CENTER);
        card.addView(englishName, new LinearLayout.LayoutParams(-1, dp(24)));
    }

    private void updateSelection() {
        for (int index = 0; index < languageCards.size(); index++) {
            boolean selected = index == selectedLanguage;
            languageCards.get(index).setBackground(roundWithStroke(
                    selected ? GOLD : surfaceColor(), 16, selected ? GOLD : borderColor()));
            ViewGroup group = languageCards.get(index);
            TextView nativeName = (TextView) group.getChildAt(0);
            TextView englishName = (TextView) group.getChildAt(1);
            nativeName.setTextColor(selected ? GOLD_ON : primaryTextColor());
            englishName.setTextColor(selected ? Color.rgb(92, 63, 13) : secondaryTextColor());
        }
    }

    private void filterLanguages(String query) {
        String normalized = query.trim().toLowerCase(Locale.US);
        for (int index = 0; index < languageCards.size(); index++) {
            boolean matches = normalized.isEmpty()
                    || languages[index][0].toLowerCase(Locale.US).contains(normalized)
                    || languages[index][1].toLowerCase(Locale.US).contains(normalized);
            languageCards.get(index).setVisibility(matches ? View.VISIBLE : View.GONE);
        }
    }

    private void continueToApp() {
        String code = languages[selectedLanguage][2];

        getSharedPreferences("fendly_language", MODE_PRIVATE)
                .edit()
                .putInt("selected_language_index", selectedLanguage)
                .apply();

        LanguageManager.setAppLanguage(this, code);

        // Instantly finish LanguageActivity and show MainActivity without window layer recreation lag
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        Typeface tf = typefaceForText(value, Typeface.NORMAL);
        view.setTypeface(tf, Typeface.NORMAL);
        view.setIncludeFontPadding(false);
        view.setLineSpacing(0, 1.1f);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private Typeface typefaceForText(String value, int style) {
        if (value == null) return Typeface.create(Typeface.SANS_SERIF, style);

        String assetPath = resolveFontAssetPath(value);
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

    private String resolveFontAssetPath(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        if (containsUnicodeRange(text, 0x0980, 0x09FF)) {
            return "fonts/NotoSansBengali[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0B80, 0x0BFF)) {
            return "fonts/NotoSansTamil[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0D00, 0x0D7F)) {
            return "fonts/NotoSansMalayalam[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0C00, 0x0C7F)) {
            return "fonts/NotoSansTelugu[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0900, 0x097F)) {
            return "fonts/NotoSansDevanagari[wdth,wght].ttf";
        } else if (containsUnicodeRange(text, 0x0C80, 0x0CFF)) {
            return "fonts/NotoSansKannada[wdth,wght].ttf";
        }
        return null;
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

    private int backgroundColor() {
        return darkMode ? Color.rgb(18, 19, 25) : Color.rgb(247, 243, 238);
    }

    private int surfaceColor() {
        return darkMode ? Color.rgb(24, 29, 36) : Color.rgb(253, 251, 248);
    }

    private int borderColor() {
        return darkMode ? Color.rgb(50, 57, 67) : Color.rgb(216, 208, 196);
    }

    private int primaryTextColor() {
        return darkMode ? Color.rgb(247, 249, 252) : Color.rgb(18, 18, 18);
    }

    private int secondaryTextColor() {
        return darkMode ? Color.rgb(170, 177, 188) : Color.rgb(77, 80, 90);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}