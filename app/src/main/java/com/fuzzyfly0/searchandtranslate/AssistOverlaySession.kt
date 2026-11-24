package com.fuzzyfly0.searchandtranslate

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.voice.VoiceInteractionSession
import android.view.ContextThemeWrapper
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.fuzzyfly0.searchandtranslate.data.BitmapRepository
import com.fuzzyfly0.searchandtranslate.ui.SearchAndTranslateScreen
import com.fuzzyfly0.searchandtranslate.ui.theme.SearchAndTranslateTheme

/**
 * The whole overlay (frozen screenshot, circle selection, pill, results, translation,
 * language sheet) hosted in the assistant session's own window instead of an activity.
 *
 * The voice interaction window sits above every app and isn't part of any task, so the
 * Home / recents gesture no longer sends the overlay away: only Back or Close end it
 * ([onCloseSystemDialogs] is ignored on purpose). Screens the overlay opens (share sheet,
 * Translator settings) appear with the session window hidden, which shows again when
 * they close (see [SessionReturnActivity]).
 */
class AssistOverlaySession(context: Context) :
    VoiceInteractionSession(context),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner,
    OnBackPressedDispatcherOwner {

    companion object {
        private const val TAG = "AssistOverlaySession"

        /** showSession arg: bring the hidden overlay back (share sheet / settings closed). */
        const val EXTRA_RESUME = "com.fuzzyfly0.searchandtranslate.extra.RESUME_OVERLAY"

        /** showSession arg: the capture is already in [BitmapRepository] (accessibility trigger). */
        private const val EXTRA_FROM_REPOSITORY = "com.fuzzyfly0.searchandtranslate.extra.FROM_REPOSITORY"
        private const val EXTRA_SEARCH_MODE_OVERRIDE = "com.fuzzyfly0.searchandtranslate.extra.SEARCH_MODE_OVERRIDE"

        /**
         * Accessibility triggers (double-tap, shortcut) open the overlay in the assistant
         * window as well while this app is the default assistant, so Home can't send it
         * away there either. False if the assistant session isn't available.
         */
        fun showFromRepository(searchModeOverride: Boolean?): Boolean {
            val service = SearchAndTranslateVoiceService.instance ?: return false
            if (BitmapRepository.getScreenshot() == null) return false
            val args = Bundle().apply {
                putBoolean(EXTRA_FROM_REPOSITORY, true)
                searchModeOverride?.let { putBoolean(EXTRA_SEARCH_MODE_OVERRIDE, it) }
            }
            return runCatching { service.showSession(args, 0) }
                .onFailure { android.util.Log.e(TAG, "showSession failed", it) }
                .isSuccess
        }

        /** How long to wait for the system's screenshot before falling back. */
        private const val SCREENSHOT_TIMEOUT_MS = 1500L

        @Volatile
        var current: AssistOverlaySession? = null
            private set

        /** Brings a hidden overlay back; false if there is no session to resume. */
        fun resume(): Boolean {
            val service = SearchAndTranslateVoiceService.instance ?: return false
            if (current?.awaitingReturn != true) return false
            return runCatching {
                service.showSession(Bundle().apply { putBoolean(EXTRA_RESUME, true) }, 0)
            }.isSuccess
        }

        /** Ends a hidden overlay for good (the share sheet sent the image to an app). */
        fun finishHidden() {
            current?.takeIf { it.awaitingReturn }?.finish()
        }
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    /** Back with no enabled handler in the screen closes the overlay. */
    override val onBackPressedDispatcher = OnBackPressedDispatcher { finish() }

    private val handler = Handler(Looper.getMainLooper())
    private val screenshot = mutableStateOf<Bitmap?>(null)
    private val searchModeOverride = mutableStateOf<Boolean?>(null)
    /** Screen is composed once a capture arrived (or it is known none will). */
    private val ready = mutableStateOf(false)
    /** New invocation = fresh screen state. */
    private val generation = mutableIntStateOf(0)
    private var waitingForScreenshot = false
    /** Hidden by us while another screen is open; it brings the overlay back. */
    private var awaitingReturn = false
    private var bridge: AssistantTranslateOverlayBridge? = null

    private val host = object : OverlayHost {
        override val isAssistSession: Boolean = true

        override fun startExternal(intent: Intent, returnHere: Boolean) {
            val started = runCatching { context.startActivity(intent) }
                .onFailure { android.util.Log.e(TAG, "startActivity failed", it) }
                .isSuccess
            if (!started) return
            if (returnHere) {
                awaitingReturn = true
                hide()
            } else {
                finish()
            }
        }

        override fun startChooser(chooser: Intent) {
            startExternal(SessionReturnActivity.chooserIntent(context, chooser), returnHere = true)
        }

        override fun settingsReturnIntent(): Intent = SessionReturnActivity.resumeIntent(context)
    }

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        current = this
        setUpWindow()
        // Predictive back: the window's back gesture drives the screen's handlers with
        // progress (as in an activity), so translation windows can make way for the
        // system arrow. Without enabled handlers it falls back to onBackPressed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            window?.window?.onBackInvokedDispatcher?.let {
                onBackPressedDispatcher.setOnBackInvokedDispatcher(it)
            }
        }
    }

    override fun onCreateContentView(): View {
        val w = window.window!!
        val uiContext = SessionUiContext(context, w)
        val root = SystemGestureGuardLayout(uiContext)
        val compose = ComposeView(uiContext).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@AssistOverlaySession))
        }
        // Compose reads the owners from the window's root view as well as its own.
        listOf<View>(w.decorView, root, compose).forEach { installOwners(it) }
        compose.setContent {
            val shot = screenshot.value
            if (ready.value) {
                key(generation.intValue) {
                    CompositionLocalProvider(LocalOverlayHost provides host) {
                        SearchAndTranslateTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent,
                                tonalElevation = 0.dp,
                            ) {
                                SearchAndTranslateScreen(
                                    capturedScreenshot = shot,
                                    searchModeOverride = searchModeOverride.value,
                                    onClose = { finish() },
                                )
                            }
                        }
                    }
                }
            }
        }
        root.addView(compose, matchParent())
        // Translation chrome: a full-screen container above the screen, as in the activity.
        bridge = AssistantTranslateOverlayBridge(
            uiContext,
            { window?.window },
            { view -> root.addView(view, matchParent()) },
        ).also {
            it.start()
            AssistantTranslateOverlay.attach(it)
        }
        return root
    }

    private fun installOwners(view: View) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewTreeOnBackPressedDispatcherOwner(this)
    }

    private fun matchParent() = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )

    /** Full screen, edge to edge, touchable and transparent, no window animation. */
    private fun setUpWindow() {
        val w = window?.window ?: return
        w.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0))
        w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        w.setGravity(Gravity.FILL)
        w.setWindowAnimations(0)
        w.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
        )
        w.addFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        )
        WindowCompat.setDecorFitsSystemWindows(w, false)
        @Suppress("DEPRECATION")
        w.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        w.navigationBarColor = android.graphics.Color.TRANSPARENT
        w.isStatusBarContrastEnforced = false
        w.isNavigationBarContrastEnforced = false
        w.attributes = w.attributes.apply {
            layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
        }
        @Suppress("DEPRECATION")
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    /** The session's stock layout pads its content by the system bars: draw under them. */
    private fun clearRootInsets() {
        val decor = window?.window?.decorView ?: return
        var v: View? = (decor as? ViewGroup)?.findViewById<View>(android.R.id.content)
        while (v != null && v !== decor) {
            v.fitsSystemWindows = false
            v.setPadding(0, 0, 0, 0)
            v = v.parent as? View
        }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        android.util.Log.d(TAG, "onShow flags=$showFlags resume=${args?.getBoolean(EXTRA_RESUME)}")
        if (com.fuzzyfly0.searchandtranslate.utils.isDeviceLocked(context)) {
            finish()
            return
        }
        setUpWindow()
        clearRootInsets()

        if (args?.getBoolean(EXTRA_RESUME) == true) {
            if (awaitingReturn && ready.value) {
                awaitingReturn = false
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            } else {
                // Nothing left to come back to.
                finish()
            }
            return
        }
        startNewOverlay(args)
    }

    private fun startNewOverlay(args: Bundle?) {
        handler.removeCallbacksAndMessages(null)
        awaitingReturn = false
        AssistantTranslateOverlay.close()
        ready.value = false
        screenshot.value = null
        searchModeOverride.value =
            if (args?.containsKey(EXTRA_SEARCH_MODE_OVERRIDE) == true) args.getBoolean(EXTRA_SEARCH_MODE_OVERRIDE) else null
        generation.intValue++
        OverlayWebSession.prepare(context)
        vibrate()

        // Contextual-search launches hand the screenshot over in the args.
        val provided: Bitmap? = args?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(AssistSessionService.EXTRA_SCREENSHOT_BITMAP, Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(AssistSessionService.EXTRA_SCREENSHOT_BITMAP)
            }
        }
            ?: if (args?.getBoolean(EXTRA_FROM_REPOSITORY) == true) BitmapRepository.getScreenshot() else null
        if (provided != null && !provided.isRecycled) {
            showCapture(provided)
            return
        }
        // Otherwise the system delivers it via onHandleScreenshot ("Use screenshot").
        waitingForScreenshot = true
        handler.postDelayed({
            if (waitingForScreenshot) {
                waitingForScreenshot = false
                noScreenshot()
            }
        }, SCREENSHOT_TIMEOUT_MS)
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        super.onHandleScreenshot(screenshot)
        android.util.Log.d(TAG, "onHandleScreenshot null=${screenshot == null} waiting=$waitingForScreenshot")
        if (!waitingForScreenshot) return
        waitingForScreenshot = false
        handler.removeCallbacksAndMessages(null)
        if (screenshot == null) noScreenshot() else showCapture(screenshot)
    }

    private fun showCapture(raw: Bitmap) {
        // OCR / native pixel access expect a software ARGB_8888 bitmap.
        val bmp = runCatching {
            if (raw.config != Bitmap.Config.ARGB_8888) raw.copy(Bitmap.Config.ARGB_8888, false) else raw
        }.getOrNull() ?: raw
        BitmapRepository.setScreenshot(bmp)
        screenshot.value = bmp
        ready.value = true
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    /**
     * No system screenshot ("Use screenshot" off, or none for the first launch after the
     * process started): capture through accessibility into the overlay activity if
     * available, else show the overlay's own missing-capture hint.
     */
    private fun noScreenshot() {
        if (SearchAndTranslateAccessibilityService.instance != null) {
            // The capture comes back into this session (showFromRepository); the window
            // draws nothing until then, so it isn't in the capture.
            android.util.Log.d(TAG, "No system screenshot; accessibility capture fallback")
            SearchAndTranslateAccessibilityService.triggerCapture()
            return
        }
        android.util.Log.w(TAG, "No screenshot source available")
        ready.value = true
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private var finishing = false

    override fun finish() {
        if (finishing) return
        finishing = true
        handler.removeCallbacksAndMessages(null)
        super.finish()
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    /** Home / recents: the overlay stays (like Circle to Search); only Back or Close end it. */
    override fun onCloseSystemDialogs() {
        android.util.Log.d(TAG, "onCloseSystemDialogs ignored")
    }

    override fun onHide() {
        super.onHide()
        android.util.Log.d(TAG, "onHide awaitingReturn=$awaitingReturn")
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        // Hidden by the system (screen off, lock): end the overlay.
        if (!awaitingReturn && !finishing) finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        bridge?.let {
            AssistantTranslateOverlay.detach(it)
            it.destroy()
        }
        bridge = null
        if (current === this) current = null
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
        // A newer capture may already be waiting for the next overlay.
        if (BitmapRepository.getScreenshot() === screenshot.value) BitmapRepository.clear()
        com.fuzzyfly0.searchandtranslate.utils.StorageUtils.clearAppCache(context)
        super.onDestroy()
    }

    /**
     * Root of the session window. A touch that starts in the system's bottom gesture zone
     * is the Home / Back swipe: the system takes it over a moment later, but its first
     * events would reach the overlay and mark a word or start a circle. Such a touch
     * never reaches the overlay at all.
     */
    private class SystemGestureGuardLayout(context: Context) : FrameLayout(context) {
        private var swallowing = false

        private fun bottomGestureZonePx(): Int {
            val insets = rootWindowInsets
                ?.getInsets(android.view.WindowInsets.Type.mandatorySystemGestures())
                ?.bottom ?: 0
            return maxOf(insets, (32 * resources.displayMetrics.density).toInt())
        }

        override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
            if (ev.actionMasked == android.view.MotionEvent.ACTION_DOWN) {
                swallowing = ev.y >= height - bottomGestureZonePx()
            }
            if (swallowing) {
                if (ev.actionMasked == android.view.MotionEvent.ACTION_UP ||
                    ev.actionMasked == android.view.MotionEvent.ACTION_CANCEL
                ) {
                    swallowing = false
                }
                return true
            }
            return super.dispatchTouchEvent(ev)
        }
    }

    /**
     * Context for the views in the session window: the app theme, the window's own
     * WindowManager (popups attach to it) and its display.
     */
    private class SessionUiContext(base: Context, private val hostWindow: Window) :
        ContextThemeWrapper(base, R.style.Theme_SearchAndTranslate_Transparent) {
        override fun getSystemService(name: String): Any? =
            if (name == Context.WINDOW_SERVICE) hostWindow.windowManager else super.getSystemService(name)

        override fun getDisplay(): Display =
            hostWindow.decorView.display
                ?: @Suppress("DEPRECATION") hostWindow.windowManager.defaultDisplay
    }
}
