package com.liberium.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.w3c.dom.Element
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

data class PreparedAttachment(
    val name: String,
    val mimeType: String,
    val extractedText: String = "",
    val imageDataUrl: String? = null
)

object AttachmentProcessor {
    private const val MAX_FILE_BYTES = 12L * 1024 * 1024
    private const val MAX_TEXT_CHARS = 80_000
    private const val MAX_ZIP_ENTRIES = 100
    private const val MAX_ZIP_TEXT_BYTES = 4L * 1024 * 1024

    suspend fun prepare(context: Context, uri: Uri): PreparedAttachment {
        val resolver = context.contentResolver
        val name = queryName(context, uri)
        val mime = resolver.getType(uri) ?: guessMime(name)
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_FILE_BYTES) { "Файл «$name» слишком большой (максимум 12 МБ на один файл на этом этапе)." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: error("Не удалось открыть файл «$name».")

        return when {
            mime.startsWith("image/") -> PreparedAttachment(name, mime, imageDataUrl = encodeImage(bytes, mime))
            mime == "application/pdf" || name.endsWith(".pdf", true) -> {
                PDFBoxResourceLoader.init(context)
                val text = PDDocument.load(bytes).use { doc ->
                    PDFTextStripper().getText(doc).take(MAX_TEXT_CHARS)
                }
                PreparedAttachment(name, mime, text.ifBlank { "[В PDF не найден извлекаемый текст. Возможно, это скан или изображения.]" })
            }
            name.endsWith(".docx", true) -> PreparedAttachment(name, mime, readDocx(bytes))
            name.endsWith(".zip", true) || mime == "application/zip" -> PreparedAttachment(name, mime, readZip(bytes))
            isTextLike(name, mime) -> PreparedAttachment(name, mime, bytes.toString(Charsets.UTF_8).take(MAX_TEXT_CHARS))
            else -> PreparedAttachment(name, mime, "[Формат распознан как $mime, но его содержимое пока не извлекается.]")
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index) ?: "attachment"
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
    }

    private fun guessMime(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "pdf" -> "application/pdf"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "zip" -> "application/zip"
        "txt", "md", "kt", "java", "js", "ts", "py", "json", "xml", "html", "css", "lua", "log" -> "text/plain"
        else -> "application/octet-stream"
    }

    private fun isTextLike(name: String, mime: String): Boolean =
        mime.startsWith("text/") || name.substringAfterLast('.', "").lowercase() in setOf(
            "txt", "md", "kt", "java", "js", "ts", "py", "json", "xml", "html", "css", "lua", "log", "csv", "gradle", "yml", "yaml"
        )

    private fun encodeImage(bytes: ByteArray, mime: String): String {
        val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Не удалось прочитать изображение.")
        val maxSide = 1800
        val scale = minOf(1f, maxSide.toFloat() / maxOf(source.width, source.height))
        val bitmap = if (scale < 1f) Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true) else source
        val output = ByteArrayOutputStream()
        val outputMime = if (mime == "image/png") "image/png" else "image/jpeg"
        bitmap.compress(if (outputMime == "image/png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG, 86, output)
        if (bitmap !== source) bitmap.recycle()
        source.recycle()
        return "data:$outputMime;base64," + android.util.Base64.encodeToString(output.toByteArray(), android.util.Base64.NO_WRAP)
    }

    private fun readDocx(bytes: ByteArray): String {
        val xml = ZipInputStream(bytes.inputStream()).use { zip ->
            var found: String? = null
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "word/document.xml") {
                    found = zip.readBytes().toString(Charsets.UTF_8)
                    break
                }
            }
            found
        } ?: return "[В DOCX не найден основной текстовый документ.]"
        return xmlToText(xml).take(MAX_TEXT_CHARS)
    }

    private fun readZip(bytes: ByteArray): String {
        val result = StringBuilder()
        var entries = 0
        var totalBytes = 0L
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries++
                if (entries > MAX_ZIP_ENTRIES) {
                    result.append("\n[Остановлено: в архиве больше 100 элементов.]")
                    break
                }
                val safeName = entry.name.replace('\\', '/')
                if (entry.isDirectory || safeName.startsWith("/") || safeName.split('/').any { it == ".." }) continue
                val ext = safeName.substringAfterLast('.', "").lowercase()
                if (ext !in setOf("txt", "md", "kt", "java", "js", "ts", "py", "json", "xml", "html", "css", "lua", "log", "yml", "yaml", "csv", "gradle")) continue
                val remaining = (MAX_ZIP_TEXT_BYTES - totalBytes).coerceAtLeast(0).toInt()
                if (remaining <= 0) break
                val buffer = ByteArray(8192)
                val output = ByteArrayOutputStream()
                while (output.size() <= remaining) {
                    val count = zip.read(buffer, 0, minOf(buffer.size, remaining + 1 - output.size()))
                    if (count <= 0) break
                    output.write(buffer, 0, count)
                }
                val content = output.toByteArray()
                totalBytes += content.size
                result.append("\n\n--- ").append(safeName).append(" ---\n")
                    .append(content.copyOf(minOf(content.size, remaining)).toString(Charsets.UTF_8))
                if (totalBytes >= MAX_ZIP_TEXT_BYTES) {
                    result.append("\n[Остановлено: достигнут лимит извлечения текста из архива.]")
                    break
                }
            }
        }
        return result.toString().take(MAX_TEXT_CHARS).ifBlank { "[В архиве не найдено поддерживаемых текстовых файлов.]" }
    }

    private fun xmlToText(xml: String): String {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        val document = factory.newDocumentBuilder().parse(xml.byteInputStream())
        val nodes = document.getElementsByTagName("w:t")
        return buildString {
            for (i in 0 until nodes.length) append(nodes.item(i).textContent).append(' ')
        }
    }
}
