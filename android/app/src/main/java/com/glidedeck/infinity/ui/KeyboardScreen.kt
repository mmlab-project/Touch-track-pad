package com.glidedeck.infinity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glidedeck.infinity.MainViewModel

@Composable
fun KeyboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var isCtrl by remember { mutableStateOf(false) }
    var isAlt by remember { mutableStateOf(false) }
    var isShift by remember { mutableStateOf(false) }
    var isWin by remember { mutableStateOf(false) }
    var isFn by remember { mutableStateOf(false) } // For F1-F12

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Dark background like physical keyboard
            .padding(4.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            
            // F-Key Toggle Row (Optional or integrated)
            KeyButton(text = "Esc", code = "ESCAPE", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
            Spacer(modifier = Modifier.width(4.dp))
            KeyButton(text = "F1", code = "F1", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
            Spacer(modifier = Modifier.width(4.dp))
            KeyButton(text = "F2", code = "F2", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
            Spacer(modifier = Modifier.width(4.dp))
            KeyButton(text = "F3", code = "F3", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
            Spacer(modifier = Modifier.width(4.dp))
            KeyButton(text = "F4", code = "F4", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
             Spacer(modifier = Modifier.width(4.dp))
            KeyButton(text = "F5", code = "F5", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
            Spacer(modifier = Modifier.width(4.dp))
            KeyButton(text = "Del", code = "DELETE", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Keyboard Rows
        val rowModifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 2.dp)
        
        // Row 1: Numbers
        Row(modifier = rowModifier) {
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=")
            val codes = listOf("D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D0", "OemMinus", "OemPlus")
            keys.forEachIndexed { index, label ->
                KeyButton(text = label, code = codes[index], weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
                if (index < keys.size - 1) Spacer(modifier = Modifier.width(2.dp))
            }
            Spacer(modifier = Modifier.width(2.dp))
            KeyButton(text = "⌫", code = "BACKSPACE", weight = 1.5f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))

        }

        // Row 2: QWERTY
        Row(modifier = rowModifier) {
             KeyButton(text = "Tab", code = "TAB", weight = 1.2f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
             Spacer(modifier = Modifier.width(2.dp))
             
             val keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]")
             val codes = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "OemOpenBrackets", "OemCloseBrackets")
             
             keys.forEachIndexed { index, label ->
                KeyButton(text = label, code = codes[index], weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
                if (index < keys.size - 1) Spacer(modifier = Modifier.width(2.dp))
             }
             Spacer(modifier = Modifier.width(2.dp))
             KeyButton(text = "Enter", code = "ENTER", weight = 1.5f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF2196F3))
        }

        // Row 3: ASDF
        Row(modifier = rowModifier) {
            KeyButton(text = "Caps", code = "CAPSLOCK", weight = 1.5f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
            Spacer(modifier = Modifier.width(2.dp))
            
            val keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "\\")
            val codes = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "OemSemicolon", "OemQuotes", "OemPipe")
            
            keys.forEachIndexed { index, label ->
                KeyButton(text = label, code = codes[index], weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
                if (index < keys.size - 1) Spacer(modifier = Modifier.width(2.dp))
            }
        }

        // Row 4: ZXCV
        Row(modifier = rowModifier) {
            ToggleKeyButton(text = "Shift", isActive = isShift, weight = 1.8f) { isShift = !isShift }
            Spacer(modifier = Modifier.width(2.dp))
            
            val keys = listOf("Z", "X", "C", "V", "B", "N", "M", ",", ".", "/")
            val codes = listOf("Z", "X", "C", "V", "B", "N", "M", "OemComma", "OemPeriod", "OemQuestion")
            
            keys.forEachIndexed { index, label ->
                KeyButton(text = label, code = codes[index], weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift)
                if (index < keys.size - 1) Spacer(modifier = Modifier.width(2.dp))
            }
            
            Spacer(modifier = Modifier.width(2.dp))
            ToggleKeyButton(text = "Shift", isActive = isShift, weight = 1.8f) { isShift = !isShift }
        }

        // Row 5: Modifiers & Space
        Row(modifier = rowModifier) {
            ToggleKeyButton(text = "Ctrl", isActive = isCtrl, weight = 1.2f) { isCtrl = !isCtrl }
            Spacer(modifier = Modifier.width(2.dp))
            ToggleKeyButton(text = "Win", isActive = isWin, weight = 1.2f) { isWin = !isWin }
            Spacer(modifier = Modifier.width(2.dp))
            ToggleKeyButton(text = "Alt", isActive = isAlt, weight = 1.2f) { isAlt = !isAlt }
            Spacer(modifier = Modifier.width(2.dp))
            
            KeyButton(text = "I M E", code = "KANJI", weight = 2f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
             Spacer(modifier = Modifier.width(2.dp))
            KeyButton(text = "", code = "SPACE", weight = 5f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift) // Space
            
             Spacer(modifier = Modifier.width(2.dp))
            KeyButton(text = "←", code = "LEFT", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
             Spacer(modifier = Modifier.width(2.dp))
             
             // Up/Down splitted? Let's just put simple arrow keys
             Column(modifier = Modifier.weight(1f)) {
                 KeyButton(text = "↑", code = "UP", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
                 Spacer(modifier = Modifier.height(1.dp))
                 KeyButton(text = "↓", code = "DOWN", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
             }
             
             Spacer(modifier = Modifier.width(2.dp))
            KeyButton(text = "→", code = "RIGHT", weight = 1f, viewModel = viewModel, isCtrl, isAlt, isWin, isShift, color = Color(0xFF424242))
        }
    }
}

@Composable
fun RowScope.KeyButton(
    text: String,
    code: String,
    weight: Float,
    viewModel: MainViewModel,
    isCtrl: Boolean, isAlt: Boolean, isWin: Boolean, isShift: Boolean,
    color: Color = Color(0xFF2D2D2D)
) {
    KeyButtonCommon(
        text = text,
        code = code,
        modifier = Modifier.weight(weight).fillMaxSize(),
        viewModel = viewModel,
        isCtrl = isCtrl, isAlt = isAlt, isWin = isWin, isShift = isShift,
        color = color
    )
}

@Composable
fun ColumnScope.KeyButton(
    text: String,
    code: String,
    weight: Float,
    viewModel: MainViewModel,
    isCtrl: Boolean, isAlt: Boolean, isWin: Boolean, isShift: Boolean,
    color: Color = Color(0xFF2D2D2D)
) {
    KeyButtonCommon(
        text = text,
        code = code,
        modifier = Modifier.weight(weight).fillMaxSize(),
        viewModel = viewModel,
        isCtrl = isCtrl, isAlt = isAlt, isWin = isWin, isShift = isShift,
        color = color
    )
}

@Composable
fun KeyButtonCommon(
    text: String,
    code: String,
    modifier: Modifier,
    viewModel: MainViewModel,
    isCtrl: Boolean, isAlt: Boolean, isWin: Boolean, isShift: Boolean,
    color: Color
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Haptic
                viewModel.hapticManager.performClick()
                
                // Send Key
                val modifiers = mutableListOf<String>()
                if (isCtrl) modifiers.add("CTRL")
                if (isAlt) modifiers.add("ALT")
                if (isWin) modifiers.add("WIN")
                if (isShift) modifiers.add("SHIFT")
                
                viewModel.networkClient.sendKey(code, modifiers)
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RowScope.ToggleKeyButton(
    text: String,
    isActive: Boolean,
    weight: Float,
    onClick: () -> Unit
) {
     Surface(
        color = if (isActive) Color(0xFF2196F3) else Color(0xFF424242),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .weight(weight)
            .fillMaxSize()
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

