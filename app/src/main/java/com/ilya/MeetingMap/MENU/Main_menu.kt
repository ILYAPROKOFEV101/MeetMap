package com.ilya.MeetingMap.Mine_menu


import com.ilya.MeetingMap.MENU.Server_API.Became_Participant_fun
import MapMarker
import MarkerAdapter
import MarkerData
import SpaceItemDecoration
import WebSocketCallback
import WebSocketManager
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
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
import generateUID
import getAddressFromCoordinates
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
import show_friends_fourth
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
                        else -> {
                            // В случае, если друзей больше четырех, показываем диалог с предупреждением или предлагаем выбор
                            AlertDialog.Builder(this)
                                .setTitle("Too many friends")
                                .setMessage("You have more than four friends to display. Please select which ones to view or refine your selection.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
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
    }

    fun showAddMarkerDialog(latLng: LatLng) {
        // Раздуйте макет диалога
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_marker, null)
        var access = false // Переменная для хранения состояния Switch
        // Найдите элементы внутри макета диалога
        val selectDateButton_start = dialogView.findViewById<Button>(R.id.selectDateButtonstart)
        val selectDateButton_end = dialogView.findViewById<Button>(R.id.selectDateButtonend)
        val selectTimeButtonstart = dialogView.findViewById<Button>(R.id.selectTimeButton_start)
        val selectTimeButtonend = dialogView.findViewById<Button>(R.id.selectTimeButton_end)
        val editName = dialogView.findViewById<EditText>(R.id.editname)
        val editobout = dialogView.findViewById<EditText>(R.id.editobout)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBar)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        val publicSwitch = dialogView.findViewById<Switch>(R.id.switch2) // Найдите ваш Switch
        var selectedDate_start: String? = null
        var selectedDate_end: String? = null
        var selectedTime: Pair<Int, Int>? = null
         var startTime: String? = null
         var endTime: String? = null
         publicSwitch.text = if (!access) "Публичная метка" else "Приватная метка"
        publicSwitch.setOnCheckedChangeListener { _, isChecked ->
            access = isChecked
            publicSwitch.text = if (!access) "Публичная метка" else "Приватная метка"
        }
        // Listener для отслеживания изменений SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Прогресс от 0 до 99, добавляем 1 для диапазона от 1 до 100
                textView.text = "мест в группе: ${progress + 1}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Действия при начале изменения значения SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Действия при остановке изменения значения SeekBar
            }
        })
         selectDateButton_start.setOnClickListener {
             val datePicker = MaterialDatePicker.Builder.datePicker()
                 .build()

             datePicker.show(supportFragmentManager, "DATE_PICKER")

             datePicker.addOnPositiveButtonClickListener {
                 val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                 selectedDate_start = dateFormat.format(Date(it))
                 selectDateButton_start.text = selectedDate_start
             }
         }
         selectDateButton_end.setOnClickListener {
             val datePicker = MaterialDatePicker.Builder.datePicker()
                 .setTitleText("Выберите дату")
                 .build()

             datePicker.show(supportFragmentManager, "DATE_PICKER")

             datePicker.addOnPositiveButtonClickListener {
                 val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                 selectedDate_end = dateFormat.format(Date(it))
                 selectDateButton_end.text = selectedDate_end
             }
         }
         selectTimeButtonstart.setOnClickListener {
             val is24HourFormat = true
             val timeFormat = if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

             val timePicker = MaterialTimePicker.Builder()
                 .setTimeFormat(timeFormat)
                 .setTitleText("Выберите время")
                 .build()

             timePicker.show(supportFragmentManager, "TIME_PICKER")

             timePicker.addOnPositiveButtonClickListener {
                 val hour = timePicker.hour
                 val minute = timePicker.minute
                 val formattedTime = String.format("%02d:%02d", hour, minute)
                 selectedTime = Pair(hour, minute)
                 selectTimeButtonstart.text = formattedTime
                 startTime = formattedTime // Сохраняем выбранное время в переменной
             }
         }
         selectTimeButtonend.setOnClickListener {
             val is24HourFormat = true
             val timeFormat = if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

             val timePicker = MaterialTimePicker.Builder()
                 .setTimeFormat(timeFormat)
                 .setTitleText("Выберите время")
                 .build()

             timePicker.show(supportFragmentManager, "TIME_PICKER")

             timePicker.addOnPositiveButtonClickListener {
                 val hour = timePicker.hour
                 val minute = timePicker.minute
                 val formattedTime = String.format("%02d:%02d", hour, minute)
                 selectedTime = Pair(hour, minute)
                 selectTimeButtonend.text = formattedTime
                 endTime = formattedTime // Сохраняем выбранное время в переменной
             }
         }


         // Создаем диалоговое окно с инфлейтированным макетом
         val builder = AlertDialog.Builder(this)
         builder.setView(dialogView)
             .setPositiveButton("Да") { dialog, _ ->
                 // Получаем текст из EditText
                 val editText = dialogView.findViewById<EditText>(R.id.editname)
                 val markerDescription = editobout.text.toString() // Получаем текст из поля editAbout
                 val markerTitle = editText.text.toString()

                 if (markerTitle.isNotEmpty()) {
                     val participants = seekBar.progress + 1

                     // Запуск корутины для получения улицы
                     CoroutineScope(Dispatchers.IO).launch {
                         val street = getAddressFromCoordinates(latLng.latitude, latLng.longitude) ?: "Unknown Street"

                         // Теперь создаем MarkerData после получения улицы
                         val markerData = MarkerData(
                             key = getUserKey(this@Main_menu).toString(),
                             username = "Ilya",
                             imguser = "Photo",
                             photomark = "photo",
                             street = street,
                             id = generateUID(),
                             lat = latLng.latitude,
                             lon = latLng.longitude,
                             name = markerTitle,
                             whatHappens = markerDescription,
                             startDate = selectedDate_start?.let { LocalDate.parse(it) }.toString(),
                             endDate = selectedDate_end?.let { LocalDate.parse(it) }.toString(),
                             startTime = startTime.toString(),
                             endTime = endTime.toString(),
                             participants = participants,
                             access = access
                         )

                         // Запуск на главном потоке для обновления UI
                         withContext(Dispatchers.Main) {
                             addMarker(latLng, markerTitle)

                             val gson = Gson()
                             val markerDataJson = gson.toJson(markerData)
                             Log.d("PushDataJoin", "MarkerData JSON: $markerDataJson")

                             // Запуск корутины для отправки данных на сервер
                             CoroutineScope(Dispatchers.IO).launch {
                                 postInvite(getUserKey(this@Main_menu).toString(), uid_main, markerData)
                             }

                             Log.d("Markersonmap", markers.toString())
                         }
                     }
                 } else {
                     Toast.makeText(this, "Название метки не может быть пустым", Toast.LENGTH_SHORT).show()
                 }
                 dialog.dismiss()
             }
             .setNegativeButton("Отмена") { dialog, _ ->
                 dialog.dismiss()
             }

         val dialog = builder.create()
         dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_background)
         dialog.show()

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
                                    Log.e("MarkerData", "Error fetching markers", e)
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
                                    lastShakeTime = currentTime // Обновляем время последнего вызова

                                    val key = getUserKey(this@Main_menu)
                                    val lat = currentLatLng.latitude
                                    val lon = currentLatLng.longitude
                                    val url = "wss://meetmap.up.railway.app/shake/$key/$lat/$lon"

                                    // Закрываем и открываем новый WebSocket
                                    webSocketManager.setupWebSocket(url)

                                    Toast.makeText(this@Main_menu, "Телефон трясут!", Toast.LENGTH_SHORT).show()
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

        routeButton.setOnClickListener{
            webSocketManager.setupWebSocket("wss://meetmap.up.railway.app/shake/q2Bl2KZmXXMuwg4ME2LHG3wKFobKNH/59.4191129/30.3393293")
        }



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
        showAddMarkerDialog(latLng)
    }

    private fun addMarker(latLng: LatLng, markerName: String): Marker? {
        // Добавьте новую метку
        val marker = mMap?.addMarker(
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

