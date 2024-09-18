import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Path

// интерфейс для отправки delete запроса на сервре
interface Delete_My_Marker {
    @DELETE("/delete/mymarker/{markerid}/{uid}/{key}")
    suspend fun deleteMarker(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Path("marker_id") marker_id: String
    ): String
}

//корутинная функция котруя я могу вызвать чтобы удолить метку с карты и из сервера .   git  
suspend fun deleteMyMarker(uid: String, key: String, marker_id: String): String {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://meetmap.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(Delete_My_Marker::class.java)
    return apiService.deleteMarker(uid, key, marker_id)
}