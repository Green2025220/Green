package tw.edu.pu.csim.s1114702.green


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MotorScreen(navController: NavController,
                viewModel: ViewModel,
                userEmail: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val carbonAI = remember {
        CarbonAIAdvisor(BuildConfig.GEMINI_API_KEY)
    }

    var totalCarbonEmission by remember { mutableStateOf(0.0) }
    var currentSpeed by remember { mutableStateOf(0f) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isCalculating by remember { mutableStateOf(false) }
    var totalDistance by remember { mutableStateOf(0.0) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    // 獎勵相關狀態
    var showRewardDialog by remember { mutableStateOf(false) }
    var showAlreadyRewardedDialog by remember { mutableStateOf(false) }
    var showInsufficientDistanceDialog by remember { mutableStateOf(false) }
    var canGetReward by remember { mutableStateOf(true) }

    // AI 相關狀態
    var isAIAnalyzing by remember { mutableStateOf(false) }
    var aiAnalysis by remember { mutableStateOf<CarbonAIAnalysis?>(null) }
    var showAIDialog by remember { mutableStateOf(false) }
    var hasCalculated by remember { mutableStateOf(false) }

    // 載入上次使用日期
    LaunchedEffect(Unit) {
        if (userEmail.isNotEmpty()) {
            viewModel.loadCarbonCalculatorDateFromFirebase(userEmail)
        }
    }

    // 檢查今天是否可以獲得獎勵
    LaunchedEffect(viewModel.lastMotorCalculatorDate) {
        canGetReward = viewModel.canGetMotorCalculatorReward()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) showPermissionDialog = true
        }
    )


    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }


    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    currentSpeed = location.speed
                    if (isCalculating) {
                        lastLocation?.let { prevLocation ->
                            val distance = prevLocation.distanceTo(location) / 1000.0
                            totalDistance += distance
                        }
                        lastLocation = location
                    }
                    Log.d("LocationUpdate", "Speed: ${currentSpeed * 3.6}, Distance: $totalDistance km")
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create().apply {
                interval = 1000
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }


    fun calculateCarbonEmission() {
        val fuelEfficiency = 0.033  // 每公里油耗 (L/km)
        val carbonPerLiter = 2.31   // 每公升燃油的 CO2 排放量 (kg/L)
        totalCarbonEmission = totalDistance * fuelEfficiency * carbonPerLiter
    }

    // AI 分析函數
    fun analyzeWithAI() {
        scope.launch {
            isAIAnalyzing = true
            try {
                Log.d("CarScreen", "開始 AI 分析...")
                Log.d("CarScreen", "碳排放: $totalCarbonEmission kg, 距離: $totalDistance km")

                val analysis = carbonAI.analyzeCarbonImpact(
                    carbonAmount = totalCarbonEmission,
                    transportType = "機車",
                    distance = totalDistance
                )

                aiAnalysis = analysis
                showAIDialog = true

                Log.d("CarScreen", "AI 分析完成: ${analysis.environmentalImpact}")
            } catch (e: Exception) {
                Log.e("CarScreen", "AI 分析失敗", e)
                // 可以在這裡顯示錯誤訊息
            } finally {
                isAIAnalyzing = false
            }
        }
    }

    // 顏色根據嚴重程度
    val severityColor = when (aiAnalysis?.severity) {
        "低" -> Color(0xFF4CAF50)
        "中" -> Color(0xFFFF9800)
        "高" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("權限不足") },
            text = { Text("請開啟位置權限以使用此功能") },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("確定")
                }
            }
        )
    }

    // 獲得獎勵對話框
    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = { Text("🎉 獲得獎勵") },
            text = {
                Column {
                    Text("完成碳排放記錄！")
                    Text("獲得 3 分")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "本次碳排放: ${String.format("%.2f", totalCarbonEmission)} kg CO₂",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "行駛距離: ${String.format("%.2f", totalDistance)} 公里",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showRewardDialog = false
                    analyzeWithAI()
                }) {
                    Text("查看 AI 分析")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRewardDialog = false }) {
                    Text("稍後")
                }
            }
        )
    }

    // 今日已獲得獎勵對話框
    if (showAlreadyRewardedDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyRewardedDialog = false },
            title = { Text("今日已記錄") },
            text = {
                Column {
                    Text("您今天已經獲得過分數了")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "本次碳排放: ${String.format("%.2f", totalCarbonEmission)} kg CO₂",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "行駛距離: ${String.format("%.2f", totalDistance)} 公里",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAlreadyRewardedDialog = false
                    analyzeWithAI()
                }) {
                    Text("查看 AI 分析")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlreadyRewardedDialog = false }) {
                    Text("稍後")
                }
            }
        )
    }

    // 距離不足對話框
    if (showInsufficientDistanceDialog) {
        AlertDialog(
            onDismissRequest = { showInsufficientDistanceDialog = false },
            title = { Text("記錄完成") },
            text = {
                Column {
                    Text("至少需要行駛 0.5 公里才能獲得分數")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "本次行駛: ${String.format("%.2f", totalDistance)} 公里",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "本次碳排放: ${String.format("%.2f", totalCarbonEmission)} kg CO₂",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInsufficientDistanceDialog = false }) {
                    Text("知道了")
                }
            }
        )
    }

    // AI 分析結果對話框
    if (showAIDialog && aiAnalysis != null) {
        AlertDialog(
            onDismissRequest = { showAIDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🤖 AI 環保顧問", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = severityColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            aiAnalysis!!.severity,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 基本資訊
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "📊 本次數據",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2CA673)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "碳排放: ${String.format("%.2f", totalCarbonEmission)} kg CO₂",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                "行駛距離: ${String.format("%.2f", totalDistance)} 公里",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "🌍 對環境的影響",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        aiAnalysis!!.environmentalImpact,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "💚 減碳建議",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    aiAnalysis!!.actionSuggestions.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "${index + 1}. ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2CA673)
                            )
                            Text(
                                suggestion,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "📊 有趣的對比",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                aiAnalysis!!.funFact,
                                fontSize = 13.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAIDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2CA673)
                    )
                ) {
                    Text("我知道了")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.road2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 返回箭頭 + 標題
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backarrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() }
                )
                Text(
                    text = "綠 森 友",
                    fontSize = 28.sp,
                    color = Color(0xFF005500),
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            Spacer(modifier = Modifier.height(8.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("中型機車碳排放計算器", fontSize = 24.sp, color = Color.Black)


                Spacer(modifier = Modifier.height(8.dp))

                // 今日狀態提示
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (canGetReward) {
                        Text(
                            "💚 今日尚未記錄 (可獲得 3 分)",
                            color = Color(0xFF2CA673),
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            "✓ 今日已記錄",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "當前速度: ${currentSpeed.times(3.6).toInt()} 公里/小時",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "行駛距離: ${String.format("%.2f", totalDistance)} 公里",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "總碳排放量: ${String.format("%.2f", totalCarbonEmission)} 公斤 CO₂",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    onClick = {
                        if (isCalculating) {
                            // 停止計算
                            isCalculating = false
                            calculateCarbonEmission()
                            hasCalculated = true

                            Log.d("MotorScreen", "=== 停止計算 ===")
                            Log.d("MotorScreen", "userEmail: '$userEmail'")
                            Log.d("MotorScreen", "行駛距離: $totalDistance km")
                            Log.d("MotorScreen", "碳排放量: $totalCarbonEmission kg CO₂")

                            // 檢查是否達到最低距離要求（0.5公里）
                            if (totalDistance < 0.5) {
                                showInsufficientDistanceDialog = true
                                Log.d("MotorScreen", "❌ 距離不足 0.5 公里，無法獲得獎勵")
                            } else if (userEmail.isNotEmpty()) {
                                Log.d("MotorScreen", "進入獎勵判斷區塊")
                                val rewarded = viewModel.rewardMotorCalculator(userEmail)

                                if (rewarded) {
                                    Log.d("MotorScreen", "顯示獎勵對話框")
                                    showRewardDialog = true
                                    canGetReward = false
                                } else {
                                    Log.d("MotorScreen", "顯示已獎勵對話框")
                                    showAlreadyRewardedDialog = true
                                }
                            } else {
                                Log.d("MotorScreen", "❌ userEmail 為空,無法獲得獎勵")
                            }
                        } else {
                            Log.d("MotorScreen", "開始計算")
                            isCalculating = true
                            hasCalculated = false
                            totalDistance = 0.0
                            totalCarbonEmission = 0.0
                            lastLocation = null
                            aiAnalysis = null
                        }
                    },
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(56.dp)
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA673)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isCalculating) "停止計算" else "開始計算",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // AI 分析按鈕（停止計算後才顯示）
                if (hasCalculated && totalCarbonEmission > 0) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { analyzeWithAI() },
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(56.dp)
                            .padding(horizontal = 32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAIAnalyzing
                    ) {
                        if (isAIAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI 分析中...", fontSize = 18.sp)
                        } else {
                            Text("🤖 AI 幫幫忙", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // AI 分析預覽卡片
                    aiAnalysis?.let { analysis ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "💡 AI 分析",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2CA673)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = severityColor,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            analysis.severity,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    analysis.environmentalImpact,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { showAIDialog = true },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("查看完整建議 →", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

            }


            Image(
                painter = painterResource(id = R.drawable.scooter2),
                contentDescription = "Scooter",
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)
                    .offset(y = 150.dp)
            )
        }
    }
}