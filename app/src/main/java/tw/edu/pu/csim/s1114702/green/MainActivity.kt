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
    val backgroundImage = painterResource(id = R.drawable.greenback1
    )
    val treeImage = painterResource(id = R.drawable.logintree)
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {



            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("帳號 (Email)") },
                modifier = Modifier.width(250.dp)
            )


            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密碼(6碼)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.width(250.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // 讓兩個按鈕平均分布
            ) {
                // 登入按鈕（圖片）
                Button(onClick = {
                    loginUser(email, password, context, navController, db, viewModel, onLoginSuccess)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.tree), // 登入圖片
                        contentDescription = "登入",
                        modifier = Modifier.size(60.dp) // 調整圖片大小
                    )
                }

                // 註冊按鈕（圖片）
                Button(onClick = {
                    registerUser(email, password, context, db)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.leaf), // 註冊圖片
                        contentDescription = "註冊",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }




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