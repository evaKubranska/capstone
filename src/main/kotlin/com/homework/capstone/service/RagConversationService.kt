package com.homework.capstone.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

data class AgentResponse(
    var reasoning: String = "",
    var critique: String = "",
    var finalAnswer: String = "",
    var selfCorrected: String = "false"
)

data class ChatResult(
    val answer: String,
    val reasoning: String = "",
    val critique: String = "",
    val evaluation: EvaluationResponse? = null,
    val selfCorrected: Boolean = false,
    val retriesUsed: Int = 0
)


@Service
class RagConversationService(
    @Qualifier("chatClientBuilder")
    private val chatClientBuilder: ChatClient.Builder,
    private val agentTools: AgentTools,
    private val chatMemory: ChatMemory,
    private val vectorStore: VectorStore,
    private val ragEvaluationService: RagEvaluationService,

    @Value("\${project.chat.threshold}")
    private val threshold: Double,

    @Value("\${project.chat.max-retries:1}")
    private val maxRetries: Int
) {

    private val log = LoggerFactory.getLogger(RagConversationService::class.java)

    companion object {
        private val PRICE_KEYWORDS = setOf(
            "price", "stock price", "trading at", "share price", "current price", "live price", "quote"
        )
    }
    private val outputConverter = BeanOutputConverter(AgentResponse::class.java)
    private val objectMapper = jacksonObjectMapper()


    fun chatWithPdf(chatId: String, userQuestion: String): ChatResult {
        log.info("Starting reasoning agent for session: {}", chatId)

        return try {
            var result: ChatResult? = null
            var retries = 0
            val lastFeedback: String? = null

            while (retries <= maxRetries) {
                retries++
                val agentResponse = executeReasoningAgent(chatId, userQuestion, lastFeedback)

                log.info("=== [Attempt {}] REASONING ===\n{}", retries, agentResponse.reasoning)
                log.info("=== [Attempt {}] CRITIQUE  ===\n{}", retries, agentResponse.critique)
                log.info("=== [Attempt {}] FINAL ANSWER ===\n{}", retries, agentResponse.finalAnswer)

               val eval = evaluateResponse(userQuestion, agentResponse.finalAnswer)
                if(eval.isPass) {
                    result = ChatResult(
                        answer        = agentResponse.finalAnswer,
                        reasoning     = agentResponse.reasoning,
                        critique      = agentResponse.critique,
                        selfCorrected = agentResponse.selfCorrected.toBooleanStrictOrNull() ?: false,
                        retriesUsed   = retries
                    )
                    break
                }
                // last run, return response anyway
                if(retries == maxRetries) {
                    result = ChatResult(
                        answer        = agentResponse.finalAnswer,
                        reasoning     = agentResponse.reasoning,
                        critique      = agentResponse.critique,
                        selfCorrected = agentResponse.selfCorrected.toBooleanStrictOrNull() ?: false,
                        retriesUsed   = retries
                    )
                    break
                }
            }

            result!!
        } catch (e: Exception) {
            log.error("Agent failed to generate a response", e)
            ChatResult(answer = "I encountered an error processing your request. Please try again.")
        }
    }


    private fun executeReasoningAgent(
        chatId: String,
        userQuestion: String,
        priorFeedback: String? = null   // non-null on reflection retries
    ): AgentResponse {

        val needsPriceTool = PRICE_KEYWORDS.any { userQuestion.lowercase().contains(it) }

        val prompt = chatClientBuilder.build().prompt()
            .system(buildSystemPrompt(userQuestion, priorFeedback))
            .user(userQuestion)
            .advisors(buildRagAdvisor(), buildMemoryAdvisor())
            .advisors { it.param(ChatMemory.CONVERSATION_ID, chatId) }

        if (needsPriceTool) prompt.tools(agentTools)

        val rawResponse = prompt.call().content()
            ?: throw IllegalStateException("LLM returned null content")

        return parseAgentResponse(rawResponse)
    }

    private fun buildSystemPrompt(userQuestion: String, priorFeedback: String? = null): String {
        val correction = if (priorFeedback != null)
            "\nYour previous answer was wrong: \"$priorFeedback\". Fix it.\n" else ""

        val priceInstruction = if (PRICE_KEYWORDS.any { userQuestion?.lowercase()?.contains(it) == true })
            "A tool will provide the current stock price — use that value directly as your final answer. Do not say you do not know."
        else ""

        return """
        You are a financial analyst. Answer using only the provided context.
        First reason about the question, then critique only the accuracy of your answer (not the format), then give a final response.
        Provide the 'finalAnswer' as at least one full sentence.
        $correction
        $priceInstruction
        IMPORTANT: Output a single JSON object. No markdown, no explanation outside the JSON.
        ${outputConverter.format}
        """.trimIndent()
    }

    private fun parseAgentResponse(rawResponse: String): AgentResponse {
        val cleaned = rawResponse
            .trimIndent()
            .replace(Regex("^```(?:json)?\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
            .trim()

        runCatching { outputConverter.convert(cleaned) }
            .onSuccess { if (it.finalAnswer.isNotBlank()) return it }

        runCatching { objectMapper.readValue(cleaned, AgentResponse::class.java) }
            .onSuccess { if (it.finalAnswer.isNotBlank()) return it }

        log.warn("Structured parse failed — attempting regex fallback")

        fun extractField(fieldName: String): String =
            Regex(""""$fieldName"\s*:\s*"((?:[^"\\]|\\.)*)"""")
                .find(cleaned)?.groupValues?.get(1)
                ?.replace("\\n", "\n")
                ?.replace("\\\"", "\"")
                ?: ""

        val reasoning   = extractField("reasoning")
        val critique    = extractField("critique")
        val finalAnswer = extractField("finalAnswer")

        if (finalAnswer.isNotBlank()) {
            return AgentResponse(reasoning, critique, finalAnswer)
        }

        log.warn("All parsing attempts failed — returning raw text as finalAnswer")
        return AgentResponse(
            reasoning   = "Could not extract structured reasoning.",
            critique    = "Could not extract structured critique.",
            finalAnswer = rawResponse
        )
    }

    private fun buildRagAdvisor() = RetrievalAugmentationAdvisor.builder()
        .documentRetriever(
            VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(2)
                .similarityThreshold(threshold)
                .build()
        )
        .build()

    private fun buildMemoryAdvisor() = MessageChatMemoryAdvisor.builder(chatMemory).build()

    private fun evaluateResponse(question: String, answer: String): EvaluationResponse {
        val docs = runCatching {
            vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(question)
                    .topK(2)
                    .similarityThreshold(threshold)
                    .build()
            )
        }.getOrElse { emptyList() }

        if (docs.isEmpty()) {
            log.warn("No docs retrieved — skipping async evaluation")
            return EvaluationResponse(isPass = false, reasoning = "No docs retrieved")
        }
        return ragEvaluationService.evaluateAgentResponse(question, docs, answer)
    }
}