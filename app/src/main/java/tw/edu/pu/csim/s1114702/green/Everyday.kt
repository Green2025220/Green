package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun EverydayScreen(navController: NavController, viewModel: ViewModel) {
    val checklistItems = viewModel.checklistItems
    val checkStates = viewModel.checkStates

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
                    text = "每日綠色挑戰",
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

        // 清單項目（可滑動內容）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            checklistItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkStates[index],
                        onCheckedChange = { checkStates[index] = it }
                    )
                    Text(
                        text = item,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 每個項目下方的分隔線
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF005500))
                )
            }
        }
    }
}
