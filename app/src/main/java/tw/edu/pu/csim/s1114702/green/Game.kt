package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun GameScreen(navController: NavController) {
    val backgroundImage = painterResource(id = R.drawable.gamebg)
    val backButtonImage = painterResource(id = R.drawable.backarrow)
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 設定背景圖片
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 返回按鈕（左上角）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart)
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back"
            )
        }

        // 資訊按鈕（右上角）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(50.dp)
                .clickable { showInfoDialog = true }
                .align(Alignment.TopEnd)
        ) {
            Image(
                painter = painterResource(id = R.drawable.tool),
                contentDescription = "tool",
                modifier = Modifier.fillMaxSize()
            )
        }

        // 三個圖片按鈕
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 回收挑戰按鈕
            ImageButton(
                imageRes = R.drawable.game1btn,
                onClick = { navController.navigate("Game1") }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 認識SDGs按鈕
            ImageButton(
                imageRes = R.drawable.turnbtn,
                onClick = { navController.navigate("Turn") }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 垃圾分類按鈕
            ImageButton(
                imageRes = R.drawable.gbggbtn,
                onClick = { navController.navigate("Garbagegame") }
            )
        }

        // 資訊對話框
        if (showInfoDialog) {
            InfoDialog(onDismiss = { showInfoDialog = false })
        }
    }
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    val pages = listOf(R.drawable.d2, R.drawable.d1, R.drawable.d3)
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

@Composable
fun ImageButton(imageRes: Int, onClick: () -> Unit) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier = Modifier
            .height(150.dp)
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}