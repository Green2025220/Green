package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SconeScreen(navController: NavController) {
    var showgameInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                // 上方兩個按鈕
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

                // 下方兩個按鈕
                ImageButton(
                    resId = R.drawable.calculatorbtn,
                    contentDescription = "碳排放計算器",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-100).dp, y = 80.dp)
                ) { navController.navigate("calculator") }

                ImageButton(
                    resId = R.drawable.garbagebtn,
                    contentDescription = "一拍即分",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 100.dp, y = 80.dp)
                ) { navController.navigate("garbage") }

                // ⭐ 新增：中間的每日任務按鈕
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(4.dp, Color(0xFF2E7D32), CircleShape)
                        .clickable { navController.navigate("dailyJournal") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📔",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "每日任務",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 商店按鈕（底部）
                ImageButton(
                    resId = R.drawable.storebtn,
                    contentDescription = "商店",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                ) { navController.navigate("store") }
            }
        }

        // 資訊按鈕（右上角）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(35.dp)
                .clickable { showgameInfoDialog = true }
                .align(Alignment.TopEnd)
        ) {
            Image(
                painter = painterResource(id = R.drawable.information),
                contentDescription = "information",
                modifier = Modifier.fillMaxSize()
            )
        }

        // 資訊對話框
        if (showgameInfoDialog) {
            gameInfoDialog(onDismiss = { showgameInfoDialog = false })
        }
    }
}

@Composable
fun gameInfoDialog(onDismiss: () -> Unit) {
    val pages = listOf(R.drawable.g1, R.drawable.g2, R.drawable.g3, R.drawable.g4, R.drawable.g5)
    var currentPageIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable { /* 防止點擊內容時關閉 */ }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 顯示當前頁面圖片
                Image(
                    painter = painterResource(id = pages[currentPageIndex]),
                    contentDescription = "Info Page ${currentPageIndex + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 左右切換按鈕
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val leftArrowColor =
                        if (currentPageIndex == 0) Color.Gray else Color.White
                    val rightArrowColor =
                        if (currentPageIndex == pages.lastIndex) Color.Gray else Color.White

                    Text(
                        text = "<",
                        fontSize = 32.sp,
                        color = leftArrowColor,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .clickable(enabled = currentPageIndex > 0) {
                                currentPageIndex--
                            }
                    )

                    Text(
                        text = "${currentPageIndex + 1} / ${pages.size}",
                        fontSize = 20.sp,
                        color = Color.White
                    )

                    Text(
                        text = ">",
                        fontSize = 32.sp,
                        color = rightArrowColor,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .clickable(enabled = currentPageIndex < pages.lastIndex) {
                                currentPageIndex++
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 關閉按鈕
                Text(
                    text = "關閉",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF408080))
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clickable { onDismiss() }
                )
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