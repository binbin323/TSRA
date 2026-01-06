package cn.binbin.tsra

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.binbin.tsra.lumo.AppTheme as LumoAppTheme
import cn.binbin.tsra.lumo.components.Button as LumoButton
import cn.binbin.tsra.lumo.components.Switch as LumoSwitch
import cn.binbin.tsra.lumo.components.Text as LumoText
import cn.binbin.tsra.lumo.components.HorizontalDivider as LumoHorizontalDivider
import cn.binbin.tsra.lumo.components.Scaffold as LumoScaffold
import cn.binbin.tsra.lumo.components.topbar.TopBar as LumoTopBar
import cn.binbin.tsra.lumo.components.AlertDialog as LumoAlertDialog

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
            LumoAppTheme {
                val activity = this@MainActivity
                val currentRootState by rootState

                when (currentRootState) {
                    RootState.Checking -> {
                        RootCheckingScreen()
                    }
                    RootState.Ok -> {
                        LumoScaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                LumoTopBar {
                                    LumoText(text = stringResource(id = R.string.app_name))
                                }
                            }
                        ) { innerPadding ->
                            CommandScreen(
                                modifier = Modifier.padding(innerPadding),
                                onRunCommand = { value -> runTouchCommand(value) },
                                onOpenSettings = {
                                    activity.startActivity(Intent(activity, SettingsActivity::class.java))
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
                            onSkip = {
                                rootState.value = RootState.Ok
                            }
                        )
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
                val success = result == 0
                rootState.value = if (success) RootState.Ok else RootState.NoRoot
            } catch (_: Exception) {
                rootState.value = RootState.NoRoot
            }
        }.start()
    }

    override fun onStop() {
        super.onStop()

        val sp = getSharedPreferences("tsra_settings", MODE_PRIVATE)
        val hideFromRecents = sp.getBoolean("hide_from_recents", false)
        val am = getSystemService(ACTIVITY_SERVICE) as? ActivityManager
        am?.appTasks?.forEach { task ->
            task.setExcludeFromRecents(hideFromRecents)
        }
    }
}

@Composable
fun CommandScreen(
    modifier: Modifier = Modifier,
    onRunCommand: (Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LumoButton(text = "125", onClick = { onRunCommand(0) })
            LumoButton(text = "180", onClick = { onRunCommand(180) })
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LumoButton(text = "240", onClick = { onRunCommand(240) })
            LumoButton(text = "360", onClick = { onRunCommand(360) })
        }

        LumoButton(
            text = stringResource(id = R.string.settings_open),
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenSettings
        )
    }
}
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    hideFromRecents: Boolean,
    onHideFromRecentsChange: (Boolean) -> Unit,
    autoGame360: Boolean,
    onAutoGame360Change: (Boolean) -> Unit,
    updateStatusText: String? = null,
    onCheckUpdate: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 列表项：在最近任务中隐藏
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LumoText(text = stringResource(id = R.string.settings_hide_recents))
                Spacer(modifier = Modifier.height(4.dp))
                LumoText(text = stringResource(id = R.string.settings_hide_recents_desc))
            }
            LumoSwitch(
                checked = hideFromRecents,
                onCheckedChange = onHideFromRecentsChange
            )
        }

        LumoHorizontalDivider()

        // 列表项：自动为指定游戏切换 360Hz
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LumoText(text = stringResource(id = R.string.settings_auto_game_360))
                Spacer(modifier = Modifier.height(4.dp))
                LumoText(text = stringResource(id = R.string.settings_auto_game_360_desc))
            }
            LumoSwitch(
                checked = autoGame360,
                onCheckedChange = onAutoGame360Change
            )
        }

        LumoHorizontalDivider()

        // 列表项：检测更新（右侧为“无更新”文本按钮）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LumoText(text = stringResource(id = R.string.settings_check_update))
            }
            LumoText(
                text = updateStatusText ?: stringResource(id = R.string.settings_check_update_status_no_update),
                modifier = Modifier.clickable(onClick = onCheckUpdate)
            )
        }

        LumoHorizontalDivider()

        // 列表项：关于
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenAbout)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            LumoText(
                text = stringResource(id = R.string.settings_about),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    versionName: String,
    onBack: () -> Unit
) {
    var showPrincipleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // 顶部栏，左侧为返回图标按钮，标题为“关于 TSRA”
        LumoTopBar {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LumoButton(
                    text = "←",
                    modifier = Modifier,
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(8.dp))
                LumoText(text = stringResource(id = R.string.about_title))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.height(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (versionName.isNotEmpty()) {
                    LumoText(text = stringResource(id = R.string.about_version, versionName))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                LumoText(text = stringResource(id = R.string.about_title))
                Spacer(modifier = Modifier.height(8.dp))
                LumoText(text = stringResource(id = R.string.about_author))
                Spacer(modifier = Modifier.height(8.dp))
                LumoText(text = stringResource(id = R.string.about_content))
            }
            Spacer(modifier = Modifier.height(16.dp))
            LumoButton(
                text = stringResource(id = R.string.about_principle_button),
                modifier = Modifier.fillMaxWidth(),
                onClick = { showPrincipleDialog = true }
            )
        }

        if (showPrincipleDialog) {
            LumoAlertDialog(
                onDismissRequest = { showPrincipleDialog = false },
                onConfirmClick = { showPrincipleDialog = false },
                title = stringResource(id = R.string.about_principle_title),
                text = stringResource(id = R.string.about_principle_content),
                confirmButtonText = stringResource(id = R.string.dialog_ok),
                dismissButtonText = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LumoAppTheme {
        CommandScreen(onRunCommand = {}, onOpenSettings = {})
    }
}

@Composable
fun RootCheckingScreen() {
    LumoScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LumoTopBar {
                LumoText(text = stringResource(id = R.string.app_name))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LumoText(text = stringResource(id = R.string.root_checking))
        }
    }
}

@Composable
fun RootRequiredScreen(onRetry: () -> Unit, onSkip: () -> Unit) {
    LumoScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LumoTopBar {
                LumoText(text = stringResource(id = R.string.app_name))
            }
        }
    ) { innerPadding ->
        var titleTapCount by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LumoText(
                text = stringResource(id = R.string.root_required_title),
                modifier = Modifier.clickable {
                    titleTapCount++
                    if (titleTapCount >= 5) {
                        titleTapCount = 0
                        onSkip()
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            LumoText(text = stringResource(id = R.string.root_required_message))
            Spacer(modifier = Modifier.height(16.dp))
            LumoButton(
                text = stringResource(id = R.string.root_retry_button),
                onClick = onRetry
            )
        }
    }
}