package com.friendoye.rss_reader.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.material.Checkbox
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TextButton
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.R
import com.friendoye.rss_reader.compose.AlertDialogButtonLayout
import com.friendoye.rss_reader.compose.MultiChoiceAlertDialog
import com.friendoye.rss_reader.utils.Config
import com.friendoye.rss_reader.utils.DataKeeper
import com.friendoye.rss_reader.utils.Packer
import java.util.*

/**
 * Dialog for picking active RSS sources.
 */
class SourcesListDialogFragment : AppCompatDialogFragment(),
    OnMultiChoiceClickListener, DialogInterface.OnClickListener {
    private var mSources: Array<String> = emptyArray()
    private var mSelectedSources: BooleanArray? = booleanArrayOf()
    private var mCallback: OnSourcesChangedListener? = null

    interface OnSourcesChangedListener {
        fun onSourcesChanged()
    }

    override fun onAttach(context: Activity) {
        super.onAttach(context)
        mCallback = try {
            context as OnSourcesChangedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() + " must " +
                        "implement OnSourcesChangedListener!"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSources = resources.getStringArray(R.array.rss_sources_array)
        val savedPack = DataKeeper.restoreString(
            activity,
            Config.SOURCES_STRING_KEY
        )
        if (savedInstanceState == null) {
            mSelectedSources = BooleanArray(mSources.size)
            if (savedPack != null) {
                val activeSources: MutableSet<String> =
                    TreeSet()
                activeSources.addAll(
                    Arrays.asList(
                        *Packer.unpackAsStringArray(
                            savedPack
                        )
                    )
                )
                for (i in mSources.indices) {
                    mSelectedSources!![i] = activeSources.contains(mSources[i])
                }
            }
        } else {
            mSelectedSources =
                savedInstanceState.getBooleanArray(SELECTED_SOURCES_KEY)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =
            AlertDialog.Builder(requireActivity(), R.style.AppTheme_Dialog)
        builder.setTitle(R.string.sources_picker_title)
            .setMultiChoiceItems(mSources, mSelectedSources, this)
            .setPositiveButton(R.string.ok_text, this)
            .setNegativeButton(R.string.cancel_text, null)
            .create()
        return builder.create()
    }

    override fun onStart() {
        super.onStart()

        // Dialog shouldn't be dismissed, if there are no selected sources.
        // For that purpose here was added this listener.
        val dialog =
            dialog as AlertDialog?
        if (dialog != null) {
            val positiveButton =
                dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener(View.OnClickListener {
                val context: Context? = activity
                val savedPack = DataKeeper.restoreString(
                    context,
                    Config.SOURCES_STRING_KEY
                )
                val pack = Packer.packCollection(
                    mSources,
                    mSelectedSources
                )
                if (pack == null) {
                    Toast.makeText(
                        activity, R.string.no_sources_forbidden_text,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnClickListener
                } else if (savedPack == null || savedPack != pack) {
                    DataKeeper.saveString(
                        context,
                        Config.SOURCES_STRING_KEY,
                        pack
                    )
                    if (mCallback != null) {
                        mCallback!!.onSourcesChanged()
                    }
                }
                dialog.dismiss()
            })
        }
    }

    override fun onClick(
        dialog: DialogInterface,
        which: Int,
        isChecked: Boolean
    ) {
        mSelectedSources!![which] = isChecked
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        // Implemented only for one purpose: positive button creation.
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(
            SELECTED_SOURCES_KEY,
            mSelectedSources
        )
    }

    override fun onDetach() {
        super.onDetach()
        mCallback = null
    }

    companion object {
        const val SELECTED_SOURCES_KEY = "selected_s_key"
    }
}

private data class SourcesListDialogState(
    val items: Map<String, Boolean>,
    val canApplyChanges: Boolean
)

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

    MaterialTheme(colors = LIGHT_COLOR_PALETTE) {
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