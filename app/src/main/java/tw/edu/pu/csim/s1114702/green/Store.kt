package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StoreScreen(navController: NavController, viewModel: ViewModel, userEmail: String) {
    val storeItems = listOf(
        StoreItem(R.drawable.s1, "澆水器", 10, R.drawable.watering),
        StoreItem(R.drawable.s4, "柵欄1", 10, R.drawable.fence1),
        StoreItem(R.drawable.s5, "柵欄2", 10, R.drawable.fence2),
        StoreItem(R.drawable.s6, "柵欄3", 10, R.drawable.fence3),
        StoreItem(R.drawable.s7, "柵欄4", 10, R.drawable.fence4),
        StoreItem(R.drawable.s8, "太陽", 10, R.drawable.sun),
        StoreItem(R.drawable.s9, "拖拉機", 30, R.drawable.tractor),
        StoreItem(R.drawable.s10, "風車", 10, R.drawable.windmill),
        StoreItem(R.drawable.s11, "帽子", 10, R.drawable.hat),
        StoreItem(R.drawable.s14, "剪刀", 10, R.drawable.scissors),
        StoreItem(R.drawable.s12, "三叉", 10, R.drawable.rake),
        StoreItem(R.drawable.s13, "鏟子", 10, R.drawable.shovel),
        StoreItem(R.drawable.s15, "小苒", 30, R.drawable.girl1),
        StoreItem(R.drawable.s16, "小薇", 30, R.drawable.girl2),
        StoreItem(R.drawable.s17, "小浩", 30, R.drawable.boy1),
        StoreItem(R.drawable.s2, "農夫", 30, R.drawable.boy2),
        StoreItem(R.drawable.s18, "狐狸", 20, R.drawable.fox),
        StoreItem(R.drawable.s19, "兔子", 20, R.drawable.rabbit),
        StoreItem(R.drawable.s20, "蝴蝶", 20, R.drawable.butterfly),
        StoreItem(R.drawable.s21, "黃雛菊", 40, R.drawable.yellowdaisy),
        StoreItem(R.drawable.s22, "台灣火刺木", 40, R.drawable.tree9),
        StoreItem(R.drawable.s23, "白雛菊", 40, R.drawable.twoflower),
        StoreItem(R.drawable.s24, "香雪蘭", 40, R.drawable.tulip),
        StoreItem(R.drawable.s25, "向日葵", 40, R.drawable.sunflower),
        StoreItem(R.drawable.s26, "鬱金香", 40, R.drawable.flower2),
        StoreItem(R.drawable.s27, "法國菊", 40, R.drawable.oneflower),
        StoreItem(R.drawable.s28, "松樹牛肝菌", 40, R.drawable.mushroom2),
        StoreItem(R.drawable.s29, "山紅菇", 40, R.drawable.mushroom1),
        StoreItem(R.drawable.s30, "鈴蘭", 40, R.drawable.lilyofthevalley),
        StoreItem(R.drawable.s31, "薰衣草", 40, R.drawable.lavender),
        StoreItem(R.drawable.s32, "風信子", 40, R.drawable.hyacinth),
        StoreItem(R.drawable.s33, "玉米", 40, R.drawable.corn),
        StoreItem(R.drawable.s34, "樹苗", 40, R.drawable.bud),
        StoreItem(R.drawable.s35, "種子", 40, R.drawable.seed35),
        StoreItem(R.drawable.s36, "粉色蘭花", 40, R.drawable.flower1),
        StoreItem(R.drawable.s37, "台灣牛樟", 40, R.drawable.tree3),
        StoreItem(R.drawable.s38, "花圃", 40, R.drawable.flower38),
        StoreItem(R.drawable.s39, "青楓", 40, R.drawable.tree1),
        StoreItem(R.drawable.s3, "牧野氏山芙蓉", 40, R.drawable.tree2),
        StoreItem(R.drawable.s40, "截萼黃槿", 40, R.drawable.tree4),
        StoreItem(R.drawable.s41, "台灣欒樹", 40, R.drawable.tree10),
        StoreItem(R.drawable.s42, "蘋果樹", 40, R.drawable.tree11),
        StoreItem(R.drawable.s43, "綠樹", 40, R.drawable.tree6),
        StoreItem(R.drawable.s44, "松樹", 40, R.drawable.tree5),
        StoreItem(R.drawable.s45, "玫瑰叢", 40, R.drawable.rosebush),
        StoreItem(R.drawable.s46, "櫻花樹", 40, R.drawable.tree7),
        StoreItem(R.drawable.s47, "銀杏樹", 40, R.drawable.tree8),

        )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // 背景圖片
        Image(
            painter = painterResource(id = R.drawable.storebg),
            contentDescription = "Store Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop  // 或使用 ContentScale.FillBounds
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
                    color = Color(0xFF005500),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF005500))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "目前累積分數：${viewModel.totalScore} 分",
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid of Store Buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                items(storeItems) { item ->
                    val isRedeemed = viewModel.redeemedItems.contains(item.itemName)

                    StoreButton(
                        storeImageRes = item.storeImageRes,
                        itemName = item.itemName,
                        score = item.score,
                        isRedeemed = isRedeemed,
                        onClick = {
                            if (!isRedeemed && viewModel.redeemItem(item.itemName, item.score)) {
                                viewModel.saveDailyChallengeToFirebase(userEmail)
                            }
                        }
                    )
                }
            }
        }
    }
}

// 數據類：儲存商店物品訊息
data class StoreItem(
    val storeImageRes: Int,      // 商店顯示的新圖片 (s1.png, s2.png, etc.)
    val itemName: String,         // 物品名稱（用於 Myforest 連接）
    val score: Int,               // 兌換所需分數
    val forestImageRes: Int       // Myforest 中使用的原圖片
)

@Composable
fun StoreButton(
    storeImageRes: Int,
    itemName: String,
    score: Int,
    isRedeemed: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)  // 保持正方形比例
            .then(if (!isRedeemed) Modifier.clickable { onClick() } else Modifier)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // 顯示商店圖片（已包含名稱和價格）
        Image(
            painter = painterResource(id = storeImageRes),
            contentDescription = itemName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 如果已兌換，顯示半透明遮罩和文字
        if (isRedeemed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "已兌換",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }
    }
}