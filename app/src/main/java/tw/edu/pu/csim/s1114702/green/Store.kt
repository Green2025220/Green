package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreButton(
                    imageRes = R.drawable.watering,
                    name = "澆水器",
                    score = 5,
                    onClick = {
                        if (viewModel.redeemItem("澆水器", 5)) {
                            viewModel.updateTotalScore(viewModel.totalScore)
                            viewModel.saveDailyChallengeToFirebase(userEmail)
                        }
                    }
                )
                StoreButton(
                    imageRes = R.drawable.scissors,
                    name = "剪刀",
                    score = 3,
                    onClick = {
                        if (viewModel.redeemItem("剪刀", 3)) {
                            viewModel.updateTotalScore(viewModel.totalScore)
                            viewModel.saveDailyChallengeToFirebase(userEmail)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StoreButton(imageRes: Int, name: String, score: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(150.dp)
            .clickable { onClick() }
            .background(Color(0xFFDDEEDD))
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
        Text(text = name, fontSize = 20.sp, color = Color.Black)
        Text(text = "${score}分", fontSize = 18.sp, color = Color.Black)
    }
}
