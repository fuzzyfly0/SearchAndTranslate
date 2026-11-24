/*
 *
 *  * Copyright (C) 2025 AKS-Labs (original author)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fuzzyfly0.searchandtranslate

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import dev.davidv.translator.TranslatorApplication

class AssistSessionService : VoiceInteractionSessionService() {

    companion object {
        /** Screenshot handed from AssistEntryActivity (contextual-search launch). */
        const val EXTRA_SCREENSHOT_BITMAP = "com.fuzzyfly0.searchandtranslate.extra.SCREENSHOT_BITMAP"

        /**
         * Statically invoked from the in-app translate action (accessibility disabled) to show
         * the module's assistant translate overlay. This reuses the module's
         * [TranslatorVoiceInteractionSession] — the exact overlay UI/UX the accessibility path
         * uses — so the translation screen looks and behaves identically; it is driven by the
         * assistant permission rather than a separately-built in-app screen.
         */
        fun showTranslateOverlay(context: Context, screenshot: Bitmap?) {
            pendingTranslateScreenshot = screenshot
            translateMode = true
            val voiceService = SearchAndTranslateVoiceService.instance
            if (voiceService == null) {
                android.util.Log.e("AssistSessionService", "Voice service not ready; cannot show translate overlay")
                translateMode = false
                return
            }
            try {
                // No SHOW_WITH_SCREENSHOT: the screenshot we already hold is handed in via
                // setScreenshot, so the system doesn't need to capture one itself.
                voiceService.showSession(Bundle(), 0)
                // Our overlay activity keeps covering the screen with the frozen capture,
                // and the module session's window isn't an activity. It is closed once the
                // session has consumed the frame — see onNewSession / closeOverlayActivity.
            } catch (e: Exception) {
                android.util.Log.e("AssistSessionService", "Failed to show translate session", e)
                translateMode = false
            }
        }

        /**
         * Closes the SearchAndTranslate overlay activity. Called once the module session has
         * been shown so its window is the one on screen; without this the frozen capture
         * drawn by the activity covers the session's toolbar/scan UI.
         */
        fun closeOverlayActivity(context: Context) {
            val activity = activityRef ?: return
            activityRef = null
            activity.runOnUiThread {
                android.util.Log.d("AssistSessionService", "Closing overlay activity so module session is visible")
                activity.finish()
            }
        }

        /** [OverlayActivity] registers itself on create so [closeOverlayActivity] can dismiss it. */
        fun registerOverlayActivity(activity: android.app.Activity) {
            activityRef = activity
        }

        private var translateMode = false
        private var pendingTranslateScreenshot: Bitmap? = null
        private var activityRef: android.app.Activity? = null

        /** Consumed once by the module session so a later plain assist isn't forced into translate. */
        internal fun consumeTranslateScreenshot(): Bitmap? =
            pendingTranslateScreenshot.also { pendingTranslateScreenshot = null }
    }

    private var langStateManager: dev.davidv.translator.LanguageStateManager? = null
    private var overlayTextTranslationHelper: dev.davidv.translator.OverlayTextTranslationHelper? = null

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("AssistSessionService", "Service onCreate")
        // Wire the module overlay's dependencies from the shared application singletons.
        // Mirrors the wiring the module's own TranslatorVoiceInteractionSessionService does,
        // so the overlay gets the same state as the accessibility path.
        val app = application as? TranslatorApplication
        if (app != null) {
            val lm = dev.davidv.translator.LanguageStateManager(
                kotlinx.coroutines.CoroutineScope(
                    kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main
                ),
                app.filePathManager,
                null,
            )
            langStateManager = lm
            overlayTextTranslationHelper =
                dev.davidv.translator.OverlayTextTranslationHelper(lm, app.languageMetadataManager)
        }
    }

    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        android.util.Log.d("AssistSessionService", "onNewSession created")
        // Translate mode shows the module's assistant overlay — the same UI/UX as the
        // accessibility path — instead of the search overlay. This is how the translation
        // screen is reused rather than reimplemented in-app.
        if (translateMode) {
            translateMode = false
            val app = application as? TranslatorApplication
            val lm = langStateManager
            val helper = overlayTextTranslationHelper
            if (app != null && lm != null && helper != null) {
                android.util.Log.d("AssistSessionService", "Returning module translator session")
                val session = dev.davidv.translator.assistantOverlay.TranslatorVoiceInteractionSession(
                    this,
                    app.settingsManager,
                    app.translationCoordinator,
                    helper,
                    lm,
                )
                // Close our overlay once the session has actually taken up the frame, so
                // dismissing our activity can't race the hand-off (onShow clears a pending
                // capture, and the first layout is what triggers consumePendingScreenshot).
                session.onScreenshotConsumed = { closeOverlayActivity(applicationContext) }
                // Hand the already-captured screenshot to the session's existing path.
                val shot = consumeTranslateScreenshot()
                if (shot != null) session.setScreenshot(shot)
                return session
            }
            android.util.Log.e("AssistSessionService", "Translate deps unavailable; falling back")
        }
        // The whole overlay runs inside this session's window (not an activity), so the
        // Home gesture can't send it to the background — see AssistOverlaySession.
        return AssistOverlaySession(this)
    }
}
