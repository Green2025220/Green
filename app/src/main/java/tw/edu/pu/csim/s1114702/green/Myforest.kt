package tw.edu.pu.csim.s1114702.green

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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

        // 顯示兌換商品，使用 DraggableItem，可縮放拖曳
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
        if ("三叉" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.rake,
                description = "三叉",
                viewModel = viewModel
            )
        }
        if ("鏟子" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.shovel,
                description = "鏟子",
                viewModel = viewModel
            )
        }
        if ("青楓" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.tree1,
                description = "青楓",
                viewModel = viewModel
            )
        }
        if ("牧野氏山芙蓉" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.tree2,
                description = "牧野氏山芙蓉",
                viewModel = viewModel
            )
        }
        if ("台灣牛樟" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.tree3,
                description = "台灣牛樟",
                viewModel = viewModel
            )
        }
        if ("截萼黃槿" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.tree4,
                description = "截萼黃槿",
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
    val initial = viewModel.getItemPosition(description)
    var offsetX by remember { mutableStateOf(initial.x) }
    var offsetY by remember { mutableStateOf(initial.y) }
    var scale by remember { mutableStateOf(1f) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    LaunchedEffect(offsetX, offsetY, scale) {
        viewModel.updateItemPosition(description, offsetX, offsetY)
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .transformable(state = transformableState)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = description,
            modifier = Modifier.size(100.dp)
        )
    }
}
