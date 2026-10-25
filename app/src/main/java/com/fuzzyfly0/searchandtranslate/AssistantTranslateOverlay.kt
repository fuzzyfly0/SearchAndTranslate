package com.fuzzyfly0.searchandtranslate

import android.graphics.Bitmap

/**
 * Activity-scoped holder for the assistant-only translate overlay. Lets
 * SearchAndTranslateScreen start translation (no accessibility permission) through the
 * same OverlayUI chrome the accessibility path uses, hosted in our own activity.
 */
object AssistantTranslateOverlay {
  @Volatile
  var bridge: AssistantTranslateOverlayBridge? = null
    private set

  fun attach(b: AssistantTranslateOverlayBridge) {
    bridge = b
  }

  fun detach(b: AssistantTranslateOverlayBridge) {
    if (bridge === b) bridge = null
  }

  /**
   * Starts OCR + translation of an already-captured screenshot using the
   * activity-hosted overlay. Returns false if no bridge is attached.
   * [tapToCopy] — false for live-screen modes: the overlay window stops
   * consuming taps (FLAG_NOT_TOUCHABLE on the hosting activity) so the app
   * underneath stays interactive, mirroring the accessibility path.
   */
  fun start(
    screenshot: Bitmap,
    uiDelayMs: Long = 0L,
    tapToCopy: Boolean = true,
    region: android.graphics.Rect? = null,
    onClosed: (() -> Unit)? = null,
  ): Boolean {
    val b = bridge ?: return false
    b.startCapturedTranslation(screenshot, uiDelayMs, onClosed, tapToCopy, region)
    return true
  }

  fun close() {
    bridge?.deactivate()
  }
}
