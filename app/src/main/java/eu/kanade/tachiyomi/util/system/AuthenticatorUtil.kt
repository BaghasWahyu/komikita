package eu.kanade.tachiyomi.util.system

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.confirm_lock_change
import tachiyomi.i18n.komikita_auth_unavailable
import kotlin.coroutines.resume

object AuthenticatorUtil {

    private const val ALLOWED_AUTHENTICATORS = Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL

    /**
     * Avoid double authentication while an external credential activity is open on older APIs.
     */
    var isAuthenticating = false
        private set

    fun FragmentActivity.startAuthentication(
        title: String,
        subtitle: String? = null,
        confirmationRequired: Boolean = true,
        onSuccess: () -> Unit,
        onError: (CharSequence) -> Unit,
    ): BiometricPrompt {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setConfirmationRequired(confirmationRequired)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isAuthenticating = false
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    isAuthenticating = false
                    onError(errString)
                }
            },
        )
        isAuthenticating = true
        prompt.authenticate(promptInfo)
        return prompt
    }

    suspend fun FragmentActivity.authenticate(
        title: String,
        subtitle: String? = stringResource(MR.strings.confirm_lock_change),
        allowUnsupported: Boolean = true,
    ): Boolean = suspendCancellableCoroutine { cont ->
        if (!isAuthenticationSupported()) {
            if (!allowUnsupported) applicationContext.toast(MR.strings.komikita_auth_unavailable)
            cont.resume(allowUnsupported)
            return@suspendCancellableCoroutine
        }

        if (isAuthenticating) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }

        val toastContext = applicationContext
        val prompt = startAuthentication(
            title,
            subtitle,
            onSuccess = { if (cont.isActive) cont.resume(true) },
            onError = { error ->
                toastContext.toast(error.toString())
                if (cont.isActive) cont.resume(false)
            },
        )
        cont.invokeOnCancellation {
            isAuthenticating = false
            prompt.cancelAuthentication()
        }
    }

    /**
     * Returns true if Class 2 biometric or credential lock is set and available to use.
     */
    fun Context.isAuthenticationSupported(): Boolean {
        return BiometricManager.from(this).canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
