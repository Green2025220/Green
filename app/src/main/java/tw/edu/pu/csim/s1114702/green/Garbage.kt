package tw.edu.pu.csim.s1114702.green

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.Detection
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController


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
    "bottle" to "瓶子",
    "wine glass" to "酒杯",
    "cup" to "杯子",
    "bowl" to "碗",
    "book" to "書",
    "spoon" to "湯匙",
    "fork" to "叉子",
    "knife" to "刀子",
    "laptop" to "筆記型電腦",
    "mouse" to "滑鼠",
    "keyboard" to "鍵盤",
    "cell phone" to "手機",
    "tv" to "電視/顯示器",
    "remote" to "遙控器",
    "microwave" to "微波爐",
    "oven" to "烤箱",
    "toaster" to "烤麵包機",
    "refrigerator" to "冰箱",
    "scissors" to "剪刀",
    "toothbrush" to "牙刷",
    "banana" to "香蕉",
    "apple" to "蘋果",
    "sandwich" to "三明治",
    "orange" to "橘子",
    "broccoli" to "花椰菜",
    "carrot" to "紅蘿蔔",
    "hot dog" to "熱狗",
    "pizza" to "披薩",
    "donut" to "甜甜圈",
    "cake" to "蛋糕",
    "teddy bear" to "泰迪熊",
    "airplane" to "飛機",
    "bicycle" to "腳踏車",
    "boat" to "船",
    "bus" to "公車",
    "car" to "汽車",
    "motorcycle" to "摩托車",
    "train" to "火車",
    "truck" to "卡車",
    "bear" to "熊",
    "bird" to "鳥",
    "cat" to "貓",
    "cow" to "牛",
    "dog" to "狗",
    "elephant" to "大象",
    "giraffe" to "長頸鹿",
    "horse" to "馬",
    "zebra" to "斑馬",
    "sheep" to "羊",
    "bed" to "床",
    "bench" to "長椅",
    "chair" to "椅子",
    "couch" to "沙發",
    "dining table" to "餐桌",
    "potted plant" to "盆栽",
    "toilet" to "馬桶",
    "sink" to "水槽",
    "baseball bat" to "棒球棍",
    "baseball glove" to "棒球手套",
    "frisbee" to "飛盤",
    "kite" to "風箏",
    "skateboard" to "滑板",
    "skis" to "滑雪板",
    "snowboard" to "滑雪板",
    "sports ball" to "運動球",
    "surfboard" to "衝浪板",
    "tennis racket" to "網球拍",
    "backpack" to "背包",
    "clock" to "時鐘",
    "hair drier" to "吹風機",
    "handbag" to "手提包",
    "suitcase" to "行李箱",
    "tie" to "領帶",
    "umbrella" to "雨傘",
    "vase" to "花瓶",
    "fire hydrant" to "消防栓",
    "parking meter" to "停車收費表",
    "stop sign" to "停止標誌",
    "traffic light" to "紅綠燈",
    "person" to "人"
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

// ====== 翻譯函數 ======
fun translateToChineseItem(englishName: String): String {
    return itemTranslations[englishName] ?: englishName
}

// ====== ImageProxy → Bitmap ======
fun ImageProxy.toBitmap(context: Context): Bitmap {
    val converter = YuvToRgbConverter(context)
    return converter.yuvToRgb(this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarbageScreen(navController: NavController) {
    val localContext = LocalContext.current

    var detectedItem by remember { mutableStateOf("尚未偵測") }
    var category by remember { mutableStateOf("未知") }
    var confidence by remember { mutableStateOf(0f) }

    var lastDetectedLabel by remember { mutableStateOf("") }
    var consecutiveCount by remember { mutableStateOf(0) }

    // ===== 動態請求相機權限 =====
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
        if (ContextCompat.checkSelfPermission(
                localContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ===== TensorFlow Lite ObjectDetector =====
    val objectDetector by remember {
        mutableStateOf(
            try {
                ObjectDetector.createFromFile(localContext, "efficientdet_lite0.tflite")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("垃圾分類") },
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
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.8f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ===== CameraX Preview =====
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

                                                        if (results.isNotEmpty()) {
                                                            val detection = results[0]
                                                            if (detection.categories.isNotEmpty()) {
                                                                val category_info = detection.categories[0]
                                                                val label = category_info.label
                                                                val score = category_info.score

                                                                // 動態穩定判斷
                                                                if (score >= 0.3f) {
                                                                    if (label == lastDetectedLabel) {
                                                                        consecutiveCount++
                                                                        if (consecutiveCount >= 5) {
                                                                            val chineseLabel = translateToChineseItem(label)
                                                                            detectedItem = chineseLabel
                                                                            category = classifyItem(label)
                                                                            confidence = score
                                                                        } else {
                                                                            detectedItem = "辨識中... ($consecutiveCount/5)"
                                                                            category = "請保持穩定"
                                                                            confidence = score
                                                                        }
                                                                    } else {
                                                                        lastDetectedLabel = label
                                                                        consecutiveCount = 1
                                                                        detectedItem = "辨識中... (1/5)"
                                                                        category = "請保持穩定"
                                                                        confidence = score
                                                                    }
                                                                } else {
                                                                    consecutiveCount = 0
                                                                    lastDetectedLabel = ""
                                                                    detectedItem = "請對準物件"
                                                                    category = "等待中..."
                                                                    confidence = score
                                                                }
                                                            }
                                                        } else {
                                                            consecutiveCount = 0
                                                            lastDetectedLabel = ""
                                                            detectedItem = "請對準物件"
                                                            category = "等待中..."
                                                            confidence = 0f
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (objectDetector != null) "模型已載入" else "模型載入失敗",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (objectDetector != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "辨識到的物品",
                                style = MaterialTheme.typography.titleMedium
                            )
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
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )

                            Text(
                                "分類結果",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                category,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                color = when {
                                    category.contains("回收") -> MaterialTheme.colorScheme.primary
                                    category.contains("一般垃圾") -> MaterialTheme.colorScheme.error
                                    category.contains("廚餘") -> MaterialTheme.colorScheme.secondary
                                    category.contains("需拆解分類/視材質而定") -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.tertiary
                                },
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}