package tw.edu.pu.csim.s1114702.green


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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


// 答題記錄資料類別
data class AnswerRecord(
    val trashImage: Int,
    val isRecyclable: Boolean,
    val userAnswer: Boolean,
    val isCorrect: Boolean
)


@Composable
fun GarbageGameScreen(
    navController: NavController,
    viewModel: ViewModel,
    userEmail: String
) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var currentTrash by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    // 記錄本次遊戲是否已經將分數加入總分
    var hasAddedScore by remember { mutableStateOf(false) }

    // 答題記錄
    var answerRecords by remember { mutableStateOf<List<AnswerRecord>>(emptyList()) }
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }


    var trashPool by remember { mutableStateOf<List<Pair<Int, Boolean>>>(emptyList()) }


    // 拖曳相關狀態
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }


    // 答題反饋狀態
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackColor by remember { mutableStateOf(Color.Green) }
    var scoreChange by remember { mutableStateOf("") }
    var showScoreChange by remember { mutableStateOf(false) }


    // 垃圾桶位置
    var generalBinPosition by remember { mutableStateOf(Offset.Zero) }
    var generalBinSize by remember { mutableStateOf(IntSize.Zero) }
    var recycleBinPosition by remember { mutableStateOf(Offset.Zero) }
    var recycleBinSize by remember { mutableStateOf(IntSize.Zero) }


    // 垃圾清單（64個項目）
    val trashList = listOf(
        // 一般垃圾 (31個)
        Pair(R.drawable.g_bamboochopsticks, false),
        Pair(R.drawable.g_bill, false),
        Pair(R.drawable.g_brokenglass, false),
        Pair(R.drawable.g_bubblewrap, false),
        Pair(R.drawable.g_candle, false),
        Pair(R.drawable.g_comb, false),
        Pair(R.drawable.g_cottonswab, false),
        Pair(R.drawable.g_courierbag, false),
        Pair(R.drawable.g_dirtyplasticbag, false),
        Pair(R.drawable.g_doll, false),
        Pair(R.drawable.g_emptymakeupjar, false),
        Pair(R.drawable.g_filter, false),
        Pair(R.drawable.g_flossstick, false),
        Pair(R.drawable.g_helmet, false),
        Pair(R.drawable.g_instantnoodlebowl, false),
        Pair(R.drawable.g_lunchbox, false),
        Pair(R.drawable.g_mask, false),
        Pair(R.drawable.g_mirror, false),
        Pair(R.drawable.g_nonwovenbag, false),
        Pair(R.drawable.g_oldshoe, false),
        Pair(R.drawable.g_penrefills, false),
        Pair(R.drawable.g_phonecase, false),
        Pair(R.drawable.g_pill, false),
        Pair(R.drawable.g_pillow, false),
        Pair(R.drawable.g_plasticbag, false),
        Pair(R.drawable.g_rubberband, false),
        Pair(R.drawable.g_toiletpapergarbage, false),
        Pair(R.drawable.g_toothbrush, false),
        Pair(R.drawable.g_toycar, false),
        Pair(R.drawable.g_umbrellacloth, false),
        Pair(R.drawable.g_usedstraw, false),
        // 可回收垃圾 (33個)
        Pair(R.drawable.r_aluminumcan, true),
        Pair(R.drawable.r_battery, true),
        Pair(R.drawable.r_billenvelope, true),
        Pair(R.drawable.r_bottlecap, true),
        Pair(R.drawable.r_bulb, true),
        Pair(R.drawable.r_can, true),
        Pair(R.drawable.r_carton, true),
        Pair(R.drawable.r_clothing, true),
        Pair(R.drawable.r_cupcarrier, true),
        Pair(R.drawable.r_disc, true),
        Pair(R.drawable.r_emptytoner, true),
        Pair(R.drawable.r_epefoam, true),
        Pair(R.drawable.r_frame, true),
        Pair(R.drawable.r_fruitnet, true),
        Pair(R.drawable.r_glassbottle, true),
        Pair(R.drawable.r_lighter, true),
        Pair(R.drawable.r_magazine, true),
        Pair(R.drawable.r_milkcarton, true),
        Pair(R.drawable.r_mouse, true),
        Pair(R.drawable.r_oldphone, true),
        Pair(R.drawable.r_plasticcup, true),
        Pair(R.drawable.r_plasticspoon, true),
        Pair(R.drawable.r_pot, true),
        Pair(R.drawable.r_screwnails, true),
        Pair(R.drawable.r_shoebox, true),
        Pair(R.drawable.r_staples, true),
        Pair(R.drawable.r_steamcooker, true),
        Pair(R.drawable.r_styrofoam, true),
        Pair(R.drawable.r_toiletpaperpackaging, true),
        Pair(R.drawable.r_tornraincoat, true),
        Pair(R.drawable.r_wiredheadphones, true),
        Pair(R.drawable.r_yogamat, true),
        Pair(R.drawable.r_zipperbag, true)
    )


    // 取得垃圾名稱
    fun getTrashName(imageId: Int): String {
        return when (imageId) {
            // 一般垃圾
            R.drawable.g_bamboochopsticks -> "竹筷"
            R.drawable.g_bill -> "發票"
            R.drawable.g_brokenglass -> "破碎玻璃"
            R.drawable.g_bubblewrap -> "泡泡紙"
            R.drawable.g_candle -> "蠟燭"
            R.drawable.g_comb -> "梳子"
            R.drawable.g_cottonswab -> "棉花棒"
            R.drawable.g_courierbag -> "破壞袋"
            R.drawable.g_dirtyplasticbag -> "髒塑膠袋"
            R.drawable.g_doll -> "娃娃"
            R.drawable.g_emptymakeupjar -> "用完的口紅"
            R.drawable.g_filter -> "過濾器"
            R.drawable.g_flossstick -> "牙線棒"
            R.drawable.g_helmet -> "安全帽"
            R.drawable.g_instantnoodlebowl -> "髒泡麵碗"
            R.drawable.g_lunchbox -> "木便當盒"
            R.drawable.g_mask -> "口罩"
            R.drawable.g_mirror -> "鏡子"
            R.drawable.g_nonwovenbag -> "不織布袋"
            R.drawable.g_oldshoe -> "舊鞋子"
            R.drawable.g_penrefills -> "筆芯"
            R.drawable.g_phonecase -> "手機殼"
            R.drawable.g_pill -> "藥丸"
            R.drawable.g_pillow -> "枕頭"
            R.drawable.g_plasticbag -> "塑膠袋"
            R.drawable.g_rubberband -> "橡皮筋"
            R.drawable.g_toiletpapergarbage -> "衛生紙"
            R.drawable.g_toothbrush -> "牙刷"
            R.drawable.g_toycar -> "玩具車"
            R.drawable.g_umbrellacloth -> "雨傘布"
            R.drawable.g_usedstraw -> "用過的吸管"
            // 可回收垃圾
            R.drawable.r_aluminumcan -> "鋁罐"
            R.drawable.r_battery -> "電池"
            R.drawable.r_billenvelope -> "信封袋"
            R.drawable.r_bottlecap -> "瓶蓋"
            R.drawable.r_bulb -> "燈泡"
            R.drawable.r_can -> "罐頭"
            R.drawable.r_carton -> "紙盒"
            R.drawable.r_clothing -> "衣服"
            R.drawable.r_cupcarrier -> "杯架"
            R.drawable.r_disc -> "光碟"
            R.drawable.r_emptytoner -> "空玻璃化妝瓶"
            R.drawable.r_epefoam -> "EPE泡棉"
            R.drawable.r_frame -> "雨傘架"
            R.drawable.r_fruitnet -> "水果網"
            R.drawable.r_glassbottle -> "玻璃瓶"
            R.drawable.r_lighter -> "打火機"
            R.drawable.r_magazine -> "雜誌"
            R.drawable.r_milkcarton -> "牛奶盒"
            R.drawable.r_mouse -> "滑鼠"
            R.drawable.r_oldphone -> "舊手機"
            R.drawable.r_plasticcup -> "塑膠杯"
            R.drawable.r_plasticspoon -> "塑膠湯匙"
            R.drawable.r_pot -> "鍋子"
            R.drawable.r_screwnails -> "螺絲釘"
            R.drawable.r_shoebox -> "鞋盒"
            R.drawable.r_staples -> "釘書針"
            R.drawable.r_steamcooker -> "蒸鍋"
            R.drawable.r_styrofoam -> "保麗龍"
            R.drawable.r_toiletpaperpackaging -> "衛生紙包裝"
            R.drawable.r_tornraincoat -> "破雨衣"
            R.drawable.r_wiredheadphones -> "有線耳機"
            R.drawable.r_yogamat -> "瑜珈墊"
            R.drawable.r_zipperbag -> "夾鏈袋"
            else -> "未知垃圾"
        }
    }


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


    // 反饋訊息自動消失
    LaunchedEffect(showFeedback) {
        if (showFeedback) {
            delay(1000L)
            showFeedback = false
        }
    }


    // 分數變化動畫自動消失
    LaunchedEffect(showScoreChange) {
        if (showScoreChange) {
            delay(800L)
            showScoreChange = false
        }
    }

    // 在遊戲結束時，將分數加入 ViewModel 的總分
    LaunchedEffect(isGameOver) {
        if (isGameOver && !hasAddedScore && score > 0) {
            viewModel.updateTotalScore(score)
            viewModel.saveDailyChallengeToFirebase(userEmail)
            hasAddedScore = true
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isGameOver) Color(0xFF004B97) else Color(0xFFEAF7E1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isGameOver) {
                // 分數和時間顯示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 分數顯示區（帶分數變化動畫）
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("分數: $score", fontSize = 24.sp, color = Color.Black)
                        if (showScoreChange) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                scoreChange,
                                fontSize = 20.sp,
                                color = if (scoreChange.startsWith("+")) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                    Text("時間: $timeLeft", fontSize = 24.sp, color = Color.Black)
                }


                Spacer(modifier = Modifier.height(40.dp))


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
                        // 顯示垃圾圖片或答題反饋
                        if (showFeedback) {
                            // 顯示答題反饋（取代垃圾圖片的位置）
                            Text(
                                feedbackMessage,
                                fontSize = 36.sp,
                                color = feedbackColor
                            )
                        } else {
                            // 顯示可拖曳的垃圾
                            Image(
                                painter = painterResource(id = trash.first),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(220.dp)
                                    .graphicsLayer(
                                        translationX = dragOffset.x,
                                        translationY = dragOffset.y
                                    )
                                    .onGloballyPositioned { coordinates ->
                                        trashPosition = coordinates.positionInRoot()
                                    }
                                    .pointerInput(trash.first) {
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
                                                        correctCount++
                                                        answerRecords = answerRecords + AnswerRecord(
                                                            trashImage = trash.first,
                                                            isRecyclable = trash.second,
                                                            userAnswer = false,
                                                            isCorrect = true
                                                        )
                                                        scoreChange = "+10"
                                                        showScoreChange = true
                                                        feedbackMessage = "✓ 答對了！"
                                                        feedbackColor = Color(0xFF4CAF50)
                                                        showFeedback = true
                                                        currentTrash = trashList.random()
                                                    }
                                                    inRecycleBin && trash.second -> {
                                                        // 正確丟入回收桶
                                                        score += 10
                                                        correctCount++
                                                        answerRecords = answerRecords + AnswerRecord(
                                                            trashImage = trash.first,
                                                            isRecyclable = trash.second,
                                                            userAnswer = true,
                                                            isCorrect = true
                                                        )
                                                        scoreChange = "+10"
                                                        showScoreChange = true
                                                        feedbackMessage = "✓ 答對了！"
                                                        feedbackColor = Color(0xFF4CAF50)
                                                        showFeedback = true
                                                        currentTrash = trashList.random()
                                                    }
                                                    inGeneralBin && trash.second -> {
                                                        // 應該回收卻丟一般垃圾桶
                                                        score -= 5
                                                        wrongCount++
                                                        answerRecords = answerRecords + AnswerRecord(
                                                            trashImage = trash.first,
                                                            isRecyclable = trash.second,
                                                            userAnswer = false,
                                                            isCorrect = false
                                                        )
                                                        scoreChange = "-5"
                                                        showScoreChange = true
                                                        feedbackMessage = "✗ 答錯了！"
                                                        feedbackColor = Color(0xFFF44336)
                                                        showFeedback = true
                                                        currentTrash = trashList.random()
                                                    }
                                                    inRecycleBin && !trash.second -> {
                                                        // 應該一般垃圾卻丟回收桶
                                                        score -= 5
                                                        wrongCount++
                                                        answerRecords = answerRecords + AnswerRecord(
                                                            trashImage = trash.first,
                                                            isRecyclable = trash.second,
                                                            userAnswer = true,
                                                            isCorrect = false
                                                        )
                                                        scoreChange = "-5"
                                                        showScoreChange = true
                                                        feedbackMessage = "✗ 答錯了！"
                                                        feedbackColor = Color(0xFFF44336)
                                                        showFeedback = true
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
                }


                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "拖曳垃圾到正確的垃圾桶！",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(40.dp))


            } else {
                // 遊戲結束畫面 - 可滾動的結算頁面
                val scrollState = rememberScrollState()
                val correctRecords = answerRecords.filter { it.isCorrect }
                val wrongRecords = answerRecords.filter { !it.isCorrect }


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))


                    Text(
                        "遊戲結束！",
                        fontSize = 32.sp,
                        color = Color.White
                    )


                    Spacer(modifier = Modifier.height(24.dp))


                    // 總分統計卡片
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("總分", fontSize = 20.sp, color = Color.Gray)
                            Text("$score", fontSize = 48.sp, color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("正確", fontSize = 18.sp, color = Color(0xFF4CAF50))
                                    Text("$correctCount", fontSize = 28.sp, color = Color(0xFF4CAF50))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("錯誤", fontSize = 18.sp, color = Color(0xFFF44336))
                                    Text("$wrongCount", fontSize = 28.sp, color = Color(0xFFF44336))
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))


                    // 正確題目區域
                    if (correctRecords.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    "✓ 答對的題目 (${correctRecords.size})",
                                    fontSize = 22.sp,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(12.dp))


                                correctRecords.forEachIndexed { index, record ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = record.trashImage),
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                getTrashName(record.trashImage),
                                                fontSize = 18.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                "正解: ${if (record.isRecyclable) "回收垃圾" else "一般垃圾"}",
                                                fontSize = 14.sp,
                                                color = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                    if (index < correctRecords.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))
                    }


                    // 錯誤題目區域
                    if (wrongRecords.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    "✗ 答錯的題目 (${wrongRecords.size})",
                                    fontSize = 22.sp,
                                    color = Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(12.dp))


                                wrongRecords.forEachIndexed { index, record ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = record.trashImage),
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                getTrashName(record.trashImage),
                                                fontSize = 18.sp,
                                                color = Color.Black
                                            )
                                            Text(
                                                "你的答案: ${if (record.userAnswer) "回收垃圾" else "一般垃圾"}",
                                                fontSize = 14.sp,
                                                color = Color(0xFFF44336)
                                            )
                                            Text(
                                                "正確答案: ${if (record.isRecyclable) "回收垃圾" else "一般垃圾"}",
                                                fontSize = 14.sp,
                                                color = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                    if (index < wrongRecords.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))
                    }


                    Spacer(modifier = Modifier.height(8.dp))


                    Button(onClick = {
                        isGameOver = false
                        score = 0
                        timeLeft = 30
                        dragOffset = Offset.Zero
                        showFeedback = false
                        showScoreChange = false
                        answerRecords = emptyList()
                        correctCount = 0
                        wrongCount = 0
                        hasAddedScore = false
                        currentTrash = trashList.random()
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD2E9FF),
                            contentColor = Color(0xFF336666)
                        )
                    ) {
                        Text("重新開始", fontSize = 18.sp)
                    }


                    Spacer(modifier = Modifier.height(12.dp))


                    Button(onClick = {
                        navController.popBackStack()
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD2E9FF),
                            contentColor = Color(0xFF336666)
                        )
                    ) {
                        Text("返回主選單", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}