package com.homework.capstone.service
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class TranscriptLoader(
    private val vectorDatabaseService: VectorDatabaseService,
    private val resourceResolver: ResourcePatternResolver,
    private val pdfExtractionService: PdfExtractionService
) {
    private val log = LoggerFactory.getLogger(TranscriptLoader::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun loadTranscripts() {
        log.info("Checking transcripts folder for new files...")

        try {
            val resources = resourceResolver.getResources("classpath:transcript/*.pdf")

            if (resources.isEmpty()) {
                log.info("No transcript files found in classpath:transcript/")
                return
            }

            resources.forEach { resource ->
                val fileName = resource.filename ?: "unknown_file"

                if (vectorDatabaseService.isFileIndexed(fileName)) {
                    log.info("File '$fileName' is already indexed. Skipping.")
                } else {
                    log.info("File '$fileName' not found in vector store. Indexing now...")
                    pdfExtractionService.extractDocumentsFromPdf(resource)
                }
            }
        } catch (e: Exception) {
            log.error("Error while scanning for transcripts: ${e.message}", e)
        }

        log.info("Transcript check completed.")
    }
}
