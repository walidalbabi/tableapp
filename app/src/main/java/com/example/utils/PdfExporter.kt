package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.model.TableCell
import com.example.viewmodel.TableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

object PdfExporter {
    suspend fun exportToPdf(context: Context, uri: Uri, tableState: TableState): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val pageWidth = 595 // A4 width in points (72 points per inch)
                val pageHeight = 842 // A4 height in points

                var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas

                val paint = Paint().apply {
                    color = Color.BLACK
                    strokeWidth = 1f
                    style = Paint.Style.STROKE
                }

                val textPaint = TextPaint().apply {
                    color = Color.BLACK
                    textSize = 12f
                    typeface = Typeface.DEFAULT
                    isAntiAlias = true
                }

                val margin = 50f
                val startX = margin
                var currentY = margin

                val usableWidth = pageWidth - 2 * margin
                val columnWidth = usableWidth / tableState.columns

                // Need to calculate row heights
                val rowHeights = FloatArray(tableState.rows) { 30f } // Min height
                val padding = 5f

                // First pass to calculate row heights
                for (r in 0 until tableState.rows) {
                    var maxRowHeight = 30f
                    for (c in 0 until tableState.columns) {
                        val cell = tableState.cells.find { it.row == r && it.column == c }
                        if (cell != null && !cell.answerCell && cell.text.isNotEmpty()) {
                            val staticLayout = StaticLayout.Builder.obtain(
                                cell.text,
                                0,
                                cell.text.length,
                                textPaint,
                                (columnWidth - 2 * padding).toInt()
                            )
                            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE) // RTL alignment
                            .build()
                            val height = staticLayout.height + 2 * padding
                            if (height > maxRowHeight) {
                                maxRowHeight = height
                            }
                        }
                    }
                    rowHeights[r] = maxRowHeight
                }

                for (r in 0 until tableState.rows) {
                    if (currentY + rowHeights[r] > pageHeight - margin) {
                        // Start new page
                        pdfDocument.finishPage(page)
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = margin
                    }

                    for (c in 0 until tableState.columns) {
                        // Drawing from right to left because RTL, so col 0 is on the right
                        val cellStartX = pageWidth - margin - (c + 1) * columnWidth
                        val cellStartY = currentY
                        val cellWidth = columnWidth
                        val cellHeight = rowHeights[r]

                        // Draw cell border
                        canvas.drawRect(cellStartX, cellStartY, cellStartX + cellWidth, cellStartY + cellHeight, paint)

                        val cell = tableState.cells.find { it.row == r && it.column == c }
                        if (cell != null && !cell.answerCell && cell.text.isNotEmpty()) {
                            val staticLayout = StaticLayout.Builder.obtain(
                                cell.text,
                                0,
                                cell.text.length,
                                textPaint,
                                (cellWidth - 2 * padding).toInt()
                            )
                            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                            .build()

                            canvas.save()
                            canvas.translate(cellStartX + padding, cellStartY + padding)
                            staticLayout.draw(canvas)
                            canvas.restore()
                        }
                    }
                    currentY += rowHeights[r]
                }

                pdfDocument.finishPage(page)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
