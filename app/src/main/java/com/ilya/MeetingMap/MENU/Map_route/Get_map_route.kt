import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject




suspend fun getMapRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double): String? {
    val client = OkHttpClient()
    val url = "https://router.project-osrm.org/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=full"

    Log.d("MapRoute", "Запрос к OSRM API: $url")

    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            Log.d("MapRoute", "Статус ответа: ${response.code}")

            if (response.isSuccessful) {
                val jsonString = response.body?.string()
                Log.d("MapRoute", "Ответ сервера: $jsonString")

                jsonString?.let {
                    val jsonObject = JSONObject(it)
                    val routes = jsonObject.getJSONArray("routes")

                    Log.d("MapRoute", "Количество маршрутов: ${routes.length()}")

                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val geometry = route.getString("geometry")
                        Log.d("MapRoute", "Закодированная геометрия маршрута: $geometry")
                        return@withContext geometry
                    } else {
                        Log.w("MapRoute", "Маршруты отсутствуют в ответе")
                    }
                }
            } else {
                Log.e("MapRoute", "Неуспешный ответ от сервера: ${response.code}")
            }
            null

        } catch (e: Exception) {
            Log.e("MapRoute", "Ошибка при выполнении запроса: ${e.localizedMessage}")
            e.printStackTrace()
            null
        }
    }
}




 fun decodePoly(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }

    return poly
}

