package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SconeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(Color(0xFFA0D6A1)) // 淺綠色背景
    ) {
        Image(
            painter = painterResource(id = R.drawable.homepage2),  // 背景
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
            // 登出按鈕 + 標題
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // 登出按鈕靠左
                Text(
                    text = "登出",
                    fontSize = 15.sp,
                    color = Color(0xFF005500),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        } }
                        .background(
                            color = Color(0xFFE8F5E9),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 標題置中
                Text(
                    text = "綠  森  友",
                    fontSize = 28.sp,
                    color = Color(0xFF005500),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 橫線
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF005500))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 圓形按鈕群組
            Box(modifier = Modifier.fillMaxSize()) {
                ImageButton(
                    resId = R.drawable.gamebtn,
                    contentDescription = "森林闖關",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-100).dp, y = (-80).dp)
                ) { navController.navigate("game") }

                ImageButton(
                    resId = R.drawable.myforestbtn,
                    contentDescription = "我的森林",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 100.dp, y = (-80).dp)
                ) { navController.navigate("myforest") }

                ImageButton(
                    resId = R.drawable.calculatorbtn,
                    contentDescription = "碳排放計算器",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-100).dp, y = 80.dp)
                ) { navController.navigate("calculator") }

                ImageButton(
                    resId = R.drawable.garbagebtn,
                    contentDescription = "垃圾分類",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 100.dp, y = 80.dp)
                ) { navController.navigate("garbage") }

                ImageButton(
                    resId = R.drawable.storebtn,
                    contentDescription = "商店",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                ) { navController.navigate("store") }
            }
        }
    }
}

// 使用圖片當作按鈕，可接受外部傳入 Modifier
@Composable
fun ImageButton(
    resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentScale = ContentScale.Crop
    )
}
