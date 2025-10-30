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
import androidx.compose.ui.unit.sp
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
        "截萼黃槿" to redeemedItems.count { it == "截萼黃槿" },
        "柵欄1" to redeemedItems.count { it == "柵欄1" },
        "柵欄2" to redeemedItems.count { it == "柵欄2" },
        "柵欄3" to redeemedItems.count { it == "柵欄3" },
        "柵欄4" to redeemedItems.count { it == "柵欄4" },
        "太陽" to redeemedItems.count { it == "太陽" },
        "拖拉機" to redeemedItems.count { it == "拖拉機" },
        "風車" to redeemedItems.count { it == "風車" },
        "帽子" to redeemedItems.count { it == "帽子" },
        "小苒" to redeemedItems.count { it == "小苒" },
        "小薇" to redeemedItems.count { it == "小薇" },
        "小浩" to redeemedItems.count { it == "小浩" },
        "農夫" to redeemedItems.count { it == "農夫" },
        "狐狸" to redeemedItems.count { it == "狐狸" },
        "兔子" to redeemedItems.count { it == "兔子" },
        "蝴蝶" to redeemedItems.count { it == "蝴蝶" },
        "黃雛菊" to redeemedItems.count { it == "黃雛菊" },
        "台灣火刺木" to redeemedItems.count { it == "台灣火刺木" },
        "白雛菊" to redeemedItems.count { it == "白雛菊" },
        "香雪蘭" to redeemedItems.count { it == "香雪蘭" },
        "向日葵" to redeemedItems.count { it == "向日葵" },
        "鬱金香" to redeemedItems.count { it == "鬱金香" },
        "法國菊" to redeemedItems.count { it == "法國菊" },
        "松樹牛肝菌" to redeemedItems.count { it == "松樹牛肝菌" },
        "山紅菇" to redeemedItems.count { it == "山紅菇" },
        "鈴蘭" to redeemedItems.count { it == "鈴蘭" },
        "薰衣草" to redeemedItems.count { it == "薰衣草" },
        "風信子" to redeemedItems.count { it == "風信子" },
        "玉米" to redeemedItems.count { it == "玉米" },
        "樹苗" to redeemedItems.count { it == "樹苗" },
        "種子" to redeemedItems.count { it == "種子" },
        "粉色蘭花" to redeemedItems.count { it == "粉色蘭花" },
        "花圃" to redeemedItems.count { it == "花圃" },
        "台灣欒樹" to redeemedItems.count { it == "台灣欒樹" },
        "蘋果樹" to redeemedItems.count { it == "蘋果樹" },
        "綠樹" to redeemedItems.count { it == "綠樹" },
        "松樹" to redeemedItems.count { it == "松樹" },
        "玫瑰叢" to redeemedItems.count { it == "玫瑰叢" },
        "櫻花樹" to redeemedItems.count { it == "櫻花樹" },
        "銀杏樹" to redeemedItems.count { it == "銀杏樹" }
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
        "柵欄1" -> R.drawable.fence1
        "柵欄2" -> R.drawable.fence2
        "柵欄3" -> R.drawable.fence3
        "柵欄4" -> R.drawable.fence4
        "太陽" -> R.drawable.sun
        "拖拉機" -> R.drawable.tractor
        "風車" -> R.drawable.windmill
        "帽子" -> R.drawable.hat
        "小苒" -> R.drawable.girl1
        "小薇" -> R.drawable.girl2
        "小浩" -> R.drawable.boy1
        "農夫" -> R.drawable.boy2
        "狐狸" -> R.drawable.fox
        "兔子" -> R.drawable.rabbit
        "蝴蝶" -> R.drawable.butterfly
        "黃雛菊" -> R.drawable.yellowdaisy
        "台灣火刺木" -> R.drawable.tree9
        "白雛菊" -> R.drawable.twoflower
        "香雪蘭" -> R.drawable.tulip
        "向日葵" -> R.drawable.sunflower
        "鬱金香" -> R.drawable.flower2
        "法國菊" -> R.drawable.oneflower
        "松樹牛肝菌" -> R.drawable.mushroom2
        "山紅菇" -> R.drawable.mushroom1
        "鈴蘭" -> R.drawable.lilyofthevalley
        "薰衣草" -> R.drawable.lavender
        "風信子" -> R.drawable.hyacinth
        "玉米" -> R.drawable.corn
        "樹苗" -> R.drawable.bud
        "種子" -> R.drawable.seed35
        "粉色蘭花" -> R.drawable.flower1
        "花圃" -> R.drawable.flower38
        "台灣欒樹" -> R.drawable.tree10
        "蘋果樹" -> R.drawable.tree11
        "綠樹" -> R.drawable.tree6
        "松樹" -> R.drawable.tree5
        "玫瑰叢" -> R.drawable.rosebush
        "櫻花樹" -> R.drawable.tree7
        "銀杏樹" -> R.drawable.tree8
        else -> R.drawable.tree4
    }
}


