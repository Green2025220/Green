package tw.edu.pu.csim.s1114702.green


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas


@Composable
fun TurnScreen(
    navController: NavController,
    turnViewModel: TurnViewModel = viewModel()
) {
    val cards = turnViewModel.cards
    val lockBoard by turnViewModel.lockBoard
    val pairsFound by turnViewModel.pairsFound
    val elapsedTime by turnViewModel.elapsedTime
    val showMatchedCard by turnViewModel.showMatchedCard
    val matchedCardImageRes by turnViewModel.matchedCardImageRes


    LaunchedEffect(Unit) {
        turnViewModel.startTimer()
    }


    // 取得螢幕寬高
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp


    val horizontalPadding = 24.dp * 2
    val cardSpacing = 6.dp


    val cardWidth = (screenWidth - horizontalPadding - cardSpacing * 3) / 3
    val verticalPadding = 24.dp * 2 + 24.dp + 24.dp + 20.dp + 28.dp + 22.dp + 16.dp
    val cardHeight = ((screenHeight - verticalPadding - cardSpacing * 4) / 4).coerceAtMost(cardWidth)
    val cardSize = cardHeight


    // 計算分鐘與秒
    val totalSeconds = elapsedTime / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60


    if (pairsFound == cards.size / 2) {
        // 遊戲完成頁面
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF81C0C0))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("恭喜您完成挑戰！", fontSize = 28.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFCAFFFF),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("用時: ${minutes}m ${seconds}s", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))

                // 水平滾動的配對卡牌列表
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))

                    turnViewModel.matchedPairs.forEach { pair ->
                        // 每一組卡牌的容器（放大）
                        Column(
                            modifier = Modifier
                                .width(220.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // a 和 b 圖片並排在上方（放大）
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = pair.aImageRes),
                                    contentDescription = "A Card",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp)
                                )

                                Image(
                                    painter = painterResource(id = pair.bImageRes),
                                    contentDescription = "B Card",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // c 圖片在下方（更大）
                            Image(
                                painter = painterResource(id = pair.cImageRes),
                                contentDescription = "C Card",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(170.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        turnViewModel.resetGame()
                        navController.navigate("scone")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003060)),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("返回主頁", fontSize = 20.sp)
                }
            }

            // 煙火特效
            FireworksEffect()

            // 返回按鈕
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clickable { navController.popBackStack() }
                    .align(Alignment.TopStart)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backarrow),
                    contentDescription = "Back"
                )
            }
        }
    } else {
        // 遊戲進行中顯示棋盤
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF81C0C0))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Turn 翻牌遊戲", fontSize = 32.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))


                Text("配對 $pairsFound / ${cards.size / 2}", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("用時: ${minutes}m ${seconds}s", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))


                // 4x4 翻牌排列
                for (row in 0 until 4) {
                    Row {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val card = cards.getOrNull(index) ?: continue


                            Box(
                                modifier = Modifier
                                    .size(cardSize)
                                    .padding(6.dp)
                                    .clickable(enabled = !lockBoard && !card.isFlipped && !card.isMatched) {
                                        turnViewModel.flipCard(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (card.isFlipped || card.isMatched) {
                                    Image(
                                        painter = painterResource(id = card.imageRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.background),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // 返回按鈕
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clickable { navController.popBackStack() }
                    .align(Alignment.TopStart)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backarrow),
                    contentDescription = "Back"
                )
            }

            // 配對成功時顯示的 c 系列卡牌（全螢幕覆蓋）
            if (showMatchedCard && matchedCardImageRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable {
                            turnViewModel.hideMatchedCard()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = matchedCardImageRes!!),
                        contentDescription = "Matched Card",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .fillMaxHeight(0.6f)
                    )
                }
            }
        }
    }
}


