package com.example.ui.translation

import androidx.compose.runtime.Composable
import com.example.data.model.AppLanguage

/**
 * Universal Dual-Language Localization Engine for AgroSense-X.
 * Automatically handles Tamil and English for all screens, components, telemetry,
 * diagnoses, advice, agronomy guides, and alerts.
 */
object AgroStrings {

    fun get(key: String, language: AppLanguage): String {
        return if (language == AppLanguage.TAMIL) {
            tamilStrings[key] ?: phrasesMapTamil[key] ?: englishStrings[key] ?: key
        } else {
            englishStrings[key] ?: key
        }
    }

    /**
     * Translates any UI string, label, sentence, or phrase to Tamil if isTamil is true.
     */
    fun tr(text: String, language: AppLanguage): String {
        if (language != AppLanguage.TAMIL || text.isBlank()) return text
        
        // Exact match check
        tamilStrings[text]?.let { return it }
        phrasesMapTamil[text]?.let { return it }
        phrasesMapTamil[text.trim()]?.let { return it }
        
        // Case-insensitive check
        val lower = text.lowercase().trim()
        caseInsensitiveTamil[lower]?.let { return it }

        // Match common patterns if no exact match
        for ((en, ta) in phrasesMapTamil) {
            if (text.equals(en, ignoreCase = true)) return ta
        }

        return text
    }

    fun t(text: String, isTamil: Boolean): String {
        return if (isTamil) tr(text, AppLanguage.TAMIL) else text
    }

    fun translateStage(stage: String, isTamil: Boolean): String {
        if (!isTamil) return stage
        return when {
            stage.contains("Germination", ignoreCase = true) || stage.contains("Seedling", ignoreCase = true) -> "முளைப்பு & நாற்றுப் பருவம் (Germination/Seedling)"
            stage.contains("Vegetative", ignoreCase = true) -> "தழைப்பருவம் / கிளைத்தல் (Vegetative Stage)"
            stage.contains("Flowering", ignoreCase = true) -> "பூக்கும் பருவம் (Flowering Stage)"
            stage.contains("Fruit", ignoreCase = true) || stage.contains("Grain", ignoreCase = true) || stage.contains("Development", ignoreCase = true) -> "காய் / கதிர் பிடிக்கும் பருவம் (Fruit Development)"
            stage.contains("Maturity", ignoreCase = true) || stage.contains("Harvest", ignoreCase = true) || stage.contains("Ripening", ignoreCase = true) -> "முதிர்ச்சி & அறுவடைப் பருவம் (Maturity/Harvest)"
            else -> stage
        }
    }

    fun translateIssue(issue: String, isTamil: Boolean): String {
        if (!isTamil) return issue
        return when {
            issue.contains("Healthy", ignoreCase = true) -> "பயிர் ஆரோக்கியமாக உள்ளது (Healthy Foliage)"
            issue.contains("Nitrogen", ignoreCase = true) -> "தழைச்சத்து (N) பற்றாக்குறை - இலை வெளிர்தல்"
            issue.contains("Phosphorus", ignoreCase = true) -> "மணிச்சத்து (P) பற்றாக்குறை - ஊதா நிற விளிம்புகள்"
            issue.contains("Potassium", ignoreCase = true) -> "சாம்பல்சத்து (K) பற்றாக்குறை - இலை விளிம்பு கருகல்"
            issue.contains("Calcium", ignoreCase = true) -> "கால்சியம் (Ca) குறைபாடு - குருத்து கருகல்"
            issue.contains("Magnesium", ignoreCase = true) -> "மெக்னீசியம் (Mg) குறைபாடு - நரம்பிடை வெளிர்தல்"
            issue.contains("Iron", ignoreCase = true) -> "இரும்புச்சத்து (Fe) பற்றாக்குறை"
            issue.contains("Water Deficit", ignoreCase = true) || issue.contains("Drought", ignoreCase = true) -> "நீர் பற்றாக்குறை / வறட்சி அழுத்தம்"
            issue.contains("Overwatering", ignoreCase = true) -> "அதிக நீர் தேக்கம் / வேர் மூச்சுத்திணறல்"
            issue.contains("Early Blight", ignoreCase = true) -> "ஆரம்ப இலைக்கருகல் பூஞ்சை நோய் (Early Blight)"
            issue.contains("Late Blight", ignoreCase = true) -> "பிற்கால இலைக்கருகல் நோய் (Late Blight)"
            issue.contains("Powdery Mildew", ignoreCase = true) -> "சாம்பல் நோய் (Powdery Mildew)"
            issue.contains("Downy Mildew", ignoreCase = true) -> "அடிச்சாம்பல் நோய் (Downy Mildew)"
            issue.contains("Leaf Curl", ignoreCase = true) -> "இலை சுருட்டை வைரஸ் நோய் (Leaf Curl Virus)"
            issue.contains("Root Rot", ignoreCase = true) -> "வேர் அழுகல் நோய் (Root Rot)"
            issue.contains("Heat Stress", ignoreCase = true) -> "அதிக வெப்ப அழுத்தம் (High Temperature Stress)"
            issue.contains("Salinity", ignoreCase = true) -> "மண் உவர்ப்புத்தன்மை அழுத்தம்"
            else -> tr(issue, AppLanguage.TAMIL)
        }
    }

