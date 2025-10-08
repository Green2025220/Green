package tw.edu.pu.csim.s1114702.green

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import kotlin.math.roundToInt

// 🧺 範例資料類
data class PlacedItem(
    val id: Int,
    val imageRes: Int,
    val description: String,
    var x: Float = 400f,
    var y: Float = 600f,
    var scale: Float = 1f
)

@Composable
fun MyforestScreen(navController: NavController, viewModel: ViewModel) {
    val backgroundImage = painterResource(id = R.drawable.grassland)
    val context = LocalContext.current

    // 籃子開關狀態
    var basketOpen by remember { mutableStateOf(false) }

    // 範例可兌換物品
    val basketItems = remember {
        mutableStateMapOf(
            "澆水器" to 1,
            "剪刀" to 2,
            "三叉" to 1,
            "鏟子" to 1,
            "青楓" to 2,
            "台灣牛樟" to 1
        )
    }

    // 已擺放的物件列表
    var placedItems by remember { mutableStateOf(listOf<PlacedItem>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景
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
                .align(Alignment.TopStart)
                .clickable { navController.popBackStack() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back",
                modifier = Modifier.size(40.dp)
            )
        }

        // 保存按鈕
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, "位置已儲存", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("保存", color = Color.White)
            }
        }

        // 草地上已擺放的物件
        placedItems.forEach { item ->
            DraggablePlacedItem(
                placedItem = item,
                onLongPress = { id ->
                    val selected = placedItems.find { it.id == id }
                    if (selected != null) {
                        placedItems = placedItems.filterNot { it.id == id }
                        basketItems[selected.description] =
                            (basketItems[selected.description] ?: 0) + 1
                    }
                },
                onPositionChange = { id, newX, newY ->
                    placedItems = placedItems.map {
                        if (it.id == id) it.copy(x = newX, y = newY) else it
                    }
                }
            )
        }

        // 🧺 籃子按鈕
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF8B4513), CircleShape)
                    .clickable { basketOpen = !basketOpen },
                contentAlignment = Alignment.Center
            ) {
                Text("🧺", fontSize = MaterialTheme.typography.headlineLarge.fontSize)
            }
        }

        // 🧺 展開的籃子內容（橫向滾動）
        if (basketOpen) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 90.dp)
                    .height(120.dp)
                    .background(Color(0xFFEDE0C8), shape = MaterialTheme.shapes.medium)
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                basketItems.forEach { (name, count) ->
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    // 點擊擺放物品
                                    val resId = when (name) {
                                        "澆水器" -> R.drawable.watering
                                        "剪刀" -> R.drawable.scissors
                                        "三叉" -> R.drawable.rake
                                        "鏟子" -> R.drawable.shovel
                                        "青楓" -> R.drawable.tree1
                                        "台灣牛樟" -> R.drawable.tree3
                                        else -> R.drawable.tree4
                                    }
                                    val newItem = PlacedItem(
                                        id = placedItems.size + 1,
                                        imageRes = resId,
                                        description = name
                                    )
                                    placedItems = placedItems + newItem
                                    basketItems[name] = count - 1
                                }
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Image(
                                    painter = painterResource(
                                        id = when (name) {
                                            "澆水器" -> R.drawable.watering
                                            "剪刀" -> R.drawable.scissors
                                            "三叉" -> R.drawable.rake
                                            "鏟子" -> R.drawable.shovel
                                            "青楓" -> R.drawable.tree1
                                            "台灣牛樟" -> R.drawable.tree3
                                            else -> R.drawable.tree4
                                        }
                                    ),
                                    contentDescription = name,
                                    modifier = Modifier.size(80.dp)
                                )
                                // 顯示數量標籤
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = count.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraggablePlacedItem(
    placedItem: PlacedItem,
    onLongPress: (Int) -> Unit,
    onPositionChange: (Int, Float, Float) -> Unit
) {
    var offsetX by remember { mutableStateOf(placedItem.x) }
    var offsetY by remember { mutableStateOf(placedItem.y) }
    var scale by remember { mutableStateOf(placedItem.scale) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    LaunchedEffect(offsetX, offsetY, scale) {
        onPositionChange(placedItem.id, offsetX, offsetY)
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
                scaleX = scale
                scaleY = scale
            }
            .transformable(state = transformableState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress(placedItem.id) }
                )
            }
    ) {
        Image(
            painter = painterResource(id = placedItem.imageRes),
            contentDescription = placedItem.description,
            modifier = Modifier.size(100.dp)
        )
    }
}
