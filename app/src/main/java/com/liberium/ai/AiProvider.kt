package com.liberium.ai

/**
 * Capabilities a provider can expose to the app.
 *
 * Keep this contract independent from any single vendor so cloud and on-device
 * engines can be registered without coupling the chat UI to their APIs.
 */
enum class AiCapability {
    TEXT_GENERATION,
    IMAGE_INPUT
}

data class AiProviderRequest(
    val apiKey: String,
    val history: List<ChatMessage>,
    val attachments: List<PreparedAttachment>,
    val model: String
)

interface AiProvider {
    val id: String
    val displayName: String
    val capabilities: Set<AiCapability>

    suspend fun send(request: AiProviderRequest): String
}

/**
 * Temporary cloud adapter. The rest of the app talks to AiProvider, not GroqClient.
 */
class GroqProvider : AiProvider {
    override val id: String = ID
    override val displayName: String = "Groq"
    override val capabilities: Set<AiCapability> =
        setOf(AiCapability.TEXT_GENERATION, AiCapability.IMAGE_INPUT)

    override suspend fun send(request: AiProviderRequest): String =
        GroqClient.send(
            apiKey = request.apiKey,
            history = request.history,
            attachments = request.attachments,
            textModel = request.model
        )

    companion object {
        const val ID = "groq"
    }
}

/**
 * Single registry for available inference engines.
 * New providers should be added here after their adapters and tests exist.
 */
object AiProviderRegistry {
    const val DEFAULT_PROVIDER_ID = GroqProvider.ID

    private val providers: List<AiProvider> = listOf(GroqProvider())

    fun all(): List<AiProvider> = providers.toList()

    fun find(providerId: String): AiProvider? =
        providers.firstOrNull { it.id == providerId }

    fun get(providerId: String): AiProvider =
        find(providerId)
            ?: throw IllegalArgumentException("AI provider '$providerId' is not registered.")
}
