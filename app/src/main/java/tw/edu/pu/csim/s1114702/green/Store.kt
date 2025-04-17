package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StoreScreen(navController: NavController, viewModel: ViewModel) {
    val completedCount = viewModel.checkStates.count { it }
    val score = when {
        completedCount >= 8 -> 10
        completedCount >= 5 -> 5
        completedCount >= 3 -> 3
        else -> 0
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFA0D6A1)) // 淺綠色背景
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFA0D6A1))
                ) {
                    // 返回箭頭 + 標題區塊（有 padding）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 返回按鈕靠左
                        Image(
                            painter = painterResource(id = R.drawable.backarrow),
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.CenterStart)
                                .clickable { navController.popBackStack() }
                        )

                        // 標題置中
                        Text(
                            text = "商 店",
                            fontSize = 28.sp,
                            color = Color(0xFF005500),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFF005500))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(text = "目前累積分數：$score 分", fontSize = 24.sp, color = Color.Black)
                }
            }
        }
    }
}
