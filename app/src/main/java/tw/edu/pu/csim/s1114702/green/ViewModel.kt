package tw.edu.pu.csim.s1114702.green


import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch


// 用來儲存商品位置的資料類別
data class ItemPosition(val x: Float, val y: Float)


class ViewModel : ViewModel() {
    val checklistItems = listOf(
        "攜帶環保餐具/杯/袋", "搭乘大眾運輸/騎腳踏車/步行",
        "做好資源回收分類", "選擇有機食物", "隨手關燈、拔插頭",
        "購買綠色商標商品", "選擇綠色旅遊行程", "節約用水"
    )
    val checkStates = mutableStateListOf(*Array(checklistItems.size) { false })
    private val _totalScore = mutableStateOf(0)
    var totalScore: Int
        get() = _totalScore.value
        set(value) {
            _totalScore.value = value
            updateScoreInFirebase(value)
        }


    private var lastCheckedDate: LocalDate = LocalDate.now()
    private var hasAddedScoreToday = false
    // 改成 List 以支援重複購買（記錄所有購買紀錄）
    var redeemedItems = mutableStateListOf<String>()


    private val itemPositions = mutableMapOf<String, ItemPosition>()


    // Firebase 資料庫引用
    private val database = FirebaseDatabase.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid


    fun initializeDailyData(email: String, onComplete: (() -> Unit)? = null) {
        val today = LocalDate.now()
        val isNewDay = today != lastCheckedDate


        if (isNewDay) {
            // 重置 checklist 狀態
            resetChecklist()
            hasAddedScoreToday = false
            lastCheckedDate = today
        }


        loadDailyChallengeFromFirebase(email) {
            if (isNewDay) {
                // 確保 Firebase 資料載入後清空勾選框狀態
                checkStates.replaceAll { false }
            }
            onComplete?.invoke()
        }
    }


    private fun resetChecklist() {
        checkStates.replaceAll { false }
    }


    fun calculateDailyScore(email: String) {
        if (hasAddedScoreToday) return
        val completedCount = checkStates.count { it }
        val score = when {
            completedCount >= 8 -> 10
            completedCount >= 5 -> 5
            completedCount >= 3 -> 3
            else -> 0
        }
        totalScore += score
        hasAddedScoreToday = true
        updateTotalScore(totalScore)

        // 立即保存到 Firebase
        saveDailyChallengeToFirebase(email)
    }


    fun hasCompletedToday(): Boolean = hasAddedScoreToday


    // 修正：允許重複購買
    fun redeemItem(name: String, cost: Int): Boolean {
        // 移除檢查是否已購買的邏輯，允許重複購買
        return if (totalScore >= cost) {
            totalScore -= cost
            redeemedItems.add(name)  // 每次購買都加入列表
            Log.d("ViewModel", "購買成功: $name, 剩餘分數: $totalScore, 已購買商品數: ${redeemedItems.size}")
            true
        } else {
            Log.d("ViewModel", "購買失敗: $name, 分數不足")
            false
        }
    }


