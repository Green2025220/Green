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

        // ===== 測試模式（private val USE_MOCK_MODE = false已停用，保留以備將來使用） =====
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
        // ===== 以上為測試模式（已停用，保留以備將來使用） =====

        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiClassifier", "🔄 開始 REST API 呼叫: $chineseName ($itemName)")

                val prompt = """
你是一個台灣垃圾分類專家。請判斷物品的分類。
物品: $chineseName

分類規則:
- 電子產品/小家電/紙類→回收
- 食物→廚餘
- 玩具/日用品→一般垃圾
- 人/動物/交通工具/大型家具→其他(非垃圾)

JSON格式: {"category":"回收/廚餘/一般垃圾/其他","reason":"理由5字內","isGarbage":true/false}
不要額外文字。
""".trimIndent()

                // 使用 v1 API（穩定版本）
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

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

                Log.d("GeminiClassifier", "📥 完整回應: $responseBody")

                // ✅ 改進的回應解析
                val jsonResponse = JSONObject(responseBody)

                val text = try {
                    // 標準格式
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val content = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    parts.getJSONObject(0).getString("text").trim()
                } catch (e: Exception) {
                    // 備用格式：有些模型直接返回 text
                    try {
                        val candidates = jsonResponse.getJSONArray("candidates")
                        candidates.getJSONObject(0).getString("text").trim()
                    } catch (e2: Exception) {
                        Log.e("GeminiClassifier", "無法解析回應", e2)
                        // 如果實在無法解析，返回整個 responseBody
                        responseBody
                    }
                }

                Log.d("GeminiClassifier", "📝 提取的文字: $text")

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