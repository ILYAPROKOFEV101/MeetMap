package com.ilya.MeetingMap


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email

import androidx.compose.material.icons.outlined.Email

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.ilya.codewithfriends.presentation.profile.ProfileScreen
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import com.ilya.codewithfriends.presentation.sign_in.SignInState
import com.ilya.codewithfriends.presentation.sign_in.SignInViewModel
import com.ilya.codewithfriends.presentation.sign_in.UserData

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.ComposeGoogleSignInCleanArchitectureTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ilya.MeetingMap.Mine_menu.Map_Activity
import com.ilya.reaction.logik.PreferenceHelper.removeUserKey

// С 20.11.2024...  я перёл свё проэкт на kmp
class MainActivity : ComponentActivity() {

    class BottomNavigationItem(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
        val hasNews: Boolean,
        val badgeCount: Int? = null
    ) {
    }


    private lateinit var auth: FirebaseAuth

    var leftop by mutableStateOf(true)


    var username by mutableStateOf("")
    var password by mutableStateOf("")


    var cloth by mutableStateOf(true)

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            toshear(userData = googleAuthUiClient.getSignedInUser())

            ComposeGoogleSignInCleanArchitectureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Регистрация прошла успешно",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("profile")
                                    viewModel.resetState()
                                    leftop = !leftop
                                }
                            }

                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                },
                                navController
                            )
                        }


                        composable("profile") {
                            Column(modifier = Modifier.fillMaxSize()) {
                                ProfileScreen(
                                    userData = googleAuthUiClient.getSignedInUser(),
                                    onSignOut = {
                                        lifecycleScope.launch {
                                            googleAuthUiClient.signOut()
                                            removeUserKey(this@MainActivity)
                                            Toast.makeText(
                                                applicationContext,
                                                "Goodbye",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            navController.popBackStack()
                                        }
                                    }
                                )
                                backtomenu()
                            }
                        }
                        composable("login")
                        {
                            LoginUsermenu()
                        }

                    }
                }
            }
        }
    }


    @Composable
    fun SignInScreen(
        state: SignInState,
        onSignInClick: () -> Unit,
        navController: NavController

    ) {

        var unvisible by remember {
            mutableStateOf(false)
        }


        var user by remember { mutableStateOf(Firebase.auth.currentUser) }

        val launcher = rememberFirebaseAuthLauncher(
            onAuthComplete = { result ->
                user = result.user

                // navController.navigate("profile")
            },
            onAuthError = {
                user = null
            }
        )
        val token = stringResource(id = R.string.web_client_id)
        val context = LocalContext.current

        val scope = rememberCoroutineScope()
        val serverSetting = remember { mutableStateOf(false) }




        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (!leftop) {

                toshear(userData = googleAuthUiClient.getSignedInUser())
                Box(
                    modifier = Modifier
                        .height(400.dp)
                        .align(Alignment.Center)
                        .padding(top = 100.dp)
                ) {
                    LoadingCircle()

                }


            }

            if (user == null) {

                if (!unvisible) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        IconButton(
                            onClick = {
                                Log.d("GoogleSignIn", "Attempting to sign in")

                                val gso =
                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(token)
                                        .requestEmail()
                                        .build()

                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                launcher.launch(googleSignInClient.signInIntent)

                                leftop = !leftop
                                unvisible = !unvisible


                                Log.d("GoogleSignIn", "Sign in intent launched")
                            }
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = "Nothing",

                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(stringResource(id = R.string.login), fontSize = 20.sp, fontFamily = robotoBold)

                        Spacer(modifier = Modifier.height(10.dp))


                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.White)
                        )
                        {
                            ButtonAppBar(navController)
                        }

                    }
                }
            }
        }

    }

    @Composable
    fun toshear(userData: UserData?) {
        if (userData?.username != null) {
            val intent = Intent(this@MainActivity, Map_Activity::class.java)
            startActivity(intent)
        }
    }

    @Composable
    fun backtomenu() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.BottomEnd // Размещаем Box внизу

        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 70.dp)
                    .height(50.dp)
                    .align(Alignment.Center),
                colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50)),
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    val intent = Intent(this@MainActivity, Map_Activity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
            {
                Text(stringResource(id = R.string.back))
            }
        }
    }

    val robotoMedium = FontFamily(
        Font(R.font.roboto_medium) // Убедитесь, что имя файла правильное и соответствует переименованному файлу
    )
    val robotoBold = FontFamily(
        Font(R.font.roboto_bold) // Убедитесь, что имя файла правильное и соответствует переименованному файлу
    )


    @Preview(showBackground = true)
    @Composable
    fun LoginUsermenu() {
        auth = FirebaseAuth.getInstance()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center // Выравниваем по центру
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .padding(start = 20.dp, end = 20.dp),
            ) {
                Text(
                    text = "Sign in",
                    fontSize = 35.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 10.dp),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = robotoMedium
                )
                Spacer(modifier = Modifier.height(60.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .height(600.dp),
                    colors = CardDefaults.cardColors(Color(0xFFCFCDCD)), // RGB 153, 153, 153 в шестнадцатеричном формате
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(100.dp)
                                ) {
                                        Use_name()
                                   }

                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(100.dp)
                                ) {
                                    Password()
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .height(60.dp)
                                ) {
                                    Login(auth)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun Use_name() {

        val keyboardControllers = LocalSoftwareKeyboardController.current
        var showtext by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFCFCDCD))
                    .padding(start = 30.dp, end = 30.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                // Текст, который будет над полем ввода
                Text(
                    text = stringResource(id = R.string.email),
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth(),

                    fontFamily = robotoBold
                )
                // Поле ввода
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            border = BorderStroke(3.dp, SolidColor(Color.Black)),
                            shape = RoundedCornerShape(20.dp)

                        ),
                    value = username, // Текущее значение текста в поле
                    onValueChange = {
                        username = it
                    }, // Обработчик изменения текста, обновляющий переменную "username"
                    textStyle = TextStyle(fontSize = 20.sp),


                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color(0xFFCFCDCD), // Цвет индикатора при фокусе на поле (прозрачный - отключает индикатор)
                        unfocusedIndicatorColor = Color(0xFFCFCDCD), // Цвет индикатора при потере фокуса на поле (прозрачный - отключает индикатор)
                        disabledIndicatorColor = Color(0xFFCFCDCD), // Цвет индикатора, когда поле неактивно (прозрачный - отключает индикатор)
                        containerColor = Color(0xFFCFCDCD)
                    ),

                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done, // Действие на кнопке "Готово" на клавиатуре (закрытие клавиатуры)
                        keyboardType = KeyboardType.Text // Тип клавиатуры (обычный текст)
                    ),

                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardControllers?.hide() // Обработчик действия при нажатии на кнопку "Готово" на клавиатуре (скрыть клавиатуру)
                            showtext = !showtext
                        }
                    ),
                )
            }
        }




    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun Password() {

        val keyboardControllers = LocalSoftwareKeyboardController.current
        var showtext by remember {
            mutableStateOf(false) }

        var passwordError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 30.dp, end = 30.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            // Текст, который будет над полем ввода
            Text(
                text = stringResource(id = R.string.password),
                fontSize = 20.sp,
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth(),

                fontFamily = robotoBold
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        border = BorderStroke(3.dp, SolidColor(Color.Black)),
                        shape = RoundedCornerShape(20.dp)
                    ),
                value = password, // Текущее значение текста в поле
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                }, // Обработчик изменения текста, обновляющий переменную "password" и проверяющий длину
                textStyle = TextStyle(fontSize = 24.sp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color(0xFFCFCDCD),
                    unfocusedIndicatorColor = Color(0xFFCFCDCD),
                    disabledIndicatorColor = Color(0xFFCFCDCD),
                    containerColor = Color(0xFFCFCDCD)
                ),

                isError = passwordError, // Показываем ошибку, если пароль слишком короткий
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardControllers?.hide() // Обработчик действия при нажатии на кнопку "Готово" на клавиатуре (скрыть клавиатуру)
                        if (password.isNotEmpty()) {
                            showtext = !showtext
                        }
                    }
                ),
            )
        }

        if (passwordError) {
            Toast.makeText(
                LocalContext.current,
                "Пароль должен содержать минимум 6 символов",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Composable
    fun Login(auth: FirebaseAuth) {
        Button(
            onClick = {
                if(username.isNotBlank() && password.isNotBlank()) {
                    registerUser(this,auth, username, password) { success ->
                        if (success) {
                            val intent = Intent(this@MainActivity,  Map_Activity::class.java)
                            startActivity(intent)
                        } else {
                            // Регистрация не удалась
                            // Обработка ошибки
                        }
                    }

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(Color(0xB93998FF)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = stringResource(id = R.string.singin), fontSize = 30.sp)
        }
    }
}

val robotoBold = FontFamily(
    Font(R.font.roboto_bold) // Убедитесь, что имя файла правильное и соответствует переименованному файлу
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonAppBar(navController: NavController) {


        var selectedItemIndex by rememberSaveable {
            mutableStateOf(0)
        }

            NavigationBar(
                modifier = Modifier.wrapContentSize(),
                containerColor = Color.White
            ) {
                val items = listOf(
                    MainActivity.BottomNavigationItem(
                        title = stringResource(id = R.string.logwithemail),
                        selectedIcon = Icons.Filled.Email,
                        unselectedIcon = Icons.Outlined.Email,
                        hasNews = false,
                    )
                )

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    selectedItemIndex = when (destination.route) {
                        "admin_fragment" -> 0
                        else -> selectedItemIndex
                    }
                }

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            when (index) {
                                0 -> navController.navigate("login")
                            }
                        },
                        label = {
                            Text(text = item.title, fontSize = 20.sp, fontFamily = robotoBold)
                        },
                        alwaysShowLabel = false,
                        icon = {
                            Icon(
                                imageVector = if (index == selectedItemIndex) {
                                    item.selectedIcon
                                } else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black, // цвет выбранного значка
                            unselectedIconColor = Color.Gray, // цвет невыбранного значка
                            selectedTextColor = Color.Black, // цвет текста
                            unselectedTextColor = Color.Gray, // цвет невыбранного текста
                            indicatorColor = Color.White // цвет фона кнопки
                        )
                    )
                }
            }
        }





@Preview(showBackground = true)
@Composable
fun LoadingCircle() {
    Box(
        modifier = Modifier
            .height(100.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        // Анимация вращения
        val rotation = rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        // Анимация для угла дуги (создаем эффект, что круг почти замкнут)
        val sweepAngle = rememberInfiniteTransition().animateFloat(
            initialValue = 1f,  // Начальный минимальный угол
            targetValue = 359f,  // Максимальный угол
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        // Определяем кастомные цвета для градиента
        val rainbowBrush = Brush.sweepGradient(
            colors = listOf(
                Color.Red,
                Color(0xFFFFA500),  // Оранжевый
                Color.Yellow,
                Color(0xA14557FF),
                Color(0xDA6EB7E7),  // Индиго
                Color(0xFF8A2BE2)   // Фиолетовый
            )
        )

        // Canvas для рисования кастомного кольца с градиентом
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .rotate(rotation.value)  // Вращаем кольцо
        ) {
            // Рисуем дугу с градиентом, которая не будет полной
            drawArc(
                brush = rainbowBrush,
                startAngle = 0f,
                sweepAngle = sweepAngle.value,  // Анимируемый угол дуги
                useCenter = false,  // Чтобы не заполнять центр
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)  // Толщина и форма концов линий
            )
        }
    }
}




@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleAuth", "account $account")
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()

                onAuthComplete(authResult)

            }
        } catch (e: ApiException) {
            Log.d("GoogleAuth", e.toString())
            onAuthError(e)
        }
    }
}


