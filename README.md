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

## 🚀 Getting Started

### Prerequisites
- JDK 21
- [Ollama](https://ollama.com/) (running locally for evaluation)
- Google Cloud API Key (for Gemini models) --currently not used

### Configuration
Update your `src/main/resources/application.properties` with your credentials:
```properties
spring.ai.google.genai.api-key=${GOOGLE_API_KEY}
spring.ai.ollama.base-url=http://localhost:11434
```

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
