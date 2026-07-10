package com.aistudio.examtable.xyzabc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.examtable.xyzabc.R
import com.aistudio.examtable.xyzabc.model.TableCell
import com.aistudio.examtable.xyzabc.viewmodel.TableState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableEditor(
    tableState: TableState,
    onCellTextChange: (row: Int, column: Int, text: String) -> Unit,
    onCellToggleAnswer: (row: Int, column: Int) -> Unit,
    onCellClear: (row: Int, column: Int) -> Unit,
    onCellSelect: (row: Int, column: Int, toggle: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!tableState.isInitialized) return

    val cellWidth = 120.dp
    val minCellHeight = 56.dp
    
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(count = tableState.rows, key = { "row_$it" }) { r ->
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    items(count = tableState.columns, key = { "col_$it" }) { c ->
                        val cell = tableState.cells.find { it.row == r && it.column == c } ?: TableCell(r, c)
                        var showMenu by remember { mutableStateOf(false) }
                        
                        val isSelected = tableState.selectedCells.contains(Pair(r, c))
                        val style = cell.style
                        
                        val bgColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else if (style.backgroundColor != null) {
                            Color(style.backgroundColor)
                        } else {
                            Color.Transparent
                        }

                        val textColor = if (style.textColor != null) Color(style.textColor) else MaterialTheme.colorScheme.onSurface
                        
                        val fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal
                        val fontStyle = if (style.isItalic) FontStyle.Italic else FontStyle.Normal
                        var textDecoration = TextDecoration.None
                        if (style.isUnderline && style.isStrikethrough) {
                            textDecoration = TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                        } else if (style.isUnderline) {
                            textDecoration = TextDecoration.Underline
                        } else if (style.isStrikethrough) {
                            textDecoration = TextDecoration.LineThrough
                        }
                        
                        val alignment = when(style.textAlign) {
                            "left" -> TextAlign.Left
                            "right" -> TextAlign.Right
                            else -> TextAlign.Center
                        }
                        
                        val contentAlignment = when(style.verticalAlign) {
                            "top" -> Alignment.TopCenter
                            "bottom" -> Alignment.BottomCenter
                            else -> Alignment.Center
                        }

                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .defaultMinSize(minHeight = minCellHeight)
                                .background(bgColor)
                                .then(
                                    if (style.showBorder) {
                                        val bColor = if (style.borderColor != null) Color(style.borderColor) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        Modifier.border(BorderStroke(style.borderThickness.dp, bColor))
                                    } else Modifier
                                )
                                .combinedClickable(
                                    onClick = { onCellSelect(r, c, false) }, // Single tap selects
                                    onLongClick = { showMenu = true }
                                )
                                .animateItem()
                                .padding(style.padding.dp),
                            contentAlignment = contentAlignment
                        ) {
                            if (cell.answerCell) {
                                Text(
                                    text = "",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                BasicTextField(
                                    value = cell.text,
                                    onValueChange = { 
                                        onCellTextChange(r, c, it) 
                                        onCellSelect(r, c, false)
                                    },
                                    textStyle = TextStyle(
                                        color = textColor,
                                        textAlign = alignment,
                                        fontSize = style.fontSize.sp,
                                        fontWeight = fontWeight,
                                        fontStyle = fontStyle,
                                        textDecoration = textDecoration
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(if (cell.answerCell) stringResource(R.string.text_cell) else stringResource(R.string.answer_cell)) 
                                    },
                                    onClick = {
                                        onCellToggleAnswer(r, c)
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_cell)) },
                                    onClick = {
                                        onCellClear(r, c)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
