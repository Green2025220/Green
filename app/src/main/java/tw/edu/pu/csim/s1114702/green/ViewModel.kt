package tw.edu.pu.csim.s1114702.green

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalDateTime

data class ItemPosition(val x: Float, val y: Float)

class ViewModel : ViewModel() {

    // 每日挑戰項目
    val checklistItems = listOf(
        "攜帶環保餐具/杯/袋", "搭乘大眾運輸/騎腳踏車/步行",
        "做好資源回收分類", "選擇有機食物", "隨手關燈、拔插頭",
        "購買綠色商標商品", "選擇綠色旅遊行程", "節約用水"
    )

    // 勾選狀態列表
    val checkStates = mutableStateListOf(*Array(checklistItems.size) { false })

    // 累積分數
    var totalScore by mutableStateOf(0)
        private set

    // 已兌換的商品
    var redeemedItems = mutableStateListOf<String>()
        private set

    // 上次檢查日期與今日是否加分
    private var lastCheckedDate: LocalDate = LocalDate.now()
    private var hasAddedScoreToday = false

    fun checkAndResetDaily() {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        if (today != lastCheckedDate) {
            resetChecklist()
            lastCheckedDate = today
            hasAddedScoreToday = false
        }
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

    private fun resetChecklist() {
        checkStates.replaceAll { false }
    }

    fun hasCompletedToday(): Boolean {
        return hasAddedScoreToday
    }

    // ✅ 用來兌換商品，會自動扣分並記錄兌換紀錄
    fun redeemItem(name: String, cost: Int): Boolean {
        return if (totalScore >= cost && !redeemedItems.contains(name)) {
            totalScore -= cost
            redeemedItems.add(name)
            true
        } else {
            false
        }
    }
    // 儲存每個商品的座標
    var itemPositions = mutableStateMapOf<String, ItemPosition>()
        private set

    // 更新位置
    fun updateItemPosition(name: String, x: Float, y: Float) {
        itemPositions[name] = ItemPosition(x, y)
    }

    // 取得位置（預設在螢幕中央附近）
    fun getItemPosition(name: String): ItemPosition {
        return itemPositions[name] ?: ItemPosition(200f, 400f)
    }
}
