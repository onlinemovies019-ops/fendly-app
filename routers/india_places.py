import re
from pathlib import Path


INDIAN_PLACE_NAMES = tuple(
    name.strip()
    for name in """
    Port Blair | Diglipur | Mayabunder | Rangat | Havelock Island | Car Nicobar | Nancowry | Campbell Bay
    Little Andaman | Baratang | Neil Island | Visakhapatnam | Vijayawada | Guntur | Nellore | Kurnool
    Rajahmundry | Kadapa | Tirupati | Anantapur | Ongole | Eluru | Srikakulam | Machilipatnam
    Chittoor | Vizianagaram | Amaravati | Kakinada | Nandyal | Sri Potti Sriramulu Nellore | Bapatla | Markapur
    Palnadu | Parvathipuram | Alluri Sitharama Raju | Anakapalli | Konaseema | NTR | Sri Sathya Sai | Tadepalligudem
    Tenali | Hindupur | Kadiri | Proddatur | Rayachoti | Srikalahasti | Gudur | Itanagar
    Naharlagun | Tawang | Pasighat | Ziro | Bomdila | Tezpur | Along | Roing
    Namsai | Aalo | Anini | Changlang | Daporijo | Dibang Valley | Diyun | Khonsa
    Koloriang | Kra Daadi | Kurung Kumey | Longding | Seppa | Tali | Tezu | Yingkiong
    Yupia | Guwahati | Silchar | Dibrugarh | Jorhat | Nagaon | Tinsukia | Sivasagar
    Bongaigaon | Dhubri | Diphu | Goalpara | Barpeta | North Lakhimpur | Amingaon | Biswanath Chariali
    Charaideo | Dhemaji | Dhing | Dima Hasao | Haflong | Hailakandi | Hojai | Karbi Anglong
    Kokrajhar | Majuli | Mangaldoi | Marigaon | Nalbari | Sadiya | Sonitpur | South Salmara
    Tamulpur | Udalguri | Patna | Gaya | Bhagalpur | Muzaffarpur | Purnia | Darbhanga
    Arrah | Begusarai | Katihar | Munger | Chhapra | Bettiah | Saharsa | Hajipur
    Bihar Sharif | Araria | Aurangabad | Bagaha | Banka | Barh | Bhabua | Buxar
    Chapra | Daudnagar | Dehri | Forbesganj | Jamui | Jehanabad | Jhanjharpur | Kaimur
    Khagaria | Kishanganj | Lakhisarai | Madhepura | Madhubani | Mahua | Masaurhi | Nawada
    Piro | Rajgir | Raxaul | Rohtas | Samastipur | Sheikhpura | Sheohar | Sitamarhi
    Siwan | Supaul | Vaishali | Chandigarh | Raipur | Bhilai | Bilaspur | Korba
    Durg | Rajnandgaon | Jagdalpur | Raigarh | Ambikapur | Dhamtari | Mahasamund | Kawardha
    Balod | Baloda Bazar | Balrampur | Bastar | Bemetara | Bijapur | Dantewada | Gariaband
    Gaurela Pendra Marwahi | Janjgir | Jashpur | Kanker | Kondagaon | Koriya | Mungeli | Narayanpur
    Sukma | Surajpur | Surguja | Dongargarh | Champa | Chirmiri | Dalli Rajhara | Kumhari
    Ratanpur | Daman | Diu | Silvassa | Amli | Naroli | New Delhi | Delhi
    Dwarka | Rohini | Saket | Karol Bagh | Lajpat Nagar | Vasant Kunj | Janakpuri | Shahdara
    Connaught Place | Chandni Chowk | Civil Lines | Defence Colony | Kalkaji | Mayur Vihar | Najafgarh | Narela
    Patel Nagar | Pitampura | Preet Vihar | Seelampur | Vikaspuri | Wazirabad | Panaji | Margao
    Mapusa | Vasco da Gama | Ponda | Bicholim | Curchorem | Cuncolim | Canacona | Porvorim
    Ahmedabad | Surat | Vadodara | Rajkot | Gandhinagar | Bhavnagar | Jamnagar | Junagadh
    Anand | Bharuch | Vapi | Navsari | Mehsana | Morbi | Bhuj | Godhra
    Palanpur | Amreli | Aravalli | Balasinor | Botad | Chhota Udaipur | Dahod | Gandhidham
    Gir Somnath | Himatnagar | Kheda | Kutch | Mahisagar | Mandvi | Modasa | Nadiad
    Patan | Porbandar | Rajpipla | Surendranagar | Valsad | Veraval | Wankaner | Dholka
    Kalol | Mundra | Sanand | Unjha | Gurugram | Faridabad | Panipat | Hisar
    Rohtak | Karnal | Sonipat | Ambala | Yamunanagar | Panchkula | Bhiwani | Sirsa
    Rewari | Bahadurgarh | Kurukshetra | Charkhi Dadri | Fatehabad | Firozpur Jhirka | Hansi | Jhajjar
    Jind | Kaithal | Narnaul | Palwal | Pehowa | Ratia | Shahabad | Tohana
    Nuh | Kalanur | Kalka | Ladwa | Radaur | Shimla | Manali | Dharamshala
    Solan | Mandi | Kullu | Baddi | Una | Hamirpur | Kangra | Chamba
    Nahan | Kasauli | Churah | Dalhousie | Kinnaur | Lahaul | Karsog | Keylong
    Lahaul Spiti | Paonta Sahib | Palampur | Rohru | Sirmaur | Sundernagar | Theog | Yol
    Srinagar | Jammu | Anantnag | Baramulla | Kathua | Udhampur | Kupwara | Pulwama
    Poonch | Rajouri | Sopore | Kishtwar | Doda | Budgam | Ganderbal | Bandipora
    Bijbehara | Kulgam | Shopian | Ramban | Reasi | Samba | Tral | Handwara
    Uri | Banihal | Chenani | Bhadarwah | Nowshera | Thanamandi | Ranchi | Jamshedpur
    Dhanbad | Bokaro | Hazaribagh | Deoghar | Giridih | Ramgarh | Phusro | Medininagar
    Dumka | Chaibasa | Sahibganj | Gumla | Chatra | Garhwa | Godda | Jamtara
    Khunti | Koderma | Latehar | Lohardaga | Pakur | Rajmahal | Saraikela | Simdega
    Adityapur | Chakradharpur | Chas | Ghatshila | Madhupur | Mihijam | Bengaluru | Mysuru
    Hubballi | Mangaluru | Belagavi | Kalaburagi | Davangere | Ballari | Vijayapura | Shivamogga
    Tumakuru | Raichur | Hassan | Udupi | Chitradurga | Kolar | Mandya | Bidar
    Bagalkot | Chamarajanagar | Chikkaballapur | Chikkamagaluru | Dakshina Kannada | Dharwad | Gadag | Haveri
    Kodagu | Koppal | Madikeri | Ramanagara | Uttara Kannada | Yadgir | Bantwal | Bhadravati
    Channapatna | Doddaballapur | Gokak | Hunsur | Karwar | Kundapura | Sirsi | Sira
    Hospet | Kolar Gold Fields | Thiruvananthapuram | Kochi | Kozhikode | Thrissur | Kollam | Kannur
    Alappuzha | Kottayam | Palakkad | Malappuram | Kasaragod | Idukki | Pathanamthitta | Varkala
    Aluva | Attingal | Changanassery | Chavakkad | Cherthala | Kanhangad | Kattappana | Kayamkulam
    Koduvally | Kondotty | Kothamangalam | Manjeri | Mavelikkara | Muvattupuzha | Nedumangad | Neyyattinkara
    Nilambur | Ottapalam | Paravur | Perinthalmanna | Ponnani | Shoranur | Taliparamba | Thalassery
    Thiruvalla | Tirur | Wadakkanchery | Leh | Kargil | Diskit | Nubra | Padum
    Drass | Kavaratti | Agatti | Amini | Andrott | Kalpeni | Minicoy | Bhopal
    Indore | Jabalpur | Gwalior | Ujjain | Sagar | Dewas | Satna | Ratlam
    Rewa | Murwara | Singrauli | Burhanpur | Khandwa | Chhindwara | Vidisha | Shivpuri
    Agar | Alirajpur | Anuppur | Ashoknagar | Balaghat | Barwani | Betul | Bhind
    Dhar | Dindori | Guna | Harda | Hoshangabad | Jhabua | Katni | Mandla
    Mandsaur | Morena | Narsinghpur | Neemuch | Panna | Raisen | Rajgarh | Sehore
    Shahdol | Sheopur | Shajapur | Sivni | Tikamgarh | Umaria | Datia | Maihar
    Niwari | Mumbai | Pune | Nagpur | Nashik | Thane | Navi Mumbai | Kolhapur
    Solapur | Amravati | Nanded | Sangli | Jalgaon | Akola | Latur | Dhule
    Ahmednagar | Satara | Ratnagiri | Beed | Bhandara | Buldhana | Chandrapur | Gadchiroli
    Gondia | Hingoli | Jalna | Karad | Lonavala | Malegaon | Malkapur | Nandurbar
    Osmanabad | Palghar | Parbhani | Raigad | Sangamner | Sindhudurg | Wardha | Washim
    Yavatmal | Baramati | Bhiwandi | Kalyan | Mira Bhayandar | Panvel | Vasai Virar | Ichalkaranji
    Ulhasnagar | Imphal | Thoubal | Kakching | Bishnupur | Ukhrul | Churachandpur | Senapati
    Tamenglong | Jiribam | Shillong | Tura | Jowai | Nongstoin | Baghmara | Nongpoh
    Williamnagar | Mairang | Khliehriat | Aizawl | Lunglei | Champhai | Serchhip | Kolasib
    Saiha | Lawngtlai | Mamit | Khawzawl | Kohima | Dimapur | Chumoukedima | Mokokchung
    Wokha | Tuensang | Mon | Phek | Zunheboto | Kiphire | Longleng | Bhubaneswar
    Cuttack | Rourkela | Puri | Sambalpur | Berhampur | Balasore | Baripada | Jharsuguda
    Bhadrak | Jeypore | Bargarh | Angul | Dhenkanal | Rayagada | Koraput | Balangir
    Boudh | Deogarh | Gajapati | Ganjam | Jagatsinghpur | Jajpur | Kalahandi | Kandhamal
    Kendrapara | Kendujhar | Khordha | Malkangiri | Mayurbhanj | Nabarangpur | Nayagarh | Nuapada
    Sonepur | Sundargarh | Athagarh | Barbil | Bhanjanagar | Boudhgarh | Jajpur Road | Paradip
    Phulbani | Puducherry | Karaikal | Mahe | Yanam | Ludhiana | Amritsar | Jalandhar
    Patiala | Bathinda | Mohali | Hoshiarpur | Batala | Pathankot | Moga | Abohar
    Sangrur | Barnala | Firozpur | Kapurthala | Fazilka | Faridkot | Fatehgarh Sahib | Gurdaspur
    Mansa | Malerkotla | Muktsar | Nawanshahr | Rupnagar | Tarn Taran | Zirakpur | Khanna
    Kharar | Rajpura | Sunam | Phagwara | Jaipur | Jodhpur | Udaipur | Kota
    Ajmer | Bikaner | Alwar | Bharatpur | Sikar | Bhilwara | Sri Ganganagar | Pali
    Chittorgarh | Barmer | Tonk | Kishangarh | Jaisalmer | Bundi | Anupgarh | Balotra
    Banswara | Baran | Beawar | Churu | Dausa | Dholpur | Didwana | Dungarpur
    Hanumangarh | Jhalawar | Jhunjhunu | Karauli | Khairthal | Nagaur | Neem Ka Thana | Phalodi
    Rajsamand | Salumbar | Sawai Madhopur | Shahpura | Sirohi | Jalore | Kekri | Kotputli
    Makrana | Mount Abu | Nathdwara | Sujangarh | Gangtok | Namchi | Gyalshing | Mangan
    Rangpo | Singtam | Jorethang | Naya Bazar | Chennai | Coimbatore | Madurai | Salem
    Tiruchirappalli | Tiruppur | Erode | Tirunelveli | Vellore | Thoothukudi | Dindigul | Thanjavur
    Hosur | Nagercoil | Kanchipuram | Cuddalore | Kumbakonam | Karur | Namakkal | Ariyalur
    Chengalpattu | Dharmapuri | Kallakurichi | Krishnagiri | Mayiladuthurai | Nagapattinam | Perambalur | Pudukkottai
    Ramanathapuram | Ranipet | Sivaganga | Tenkasi | Theni | The Nilgiris | Thiruvarur | Tiruvallur
    Tiruvannamalai | Viluppuram | Virudhunagar | Avadi | Ambattur | Karaikudi | Kovilpatti | Pollachi
    Rajapalayam | Sivakasi | Tiruchengode | Udumalaipettai | Hyderabad | Warangal | Nizamabad | Khammam
    Karimnagar | Ramagundam | Mahbubnagar | Nalgonda | Adilabad | Suryapet | Siddipet | Miryalaguda
    Jagtial | Mancherial | Kamareddy | Agartala | Khowai | Belonia | Kailashahar | Dharmanagar
    Ambassa | Sabroom | Sonamura | Lucknow | Kanpur | Agra | Varanasi | Noida
    Prayagraj | Ghaziabad | Meerut | Bareilly | Aligarh | Moradabad | Saharanpur | Gorakhpur
    Mathura | Ayodhya | Jhansi | Firozabad | Muzaffarnagar | Rampur | Mirzapur | Etawah
    Hapur | Amroha | Azamgarh | Bahraich | Ballia | Banda | Barabanki | Basti
    Bhadohi | Bijnor | Budaun | Bulandshahr | Chandauli | Chitrakoot | Deoria | Etah
    Farrukhabad | Fatehpur | Ghazipur | Gonda | Hardoi | Hathras | Jalaun | Jaunpur
    Kannauj | Kanpur Dehat | Kasganj | Kaushambi | Kushinagar | Lakhimpur | Lalitpur | Maharajganj
    Mahoba | Mainpuri | Mau | Pilibhit | Pratapgarh | Raebareli | Sambhal | Sant Kabir Nagar
    Shahjahanpur | Shamli | Shravasti | Siddharthnagar | Sitapur | Sonbhadra | Sultanpur | Unnao
    Vrindavan | Robertsganj | Sikandrabad | Dehradun | Haridwar | Rishikesh | Haldwani | Roorkee
    Nainital | Rudrapur | Kashipur | Almora | Pithoragarh | Mussoorie | Kotdwar | Chamoli
    Kolkata | Durgapur | Asansol | Siliguri | Howrah | Darjeeling | Kharagpur | Haldia
    Malda | Bardhaman | Baharampur | Jalpaiguri | Cooch Behar | Raiganj | Krishnanagar | Serampore
    Dum Dum | Alipurduar | Arambagh | Bankura | Bangaon | Barasat | Barrackpore | Basirhat
    Bidhannagar | Bolpur | Chakdaha | Contai | Diamond Harbour | Egra | English Bazar | Gangarampur
    Ghatal | Habra | Halisahar | Jangipur | Jhargram | Kalna | Katwa | Kalyani
    Kamarhati | Kanchrapara | Kandi | Kulti | Madhyamgram | Murarai | Nabadwip | Naihati
    Purulia | Raghunathganj | Rampurhat | Shantiniketan | Suri | Tamluk | Tarakeswar | Uluberia
    """.replace("\n", "|").split("|")
    if name.strip()
)
APP_PLACE_LOOKUP = {name.casefold(): name for name in INDIAN_PLACE_NAMES}
INDIAN_PLACE_LOOKUP = dict(APP_PLACE_LOOKUP)
INDIAN_PLACE_POPULATION: dict[str, int] = {}
INDIAN_PLACE_ALIASES = {
    "नागपूर": "Nagpur",
    "नागपूरमध्ये": "Nagpur",
    "नागपूरला": "Nagpur",
    "अमरावती": "Amravati",
    "अमरावतीमध्ये": "Amravati",
    "अमरावतीत": "Amravati",
    "अमरावतीला": "Amravati",
    "वर्धा": "Wardha",
    "वर्ध्यामध्ये": "Wardha",
    "वर्ध्यात": "Wardha",
    "वर्ध्याला": "Wardha",
}

