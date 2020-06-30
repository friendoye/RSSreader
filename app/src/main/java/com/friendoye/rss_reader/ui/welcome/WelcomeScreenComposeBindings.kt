package com.friendoye.rss_reader.ui.welcome

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.currentTextStyle
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.Surface
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.text.font.FontWeight
import androidx.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.utils.LoadingState
import com.squareup.workflow.ui.compose.composedViewFactory
import com.squareup.workflow.ui.compose.tooling.preview

@Preview(widthDp = 300, heightDp = 600)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen.preview(
        WelcomeScreenState(
            retry = {},
            loadingState = LoadingState.FAILURE
        )
    )
}

val WelcomeScreen = composedViewFactory<WelcomeScreenState> { state, _ ->
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = { state.retry() }, indication = null),
        color = colorResource(id = R.color.orange_500)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalGravity = Alignment.CenterHorizontally
        ) {
            when (state.loadingState) {
                LoadingState.LOADING,
                LoadingState.SUCCESS,
                LoadingState.NONE -> {
                    Text(
                        text = stringResource(id = R.string.wait_text),
                        fontSize = TextUnit.Sp(46),
                        color = colorResource(id = R.color.amber_A400),
                        textAlign = TextAlign.Center,
                        style = currentTextStyle().copy(fontWeight = FontWeight.Bold)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.gravity(Alignment.CenterHorizontally),
                        color = colorResource(id = R.color.amber_A400)
                    )
                }
                LoadingState.FAILURE -> {
                    Column {
                        Image(
                            modifier = Modifier.gravity(Alignment.CenterHorizontally),
                            asset = imageResource(id = R.drawable.ic_warning_grey_300_36dp)
                        )
                        Text(
                            text = stringResource(id = R.string.welcome_network_error_message_text),
                            color = colorResource(id = R.color.amber_A400),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}