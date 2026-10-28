package dev.davidv.translator.assistantOverlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Build
import android.view.RoundedCorner
import android.view.View

/**
 * Screen border shown while the translator overlay is active. Matches the host
 * app's border: a thin orange-to-pink line that follows the physical screen
 * outline (exact display shape on Android 14+, reported corner radii before),
 * drawn as the inner half of a stroke clipped to that outline so it has the same
 * width everywhere - the rounded corners included.
 */
class BorderWaveView private constructor(
  context: Context,
  private val strokePx: Float,
) : View(context) {
  companion object {
    private const val STROKE_DP = 3f

    // Logo colours of the host app (orange -> pink -> orange), 90% opacity.
    private val COLORS = intArrayOf(0xE6F97316.toInt(), 0xE6DB2777.toInt(), 0xE6F97316.toInt())

    fun create(context: Context): BorderWaveView {
      val density = context.resources.displayMetrics.density
      return BorderWaveView(context, (STROKE_DP * density).coerceAtLeast(1f))
    }
  }

  private val paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = strokePx * 2
    }
  private val outline = Path()
  private val location = IntArray(2)

  init {
    visibility = GONE
  }

  fun startAnimation() {
    visibility = VISIBLE
  }

  fun stopAnimation() {
    visibility = GONE
  }

  override fun onSizeChanged(
    w: Int,
    h: Int,
    oldw: Int,
    oldh: Int,
  ) {
    super.onSizeChanged(w, h, oldw, oldh)
    paint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), COLORS, null, Shader.TileMode.CLAMP)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (width == 0 || height == 0) return
    buildOutline()
    canvas.save()
    canvas.clipPath(outline)
    canvas.drawPath(outline, paint)
    canvas.restore()
  }

  private fun buildOutline() {
    outline.reset()
    if (Build.VERSION.SDK_INT >= 34) {
      val screenShape = runCatching { display?.shape?.path }.getOrNull()
      if (screenShape != null && !screenShape.isEmpty) {
        getLocationOnScreen(location)
        outline.set(screenShape)
        outline.offset(-location[0].toFloat(), -location[1].toFloat())
        return
      }
    }
    val r = cornerRadii()
    outline.addRoundRect(
      0f,
      0f,
      width.toFloat(),
      height.toFloat(),
      floatArrayOf(r[0], r[0], r[1], r[1], r[2], r[2], r[3], r[3]),
      Path.Direction.CW,
    )
  }

  /** Top-left, top-right, bottom-right, bottom-left corner radii in px. */
  private fun cornerRadii(): FloatArray {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val d = display
      if (d != null) {
        fun at(position: Int): Float = d.getRoundedCorner(position)?.radius?.toFloat() ?: 0f
        return floatArrayOf(
          at(RoundedCorner.POSITION_TOP_LEFT),
          at(RoundedCorner.POSITION_TOP_RIGHT),
          at(RoundedCorner.POSITION_BOTTOM_RIGHT),
          at(RoundedCorner.POSITION_BOTTOM_LEFT),
        )
      }
    }
    val id = resources.getIdentifier("rounded_corner_radius", "dimen", "android")
    val radius = if (id != 0) resources.getDimension(id) else 0f
    return floatArrayOf(radius, radius, radius, radius)
  }
}
