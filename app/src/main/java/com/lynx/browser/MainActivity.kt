package com.lynx.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lynx.browser.ui.theme.LynxTheme
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.support.ktx.kotlin.isUrl

class MainActivity : ComponentActivity() {
    private lateinit var engine: Engine
    private lateinit var engineSession: EngineSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the GeckoEngine
        engine = GeckoEngine(this)
        engineSession = engine.createSession()

        setContent {
            LynxTheme {
                BrowserScreen(engine, engineSession)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(engine: Engine, engineSession: EngineSession) {
    var urlText by remember { mutableStateOf("") }
    var engineView by remember { mutableStateOf<EngineView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar with Tabs
        ScrollableTabRow(
            selectedTabIndex = 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = true,
                onClick = { /* Handle tab click */ },
                text = { Text("New Tab") }
            )
        }

        // Browser Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { context ->
                    engine.createView(context).asView().also {
                        engineView = engine.createView(context)
                        engineView?.render(engineSession)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Navigation Bar
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = { engineSession.goBack() },
                    enabled = true
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                IconButton(
                    onClick = { engineSession.goForward() },
                    enabled = true
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                }

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    placeholder = { Text("Enter URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            val url = if (urlText.isUrl()) {
                                if (urlText.startsWith("http://") || urlText.startsWith("https://")) {
                                    urlText
                                } else {
                                    "https://$urlText"
                                }
                            } else {
                                "https://duckduckgo.com/?q=$urlText"
                            }
                            engineSession.loadUrl(url)
                        }
                    )
                )
            }
        }
    }

    // Load initial URL
    LaunchedEffect(engineSession) {
        engineSession.loadUrl("https://duckduckgo.com")
    }
}