package com.ilya.MeetingMap.Mine_menu


import com.ilya.MeetingMap.MENU.Server_API.Became_Participant_fun
import MapMarker
import MarkerAdapter
import MarkerData
import SpaceItemDecoration
import WebSocketCallback
import WebSocketManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
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
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bitmapDescriptorFromVector
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.ilya.MeetingMap.MENU.Server_API.getMarker

import com.ilya.MeetingMap.MENU.Server_API.postInvite
import com.ilya.MeetingMap.MENU.WebSocketClient.Friends_type
import com.ilya.MeetingMap.MENU.WebSocketClient.WebSocketClient
import com.ilya.MeetingMap.MENU.WebSocketClient.show_friends.show_friends_one
import com.ilya.MeetingMap.MENU.shake_logik.ShakeDetector


import com.ilya.MeetingMap.R
import com.ilya.codewithfriends.presentation.profile.ID
import com.ilya.codewithfriends.presentation.profile.IMG
import com.ilya.codewithfriends.presentation.profile.UID
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import com.ilya.reaction.logik.PreferenceHelper.getUserKey
import decodePoly
import generateUID
import getAddressFromCoordinates
import getMapRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import markerDataToMapMarker
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import okhttp3.OkHttpClient
import post_user_info
import sendGetRequest
import showAddMarkerDialog
import show_friends_fourth
import show_friends_more
import show_friends_third
import show_friends_two
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalPermissionsApi::class)
class Main_menu : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMapClickListener, WebSocketCallback {

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
    private lateinit var webSocketClient: WebSocketClient
    private var currentDialog: AlertDialog? = null
    private val collectedFriends = mutableListOf<Friends_type>()

    var currentLatLngGlobal by mutableStateOf<LatLng>(LatLng(0.0, 0.0))
    var routePoints by mutableStateOf<LatLng>(LatLng(0.0, 0.0))
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private val client = OkHttpClient()
    var markers by mutableStateOf(listOf<MarkerData>())
    private lateinit var shakeDetector: ShakeDetector


    val webSocketManager = WebSocketManager(client, this)

