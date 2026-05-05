package com.chamaflow.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ExportUtils {
    fun shareCsv(context: Context, fileName: String, content: String) {
        try {
            val file = File(context.cacheDir, fileName)
            file.writeText(content)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
