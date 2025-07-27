package moe.lava.banksia.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp

@Composable
fun StopInfoPanel(
    stopState: StopState,
    onPeekHeightChange: (Dp) -> Unit,
) {
    val localDensity = LocalDensity.current
    val (stop, departures) = stopState

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .heightIn(max = 250.dp)
            .verticalScroll(rememberScrollState())
            .onSizeChanged {
                onPeekHeightChange(with(localDensity) { it.height.toDp().coerceAtMost(250.dp) })
            }
    ) {
        Box {
            Column(Modifier.fillMaxWidth()) {
                val split = stop.stopName.split("/")
                val mainName = split[0]
                val subName = split.getOrNull(1)
                Text(
                    mainName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                if (subName != null)
                    Text(
                        "/ $subName",
                        modifier = Modifier.padding(start = 5.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    )
                departures?.let {
                    Spacer(Modifier.height(5.dp))
                    it.forEach { (name, formatted) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(formatted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 5.dp))
                        }
                    }
                }
            }
            if (departures == null)
                CircularProgressIndicator(
                    modifier = Modifier.width(32.dp).align(Alignment.CenterEnd)
                )
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeContent))
    }
}
