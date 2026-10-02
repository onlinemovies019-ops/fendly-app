package com.example.fendly

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    private const val PREF_NAME = "fendly_language"
    private const val KEY_LANG_CODE = "selected_language_code"
    private const val KEY_LANG_INDEX = "selected_language_index"
    private val supportedLanguageCodes = arrayOf("en", "hi", "mr", "gu", "bn", "ta", "te", "kn", "ml")

    @JvmStatic
    fun normalizeLanguageCode(languageCode: String?): String {
        val code = languageCode?.trim()?.ifEmpty { "en" } ?: "en"
        return if (supportedLanguageCodes.contains(code)) code else "en"
    }

    @JvmStatic
    @Suppress("ApplySharedPref")
    fun setAppLanguage(context: Context, languageCode: String?) {
        val safeCode = normalizeLanguageCode(languageCode)
        val index = supportedLanguageCodes.indexOf(safeCode).takeIf { it >= 0 } ?: 0

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG_CODE, safeCode)
            .putInt(KEY_LANG_INDEX, index)
            .apply()

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(safeCode))
    }

    @JvmStatic
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return normalizeLanguageCode(prefs.getString(KEY_LANG_CODE, "en"))
    }

    @JvmStatic
    fun getSavedLanguageIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedCode = normalizeLanguageCode(prefs.getString(KEY_LANG_CODE, "en"))
        return supportedLanguageCodes.indexOf(savedCode).takeIf { it >= 0 } ?: 0
    }

    @JvmStatic
    fun profileText(context: Context, key: String): String {
        val languageIndex = getSavedLanguageIndex(context)
        val translations = when (key) {
            "logout" -> arrayOf("Log out", "लॉग आउट करें", "लॉग आउट करा", "લૉગ આઉટ કરો", "লগ আউট করুন", "வெளியேறு", "లాగ్ అవుట్ చేయండి", "ಲಾಗ್ ಔಟ್ ಮಾಡಿ", "ലോഗ് ഔട്ട് ചെയ്യുക")
            "logout_confirm" -> arrayOf(
                "Do you really want to log out of the app?", "क्या आप वाकई ऐप से लॉग आउट करना चाहते हैं?",
                "तुम्हाला खरोखर अॅपमधून लॉग आउट करायचे आहे का?", "શું તમે ખરેખર એપમાંથી લૉગ આઉટ કરવા માંગો છો?",
                "আপনি কি সত্যিই অ্যাপ থেকে লগ আউট করতে চান?", "செயலியிலிருந்து வெளியேற விரும்புகிறீர்களா?",
                "మీరు నిజంగా యాప్ నుండి లాగ్ అవుట్ చేయాలనుకుంటున్నారా?", "ನೀವು ನಿಜವಾಗಿಯೂ ಆ್ಯಪ್‌ನಿಂದ ಲಾಗ್ ಔಟ್ ಮಾಡಲು ಬಯಸುವಿರಾ?",
                "ആപ്പിൽ നിന്ന് ലോഗ് ഔട്ട് ചെയ്യണോ?"
            )
            "yes" -> arrayOf("Yes", "हाँ", "होय", "હા", "হ্যাঁ", "ஆம்", "అవును", "ಹೌದು", "അതെ")
            "no" -> arrayOf("No", "नहीं", "नाही", "ના", "না", "இல்லை", "కాదు", "ಇಲ್ಲ", "ഇല്ല")
            "no_notifications" -> arrayOf(
                "No notifications", "कोई सूचना नहीं", "कोणत्याही सूचना नाहीत", "કોઈ સૂચના નથી", "কোনো বিজ্ঞপ্তি নেই",
                "அறிவிப்புகள் இல்லை", "నోటిఫికేషన్లు లేవు", "ಯಾವುದೇ ಅಧಿಸೂಚನೆಗಳಿಲ್ಲ", "അറിയിപ്പുകളൊന്നുമില്ല"
            )
            "lost_theft_button" -> arrayOf(
                "LOST/THEFT", "खोया/चोरी", "हरवले/चोरी", "ખોવાયેલ/ચોરાયેલ", "হারানো/চুরি",
                "தொலைந்தது/திருடப்பட்டது", "కోల్పోయినవి/దొంగిలించబడినవి", "ಕಳೆದುಹೋದ/ಕಳವು", "നഷ്ടപ്പെട്ടു/മോഷണം"
            )
            "advanced_settings" -> arrayOf(
                "Advanced settings", "उन्नत सेटिंग्स", "प्रगत सेटिंग्ज", "અદ્યતન સેટિંગ્સ",
                "উন্নত সেটিংস", "மேம்பட்ட அமைப்புகள்", "అధునాతన సెట్టింగ్‌లు", "ಸುಧಾರಿತ ಸೆಟ್ಟಿಂಗ್‌ಗಳು", "വിപുലമായ ക്രമീകരണങ്ങൾ"
            )
            "imei_hint" -> arrayOf(
                "Protect this device against future loss or theft", "इस डिवाइस को भविष्य में खोने या चोरी होने से सुरक्षित रखें",
                "भविष्यात हे डिव्हाइस हरवल्यास किंवा चोरीला गेल्यास सुरक्षित ठेवा", "ભવિષ્યમાં આ ઉપકરણ ખોવાય કે ચોરાય તો તેને સુરક્ષિત રાખો",
                "ভবিষ্যতে ডিভাইসটি হারানো বা চুরি হওয়া থেকে সুরক্ষিত রাখুন", "எதிர்காலத்தில் இந்தச் சாதனம் தொலைந்தாலோ திருடப்பட்டாலோ பாதுகாக்கவும்",
                "భవిష్యత్తులో ఈ పరికరం పోయినా లేదా దొంగిలించబడినా రక్షించండి", "ಭವಿಷ್ಯದಲ್ಲಿ ಈ ಸಾಧನ ಕಳೆದುಹೋದರೆ ಅಥವಾ ಕಳವಾದರೆ ರಕ್ಷಿಸಿ",
                "ഭാവിയിൽ ഈ ഉപകരണം നഷ്ടപ്പെടുകയോ മോഷണം പോകുകയോ ചെയ്താൽ സംരക്ഷിക്കുക"
            )
            "imei_label" -> arrayOf(
                "IMEI number", "IMEI नंबर", "IMEI क्रमांक", "IMEI નંબર", "IMEI নম্বর", "IMEI எண்", "IMEI నంబర్", "IMEI ಸಂಖ್ಯೆ", "IMEI നമ്പർ"
            )
            "serial_label" -> arrayOf(
                "Mobile serial number", "मोबाइल सीरियल नंबर", "मोबाइल अनुक्रमांक", "મોબાઇલ સીરિયલ નંબર",
                "মোবাইল সিরিয়াল নম্বর", "மொபைல் வரிசை எண்", "మొబైల్ సీరియల్ నంబర్", "ಮೊಬೈಲ್ ಸರಣಿ ಸಂಖ್ಯೆ", "മൊബൈൽ സീരിയൽ നമ്പർ"
            )
            "scan_imei" -> arrayOf(
                "Scan IMEI", "IMEI स्कैन करें", "IMEI स्कॅन करा", "IMEI સ્કેન કરો", "IMEI স্ক্যান করুন",
                "IMEI-ஐ ஸ்கேன் செய்யவும்", "IMEI స్కాన్ చేయండి", "IMEI ಸ್ಕ್ಯಾನ್ ಮಾಡಿ", "IMEI സ്കാൻ ചെയ്യുക"
            )
            "scan_serial" -> arrayOf(
                "Scan mobile serial number", "मोबाइल सीरियल नंबर स्कैन करें", "मोबाइल अनुक्रमांक स्कॅन करा",
                "મોબાઇલ સીરિયલ નંબર સ્કેન કરો", "মোবাইল সিরিয়াল নম্বর স্ক্যান করুন", "மொபைல் வரிசை எண்ணை ஸ்கேன் செய்யவும்",
                "మొబైల్ సీరియల్ నంబర్ స్కాన్ చేయండి", "ಮೊಬೈಲ್ ಸರಣಿ ಸಂಖ್ಯೆಯನ್ನು ಸ್ಕ್ಯಾನ್ ಮಾಡಿ", "മൊബൈൽ സീരിയൽ നമ്പർ സ്കാൻ ചെയ്യുക"
            )
            "align_barcode" -> arrayOf(
                "Align barcode within the frame", "बारकोड को फ्रेम के भीतर रखें", "बारकोड चौकटीच्या आत ठेवा",
                "બારકોડને ફ્રેમની અંદર ગોઠવો", "বারকোডটি ফ্রেমের মধ্যে রাখুন", "பார்கோடை சட்டகத்திற்குள் வைக்கவும்",
                "బార్‌కోడ్‌ను ఫ్రేమ్‌లో ఉంచండి", "ಬಾರ್‌ಕೋಡ್ ಅನ್ನು ಚೌಕಟ್ಟಿನೊಳಗೆ ಇರಿಸಿ", "ബാർകോഡ് ഫ്രെയിമിനുള്ളിൽ വയ്ക്കുക"
            )
            "scanner_status" -> arrayOf(
                "Place a barcode inside the viewfinder rectangle to scan it.",
                "स्कैन करने के लिए बारकोड को व्यूफ़ाइंडर के आयत के अंदर रखें।",
                "स्कॅन करण्यासाठी बारकोड दृश्यचौकटीच्या आत ठेवा.",
                "સ્કેન કરવા માટે બારકોડને વ્યૂફાઇન્ડર ફ્રેમની અંદર મૂકો.",
                "স্ক্যান করতে বারকোডটি ভিউফাইন্ডার আয়তক্ষেত্রের মধ্যে রাখুন।",
                "ஸ்கேன் செய்ய பார்கோடை வ்யூஃபைண்டர் செவ்வகத்திற்குள் வைக்கவும்.",
                "స్కాన్ చేయడానికి బార్‌కోడ్‌ను వ్యూఫైండర్ చతురస్రం లోపల ఉంచండి.",
                "ಸ್ಕ್ಯಾನ್ ಮಾಡಲು ಬಾರ್‌ಕೋಡ್ ಅನ್ನು ವ್ಯೂಫೈಂಡರ್ ಆಯತದೊಳಗೆ ಇರಿಸಿ.",
                "സ്കാൻ ചെയ്യാൻ ബാർകോഡ് വ്യൂഫൈൻഡർ ചതുരത്തിനുള്ളിൽ വയ്ക്കുക."
            )
            "camera_error" -> arrayOf(
                "The camera encountered a problem. Please try again.", "कैमरे में समस्या आई। कृपया फिर से प्रयास करें।",
                "कॅमेऱ्यात समस्या आली. कृपया पुन्हा प्रयत्न करा.", "કેમેરામાં સમસ્યા આવી. કૃપા કરીને ફરી પ્રયાસ કરો.",
                "ক্যামেরায় সমস্যা হয়েছে। আবার চেষ্টা করুন।", "கேமராவில் சிக்கல் ஏற்பட்டது. மீண்டும் முயற்சிக்கவும்.",
                "కెమెరాలో సమస్య ఏర్పడింది. మళ్లీ ప్రయత్నించండి.", "ಕ್ಯಾಮೆರಾದಲ್ಲಿ ಸಮಸ್ಯೆ ಉಂಟಾಗಿದೆ. ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
                "ക്യാമറയിൽ പ്രശ്നമുണ്ടായി. വീണ്ടും ശ്രമിക്കുക."
            )
            "ok" -> arrayOf("OK", "ठीक है", "ठीक आहे", "બરાબર", "ঠিক আছে", "சரி", "సరే", "ಸರಿ", "ശരി")
            else -> return key
        }
        return translations[languageIndex]
    }

    @JvmStatic
    fun wrapContext(context: Context): Context {
        val safeCode = getSavedLanguage(context)
        val locale = Locale.forLanguageTag(safeCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return context.createConfigurationContext(config)
    }

    @JvmStatic
    fun restoreSavedLanguage(context: Context) {
        val safeCode = getSavedLanguage(context)
        val locale = Locale.forLanguageTag(safeCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