@Composable
fun MyforestScreen(navController: NavController, viewModel: ViewModel, userEmail: String = "user@example.com") {
    val backgroundImage = painterResource(id = R.drawable.grassland)
    val context = LocalContext.current

    var basketOpen by remember { mutableStateOf(false) }

    // 追蹤是否有未儲存的變更
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 儲存初始狀態用於比較
    var initialPlacedItems by remember { mutableStateOf(listOf<PlacedItem>()) }

    // 從 ViewModel 中的已兌換物品動態生成籃子內容
    val basketItems = remember(viewModel.redeemedItems) {
        getBasketItemsFromStore(viewModel.redeemedItems).toMutableMap()
    }

    var placedItems by remember { mutableStateOf(listOf<PlacedItem>()) }
    var usedItemCounts by remember { mutableStateOf(mapOf<String, Int>()) }

    // 從 Firebase 載入已放置的物品
    LaunchedEffect(Unit) {
        val email = userEmail.ifEmpty { "user@example.com" }
        viewModel.loadPlacedItemsFromFirebase(email) { loadedItems ->
            placedItems = loadedItems
            initialPlacedItems = loadedItems.map { it.copy() } // 複製初始狀態
            hasUnsavedChanges = false // 初始載入不算變更

            // 更新已使用的物品數量
            val counts = mutableMapOf<String, Int>()
            loadedItems.forEach { item ->
                counts[item.description] = (counts[item.description] ?: 0) + 1
            }
            usedItemCounts = counts
        }
    }

    // 監控 placedItems 變化，判斷是否有未儲存的變更
    LaunchedEffect(placedItems) {
        if (initialPlacedItems.isNotEmpty()) {
            hasUnsavedChanges = placedItems != initialPlacedItems
        }
    }

    // 儲存函數
    fun saveChanges() {
        val email = userEmail.ifEmpty { "user@example.com" }
        viewModel.savePlacedItemsToFirebase(email, placedItems)
        initialPlacedItems = placedItems.map { it.copy() }
        hasUnsavedChanges = false
        Toast.makeText(context, "✓ 位置已儲存", Toast.LENGTH_SHORT).show()
    }

    // 返回確認對話框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    "⚠️ 尚未儲存",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "你的森林佈局尚未儲存，是否要先儲存再離開？",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("取消", color = Color.Gray)
                    }
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("不儲存離開", color = Color(0xFFE53935))
                    }
                    Button(
                        onClick = {
                            saveChanges()
                            showExitDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F9D9D))
                    ) {
                        Text("儲存離開")
                    }
                }
            },
            dismissButton = {},
            containerColor = Color(0xFFE8FFF5)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // 返回按鈕（左上）- 加入儲存檢查
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clickable {
                    if (hasUnsavedChanges) {
                        showExitDialog = true
                    } else {
                        navController.popBackStack()
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.backarrow),
                contentDescription = "Back",
                modifier = Modifier.size(40.dp)
            )
        }

        // 儲存按鈕（右上）- 顯示儲存狀態
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = { saveChanges() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasUnsavedChanges) Color(0xFF4F9D9D) else Color(0xFF4CAF50)
                    ),
                    enabled = hasUnsavedChanges
                ) {
                    Text(
                        text = if (hasUnsavedChanges) "儲存" else "已儲存",
                        color = Color.White
                    )
                }


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
                        hasUnsavedChanges = true
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
                                    hasUnsavedChanges = true
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