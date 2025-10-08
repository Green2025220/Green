package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun GarbageGameScreen(navController: NavController) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(60) }
    var currentTrash by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    // 拖曳相關狀態
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    // 垃圾桶位置
    var generalBinPosition by remember { mutableStateOf(Offset.Zero) }
    var generalBinSize by remember { mutableStateOf(IntSize.Zero) }
    var recycleBinPosition by remember { mutableStateOf(Offset.Zero) }
    var recycleBinSize by remember { mutableStateOf(IntSize.Zero) }

    // 垃圾清單（不包含垃圾桶）
    val trashList = listOf(
        Pair(R.drawable.g_bamboochopsticks, false), // 一般垃圾
        Pair(R.drawable.g_bill, false),              // 一般垃圾
        Pair(R.drawable.g_brokenglass, false),       // 一般垃圾
        Pair(R.drawable.r_aluminumcan, true)         // 可回收垃圾
    )

    // 初始化垃圾
    LaunchedEffect(Unit) {
        currentTrash = trashList.random()
    }

    // 倒數計時
    LaunchedEffect(isGameOver, timeLeft) {
        if (!isGameOver && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft <= 0) {
            isGameOver = true
        }
    }

    // 檢查是否拖曳到垃圾桶範圍內
    fun isInBinArea(binPos: Offset, binSize: IntSize, itemPos: Offset): Boolean {
        return itemPos.x >= binPos.x &&
                itemPos.x <= binPos.x + binSize.width &&
                itemPos.y >= binPos.y &&
                itemPos.y <= binPos.y + binSize.height
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF7E1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 分數和時間顯示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("分數: $score", fontSize = 24.sp, color = Color.Black)
                Text("時間: $timeLeft", fontSize = 24.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!isGameOver) {
                // 垃圾桶區域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 一般垃圾桶
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("一般垃圾", fontSize = 18.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.gtrash),
                            contentDescription = "一般垃圾桶",
                            modifier = Modifier
                                .size(120.dp)
                                .onGloballyPositioned { coordinates ->
                                    generalBinPosition = coordinates.positionInRoot()
                                    generalBinSize = coordinates.size
                                }
                        )
                    }

                    // 回收垃圾桶
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("回收垃圾", fontSize = 18.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.recyclebin),
                            contentDescription = "回收垃圾桶",
                            modifier = Modifier
                                .size(120.dp)
                                .onGloballyPositioned { coordinates ->
                                    recycleBinPosition = coordinates.positionInRoot()
                                    recycleBinSize = coordinates.size
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 可拖曳的垃圾
                currentTrash?.let { trash ->
                    var trashPosition by remember { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = trash.first),
                            contentDescription = null,
                            modifier = Modifier
                                .size(220.dp)  // 放大垃圾圖片
                                .graphicsLayer(
                                    translationX = dragOffset.x,
                                    translationY = dragOffset.y
                                )
                                .onGloballyPositioned { coordinates ->
                                    trashPosition = coordinates.positionInRoot()
                                }
                                .pointerInput(trash.first) {  // 使用 trash.first 作為 key
                                    detectDragGestures(
                                        onDragStart = {
                                            isDragging = true
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                        },
                                        onDragEnd = {
                                            isDragging = false

                                            // 計算垃圾的實際屏幕位置（中心點）
                                            val itemCenterX = trashPosition.x + dragOffset.x + (220.dp.toPx() / 2)
                                            val itemCenterY = trashPosition.y + dragOffset.y + (220.dp.toPx() / 2)

                                            // 檢查是否與一般垃圾桶重疊
                                            val inGeneralBin = itemCenterX >= generalBinPosition.x &&
                                                    itemCenterX <= generalBinPosition.x + generalBinSize.width &&
                                                    itemCenterY >= generalBinPosition.y &&
                                                    itemCenterY <= generalBinPosition.y + generalBinSize.height

                                            // 檢查是否與回收桶重疊
                                            val inRecycleBin = itemCenterX >= recycleBinPosition.x &&
                                                    itemCenterX <= recycleBinPosition.x + recycleBinSize.width &&
                                                    itemCenterY >= recycleBinPosition.y &&
                                                    itemCenterY <= recycleBinPosition.y + recycleBinSize.height

                                            when {
                                                inGeneralBin && !trash.second -> {
                                                    // 正確丟入一般垃圾桶
                                                    score += 10
                                                    currentTrash = trashList.random()
                                                }
                                                inRecycleBin && trash.second -> {
                                                    // 正確丟入回收桶
                                                    score += 10
                                                    currentTrash = trashList.random()
                                                }
                                                inGeneralBin || inRecycleBin -> {
                                                    // 丟錯垃圾桶
                                                    score -= 5
                                                    currentTrash = trashList.random()
                                                }
                                            }

                                            // 重置位置
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "拖曳垃圾到正確的垃圾桶！",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(40.dp))

            } else {
                // 遊戲結束畫面
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "遊戲結束！",
                    fontSize = 32.sp,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "你的分數: $score",
                    fontSize = 28.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(onClick = {
                    isGameOver = false
                    score = 0
                    timeLeft = 60
                    dragOffset = Offset.Zero
                    currentTrash = trashList.random()
                }) {
                    Text("重新開始", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    navController.popBackStack()
                }) {
                    Text("返回主選單", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}