package com.ksheera.sagara.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.data.entity.MilkEntry
import java.io.File
import java.io.FileOutputStream

object PdfExportUtils {

    fun generateMonthlySummary(
        context: Context,
        milkEntries: List<MilkEntry>,
        expenseEntries: List<ExpenseEntry>,
        monthLabel: String
    ): File? {
        return try {
            val totalIncome = milkEntries.sumOf { it.totalPayment }
            val totalExpense = expenseEntries.sumOf { it.amount }
            val netProfit = totalIncome - totalExpense
            val totalLiters = milkEntries.sumOf { it.liters }
            val profitPerLiter = if (totalLiters > 0) netProfit / totalLiters else 0.0

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val fileName = "KsheeraSagara_${monthLabel.replace(" ", "_")}.pdf"
            val file = File(dir, fileName)

            val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            // ─── Fonts ───────────────────────────────────────────────
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f, BaseColor(46, 125, 50))
            val subFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, BaseColor.DARK_GRAY)
            val normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11f)
            val boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f)
            val smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9f, BaseColor.GRAY)

            // ─── Title ───────────────────────────────────────────────
            document.add(Paragraph("🐄  Ksheera-Sagara", titleFont).apply {
                alignment = Element.ALIGN_CENTER
                spacingAfter = 4f
            })
            document.add(Paragraph("Monthly Financial Summary — $monthLabel", subFont).apply {
                alignment = Element.ALIGN_CENTER
                spacingAfter = 20f
            })

            // ─── Summary Cards ────────────────────────────────────────
            val summaryTable = PdfPTable(3).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(1f, 1f, 1f))
                spacingAfter = 20f
            }
            fun summaryCell(label: String, value: String, bg: BaseColor): PdfPCell {
                val cell = PdfPCell()
                cell.backgroundColor = bg
                cell.setPadding(10f)
                cell.addElement(Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 9f, BaseColor.WHITE)))
                cell.addElement(Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, BaseColor.WHITE)))
                cell.border = Rectangle.NO_BORDER
                return cell
            }
            summaryTable.addCell(summaryCell("Total Income", CurrencyUtils.formatSimple(totalIncome), BaseColor(46, 125, 50)))
            summaryTable.addCell(summaryCell("Total Expense", CurrencyUtils.formatSimple(totalExpense), BaseColor(198, 40, 40)))
            val profitColor = if (netProfit >= 0) BaseColor(21, 101, 192) else BaseColor(136, 14, 79)
            summaryTable.addCell(summaryCell("Net Profit", CurrencyUtils.formatSimple(netProfit), profitColor))
            document.add(summaryTable)

            // ─── Key Metrics ──────────────────────────────────────────
            document.add(Paragraph("Key Metrics", subFont).apply { spacingAfter = 8f })
            val metricsTable = PdfPTable(2).apply {
                widthPercentage = 100f
                spacingAfter = 20f
            }
            fun metricRow(label: String, value: String) {
                metricsTable.addCell(PdfPCell(Phrase(label, normalFont)).apply { setPadding(6f) })
                metricsTable.addCell(PdfPCell(Phrase(value, boldFont)).apply {
                    setPadding(6f)
                    horizontalAlignment = Element.ALIGN_RIGHT
                })
            }
            metricRow("Total Liters Produced", "%.2f L".format(totalLiters))
            metricRow("Profit per Liter", CurrencyUtils.formatSimple(profitPerLiter))
            metricRow("Total Milk Entries", "${milkEntries.size}")
            metricRow("Total Expense Entries", "${expenseEntries.size}")
            document.add(metricsTable)

            // ─── Expense Breakdown ────────────────────────────────────
            document.add(Paragraph("Expense Breakdown", subFont).apply { spacingAfter = 8f })
            val categories = listOf("Fodder", "Medical", "Labor", "Other")
            val expTable = PdfPTable(2).apply {
                widthPercentage = 60f
                horizontalAlignment = Element.ALIGN_LEFT
                spacingAfter = 20f
            }
            categories.forEach { cat ->
                val catTotal = expenseEntries.filter { it.category == cat }.sumOf { it.amount }
                if (catTotal > 0) {
                    expTable.addCell(PdfPCell(Phrase(cat, normalFont)).apply { setPadding(5f) })
                    expTable.addCell(PdfPCell(Phrase(CurrencyUtils.formatSimple(catTotal), boldFont)).apply {
                        setPadding(5f)
                        horizontalAlignment = Element.ALIGN_RIGHT
                    })
                }
            }
            document.add(expTable)

            // ─── Income Entries Table ─────────────────────────────────
            document.add(Paragraph("Income Log", subFont).apply { spacingAfter = 8f })
            val incomeTable = PdfPTable(5).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(2f, 1.5f, 1f, 1f, 1.5f))
                spacingAfter = 20f
            }
            fun headerCell(text: String) = PdfPCell(Phrase(text, boldFont)).apply {
                backgroundColor = BaseColor(200, 230, 201)
                setPadding(5f)
            }
            incomeTable.addCell(headerCell("Date"))
            incomeTable.addCell(headerCell("Liters"))
            incomeTable.addCell(headerCell("Fat%"))
            incomeTable.addCell(headerCell("SNF%"))
            incomeTable.addCell(headerCell("Amount"))
            milkEntries.forEach { e ->
                incomeTable.addCell(PdfPCell(Phrase(DateUtils.format(e.date), smallFont)).apply { setPadding(4f) })
                incomeTable.addCell(PdfPCell(Phrase("%.2f".format(e.liters), smallFont)).apply { setPadding(4f) })
                incomeTable.addCell(PdfPCell(Phrase("%.1f".format(e.fatPercentage), smallFont)).apply { setPadding(4f) })
                incomeTable.addCell(PdfPCell(Phrase("%.1f".format(e.snfPercentage), smallFont)).apply { setPadding(4f) })
                incomeTable.addCell(PdfPCell(Phrase(CurrencyUtils.formatSimple(e.totalPayment), smallFont)).apply { setPadding(4f) })
            }
            document.add(incomeTable)

            // ─── Footer ───────────────────────────────────────────────
            document.add(Paragraph("Generated by Ksheera-Sagara App  •  ${DateUtils.format(System.currentTimeMillis())}",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8f, BaseColor.GRAY)).apply {
                alignment = Element.ALIGN_CENTER
            })

            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Summary"))
    }
}
