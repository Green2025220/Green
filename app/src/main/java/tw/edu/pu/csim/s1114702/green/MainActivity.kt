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
import androidx.compose.ui.text.style.TextDecoration


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
    var confirmPassword by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPasswordMismatchDialog by remember { mutableStateOf(false) }
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
                .padding(top = screenHeight * 0.336f)
                .width(screenWidth * 0.80f)
                .height(screenHeight * 0.80f * 187f / 934f)
        ) {
            // 背景圖片
            Image(
                painter = painterResource(id = R.drawable.account),
                contentDescription = null,
                contentScale = ContentScale.Fit,
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
                    .padding(start = 70.dp, end = 20.dp),
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
                .padding(top = screenHeight * 0.422f)
                .width(screenWidth * 0.80f)
                .height(screenHeight * 0.80f * 187f / 934f)
        ) {
            // 背景圖片
            Image(
                painter = painterResource(id = R.drawable.password),
                contentDescription = null,
                contentScale = ContentScale.Fit,
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
                    .padding(start = 70.dp, end = 20.dp),
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

        // 登入按鈕（樹形圖案）
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "登入",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.421f,
                    top = screenHeight * 0.588f
                )
                .width(screenWidth * 0.144f)
                .height(screenHeight * 0.075f)
                .clickable {
                    loginUser(email, password, context, navController, db, viewModel, onLoginSuccess)
                }
        )

        Text(
            text = "註冊",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            style = LocalTextStyle.current.copy(
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = screenWidth * 0.781f,
                    top = screenHeight * 0.540f
                )
                .clickable {
                    // 先驗證基本條件
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "請輸入帳號和密碼", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    // 驗證密碼長度
                    if (password.length != 6) {
                        Toast.makeText(context, "密碼必須是 6 位數", Toast.LENGTH_LONG).show()
                        return@clickable
                    }

                    // 驗證密碼格式（只能英數字）
                    if (!isValidPassword(password)) {
                        Toast.makeText(context, "密碼只能包含英文字母和數字", Toast.LENGTH_LONG).show()
                        return@clickable
                    }

                    // 驗證 email 格式
                    if (!isValidEmail(email)) {
                        Toast.makeText(context, "此郵件無效，請重新輸入", Toast.LENGTH_LONG).show()
                        return@clickable
                    }

                    // 所有驗證通過，顯示確認密碼對話框
                    showConfirmDialog = true
                }
        )

        // 密碼確認對話框
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    confirmPassword = ""
                },
                title = { Text("確認密碼") },
                text = {
                    Column {
                        Text("請再次輸入密碼以確認：")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("確認密碼") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (confirmPassword == password) {
                                // 密碼確認正確，執行註冊
                                registerUser(email, password, context, db)
                                showConfirmDialog = false
                                confirmPassword = ""
                            } else {
                                // 密碼不一致，顯示錯誤對話框
                                showConfirmDialog = false
                                showPasswordMismatchDialog = true
                            }
                        }
                    ) {
                        Text("確認")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            confirmPassword = ""
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        // 密碼不一致對話框
        if (showPasswordMismatchDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPasswordMismatchDialog = false
                    confirmPassword = ""
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("密碼不一致")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "您輸入的密碼與確認密碼不一致\n請選擇下列操作：",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // 重新確認按鈕
                        Button(
                            onClick = {
                                showPasswordMismatchDialog = false
                                confirmPassword = ""
                                showConfirmDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                        ) {
                            Text("重新確認", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 重新註冊按鈕
                        OutlinedButton(
                            onClick = {
                                showPasswordMismatchDialog = false
                                email = ""
                                password = ""
                                confirmPassword = ""
                                Toast.makeText(context, "已清空，請重新填寫註冊資料", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                        ) {
                            Text("重新註冊", fontSize = 16.sp)
                        }
                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }

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


// 密碼格式驗證函數（只允許英文字母和數字）
fun isValidPassword(password: String): Boolean {
    val passwordPattern = "^[a-zA-Z0-9]+$"
    return password.matches(passwordPattern.toRegex())
}


// Email 格式驗證函數
fun isValidEmail(email: String): Boolean {

    // 基本格式檢查
    val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    if (!email.matches(emailPattern.toRegex())) {
        return false
    }

    // 檢查是否為常見的郵件服務提供商
    val validDomains = listOf(
        "gmail.com", "yahoo.com", "yahoo.com.tw", "hotmail.com",
        "outlook.com", "icloud.com", "live.com", "msn.com",
        "mail.com", "aol.com", "protonmail.com", "zoho.com"
    )

    val domain = email.substringAfter("@").lowercase()
    return validDomains.any { domain == it }
}


fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}