    private val englishStrings = mapOf(
        // Navigation & Top Bar
        "nav_home" to "Home",
        "nav_dashboard" to "Dashboard",
        "nav_plants" to "My Plants",
        "nav_scan" to "Scan Plant",
        "nav_alerts" to "Alerts",
        "nav_analytics" to "Analytics",
        "farm_heatmap" to "Farm Map",
        "ai_advisor" to "AI Assistant",
        "knowledge_center" to "Knowledge Center",
        "settings" to "Settings",
        "profile" to "Profile",
        "app_title" to "AgroSense-X",
        "tagline" to "AI Smart Agriculture & Foliar Biotech",
        "lang_switch" to "Language",
        "english" to "English",
        "tamil" to "தமிழ்",
        "crop_calendar" to "Crop Planting Calendar",
        "digital_twin" to "Digital Plant Twin",
        "lab_assays" to "Biochemical Lab Assays",
        "feedback" to "AI Diagnostic Feedback",
        "guided_tour" to "System Guided Tour",
        "admin_center" to "Admin Control Center",
        "sign_out" to "Sign Out",

        // Plant Health & Status
        "plant_health" to "Plant Health",
        "healthy" to "Healthy",
        "moderate_risk" to "Moderate Risk",
        "high_risk" to "High Risk",
        "critical_risk" to "Critical Risk",
        "overall_health" to "Overall Health",
        "health_score" to "Health Score",
        "stress_detected" to "Stress Detected",

        // Stress Categories
        "water_stress" to "Water Stress",
        "nutrient_stress" to "Nutrient Stress",
        "disease_risk" to "Disease Risk",
        "pest_risk" to "Pest Risk",
        "heat_stress" to "Heat Stress",
        "cold_stress" to "Cold Stress",
        "environmental_stress" to "Environmental Stress",
        "physiological_stress" to "Physiological Stress",

        // AI Analysis & Predictions
        "ai_analysis_complete" to "AI Analysis Complete",
        "ai_detected_water_stress" to "The system detected patterns associated with moderate water stress.",
        "ai_prediction" to "AI Prediction",
        "confidence" to "Confidence",
        "actual_measurement" to "Actual measurement",
        "estimated_value" to "Estimated value",
        "confirmed_result" to "Confirmed result",

        // Recommendations
        "check_soil_moisture" to "Check Soil Moisture",
        "monitor_temperature" to "Monitor Temperature",
        "rescan_in_24h" to "Re-scan the plant in 24 hours",

        // System & Error Messages
        "something_went_wrong" to "Something went wrong",
        "unable_to_connect_server" to "Unable to connect to the server",
        "image_uploaded_success" to "Image uploaded successfully",
        "ai_analysis_unavailable" to "AI analysis is temporarily unavailable",

        // Scanner Screen
        "scanner_title" to "AI SPECTRAL LEAF DEFICIENCY & CROP SCANNER",
        "target_crop" to "Target crop",
        "new_scan_tab" to "NEW SCAN",
        "previous_scans_tab" to "PREVIOUS SCANS",
        "camera_permission_req" to "Camera Permission is Required to capture leaf images or stress symptoms.",
        "grant_camera_access" to "GRANT CAMERA ACCESS",
        "imaging_modality" to "IMAGING MODALITY",
        "lux_level" to "Lux",
        "leaf_roi_locked" to "Leaf ROI Locked",
        "align_leaf" to "Align Leaf",
        "edge_ai_ready" to "Edge AI Ready",
        "integrated_camera" to "INTEGRATED CAMERA",
        "simulation_sandbox" to "SIMULATION SANDBOX",
        "sandbox_title" to "AgroSense AI Foliar Analysis Sandbox",
        "sandbox_desc" to "Choose a crop condition to trigger the AI analysis sequence. Results are cached into Room DB immediately.",
        "run_ai_scan" to "RUN AI SPECTRAL SCAN",
        "analyzing_leaf" to "Running Spectral AI stress & species parsing...",

        // Scanned Plant Name Prediction & Selection
        "predicted_plant_identity" to "AI PREDICTED PLANT IDENTITY & RECOMMENDATIONS",
        "predicted_identity_desc" to "Select the matching plant name from AI recommendations or enter your own custom name manually:",
        "choose_recommended_name" to "Choose Predicted Name",
        "name_manually" to "Name Manually",
        "custom_plant_name_label" to "Enter Custom Plant Name",
        "save_plant_name" to "Save & Update Plant Name",
        "update_current_crop" to "Update Active Crop Name",
        "add_as_new_crop" to "Add as New Crop Profile",
        "plant_name_updated_toast" to "Plant name updated to",
        "botanical_name" to "Botanical name",
        "prediction_confidence" to "Confidence",
        "manual_name_placeholder" to "e.g. Organic Tomato Plot 2 / நாட்டு கத்திரி",

        // Scan Results
        "diagnosis_title" to "PRE-SYMPTOMATIC STRESS DIAGNOSIS",
        "cached_room_db" to "Cached in Room DB",
        "predicted_issue" to "Predicted Issue",
        "stress_probability" to "Stress Probability",
        "actionable_advice" to "Agronomist Actionable Advice:",
        "validated_by_ai" to "Diagnosis validated by AgroSense-X AI Neural Engine (Offline Cached).",
        "open_digital_twin" to "OPEN DIGITAL PLANT TWIN & XAI SUITE",
        "rate_ai_title" to "RATE AI ACCURACY & RECOMMENDATION",
        "rate_ai_desc" to "Your feedback is recorded to Room DB and synced to Firebase Firestore.",
        "accuracy_rating" to "1. AI Prediction Accuracy",
        "rec_rating" to "2. Recommendation Quality",
        "farmer_remarks" to "Optional agronomist / farmer remarks...",
        "submit_feedback" to "SUBMIT FEEDBACK RATING",

        // Weather & Telemetry
        "micro_climate_gauges" to "REAL-TIME MICRO-ENVIRONMENT GAUGES",
        "gps_live" to "Live GPS Device Telemetry • Synced",
        "air_temp" to "Air Temperature",
        "air_humidity" to "Air Humidity",
        "soil_moisture" to "Soil Moisture",
        "vpd" to "VPD Pressure",
        "solar_radiation" to "Solar Radiation",
        "wind_drift" to "Wind & Spray Drift",
        "safe_spray" to "Safe Spray",
        "high_drift" to "High Drift",
        "feels_like" to "Feels",

        // Dashboard & Calendar
        "crop_calendar_widget" to "PLANTING CALENDAR & PHENOLOGY",
        "active_crops" to "Active Crops",
        "tasks_due" to "Climate Care Tasks Due",
        "open_calendar" to "CALENDAR →",
        "room_cache_title" to "ROOM LOCAL DATABASE CACHE",
        "online_sync" to "ONLINE SYNC",
        "offline_cache" to "OFFLINE CACHE",
        "last_cloud_sync" to "Last Cloud Sync",
        "cache_status" to "Cache Status",

        // Actions & Advisories
        "irrigation_deficit" to "Irrigation Deficit Detected",
        "irrigation_adequate" to "Root-Zone Moisture Adequate",
        "spray_optimal" to "Optimal Foliar Spray Window",
        "spray_risk" to "High Spray Drift Risk",
        "refresh_live" to "REFRESH LIVE DATA",
        "close" to "CLOSE",
        "filter" to "Filter"
    )

