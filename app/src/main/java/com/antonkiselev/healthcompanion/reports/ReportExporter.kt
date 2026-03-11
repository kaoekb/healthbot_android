package com.antonkiselev.healthcompanion.reports

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.antonkiselev.healthcompanion.ui.ReportUiModel
import com.antonkiselev.healthcompanion.ui.TimelineEntryUi
import java.io.File
import java.io.FileOutputStream

object ReportExporter {
    fun shareReport(
        context: Context,
        report: ReportUiModel,
        recentEntries: List<TimelineEntryUi>,
    ) {
        val uri = exportReport(context, report, recentEntries) ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Health Compass ${report.window.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Поделиться PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun exportReport(
        context: Context,
        report: ReportUiModel,
        recentEntries: List<TimelineEntryUi>,
    ): android.net.Uri? {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val outputFile = File(reportsDir, "health_report_${report.window.id}.pdf")
        val document = PdfDocument()

        val page = document.startPage(PdfDocument.PageInfo.Builder(1080, 1440, 1).create())
        val canvas = page.canvas
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 40f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 26f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f
            alpha = 180
        }

        var y = 120f
        canvas.drawText("Health Compass отчет", 72f, y, titlePaint)
        y += 56f
        canvas.drawText("Период: ${report.window.title}", 72f, y, bodyPaint)
        y += 70f

        canvas.drawText("Сахар: ${report.sugarAverage}", 72f, y, bodyPaint)
        y += 42f
        canvas.drawText("Давление: ${report.pressureAverage}", 72f, y, bodyPaint)
        y += 42f
        canvas.drawText("Замеры сахара: ${report.sugarCount}", 72f, y, bodyPaint)
        y += 42f
        canvas.drawText("Замеры давления: ${report.pressureCount}", 72f, y, bodyPaint)
        y += 70f

        canvas.drawText("Последние записи", 72f, y, titlePaint)
        y += 46f

        recentEntries.take(12).forEach { entry ->
            canvas.drawText(entry.title, 72f, y, bodyPaint)
            y += 32f
            canvas.drawText("${entry.supportingText} • ${entry.timeLabel}", 72f, y, mutedPaint)
            y += 44f
        }

        document.finishPage(page)

        return try {
            FileOutputStream(outputFile).use(document::writeTo)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile,
            )
        } finally {
            document.close()
        }
    }
}
