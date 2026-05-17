package cn.binbin.tsra

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.binbin.tsra.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private enum class RootState {
        Checking,
        Ok,
        NoRoot
    }

    private val rootState = mutableStateOf(RootState.Checking)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startRootCheck()
        setContent {
            AppTheme {
                val currentRootState by rootState

                AnimatedContent(
                    targetState = currentRootState,
                    transitionSpec = {
                        (fadeIn(spring()) + scaleIn(spring(), initialScale = 0.9f))
                            .togetherWith(fadeOut(spring()) + scaleOut(spring(), targetScale = 1.1f))
                    },
                    contentKey = { it },
                    modifier = Modifier.fillMaxSize(),
                    label = "RootStateTransition"
                ) { state ->
                    when (state) {
                        RootState.Checking -> RootCheckingScreen()
                        RootState.Ok -> {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                CommandScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onRunCommand = { value -> runTouchCommand(value) },
                                    onOpenAbout = {
                                        startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                                    }
                                )
                            }
                        }
                        RootState.NoRoot -> {
                            RootRequiredScreen(
                                onRetry = {
                                    rootState.value = RootState.Checking
                                    startRootCheck()
                                },
                                onSkip = { rootState.value = RootState.Ok }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun runTouchCommand(value: Int) {
        TouchRunner.run(this, value)
    }

    private fun startRootCheck() {
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                val result = process.waitFor()
                rootState.value = if (result == 0) RootState.Ok else RootState.NoRoot
            } catch (_: Exception) {
                rootState.value = RootState.NoRoot
            }
        }.start()
    }
}

@Composable
fun CommandScreen(
    modifier: Modifier = Modifier,
    onRunCommand: (Int) -> Unit,
    onOpenAbout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onRunCommand(0) }) { Text("125") }
            Button(onClick = { onRunCommand(180) }) { Text("180") }
            Button(onClick = { onRunCommand(240) }) { Text("240") }
            Button(onClick = { onRunCommand(360) }) { Text("360") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenAbout
        ) { Text(text = stringResource(id = R.string.about_title)) }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        CommandScreen(onRunCommand = {}, onOpenAbout = {})
    }
}

@Composable
fun RootCheckingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.root_checking),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun RootRequiredScreen(onRetry: () -> Unit, onSkip: () -> Unit) {
    var titleTapCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.root_required_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.clickable {
                titleTapCount++
                if (titleTapCount >= 5) {
                    titleTapCount = 0
                    onSkip()
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.root_required_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.root_retry_button))
        }
    }
}