    private val tamilStrings = mapOf(
        // Navigation & Top Bar
        "nav_home" to "முகப்பு",
        "nav_dashboard" to "முகப்பு",
        "nav_plants" to "எனது தாவரங்கள்",
        "nav_scan" to "தாவரத்தை ஸ்கேன் செய்க",
        "nav_alerts" to "எச்சரிக்கைகள்",
        "nav_analytics" to "பகுப்பாய்வு",
        "farm_heatmap" to "பண்ணை வரைபடம்",
        "ai_advisor" to "AI உதவியாளர்",
        "knowledge_center" to "அறிவு மையம்",
        "settings" to "அமைப்புகள்",
        "profile" to "சுயவிவரம்",
        "app_title" to "அக்ரோசென்ஸ்-X",
        "tagline" to "AI ஸ்மார்ட் விவசாயம் மற்றும் இலை நோயறிதல்",
        "lang_switch" to "மொழி",
        "english" to "English",
        "tamil" to "தமிழ்",
        "crop_calendar" to "பயிர் நடவு காலண்டர்",
        "digital_twin" to "டிஜிட்டல் தாவர மாதிரி (Twin)",
        "lab_assays" to "உயிர்வேதியியல் ஆய்வகம்",
        "feedback" to "AI நோயறிதல் பின்னூட்டம்",
        "guided_tour" to "பயன்பாட்டு வழிகாட்டி",
        "admin_center" to "நிர்வாகக் கட்டுப்பாட்டு மையம்",
        "sign_out" to "வெளியேறு (Sign Out)",

        // Plant Health & Status
        "plant_health" to "தாவர ஆரோக்கியம்",
        "healthy" to "ஆரோக்கியமானது",
        "moderate_risk" to "மிதமான அபாயம்",
        "high_risk" to "அதிக அபாயம்",
        "critical_risk" to "கடுமையான அபாயம்",
        "overall_health" to "மொத்த ஆரோக்கியம்",
        "health_score" to "ஆரோக்கிய மதிப்பெண்",
        "stress_detected" to "அழுத்த நிலை கண்டறியப்பட்டது",

        // Stress Categories
        "water_stress" to "நீர் அழுத்தம்",
        "nutrient_stress" to "ஊட்டச்சத்து அழுத்தம்",
        "disease_risk" to "நோய் அபாயம்",
        "pest_risk" to "பூச்சி அபாயம்",
        "heat_stress" to "வெப்ப அழுத்தம்",
        "cold_stress" to "குளிர் அழுத்தம்",
        "environmental_stress" to "சுற்றுச்சூழல் அழுத்தம்",
        "physiological_stress" to "உடலியல் அழுத்தம்",

        // AI Analysis & Predictions
        "ai_analysis_complete" to "AI பகுப்பாய்வு முடிந்தது",
        "ai_detected_water_stress" to "இந்த அமைப்பு மிதமான நீர் அழுத்தத்துடன் தொடர்புடைய வடிவங்களை கண்டறிந்துள்ளது.",
        "ai_prediction" to "AI கணிப்பு",
        "confidence" to "நம்பகத்தன்மை",
        "actual_measurement" to "உண்மையான அளவீடு",
        "estimated_value" to "மதிப்பிடப்பட்ட மதிப்பு",
        "confirmed_result" to "உறுதிப்படுத்தப்பட்ட முடிவு",

        // Recommendations
        "check_soil_moisture" to "மண்ணின் ஈரப்பதத்தை சரிபார்க்கவும்",
        "monitor_temperature" to "வெப்பநிலையை கண்காணிக்கவும்",
        "rescan_in_24h" to "24 மணி நேரத்திற்குப் பிறகு தாவரத்தை மீண்டும் ஸ்கேன் செய்யவும்",

        // System & Error Messages
        "something_went_wrong" to "ஏதோ தவறு ஏற்பட்டுள்ளது",
        "unable_to_connect_server" to "சேவையகத்துடன் இணைக்க முடியவில்லை",
        "image_uploaded_success" to "படம் வெற்றிகரமாக பதிவேற்றப்பட்டது",
        "ai_analysis_unavailable" to "AI பகுப்பாய்வு தற்காலிகமாக கிடைக்கவில்லை",

        // Scanner Screen
        "scanner_title" to "AI இலை நோய் மற்றும் பயிர் ஸ்கேனர்",
        "target_crop" to "தேர்ந்தெடுக்கப்பட்ட பயிர்",
        "new_scan_tab" to "புதிய ஸ்கேன்",
        "previous_scans_tab" to "முந்தைய ஸ்கேன்கள்",
        "camera_permission_req" to "இலை நரம்புகள் மற்றும் நோய் அறிகுறிகளை படம் பிடிக்க கேமரா அனுமதி தேவை.",
        "grant_camera_access" to "கேமரா அனுமதி வழங்கவும்",
        "imaging_modality" to "படமெடுக்கும் முறை (Modality)",
        "lux_level" to "ஒளி அளவு (Lux)",
        "leaf_roi_locked" to "இலை பகுதி கண்டறியப்பட்டது",
        "align_leaf" to "இலையை நேராக வைக்கவும்",
        "edge_ai_ready" to "AI தயார் நிலையில் உள்ளது",
        "integrated_camera" to "கேமரா ஸ்கேனர்",
        "simulation_sandbox" to "மாதிரி சோதனைப் பகுதி (Sandbox)",
        "sandbox_title" to "அக்ரோசென்ஸ் AI இலை மாதிரி ஆய்வு",
        "sandbox_desc" to "பயிரின் இலை நிலையைத் தேர்வு செய்து AI பரிசோதனையைத் தொடங்குங்கள். முடிவுகள் உடனடியாக சேமிக்கப்படும்.",
        "run_ai_scan" to "AI இலை ஸ்கேன் தொடங்கு",
        "analyzing_leaf" to "AI இலை நரம்பு மற்றும் நோய் பகுப்பாய்வு நடக்கிறது...",

        // Scanned Plant Name Prediction & Selection
        "predicted_plant_identity" to "AI கணிக்கப்பட்ட பயிர் பெயர்கள் & பரிந்துரைகள்",
        "predicted_identity_desc" to "AI பரிந்துரைக்கும் பட்டியலில் இருந்து சரியான பயிர் பெயரைத் தேர்ந்தெடுக்கவும் அல்லது கைகளால் உள்ளிடவும்:",
        "choose_recommended_name" to "பரிந்துரைக்கப்பட்ட பயிரைத் தேர்ந்தெடு",
        "name_manually" to "கைகளால் பெயரிடுங்கள்",
        "custom_plant_name_label" to "பயிரின் பெயரை உள்ளிடவும்",
        "save_plant_name" to "பயிர் பெயரைச் சேமிக்கவும்",
        "update_current_crop" to "தற்போதைய பயிர் பெயரைப் புதுப்பி",
        "add_as_new_crop" to "புதிய பயிராகச் சேர்",
        "plant_name_updated_toast" to "பயிர் பெயர் வெற்றிகரமாக மாற்றப்பட்டது: ",
        "botanical_name" to "தாவரவியல் பெயர்",
        "prediction_confidence" to "நம்பகத்தன்மை",
        "manual_name_placeholder" to "எ.கா: நாட்டு தக்காளி தோட்டம் 1 / கத்திரி",

        // Scan Results
        "diagnosis_title" to "முன்கூட்டிய நோய் மற்றும் அழுத்தக் கணிப்பு",
        "cached_room_db" to "உள்ளூர் நினைவகத்தில் சேமிக்கப்பட்டது",
        "predicted_issue" to "கண்டறியப்பட்ட பாதிப்பு",
        "stress_probability" to "அழுத்த ஆபத்து அளவு",
        "actionable_advice" to "விவசாயிக்கான செயல் வழிகாட்டுதல்:",
        "validated_by_ai" to "AgroSense-X AI இயந்திரத்தால் சரிபார்க்கப்பட்டது (ஆஃப்லைன் வசதியுடன்).",
        "open_digital_twin" to "டிஜிட்டல் தாவர மாதிரி & XAI திறக்கவும்",
        "rate_ai_title" to "AI துல்லியம் மற்றும் பரிந்துரையை மதிப்பிடவும்",
        "rate_ai_desc" to "உங்கள் மதிப்பீடு சேமிக்கப்பட்டு AI-ஐ மேலும் மேம்படுத்த உதவும்.",
        "accuracy_rating" to "1. AI கணிப்பின் துல்லியம்",
        "rec_rating" to "2. பரிந்துரையின் தரம்",
        "farmer_remarks" to "விவசாயியின் கூடுதல் குறிப்புகள் (விருப்பத்தேர்வு)...",
        "submit_feedback" to "மதிப்பீட்டைச் சமர்ப்பிக்கவும்",

        // Weather & Telemetry
        "micro_climate_gauges" to "நிகழ்நேர பண்ணை சுற்றுச்சூழல் அளவீடுகள்",
        "gps_live" to "நேரலை GPS சென்சார் தரவு • இணைக்கப்பட்டது",
        "air_temp" to "காற்று வெப்பநிலை",
        "air_humidity" to "காற்று ஈரப்பதம்",
        "soil_moisture" to "மண் ஈரப்பதம்",
        "vpd" to "ஆவியாதல் அழுத்தம் (VPD)",
        "solar_radiation" to "சூரிய கதிர்வீச்சு",
        "wind_drift" to "காற்று & மருந்து தெளிப்பு நிலை",
        "safe_spray" to "தெளிக்க உகந்தது",
        "high_drift" to "காற்று அதிகம்",
        "feels_like" to "உணரப்படும் வெப்பம்",

        // Dashboard & Calendar
        "crop_calendar_widget" to "பயிர் நடவு காலண்டர் & வளர்ச்சி நிலை",
        "active_crops" to "பயிரிடப்பட்ட பயிர்கள்",
        "tasks_due" to "இன்றைய பண்ணைப் பணிகள்",
        "open_calendar" to "காலண்டர் பார்க்க →",
        "room_cache_title" to "உள்ளூர் சேமிப்பு நினைவகம் (Room DB)",
        "online_sync" to "நேரலை இணைப்பு",
        "offline_cache" to "ஆஃப்லைன் முறை",
        "last_cloud_sync" to "கடைசி ஒத்திசைவு",
        "cache_status" to "சேமிப்பு நிலை",

        // Actions & Advisories
        "irrigation_deficit" to "நீர் பற்றாக்குறை கண்டறியப்பட்டது",
        "irrigation_adequate" to "மண்ணில் போதுமான ஈரப்பதம் உள்ளது",
        "spray_optimal" to "மருந்து தெளிக்க சிறந்த நேரம்",
        "spray_risk" to "காற்று அதிகம் - தெளிப்பதைத் தவிர்க்கவும்",
        "refresh_live" to "புதிய தகவல்களைப் பெறு",
        "close" to "மூடு",
        "filter" to "வடிகட்டு"
    )

