package com.homework.capstone.service

import org.slf4j.LoggerFactory
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class PdfExtractionService(
    private val vectorDatabaseService: VectorDatabaseService
) {
    private val log = LoggerFactory.getLogger(PdfExtractionService::class.java)


    fun extractDocumentsFromPdf(pdfResource: Resource) {
        log.info("Starting PDF extraction and embedding process...")
        val config = PdfDocumentReaderConfig.builder()
            .withPagesPerDocument(1)
            .build()
        val pdfReader = PagePdfDocumentReader(pdfResource, config)
        val documents = pdfReader.get()

        vectorDatabaseService.chunkAndStoreDocuments(documents)
        log.info("Success! Extracted, chunked, and saved {} documents to ChromaDB.", documents.size)
    }

}