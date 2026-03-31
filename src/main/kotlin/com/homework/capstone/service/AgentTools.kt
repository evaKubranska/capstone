package com.homework.capstone.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class StockResponse(
    val ticker: String,
    val price: BigDecimal,
    val currency: String,
    val lastUpdated: String
)

@Component
class AgentTools(
    @Value("\${finnhub.api.key}") private val finnhubApiKey: String
) {
    private val log = LoggerFactory.getLogger(AgentTools::class.java)
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()


    @Tool(description = "Get the live stock price for a ticker symbol e.g. AAPL, MSFT, TSLA.")
    fun getLiveStockPriceTool(tickerSymbol: String): StockResponse {
        val ticker = tickerSymbol.trim().uppercase()
        log.info("AGENT TOOL: getLiveStockPriceTool called for '{}'", ticker)

        val url = "https://finnhub.io/api/v1/quote?symbol=$ticker&token=$finnhubApiKey"

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            log.warn("Finnhub API error — status={} body={}", response.statusCode(), response.body())
            throw RuntimeException("Finnhub returned HTTP ${response.statusCode()} for $ticker")
        }

        val json = objectMapper.readTree(response.body())
        val price = json.get("c")?.asText()?.toBigDecimalOrNull()
            ?: throw RuntimeException("Missing price field in Finnhub response for $ticker")

        if (price.compareTo(BigDecimal.ZERO) == 0) {
            throw RuntimeException("No price data for $ticker — check the ticker symbol is valid")
        }

        return StockResponse(
            ticker      = ticker,
            price       = price,
            currency    = "USD",
            lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
    }
}
