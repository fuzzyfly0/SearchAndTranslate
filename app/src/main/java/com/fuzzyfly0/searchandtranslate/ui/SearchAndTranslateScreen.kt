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

package com.fuzzyfly0.searchandtranslate.ui

import android.graphics.Bitmap
import android.graphics.Rect
import com.fuzzyfly0.searchandtranslate.AssistSessionService
import com.fuzzyfly0.searchandtranslate.SearchAndTranslateAccessibilityService
import android.util.Base64
import android.view.Display
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.automirrored.filled.FormatTextdirectionRToL
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.fuzzyfly0.searchandtranslate.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.webkit.WebSettingsCompat
import com.fuzzyfly0.searchandtranslate.data.SearchEngine
import com.fuzzyfly0.searchandtranslate.ui.theme.OverlayGradientColors
import com.fuzzyfly0.searchandtranslate.utils.ImageSearchUploader
import com.fuzzyfly0.searchandtranslate.utils.ImageUtils
import dev.davidv.translator.localizedName
import com.fuzzyfly0.searchandtranslate.utils.UIPreferences
import dev.davidv.translator.TranslatorApplication
import dev.davidv.translator.ui.components.DetectedRegions
import dev.davidv.translator.ui.components.ImageWordSelection
import dev.davidv.translator.ui.components.ScanAnimationOverlay
import dev.davidv.translator.ui.components.WordSelectionOverlay
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.webkit.WebViewFeature
import androidx.webkit.UserAgentMetadata
import com.fuzzyfly0.searchandtranslate.data.isDirectUpload
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import android.os.Build
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Surface



/** Lets the SearchAndTranslate chrome hide before the translator overlay draws. */
private const val TRANSLATE_HIDE_DELAY_MS = 150L

/**
 * Runs the translator module's OCR over [bmp] and returns its recognized source words —
 * no translation, no overlay bitmap. [onRegions] fires as soon as detection boxes arrive
 * (for the scan animation). Returns null on failure: missing OCR models and no-text both
 * show a toast.
 */
private suspend fun runInstantScreenshotOcr(
    context: android.content.Context,
    bmp: Bitmap,
    onRegions: (DetectedRegions) -> Unit,
): ImageWordSelection? {
    val app = context.applicationContext as? TranslatorApplication ?: return null
    val coordinator = runCatching { app.translationCoordinator }.getOrNull() ?: return null
    // The accessibility service exposes the configured target language when it is
    // running, but the assistant launch path must also work with accessibility
    // disabled — resolve the same setting from the translator application directly.
    val target = SearchAndTranslateAccessibilityService.instance?.defaultTargetLanguage()
        ?: runCatching {
            app.languageCatalog?.languageByCode(
                app.settingsManager.settings.value.defaultTargetLanguageCode
            )
        }.getOrNull()
        ?: return null
    var ocrMissing = false
    var sourceDetectFailed = false
    val result = coordinator.ocrWordsOnly(
        bitmap = bmp,
        to = target,
        onDetectedRegions = { boxes, w, h -> onRegions(DetectedRegions(w, h, boxes)) },
        onOcrUnavailable = { ocrMissing = true },
        onAutoSourceFailed = { sourceDetectFailed = true },
    )
    if (result == null) {
        val messageId =
            when {
                ocrMissing -> dev.davidv.translator.R.string.ocr_models_missing
                sourceDetectFailed -> R.string.label_language_detect_failed
                else -> R.string.label_no_text_found
            }
        withContext(Dispatchers.Main) {
            android.widget.Toast.makeText(
                context,
                context.getString(messageId),
                android.widget.Toast.LENGTH_SHORT,
            ).show()
        }
        return null
    }
    return ImageWordSelection(
        imageWidth = result.imageWidth,
        imageHeight = result.imageHeight,
        sourceWords = result.sourceWords,
        translatedWords = result.sourceWords,
    )
}

/**
 * Fit (contain) mapping for a bitmap centered in a view — the app-side mirror of the
 * module's internal fitTransform, which the word/scan overlays use. Unit-agnostic: pass
 * view/image sizes in px (crop path) or dp (chip path) consistently for both.
 */
private class FitMapping(val scale: Float, val offsetX: Float, val offsetY: Float) {
    fun unmapX(x: Float) = (x - offsetX) / scale
    fun unmapY(y: Float) = (y - offsetY) / scale
}

private fun fitMapping(viewW: Float, viewH: Float, imageW: Float, imageH: Float): FitMapping {
    val scale = min(viewW / imageW, viewH / imageH)
    return FitMapping(scale, (viewW - imageW * scale) / 2f, (viewH - imageH * scale) / 2f)
}

/**
 * Maps a screen-space rect into bitmap space for the ContentScale.Fit display, clamping to
 * the bitmap. For screen captures (bitmap size == view size) this is the identity.
 */
private fun fitCrop(
    rect: android.graphics.Rect,
    t: FitMapping,
    imageW: Int,
    imageH: Int,
): android.graphics.Rect =
    android.graphics.Rect(
        t.unmapX(rect.left.toFloat()).toInt().coerceIn(0, imageW),
        t.unmapY(rect.top.toFloat()).toInt().coerceIn(0, imageH),
        t.unmapX(rect.right.toFloat()).toInt().coerceIn(0, imageW),
        t.unmapY(rect.bottom.toFloat()).toInt().coerceIn(0, imageH),
    )

/**
 * The device's physical screen corner radii in px: upper-left, upper-right,
 * lower-right, lower-left. `Display.getRoundedCorner` (Android 12+) reports the
 * real bezel radius per corner and returns null where the screen is rectangular;
 * older OEM builds usually expose the physical radius as an `android` framework
 * dimen. Every unresolved path yields 0, so the overlay border follows the true
 * screen shape on any device.
 */
