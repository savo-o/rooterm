package com.savoo.rooterm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.savoo.rooterm.ui.screens.TerminalScreen
import com.savoo.rooterm.ui.theme.RooTermTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val vm: TerminalViewModel by viewModels()
    private var authenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val requireFingerprint = vm.requireFingerprint.first()

            if (requireFingerprint) {
                val biometricManager = BiometricManager.from(this@MainActivity)
                var canAuthenticate = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
                    canAuthenticate = biometricManager.canAuthenticate(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }

                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    val executor = ContextCompat.getMainExecutor(this@MainActivity)
                    val callback = object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            authenticated = true
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            finish()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            finish()
                        }
                    }

                    val prompt = BiometricPrompt(this@MainActivity, executor, callback)
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Rooterm")
                        .setSubtitle("Authenticate to continue")
                        .setAllowedAuthenticators(
                            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build()

                    prompt.authenticate(promptInfo)
                } else {
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Biometric authentication is not available on this device",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@launch
                }
            } else {
                authenticated = true
            }
        }

        setContent {
            val theme    = vm.termTheme.collectAsState()
            val fontSize = vm.fontSize.collectAsState()
            val darkMode = vm.darkMode.collectAsState()

            RooTermTheme(
                termColorTheme  = theme.value,
                fontSize        = fontSize.value.sp,
                useDynamicColor = false,
                isDarkMode      = darkMode.value,
            ) {
                if (authenticated) {
                    TerminalScreen(vm = vm)
                }
            }
        }
    }
}
