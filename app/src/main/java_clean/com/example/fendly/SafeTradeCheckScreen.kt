package com.example.fendly

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.example.fendly.ui.theme.FendlyCard
import com.example.fendly.ui.theme.FendlySecondaryButton
import com.example.fendly.ui.theme.FendlyTextField
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

internal fun imeiFromBarcodePayload(value: String): String? {
    Regex("\\d{15,}").find(value)?.value?.let { return it.take(15) }
    return Regex("(?<!\\d)(?:\\d[\\s-]?){14}\\d(?!\\d)")
        .find(value)
        ?.value
        ?.filter { it in '0'..'9' }
        ?.takeIf { it.length == 15 }
}

private fun safeTradeText(context: android.content.Context, key: String): String {
    val languageIndex = LanguageManager.getSavedLanguageIndex(context)
    val translations = when (key) {
        "title" -> arrayOf(
            "SafeTrade IMEI check", "SafeTrade IMEI जांच", "SafeTrade IMEI तपासणी", "SafeTrade IMEI તપાસ",
            "SafeTrade IMEI যাচাই", "SafeTrade IMEI சரிபார்ப்பு", "SafeTrade IMEI తనిఖీ", "SafeTrade IMEI ಪರಿಶೀಲನೆ",
            "SafeTrade IMEI പരിശോധന",
        )
        "heading" -> arrayOf(
            "Check before you buy", "खरीदने से पहले जांचें", "खरेदी करण्यापूर्वी तपासा", "ખરીદતા પહેલાં તપાસો",
            "কেনার আগে যাচাই করুন", "வாங்குவதற்கு முன் சரிபார்க்கவும்", "కొనుగోలు చేసే ముందు తనిఖీ చేయండి",
            "ಖರೀದಿಸುವ ಮೊದಲು ಪರಿಶೀಲಿಸಿ", "വാങ്ങുന്നതിന് മുമ്പ് പരിശോധിക്കുക",
        )
        "instruction" -> arrayOf(
            "Enter the 15-digit IMEI shown on the device or its box.",
            "डिवाइस या उसके बॉक्स पर दिया गया 15 अंकों का IMEI दर्ज करें।",
            "डिव्हाइसवर किंवा बॉक्सवर दिलेला 15 अंकी IMEI टाका.",
            "ડિવાઇસ અથવા તેના બોક્સ પર દર્શાવેલ 15 અંકનો IMEI દાખલ કરો.",
            "ডিভাইস বা বাক্সে থাকা ১৫ সংখ্যার IMEI লিখুন।",
            "சாதனம் அல்லது அதன் பெட்டியில் உள்ள 15 இலக்க IMEI-ஐ உள்ளிடவும்.",
            "పరికరం లేదా దాని పెట్టెపై ఉన్న 15 అంకెల IMEIని నమోదు చేయండి.",
            "ಸಾಧನ ಅಥವಾ ಅದರ ಬಾಕ್ಸ್‌ನಲ್ಲಿರುವ 15 ಅಂಕಿಯ IMEI ನಮೂದಿಸಿ.",
            "ഉപകരണത്തിലോ അതിന്റെ ബോക്സിലോ ഉള്ള 15 അക്ക IMEI നൽകുക.",
        )
        "imei_label" -> arrayOf(
            "15-digit IMEI", "15 अंकों का IMEI", "15 अंकी IMEI", "15 અંકનો IMEI", "১৫ সংখ্যার IMEI",
            "15 இலக்க IMEI", "15 అంకెల IMEI", "15 ಅಂಕಿಯ IMEI", "15 അക്ക IMEI",
        )
        "digits_count" -> arrayOf(
            "{count}/15 digits", "{count}/15 अंक", "{count}/15 अंक", "{count}/15 અંક",
            "{count}/15 সংখ্যা", "{count}/15 இலக்கங்கள்", "{count}/15 అంకెలు",
            "{count}/15 ಅಂಕಿಗಳು", "{count}/15 അക്കങ്ങൾ",
        )
        "about" -> arrayOf(
            "ABOUT THIS CHECK", "इस जांच के बारे में", "या तपासणीबद्दल", "આ તપાસ વિશે", "এই যাচাই সম্পর্কে",
            "இந்தச் சரிபார்ப்பைப் பற்றி", "ఈ తనిఖీ గురించి", "ಈ ಪರಿಶೀಲನೆಯ ಬಗ್ಗೆ", "ഈ പരിശോധനയെക്കുറിച്ച്",
        )
        "disclaimer" -> arrayOf(
            "SafeTrade checks device IMEIs against reported lost/stolen databases to protect buyers. By searching, you agree to our terms.",
            "SafeTrade खरीदारों की सुरक्षा के लिए डिवाइस IMEI को खोए या चोरी हुए उपकरणों की रिपोर्ट से जांचता है। खोज करके आप हमारी शर्तों से सहमत होते हैं।",
            "खरेदीदारांच्या संरक्षणासाठी SafeTrade डिव्हाइस IMEI ची हरवलेल्या किंवा चोरीच्या नोंदींशी तपासणी करते. शोध घेतल्याने तुम्ही आमच्या अटी मान्य करता.",
            "ખરીદદારોની સુરક્ષા માટે SafeTrade ડિવાઇસના IMEIની ખોવાયેલા અથવા ચોરાયેલા ઉપકરણોની નોંધ સાથે તપાસ કરે છે. શોધ કરીને તમે અમારી શરતો સ્વીકારો છો.",
            "ক্রেতাদের সুরক্ষায় SafeTrade ডিভাইসের IMEI হারানো বা চুরি হওয়া ডিভাইসের রিপোর্টের সঙ্গে মিলিয়ে দেখে। অনুসন্ধান করলে আপনি আমাদের শর্তে সম্মত হন।",
            "வாங்குபவர்களைப் பாதுகாக்க, SafeTrade சாதன IMEI-ஐ தொலைந்த/திருடப்பட்ட சாதன அறிக்கைகளுடன் சரிபார்க்கிறது. தேடுவதன் மூலம் எங்கள் விதிமுறைகளை ஏற்கிறீர்கள்.",
            "కొనుగోలుదారుల రక్షణ కోసం SafeTrade పరికరం IMEIని పోయిన/దొంగిలించబడిన పరికరాల నివేదికలతో తనిఖీ చేస్తుంది. శోధించడం ద్వారా మీరు మా నిబంధనలను అంగీకరిస్తారు.",
            "ಖರೀದಿದಾರರ ರಕ್ಷಣೆಗಾಗಿ SafeTrade ಸಾಧನದ IMEI ಅನ್ನು ಕಳೆದುಹೋದ/ಕಳವಾದ ಸಾಧನಗಳ ವರದಿಗಳೊಂದಿಗೆ ಪರಿಶೀಲಿಸುತ್ತದೆ. ಹುಡುಕುವ ಮೂಲಕ ನೀವು ನಮ್ಮ ನಿಯಮಗಳಿಗೆ ಒಪ್ಪುತ್ತೀರಿ.",
            "വാങ്ങുന്നവരെ സംരക്ഷിക്കാൻ SafeTrade ഉപകരണ IMEI നഷ്ടപ്പെട്ട/മോഷ്ടിക്കപ്പെട്ട ഉപകരണങ്ങളുടെ റിപ്പോർട്ടുകളുമായി പരിശോധിക്കുന്നു. തിരയുന്നതിലൂടെ നിങ്ങൾ നിബന്ധനകൾ അംഗീകരിക്കുന്നു.",
        )
        "scan" -> arrayOf(
            "Scan IMEI", "IMEI स्कैन करें", "IMEI स्कॅन करा", "IMEI સ્કેન કરો",
            "IMEI স্ক্যান করুন", "IMEI-ஐ ஸ்கேன் செய்யவும்", "IMEI స్కాన్ చేయండి",
            "IMEI ಸ್ಕ್ಯಾನ್ ಮಾಡಿ", "IMEI സ്കാൻ ചെയ്യുക",
        )
        "camera_denied" -> arrayOf(
            "Camera access was denied. You can still enter the IMEI manually.",
            "कैमरा अनुमति नहीं मिली। आप IMEI मैन्युअल रूप से दर्ज कर सकते हैं।",
            "कॅमेरा परवानगी नाकारली. तुम्ही IMEI स्वतः टाकू शकता.",
            "કેમેરાની મંજૂરી નકારવામાં આવી. તમે IMEI જાતે દાખલ કરી શકો છો.",
            "ক্যামেরার অনুমতি দেওয়া হয়নি। আপনি হাতে IMEI লিখতে পারেন।",
            "கேமரா அனுமதி மறுக்கப்பட்டது. IMEI-ஐ கைமுறையாக உள்ளிடலாம்.",
            "కెమెరా అనుమతి నిరాకరించబడింది. మీరు IMEIని చేతితో నమోదు చేయవచ్చు.",
            "ಕ್ಯಾಮೆರಾ ಅನುಮತಿ ನಿರಾಕರಿಸಲಾಗಿದೆ. IMEI ಅನ್ನು ಕೈಯಾರೆ ನಮೂದಿಸಬಹುದು.",
            "ക്യാമറ അനുമതി നിഷേധിച്ചു. IMEI സ്വമേധയാ നൽകാം.",
        )
        "verify" -> arrayOf(
            "Verify IMEI", "IMEI सत्यापित करें", "IMEI सत्यापित करा", "IMEI ચકાસો", "IMEI যাচাই করুন",
            "IMEI-ஐ சரிபார்க்கவும்", "IMEIని ధృవీకరించండి", "IMEI ಪರಿಶೀಲಿಸಿ", "IMEI പരിശോധിക്കുക",
        )
        "back" -> arrayOf(
            "Back home", "होम पर वापस जाएं", "मुख्यपृष्ठावर परत जा", "હોમ પર પાછા જાઓ", "হোমে ফিরে যান",
            "முகப்புக்குத் திரும்பு", "హోమ్‌కు తిరిగి వెళ్లండి", "ಮುಖಪುಟಕ್ಕೆ ಹಿಂತಿರುಗಿ", "ഹോമിലേക്ക് മടങ്ങുക",
        )
        "clean" -> arrayOf(
            "No active loss reports were found for this device.",
            "इस डिवाइस के लिए गुम होने की कोई सक्रिय रिपोर्ट नहीं मिली।",
            "या डिव्हाइससाठी हरवल्याची कोणतीही सक्रिय नोंद आढळली नाही.",
            "આ ડિવાઇસ માટે ખોવાયેલ હોવાની કોઈ સક્રિય નોંધ મળી નથી.",
            "এই ডিভাইসের জন্য হারানোর কোনো সক্রিয় রিপোর্ট পাওয়া যায়নি।",
            "இந்தச் சாதனத்திற்கு தொலைந்ததாகச் செயலில் உள்ள அறிக்கைகள் எதுவும் இல்லை.",
            "ఈ పరికరానికి సంబంధించిన క్రియాశీల పోయినట్లు నివేదికలు కనుగొనబడలేదు.",
            "ಈ ಸಾಧನಕ್ಕೆ ಸಂಬಂಧಿಸಿದ ಸಕ್ರಿಯ ಕಳೆದುಹೋದ ವರದಿಗಳು ಕಂಡುಬಂದಿಲ್ಲ.",
            "ഈ ഉപകരണത്തിന് സജീവമായ നഷ്ട റിപ്പോർട്ടുകളൊന്നും കണ്ടെത്തിയില്ല.",
        )
        "flagged" -> arrayOf(
            "This device is currently reported missing. Do not complete purchase.",
            "यह डिवाइस अभी गुम होने की रिपोर्ट में है। खरीद पूरी न करें।",
            "हे डिव्हाइस सध्या हरवल्याची नोंद आहे. खरेदी पूर्ण करू नका.",
            "આ ડિવાઇસ હાલમાં ખોવાયેલ તરીકે નોંધાયેલ છે. ખરીદી પૂર્ણ કરશો નહીં.",
            "এই ডিভাইসটি বর্তমানে হারানো হিসেবে রিপোর্ট করা হয়েছে। কেনাকাটা সম্পন্ন করবেন না।",
            "இந்தச் சாதனம் தற்போது காணாமல் போனதாகப் புகாரளிக்கப்பட்டுள்ளது. வாங்குவதை முடிக்க வேண்டாம்.",
            "ఈ పరికరం ప్రస్తుతం పోయినట్లు నివేదించబడింది. కొనుగోలును పూర్తి చేయవద్దు.",
            "ಈ ಸಾಧನವು ಪ್ರಸ್ತುತ ಕಾಣೆಯಾಗಿದೆ ಎಂದು ವರದಿಯಾಗಿದೆ. ಖರೀದಿಯನ್ನು ಪೂರ್ಣಗೊಳಿಸಬೇಡಿ.",
            "ഈ ഉപകരണം കാണാതായതായി റിപ്പോർട്ട് ചെയ്തിട്ടുണ്ട്. വാങ്ങൽ പൂർത്തിയാക്കരുത്.",
        )
        "unexpected" -> arrayOf(
            "The verification service returned an unexpected result.",
            "सत्यापन सेवा से अनपेक्षित परिणाम मिला।", "तपासणी सेवेकडून अनपेक्षित परिणाम मिळाला.",
            "ચકાસણી સેવાએ અનપેક્ષિત પરિણામ આપ્યું.", "যাচাই পরিষেবা অপ্রত্যাশিত ফল দিয়েছে।",
            "சரிபார்ப்பு சேவை எதிர்பாராத முடிவை அளித்தது.", "ధృవీకరణ సేవ ఊహించని ఫలితాన్ని ఇచ్చింది.",
            "ಪರಿಶೀಲನಾ ಸೇವೆಯು ನಿರೀಕ್ಷಿಸದ ಫಲಿತಾಂಶವನ್ನು ನೀಡಿದೆ.", "പരിശോധനാ സേവനം പ്രതീക്ഷിക്കാത്ത ഫലം നൽകി.",
        )
        "rate_limit" -> arrayOf(
            "Too many checks. Please wait a moment and try again.",
            "बहुत अधिक जांचें हुईं। थोड़ी देर बाद फिर कोशिश करें।",
            "खूप तपासण्या झाल्या. थोडा वेळ थांबून पुन्हा प्रयत्न करा.",
            "ઘણી બધી તપાસ થઈ છે. થોડી વાર પછી ફરી પ્રયાસ કરો.",
            "অনেকবার যাচাই করা হয়েছে। একটু অপেক্ষা করে আবার চেষ্টা করুন।",
            "பலமுறை சரிபார்க்கப்பட்டுள்ளது. சிறிது நேரம் காத்திருந்து மீண்டும் முயற்சிக்கவும்.",
            "చాలా తనిఖీలు జరిగాయి. కొద్దిసేపు వేచి మళ్లీ ప్రయత్నించండి.",
            "ಹಲವು ಬಾರಿ ಪರಿಶೀಲಿಸಲಾಗಿದೆ. ಸ್ವಲ್ಪ ಸಮಯ ಕಾಯ್ದು ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
            "നിരവധി പരിശോധനകൾ നടത്തി. അൽപ്പം കാത്ത് വീണ്ടും ശ്രമിക്കുക.",
        )
        "invalid" -> arrayOf(
            "Enter a valid 15-digit IMEI.", "सही 15 अंकों का IMEI दर्ज करें।", "वैध 15 अंकी IMEI टाका.",
            "માન્ય 15 અંકનો IMEI દાખલ કરો.", "সঠিক ১৫ সংখ্যার IMEI লিখুন।",
            "சரியான 15 இலக்க IMEI-ஐ உள்ளிடவும்.", "చెల్లుబాటు అయ్యే 15 అంకెల IMEIని నమోదు చేయండి.",
            "ಮಾನ್ಯವಾದ 15 ಅಂಕಿಯ IMEI ನಮೂದಿಸಿ.", "സാധുവായ 15 അക്ക IMEI നൽകുക.",
        )
        "verify_error" -> arrayOf(
            "Could not verify this IMEI right now. Please try again.",
            "अभी इस IMEI की जांच नहीं हो सकी। फिर कोशिश करें।",
            "आत्ता या IMEI ची तपासणी करता आली नाही. पुन्हा प्रयत्न करा.",
            "હમણાં આ IMEI ચકાસી શકાયો નથી. ફરી પ્રયાસ કરો.",
            "এখন এই IMEI যাচাই করা যায়নি। আবার চেষ্টা করুন।",
            "தற்போது இந்த IMEI-ஐ சரிபார்க்க முடியவில்லை. மீண்டும் முயற்சிக்கவும்.",
            "ప్రస్తుతం ఈ IMEIని తనిఖీ చేయలేకపోయాం. మళ్లీ ప్రయత్నించండి.",
            "ಈಗ ಈ IMEI ಪರಿಶೀಲಿಸಲಾಗಲಿಲ್ಲ. ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
            "ഇപ്പോൾ ഈ IMEI പരിശോധിക്കാനായില്ല. വീണ്ടും ശ്രമിക്കുക.",
        )
        "network_error" -> arrayOf(
            "Could not connect to SafeTrade. Check your connection and try again.",
            "SafeTrade से कनेक्ट नहीं हो सका। इंटरनेट जांचें और फिर कोशिश करें।",
            "SafeTrade शी जोडता आले नाही. इंटरनेट तपासून पुन्हा प्रयत्न करा.",
            "SafeTrade સાથે જોડાઈ શકાયું નથી. કનેક્શન તપાસીને ફરી પ્રયાસ કરો.",
            "SafeTrade-এ সংযোগ করা যায়নি। সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।",
            "SafeTrade-ஐ இணைக்க முடியவில்லை. இணைய இணைப்பைச் சரிபார்த்து மீண்டும் முயற்சிக்கவும்.",
            "SafeTradeకు కనెక్ట్ కాలేదు. కనెక్షన్ తనిఖీ చేసి మళ్లీ ప్రయత్నించండి.",
            "SafeTrade ಗೆ ಸಂಪರ್ಕಿಸಲಾಗಲಿಲ್ಲ. ಸಂಪರ್ಕವನ್ನು ಪರಿಶೀಲಿಸಿ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
            "SafeTrade-ലേക്ക് ബന്ധിപ്പിക്കാനായില്ല. കണക്ഷൻ പരിശോധിച്ച് വീണ്ടും ശ്രമിക്കുക.",
        )
        "scanner_error" -> arrayOf(
            "The barcode scanner could not start. Enter the IMEI manually.",
            "बारकोड स्कैनर शुरू नहीं हो सका। IMEI मैन्युअल रूप से दर्ज करें।",
            "बारकोड स्कॅनर सुरू होऊ शकला नाही. IMEI स्वतः टाका.",
            "બારકોડ સ્કેનર શરૂ થઈ શક્યું નથી. IMEI જાતે દાખલ કરો.",
            "বারকোড স্ক্যানার চালু করা যায়নি। হাতে IMEI লিখুন।",
            "பார்கோடு ஸ்கேனரைத் தொடங்க முடியவில்லை. IMEI-ஐ கைமுறையாக உள்ளிடவும்.",
            "బార్‌కోడ్ స్కానర్ ప్రారంభం కాలేదు. IMEIని చేతితో నమోదు చేయండి.",
            "ಬಾರ್‌ಕೋಡ್ ಸ್ಕ್ಯಾನರ್ ಪ್ರಾರಂಭಿಸಲಾಗಲಿಲ್ಲ. IMEI ಅನ್ನು ಕೈಯಾರೆ ನಮೂದಿಸಿ.",
            "ബാർകോഡ് സ്കാനർ ആരംഭിക്കാനായില്ല. IMEI സ്വമേധയാ നൽകുക.",
        )
        "camera_prompt" -> arrayOf(
            "Scan device IMEI", "डिवाइस का IMEI स्कैन करें", "डिव्हाइसचा IMEI स्कॅन करा",
            "ડિવાઇસનો IMEI સ્કેન કરો", "ডিভাইসের IMEI স্ক্যান করুন", "சாதன IMEI-ஐ ஸ்கேன் செய்யவும்",
            "పరికరం IMEIని స్కాన్ చేయండి", "ಸಾಧನದ IMEI ಸ್ಕ್ಯಾನ್ ಮಾಡಿ", "ഉപകരണ IMEI സ്കാൻ ചെയ്യുക",
        )
        "scan_status" -> arrayOf(
            "Point at the IMEI barcode and hold steady.", "IMEI बारकोड पर कैमरा रखें और स्थिर रखें।",
            "IMEI बारकोडकडे कॅमेरा धरून स्थिर ठेवा.", "IMEI બારકોડ તરફ કેમેરો રાખીને સ્થિર રહો.",
            "IMEI বারকোডের দিকে ক্যামেরা তাক করে স্থির রাখুন।", "IMEI பார்கோடை நோக்கி கேமராவை வைத்து அசையாமல் பிடிக்கவும்.",
            "IMEI బార్‌కోడ్ వైపు కెమెరాను ఉంచి స్థిరంగా పట్టుకోండి.", "IMEI ಬಾರ್‌ಕೋಡ್ ಕಡೆಗೆ ಕ್ಯಾಮೆರಾ ಹಿಡಿದು ಸ್ಥಿರವಾಗಿರಿ.",
            "IMEI ബാർകോഡിലേക്ക് ക്യാമറ ചൂണ്ടി അനക്കാതെ പിടിക്കുക.",
        )
        "scan_help" -> arrayOf(
            "A valid 15-digit IMEI fills the field automatically. If needed, move closer and keep the barcode clear.",
            "सही 15 अंकों का IMEI अपने आप भरेगा। जरूरत हो तो पास जाएं और बारकोड साफ रखें।",
            "वैध 15 अंकी IMEI आपोआप भरेल. गरज असल्यास जवळ जा आणि बारकोड स्पष्ट ठेवा.",
            "માન્ય 15 અંકનો IMEI આપમેળે ભરાશે. જરૂર પડે તો નજીક જાઓ અને બારકોડ સ્પષ્ટ રાખો.",
            "সঠিক ১৫ সংখ্যার IMEI নিজে থেকে পূরণ হবে। প্রয়োজনে কাছে যান এবং বারকোড পরিষ্কার রাখুন।",
            "சரியான 15 இலக்க IMEI தானாக நிரப்பப்படும். தேவைப்பட்டால் அருகில் சென்று பார்கோடை தெளிவாக வைக்கவும்.",
            "సరైన 15 అంకెల IMEI ఆటోమేటిక్‌గా నింపబడుతుంది. అవసరమైతే దగ్గరకు వెళ్లి బార్‌కోడ్ స్పష్టంగా ఉంచండి.",
            "ಮಾನ್ಯವಾದ 15 ಅಂಕಿಯ IMEI ಸ್ವಯಂಚಾಲಿತವಾಗಿ ತುಂಬುತ್ತದೆ. ಅಗತ್ಯವಿದ್ದರೆ ಹತ್ತಿರ ಹೋಗಿ ಬಾರ್‌ಕೋಡ್ ಸ್ಪಷ್ಟವಾಗಿರಲಿ.",
            "സാധുവായ 15 അക്ക IMEI തനിയെ പൂരിപ്പിക്കും. ആവശ്യമെങ്കിൽ അടുത്തേക്ക് നീങ്ങി ബാർകോഡ് വ്യക്തമായി കാണിക്കുക.",
        )
        "barcode_other" -> arrayOf(
            "Barcode detected, but it doesn't contain a 15-digit IMEI. Try the IMEI barcode on the device box.",
            "बारकोड मिला, लेकिन उसमें 15 अंकों का IMEI नहीं है। डिवाइस बॉक्स पर IMEI बारकोड आज़माएं।",
            "बारकोड आढळला, पण त्यात 15 अंकी IMEI नाही. डिव्हाइस बॉक्सवरील IMEI बारकोड वापरून पाहा.",
            "બારકોડ મળ્યો, પરંતુ તેમાં 15 અંકનો IMEI નથી. ડિવાઇસ બોક્સ પરનો IMEI બારકોડ અજમાવો.",
            "বারকোড পাওয়া গেছে, তবে এতে ১৫ সংখ্যার IMEI নেই। ডিভাইসের বাক্সের IMEI বারকোড চেষ্টা করুন।",
            "பார்கோடு கண்டறியப்பட்டது, ஆனால் அதில் 15 இலக்க IMEI இல்லை. சாதனப் பெட்டியில் உள்ள IMEI பார்கோடை முயற்சிக்கவும்.",
            "బార్‌కోడ్ గుర్తించబడింది, కానీ అందులో 15 అంకెల IMEI లేదు. పరికరం పెట్టెపై ఉన్న IMEI బార్‌కోడ్‌ను ప్రయత్నించండి.",
            "ಬಾರ್‌ಕೋಡ್ ಪತ್ತೆಯಾಗಿದೆ, ಆದರೆ ಅದರಲ್ಲಿ 15 ಅಂಕಿಯ IMEI ಇಲ್ಲ. ಸಾಧನದ ಬಾಕ್ಸ್‌ನಲ್ಲಿರುವ IMEI ಬಾರ್‌ಕೋಡ್ ಪ್ರಯತ್ನಿಸಿ.",
            "ബാർകോഡ് കണ്ടെത്തി, പക്ഷേ അതിൽ 15 അക്ക IMEI ഇല്ല. ഉപകരണ ബോക്സിലെ IMEI ബാർകോഡ് പരീക്ഷിക്കുക.",
        )
        "barcode_read_error" -> arrayOf(
            "Could not read that barcode. Hold it steady and try again.",
            "बारकोड पढ़ा नहीं जा सका। स्थिर रखें और फिर कोशिश करें।",
            "बारकोड वाचता आला नाही. स्थिर धरून पुन्हा प्रयत्न करा.",
            "બારકોડ વાંચી શકાયો નથી. સ્થિર રાખીને ફરી પ્રયાસ કરો.",
            "বারকোড পড়া যায়নি। স্থির ধরে আবার চেষ্টা করুন।",
            "பார்கோடைப் படிக்க முடியவில்லை. அசையாமல் பிடித்து மீண்டும் முயற்சிக்கவும்.",
            "బార్‌కోడ్ చదవలేకపోయాం. స్థిరంగా ఉంచి మళ్లీ ప్రయత్నించండి.",
            "ಬಾರ್‌ಕೋಡ್ ಓದಲಾಗಲಿಲ್ಲ. ಸ್ಥಿರವಾಗಿ ಹಿಡಿದು ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
            "ബാർകോഡ് വായിക്കാനായില്ല. അനക്കാതെ പിടിച്ച് വീണ്ടും ശ്രമിക്കുക.",
        )
        "barcode_not_read" -> arrayOf(
            "No barcode was read. Try again with the barcode in focus, or enter the IMEI manually.",
            "कोई बारकोड नहीं पढ़ा गया। बारकोड को स्पष्ट करके फिर कोशिश करें या IMEI स्वयं दर्ज करें।",
            "बारकोड वाचता आला नाही. स्पष्ट बारकोडसह पुन्हा प्रयत्न करा किंवा IMEI स्वतः टाका.",
            "કોઈ બારકોડ વાંચી શકાયો નથી. બારકોડ સ્પષ્ટ કરીને ફરી પ્રયાસ કરો અથવા IMEI જાતે દાખલ કરો.",
            "কোনো বারকোড পড়া যায়নি। বারকোড স্পষ্ট রেখে আবার চেষ্টা করুন, অথবা IMEI হাতে লিখুন।",
            "பார்கோடு படிக்கப்படவில்லை. தெளிவாக வைத்து மீண்டும் முயற்சிக்கவும் அல்லது IMEI-ஐ கைமுறையாக உள்ளிடவும்.",
            "బార్‌కోడ్ చదవబడలేదు. స్పష్టంగా కనిపించేలా ఉంచి మళ్లీ ప్రయత్నించండి లేదా IMEIని చేతితో నమోదు చేయండి.",
            "ಬಾರ್‌ಕೋಡ್ ಓದಲಾಗಲಿಲ್ಲ. ಸ್ಪಷ್ಟವಾಗಿ ಕಾಣುವಂತೆ ಮಾಡಿ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ ಅಥವಾ IMEI ಅನ್ನು ಕೈಯಾರೆ ನಮೂದಿಸಿ.",
            "ബാർകോഡ് വായിക്കാനായില്ല. വ്യക്തമായി കാണുന്നവിധം വീണ്ടും ശ്രമിക്കുക അല്ലെങ്കിൽ IMEI സ്വമേധയാ നൽകുക.",
        )
        "barcode_captured" -> arrayOf(
            "IMEI captured. Check the number, then tap Verify IMEI.",
            "IMEI मिल गया। नंबर जांचें और फिर IMEI सत्यापित करें दबाएं।",
            "IMEI मिळाला. क्रमांक तपासा आणि IMEI सत्यापित करा दाबा.",
            "IMEI મળી ગયો. નંબર તપાસો અને પછી IMEI ચકાસો દબાવો.",
            "IMEI পাওয়া গেছে। নম্বরটি দেখে IMEI যাচাই করুন চাপুন।",
            "IMEI கிடைத்தது. எண்ணைச் சரிபார்த்து IMEI-ஐ சரிபார்க்கவும் என்பதைத் தட்டவும்.",
            "IMEI గుర్తించబడింది. నంబర్‌ను చూసి IMEIని ధృవీకరించండి నొక్కండి.",
            "IMEI ಪತ್ತೆಯಾಗಿದೆ. ಸಂಖ್ಯೆಯನ್ನು ಪರಿಶೀಲಿಸಿ ನಂತರ IMEI ಪರಿಶೀಲಿಸಿ ಒತ್ತಿರಿ.",
            "IMEI ലഭിച്ചു. നമ്പർ പരിശോധിച്ച ശേഷം IMEI പരിശോധിക്കുക അമർത്തുക.",
        )
        "cancel" -> arrayOf(
            "Cancel", "रद्द करें", "रद्द करा", "રદ કરો", "বাতিল", "ரத்து செய்", "రద్దు చేయండి", "ರದ್ದುಮಾಡಿ", "റദ്ദാക്കുക",
        )
        else -> return key
    }
    return localizeSafeTradeDigits(context, translations[languageIndex.coerceIn(translations.indices)])
}