private fun registerUser(
    context: Context,
    auth: FirebaseAuth,
    email: String,
    password: String,
    onResult: (Boolean) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Registration successful
                val user = auth.currentUser
                Log.d("Registration", "User registered successfully")
                onResult(true) // Пользователь успешно зарегистрирован
            } else {
                // Registration failed
                val exception = task.exception
                if (exception is FirebaseAuthUserCollisionException) {
                    // Пользователь уже существует, попытаемся войти
                    signInUser(context, auth, email, password, onResult)
                } else {
                    // Другая ошибка, обработаем ее
                    val message = exception?.message ?: "Unknown error"
                    Log.d("Registration", "Registration failed: $message")
                    showToast(context, "Registration failed: $message")
                    onResult(false) // Регистрация не удалась
                }
            }
        }
}

private fun signInUser(
    context: Context,
    auth: FirebaseAuth,
    email: String,
    password: String,
    onResult: (Boolean) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Вход успешный
                Log.d("Registration", "User signed in successfully")
                onResult(true) // Пользователь успешно вошел
            } else {
                // Вход не удался
                val exception = task.exception
                val message = exception?.message ?: "Unknown error"
                Log.d("Registration", "Sign in failed: $message")
                showToast(context, "Sign in failed: $message")
                onResult(false) // Вход не удался
            }
        }
}

private fun showToast(context: Context, message: String)
{
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

