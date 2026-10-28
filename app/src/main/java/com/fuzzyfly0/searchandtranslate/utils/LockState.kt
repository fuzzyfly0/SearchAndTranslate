package com.fuzzyfly0.searchandtranslate.utils

import android.app.KeyguardManager
import android.content.Context

/**
 * True while the lock screen is showing. Search & Translate never launches then
 * (no trigger, assistant or tile): it would capture and show the screen of a
 * locked device.
 */
fun isDeviceLocked(context: Context): Boolean =
    (context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.isKeyguardLocked == true
