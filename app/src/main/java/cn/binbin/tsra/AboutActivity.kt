package cn.binbin.tsra

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.binbin.tsra.lumo.components.Text as LumoText
import cn.binbin.tsra.lumo.components.topbar.TopBar as LumoTopBar
import cn.binbin.tsra.lumo.AppTheme as LumoAppTheme

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumoAppTheme {
                AboutScreen(
                    modifier = Modifier.fillMaxSize(),
                    versionName = getVersionName(),
                    onBack = { finish() }
                )
            }
        }
    }

    private fun getVersionName(): String {
        return try {
            val pm = packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    LumoAppTheme {
        AboutScreen(modifier = Modifier.fillMaxSize(), versionName = "1.0.0", onBack = {})
    }
}
