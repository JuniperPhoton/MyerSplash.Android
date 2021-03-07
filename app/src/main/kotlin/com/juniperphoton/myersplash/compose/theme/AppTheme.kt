package com.juniperphoton.myersplash.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @author dengweichao
 * @since 2021-03-07
 */
private val LightColorPalette = lightColors(
    primary = Color(0xFFFFFF),
    primaryVariant = Color(0xFF000000),
    secondary = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000)
)

private val DarkColorPalette = darkColors(
    primary = Color(0xFF181818),
    primaryVariant = Color(0xFF181818),
    secondary = Color(0xFFFFFFFF),
    surface = Color(0xFF181818),
    onPrimary = Color(0xFFFFFFFF)
)

private val shapes = Shapes(
    small = RoundedCornerShape(percent = 50),
    medium = RoundedCornerShape(size = 4.dp),
    large = RoundedCornerShape(size = 4.dp),
)

private val typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)


@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}