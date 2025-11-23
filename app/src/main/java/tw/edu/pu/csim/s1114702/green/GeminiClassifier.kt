package tw.edu.pu.csim.s1114702.green

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiClassificationResult(
    val category: String,
    val reason: String,
    val isGarbage: Boolean
)

class GeminiClassifier(private val apiKey: String) {

    private val USE_MOCK_MODE = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun classifyGarbage(itemName: String, chineseName: String): GeminiClassificationResult {
        /*
        // ===== 測試模式（已停用，保留以備將來使用） =====
        if (USE_MOCK_MODE) {
            Log.d("GeminiClassifier", "🔧 使用模擬模式: $itemName")
            kotlinx.coroutines.delay(800)
            return when (itemName) {
                "banana", "apple", "orange", "broccoli", "carrot",
                "sandwich", "hot dog", "pizza", "donut", "cake" ->
                    GeminiClassificationResult("廚餘", "食物殘渣", true)
                "mouse", "keyboard", "laptop", "cell phone", "remote", "tv" ->
                    GeminiClassificationResult("回收", "電子產品", true)
                "bottle", "wine glass", "cup" ->
                    GeminiClassificationResult("回收", "可回收容器", true)
                "book" -> GeminiClassificationResult("回收", "紙類", true)
                "bicycle" -> GeminiClassificationResult("回收", "金屬", true)
                "hair drier", "microwave", "oven", "toaster", "refrigerator" ->
                    GeminiClassificationResult("回收", "小家電", true)
                "teddy bear" -> GeminiClassificationResult("一般垃圾", "玩具", true)
                "toothbrush" -> GeminiClassificationResult("一般垃圾", "日用品", true)
                "clock", "baseball bat", "baseball glove", "sports ball" ->
                    GeminiClassificationResult("一般垃圾", "雜物", true)
                "backpack", "handbag", "suitcase", "tie" ->
                    GeminiClassificationResult("一般垃圾", "布料", true)
                "umbrella", "skateboard", "surfboard", "tennis racket",
                "kite", "frisbee", "skis", "snowboard" ->
                    GeminiClassificationResult("回收", "需拆解", true)
                "vase", "spoon", "fork", "bowl", "knife", "scissors" ->
                    GeminiClassificationResult("回收", "視材質", true)
                "person", "cat", "dog", "bird", "bear", "elephant", "giraffe",
                "horse", "zebra", "sheep", "cow" ->
                    GeminiClassificationResult("其他", "生物", false)
                "car", "bus", "train", "airplane", "boat", "motorcycle", "truck" ->
                    GeminiClassificationResult("其他", "交通工具", false)
                "bed", "couch", "chair", "bench", "dining table" ->
                    GeminiClassificationResult("其他", "大型家具", false)
                "traffic light", "fire hydrant", "stop sign", "parking meter" ->
                    GeminiClassificationResult("其他", "公共設施", false)
                else -> GeminiClassificationResult("其他", "未知物品", false)
            }
        }
         */

        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiClassifier", "🔄 開始 REST API 呼叫: $chineseName ($itemName)")

                val prompt = """
你是一個台灣垃圾分類專家。請根據台灣的垃圾分類規則，判斷以下物品的分類。

偵測到的物品：$chineseName ($itemName)

請嚴格按照以下 JSON 格式回答，不要包含任何其他文字或 Markdown 標記：
{
  "category": "回收/一般垃圾/廚餘/其他",
  "reason": "簡短理由(10字內)",
  "isGarbage": true/false
}

台灣垃圾分類規則：

【回收類】(isGarbage=true)
- 塑膠類：塑膠瓶、塑膠杯、塑膠袋（乾淨）
- 紙類：紙箱、書本、報紙（乾淨無污染）
- 金屬：鋁罐、鐵罐、金屬餐具
- 玻璃：玻璃瓶、玻璃杯
- 電子產品：手機、電腦、滑鼠、鍵盤、遙控器、電視、筆電
- 家電：吹風機、烤箱、微波爐、冰箱
- 其他：腳踏車、雨傘（需拆解）

【一般垃圾類】(isGarbage=true)
- 受污染無法回收的物品：油膩的紙盒、髒塑膠袋
- 玩具：泰迪熊、球類
- 日用品：牙刷、時鐘
- 布料：背包、手提包、行李箱、領帶

【廚餘類】(isGarbage=true)
- 所有食物：水果、蔬菜、熟食、零食、蛋糕

【其他類】(isGarbage=false，這些不是垃圾)
- 人、動物
- 交通工具：汽車、公車、火車、飛機
- 大型家具：床、沙發、餐桌（這些需要特殊處理，不是一般垃圾分類範圍）
- 建築物、紅綠燈、消防栓等公共設施

重要規則：
1. 電子產品一律分類為「回收」
2. 小家電一律分類為「回收」
3. 人、動物、交通工具 → category="其他", isGarbage=false
4. 大型家具（床、沙發、餐桌）→ category="其他", isGarbage=false
5. 如果不確定，優先考慮是否為電子產品或小家電
""".trimIndent()

                // 使用 v1 API（穩定版本）
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-001:generateContent?key=$apiKey"

                val requestBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("topK", 32)
                        put("topP", 0.95)
                        put("maxOutputTokens", 256)
                    })
                }.toString()

                Log.d("GeminiClassifier", "📤 發送請求到: $url")

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("GeminiClassifier", "📥 HTTP Status: ${response.code}")

                if (!response.isSuccessful) {
                    if (response.code == 429) {
                        Log.w("GeminiClassifier", "⚠️ API 配額用完，請稍後再試")
                        return@withContext GeminiClassificationResult(
                            category = "其他",
                            reason = "配額用完",
                            isGarbage = false
                        )
                    }
                    Log.e("GeminiClassifier", "❌ API 錯誤: $responseBody")
                    return@withContext GeminiClassificationResult(
                        category = "其他",
                        reason = "API 錯誤",
                        isGarbage = false
                    )
                }

                Log.d("GeminiClassifier", "✅ API 回應成功")

                // 解析回應
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.getJSONArray("candidates")
                val content = candidates.getJSONObject(0)
                    .getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text").trim()

                Log.d("GeminiClassifier", "📝 回應內容: $text")

                val result = parseGeminiResponse(text)
                Log.d("GeminiClassifier", "📊 分類結果: ${result.category} - ${result.reason}")
                result

            } catch (e: Exception) {
                Log.e("GeminiClassifier", "❌ 請求失敗", e)
                GeminiClassificationResult(
                    category = "其他",
                    reason = "連線失敗",
                    isGarbage = false
                )
            }
        }
    }

    private fun parseGeminiResponse(text: String): GeminiClassificationResult {
        return try {
            val cleanText = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val categoryMatch = Regex(""""category":\s*"([^"]+)"""").find(cleanText)
            val reasonMatch = Regex(""""reason":\s*"([^"]+)"""").find(cleanText)
            val isGarbageMatch = Regex(""""isGarbage":\s*(true|false)""").find(cleanText)

            val category = categoryMatch?.groupValues?.get(1) ?: "其他"
            val reason = reasonMatch?.groupValues?.get(1) ?: "無法判斷"
            val isGarbage = isGarbageMatch?.groupValues?.get(1)?.toBoolean() ?: false

            GeminiClassificationResult(
                category = category,
                reason = reason,
                isGarbage = isGarbage
            )
        } catch (e: Exception) {
            Log.e("GeminiClassifier", "JSON 解析失敗", e)
            GeminiClassificationResult(
                category = "其他",
                reason = "解析失敗",
                isGarbage = false
            )
        }
    }
}