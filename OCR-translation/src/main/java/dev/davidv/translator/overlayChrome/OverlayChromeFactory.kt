package dev.davidv.translator.overlayChrome

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import dev.davidv.translator.Language
import dev.davidv.translator.R
import dev.davidv.translator.ReadingOrder
import dev.davidv.translator.localizedName
import kotlin.math.roundToInt

data class LanguageToolbarViews(
  val root: View,
  val sourceLabel: TextView,
  val targetLabel: TextView,
  val readingOrderButton: View? = null,
  val readingOrderIcon: ImageView? = null,
  val pauseIcon: ImageView? = null,
  val flipIcon: ImageView? = null,
  val flipButton: View? = null,
  val refreshButton: View? = null,
  val selectIcon: ImageView? = null,
)

/** Live handle to a shown language picker: rows can be replaced (install-state
 *  flips after a download) and download rows refreshed in place on progress. */
class LanguagePickerHandle(
  val root: View,
  internal val setRows: (List<dev.davidv.translator.OverlayLanguagePickerRow>) -> Unit,
  internal val updateDownloadStates: () -> Unit,
)

object OverlayChromeFactory {
  val ACTIVE_ICON_TINT: Int = Color.parseColor("#FDBA74")

  /**
   * Exact CTS bottom-bar color published by SearchAndTranslateScreen via SideEffect
   * (`surfaceContainerLow`). When set, the toolbar uses it pixel-for-pixel
   * instead of the locally derived scheme.
   */
  @Volatile
  var appBarContainerColor: Int? = null

  /** Button-pill fill (`surfaceContainerLowest`) — used for the toolbar's button pills. */
  @Volatile
  var appPillColor: Int? = null

  /** Fill of the host app's circle chooser pill (surfaceContainerHigh); the translate
   *  toolbar uses it so both pills look the same. */
  var appChooserPillColor: Int? = null

  /**
   * Host app mode: the translate toolbar takes the place of the host's main pill — full
   * width, same shape — and shows only the languages and Close. Set by the host.
   */
  var hostPillMode: Boolean = false

  /** Host app mode: its logo left in the toolbar with a small settings gear (bottom left)
   *  on [hostGearColor] / [hostGearTint]. A tap closes the translation and calls
   *  [hostLogoClick]. 0 = no logo. */
  var hostLogoRes: Int = 0
  var hostGearColor: Int? = null
  var hostGearTint: Int? = null
  var hostLogoClick: (() -> Unit)? = null

  /**
   * Toolbar/chrome colors derived from the same Material3 color scheme the app's
   * Compose UI uses — including Material You dynamic colors on Android 12+ — so the
   * bar background matches the translate button's `surfaceContainer` exactly.
   */
  data class ChromePalette(
    val onSurface: Int,
    val barColor: Int,
    val barStroke: Int,
    val chipColor: Int,
    val gripColor: Int,
    val activeTint: Int,
    val menuBg: Int,
    val menuText: Int,
    val menuTextDisabled: Int,
  )

  /** One cell of the host's 5-column main pill: width minus 2 × (12dp margin + 12dp padding). */
  private fun hostCellPx(context: Context, dpToPx: (Int) -> Int): Int =
    (context.resources.displayMetrics.widthPixels - 4 * dpToPx(12)) / 5

  /** Button size in the host's main pill: cell − 8dp, within 40–52dp. */
  private fun hostButtonPx(context: Context, dpToPx: (Int) -> Int): Int =
    (hostCellPx(context, dpToPx) - dpToPx(8)).coerceIn(dpToPx(40), dpToPx(52))

