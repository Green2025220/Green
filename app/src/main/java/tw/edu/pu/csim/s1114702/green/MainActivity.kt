package tw.edu.pu.csim.s1114702.green


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import tw.edu.pu.csim.s1114702.green.ui.theme.GreenTheme
import java.security.MessageDigest
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.LocalTextStyle


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Firebase 初始化
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }


        setContent {
            GreenTheme {
                AppNavigation()
            }
        }
    }
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: ViewModel = viewModel()
    var userEmail by remember { mutableStateOf("") }


    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController, viewModel) { email ->
                userEmail = email
            }
        }
        composable("scone") { SconeScreen(navController) }
        composable("calculator") { CalculatorScreen(navController) }
        composable("car") { CarScreen(navController) }
        composable("motor") { MotorScreen(navController) }
        composable("bus") { BusScreen(navController) }
        composable("game") { GameScreen(navController) }
        composable("myforest") { MyforestScreen(navController, viewModel, userEmail) }
        composable("everyday") { EverydayScreen(navController, viewModel) }
        composable("store") { StoreScreen(navController, viewModel, userEmail) }
        composable("Game1") { QuizGameScreen(navController, viewModel = viewModel) }
        composable("turn") {
            TurnScreen(
                navController = navController,
                viewModel = viewModel,
                userEmail = userEmail
            )
        }
        composable("Garbagegame") {
            GarbageGameScreen(
                navController = navController,
                viewModel = viewModel,
                userEmail = userEmail
            )
        }
        composable("garbage") { GarbageScreen(navController) }
    }
}


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: ViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val backgroundImage = painterResource(id = R.drawable.greenback1)
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // 帳號輸入框（使用自訂圖片背景）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.386f)
                .width(screenWidth * 0.657f)
                .height(screenHeight * 0.08f)
        ) {
            // 背景圖片
            Image(
                painter = painterResource(id = R.drawable.account),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            // 文字輸入
            BasicTextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(start = 55.dp, end = 20.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (email.isEmpty()) {
                            Text(
                                text = "(Email)",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // 密碼輸入框（使用自訂圖片背景）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.472f)
                .width(screenWidth * 0.657f)
                .height(screenHeight * 0.08f)
        ) {
            // 背景圖片
            Image(
                painter = painterResource(id = R.drawable.password),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            // 密碼輸入（使用 PasswordVisualTransformation）
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(start = 55.dp, end = 20.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (password.isEmpty()) {
                            Text(
                                text = "(6碼)",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // 註冊按鈕（葉子圖案）
        Image(
            painter = painterResource(id = R.drawable.sign),
            contentDescription = "註冊",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.157f,
                    top = screenHeight * 0.595f
                )
                .width(screenWidth * 0.217f)
                .height(screenHeight * 0.065f)
                .clickable {
                    registerUser(email, password, context, db)
                }
        )

        // 登入按鈕（樹形圖案）
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "登入",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.421f,
                    top = screenHeight * 0.603f
                )
                .width(screenWidth * 0.144f)
                .height(screenHeight * 0.075f)
                .clickable {
                    loginUser(email, password, context, navController, db, viewModel, onLoginSuccess)
                }
        )

        // 錯誤訊息
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }
}


fun loginUser(
    email: String,
    password: String,
    context: android.content.Context,
    navController: NavController,
    db: FirebaseFirestore,
    viewModel: ViewModel,
    onLoginSuccess: (String) -> Unit
) {
    if (email.isBlank() || password.isBlank()) {
        Toast.makeText(context, "請輸入帳號和密碼", Toast.LENGTH_SHORT).show()
        return
    }


    db.collection("users").document(email).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val storedPasswordHash = document.getString("password")
                val inputPasswordHash = hashPassword(password)
                if (storedPasswordHash == inputPasswordHash) {
                    Toast.makeText(context, "登入成功！", Toast.LENGTH_SHORT).show()
                    viewModel.loadDailyChallengeFromFirebase(email) {
                        onLoginSuccess(email)
                        navController.navigate("scone")
                    }
                } else {
                    Toast.makeText(context, "密碼錯誤", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "帳號不存在，請註冊", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "登入失敗: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}


fun registerUser(email: String, password: String, context: android.content.Context, db: FirebaseFirestore) {
    if (email.isBlank() || password.isBlank()) {
        Toast.makeText(context, "請輸入帳號和密碼", Toast.LENGTH_SHORT).show()
        return
    }


    val hashedPassword = hashPassword(password)
    val user = hashMapOf(
        "email" to email,
        "password" to hashedPassword,
        "score" to 0
    )


    db.collection("users").document(email).set(user)
        .addOnSuccessListener {
            Toast.makeText(context, "註冊成功，請登入！", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "註冊失敗: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}


fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}