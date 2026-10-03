package com.stockapp.security

import android.content.Context
import android.os.CancellationSignal
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executor

class BiometricAuthHelper(
    private val activity: FragmentActivity,
    private val executor: Executor = ContextCompat.getMainExecutor(activity)
) {

    private var authenticationCallback: BiometricPrompt.AuthenticationCallback? = null
    private var biometricPrompt: BiometricPrompt? = null

    interface AuthCallback {
        fun onSuccess()
        fun onError(errorCode: Int, errorMessage: String)
        fun onCancel()
    }

    fun authenticate(
        title: String = "Biometric Authentication",
        subtitle: String = "Use your fingerprint or face to unlock",
        description: String = "Stock Portfolio Manager requires authentication",
        negativeButtonText: String = "Cancel",
        callback: AuthCallback
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setDeviceCredentialAllowed(true)
            .build()

        authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                callback.onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                callback.onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                // Not treated as error, user can retry
            }
        }

        biometricPrompt = BiometricPrompt(activity, executor, authenticationCallback!!)
        biometricPrompt!!.authenticate(promptInfo)
    }

    fun cancel() {
        biometricPrompt?.cancelAuthentication()
    }
}

// Coroutine wrapper for easier use in ViewModels
suspend fun CoroutineScope.authenticateWithBiometric(
    activity: FragmentActivity,
    title: String = "Biometric Authentication",
    subtitle: String = "Use your fingerprint or face to unlock",
    description: String = "Stock Portfolio Manager requires authentication"
): Boolean = suspendCancellableCoroutine { continuation ->
    val helper = BiometricAuthHelper(activity)
    helper.authenticate(title, subtitle, description, "Cancel", object : BiometricAuthHelper.AuthCallback {
        override fun onSuccess() {
            continuation.resume(true)
        }

        override fun onError(errorCode: Int, errorMessage: String) {
            continuation.resume(false)
        }

        override fun onCancel() {
            continuation.resume(false)
        }
    })
    continuation.invokeOnCancellation {
        helper.cancel()
    }
}