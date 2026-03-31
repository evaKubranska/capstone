package com.homework.capstone.controller

import com.homework.capstone.service.PdfExtractionService
import com.homework.capstone.service.RagConversationService
import org.springframework.core.io.FileSystemResource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Controller
class UIController(
    private val pdfService: PdfExtractionService,
) {

    @GetMapping("/")
    fun index(model: Model): String {
        return "index"
    }

    @PostMapping("/api/upload")
    @ResponseBody
    fun uploadPdf(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        return try {
            val tempFile = File.createTempFile("uploaded-", ".pdf")
            file.transferTo(tempFile)
            pdfService.extractDocumentsFromPdf(FileSystemResource(tempFile))

            ResponseEntity.ok("Success: PDF embedded into ChromaDB!")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error: ${e.message}")
        }
    }

}