package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button


@Composable
fun MyforestScreen(navController: NavController, viewModel: ViewModel) {
    val backgroundImage = painterResource(id = R.drawable.grassland)
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // 返回按鈕（左上）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clickable { navController.popBackStack() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back",
                modifier = Modifier.size(40.dp)
            )
        }

        // 保存按鈕（右上）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Button(
                onClick = {
                    val email = "user@example.com" // ⛳ 替換成登入後的 email
                    viewModel.saveDailyChallengeToFirebase(email)
                    Toast.makeText(context, "位置已儲存", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(text = "保存", color = Color.White)
            }
        }

        // 顯示兌換商品
        if ("澆水器" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.watering,
                description = "澆水器",
                viewModel = viewModel
            )
        }
        if ("剪刀" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.scissors,
                description = "剪刀",
                viewModel = viewModel
            )
        }
    }
}



@Composable
fun DraggableItem(
    imageRes: Int,
    description: String,
    viewModel: ViewModel
) {
    // 初始位置從 ViewModel 讀取
    val initial = viewModel.getItemPosition(description)
    var offsetX by remember { mutableStateOf(initial.x) }
    var offsetY by remember { mutableStateOf(initial.y) }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = description,
        modifier = Modifier
            .size(100.dp)
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    viewModel.updateItemPosition(description, offsetX, offsetY) // ✅ 寫入 ViewModel
                }
            }
    )
}

