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
fun StoreScreen(navController: NavController, viewModel: ViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA0D6A1)) // 淺綠色背景
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
                    .fillMaxSize()
                    .background(Color(0xFFA0D6A1))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFA0D6A1))
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
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "目前累積分數：${viewModel.totalScore} 分", fontSize = 24.sp, color = Color.Black)
                }
            }
        }
    }
}