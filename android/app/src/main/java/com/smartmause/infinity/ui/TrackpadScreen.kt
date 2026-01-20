package com.smartmause.infinity.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.smartmause.infinity.MainViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TrackpadScreen(
    viewModel: MainViewModel,
    onNavigateToKeyboard: () -> Unit,
    onNavigateToMacros: () -> Unit
) {
    val window = (LocalView.current.context as Activity).window
    DisposableEffect(Unit) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val cursorSpeed by viewModel.cursorSpeed.collectAsState(initial = 1.5f)
    val scrollSpeed by viewModel.scrollSpeed.collectAsState(initial = 1.0f)
    val scrollReverse by viewModel.scrollReverse.collectAsState(initial = false)
    val isMenuRight by viewModel.isMenuRight.collectAsState(initial = false)
    
    var showMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use Theme background
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.networkClient.sendClick("LEFT"); viewModel.hapticManager.performClick() },
                    onLongPress = { 
                        showMenu = !showMenu // Toggle menu on long press
                        viewModel.hapticManager.performHeavyClick() 
                    },
                    onDoubleTap = { viewModel.networkClient.sendClick("LEFT"); viewModel.hapticManager.performClick() }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var isGestureInProgress = false
                    var gestureType = "NONE" // NONE, TAP, SCROLL, SWIPE, GESTURE_3, GESTURE_4
                    var initialCentroid = androidx.compose.ui.geometry.Offset.Zero
                    var initialSpan = 0f
                    var initialTime = 0L
                    var hasTriggered = false
                    var maxPressedCount = 0
                    var previousPressedCount = 0

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        val pressed = changes.filter { it.pressed }
                        val pressedCount = pressed.size
                        
                        // Detect finger count increase (Sequential touch support)
                        if (pressedCount > previousPressedCount) {
                             maxPressedCount = maxOf(maxPressedCount, pressedCount)
                             
                             if (pressedCount == 2) {
                                 // Transition 1 -> 2 fingers: Upgrade to TAP_2 / Scroll mode
                                 gestureType = "TAP_2"
                             }
                             // If we transitioned to 3 or 4 fingers, RESET the anchor to treat this as a fresh multi-finger gesture
                             else if (pressedCount >= 3) {
                                 val cx = pressed.map { it.position.x }.average().toFloat()
                                 val cy = pressed.map { it.position.y }.average().toFloat()
                                 initialCentroid = androidx.compose.ui.geometry.Offset(cx, cy)
                                 initialTime = System.currentTimeMillis()
                                 
                                 // Calculate initial span for pinch
                                 val distSum = pressed.map { 
                                    val dx = it.position.x - cx
                                    val dy = it.position.y - cy
                                    kotlin.math.sqrt(dx*dx + dy*dy)
                                 }.sum().toFloat()
                                 initialSpan = distSum / pressedCount
                                 
                                 gestureType = if (pressedCount == 3) "GESTURE_3" else "GESTURE_4"
                                 hasTriggered = false // Reset trigger state for the new gesture phase
                             }
                        }
                        previousPressedCount = pressedCount

                        if (pressedCount == 0) {
                            // Gesture ended (Lift all fingers)
                            // Check for Taps
                           if (!hasTriggered && isGestureInProgress && System.currentTimeMillis() - initialTime < 300) {
                                if (maxPressedCount == 2 && gestureType != "SCROLL" && gestureType != "SWIPE") {
                                    viewModel.networkClient.sendClick("RIGHT")
                                    viewModel.hapticManager.performClick()
                                } else if (maxPressedCount == 3 && gestureType == "GESTURE_3") {
                                    viewModel.networkClient.sendClick("MIDDLE")
                                    viewModel.hapticManager.performClick()
                                }
                            }
                            isGestureInProgress = false
                            hasTriggered = false
                            gestureType = "NONE"
                            maxPressedCount = 0
                            previousPressedCount = 0
                        } else if (!isGestureInProgress) {
                            // First finger down
                            isGestureInProgress = true
                            hasTriggered = false
                            initialTime = System.currentTimeMillis()
                            maxPressedCount = pressedCount
                            
                            val cx = pressed.map { it.position.x }.average().toFloat()
                            val cy = pressed.map { it.position.y }.average().toFloat()
                            initialCentroid = androidx.compose.ui.geometry.Offset(cx, cy)
                            
                            if (pressedCount == 2) {
                                gestureType = "TAP_2" // Potential 2-finger tap or scroll
                            } else if (pressedCount == 3) {
                                gestureType = "GESTURE_3" 
                            } else if (pressedCount == 4) {
                                gestureType = "GESTURE_4"
                            } else {
                                gestureType = "MOVE"
                            }
                            
                            // Calc span if >= 3
                             if (pressedCount >= 3) {
                                  val distSum = pressed.map { 
                                    val dx = it.position.x - cx
                                    val dy = it.position.y - cy
                                    kotlin.math.sqrt(dx*dx + dy*dy)
                                 }.sum().toFloat()
                                 initialSpan = distSum / pressedCount
                             }
                        } else {
                            // Gesture continues
                            if (pressedCount == 1 && gestureType == "MOVE") {
                                val change = pressed.first()
                                if (change.pressed && change.previousPressed) {
                                  val dx = (change.position.x - change.previousPosition.x) * cursorSpeed
                                  val dy = (change.position.y - change.previousPosition.y) * cursorSpeed
                                  if (dx != 0f || dy != 0f) {
                                       viewModel.networkClient.sendMouseMove(dx.toInt(), dy.toInt())
                                       if (abs(dx) > 2f || abs(dy) > 2f) change.consume()
                                  }
                                }
                            } else if (pressedCount == 2) {
                                val change1 = pressed[0]
                                if (change1.pressed && change1.previousPressed) {
                                    val rawDy = (change1.position.y - change1.previousPosition.y)
                                    val rawDx = (change1.position.x - change1.previousPosition.x)
                                    
                                    val dy = rawDy * scrollSpeed * (if (scrollReverse) -1 else 1)
                                    val dx = rawDx * scrollSpeed * (if (scrollReverse) -1 else 1)
                                    
                                    // Threshold to switch from TAP_2 to SCROLL
                                    if (gestureType == "TAP_2" && (abs(dy) > 5 || abs(dx) > 5)) {
                                         gestureType = "SCROLL"
                                    }
                                    
                                    if (gestureType == "SCROLL" || gestureType == "TAP_2") { // Allow minor movement to scroll
                                        if (abs(dy) > 1 || abs(dx) > 1) {
                                            viewModel.networkClient.sendScroll((-dx).toInt(), (-dy).toInt())
                                        }
                                    }
                                }
                                
                                // Swipe Logic
                                if (!hasTriggered && gestureType != "SWIPE") {
                                     val deltaX = pressed[0].position.x - pressed[0].previousPosition.x
                                     if (abs(deltaX) > 20) { 
                                         if (deltaX > 0) {
                                             viewModel.networkClient.sendKey("Left", listOf("ALT")) // Back
                                             viewModel.hapticManager.performHeavyClick()
                                             hasTriggered = true
                                             gestureType = "SWIPE"
                                         } else {
                                             viewModel.networkClient.sendKey("Right", listOf("ALT")) // Forward
                                             viewModel.hapticManager.performHeavyClick()
                                             hasTriggered = true
                                             gestureType = "SWIPE"
                                         }
                                     }
                                }
                                changes.forEach { it.consume() }
                            } else if (pressedCount == 3 && gestureType.startsWith("GESTURE_3")) {
                                val cx = pressed.map { it.position.x }.average().toFloat()
                                val cy = pressed.map { it.position.y }.average().toFloat()
                                
                                val distSum = pressed.map { 
                                    val dx = it.position.x - cx
                                    val dy = it.position.y - cy
                                    kotlin.math.sqrt(dx*dx + dy*dy)
                                }.sum().toFloat()
                                val currentSpan = distSum / 3
                                
                                val moveX = cx - initialCentroid.x
                                val moveY = cy - initialCentroid.y
                                val spanRatio = currentSpan / (initialSpan + 0.1f)
                                
                                // Invalidate Tap if moved significantly
                                if (abs(moveX) > 30 || abs(moveY) > 30 || abs(currentSpan - initialSpan) > 30) {
                                    // gestureType remains GESTURE_3 but trigger logic activates
                                }

                                if (!hasTriggered) {
                                    // Pinch In (Show Desktop)
                                    if (spanRatio < 0.7f) {
                                        viewModel.networkClient.sendKey("D", listOf("WIN"))
                                        viewModel.hapticManager.performHeavyClick()
                                        hasTriggered = true
                                    }
                                    // Swipe X (Alt Tab) - Increased threshold slightly
                                    else if (abs(moveX) > 60 && abs(moveY) < 50) {
                                        viewModel.networkClient.sendKey("Tab", listOf("ALT"))
                                        viewModel.hapticManager.performHeavyClick()
                                        hasTriggered = true
                                    }
                                }
                                changes.forEach { it.consume() }
                            } else if (pressedCount == 4 && gestureType.startsWith("GESTURE_4")) {
                                val cx = pressed.map { it.position.x }.average().toFloat()
                                val moveX = cx - initialCentroid.x
                                val moveY = pressed.map { it.position.y }.average().toFloat() - initialCentroid.y

                                if (!hasTriggered) {
                                    // 4-Finger Swipe (Any direction widely) -> Task View
                                    if (abs(moveX) > 80 || abs(moveY) > 80) {
                                        viewModel.networkClient.sendKey("Tab", listOf("WIN"))
                                        viewModel.hapticManager.performHeavyClick()
                                        hasTriggered = true
                                    }
                                }
                                changes.forEach { it.consume() }
                            }
                        }
                    }
                }
            }
    ) {
        // Overlay Menu (Side Bar)
        AnimatedVisibility(
            visible = showMenu,
            enter = slideInHorizontally(initialOffsetX = { if (isMenuRight) it else -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { if (isMenuRight) it else -it }) + fadeOut(),
            modifier = Modifier.align(if (isMenuRight) Alignment.CenterEnd else Alignment.CenterStart)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    IconButton(onClick = { showMenu = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = { onNavigateToKeyboard(); showMenu = false }) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Keyboard")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    IconButton(onClick = { onNavigateToMacros(); showMenu = false }) {
                        Icon(androidx.compose.material.icons.Icons.Default.List, contentDescription = "Macros")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    IconButton(onClick = { showSettingsDialog = true; showMenu = false }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        if (showMenu) {
            // Dismiss area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isMenuRight) androidx.compose.foundation.layout.PaddingValues(end = 80.dp) else androidx.compose.foundation.layout.PaddingValues(start = 80.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { showMenu = false }
                    }
            )
        }
    }
    
    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun SettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val cursorSpeed by viewModel.cursorSpeed.collectAsState(initial = 1.5f)
    val scrollSpeed by viewModel.scrollSpeed.collectAsState(initial = 1.0f)
    val scrollReverse by viewModel.scrollReverse.collectAsState(initial = false)
    val hapticStrength by viewModel.hapticStrength.collectAsState(initial = 2)
    val isMenuRight by viewModel.isMenuRight.collectAsState(initial = false)
    val themeMode by viewModel.themeMode.collectAsState(initial = "Dark")
    
    val uiStrings by viewModel.uiStrings.collectAsState(initial = com.smartmause.infinity.util.Localization.En)
    val language by viewModel.language.collectAsState(initial = "en")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.95f) // Wider dialog
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = uiStrings.settingsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // Responsive Layout
                androidx.compose.foundation.layout.BoxWithConstraints {
                    val isWide = maxWidth > 600.dp
                    
                    if (isWide) {
                        // 2-Column Layout (Landscape / Tablet)
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            // Left Column: Motion & Speed
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                MotionSettingsContent(uiStrings, cursorSpeed, scrollSpeed, scrollReverse, viewModel)
                            }

                            // Right Column: General
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                GeneralSettingsContent(uiStrings, hapticStrength, isMenuRight, themeMode, language, viewModel)
                            }
                        }
                    } else {
                        // 1-Column Layout (Portrait / Phone) - Scrollable
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp) // Limit height to allow scrolling within dialog
                                .verticalScroll(rememberScrollState())
                        ) {
                             MotionSettingsContent(uiStrings, cursorSpeed, scrollSpeed, scrollReverse, viewModel)
                             Spacer(modifier = Modifier.height(24.dp))
                             androidx.compose.material3.HorizontalDivider()
                             Spacer(modifier = Modifier.height(24.dp))
                             GeneralSettingsContent(uiStrings, hapticStrength, isMenuRight, themeMode, language, viewModel)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(uiStrings.close)
                }
            }
        }
    }
}

