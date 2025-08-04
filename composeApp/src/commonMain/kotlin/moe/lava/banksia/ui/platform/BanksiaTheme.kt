package moe.lava.banksia.ui.platform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BanksiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable (() -> Unit)
) {
    MaterialTheme(
        colorScheme = BanksiaTheme.colors(darkTheme, dynamicColor),
        content = content
    )
}

@Composable
expect fun BanksiaTheme.colors(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme

object BanksiaTheme {
    val colors: ColorScheme
        @Composable
        get() = colors(isSystemInDarkTheme(), true)
}