private fun deviceCornerRadiiPx(context: android.content.Context): FloatArray {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val display = context.display
            ?: (context as? android.app.Activity)?.windowManager?.let {
                @Suppress("DEPRECATION")
                it.defaultDisplay
            }
        if (display != null) {
            fun radiusAt(position: Int): Float =
                display.getRoundedCorner(position)?.radius?.toFloat() ?: 0f
            return floatArrayOf(
                radiusAt(android.view.RoundedCorner.POSITION_TOP_LEFT),
                radiusAt(android.view.RoundedCorner.POSITION_TOP_RIGHT),
                radiusAt(android.view.RoundedCorner.POSITION_BOTTOM_RIGHT),
                radiusAt(android.view.RoundedCorner.POSITION_BOTTOM_LEFT),
            )
        }
    }
    val id = context.resources.getIdentifier("rounded_corner_radius", "dimen", "android")
    val r = if (id != 0) context.resources.getDimension(id) else 0f
    return floatArrayOf(r, r, r, r)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchAndTranslateScreen(
    capturedScreenshot: Bitmap?,
    onClose: () -> Unit,
    searchModeOverride: Boolean? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Border shape follows the physical screen corners (rounded bezel vs.
    // rectangular) so the overlay outline matches the device on any screen ratio.
    val density = LocalDensity.current
    val deviceCornerShape = remember(density, context) {
        val radii = deviceCornerRadiiPx(context)
        with(density) {
            RoundedCornerShape(
                topStart = radii[0].toDp(),
                topEnd = radii[1].toDp(),
                bottomEnd = radii[2].toDp(),
                bottomStart = radii[3].toDp(),
            )
        }
    }
    // Exact physical screen outline (Android 14+), e.g. Pixel's squircle-like
    // corners; null on older versions, where the reported corner radii are used.
    val displayOutlinePath = remember(context) {
        if (Build.VERSION.SDK_INT >= 34) {
            runCatching { context.display?.shape?.path }.getOrNull()
        } else null
    }

    // Upload flow: the user can swap the canvas for a gallery image and run Circle to
    // Search on it. Keyed on the capture param so a fresh capture supersedes a pick.
    var uploadedImage by remember(capturedScreenshot) { mutableStateOf<Bitmap?>(null) }
    val screenshot = uploadedImage ?: capturedScreenshot

    // Pixel size of the canvas layer — the image-fit mapping (crop-to-search, chips) needs it.
    var canvasSizePx by remember { mutableStateOf(IntSize.Zero) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Decode at original proportions (EXIF-rotated, only sampled past a 12 MP budget).
            // The display fits the bitmap to the screen, so nothing is cropped or stretched.
            val maxPixels = 12 * 1000 * 1000
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val picked = ImageUtils.loadImageUri(context, uri, maxPixels)
                if (picked != null) {
                    uploadedImage = picked
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.toast_image_load_failed),
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }
    
    // Initialize preferences
    val uiPreferences = remember { UIPreferences(context) }
    
    
    
    // New Sheet States

    // Material You logic for colors
    val isDark = isSystemInDarkTheme()
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isDark -> dynamicDarkColorScheme(context)
        dynamicColor && !isDark -> dynamicLightColorScheme(context)
        else -> MaterialTheme.colorScheme // Fallback standard
    }
    //Colors for dark theme
    val barBgColor = if (isDark) Color(0xFF0D0D0D) else colorScheme.surface
    val bubbleColor = if (isDark) Color(0xFF1F1F1F) else colorScheme.secondaryContainer.copy(alpha = 0.9f)
    val contentColor = if (isDark) Color.White else colorScheme.onSecondaryContainer

    // Publish the exact CTS bottom-bar colors so the translate toolbar matches them
    // pixel-for-pixel (the overlay can't read the Compose theme directly).
    val chooserPillColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val closeLabel = stringResource(R.string.cd_close)
    androidx.compose.runtime.SideEffect {
        // The translate toolbar takes the main pill's place: languages · Close.
        dev.davidv.translator.overlayChrome.OverlayChromeFactory.hostPillMode = true
        dev.davidv.translator.overlayChrome.OverlayChromeFactory.hostCloseLabel = closeLabel
        // The translate toolbar copies the circle chooser pill's fill.
        dev.davidv.translator.overlayChrome.OverlayChromeFactory.appChooserPillColor =
            chooserPillColor.toArgb()
        dev.davidv.translator.overlayChrome.OverlayChromeFactory.appBarContainerColor =
            colorScheme.surfaceContainerLow.toArgb()
        dev.davidv.translator.overlayChrome.OverlayChromeFactory.appPillColor =
            colorScheme.surfaceContainerLowest.toArgb()
    }

    //Haptic feedback
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    //Slide-in animation
    var isUIVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isUIVisible = true // Déclenche l'animation à l'ouverture
    }

    // Search Engines Order Logic
    val preferredOrder = remember(uiPreferences.getSearchEngineOrder()) {
        val allEngines = SearchEngine.values()
        val orderString = uiPreferences.getSearchEngineOrder()
        if (orderString == null) allEngines
        else {
            val preferredNames = orderString.split(",")
            val ordered = mutableListOf<SearchEngine>()
            preferredNames.forEach { name ->
                allEngines.find { it.name == name }?.let { ordered.add(it) }
            }
            allEngines.forEach { if (!ordered.contains(it)) ordered.add(it) }
            ordered
        }
    }
    val searchEngines = preferredOrder
    
    // Resizing state
    var isResizing by remember { mutableStateOf(false) }
    var activeHandle by remember { mutableStateOf<String?>(null) } // "tl", "tr", "bl", "br"
    
    

    // Search State
    var selectedEngine by remember(searchEngines) { mutableStateOf<SearchEngine>(searchEngines.first()) }
    var searchUrl by remember { mutableStateOf<String?>(null) }
    var hostedImageUrl by remember { mutableStateOf<String?>(null) }
    var pageBackgroundColor by remember { mutableStateOf(Color.White) }
    var refreshingEngine by remember { mutableStateOf<SearchEngine?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Result pages follow the system dark/light setting.
    val isDarkMode = androidx.compose.foundation.isSystemInDarkTheme()
    

    // Track initialized engines for Smart Loading
    val initializedEngines = remember { mutableStateListOf<SearchEngine>() }

    // Keep the tab strip background equal to the webpage's own background so
    // the sheet never shows a Material color that clashes with the page.
    fun samplePageBackgroundColor(view: WebView?) {
        if (view == null) return
        val js = "(function(){var b=getComputedStyle(document.body).backgroundColor;" +
            "if(b==='rgba(0, 0, 0, 0)'||b==='transparent'){b=getComputedStyle(document.documentElement).backgroundColor;}" +
            "return b;})()"
        val parse: (String) -> Unit = { raw ->
            val m = Regex("""rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)""").find(raw.trim('"'))
            if (m != null) {
                val a = m.groupValues[4].toFloatOrNull() ?: 1f
                if (a > 0f) {
                    pageBackgroundColor = Color(
                        red = m.groupValues[1].toInt() / 255f,
                        green = m.groupValues[2].toInt() / 255f,
                        blue = m.groupValues[3].toInt() / 255f
                    )
                }
            }
        }
        try {
            view.evaluateJavascript(js) { raw -> parse(raw ?: "") }
            view.postDelayed({
                try { view.evaluateJavascript(js) { raw -> parse(raw ?: "") } } catch (_: Exception) {}
            }, 800L)
        } catch (_: Exception) {}
    }
    
    // Result pages always use the mobile layout.
    @Suppress("UNUSED_PARAMETER")
    fun isDesktop(engine: SearchEngine) = false

    // Cache for preloaded URLs to avoid re-uploading/re-generating
    val preloadedUrls = remember { mutableMapOf<SearchEngine, String>() }
    
    // WebView Cache
    val webViews = remember { mutableMapOf<SearchEngine, WebView>() }

    // Clears the pull-to-refresh spinner once the refreshed tab finishes loading.
    fun clearRefreshIfDone(view: WebView?) {
        val re = refreshingEngine
        if (view != null && re != null && view === webViews[re]) refreshingEngine = null
    }
    
    // Update WebViews when dark mode changes
    LaunchedEffect(isDarkMode) {
        webViews.values.forEach { wv ->
            try {
                // Update WebViewClient for dark mode
                if (isDarkMode) {
                    wv.webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            samplePageBackgroundColor(view)
                            clearRefreshIfDone(view)
                            val darkModeCSS = """
                                (function() {
                                    var style = document.createElement('style');
                                    style.innerHTML = `
                                        html { filter: invert(1) hue-rotate(180deg) !important; background: #000 !important; }
                                        img, video, [style*="background-image"] { filter: invert(1) hue-rotate(180deg) !important; }
                                    `;
                                    document.head.appendChild(style);
                                })()
                            """.trimIndent()
                            view?.evaluateJavascript(darkModeCSS, null)
                        }
                    }
                } else {
                    wv.webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            samplePageBackgroundColor(view)
                            clearRefreshIfDone(view)
                        }
                    }
                }
                wv.reload()
                android.util.Log.d("SearchAndTranslate", "Dark mode changed to: $isDarkMode - Reloaded WebViews")
            } catch (e: Exception) {
                android.util.Log.e("SearchAndTranslate", "Error updating dark mode", e)
            }
        }
    }
    
    // Bottom Sheet State
    val scaffoldState = androidx.compose.material3.rememberBottomSheetScaffoldState(
        bottomSheetState = androidx.compose.material3.rememberStandardBottomSheetState(
            initialValue = androidx.compose.material3.SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Drawing State
    val currentPathPoints = remember { mutableStateListOf<Offset>() }
    
    // Selection State
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var selectionRect by remember { mutableStateOf<Rect?>(null) }
    val selectionAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    // Translator overlay mode: SearchAndTranslate chrome hidden, the frozen screenshot
    // stays visible as the backdrop under the module's scan/result overlays.
    var isTranslatorMode by remember { mutableStateOf(false) }

    // "Ask" circle action: the circled area (bitmap px) waiting for the user to pick
    // Search or Translate in the chooser shown next to the selection. Null = no chooser.
    var pendingChoiceRegion by remember { mutableStateOf<android.graphics.Rect?>(null) }

    // The circled area (screen rect + bitmap region) kept while Search, Translate or Lens
    // run on it, so closing them with back brings the circle and its chooser back.
    var savedCircle by remember { mutableStateOf<Pair<android.graphics.Rect, android.graphics.Rect>?>(null) }
    fun saveCircle(rect: android.graphics.Rect?, region: android.graphics.Rect?) {
        savedCircle = if (rect != null && region != null) {
            Pair(android.graphics.Rect(rect), android.graphics.Rect(region))
        } else {
            null
        }
    }
    fun restoreCircle() {
        val (rect, region) = savedCircle ?: return
        savedCircle = null
        selectionRect = rect
        pendingChoiceRegion = region
        selectedBitmap = null
        isSearching = false
        scope.launch { selectionAnim.snapTo(1f) }
    }

    // Instant OCR over the screenshot: scan animation boxes while recognizing, then the
    // recognized source words — tap a word to select and copy (original text, no translation).
    var instantWords by remember { mutableStateOf<ImageWordSelection?>(null) }
    var instantRegions by remember { mutableStateOf<DetectedRegions?>(null) }
    // OCR finished with no words (plan failed / nothing recognized): stop the scan
    // animation instead of breathing over the image forever.
    var instantOcrDone by remember { mutableStateOf(false) }
    // A word selection in the instant-OCR overlay hides the bottom bar; tapping outside the
    // text clears the selection and the bar comes back.
    var wordSelected by remember { mutableStateOf(false) }
    // Bumped to force the word overlay to drop its selection — text selection and a drawn
    // circle are mutually exclusive, so completing a circle clears any active selection.
    var wordSelectionClearSignal by remember { mutableStateOf(0) }

    // Circle-draw gesture handlers, shared by the drag-detector wrapper above the drawing
    // layer (see "Circle-draw gestures" below). Plain drags draw; handle grabs and taps are
    // claimed by the word overlay (child, seen first) so text selection keeps priority.
    fun beginCircleDrag(offset: Offset) {
        val rect = selectionRect
        if (rect != null && selectionAnim.value == 1f) {
            val handleSize = 64f // px for hit testing
            val tl = Offset(rect.left.toFloat(), rect.top.toFloat())
            val tr = Offset(rect.right.toFloat(), rect.top.toFloat())
            val bl = Offset(rect.left.toFloat(), rect.bottom.toFloat())
            val br = Offset(rect.right.toFloat(), rect.bottom.toFloat())

            when {
                (offset - tl).getDistance() < handleSize -> { isResizing = true; activeHandle = "tl" }
                (offset - tr).getDistance() < handleSize -> { isResizing = true; activeHandle = "tr" }
                (offset - bl).getDistance() < handleSize -> { isResizing = true; activeHandle = "bl" }
                (offset - br).getDistance() < handleSize -> { isResizing = true; activeHandle = "br" }
                else -> { isResizing = false; activeHandle = null }
            }

            if (isResizing) return
        }

        // Clear previous state if starting new draw
        currentPathPoints.clear()
        currentPathPoints.add(offset)
        selectionRect = null
        pendingChoiceRegion = null
        scope.launch { selectionAnim.snapTo(0f) }
        // Text selection and drawing are exclusive from the first moment of the gesture:
        // drop any active selection as soon as the user starts drawing, not when the
        // circle completes.
        if (wordSelected) wordSelectionClearSignal++
    }

    fun moveCircleDrag(change: PointerInputChange) {
        if (isResizing && activeHandle != null) {
            val rect = selectionRect ?: return
            val pos = change.position
            val newRect = android.graphics.Rect(rect)
            when (activeHandle) {
                "tl" -> { newRect.left = pos.x.toInt(); newRect.top = pos.y.toInt() }
                "tr" -> { newRect.right = pos.x.toInt(); newRect.top = pos.y.toInt() }
                "bl" -> { newRect.left = pos.x.toInt(); newRect.bottom = pos.y.toInt() }
                "br" -> { newRect.right = pos.x.toInt(); newRect.bottom = pos.y.toInt() }
            }
            // Basic validation (min size)
            if (newRect.width() > 20 && newRect.height() > 20) {
                selectionRect = newRect
            }
        } else {
            currentPathPoints.add(change.position)
        }
    }

    /** The selected area: the searched crop, or the circled area still waiting in the chooser. */
    fun selectionBitmapOrNull(): Bitmap? {
        selectedBitmap?.let { return it }
        val region = pendingChoiceRegion ?: return null
        val bmp = screenshot ?: return null
        return ImageUtils.cropBitmap(bmp, region)
    }

    /** Shares the selected area as a PNG (selection toolbar and results header). */
    fun shareSelection() {
        val bmp = selectionBitmapOrNull() ?: return
        scope.launch {
            try {
                val fileName = "selection_${java.util.UUID.randomUUID()}.png"
                val path = ImageUtils.saveBitmap(context, bmp, fileName)
                val file = java.io.File(path)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("Selection", uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title)).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                android.util.Log.e("SearchAndTranslate", "Failed to share selection", e)
            }
        }
    }

    /** Saves the selected area to the gallery (selection toolbar and results header). */
    fun saveSelection() {
        val bmp = selectionBitmapOrNull() ?: return
        val success = ImageUtils.saveToGallery(context, bmp)
        if (success) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            android.widget.Toast.makeText(context, context.getString(R.string.toast_saved_to_gallery), android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, context.getString(R.string.toast_failed_to_save), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Translates only [region] (bitmap/screen px) of [bmp] with the translator overlay —
     * the result is drawn in place over the frozen capture. Close/back returns to
     * Circle to Search with a cleared selection. Returns false if no translator host
     * is available (caller then falls back to the normal search).
     */
    fun startLassoTranslate(bmp: Bitmap, region: android.graphics.Rect): Boolean {
        if (region.width() <= 10 || region.height() <= 10) return false
        saveCircle(selectionRect, region)
        pendingChoiceRegion = null
        selectionRect = null
        selectedBitmap = null
        isSearching = false
        scope.launch { selectionAnim.snapTo(0f) }
        if (wordSelected) wordSelectionClearSignal++

        val onClosed: () -> Unit = {
            isTranslatorMode = false
            restoreCircle()
        }
        val translatorService = SearchAndTranslateAccessibilityService.instance
        if (translatorService != null) {
            isTranslatorMode = true
            if (translatorService.translateCapturedScreen(
                    bmp, TRANSLATE_HIDE_DELAY_MS, onClosed = onClosed, region = region
                )
            ) return true
            isTranslatorMode = false
        }
        isTranslatorMode = true
        val started = com.fuzzyfly0.searchandtranslate.AssistantTranslateOverlay.start(
            bmp, TRANSLATE_HIDE_DELAY_MS, region = region, onClosed = onClosed
        )
        if (!started) {
            isTranslatorMode = false
            android.widget.Toast.makeText(context, context.getString(R.string.toast_translate_unavailable), android.widget.Toast.LENGTH_SHORT).show()
        }
        return started
    }

    fun endCircleDrag() {
        val bmp = screenshot
        // View size in px — with no bitmap the old behavior (degenerate rect) is kept.
        val viewW = bmp?.let { if (canvasSizePx.width > 0) canvasSizePx.width else it.width } ?: 0
        val viewH = bmp?.let { if (canvasSizePx.height > 0) canvasSizePx.height else it.height } ?: 0
        if (isResizing) {
            isResizing = false
            activeHandle = null
            // Update cropped bitmap after resize. selectionRect is screen-space; map it into
            // image space first (the Fit display may letterbox/scale the bitmap).
            if (bmp != null && selectionRect != null) {
                val t = fitMapping(viewW.toFloat(), viewH.toFloat(), bmp.width.toFloat(), bmp.height.toFloat())
                val region = fitCrop(selectionRect!!, t, bmp.width, bmp.height)
                if (pendingChoiceRegion != null) {
                    // Chooser still open: just move the pending area, don't start a search.
                    pendingChoiceRegion = region
                } else {
                    selectedBitmap = ImageUtils.cropBitmap(bmp, region)
                }
            }
        } else if (currentPathPoints.isNotEmpty()) {
            savedCircle = null
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            currentPathPoints.forEach { p ->
                minX = kotlin.math.min(minX, p.x)
                minY = kotlin.math.min(minY, p.y)
                maxX = kotlin.math.max(maxX, p.x)
                maxY = kotlin.math.max(maxY, p.y)
            }

            val border = 20
            val rect = android.graphics.Rect(
                (minX - border).toInt().coerceAtLeast(0),
                (minY - border).toInt().coerceAtLeast(0),
                (maxX + border).toInt().coerceAtMost(viewW),
                (maxY + border).toInt().coerceAtMost(viewH)
            )

            if (rect.width() > 10 && rect.height() > 10) {
                // Circle action: translate in place, ask (Search | Translate chooser),
                // or fall through to the normal search below.
                if (bmp != null) {
                    val t = fitMapping(viewW.toFloat(), viewH.toFloat(), bmp.width.toFloat(), bmp.height.toFloat())
                    val region = fitCrop(rect, t, bmp.width, bmp.height)
                    when (uiPreferences.getCircleAction()) {
                        UIPreferences.CIRCLE_ACTION_TRANSLATE -> {
                            selectionRect = rect
                            if (startLassoTranslate(bmp, region)) {
                                currentPathPoints.clear()
                                return
                            }
                        }
                        UIPreferences.CIRCLE_ACTION_ASK -> {
                            if (region.width() > 10 && region.height() > 10) {
                                selectionRect = rect
                                currentPathPoints.clear()
                                pendingChoiceRegion = region
                                scope.launch { selectionAnim.animateTo(1f, tween(600)) }
                                return
                            }
                        }
                    }
                }
                selectionRect = rect
                currentPathPoints.clear() // Hide the drawn circle
                if (bmp != null) {
                    val t = fitMapping(viewW.toFloat(), viewH.toFloat(), bmp.width.toFloat(), bmp.height.toFloat())
                    val searchRegion = fitCrop(rect, t, bmp.width, bmp.height)
                    saveCircle(rect, searchRegion)
                    selectedBitmap = ImageUtils.cropBitmap(bmp, searchRegion)
                }
                scope.launch {
                    selectionAnim.animateTo(1f, tween(600))
                    isSearching = true
                }
            }
        }
    }

    // Lifecycle reset: When screenshot changes, reset selection and modes
    LaunchedEffect(screenshot) {
        if (screenshot != null) {
            savedCircle = null
            selectionRect = null
            selectedBitmap = null
            pendingChoiceRegion = null
            isSearching = false
            currentPathPoints.clear()
            selectionAnim.snapTo(0f)
            if (isTranslatorMode) {
                val a11y = SearchAndTranslateAccessibilityService.instance
                if (a11y != null) a11y.closeTranslateOverlay()
                else com.fuzzyfly0.searchandtranslate.AssistantTranslateOverlay.close()
            }
        }
    }
    
    
    // searchEngines moved to top
    // val searchEngines = SearchEngine.values()

    // Gradient Animation
    val alphaAnim by animateFloatAsState(
        targetValue = if (screenshot != null) 1f else 0f,
        animationSpec = tween(1000), label = "alpha"
    )


    // Instant OCR: recognize the screenshot's words as soon as it arrives (module
    // pipeline, no translation) — shown as tappable words over the frozen capture.
    LaunchedEffect(screenshot) {
        instantWords = null
        instantRegions = null
        instantOcrDone = false
        wordSelected = false
        val bmp = screenshot ?: return@LaunchedEffect
        instantWords = runInstantScreenshotOcr(context, bmp) { instantRegions = it }
        instantOcrDone = true
    }

    // The word overlay unmounts in these modes (dropping its selection) - drop ours too so
    // the bottom bar doesn't stay hidden after leaving them.
    LaunchedEffect(isTranslatorMode) {
        if (isTranslatorMode) wordSelected = false
    }

    // Helper to create and configure WebView
    fun createWebView(ctx: android.content.Context, engine: SearchEngine): WebView {
        return WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // Caching & Performance - must be set on WebView directly
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // Performance & UI
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                
                // Caching for Speed
                cacheMode = WebSettings.LOAD_DEFAULT // Was LOAD_CACHE_ELSE_NETWORK - caused refresh issues
                
                // Zoom support
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                
                useWideViewPort = true
                loadWithOverviewMode = true
                
                userAgentString = if (isDesktop(engine)) {
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                } else {
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                // Dark Mode - Use CSS injection for universal compatibility
                // Algorithmic darkening alone doesn't work on many websites
                android.util.Log.d("SearchAndTranslate", "Dark mode enabled: $isDarkMode")
            }

            // UI Tweaks
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            // Enable Third-Party Cookies
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            // Instrument fetch/XHR so the Lens results RPC (and its status) is
            // visible even when it fails silently. Injected at page start AND
            // finish (start can race the document commit); per-document guard.
            fun installNetDiag(view: WebView) {
                val netJs = """
                    (function() {
                        if (window.__netDiag) return 'already';
                        window.__netDiag = 1;
                        try {
                            var of = window.fetch;
                            window.fetch = function(u, init) {
                                var m = (init && init.method) || 'GET';
                                var url = (typeof u === 'string') ? u : ((u && u.url) || '');
                                console.warn('NET fetch ' + m + ' ' + url.slice(0, 200));
                                return of.apply(this, arguments).then(function(r) {
                                    console.warn('NET fetchdone ' + r.status + ' ' + m + ' ' + url.slice(0, 200));
                                    if (/Validate|qfmetadata|sgasync|httpservice|batchexecute|folsrch|bgasy|\/search\?/.test(url)) {
                                        try {
                                            console.warn('NETBODY_CLONE ' + url.split('?')[0].slice(-70));
                                            r.clone().text().then(function(t) {
                                                var i = t.search(/not associated with your account|No image at the URL|Image not found|Something went wrong/);
                                                if (i >= 0) {
                                                    console.warn('NETBODY_HIT @' + i + '/' + t.length + ' >> ' + t.slice(Math.max(0, i - 150), i + 250));
                                                } else {
                                                    console.warn('NETBODY ' + url.split('?')[0].slice(-70) + ' len=' + t.length + ' head=' + t.slice(0, 200));
                                                }
                                            }).catch(function(e) { console.warn('NETBODY_ERR ' + e); });
                                        } catch (e) { console.warn('NETBODY_ERR ' + e); }
                                    }
                                    return r;
                                }, function(e) {
                                    console.warn('NET fetcherr ' + m + ' ' + url.slice(0, 200) + ' ' + e);
                                    throw e;
                                });
                            };
                            var ox = XMLHttpRequest.prototype.open;
                            var os = XMLHttpRequest.prototype.send;
                            XMLHttpRequest.prototype.open = function(m, u) { this.__dm = m; this.__du = u; return ox.apply(this, arguments); };
                            XMLHttpRequest.prototype.send = function() {
                                var s = this, m = this.__dm, u = this.__du;
                                console.warn('NET xhr ' + m + ' ' + String(u).slice(0, 200));
                                s.addEventListener('loadend', function() {
                                    console.warn('NET xhrdone ' + s.status + ' ' + m + ' ' + String(u).slice(0, 200));
                                    try {
                                        if (/Validate|qfmetadata|sgasync|httpservice|batchexecute|folsrch|bgasy/.test(String(u)) && s.responseText) {
                                            console.warn('NETBODY_XHR ' + String(u).split('?')[0].slice(-70) + ' >> ' + String(s.responseText).slice(0, 400));
                                        }
                                    } catch (e) {}
                                });
                                return os.apply(this, arguments);
                            };
                            return 'installed';
                        } catch (e) { return 'NETDIAG_ERR ' + e; }
                    })()
                """.trimIndent()
                view.evaluateJavascript(netJs) { r ->
                    if (r != null && r != "\"already\"") {
                        android.util.Log.d("LensDiag", "NETDIAG $r")
                    }
                }
            }
            
            webViewClient = object : WebViewClient() {
                
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (url != null && url.contains("google.") && view != null) {
                        android.util.Log.d("LensDiag", "NAV $url")
                        installNetDiag(view)
                    }
                }
                
                override fun onReceivedHttpError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    response: android.webkit.WebResourceResponse?
                ) {
                    val host = request?.url?.host ?: ""
                    if (response != null && response.statusCode in 400..599 &&
                        (host.contains("google") || host.contains("imgdb") || host.contains("catbox"))
                    ) {
                        android.util.Log.e("LensDiag", "HTTP ${response.statusCode} ${response.reasonPhrase} ${request?.url}")
                    }
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    samplePageBackgroundColor(view)
                    clearRefreshIfDone(view)
                    // Lens loads its result tabs via async RPCs after page finish.
                    // Snapshot the network + DOM state so silent failures are visible.
                    if (url != null && url.contains("google.") && view != null) {
                        val v = view
                        installNetDiag(v)
                        v.postDelayed({
                            try {
                                val resJs = """
                                    (function() {
                                        try {
                                            var noise = ['xjs', '/log', 'gen_204', 'client_204', 'folsrch', 'sprites', '.css', '.woff', '.ttf'];
                                            var r = performance.getEntriesByType('resource')
                                                .filter(function(e) {
                                                    var n = e.name;
                                                    var host = n.indexOf('google') >= 0 || n.indexOf('gstatic') >= 0;
                                                    var bad = noise.some(function(s) { return n.indexOf(s) >= 0; });
                                                    return host && !bad;
                                                })
                                                .slice(-40)
                                                .map(function(e) { return (e.responseStatus || '?') + ' ' + e.name.split('?')[0].slice(0, 110); })
                                                .join(String.fromCharCode(10));
                                            return r || 'NO_GOOGLE_RESOURCES';
                                        } catch (err) { return 'SNAP_ERR ' + err; }
                                    })()
                                """.trimIndent()
                                v.evaluateJavascript(resJs) { r ->
                                    android.util.Log.d("LensDiag", "GOOGLE_RES $r")
                                }
                                val envJs = """
                                    (function() {
                                        try {
                                            var d = {
                                                ua: (navigator.userAgent || '').slice(0, 110),
                                                brands: navigator.userAgentData ? navigator.userAgentData.brands : 'NO_UADATA',
                                                mobile: navigator.userAgentData ? navigator.userAgentData.mobile : '?',
                                                platform: navigator.userAgentData ? navigator.userAgentData.platform : '?',
                                                chrome: typeof window.chrome === 'undefined' ? 'UNDEFINED' : Object.keys(window.chrome).join(','),
                                                webdriver: navigator.webdriver,
                                                cookies: (document.cookie || '').slice(0, 300)
                                            };
                                            return JSON.stringify(d);
                                        } catch (err) { return 'ENV_ERR ' + err; }
                                    })()
                                """.trimIndent()
                                v.evaluateJavascript(envJs) { r ->
                                    android.util.Log.d("LensDiag", "GOOGLE_ENV $r")
                                }
                                val bodyJs = """
                                    (function() {
                                        try {
                                            var txt = (document.body.innerText || '').replace(/\s+/g, ' ').slice(0, 450);
                                            return location.href.slice(0, 250) + ' >> ' + document.title + ' >> ' + txt;
                                        } catch (err) { return 'SNAP_ERR ' + err; }
                                    })()
                                """.trimIndent()
                                v.postDelayed({
                                    try {
                                        v.evaluateJavascript(bodyJs) { r ->
                                            android.util.Log.d("LensDiag", "GOOGLE_BODY $r")
                                        }
                                        android.util.Log.d("LensDiag", "GOOGLE_COOKIES ${android.webkit.CookieManager.getInstance().getCookie("https://lens.google.com")}")
                                    } catch (e: Exception) { android.util.Log.e("LensDiag", "snap failed", e) }
                                }, 3000)
                            } catch (e: Exception) {
                                android.util.Log.e("LensDiag", "snapshot failed", e)
                            }
                        }, 4000)
                    }
                    // Inject dark mode CSS if enabled
                    if (isDarkMode) {
                        val darkModeCSS = """
                            (function() {
                                var style = document.createElement('style');
                                style.innerHTML = `
                                    html { filter: invert(1) hue-rotate(180deg) !important; background: #000 !important; }
                                    img, video, [style*="background-image"] { filter: invert(1) hue-rotate(180deg) !important; }
                                `;
                                document.head.appendChild(style);
                            })()
                        """.trimIndent()
                        view?.evaluateJavascript(darkModeCSS, null)
                        android.util.Log.d("SearchAndTranslate", "Dark mode CSS injected for: $url")
                    }
                }
            }
            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    val msg = consoleMessage?.message() ?: return super.onConsoleMessage(consoleMessage)
                    val isNet = msg.contains("NET ") || msg.contains("NETBODY")
                    val isGoogleSrc = consoleMessage.sourceId()?.contains("google") == true
                    when {
                        consoleMessage.messageLevel() == android.webkit.ConsoleMessage.MessageLevel.ERROR ->
                            android.util.Log.e("LensDiag", "JS $msg @${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}")
                        consoleMessage.messageLevel() == android.webkit.ConsoleMessage.MessageLevel.WARNING && (isNet || isGoogleSrc) ->
                            android.util.Log.w("LensDiag", "JSW $msg")
                    }
                    return super.onConsoleMessage(consoleMessage)
                }
            }
            isNestedScrollingEnabled = true
            setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }
        }
    }

    // Closes the results completely and brings a circle from before the search back.
    fun closeResults() {
        scope.launch { scaffoldState.bottomSheetState.hide() }
        // Closing the results also ends the selection, so its Share/Save/Pin
        // toolbar doesn't linger over the bottom controls.
        selectionRect = null
        selectedBitmap = null
        isSearching = false
        scope.launch { selectionAnim.snapTo(0f) }
        restoreCircle()
    }

    // Leaving the overlay (Home, recents, another app) closes an open language picker,
    // whose window would otherwise stay on screen.
    val pickerLifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(pickerLifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                SearchAndTranslateAccessibilityService.instance?.dismissTranslatePicker()
                com.fuzzyfly0.searchandtranslate.AssistantTranslateOverlay.dismissPicker()
            }
        }
        pickerLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { pickerLifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Back Handler Logic
    BackHandler(enabled = true) {
        if (isTranslatorMode) {
            // An open language picker closes first; the translation stays.
            if (SearchAndTranslateAccessibilityService.instance?.dismissTranslatePicker() == true ||
                com.fuzzyfly0.searchandtranslate.AssistantTranslateOverlay.dismissPicker()
            ) return@BackHandler
            val service = SearchAndTranslateAccessibilityService.instance
            if (service != null) {
                service.closeTranslateOverlay()
            } else {
                com.fuzzyfly0.searchandtranslate.AssistantTranslateOverlay.close()
            }
            return@BackHandler
        }
        val currentWebView = webViews[selectedEngine]
        val resultsVisible = scaffoldState.bottomSheetState.currentValue != androidx.compose.material3.SheetValue.Hidden
        if (resultsVisible && currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack()
        } else if (resultsVisible) {
            // One Back closes the results completely (expanded or peeking).
            closeResults()
        } else if (selectionRect != null || pendingChoiceRegion != null || currentPathPoints.isNotEmpty()) {
            // A circled area (or the Search | Translate chooser) without results:
            // Back drops the selection instead of closing the whole overlay.
            selectionRect = null
            selectedBitmap = null
            pendingChoiceRegion = null
            isSearching = false
            currentPathPoints.clear()
            scope.launch { selectionAnim.snapTo(0f) }
        } else if (wordSelected) {
            wordSelectionClearSignal++
        } else {
            onClose()
        }
    }

    // Predictive back arrow: the system animates the back gesture (edge arrow + progress)
    // only when a prioritized callback receives it. In translator mode back is consumed
    // with no window transition, so a predictive handler keeps the arrow visible — and
    // the overlay windows (drawn above the arrow) fade out while the gesture runs.
    // Only the accessibility path draws its chrome in separate windows it can fade, so
    // assistant mode (activity-hosted chrome) falls back to the plain BackHandler above.
    PredictiveBackHandler(enabled = isTranslatorMode && SearchAndTranslateAccessibilityService.instance != null) { progress ->
        val service = SearchAndTranslateAccessibilityService.instance
        // Back with the language picker open only closes the picker.
        val pickerOpen = service?.isTranslatePickerShowing() == true
        try {
            progress.collect { event ->
                if (!pickerOpen) {
                    val fade = (1f - event.progress * 6f).coerceIn(0f, 1f)
                    service?.setTranslatorGestureAlpha(fade)
                }
            }
            if (pickerOpen) {
                service?.dismissTranslatePicker()
                return@PredictiveBackHandler
            }
            service?.closeTranslateOverlay()
        } catch (_: java.util.concurrent.CancellationException) {
            // Gesture cancelled — restore the overlay windows.
            service?.setTranslatorGestureAlpha(1f)
        }
    }

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        androidx.compose.material3.BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * 0.55f), // Dynamic 55% peek
            containerColor = Color.Transparent,
            sheetContainerColor = Color.Transparent,
            sheetContentColor = MaterialTheme.colorScheme.onSurface,
            sheetDragHandle = null,
            sheetSwipeEnabled = true,
            sheetContent = {
                // Bottom Sheet Content (Results)
                // Background tracks the loaded page's own background color so the
                // tab strip matches the webpage instead of Material surface colors.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(800.dp)
                        .background(pageBackgroundColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header: engine tabs + Share / Save for the searched area, so they stay
                // reachable while the sheet covers the selection toolbar.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Engine picker in the same flat style as the Share / Save buttons: compact
                // text buttons (no chips, no 90dp minimum tab width), so all engines fit
                // without scrolling; the selected one is bold in the primary color.
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    searchEngines.forEach { engine ->
                        val selected = selectedEngine == engine
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { selectedEngine = engine }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                engine.getDisplayName(context),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (selectedBitmap != null) {
                    IconButton(onClick = { saveSelection() }) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = stringResource(R.string.save),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { shareSelection() }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.label_share),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Close the results (same as Back)
                IconButton(onClick = { closeResults() }, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                }

                // Reset everything when bitmap changes (new area selected)
                LaunchedEffect(selectedBitmap) {
                    hostedImageUrl = null
                    refreshingEngine = null
                    searchUrl = null
                    preloadedUrls.clear()
                    initializedEngines.clear() // Reset smart loading
                    // Do NOT destroy webviews here to keep them cached if possible? 
                    // PROBABLY safer to destroy to avoid stale state from previous searches.
                    webViews.values.forEach { 
                        it.stopLoading()
                        it.clearHistory()
                        it.clearCache(true)
                        it.loadUrl("about:blank")
                        it.destroy() 
                    }
                    webViews.clear()
                }

                LaunchedEffect(selectedBitmap, hostedImageUrl) {
                    if (selectedBitmap != null) {
                        isLoading = true
                        
                        // Multi-Search (Expanded UI)
                        scope.launch { scaffoldState.bottomSheetState.expand() }

                        // Upload to host if needed
                        if (hostedImageUrl == null) {
                            val url = ImageSearchUploader.uploadToImageHost(selectedBitmap!!)
                            if (url != null) {
                                hostedImageUrl = url
                            } else {
                                isLoading = false
                                return@LaunchedEffect
                            }
                        }

                        // 2. Generate URLs for ALL engines (lightweight string op)
                        searchEngines.forEach { engine ->
                            if (!preloadedUrls.containsKey(engine)) {
                                val url = when (engine) {
                                    SearchEngine.Google -> ImageSearchUploader.getGoogleLensUrl(hostedImageUrl!!)
                                    SearchEngine.Bing -> ImageSearchUploader.getBingUrl(hostedImageUrl!!)
                                    SearchEngine.TinEye -> ImageSearchUploader.getTinEyeUrl(hostedImageUrl!!)
                                }
                                if (url != null) preloadedUrls[engine] = url
                            }
                        }

                        // 3. Set initial URL
                        if (preloadedUrls.containsKey(selectedEngine)) {
                             searchUrl = preloadedUrls[selectedEngine]
                        }
                        
                        // 4. SMART LOADING SEQUENCE
                        // First, ensure selected engine is initialized
                        if (!initializedEngines.contains(selectedEngine)) {
                            initializedEngines.add(selectedEngine)
                        }
                        
                        isLoading = false
                        
                        // Then, load others sequentially
                        scope.launch {
                            searchEngines.forEach { engine ->
                                if (engine != selectedEngine) {
                                    delay(300) // Reduced from 800ms for faster loading
                                    if (!initializedEngines.contains(engine)) {
                                        initializedEngines.add(engine)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Update searchUrl when engine changes
                LaunchedEffect(selectedEngine, preloadedUrls) {
                    if (preloadedUrls.containsKey(selectedEngine)) {
                        searchUrl = preloadedUrls[selectedEngine]
                    }
                }

                // Memory Optimization: REMOVED Aggressive Cleanup
                // User Requirement: "dont refresh tabs when user switch tabs keep them in background"
                // We keep them alive.
                /* 
                LaunchedEffect(selectedEngine) {
                     // ... (Cleanup logic removed)
                }
                */

                PullToRefreshBox(
                    isRefreshing = refreshingEngine != null,
                    onRefresh = {
                        val wv = webViews[selectedEngine]
                        if (wv != null) {
                            refreshingEngine = selectedEngine
                            wv.reload()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Show loading only if the SELECTED engine isn't ready or just starting
                    if (isLoading || (preloadedUrls.containsKey(selectedEngine) && !webViews.containsKey(selectedEngine))) {
                         // We delay showing the loader slightly to avoid flicker if WebView attaches instantly
                        var showLoader by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(100)
                            showLoader = true
                        }
                        
                        if (showLoader || isLoading) {
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ContainedLoadingIndicatorSample()
                             }
                        }
                    }

                    // Dynamic Settings Update (User Agent etc) - Now handled at top level
                    // Cleanup on Dispose
                    DisposableEffect(Unit) {
                        onDispose {
                            webViews.values.forEach { 
                                it.stopLoading()
                                it.clearHistory()
                                it.clearCache(true)
                                it.loadUrl("about:blank")
                                it.destroy() 
                            }
                            webViews.clear()
                        }
                    }

                    // Render WebViews
                    searchEngines.forEach { engine ->
                         // Logic: Render if it's in the initialized set (Smart Loading)
                         // This ensures we don't load everything at once, but once loaded, we keep it.
                         if (initializedEngines.contains(engine) && preloadedUrls.containsKey(engine)) {
                             val url = preloadedUrls[engine]!!
                             val isSelected = (engine == selectedEngine)
                             
                             androidx.compose.runtime.key(engine) {
                                AndroidView(
                                    factory = { ctx ->
                                        if (webViews.containsKey(engine)) {
                                            // Should not happen with key(), but safety check
                                            val v = webViews[engine]!!
                                            (v.parent as? ViewGroup)?.removeView(v)
                                            v
                                        } else {
                                            val webView = createWebView(ctx, engine)
                                            // Apply current settings
                                             if (isDesktop(engine)) {
                                                 webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                             }
                                            
                                            webViews[engine] = webView
                                            webView.loadUrl(url)
                                            webView
                                        }
                                    },
                                    update = { webView ->
                                        if (webView.url != url && url != webView.originalUrl) {
                                            webView.loadUrl(url)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .zIndex(if (isSelected) 1f else 0f)
                                        .graphicsLayer { 
                                            alpha = if (isSelected) 1f else 0f 
                                        }
                                )
                             }
                        }
                    }
                }
            }
        }
    ) { _ ->
        // Root Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) // Changed from Black to Transparent
        ) {
            // Close button for Copy Mode (Top Left)

            // 1. Screenshot Layer
            if (screenshot != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Letterbox bars behind the Fit image are solid black, not see-through.
                        .background(Color.Black)
                        // View size for the image-fit mapping (crop-to-search, chips).
                        .onSizeChanged { canvasSizePx = it }
                ) {
                    Image(
                        bitmap = screenshot.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_screenshot),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )


                    // Tint Overlay (Punch-out style) — dimmed during normal CTS use,
                    // hidden in translator mode so the frozen backdrop stays undimmed.
                    // The Canvas renders to its OWN offscreen buffer: BlendMode.Clear then
                    // erases only the tint (the hole), while the image underneath — drawn in
                    // the parent, a separate buffer — stays intact and shows through the hole.
                    // (Offscreen on the parent Box would share one buffer: Clear would erase
                    // the image too and the hole would punch through to the black window.)
                    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
                        alpha = if (isTranslatorMode) 0f else 1f
                        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                    }) {
                        val dimAlpha = 0.08f
                        
                        // 1. Draw global dim
                        drawRect(Color.Black.copy(alpha = dimAlpha))
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = OverlayGradientColors.map { it.copy(alpha = dimAlpha) }
                            )
                        )
                        
                        // 2. Clear selection area if it exists
                        if (selectionRect != null && selectionAnim.value > 0f) {
                            val rect = selectionRect!!
                            val progress = selectionAnim.value
                            val holeRect = androidx.compose.ui.geometry.Rect(
                                rect.left.toFloat(), 
                                rect.top.toFloat(), 
                                rect.right.toFloat(), 
                                rect.bottom.toFloat()
                            )
                            
                            // Punch the hole
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = holeRect.topLeft,
                                size = holeRect.size,
                                cornerRadius = CornerRadius(min(48f, min(holeRect.width, holeRect.height) / 2f)),
                                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                            )
                        }
                    }
                }
            }

            // 2. Gradient Border Layer (Overlaying screenshot, clipped to rounded corners)
            // Sibling drawn AFTER the dim/tint layer, so overlay dimness can never
            // wash the border out — it stays at its own alpha regardless of dimAlpha.
            if (!isTranslatorMode) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isUIVisible,
                    enter = androidx.compose.animation.fadeIn(animationSpec = tween(700))
                ) {
                    // Border drawn as the inner half of a stroke along the screen outline,
                    // clipped to that outline: every part - corners included - shows the
                    // same width, instead of the physical corners cutting a border that
                    // sits on the edge.
                    var originInWindow by remember { mutableStateOf(Offset.Zero) }
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { originInWindow = it.positionInWindow() }
                    ) {
                        val outlinePath = displayOutlinePath?.let { screenPath ->
                            android.graphics.Path(screenPath).apply {
                                offset(-originInWindow.x, -originInWindow.y)
                            }.asComposePath()
                        } ?: Path().apply {
                            addOutline(deviceCornerShape.createOutline(size, layoutDirection, this@Canvas))
                        }
                        val brush = Brush.verticalGradient(
                            colors = OverlayGradientColors.map { it.copy(alpha = 0.9f) }
                        )
                        clipPath(outlinePath) {
                            drawPath(outlinePath, brush, style = Stroke(width = 3.dp.toPx() * 2))
                        }
                    }
                }
            }

            // 3. Drawing Canvas (Interactive Layer)
            if (!isTranslatorMode) {                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Draw current path (Real-time)
                    if (currentPathPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                            for (i in 1 until currentPathPoints.size) {
                                lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                            }
                        }
                        
                        // Glow
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(OverlayGradientColors),
                            style = Stroke(width = 30f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            alpha = 0.6f
                        )
                        // Core
                        drawPath(
                            path = path,
                            color = Color.White,
                            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // Draw Lens Animation (Rounded Corner Brackets)
                    if (selectionRect != null && selectionAnim.value > 0f) {
                        val rect = selectionRect!!
                        val progress = selectionAnim.value
                        val left = rect.left.toFloat()
                        val top = rect.top.toFloat()
                        val right = rect.right.toFloat()
                        val bottom = rect.bottom.toFloat()
                        
                        val width = right - left
                        val height = bottom - top
                        // Constant corner look for every frame size: fixed radius and fixed
                        // arm length (what a small square frame used to draw), clamped only
                        // so frames smaller than the radius/arm still render correctly.
                        val maxRadius = min(width, height) / 2f
                        val cornerRadius = min(48f, maxRadius)
                        val armLength = max(cornerRadius, min(64f, maxRadius))

                        // Top Left
                        val tlPath = Path().apply {
                            moveTo(left, top + armLength)
                            lineTo(left, top + cornerRadius)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left, top, left + 2 * cornerRadius, top + 2 * cornerRadius),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left + armLength, top)
                        }
                        // Top Right
                        val trPath = Path().apply {
                            moveTo(right - armLength, top)
                            lineTo(right - cornerRadius, top)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(right - 2 * cornerRadius, top, right, top + 2 * cornerRadius),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(right, top + armLength)
                        }
                        // Bottom Right
                        val brPath = Path().apply {
                            moveTo(right, bottom - armLength)
                            lineTo(right, bottom - cornerRadius)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(right - 2 * cornerRadius, bottom - 2 * cornerRadius, right, bottom),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(right - armLength, bottom)
                        }
                        // Bottom Left
                        val blPath = Path().apply {
                            moveTo(left + armLength, bottom)
                            lineTo(left + cornerRadius, bottom)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left, bottom - 2 * cornerRadius, left + 2 * cornerRadius, bottom),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left, bottom - armLength)
                        }

                        val bracketAlpha = progress
                        val bracketStroke = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        
                        // Draw Brackets (Solid White)
                        listOf(tlPath, trPath, brPath, blPath).forEach { p ->
                            drawPath(p, Color.White, style = bracketStroke, alpha = bracketAlpha)
                        }
                        
                        // Optional: Flash effect inside
                         drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(cornerRadius),
                            style = Stroke(width = 4f),
                            alpha = (1f - progress) * 0.5f
                         )
                    }
                }

            }

            // Circle-draw gestures + instant OCR overlays. The drag detector is the PARENT of
            // the word overlay, not a lower sibling: an ancestor is always in the hit path, the
            // child sees events first (taps and handle-drags stay with text selection, since it
            // consumes them), and plain drags fall through to circle drawing — the same
            // container/overlay split the translator module uses for pan vs. word selection.
            if (!isTranslatorMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { beginCircleDrag(it) },
                                onDrag = { change, _ -> moveCircleDrag(change) },
                                onDragEnd = { endCircleDrag() },
                            )
                        }
                ) {
                    if (screenshot != null) {
                        val words = instantWords
                        if (words != null) {
                            WordSelectionOverlay(
                                words = words.sourceWords,
                                imageWidth = words.imageWidth,
                                imageHeight = words.imageHeight,
                                modifier = Modifier.fillMaxSize(),
                                onSelectionChanged = { selected ->
                                    wordSelected = selected
                                    // Mutually exclusive with the drawn circle: selecting
                                    // text drops the frame (and its crop) so only one stays.
                                    if (selected && selectionRect != null) {
                                        selectionRect = null
                                        selectedBitmap = null
                                        pendingChoiceRegion = null
                                    }
                                },
                                clearSelectionSignal = wordSelectionClearSignal,
                            )
                        } else if (!instantOcrDone) {
                            instantRegions?.let { regions ->
                                ScanAnimationOverlay(
                                    regions = regions,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }

            // 3b. Search | Translate chooser next to the circled area ("Ask" circle action)
            val choiceRegion = pendingChoiceRegion
            val choiceRect = selectionRect
            val choiceBitmap = screenshot
            if (choiceRegion != null && choiceRect != null && choiceBitmap != null &&
                selectedBitmap == null && !isTranslatorMode
            ) {
                var chooserSize by remember { mutableStateOf(IntSize.Zero) }
                val gapPx = with(density) { 16.dp.roundToPx() }
                val marginPx = with(density) { 16.dp.roundToPx() }
                Row(
                    modifier = Modifier
                        .offset {
                            val rootW = if (canvasSizePx.width > 0) canvasSizePx.width else choiceBitmap.width
                            val rootH = if (canvasSizePx.height > 0) canvasSizePx.height else choiceBitmap.height
                            val w = chooserSize.width
                            val h = chooserSize.height
                            val x = (choiceRect.centerX() - w / 2)
                                .coerceIn(marginPx, maxOf(marginPx, rootW - w - marginPx))
                            // Prefer below the selection, but stay clear of the bottom bar
                            // (lower ~quarter of the screen); otherwise above; else inside.
                            val below = choiceRect.bottom + gapPx
                            val above = choiceRect.top - gapPx - h
                            val y = when {
                                below + h <= rootH * 3 / 4 -> below
                                above >= marginPx * 4 -> above
                                else -> (choiceRect.centerY() - h / 2)
                                    .coerceIn(marginPx, maxOf(marginPx, rootH - h - marginPx))
                            }
                            androidx.compose.ui.unit.IntOffset(x, y)
                        }
                        .onSizeChanged { chooserSize = it }
                        .zIndex(1500f)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close only: Search, Translate, Save and Share for the circled area run
                    // from the main pill.
                    // Close: drop the circle and keep the overlay open (same as Back).
                    IconButton(onClick = {
                        selectionRect = null
                        selectedBitmap = null
                        pendingChoiceRegion = null
                        isSearching = false
                        currentPathPoints.clear()
                        scope.launch { selectionAnim.snapTo(0f) }
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 4. Header (Top)
            androidx.compose.animation.AnimatedVisibility(
                visible = isUIVisible && !isTranslatorMode,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -it }, // Commence au-dessus de l'écran (-100%)
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ),
                modifier = Modifier.align(Alignment.TopCenter).zIndex(2000f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 4.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Engine name only while the results sheet is actually open (settled,
                    // not just starting to animate).
                    if (selectedBitmap != null &&
                        scaffoldState.bottomSheetState.currentValue != androidx.compose.material3.SheetValue.Hidden
                    ) {
                        Text(
                            text = selectedEngine.getDisplayName(context),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                }
            }

            // 5. Bottom Bar — Material 3 Expressive two-row card
            androidx.compose.animation.AnimatedVisibility(
                visible = isUIVisible && !isTranslatorMode && !wordSelected,
                enter = slideInVertically(
                    initialOffsetY = { it }, // slides up from below
                    animationSpec = tween(300, easing = androidx.compose.animation.core.CubicBezierEasing(0f, 0f, 0.2f, 1f))
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it }, // slides back down
                    animationSpec = tween(200, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0f, 1f, 1f))
                ) + fadeOut(animationSpec = tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(2000f)
            ) {
                // ── Outer card container ──────────────────────────────────────────
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp
                ) {
                    androidx.compose.foundation.layout.BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                    // Both rows share one 5-column grid; buttons are at most 52 dp and the gap
                    // between the rows matches the gap between two neighbouring buttons.
                    val cellWidth = maxWidth / 5
                    val buttonSize = (cellWidth - 8.dp).coerceIn(40.dp, 52.dp)
                    val buttonGap = (cellWidth - buttonSize).coerceAtLeast(4.dp)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(buttonGap)
                    ) {
                        // Labelled round button; both rows use it so sizes and labels match.
                        @Composable
                        fun androidx.compose.foundation.layout.RowScope.BottomBarButton(
                            label: String, 
                            icon: @Composable () -> Unit, 
                            enabled: Boolean = true,
                            // Framed: the button acts on the circled area, not the whole screen.
                            framed: Boolean = false,
                            onClick: () -> Unit
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    OverlayRoundButton(
                                        size = buttonSize,
                                        onClick = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            onClick()
                                        },
                                        enabled = enabled,
                                        framed = framed
                                    ) { icon() }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = label, 
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp, 
                                            lineHeight = 12.sp,
                                            letterSpacing = (-0.4).sp,
                                            fontWeight = FontWeight.Medium
                                        ), 
                                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f), 
                                        textAlign = TextAlign.Center, 
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible,
                                        modifier = Modifier.padding(horizontal = 0.dp)
                                    )
                                }
                            }
                        }

                        // A circled area waiting for an action: Search / Translate act on it
                        // (framed), and Save / Share appear next to Close.
                        val circleRegion = pendingChoiceRegion
                        val circleShown = circleRegion != null && selectionRect != null && selectedBitmap == null

                        // ── ROW 1: Labelled action buttons ──────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp), // Maximize space for labels
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Translator: type text to translate (its settings live in the app settings).
                            // Own document task like Live cam: back returns here, and the app
                            // icon still opens the settings, not the translator.
                            BottomBarButton(stringResource(R.string.label_translator_title), { Icon(Icons.Default.Keyboard, null) }) {
                                SearchAndTranslateAccessibilityService.instance?.closeTranslateOverlay()
                                val translatorIntent = android.content.Intent(context, dev.davidv.translator.MainActivity::class.java)
                                    .putExtra(dev.davidv.translator.MainActivity.EXTRA_OVERLAY_LANGUAGES, true)
                                    .addFlags(
                                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                            android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                            android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                    )
                                runCatching { context.startActivity(translatorIntent) }.onFailure {
                                    android.widget.Toast.makeText(context, context.getString(R.string.toast_translate_unavailable), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }

                            // Camera — open the translator module's live camera directly.
                            // Own document task (not the app's launcher task): back returns here,
                            // and the app icon still opens the settings, not the camera.
                            BottomBarButton(stringResource(R.string.label_camera), {
                                Icon(com.fuzzyfly0.searchandtranslate.ui.AppIcons.LiveCam, null)
                            }) {
                                // Clear any lingering translator overlay windows so a
                                // stale touchable result/select window can't intercept
                                // taps inside the camera screen.
                                SearchAndTranslateAccessibilityService.instance?.closeTranslateOverlay()
                                val cameraIntent = android.content.Intent(context, dev.davidv.translator.MainActivity::class.java)
                                    .putExtra(dev.davidv.translator.MainActivity.EXTRA_OPEN_LIVE_CAMERA, true)
                                    .addFlags(
                                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                            android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                            android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                    )
                                runCatching {
                                    context.startActivity(cameraIntent)
                                }.onFailure {
                                    android.widget.Toast.makeText(context, context.getString(R.string.toast_translate_unavailable), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }

                            // Pin
//                            val isPinEnabled = selectedBitmap != null
//                            BottomBarButton(
//                                label = "Pin",
//                                icon = { Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(22.dp)) },
//                                enabled = isPinEnabled
//                            ) {
//                                selectedBitmap?.let { bmp ->
//                                    SearchAndTranslateAccessibilityService.pinArea(bmp, selectionRect ?: android.graphics.Rect())
//                                    (context as? android.app.Activity)?.finish()
//                                }
//                            }



                            // Translate — freeze the overlay (chrome hides, screenshot stays) and run the
                            //   module's translator overlay on the capture; close/back returns here.
                            // With a circle on screen: "Translate", framed, and it translates the circle.
                            BottomBarButton(
                                stringResource(if (circleShown) R.string.label_translate else R.string.label_translate_all),
                                { Icon(painterResource(id = com.fuzzyfly0.searchandtranslate.R.drawable.ic_translate_a), null) },
                                framed = circleShown,
                            ) {
                                val shot = screenshot
                                if (circleShown && circleRegion != null && shot != null) {
                                    startLassoTranslate(shot, circleRegion)
                                    return@BottomBarButton
                                }
                                val bmp = screenshot
                                if (bmp == null) {
                                    android.widget.Toast.makeText(context, context.getString(R.string.toast_translate_unavailable), android.widget.Toast.LENGTH_SHORT).show()
                                    return@BottomBarButton
                                }
                                if (pendingChoiceRegion != null) saveCircle(selectionRect, pendingChoiceRegion)
                                pendingChoiceRegion = null
                                selectionRect = null
                                selectedBitmap = null
                                isSearching = false
                                currentPathPoints.clear()
                                scope.launch { selectionAnim.snapTo(0f) }

                                val translatorService = SearchAndTranslateAccessibilityService.instance
                                if (translatorService != null) {
                                    isTranslatorMode = true
                                    // OCR starts now; only the overlay chrome waits for the hide delay.
                                    val started = translatorService.translateCapturedScreen(bmp, TRANSLATE_HIDE_DELAY_MS, onClosed = {
                                        isTranslatorMode = false
                                        restoreCircle()
                                    })
                                    if (started) return@BottomBarButton
                                    isTranslatorMode = false
                                }
                                // No accessibility service (or its bridge failed):
                                // show the module's translate overlay hosted in our
                                // own activity — same OverlayUI chrome as the a11y path.
                                isTranslatorMode = true
                                val startedTranslate = com.fuzzyfly0.searchandtranslate.AssistantTranslateOverlay.start(bmp, TRANSLATE_HIDE_DELAY_MS) {
                                    isTranslatorMode = false
                                    restoreCircle()
                                }
                                if (!startedTranslate) {
                                    isTranslatorMode = false
                                    android.widget.Toast.makeText(context, context.getString(R.string.toast_translate_unavailable), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }

                            // Fullscreen
                            // With a circle on screen: "Search", framed, and it searches the circle.
                            BottomBarButton(
                                stringResource(if (circleShown) R.string.label_search else R.string.label_fullscreen),
                                { Icon(com.fuzzyfly0.searchandtranslate.ui.AppIcons.Search, null) },
                                framed = circleShown,
                            ) {
                                val shot = screenshot
                                if (circleShown && circleRegion != null && shot != null) {
                                    saveCircle(selectionRect, circleRegion)
                                    pendingChoiceRegion = null
                                    selectedBitmap = ImageUtils.cropBitmap(shot, circleRegion)
                                    isSearching = true
                                    return@BottomBarButton
                                }
                                if (screenshot != null) {
                                    if (pendingChoiceRegion != null) saveCircle(selectionRect, pendingChoiceRegion)
                                    // Full-image selection in screen space (view size, not bitmap size).
                                    val viewW = if (canvasSizePx.width > 0) canvasSizePx.width else screenshot.width
                                    val viewH = if (canvasSizePx.height > 0) canvasSizePx.height else screenshot.height
                                    // Mark the area as searched before it appears: the
                                    // Share/Save/Pin toolbar only shows for unsearched areas,
                                    // and a previous selection's finished animation (value 1)
                                    // would otherwise flash it up for a frame.
                                    selectedBitmap = screenshot
                                    isSearching = true
                                    selectionRect = Rect(0, 0, viewW, viewH)
                                    pendingChoiceRegion = null
                                    currentPathPoints.clear()
                                    scope.launch {
                                        selectionAnim.snapTo(0f)
                                        selectionAnim.animateTo(1f, tween(600))
                                    }
                                }
                            }

                            if (uploadedImage != null) {
                                // Screen — drop the gallery image and show the captured screen again
                                BottomBarButton(stringResource(R.string.label_screen), { Icon(Icons.Default.Screenshot, null) }) {
                                    uploadedImage = null
                                    selectionRect = null
                                    selectedBitmap = null
                                    pendingChoiceRegion = null
                                    isSearching = false
                                    currentPathPoints.clear()
                                    scope.launch { selectionAnim.snapTo(0f) }
                                }
                            } else {
                                // Gallery — pick an image and run SearchAndTranslate on it
                                BottomBarButton(stringResource(R.string.label_gallery), { Icon(painterResource(id = com.fuzzyfly0.searchandtranslate.R.drawable.ic_gallery), null, modifier = Modifier.size(22.dp)) }) {
                                    pickImageLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                        }

                        // ── ROW 2: Logo + labelled buttons (Settings … Lens, Translator, Close) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // App logo, same size as the round buttons, no background
                            Image(
                                painter = painterResource(id = com.fuzzyfly0.searchandtranslate.R.drawable.ic_logo),
                                contentDescription = stringResource(R.string.cd_logo),
                                modifier = Modifier.weight(1f).wrapContentWidth().size(buttonSize)
                            )

                            // Settings — opens the app's settings overview (home screen)
                            BottomBarButton(stringResource(R.string.settings), { Icon(Icons.Default.Settings, null) }) {
                                val settingsIntent = android.content.Intent(context, com.fuzzyfly0.searchandtranslate.MainActivity::class.java)
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                // The overlay stays open underneath, so back from settings returns to it.
                                runCatching { context.startActivity(settingsIntent) }
                            }

                            // Logo and settings left, Close right; with a circle Save and Share
                            // take the gap, left of Close, framed like Search / Translate.
                            if (circleShown) {
                                BottomBarButton(stringResource(R.string.save), { Icon(Icons.Default.Download, null) }, framed = true) {
                                    saveSelection()
                                }
                                BottomBarButton(stringResource(R.string.label_share), { Icon(Icons.Default.Share, null) }, framed = true) {
                                    shareSelection()
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(2f))
                            }

                            // Close
                            BottomBarButton(stringResource(R.string.cd_close), { Icon(Icons.Default.Close, null) }) {
                                onClose()
                            }
                        }
                    }
                    }
                }
            }




        // Input shield while the translator overlay owns the screen: consumes touches so
        // the hidden chrome underneath can't be tapped (module windows sit above this).
        if (isTranslatorMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(4000f)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
            )
        }

    }
}
}
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@androidx.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
fun ContainedLoadingIndicatorSample() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.ContainedLoadingIndicator()
    }
}

