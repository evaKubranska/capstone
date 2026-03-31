package com.homework.capstone.service

import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.stereotype.Service

@Service
class VectorDatabaseService(
    private val vectorStore: VectorStore
) {


    /**
     * Checks if any documents in the vector store have the metadata 'file_name' equal to [fileName].
     */
    fun isFileIndexed(fileName: String): Boolean {

        val filter = FilterExpressionBuilder()
            .eq("file_name", fileName)
            .build()

        val searchRequest = SearchRequest.builder()
            .query("document existence check")
            .topK(1)
            .filterExpression(filter)
            .build()

        val results = vectorStore.similaritySearch(searchRequest)

        return results.isNotEmpty()
    }


    fun chunkAndStoreDocuments(rawDocuments: List<Document>, metadata: Map<String, Any> = emptyMap()) {

        val textSplitter = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withMinChunkSizeChars(100)
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(10000)
            .withKeepSeparator(true)
            .build()

        if (metadata.isNotEmpty()) {
            rawDocuments.forEach { doc ->
                doc.metadata.putAll(metadata)
            }
        }

        val chunkedDocuments = textSplitter.apply(rawDocuments)
        vectorStore.add(chunkedDocuments)
        println("Successfully embedded and stored ${chunkedDocuments.size} chunks for file: ${metadata["file_name"] ?: "unknown"}!")
    }
}
