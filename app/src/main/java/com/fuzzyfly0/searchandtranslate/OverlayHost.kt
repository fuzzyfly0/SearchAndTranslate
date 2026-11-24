package com.fuzzyfly0.searchandtranslate

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * What hosts the overlay UI: the [OverlayActivity] (accessibility launch) or the
 * assistant session's own window (default digital assistant launch). The screen is the
 * same; only opening other screens and coming back from them differs.
 */
interface OverlayHost {
    /** True inside the assistant session window: the translation chrome lives there too. */
    val isAssistSession: Boolean

    /**
     * Opens [intent] (a NEW_TASK activity intent). With [returnHere] that screen brings the
     * overlay back itself through [settingsReturnIntent]; otherwise the overlay may close.
     */
    fun startExternal(intent: Intent, returnHere: Boolean)

    /** Opens the share sheet; the overlay comes back if it is cancelled. */
    fun startChooser(chooser: Intent)

    /** Started by the Translator settings when they close, to bring the overlay back. */
    fun settingsReturnIntent(): Intent
}

val LocalOverlayHost = staticCompositionLocalOf<OverlayHost?> { null }

/**
 * Content drawn above the translation chrome (assistant window only): the screen puts a
 * translated circle's brackets there, so the translation can't cover them.
 */
val LocalOverlayTopLayer =
    staticCompositionLocalOf<androidx.compose.runtime.MutableState<(@androidx.compose.runtime.Composable () -> Unit)?>?> { null }

/** Activity hosting: other screens simply open on top of the overlay activity. */
class ActivityOverlayHost(private val context: Context) : OverlayHost {
    override val isAssistSession: Boolean = false

    override fun startExternal(intent: Intent, returnHere: Boolean) {
        runCatching { context.startActivity(intent) }
            .onFailure { android.util.Log.e("OverlayHost", "startActivity failed", it) }
    }

    override fun startChooser(chooser: Intent) = startExternal(chooser, returnHere = true)

    override fun settingsReturnIntent(): Intent =
        Intent(context, OverlayActivity::class.java)
            .putExtra(OverlayActivity.EXTRA_RETURN_FROM_SETTINGS, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
}
