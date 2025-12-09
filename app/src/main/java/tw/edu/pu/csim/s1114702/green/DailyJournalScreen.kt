package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyJournalScreen(
    navController: NavController,
    viewModel: ViewModel,
    userEmail: String
) {
    // 載入所有遊戲數據
    LaunchedEffect(Unit) {
        if (userEmail.isNotEmpty()) {
            viewModel.loadDailyChallengeFromFirebase(userEmail)
            viewModel.loadCarbonCalculatorDateFromFirebase(userEmail)
            viewModel.loadGarbageDataFromFirebase(userEmail)
            viewModel.loadQuizGameDataFromFirebase(userEmail)
            viewModel.loadTurnGameDataFromFirebase(userEmail)
            viewModel.loadGarbageGameDataFromFirebase(userEmail)
        }
    }

    // 取得今日日期
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val todayString = today.format(dateFormatter)

    // 計算各項目完成狀態
    val forestGameStatus = getForestGameStatus(viewModel)
    val carbonCalculatorStatus = getCarbonCalculatorStatus(viewModel)
    val garbageClassifyStatus = getGarbageClassifyStatus(viewModel)

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片
        Image(
            painter = painterResource(id = R.drawable.homepage2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 標題列
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Image(
                        painter = painterResource(id = R.drawable.backarrow),
                        contentDescription = "返回",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "每日任務日記",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005500)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日期與總分卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = todayString,
                        fontSize = 18.sp,
                        color = Color(0xFF336666),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "當前總分",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${viewModel.totalScore}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2CA673)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 森林闖關區塊
            TaskCategoryCard(
                title = "🌲 森林闖關",
                totalTasks = 3,
                completedTasks = forestGameStatus.completed,
                onNavigate = { navController.navigate("game") }
            ) {
                TaskItem(
                    name = "永續挑戰",
                    status = forestGameStatus.quizStatus,
                    remainingTimes = forestGameStatus.quizRemaining,
                    maxTimes = 1,
                    onClick = { navController.navigate("Game1") }
                )
                TaskItem(
                    name = "Turn 翻牌",
                    status = forestGameStatus.turnStatus,
                    remainingTimes = forestGameStatus.turnRemaining,
                    maxTimes = 3,
                    onClick = { navController.navigate("turn") }
                )
                TaskItem(
                    name = "垃圾分類挑戰",
                    status = forestGameStatus.garbageGameStatus,
                    remainingTimes = forestGameStatus.garbageGameRemaining,
                    maxTimes = 3,
                    onClick = { navController.navigate("Garbagegame") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 碳排放計算區塊
            TaskCategoryCard(
                title = "🚗 碳排放計算",
                totalTasks = 3,
                completedTasks = carbonCalculatorStatus.completed,
                onNavigate = { navController.navigate("calculator") }
            ) {
                TaskItem(
                    name = "汽車 (5分)",
                    status = carbonCalculatorStatus.carStatus,
                    onClick = { navController.navigate("car") }
                )
                TaskItem(
                    name = "機車 (3分)",
                    status = carbonCalculatorStatus.motorStatus,
                    onClick = { navController.navigate("motor") }
                )
                TaskItem(
                    name = "公車 (10分)",
                    status = carbonCalculatorStatus.busStatus,
                    onClick = { navController.navigate("bus") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 一拍即分區塊
            TaskCategoryCard(
                title = "📸 一拍即分",
                totalTasks = 1,
                completedTasks = if (garbageClassifyStatus.isCompleted) 1 else 0,
                onNavigate = { navController.navigate("garbage") }
            ) {
                TaskItem(
                    name = "垃圾辨識",
                    status = garbageClassifyStatus.status,
                    remainingTimes = garbageClassifyStatus.remaining,
                    maxTimes = 3,
                    onClick = { navController.navigate("garbage") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 統計資訊
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 今日統計",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val totalCompleted = forestGameStatus.completed +
                            carbonCalculatorStatus.completed +
                            if (garbageClassifyStatus.isCompleted) 1 else 0
                    val totalTasks = 7

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("已完成任務", color = Color(0xFF555555))
                        Text(
                            "$totalCompleted / $totalTasks",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2CA673)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = totalCompleted.toFloat() / totalTasks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2CA673),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    if (totalCompleted == totalTasks) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "🎉 太棒了！今日所有任務都已完成！",
                            fontSize = 14.sp,
                            color = Color(0xFF2CA673),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TaskCategoryCard(
    title: String,
    totalTasks: Int,
    completedTasks: Int,
    onNavigate: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005500)
                )
                Surface(
                    shape = CircleShape,
                    color = if (completedTasks == totalTasks) Color(0xFF2CA673) else Color(0xFFFF9800)
                ) {
                    Text(
                        text = "$completedTasks/$totalTasks",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun TaskItem(
    name: String,
    status: String,
    remainingTimes: Int? = null,
    maxTimes: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when {
                status.contains("已完成") || status.contains("✓") -> "✓"
                status.contains("未完成") -> "○"
                else -> "○"
            }
            val iconColor = when {
                status.contains("已完成") || status.contains("✓") -> Color(0xFF2CA673)
                else -> Color.Gray
            }

            Text(
                text = icon,
                fontSize = 20.sp,
                color = iconColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                if (remainingTimes != null && maxTimes != null) {
                    Text(
                        text = "剩餘 $remainingTimes/$maxTimes 次",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Text(
            text = status,
            fontSize = 14.sp,
            color = when {
                status.contains("已完成") || status.contains("✓") -> Color(0xFF2CA673)
                else -> Color(0xFFFF9800)
            },
            fontWeight = FontWeight.Medium
        )
    }

    if (remainingTimes != null && remainingTimes < (maxTimes ?: 0)) {
        Divider(
            modifier = Modifier.padding(start = 32.dp),
            color = Color(0xFFE0E0E0)
        )
    }
}

// 資料類別
data class ForestGameStatus(
    val quizStatus: String,
    val quizRemaining: Int,
    val turnStatus: String,
    val turnRemaining: Int,
    val garbageGameStatus: String,
    val garbageGameRemaining: Int,
    val completed: Int
)

data class CarbonCalculatorStatus(
    val carStatus: String,
    val motorStatus: String,
    val busStatus: String,
    val completed: Int
)

data class GarbageClassifyStatus(
    val status: String,
    val remaining: Int,
    val isCompleted: Boolean
)

// 輔助函數
fun getForestGameStatus(viewModel: ViewModel): ForestGameStatus {
    val quizRemaining = viewModel.getRemainingQuizGamePlays(1)
    val turnRemaining = viewModel.getRemainingTurnGamePlays(3)
    val garbageGameRemaining = viewModel.getRemainingGarbageGamePlays(3)

    val quizStatus = if (quizRemaining == 0) "✓ 已完成" else "未完成"
    val turnStatus = if (turnRemaining == 0) "✓ 已完成" else "未完成"
    val garbageGameStatus = if (garbageGameRemaining == 0) "✓ 已完成" else "未完成"

    val completed = listOf(quizRemaining, turnRemaining, garbageGameRemaining).count { it == 0 }

    return ForestGameStatus(
        quizStatus = quizStatus,
        quizRemaining = quizRemaining,
        turnStatus = turnStatus,
        turnRemaining = turnRemaining,
        garbageGameStatus = garbageGameStatus,
        garbageGameRemaining = garbageGameRemaining,
        completed = completed
    )
}

fun getCarbonCalculatorStatus(viewModel: ViewModel): CarbonCalculatorStatus {
    val carDone = !viewModel.canGetCarCalculatorReward()
    val motorDone = !viewModel.canGetMotorCalculatorReward()
    val busDone = !viewModel.canGetBusCalculatorReward()

    val carStatus = if (carDone) "✓ 已完成" else "未完成"
    val motorStatus = if (motorDone) "✓ 已完成" else "未完成"
    val busStatus = if (busDone) "✓ 已完成" else "未完成"

    val completed = listOf(carDone, motorDone, busDone).count { it }

    return CarbonCalculatorStatus(
        carStatus = carStatus,
        motorStatus = motorStatus,
        busStatus = busStatus,
        completed = completed
    )
}

fun getGarbageClassifyStatus(viewModel: ViewModel): GarbageClassifyStatus {
    val remaining = viewModel.getRemainingGarbageRewards()
    val isCompleted = remaining == 0

    return GarbageClassifyStatus(
        status = if (isCompleted) "✓ 已完成" else "未完成",
        remaining = remaining,
        isCompleted = isCompleted
    )
}