// GreenViewModel.kt
package tw.edu.pu.csim.s1114702.green

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ViewModel : ViewModel() {
    val checklistItems = listOf(
        "攜帶環保餐具/杯/袋", "搭乘大眾運輸/騎腳踏車/步行", "做好資源回收分類", "選擇有機食物", "隨手關燈、拔插頭",
        "購買綠色商標商品", "選擇綠色旅遊行程", "節約用水"
    )
    val checkStates = mutableStateListOf(*Array(checklistItems.size) { false })
}
