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

    /** Arabic ain top left, Latin "a" bottom right. */
    val Translate: ImageVector by lazy {
        strokeIcon(
            "AppTranslate",
            "M9.4,3.4 C8.7,2.7 7.3,2.8 6.9,3.8 C6.5,4.8 7.4,5.8 9.0,5.9 C6.2,6.0 4.0,7.8 4.0,10.4 C4.0,13.2 6.2,14.6 8.8,14.4 C9.7,14.3 10.3,14.0 10.8,13.6" to 1.8f,
            "M12.74,13.12 C13.40,11.80 14.54,11.20 16.10,11.20 C18.38,11.20 19.58,12.46 19.58,14.56 L19.58,19.12 C19.58,19.84 19.88,20.32 20.42,20.56 M19.58,15.64 C17.90,15.64 15.74,15.70 14.42,16.24 C13.10,16.78 12.50,17.62 12.50,18.64 C12.50,19.90 13.52,20.62 15.02,20.62 C16.88,20.62 18.56,19.72 19.58,18.22" to 1.8f,
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
