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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt


data class PlacedItem(
    val id: Int,
    val imageRes: Int,
    val description: String,
    var x: Float = 400f,
    var y: Float = 600f,
    var scale: Float = 1f
)


// 籃子物品與商店商品的映射
fun getBasketItemsFromStore(redeemedItems: List<String>): Map<String, Int> {
    return mapOf(
        "澆水器" to redeemedItems.count { it == "澆水器" },
        "剪刀" to redeemedItems.count { it == "剪刀" },
        "三叉" to redeemedItems.count { it == "三叉" },
        "鏟子" to redeemedItems.count { it == "鏟子" },
        "青楓" to redeemedItems.count { it == "青楓" },
        "牧野氏山芙蓉" to redeemedItems.count { it == "牧野氏山芙蓉" },
        "台灣牛樟" to redeemedItems.count { it == "台灣牛樟" },
        "截萼黃槿" to redeemedItems.count { it == "截萼黃槿" }
    )
}


// 獲取物品對應的圖片資源
fun getImageResourceForItem(name: String): Int {
    return when (name) {
        "澆水器" -> R.drawable.watering
        "剪刀" -> R.drawable.scissors
        "三叉" -> R.drawable.rake
        "鏟子" -> R.drawable.shovel
        "青楓" -> R.drawable.tree1
        "牧野氏山芙蓉" -> R.drawable.tree2
        "台灣牛樟" -> R.drawable.tree3
        "截萼黃槿" -> R.drawable.tree4
        else -> R.drawable.tree4
    }
}




@Composable
fun MyforestScreen(navController: NavController, viewModel: ViewModel) {
    val backgroundImage = painterResource(id = R.drawable.grassland)
    val context = LocalContext.current


    var basketOpen by remember { mutableStateOf(false) }


    // 從 ViewModel 中的已兌換物品動態生成籃子內容
    val basketItems = remember(viewModel.redeemedItems) {
        getBasketItemsFromStore(viewModel.redeemedItems).toMutableMap()
    }


    var placedItems by remember { mutableStateOf(listOf<PlacedItem>()) }
    var usedItemCounts by remember { mutableStateOf(mapOf<String, Int>()) }


    // 從 Firebase 載入已放置的物品
    LaunchedEffect(Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: "user@example.com"
        viewModel.loadPlacedItemsFromFirebase(email) { loadedItems ->
            placedItems = loadedItems
            // 更新已使用的物品數量
            val counts = mutableMapOf<String, Int>()
            loadedItems.forEach { item ->
                counts[item.description] = (counts[item.description] ?: 0) + 1
            }
            usedItemCounts = counts
        }
    }


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
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: "user@example.com"
                    // 保存草地上物品的位置到 Firebase
                    viewModel.savePlacedItemsToFirebase(email, placedItems)
                    Toast.makeText(context, "位置已儲存", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(text = "保存", color = Color.White)
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
                        usedItemCounts = usedItemCounts.toMutableMap().apply {
                            put(selected.description, (get(selected.description) ?: 0) - 1)
                        }
                    }
                },
                onPositionChange = { id, newX, newY ->
                    placedItems = placedItems.map {
                        if (it.id == id) it.copy(x = newX, y = newY) else it
                    }
                },
                onScaleChange = { id, newScale ->
                    placedItems = placedItems.map {
                        if (it.id == id) it.copy(scale = newScale) else it
                    }
                }
            )
        }


        // 籃子按鈕 - 放在底部
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
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


        // 籃子內容（橫向滾動）- 與商店連結
        if (basketOpen) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 90.dp, bottom = 16.dp)
                    .height(120.dp)
                    .background(Color(0xFFEDE0C8), shape = MaterialTheme.shapes.medium)
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                basketItems.forEach { (name, totalCount) ->
                    val usedCount = usedItemCounts[name] ?: 0
                    val availableCount = totalCount - usedCount
                    if (availableCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    val resId = getImageResourceForItem(name)
                                    val newItem = PlacedItem(
                                        id = (placedItems.maxOfOrNull { it.id } ?: 0) + 1,
                                        imageRes = resId,
                                        description = name
                                    )
                                    placedItems = placedItems + newItem
                                    usedItemCounts = usedItemCounts.toMutableMap().apply {
                                        put(name, (get(name) ?: 0) + 1)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Image(
                                    painter = painterResource(id = getImageResourceForItem(name)),
                                    contentDescription = name,
                                    modifier = Modifier.size(80.dp)
                                )
                                // 顯示可用數量標籤
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.Green, CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = availableCount.toString(),
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


/**
 * 可拖曳、縮放的已放置物品組件
 * @param placedItem 已放置的物品資料
 * @param onLongPress 長按刪除的回調函數
 * @param onPositionChange 位置改變的回調函數
 * @param onScaleChange 縮放改變的回調函數
 */
@Composable
fun DraggablePlacedItem(
    placedItem: PlacedItem,
    onLongPress: (Int) -> Unit,
    onPositionChange: (Int, Float, Float) -> Unit,
    onScaleChange: (Int, Float) -> Unit
) {
    var offsetX by remember { mutableStateOf(placedItem.x) }
    var offsetY by remember { mutableStateOf(placedItem.y) }
    var scale by remember { mutableStateOf(placedItem.scale) }


    // 用於處理縮放和拖曳
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        // 處理縮放
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        onScaleChange(placedItem.id, scale)


        // 處理拖曳
        offsetX += panChange.x
        offsetY += panChange.y
        onPositionChange(placedItem.id, offsetX, offsetY)
    }


    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .transformable(state = transformableState)
            .pointerInput(placedItem.id) {
                detectTapGestures(
                    onLongPress = {
                        // 長按刪除物品
                        onLongPress(placedItem.id)
                    }
                )
            }
    ) {
        Image(
            painter = painterResource(id = placedItem.imageRes),
            contentDescription = placedItem.description,
            modifier = Modifier.size(100.dp)
        )
    }


    // 當位置改變時同步更新
    LaunchedEffect(offsetX, offsetY) {
        onPositionChange(placedItem.id, offsetX, offsetY)
    }


    // 當縮放改變時同步更新
    LaunchedEffect(scale) {
        onScaleChange(placedItem.id, scale)
    }
}

