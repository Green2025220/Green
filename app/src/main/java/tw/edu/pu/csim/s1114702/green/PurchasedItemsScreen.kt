package tw.edu.pu.csim.s1114702.green

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

@Composable
fun PurchasedItemsScreen(navController: NavController, viewModel: ViewModel) {

    val categories = listOf("物品類", "動物類", "人物類", "植物類")
    var currentCategoryIndex by remember { mutableStateOf(0) }
    val currentCategory = categories[currentCategoryIndex]

    // 所有商品列表（與 StoreScreen 相同）
    val allStoreItems = listOf(
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
        StoreItem(R.drawable.s9, "拖拉機", 10, R.drawable.tractor, "物品類"),
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

    // 建立 Map 來記錄每個商品的購買次數
    val purchaseCountMap = remember(viewModel.redeemedItems.toList()) {
        viewModel.redeemedItems.groupingBy { it }.eachCount()
    }

    // 將購買過的商品與購買次數配對（去重）
    val purchasedItemsWithCount = remember(purchaseCountMap) {
        allStoreItems.mapNotNull { item ->
            val count = purchaseCountMap[item.itemName]
            if (count != null && count > 0) {
                PurchasedItemWithCount(item, count)
            } else {
                null
            }
        }
    }

    // 依類別篩選
    val filteredItems = purchasedItemsWithCount.filter { it.item.category == currentCategory }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.greenbackground),
            contentDescription = "Background",
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
                    text = "已購買商品",
                    fontSize = 28.sp,
                    color = Color(0xFF408080),
                    modifier = Modifier.align(Alignment.Center)
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

            // 已購買商品標題
            Box(
                modifier = Modifier
                    .background(Color(0xFF46A3FF))
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "已購買數量：${viewModel.redeemedItems.size} 件",
                    fontSize = 22.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 類別選單
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.White.copy(alpha = 0.5f))
                    .padding(vertical = 4.dp)
            ) {
                val leftArrowColor =
                    if (currentCategoryIndex == 0) Color.LightGray else Color(0xFF408080)
                val rightArrowColor =
                    if (currentCategoryIndex == categories.lastIndex) Color.LightGray else Color(0xFF408080)

                Text(
                    text = "<",
                    fontSize = 28.sp,
                    color = leftArrowColor,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(enabled = currentCategoryIndex > 0) { currentCategoryIndex-- }
                )

                Text(
                    text = currentCategory,
                    fontSize = 26.sp,
                    color = Color(0xFF408080)
                )

                Text(
                    text = ">",
                    fontSize = 28.sp,
                    color = rightArrowColor,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable(enabled = currentCategoryIndex < categories.lastIndex) { currentCategoryIndex++ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 顯示已購買商品
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "此類別尚無已購買商品",
                        fontSize = 20.sp,
                        color = Color(0xFF408080)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(filteredItems) { itemWithCount ->
                        PurchasedItemDisplay(
                            storeImageRes = itemWithCount.item.storeImageRes,
                            itemName = itemWithCount.item.itemName,
                            count = itemWithCount.count
                        )
                    }
                }
            }
        }
    }
}

// 輔助資料類別：商品 + 購買次數
data class PurchasedItemWithCount(
    val item: StoreItem,
    val count: Int
)

@Composable
fun PurchasedItemDisplay(
    storeImageRes: Int,
    itemName: String,
    count: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = storeImageRes),
            contentDescription = itemName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 如果購買次數 > 1，顯示數量角標
        if (count > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xFF408080))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "x$count",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}