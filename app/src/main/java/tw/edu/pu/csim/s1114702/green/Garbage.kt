package tw.edu.pu.csim.s1114702.green

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

// ====== 垃圾分類表 ======
val recycleItems = setOf(
    "bottle", "wine glass", "cup", "book", "bicycle",
    "knife", "laptop", "mouse", "keyboard", "cell phone",
    "tv", "remote", "microwave", "oven", "toaster",
    "refrigerator", "scissors", "couch", "chair", "bench",
    "dining table", "hair drier"
)

val trashItems = setOf(
    "teddy bear", "toothbrush", "clock", "baseball bat", "baseball glove",
    "sports ball", "backpack", "handbag", "suitcase", "tie"
)

val leftoverItems = setOf(
    "banana", "apple", "sandwich", "orange", "broccoli",
    "carrot", "hot dog", "pizza", "donut", "cake"
)

val disassembleItems = setOf(
    "umbrella", "skateboard", "surfboard", "tennis racket", "kite", "frisbee",
    "skis", "snowboard", "vase", "spoon", "fork", "bowl"
)

// ====== 英文轉中文對照表 ======
val itemTranslations = mapOf(
    "bottle" to "瓶子", "wine glass" to "酒杯", "cup" to "杯子", "bowl" to "碗",
    "book" to "書", "spoon" to "湯匙", "fork" to "叉子", "knife" to "刀子",
    "laptop" to "筆記型電腦", "mouse" to "滑鼠", "keyboard" to "鍵盤",
    "cell phone" to "手機", "tv" to "電視/顯示器", "remote" to "遙控器",
    "microwave" to "微波爐", "oven" to "烤箱", "toaster" to "烤麵包機",
    "refrigerator" to "冰箱", "scissors" to "剪刀", "toothbrush" to "牙刷",
    "banana" to "香蕉", "apple" to "蘋果", "sandwich" to "三明治",
    "orange" to "橘子", "broccoli" to "花椰菜", "carrot" to "紅蘿蔔",
    "hot dog" to "熱狗", "pizza" to "披薩", "donut" to "甜甜圈",
    "cake" to "蛋糕", "teddy bear" to "泰迪熊", "umbrella" to "雨傘",
    "vase" to "花瓶", "clock" to "時鐘", "hair drier" to "吹風機"
    // ... 其他翻譯省略
)

fun classifyItem(itemName: String): String {
    return when {
        recycleItems.contains(itemName) -> "回收"
        trashItems.contains(itemName) -> "一般垃圾"
        leftoverItems.contains(itemName) -> "廚餘"
        disassembleItems.contains(itemName) -> "需拆解分類/視材質而定"
        else -> "其他"
    }
}

fun translateToChineseItem(englishName: String): String {
    return itemTranslations[englishName] ?: englishName
}

fun ImageProxy.toBitmap(context: Context): Bitmap {
    val converter = YuvToRgbConverter(context)
    return converter.yuvToRgb(this)
}

