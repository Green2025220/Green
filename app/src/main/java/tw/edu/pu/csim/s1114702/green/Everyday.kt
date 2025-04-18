package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun EverydayScreen(navController: NavController, viewModel: ViewModel) {
    val checklistItems = viewModel.checklistItems
    val checkStates = viewModel.checkStates
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 是否已完成今日挑戰（由 ViewModel 控制邏輯會更準確）
    var isCompleted by remember { mutableStateOf(viewModel.hasCompletedToday()) }

    LaunchedEffect(Unit) {
        viewModel.checkAndResetDaily()
        isCompleted = viewModel.hasCompletedToday()
    }

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
            // 標題列
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
                    text = "每日綠色挑戰",
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

        // 勾選清單內容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp, bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            checklistItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkStates[index],
                        onCheckedChange = { checkStates[index] = it }
                    )
                    Text(
                        text = item,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF005500))
                )
            }
        }

        // 底部「完成挑戰」按鈕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (!isCompleted) {
                        viewModel.calculateDailyScore()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("今日挑戰已完成，分數已加總！")
                        }
                        isCompleted = true
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("今日已完成過挑戰！")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isCompleted
            ) {
                Text("完成挑戰", fontSize = 20.sp)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
