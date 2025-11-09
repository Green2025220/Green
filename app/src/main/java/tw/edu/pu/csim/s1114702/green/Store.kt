package tw.edu.pu.csim.s1114702.green

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(navController: NavController, viewModel: ViewModel, userEmail: String) {

    val categories = listOf("物品類", "動物類", "人物類", "植物類")
    var currentCategoryIndex by remember { mutableStateOf(0) }
    val currentCategory = categories[currentCategoryIndex]
    var showInsufficientScore by remember { mutableStateOf(false) }
    var showPurchaseSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 監控購買狀態變化
    LaunchedEffect(viewModel.redeemedItems.size) {
        Log.d("StoreScreen", "已購買商品數量: ${viewModel.redeemedItems.size}")
    }

    val storeItems = listOf(
        StoreItem(R.drawable.s1, "澆水器", 10, R.drawable.watering, "物品類"),
        StoreItem(R.drawable.s4, "柵欄1", 10, R.drawable.fence1, "物品類"),
        StoreItem(R.drawable.s5, "柵欄2", 10, R.drawable.fence2, "物品類"),
        StoreItem(R.drawable.s6, "柵欄3", 10, R.drawable.fence3, "物品類"),
        StoreItem(R.drawable.s7, "柵欄4", 10, R.drawable.fence4, "物品類"),
        StoreItem(R.drawable.s8, "太陽", 10, R.drawable.sun, "物品類"),
        StoreItem(R.drawable.s10, "風車", 10, R.drawable.windmill, "物品類"),
        StoreItem(R.drawable.s11, "帽子", 10, R.drawable.hat, "物品類"),
        StoreItem(R.drawable.s12, "三叉", 10, R.drawable.rake, "物品類"),
        StoreItem(R.drawable.s13, "鏟子", 10, R.drawable.shovel, "物品類"),
        StoreItem(R.drawable.s14, "剪刀", 10, R.drawable.scissors, "物品類"),
        StoreItem(R.drawable.s18, "狐狸", 20, R.drawable.fox, "動物類"),
        StoreItem(R.drawable.s19, "兔子", 20, R.drawable.rabbit, "動物類"),
        StoreItem(R.drawable.s20, "蝴蝶", 20, R.drawable.butterfly, "動物類"),
        StoreItem(R.drawable.s9, "拖拉機", 30, R.drawable.tractor, "物品類"),
        StoreItem(R.drawable.s15, "小苒", 30, R.drawable.girl1, "人物類"),
        StoreItem(R.drawable.s16, "小薇", 30, R.drawable.girl2, "人物類"),
        StoreItem(R.drawable.s17, "小浩", 30, R.drawable.boy1, "人物類"),
        StoreItem(R.drawable.s2, "農夫", 30, R.drawable.boy2, "人物類"),
        StoreItem(R.drawable.s21, "黃雛菊", 40, R.drawable.yellowdaisy, "植物類"),
        StoreItem(R.drawable.s22, "台灣火刺木", 40, R.drawable.tree9, "植物類"),
        StoreItem(R.drawable.s23, "白雛菊", 40, R.drawable.twoflower, "植物類"),
        StoreItem(R.drawable.s24, "香雪蘭", 40, R.drawable.tulip, "植物類"),
        StoreItem(R.drawable.s25, "向日葵", 40, R.drawable.sunflower, "植物類"),
        StoreItem(R.drawable.s26, "鬱金香", 40, R.drawable.flower2, "植物類"),
        StoreItem(R.drawable.s27, "法國菊", 40, R.drawable.oneflower, "植物類"),
        StoreItem(R.drawable.s28, "松樹牛肝菌", 40, R.drawable.mushroom2, "植物類"),
        StoreItem(R.drawable.s29, "山紅菇", 40, R.drawable.mushroom1, "植物類"),
        StoreItem(R.drawable.s30, "鈴蘭", 40, R.drawable.lilyofthevalley, "植物類"),
        StoreItem(R.drawable.s31, "薰衣草", 40, R.drawable.lavender, "植物類"),
        StoreItem(R.drawable.s32, "風信子", 40, R.drawable.hyacinth, "植物類"),
        StoreItem(R.drawable.s33, "玉米", 40, R.drawable.corn, "植物類"),
        StoreItem(R.drawable.s34, "樹苗", 40, R.drawable.bud, "植物類"),
        StoreItem(R.drawable.s35, "種子", 40, R.drawable.seed35, "植物類"),
        StoreItem(R.drawable.s36, "粉色蘭花", 40, R.drawable.flower1, "植物類"),
        StoreItem(R.drawable.s37, "台灣牛樟", 40, R.drawable.tree3, "植物類"),
        StoreItem(R.drawable.s38, "花圃", 40, R.drawable.flower38, "植物類"),
        StoreItem(R.drawable.s39, "青楓", 40, R.drawable.tree1, "植物類"),
        StoreItem(R.drawable.s3, "牧野氏山芙蓉", 40, R.drawable.tree2, "植物類"),
        StoreItem(R.drawable.s40, "截萼黃槿", 40, R.drawable.tree4, "植物類"),
        StoreItem(R.drawable.s41, "台灣欒樹", 40, R.drawable.tree10, "植物類"),
        StoreItem(R.drawable.s42, "蘋果樹", 40, R.drawable.tree11, "植物類"),
        StoreItem(R.drawable.s43, "綠樹", 40, R.drawable.tree6, "植物類"),
        StoreItem(R.drawable.s44, "松樹", 40, R.drawable.tree5, "植物類"),
        StoreItem(R.drawable.s45, "玫瑰叢", 40, R.drawable.rosebush, "植物類"),
        StoreItem(R.drawable.s46, "櫻花樹", 40, R.drawable.tree7, "植物類"),
        StoreItem(R.drawable.s47, "銀杏樹", 40, R.drawable.tree8, "植物類"),
    )

    val filteredItems = storeItems.filter { it.category == currentCategory }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.greenbackground),
            contentDescription = "Store Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backarrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = "商 店",
                    fontSize = 28.sp,
                    color = Color(0xFF408080),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Shop 圖片，點擊進入已購買商品頁（注意路由名稱統一使用小寫開頭）
                Image(
                    painter = painterResource(id = R.drawable.shop),
                    contentDescription = "Purchased Items",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopEnd)
                        .clickable {
                            Log.d("StoreScreen", "導航到已購買商品頁面")
                            navController.navigate("purchasedItems")
                        }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF408080))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 累積分數 + 底色（稍微放大）
            Box(
                modifier = Modifier
                    .background(Color(0xFF5CADAD))
                    .padding(horizontal = 22.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "目前累積分數：${viewModel.totalScore} 分",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            // 類別選單（稍微放大）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.White.copy(alpha = 0.5f))
                    .padding(vertical = 3.dp)
            ) {
                val leftArrowColor =
                    if (currentCategoryIndex == 0) Color.LightGray else Color(0xFF408080)
                val rightArrowColor =
                    if (currentCategoryIndex == categories.lastIndex) Color.LightGray else Color(0xFF408080)

                Text(
                    text = "<",
                    fontSize = 26.sp,
                    color = leftArrowColor,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(enabled = currentCategoryIndex > 0) {
                            currentCategoryIndex--
                            showInsufficientScore = false
                            showPurchaseSuccess = false
                        }
                )

                Text(
                    text = currentCategory,
                    fontSize = 24.sp,
                    color = Color(0xFF408080)
                )

                Text(
                    text = ">",
                    fontSize = 26.sp,
                    color = rightArrowColor,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(enabled = currentCategoryIndex < categories.lastIndex) {
                            currentCategoryIndex++
                            showInsufficientScore = false
                            showPurchaseSuccess = false
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 顯示物品，允許重複購買
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
            ) {
                items(filteredItems) { item ->
                    StoreButton(
                        storeImageRes = item.storeImageRes,
                        itemName = item.itemName,
                        score = item.score,
                        onClick = {
                            Log.d("StoreScreen", "嘗試購買: ${item.itemName}, 需要分數: ${item.score}, 當前分數: ${viewModel.totalScore}")

                            if (viewModel.totalScore < item.score) {
                                showInsufficientScore = true
                                showPurchaseSuccess = false
                                Log.d("StoreScreen", "分數不足")

                                // 1秒後自動隱藏分數不足訊息
                                coroutineScope.launch {
                                    delay(1000)
                                    showInsufficientScore = false
                                }
                            } else {
                                val success = viewModel.redeemItem(item.itemName, item.score)
                                if (success) {
                                    viewModel.saveDailyChallengeToFirebase(userEmail)
                                    showInsufficientScore = false
                                    showPurchaseSuccess = true
                                    Log.d("StoreScreen", "購買成功，當前已購買: ${viewModel.redeemedItems.toList()}")

                                    // 1秒後隱藏成功訊息
                                    coroutineScope.launch {
                                        delay(1000)
                                        showPurchaseSuccess = false
                                    }
                                } else {
                                    Log.d("StoreScreen", "購買失敗")
                                }
                            }
                        }
                    )
                }
            }

            // 訊息提示區域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    showInsufficientScore -> {
                        Text(
                            text = "分數不足，無法購買！",
                            fontSize = 20.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    showPurchaseSuccess -> {
                        Text(
                            text = "購買成功！",
                            fontSize = 20.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

data class StoreItem(
    val storeImageRes: Int,
    val itemName: String,
    val score: Int,
    val forestImageRes: Int,
    val category: String
)

@Composable
fun StoreButton(
    storeImageRes: Int,
    itemName: String,
    score: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = storeImageRes),
            contentDescription = itemName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

    }
}