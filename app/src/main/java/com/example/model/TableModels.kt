package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CellStyle(
    val fontSize: Float = 16f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val textAlign: String = "center", // left, center, right
    val verticalAlign: String = "center", // top, center, bottom
    val textColor: Long? = null, // null means use default
    val backgroundColor: Long? = null,
    val borderThickness: Float = 1f,
    val borderColor: Long? = null,
    val showBorder: Boolean = true,
    val padding: Float = 4f
)

@Serializable
data class TableCell(
    val row: Int,
    val column: Int,
    val text: String = "",
    val answerCell: Boolean = false,
    val style: CellStyle = CellStyle(),
    val rowSpan: Int = 1,
    val colSpan: Int = 1,
    val isMerged: Boolean = false,
    val rootRow: Int = -1,
    val rootCol: Int = -1,
    val isTitle: Boolean = false
)

@Serializable
data class TableStyle(
    val tableBackground: Long? = null,
    val alternateRowColor: Long? = null,
    val defaultFontSize: Float = 16f,
    val defaultTextColor: Long = 0xFF000000,
    val defaultBorderColor: Long = 0x80808080,
    val defaultBorderThickness: Float = 1f,
    val defaultPadding: Float = 4f
)

@Serializable
data class TableFile(
    val rows: Int,
    val columns: Int,
    val theme: String = "light",
    val tableStyle: TableStyle = TableStyle(),
    val rowHeights: Map<Int, Float> = emptyMap(),
    val colWidths: Map<Int, Float> = emptyMap(),
    val cells: List<TableCell>
)
