package com.fuzzyfly0.searchandtranslate.ui.wobblepulldown

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fuzzyfly0.searchandtranslate.MainActivity
import com.fuzzyfly0.searchandtranslate.R
import com.fuzzyfly0.searchandtranslate.data.ActionType
import com.fuzzyfly0.searchandtranslate.data.GestureType
import com.fuzzyfly0.searchandtranslate.data.OverlayConfigurationManager
import com.fuzzyfly0.searchandtranslate.data.StatusBarConfig
import com.fuzzyfly0.searchandtranslate.ui.SettingsSectionHeader
import com.fuzzyfly0.searchandtranslate.utils.UIPreferences
import kotlinx.coroutines.delay

private const val BOTTOM_EDGE_MARKER = "cts_bottom_edge"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun WobbleHomeScreen(
    onOpenDeviceInfo: () -> Unit,
    modifier: Modifier = Modifier,
    wobbleEvent: Int = 0,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onOpenSettings: ((String) -> Unit)? = null,
    // Opens straight on the Launch methods page (e.g. nothing could capture the screen).
    openLaunchMethods: Boolean = false,
    onLaunchMethodsOpened: () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState =
        androidx.compose.foundation.lazy.rememberLazyListState(),
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            WobbleHaptics.performUIHaptic(view)
            onOpenDeviceInfo()
            isRefreshing = false
        }
    }

    var rippleTrigger by remember { mutableIntStateOf(0) }
    var textCenterOffset by remember { mutableStateOf(Offset.Zero) }
    var lastHapticBucket by remember { mutableStateOf(0) }

    LaunchedEffect(pullRefreshState.distanceFraction) {
        val fraction = pullRefreshState.distanceFraction
        val currentBucket = (fraction * 10).toInt()

        if (fraction >= 1f && lastHapticBucket < 10) {
            WobbleHaptics.performUIHaptic(view)
            rippleTrigger++
            lastHapticBucket = 10
        } else if (fraction < 1f && currentBucket != lastHapticBucket) {
            if (currentBucket > lastHapticBucket) {
                WobbleHaptics.performSliderHaptic(view)
            }
            lastHapticBucket = currentBucket
        }

        if (fraction == 0f) {
            lastHapticBucket = 0
        }
    }

    val appPrefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    val configManager = remember { OverlayConfigurationManager(context) }
    val uiPreferences = remember { UIPreferences(context) }
    var a11yGranted by remember { mutableStateOf(isA11yGranted(context)) }
    var assistantGranted by remember { mutableStateOf(isAssistantGranted(context)) }
    var statusBarCfg by remember { mutableStateOf(configManager.getStatusBarConfig()) }
    // Launch method sections start collapsed (expanding shows their overlay preview).
    var statusBarExpanded by remember { mutableStateOf(false) }
    var customOverlayExpanded by remember { mutableStateOf(false) }
    var showDisclosure by remember { mutableStateOf(false) }
    var showAssistantSheet by remember { mutableStateOf(false) }
    val running = assistantGranted || (a11yGranted && statusBarCfg.isEnabled)

    val refreshHomeState = {
        a11yGranted = isA11yGranted(context)
        assistantGranted = isAssistantGranted(context)
        statusBarCfg = configManager.getStatusBarConfig()
    }

    val settingsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
    ) {
        refreshHomeState()
    }

    if (showDisclosure) {
        com.fuzzyfly0.searchandtranslate.ui.components.AccessibilityDisclosureDialog(
            onAccept = {
                showDisclosure = false
                runCatching { settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            },
            onDismiss = { showDisclosure = false },
        )
    }

    if (showAssistantSheet) {
        AssistantSetupSheet(
            onLaunch = { intent -> settingsLauncher.launch(intent) },
            onDismiss = { showAssistantSheet = false },
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            refreshHomeState()
            if (event == Lifecycle.Event.ON_STOP) {
                // Switching away from the app closes both settings dropdowns, which
                // also drops their on-screen overlay previews.
                statusBarExpanded = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            refreshHomeState()
            delay(500)
        }
    }

    val openSettingsPage: (String) -> Unit = { page ->
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_SETTINGS_PAGE, page)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            context,
            R.anim.anim_slide_in_top,
            R.anim.anim_stay,
        )
        context.startActivity(intent, options.toBundle())
    }

    val saveStatusBar: (StatusBarConfig) -> Unit = { cfg ->
        configManager.saveStatusBarConfig(cfg)
        statusBarCfg = cfg
    }

    // Strip any legacy full-width bottom strip that older builds stored as a segment.
    LaunchedEffect(Unit) {
        val cfg = configManager.getConfig()
        if (cfg.segments.any { it.gestureData[GestureType.LONG_PRESS] == BOTTOM_EDGE_MARKER }) {
            configManager.saveConfig(
                cfg.copy(segments = cfg.segments.filterNot {
                    it.gestureData[GestureType.LONG_PRESS] == BOTTOM_EDGE_MARKER
                }),
            )
        }
    }

    // Clear preview when leaving the screen so overlays don't stay visible.
    DisposableEffect(Unit) {
        onDispose {
            val status = configManager.getStatusBarConfig()
            if (status.previewVisible) configManager.saveStatusBarConfig(status.copy(previewVisible = false))
        }
    }

    LaunchedEffect(wobbleEvent) {
        if (wobbleEvent > 0) {
            WobbleHaptics.performUIHaptic(view)
            rippleTrigger++
        }
    }

    // No pull-down gesture any more: the status screen is a regular settings
    // category ("Status & permissions" under More).
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .liquidRipple(
                trigger = rippleTrigger,
                origin = textCenterOffset,
                enabled = true,
                durationMillis = 2800,
                amplitudeDp = 34f,
                frequency = 12f,
                decay = 4.5f,
                speedDp = 1400f,
            ),
    ) {
        val deviceInfo = remember { WobbleDeviceUtils.getDeviceInfo(context) }
        val displayFraction = if (isRefreshing) 1f else pullRefreshState.distanceFraction
        val thresholdPassed = displayFraction >= 1f
        val statusBarPadding = contentPadding.calculateTopPadding()

        val cardExpansion by animateDpAsState(
            targetValue = 120.dp * displayFraction.coerceIn(0f, 1f),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "cardExpansion",
        )

        val containerColor by animateColorAsState(
            targetValue = when {
                thresholdPassed -> MaterialTheme.colorScheme.primary
                running -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            },
            animationSpec = tween(durationMillis = 300),
            label = "containerColor",
        )

        val contentColor by animateColorAsState(
            targetValue = when {
                thresholdPassed -> MaterialTheme.colorScheme.onPrimary
                running -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.primary
            },
            animationSpec = tween(durationMillis = 300),
            label = "contentColor",
        )

        val borderColor by animateColorAsState(
            targetValue = if (thresholdPassed || running) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
            animationSpec = tween(durationMillis = 300),
            label = "borderColor",
        )

        val chevronAlpha by animateFloatAsState(
            targetValue = if (thresholdPassed) 0f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "chevronAlpha",
        )

        val chevronWidth by animateDpAsState(
            targetValue = if (thresholdPassed) 0.dp else 24.dp + 8.dp,
            animationSpec = tween(durationMillis = 300),
            label = "chevronWidth",
        )

        val fontWeight by animateIntAsState(
            targetValue = if (thresholdPassed) 700 else 500,
            animationSpec = tween(durationMillis = 100),
            label = "fontWeight",
        )

        val textScale by animateFloatAsState(
            targetValue = if (thresholdPassed) 1.5f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "textScale",
        )

        val fontWidth = remember(displayFraction) {
            (100f + (displayFraction.coerceIn(0f, 1f) * 50f))
        }

        val fontFamily = FontFamily.Default

        val heroStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily)
        val heroColor by animateColorAsState(
            targetValue = when {
                thresholdPassed -> MaterialTheme.colorScheme.onPrimary
                running -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
            animationSpec = tween(durationMillis = 300),
            label = "heroColor",
        )

        // One page like Circle to Share: Setup, How to open it, Search, Translator, About.
        // Setup starts open while nothing is set up (first start) or when asked for.
        var setupExpanded by androidx.compose.runtime.saveable.rememberSaveable {
            mutableStateOf(!a11yGranted && !assistantGranted)
        }
        var howToExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(openLaunchMethods) {
            if (openLaunchMethods) {
                setupExpanded = true
                onLaunchMethodsOpened()
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = 20.dp,
                top = statusBarPadding + 16.dp,
                end = 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_page_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                val ok = "\u2713"
                val missing = "\u2717"
                PageSection(
                    icon = Icons.Default.Checklist,
                    title = stringResource(R.string.section_setup),
                    status = "${if (assistantGranted) ok else missing} " +
                        stringResource(R.string.setup_status_assistant) +
                        " \u00b7 ${if (a11yGranted) ok else missing} " +
                        stringResource(R.string.launch_a11y_settings_title),
                    warning = !a11yGranted && !assistantGranted,
                    expanded = setupExpanded,
                    onExpandedChange = { setupExpanded = it },
                ) {
                    SectionText(stringResource(R.string.setup_intro))
                    HomeLinkRow(
                        icon = Icons.Default.Assistant,
                        title = stringResource(R.string.launch_assistant_settings_title),
                        subtitle = stringResource(R.string.launch_assistant_settings_subtitle),
                        subtitleMaxLines = 3,
                        granted = assistantGranted,
                        onClick = {
                            WobbleHaptics.performUIHaptic(view)
                            showAssistantSheet = true
                        },
                    )
                    HomeLinkRow(
                        icon = Icons.Default.AccessibilityNew,
                        title = stringResource(R.string.launch_a11y_settings_title),
                        subtitle = stringResource(R.string.launch_a11y_settings_subtitle),
                        subtitleMaxLines = 3,
                        granted = a11yGranted,
                        warning = !a11yGranted,
                        onClick = {
                            WobbleHaptics.performUIHaptic(view)
                            // Granted: review or revoke; otherwise explain the use first.
                            if (a11yGranted) {
                                runCatching { settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                            } else {
                                showDisclosure = true
                            }
                        },
                    )
                }
            }
            item {
                PageSection(
                    icon = Icons.Default.TouchApp,
                    title = stringResource(R.string.section_how_to_open),
                    expanded = howToExpanded,
                    onExpandedChange = { howToExpanded = it },
                ) {
                    SectionText(stringResource(R.string.how_to_open_intro))
                    listOf(
                        R.string.how_to_open_assistant,
                        R.string.how_to_open_status_bar,
                        R.string.how_to_open_shortcut,
                        R.string.how_to_open_tile,
                        R.string.how_to_open_overlay,
                    ).forEach { SectionText("\u2022 " + stringResource(it)) }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!a11yGranted) {
                        Text(
                            text = stringResource(R.string.trigger_hw_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    HomeStatusBarSection(
                        config = statusBarCfg,
                        a11yGranted = a11yGranted,
                        expanded = statusBarExpanded,
                        onExpandedChange = { statusBarExpanded = it },
                        onChange = saveStatusBar,
                    )
                    com.fuzzyfly0.searchandtranslate.ui.CustomOverlaySection(
                        a11yGranted = a11yGranted,
                        expanded = customOverlayExpanded,
                        onExpandedChange = { customOverlayExpanded = it },
                    )
                }
            }
            item {
                PageSection(
                    icon = com.fuzzyfly0.searchandtranslate.ui.AppIcons.Search,
                    title = stringResource(R.string.settings_cat_search),
                ) {
                    SectionText(stringResource(R.string.search_intro))
                    com.fuzzyfly0.searchandtranslate.ui.SearchSettingsContent()
                }
            }
            item {
                PageSection(
                    icon = ImageVector.vectorResource(R.drawable.ic_translate_a),
                    title = stringResource(R.string.label_translator_title),
                ) {
                    SectionText(stringResource(R.string.translator_intro))
                    HomeLinkRow(
                        icon = Icons.Default.Settings,
                        title = stringResource(R.string.translator_settings_row),
                        subtitle = stringResource(R.string.label_translator_subtitle),
                        onClick = {
                            runCatching {
                                // Settings only; typing text to translate starts from the overlay
                                context.startActivity(
                                    Intent(context, dev.davidv.translator.MainActivity::class.java)
                                        .putExtra(dev.davidv.translator.MainActivity.EXTRA_OPEN_SETTINGS, true)
                                        .putExtra(dev.davidv.translator.MainActivity.EXTRA_SETTINGS_SECTION, "translator")
                                )
                            }.onFailure {
                                android.widget.Toast.makeText(context, it.message, android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                    )
                }
            }
            item {
                PageSection(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_cat_about),
                    divider = false,
                ) {
                    AboutContent()
                }
            }
        }
    }

}

/**
 * A section of the one-page settings (Circle to Share style): icon and bold title, an
 * optional status line and chevron when it folds, its content, then a thin line.
 */
@Composable
private fun PageSection(
    icon: ImageVector,
    title: String,
    status: String? = null,
    warning: Boolean = false,
    expanded: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    divider: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { m -> if (onExpandedChange != null) m.clickable { onExpandedChange(!expanded) } else m }
                .padding(vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                // In the accent colour, like Circle to Share's section icons.
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                // Circle to Share sizes: section titles a step below the page title.
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (status != null) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onExpandedChange != null) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        androidx.compose.animation.AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(bottom = 20.dp)) { content() }
        }
        if (divider) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

/** Explanation text in a section of the one-page settings (Circle to Share style). */
@Composable
private fun SectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

/** Where a card sits in a segmented group: outer corners large, inner corners small. */
private enum class GroupPos { Single, Top, Middle, Bottom }

/**
 * One segment of an Android 16 (Material 3 Expressive) settings group: segments sit
 * 2dp apart, the group's outer corners are 24dp, the inner ones 4dp.
 */
@Composable
private fun HomeBubble(pos: GroupPos = GroupPos.Single, content: @Composable () -> Unit) {
    val outer = 24.dp
    val inner = 4.dp
    // Flat like Circle to Share: no card, a thin line closes each group.
    Column(modifier = Modifier.fillMaxWidth()) {
        content()
        if (pos == GroupPos.Single || pos == GroupPos.Bottom) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun HomeRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
    )
}

@Composable
private fun HomeLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    // Main menu: the icon sits in a coloured circle like the AOSP Settings homepage.
    iconContainer: androidx.compose.ui.graphics.Color? = null,
    iconColor: androidx.compose.ui.graphics.Color? = null,
    // Red exclamation mark before the chevron, e.g. missing permissions on Launch methods.
    warning: Boolean = false,
    // Check mark before the chevron: the permission behind this row is granted.
    granted: Boolean = false,
    subtitleMaxLines: Int = 1,
) {
    val alpha = if (enabled) 1f else 0.38f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null && enabled) it.clickable { onClick() } else it }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconContainer != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = (iconColor ?: MaterialTheme.colorScheme.onSurface).copy(alpha = alpha),
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (granted) {
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = stringResource(R.string.wizard_granted),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp).size(22.dp),
            )
        }
        if (warning) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp).size(22.dp),
            )
        }
        if (onClick != null && enabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

/**
 * Expandable block for the status-bar overlay:
 * a header row with a master switch, and a dropdown with an animated strip preview
 * plus width / height / vertical-position sliders for the top trigger strip.
 * Driven by [StatusBarConfig] — fully independent from Custom overlay segments.
 */
@Composable
private fun HomeStatusBarSection(
    config: StatusBarConfig,
    a11yGranted: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onChange: (StatusBarConfig) -> Unit,
) {
    val masterOn = config.isEnabled
    val density = LocalDensity.current
    val expandProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "statusBarExpand",
    )

    // Expanding shows the trigger area on screen as a live preview; collapsing hides it.
    LaunchedEffect(expanded) {
        if (config.previewVisible != expanded) {
            onChange(config.copy(previewVisible = expanded))
        }
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    val widthDp = if (config.widthPx > 0) config.widthPx / density.density else screenWidthDp
    val heightDp = if (config.heightPx > 0) config.heightPx / density.density else 24f
    val yDp = config.yOffsetPx / density.density

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = a11yGranted) { onExpandedChange(!expanded) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val rowAlpha = if (a11yGranted) 1f else 0.38f
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        alpha = rowAlpha
                        rotationZ = expandProgress * 180f
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trigger_statusbar_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = rowAlpha),
                )
                Text(
                    text = stringResource(R.string.trigger_statusbar_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = rowAlpha),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = masterOn,
                enabled = a11yGranted,
                onCheckedChange = { on ->
                    onChange(config.copy(isEnabled = on))
                    if (on) onExpandedChange(true)
                },
            )
        }

        AnimatedVisibility(
            visible = expanded && a11yGranted,
            enter = expandVertically(tween(240)) + fadeIn(tween(240)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(160)),
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Animated preview of the trigger strip on a mini phone canvas.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .graphicsLayer { alpha = expandProgress },
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                val stripWidthFraction =
                                    (widthDp / screenWidthDp).coerceIn(0.08f, 1f)
                                Box(
                                    modifier = Modifier
                                        .padding(top = yDp.dp)
                                        .fillMaxWidth(stripWidthFraction)
                                        .height(heightDp.coerceAtMost(56f).dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        ),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LabelledSlider(
                            label = stringResource(R.string.home_statusbar_width),
                            value = widthDp,
                            valueRange = 60f..maxOf(screenWidthDp, widthDp),
                            valueText = "${widthDp.toInt()}dp",
                            onValueChange = {
                                onChange(config.copy(widthPx = (it * density.density).toInt()))
                            },
                        )
                        LabelledSlider(
                            label = stringResource(R.string.home_statusbar_height),
                            value = heightDp,
                            valueRange = 24f..140f,
                            valueText = "${heightDp.toInt()}dp",
                            onValueChange = {
                                onChange(config.copy(heightPx = (it * density.density).toInt()))
                            },
                        )
                        LabelledSlider(
                            label = stringResource(R.string.home_statusbar_ypos),
                            value = yDp,
                            valueRange = 0f..160f,
                            valueText = "${yDp.toInt()}dp",
                            onValueChange = {
                                onChange(config.copy(yOffsetPx = (it * density.density).toInt()))
                            },
                        )
                    }
                }
                OverlayResetRow(
                    // Size and position back to the defaults; on/off and preview stay.
                    onReset = {
                        onChange(StatusBarConfig(isEnabled = config.isEnabled, previewVisible = config.previewVisible))
                    },
                )
            }
        }
    }
}

/** Reset button at the bottom of every Launch methods section. */
@Composable
internal fun OverlayResetRow(onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onReset) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.cd_reset))
        }
    }
}

@Composable
private fun LabelledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

