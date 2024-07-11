import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.time.LocalDate

data class MapProperties(val googleMap: com.google.android.gms.maps.GoogleMap)
data class MapUiSettings(val uiSettings: com.google.android.gms.maps.UiSettings)
data class MarkerData(
    val position: LatLng,
    val name: String,
    val whatHappens: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val selectedTime: Pair<Int, Int>?,
    val participants: Int,
    val access: String
)


// Класс для хранения состояния MapView
data class MapViewState(
    val userLocation: LatLng? = null,
    val userMarker: Marker? = null
)