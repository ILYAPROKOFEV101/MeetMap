package com.ilya.MeetingMap.Mine_menu


import MapProperties
import MapUiSettings
import MapViewState
import MarkerData
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.compose.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.ilya.MeetingMap.R
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalPermissionsApi::class)
class Main_menu : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,

    GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var polylineOptions: PolylineOptions
    private var speedTextView: TextView? = null
    private var distanceTextView: TextView? = null
    private var totalDistance: Double = 0.0
    private var lastLocation: Location? = null
    private var speedUnit = "KM/H"
    private val updateSpeedHandler = Handler()
    private var destinationMarker: Marker? = null
    private lateinit var polyline: Polyline


    var name by mutableStateOf("")
    var googleMapState by  mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null)
    var newMarkerPosition by  mutableStateOf<LatLng?>(null)
    private var showDialog by  mutableStateOf(false)
    //  var markers by  mutableStateOf(listOf<LatLng>())
    var markers by mutableStateOf(listOf<MarkerData>())


    var selectedTime by mutableStateOf<Pair<Int, Int>?>(null)
    var userMarker by mutableStateOf<Marker?>(null)



    private companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        supportActionBar?.hide()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        speedTextView = findViewById(R.id.speedTextView)
        distanceTextView = findViewById(R.id.distanceTextView)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initializeMap()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun showAddMarkerDialog(latLng: LatLng) {
        // Раздуйте макет диалога
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_marker, null)

        // Найдите элементы внутри макета диалога
        val selectDateButton = dialogView.findViewById<Button>(R.id.selectDateButton)
        val selectTimeButton = dialogView.findViewById<Button>(R.id.selectTimeButton)
        val dialogOkButton = dialogView.findViewById<Button>(R.id.dialogOkButton)
        val editName = dialogView.findViewById<EditText>(R.id.editname)

        var selectedDate: String? = null
        var selectedTime: String? = null

        selectDateButton.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.format(Date(it))
                selectDateButton.text = selectedDate
            }
        }
        selectTimeButton.setOnClickListener {
            val is24HourFormat = true // Измените на false для 12-часового формата
            val timeFormat = if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(timeFormat)
                // .setTheme(R.style.ThemeOverlay_MaterialComponents_TimePicker)
                .setTitleText("Выберите время")
                .build()

            timePicker.show(supportFragmentManager, "TIME_PICKER")

            timePicker.addOnPositiveButtonClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute
                selectedTime = String.format("%02d:%02d", hour, minute)
                selectTimeButton.text = selectedTime
            }
        }

        dialogOkButton.setOnClickListener {
            val name = editName.text.toString()

            if (selectedDate != null && selectedTime != null) {
                Toast.makeText(this, "Вы выбрали метку: $name на $selectedDate в $selectedTime", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Пожалуйста, выберите дату и время", Toast.LENGTH_SHORT).show()
            }
        }

        // Создаем диалоговое окно с инфлейтированным макетом
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setTitle("Добавить метку?")
            .setPositiveButton("Да") { dialog, _ ->
                // Получаем текст из EditText
                val editText = dialogView.findViewById<EditText>(R.id.editname)
                val markerTitle = editText.text.toString()
                if (markerTitle.isNotEmpty()) {
                    // Ваш метод для добавления маркера
                     addMarker(latLng, markerTitle)
                } else {
                    Toast.makeText(this, "Название метки не может быть пустым", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        polylineOptions = PolylineOptions()
    }


    fun onStandardButtonClick(view: View) {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    // Метод для обработки нажатия на кнопку "Satellite"
    fun onSatelliteButtonClick(view: View) {
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Включение отображения кнопки переключения типа карты
        mMap.uiSettings.isMapToolbarEnabled = true

        // Включение слоя спутниковой карты
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        // Включение слоя трафика
        mMap.isTrafficEnabled = false

        // Включение слоя зданий
        mMap.isBuildingsEnabled = true

        // Включение внутренних карт (если данные поддерживаются)
        mMap.isIndoorEnabled = true

        // Отключение отображения магазинов, музеев и других POI
        //  mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        // Отключение отображения магазинов, кофе, ресторанов и других POI
        val styleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        mMap.setMapStyle(styleOptions)

        val locationAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.locationAutoCompleteTextView)
        val findButton = findViewById<ImageView>(R.id.findButton)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        locationAutoCompleteTextView.setAdapter(adapter)

        // Добавление слушателя нажатия по карте
        mMap.setOnMapClickListener(this)

        // Обработка выбора места из AutoCompleteTextView
        locationAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position).toString()
            findLocation(selectedItem)
        }

        // Обработка нажатия на кнопку "Найти"
        findButton.setOnClickListener {
            val locationText = locationAutoCompleteTextView.text.toString()
            if (locationText.isNotEmpty()) {
                findLocation(locationText)
            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }


        // Обработка выбора места из AutoCompleteTextView

        locationAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position).toString()
            findLocation(selectedItem)
        }


        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                     //   mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))


                        // Отслеживание скорости и расстояния
                        updateSpeed(it.speed)
                        updateDistance(it)

                        // Добавление метки вручную на карту
                        val customMarkerLatLng = LatLng(37.7749, -122.4194)
                        mMap.addMarker(MarkerOptions().position(customMarkerLatLng).title("Custom Marker"))
                    }
                }
        }


        // Добавление слушателя нажатия по карте
        mMap.setOnMapClickListener(this)


        // Инициализация объекта Polyline для отображения маршрута
        polyline = mMap.addPolyline(PolylineOptions().width(5f).color(android.graphics.Color.BLUE))

