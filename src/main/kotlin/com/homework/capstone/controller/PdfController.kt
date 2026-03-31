package com.homework.capstone.controller

import com.homework.capstone.service.PdfExtractionService
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.ai.vectorstore.VectorStore

@RestController
class PdfController(
    private val pdfService: PdfExtractionService,
) {

    @GetMapping("/extract-pdf")
    fun extract(): String {
        val resource = ClassPathResource("sample.pdf")
        pdfService.extractDocumentsFromPdf(resource)
        return "Success: PDF 'sample.pdf' has been parsed, chunked, and stored in the vector database."
    }

    @RestController
    class DebugController(private val vectorStore: VectorStore) {

        @GetMapping("/debug/search")
        fun testVectorStore(@RequestParam query: String): Map<String, Any> {
            return try {
                val results = vectorStore.similaritySearch(
                    SearchRequest.Builder().query(query).topK(5).build()
                )

                if (results.isEmpty()) {
                    mapOf("status" to "Empty", "message" to "No documents found for: $query")
                } else {
                    mapOf(
                        "status" to "Success",
                        "total_found" to results.size,
                        "documents" to results.map { doc ->
                            mapOf(
                                "file" to (doc.metadata["file_name"] ?: "unknown"),
                                "text" to (doc.text?.trim() ?: "nothing")
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                mapOf("status" to "Error", "message" to (e.message ?: "Unknown error"))
            }
        }
    }
}