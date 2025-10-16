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
    var redeemedItems = mutableStateListOf<String>()


    private val itemPositions = mutableMapOf<String, ItemPosition>()


    // Firebase 資料庫引用
    private val database = FirebaseDatabase.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid


    init {
        userId?.let { loadScoreFromFirebase(it) }
    }


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


    fun calculateDailyScore() {
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
    }


    fun hasCompletedToday(): Boolean = hasAddedScoreToday


    fun redeemItem(name: String, cost: Int): Boolean {
        if (redeemedItems.contains(name)) return false
        return if (totalScore >= cost) {
            totalScore -= cost
            redeemedItems.add(name)
            true
        } else false
    }


    fun saveDailyChallengeToFirebase(email: String) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "score" to totalScore,
            //"checklist" to checkStates.toList(),
            "redeemedItems" to redeemedItems,
            "itemPositions" to itemPositions.mapValues { mapOf("x" to it.value.x, "y" to it.value.y) }
        )
        db.collection("users").document(email)
            .update(data)
    }


    fun loadDailyChallengeFromFirebase(email: String, onComplete: (() -> Unit)? = null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    totalScore = document.getLong("score")?.toInt() ?: 0
                    val checklist = (document["checklist"] as? List<*>)?.map { it as? Boolean ?: false }
                        ?: List(checklistItems.size) { false }
                    val redeemed = (document["redeemedItems"] as? List<*>)?.mapNotNull { it as? String } ?: listOf()
                    val positions = document["itemPositions"] as? Map<String, Map<String, Float>> ?: mapOf()


                    itemPositions.clear()
                    positions.forEach { (name, pos) ->
                        itemPositions[name] = ItemPosition(pos["x"] ?: 0f, pos["y"] ?: 0f)
                    }


                    redeemedItems.clear()
                    redeemedItems.addAll(redeemed)
                }
                onComplete?.invoke()
            }
            .addOnFailureListener {
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


    // 從 Firebase 載入用戶的分數
    private fun loadScoreFromFirebase(userId: String) {
        val scoreRef = database.getReference("users/$userId/score")
        scoreRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                totalScore = snapshot.getValue(Int::class.java) ?: 0
                Log.d("ViewModel", "Loaded score from Firebase: $totalScore")
            }
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
}

