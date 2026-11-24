/*
 *
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
package com.fuzzyfly0.searchandtranslate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fuzzyfly0.searchandtranslate.ui.theme.SearchAndTranslateTheme

/** Licenses on their own page, laid out like Circle to Share's. */
class LicensesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SearchAndTranslateTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LicensesScreen(onBack = { finish() })
                }
            }
        }
    }
}

private class Component(val name: String, val holder: String)
private class LicenseGroup(val title: String, val asset: String, val components: List<Component>)

private val GROUPS = listOf(
    LicenseGroup("GPL-3.0", "GPL-3.0.txt", listOf(
        Component("Offline Translator", "David Ventura"),
        Component("eSpeak NG", "eSpeak NG contributors"),
    )),
    LicenseGroup("AGPL-3.0", "AGPL-3.0.txt", listOf(
        Component("MuPDF", "Artifex Software"),
    )),
    LicenseGroup("MPL-2.0", "MPL-2.0.txt", listOf(
        Component("bergamot-translator", "Bergamot project"),
        Component("adblock-rust", "Brave Software"),
        Component("UniFFI", "Mozilla"),
    )),
    LicenseGroup("Apache-2.0", "Apache-2.0.txt", listOf(
        Component("PP-OCR models", "PaddlePaddle"),
        Component("Readability.js", "Arc90 Inc, Mozilla"),
        Component("MNN", "Alibaba"),
        Component("SentencePiece", "Google"),
        Component("AndroidX, Jetpack Compose, Material Components", "Google"),
        Component("Kotlin, kotlinx.coroutines, kotlinx.serialization", "JetBrains"),
        Component("Gson", "Google"),
        Component("uCrop", "Yalantis"),
        Component("JNA (also LGPL-2.1)", "JNA contributors"),
        Component("Material Icons", "Google"),
    )),
    LicenseGroup("MIT", "MIT-translator-rs.txt", listOf(
        Component("translator-rs", "David Ventura"),
        Component("Piper / piper-rs", "Rhasspy contributors"),
    )),
)

@Composable
private fun LicensesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.licenses_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = (-4).dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        ) {
            Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.licenses_app_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = stringResource(R.string.licenses_note),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(
                onClick = { openUrl(context, "https://github.com/fuzzyfly0/SearchAndTranslate") },
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Text(stringResource(R.string.about_source_code))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            LicenseTextRow(asset = "GPL-3.0.txt")

            GROUPS.forEach { group ->
                HorizontalDivider(
                    modifier = Modifier.padding(top = 24.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 2.dp),
                )
                group.components.forEach { c ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(text = c.name, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = stringResource(R.string.licenses_copyright, c.holder),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                LicenseTextRow(asset = group.asset)
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = stringResource(R.string.licenses_trademarks),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

/** "License text" row that folds the full text open in place. */
@Composable
private fun LicenseTextRow(asset: String) {
    val context = LocalContext.current
    var open by rememberSaveable(asset) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { open = !open },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.licenses_text),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = if (open) 180f else 0f },
            )
        }
        if (open) {
            val text = remember(asset) {
                runCatching { context.assets.open(asset).bufferedReader().use { it.readText() } }.getOrDefault("")
            }
            SelectionContainer {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
