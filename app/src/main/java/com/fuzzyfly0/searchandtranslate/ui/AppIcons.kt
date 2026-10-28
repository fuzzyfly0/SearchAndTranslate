package com.fuzzyfly0.searchandtranslate.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The app's own symbols (same artwork as res/drawable/ic_search_lens and ic_translate_a),
 * as ImageVectors for places that need one outside a composable, e.g. action icon maps.
 */
object AppIcons {
    /** Lens ring with a short round handle, as in the app logo. */
    val Search: ImageVector by lazy {
        strokeIcon("AppSearch", "M11.5,5 A6.5,6.5 0 1,1 11.5,18 A6.5,6.5 0 1,1 11.5,5 Z" to 2f, "M16.5,16.5 L18.2,18.2" to 2.6f)
    }

    /** Live cam: a plain circle. */
    val LiveCam: ImageVector by lazy {
        strokeIcon("AppLiveCam", "M12,4 A8,8 0 1,1 12,20 A8,8 0 1,1 12,4 Z" to 2f)
    }

    /** Latin "a" top left, Arabic ain bottom right. */
    val Translate: ImageVector by lazy {
        strokeIcon(
            "AppTranslate",
            "M4.24,4.62 C4.90,3.30 6.04,2.70 7.60,2.70 C9.88,2.70 11.08,3.96 11.08,6.06 L11.08,10.62 C11.08,11.34 11.38,11.82 11.92,12.06 M11.08,7.14 C9.40,7.14 7.24,7.20 5.92,7.74 C4.60,8.28 4.00,9.12 4.00,10.14 C4.00,11.40 5.02,12.12 6.52,12.12 C8.38,12.12 10.06,11.22 11.08,9.72" to 1.8f,
            "M18.80,9.80 C18.10,9.10 16.70,9.20 16.30,10.20 C15.90,11.20 16.80,12.20 18.40,12.30 C15.60,12.40 13.40,14.20 13.40,16.80 C13.40,19.60 15.60,21.00 18.20,20.80 C19.10,20.70 19.70,20.40 20.20,20.00" to 1.8f,
        )
    }
}

private fun strokeIcon(name: String, vararg strokes: Pair<String, Float>): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        strokes.forEach { (path, width) ->
            addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()