    fun saveDailyChallengeToFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "score" to totalScore,
            "redeemedItems" to redeemedItems.toList(),  // 保存所有購買紀錄
            "itemPositions" to itemPositions.mapValues { mapOf("x" to it.value.x, "y" to it.value.y) }
        )
        db.collection("users").document(email)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ViewModel", "數據保存成功 - 分數: $totalScore")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "數據保存失敗: ${e.message}")
            }
    }


    fun loadDailyChallengeFromFirebase(email: String, onComplete: (() -> Unit)? = null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 使用 _totalScore.value 直接設置，避免觸發 setter
                    _totalScore.value = document.getLong("score")?.toInt() ?: 0

                    val checklist = (document["checklist"] as? List<*>)?.map { it as? Boolean ?: false }
                        ?: List(checklistItems.size) { false }
                    val redeemed = (document["redeemedItems"] as? List<*>)?.mapNotNull { it as? String } ?: listOf()
                    val positions = document["itemPositions"] as? Map<String, Map<String, Float>> ?: mapOf()

                    // 載入碳排放計算器日期
                    lastCarbonCalculatorDate = document.getString("lastCarbonCalculatorDate") ?: ""

                    //載入一拍即分數據
                    lastGarbageDate = document.getString("lastGarbageDate") ?: ""
                    garbageRewardCount = (document.getLong("garbageRewardCount")?.toInt()) ?: 0

                    //載入永續挑戰遊戲數據
                    lastQuizGameDate = document.getString("lastQuizGameDate") ?: ""
                    quizGamePlayCount = (document.getLong("quizGamePlayCount")?.toInt()) ?: 0

                    //載入 Turn 遊戲數據
                    lastTurnGameDate = document.getString("lastTurnGameDate") ?: ""
                    turnGamePlayCount = (document.getLong("turnGamePlayCount")?.toInt()) ?: 0

                    //載入 GarbageGame 數據
                    lastGarbageGameDate = document.getString("lastGarbageGameDate") ?: ""
                    garbageGamePlayCount = (document.getLong("garbageGamePlayCount")?.toInt()) ?: 0

                    // 檢查是否為新的一天
                    val today = LocalDate.now().toString()
                    if (lastGarbageDate != today) {
                        garbageRewardCount = 0
                    }
                    if (lastQuizGameDate != today) {
                        quizGamePlayCount = 0
                    }
                    if (lastTurnGameDate != today) {
                        turnGamePlayCount = 0
                    }
                    if (lastGarbageGameDate != today) {
                        garbageGamePlayCount = 0
                    }


                    itemPositions.clear()
                    positions.forEach { (name, pos) ->
                        itemPositions[name] = ItemPosition(pos["x"] ?: 0f, pos["y"] ?: 0f)
                    }


                    redeemedItems.clear()
                    redeemedItems.addAll(redeemed)

                    Log.d("ViewModel", "載入成功 - 分數: ${_totalScore.value}, 已購買商品數: ${redeemedItems.size}")
                }
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "載入失敗: ${e.message}")
                onComplete?.invoke()
            }
    }


    fun getItemPosition(itemName: String) = itemPositions[itemName] ?: ItemPosition(0f, 0f)


    fun updateItemPosition(itemName: String, x: Float, y: Float) {
        itemPositions[itemName] = ItemPosition(x, y)
    }


    fun updateTotalScore(scoreToAdd: Int) {
        totalScore += scoreToAdd
    }


    private fun updateScoreInFirebase(score: Int) {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(email)
                .update("score", score)
                .addOnSuccessListener {
                    Log.d("ViewModel", "Score successfully updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.w("ViewModel", "Error updating score", e)
                }
        } else {
            Log.e("ViewModel", "User email is null, cannot update score")
        }
    }


    // ========== 新增：草地物品的保存和載入功能 ==========


    /**
     * 保存草地上已放置物品的位置到 Firebase
     * @param email 用戶 email
     * @param placedItems 已放置的物品列表
     */
    fun savePlacedItemsToFirebase(email: String, placedItems: List<PlacedItem>) {
        val db = FirebaseFirestore.getInstance()


        // 將 PlacedItem 轉換為 Map 格式以便儲存
        val itemsData = placedItems.map { item ->
            mapOf(
                "id" to item.id,
                "imageRes" to item.imageRes,
                "description" to item.description,
                "x" to item.x,
                "y" to item.y,
                "scale" to item.scale
            )
        }


        db.collection("users")
            .document(email)
            .collection("forest")
            .document("placedItems")
            .set(mapOf("items" to itemsData))
            .addOnSuccessListener {
                Log.d("ViewModel", "草地物品位置保存成功，共 ${placedItems.size} 個物品")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "草地物品位置保存失敗: ${e.message}")
            }
    }


    /**
     * 從 Firebase 載入草地上已放置物品的位置
     * @param email 用戶 email
     * @param onLoaded 載入完成後的回調函數，返回物品列表
     */
    fun loadPlacedItemsFromFirebase(email: String, onLoaded: (List<PlacedItem>) -> Unit) {
        val db = FirebaseFirestore.getInstance()


        db.collection("users")
            .document(email)
            .collection("forest")
            .document("placedItems")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val itemsData = document.get("items") as? List<Map<String, Any>> ?: emptyList()


                    val placedItems = itemsData.mapNotNull { itemMap ->
                        try {
                            PlacedItem(
                                id = (itemMap["id"] as? Long)?.toInt() ?: 0,
                                imageRes = (itemMap["imageRes"] as? Long)?.toInt() ?: 0,
                                description = itemMap["description"] as? String ?: "",
                                x = (itemMap["x"] as? Number)?.toFloat() ?: 400f,
                                y = (itemMap["y"] as? Number)?.toFloat() ?: 600f,
                                scale = (itemMap["scale"] as? Number)?.toFloat() ?: 1f
                            )
                        } catch (e: Exception) {
                            Log.e("ViewModel", "載入物品資料時出錯: ${e.message}")
                            null
                        }
                    }


                    onLoaded(placedItems)
                    Log.d("ViewModel", "成功載入 ${placedItems.size} 個草地物品")
                } else {
                    onLoaded(emptyList())
                    Log.d("ViewModel", "沒有找到已保存的草地物品")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "載入草地物品失敗: ${e.message}")
                onLoaded(emptyList())
            }
    }


    // 在 ViewModel 類別中加入
    private val _lastCarbonCalculatorDate = mutableStateOf("")
    var lastCarbonCalculatorDate: String
        get() = _lastCarbonCalculatorDate.value
        set(value) { _lastCarbonCalculatorDate.value = value }

    /**
     * 檢查今天是否可以從碳排放計算器獲得分數
     * @return true 表示可以獲得分數，false 表示今天已經獲得過了
     */
    fun canGetCarbonCalculatorReward(): Boolean {
        val today = LocalDate.now().toString()
        return lastCarbonCalculatorDate != today
    }

    /**
     * 碳排放計算器獎勵 - 每天第一次使用可獲得 5 分
     * @param email 使用者 email
     * @return true 表示成功獲得分數，false 表示今天已經獲得過
     */
    fun rewardCarbonCalculator(email: String): Boolean {
        if (canGetCarbonCalculatorReward()) {
            val today = LocalDate.now().toString()
            lastCarbonCalculatorDate = today
            totalScore += 5

            // 儲存到 Firebase
            saveCarbonCalculatorDateToFirebase(email, today)

            Log.d("ViewModel", "碳排放計算器獎勵：獲得 5 分，當前總分: $totalScore")
            return true
        }
        Log.d("ViewModel", "碳排放計算器獎勵：今天已經獲得過了")
        return false
    }

    /**
     * 儲存碳排放計算器最後使用日期到 Firebase
     */
    private fun saveCarbonCalculatorDateToFirebase(email: String, date: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email)
            .update("lastCarbonCalculatorDate", date)
            .addOnSuccessListener {
                Log.d("ViewModel", "碳排放計算器日期儲存成功: $date")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "碳排放計算器日期儲存失敗: ${e.message}")
            }
    }

    /**
     * 從 Firebase 載入碳排放計算器最後使用日期
     */
    fun loadCarbonCalculatorDateFromFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    lastCarbonCalculatorDate = document.getString("lastCarbonCalculatorDate") ?: ""
                    Log.d("ViewModel", "載入碳排放計算器日期: $lastCarbonCalculatorDate")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "載入碳排放計算器日期失敗: ${e.message}")
            }
    }

    // 一拍即分相關狀態
    private val _lastGarbageDate = mutableStateOf("")
    var lastGarbageDate: String
        get() = _lastGarbageDate.value
        set(value) { _lastGarbageDate.value = value }

    private val _garbageRewardCount = mutableStateOf(0)
    var garbageRewardCount: Int
        get() = _garbageRewardCount.value
        set(value) { _garbageRewardCount.value = value }

    /**
     * 檢查今天是否可以從一拍即分獲得分數
     * @return true 表示今天還有獲得分數的機會（未達3次）
     */
    fun canGetGarbageReward(): Boolean {
        val today = LocalDate.now().toString()

        // 如果是新的一天,重置計數
        if (lastGarbageDate != today) {
            garbageRewardCount = 0
            lastGarbageDate = today
        }

        return garbageRewardCount < 3
    }

    /**
     * 一拍即分獎勵 - 每天前3次可獲得 1 分
     * @param email 使用者 email
     * @return true 表示成功獲得分數，false 表示今天已達上限
     */
    fun rewardGarbageClassification(email: String): Boolean {
        if (canGetGarbageReward()) {
            val today = LocalDate.now().toString()

            // 如果是新的一天,重置計數
            if (lastGarbageDate != today) {
                garbageRewardCount = 0
                lastGarbageDate = today
            }

            garbageRewardCount += 1
            totalScore += 1

            // 儲存到 Firebase
            saveGarbageDataToFirebase(email, today, garbageRewardCount)

            Log.d("ViewModel", "一拍即分獎勵：獲得 1 分 (今日第 $garbageRewardCount 次)，當前總分: $totalScore")
            return true
        }

        Log.d("ViewModel", "一拍即分獎勵：今天已達上限 (3次)")
        return false
    }

    /**
     * 獲取今日剩餘獎勵次數
     */
    fun getRemainingGarbageRewards(): Int {
        val today = LocalDate.now().toString()

        // 如果是新的一天,重置計數
        if (lastGarbageDate != today) {
            return 3
        }

        return 3 - garbageRewardCount
    }

    /**
     * 儲存一拍即分數據到 Firebase
     */
    private fun saveGarbageDataToFirebase(email: String, date: String, count: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email)
            .update(
                mapOf(
                    "lastGarbageDate" to date,
                    "garbageRewardCount" to count,
                    "score" to totalScore
                )
            )
            .addOnSuccessListener {
                Log.d("ViewModel", "一拍即分數據儲存成功: 日期=$date, 次數=$count")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "一拍即分數據儲存失敗: ${e.message}")
            }
    }

    /**
     * 從 Firebase 載入一拍即分數據
     */
    fun loadGarbageDataFromFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    lastGarbageDate = document.getString("lastGarbageDate") ?: ""
                    garbageRewardCount = (document.getLong("garbageRewardCount")?.toInt()) ?: 0

                    // 檢查是否為新的一天
                    val today = LocalDate.now().toString()
                    if (lastGarbageDate != today) {
                        garbageRewardCount = 0
                    }

                    Log.d("ViewModel", "載入一拍即分數據: 日期=$lastGarbageDate, 次數=$garbageRewardCount")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "載入一拍即分數據失敗: ${e.message}")
            }
    }
    // 永續挑戰遊戲相關狀態
    private val _lastQuizGameDate = mutableStateOf("")
    var lastQuizGameDate: String
        get() = _lastQuizGameDate.value
        set(value) { _lastQuizGameDate.value = value }

    private val _quizGamePlayCount = mutableStateOf(0)
    var quizGamePlayCount: Int
        get() = _quizGamePlayCount.value
        set(value) { _quizGamePlayCount.value = value }

    /**
     * 檢查今天是否可以玩永續挑戰遊戲
     * @param maxPlays 每天最大遊玩次數，默認為3次
     * @return true 表示今天還有遊玩機會
     */
    fun canPlayQuizGame(maxPlays: Int = 3): Boolean {
        val today = LocalDate.now().toString()

        // 如果是新的一天，重置計數
        if (lastQuizGameDate != today) {
            quizGamePlayCount = 0
            lastQuizGameDate = today
        }

        return quizGamePlayCount < maxPlays
    }

    /**
     * 獲取今日剩餘遊玩次數
     * @param maxPlays 每天最大遊玩次數，默認為3次
     */
    fun getRemainingQuizGamePlays(maxPlays: Int = 3): Int {
        val today = LocalDate.now().toString()

        // 如果是新的一天，返回最大次数
        if (lastQuizGameDate != today) {
            return maxPlays
        }

        return maxPlays - quizGamePlayCount
    }

    /**
     * 紀錄一次遊戲遊玩（遊戲結束時適用）
     * @param email 使用者 email
     * @param score 本次遊戲得分
     * @return true 表示成功紀錄，false 表示今天已達上限
     */
    fun recordQuizGamePlay(email: String, score: Int): Boolean {
        if (canPlayQuizGame()) {
            val today = LocalDate.now().toString()

            // 如果是新的一天，重置計數
            if (lastQuizGameDate != today) {
                quizGamePlayCount = 0
                lastQuizGameDate = today
            }

            quizGamePlayCount += 1
            totalScore += score  // 添加分數

            // 儲存到 Firebase
            saveQuizGameDataToFirebase(email, today, quizGamePlayCount)

            Log.d("ViewModel", "永續挑戰遊戲紀錄：本次得分 $score，今日第 $quizGamePlayCount 次，當前總分: $totalScore")
            return true
        }

        Log.d("ViewModel", "永續挑戰遊戲：今天已達上限")
        return false
    }

    /**
     * 儲存永續挑戰遊戲數據到 Firebase
     */
    private fun saveQuizGameDataToFirebase(email: String, date: String, count: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email)
            .update(
                mapOf(
                    "lastQuizGameDate" to date,
                    "quizGamePlayCount" to count,
                    "score" to totalScore
                )
            )
            .addOnSuccessListener {
                Log.d("ViewModel", "永續挑戰遊戲數據儲存成功: 日期=$date, 次數=$count")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "永續挑戰遊戲數據儲存失敗: ${e.message}")
            }
    }

    /**
     * 從 Firebase 載入永續挑戰遊戲數據
     */
    fun loadQuizGameDataFromFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    lastQuizGameDate = document.getString("lastQuizGameDate") ?: ""
                    quizGamePlayCount = (document.getLong("quizGamePlayCount")?.toInt()) ?: 0

                    // 檢查是否為新的一天
                    val today = LocalDate.now().toString()
                    if (lastQuizGameDate != today) {
                        quizGamePlayCount = 0
                    }

                    Log.d("ViewModel", "載入永續挑戰遊戲數據: 日期=$lastQuizGameDate, 次數=$quizGamePlayCount")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "永續挑戰遊戲數據儲存失敗: ${e.message}")
            }
    }
    // Turn 翻牌遊戲相關狀態
    private val _lastTurnGameDate = mutableStateOf("")
    var lastTurnGameDate: String
        get() = _lastTurnGameDate.value
        set(value) { _lastTurnGameDate.value = value }

    private val _turnGamePlayCount = mutableStateOf(0)
    var turnGamePlayCount: Int
        get() = _turnGamePlayCount.value
        set(value) { _turnGamePlayCount.value = value }

    /**
     * 檢查今天是否可以玩 Turn 翻牌遊戲
     * @param maxPlays 每天最大遊玩次數，默認為3次
     * @return true 表示今天還有遊玩機會
     */
    fun canPlayTurnGame(maxPlays: Int = 3): Boolean {
        val today = LocalDate.now().toString()

        // 如果是新的一天，重置計數
        if (lastTurnGameDate != today) {
            turnGamePlayCount = 0
            lastTurnGameDate = today
        }

        return turnGamePlayCount < maxPlays
    }

    /**
     * 獲取今日剩餘 Turn 遊玩次數
     * @param maxPlays 每天最大遊玩次數，默認為3次
     */
    fun getRemainingTurnGamePlays(maxPlays: Int = 3): Int {
        val today = LocalDate.now().toString()

        // 如果是新的一天，返回最大次數
        if (lastTurnGameDate != today) {
            return maxPlays
        }

        return maxPlays - turnGamePlayCount
    }

    /**
     * 紀錄一次 Turn 遊戲遊玩（遊戲結束時調用）
     * @param email 使用者 email
     * @param score 本次遊戲得分
     * @return true 表示成功紀錄，false 表示今天已達上限
     */
    fun recordTurnGamePlay(email: String, score: Int): Boolean {
        if (canPlayTurnGame()) {
            val today = LocalDate.now().toString()

            // 如果是新的一天，重置計數
            if (lastTurnGameDate != today) {
                turnGamePlayCount = 0
                lastTurnGameDate = today
            }

            turnGamePlayCount += 1
            totalScore += score  // 添加分數

            // 儲存到 Firebase
            saveTurnGameDataToFirebase(email, today, turnGamePlayCount)

            Log.d("ViewModel", "Turn 遊戲紀錄：本次得分 $score，今日第 $turnGamePlayCount 次，當前總分: $totalScore")
            return true
        }

        Log.d("ViewModel", "Turn 遊戲：今天已達上限")
        return false
    }

    /**
     * 儲存 Turn 遊戲數據到 Firebase
     */
    private fun saveTurnGameDataToFirebase(email: String, date: String, count: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email)
            .update(
                mapOf(
                    "lastTurnGameDate" to date,
                    "turnGamePlayCount" to count,
                    "score" to totalScore
                )
            )
            .addOnSuccessListener {
                Log.d("ViewModel", "Turn 遊戲數據儲存成功: 日期=$date, 次數=$count")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Turn 遊戲數據儲存失敗: ${e.message}")
            }
    }

    /**
     * 從 Firebase 載入 Turn 遊戲數據
     */
    fun loadTurnGameDataFromFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    lastTurnGameDate = document.getString("lastTurnGameDate") ?: ""
                    turnGamePlayCount = (document.getLong("turnGamePlayCount")?.toInt()) ?: 0

                    // 檢查是否為新的一天
                    val today = LocalDate.now().toString()
                    if (lastTurnGameDate != today) {
                        turnGamePlayCount = 0
                    }

                    Log.d("ViewModel", "載入 Turn 遊戲數據: 日期=$lastTurnGameDate, 次數=$turnGamePlayCount")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "載入 Turn 遊戲數據失敗: ${e.message}")
            }
    }

    // GarbageGame 相關狀態
    private val _lastGarbageGameDate = mutableStateOf("")
    var lastGarbageGameDate: String
        get() = _lastGarbageGameDate.value
        set(value) { _lastGarbageGameDate.value = value }

    private val _garbageGamePlayCount = mutableStateOf(0)
    var garbageGamePlayCount: Int
        get() = _garbageGamePlayCount.value
        set(value) { _garbageGamePlayCount.value = value }

    /**
     * 檢查今天是否可以玩 GarbageGame
     * @param maxPlays 每天最大遊玩次數，默認為3次
     * @return true 表示今天還有遊玩機會
     */
    fun canPlayGarbageGame(maxPlays: Int = 3): Boolean {
        val today = LocalDate.now().toString()

        // 如果是新的一天，重置計數
        if (lastGarbageGameDate != today) {
            garbageGamePlayCount = 0
            lastGarbageGameDate = today
        }

        return garbageGamePlayCount < maxPlays
    }

    /**
     * 獲取今日剩餘 GarbageGame 遊玩次數
     * @param maxPlays 每天最大遊玩次數，默認為3次
     */
    fun getRemainingGarbageGamePlays(maxPlays: Int = 3): Int {
        val today = LocalDate.now().toString()

        // 如果是新的一天，返回最大次數
        if (lastGarbageGameDate != today) {
            return maxPlays
        }

        return maxPlays - garbageGamePlayCount
    }

    /**
     * 紀錄一次 GarbageGame 遊玩（遊戲結束時調用）
     * @param email 使用者 email
     * @param score 本次遊戲得分
     * @return true 表示成功紀錄，false 表示今天已達上限
     */
    fun recordGarbageGamePlay(email: String, score: Int): Boolean {
        if (canPlayGarbageGame()) {
            val today = LocalDate.now().toString()

            // 如果是新的一天，重置計數
            if (lastGarbageGameDate != today) {
                garbageGamePlayCount = 0
                lastGarbageGameDate = today
            }

            garbageGamePlayCount += 1
            totalScore += score  // 添加分數

            // 儲存到 Firebase
            saveGarbageGameDataToFirebase(email, today, garbageGamePlayCount)

            Log.d("ViewModel", "GarbageGame 紀錄：本次得分 $score，今日第 $garbageGamePlayCount 次，當前總分: $totalScore")
            return true
        }

        Log.d("ViewModel", "GarbageGame：今天已達上限")
        return false
    }

    /**
     * 儲存 GarbageGame 數據到 Firebase
     */
    private fun saveGarbageGameDataToFirebase(email: String, date: String, count: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email)
            .update(
                mapOf(
                    "lastGarbageGameDate" to date,
                    "garbageGamePlayCount" to count,
                    "score" to totalScore
                )
            )
            .addOnSuccessListener {
                Log.d("ViewModel", "GarbageGame 數據儲存成功: 日期=$date, 次數=$count")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "GarbageGame 數據儲存失敗: ${e.message}")
            }
    }

    /**
     * 從 Firebase 載入 GarbageGame 數據
     */
    fun loadGarbageGameDataFromFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    lastGarbageGameDate = document.getString("lastGarbageGameDate") ?: ""
                    garbageGamePlayCount = (document.getLong("garbageGamePlayCount")?.toInt()) ?: 0

                    // 檢查是否為新的一天
                    val today = LocalDate.now().toString()
                    if (lastGarbageGameDate != today) {
                        garbageGamePlayCount = 0
                    }

                    Log.d("ViewModel", "載入 GarbageGame 數據: 日期=$lastGarbageGameDate, 次數=$garbageGamePlayCount")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "載入 GarbageGame 數據失敗: ${e.message}")
            }
    }

}