package cn.binbin.tsra.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray200,
    onPrimaryContainer = Black,
    secondary = Gray400,
    onSecondary = Black,
    secondaryContainer = Gray200,
    onSecondaryContainer = Black,
    tertiary = Blue900,
    onTertiary = White,
    tertiaryContainer = Gray100,
    onTertiaryContainer = Black,
    error = Red600,
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    outlineVariant = Gray200,
    inverseSurface = Gray900,
    inverseOnSurface = White,
    inversePrimary = Gray400,
    surfaceTint = Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray800,
    onPrimaryContainer = White,
    secondary = Gray400,
    onSecondary = White,
    secondaryContainer = Gray800,
    onSecondaryContainer = White,
    tertiary = Blue300,
    onTertiary = Black,
    tertiaryContainer = Gray800,
    onTertiaryContainer = White,
    error = Red400,
    onError = Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray700,
    outlineVariant = Gray800,
    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = Gray700,
    surfaceTint = White,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
