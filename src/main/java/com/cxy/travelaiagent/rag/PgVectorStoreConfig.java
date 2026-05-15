package com.cxy.travelaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
@Slf4j
public class PgVectorStoreConfig {

    @Resource
    private TraverAppDocumentLoader traverAppDocumentLoader;

    @Value("${travel.rag.init-documents:false}")
    private boolean initDocuments;

    @Value("${travel.rag.init-batch-size:25}")
    private int initBatchSize;

    @Bean
    @Lazy
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        PgVectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(25)
                .build();

        return vectorStore;
    }

    @Bean
    public CommandLineRunner pgVectorDocumentInitializer(VectorStore pgVectorStore) {
        return args -> {
            if (!initDocuments) {
                log.info("Skip PGvector document initialization. Set travel.rag.init-documents=true to import Markdown files.");
                return;
            }
            List<Document> documents = traverAppDocumentLoader.loadMarkdowns();
            addDocumentsInBatches(pgVectorStore, documents);
        };
    }

    private void addDocumentsInBatches(VectorStore vectorStore, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.info("No RAG documents found, skip PGvector initialization.");
            return;
        }

        int batchSize = Math.max(1, Math.min(initBatchSize, 25));
        for (int start = 0; start < documents.size(); start += batchSize) {
            int end = Math.min(start + batchSize, documents.size());
            vectorStore.add(documents.subList(start, end));
            log.info("Added RAG documents to PGvector: {}/{}", end, documents.size());
        }
    }
}