private fun localizeSafeTradeDigits(context: android.content.Context, value: String): String {
    val digitSets = arrayOf(
        "0123456789",
        "०१२३४५६७८९",
        "०१२३४५६७८९",
        "૦૧૨૩૪૫૬૭૮૯",
        "০১২৩৪৫৬৭৮৯",
        "௦௧௨௩௪௫௬௭௮௯",
        "౦౧౨౩౪౫౬౭౮౯",
        "೦೧೨೩೪೫೬೭೮೯",
        "൦൧൨൩൪൫൬൭൮൯",
    )
    val localizedDigits = digitSets[LanguageManager.getSavedLanguageIndex(context).coerceIn(digitSets.indices)]
    return buildString(value.length) {
        value.forEach { character ->
            val index = "0123456789".indexOf(character)
            append(if (index >= 0) localizedDigits[index] else character)
        }
    }
}

data class ImeiVerificationResponse(
    val status: String,
    val message: String,
    val is_flagged: Boolean,
)

private interface SafeTradeApi {
    @GET("api/v1/imei/verify/{imei_number}")
    fun verifyImei(@Path("imei_number") imeiNumber: String): Call<ImeiVerificationResponse>
}

private object SafeTradeApiClient {
    val api: SafeTradeApi by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.API_BASE_URL.trimEnd('/')}/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SafeTradeApi::class.java)
    }
}

