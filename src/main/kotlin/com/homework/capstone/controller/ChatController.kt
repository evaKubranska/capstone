package com.homework.capstone.controller

import com.homework.capstone.service.RagConversationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class ChatController(
    private val ragService: RagConversationService
) {

    @GetMapping("/chat")
    fun chat(
        @RequestParam(required = false) chatId: String?,
        @RequestParam question: String
    ): Map<String, Any?> {

        val activeChatId = chatId ?: UUID.randomUUID().toString()
        val result = ragService.chatWithPdf(activeChatId, question)

        return mapOf(
            "chatId"     to activeChatId,
            "answer"     to result.answer,
            "reasoning"  to result.reasoning,
            "critique"   to result.critique,
            "evaluation" to result.evaluation
        )
    }
}