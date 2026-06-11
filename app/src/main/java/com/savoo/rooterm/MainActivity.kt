package com.savoo.rooterm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import com.savoo.rooterm.ui.screens.TerminalScreen
import com.savoo.rooterm.ui.theme.RooTermTheme

class MainActivity : ComponentActivity() {
    private val vm: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme    = vm.termTheme.collectAsState()
            val fontSize = vm.fontSize.collectAsState()
            val dynColor = vm.dynamicColor.collectAsState()

            RooTermTheme(
                termColorTheme  = theme.value,
                fontSize        = fontSize.value.sp,
                useDynamicColor = dynColor.value,
            ) {
                TerminalScreen(vm = vm)
            }
        }
    }
}