    private companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 1
    }

    var uid_main = ""

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        // Инициализация WebSocketClient
        val uid_user = ID(userData = googleAuthUiClient.getSignedInUser())
        val url = "wss://meetmap.up.railway.app/map/$uid_user/${getUserKey(this)}"
        webSocketClient = WebSocketClient(url)
        webSocketClient.start()



        setContentView(R.layout.activity_map)



        val name = UID(userData = googleAuthUiClient.getSignedInUser())
        val img = IMG(userData = googleAuthUiClient.getSignedInUser())
        val uid = ID(userData = googleAuthUiClient.getSignedInUser())

        // Запуск корутины в соответствующем месте
        CoroutineScope(Dispatchers.IO).launch {
            if(getUserKey(this@Main_menu) == "")
            {
                sendGetRequest("$uid", client, this@Main_menu)
            }
            getUserKey(this@Main_menu)?.let { post_user_info(it, uid.toString(), name.toString(), img.toString()) }
            }

        val bottomSheet: View = findViewById(R.id.bottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        // Устанавливаем начальное состояние
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        // Устанавливаем начальное состояние свернутого листа
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        // Можно установить максимальную высоту или другие параметры
        bottomSheetBehavior.peekHeight = 200 // Высота в свернутом состоянии
        // Добавляем слушатель, чтобы отслеживать изменения состояния
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {

                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {

                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        val textView: TextView = findViewById(R.id.infoTextView)
                        textView.text = getString(R.string.my_tags)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        Log.d("URL_GET_MAKER", "${currentLatLngGlobal.latitude} and ${currentLatLngGlobal.longitude}")

        uid_main = uid.toString()

        Log.d("UserKey", getUserKey(this).toString())
        supportActionBar?.hide()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Пример вызова этой функции из корутины

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
                //WEBSOCKET
            // получаю данные по метка где я участник, по websoket
        lifecycleScope.launch {
            var previousMarkers: List<MarkerData>? = null // Храним предыдущие данные
            while (true) {
                val markerList: MutableList<MarkerData> = mutableListOf()
                while (true) {
                    try {
                        // Ждем завершения асинхронного запроса
                        val response = webSocketClient.sendCommandAndGetResponse("get_participant_mark $uid_main ${getUserKey(this@Main_menu)}").await()
                        Log.d("WebSocket i got", " $response")

                        // Декодируем текущие данные из ответа
                        val currentMarkers = Json.decodeFromString<List<MarkerData>>(response)

                        // Сравниваем текущие данные с предыдущими
                        if (currentMarkers != previousMarkers) {
                            // Данные изменились, обрабатываем их
                            handleReceivedMarkers(response, "$uid", markerList)

                            withContext(Dispatchers.Main) {
                                // Обновление UI или выполнение других действий с данными
                                currentMarkers.forEach { markerData ->
                                    Log.d("WebSocket", "Marker: $markerData")

                                    // Преобразование MarkerData в MapMarker
                                    val mapMarker = markerDataToMapMarker(markerData)

                                    // Добавление маркера на карту
                                    val markerLatLng = LatLng(mapMarker.lat, mapMarker.lon)
                                    val marker = addMarker(markerLatLng, mapMarker.name)
                                    if (marker != null) {
                                        markerDataMap[marker] = mapMarker
                                    }
                                }
                            }
                        } else {
                            Log.d("WebSocket", "Markers unchanged, skipping handleReceivedMarkers")
                        }
                        // Обновляем предыдущие данные
                        previousMarkers = currentMarkers
                    } catch (e: Exception) {
                        Log.e("WebSocket", "Error processing markers", e)
                    }
                    // Пауза перед следующим запросом
                    delay(10000) // 10 секунд
                }
            }
        }

    }


    private var isItemDecorationAdded = false // Флаг

    private suspend fun handleReceivedMarkers(jsonData: String, uid: String, markerList: MutableList<MarkerData>) {

        val markers = withContext(Dispatchers.IO) {
            try {
                Json.decodeFromString<List<MarkerData>>(jsonData)
            } catch (e: Exception) {
                Log.e("WebSocket", "Error parsing JSON: ${e.message}")
                emptyList<MarkerData>()
            }
        }

        withContext(Dispatchers.Main) {
            if (markers.isNotEmpty()) {
                markerList.clear()
                markerList.addAll(markers)

                Log.d("WebSocket", "Markers updated: $markerList")

                val recyclerView: RecyclerView = findViewById(R.id.markerRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(this@Main_menu)

                // Добавляем ItemDecoration только один раз
                if (!isItemDecorationAdded) {
                    val space = resources.getDimensionPixelSize(R.dimen.space_between_items)
                    recyclerView.addItemDecoration(SpaceItemDecoration(space))
                    isItemDecorationAdded = true
                }

                val adapter = recyclerView.adapter
                if (adapter is MarkerAdapter) {
                    adapter.notifyDataSetChanged()
                } else {
                    recyclerView.adapter = MarkerAdapter(markerList, this@Main_menu, uid)
                }
            } else {
                Log.d("WebSocket", "No new markers or markers are the same")
            }
        }
    }

        // Вызов алерт диалог , для тогочтобы показать друга
    // Реализация метода интерфейса WebSocketCallback
    override fun onMessageReceived(dataList: List<WebSocketManager.ReceivedData>) {
            runOnUiThread {
                if (dataList.isEmpty()) return@runOnUiThread

                // Преобразуем список ReceivedData в список Friends_type
                val newFriends = dataList.map { data ->
                    Friends_type(
                        name = data.user_name,
                        img = data.img,
                        key = data.key
                    )
                }

                // Добавляем новых друзей в список собранных данных
                collectedFriends.addAll(newFriends)

                // Ждем 1-2 секунды перед обработкой собранных данных
                Handler(Looper.getMainLooper()).postDelayed({
                    // Проверяем, сколько друзей собрано, и показываем соответствующий диалог
                    when (collectedFriends.size) {
                            1 -> {
                            currentDialog?.dismiss()  // Закрываем текущий диалог, если он есть

                                    show_friends_one(this, collectedFriends)

                        }

                        2 -> {
                            currentDialog?.dismiss()  // Закрываем текущий диалог, если он есть

                                show_friends_two(this, collectedFriends)

                        }
                        3 -> {
                            currentDialog?.dismiss()  // Закрываем текущий диалог, если он есть

                                show_friends_third(this, collectedFriends)


                        }
                        4 -> {
                            currentDialog?.dismiss()  // Закрываем текущий диалог, если он есть

                                show_friends_fourth(this, collectedFriends)


                        }
                        5 -> {
                               show_friends_more(this, collectedFriends)

                        }

                    }

                    // Очищаем список после отображения
                    collectedFriends.clear()
                   // webSocketManager.shutdown()

                    // Показать конфетти
                    val konfettiView = findViewById<KonfettiView>(R.id.konfettiView_map)
                    val emitterConfig = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
                    konfettiView.start(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(Color.YELLOW, Color.GREEN, Color.MAGENTA),
                            position = Position.Relative(0.5, 0.0),
                            size = listOf(Size.SMALL, Size.LARGE),
                            timeToLive = 3000L,
                            shapes = listOf(Shape.Square),
                            emitter = emitterConfig
                        )
                    )

                    // Показываем Toast с информацией о первой записи
                    val firstData = collectedFriends.firstOrNull()
                    firstData?.let {
                        Toast.makeText(this, "Получены данные: ${it.name}, ${it.img}, ${it.key}", Toast.LENGTH_LONG).show()
                    }
                }, 200) // Ждем 1 секунду перед обработкой
            }
        }

    fun onFindLocation(lat: Double, lon: Double) {
        findLocation_mark(lat, lon) // Вызов функции перемещения камеры
        routePoints = LatLng(lat, lon)
    }

    private var currentPolyline: Polyline? = null

    fun findLocation_route() {
        CoroutineScope(Dispatchers.Main).launch {
            val routeGeometry = getMapRoute(currentLatLngGlobal.latitude, currentLatLngGlobal.longitude, routePoints.latitude, routePoints.longitude)
            routeGeometry?.let {
                val routePoints = decodePoly(it)

                // Удаляем предыдущий маршрут, если он существует
                currentPolyline?.remove()

                // Добавляем новый маршрут
                currentPolyline = addRouteToMap(routePoints, 5)
            }
            Log.d("MapRoute", "Route geometry: $routeGeometry")
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        polylineOptions = PolylineOptions()
    }

    private val markerDataMap = mutableMapOf<Marker, MapMarker>()

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
        // ключение отображения магазинов, кофе, ресторанов и других POI
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
// Где я могу нати положение маркера
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
                        var currentLatLng = LatLng(it.latitude, it.longitude)
                        currentLatLngGlobal = currentLatLng
                        //   mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                        lifecycleScope.launch {
                            while (true) {
                                try {
                                    val markers = getMarker(uid_main, LatLng(it.latitude, it.longitude))
                                    // Обработка списка маркеров
                                    markers.forEach { mapMarker ->
                                        val markerLatLngnew = LatLng(mapMarker.lat, mapMarker.lon)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val marker = addMarker(markerLatLngnew, mapMarker.name)
                                            // Проверка на null
                                            if (marker != null) {
                                                // Сохраните соответствие между меткой и данными
                                                markerDataMap[marker] = mapMarker
                                            }
                                        }
                                    }

                                } catch (e: Exception) {
                                    // Обработка ошибки
                                 //   Log.e("MarkerData", "Error fetching markers", e)
                                }
                            }

                        }


                            // иотправку данных в WebSocket по таймеру
                        // код котрый опредляет тряску телефона
                        shakeDetector = ShakeDetector(this, object : ShakeDetector.OnShakeListener {
                            private val shakeInterval: Long = 10 * 1000 // 10 секунд
                            private var lastShakeTime: Long = 0

                            override fun onShake() {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastShakeTime >= shakeInterval) {
                                    lastShakeTime = currentTime

                                    val key = getUserKey(this@Main_menu)
                                    val lat = currentLatLng.latitude
                                    val lon = currentLatLng.longitude
                                    val url = "wss://meetmap.up.railway.app/shake/$key/$lat/$lon"

                                    // Открываем WebSocket
                                    webSocketManager.setupWebSocket(url)

                                    Toast.makeText(this@Main_menu, "Телефон трясут! Подключение...", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.d("ShakeDetector", "Слишком рано для нового вызова.")
                                }
                            }
                        })



                        // Отслеживание скорости и расстояния
                        updateSpeed(it.speed)
                        updateDistance(it)

                        mMap.setOnMarkerClickListener { marker ->
                            markerDataMap[marker]?.let { mapMarker ->
                                showMarkerDialog(mapMarker)
                                Log.d("MarkerData_new2", mapMarker.toString())
                            }
                            true // Возвращаем true, чтобы событие не обрабатывалось дальше
                        }
                    }
                }
        }


        // Добавление слушателя нажатия по карте
        mMap.setOnMapClickListener(this)


        // Инициализация объекта Polyline для отображения маршрута
        polyline = mMap.addPolyline(PolylineOptions().width(5f).color(android.graphics.Color.BLUE))

            // Добавьте обработчик для кнопки проложения маршрута
        val routeButton = findViewById<ImageView>(R.id.routeButton)

        var isRouteDrawn = false // Флаг для проверки, построен ли маршрут

        routeButton.setOnClickListener {
            if (isRouteDrawn) {
                // Если маршрут уже построен, удаляем его
                currentPolyline?.remove() // Удаление полилинии
                removeMarkers()
                isRouteDrawn = false // Меняем состояние
            } else {
                // Если маршрут не построен, строим его
                findLocation_route() // Функция для построения маршрута
                isRouteDrawn = true // Меняем состояние
            }
        }

    }


    private val markerList = mutableListOf<Marker>()  // Список для сохранения маркеров

    private fun addRouteToMap(routePoints: List<LatLng>, circleSpacing: Int): Polyline {
        // Удаляем предыдущую полилинию, если она существует
        currentPolyline?.remove()

        // Удаляем все маркеры с карты
        removeMarkers()

        // Создаем полилинию для маршрута
        val polylineOptions = PolylineOptions()
            .addAll(routePoints)
            .width(12f)
            .color(Color.parseColor("#4285F4"))  // Основной цвет линии
            .geodesic(true)               // Сглаживание углов
            .startCap(RoundCap())         // Закругление начала линии
            .endCap(RoundCap())           // Закругление конца линии
            .jointType(JointType.ROUND)   // Закругление соединений между линиями

        // Добавляем полилинию на карту и сохраняем её в currentPolyline
        currentPolyline = mMap.addPolyline(polylineOptions)

        // Получаем уменьшенный Bitmap для кружков
        val customCircleBitmap = getResizedBitmap(R.drawable.custom_circle, 20, 20)  // Уменьшаем до 20x20 пикселей

        // Добавляем кастомные кружки с регулируемым расстоянием
        for (i in routePoints.indices step circleSpacing) {
            val point = routePoints[i]
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .icon(customCircleBitmap)  // Используем уменьшенное изображение кружка
                    .anchor(0.5f, 0.5f)  // Центр маркера совпадает с точкой маршрута
            )
            // Добавляем маркер в список для дальнейшего удаления
            markerList.add(marker!!)
        }

        // Возвращаем новую полилинию
        return currentPolyline!!
    }

    // Удаление всех маркеров с карты
    private fun removeMarkers() {
        for (marker in markerList) {
            marker.remove()
        }
        // Очищаем список маркеров
        markerList.clear()
    }

    // Функция для изменения размера Bitmap
    private fun getResizedBitmap(drawableId: Int, width: Int, height: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // Создаем уменьшенный bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    private fun showMarkerDialog(marker: MapMarker) {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_view_marker, null)
        val marker_image  = dialogView.findViewById<ImageView>(R.id.marker_image)
        val marker_name = dialogView.findViewById<TextView>(R.id.marker_name)
        val marker_about_marker = dialogView.findViewById<TextView>(R.id.marker_about_marker)
        val marker_street = dialogView.findViewById<TextView>(R.id.marker_street)
        val marker_start_Date = dialogView.findViewById<TextView>(R.id.marker_start_Date)
        val marker_end_Date = dialogView.findViewById<TextView>(R.id.marker_end_Date)
        val marker_button_not = dialogView.findViewById<Button>(R.id.marker_button_not)
        val marker_button_ready = dialogView.findViewById<Button>(R.id.marker_button_ready)

        val key = getUserKey(this@Main_menu)

        // Установка данных маркера в элементы диалога
        marker_name.text = marker.name
        marker_about_marker.text = marker.whatHappens
        marker_street.text = ""
        marker_start_Date.text = "${marker.startDate} Time:${marker.startTime}"
        marker_end_Date.text = "${marker.endDate} Time:${marker.endTime}"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        // Создание и показ диалога
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_background)

        // Обработка нажатия кнопки "Нет"
        marker_button_not.setOnClickListener {
            dialog.dismiss()
        }

        // Обработка нажатия кнопки "Готово"
        marker_button_ready.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Became_Participant_fun(uid_main, key.toString(), marker.id)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onMapClick(latLng: LatLng) {
        showAddMarkerDialog(latLng, this, uid_main, this)
    }

    fun set_addMarker(latLng: LatLng, markerName: String){
        addMarker(latLng, markerName)
    }

   private fun addMarker(latLng: LatLng, markerName: String): Marker? {
        // Проверьте, инициализирована ли карта
        if (::mMap.isInitialized) {
            // Добавьте новую метку
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(markerName)
                    .icon(
                        bitmapDescriptorFromVector(
                            this@Main_menu, // Контекст (возможно, вам понадобится другой)
                            R.drawable.location_on_, // Ресурс маркера
                            "FF005B", // Цвет маркера в шестнадцатеричном формате
                            140, // Ширина маркера
                            140  // Высота маркера
                        )
                    )
            )
            // Сохраните метку в переменную destinationMarker, если нужно
            destinationMarker = marker

            // Возвращаем созданную метку
            return marker
        } else {
            Log.e("MapError", "mMap не инициализирован")
            return null
        }
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

                routePoints = LatLng(location.latitude, location.longitude)
            } else {
                // Обработка случая, когда результаты геокодирования пусты
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun findLocation_mark(lat: Double, lon: Double) {
        // Создание объекта LatLng с переданными координатами
        val latLng = LatLng(lat, lon)

        // Проверка, что карта доступна
        mMap?.let { map ->
            // Добавление метки на карту
            // Перемещение камеры к метке
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        } ?: run {
            // Обработка случая, когда карта не доступна
            Toast.makeText(this, "Map is not available", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.closeWebSocket()
        webSocketClient.close()
        shakeDetector.stop()  // Останавливаем детектор, когда activity уничтожается
    }

    private fun generateUniqueRequestId(): String {
        // Генерация уникального идентификатора для запроса
        return System.currentTimeMillis().toString()
    }



}

