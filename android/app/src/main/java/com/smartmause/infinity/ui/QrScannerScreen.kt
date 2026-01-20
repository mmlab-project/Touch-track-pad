package com.smartmause.infinity.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.smartmause.infinity.MainViewModel
import org.json.JSONObject
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("QR Scan") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Manual Input") })
        }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (selectedTab == 0) {
                QrScanContent(viewModel)
            } else {
                ManualInputContent(viewModel)
            }
        }
    }
}

@Composable
fun QrScanContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setTargetResolution(android.util.Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), QrAnalyzer { result ->
                                    // Parse Result
                                    try {
                                        val json = JSONObject(result)
                                        val ip = json.getString("ip")
                                        val port = json.getInt("port")
                                        val token = json.getString("token")
                                        viewModel.connect(ip, port, token)
                                    } catch (e: Exception) {
                                        // Invalid QR
                                    }
                                })
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalyzer
                            )
                        } catch (e: Exception) {
                            // Binding failed
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay Guide
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rectSize = 250.dp.toPx()
                drawRoundRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (this.size.width - rectSize) / 2, 
                        (this.size.height - rectSize) / 2
                    ),
                    size = Size(rectSize, rectSize),
                    cornerRadius = CornerRadius(20f, 20f),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    } else {
        Text("Camera permission required")
    }
}

@Composable
fun ManualInputContent(viewModel: MainViewModel) {
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("50000") }
    var token by remember { mutableStateOf("") }
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Manual Connection", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("IP Address") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("Token") })
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    port.toIntOrNull()?.let { p ->
                        viewModel.connect(ip, p, token)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect")
            }
        }
    }
}

class QrAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner = BarcodeScanning.getClient(options)

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            onQrCodeScanned(it)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
