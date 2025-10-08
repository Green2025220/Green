package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun TurnScreen(
    navController: NavController,
    turnViewModel: TurnViewModel = viewModel()
) {
    val cards = turnViewModel.cards
    val lockBoard by turnViewModel.lockBoard
    val pairsFound by turnViewModel.pairsFound
    val elapsedTime by turnViewModel.elapsedTime

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF81C0C0))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("恭喜您完成挑戰！", fontSize = 28.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("您已用時: ${minutes}m ${seconds}s", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    turnViewModel.resetGame()
                    navController.navigate("scone")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003060)), // <-- 改顏色
                modifier = Modifier.padding(8.dp)
            ) {
                Text("返回主頁", fontSize = 20.sp)
            }
        }
    } else {
        // 遊戲進行中顯示棋盤
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
    }
}

// ---------- Card 資料類 ----------
data class Card(
    val id: Int,
    val content: String,
    val imageRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

// ---------- TurnViewModel ----------
class TurnViewModel : ViewModel() {
    private val aImages = listOf(
        R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4,
        R.drawable.a5, R.drawable.a6, R.drawable.a7, R.drawable.a8,
        R.drawable.a9, R.drawable.a10, R.drawable.a11, R.drawable.a12,
        R.drawable.a13, R.drawable.a14, R.drawable.a15, R.drawable.a16,R.drawable.a17
    )
    private val bImages = listOf(
        R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4,
        R.drawable.b5, R.drawable.b6, R.drawable.b7, R.drawable.b8,
        R.drawable.b9, R.drawable.b10, R.drawable.b11, R.drawable.b12,
        R.drawable.b13, R.drawable.b14, R.drawable.b15, R.drawable.b16,R.drawable.b17
    )

    val cards = mutableStateListOf<Card>()
    var firstFlippedIndex = mutableStateOf<Int?>(null)
    var lockBoard = mutableStateOf(false)
    var pairsFound = mutableStateOf(0)
    var elapsedTime = mutableStateOf(0L)
    private var startTime = 0L
    private var timerRunning = false

    init {
        resetGame()
    }

    fun resetGame() {
        cards.clear()

        // 確保 a 與 b 的圖片數量一致
        require(aImages.size == bImages.size) {
            "aImages (${aImages.size}) 和 bImages (${bImages.size}) 數量不一致！"
        }

        // 從所有可用對中隨機抽 8 對
        val selectedIndices = aImages.indices.shuffled().take(6)

        val pairs = mutableListOf<Card>()

        selectedIndices.forEach { i ->
            // aX 對應 bX，強制配對
            val aCard = Card(id = i * 2, content = "pair$i", imageRes = aImages[i])
            val bCard = Card(id = i * 2 + 1, content = "pair$i", imageRes = bImages[i])
            pairs.add(aCard)
            pairs.add(bCard)

            // Log 檢查每一對
            println("抽到配對：a${i + 1} ↔ b${i + 1} (content=${aCard.content})")
        }

        // 打亂整個棋盤順序
        cards.addAll(pairs.shuffled())

        // 顯示打亂後的卡片順序（id + content）
        println("棋盤順序：")
        cards.forEachIndexed { index, card ->
            println("Index $index -> id=${card.id}, content=${card.content}")
        }

        // 重置狀態
        firstFlippedIndex.value = null
        lockBoard.value = false
        pairsFound.value = 0
        elapsedTime.value = 0L
        timerRunning = false
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

    fun startTimer() {
        if (timerRunning) return
        timerRunning = true
        startTime = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (timerRunning) {
                elapsedTime.value = System.currentTimeMillis() - startTime
                delay(1000)
            }
        }
    }
}
