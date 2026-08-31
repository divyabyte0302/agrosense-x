package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.AppLanguage
import com.example.ui.theme.*
import com.example.ui.translation.AgroStrings
import com.example.ui.viewmodel.AgroViewModel

data class KnowledgeArticle(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val categoryEn: String,
    val categoryTa: String,
    val iconEmoji: String,
    val readTimeMinutes: Int,
    val simpleSummaryEn: String,
    val simpleSummaryTa: String,
    val scientificMechanismEn: String,
    val scientificMechanismTa: String,
    val earlySymptomsEn: List<String>,
    val earlySymptomsTa: List<String>,
    val detectionMethodsEn: List<String>,
    val detectionMethodsTa: List<String>,
    val preventativeProtocolsEn: List<String>,
    val preventativeProtocolsTa: List<String>,
    val labConfirmationGuideEn: String,
    val labConfirmationGuideTa: String,
    val spectralBand: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeCenterScreen(
    navController: NavController,
    viewModel: AgroViewModel? = null
) {
    val appLanguageState = viewModel?.appLanguage?.collectAsStateWithLifecycle()
    val isTamil = appLanguageState?.value == AppLanguage.TAMIL

    val categories = remember(isTamil) {
        if (isTamil) {
            listOf("அனைத்தும்", "நீர் அழுத்தம்", "ஊட்டச்சத்து", "பயிர் நோய்கள்", "தாவர உடலியல்", "மண் & காலநிலை", "வேளாண் AI")
        } else {
            listOf("All", "Water Stress", "Nutrient Stress", "Plant Diseases", "Plant Physiology", "Soil & Microclimate", "AI in Agriculture")
        }
    }

    val articles = remember {
        listOf(
            KnowledgeArticle(
                id = "water_stress_stomatal",
                titleEn = "Stomatal Conductance & Pre-Symptomatic Water Deficit",
                titleTa = "இலைத்துளை இயக்கம் & ஆரம்ப நீர் பற்றாக்குறை கண்டறிதல்",
                categoryEn = "Water Stress",
                categoryTa = "நீர் அழுத்தம்",
                iconEmoji = "💧",
                readTimeMinutes = 4,
                simpleSummaryEn = "Plants close their microscopic leaf pores (stomata) to save water long before leaves visibly curl or wilt.",
                simpleSummaryTa = "இலைகள் சுருங்குவதற்கு அல்லது வாடுவதற்கு முன்பே, தாவரங்கள் நீரைக் காப்பாற்ற தங்கள் நுண்ணிய இலைத்துளைகளை மூடுகின்றன.",
                scientificMechanismEn = "When soil water potential drops below -0.5 MPa, roots produce Abscisic Acid (ABA) that translocates to guard cells. This triggers efflux of potassium ions (K+) and turgor loss, reducing transpiration cooling and elevating canopy temperature by 1.2–2.5°C.",
                scientificMechanismTa = "மண் நீர் அளவு குறையும் போது வேர்கள் ABA அமிலத்தை உற்பத்தி செய்கின்றன. இதனால் இலைத்துளை காவற்கலங்கள் மூடி, ஆவியாதல் குறைந்து இலை வெப்பநிலை 1.2–2.5°C வரை உயர்கிறது.",
                earlySymptomsEn = listOf(
                    "Canopy thermal elevation detected via LWIR thermal imaging (CWSI > 0.4)",
                    "Subtle reduction in leaf angle (nyctinastic micro-droop)",
                    "Decline in photochemical quantum yield (Fv/Fm < 0.78)"
                ),
                earlySymptomsTa = listOf(
                    "வெப்பக் கேமரா மூலம் இலை வெப்பநிலை உயர்வு கண்டறிதல் (CWSI > 0.4)",
                    "இலை கோணத்தில் நுட்பமான சரிவு மற்றும் தளர்வு",
                    "ஒளிச்சேர்க்கை ஆற்றல் திறனில் ஆரம்ப வீழ்ச்சி (Fv/Fm < 0.78)"
                ),
                detectionMethodsEn = listOf(
                    "Crop Water Stress Index (CWSI) calculation",
                    "Continuous sap flow sensors and stem dendrometry",
                    "Multispectral NIR/Red edge reflection anomalies"
                ),
                detectionMethodsTa = listOf(
                    "பயிர் நீர் அழுத்தக் குறியீடு (CWSI) கணக்கீடு",
                    "சாறு ஓட்ட உணரிகள் மற்றும் தண்டு விரிசல் அளவீடு",
                    "மல்டிஸ்பெக்ட்ரல் அகச்சிவப்பு ஒளி பிரதிபலிப்பு பகுப்பாய்வு"
                ),
                preventativeProtocolsEn = listOf(
                    "Trigger pulse micro-irrigation when CWSI exceeds 0.35 during peak solar hours",
                    "Adjust substrate EC to minimize osmotic root shock",
                    "Apply anti-transpirant foliar silicates during severe heatwaves"
                ),
                preventativeProtocolsTa = listOf(
                    "நண்பகல் வேளையில் சொட்டு நீர்ப்பாசனத்தை இயக்கி நீர் பற்றாக்குறையைத் தணிக்கவும்",
                    "வேர் அழுத்தத்தைக் குறைக்க உரக் கரைசலின் EC அளவை சரிசெய்யவும்",
                    "கடும் வெப்பத்தில் சிலிக்கான் இலைவழித் தெளிப்பு மேற்கொள்ளவும்"
                ),
                labConfirmationGuideEn = "Measure leaf water potential using a Scholander Pressure Chamber. Pressure < -1.2 MPa confirms severe internal dehydration.",
                labConfirmationGuideTa = "ஸ்கோலாண்டர் பிரஷர் சேம்பர் மூலம் இலை நீர் அழுத்தத்தை அளவிடவும். அழுத்தம் < -1.2 MPa இருந்தால் கடுமையான நீர் பற்றாக்குறையைக் குறிக்கும்.",
                spectralBand = "Thermal LWIR (8–14 µm) & Red Edge (705–740 nm)"
            ),
            KnowledgeArticle(
                id = "nitrogen_chlorosis",
                titleEn = "Nitrogen Partitioning & Chlorophyll Fluorescence Dynamics",
                titleTa = "தழைச்சத்து விநியோகம் & பச்சைய ஒளிர்தல் பகுப்பாய்வு",
                categoryEn = "Nutrient Stress",
                categoryTa = "ஊட்டச்சத்து",
                iconEmoji = "🧪",
                readTimeMinutes = 5,
                simpleSummaryEn = "Nitrogen is essential for chloroplast formation. Low nitrogen limits leaf greening and slows down energy capture.",
                simpleSummaryTa = "பச்சையம் உருவாவதற்கு தழைச்சத்து (நைட்ரஜன்) இன்றியமையாதது. இதன் குறைபாடு இலை மஞ்சளாதலை ஏற்படுத்துகிறது.",
                scientificMechanismEn = "Nitrogen is highly mobile within the plant. Under deficit, the plant catabolizes RuBisCO enzymes in older leaves to mobilize amino acids to developing apical shoots, leading to baseline non-photochemical quenching.",
                scientificMechanismTa = "தழைச்சத்து பற்றாக்குறை ஏற்படும் போது முதிர்ந்த இலைகளில் உள்ள என்சைம்கள் சிதைக்கப்பட்டு இளம் தளிர்களுக்கு நகர்த்தப்படுவதால் அடி இலைகள் மஞ்சளாகின்றன.",
                earlySymptomsEn = listOf(
                    "NDVI and MCARI drop in lower canopy leaves",
                    "SPAD chlorophyll meter reading < 42 on mature leaves",
                    "Decreased leaf photosynthetic rate prior to visible yellowing"
                ),
                earlySymptomsTa = listOf(
                    "அடி இலைகளில் NDVI மற்றும் பச்சையக் குறியீட்டு சரிவு",
                    "SPAD மீட்டரில் பச்சைய அளவு 42-க்கு கீழ் குறைதல்",
                    "மஞ்சள் நிறம் வெளிப்படையாகத் தோன்றும் முன்பே ஒளிச்சேர்க்கை வீழ்ச்சி"
                ),
                detectionMethodsEn = listOf(
                    "Green Normalized Difference Vegetation Index (GNDVI)",
                    "Dualex optical sensor for Nitrogen Balance Index (NBI)",
                    "Near-infrared reflectance spectroscopy (NIR 850 nm)"
                ),
                detectionMethodsTa = listOf(
                    "பசுமை தாவரக் குறியீடு (GNDVI) ஸ்பெக்ட்ரம் ஸ்கேன்",
                    "தழைச்சத்து சமநிலை குறியீட்டு ஒளி உணரி (NBI)",
                    "அகச்சிவப்பு பிரதிபலிப்பு ஸ்பெக்ட்ரோஸ்கோபி"
                ),
                preventativeProtocolsEn = listOf(
                    "Side-dress with balanced nitrate-to-ammonium ratios (80:20)",
                    "Maintain soil pH between 6.0 and 6.8 to ensure nitrate uptake bioavailability",
                    "Inject humic/fulvic chelates to enhance nutrient absorption"
                ),
                preventativeProtocolsTa = listOf(
                    "சரியான விகிதத்தில் நைட்ரேட் தழைச்சத்து உரம் இடவும்",
                    "மண் pH அளவை 6.0–6.8 வரம்பில் பராமரித்து சத்து உறிஞ்சுதலை உறுதிசெய்யவும்",
                    "ஹியூமிக் அமிலக் கரைசல் சேர்த்து வேர் உறிஞ்சும் திறனை அதிகரிக்கவும்"
                ),
                labConfirmationGuideEn = "Perform dry-matter tissue leaf testing. Total Nitrogen < 3.2% dry weight confirms deficiency in solanaceous crops.",
                labConfirmationGuideTa = "இலை திசு ஆய்வகம் மூலம் தழைச்சத்து < 3.2% உள்ளதா என பரிசோதிக்கவும்.",
                spectralBand = "GNDVI (NIR 800 nm / Green 550 nm)"
            ),
            KnowledgeArticle(
                id = "early_blight_pathogen",
                titleEn = "Alternaria & Foliar Pathogen Spectral Disruption",
                titleTa = "ஆல்டர்னேரியா இலைக்கருகல் & பூஞ்சை நோய் மேலாண்மை",
                categoryEn = "Plant Diseases",
                categoryTa = "பயிர் நோய்கள்",
                iconEmoji = "🦠",
                readTimeMinutes = 6,
                simpleSummaryEn = "Fungal pathogens degrade plant cell walls and reduce photosynthetic efficiency days before brown necrotic spots appear.",
                simpleSummaryTa = "இலைகளில் கரும்புள்ளிகள் தோன்றுவதற்குப் பல நாட்களுக்கு முன்பே பூஞ்சை வித்துக்கள் இலைச் செல்களை சேதப்படுத்துகின்றன.",
                scientificMechanismEn = "Conidial germination on humid leaf surfaces leads to hyphal penetration via stomata. The fungal secretion of host-specific phytotoxins (such as alternariol) damages cell membranes, inducing local localized cellular leakage and localized hyperthermia.",
                scientificMechanismTa = "ஈரப்பதம் மிகுந்த இலைகளில் பூஞ்சை வித்துக்கள் முளைத்து இலைத்துளைகள் வழியாக ஊடுருவி செல்களை சேதப்படுத்தி நச்சுக்களை வெளியிடுகின்றன.",
                earlySymptomsEn = listOf(
                    "Localized micro-lesion hotspots visible in NIR false-color imaging",
                    "Subtle yellow halo around stomatal openings under UV fluorescence",
                    "Rapid decline in maximum fluorescence yield (Fm)"
                ),
                earlySymptomsTa = listOf(
                    "அகச்சிவப்பு கேமராவில் இலை நுண்ணிய புள்ளி வெப்ப மாற்றங்கள்",
                    "புற ஊதாக் கதிரின் கீழ் இலைத்துளைகளில் மஞ்சள் வளையம் தெரிதல்",
                    "இலை ஒளி ஒளிர்தல் திறனில் உடனடி சரிவு"
                ),
                detectionMethodsEn = listOf(
                    "Pre-symptomatic chlorophyll fluorescence imaging",
                    "Surface plasmon resonance biosensors",
                    "Deep learning convolutional pattern scanning on leaf venation"
                ),
                detectionMethodsTa = listOf(
                    "முன் அறிகுறி பச்சைய ஒளிர்தல் படமெடுத்தல்",
                    "உயிரி உணரி நோய் கண்டறிதல் கருவிகள்",
                    "இலை நரம்பமைவு AI விஷன் ஸ்கேன்"
                ),
                preventativeProtocolsEn = listOf(
                    "Maintain canopy relative humidity below 80% with ventilation fans",
                    "Apply prophylactic bio-fungicides (Bacillus subtilis strains)",
                    "Ensure adequate leaf spacing to prevent microclimate condensation"
                ),
                preventativeProtocolsTa = listOf(
                    "காற்றோட்டம் மூலம் இலைப்பரப்பு ஈரப்பதத்தை 80%-க்கு கீழ் வைத்திருக்கவும்",
                    "சூடோமோனாஸ் அல்லது பேசிலஸ் உயிர் உரங்களை முன்கூட்டியே தெளிக்கவும்",
                    "இலைகள் ஒன்றோடொன்று உரசாமல் போதுமான இடைவெளி விடவும்"
                ),
                labConfirmationGuideEn = "Perform qPCR assay for Alternaria solani DNA or cultivate leaf scrapings on Potato Dextrose Agar (PDA).",
                labConfirmationGuideTa = "ஆய்வகத்தில் qPCR அல்லது கல்ச்சர் சோதனை மூலம் ஆல்டர்னேரியா பூஞ்சையை உறுதி செய்யவும்.",
                spectralBand = "Chlorophyll Fluorescence (685 nm / 735 nm)"
            ),
            KnowledgeArticle(
                id = "heat_vpd_stress",
                titleEn = "Vapor Pressure Deficit (VPD) & Heat Stress Coupling",
                titleTa = "நீராவி அழுத்தப் பற்றாக்குறை (VPD) & வெப்ப அழுத்தம்",
                categoryEn = "Plant Physiology",
                categoryTa = "தாவர உடலியல்",
                iconEmoji = "☀️",
                readTimeMinutes = 5,
                simpleSummaryEn = "High temperature coupled with dry air forces plants into hyper-transpiration, causing cellular overheating.",
                simpleSummaryTa = "அதிக வெப்பமும் வறண்ட காற்றும் தாவரங்களில் அதிகப்படியான நீர் இழப்பை ஏற்படுத்தி செல்களை வெப்பமடையச் செய்கின்றன.",
                scientificMechanismEn = "When VPD rises above 1.8 kPa, atmospheric drying demand outpaces root water intake capacity. This causes cavitation in xylem vessels, cellular plasmolysis, and denaturation of Photosystem II D1 reaction center proteins.",
                scientificMechanismTa = "VPD அளவு 1.8 kPa-க்கு மேல் உயரும் போது வேர்களின் நீர் உறிஞ்சும் வேகத்தை விட இலைகளின் நீராவிப்போக்கு அதிகரித்து வாடல் ஏற்படுகிறது.",
                earlySymptomsEn = listOf(
                    "Canopy leaf temperature exceeding ambient air temperature by > 2.0°C",
                    "Temporary mid-day leaf rolling or inward curling",
                    "Elevated basal fluorescence (F0) indicating PSII thermal damage"
                ),
                earlySymptomsTa = listOf(
                    "சுற்றுப்புற வெப்பத்தை விட இலை வெப்பநிலை 2°C வரை கூடுதல் ஆகுதல்",
                    "நண்பகலில் இலைகள் தற்காலிகமாக உள்நோக்கிச் சுருங்குதல்",
                    "ஒளிச்சேர்க்கை மையங்களில் வெப்ப சேதம் ஏற்படுதல்"
                ),
                detectionMethodsEn = listOf(
                    "Real-time VPD continuous monitoring stations",
                    "Infrared radiometer canopy temperature tracking",
                    "Hyperspectral Photochemical Reflectance Index (PRI)"
                ),
                detectionMethodsTa = listOf(
                    "நேரலை VPD தொடர் கண்காணிப்பு உணரிகள்",
                    "இன்ஃப்ராரெட் இலை வெப்பநிலை கண்காணிப்பான்",
                    "ஒளிவேதியியல் பிரதிபலிப்புக் குறியீடு (PRI)"
                ),
                preventativeProtocolsEn = listOf(
                    "Activate high-pressure fogging/misting when VPD exceeds 1.5 kPa",
                    "Deploy 30% aluminized shade netting during solar noon",
                    "Ensure root zone temperature remains below 24°C"
                ),
                preventativeProtocolsTa = listOf(
                    "VPD அளவு 1.5 kPa தாண்டும் போது பனிப்புகை தெளிப்பான்களை இயக்கவும்",
                    "நண்பகலில் 30% நிழல் வலைகளைப் பயன்படுத்தவும்",
                    "வேர்ப் பகுதி வெப்பநிலை 24°C-க்கு மிகாமல் பராமரிக்கவும்"
                ),
                labConfirmationGuideEn = "Conduct electrolyte leakage assay on leaf discs. Electrical conductivity increase > 25% indicates cell membrane thermal rupture.",
                labConfirmationGuideTa = "இலை செல்களில் மின்கடத்துத்திறன் கசிவு சோதனை மூலம் வெப்ப சேதத்தை உறுதி செய்யவும்.",
                spectralBand = "Photochemical Reflectance Index (PRI 531 nm / 570 nm)"
            ),
            KnowledgeArticle(
                id = "soil_chemistry_ec_ph",
                titleEn = "Rhizosphere Dynamics: Soil EC, pH & Nutrient Lockout",
                titleTa = "மண் வேர்ப்பகுதி வேதியியல்: EC, pH & சத்து உறிஞ்சுதல்",
                categoryEn = "Soil & Microclimate",
                categoryTa = "மண் & காலநிலை",
                iconEmoji = "🌱",
                readTimeMinutes = 4,
                simpleSummaryEn = "Imbalanced soil pH or excessive salts prevent roots from absorbing nutrients even if fertilizer is abundant.",
                simpleSummaryTa = "மண் pH தவறாகவோ அல்லது உப்புத்தன்மை அதிகமாகவோ இருந்தால் உரம் இருந்தாலும் செடிகளால் உறிஞ்ச முடியாது.",
                scientificMechanismEn = "Soil pH governs nutrient ion speciation. At pH < 5.5, phosphorus precipitates with aluminum/iron, while at pH > 7.5, phosphorus fixes with calcium. High electrical conductivity (EC > 3.0 mS/cm) generates negative osmotic potential, drawing water out of root cells.",
                scientificMechanismTa = "மண் pH 5.5-க்கு குறைவாகவோ அல்லது 7.5-க்கு அதிகமாகவோ இருந்தால் மணிச்சத்து மற்றும் நுண்ணூட்டச்சத்துக்கள் உறிஞ்சப்படாமல் முடக்கப்படும்.",
                earlySymptomsEn = listOf(
                    "Tip burn on young leaves due to osmotic calcium transportation failure",
                    "Stunted root tip elongation with darkened root caps",
                    "Interveinal chlorosis despite regular fertigation"
                ),
                earlySymptomsTa = listOf(
                    "சுண்ணாம்புச் சத்து குறைபாட்டால் இளம் இலை நுனிகள் கருகிப் போதல்",
                    "வேர் நுனி வளர்ச்சி குன்றி கருமையாக மாறுதல்",
                    "உரமிட்டாலும் இலை நரம்புகளுக்கு இடையே மஞ்சள் நிறம் தோன்றுதல்"
                ),
                detectionMethodsEn = listOf(
                    "Direct insertion FDR soil moisture/EC/pH probes",
                    "Pore water squeeze extraction with benchtop meters",
                    "1:2 soil-to-water dilution testing"
                ),
                detectionMethodsTa = listOf(
                    "டிஜிட்டல் மண் ஈரப்பதம்/EC/pH உணரிகள்",
                    "மண் சாறு பிரித்தெடுத்து pH மீட்டர் ஆய்வு",
                    "1:2 மண்-நீர் கரைசல் பரிசோதனை"
                ),
                preventativeProtocolsEn = listOf(
                    "Flush root zone with reverse-osmosis water when EC exceeds target by 25%",
                    "Buffer fertigation solutions to maintain runoff pH at 6.0–6.5",
                    "Incorporate beneficial mycorrhizal fungi to expand root surface area"
                ),
                preventativeProtocolsTa = listOf(
                    "உப்புத்தன்மை (EC) கூடும் போது நன்னீரைக் கொண்டு வேர்ப்பகுதியைக் கழுவவும்",
                    "நீர் பாய்ச்சும் போது pH அளவை 6.0–6.5 வரம்பில் வைத்திருக்கவும்",
                    "வேர் பரப்பை அதிகரிக்க மைக்கோரைசா பூஞ்சை இடவும்"
                ),
                labConfirmationGuideEn = "Perform standard saturated paste extract (SPE) analysis in an accredited agricultural laboratory.",
                labConfirmationGuideTa = "அங்கீகரிக்கப்பட்ட மண் பரிசோதனை ஆய்வகத்தில் SPE பகுப்பாய்வு செய்யவும்.",
                spectralBand = "Rhizosphere Sensor Array (EC / pH / VWC)"
            ),
            KnowledgeArticle(
                id = "multispectral_ai_ag",
                titleEn = "Multispectral AI Diagnostics in Precision AgriTech",
                titleTa = "மல்டிஸ்பெக்ட்ரல் AI தொழில் நுட்பம் & பயிர் ஸ்கேன்",
                categoryEn = "AI in Agriculture",
                categoryTa = "வேளாண் AI",
                iconEmoji = "🤖",
                readTimeMinutes = 5,
                simpleSummaryEn = "Computer vision models analyze narrow electromagnetic bands to extract plant stress signals invisible to the naked human eye.",
                simpleSummaryTa = "மனிதக் கண்களுக்குப் புலப்படாத பயிர் அழுத்த சமிக்ஞைகளை AI கணினி பார்வை உணரிகள் கண்டறிகின்றன.",
                scientificMechanismEn = "Healthy leaves absorb blue and red light for photosynthesis while strongly reflecting near-infrared (NIR) light due to internal spongy mesophyll cell structure. Pre-symptomatic stress damages internal cell walls, causing subtle reflectance shifts across the red-edge spectrum (690–730 nm).",
                scientificMechanismTa = "ஆரோக்கியமான இலைகள் அகச்சிவப்பு ஒளியை அதிகமாகப் பிரதிபலிக்கின்றன. மன அழுத்தம் ஏற்படும் போது சிவப்பு-விளிம்பு நிறமாலையில் மாற்றங்கள் நிகழ்கின்றன.",
                earlySymptomsEn = listOf(
                    "Deviation in Normalized Difference Red Edge Index (NDRE)",
                    "Subtle texture heterogeneity on leaf lamina feature maps",
                    "Confidence degradation in baseline healthy vegetation models"
                ),
                earlySymptomsTa = listOf(
                    "சிவப்பு விளிம்புக் குறியீட்டில் (NDRE) விலகல்கள்",
                    "இலைப்பரப்பு நுண்ணிய அமைப்பில் மாறுதல்கள்",
                    "பயிர் நலம் மாதிரியில் தொடக்கநிலை மாற்றம்"
                ),
                detectionMethodsEn = listOf(
                    "Multispectral edge neural network inference",
                    "Continuous high-resolution UAV ortho-mosaics",
                    "Explainable Grad-CAM feature localization on leaf venation"
                ),
                detectionMethodsTa = listOf(
                    "மொபைல் எட்ஜ் நியூரல் நெட்வொர்க் கணிப்பு",
                    "ட்ரோன் மல்டிஸ்பெக்ட்ரல் வரைபடங்கள்",
                    "Grad-CAM இலை நரம்புப் பார்வை பகுப்பாய்வு"
                ),
                preventativeProtocolsEn = listOf(
                    "Perform routine drone or handheld scans every 48 hours",
                    "Calibrate camera sensors with 99% diffuse reflectance white-balance targets",
                    "Correlate AI predictions with on-ground micro-climate telemetry"
                ),
                preventativeProtocolsTa = listOf(
                    "ஒவ்வொரு 48 மணி நேரத்திற்கும் ஒரு முறை பயிர் ஸ்கேன் செய்யவும்",
                    "கேமரா உணரியை சரியான முறையில் அளவீடு செய்யவும்",
                    "AI முடிவுகளை நிலத்தடி உணரித் தரவுகளுடன் ஒப்பிட்டு சரிபார்க்கவும்"
                ),
                labConfirmationGuideEn = "Cross-validate edge AI predictions with benchtop UV-Vis spectrophotometer leaf transmittance curves.",
                labConfirmationGuideTa = "ஆய்வக ஸ்பெக்ட்ரோஃபோட்டோமீட்டர் ஒளி ஊடுருவல் வளைவுகளுடன் AI கணிப்புகளை ஒப்பிடவும்.",
                spectralBand = "Multispectral 5-Band (Blue, Green, Red, Red-Edge, NIR)"
            )
        )
    }

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<KnowledgeArticle?>(null) }

    val filteredArticles = remember(selectedCategoryIndex, searchQuery, isTamil) {
        val selectedCat = categories.getOrNull(selectedCategoryIndex) ?: categories.first()
        val isAll = selectedCategoryIndex == 0
        articles.filter { article ->
            val matchesCategory = isAll || (if (isTamil) article.categoryTa == selectedCat else article.categoryEn == selectedCat)
            val title = if (isTamil) article.titleTa else article.titleEn
            val summary = if (isTamil) article.simpleSummaryTa else article.simpleSummaryEn
            val mechanism = if (isTamil) article.scientificMechanismTa else article.scientificMechanismEn
            val matchesSearch = searchQuery.isBlank() ||
                    title.contains(searchQuery, ignoreCase = true) ||
                    summary.contains(searchQuery, ignoreCase = true) ||
                    mechanism.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = DeepForestBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTamil) "வேளாண் பயோடெக் அறிவு மையம்" else "AGRO-BIOTECH KNOWLEDGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isTamil) "தாவர உடலியல் & AI வழிகாட்டி" else "Plant Physiology & AI Diagnostics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                actions = {
                    viewModel?.let { vm ->
                        Surface(
                            onClick = { vm.toggleAppLanguage() },
                            color = GlowingEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GlowingEmerald),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("knowledge_lang_toggle")
                        ) {
                            Text(
                                text = if (isTamil) "தமிழ்" else "EN",
                                color = GlowingEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepForestBg)
            )
        }
    ) { padding ->
        if (selectedArticle != null) {
            // Article Detail View
            ArticleDetailView(
                article = selectedArticle!!,
                isTamil = isTamil,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onBack = { selectedArticle = null }
            )
        } else {
            // Articles Catalog View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("knowledge_search_input"),
                        placeholder = {
                            Text(
                                if (isTamil) "நோய், பச்சையம், உடலியல் தேடவும்..." else "Search mechanisms, NDVI, pathogens...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = GlowingEmerald) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, null, tint = TextMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedContainerColor = CardSurfaceDark,
                            unfocusedContainerColor = CardSurfaceDark,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )
                }

                item {
                    // Category Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories.size) { index ->
                            val cat = categories[index]
                            val isSelected = index == selectedCategoryIndex
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) GlowingEmerald else CardSurfaceDark,
                                border = BorderStroke(1.dp, if (isSelected) GlowingEmerald else SoftGrass),
                                modifier = Modifier
                                    .clickable { selectedCategoryIndex = index }
                                    .testTag("cat_filter_$cat")
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DeepForestBg else TextLight,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    // Scientific Standards Banner
                    Surface(
                        color = SoftGrass,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔬", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "அறிவியல் அடிப்படையிலான வேளாண் நெறிமுறைகள்" else "Evidence-Based Agronomic Protocols",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlowingEmerald
                                )
                                Text(
                                    text = if (isTamil) "ISO மற்றும் சர்வதேச வேளாண் தரநிலைகளுக்கு ஏற்ப தாவர உடலியல் மாதிரிகள்." else "Peer-reviewed plant physiological models aligned with ISO/USDA smart crop standards.",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                items(filteredArticles) { article ->
                    val title = if (isTamil) article.titleTa else article.titleEn
                    val category = if (isTamil) article.categoryTa else article.categoryEn
                    val summary = if (isTamil) article.simpleSummaryTa else article.simpleSummaryEn

                    Surface(
                        color = CardSurfaceDark,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedArticle = article }
                            .testTag("article_card_${article.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = GlowingEmerald.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = category.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlowingEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTamil) "${article.readTimeMinutes} நிமிடம்" else "${article.readTimeMinutes} min read",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SoftGrass),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(article.iconEmoji, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLight,
                                        lineHeight = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = summary,
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SoftGrass)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (isTamil) "அலைவரிசை" else "Band"}: ${article.spectralBand}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (isTamil) "கட்டுரை காண்க →" else "Read Guide →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlowingEmerald
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ArticleDetailView(
    article: KnowledgeArticle,
    isTamil: Boolean = false,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val title = if (isTamil) article.titleTa else article.titleEn
    val category = if (isTamil) article.categoryTa else article.categoryEn
    val summary = if (isTamil) article.simpleSummaryTa else article.simpleSummaryEn
    val mechanism = if (isTamil) article.scientificMechanismTa else article.scientificMechanismEn
    val symptoms = if (isTamil) article.earlySymptomsTa else article.earlySymptomsEn
    val detectionMethods = if (isTamil) article.detectionMethodsTa else article.detectionMethodsEn
    val protocols = if (isTamil) article.preventativeProtocolsTa else article.preventativeProtocolsEn
    val labGuide = if (isTamil) article.labConfirmationGuideTa else article.labConfirmationGuideEn

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = GlowingEmerald, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isTamil) "அறிவு மையத்திற்குத் திரும்பு" else "Back to Knowledge Center",
                color = GlowingEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SoftGrass),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(article.iconEmoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Surface(
                            color = GlowingEmerald.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = category.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowingEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            lineHeight = 22.sp
                        )
                    }
                }

                // 1. Simple Summary
                Surface(
                    color = DeepForestBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SoftGrass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            if (isTamil) "🌱 முக்கிய விளக்கம்" else "🌱 High-Level Summary",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(summary, fontSize = 13.sp, color = TextLight, lineHeight = 18.sp)
                    }
                }

                // 2. Scientific Mechanism
                Column {
                    Text(
                        if (isTamil) "🔬 உயிரியல் வழிமுறை & உடலியல்" else "🔬 Biological Mechanism & Physiology",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(mechanism, fontSize = 13.sp, color = TextMuted, lineHeight = 19.sp)
                }

                // 3. Early Pre-Symptomatic Symptoms
                Column {
                    Text(
                        if (isTamil) "⚡ ஆரம்ப முன்-அறிகுறி அடையாளங்கள்" else "⚡ Early Pre-Symptomatic Indicators",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    symptoms.forEach { symptom ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Text("•", fontSize = 14.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(symptom, fontSize = 12.sp, color = TextLight, lineHeight = 17.sp)
                        }
                    }
                }

                // 4. Detection Methods
                Column {
                    Text(
                        if (isTamil) "📷 மல்டிஸ்பெக்ட்ரல் & உணரி கண்டறிதல்" else "📷 Multispectral & Sensor Detection",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    detectionMethods.forEach { method ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Check, null, tint = TechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(method, fontSize = 12.sp, color = TextLight, lineHeight = 17.sp)
                        }
                    }
                }

                // 5. Actionable Preventative Protocols
                Column {
                    Text(
                        if (isTamil) "🛡️ வேளாண் தடுப்பு & தீர்வு நெறிமுறைகள்" else "🛡️ Agronomic Prevention & Mitigation",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    protocols.forEach { protocol ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Shield, null, tint = GlowingEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(protocol, fontSize = 12.sp, color = TextLight, lineHeight = 17.sp)
                        }
                    }
                }

                // 6. Laboratory Confirmation Notice
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SoftGrass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WarningAmber, null, tint = AlertWarning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isTamil) "ஆய்வக உறுதிப்படுத்தல் தேவைப்படும் சூழல்" else "When Laboratory Confirmation Is Required",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(labGuide, fontSize = 12.sp, color = TextLight, lineHeight = 17.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
