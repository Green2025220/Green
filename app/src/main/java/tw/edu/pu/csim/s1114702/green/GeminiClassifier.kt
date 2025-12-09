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

// ===== 原有的分類結果 =====
data class GeminiClassificationResult(
    val category: String,
    val reason: String,
    val isGarbage: Boolean
)

// ===== 新增：材質分析結果 =====
data class MaterialAnalysisResult(
    val material: String,                    // 主要材質
    val materialCode: String,                // 材質代碼
    val isComposite: Boolean,                // 是否為複合材質
    val compositeDescription: String,        // 複合材質說明
    val recyclable: Boolean,                 // 可回收性
    val tips: List<String>,                  // 回收小知識
    val disassemblySteps: List<String>,      // 拆解步驟
    val error: String?                       // 錯誤訊息
) {
    companion object {
        fun createError(message: String) = MaterialAnalysisResult(
            material = "未知",
            materialCode = "",
            isComposite = false,
            compositeDescription = "",
            recyclable = false,
            tips = emptyList(),
            disassemblySteps = emptyList(),
            error = message
        )
    }
}

class GeminiClassifier(private val apiKey: String) {
    init {
        Log.d("GeminiClassifier", "========== API KEY 診斷 ==========")
        Log.d("GeminiClassifier", "📌 API Key 長度: ${apiKey.length}")
        Log.d("GeminiClassifier", "📌 API Key 前15字元: ${apiKey.take(15)}")
        Log.d("GeminiClassifier", "📌 是否為空: ${apiKey.isEmpty()}")
        Log.d("GeminiClassifier", "====================================")
    }

