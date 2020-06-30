package com.friendoye.rss_reader.compose

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.semantics.semantics
import androidx.ui.foundation.*
import androidx.ui.foundation.selection.selectable
import androidx.ui.foundation.selection.toggleable
import androidx.ui.graphics.Shape
import androidx.ui.layout.*
import androidx.ui.layout.ColumnScope.gravity
import androidx.ui.layout.RowScope.weight
import androidx.ui.material.*
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.friendoye.rss_reader.LIGHT_COLOR_PALETTE
import com.friendoye.rss_reader.R

/** @see {@link androidx.ui.material.AlertDialog} */

@Composable
fun MultiChoiceAlertDialog(
    onCloseRequest: () -> Unit,
    title: (@Composable ColumnScope.() -> Unit)? = null,
    text: (@Composable ColumnScope.() -> Unit)? = null,
    customContent: (@Composable ColumnScope.() -> Unit)? = null,
    buttons: (@Composable ColumnScope.() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium
) {
    // TODO: Find a cleaner way to pass the properties of the MaterialTheme
    val currentColors = MaterialTheme.colors
    val currentTypography = MaterialTheme.typography
    Dialog(onCloseRequest = onCloseRequest) {
        MaterialTheme(colors = currentColors, typography = currentTypography) {
            Surface(
                shape = shape
            ) {
                val emphasisLevels = EmphasisAmbient.current
                Column {
                    if (title != null) {
                        Box(TitlePadding.gravity(Alignment.Start)) {
                            ProvideEmphasis(emphasisLevels.high) {
                                val textStyle = MaterialTheme.typography.h6
                                ProvideTextStyle(textStyle) {
                                    title()
                                }
                            }
                        }
                    } else {
                        // TODO(b/138924683): Temporary until padding for the Text's
                        //  baseline
                        Spacer(NoTitleExtraHeight)
                    }

                    if (text != null) {
                        Box(TextPadding.gravity(Alignment.Start)) {
                            ProvideEmphasis(emphasisLevels.medium) {
                                val textStyle = MaterialTheme.typography.body1
                                ProvideTextStyle(textStyle) {
                                    text()
                                }
                            }
                        }
                        Spacer(TextToButtonsHeight)
                    }

                    if (customContent != null) {
                        Box(Modifier.fillMaxWidth().gravity(Alignment.CenterHorizontally)) {
                            customContent()
                        }
                        Spacer(CustomHeightSpace)
                    }

                    if (buttons != null) {
                        buttons()
                    }
                }
            }
        }
    }
}

// TODO(b/138925106): Add Auto mode when the flow layout is implemented
/**
 * An enum which specifies how the buttons are positioned inside the [AlertDialog]:
 *
 * [SideBySide] - positions the dismiss button to the left side of the confirm button in LTR
 * layout direction contexts, and to the right otherwise.
 * [Stacked] - positions the dismiss button below the confirm button.
 */
enum class AlertDialogButtonLayout {
    SideBySide,
    Stacked
}

@Composable
internal fun AlertDialogButtonLayout(
    confirmButton: @Composable (() -> Unit)?,
    dismissButton: @Composable (() -> Unit)?,
    buttonLayout: AlertDialogButtonLayout
) {
    Box(ButtonsBoxModifier, gravity = ContentGravity.CenterEnd) {
        if (buttonLayout == AlertDialogButtonLayout.SideBySide) {
            Row(horizontalArrangement = Arrangement.End) {
                if (dismissButton != null) {
                    dismissButton()
                    Spacer(ButtonsWidthSpace)
                }

                if (confirmButton != null) {
                    confirmButton()
                }
            }
        } else {
            Column {
                if (confirmButton != null) {
                    confirmButton()
                }

                if (dismissButton != null) {
                    Spacer(ButtonsHeightSpace)
                    dismissButton()
                }
            }
        }
    }
}

private val ButtonsBoxModifier = Modifier.fillMaxWidth().padding(all = 8.dp)
private val ButtonsWidthSpace = Modifier.preferredWidth(8.dp)
private val ButtonsHeightSpace = Modifier.preferredHeight(12.dp)
private val CustomHeightSpace = Modifier.preferredHeight(4.dp)
// TODO(b/138924683): Top padding should be actually be a distance between the Text baseline and
//  the Title baseline
private val TextPadding = Modifier.padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 0.dp)
// TODO(b/138924683): Top padding should be actually be relative to the Text baseline
private val TitlePadding = Modifier.padding(start = 16.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
// The height difference of the padding between a Dialog with a title and one without a title
private val NoTitleExtraHeight = Modifier.preferredHeight(2.dp)
private val TextToButtonsHeight = Modifier.preferredHeight(28.dp)