@Composable
fun MotionSettingsContent(
    uiStrings: com.smartmause.infinity.util.AppStrings,
    cursorSpeed: Float,
    scrollSpeed: Float,
    scrollReverse: Boolean,
    viewModel: MainViewModel
) {
    Text(
        text = uiStrings.motionHeader,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Cursor Speed
    Text("${uiStrings.cursorSpeed}: ${String.format("%.1f", cursorSpeed)}x")
    Slider(
        value = cursorSpeed,
        onValueChange = { viewModel.updateCursorSpeed(it) },
        valueRange = 0.5f..5.0f
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    // Scroll Speed
    Text("${uiStrings.scrollSpeed}: ${String.format("%.1f", scrollSpeed)}x")
    Slider(
        value = scrollSpeed,
        onValueChange = { viewModel.updateScrollSpeed(it) },
        valueRange = 0.5f..5.0f
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    // Scroll Direction
    Text(uiStrings.scrollDirection)
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(uiStrings.scrollStandard, style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = scrollReverse,
            onCheckedChange = { viewModel.updateScrollReverse(it) }
        )
        Text(uiStrings.scrollReverse, style = MaterialTheme.typography.bodySmall)
    }
    // Scroll Explanation
    Text(
        text = if (scrollReverse) uiStrings.scrollExplanationReverse else uiStrings.scrollExplanationStandard,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun GeneralSettingsContent(
    uiStrings: com.smartmause.infinity.util.AppStrings,
    hapticStrength: Int,
    isMenuRight: Boolean,
    themeMode: String,
    language: String,
    viewModel: MainViewModel
) {
    Text(
        text = uiStrings.generalHeader,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Haptic
    Text("${uiStrings.haptic}: ${if (hapticStrength == 0) uiStrings.hapticOff else if (hapticStrength == 1) uiStrings.hapticWeak else uiStrings.hapticStrong}")
    Slider(
        value = hapticStrength.toFloat(),
        onValueChange = { viewModel.updateHapticStrength(it.toInt()) },
        valueRange = 0f..2f,
        steps = 1
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    // Menu Position
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(uiStrings.menuRight, modifier = Modifier.weight(1f))
        Switch(
            checked = isMenuRight,
            onCheckedChange = { viewModel.updateMenuPosition(it) }
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))

    // Theme Mode
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(uiStrings.themeLight, modifier = Modifier.weight(1f))
        Switch(
            checked = themeMode == "Light",
            onCheckedChange = { viewModel.updateThemeMode(if (it) "Light" else "Dark") }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Language Selector
    Text(uiStrings.language)
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text("English", style = MaterialTheme.typography.bodySmall)
        Switch(
            checked = language == "ja",
            onCheckedChange = { viewModel.updateLanguage(if (it) "ja" else "en") }
        )
        Text("日本語", style = MaterialTheme.typography.bodySmall)
    }
}
