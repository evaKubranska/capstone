package com.homework.capstone.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "evaluations")
class Evaluation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(columnDefinition = "TEXT")
    var userQuestion: String = "",

    @Column(columnDefinition = "TEXT")
    var aiAnswer: String = "",

    @Column(columnDefinition = "TEXT")
    var retrievedContext: String = "",

    var isPass: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var reasoning: String = "",

    var timestamp: LocalDateTime = LocalDateTime.now()
)
