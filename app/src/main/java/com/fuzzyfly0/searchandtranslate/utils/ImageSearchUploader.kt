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


package com.fuzzyfly0.searchandtranslate.utils

import android.graphics.Bitmap
import android.util.Base64
import com.fuzzyfly0.searchandtranslate.data.SearchEngine
import java.io.ByteArrayOutputStream

/**
 * Image search without a third-party image host: each results tab loads a tiny page that
 * posts the circled image straight to the engine's own upload form (what its website does
 * when you pick a file). The image only goes to the engine shown in that tab.
 */
object ImageSearchUploader {

    /** Scheme of the placeholder URLs that stand for "post the image to this engine". */
    const val DIRECT_UPLOAD_PREFIX = "direct-upload:"

    private const val MAX_SIDE = 1600

    /** The image as JPEG (longest side ≤ 1600 px), base64 without line breaks. */
    data class EncodedImage(val base64: String, val width: Int, val height: Int)

    fun encode(bitmap: Bitmap): EncodedImage {
        val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(bitmap.width, bitmap.height))
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
        return EncodedImage(Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP), scaled.width, scaled.height)
    }

    fun placeholderUrl(engine: SearchEngine): String = DIRECT_UPLOAD_PREFIX + engine.name

    /**
     * Base URL (the engine's own origin, so the form post is same-site and keeps its
     * consent cookies) and the HTML that submits [image] to the engine.
     */
    fun uploadPage(engine: SearchEngine, image: EncodedImage, language: String): Pair<String, String> {
        // fileField: multipart file input; textFields: plain fields (value may be the base64 data).
        val (base, action, fileField, textFields) = when (engine) {
            SearchEngine.Google -> Quad(
                "https://lens.google.com/",
                "https://lens.google.com/v3/upload?hl=$language&re=df&st=${System.currentTimeMillis()}&ep=gsbubb",
                "encoded_image",
                mapOf("processed_image_dimensions" to "${image.width},${image.height}"),
            )
            SearchEngine.Bing -> Quad(
                "https://www.bing.com/",
                "https://www.bing.com/images/search?view=detailv2&iss=sbiupload&FORM=SBIIDP",
                null,
                mapOf("cbir" to "sbi", "imageBin" to BASE64_MARKER),
            )
            SearchEngine.TinEye -> Quad(
                "https://tineye.com/",
                "https://tineye.com/search",
                "image",
                emptyMap(),
            )
        }
        val fields = textFields.entries.joinToString("") { (name, value) ->
            val js = if (value == BASE64_MARKER) "b64" else "'" + value.replace("'", "") + "'"
            "addText('$name', $js);"
        }
        val file = if (fileField != null) "addFile('$fileField');" else ""
        val html = """
            <!doctype html><html><head><meta name="viewport" content="width=device-width"></head>
            <body style="background:#fff"><form id="f" method="POST" enctype="multipart/form-data" action="$action"></form>
            <script>
            var b64 = "${image.base64}";
            var f = document.getElementById('f');
            function addText(name, value) {
              var i = document.createElement('input'); i.type = 'hidden'; i.name = name; i.value = value; f.appendChild(i);
            }
            function addFile(name) {
              var bin = atob(b64), bytes = new Uint8Array(bin.length);
              for (var k = 0; k < bin.length; k++) bytes[k] = bin.charCodeAt(k);
              var dt = new DataTransfer();
              dt.items.add(new File([bytes], 'image.jpg', { type: 'image/jpeg' }));
              var i = document.createElement('input'); i.type = 'file'; i.name = name; i.files = dt.files; f.appendChild(i);
            }
            $fields$file
            f.submit();
            </script></body></html>
        """.trimIndent()
        return base to html
    }

    private const val BASE64_MARKER = "\u0000base64"

    private data class Quad(val base: String, val action: String, val fileField: String?, val textFields: Map<String, String>)
}