/** Flat overlay icon button (circular ripple, no fill) like the circle chooser pill; every button in both rows uses it. */
@Composable
private fun OverlayRoundButton(
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    framed: Boolean = false,
    content: @Composable () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface.let { if (enabled) it else it.copy(alpha = 0.38f) }
    val frameColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .size(size)
            // Framed: the same rounded corner brackets as around a circled area
            // (drawn before the round clip so the corners stay visible).
            .then(
                if (framed) Modifier.drawBehind {
                    val stroke = 2.dp.toPx()
                    // 75 % of the button size, centred around the icon.
                    val frame = this.size.minDimension * 0.75f
                    val l = (this.size.width - frame) / 2f
                    val t = (this.size.height - frame) / 2f
                    val r = l + frame
                    val b = t + frame
                    val radius = frame * 0.22f
                    val arm = frame * 0.34f
                    fun corner(x0: Float, y0: Float, sx: Float, sy: Float) = Path().apply {
                        // x0/y0: the corner; sx/sy: direction towards the button centre
                        moveTo(x0, y0 + sy * arm)
                        lineTo(x0, y0 + sy * radius)
                        quadraticBezierTo(x0, y0, x0 + sx * radius, y0)
                        lineTo(x0 + sx * arm, y0)
                    }
                    listOf(
                        corner(l, t, 1f, 1f),
                        corner(r, t, -1f, 1f),
                        corner(r, b, -1f, -1f),
                        corner(l, b, 1f, -1f),
                    ).forEach {
                        drawPath(it, frameColor, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                } else Modifier
            )
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor,
            content = content
        )
    }
}
