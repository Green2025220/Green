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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

@Composable
fun CarScreen(navController: NavController, context: Context) {
    val backgroundImage = painterResource(id = R.drawable.greenback)
    val backButtonImage = painterResource(id = R.drawable.backarrow)

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

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

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

                    Log.d("LocationUpdate", "Speed: ${currentSpeed * 3.6}, Distance: $totalDistance km")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create().apply {
                interval = 1000
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    fun calculateCarbonEmission() {
        val fuelEfficiency = 0.125  // 每公里油耗 (L/km)
        val carbonPerLiter = 2.31  // 每公升燃油的 CO2 排放量 (kg/L)

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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .padding(16.dp)
                .clickable { navController.popBackStack() }
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back",
                modifier = Modifier.size(40.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("中型汽油車碳排放計算器", fontSize = 24.sp, color = Color.Black)

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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA673))
            ) {
                Text("開始計算", color = Color.White)
            }
        }
    }
}
