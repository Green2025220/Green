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

@Composable
fun MyforestScreen(navController: NavController, viewModel: ViewModel) {
    val backgroundImage = painterResource(id = R.drawable.grassland)

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // 返回按鈕
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clickable { navController.popBackStack() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back",
                modifier = Modifier.size(40.dp)
            )
        }

        // 可移動的兌換商品圖示
        if ("澆水器" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.watering,
                description = "澆水器"
            )
        }
        if ("剪刀" in viewModel.redeemedItems) {
            DraggableItem(
                imageRes = R.drawable.scissors,
                description = "剪刀"
            )
        }
    }
}

@Composable
fun DraggableItem(imageRes: Int, description: String) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

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
                }
            }
    )
}
