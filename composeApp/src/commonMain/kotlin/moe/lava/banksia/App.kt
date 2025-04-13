package moe.lava.banksia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import banksia.composeapp.generated.resources.Res
import banksia.composeapp.generated.resources.compose_multiplatform
import moe.lava.banksia.native.maps.Maps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )
    MaterialTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = { Box(modifier = Modifier) },
        ) {
            Maps(
                modifier = Modifier.fillMaxSize(),
                sheetState = scaffoldState.bottomSheetState,
            )
        }
    }
}