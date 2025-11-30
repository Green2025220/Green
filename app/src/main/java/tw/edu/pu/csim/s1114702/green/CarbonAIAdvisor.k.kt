package tw.edu.pu.csim.s1114702.green

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class CarbonAIAnalysis(
    val environmentalImpact: String,
    val actionSuggestions: List<String>,
    val funFact: String,
    val severity: String
)

class CarbonAIAdvisor(private val apiKey: String) {

    // 嘗試多個可能的模型名稱
    private val modelCandidates = listOf(
        "gemini-2.0-flash",           // 與 GeminiClassifier.kt 相同
        "gemini-2.0-flash-exp",
        "gemini-1.5-flash",
        "gemini-1.5-pro"
    )

    private var workingModel: String? = null

    suspend fun analyzeCarbonImpact(
        carbonAmount: Double,
        transportType: String,
        distance: Double
    ): CarbonAIAnalysis {
        return withContext(Dispatchers.IO) {
            try {
                // 如果還沒找到可用的模型,先測試
                if (workingModel == null) {
                    workingModel = findWorkingModel()
                }

                if (workingModel == null) {
                    Log.e("CarbonAIAdvisor", "找不到可用的模型")
                    return@withContext getFallbackAnalysis(carbonAmount, transportType)
                }

                val prompt = buildPrompt(carbonAmount, transportType, distance)
                val response = callGeminiAPI(prompt, workingModel!!)
                parseAIResponse(response)
            } catch (e: Exception) {
                Log.e("CarbonAIAdvisor", "AI 分析失敗", e)
                getFallbackAnalysis(carbonAmount, transportType)
            }
        }
    }

    private suspend fun findWorkingModel(): String? {
        return withContext(Dispatchers.IO) {
            for (model in modelCandidates) {
                try {
                    Log.d("CarbonAIAdvisor", "測試模型: $model")
                    val testPrompt = "test"
                    callGeminiAPI(testPrompt, model)
                    Log.i("CarbonAIAdvisor", "找到可用模型: $model")
                    return@withContext model
                } catch (e: Exception) {
                    Log.d("CarbonAIAdvisor", "模型 $model 不可用: ${e.message}")
                }
            }
            null
        }
    }

    private fun buildPrompt(carbonAmount: Double, transportType: String, distance: Double): String {
        return """
你是一位環保專家。使用者剛剛使用「$transportType」行駛了 $distance 公里,產生了 $carbonAmount 公斤的碳排放。

請以**台灣使用者**的角度,用**繁體中文**分析這個碳排放量,並提供具體建議。請用 JSON 格式回答(不要包含 markdown 符號):

{
  "environmentalImpact": "簡短描述這個碳排放量對地球的影響(2-3句話,要具體且生動)",
  "actionSuggestions": [
    "建議1:具體的減碳行動",
    "建議2:生活習慣調整",
    "建議3:長期環保行動"
  ],
  "funFact": "一個有趣的對比數據,讓使用者更有感",
  "severity": "根據碳排放量判斷嚴重程度:低(0-5kg)、中(5-15kg)、高(15kg以上)"
}

請直接輸出 JSON,不要有任何前後文字或 markdown 格式。
        """.trimIndent()
    }

    private fun callGeminiAPI(prompt: String, modelName: String): String {
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
        val url = URL("$apiUrl?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

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
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1000)
                })
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("CarbonAIAdvisor", "API 錯誤 (模型:$modelName): $responseCode, $errorBody")
                throw Exception("API 回應錯誤: $responseCode")
            }

        } finally {
            connection.disconnect()
        }
    }

    private fun parseAIResponse(response: String): CarbonAIAnalysis {
        try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            val cleanedText = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val analysisJson = JSONObject(cleanedText)

            val suggestionsArray = analysisJson.getJSONArray("actionSuggestions")
            val suggestions = mutableListOf<String>()
            for (i in 0 until suggestionsArray.length()) {
                suggestions.add(suggestionsArray.getString(i))
            }

            return CarbonAIAnalysis(
                environmentalImpact = analysisJson.getString("environmentalImpact"),
                actionSuggestions = suggestions,
                funFact = analysisJson.getString("funFact"),
                severity = analysisJson.getString("severity")
            )

        } catch (e: Exception) {
            Log.e("CarbonAIAdvisor", "解析 AI 回應失敗", e)
            throw e
        }
    }

    private fun getFallbackAnalysis(carbonAmount: Double, transportType: String): CarbonAIAnalysis {
        val impact = when {
            carbonAmount < 5 -> "這次的碳排放量相對較低,相當於一棵樹一天吸收的二氧化碳量。"
            carbonAmount < 15 -> "這次的碳排放量中等,相當於 ${String.format("%.1f", carbonAmount / 0.5)} 小時的冷氣用電。"
            else -> "這次的碳排放量較高,相當於 ${String.format("%.1f", carbonAmount / 21.7)} 公斤的樹木才能吸收。"
        }

        val suggestions = when (transportType) {
            "汽車" -> listOf(
                "改搭乘大眾運輸工具,可減少約 70% 的碳排放",
                "考慮共乘或使用共享汽車服務",
                "規劃行程,減少不必要的短途駕駛"
            )
            "機車" -> listOf(
                "短距離可改騎自行車或步行",
                "定期保養機車,維持良好油耗",
                "考慮換購電動機車"
            )
            else -> listOf(
                "繼續使用大眾運輸是很好的選擇",
                "可以考慮步行或騎自行車完成最後一哩路",
                "鼓勵身邊朋友一起搭乘大眾運輸"
            )
        }

        val funFact = "如果台灣 2300 萬人每人每天減少 ${String.format("%.2f", carbonAmount)} kg 碳排放,一年可減少 ${String.format("%.0f", carbonAmount * 23000000 * 365 / 1000000)} 萬噸碳排放!"

        val severity = when {
            carbonAmount < 5 -> "低"
            carbonAmount < 15 -> "中"
            else -> "高"
        }

        return CarbonAIAnalysis(
            environmentalImpact = impact,
            actionSuggestions = suggestions,
            funFact = funFact,
            severity = severity
        )
    }
}