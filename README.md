# Liberium AI — Android

Native Android AI assistant built with Kotlin and Jetpack Compose.

## Implemented
- Dark chat UI and Groq chat-completions integration.
- User API key encrypted on-device with Android Keystore.
- Chat history restored locally after restarting the app.
- Android system file picker for multiple attachments; no broad storage permission.
- Photo understanding using Groq Qwen 3.8 Vision.
- Local text extraction from text/code formats, PDF text, and DOCX.
- ZIP inspection for supported text/code files with entry-count, extraction-size, and path traversal safeguards.
- Extracted document text is retained in chat context for follow-up questions.
- Selectable Groq text models: GPT-OSS 20B and GPT-OSS 120B; vision requests switch to Qwen automatically.
- Ongoing AI activity notification with a tap-to-open action, intended to appear in compatible Android status chips / TECNO Dynamic Port surfaces while generating or analyzing attachments.

## Models and provider limits
- Text: `openai/gpt-oss-20b` or `openai/gpt-oss-120b`.
- Images: `qwen/qwen3.8-27b`.
- Both currently use the user's Groq API key. Access, pricing, rate limits, and quotas are controlled by Groq and can change. The app does not promise unlimited provider usage.

## Attachment limits and privacy
The app has no daily attachment quota. Current per-file processing is bounded at 12 MB, ZIP extraction is bounded, and image requests support up to 3 images. These are technical/provider limits. Large extracted text is truncated to fit a practical request context.

Scanned/image-only PDFs are not yet OCR-rendered page by page; ordinary PDF text extraction works. Photos can be analyzed by the vision model, including text visible in them.

Files are opened through Android's document picker. Relevant extracted text or image data is sent to Groq for AI analysis. Chat text and extracted document text are stored locally in app preferences; attached image binaries are not kept in chat history. Do not attach passwords, API keys, or private documents unless you are comfortable sending their contents to the selected provider.

## Setup
1. Create a Groq account at https://console.groq.com/
2. Create an API key at https://console.groq.com/keys
3. Open Settings in the app and paste the key.
4. Choose a text model in Settings. Images automatically use Qwen Vision.

The API key is encrypted on this device. Removing the key in Settings clears its encrypted local copy.

## Build
Open in Android Studio with JDK 17, sync Gradle, then Build > Build Bundle(s) / APK(s) > Build APK(s).
Package: `com.liberium.ai`. Minimum Android: API 26.