  fun isDark(context: Context): Boolean =
    (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
      android.content.res.Configuration.UI_MODE_NIGHT_YES

  private fun opaque(color: Int): Int = color or 0xFF000000.toInt()

  private fun withAlpha(color: Int, alpha: Float): Int =
    (color and 0x00FFFFFF) or (((alpha * 255).toInt() and 0xFF) shl 24)

  fun palette(context: Context): ChromePalette {
    val dark = isDark(context)
    val scheme =
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (dark) androidx.compose.material3.dynamicDarkColorScheme(context)
        else androidx.compose.material3.dynamicLightColorScheme(context)
      } else {
        if (dark) androidx.compose.material3.darkColorScheme()
        else androidx.compose.material3.lightColorScheme()
      }
    val onSurface = scheme.onSurface.toArgb()
    return ChromePalette(
      onSurface = onSurface,
      // CTS bottom-bar container (surfaceContainerLow) — prefer the exact value
      // published by the app UI so the bar matches pixel-for-pixel.
      barColor = opaque(appBarContainerColor ?: scheme.surfaceContainerLow.toArgb()),
      barStroke = withAlpha(onSurface, if (dark) 0.18f else 0.15f),
      // Button-pill fill (surfaceContainerLowest) — same fill for every button pill.
      chipColor = opaque(appPillColor ?: scheme.surfaceContainerLowest.toArgb()),
      gripColor = withAlpha(onSurface, 0.5f),
      activeTint = scheme.primary.toArgb(),
      menuBg = opaque(scheme.surfaceContainerHighest.toArgb()),
      menuText = onSurface,
      menuTextDisabled = withAlpha(onSurface, 0.38f),
    )
  }

  fun activeIconTint(context: Context): Int = palette(context).activeTint

  /**
   * Size for the language-picker dialog in pixels: most of the screen so the
   * full language list is browsable, capped on wide (tablet) screens.
   */
  fun pickerWindowSize(context: Context): Pair<Int, Int> {
    val dm = context.resources.displayMetrics
    val width = (dm.widthPixels * 0.88f).toInt().coerceAtMost((480 * dm.density).toInt())
    val height = (dm.heightPixels * 0.78f).toInt().coerceAtMost((760 * dm.density).toInt())
    return width to height
  }

  fun formatSourceLabel(
    forcedSourceLanguage: Language?,
    isAutoSource: Boolean,
  ): String =
    when {
      isAutoSource && forcedSourceLanguage != null -> "✨ ${forcedSourceLanguage.shortDisplayName}"
      isAutoSource -> "✨ Auto"
      else -> forcedSourceLanguage?.shortDisplayName ?: "?"
    }

  fun createLanguageToolbar(
    context: Context,
    dpToPx: (Int) -> Int,
    forcedSourceLanguage: Language?,
    forcedTargetLanguage: Language?,
    defaultTargetLanguage: Language,
    onClose: () -> Unit,
    onSelectModeClick: (() -> Unit)? = null,
    onSourceClick: () -> Unit,
    onSwap: () -> Unit,
    onTargetClick: () -> Unit,
    showReadingOrderButton: Boolean = false,
    readingOrder: ReadingOrder? = null,
    onReadingOrderClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    onRegionClick: (() -> Unit)? = null,
    onFlipOriginal: (() -> Unit)? = null,
    onPauseClick: (() -> Unit)? = null,
    isPaused: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    isAutoSource: Boolean = false,
  ): LanguageToolbarViews {
    // Same metrics as the circle chooser pill: 48dp flat icon buttons, 24dp icons.
    // Host pill mode copies the host's main pill grid instead: 5 columns across the
    // width minus 2 × (12dp margin + 12dp padding), buttons cell − 8dp within 40–52dp.
    val hostCell = hostCellPx(context, dpToPx)
    val btnSize = if (hostPillMode) hostButtonPx(context, dpToPx) else dpToPx(48)
    val iconPad = (btnSize - dpToPx(24)) / 2
    val p = palette(context)
    val pillColor =
      opaque(
        appChooserPillColor
          ?: (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (isDark(context)) androidx.compose.material3.dynamicDarkColorScheme(context)
            else androidx.compose.material3.dynamicLightColorScheme(context)
          } else {
            if (isDark(context)) androidx.compose.material3.darkColorScheme()
            else androidx.compose.material3.lightColorScheme()
          }).surfaceContainerHigh.toArgb(),
      )

    // M3 Expressive drag grip: a small tonal pill above the bar (bottom-sheet style).
    val grip =
      FrameLayout(context).apply {
        val gripBar =
          View(context).apply {
            background =
              GradientDrawable().apply {
                setColor(p.gripColor)
                cornerRadius = dpToPx(2).toFloat()
              }
          }
        addView(
          gripBar,
          FrameLayout.LayoutParams(dpToPx(28), dpToPx(4)).apply { gravity = Gravity.CENTER },
        )
      }

