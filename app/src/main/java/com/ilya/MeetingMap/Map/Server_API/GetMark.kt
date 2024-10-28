package com.ilya.MeetingMap.Map.Server_API


import MapMarker
import android.util.Log

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException

import java.io.IOException
import java.util.concurrent.TimeUnit



// Интерфейс для получения маркеров
interface GetMark {
    @GET("/get/public/mark/{uid}/{lat}/{lon}")
    suspend fun getMarker(
        @Path("uid") uid: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double
    ): List<MapMarker>
}

// Функция для получения маркеров по координатам
suspend fun getMarker(uid: String, latLng: LatLng): List<MapMarker> {
    // Логирование запросов для диагностики
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Клиент с увеличенными тайм-аутами
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS) // время подключения
        .readTimeout(30, TimeUnit.SECONDS)    // время чтения ответа
        .writeTimeout(30, TimeUnit.SECONDS)   // время записи данных
        .build()

    // Инициализация Retrofit с OkHttpClient и базовым URL
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    Log.d("MapMarker_getMarker", "URL запроса: https://meetmap.up.railway.app/get/public/mark/$uid/${latLng.latitude}/${latLng.longitude}")

    // Создание экземпляра интерфейса API
    val apiService = retrofit.create(GetMark::class.java)

    // Выполнение запроса в фоновом потоке
    return withContext(Dispatchers.IO) {
        try {
            val markerData = apiService.getMarker(uid, latLng.latitude, latLng.longitude)
            Log.d("MapMarker_getMarker", "Полученные данные маркера: $markerData")
            markerData
        } catch (e: HttpException) {
            Log.e("MapMarker_getMarker", "HTTP ошибка: ${e.code()} - ${e.response()?.errorBody()?.string()}", e)
            throw e
        } catch (e: IOException) {
            Log.e("MapMarker_getMarker", "Сетевая ошибка", e)
            throw e
        } catch (e: Exception) {
            Log.e("MapMarker_getMarker", "Неизвестная ошибка", e)
            throw e
        }
    }

}
