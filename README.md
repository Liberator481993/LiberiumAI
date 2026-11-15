# Liberium AI — Android

Native Android AI chat app built with Kotlin and Jetpack Compose.

## Stage 1
- Dark chat UI and sidebar.
- Groq chat-completions integration.
- User-provided Groq API key.
- API key encrypted locally with Android Keystore (AES-GCM).
- Chat context sent with the latest 24 messages.

## Connect the AI
1. Create a Groq account at https://console.groq.com/
2. Create an API key at https://console.groq.com/keys
3. Install the app and open Settings.
4. Paste the key. The key is stored encrypted on this device.
5. Send a message.

The app calls https://api.groq.com/openai/v1/chat/completions using model openai/gpt-oss-20b. Model access and free quotas are controlled by Groq and may change. No provider quota is guaranteed.

## Privacy and security
- Never share your API key or commit it to GitHub.
- Requests are sent directly from this phone to Groq.
- The provider receives the messages included in each request; do not send secrets or sensitive personal data.
- Removing the key in app settings clears its encrypted local copy.

## Build
Open in Android Studio with JDK 17, sync Gradle, then Build > Build Bundle(s) / APK(s) > Build APK(s).
Package: com.liberium.ai. Min Android: API 26.