    // One fully rounded bar like the circle chooser pill: surfaceContainerHigh,
    // 8dp shadow, 4dp inner padding, no stroke, no dividers.
    val bar =
      LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background =
          GradientDrawable().apply {
            setColor(pillColor)
            cornerRadius = dpToPx(if (hostPillMode) 28 else 64).toFloat()
          }
        elevation = dpToPx(8).toFloat()
        if (hostPillMode) {
          setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
        } else {
          setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }
      }

    // Flat icon button (no tonal pill), circular ripple — like an M3 IconButton.
    fun iconButton(icon: ImageView): FrameLayout {
      val fl = FrameLayout(context)
      val out = TypedValue()
      if (context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, out, true) && out.resourceId != 0) {
        fl.background = context.getDrawable(out.resourceId)
      }
      fl.addView(
        icon,
        FrameLayout.LayoutParams(btnSize, btnSize).apply { gravity = Gravity.CENTER },
      )
      return fl
    }

    fun iconLayoutParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(btnSize, btnSize)

    val closeBtn =
      ImageView(context).apply {
        setImageResource(R.drawable.cancel)
        setColorFilter(p.onSurface)
        setPadding(iconPad, iconPad, iconPad, iconPad)
        setOnClickListener { onClose() }
        contentDescription = context.getString(R.string.a11y_close)
      }

    val hasSelectMode = onSelectModeClick != null
    var selectIcon: ImageView? = null
    var selectPill: View? = null
    onSelectModeClick?.let {
      val selectBtn =
        ImageView(context).apply {
          setImageResource(R.drawable.text_select_start)
          setColorFilter(p.onSurface)
          setPadding(iconPad, iconPad, iconPad, iconPad)
          setOnClickListener { onSelectModeClick() }
          contentDescription = context.getString(R.string.a11y_select_text)
        }
      selectIcon = selectBtn
      selectPill = iconButton(selectBtn)
    }

    var readingOrderIcon: ImageView? = null
    val readingOrderPill =
      onReadingOrderClick?.let {
        val readingBtn =
          ImageView(context).apply {
            setPadding(iconPad, iconPad, iconPad, iconPad)
            setOnClickListener { onReadingOrderClick() }
          }
        readingOrderIcon = readingBtn
        updateReadingOrderButtonState(
          readingButton = null,
          readingIcon = readingBtn,
          visible = showReadingOrderButton,
          readingOrder = readingOrder,
        )
        iconButton(readingBtn).also { pill ->
          pill.visibility = if (showReadingOrderButton) View.VISIBLE else View.GONE
        }
      }

    val langRow = LinearLayout(context)
    langRow.orientation = LinearLayout.HORIZONTAL
    langRow.gravity = Gravity.CENTER_VERTICAL
    langRow.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))

    val sourceLabel = TextView(context)
    sourceLabel.text = formatSourceLabel(forcedSourceLanguage, isAutoSource)
    sourceLabel.setTextColor(p.onSurface)
    sourceLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    sourceLabel.gravity = Gravity.CENTER
    sourceLabel.maxLines = 1
    sourceLabel.ellipsize = TextUtils.TruncateAt.END
    sourceLabel.setOnClickListener { onSourceClick() }
    langRow.addView(sourceLabel, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))

    val swapBtn = ImageView(context)
    swapBtn.setImageResource(R.drawable.compare)
    swapBtn.setColorFilter(p.onSurface)
    val swapSize = dpToPx(20)
    val swapPad = dpToPx(6)
    swapBtn.setPadding(swapPad, 0, swapPad, 0)
    swapBtn.setOnClickListener { onSwap() }
    langRow.addView(swapBtn, LinearLayout.LayoutParams(swapSize + swapPad * 2, swapSize))

    val currentTarget = forcedTargetLanguage ?: defaultTargetLanguage
    val targetLabel = TextView(context)
    targetLabel.text = currentTarget.shortDisplayName
    targetLabel.setTextColor(p.onSurface)
    targetLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    targetLabel.gravity = Gravity.CENTER
    targetLabel.maxLines = 1
    targetLabel.ellipsize = TextUtils.TruncateAt.END
    targetLabel.setOnClickListener { onTargetClick() }
    langRow.addView(targetLabel, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))

    // Language row sits flat in the bar.
    val langChip =
      FrameLayout(context).apply {
        addView(
          langRow,
          FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            .apply { gravity = Gravity.CENTER_VERTICAL },
        )
      }

    var refreshButton: View? = null
    onRefreshClick?.let {
      val refreshBtn =
        ImageView(context).apply {
          setImageResource(R.drawable.refresh)
          setColorFilter(p.onSurface)
          setPadding(iconPad, iconPad, iconPad, iconPad)
          setOnClickListener { onRefreshClick() }
          contentDescription = context.getString(R.string.a11y_refresh)
        }
      val refreshPill = iconButton(refreshBtn)
      refreshButton = refreshPill
    }

    var regionPill: View? = null
    onRegionClick?.let {
      val regionBtn =
        ImageView(context).apply {
          setImageResource(R.drawable.activity_zone)
          setColorFilter(p.onSurface)
          setPadding(iconPad, iconPad, iconPad, iconPad)
          setOnClickListener { onRegionClick() }
          contentDescription = context.getString(R.string.a11y_set_translation_area)
        }
      regionPill = iconButton(regionBtn)
    }

    var flipIcon: ImageView? = null
    var flipButton: View? = null
    onFlipOriginal?.let {
      val flipBtn =
        ImageView(context).apply {
          setImageResource(R.drawable.flip)
          setColorFilter(p.onSurface)
          setPadding(iconPad, iconPad, iconPad, iconPad)
          setOnClickListener { onFlipOriginal() }
          contentDescription = context.getString(R.string.a11y_show_original)
        }
      flipIcon = flipBtn
      val flipPill = iconButton(flipBtn)
      flipButton = flipPill
    }

    var pauseIcon: ImageView? = null
    var pausePill: View? = null
    onPauseClick?.let {
      val pauseBtn =
        ImageView(context).apply {
          setImageResource(if (isPaused) R.drawable.play_arrow else R.drawable.pause)
          setColorFilter(p.onSurface)
          setPadding(iconPad, iconPad, iconPad, iconPad)
          setOnClickListener { onPauseClick() }
          contentDescription = context.getString(R.string.a11y_pause_resume)
        }
      pauseIcon = pauseBtn
      pausePill = iconButton(pauseBtn)
    }

    var menuPill: View? = null
    onMenuClick?.let {
      val menuBtn =
        ImageView(context).apply {
          setImageResource(R.drawable.more_vert)
          setPadding(iconPad, iconPad, iconPad, iconPad)
          setOnClickListener { onMenuClick() }
        }
      menuPill = iconButton(menuBtn)
    }

    if (hostPillMode) {
      // Same look as the host's main pill: logo (settings), languages, Close (no labels).
      val closeCell =
        LinearLayout(context).apply {
          gravity = Gravity.CENTER
          // Framed like the host's Close: rounded corner brackets at 75 % of the button.
          addView(iconButton(closeBtn).apply { foreground = CornerBracketsDrawable(p.onSurface, dpToPx(2).toFloat()) }, iconLayoutParams())
        }
      if (hostLogoRes != 0) {
        val gearSize = (btnSize * 0.38f).toInt()
        val logo =
          FrameLayout(context).apply {
            addView(
              ImageView(context).apply { setImageResource(hostLogoRes) },
              FrameLayout.LayoutParams(btnSize, btnSize),
            )
            addView(
              ImageView(context).apply {
                setImageResource(R.drawable.settings_filled)
                setColorFilter(hostGearTint ?: p.onSurface)
                val pad = dpToPx(2)
                setPadding(pad, pad, pad, pad)
                background = GradientDrawable().apply {
                  shape = GradientDrawable.OVAL
                  setColor(hostGearColor ?: p.chipColor)
                }
              },
              FrameLayout.LayoutParams(gearSize, gearSize).apply { gravity = Gravity.BOTTOM or Gravity.START },
            )
            contentDescription = context.getString(R.string.a11y_settings)
            isClickable = true
            setOnClickListener {
              onClose()
              hostLogoClick?.invoke()
            }
          }
        val logoCell =
          LinearLayout(context).apply {
            gravity = Gravity.CENTER
            addView(logo, LinearLayout.LayoutParams(btnSize, btnSize))
          }
        bar.addView(logoCell, LinearLayout.LayoutParams(hostCell, LinearLayout.LayoutParams.WRAP_CONTENT))
      }
      bar.addView(langChip, LinearLayout.LayoutParams(0, btnSize, 1f))
      bar.addView(closeCell, LinearLayout.LayoutParams(hostCell, LinearLayout.LayoutParams.WRAP_CONTENT))
    } else {
      // Order: actions on the left (refresh first), languages in the middle,
      // close at the far right.
      val leftButtons = listOfNotNull(refreshButton, regionPill, flipButton, pausePill, selectPill, readingOrderPill)
      leftButtons.forEach { bar.addView(it, iconLayoutParams()) }
      bar.addView(langChip, LinearLayout.LayoutParams(0, btnSize, 1f))
      menuPill?.let { bar.addView(it, iconLayoutParams()) }
      bar.addView(iconButton(closeBtn), iconLayoutParams())
    }

    // Root: grip stacked on the bar; the whole thing is wrap-content so it can be
    // dragged around the screen (OverlayUI moves it via translation / window params).
    val root =
      LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        if (hostPillMode) {
          // In place of the host's main pill: full width with its 12dp side margins.
          setPadding(dpToPx(12), 0, dpToPx(12), 0)
          addView(bar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        } else {
          addView(
            grip,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
              .apply { bottomMargin = dpToPx(2) },
          )
          addView(bar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
      }

    return LanguageToolbarViews(
      root = root,
      sourceLabel = sourceLabel,
      targetLabel = targetLabel,
      readingOrderButton = readingOrderPill,
      readingOrderIcon = readingOrderIcon,
      pauseIcon = pauseIcon,
      flipIcon = flipIcon,
      flipButton = flipButton,
      refreshButton = refreshButton,
      selectIcon = selectIcon,
    )
  }

  fun setPauseIconState(
    pauseIcon: ImageView?,
    isPaused: Boolean,
  ) {
    pauseIcon?.setImageResource(if (isPaused) R.drawable.play_arrow else R.drawable.pause)
    pauseIcon?.setColorFilter(palette(pauseIcon.context).onSurface)
  }

  fun updateReadingOrderButtonState(
    readingButton: View?,
    readingIcon: ImageView?,
    visible: Boolean,
    readingOrder: ReadingOrder?,
  ) {
    readingButton?.visibility = if (visible) View.VISIBLE else View.GONE
    readingIcon?.apply {
      val (iconRes, description) =
        when (readingOrder) {
          null -> R.drawable.text_rotation_auto to "Japanese OCR auto mode"
          ReadingOrder.TOP_TO_BOTTOM_RIGHT_TO_LEFT -> R.drawable.text_rotate_vertical to "Japanese OCR vertical mode"
          ReadingOrder.LEFT_TO_RIGHT -> R.drawable.text_rotation_none to "Japanese OCR horizontal mode"
        }
      setImageResource(iconRes)
      setColorFilter(palette(context).onSurface)
      contentDescription = description
    }
  }

  fun createLanguagePicker(
    context: Context,
    dpToPx: (Int) -> Int,
    isSource: Boolean,
    rowsProvider: () -> List<dev.davidv.translator.OverlayLanguagePickerRow>,
    allowAuto: Boolean = true,
    downloadStateFor: (Language) -> dev.davidv.translator.DownloadState?,
    onPick: (Language?) -> Unit,
    onDownload: (dev.davidv.translator.OverlayLanguagePickerRow) -> Unit,
    onCancelDownload: (Language) -> Unit,
    onDelete: (dev.davidv.translator.OverlayLanguagePickerRow) -> Unit,
    onToggleFavorite: (Language, Boolean) -> Unit,
    // Shows a close (✕) button above the list when set.
    onClose: (() -> Unit)? = null,
  ): LanguagePickerHandle {
    val p = palette(context)
    val scroll = ScrollView(context)
    val list = LinearLayout(context)
    list.orientation = LinearLayout.VERTICAL
    // Rounded surface on the ScrollView itself (not the scrolling content), so the
    // corners stay rounded while scrolling instead of clipping to a square window.
    val bg =
      GradientDrawable().apply {
        setColor(p.menuBg)
        cornerRadius = dpToPx(20).toFloat()
        setStroke(dpToPx(1), p.barStroke)
      }
    scroll.background = bg
    scroll.clipToOutline = true
    val listPad = dpToPx(8)
    list.setPadding(listPad, listPad, listPad, listPad)
    scroll.addView(list)

    var currentRows = rowsProvider()
    val stateUpdaters = mutableListOf<() -> Unit>()

    fun formatSize(sizeBytes: Long): String {
      val sizeMiB = sizeBytes / (1024f * 1024f)
      return if (sizeMiB < 1f) {
        "\u003c1 MB"
      } else {
        "${sizeMiB.roundToInt()} MB"
      }
    }

    fun formatPercent(downloaded: Long, total: Long): String {
      val pct =
        if (total > 0) {
          ((downloaded * 100) / total).toInt().coerceIn(0, 100)
        } else {
          0
        }
      return "$pct%"
    }

    fun applyRipple(view: View) {
      val out = TypedValue()
      if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true) && out.resourceId != 0) {
        view.setBackgroundResource(out.resourceId)
      }
    }

    fun render(rowList: List<dev.davidv.translator.OverlayLanguagePickerRow>) {
      currentRows = rowList
      list.removeAllViews()
      stateUpdaters.clear()

      if (isSource && allowAuto) {
        val autoItem =
          TextView(context).apply {
            text = "\u2728  Auto-detect"
            setTextColor(p.menuText)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            val pad = dpToPx(12)
            setPadding(pad, pad, pad, pad)
            applyRipple(this)
            setOnClickListener { onPick(null) }
          }
        list.addView(autoItem)
      }

      for (row in rowList) {
        val language = row.language
        val state = downloadStateFor(language)
        val showAsInstalled = row.installed || state?.isCompleted == true
        val downloadingState = if (!showAsInstalled && state?.isDownloading == true) state else null

        val item = LinearLayout(context)
        item.orientation = LinearLayout.HORIZONTAL
        item.gravity = Gravity.CENTER_VERTICAL
        val itemPad = dpToPx(12)
        item.setPadding(itemPad, itemPad, itemPad, itemPad)
        applyRipple(item)

        val name =
          TextView(context).apply {
            text = language.localizedName()
            setTextColor(p.menuText)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
          }
        item.addView(name, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        // Trailing group: [size % / progress ring] [star] [download button]
        val trailing =
          LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
          }

        var sizeText: TextView? = null
        if (!showAsInstalled || row.installedSizeBytes > 0) {
          val label =
            TextView(context).apply {
              text =
                when {
                  downloadingState != null -> formatPercent(downloadingState.downloaded, downloadingState.totalSize)
                  showAsInstalled -> formatSize(row.installedSizeBytes)
                  else -> formatSize(row.downloadSizeBytes)
                }
              setTextColor(p.menuTextDisabled)
              setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
              gravity = Gravity.END
            }
          sizeText = label
          trailing.addView(
            label,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
              .apply { marginEnd = dpToPx(8) },
          )
        }

        val starHolder =
          FrameLayout(context).apply {
            val starSize = dpToPx(36)
            val starPad = dpToPx(9)
            val star =
              ImageView(context).apply {
                setPadding(starPad, starPad, starPad, starPad)
              }
            fun applyStar(favorite: Boolean) {
              star.setImageResource(if (favorite) R.drawable.star_filled else R.drawable.star)
              star.setColorFilter(if (favorite) p.activeTint else p.menuTextDisabled)
              star.contentDescription = if (favorite) "Unstar ${language.displayName}" else "Star ${language.displayName}"
            }
            applyStar(row.favorite)
            addView(star, FrameLayout.LayoutParams(starSize, starSize).apply { gravity = Gravity.CENTER })
            setOnClickListener {
              val newFavorite = !(currentRows.find { it.language.code == language.code }?.favorite ?: row.favorite)
              applyStar(newFavorite)
              onToggleFavorite(language, newFavorite)
            }
          }
        trailing.addView(starHolder)

        if (!showAsInstalled) {
          val iconHolder = FrameLayout(context)
          val iconSize = dpToPx(36)
          fun setDownloadIcon() {
            iconHolder.removeAllViews()
            iconHolder.setOnClickListener(null)
            val pill =
              GradientDrawable().apply {
                setColor(p.chipColor)
                cornerRadius = dpToPx(18).toFloat()
              }
            val out = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true) && out.resourceId != 0) {
              iconHolder.background =
                android.graphics.drawable.LayerDrawable(
                  arrayOf(
                    pill,
                    context.getDrawable(out.resourceId) ?: android.graphics.drawable.ColorDrawable(Color.TRANSPARENT),
                  ),
                )
            } else {
              iconHolder.background = pill
            }
            val iconPad = dpToPx(8)
            val icon =
              ImageView(context).apply {
                setImageResource(R.drawable.download)
                setColorFilter(p.onSurface)
                setPadding(iconPad, iconPad, iconPad, iconPad)
                contentDescription = "Download ${language.displayName}"
              }
            iconHolder.addView(icon, FrameLayout.LayoutParams(iconSize, iconSize).apply { gravity = Gravity.CENTER })
            iconHolder.setOnClickListener { onDownload(row) }
          }
          fun setProgressRing(downloaded: Long, total: Long) {
            iconHolder.removeAllViews()
            iconHolder.background = null
            val ring =
              android.widget.ProgressBar(context, null, android.R.attr.progressBarStyleSmall).apply {
                isIndeterminate = false
                max = 100
                progress =
                  if (total > 0) {
                    ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                  } else {
                    0
                  }
                progressTintList = android.content.res.ColorStateList.valueOf(p.activeTint)
              }
            val ringSize = dpToPx(24)
            iconHolder.addView(ring, FrameLayout.LayoutParams(ringSize, ringSize).apply { gravity = Gravity.CENTER })
            // Tapping the ring cancels the download, like the language setup screen.
            iconHolder.setOnClickListener { onCancelDownload(language) }
          }
          if (downloadingState != null) {
            setProgressRing(downloadingState.downloaded, downloadingState.totalSize)
          } else {
            setDownloadIcon()
          }
          trailing.addView(iconHolder, LinearLayout.LayoutParams(iconSize, iconSize))

          stateUpdaters.add {
            val currentState = downloadStateFor(language)
            if (currentState?.isDownloading == true) {
              setProgressRing(currentState.downloaded, currentState.totalSize)
              sizeText?.text = formatPercent(currentState.downloaded, currentState.totalSize)
            } else {
              setDownloadIcon()
              sizeText?.text = formatSize(row.downloadSizeBytes)
            }
          }
        } else if (!language.isEnglish) {
          // Installed rows: delete button (English has no downloadable assets).
          val deleteHolder =
            FrameLayout(context).apply {
              val delSize = dpToPx(36)
              val pill =
                GradientDrawable().apply {
                  setColor(p.chipColor)
                  cornerRadius = dpToPx(18).toFloat()
                }
              val out = TypedValue()
              if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true) && out.resourceId != 0) {
                background =
                  android.graphics.drawable.LayerDrawable(
                    arrayOf(
                      pill,
                      context.getDrawable(out.resourceId) ?: android.graphics.drawable.ColorDrawable(Color.TRANSPARENT),
                    ),
                  )
              } else {
                background = pill
              }
              val iconPad = dpToPx(8)
              val icon =
                ImageView(context).apply {
                  setImageResource(R.drawable.delete)
                  setColorFilter(p.onSurface)
                  setPadding(iconPad, iconPad, iconPad, iconPad)
                  contentDescription = "Delete ${language.displayName}"
                }
              addView(icon, FrameLayout.LayoutParams(delSize, delSize).apply { gravity = Gravity.CENTER })
              setOnClickListener { onDelete(row) }
            }
          trailing.addView(deleteHolder, LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)))
        }

        item.addView(trailing)
        if (showAsInstalled) {
          item.setOnClickListener { onPick(language) }
        }
        list.addView(item)
      }
      scroll.scrollTo(0, 0)
    }

    render(currentRows)

    // With a close button the rounded surface moves to a wrapper: list on top, ✕ row below.
    val root: View =
      if (onClose == null) {
        scroll
      } else {
        scroll.background = null
        scroll.clipToOutline = false
        // Host pill mode: the ✕ is as large as the buttons of the host's main pill.
        val closeSize = if (hostPillMode) hostButtonPx(context, dpToPx) else dpToPx(44)
        LinearLayout(context).apply {
          orientation = LinearLayout.VERTICAL
          background = bg
          clipToOutline = true
          val closeRow =
            LinearLayout(context).apply {
              orientation = LinearLayout.HORIZONTAL
              gravity = Gravity.END or Gravity.CENTER_VERTICAL
              setPadding(dpToPx(8), 0, dpToPx(4), dpToPx(4))
              val close =
                ImageView(context).apply {
                  setImageResource(R.drawable.cancel)
                  setColorFilter(p.onSurface)
                  val pad = (closeSize - dpToPx(24)) / 2
                  setPadding(pad, pad, pad, pad)
                  contentDescription = context.getString(R.string.a11y_close)
                  val out = TypedValue()
                  if (context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, out, true) && out.resourceId != 0) {
                    background = context.getDrawable(out.resourceId)
                  }
                  setOnClickListener { onClose() }
                }
              addView(close, LinearLayout.LayoutParams(closeSize, closeSize))
            }
          addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
          addView(closeRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
      }
    return LanguagePickerHandle(
      root = root,
      setRows = { render(it) },
      updateDownloadStates = { stateUpdaters.forEach { update -> update() } },
    )
  }

  fun createMenuContainer(
    context: Context,
    dpToPx: (Int) -> Int,
  ): LinearLayout {
    val container = LinearLayout(context)
    container.orientation = LinearLayout.VERTICAL
    val bg = GradientDrawable()
    bg.setColor(palette(context).menuBg)
    bg.cornerRadius = dpToPx(20).toFloat()
    container.background = bg
    val pad = dpToPx(8)
    container.setPadding(pad, pad, pad, pad)
    return container
  }

  fun makePill(
    context: Context,
    dpToPx: (Int) -> Int,
    view: View,
  ): FrameLayout {
    val pill = FrameLayout(context)
    val bg = GradientDrawable()
    bg.setColor(Color.parseColor("#CC303030"))
    bg.cornerRadius = dpToPx(20).toFloat()
    pill.background = bg
    pill.addView(view)
    return pill
  }

  fun addMenuItem(
    context: Context,
    parent: LinearLayout,
    dpToPx: (Int) -> Int,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
  ) {
    val item = TextView(context)
    item.text = label
    val p = palette(context)
    item.setTextColor(if (enabled) p.menuText else p.menuTextDisabled)
    item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
    val pad = dpToPx(12)
    item.setPadding(pad, pad, pad, pad)
    val out = TypedValue()
    if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, out, true) && out.resourceId != 0) {
      item.setBackgroundResource(out.resourceId)
    }
    item.setOnClickListener { onClick() }
    parent.addView(item)
  }
}

