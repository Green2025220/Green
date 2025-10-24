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
        StoreItem(R.drawable.s14, "剪刀", 20, R.drawable.scissors),
        StoreItem(R.drawable.s12, "三叉", 30, R.drawable.rake),
        StoreItem(R.drawable.s13, "鏟子", 10, R.drawable.shovel),
        StoreItem(R.drawable.s39, "青楓", 30, R.drawable.tree1),
        StoreItem(R.drawable.s3, "牧野氏山芙蓉", 30, R.drawable.tree2),
        StoreItem(R.drawable.s37, "台灣牛樟", 30, R.drawable.tree3),
        StoreItem(R.drawable.s40, "截萼黃槿", 30, R.drawable.tree4),

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