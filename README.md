# Liberium AI — Android

Native Android AI assistant built with Kotlin and Jetpack Compose.

## Current features
- Dark chat UI and sidebar.
- Groq chat-completions integration with encrypted API-key storage (Android Keystore).
- Conversation context.
- Android system file picker (Storage Access Framework), without broad storage permissions.
- Image understanding through Groq's Qwen vision model.
- Local extraction of text from PDF, DOCX, TXT, Markdown, source code, JSON, XML, and supported text formats.
- ZIP inspection for supported text/code files with entry-count, extraction-size, and path-traversal safeguards.

## AI providers
- Text conversations use `openai/gpt-oss-20b`.
- Requests containing images use `qwen/qwen3.8-27b` for vision.
- Both currently require the user's Groq API key. Provider availability, pricing, model access, rate limits, and quotas are controlled by Groq and can change.

## Attachments and practical limits
The app does not impose a daily attachment quota. Current per-file processing is bounded (12 MB per file), ZIP text extraction is bounded, and the vision provider currently accepts up to 3 images in one request. These are technical/provider limits, not a promise of unlimited processing. Large documents are truncated to a safe context size. Scanned PDFs without embedded text need a later OCR/image-rendering stage.

Files are opened through Android's document picker. Their content is processed on-device before relevant text or image data is sent to Groq. Do not attach passwords, API keys, or private documents unless you are comfortable sending their contents to the selected provider.

## Connect the AI
1. Create a Groq account at https://console.groq.com/
2. Create an API key at https://console.groq.com/keys
3. Open Settings in the app and paste the key.
4. The key is stored encrypted on this device.

## Build
Open in Android Studio with JDK 17, sync Gradle, then Build > Build Bundle(s) / APK(s) > Build APK(s).
Package: `com.liberium.ai`. Minimum Android: API 26.
