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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MotorScreen(navController: NavController,
                viewModel: ViewModel,
                userEmail: String
) {
    val context = LocalContext.current

    var totalCarbonEmission by remember { mutableStateOf(0.0) }
    var currentSpeed by remember { mutableStateOf(0f) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isCalculating by remember { mutableStateOf(false) }
    var totalDistance by remember { mutableStateOf(0.0) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    // 獎勵相關狀態
    var showRewardDialog by remember { mutableStateOf(false) }
    var showAlreadyRewardedDialog by remember { mutableStateOf(false) }
    var canGetReward by remember { mutableStateOf(true) }

    // 載入上次使用日期
    LaunchedEffect(Unit) {
        if (userEmail.isNotEmpty()) {
            viewModel.loadCarbonCalculatorDateFromFirebase(userEmail)
        }
    }

    // 檢查今天是否可以獲得獎勵
    LaunchedEffect(viewModel.lastCarbonCalculatorDate) {
        canGetReward = viewModel.canGetCarbonCalculatorReward()
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


    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)


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
                    Text("獲得 1 分")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "本次碳排放: ${String.format("%.2f", totalCarbonEmission)} kg CO₂",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "明天再來記錄吧！",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showRewardDialog = false }) {
                    Text("太好了！")
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
                        "明天再來繼續記錄碳排放吧！",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAlreadyRewardedDialog = false }) {
                    Text("知道了")
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
                            "💚 今日尚未記錄 (可獲得 5 分)",
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

                            // 嘗試獲得獎勵
                            if (userEmail.isNotEmpty()) {
                                val rewarded = viewModel.rewardCarbonCalculator(userEmail)
                                if (rewarded) {
                                    showRewardDialog = true
                                    canGetReward = false
                                } else {
                                    showAlreadyRewardedDialog = true
                                }
                            }
                        } else {
                            isCalculating = true
                            totalDistance = 0.0
                            totalCarbonEmission = 0.0
                            lastLocation = null
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA673))
                ) {
                    Text(if (isCalculating) "停止計算" else "開始計算")
                }
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

