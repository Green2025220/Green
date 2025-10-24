package tw.edu.pu.csim.s1114702.green


import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement

@Composable
fun CalculatorScreen(navController: NavController) {
    val backgroundImage = painterResource(id = R.drawable.road)
    val backButtonImage = painterResource(id = R.drawable.backarrow)


    // 閃爍動畫
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景圖片
        Image(
            painter = backgroundImage,
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        // **返回箭頭 + 提示文字**
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // **返回按鈕靠左**
            Image(
                painter = backButtonImage,
                contentDescription = "Back",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { navController.popBackStack() }
            )


            Spacer(modifier = Modifier.width(16.dp))


            // **提示文字 - 閃爍效果**
            Text(
                text = "請點選交通方式",
                fontSize = 24.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
        }


        // 中型汽油車按鈕 - 用 Box 包裹
        Box(
            modifier = Modifier
                .size(300.dp)  // 👈 控制點擊區域
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = 10.dp)
                .padding(top = 80.dp, end = 0.dp)
                .clickable { navController.navigate("Car") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.car5),
                contentDescription = "Car",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 中型機車按鈕 - 用 Box 包裹
        Box(
            modifier = Modifier
                .size(260.dp)  // 👈 控制點擊區域
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 20.dp)
                .padding(top = 80.dp, end = 0.dp)
                .clickable { navController.navigate("Motor") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.scooter4),
                contentDescription = "Scooter",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // 城市公車按鈕 - 用 Box 包裹
        Box(
            modifier = Modifier
                .size(280.dp)  // 👈 控制點擊區域
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-10).dp)
                .padding(top = 80.dp, end = 0.dp)
                .clickable { navController.navigate("Bus") }
        ) {
            Image(
                painter = painterResource(id = R.drawable.bus4),
                contentDescription = "Bus",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}