    private val phrasesMapTamil = mapOf(
        // Common UI labels
        "Overview" to "கண்ணோட்டம்",
        "Dashboard" to "முகப்புப் பலகை",
        "My Plants" to "எனது பயிர்கள்",
        "Plant Profiles" to "பயிர் விவரங்கள்",
        "Add New Plant" to "புதிய பயிர் சேர்",
        "Add Plant" to "பயிர் சேர்",
        "Save" to "சேமி",
        "Cancel" to "ரத்துசெய்",
        "Delete" to "நீக்கு",
        "Edit" to "மாற்று",
        "Update" to "புதுப்பி",
        "Submit" to "சமர்ப்பி",
        "Back" to "பின்செல்",
        "Next" to "அடுத்து",
        "Previous" to "முந்தைய",
        "Done" to "முடிந்தது",
        "Search" to "தேடு",
        "Loading..." to "ஏற்றப்படுகிறது...",
        "Please wait..." to "தயவுசெய்து காத்திருக்கவும்...",
        "All" to "அனைத்தும்",
        "Critical" to "அவசரம் / ஆபத்து",
        "Warning" to "எச்சரிக்கை",
        "Advisory" to "வழிகாட்டுதல்",
        "Info" to "தகவல்",
        "Resolved" to "தீர்க்கப்பட்டது",
        "Unresolved" to "நடவடிக்கை தேவை",
        "Mark as Read" to "படித்ததாகக் குறி",
        "Clear All" to "அனைத்தையும் நீக்கு",
        "No Alerts" to "எச்சரிக்கைகள் எதுவும் இல்லை",
        "No alerts found" to "எந்த எச்சரிக்கையும் இல்லை",
        "Active" to "செயலில் உள்ளது",
        "Optimal" to "சிறந்த நிலை",
        "Normal" to "சாதாரண நிலை",
        "Low" to "குறைவு",
        "High" to "அதிகம்",
        "Healthy" to "ஆரோக்கியம்",
        "Stressed" to "அழுத்தத்தில் உள்ளது",
        "Deficit" to "பற்றாக்குறை",
        "Surplus" to "அதிகப்படியானது",

        // Phenological Timeline & Care
        "GROWTH STAGES & PHENOLOGY TIMELINE" to "பயிர் வளர்ச்சி நிலைகள் & காலண்டர் அட்டவணை",
        "Climate Adaptive Maturity Forecasting" to "பருவநிலைக்கேற்ப முதிர்ச்சி & அறுவடை கணிப்பு",
        "Current Stage" to "தற்போதைய வளர்ச்சி நிலை",
        "Maturity Forecast" to "அறுவடை நாள் கணிப்பு",
        "Days Remaining" to "மீதமுள்ள நாட்கள்",
        "Accumulated GDD" to "சேமிக்கப்பட்ட வெப்ப அலகுகள் (GDD)",
        "Base Temp" to "அடிப்படை வெப்பநிலை",
        "Climate Adjustment" to "காலநிலை மாற்றம்",
        "Care Reminders" to "பண்ணைப் பராமரிப்பு நினைவூட்டல்கள்",
        "Complete" to "முடிந்தது",
        "Pending" to "நிலுவையில் உள்ளது",
        "Due Today" to "இன்று செய்ய வேண்டியது",
        "What-If Temperature Simulation" to "வெப்பநிலை மாற்ற மாதிரி சோதனை (Simulation)",
        "Temp Adjustment" to "வெப்பநிலை மாற்றம்",
        "Estimated Shift" to "எதிர்பார்க்கப்படும் கால மாற்றம்",
        "Irrigation" to "பாசனம்",
        "Fertigation" to "உர மேலாண்மை",
        "Protection" to "பயிர் பாதுகாப்பு",
        "Pruning" to "கவாத்து செய்தல்",

        // Analytics & Charts
        "FARM SENSOR ANALYTICS & TELEMETRY" to "பண்ணை சென்சார் பகுப்பாய்வு & வரைபடங்கள்",
        "Historical Environmental Trends & Telemetry" to "வரலாற்று காலநிலை மற்றும் சென்சார் மாற்றங்கள்",
        "Last 24 Hours" to "கடந்த 24 மணிநேரம்",
        "Last 7 Days" to "கடந்த 7 நாட்கள்",
        "Last 30 Days" to "கடந்த 30 நாட்கள்",
        "Vapor Pressure Deficit (VPD)" to "ஆவியாதல் பற்றாக்குறை அழுத்தம் (VPD)",
        "Soil Moisture Dynamics" to "மண் ஈரப்பத மாற்றங்கள்",
        "Canopy vs Ambient Temperature" to "பயிர் இலை vs காற்று வெப்பநிலை",
        "Photochemical Quantum Yield" to "ஒளிச்சேர்க்கை திறன் குறியீடு (Fv/Fm)",
        "Yield Forecast Model" to "எதிர்பார்க்கப்படும் மகசூல் கணிப்பு மாதிரி",
        "Export Telemetry CSV" to "சென்சார் தரவை பதிவிறக்கு (CSV)",

        // Knowledge Center
        "AGRONOMY & PHYSIOLOGY KNOWLEDGE BASE" to "விவசாய அறிவியல் & பயிர் நோயறிதல் களஞ்சியம்",
        "Water Stress" to "நீர் பற்றாக்குறை & வறட்சி",
        "Nutrient Stress" to "ஊட்டச்சத்து குறைபாடுகள்",
        "Plant Diseases" to "பயிர் பூஞ்சை & வைரஸ் நோய்கள்",
        "Plant Physiology" to "தாவர உடலியல் & ஒளிச்சேர்க்கை",
        "Soil & Microclimate" to "மண் வளம் & பசுமைக்குடில்",
        "AI in Agriculture" to "விவசாயத்தில் செயற்கை நுண்ணறிவு (AI)",
        "Read Article" to "கட்டுரையைப் படிக்கவும்",
        "Scientific Mechanism" to "அறிவியல் செயல்முறை விளக்கம்",
        "Early Symptoms" to "முன்கூட்டியே தோன்றும் அறிகுறிகள்",
        "Detection Methods" to "கண்டறியும் நவீன முறைகள்",
        "Preventative Protocols" to "தடுப்பு மற்றும் மேலாண்மை வழிமுறைகள்",

        // Settings & Admin
        "SETTINGS & PREFERENCES" to "அமைப்புகள் & விருப்பத்தேர்வுகள்",
        "FARMER LANGUAGE / மொழி" to "பயன்பாட்டு மொழி / FARMER LANGUAGE",
        "Select Tamil or English for farmer accessibility" to "விவசாயிகளின் பயன்பாட்டிற்கு தமிழ் அல்லது ஆங்கிலத்தைத் தேர்ந்தெடுக்கவும்",
        "Backend FastAPI Endpoint" to "பின்னணி சேவையக முகவரி (FastAPI)",
        "Device GPS Telemetry" to "மொபைல் GPS நேரலை இருப்பிடம்",
        "Offline Database Storage" to "ஆஃப்லைன் உள்ளூர் சேமிப்பகம் (Room DB)",
        "Total Cached Records" to "சேமிக்கப்பட்ட மொத்த பதிவுகள்",
        "Clear Local Cache" to "உள்ளூர் நினைவகத்தை அழிக்கவும்",
        "Cloud Sync Status" to "மேகக்கணி ஒத்திசைவு நிலை (Firestore)",
        "User Profile" to "விவசாயி சுயவிவரம்",
        "Farmer" to "விவசாயி",
        "Agronomist" to "வேளாண் விஞ்ஞானி",
        "Admin" to "நிர்வாகி",

        // Digital Twin & Lab
        "DIGITAL PLANT TWIN & XAI ENGINE" to "டிஜிட்டல் தாவர மாதிரி & XAI விளக்கம்",
        "Transpiration Rate" to "நீராவிப்போக்கு வேகம்",
        "Stomatal Conductance" to "இலைத்துளை கடத்துதிறன்",
        "Chlorophyll Index (SPAD)" to "குளோரோபில் பச்சைய அளவு (SPAD)",
        "Nitrogen Balance Index" to "தழைச்சத்து சமநிலை குறியீடு (NBI)",
        "Anthocyanin Stress Marker" to "அந்தோசயனின் அழுத்த குறியீடு",
        "Cell Membrane Stability" to "செல் சவ்வு உறுதித்தன்மை",
        "Biochemical Lab Assays" to "உயிர்வேதியியல் ஆய்வக சோதனைகள்",
        "Run Assay" to "ஆய்வை மேற்கொள்",
        "Farm Heatmap Matrix" to "பண்ணை வெப்பநிலை வரைபடம்",
        "Sensor Node Grid" to "சென்சார் முனை வரைபடம்",

        // AI Assistant
        "AgroSense AI Agronomist" to "அக்ரோசென்ஸ் AI வேளாண் ஆலோசகர்",
        "Ask anything about your crops, irrigation, fertilizers or diseases" to "பயிர் பராமரிப்பு, உர மேலாண்மை அல்லது பூச்சி நோய்கள் பற்றி எதுவேண்டுமானாலும் கேளுங்கள்",
        "Type your question..." to "உங்கள் கேள்வியை உள்ளிடவும்...",
        "Send" to "அனுப்பு",
        "Why is my plant stressed?" to "எனது பயிர் ஏன் அழுத்தத்தில் உள்ளது?",
        "What should I check right now?" to "இப்போது நான் என்ன செய்ய வேண்டும்?",
        "Show plants with high risk" to "பாதிப்பு அதிகமுள்ள பயிர்களைக் காட்டு",
        "Explain my latest AI prediction" to "கடைசி AI பரிசோதனை முடிவை விளக்கு",

        // Admin Center & Dialogs
        "Create AI Scan Record" to "AI ஸ்கேன் பதிவை உருவாக்கவும்",
        "Target Crop:" to "தேர்ந்தெடுக்கப்பட்ட பயிர்:",
        "Diagnosed Issue" to "கண்டறியப்பட்ட பாதிப்பு",
        "Health Score: " to "ஆரோக்கிய மதிப்பெண்: ",
        "Stress Probability: " to "அழுத்த ஆபத்து அளவு: ",
        "Recommendation" to "பரிந்துரை",
        "SAVE SCAN" to "ஸ்கேனைச் சேமி",
        "CLEAR ALL" to "அனைத்தையும் நீக்கு",
        "Clear All" to "அனைத்தையும் நீக்கு",
        "Add Entry" to "பதிவு சேர்",
        "No physiological health timeline records." to "உடலியல் ஆரோக்கிய காலவரிசை பதிவுகள் எதுவும் இல்லை.",
        "Add Health History Record" to "உடலியல் ஆரோக்கிய பதிவைச் சேர்",
        "Crop:" to "பயிர்:",
        "Date (YYYY-MM-DD)" to "தேதி (YYYY-MM-DD)",
        "ADD RECORD" to "பதிவைச் சேர்",
        "Clear All History" to "அனைத்து வரலாற்றுப் பதிவுகளையும் நீக்கு",
        "Are you sure you want to clear all historical health tracking entries?" to "அனைத்து வரலாற்று ஆரோக்கிய கண்காணிப்பு பதிவுகளையும் நீக்க விரும்புகிறீர்களா?",
        "Clear" to "நீக்கு",
        "Broadcast" to "அறிவிப்பு செய்",
        "No active system alerts in database." to "தரவுத்தளத்தில் সক্রিয় எச்சரிக்கைகள் எதுவும் இல்லை.",
        "Broadcast Agronomic Alert" to "விவசாய எச்சரிக்கையை வெளியிடு",
        "Severity:" to "தீவிரம் (Severity):",
        "Alert Message" to "எச்சரிக்கை செய்தி",
        "BROADCAST" to "வெளியிடு",
        "Clear All Alerts" to "அனைத்து எச்சரிக்கைகளையும் நீக்கு",
        "Are you sure you want to clear all active alerts from database?" to "தரவுத்தளத்தில் உள்ள அனைத்து எச்சரிக்கைகளையும் நீக்க விரும்புகிறீர்களா?",
        "No user/agronomist feedback entries in database." to "தரவுத்தளத்தில் பயனர்/விவசாயி கருத்துகள் எதுவும் இல்லை.",
        "Prediction Accuracy (Stars: " to "கணிப்பு துல்லியம் (நட்சத்திரங்கள்: ",
        "Advice Quality (Stars: " to "பரிந்துரை தரம் (நட்சத்திரங்கள்: ",
        "Agronomist Comments" to "விவசாயியின் கருத்துகள்",
        "Clear All Feedback" to "அனைத்து கருத்துகளையும் நீக்கு",
        "Are you sure you want to clear all feedback entries from database?" to "தரவுத்தளத்தில் உள்ள அனைத்து கருத்துப் பதிவுகளையும் நீக்க விரும்புகிறீர்களா?",
        "FastAPI Endpoint URL" to "FastAPI சேவையக முகவரி URL",
        "Soil Moisture Alert Threshold (%)" to "மண் ஈரப்பதம் எச்சரிக்கை வரம்பு (%)",
        "RESTORE DEFAULT SEED DATASET" to "இயல்புநிலை தரவுத்தொகுப்பை மீட்டமை",
        "WIPE ENTIRE DATABASE (CLEAR ALL)" to "தரவுத்தளத்தை முழுமையாக அழிக்கவும்",
        "Restore Default Seed Data" to "இயல்புநிலை தரவுத்தொகுப்பை மீட்டமை",
        "This will reset all tables (plants, sensors, predictions, history, alerts, feedback) and seed clean initial demo data. Continue?" to "இது அனைத்து அட்டவணைகளையும் மீட்டமைத்து சுத்தமான டெமோ தரவை வழங்கும். தொடரவா?",
        "CONFIRM RESTORE" to "மீட்டமைப்பை உறுதிப்படுத்து",
        "Wipe Entire Database" to "தரவுத்தளத்தை முழுமையாக அழிக்கவும்",
        "WARNING: All data across all 6 tables will be permanently erased. Are you completely sure?" to "எச்சரிக்கை: அனைத்து தரவுகளும் நிரந்தரமாக நீக்கப்படும். நிச்சயமாகத் தொடர விரும்புகிறீர்களா?",
        "CONFIRM WIPE" to "அழிப்பை உறுதிப்படுத்து"
    )

