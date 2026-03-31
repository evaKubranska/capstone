package com.homework.capstone.config
import com.google.genai.Client
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.google.genai.GoogleGenAiChatModel
import org.springframework.ai.google.genai.GoogleGenAiChatOptions
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.ai.ollama.api.OllamaModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AIConfig {
    @Bean
    fun chatMemory(): ChatMemory {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(InMemoryChatMemoryRepository())
            .maxMessages(5)
            .build()
    }


    @Bean("chatClientBuilder")
    fun chatClientBuilder(
        geminiChatModel: GoogleGenAiChatModel, ollamaChatModel: OllamaChatModel,
        @Value("\${project.chat.model:gemini}") activeModel: String
    ): ChatClient.Builder {
        val selectedModel = if (activeModel.equals("gemini", ignoreCase = true)) {
            geminiChatModel
        } else {
            ollamaChatModel
        }

        return ChatClient.builder(selectedModel)
    }

}