with Path(__file__).with_name("india_places_geonames.tsv").open(
    encoding="utf-8",
) as places_file:
    for line in places_file:
        if line.startswith("#") or not line.strip():
            continue
        local_name, ascii_name, _admin_code, population_text = line.rstrip("\n").split("\t")
        canonical_name = INDIAN_PLACE_LOOKUP.get(ascii_name.casefold(), ascii_name)
        INDIAN_PLACE_LOOKUP.setdefault(ascii_name.casefold(), canonical_name)
        INDIAN_PLACE_LOOKUP.setdefault(local_name.casefold(), canonical_name)
        population = int(population_text)
        for place_name in (local_name, ascii_name, canonical_name):
            place_key = place_name.casefold()
            INDIAN_PLACE_POPULATION[place_key] = max(
                INDIAN_PLACE_POPULATION.get(place_key, 0),
                population,
            )

for alias, canonical_name in INDIAN_PLACE_ALIASES.items():
    INDIAN_PLACE_LOOKUP[alias.casefold()] = INDIAN_PLACE_LOOKUP.get(
        canonical_name.casefold(),
        canonical_name,
    )

# Keep small gazetteer settlements from being mistaken for area/locality names.
MIN_GAZETTEER_CITY_POPULATION = 10_000

INDIAN_PLACE_PATTERN = re.compile(
    r"(?<!\w)(?:"
    + "|".join(
        re.escape(place)
        for place in sorted(INDIAN_PLACE_LOOKUP, key=len, reverse=True)
    )
    + r")(?!\w)",
    re.IGNORECASE,
)
