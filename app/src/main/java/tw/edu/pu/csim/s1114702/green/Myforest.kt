package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun MyforestScreen(navController: NavController) {
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
            modifier = Modifier.matchParentSize()
        )

        // 返回按鈕（左上角）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clickable { navController.popBackStack() } // 點擊後返回上一頁
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back",
                modifier = Modifier.size(40.dp) // 設定返回按鈕大小
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
            Text(
                text = "這是我的森林頁面",
                fontSize = 24.sp,
                color = Color.Black
            )
        }
    }
}
