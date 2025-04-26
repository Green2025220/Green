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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

// 問題資料模型
data class Question(
    val questionText: String,
    val imageResource: Int? = null, // 如果您想為每個問題提供不同的圖片
    val options: List<String>,
    val correctAnswerIndex: Int
)

@Composable
fun G1levelScreen(navController: NavController) {
    val backgroundImage = painterResource(id = R.drawable.greenback)
    val backButtonImage = painterResource(id = R.drawable.backarrow)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 設定背景圖片
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize() // 使用 fillMaxSize() 確保背景鋪滿
        )

        // 返回按鈕（左上角）
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() } // 點擊後返回上一頁
                .align(Alignment.TopStart) // 確保按鈕位於左上角
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back"
            )
        }

        // 難度選擇按鈕
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonLabels = listOf("1低等難度", "中等難度", "高等難度")
            buttonLabels.forEach { label ->
                CustomButton(text = label, onClick = {
                    when (label) {
                        "1低等難度" -> navController.navigate("level1") //導到遊戲畫面
                        "中等難度" -> navController.navigate("secgame") //
                        "高等難度" -> navController.navigate("secgame") //
                    }
                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(80.dp)
            .background(Color(0xFFB8E6C0), shape = RoundedCornerShape(40.dp)) // 圓角矩形
            .clickable { onClick() },  // 處理點擊事件
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
fun QuizGameScreen(navController: NavController) {
    // 題庫
    val questions = remember {
        listOf(
            Question(
                "小王和小張剛吃完晚餐，兩人開始收拾桌面。小王喝完一杯飲料後，準備丟掉塑膠吸管，但不確定應該丟哪裡。於是他問小張：「這根塑膠吸管應該丟在哪裡？」",
                R.drawable.question1_bg,
                listOf("可回收", "一般垃圾"),
                1
            ),
            Question(
                "小張正在收拾廚房的桌面，看到桌上有個玻璃瓶，他準備把瓶子丟進垃圾桶，但猶豫了一下，問小王：「這瓶玻璃應該怎麼處理？我記得玻璃是可以回收的對吧？」",
                R.drawable.question2_bg,
                listOf("可回收", "一般垃圾"),
                0
            ),
            Question(
                "小王順手拿起了用過的餐巾紙，準備丟進垃圾袋，這時小張提醒他：「別忘了，這些用過的衛生紙應該丟哪裡？」小王有些疑惑，回頭問小張。？",
                R.drawable.question3_bg,
                listOf("可回收", "一般垃圾"),
                1
            ),
            Question(
                "小張正在整理餐桌，看到旁邊的可樂罐，說道：「我記得這種鋁罐是可以回收的，對嗎？這個鋁罐應該放哪裡？」",
                R.drawable.question4_bg,
                listOf("是", "否"),
                0
            ),
            Question(
                "小王拿起旁邊的寶特瓶，看到瓶蓋還沒有拆下來，問小張：「那這個寶特瓶的瓶蓋要回收嗎？是不是跟瓶身分開處理？」",
                R.drawable.question5_bg,
                listOf("是", "否"),
                0
            ),
            Question(
                "小張和小王剛買完晚餐外賣，兩人準備將剛買回來的餐盒處理掉。小張拿起保麗龍餐盒，問小王：「這個保麗龍餐盒可以回收嗎？我記得這種東西不容易回收吧？」",
                R.drawable.question6_bg,
                listOf("是", "否"),
                1
            ),
            Question(
                "回到家後，小張開始整理購物袋，發現袋子裡有一個牛奶紙盒。他問小王：「這個牛奶紙盒該丟哪裡？我們是不是可以丟進回收箱？」",
                R.drawable.question7_bg,
                listOf("可回收", "一般垃圾"),
                0
            ),
            Question(
                "小王發現旁邊還有一包剛吃完的即食餐的鋁箔包，他隨手拿起來，問小張：「這個用過的鋁箔包可以回收嗎？是不是需要先清理乾淨？」",
                R.drawable.question8_bg,
                listOf("是", "否"),
                0
            )
        )
    }

    // 遊戲狀態
    var score by remember { mutableStateOf(0) }
    var questionIndex by remember { mutableStateOf(0) }
    var usedQuestionIndices by remember { mutableStateOf(mutableSetOf<Int>()) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    val maxQuestions = 5 // 設定遊戲總題數

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

    // 初始載入第一題
    LaunchedEffect(key1 = Unit) {
        getNextRandomQuestion()
    }

    // 處理答題
    fun handleAnswer(selectedIndex: Int) {
        val currentQuestion = questions[questionIndex]
        isCorrect = selectedIndex == currentQuestion.correctAnswerIndex

        if (isCorrect) {
            score += 10 // 答對加10分
        }

        showFeedback = true
    }

    // 進入下一題
    fun nextQuestion() {
        showFeedback = false
        getNextRandomQuestion()
    }

    // UI建構
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景圖片
        val backgroundImage = painterResource(id =
            if (questions[questionIndex].imageResource != null)
                questions[questionIndex].imageResource!!
            else
                R.drawable.greenback)

        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 返回按鈕
        val backButtonImage = painterResource(id = R.drawable.backarrow)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart)
        ) {
            Image(
                painter = backButtonImage,
                contentDescription = "Back"
            )
        }

        // 分數顯示
        Text(
            text = "分數: $score",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .background(Color(0x88000000), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        )

        // 題目顯示
        if (!gameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 題目文字區域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 80.dp)
                        .background(Color(0x88000000), shape = RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = questions[questionIndex].questionText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // 答案選項
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
        } else {
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
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { navController.popBackStack() }
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
                        // 正確/錯誤圖示
                        Image(
                            painter = painterResource(
                                id = if (isCorrect) R.drawable.circle1 else R.drawable.cross1
                            ),
                            contentDescription = if (isCorrect) "正確" else "錯誤",
                            modifier = Modifier.size(60.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 標題文字
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

                        // 您也可以在這裡添加更多的內容或圖像
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
                // 設置對話框樣式
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
                color = if (enabled) Color(0xFFB8E6C0) else Color(0xFFAAAAAA),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}