package tw.edu.pu.csim.s1114702.green

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalDateTime

class ViewModel : ViewModel() {

    // 每日挑戰項目
    val checklistItems = listOf(
        "攜帶環保餐具/杯/袋", "搭乘大眾運輸/騎腳踏車/步行",
        "做好資源回收分類", "選擇有機食物", "隨手關燈、拔插頭",
        "購買綠色商標商品", "選擇綠色旅遊行程", "節約用水"
    )

    // 勾選狀態列表，使用 mutableStateList 以便於 Compose 更新界面
    val checkStates = mutableStateListOf(*Array(checklistItems.size) { false })

    // 累積的總分數
    var totalScore by mutableStateOf(0)
        private set

    // 上次檢查的日期
    private var lastCheckedDate: LocalDate = LocalDate.now()

    // 是否已經加過分數（確保每天只加一次分數）
    private var hasAddedScoreToday = false

    // 每次進入每日/商店畫面時呼叫此方法
    fun checkAndResetDaily() {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        if (today != lastCheckedDate) {
            // 日期變更時，重設勾選清單與狀態
            resetChecklist()
            lastCheckedDate = today
            hasAddedScoreToday = false
        }
    }

    // 計算每日挑戰的分數（只會執行一次）
    fun calculateDailyScore() {
        if (hasAddedScoreToday) return  // 防止重複加分

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

    // 重設勾選清單
    private fun resetChecklist() {
        checkStates.replaceAll { false }
    }

    // 花費分數（兌換商品）
    fun spendPoints(amount: Int): Boolean {
        return if (totalScore >= amount) {
            totalScore -= amount
            true
        } else {
            false
        }
    }

    // 檢查今天是否已完成（供 UI 使用）
    fun hasCompletedToday(): Boolean {
        return hasAddedScoreToday
    }
}
