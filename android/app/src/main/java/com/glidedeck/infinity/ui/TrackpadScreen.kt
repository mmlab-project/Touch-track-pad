package com.glidedeck.infinity.ui

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.clickable
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
import com.glidedeck.infinity.MainViewModel
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
                awaitPointerEventScope {
                    var isGestureInProgress = false
                    var gestureType = "NONE" // NONE, TAP, SCROLL, SWIPE, GESTURE_3, GESTURE_4, DRAG_1
                    var initialCentroid = androidx.compose.ui.geometry.Offset.Zero
                    var initialSpan = 0f
                    var initialTime = 0L
                    var hasTriggered = false
                    var maxPressedCount = 0
                    var previousPressedCount = 0
                    
                    // Double-tap-and-hold state
                    var lastTapTime = 0L
                    var lastTapPosition = androidx.compose.ui.geometry.Offset.Zero
                    var tapCount = 0
                    var isDragging = false
                    
                    // Smooth Scroll Accumulators
                    var scrollAccumulatorX = 0f
                    var scrollAccumulatorY = 0f


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
                                 
                                 // Reset anchor for 2-finger gesture stability check (Long Press)
                                 val cx = pressed.map { it.position.x }.average().toFloat()
                                 val cy = pressed.map { it.position.y }.average().toFloat()
                                 initialCentroid = androidx.compose.ui.geometry.Offset(cx, cy)
                                 initialTime = System.currentTimeMillis()
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
                            val currentTime = System.currentTimeMillis()
                            val gestureDuration = currentTime - initialTime
                            
                            // Release mouse button if dragging
                            if (isDragging || gestureType == "DRAG_1") {
                                viewModel.networkClient.sendMouseUp("LEFT")
                                isDragging = false
                            }
                            
                            // Handle 1-finger taps (single and double)
                            if (maxPressedCount == 1 && gestureDuration < 300 && !hasTriggered && gestureType != "LONG_PRESS" && gestureType != "DRAG_1") {
                                val timeSinceLastTap = currentTime - lastTapTime
                                
                                // Send click immediately for every tap
                                viewModel.networkClient.sendClick("LEFT")
                                viewModel.hapticManager.performClick()
                                
                                // Track for double-tap-and-hold detection
                                if (timeSinceLastTap < 300) {
                                    tapCount = 2
                                } else {
                                    tapCount = 1
                                }
                                lastTapTime = currentTime
                                lastTapPosition = initialCentroid
                            }
                            
                            // Check for other multi-finger taps
                           if (!hasTriggered && isGestureInProgress && gestureDuration < 300) {
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
                            scrollAccumulatorX = 0f
                            scrollAccumulatorY = 0f
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
                            if (pressedCount == 1 && (gestureType == "MOVE" || gestureType == "DRAG_1")) {
                                val change = pressed.first()
                                val currentTime = System.currentTimeMillis()
                                
                                // Check for double-tap-and-hold to start drag
                                if (!isDragging && gestureType == "MOVE") {
                                    val timeSinceLastTap = currentTime - lastTapTime
                                    val distanceFromLastTap = kotlin.math.sqrt(
                                        (change.position.x - lastTapPosition.x) * (change.position.x - lastTapPosition.x) +
                                        (change.position.y - lastTapPosition.y) * (change.position.y - lastTapPosition.y)
                                    )
                                    
                                    // If this touch started right after a recent tap (double-tap-and-hold pattern)
                                    if (tapCount >= 1 && timeSinceLastTap < 300 && distanceFromLastTap < 50) {
                                        // Wait for hold (150ms) before starting drag
                                        if (currentTime - initialTime > 150) {
                                            // Start drag mode
                                            isDragging = true
                                            gestureType = "DRAG_1"
                                            viewModel.networkClient.sendMouseDown("LEFT")
                                            viewModel.hapticManager.performClick()
                                            tapCount = 0
                                        }
                                    }
                                    
                                    // 1-finger long press removed - now using 2-finger long press for menu
                                }
                                
                                // Handle cursor movement or dragging
                                if (change.pressed && change.previousPressed && gestureType != "LONG_PRESS") {
                                  val dx = (change.position.x - change.previousPosition.x) * cursorSpeed
                                  val dy = (change.position.y - change.previousPosition.y) * cursorSpeed
                                  if (dx != 0f || dy != 0f) {
                                       viewModel.networkClient.sendMouseMove(dx.toInt(), dy.toInt())
                                       if (abs(dx) > 2f || abs(dy) > 2f) change.consume()
                                  }
                                }
                            } else if (pressedCount == 2) {
                                val change1 = pressed[0]
                                val currentTime = System.currentTimeMillis()
                                
                                // Calculate total movement from initial position
                                val cx = pressed.map { it.position.x }.average().toFloat()
                                val cy = pressed.map { it.position.y }.average().toFloat()
                                val totalMove = kotlin.math.sqrt(
                                    (cx - initialCentroid.x) * (cx - initialCentroid.x) +
                                    (cy - initialCentroid.y) * (cy - initialCentroid.y)
                                )
                                
                                // 2-Finger Long Press removed
                                
                                if (change1.pressed && change1.previousPressed && gestureType != "LONG_PRESS_2") {
                                    val rawDy = (change1.position.y - change1.previousPosition.y)
                                    val rawDx = (change1.position.x - change1.previousPosition.x)
                                    
                                    val dy = rawDy * scrollSpeed * (if (scrollReverse) -1 else 1)
                                    val dx = rawDx * scrollSpeed * (if (scrollReverse) -1 else 1)
                                    
                                    // Threshold to switch from TAP_2 to SCROLL
                                    if (gestureType == "TAP_2" && (abs(dy) > 10 || abs(dx) > 10)) {
                                         gestureType = "SCROLL"
                                    }
                                    
                                    if (gestureType == "SCROLL" || gestureType == "TAP_2") { // Allow minor movement to scroll
                                        scrollAccumulatorX += -dx
                                        scrollAccumulatorY += -dy
                                        
                                        val sendX = scrollAccumulatorX.toInt()
                                        val sendY = scrollAccumulatorY.toInt()
                                        
                                        if (sendX != 0 || sendY != 0) {
                                            viewModel.networkClient.sendScroll(sendX, sendY)
                                            scrollAccumulatorX -= sendX
                                            scrollAccumulatorY -= sendY
                                        }
                                    }
                                }
                                
                                // Swipe Logic
                                if (!hasTriggered && gestureType != "SWIPE" && gestureType != "LONG_PRESS_2") {
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
                                val currentTime = System.currentTimeMillis()
                                val totalMove = kotlin.math.sqrt(moveX*moveX + moveY*moveY)
                                
                                if (!hasTriggered) {
                                    // 3-Finger Long Press -> Show Menu
                                    if (currentTime - initialTime > 400 && totalMove < 50) {
                                        showMenu = !showMenu
                                        viewModel.hapticManager.performHeavyClick()
                                        hasTriggered = true
                                        gestureType = "LONG_PRESS_3"
                                    }
                                    
                                    // Pinch In (Show Desktop)
                                    if (spanRatio < 0.7f && gestureType != "LONG_PRESS_3") {
                                        viewModel.networkClient.sendKey("D", listOf("WIN"))
                                        viewModel.hapticManager.performHeavyClick()
                                        hasTriggered = true
                                    }
                                    // Swipe X (Alt Tab)
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
                        Icon(Icons.Default.Menu, contentDescription = "Macros")
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
    
    val uiStrings by viewModel.uiStrings.collectAsState(initial = com.glidedeck.infinity.util.Localization.En)
    val language by viewModel.language.collectAsState(initial = "en")

    Dialog(onDismissRequest = onDismiss) {
        // iOS Modal Style
        Surface(
            shape = RoundedCornerShape(14.dp), // iOS modal corner radius
            color = MaterialTheme.colorScheme.background, // iOS Grouped Background
            modifier = Modifier
                .padding(vertical = 24.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Tall modal
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiStrings.settingsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Done Button
                    Text(
                        text = uiStrings.close,
                        style = MaterialTheme.typography.bodyLarge.copy(
                             color = MaterialTheme.colorScheme.primary,
                             fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { onDismiss() }
                            .padding(8.dp) // larger touch target
                    )
                }

                // Content
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    // Motion Section
                    IosSectionHeader(uiStrings.motionHeader)
                    IosGroup {
                        IosSliderItem(
                            label = "${uiStrings.cursorSpeed}: ${String.format("%.1f", cursorSpeed)}x",
                            value = cursorSpeed,
                            onValueChange = { viewModel.updateCursorSpeed(it) },
                            valueRange = 0.5f..5.0f
                        )
                        HorizontalDivider()
                        IosSliderItem(
                            label = "${uiStrings.scrollSpeed}: ${String.format("%.1f", scrollSpeed * 2)}x",
                            value = scrollSpeed,
                            onValueChange = { viewModel.updateScrollSpeed(it) },
                            valueRange = 0.1f..2.5f
                        )
                        HorizontalDivider()
                        IosSwitchItem(
                            label = uiStrings.scrollStandard,
                            checked = scrollReverse,
                            onCheckedChange = { viewModel.updateScrollReverse(it) },
                            description = if (scrollReverse) uiStrings.scrollExplanationReverse else uiStrings.scrollExplanationStandard
                        )
                    }

                    // Haptic Section
                    IosSectionHeader(uiStrings.generalHeader)
                    IosGroup {
                        IosSliderItem(
                            label = "${uiStrings.haptic}: ${if (hapticStrength == 0) uiStrings.hapticOff else if (hapticStrength == 1) uiStrings.hapticWeak else uiStrings.hapticStrong}",
                            value = hapticStrength.toFloat(),
                            onValueChange = { viewModel.updateHapticStrength(it.toInt()) },
                            valueRange = 0f..2f,
                            steps = 1
                        )
                    }

                    // Interface Section
                    IosSectionHeader("INTERFACE")
                    IosGroup {
                        IosSwitchItem(
                            label = uiStrings.menuRight,
                            checked = isMenuRight,
                            onCheckedChange = { viewModel.updateMenuPosition(it) }
                        )
                        HorizontalDivider()
                        // Theme Toggle
                        IosSegmentedControl(
                            options = listOf("Light", "Dark"),
                            selectedIndex = if (themeMode == "Light") 0 else 1,
                            onOptionSelected = { idx -> viewModel.updateThemeMode(if (idx == 0) "Light" else "Dark") }
                        )
                        HorizontalDivider()
                        // Language Toggle
                        IosSegmentedControl(
                            options = listOf("English", "日本語"),
                            selectedIndex = if (language == "ja") 1 else 0,
                            onOptionSelected = { idx -> viewModel.updateLanguage(if (idx == 0) "en" else "ja") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// --- iOS Style Components ---

@Composable
fun IosSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun IosGroup(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun HorizontalDivider() {
    androidx.compose.material3.HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 16.dp)
    )
}

@Composable
fun IosSliderItem(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column(modifier = Modifier.padding(16.dp, 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
fun IosSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun IosSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.15f) else MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clickable { onOptionSelected(index) }
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

