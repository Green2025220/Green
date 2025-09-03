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
fun QuizGameScreen(navController: NavController, viewModel: ViewModel) {
    // 題庫
    val questions = remember {
        listOf(
            Question(
                "小王和小張剛吃完晚餐，兩人開始收拾桌面。小王喝完一杯飲料後，準備丟掉塑膠吸管，但不確定應該丟哪裡。於是他問小張：「這根塑膠吸管應該丟在哪裡？」",
                R.drawable.question1,
                listOf("可回收", "一般垃圾"),
                1
            ),
            Question(
                "小張正在收拾廚房的桌面，看到桌上有個玻璃瓶，他準備把瓶子丟進垃圾桶，但猶豫了一下，問小王：「這瓶玻璃應該怎麼處理？我記得玻璃是可以回收的對吧？」",
                R.drawable.question2,
                listOf("可回收", "一般垃圾"),
                0
            ),
            Question(
                "小王順手拿起了用過的餐巾紙，準備丟進垃圾袋，這時小張提醒他：「別忘了，這些用過的衛生紙應該丟哪裡？」小王有些疑惑，回頭問小張。？",
                R.drawable.question3,
                listOf("可回收", "一般垃圾"),
                1
            ),
            Question(
                "小張正在整理餐桌，看到旁邊的可樂罐，說道：「我記得這種鋁罐是可以回收的，對嗎？這個鋁罐應該放哪裡？」",
                R.drawable.question4,
                listOf("是", "否"),
                0
            ),
            Question(
                "小王拿起旁邊的寶特瓶，看到瓶蓋還沒有拆下來，問小張：「那這個寶特瓶的瓶蓋要回收嗎？是不是跟瓶身分開處理？」",
                R.drawable.question5,
                listOf("是", "否"),
                0
            ),
            Question(
                "小張和小王剛買完晚餐外賣，兩人準備將剛買回來的餐盒處理掉。小張拿起保麗龍餐盒，問小王：「這個保麗龍餐盒可以回收嗎？我記得這種東西不容易回收吧？」",
                R.drawable.question6,
                listOf("是","否"),
                1
            ),
            Question(
                "回到家後，小張開始整理購物袋，發現袋子裡有一個牛奶紙盒。他問小王：「這個牛奶紙盒該丟哪裡？我們是不是可以丟進回收箱？」",
                R.drawable.question7,
                listOf("可回收", "一般垃圾"),
                0
            ),
            Question(
                "小王發現旁邊還有一包剛吃完的即食餐的鋁箔包，他隨手拿起來，問小張：「這個用過的鋁箔包可以回收嗎？是不是需要先清理乾淨？」",
                R.drawable.question8,
                listOf("是","否"),
                0
            ),
            Question(
                "小張整理家裡的舊報紙時，看見一堆新聞紙堆在角落，問小王：「這些舊報紙應該丟在哪？我記得紙類可以回收，但不確定是不是所有的報紙都能回收。」",
                R.drawable.question9,
                listOf("可回收", "一般垃圾"),
                0
            ),
            Question(
                "小王把舊手機的電池拿出來，準備放進垃圾桶，小張馬上停住他，問道：「這個手機電池可以丟在一般垃圾裡嗎？」",
                R.drawable.question10,
                listOf("是", "否"),
                1
            ),
            Question(
                "小李正在整理廚房，突然一個陶瓷碗不小心摔碎了。小李看著地上的碎片，陷入了兩難，問小張：「這個破掉的陶瓷碗該丟哪裡？是丟一般垃圾嗎？還是有其他處理方式？」",
                R.drawable.question11,
                listOf("可回收", "一般垃圾","大型垃圾"),
                0
            ),
            Question(
                "晚餐後，小李和小張坐下來聊起下午買的珍珠奶茶。小李沒有喝完，他準備把剩下的倒掉，但發現還有一點珍珠，於是問小張：「這杯珍珠奶茶該怎麼處理？如果倒掉剩餘的液體，杯子可以回收嗎？」",
                R.drawable.question12,
                listOf("倒掉液體再分類回收", "直接丟垃圾桶","整杯一起回收"),
                0
            ),
            Question(
                "小李正在洗碗，看到洗碗精瓶子已經用完了，他拿起瓶子看了一會兒，問小張：「這瓶洗碗精瓶該怎麼處理？我知道塑膠瓶可以回收，但是這種瓶子裡面有一些殘留的洗潔精，要怎麼處理呢？」",
                R.drawable.question13,
                listOf("直接丟掉","丟入廚餘桶","清洗乾淨後回收"),
                2
            ),
            Question(
                "小李和小張一起拆開一個包裹，發現包裹上的膠帶黏在紙箱上。小李問：「膠帶黏在紙箱上，這樣能直接回收嗎？還是需要先撕掉膠帶？」",
                R.drawable.question14,
                listOf("可以直接回收", "需要撕掉膠帶後再回收","紙箱和膠帶都要丟垃圾"),
                1
            ),
            Question(
                "小李剛買了一些雞蛋，看到雞蛋殼還堆在旁邊。他問小張：「這些雞蛋殼應該丟在哪裡？」",
                R.drawable.question15,
                listOf("廚餘桶", "一般垃圾","可回收"),
                0
            ),
            Question(
                "小李看著剛使用過的保鮮膜，準備丟掉，但他知道這類物品回收比較麻煩，於是問小張：「這個用過的保鮮膜能回收嗎？還是應該丟掉？」",
                R.drawable.question16,
                listOf("可以", "需要先清洗","不可以"),
                2
            ),
            Question(
                "小張拿起了一個用過的鋁箔包，準備回收。小李提醒他：「這個鋁箔包是不是要沖洗乾淨才能回收？」",
                R.drawable.question17,
                listOf("剪開沖洗後再回收", "直接丟垃圾桶","可直接回收"),
                0
            ),
            Question(
                "小李在整理衣櫃時，發現了一些已經不穿的舊衣服。他問小張：「這些舊衣服該怎麼處理才最環保？丟掉嗎，還是有其他辦法？」",
                R.drawable.question18,
                listOf("直接丟垃圾桶", "捐給需要的人或二手回收","剪碎當抹布"),
                1
            ),
            Question(
                "小李看到桌上散落的碎紙屑，問小張：「這些碎紙屑可以回收嗎？」",
                R.drawable.question19,
                listOf("不可以", "可以","需要裝袋才行"),
                0
            ),
            Question(
                "小張換了一支新的牙刷，將舊的牙刷放到一旁，他問小李：「這支用過的牙刷應該怎麼丟？一般垃圾還是有專門的回收處？」",
                R.drawable.question20,
                listOf("可回收", "有專門回收點","一般垃圾"),
                2
            ),
            Question(
                "小王在整理家裡的垃圾時，發現一堆舊物品，他問小李：「下面哪種物品不能回收？我有點搞不清楚。」",
                R.drawable.question21,
                listOf("廢電池", "寶特瓶","玻璃罐","油漬紙盒"),
                3
            ),
            Question(
                "小李正在清理新買的包裝盒，他問小王：「以下哪種材質的包裝盒最難回收？我知道塑膠袋回收麻煩，但其他的包裝盒呢？」",
                R.drawable.question22,
                listOf("紙盒", "塑膠袋","鋁箔紙盒","保麗龍"),
                3
            ),
            Question(
                "小王看到廚房的塑膠瓶，他問小李：「哪種材質的塑膠是可回收的？我聽說有些塑膠不能回收。」",
                R.drawable.question23,
                listOf("PET（寶特瓶）","PP（免洗餐具）","PS（保麗龍）","都可以"),
                0
            ),
            Question(
                "小張家裡有一部舊手機，他不再使用了，問小李：「這部舊手機應該怎麼處理？丟掉還是有其他辦法？」",
                R.drawable.question24,
                listOf("丟垃圾桶", "拿去3C回收站","賣給二手回收商","放在家裡當備用"),
                1
            ),
            Question(
                "小王清理家裡的玻璃瓶，問小李：「這個玻璃瓶該怎麼處理？我記得玻璃可以回收，但需要清洗嗎？」",
                R.drawable.question25,
                listOf("直接丟回收桶", "清洗乾淨後回收","敲碎後再回收","跟塑膠瓶一起回收"),
                1
            ),
            Question(
                "小李把一個鐵製衣架丟到垃圾桶，問小張：「這個鐵製衣架能回收嗎？」",
                R.drawable.question26,
                listOf("可以", "不可以"),
                0
            ),
            Question(
                "小張拿起一堆舊報紙、雜誌和紙箱，問小李：「這些舊報紙、雜誌、紙箱該怎麼分類？我記得有些是可以回收的，但也不確定應該怎麼分類。」",
                R.drawable.question27,
                listOf("全部一起回收", "紙箱分開，報紙與雜誌可一起回收","雜誌丟垃圾，報紙和紙箱回收","報紙回收，紙箱與雜誌丟垃圾"),
                1
            ),
            Question(
                "小李問小張：「回收塑膠時，哪個步驟最重要？是直接丟進回收桶，還是有其他更重要的處理方式？」",
                R.drawable.question28,
                listOf("分類不同種類的塑膠","清洗乾淨再回收","全部都重要"),
                2
            ),Question(
                "小李和小張討論鋁罐的回收，他問小王：「回收後的鋁罐可以被製成什麼？聽說它能做成很多東西。」",
                R.drawable.question29,
                listOf("新的鋁罐", "衣服","木材","保麗龍"),
                0
            ),
            Question(
                "小張看著滿桌的垃圾，問小李：「如果想要延長資源的使用壽命，減少浪費，應該怎麼做？」",
                R.drawable.question30,
                listOf("多用塑膠袋來裝垃圾", "減少購買一次性用品","只使用可回收的東西","將所有垃圾都回收"),
                1
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
            //viewModel.totalScore += score
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
                        onClick = {
                            viewModel.updateTotalScore(score)// 在 ViewModel 中更新總分
                            navController.popBackStack() }
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