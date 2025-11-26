package tw.edu.pu.csim.s1114702.green

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

// 定義自定義字體
val SentyDragonPalaceFont = FontFamily(
    Font(R.font.senty_dragon_palace) // 確保字體文件名稱為 senty_dragon_palace.ttf
)

// 問題資料模型
data class Question(
    val imageResource: Int,
    val options: List<String>,
    val correctAnswerIndex: Int
)

@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(80.dp)
            .background(Color(0xFFB8E6C0), shape = RoundedCornerShape(40.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
fun QuizGameScreen(navController: NavController, viewModel: ViewModel, userEmail: String) {
    // 題庫
    val questions = remember {
        listOf(
            Question(R.drawable.question1, listOf("可回收", "一般垃圾"), 1),
            Question(R.drawable.question2, listOf("可回收", "一般垃圾"), 0),
            Question(R.drawable.question3, listOf("可回收", "一般垃圾"), 1),
            Question(R.drawable.question4, listOf("是", "否"), 0),
            Question(R.drawable.question5, listOf("是", "否"), 0),
            Question(R.drawable.question6, listOf("是","否"), 1),
            Question(R.drawable.question7, listOf("可回收", "一般垃圾"), 0),
            Question(R.drawable.question8, listOf("是","否"), 0),
            Question(R.drawable.question9, listOf("可回收", "一般垃圾"), 0),
            Question(R.drawable.question10, listOf("是", "否"), 1),
            Question(R.drawable.question11, listOf("可回收", "一般垃圾","大型垃圾"), 1),
            Question(R.drawable.question12, listOf("倒掉液體再分類回收", "直接丟垃圾桶","整杯一起回收"), 0),
            Question(R.drawable.question13, listOf("直接丟掉","丟入廚餘桶","清洗乾淨後回收"), 2),
            Question(R.drawable.question14, listOf("可以直接回收", "需要撕掉膠帶後再回收","紙箱和膠帶都要丟垃圾"), 1),
            Question(R.drawable.question15, listOf("廚餘桶", "一般垃圾","可回收"), 0),
            Question(R.drawable.question16, listOf("可以", "需要先清洗","不可以"), 2),
            Question(R.drawable.question17, listOf("剪開沖洗後再回收", "直接丟垃圾桶","可直接回收"), 0),
            Question(R.drawable.question18, listOf("直接丟垃圾桶", "捐給需要的人或二手回收","剪碎當抹布"), 1),
            Question(R.drawable.question19, listOf("不可以", "可以","需要裝袋才行"), 0),
            Question(R.drawable.question20, listOf("可回收", "有專門回收點","一般垃圾"), 2),
            Question(R.drawable.question21, listOf("廢電池", "寶特瓶","玻璃罐","油漬紙盒"), 3),
            Question(R.drawable.question22, listOf("紙盒", "塑膠袋","鋁箔紙盒","保麗龍"), 3),
            Question(R.drawable.question23, listOf("PET（寶特瓶）","PP（免洗餐具）","PS（保麗龍）","都可以"), 0),
            Question(R.drawable.question24, listOf("丟垃圾桶", "拿去3C回收站","賣給二手回收商","放在家裡當備用"), 1),
            Question(R.drawable.question25, listOf("直接丟回收桶", "清洗乾淨後回收","敲碎後再回收","跟塑膠瓶一起回收"), 1),
            Question(R.drawable.question26, listOf("可以", "不可以"), 0),
            Question(R.drawable.question27, listOf("全部一起回收", "紙箱分開，報紙與雜誌可一起回收","雜誌丟垃圾，報紙和紙箱回收","報紙回收，紙箱與雜誌丟垃圾"), 1),
            Question(R.drawable.question28, listOf("分類不同種類的塑膠","清洗乾淨再回收","全部都重要"), 2),
            Question(R.drawable.question29, listOf("新的鋁罐", "衣服","木材","保麗龍"), 0),
            Question(R.drawable.question30, listOf("多用塑膠袋來裝垃圾", "減少購買一次性用品","只使用可回收的東西","將所有垃圾都回收"), 1),

            Question(R.drawable.question31, listOf("機車", "腳踏車"), 1),
            Question(R.drawable.question32, listOf("牛肉", "雞肉"), 0),
            Question(R.drawable.question33, listOf("要", "不要"), 0),
            Question(R.drawable.question34, listOf("塑膠袋", "環保袋"), 1),
            Question(R.drawable.question35, listOf("電動車", "傳統燃油車"), 0),
            Question(R.drawable.question36, listOf("能", "不能"), 0),
            Question(R.drawable.question37, listOf("能", "不能"), 0),
            Question(R.drawable.question38, listOf("傳統燈泡", "LED燈泡"), 1),
            Question(R.drawable.question39, listOf("每天用塑膠吸管", "隨身攜帶環保杯", "用一次性塑膠袋"), 1),
            Question(R.drawable.question40, listOf("住在大房子", "住在都市、使用大眾運輸", "每天開車上下班"), 1),
            Question(R.drawable.question41, listOf("傳統電視", "變頻冷氣", "電熱水器"), 1),
            Question(R.drawable.question42, listOf("低碳物流", "空運", "每件物品單獨送"), 0),
            Question(R.drawable.question43, listOf("讓燈整晚開著", "使用節能家電"), 1),
            Question(R.drawable.question44, listOf("用冷水洗", "洗幾件衣物一次洗", "每天洗少量的衣服"), 1),
            Question(R.drawable.question45, listOf("太陽變熱", "火山爆發", "人類活動產生的碳排放", "海洋汙染"), 2),
            Question(R.drawable.question46, listOf("每天吃速食", "買進口食品", "不分類垃圾", "減少食物浪費"), 3),
            Question(R.drawable.question47, listOf("資訊科技業", "旅遊業", "重工業", "教育業"), 2),
            Question(R.drawable.question48, listOf("住大房子", "使用太陽能或綠能設備", "冷氣24小時開著", "購買昂貴家具"), 1),
            Question(R.drawable.question49, listOf("開汽油車", "坐飛機", "搭電動大眾交通工具", "騎重型機車"), 2),
            Question(R.drawable.question50, listOf("增加產品包裝", "改用再生材料、減少浪費", "使用更多一次性產品", "鼓勵員工開車通勤"), 1),
            Question(R.drawable.question51, listOf("每天丟食物", "實踐零浪費生活（Zero Waste）", "用大量清潔劑洗碗", "買新的車但不開"), 1),
            Question(R.drawable.question52, listOf("過度使用冷氣和暖氣", "用更多塑膠袋", "增加肉類攝取", "安裝節能設備"), 3),
            Question(R.drawable.question53, listOf("收集碳排放並儲存或再利用", "一種新型燃料", "減少森林砍伐的方法", "一種新型發電機"), 0),
            Question(R.drawable.question54, listOf("為了購買更多產品", "平衡人類活動的碳排放，減少氣候變遷影響", "讓工廠排放更多二氧化碳", "減少動物數量"), 1),
            Question(R.drawable.question55, listOf("對，要把可回收物分開並簡單清潔", "不用，全部一起丟就行"), 0),
            Question(R.drawable.question56, listOf("直接丟回收，不用處理", "倒空、沖一下再壓扁回收"), 1),
            Question(R.drawable.question57, listOf("不用拆，整瓶丟", "要倒空、拆蓋拆標再回收"), 1),
            Question(R.drawable.question58, listOf("攤平、綁好再回收", "直接皺著丟進回收桶"), 0),
        )
    }

    // 遊戲狀態
    var score by remember { mutableStateOf(0) }
    var questionIndex by remember { mutableStateOf(0) }
    var usedQuestionIndices by remember { mutableStateOf(mutableSetOf<Int>()) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }  // 控制是否顯示選項
    val maxQuestions = 5

    val maxPlaysPerDay = 3
    var showLimitDialog by remember { mutableStateOf(false) }
    var canPlay by remember { mutableStateOf(true) }
    var remainingPlays by remember { mutableStateOf(maxPlaysPerDay) }

    // 選擇隨機題目
    fun getNextRandomQuestion() {
        if (usedQuestionIndices.size >= minOf(maxQuestions, questions.size)) {
            gameOver = true
            return
        }

        var randomIndex: Int
        do {
            randomIndex = Random.nextInt(0, questions.size)
        } while (randomIndex in usedQuestionIndices)

        questionIndex = randomIndex
        usedQuestionIndices.add(randomIndex)
    }

    LaunchedEffect(key1 = Unit) {
        canPlay = viewModel.canPlayQuizGame(maxPlaysPerDay)
        remainingPlays = viewModel.getRemainingQuizGamePlays(maxPlaysPerDay)

        if (!canPlay) {
            showLimitDialog = true
        } else {
            getNextRandomQuestion()
        }
    }

    // 處理答題
    fun handleAnswer(selectedIndex: Int) {
        val currentQuestion = questions[questionIndex]
        isCorrect = selectedIndex == currentQuestion.correctAnswerIndex

        if (isCorrect) {
            score += 10
        }

        showFeedback = true
    }

    // 進入下一題
    fun nextQuestion() {
        showFeedback = false
        showOptions = false  // 重置選項顯示狀態
        getNextRandomQuestion()
    }

    // UI建構
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片
        val backgroundImage = painterResource(id = questions[questionIndex].imageResource)

        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = !showOptions && !showFeedback && !gameOver && canPlay,
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {
                    showOptions = true  // 點擊圖片後顯示選項
                }
        )

        // 返回按鈕
        val backButtonImage = painterResource(id = R.drawable.backarrow)
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(30.dp)
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart)
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back"
            )
        }

        Row(
            modifier = Modifier
                .padding(6.dp)
                .align(Alignment.TopEnd)
                .background(Color(0x55000000), shape = RoundedCornerShape(4.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "分數: $score",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(10.dp)
                    .background(Color.White.copy(alpha = 0.5f))
            )

            Text(
                text = "剩餘: $remainingPlays 次",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 次數用完提示對話框
        if (showLimitDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cross1),
                            contentDescription = "提示",
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "今日次數已用完",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "您今天已經玩過 $maxPlaysPerDay 次了\n明天再来挑戰吧！",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("確定")
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }

        // 題目顯示 - 答案選項（點擊畫面後才顯示）
        if (!gameOver && canPlay && showOptions) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    questions[questionIndex].options.forEachIndexed { index, option ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        AnswerButton(
                            text = option,
                            onClick = { handleAnswer(index) },
                            enabled = !showFeedback
                        )
                    }
                }
            }
        } else if (gameOver && canPlay) {
            // 遊戲結束畫面
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(Color(0x88000000), shape = RoundedCornerShape(16.dp))
                        .padding(32.dp)
                ) {
                    Text(
                        text = "遊戲結束！",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "您的最終分數: $score",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val newRemainingPlays = remainingPlays - 1
                    Text(
                        text = if (newRemainingPlays > 0)
                            "今日還可遊玩 $newRemainingPlays 次"
                        else
                            "今日次數已用完，明天再來！",
                        fontSize = 18.sp,
                        color = Color.Yellow
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.recordQuizGamePlay(userEmail, score)
                            navController.popBackStack()
                        }
                    ) {
                        Text("回到主選單")
                    }
                }
            }
        }

        // 答題反饋對話框
        if (showFeedback) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isCorrect) R.drawable.circle1 else R.drawable.cross1
                            ),
                            contentDescription = if (isCorrect) "正確" else "錯誤",
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (isCorrect) "答對了！" else "答錯了！",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCorrect)
                                "恭喜獲得10分！"
                            else
                                "正確答案是: ${questions[questionIndex].options[questions[questionIndex].correctAnswerIndex]}",
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { nextQuestion() },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("下一題")
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun AnswerButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(60.dp)
            .background(
                color = if (enabled) Color(0xFFADFEDC) else Color(0xFFAAAAAA),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            fontFamily = SentyDragonPalaceFont  // 使用自定義字體
        )
    }
}