package com.friendoye.rss_reader.ui.details

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.layout.ColumnScope.gravity
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.text.style.TextAlign
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.LoadingState
import com.squareup.workflow.ui.compose.composedViewFactory
import com.squareup.workflow.ui.compose.tooling.preview
import dev.chrisbanes.accompanist.coil.CoilImage
import java.util.*


@Preview(heightDp = 200, widthDp = 300, showBackground = true)
@Composable
fun LceLayoutLoadingPreview() {
    LceLayout(loadingState = LoadingState.LOADING)
}

@Preview(heightDp = 200, widthDp = 300, showBackground = true)
@Composable
fun LceLayoutErrorPreview() {
    LceLayout(loadingState = LoadingState.FAILURE)
}

@Preview
@Composable
fun DetailsLayoutPreview() {
    DetailsScreen.preview(
        DetailsScreenState(
            loadingState = LoadingState.SUCCESS,
            title = stringResource(id = R.string.title_text),
            publicationDate = Date(),
            posterUrl = null,
            description = stringResource(id = R.string.description_text),
            onUpNavigation = {},
            onRetry = {},
            previewMode = true
        )
    )
}

val DetailsScreen = composedViewFactory<DetailsScreenState> { state, _ ->
    MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
        Column(horizontalGravity = Alignment.CenterHorizontally) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(stringResource(id = R.string.title_activity_details))
                        },
                        elevation = 4.dp,
                        navigationIcon = {
                            IconButton(onClick = state.onUpNavigation) {
                                Icon(Icons.Filled.ArrowBack)
                            }
                        }
                    )
                }
            ) {
                LceLayout(
                    loadingState = state.loadingState,
                    onRetry = state.onRetry
                ) {
                    DetailsContentLayout(state)
                }
            }
        }
    }
}

@Composable
fun DetailsContentLayout(state: DetailsScreenState) {
    VerticalScroller(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))

        Text(
            text = state.title ?: "",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.h6
        )

        state.publicationDate?.let { date ->
            val formattedDateString = Config.DATE_FORMATTER.format(date)
            Text(
                text = formattedDateString,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.End
            )
        }

        if (state.previewMode) {
            Image(
                asset = imageResource(id = R.drawable.image_post_placeholder),
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        } else {
            state.posterUrl?.let { posterUrl ->
                CoilImage(
                    data = posterUrl,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Text(
            text = state.description ?: "",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))
    }
}

@Composable
fun LceLayout(
    loadingState: LoadingState,
    onRetry: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalGravity = Alignment.CenterHorizontally
    ) {
        when (loadingState) {
            LoadingState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.gravity(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    color = colorResource(id = R.color.amber_A400)
                )
            }
            LoadingState.FAILURE -> {
                Text(
                    modifier = Modifier.gravity(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.network_error_message_text),
                    textAlign = TextAlign.Center
                )
                Button(
                    modifier = Modifier.gravity(Alignment.CenterHorizontally),
                    text = {
                        Text(stringResource(id = R.string.retry_text))
                    },
                    onClick = onRetry
                )
            }
            LoadingState.NONE -> {
                // Nothing
            }
            LoadingState.SUCCESS -> {
                content()
            }
        }
    }
}