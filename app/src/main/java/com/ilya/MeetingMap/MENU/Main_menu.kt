package com.ilya.MeetingMap.Mine_menu


import MapProperties
import MapUiSettings
import MapViewState
import MarkerData
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.AlertDialog
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
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ilya.MeetingMap.R
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalPermissionsApi::class)
class Main_menu : ComponentActivity() {

    var name by mutableStateOf("")
    var googleMapState by  mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null)
    var newMarkerPosition by  mutableStateOf<LatLng?>(null)
    private var showDialog by  mutableStateOf(false)
       //  var markers by  mutableStateOf(listOf<LatLng>())
    var markers by mutableStateOf(listOf<MarkerData>())
    

    var selectedTime by mutableStateOf<Pair<Int, Int>?>(null)
    var userMarker by mutableStateOf<Marker?>(null)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(showDialog){
                        CrearMarc()
                    }
                    MapScreen()
                }
            }
            Log.d("Marker", "onCreate: $markers")
        }
    }


    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun MapScreen() {
        val context = LocalContext.current
        val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
        val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val coroutineScope = rememberCoroutineScope()

        var userLocation by remember { mutableStateOf<LatLng?>(null) }

        var mapProperties by remember { mutableStateOf<MapProperties?>(null) }
        var uiSettings by remember { mutableStateOf<MapUiSettings?>(null) }
        var isMyLocationEnabled by remember { mutableStateOf(true) }
        var mapType by remember { mutableStateOf(com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL) }
        var mapState by remember { mutableStateOf(MapViewState()) }
        val permissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)



        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    MapView(context).apply {
                        onCreate(Bundle())
                        onResume() // Добавлено для перезапуска MapView
                        getMapAsync { googleMap ->
                            googleMapState = googleMap
                            googleMap.isMyLocationEnabled = true
                            MapsInitializer.initialize(context)
                            mapProperties = MapProperties(googleMap)
                            uiSettings = MapUiSettings(googleMap.uiSettings)

                            googleMap.apply {
                                mapType = this.mapType
                                isMyLocationEnabled = this.isMyLocationEnabled

                                // Установка слушателя кликов на карту
                                googleMap.setOnMapClickListener { latLng ->
                                    newMarkerPosition = latLng
                                    showDialog = true
                                }

                                try {
                                    val success = googleMap.setMapStyle(
                                        MapStyleOptions.loadRawResourceStyle(
                                            context,
                                            R.raw.map_style
                                        )
                                    )
                                    if (!success) {
                                        Log.e("MapsActivity", "Style parsing failed.")
                                    }
                                } catch (e: Resources.NotFoundException) {
                                    Log.e("MapsActivity", "Can't find style.", e)
                                }
                            }
                        }
                    }
                },
                update = { mapView ->
                    mapView.getMapAsync { googleMap ->
                        coroutineScope.launch {
                            // 1. Check and Request Location Permissions


                            // 2. Get FusedLocationProviderClient
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                            // 3. Create Location Request
                            val locationRequest = LocationRequest.create().apply {
                                interval = 1000 // Update interval in milliseconds
                                fastestInterval = 500 // Fastest update interval in milliseconds
                                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            }

                            // 4. Location Callback
                            val locationCallback = object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult) {
                                    for (location in locationResult.locations) {
                                        // Update UI with location data
                                        val currentLatLng = LatLng(location.latitude, location.longitude)
                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                                        mapState.userMarker?.position = currentLatLng
                                    }
                                }
                            }

                            // 5. Request Location Updates
                            try {
                                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                            } catch (unlikely: SecurityException) {
                                Log.e("MapScreen", "Lost location permission. Could not request updates. $unlikely")
                            }
                        }
                    }
                }

            )


            // Кнопка для изменения типа карты
            Button(
                onClick = {
                    mapType =
                        if (mapType == com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL) {
                            com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
                        } else {
                            com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
                        }
                    googleMapState?.mapType = mapType
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Text(text = if (mapType == com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL) "Спутник" else "Обычный")
            }



           /* // Кнопка для нахождения текущего местоположения
            IconButton(
                onClick = {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            googleMapState?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    15f
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Icon(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(id = R.drawable.my_location_24px),
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.primary
                )
            }*/
        }
    }

    @Preview
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CrearMarc() {
        var name by remember { mutableStateOf("") }
        var whathapends by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        val calendarState = rememberSheetState()
        var currentRange by remember { mutableStateOf(5) }
        var currentValue by remember { mutableStateOf(1) }
        var access by remember { mutableStateOf("") }
        // Состояния для хранения выбранных дат
        val startDateState = remember { mutableStateOf<LocalDate?>(null) }
        val endDateState = remember { mutableStateOf<LocalDate?>(null) }

        // Состояние для отслеживания, какую дату мы выбираем (начало или конец)
        val isSelectingStartDate = remember { mutableStateOf(true) }



        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { /* Ничего не делаем здесь, кнопки ниже */ },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
          //  containerColor = Color(0xFF5AC4F6), // Синий фон
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), // Преобразование в Int
            shape = RoundedCornerShape(30.dp),
            dismissButton = {
                Column(Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            TextField(
                                value = name, // Текущее значение текста в поле
                                onValueChange = {
                                    name = it
                                }, // Обработчик изменения текста, обновляющий переменную "text"
                                textStyle = TextStyle(fontSize = 20.sp), // Стиль текста, используемый в поле ввода
                                // textStyle = TextStyle.Default, // Стиль текста, используемый в поле ввода (используется стандартный стиль)

                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = Color.Transparent, // Цвет индикатора при фокусе на поле (прозрачный - отключает индикатор)
                                    unfocusedIndicatorColor = Color.Transparent, // Цвет индикатора при потере фокуса на поле (прозрачный - отключает индикатор)
                                    disabledIndicatorColor = MaterialTheme.colorScheme.background, // Цвет индикатора, когда поле неактивно (прозрачный - отключает индикатор)
                                    containerColor = MaterialTheme.colorScheme.background, // Цвет фона для поле ввода

                                ),


                                label = { // Метка, которая отображается над полем ввода
                                    if (name == "") {
                                        Text(
                                            text = "Названия метки",
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        )
                                    }
                                },

                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done, // Действие на кнопке "Готово" на клавиатуре (закрытие клавиатуры)

                                    keyboardType = KeyboardType.Text // Тип клавиатуры (обычный текст)
                                ),

                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide() // Обработчик действия при нажатии на кнопку "Готово" на клавиатуре (скрыть клавиатуру)

                                    }
                                ),
                                modifier = Modifier
                                                                        .fillMaxWidth() // Занимает все доступное пространство по ширине и высоте
                                    .height(70.dp)
                                    .padding(start = 0.dp, end = 10.dp)
                                    .clip(RoundedCornerShape(30.dp)) // Закругление углов поля
                                    .background(Color.LightGray) // Цвет фона поля
                                    .focusRequester(focusRequester = focusRequester) // Позволяет управлять фокусом поля ввода
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            TextField(
                                value = whathapends,
                                onValueChange = { whathapends = it },
                                textStyle = TextStyle(fontSize = 24.sp),

                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = Color.Transparent, // Цвет индикатора при фокусе на поле (прозрачный - отключает индикатор)
                                    unfocusedIndicatorColor = Color.Transparent, // Цвет индикатора при потере фокуса на поле (прозрачный - отключает индикатор)
                                    disabledIndicatorColor = MaterialTheme.colorScheme.background, // Цвет индикатора, когда поле неактивно (прозрачный - отключает индикатор)
                                    containerColor = MaterialTheme.colorScheme.background, // Цвет фона для поле ввода

                                ),


                                label = { // Метка, которая отображается над полем ввода
                                    if (whathapends == "") {
                                        Text(
                                            text = "Что здесь будет ?",
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        )
                                    }
                                },

                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done, // Действие на кнопке "Готово" на клавиатуре (закрытие клавиатуры)

                                    keyboardType = KeyboardType.Text // Тип клавиатуры (обычный текст)
                                ),

                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide() // Обработчик действия при нажатии на кнопку "Готово" на клавиатуре (скрыть клавиатуру)

                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth() // Занимает все доступное пространство по ширине и высоте
                                    .height(70.dp)
                                    .padding(start = 10.dp, end = 10.dp)
                                    .clip(RoundedCornerShape(30.dp)) // Закругление углов поля
                                    .background(Color.LightGray) // Цвет фона поля
                                    .focusRequester(focusRequester = focusRequester) // Позволяет управлять фокусом поля ввода
                            )
                        }
                        item {

                                Spacer(modifier = Modifier.height(10.dp))

                                CalendarDialog(
                                    state = calendarState,
                                    config = CalendarConfig(
                                        monthSelection = true,
                                        yearSelection = true
                                    ),
                                    selection = CalendarSelection.Date { date ->
                                        if (isSelectingStartDate.value) {
                                            startDateState.value = date
                                            isSelectingStartDate.value = false // Переключаемся на выбор конечной даты
                                        } else {
                                            endDateState.value = date
                                            isSelectingStartDate.value = true // Переключаемся обратно на выбор начальной даты
                                        }
                                    }
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 10.dp, end = 10.dp)
                                        .height(70.dp)
                                ) {
                                    ButtonWithDate(

                                        date = startDateState.value,
                                        onClick = {
                                            calendarState.show()
                                            isSelectingStartDate.value = true
                                        },
                                        label = stringResource(id = R.string.fromchosedata),
                                        modifier = Modifier.weight(1f), // Equal weight for this button

                                    )

                                    // Optional: Spacer for visual separation
                                    Spacer(modifier = Modifier.width(8.dp))

                                    ButtonWithDate(
                                        date = endDateState.value,
                                        onClick = {
                                            calendarState.show()
                                            isSelectingStartDate.value = false
                                        },
                                        label = stringResource(id = R.string.tochosedata),
                                        modifier = Modifier.weight(1f) // Equal weight for this button
                                    )
                                }
                            }
                        item {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    val clockState = rememberSheetState()

                                                    ClockDialog(
                                                        state = clockState,
                                                        selection = ClockSelection.HoursMinutes { hours, minutes ->
                                                            // Сохраните выбранное время в глобальной переменной
                                                            selectedTime = Pair(hours, minutes)
                                                        }
                                                    )
                                                    Log.d("TAG", "pikdata: $selectedTime")

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(70.dp)
                                                            .padding(start = 10.dp, end = 10.dp),
                                                    ) {
                                                        Button(
                                                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                                                            modifier = Modifier
                                                                .fillMaxSize(),
                                                            shape = RoundedCornerShape(30.dp),
                                                            onClick = {
                                                                clockState.show()
                                                            }
                                                        ) {
                                                            Text(
                                                                text = selectedTime?.let { (hours, minutes) ->
                                                                    stringResource(id = R.string.time).format(hours, minutes)
                                                                } ?: stringResource(id = R.string.chosetime),
                                                                fontSize = 20.sp,
                                                                color = MaterialTheme.colorScheme.onBackground,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                        item { 
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 10.dp, end = 10.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(id = R.string.place) + " $currentValue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(10.dp))
                                Slider(
                                    value = currentValue.toFloat(),
                                    onValueChange = { currentValue = it.toInt() },
                                    valueRange = 1f..currentRange.toFloat(),
                                    steps = currentRange - 1,
                                    modifier = Modifier.height(0.dp), // Скрываем точки-деления
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.onPrimaryContainer, // Цвет ползунка
                                        activeTrackColor = MaterialTheme.colorScheme.primary, // Цвет активной части дорожки
                                        inactiveTrackColor = Color.White.copy(1f) // Цвет неактивной части дорожки
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    modifier = Modifier,
                                    colors =   ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                                    onClick = {
                                    currentRange = when (currentRange) {
                                        5 -> 10
                                        10 -> 25
                                        25 -> 50
                                        50 -> 100
                                        else -> 5 // Возвращаемся к начальному диапазону
                                    }
                                    // Сбрасываем значение при смене диапазона
                                }) {
                                    Text(text = stringResource(id = R.string.places), fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }}
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(start = 10.dp, end = 10.dp)
                            ) {
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth(0.45f)
                                        .clip(RoundedCornerShape(30.dp))
                                    ,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    ),
                                    onClick = {
                                        access = "public"
                                    })
                                {
                                    Text(text = stringResource(R.string.publick), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth(1f)
                                        .clip(RoundedCornerShape(30.dp))
                                    ,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    ),
                                    onClick = {
                                        access = "private"
                                    })
                                {
                                    Text(text = stringResource(R.string.pravet), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp)
                        .height(50.dp)
                    )
                    {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(0.45f)
                                .fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            onClick = {
                                newMarkerPosition?.let { latLng ->
                                    val newMarker = MarkerData(
                                        position = latLng,
                                        name = name,
                                        whatHappens = whathapends,
                                        startDate = startDateState.value,
                                        endDate = endDateState.value,
                                        selectedTime = selectedTime,
                                        participants = currentValue,
                                        access = access
                                    )
                                    markers = markers + newMarker
                                    googleMapState?.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .title(name)
                                            .icon(bitmapDescriptorFromVector(
                                                this@Main_menu, // Контекст (возможно, вам понадобится другой)
                                                R.drawable.location_on_, // Ресурс маркера
                                                "FF005B", // Цвет маркера в шестнадцатеричном формате
                                                140, // Ширина маркера
                                                140  // Высота маркера
                                            ))
                                    )
                                    googleMapState?.animateCamera(CameraUpdateFactory.newLatLng(latLng)) // Обновление камеры
                                }
                                showDialog = false
                                // Reset fields after adding the marker
                                name = ""
                                whathapends = ""
                                startDateState.value = null
                                endDateState.value = null
                                selectedTime = null
                                currentValue = 1
                                access = ""
                            }) {
                            Text(
                                text = "Создать",
                                fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background
                            ),
                            onClick = {
                                name = ""
                                whathapends = ""

                                showDialog = false
                            }
                        ) {
                            Text(
                                text = "Не создать",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        )
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

    // Вспомогательная функция для создания кнопки с датой
    @Composable
    fun ButtonWithDate(date: LocalDate?, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
        Button(
            colors = ButtonDefaults.buttonColors(  MaterialTheme.colorScheme.background), // Цвет фона для поле ввода),
            modifier = modifier
                .fillMaxHeight(),
            shape = RoundedCornerShape(30.dp),
            onClick = onClick
        ) {
            Text(
                text = date?.toString() ?: label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}