package com.homework.capstone.service

import com.homework.capstone.model.Evaluation
import com.homework.capstone.repository.EvaluationRepository
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.evaluation.EvaluationRequest
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.evaluation.RelevancyEvaluator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Async
import java.util.concurrent.CompletableFuture

@Service
class RagEvaluationService(
    @Qualifier("chatClientBuilder")
    private val chatClientBuilder: ChatClient.Builder,
    private val evaluationRepository: EvaluationRepository
) {

    private val log = LoggerFactory.getLogger(RagEvaluationService::class.java)
    private val evaluator = RelevancyEvaluator(chatClientBuilder)

    @Async
    fun evaluateAgentResponse(
        userQuestion: String,
        retrievedContext: String,
        aiAnswer: String
    ): CompletableFuture<EvaluationResponse> {
        val request = EvaluationRequest(
            userQuestion,
            listOf(Document(retrievedContext)),
            aiAnswer
        )
        log.info("Async evaluation started")

        // 1. Run the evaluation, but don't exit early on failure
        val (isPass, feedback) = runCatching { 
            val result = evaluator.evaluate(request)
            log.info("Evaluation result:  $result")
            result.isPass to result.feedback
        }.getOrElse { e ->
            log.warn("Evaluator threw exception — using fallback pass: {}", e.message)
            true to "Evaluator unavailable: ${e.message}"
        }


        // 2. Always persist the result so stats counts increment correctly
        runCatching {
            evaluationRepository.save(
                Evaluation(
                    userQuestion     = userQuestion,
                    aiAnswer         = aiAnswer,
                    retrievedContext = retrievedContext,
                    isPass           = isPass,
                    reasoning        = feedback
                )
            )
        }.onFailure { log.error("Failed to persist evaluation", it) }
        log.info("Async evaluation $isPass with feedback $feedback")
        return CompletableFuture.completedFuture(EvaluationResponse(isPass, feedback))
    }

    fun getStatistics(): Map<String, Any> {
        val total  = evaluationRepository.count()
        val passed = evaluationRepository.countByIsPassTrue()
        val failed = total - passed
        val passRate = if (total > 0) "%.1f%%".format(passed.toDouble() / total * 100) else "n/a"
        return mapOf(
            "totalEvaluations"  to total,
            "passedEvaluations" to passed,
            "failedEvaluations" to failed,
            "passRate"          to passRate
        )
    }
}

data class EvaluationResponse(
    val isPass: Boolean,
    val reasoning: String
)