package com.liberium.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiProviderRegistryTest {
    @Test
    fun defaultProviderIsRegistered() {
        val provider = AiProviderRegistry.get(AiProviderRegistry.DEFAULT_PROVIDER_ID)

        assertEquals(GroqProvider.ID, provider.id)
        assertEquals("Groq", provider.displayName)
    }

    @Test
    fun registeredProviderIdsAreUnique() {
        val ids = AiProviderRegistry.all().map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun groqDeclaresItsCurrentCapabilities() {
        val provider = AiProviderRegistry.get(GroqProvider.ID)

        assertTrue(provider.capabilities.contains(AiCapability.TEXT_GENERATION))
        assertTrue(provider.capabilities.contains(AiCapability.IMAGE_INPUT))
    }

    @Test
    fun unknownProviderDoesNotSilentlyFallBack() {
        assertNull(AiProviderRegistry.find("not-installed"))
    }

    @Test
    fun registryReturnsAProviderForTheCurrentDefault() {
        assertNotNull(AiProviderRegistry.find(AiProviderRegistry.DEFAULT_PROVIDER_ID))
    }
}