    private val caseInsensitiveTamil = phrasesMapTamil.entries.associate { (k, v) -> k.lowercase() to v }
}

/**
 * Data class representing an AI-predicted crop identity recommendation.
 */
data class CropPredictionCandidate(
    val englishName: String,
    val tamilName: String,
    val botanicalName: String,
    val iconEmoji: String,
    val defaultConfidence: Int,
    val varietySuggestion: String,
    val descriptionEn: String,
    val descriptionTa: String,
    val typicalSoilMoistureTarget: String
)

/**
 * Knowledge registry providing crop prediction candidates and fuzzy matching.
 */
object CommonCropsRegistry {

    val allCrops = listOf(
        CropPredictionCandidate(
            englishName = "Tomato",
            tamilName = "தக்காளி",
            botanicalName = "Solanum lycopersicum",
            iconEmoji = "🍅",
            defaultConfidence = 96,
            varietySuggestion = "Shivam Hybrid / PKM-1",
            descriptionEn = "Solanaceous fruit crop with high calcium and potassium demands during fruit set.",
            descriptionTa = "கால்சியம் மற்றும் பொட்டாசியம் சத்துக்கள் அதிகம் தேவைப்படும் முக்கிய காய்கறி பயிர்.",
            typicalSoilMoistureTarget = "60% - 75% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Chilli / Pepper",
            tamilName = "மிளகாய்",
            botanicalName = "Capsicum annuum",
            iconEmoji = "🌶️",
            defaultConfidence = 94,
            varietySuggestion = "K1 / Co-4 / Guntur Sanam",
            descriptionEn = "Susceptible to thrips and leaf curl virus under hot, dry conditions.",
            descriptionTa = "வெப்பமான சூழ்நிலையில் இலைச்சுருட்டை மற்றும் அசுவினி தாக்குதலுக்கு வாய்ப்புள்ளது.",
            typicalSoilMoistureTarget = "55% - 70% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Eggplant / Brinjal",
            tamilName = "கத்தரி",
            botanicalName = "Solanum melongena",
            iconEmoji = "🍆",
            defaultConfidence = 92,
            varietySuggestion = "Annamalai / Bhavani / Co-2",
            descriptionEn = "Requires balanced nitrogen and regular irrigation; vulnerable to fruit/shoot borer.",
            descriptionTa = "சீரான தழைச்சத்து மற்றும் நீர் பாசனம் தேவைப்படும் நீண்டகால காய்கறி பயிர்.",
            typicalSoilMoistureTarget = "60% - 75% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Paddy / Rice",
            tamilName = "நெல்",
            botanicalName = "Oryza sativa",
            iconEmoji = "🌾",
            defaultConfidence = 95,
            varietySuggestion = "ADT 45 / Ponni / IR 64",
            descriptionEn = "Staple cereal requiring saturated soil and timely split nitrogen application.",
            descriptionTa = "நிலையான நீர் மற்றும் தகுந்த தழைச்சத்து மேலாண்மை தேவைப்படும் முதன்மை உணவுப் பயிர்.",
            typicalSoilMoistureTarget = "Saturated / Submerged 2-5cm"
        ),
        CropPredictionCandidate(
            englishName = "Okra / Ladyfinger",
            tamilName = "வெண்டை",
            botanicalName = "Abelmoschus esculentus",
            iconEmoji = "🌿",
            defaultConfidence = 91,
            varietySuggestion = "Co-3 / Arka Anamika",
            descriptionEn = "Fast growing annual; monitor for Yellow Vein Mosaic Virus (YVMV).",
            descriptionTa = "வேகமாக வளரும் பயிர்; மஞ்சள் நரம்பு தேமல் வைரஸ் நோயைக் கண்காணிக்கவும்.",
            typicalSoilMoistureTarget = "50% - 65% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Banana",
            tamilName = "வாழை",
            botanicalName = "Musa acuminata",
            iconEmoji = "🍌",
            defaultConfidence = 89,
            varietySuggestion = "Grand Naine / Robusta / Nendran",
            descriptionEn = "Heavy potassium consumer; sensitive to soil dehydration and wind stress.",
            descriptionTa = "அதிக சாம்பல்சத்து மற்றும் தொடர் பாசனம் தேவைப்படும் பணப்பயிர்.",
            typicalSoilMoistureTarget = "65% - 80% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Sugarcane",
            tamilName = "கரும்பு",
            botanicalName = "Saccharum officinarum",
            iconEmoji = "🎋",
            defaultConfidence = 88,
            varietySuggestion = "Co 86032 / Co 0212",
            descriptionEn = "Long-duration crop; high biomass accumulation requiring adequate moisture.",
            descriptionTa = "நீண்டகால பணப்பயிர்; கரும்பு தடிமன் அதிகரிக்க சீரான நீர்ப்பாசனம் அவசியம்.",
            typicalSoilMoistureTarget = "60% - 75% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Cotton",
            tamilName = "பருத்தி",
            botanicalName = "Gossypium hirsutum",
            iconEmoji = "☁️",
            defaultConfidence = 87,
            varietySuggestion = "MCU 5 / Suraj / Bt-II",
            descriptionEn = "Requires deep taproot moisture; susceptible to sucking pests and bollworms.",
            descriptionTa = "ஆழமான வேர் மண்டலம் கொண்டது; உறிஞ்சும் பூச்சிகளைக் கட்டுப்படுத்துவது முக்கியம்.",
            typicalSoilMoistureTarget = "50% - 65% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Maize / Corn",
            tamilName = "மக்காச்சோளம்",
            botanicalName = "Zea mays",
            iconEmoji = "🌽",
            defaultConfidence = 93,
            varietySuggestion = "CoH(M) 6 / Pioneer 3396",
            descriptionEn = "High photosynthetic efficiency (C4); critical water demand at tasseling/silking.",
            descriptionTa = "அதிவேக வளர்ச்சி கொண்ட C4 பயிர்; பூக்கும் தருணத்தில் தண்ணீர் பற்றாக்குறை இருக்கக்கூடாது.",
            typicalSoilMoistureTarget = "60% - 70% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Turmeric",
            tamilName = "மஞ்சள்",
            botanicalName = "Curcuma longa",
            iconEmoji = "🟡",
            defaultConfidence = 90,
            varietySuggestion = "Erode Local / BSR-2 / Alleppey",
            descriptionEn = "Rhizome crop; thrives in rich organic matter with good drainage to prevent rot.",
            descriptionTa = "கிழங்கு அழுகல் ஏற்படாமல் இருக்க நல்ல வடிகால் வசதியுடன் கூடிய மண் தேவை.",
            typicalSoilMoistureTarget = "60% - 75% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Groundnut / Peanut",
            tamilName = "நிலக்கடலை",
            botanicalName = "Arachis hypogaea",
            iconEmoji = "🥜",
            defaultConfidence = 88,
            varietySuggestion = "TMV 7 / VRI 2 / Kadiri 6",
            descriptionEn = "Legume with symbiotic nitrogen fixation; gypsum application essential at pegging.",
            descriptionTa = "விருது இறங்கும் பருவத்தில் ஜிப்சம் இடுவது திரட்சியான காய்களுக்கு உதவும்.",
            typicalSoilMoistureTarget = "50% - 65% Field Capacity"
        ),
        CropPredictionCandidate(
            englishName = "Onion",
            tamilName = "வெங்காயம்",
            botanicalName = "Allium cepa",
            iconEmoji = "🧅",
            defaultConfidence = 91,
            varietySuggestion = "Co (On) 5 / Bhima Super",
            descriptionEn = "Shallow root system requiring frequent light irrigations; monitor for thrips.",
            descriptionTa = "மேலோட்டமான வேர் அமைப்பு; அடிக்கடி மிதமான நீர்ப்பாசனம் மற்றும் சாம்பல் நோய் பாதுகாப்பு தேவை.",
            typicalSoilMoistureTarget = "60% - 70% Field Capacity"
        )
    )

    fun getBestMatchesFor(query: String): List<CropPredictionCandidate> {
        val q = query.lowercase().trim()
        if (q.isBlank()) return allCrops

        val matches = allCrops.filter { crop ->
            crop.englishName.lowercase().contains(q) ||
            crop.tamilName.lowercase().contains(q) ||
            crop.botanicalName.lowercase().contains(q) ||
            q.contains(crop.englishName.lowercase()) ||
            q.contains(crop.tamilName.lowercase())
        }

        return if (matches.isNotEmpty()) {
            val nonMatches = allCrops.filterNot { matches.contains(it) }
            matches + nonMatches
        } else {
            allCrops
        }
    }
}