// ---------- 煙火特效 ----------
@Composable
fun FireworksEffect() {
    var particles by remember { mutableStateOf(listOf<Particle>()) }
    var hasTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasTriggered) {
            hasTriggered = true
            // 從左右兩側同時發射 5 組煙火
            val launches = listOf(
                Triple(0.15f, 0.3f, 0L),    // 左側第一發
                Triple(0.85f, 0.25f, 300L), // 右側第一發
                Triple(0.25f, 0.35f, 600L), // 左側第二發
                Triple(0.75f, 0.3f, 900L),  // 右側第二發
                Triple(0.5f, 0.2f, 1200L)   // 中間最後一發
            )

            launches.forEach { (xPos, yPos, delayTime) ->
                launch {
                    delay(delayTime)
                    val newParticles = List(80) {
                        val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                        val speed = Random.nextFloat() * 0.015f + 0.005f
                        Particle(
                            x = xPos,
                            y = yPos,
                            vx = kotlin.math.cos(angle) * speed,
                            vy = kotlin.math.sin(angle) * speed,
                            color = listOf(
                                Color(0xFFFFD700), // 金色
                                Color(0xFFFF6B6B), // 紅色
                                Color(0xFF4ECDC4), // 青色
                                Color(0xFFFFE66D), // 黃色
                                Color(0xFFFF69B4), // 粉紅色
                                Color(0xFF95E1D3)  // 薄荷綠
                            ).random(),
                            life = 1f,
                            size = Random.nextFloat() * 4f + 4f
                        )
                    }
                    particles = particles + newParticles
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // 60 FPS
            particles = particles.mapNotNull { particle ->
                val newLife = particle.life - 0.015f
                if (newLife <= 0f) null
                else particle.copy(
                    x = particle.x + particle.vx,
                    y = particle.y + particle.vy,
                    vy = particle.vy + 0.0008f, // 重力效果
                    life = newLife
                )
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.life * 0.9f),
                radius = particle.size,
                center = Offset(
                    x = particle.x * size.width,
                    y = particle.y * size.height
                )
            )
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val life: Float,
    val size: Float = 4f
)


// ---------- Card 資料類 ----------
data class Card(
    val id: Int,
    val content: String,
    val imageRes: Int,
    val pairIndex: Int, // 新增：記錄是第幾對（用來對應 c 系列圖片）
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

// ---------- MatchedPair 資料類 ----------
data class MatchedPair(
    val aImageRes: Int,
    val bImageRes: Int,
    val cImageRes: Int,
    val pairIndex: Int
)


// ---------- TurnViewModel ----------
class TurnViewModel : ViewModel() {
    private val aImages = listOf(
        R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4,
        R.drawable.a5, R.drawable.a6, R.drawable.a7, R.drawable.a8,
        R.drawable.a9, R.drawable.a10, R.drawable.a11, R.drawable.a12,
        R.drawable.a13, R.drawable.a14, R.drawable.a15, R.drawable.a16, R.drawable.a17
    )
    private val bImages = listOf(
        R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4,
        R.drawable.b5, R.drawable.b6, R.drawable.b7, R.drawable.b8,
        R.drawable.b9, R.drawable.b10, R.drawable.b11, R.drawable.b12,
        R.drawable.b13, R.drawable.b14, R.drawable.b15, R.drawable.b16, R.drawable.b17
    )
    private val cImages = listOf(
        R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4,
        R.drawable.c5, R.drawable.c6, R.drawable.c7, R.drawable.c8,
        R.drawable.c9, R.drawable.c10, R.drawable.c11, R.drawable.c12,
        R.drawable.c13, R.drawable.c14, R.drawable.c15, R.drawable.c16, R.drawable.c17
    )


    val cards = mutableStateListOf<Card>()
    var firstFlippedIndex = mutableStateOf<Int?>(null)
    var lockBoard = mutableStateOf(false)
    var pairsFound = mutableStateOf(0)
    var elapsedTime = mutableStateOf(0L)
    var showMatchedCard = mutableStateOf(false)
    var matchedCardImageRes = mutableStateOf<Int?>(null)
    val matchedPairs = mutableStateListOf<MatchedPair>() // 記錄所有配對成功的組合

    private var startTime = 0L
    private var timerRunning = false
    private var pausedTime = 0L // 記錄暫停時的累計時間（不需要 mutableStateOf）


    init {
        resetGame()
    }


    fun resetGame() {
        cards.clear()


        // 確保 a、b、c 的圖片數量一致
        require(aImages.size == bImages.size && bImages.size == cImages.size) {
            "aImages、bImages 和 cImages 數量不一致！"
        }


        // 從所有可用對中隨機抽 6 對
        val selectedIndices = aImages.indices.shuffled().take(6)


        val pairs = mutableListOf<Card>()


        selectedIndices.forEach { i ->
            // aX 對應 bX，強制配對，並記錄 pairIndex
            val aCard = Card(id = i * 2, content = "pair$i", imageRes = aImages[i], pairIndex = i)
            val bCard = Card(id = i * 2 + 1, content = "pair$i", imageRes = bImages[i], pairIndex = i)
            pairs.add(aCard)
            pairs.add(bCard)


            println("抽到配對：a${i + 1} ↔ b${i + 1} (content=${aCard.content})")
        }


        // 打亂整個棋盤順序
        cards.addAll(pairs.shuffled())


        println("棋盤順序：")
        cards.forEachIndexed { index, card ->
            println("Index $index -> id=${card.id}, content=${card.content}, pairIndex=${card.pairIndex}")
        }


        // 重置狀態
        firstFlippedIndex.value = null
        lockBoard.value = false
        pairsFound.value = 0
        elapsedTime.value = 0L
        pausedTime = 0L // 直接賦值，不是 .value
        timerRunning = false
        showMatchedCard.value = false
        matchedCardImageRes.value = null
        matchedPairs.clear() // 清空配對記錄
    }


    fun flipCard(index: Int) {
        if (lockBoard.value || cards[index].isFlipped || cards[index].isMatched) return


        cards[index].isFlipped = true


        val prevIndex = firstFlippedIndex.value
        if (prevIndex == null) {
            firstFlippedIndex.value = index
            if (!timerRunning) startTimer()
        } else {
            if (cards[prevIndex].content == cards[index].content) {
                // 配對成功
                cards[prevIndex].isMatched = true
                cards[index].isMatched = true
                pairsFound.value++

                // 記錄配對成功的組合
                val pairIndex = cards[index].pairIndex
                val matchedPair = MatchedPair(
                    aImageRes = aImages[pairIndex],
                    bImageRes = bImages[pairIndex],
                    cImageRes = cImages[pairIndex],
                    pairIndex = pairIndex
                )
                matchedPairs.add(matchedPair)

                // 暫停計時
                pauseTimer()

                // 顯示對應的 c 系列卡牌
                matchedCardImageRes.value = cImages[pairIndex]
                showMatchedCard.value = true

                // 如果遊戲已完成，停止計時
                if (pairsFound.value == cards.size / 2) {
                    timerRunning = false
                }

            } else {
                // 配對失敗，延遲翻回
                lockBoard.value = true
                CoroutineScope(Dispatchers.Main).launch {
                    delay(800)
                    cards[prevIndex].isFlipped = false
                    cards[index].isFlipped = false
                    lockBoard.value = false
                }
            }
            firstFlippedIndex.value = null
        }
    }

    fun hideMatchedCard() {
        showMatchedCard.value = false
        matchedCardImageRes.value = null

        // 繼續計時（如果遊戲未結束）
        if (pairsFound.value < cards.size / 2) {
            resumeTimer()
        }
    }

    fun startTimer() {
        if (timerRunning) return
        timerRunning = true
        startTime = System.currentTimeMillis() - pausedTime
        CoroutineScope(Dispatchers.Main).launch {
            while (timerRunning) {
                elapsedTime.value = System.currentTimeMillis() - startTime
                delay(1000)
            }
        }
    }


    private fun pauseTimer() {
        if (timerRunning) {
            pausedTime = elapsedTime.value
            timerRunning = false
        }
    }

    private fun resumeTimer() {
        if (!timerRunning) {
            timerRunning = true
            startTime = System.currentTimeMillis() - pausedTime
            CoroutineScope(Dispatchers.Main).launch {
                while (timerRunning) {
                    elapsedTime.value = System.currentTimeMillis() - startTime
                    delay(1000)
                }
            }
        }
    }
}