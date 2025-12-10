package tw.edu.pu.csim.s1114702.green

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
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

    // ⭐ 根據官方文檔，使用不帶版本號的模型名稱
    private val RECOMMENDED_MODELS = listOf(
        "gemini-2.5-flash",      // 最新穩定版
        "gemini-2.0-flash",      // 2.0 版本
        "gemini-1.5-flash",      // 1.5 版本
        "gemini-1.5-pro",        // Pro 版本
        "gemini-pro"             // 基礎版本
    )

    // 當前使用的模型
    private var currentModel: String? = null
    private var modelInitialized = false

    // ⭐ 新增：請求追蹤（配額管理）
    private val requestTimestamps = mutableListOf<Long>()
    private val maxRequestsPerMinute = 4  // Gemini 2.5 限制是 5，設 4 保險

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 自動查找並設定可用的模型
     */
    suspend fun initializeModel(): Boolean {
        if (modelInitialized && currentModel != null) {
            return true
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiClassifier", "🔍 開始查找可用模型...")

                // 方法 1: 先嘗試推薦的模型
                for (model in RECOMMENDED_MODELS) {
                    if (testModelQuick(model)) {
                        currentModel = model
                        modelInitialized = true
                        Log.d("GeminiClassifier", "✅ 使用模型: $model")
                        return@withContext true
                    }
                }

                // 方法 2: 如果推薦模型都不行，查詢所有可用模型
                Log.d("GeminiClassifier", "📋 查詢 API 支援的所有模型...")
                val availableModels = listAvailableModels()

                for (modelInfo in availableModels) {
                    if (testModelQuick(modelInfo.name)) {
                        currentModel = modelInfo.name
                        modelInitialized = true
                        Log.d("GeminiClassifier", "✅ 使用模型: ${modelInfo.name}")
                        return@withContext true
                    }
                }

                Log.e("GeminiClassifier", "❌ 找不到任何可用的模型")
                false

            } catch (e: Exception) {
                Log.e("GeminiClassifier", "❌ 初始化失敗", e)
                false
            }
        }
    }

    /**
     * 快速測試模型是否可用
     */
    private suspend fun testModelQuick(modelName: String): Boolean {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent?key=$apiKey"

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "hi")
                            })
                        })
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                Log.d("GeminiClassifier", "  ✅ $modelName 可用")
            } else {
                Log.d("GeminiClassifier", "  ❌ $modelName 不可用 (${response.code})")
            }

            response.close()
            success

        } catch (e: Exception) {
            Log.d("GeminiClassifier", "  ❌ $modelName 測試失敗")
            false
        }
    }

    /**
     * 列出所有可用模型（使用 v1beta API）
     */
    private suspend fun listAvailableModels(): List<ModelInfo> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return emptyList()
            }

            val jsonResponse = JSONObject(responseBody)
            val modelsArray = jsonResponse.getJSONArray("models")

            val modelsList = mutableListOf<ModelInfo>()

            for (i in 0 until modelsArray.length()) {
                val modelObj = modelsArray.getJSONObject(i)
                val fullName = modelObj.getString("name")
                val modelName = fullName.substringAfter("models/")

                val supportedMethods = modelObj.optJSONArray("supportedGenerationMethods")
                    ?: JSONArray()
                val supportsGenerate = (0 until supportedMethods.length())
                    .map { supportedMethods.getString(it) }
                    .contains("generateContent")

                if (supportsGenerate) {
                    modelsList.add(ModelInfo(
                        name = modelName,
                        displayName = modelObj.optString("displayName", modelName)
                    ))
                    Log.d("GeminiClassifier", "  📌 發現: $modelName")
                }
            }

            modelsList

        } catch (e: Exception) {
            Log.e("GeminiClassifier", "查詢模型列表失敗", e)
            emptyList()
        }
    }
    /*
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

     */

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

        // 確保模型已初始化
        if (!modelInitialized) {
            val initialized = initializeModel()
            if (!initialized || currentModel == null) {
                return GeminiClassificationResult(
                    category = "其他",
                    reason = "模型初始化失敗",
                    isGarbage = false
                )
            }
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiClassifier", "🔄 開始 REST API 呼叫: $chineseName ($itemName)")
                Log.d("GeminiClassifier", "📌 使用模型: $currentModel")

                // ⭐ 檢查配額
                //checkAndWaitForQuota()

                val prompt = """
你是台灣垃圾分類專家。請判斷物品「$chineseName」的垃圾分類。

分類規則:
1. 回收 (isGarbage=true): 電子產品、小家電、金屬、塑膠、紙類、玻璃、容器
2. 廚餘 (isGarbage=true): 食物殘渣、果皮、茶葉渣
3. 一般垃圾 (isGarbage=true): 玩具、日用品、文具、衣物、布料
4. 其他 (isGarbage=false): 生物(人/動物)、交通工具、大型家具、公共設施

直接輸出JSON: {"category":"分類","reason":"理由","isGarbage":true/false}
""".trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1/models/$currentModel:generateContent?key=$apiKey"

                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
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
                        put("maxOutputTokens", 512)
                    })
                }.toString()

                Log.d("GeminiClassifier", "📤 發送請求到 Gemini API")
                Log.d("GeminiClassifier", "🌐 URL: $url")

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

        // 確保模型已初始化
        if (!modelInitialized) {
            initializeModel()
        }

        if (currentModel == null) {
            return MaterialAnalysisResult.createError("模型未初始化")
        }

        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiClassifier", "🔬 開始材質分析: $chineseName")
                Log.d("GeminiClassifier", "📌 使用模型: $currentModel")

                // ⭐ 檢查配額
                //checkAndWaitForQuota()

                val prompt = """
物品：$chineseName ($englishName)

物品：$chineseName

嚴格按照以下格式回答，不要額外說明：

材質：[主要材質名稱]
代碼：[塑膠回收編號1-7或"無"]
複材：[是/否，說明成分]
可回收：[是/否]
知識：
- [回收前處理]
- [清洗注意事項]
- [常見錯誤]
- [環保提醒]
拆解：
- [步驟1或"不需拆解"]
- [步驟2]

範例：
材質：聚酯纖維
代碼：無
複材：是，含棉花填充物
可回收：否
知識：
- 布類玩具不可回收
- 捐贈前需清洗乾淨
- 不可混入紙類回收
- 考慮二手捐贈
拆解：
- 不需拆解
""".trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1/models/$currentModel:generateContent?key=$apiKey"

                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
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
                        put("maxOutputTokens", 2048)
                    })
                }.toString()

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
    data class ModelInfo(
        val name: String,
        val displayName: String
    )
}
