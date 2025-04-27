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
            resetChecklist()
            hasAddedScoreToday = false
            lastCheckedDate = today
        }

        loadDailyChallengeFromFirebase(email) {
            if (isNewDay) {
                // 如果是新的一天，Firebase 載入後也要清空勾選狀態（避免覆蓋掉 resetChecklist）
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
            "checklist" to checkStates.toList(),
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

    fun updateTotalScore(newScore: Int) {
        totalScore += newScore
        // 呼叫 Firebase 更新分數
        updateScoreInFirebase(totalScore)
    }

    private fun updateScoreInFirebase(score: Int) {
        userId?.let {
            val userScoreRef = database.getReference("users/$it/score")
            userScoreRef.setValue(score) // 設置使用者的分數
        } ?: Log.e("ViewModel", "User ID is null, unable to update score")
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
}
