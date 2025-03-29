package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun SconeScreen(navController: NavController) {
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
                    text = "綠  森  友",
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

            // 按鈕列表
            val buttonLabels = listOf("全台回收地點", "碳排放計算器", "森林闖關", "我的森林", "每日綠色挑戰")

            buttonLabels.forEach { label ->
                RoundedButton(text = label, onClick = {
                    when (label) {
                        "全台回收地點" -> navController.navigate("recycle")
                        "碳排放計算器" -> navController.navigate("calculator")
                        "森林闖關" -> navController.navigate("game")
                        "我的森林" -> navController.navigate("myforest")
                        "每日綠色挑戰" -> navController.navigate("everyday")
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



// 圓角按鈕組件
@Composable
fun RoundedButton(text: String, onClick: () -> Unit) {
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
