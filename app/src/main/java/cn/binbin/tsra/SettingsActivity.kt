package cn.binbin.tsra

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.binbin.tsra.lumo.AppTheme as LumoAppTheme
import cn.binbin.tsra.lumo.components.Button as LumoButton
import cn.binbin.tsra.lumo.components.Text as LumoText
import cn.binbin.tsra.lumo.components.topbar.TopBar as LumoTopBar
import cn.binbin.tsra.lumo.components.Scaffold as LumoScaffold
import cn.binbin.tsra.lumo.components.AlertDialog as LumoAlertDialog

class SettingsActivity : ComponentActivity() {

    private data class UpdateInfo(
        val versionName: String,
        val changelog: String,
        val apkUrl: String?
    )

    private val updateInfoState = mutableStateOf<UpdateInfo?>(null)
    private val updateStatusTextState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumoAppTheme {
                var hideFromRecents by remember { mutableStateOf(loadHideFromRecents()) }
                var autoGame360 by remember { mutableStateOf(loadAutoGame360()) }
                val activity = this@SettingsActivity
                val updateInfo = updateInfoState.value
                val updateStatusText = updateStatusTextState.value
                LumoScaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        LumoTopBar {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                LumoButton(
                                    text = "←",
                                    modifier = Modifier,
                                    onClick = { activity.finish() }
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                                LumoText(text = stringResource(id = R.string.settings_title))
                            }
                        }
                    }
                ) { innerPadding ->
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        hideFromRecents = hideFromRecents,
                        onHideFromRecentsChange = { enabled ->
                            hideFromRecents = enabled
                            saveHideFromRecents(enabled)
                        },
                        autoGame360 = autoGame360,
                        onAutoGame360Change = { enabled ->
                            autoGame360 = enabled
                            saveAutoGame360(enabled)
                            if (enabled) {
                                startGameMonitorService()
                            } else {
                                stopGameMonitorService()
                            }
                        },
                        updateStatusText = updateStatusText,
                        onCheckUpdate = {
                            checkForUpdate()
                        },
                        onOpenAbout = {
                            activity.startActivity(Intent(activity, AboutActivity::class.java))
                        },
                        onBack = { activity.finish() }
                    )

                    if (updateInfo != null) {
                        LumoAlertDialog(
                            onDismissRequest = { updateInfoState.value = null },
                            onConfirmClick = {
                                openUpdateUrl(updateInfo.apkUrl)
                                updateInfoState.value = null
                            },
                            title = stringResource(id = R.string.update_available, updateInfo.versionName),
                            text = updateInfo.changelog,
                            confirmButtonText = stringResource(id = R.string.update_download_button),
                            dismissButtonText = stringResource(id = R.string.update_cancel_button)
                        )
                    }
                }
            }
        }
    }

    private fun loadHideFromRecents(): Boolean {
        val sp = getSharedPreferences("tsra_settings", MODE_PRIVATE)
        return sp.getBoolean("hide_from_recents", false)
    }

    private fun saveHideFromRecents(enabled: Boolean) {
        val sp = getSharedPreferences("tsra_settings", MODE_PRIVATE)
        sp.edit().putBoolean("hide_from_recents", enabled).apply()
    }

    private fun loadAutoGame360(): Boolean {
        val sp = getSharedPreferences("tsra_settings", MODE_PRIVATE)
        return sp.getBoolean("auto_game_360", false)
    }

    private fun saveAutoGame360(enabled: Boolean) {
        val sp = getSharedPreferences("tsra_settings", MODE_PRIVATE)
        sp.edit().putBoolean("auto_game_360", enabled).apply()
    }

    private fun checkForUpdate() {
        val context = this
        Thread {
            try {
                val url = java.net.URL("https://blog.wzwzx.cn/static-res/tsra.json")
                val connection = url.openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val stream = connection.getInputStream()
                val text = stream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(text)
                val latestVersionName = json.optString("latestVersionName", "")
                val latestVersionCode = json.optInt("latestVersionCode", -1)
                val changelog = json.optString("changelog", "")
                val apkUrl = json.optString("apkUrl", null)?.takeIf { it.isNotBlank() }

                val pm = context.packageManager
                val packageInfo = pm.getPackageInfo(context.packageName, 0)
                val currentVersionName = packageInfo.versionName ?: ""
                val currentVersionCode = packageInfo.longVersionCode.toInt()

                val hasUpdate = when {
                    latestVersionCode > 0 && latestVersionCode > currentVersionCode -> true
                    latestVersionName.isNotEmpty() && latestVersionName != currentVersionName -> true
                    else -> false
                }

                if (hasUpdate) {
                    runOnUiThread {
                        // 记录最新版本号，用于设置页右侧显示
                        updateStatusTextState.value = latestVersionName
                        updateInfoState.value = UpdateInfo(
                            versionName = latestVersionName,
                            changelog = changelog,
                            apkUrl = apkUrl
                        )
                    }
                } else {
                    runOnUiThread {
                        // 没有更新时，状态文字显示“无更新”
                        updateStatusTextState.value = null
                        val message = context.getString(R.string.update_not_available)
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.update_check_failed, e.message ?: ""),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun openUpdateUrl(apkUrl: String?) {
        if (apkUrl.isNullOrBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
            startActivity(intent)
        } catch (_: Exception) {
            // ignore
        }
    }

    private fun startGameMonitorService() {
        val intent = Intent(this, GameMonitorService::class.java)
        startService(intent)
    }

    private fun stopGameMonitorService() {
        val intent = Intent(this, GameMonitorService::class.java)
        stopService(intent)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    LumoAppTheme {
        SettingsScreen(
            hideFromRecents = false,
            onHideFromRecentsChange = {},
            autoGame360 = false,
            onAutoGame360Change = {},
            onCheckUpdate = {},
            onOpenAbout = {},
            onBack = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
