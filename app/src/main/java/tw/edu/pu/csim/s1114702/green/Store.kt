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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StoreScreen(navController: NavController, viewModel: ViewModel, userEmail: String) {
    val storeItems = listOf(
        Triple(R.drawable.watering, "澆水器", 5),
        Triple(R.drawable.scissors, "剪刀", 3),
        Triple(R.drawable.rake, "三叉", 8),
        Triple(R.drawable.shovel, "鏟子", 8),
        Triple(R.drawable.tree1, "青楓", 30),
        Triple(R.drawable.tree2, "牧野氏山芙蓉", 30),
        Triple(R.drawable.tree3, "台灣牛樟", 30),
        Triple(R.drawable.tree4, "截萼黃槿", 30),

        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA0D6A1))
    ) {
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
                    val isRedeemed = viewModel.redeemedItems.contains(item.second)

                    StoreButton(
                        imageRes = item.first,
                        name = item.second,
                        score = item.third,
                        isRedeemed = isRedeemed,
                        onClick = {
                            if (!isRedeemed && viewModel.redeemItem(item.second, item.third)) {
//                                viewModel.updateTotalScore(viewModel.totalScore)
                                viewModel.saveDailyChallengeToFirebase(userEmail)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StoreButton(
    imageRes: Int,
    name: String,
    score: Int,
    isRedeemed: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isRedeemed) Color.LightGray else Color(0xFFDDEEDD)
    val textColor = if (isRedeemed) Color.DarkGray else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .then(if (!isRedeemed) Modifier.clickable { onClick() } else Modifier)
            .background(bgColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = name,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = name, fontSize = 20.sp, color = textColor)
        Text(text = "${score}分", fontSize = 18.sp, color = textColor)

        if (isRedeemed) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "已兌換", fontSize = 16.sp, color = Color.Red)
        }
    }
}