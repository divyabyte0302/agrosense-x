package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Real-time Micro-Environment weather telemetry gathered from device location and meteorological station APIs.
 */
@Entity(tableName = "weather_telemetry")
@JsonClass(generateAdapter = true)
data class LiveWeatherTelemetry(
    @PrimaryKey val id: Int = 1,
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val locationName: String = "Farm Station Alpha",
    val region: String = "",
    val country: String = "",
    val elevation: Double = 120.0,
    val temperatureCelsius: Float = 24.5f,
    val apparentTemperatureCelsius: Float = 25.1f,
    val relativeHumidity: Float = 62.0f,
    val soilTemperatureCelsius: Float = 21.8f,
    val soilMoisturePercentage: Float = 42.5f,
    val vaporPressureDeficitKPa: Float = 1.15f,
    val solarRadiationWattsPerSqM: Float = 680.0f,
    val precipitationMm: Float = 0.0f,
    val rainMm: Float = 0.0f,
    val windSpeedKmh: Float = 7.5f,
    val windDirectionDegrees: Float = 180.0f,
    val surfacePressureHPa: Float = 1013.2f,
    val weatherCode: Int = 0,
    val weatherDescription: String = "Clear Sky",
    val weatherIcon: String = "☀️",
    val isGpsLive: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    val temperatureC: Float get() = temperatureCelsius
    val humidityPercent: Float get() = relativeHumidity
}

/**
 * Open-Meteo Forecast Response DTOs
 */
@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val elevation: Double = 0.0,
    val timezone: String? = null,
    val current: OpenMeteoCurrent? = null,
    val hourly: OpenMeteoHourly? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    val time: String = "",
    val interval: Int = 0,
    @field:Json(name = "temperature_2m") val temperature2m: Float = 0f,
    @field:Json(name = "relative_humidity_2m") val relativeHumidity2m: Float = 0f,
    @field:Json(name = "apparent_temperature") val apparentTemperature: Float = 0f,
    val precipitation: Float = 0f,
    val rain: Float = 0f,
    @field:Json(name = "weather_code") val weatherCode: Int = 0,
    @field:Json(name = "surface_pressure") val surfacePressure: Float = 1013.2f,
    @field:Json(name = "wind_speed_10m") val windSpeed10m: Float = 0f,
    @field:Json(name = "wind_direction_10m") val windDirection10m: Float = 0f,
    @field:Json(name = "vapour_pressure_deficit") val vapourPressureDeficit: Float? = null,
    @field:Json(name = "direct_radiation") val directRadiation: Float? = null,
    @field:Json(name = "soil_temperature_0cm") val soilTemperature0cm: Float? = null,
    @field:Json(name = "soil_moisture_0_to_1cm") val soilMoisture0to1cm: Float? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    val time: List<String> = emptyList(),
    @field:Json(name = "temperature_2m") val temperature2m: List<Float> = emptyList(),
    @field:Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Float> = emptyList(),
    @field:Json(name = "soil_temperature_0cm") val soilTemperature0cm: List<Float> = emptyList(),
    @field:Json(name = "soil_moisture_0_to_1cm") val soilMoisture0to1cm: List<Float> = emptyList(),
    @field:Json(name = "vapour_pressure_deficit") val vapourPressureDeficit: List<Float> = emptyList()
)

/**
 * Hourly Microclimate Forecast Item for UI Charts & Insights
 */
data class HourlyMicroclimate(
    val timeHour: String, // e.g. "14:00"
    val temperature: Float,
    val humidity: Float,
    val soilMoisture: Float,
    val vpd: Float
)

/**
 * Location coordinate wrapper with reverse geocoding result.
 */
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val cityName: String = "",
    val stateOrRegion: String = "",
    val countryName: String = "",
    val formattedAddress: String = ""
)

object WeatherCodeMapper {
    fun getInfo(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Clear Sky" to "☀️"
            1 -> "Mainly Clear" to "🌤️"
            2 -> "Partly Cloudy" to "⛅"
            3 -> "Overcast" to "☁️"
            45, 48 -> "Foggy / Mist" to "🌫️"
            51, 53, 55 -> "Drizzle" to "🌦️"
            56, 57 -> "Freezing Drizzle" to "🌧️"
            61, 63, 65 -> "Rain Showers" to "🌧️"
            66, 67 -> "Freezing Rain" to "🌧️"
            71, 73, 75, 77 -> "Snowfall" to "❄️"
            80, 81, 82 -> "Heavy Showers" to "🌧️"
            85, 86 -> "Snow Showers" to "🌨️"
            95, 96, 99 -> "Thunderstorm" to "⛈️"
            else -> "Fair" to "🌤️"
        }
    }
}