/** Rounded corner brackets (75 % of the bounds, centred) — the host's framed-button look. */
private class CornerBracketsDrawable(
  color: Int,
  strokeWidth: Float,
) : android.graphics.drawable.Drawable() {
  private val paint =
    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
      style = android.graphics.Paint.Style.STROKE
      this.color = color
      this.strokeWidth = strokeWidth
      strokeCap = android.graphics.Paint.Cap.ROUND
      strokeJoin = android.graphics.Paint.Join.ROUND
    }

  override fun draw(canvas: android.graphics.Canvas) {
    val b = bounds
    val frame = minOf(b.width(), b.height()) * 0.75f
    val l = b.left + (b.width() - frame) / 2f
    val t = b.top + (b.height() - frame) / 2f
    val r = l + frame
    val bt = t + frame
    val radius = frame * 0.22f
    val arm = frame * 0.34f
    fun corner(x0: Float, y0: Float, sx: Float, sy: Float) {
      val path =
        android.graphics.Path().apply {
          moveTo(x0, y0 + sy * arm)
          lineTo(x0, y0 + sy * radius)
          quadTo(x0, y0, x0 + sx * radius, y0)
          lineTo(x0 + sx * arm, y0)
        }
      canvas.drawPath(path, paint)
    }
    corner(l, t, 1f, 1f)
    corner(r, t, -1f, 1f)
    corner(r, bt, -1f, -1f)
    corner(l, bt, 1f, -1f)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
  }

  override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
    paint.colorFilter = colorFilter
  }

  @Deprecated("Deprecated in Java")
  override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
