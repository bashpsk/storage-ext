package io.bash_psk.storage_ext.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun NewFolderDialog(
    dialogVisibleState: MutableTransitionState<Boolean>,
    onCreateDirectory: (directory: String) -> Unit
) {

    val directoryFieldValue = remember { mutableStateOf(value = TextFieldValue(text = "")) }

    AnimatedVisibility(visibleState = dialogVisibleState) {

        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {

                dialogVisibleState.targetState = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            ),
            shape = MaterialTheme.shapes.small,
            titleContentColor = AlertDialogDefaults.titleContentColor,
            containerColor = AlertDialogDefaults.containerColor.copy(alpha = 0.70f),
            textContentColor = AlertDialogDefaults.textContentColor,
            iconContentColor = AlertDialogDefaults.iconContentColor,
            title = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        modifier = Modifier.weight(weight = 1.0F),
                        text = "New Directory",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        onClick = {

                            directoryFieldValue.value = directoryFieldValue.value.copy(text = "")
                            dialogVisibleState.targetState = false
                        }
                    ) {

                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            },
            text = {

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = directoryFieldValue.value,
                    onValueChange = { directory ->

                        directoryFieldValue.value = directory
                    },
                    label = {

                        Text(
                            text = "New Directory",
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    placeholder = {

                        Text(
                            text = "Enter Directory Name",
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            },
            confirmButton = {

                Button(
                    onClick = {

                        onCreateDirectory(directoryFieldValue.value.text)
                        directoryFieldValue.value = directoryFieldValue.value.copy(text = "")
                        dialogVisibleState.targetState = false
                    }
                ) {

                    Icon(imageVector = Icons.Filled.Done, contentDescription = "OK")
                    Spacer(modifier = Modifier.width(width = 2.dp))

                    Text(
                        text = "OK",
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            dismissButton = {

                Button(
                    onClick = {

                        directoryFieldValue.value = directoryFieldValue.value.copy(text = "")
                        dialogVisibleState.targetState = false
                    }
                ) {

                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Cancel")
                    Spacer(modifier = Modifier.width(width = 2.dp))

                    Text(
                        text = "Cancel",
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )
    }
}