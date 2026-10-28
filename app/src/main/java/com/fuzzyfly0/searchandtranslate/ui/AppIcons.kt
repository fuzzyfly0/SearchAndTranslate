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

    /** Latin "b" top left, Arabic ba (with its dot) bottom right. */
    val Translate: ImageVector by lazy {
        strokeIcon(
            "AppTranslate",
            "M4.6,2.6 L4.6,12.3 M4.6,9.2 A3.1,3.1 0 1,1 10.8,9.2 A3.1,3.1 0 1,1 4.6,9.2" to 1.8f,
            "M20.9,13.4 C21.3,15.9 19.7,17.3 16.8,17.3 C14.1,17.3 12.8,16.4 12.5,14.6" to 1.8f,
            "M16.7,20.2 m-0.15,0 a0.15,0.15 0 1,0 0.3,0 a0.15,0.15 0 1,0 -0.3,0" to 2.3f,
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