private enum class VerificationState {
    IDLE,
    LOADING,
    CLEAN,
    FLAGGED,
    ERROR,
}

@Composable
fun SafeTradeCheckScreen(onBack: () -> Unit, darkMode: Boolean) {
    var imei by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf(VerificationState.IDLE) }
    var responseMessage by rememberSaveable { mutableStateOf("") }
    var scanFeedback by rememberSaveable { mutableStateOf("") }
    var scanSucceeded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (contents == null) {
            scanSucceeded = false
            scanFeedback = safeTradeText(context, "barcode_not_read")
        } else {
            val detectedImei = imeiFromBarcodePayload(contents)
            if (detectedImei != null) {
                imei = detectedImei
                state = VerificationState.IDLE
                responseMessage = ""
                scanSucceeded = true
                scanFeedback = safeTradeText(context, "barcode_captured")
            } else {
                scanSucceeded = false
                scanFeedback = safeTradeText(context, "barcode_other")
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = safeTradeText(context, "title"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(contentPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = safeTradeText(context, "heading"),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = safeTradeText(context, "instruction"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FendlyCard(modifier = Modifier.fillMaxWidth()) {
                    FendlyTextField(
                        value = imei,
                        onValueChange = { value ->
                            imei = value.filter { it in '0'..'9' }.take(15)
                            state = VerificationState.IDLE
                            responseMessage = ""
                        },
                        label = safeTradeText(context, "imei_label"),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = safeTradeText(context, "digits_count")
                                .replace("{count}", imei.length.toString())
                                .let { localizeSafeTradeDigits(context, it) },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                FendlyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = safeTradeText(context, "about"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = safeTradeText(context, "disclaimer"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FendlySecondaryButton(
                    text = safeTradeText(context, "scan"),
                    onClick = {
                        val options = ScanOptions().apply {
                            setPrompt(safeTradeText(context, "scan_status"))
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                        }
                        scanFeedback = ""
                        scanSucceeded = false
                        scannerLauncher.launch(options)
                    },
                )
                if (scanFeedback.isNotEmpty()) {
                    Text(
                        text = scanFeedback,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (scanSucceeded) {
                                    if (darkMode) Color(0xFF18352B) else Color(0xFFE3F4E8)
                                } else {
                                    if (darkMode) Color(0xFF42351E) else Color(0xFFFFF1D6)
                                },
                                RoundedCornerShape(12.dp),
                            )
                            .padding(14.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = {
                        state = VerificationState.LOADING
                        responseMessage = ""
                        SafeTradeApiClient.api.verifyImei(imei).enqueue(
                            object : Callback<ImeiVerificationResponse> {
                                override fun onResponse(
                                    call: Call<ImeiVerificationResponse>,
                                    response: Response<ImeiVerificationResponse>,
                                ) {
                                    val result = response.body()
                                    if (response.isSuccessful && result != null) {
                                        state = when {
                                            result.is_flagged -> VerificationState.FLAGGED
                                            result.status == "CLEAN" -> VerificationState.CLEAN
                                            else -> VerificationState.ERROR
                                        }
                                        responseMessage = if (state == VerificationState.ERROR) {
                                            safeTradeText(context, "unexpected")
                                        } else {
                                            safeTradeText(
                                                context,
                                                if (result.is_flagged) "flagged" else "clean",
                                            )
                                        }
                                    } else {
                                        state = VerificationState.ERROR
                                        responseMessage = when (response.code()) {
                                            429 -> safeTradeText(context, "rate_limit")
                                            400 -> safeTradeText(context, "invalid")
                                            else -> safeTradeText(context, "verify_error")
                                        }
                                    }
                                }

                                override fun onFailure(
                                    call: Call<ImeiVerificationResponse>,
                                    error: Throwable,
                                ) {
                                    state = VerificationState.ERROR
                                    responseMessage = safeTradeText(context, "network_error")
                                }
                            },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = imei.length == 15 && state != VerificationState.LOADING,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    if (state == VerificationState.LOADING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(safeTradeText(context, "verify"))
                    }
                }

                FendlySecondaryButton(
                    text = safeTradeText(context, "back"),
                    onClick = onBack,
                )

                when (state) {
                    VerificationState.CLEAN -> VerificationBanner(
                        message = responseMessage.ifBlank {
                            safeTradeText(context, "clean")
                        },
                        background = if (darkMode) Color(0xFF18352B) else Color(0xFFE3F4E8),
                        foreground = if (darkMode) Color(0xFFB9E6D0) else Color(0xFF14532D),
                    )
                    VerificationState.FLAGGED -> VerificationBanner(
                        message = safeTradeText(context, "flagged"),
                        background = if (darkMode) Color(0xFF452522) else Color(0xFFFFE8E6),
                        foreground = if (darkMode) Color(0xFFFFC5BE) else Color(0xFF8B1E18),
                    )
                    VerificationState.ERROR -> VerificationBanner(
                        message = responseMessage,
                        background = if (darkMode) Color(0xFF42351E) else Color(0xFFFFF1D6),
                        foreground = if (darkMode) Color(0xFFFFD88A) else Color(0xFF6D4600),
                    )
                    VerificationState.IDLE,
                    VerificationState.LOADING -> Unit
                }
                }
            }
        }
    }
}

@Composable
private fun VerificationBanner(
    message: String,
    background: Color,
    foreground: Color,
) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(16.dp),
        color = foreground,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium,
    )
}
