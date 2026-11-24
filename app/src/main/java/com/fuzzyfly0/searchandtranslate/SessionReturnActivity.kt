package com.fuzzyfly0.searchandtranslate

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * Invisible trampoline that brings the assistant-hosted overlay ([AssistOverlaySession])
 * back after another screen it opened closes:
 * - with [EXTRA_TARGET] (the share sheet): starts it and, when it returns without a
 *   chosen app, shows the overlay again; once an app received the image the overlay ends;
 * - without (started by the Translator settings closing): shows the overlay again.
 */
class SessionReturnActivity : Activity() {

    companion object {
        private const val EXTRA_TARGET = "com.fuzzyfly0.searchandtranslate.extra.RETURN_TARGET"
        private const val ACTION_CHOSEN = "com.fuzzyfly0.searchandtranslate.action.SHARE_CHOSEN"
        private const val REQUEST_TARGET = 1
        /** The chooser reports the picked app slightly before it returns. */
        private const val CHOSEN_GRACE_MS = 250L

        fun resumeIntent(context: Context): Intent =
            Intent(context, SessionReturnActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)

        fun chooserIntent(context: Context, chooser: Intent): Intent =
            resumeIntent(context).putExtra(EXTRA_TARGET, chooser)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var chosen = false
    private val chosenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            chosen = true
        }
    }
    private var receiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noTransition()
        val target: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TARGET, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TARGET)
        }
        if (target == null) {
            AssistOverlaySession.resume()
            finish()
            return
        }
        if (savedInstanceState != null) return
        val filter = IntentFilter(ACTION_CHOSEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(chosenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(chosenReceiver, filter)
        }
        receiverRegistered = true
        val chosenSender = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_CHOSEN).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        ).intentSender
        target.putExtra(Intent.EXTRA_CHOSEN_COMPONENT_INTENT_SENDER, chosenSender)
        // For a result the chooser must run in this task.
        target.flags = target.flags and Intent.FLAG_ACTIVITY_NEW_TASK.inv()
        try {
            startActivityForResult(target, REQUEST_TARGET)
        } catch (e: Exception) {
            android.util.Log.e("SessionReturn", "Could not open share sheet", e)
            AssistOverlaySession.resume()
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_TARGET) return
        handler.postDelayed({
            if (chosen) AssistOverlaySession.finishHidden() else AssistOverlaySession.resume()
            finish()
        }, CHOSEN_GRACE_MS)
    }

    override fun finish() {
        super.finish()
        noTransition()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (receiverRegistered) runCatching { unregisterReceiver(chosenReceiver) }
        super.onDestroy()
    }

    private fun noTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
