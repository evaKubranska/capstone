package com.homework.capstone.controller

import com.homework.capstone.service.RagEvaluationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/evaluations")
class EvaluationController(
    private val ragEvaluationService: RagEvaluationService
) {

    @GetMapping("/stats")
    fun getStats(): Map<String, Any> {
        return ragEvaluationService.getStatistics()
    }
}
