import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface Get_MY_Participant {
    @GET("/get/participantmark/{uid}/{key}") //Получить метки участников
    suspend fun getParticipant(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): List<MapMarker>
}

suspend fun getParticipant(uid: String, key: String): List<MapMarker> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(Get_MY_Participant::class.java)
    return withContext(Dispatchers.IO) {
        try {
            val markerData = apiService.getParticipant(uid, key)
            Log.d("MarkerData", "Received marker data: $markerData")
            markerData
        } catch (e: Exception) {
            Log.d("MarkerData", "Error fetching marker data", e)
            throw e
        }
    }
}