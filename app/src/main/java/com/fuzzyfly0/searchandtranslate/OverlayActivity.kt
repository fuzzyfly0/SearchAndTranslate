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

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fuzzyfly0.searchandtranslate.data.BitmapRepository
import com.fuzzyfly0.searchandtranslate.ui.SearchAndTranslateScreen
import com.fuzzyfly0.searchandtranslate.ui.theme.SearchAndTranslateTheme

class OverlayActivity : ComponentActivity() {

    private companion object {
        /** Cookie-banner choices kept across sessions, per site. */
        val CONSENT_COOKIES = listOf(
            "https://www.google.com" to setOf("SOCS", "CONSENT"),
            "https://www.bing.com" to setOf("BCP", "MSCC"),
        )
    }
    
    private val screenshotBitmap = androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(null)
    private val searchModeOverride = androidx.compose.runtime.mutableStateOf<Boolean?>(null)
    private var translateBridge: AssistantTranslateOverlayBridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0))
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        android.util.Log.d("SearchAndTranslate", "OverlayActivity onCreate")
        
        // Allow chrome://inspect attachment for diagnosing engine tabs (debug builds only)
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            android.webkit.WebView.setWebContentsDebuggingEnabled(true)
        }

        // Fresh cookie jar each session (Incognito-equivalent): Google cookies
        // accumulated across challenged/failed sessions may be trust-flagged,
        // which breaks Lens ("image not associated with your account") while a
        // clean Incognito profile loads the same URL fine.
        // Only the cookie-banner choice is carried over (Google: SOCS/CONSENT,
        // Bing/Microsoft: BCP/MSCC), so the consent dialog doesn't reappear on
        // every search while all other (tracking/trust) cookies are still wiped.
        try {
            val cm = android.webkit.CookieManager.getInstance()
            val keptConsent = CONSENT_COOKIES.flatMap { (url, names) ->
                cm.getCookie(url).orEmpty()
                    .split(";")
                    .map { it.trim() }
                    .filter { c -> names.any { c.startsWith("$it=") } }
                    .map { url to it }
            }
            cm.removeAllCookies {
                keptConsent.forEach { (url, cookie) ->
                    val host = android.net.Uri.parse(url).host.orEmpty().removePrefix("www.")
                    cm.setCookie(url, "$cookie; Domain=.$host; Path=/; Secure; Max-Age=31536000")
                }
                cm.flush()
            }
            android.util.Log.d("LensDiag", "COOKIES_CLEARED (kept ${keptConsent.size} consent cookies)")
        } catch (e: Exception) {
            android.util.Log.e("LensDiag", "cookie clear failed: $e")
        }
        
        // Ensure the activity can receive touches and focus properly
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        
        com.fuzzyfly0.searchandtranslate.AssistSessionService.registerOverlayActivity(this)

        loadScreenshot()
        updateOverride(intent)

        setContent {
            SearchAndTranslateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    SearchAndTranslateScreen(
                        capturedScreenshot = screenshotBitmap.value,
                        searchModeOverride = searchModeOverride.value,
                        onClose = { 
                            BitmapRepository.clear()
                            finish() 
                        }
                    )
                }
            }
        }

        // Activity-hosted translate overlay: reuses the exact same OverlayUI chrome the
        // accessibility path uses, hosted in this activity's window (no a11y permission
        // needed for the OCR/translation — only the window type differed).
        // NOTE: must be created AFTER setContent — setContent installs the ComposeView
        // via setContentView, which removes any previously added content views (including
        // the bridge's container), detaching the translate chrome from the window.
        translateBridge = AssistantTranslateOverlayBridge(this).also { it.start() }
        AssistantTranslateOverlay.attach(translateBridge!!)
    }


    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        android.util.Log.d("SearchAndTranslate", "OverlayActivity onNewIntent - Resetting state")
        setIntent(intent)
        
        // IMMEDIATE NULLING to prevent flash of previous screen
        screenshotBitmap.value = null
        searchModeOverride.value = null
        
        loadScreenshot()
        updateOverride(intent)
    }

    private fun updateOverride(intent: android.content.Intent) {
        if (intent.hasExtra("EXTRA_SEARCH_MODE_OVERRIDE")) {
            searchModeOverride.value = intent.getBooleanExtra("EXTRA_SEARCH_MODE_OVERRIDE", false)
        } else {
            searchModeOverride.value = null
        }
    }

    private fun loadScreenshot() {
        val bitmap = BitmapRepository.getScreenshot()
        if (bitmap != null) {
            android.util.Log.d("SearchAndTranslate", "Bitmap loaded from Repository. Size: ${bitmap.width}x${bitmap.height}")
            screenshotBitmap.value = bitmap
        } else {
            android.util.Log.e("SearchAndTranslate", "No bitmap in Repository")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        translateBridge?.let {
            AssistantTranslateOverlay.detach(it)
            it.destroy()
        }
        translateBridge = null
        if (isFinishing) {
             com.fuzzyfly0.searchandtranslate.data.BitmapRepository.clear()
             com.fuzzyfly0.searchandtranslate.utils.StorageUtils.clearAppCache(this)
        }
    }
}
