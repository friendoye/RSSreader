package com.friendoye.rss_reader.ui.dialogs.sourceslist

import android.util.Log
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.material.Checkbox
import androidx.ui.material.TextButton
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.ui.RssReaderAppTheme
import com.friendoye.rss_reader.domain.getActiveSources
import com.friendoye.rss_reader.utils.compose.AlertDialogButtonLayout
import com.friendoye.rss_reader.utils.compose.MultiChoiceAlertDialog

private data class SourcesListDialogState(
    val items: Map<String, Boolean>,
    val canApplyChanges: Boolean
)

@Composable
fun SourcesListDialogScreenLayout(
    onClose: () -> Unit,
    onOptionsUpdated: (Map<String, Boolean>) -> Unit
) {
    val onCloseCallback = remember { onClose }
    var mSourcesDialogState by state {
        SourcesListDialogScreenState(
            isShowing = false,
            options = emptyMap(),
            onOptionsUpdated = onOptionsUpdated
        )
    }

    onActive {
        mSourcesDialogState = mSourcesDialogState.copy(
            isShowing = true,
            options = getActiveSources()
        )
    }

    if (mSourcesDialogState.isShowing) {
        SourcesListDialogScreen(
            sourceOptions = mSourcesDialogState.options,
            onApplySourceOptionsRequest = { newItems ->
                mSourcesDialogState = mSourcesDialogState.copy(
                    isShowing = false,
                    options = newItems
                )
                mSourcesDialogState.onOptionsUpdated(newItems)
                onCloseCallback()
            },
            onCloseRequest = {
                mSourcesDialogState = mSourcesDialogState.copy(
                    isShowing = false
                )
                onCloseCallback()
            }
        )
    }
}

@Composable
fun SourcesListDialogScreen(
    sourceOptions: Map<String, Boolean>,
    onApplySourceOptionsRequest: (Map<String, Boolean>) -> Unit,
    onCloseRequest: () -> Unit
) {
    var internalState by state {
        SourcesListDialogState(
            items = sourceOptions,
            canApplyChanges = sourceOptions.any { (_, selected) -> selected })
    }

    val onSourceOptionCheckChange: (String, Boolean) -> Unit = { text, isChecked ->
        val newItems = internalState.items + (text to isChecked)
        internalState = internalState.copy(
            items = newItems,
            canApplyChanges = newItems.any { (_, selected) -> selected }
        )
        Log.i("Test", internalState.toString())
    }

    MultiChoiceAlertDialog(
        onCloseRequest = onCloseRequest,
        title = {
            Text(text = stringResource(id = R.string.sources_picker_title))
        },
        customContent = {
            internalState.items.forEach { (text, isChecked) ->
                Row(
                    modifier = Modifier.height(48.dp).fillMaxWidth()
                        .clickable(onClick = { onSourceOptionCheckChange(text, !isChecked) }),
                    verticalGravity = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(20.dp))
                    // TODO: Determine how to ripple whole item, when any child view was pressed
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { viewIsChecked -> onSourceOptionCheckChange(text, viewIsChecked) }
                    )
                    Text(
                        text = text,
                        modifier = Modifier.padding(start = 8.dp)
                            .clickable(onClick = {}, enabled = false)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        },
        buttons = {
            AlertDialogButtonLayout(
                // TODO: Do something with Locale
                confirmButton = {
                    TextButton(
                        text = {
                            Text(text = stringResource(id = R.string.ok_text).toUpperCase())
                        },
                        enabled = internalState.canApplyChanges,
                        onClick = { onApplySourceOptionsRequest(internalState.items) }
                    )
                },
                dismissButton = {
                    TextButton(
                        text = {
                            Text(text = stringResource(id = R.string.cancel_text).toUpperCase())
                        },
                        onClick = onCloseRequest
                    )
                },
                buttonLayout = AlertDialogButtonLayout.SideBySide
            )
        }
    )
}

@Preview(widthDp = 300, heightDp = 600)
@Composable
fun SourcesListDialogScreenPreview() {
    var items by state {
        mapOf(
            "https://www.onliner.by/feed" to true,
            "https://news.tut.by/rss/all.rss" to false
        )
    }
    var isShowing by state { true }

    if (!isShowing) return

    RssReaderAppTheme(darkTheme = false) {
        SourcesListDialogScreen(
            sourceOptions = items,
            onApplySourceOptionsRequest = { newItems ->
                items = newItems
                isShowing = false
            },
            onCloseRequest = {
                isShowing = false
            }
        )
    }
}