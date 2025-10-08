package tw.edu.pu.csim.s1114702.green

import android.widget.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier  // 確保這行已經添加

@Composable
fun GameScreen(navController: NavController) {
    val backgroundImage = painterResource(id = R.drawable.greenback)
    val backButtonImage = painterResource(id = R.drawable.backarrow)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 設定背景圖片
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize() // 使用 fillMaxSize() 確保背景鋪滿
        )

        // 返回按鈕（左上角）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() } // 點擊後返回上一頁
                .align(Alignment.TopStart) // 確保按鈕位於左上角
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back"
            )
        }

        // 顯示回收地點頁面的文字
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonLabels = listOf("回收挑戰","垃圾分類","認識SDGs")
            buttonLabels.forEach { label ->
                Button(text = label, onClick = {

                    if(label == "回收挑戰"){
                        navController.navigate("g1level")
                    }
                    if(label == "垃圾分類"){
                        navController.navigate("Garbagegame")
                    }
                    if(label == "認識SDGs"){
                        navController.navigate("Turn")
                    }

                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun Button(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(80.dp)
            .background(Color(0xFFB8E6C0), shape = RoundedCornerShape(40.dp)) // 圓角矩形
            .clickable { onClick() },  // 使用 onClick 來處理點擊事件
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}