// Добавьте обработчик для кнопки проложения маршрута
        val routeButton = findViewById<ImageView>(R.id.routeButton)
        routeButton.setOnClickListener {

        }


    }


    override fun onMapClick(latLng: LatLng) {
        showAddMarkerDialog(latLng)
    }

    private fun addMarker(latLng: LatLng, markerName: String) {

        // Добавьте новую метку
        destinationMarker = mMap
            ?.addMarker(MarkerOptions()
                .position(latLng)
                .title("$markerName")
                .icon(bitmapDescriptorFromVector(
                    this@Main_menu, // Контекст (возможно, вам понадобится другой)
                    R.drawable.location_on_, // Ресурс маркера
                    "FF005B", // Цвет маркера в шестнадцатеричном формате
                    140, // Ширина маркера
                    140  // Высота маркера
                ))
            )

        // Другие действия, если нужно
    }


    // Добавьте метод для поиска местоположения по адресу
    private fun findLocation(address: String) {
        val geocoder = Geocoder(this)
        try {
            val results = geocoder.getFromLocationName(address, 1)
            if (results != null && results.isNotEmpty()) {
                val location = results[0]
                val latLng = LatLng(location.latitude, location.longitude)

                // Добавление метки на карту
                mMap.addMarker(MarkerOptions().position(latLng).title(address))

                // Перемещение камеры к метке
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                // Обработка случая, когда результаты геокодирования пусты
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initializeMap()
                } else {
                    // Handle the case where the user denies location permission
                }
            }
        }
    }

    private fun addPointToPolyline(latLng: LatLng) {
        polylineOptions.add(latLng)
        mMap.addPolyline(polylineOptions)
    }

    private fun clearPolyline() {
        polylineOptions.points.clear()
        mMap.clear()
    }

    override fun onPolylineClick(polyline: Polyline) {
        // Handle the click event on the polyline if needed
    }

    private fun updateSpeed(speed: Float) {
        speedUnit = "km/h" // Установка единиц измерения в километры в час

        // Отображение скорости
        speedTextView?.text = String.format("Speed: %.2f $speedUnit", speed)
    }

    private fun updateDistance(location: Location) {
        if (lastLocation != null) {
            val distance = location.distanceTo(lastLocation!!)
            totalDistance += distance.toDouble()
            distanceTextView?.text = String.format("Distance: %.2f meters", totalDistance)
        }
        lastLocation = location
    }



}



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


