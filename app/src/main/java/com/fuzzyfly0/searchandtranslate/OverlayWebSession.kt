package com.fuzzyfly0.searchandtranslate

import android.content.Context

/** WebView setup done each time an overlay opens (activity or assistant session window). */
object OverlayWebSession {
    /** Cookie-banner choices kept across sessions, per site. */
    private val CONSENT_COOKIES = listOf(
        "https://www.google.com" to setOf("SOCS", "CONSENT"),
        "https://www.bing.com" to setOf("BCP", "MSCC"),
    )

    fun prepare(context: Context) {
        // Allow chrome://inspect attachment for diagnosing engine tabs (debug builds only)
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
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
    }
}
