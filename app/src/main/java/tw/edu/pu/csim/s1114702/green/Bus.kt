package tw.edu.pu.csim.s1114702.green

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import tw.edu.pu.csim.s1114702.green.R

@Composable
fun BusScreen(navController: NavController, context: Context) {

    var totalCarbonEmission by remember { mutableStateOf(0.0) }
    var currentSpeed by remember { mutableStateOf(0f) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isCalculating by remember { mutableStateOf(false) }

    var totalDistance by remember { mutableStateOf(0.0) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                showPermissionDialog = true
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // 修正的地方：繼承 LocationCallback 並覆寫 onLocationResult 方法
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { location ->
                    currentSpeed = location.speed  // 更新速度 (m/s)

                    if (isCalculating) {
                        lastLocation?.let { prevLocation ->
                            val distance = prevLocation.distanceTo(location) / 1000.0 // 轉為公里
                            totalDistance += distance
                        }
                        lastLocation = location
                    }

                    Log.d(
                        "LocationUpdate",
                        "Speed: ${currentSpeed * 3.6}, Distance: $totalDistance km"
                    )
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
        val fuelEfficiency = 0.33  // 每公里油耗 (L/km)
        val carbonPerLiter = 2.68  // 每公升燃油的 CO2 排放量 (kg/L)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA0D6A1)) // 淺綠色背景
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Top,  // 讓內容從上方開始排列
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            // **返回箭頭 + 標題**
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // **返回按鈕靠左**
                Image(
                    painter = painterResource(id = R.drawable.backarrow), // 確保 R.drawable.backarrow 存在
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(40.dp) // 設定返回按鈕大小
                        .align(Alignment.CenterStart) // **對齊 Box 左側**
                        .clickable { navController.popBackStack() } // 點擊返回上一頁
                )

                // **標題置中**
                Text(
                    text = "綠  森  友",
                    fontSize = 28.sp,
                    color = Color(0xFF005500), // 深綠色
                    modifier = Modifier.align(Alignment.Center) // **文字置中**
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // **橫線**
            Box(
                modifier = Modifier
                    .fillMaxWidth() // 橫線貼齊左右
                    .height(4.dp) // 設定線條厚度
                    .background(Color(0xFF005500))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("城市公車碳排放計算器", fontSize = 24.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            isCalculating = false
                            calculateCarbonEmission()
                        } else {
                            isCalculating = true
                            totalDistance = 0.0
                            totalCarbonEmission = 0.0
                            lastLocation = null
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(if (isCalculating) "停止計算" else "開始計算")
                }
            }
        }
    }
}