    private val USE_MOCK_MODE = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ===== 原有的垃圾分類方法 =====
    suspend fun classifyGarbage(itemName: String, chineseName: String): GeminiClassificationResult {

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

                Log.d("GeminiClassifier", "📤 發送請求到 Gemini API")

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("GeminiClassifier", "📥 HTTP Status: ${response.code}")

                if (!response.isSuccessful) {
                    Log.e("GeminiClassifier", "❌ API 錯誤: ${response.code}")
                    Log.e("GeminiClassifier", "❌ 錯誤內容: $responseBody")

                    // 提供更詳細的錯誤訊息
                    val errorReason = when (response.code) {
                        400 -> "請求格式錯誤"
                        401 -> "API Key 無效"
                        403 -> "API Key 沒有權限"
                        404 -> "模型不存在"
                        429 -> "配額已用完"
                        500, 503 -> "伺服器錯誤"
                        else -> "API 錯誤"
                    }

                    return@withContext GeminiClassificationResult(
                        category = "其他",
                        reason = errorReason,
                        isGarbage = false
                    )
                }

                Log.d("GeminiClassifier", "✅ API 回應成功")
                Log.d("GeminiClassifier", "📥 完整回應: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val text = try {
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val content = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    parts.getJSONObject(0).getString("text").trim()
                } catch (e: Exception) {
                    Log.e("GeminiClassifier", "無法解析回應", e)
                    responseBody
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

    // ===== 新增：材質詳細分析方法 =====
    suspend fun analyzeMaterialDetails(
        englishName: String,
        chineseName: String
    ): MaterialAnalysisResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiClassifier", "🔬 開始材質分析: $chineseName")

                val prompt = """
物品：$chineseName ($englishName)

請提供詳細的回收分析，包含：

1. **主要材質**：具體材質名稱（如：PET塑膠、玻璃、不鏽鋼等）
2. **材質代碼**：如果是塑膠，請標示回收編號（1-7號）
3. **是否為複合材質**：如果是，列出所有材質成分
4. **可回收性**：明確說明是否可回收
5. **回收小知識**（至少4條）：
   - 回收前的處理方式
   - 清洗或分類注意事項
   - 常見錯誤做法
   - 環保小提醒
6. **拆解指南**（如果需要拆解）：
   - 具體拆解步驟
   - 各部分如何分類
   - 拆解注意事項

請用繁體中文回答，格式如下：
材質：[主要材質]
代碼：[材質代碼或"無"]
複材：[是/否，如果是請說明]
可回收：[是/否]
知識：
- [知識點1]
- [知識點2]
- [知識點3]
- [知識點4]
拆解：
- [步驟1或"不需拆解"]
- [步驟2]
""".trimIndent()

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
                        put("maxOutputTokens", 1024)
                    })
                }.toString()

                Log.d("GeminiClassifier", "📤 發送材質分析請求")
                Log.d("GeminiClassifier", "🌐 URL: $url")

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e("GeminiClassifier", "❌ 材質分析 API 錯誤: ${response.code}")
                    Log.e("GeminiClassifier", "❌ 錯誤內容: $responseBody")
                    return@withContext MaterialAnalysisResult.createError("API 錯誤 (${response.code})")
                }

                val jsonResponse = JSONObject(responseBody)
                val text = try {
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val content = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    parts.getJSONObject(0).getString("text").trim()
                } catch (e: Exception) {
                    Log.e("GeminiClassifier", "解析回應失敗", e)
                    return@withContext MaterialAnalysisResult.createError("解析失敗")
                }

                Log.d("GeminiClassifier", "✅ 材質分析成功")
                Log.d("GeminiClassifier", "📋 材質分析結果: $text")

                return@withContext parseMaterialAnalysis(text)

            } catch (e: Exception) {
                Log.e("GeminiClassifier", "❌ 材質分析失敗", e)
                MaterialAnalysisResult.createError("連線失敗")
            }
        }
    }

    // ===== 解析材質分析結果 =====
    private fun parseMaterialAnalysis(aiResponse: String): MaterialAnalysisResult {
        try {
            val lines = aiResponse.lines()
            var material = "未知"
            var code = ""
            var isComposite = false
            var compositeDesc = ""
            var recyclable = true
            val tips = mutableListOf<String>()
            val disassembly = mutableListOf<String>()

            var currentSection = ""

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("材質：") || trimmed.startsWith("材質:") -> {
                        material = trimmed.substringAfter("：").substringAfter(":").trim()
                    }
                    trimmed.startsWith("代碼：") || trimmed.startsWith("代碼:") -> {
                        code = trimmed.substringAfter("：").substringAfter(":").trim()
                        if (code == "無" || code == "无") code = ""
                    }
                    trimmed.startsWith("複材：") || trimmed.startsWith("複材:") -> {
                        val content = trimmed.substringAfter("：").substringAfter(":").trim()
                        isComposite = content.startsWith("是")
                        if (isComposite) {
                            compositeDesc = content.substringAfter("是").trim()
                                .removePrefix("，").removePrefix(",").trim()
                        }
                    }
                    trimmed.startsWith("可回收：") || trimmed.startsWith("可回收:") -> {
                        recyclable = trimmed.substringAfter("：").substringAfter(":").trim().startsWith("是")
                    }
                    trimmed.startsWith("知識：") || trimmed.startsWith("知識:") -> {
                        currentSection = "tips"
                    }
                    trimmed.startsWith("拆解：") || trimmed.startsWith("拆解:") -> {
                        currentSection = "disassembly"
                    }
                    trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("*") -> {
                        val content = trimmed
                            .removePrefix("-").removePrefix("•").removePrefix("*")
                            .trim()
                        if (content.isNotEmpty()) {
                            when (currentSection) {
                                "tips" -> tips.add(content)
                                "disassembly" -> disassembly.add(content)
                            }
                        }
                    }
                }
            }

            return MaterialAnalysisResult(
                material = material,
                materialCode = code,
                isComposite = isComposite,
                compositeDescription = compositeDesc,
                recyclable = recyclable,
                tips = tips,
                disassemblySteps = disassembly,
                error = null
            )

        } catch (e: Exception) {
            Log.e("GeminiClassifier", "解析材質資料失敗", e)
            return MaterialAnalysisResult.createError("解析失敗")
        }
    }
}
