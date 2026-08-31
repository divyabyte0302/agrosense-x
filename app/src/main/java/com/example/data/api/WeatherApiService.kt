package com.example.data.api

import com.example.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getRealTimeWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentParams: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,vapour_pressure_deficit,direct_radiation,soil_temperature_0cm,soil_moisture_0_to_1cm",
        @Query("hourly") hourlyParams: String = "temperature_2m,relative_humidity_2m,soil_temperature_0cm,soil_moisture_0_to_1cm,vapour_pressure_deficit",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 2
    ): OpenMeteoResponse
}
