package tw.edu.pu.csim.s1114702.green

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*

@Composable
fun RecycleScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var permissionsGranted by remember { mutableStateOf(false) }

    // 檢查並請求位置權限
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            (context as Activity).requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        } else {
            permissionsGranted = true
        }
    }

    // 如果權限已經授予，獲取位置
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                } ?: run {
                    // 無法獲取位置
                    Log.e("RecycleScreen", "Unable to get location")
                }
            }
        }
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
        ) {
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
                    text = "回收地點",
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

            Spacer(modifier = Modifier.height(32.dp)) // 與按鈕列表的間距

            // **顯示地圖，直到拿到位置**
            if (userLocation != null) {
                // 使用 MapView 顯示地圖
                AndroidView(factory = { context ->
                    MapView(context).apply {
                        onCreate(Bundle())  // 確保 MapView 的 onCreate 被調用
                        onResume()          // 確保 onResume 被調用

                        getMapAsync { googleMap ->
                            // 確保用戶位置存在後再更新地圖
                            userLocation?.let { loc ->
                                val markerOptions = MarkerOptions().position(loc).title("您的位置")
                                googleMap.addMarker(markerOptions)
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        loc,
                                        15f
                                    )
                                )

                                // 添加回收場地點
                                val recycleCenters = listOf(
                                    LatLng(loc.latitude + 0.01, loc.longitude + 0.01),
                                    LatLng(loc.latitude - 0.01, loc.longitude - 0.01)
                                )
                                recycleCenters.forEach {
                                    googleMap.addMarker(
                                        MarkerOptions().position(it).title("回收場")
                                    )
                                }
                            }
                        }

                        // MapView 不需要顯式調用 onPause() 和 onDestroy()
                    }
                }, modifier = Modifier.fillMaxSize())
            } else {
                // 顯示一個正在獲取位置的提示
                Text(text = "正在獲取您的位置...", color = Color.Black)
            }
        }
    }
}
