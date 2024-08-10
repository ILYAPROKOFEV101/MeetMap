import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException

interface Get_MY_Participant {
    @GET("/get/participantmark/{uid}/{key}") //Получить метки участников
    suspend fun getParticipant(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): List<MarkerData>
}

suspend fun getParticipant(uid: String, key: String): List<MarkerData> {
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
        } catch (e: HttpException) {
            Log.e("MarkerData", "HTTP error: ${e.code()}", e)
            throw e
        } catch (e: IOException) {
            Log.e("MarkerData", "Network error", e)
            throw e
        } catch (e: Exception) {
            Log.e("MarkerData", "Unknown error", e)
            throw e
        }

    }
}