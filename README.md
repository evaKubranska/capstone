# Equity Analyst Terminal 🏛️

A professional, RAG-powered AI financial assistant designed for institutional-grade document analysis. Built with **Spring Boot 4**, **Kotlin**, and **Spring AI**, this terminal transforms raw PDFs into structured financial insights with built-in reflection loops and automated quality auditing.

## 🌟 Key Features

### 1. Institutional Analyst Persona
- **Deep Navy Aesthetic**: A premium "Bloomberg-style" terminal interface using modern typography (Inter) and a sophisticated slate color palette.
- **Analyst Workspace**: A dedicated chat stage for executing complex inquiries against financial datasets.

### 2. Advanced RAG Pipeline
- **Vector Intelligence**: Seamless integration with **Chroma Vector Store** for high-fidelity retrieval.
- **PDF Ingestion**: Automated extraction and chunking of complex financial reports.
- **Reflection Loops**: The AI performs internal **Reasoning** (Chain of Thought) and **Self-Critique** before delivering final memos.

### 3. Automated Quality Auditing
- **AI-in-the-Loop Evaluation**: An "Auditor" model validates every response for factual accuracy.
- **Verification Ribbons**: Every answer is visually flagged for verification status (Validated vs. Discrepancy).

## 🛠️ Tech Stack

- **Backend**: Spring Boot 4.0.5, Kotlin 2.2.21
- **AI Framework**: [Spring AI](https://spring.io/projects/spring-ai) (2.0.0-M3)
- **Models**:
  -Ollama (llama3.2:3b)  and Gemini Embedding 1
- **Database**: 
  - **Relational**: H2 (Metadata & Evaluations)
  - **Vector**: Chroma DB
- **Frontend**: Thymeleaf, Modern Vanilla CSS, Smart JS Polling

## 🤖 AI Infrastructure & Spring AI Integration

The Equity Analyst Terminal is built using **Spring AI**, leveraging its high-level abstractions to create a robust and model-agnostic RAG system.

### 1. Unified Model Access
The system uses the `ChatClient` builder to swap between **Google Gemini** and **Ollama** dynamically based on environment configuration. This is managed in `AIConfig.kt`.

### 2. Retrieval-Augmented Generation (RAG)
- **Vector Intelligence**: We use `ChromaVectorStore` paired with `GoogleGenAiEmbeddingModel` to index and retrieve financial document segments.
- **Pipeline Enrichment**: The `RetrievalAugmentationAdvisor` automatically intercepts user queries, fetches the Top-K relevant context from Chroma, and injects it into the system prompt.
- **Semantic Filtering**: Implements `similarityThreshold` to ensure only highly relevant data informs the AI's reasoning.

### 3. Agentic Reasoning & Self-Correction
The core logic in `RagConversationService.kt` implements a **Reflection Loop**:
- **Reasoning**: The model first thinks through the financial data.
- **Critique**: The model performs an internal audit of its own logic.
- **Structured Output**: Uses `BeanOutputConverter` to guarantee response integrity via a strict JSON schema.

### 4. Tool Calling (Function Calling)
The AI is empowered with real-time capabilities via the `@Tool` annotation.
- **Live Stock Prices**: The `AgentTools.kt` provides the LLM with a direct interface to the **Finnhub API**, allowing it to fetch real-time market data when the user question warrants it.

### 5. Automated Quality Auditing (LLM-as-a-Judge)
Every response is validated asynchronously by a secondary "Auditor" process:
- **Fact Checking**: Uses the `FactCheckingEvaluator` from Spring AI to compare the AI's final answer against the retrieved source documents.
- **Persistence**: Results are stored in H2 and displayed on the terminal as "Validated" or "Discrepancy" via background polling.

## 🚀 Getting Started

### Prerequisites
- JDK 21
- [Ollama](https://ollama.com/) (running locally for evaluation: `ollama run llama3.2:3b`)
- Google Cloud AI Key (for Gemini models)
- Finnhub API Key (for real-time stock data)

### Configuration
Update your `src/main/resources/application.properties` with your credentials:
```properties
spring.ai.google.genai.api-key=${GOOGLE_API_KEY}
spring.ai.ollama.base-url=http://localhost:11434
finnhub.api.key=${FINNHUB_KEY}
### Running the Application
```bash
./gradlew bootRun
```
The terminal will be available at `http://localhost:8080`.

## 📁 Project Structure

```text
src/main/kotlin/com/homework/capstone/
├── controller/       # REST Endpoints (Chat, Upload, Stats)
├── service/          # Core Logistics
│   ├── RagConversationService # Reflection & Chat flow
│   ├── RagEvaluationService   # AI Auditing
│   └── VectorDatabaseService  # Document Context Management
├── repository/       # JPA Access for Evaluations
└── model/            # Domain Entities
---
*Built for excellence in financial AI.*

