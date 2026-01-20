package com.smartmause.infinity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmause.infinity.MainViewModel
import com.smartmause.infinity.network.Macro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val macros by viewModel.macros.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.fetchMacros()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Macros") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchMacros() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (macros.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "No Macros Found.\nCreate macros in Windows app.",
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(macros) { macro ->
                    MacroButton(
                        name = macro.name,
                        onClick = { viewModel.executeMacro(macro.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MacroButton(name: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF252525),
        contentColor = Color.White,
        modifier = Modifier.height(80.dp).fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
