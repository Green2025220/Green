package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun CalculatorScreen(navController: NavController) {
    val backgroundImage = painterResource(id = R.drawable.greenback)
    val backButtonImage = painterResource(id = R.drawable.backarrow)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA0D6A1)) // 淺綠色背景
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Top,  // 讓內容從上方開始排列
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // **返回箭頭 + 標題**
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // **返回按鈕靠左**
                Image(
                    painter = painterResource(id = R.drawable.backarrow), // 確保 R.drawable.backarrow 存在
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(40.dp) // 設定返回按鈕大小
                        .align(Alignment.CenterStart) // **對齊 Box 左側**
                        .clickable { navController.popBackStack() } // 點擊返回上一頁
                )

                // **標題置中**
                Text(
                    text = "綠  森  友",
                    fontSize = 28.sp,
                    color = Color(0xFF005500), // 深綠色
                    modifier = Modifier.align(Alignment.Center) // **文字置中**
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            // **橫線**
            Box(
                modifier = Modifier
                    .fillMaxWidth() // 橫線貼齊左右
                    .height(4.dp) // 設定線條厚度
                    .background(Color(0xFF005500))
            )

            Spacer(modifier = Modifier.height(32.dp)) // 與按鈕列表的間距

            // 顯示回收地點頁面的文字
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 三個按鈕：汽油車、機車、公車
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("Car") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.5f)) // 可根據需求修改背景顏色
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically, // 垂直置中
                            horizontalArrangement = Arrangement.Center // **水平方向也置中**
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.car), // 放你的按鈕內圖片
                                contentDescription = "Car Icon",
                                modifier = Modifier
                                    .size(100.dp) // 調整圖片大小
                            )
                            Spacer(modifier = Modifier.size(16.dp)) // 增加圖片與文字間距
                            Text(
                                text = "中型汽油車",
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                        }
                    }


                    Button(
                        onClick = { navController.navigate("Motor") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically, // 垂直置中
                            horizontalArrangement = Arrangement.Center // 水平方向置中
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.scooter), // 放你的按鈕內圖片
                                contentDescription = "Scooter Icon",
                                modifier = Modifier
                                    .size(100.dp) // 調整圖片大小
                                    .weight(1f) // 確保左右佔比相等，讓圖片與文字保持對稱
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // 控制間距
                            Column(
                                modifier = Modifier.weight(1f), // 確保與圖片權重相同，保持居中
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "中型機車",
                                    fontSize = 20.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "(125-150cc)",
                                    fontSize = 14.sp, // 縮小字體
                                    color = Color.Gray // 可以根據需求改變顏色
                                )
                            }
                        }
                    }



                Button(
                        onClick = { navController.navigate("Bus") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.5f)) // 可根據需求修改背景顏色
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically, // 垂直置中
                            horizontalArrangement = Arrangement.Center // **水平方向也置中**
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bus), // 放你的按鈕內圖片
                                contentDescription = "Car Icon",
                                modifier = Modifier
                                    .size(100.dp) // 調整圖片大小
                            )
                            Spacer(modifier = Modifier.size(16.dp)) // 增加圖片與文字間距
                            Text(
                                text = "城市公車(柴油)",
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
