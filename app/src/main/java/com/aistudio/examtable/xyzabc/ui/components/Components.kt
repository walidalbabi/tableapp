package com.aistudio.examtable.xyzabc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.examtable.xyzabc.R

@Composable
fun CreateTableDialog(
    onDismiss: () -> Unit,
    onCreate: (rows: Int, columns: Int) -> Unit
) {
    var rows by remember { mutableStateOf("4") }
    var columns by remember { mutableStateOf("3") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.create_new_table)) },
        text = {
            Column {
                OutlinedTextField(
                    value = rows,
                    onValueChange = { rows = it },
                    label = { Text(stringResource(R.string.rows_count)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = columns,
                    onValueChange = { columns = it },
                    label = { Text(stringResource(R.string.columns_count)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )
                if (isError) {
                    Text(
                        text = stringResource(R.string.invalid_input),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val r = rows.toIntOrNull()
                    val c = columns.toIntOrNull()
                    if (r != null && c != null && r in 1..50 && c in 1..20) {
                        onCreate(r, c)
                        onDismiss()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
