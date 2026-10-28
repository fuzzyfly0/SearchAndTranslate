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


package com.fuzzyfly0.searchandtranslate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fuzzyfly0.searchandtranslate.onboarding.OnboardingScreen
import com.fuzzyfly0.searchandtranslate.ui.theme.SearchAndTranslateTheme
import com.fuzzyfly0.searchandtranslate.ui.wobblepulldown.WobbleDeviceInfoActivity
import com.fuzzyfly0.searchandtranslate.ui.wobblepulldown.WobbleHomeScreen
import com.fuzzyfly0.searchandtranslate.ui.wobblepulldown.isA11yGranted
import com.fuzzyfly0.searchandtranslate.ui.wobblepulldown.isAssistantGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingSettingsPage by mutableStateOf<String?>(null)
    private var navTick by mutableStateOf(0)
    private var forceOnboarding by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingSettingsPage = intent.getStringExtra(EXTRA_SETTINGS_PAGE)
        forceOnboarding = intent.getBooleanExtra(EXTRA_SHOW_ONBOARDING, false)
        Toast.makeText(this, getString(R.string.toast_getting_started), Toast.LENGTH_LONG).show()
        setContent {
            SearchAndTranslateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val appPrefs = remember {
                        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    }
                    var showOnboarding by remember {
                        mutableStateOf(
                            forceOnboarding || !appPrefs.getBoolean("onboarding_completed", false)
                        )
                    }
                    if (showOnboarding) {
                        OnboardingScreen(
                            onFinish = {
                                appPrefs.edit().putBoolean("onboarding_completed", true).apply()
                                showOnboarding = false
                            }
                        )
                    } else {
                        var currentScreen by remember {
                            mutableStateOf(if (pendingSettingsPage != null) "settings" else "home")
                        }
                        LaunchedEffect(navTick) {
                            if (navTick > 0) {
                                currentScreen = if (pendingSettingsPage != null) "settings" else "home"
                            }
                        }
                        androidx.compose.animation.Crossfade(targetState = currentScreen) { screen ->
                            when (screen) {
                                "settings" -> key(navTick) {
                                    com.fuzzyfly0.searchandtranslate.ui.OverlaySettingsScreen(
                                        onBack = {
                                            pendingSettingsPage = null
                                            currentScreen = "home"
                                        },
                                        initialPage = pendingSettingsPage,
                                    )
                                }
                                // Status & permissions: a regular settings page inside the app
                                "status" -> {
                                    androidx.activity.compose.BackHandler { currentScreen = "home" }
                                    com.fuzzyfly0.searchandtranslate.ui.wobblepulldown.WobbleAppInfoScreen(
                                        onBackClick = { currentScreen = "home" }
                                    )
                                }
                                else -> {
                                    var wobbleTick by remember { mutableStateOf(0) }
                                    var wizardBackdrop by remember { mutableStateOf(false) }
                                    val wizardScope = rememberCoroutineScope()
                                    val wizardLauncher = rememberLauncherForActivityResult(
                                        ActivityResultContracts.StartActivityForResult()
                                    ) { result ->
                                        if (result.resultCode == Activity.RESULT_OK) {
                                            wobbleTick++
                                        }
                                        wizardScope.launch {
                                            delay(400)
                                            wizardBackdrop = false
                                        }
                                    }
                                    LaunchedEffect(Unit) {
                                        val granted = isA11yGranted(context) || isAssistantGranted(context)
                                        if (!granted) {
                                            wizardBackdrop = true
                                            val intent = Intent(context, WobbleDeviceInfoActivity::class.java)
                                                .putExtra(WobbleDeviceInfoActivity.EXTRA_SETUP_WIZARD, true)
                                            val options = androidx.core.app.ActivityOptionsCompat.makeCustomAnimation(
                                                context,
                                                R.anim.anim_slide_in_top,
                                                R.anim.anim_stay,
                                            )
                                            wizardLauncher.launch(intent, options)
                                        }
                                    }
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        WobbleHomeScreen(
                                            onOpenDeviceInfo = { currentScreen = "status" },
                                            onOpenSettings = { page ->
                                                pendingSettingsPage = page
                                                currentScreen = "settings"
                                            },
                                            wobbleEvent = wobbleTick,
                                            contentPadding = WindowInsets.systemBars.asPaddingValues(),
                                        )
                                        AnimatedVisibility(
                                            visible = wizardBackdrop,
                                            enter = EnterTransition.None,
                                            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.10f)),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingSettingsPage = intent.getStringExtra(EXTRA_SETTINGS_PAGE)
        navTick = navTick + 1
    }

    companion object {
        const val EXTRA_SETTINGS_PAGE = "settings_page"
        /** Dev/testing: re-show the onboarding flow without clearing app data. */
        const val EXTRA_SHOW_ONBOARDING = "show_onboarding"
    }
}

// Helper Functions
fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val expectedComponentName = android.content.ComponentName(context, SearchAndTranslateAccessibilityService::class.java)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    
    val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledComponent = android.content.ComponentName.unflattenFromString(componentNameString)
        if (enabledComponent != null && enabledComponent == expectedComponentName)
            return true
    }
    return false
}

fun isDefaultAssistant(context: android.content.Context): Boolean {
    // Basic check: triggers the settings intent, but actual "is default" check is complex
    // on some Android versions. For simplified UI, we might relay on user return, 
    // or use RoleManager on Android 10+. 
    // For now, let's keep it simple or check specific secure settings if possible.
    // A reliable check is to see if we are arguably the voice interaction service.
    
    val assistant = Settings.Secure.getString(context.contentResolver, "voice_interaction_service")
    val component = android.content.ComponentName(context, SearchAndTranslateVoiceService::class.java)
    val myComponentString = component.flattenToString()
    
    // Check if our VoiceInteractionService is the one currently set as default
    return assistant == myComponentString
}

