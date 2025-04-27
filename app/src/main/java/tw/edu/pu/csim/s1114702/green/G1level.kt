package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun G1levelScreen(navController: NavController) {
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

        // 難度選擇按鈕
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonLabels = listOf("1低等難度", "中等難度", "高等難度")
            buttonLabels.forEach { label ->
                CustomButton(text = label, onClick = {
                    when (label) {
                        "1低等難度" -> navController.navigate("level1") //導到遊戲畫面
                        "中等難度" -> navController.navigate("secgame") //
                        "高等難度" -> navController.navigate("secgame") //
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(80.dp)
            .background(Color(0xFFB8E6C0), shape = RoundedCornerShape(40.dp)) // 圓角矩形
            .clickable { onClick() },  // 處理點擊事件
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}
