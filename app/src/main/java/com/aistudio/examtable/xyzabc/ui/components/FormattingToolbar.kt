package com.aistudio.examtable.xyzabc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aistudio.examtable.xyzabc.R
import com.aistudio.examtable.xyzabc.model.CellStyle
import com.aistudio.examtable.xyzabc.viewmodel.TableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormattingToolbar(
    viewModel: TableViewModel,
    modifier: Modifier = Modifier
) {
    val tableState by viewModel.tableState.collectAsState()
    if (tableState.selectedCells.isEmpty()) return

    // Calculate common style properties of selected cells to show in toolbar
    // For simplicity, we just use the first selected cell's style
    val firstSelected = tableState.selectedCells.first()
    val cell = tableState.cells.find { it.row == firstSelected.first && it.column == firstSelected.second }
    val style = cell?.style ?: CellStyle()
    
    val colors = listOf(
        Color.Black, Color.White, Color.Gray, Color.Red,
        Color.Green, Color.Blue, Color(0xFFFFA500), Color(0xFF800080)
    )
    
    val bgColors = listOf(
        Color.Transparent, Color.White, Color.LightGray,
        Color.Yellow, Color.Cyan, Color.Green, Color(0xFFFFC0CB)
    )

    var showTextColorPicker by remember { mutableStateOf(false) }
    var showBgColorPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Main formatting row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(isBold = !it.isBold) } }) {
                        Icon(Icons.Filled.FormatBold, contentDescription = stringResource(R.string.bold), tint = if (style.isBold) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(isItalic = !it.isItalic) } }) {
                        Icon(Icons.Filled.FormatItalic, contentDescription = stringResource(R.string.italic), tint = if (style.isItalic) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(isUnderline = !it.isUnderline) } }) {
                        Icon(Icons.Filled.FormatUnderlined, contentDescription = stringResource(R.string.underline), tint = if (style.isUnderline) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(isStrikethrough = !it.isStrikethrough) } }) {
                        Icon(Icons.Filled.FormatStrikethrough, contentDescription = stringResource(R.string.strikethrough), tint = if (style.isStrikethrough) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    Divider(modifier = Modifier.height(24.dp).width(1.dp))
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(fontSize = it.fontSize + 2f) } }) {
                        Icon(Icons.Filled.TextIncrease, contentDescription = "Increase size")
                    }
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(fontSize = (it.fontSize - 2f).coerceAtLeast(8f)) } }) {
                        Icon(Icons.Filled.TextDecrease, contentDescription = "Decrease size")
                    }
                }
                item {
                    Divider(modifier = Modifier.height(24.dp).width(1.dp))
                }
                item {
                    IconButton(onClick = { viewModel.copyStyle() }) {
                        Icon(Icons.Filled.Brush, contentDescription = "نسخ التنسيق")
                    }
                }
                item {
                    IconButton(
                        onClick = { viewModel.pasteStyle() },
                        enabled = tableState.copiedStyle != null
                    ) {
                        Icon(Icons.Filled.ImagesearchRoller, contentDescription = "لصق التنسيق", tint = if (tableState.copiedStyle != null) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.3f))
                    }
                }
                item {
                    Divider(modifier = Modifier.height(24.dp).width(1.dp))
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(textAlign = "right") } }) {
                        Icon(Icons.Filled.FormatAlignRight, contentDescription = stringResource(R.string.align_right), tint = if (style.textAlign == "right") MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(textAlign = "center") } }) {
                        Icon(Icons.Filled.FormatAlignCenter, contentDescription = stringResource(R.string.align_center), tint = if (style.textAlign == "center") MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    IconButton(onClick = { viewModel.applyStyleToSelection { it.copy(textAlign = "left") } }) {
                        Icon(Icons.Filled.FormatAlignLeft, contentDescription = stringResource(R.string.align_left), tint = if (style.textAlign == "left") MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                }
                item {
                    Divider(modifier = Modifier.height(24.dp).width(1.dp))
                }
                item {
                    IconButton(onClick = { showTextColorPicker = !showTextColorPicker; showBgColorPicker = false }) {
                        Icon(Icons.Filled.FormatColorText, contentDescription = stringResource(R.string.text_color))
                    }
                }
                item {
                    IconButton(onClick = { showBgColorPicker = !showBgColorPicker; showTextColorPicker = false }) {
                        Icon(Icons.Filled.FormatColorFill, contentDescription = stringResource(R.string.bg_color))
                    }
                }
            }

            // Color Pickers
            if (showTextColorPicker) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors.size) { index ->
                        val color = colors[index]
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    val argb = (color.alpha * 255).toLong() shl 24 or
                                               ((color.red * 255).toLong() shl 16) or
                                               ((color.green * 255).toLong() shl 8) or
                                               (color.blue * 255).toLong()
                                    viewModel.applyStyleToSelection { it.copy(textColor = argb) }
                                    showTextColorPicker = false
                                }
                        )
                    }
                }
            }
            if (showBgColorPicker) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bgColors.size) { index ->
                        val color = bgColors[index]
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    val argb = (color.alpha * 255).toLong() shl 24 or
                                               ((color.red * 255).toLong() shl 16) or
                                               ((color.green * 255).toLong() shl 8) or
                                               (color.blue * 255).toLong()
                                    viewModel.applyStyleToSelection { it.copy(backgroundColor = if (color == Color.Transparent) null else argb) }
                                    showBgColorPicker = false
                                }
                        )
                    }
                }
            }
        }
    }
}
