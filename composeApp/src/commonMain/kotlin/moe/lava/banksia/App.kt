package moe.lava.banksia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import moe.lava.banksia.api.ptv.PtvService
import moe.lava.banksia.native.maps.Maps
import moe.lava.banksia.ui.Searcher
import org.jetbrains.compose.ui.tooling.preview.Preview

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

    var searchTextState by remember { mutableStateOf("") }
    var searchExpandedState by remember { mutableStateOf(false) }

    MaterialTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = { Box(modifier = Modifier) },
        ) {
            Maps(
                modifier = Modifier.fillMaxSize(),
                sheetState = scaffoldState.bottomSheetState,
            )
            Searcher(
                ptvService = PtvService(),
                expanded = searchExpandedState,
                onExpandedChange = { searchExpandedState = it },
                text = searchTextState,
                onTextChange = { searchTextState = it },
                onRouteChange = {}
            )
        }
    }
}
