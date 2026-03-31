package com.homework.capstone.repository

import com.homework.capstone.model.Evaluation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EvaluationRepository : JpaRepository<Evaluation, Long> {

    fun countByIsPassTrue(): Long
}
