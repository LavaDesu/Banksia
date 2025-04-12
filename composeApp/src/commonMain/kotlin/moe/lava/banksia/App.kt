package moe.lava.banksia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import banksia.composeapp.generated.resources.Res
import banksia.composeapp.generated.resources.compose_multiplatform
import moe.lava.banksia.native.maps.Maps

@Composable
@Preview
fun App() {
    MaterialTheme {
        Scaffold {
            Maps(modifier = Modifier.fillMaxSize())
        }
    }
}