@Composable
fun CooldownDisplay(
    lastRewardTime: Long,
    cooldownDuration: Long,
    remainingRewards: Int
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastRewardTime) {
        if (lastRewardTime > 0) {
            while (true) {
                currentTime = System.currentTimeMillis()
                val timeSinceLastReward = currentTime - lastRewardTime
                if (timeSinceLastReward >= cooldownDuration) break
                kotlinx.coroutines.delay(100)
            }
        }
    }

    val timeSinceLastReward = currentTime - lastRewardTime
    val isInCooldown = timeSinceLastReward < cooldownDuration && lastRewardTime > 0

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        when {
            isInCooldown -> {
                val remainingCooldown = (cooldownDuration - timeSinceLastReward) / 1000
                Text("⏱️ 冷卻中... (${remainingCooldown}秒)", color = Color(0xFFFF9800), fontSize = 14.sp)
            }
            remainingRewards > 0 -> {
                Text("💚 今日剩餘獎勵次數: $remainingRewards/3", color = Color(0xFF2CA673), fontSize = 14.sp)
            }
            else -> {
                Text("✓ 今日已達上限 (3/3)", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}
// ===== 新增：材質詳情對話框 =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailsDialog(
    materialData: MaterialAnalysisResult?,
    onDismiss: () -> Unit
) {
    if (materialData == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 標題
                Text(
                    "🔬 材質詳細分析",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 錯誤訊息
                if (materialData.error != null) {
                    Text(
                        "❌ ${materialData.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // 主要材質
                    InfoSection(
                        icon = "📦",
                        title = "主要材質",
                        content = materialData.material
                    )

                    // 材質代碼
                    if (materialData.materialCode.isNotEmpty()) {
                        InfoSection(
                            icon = "🔢",
                            title = "回收代碼",
                            content = materialData.materialCode,
                            highlight = true
                        )
                    }

                    // 複合材質
                    if (materialData.isComposite) {
                        InfoSection(
                            icon = "⚠️",
                            title = "複合材質",
                            content = materialData.compositeDescription,
                            warning = true
                        )
                    }

                    // 可回收性
                    InfoSection(
                        icon = if (materialData.recyclable) "♻️" else "🚫",
                        title = "可回收性",
                        content = if (materialData.recyclable) "可回收" else "不可回收",
                        highlight = materialData.recyclable
                    )

                    // 回收小知識
                    if (materialData.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "💡 回收小知識",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        materialData.tips.forEachIndexed { index, tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "${index + 1}. ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2CA673)
                                )
                                Text(
                                    tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 拆解指南
                    if (materialData.disassemblySteps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "🔧 拆解指南",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        materialData.disassemblySteps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "${index + 1}. ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFF9800)
                                )
                                Text(
                                    step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 關閉按鈕
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("關閉")
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    icon: String,
    title: String,
    content: String,
    highlight: Boolean = false,
    warning: Boolean = false
) {
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = when {
                    warning -> Color(0xFFFF9800)
                    highlight -> Color(0xFF2CA673)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarbageScreen(
    navController: NavController,
    viewModel: ViewModel,
    userEmail: String
) {
    val localContext = LocalContext.current

    // ===== Gemini 相關狀態 =====
    val geminiClassifier = remember { GeminiClassifier(BuildConfig.GEMINI_API_KEY) }
    var isAIAnalyzing by remember { mutableStateOf(false) }
    var aiReason by remember { mutableStateOf("") }

    var detectedItem by remember { mutableStateOf("尚未偵測") }
    var category by remember { mutableStateOf("未知") }
    var confidence by remember { mutableStateOf(0f) }

    var lastDetectedLabel by remember { mutableStateOf("") }
    var consecutiveCount by remember { mutableStateOf(0) }
    var lastApiCallTime by remember { mutableStateOf(0L) }
    val minApiInterval = 2000L

    // ===== 新增：材質分析相關狀態 =====
    var showMaterialDialog by remember { mutableStateOf(false) }
    var materialAnalysis by remember { mutableStateOf<MaterialAnalysisResult?>(null) }
    var isAnalyzingMaterial by remember { mutableStateOf(false) }
    var currentEnglishName by remember { mutableStateOf("") }
    var currentChineseName by remember { mutableStateOf("") }

    // ===== 獎勵相關狀態 =====
    var showRewardDialog by remember { mutableStateOf(false) }
    var remainingRewards by remember { mutableStateOf(3) }
    var lastRewardedCategory by remember { mutableStateOf("") }
    var lastRewardTime by remember { mutableStateOf(0L) }
    val cooldownDuration = 5000L
    var lastAnalyzedLabel by remember { mutableStateOf("") }

    // 載入數據
    LaunchedEffect(Unit) {
        if (userEmail.isNotEmpty()) {
            viewModel.loadGarbageDataFromFirebase(userEmail)
            remainingRewards = viewModel.getRemainingGarbageRewards()
        }
    }

    // ===== 相機權限 =====
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                detectedItem = "無法取得相機權限"
                category = "請允許權限"
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(localContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ===== TensorFlow 模型 =====
    val objectDetector by remember {
        mutableStateOf(
            try {
                ObjectDetector.createFromFile(localContext, "efficientdet_lite1.tflite")
            } catch (e: Exception) {
                Log.e("GarbageScreen", "模型載入失敗", e)
                null
            }
        )
    }

    // ===== 獎勵對話框 =====
    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = { Text("🎉 獲得獎勵") },
            text = {
                Column {
                    Text("成功辨識垃圾分類！")
                    Text("獲得 1 分")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("辨識結果: $lastRewardedCategory", fontSize = 14.sp, color = Color.Gray)
                    if (aiReason.isNotEmpty()) {
                        Text("AI 理由: $aiReason", fontSize = 12.sp, color = Color(0xFF2CA673))
                    }
                    Text("今日剩餘次數: ${remainingRewards}/3", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showRewardDialog = false }) {
                    Text("太好了！")
                }
            }
        )
    }

    // ===== 材質詳情對話框 =====
    if (showMaterialDialog) {
        MaterialDetailsDialog(
            materialData = materialAnalysis,
            onDismiss = { showMaterialDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("一拍即分") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box {
            Image(
                painter = painterResource(id = R.drawable.garbage_bg),
                contentDescription = "背景圖片",
                modifier = Modifier.fillMaxSize().alpha(0.8f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                // ===== 相機預覽區 =====
                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(surfaceProvider)
                                    }

                                    val imageAnalyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analyzer ->
                                            analyzer.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                                try {
                                                    objectDetector?.let { detector ->
                                                        val bitmap = imageProxy.toBitmap(ctx)
                                                        val tensorImage = TensorImage.fromBitmap(bitmap)
                                                        val results: List<Detection> = detector.detect(tensorImage)

                                                        val excludedCategories = setOf(
                                                            "person", "sheep", "zebra", "horse", "giraffe",
                                                            "elephant", "dog", "cow", "cat", "bird", "bear"
                                                        )

                                                        if (results.isNotEmpty()) {
                                                            val topDetection = results[0]
                                                            val topLabel = topDetection.categories.firstOrNull()?.label
                                                            val topScore = topDetection.categories.firstOrNull()?.score ?: 0f

                                                            if (excludedCategories.contains(topLabel) && topScore >= 0.5f) {
                                                                consecutiveCount = 0
                                                                lastDetectedLabel = ""
                                                                lastAnalyzedLabel = ""
                                                                detectedItem = translateToChineseItem(topLabel ?: "")
                                                                category = "這不是垃圾物品"
                                                                confidence = topScore
                                                                aiReason = "請對準可分類的垃圾"
                                                                currentEnglishName = ""
                                                                currentChineseName = ""
                                                                imageProxy.close()
                                                                return@setAnalyzer
                                                            }
                                                        }

                                                        val filteredResults = results.filter { detection ->
                                                            val label = detection.categories.firstOrNull()?.label
                                                            val score = detection.categories.firstOrNull()?.score ?: 0f
                                                            !excludedCategories.contains(label) && score >= 0.4f
                                                        }

                                                        if (filteredResults.isNotEmpty()) {
                                                            val detection = filteredResults[0]
                                                            if (detection.categories.isNotEmpty()) {
                                                                val category_info = detection.categories[0]
                                                                val label = category_info.label
                                                                val score = category_info.score

                                                                if (score >= 0.3f) {
                                                                    if (label == lastDetectedLabel) {
                                                                        consecutiveCount++
                                                                        if (consecutiveCount >= 5 && label != lastAnalyzedLabel && !isAIAnalyzing) {
                                                                            val currentTime = System.currentTimeMillis()
                                                                            if (currentTime - lastApiCallTime < minApiInterval) {
                                                                                return@setAnalyzer
                                                                            }

                                                                            lastApiCallTime = currentTime
                                                                            lastAnalyzedLabel = label
                                                                            val chineseLabel = translateToChineseItem(label)

                                                                            // ✅ 儲存名稱
                                                                            currentEnglishName = label
                                                                            currentChineseName = chineseLabel

                                                                            isAIAnalyzing = true
                                                                            detectedItem = chineseLabel
                                                                            category = "AI 分析中..."
                                                                            confidence = score

                                                                            GlobalScope.launch {
                                                                                try {
                                                                                    val aiResult = geminiClassifier.classifyGarbage(label, chineseLabel)

                                                                                    withContext(Dispatchers.Main) {
                                                                                        if (aiResult.category == "其他" &&
                                                                                            (aiResult.reason == "配額用完" ||
                                                                                                    aiResult.reason == "API 錯誤" ||
                                                                                                    aiResult.reason == "連線失敗" ||
                                                                                                    aiResult.reason == "解析失敗")) {

                                                                                            detectedItem = chineseLabel
                                                                                            category = classifyItem(label)
                                                                                            aiReason = "AI 暫時無法使用 (${aiResult.reason})"
                                                                                            confidence = score

                                                                                            if (score >= 0.5f && category != "其他" && userEmail.isNotEmpty()) {
                                                                                                val timeSinceLastReward = System.currentTimeMillis() - lastRewardTime
                                                                                                if (timeSinceLastReward >= cooldownDuration) {
                                                                                                    val rewarded = viewModel.rewardGarbageClassification(userEmail)
                                                                                                    if (rewarded) {
                                                                                                        lastRewardedCategory = category
                                                                                                        remainingRewards = viewModel.getRemainingGarbageRewards()
                                                                                                        showRewardDialog = true
                                                                                                        lastRewardTime = System.currentTimeMillis()
                                                                                                        consecutiveCount = 0
                                                                                                        lastDetectedLabel = ""
                                                                                                        lastAnalyzedLabel = ""
                                                                                                    }
                                                                                                }
                                                                                            }

                                                                                        } else if (aiResult.isGarbage) {
                                                                                            detectedItem = chineseLabel
                                                                                            category = aiResult.category
                                                                                            aiReason = aiResult.reason
                                                                                            confidence = score

                                                                                            if (score >= 0.5f && aiResult.category != "其他" && userEmail.isNotEmpty()) {
                                                                                                val timeSinceLastReward = System.currentTimeMillis() - lastRewardTime
                                                                                                if (timeSinceLastReward >= cooldownDuration) {
                                                                                                    val rewarded = viewModel.rewardGarbageClassification(userEmail)
                                                                                                    if (rewarded) {
                                                                                                        lastRewardedCategory = aiResult.category
                                                                                                        remainingRewards = viewModel.getRemainingGarbageRewards()
                                                                                                        showRewardDialog = true
                                                                                                        lastRewardTime = System.currentTimeMillis()
                                                                                                        consecutiveCount = 0
                                                                                                        lastDetectedLabel = ""
                                                                                                        lastAnalyzedLabel = ""
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        } else {
                                                                                            detectedItem = chineseLabel
                                                                                            category = "非垃圾物品"
                                                                                            aiReason = aiResult.reason
                                                                                            confidence = score
                                                                                            currentEnglishName = ""
                                                                                            currentChineseName = ""
                                                                                        }
                                                                                        isAIAnalyzing = false
                                                                                    }
                                                                                } catch (e: Exception) {
                                                                                    withContext(Dispatchers.Main) {
                                                                                        detectedItem = chineseLabel
                                                                                        category = classifyItem(label)
                                                                                        aiReason = "AI 暫時無法使用"
                                                                                        confidence = score
                                                                                        isAIAnalyzing = false
                                                                                    }
                                                                                }
                                                                            }
                                                                        } else if (consecutiveCount < 5) {
                                                                            detectedItem = "辨識中... ($consecutiveCount/5)"
                                                                            category = "請保持穩定"
                                                                            confidence = score
                                                                        }
                                                                    } else {
                                                                        lastDetectedLabel = label
                                                                        lastAnalyzedLabel = ""
                                                                        consecutiveCount = 1
                                                                        detectedItem = "辨識中... (1/5)"
                                                                        category = "請保持穩定"
                                                                        confidence = score
                                                                        aiReason = ""
                                                                        currentEnglishName = ""
                                                                        currentChineseName = ""
                                                                    }
                                                                } else {
                                                                    consecutiveCount = 0
                                                                    lastDetectedLabel = ""
                                                                    lastAnalyzedLabel = ""
                                                                    detectedItem = "請對準物件"
                                                                    category = "等待中..."
                                                                    confidence = score
                                                                    aiReason = ""
                                                                    currentEnglishName = ""
                                                                    currentChineseName = ""
                                                                }
                                                            }
                                                        } else {
                                                            consecutiveCount = 0
                                                            lastDetectedLabel = ""
                                                            lastAnalyzedLabel = ""
                                                            detectedItem = "請對準垃圾物品"
                                                            category = "等待中..."
                                                            confidence = 0f
                                                            aiReason = ""
                                                            currentEnglishName = ""
                                                            currentChineseName = ""
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    detectedItem = "辨識錯誤"
                                                    category = "錯誤"
                                                } finally {
                                                    imageProxy.close()
                                                }
                                            }
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            ctx as LifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalyzer
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        }
                    )
                }

                // ===== 底部結果顯示 =====
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CooldownDisplay(
                        lastRewardTime = lastRewardTime,
                        cooldownDuration = cooldownDuration,
                        remainingRewards = remainingRewards
                    )

                    Text(
                        text = if (objectDetector != null) "模型已載入" else "模型載入失敗",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (objectDetector != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.garbagetext),
                            contentDescription = "結果背景",
                            modifier = Modifier.fillMaxWidth().offset(y = (-10).dp),
                            contentScale = ContentScale.FillWidth
                        )
                        Image(
                            painter = painterResource(id = R.drawable.garbageflow1),
                            contentDescription = "左下角裝飾",
                            modifier = Modifier.align(Alignment.BottomStart).size(60.dp)
                                .offset(x = (-10).dp, y = 15.dp),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            painter = painterResource(id = R.drawable.garbageflow2),
                            contentDescription = "右下角裝飾",
                            modifier = Modifier.align(Alignment.BottomEnd).size(110.dp)
                                .offset(x = 8.dp, y = 15.dp),
                            contentScale = ContentScale.Fit
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp).offset(y = (-10).dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("辨識到的物品", style = MaterialTheme.typography.titleMedium)
                            Text(
                                detectedItem,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (confidence > 0) {
                                Text(
                                    "信心度: ${String.format("%.1f%%", confidence * 100)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )

                            Text("分類結果", style = MaterialTheme.typography.titleMedium)
                            Text(
                                category,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                color = when {
                                    category.contains("回收") -> MaterialTheme.colorScheme.primary
                                    category.contains("一般垃圾") -> MaterialTheme.colorScheme.error
                                    category.contains("廚餘") -> MaterialTheme.colorScheme.secondary
                                    category.contains("需拆解") -> MaterialTheme.colorScheme.tertiary
                                    category.contains("AI 分析中") -> Color(0xFF2CA673)
                                    else -> MaterialTheme.colorScheme.tertiary
                                },
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            if (aiReason.isNotEmpty() && !isAIAnalyzing) {
                                Text(
                                    "💡 $aiReason",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (isAIAnalyzing) {
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF2CA673)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "🤖 AI 分析中...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2CA673)
                                    )
                                }
                            }

                            // ===== 查看材質詳情按鈕 =====
                            if (category != "未知" &&
                                category != "等待中..." &&
                                category != "請保持穩定" &&
                                category != "這不是垃圾物品" &&
                                category != "非垃圾物品" &&
                                !category.contains("分析中") &&
                                currentChineseName.isNotEmpty()) {

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        isAnalyzingMaterial = true
                                        GlobalScope.launch {
                                            try {
                                                val result = geminiClassifier.analyzeMaterialDetails(
                                                    currentEnglishName,
                                                    currentChineseName
                                                )
                                                withContext(Dispatchers.Main) {
                                                    materialAnalysis = result
                                                    showMaterialDialog = true
                                                    isAnalyzingMaterial = false
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    materialAnalysis = MaterialAnalysisResult.createError("分析失敗")
                                                    showMaterialDialog = true
                                                    isAnalyzingMaterial = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isAnalyzingMaterial,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2CA673)
                                    ),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    if (isAnalyzingMaterial) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("分析中...")
                                    } else {
                                        Text("🔬 查看材質詳情")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}