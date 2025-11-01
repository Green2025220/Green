package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GameScreen(navController: NavController) {
    val backgroundImage = painterResource(id = R.drawable.gamebg)
    val backButtonImage = painterResource(id = R.drawable.backarrow)

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