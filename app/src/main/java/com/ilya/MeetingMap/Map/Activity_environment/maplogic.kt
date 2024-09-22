import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.Locale

    fun bitmapDescriptorFromVector(context: Context, icon: Any, colorString: String, width: Int, height: Int): BitmapDescriptor {
        when (icon) {
            is Int -> {
                val vectorDrawable = ContextCompat.getDrawable(context, icon)
                vectorDrawable?.let {
                    val drawable = DrawableCompat.wrap(it).mutate()

                    val color = Color.fromHex(colorString).toArgb() // Получаем ARGB представление цвета


                    DrawableCompat.setTint(drawable, color)
                    drawable.setBounds(0, 0, width, height) // Устанавливаем заданный размер
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.draw(canvas)
                    return BitmapDescriptorFactory.fromBitmap(bitmap)
                }
            }
            is BitmapDrawable -> {
                val bitmap = Bitmap.createScaledBitmap(icon.bitmap, width, height, false)
                return BitmapDescriptorFactory.fromBitmap(bitmap)
            }
            else -> {
                // Обработка других типов источников, если необходимо
            }
        }
        // В случае ошибки возвращаем стандартный маркер
        return BitmapDescriptorFactory.defaultMarker()
    }



    fun Color.Companion.fromHex(colorString: String): Color {
        val colorWithoutHash = colorString.removePrefix("#") // Убираем #, если он есть

        if (colorWithoutHash.length != 6 && colorWithoutHash.length != 8) {
            throw IllegalArgumentException("Invalid hex color string: $colorString")
        }

        val color = android.graphics.Color.parseColor("#$colorWithoutHash") // Добавляем #, если его не было
        return Color(color)
    }


fun getAddressFromLatLon(context: Context, lat: Double, lon: Double): String? {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses: List<Address>?
    val address: Address?
    var addressText: String? = null

    try {
        // Получаем список адресов по координатам
        addresses = geocoder.getFromLocation(lat, lon, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            address = addresses[0]
            addressText = address.getAddressLine(0) // Получаем полный адрес
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return addressText
}


// Data классы для Nominatim API
data class NominatimResponse(
    val place_id: String,
    val lat: String,
    val lon: String,
    val display_name: String,
    val address: Addressnew
)
data class Addressnew(
    val road: String?,       // Название улицы (может быть null)
    val house_number: String?, // Номер дома (может быть null)
    val suburb: String?,      // Район (может быть null)
    val city: String?,        // Город (может быть null)
    val state: String?,       // Штат/область (может быть null)
    val country: String?      // Страна (может быть null)
)



// Интерфейс для Retrofit
interface NominatimService {
    @GET("reverse")
    suspend fun getAddress(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): NominatimResponse
}


suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? {
    // Создание Retrofit экземпляра
    val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Создание сервиса
    val service = retrofit.create(NominatimService::class.java)

    return try {
        // Запрос на получение адреса
        val response = service.getAddress(latitude, longitude)
        // Возврат названия улицы, если оно существует
        response.address.road ?: "Street not found"
    } catch (e: Exception) {
        // Логирование ошибки
        Log.e("NominatimResponse", "Error getting street: ${e.message}", e)
        null
    }
}


