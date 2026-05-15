package com.cxy.travelaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("local")
class PgVectorStoreConfigTest {

    @Resource
    VectorStore pgVectorStore;
    @Test
    void pgVectorStore() {
        List<Document> documents = List.of(
                new Document("有什么演出现场推荐？华晨宇的火星演唱会2.0版本啊", Map.of("meta1", "meta1")),
                new Document("华晨宇买下三块开演唱会"),
                new Document("华晨宇太有实力了", Map.of("meta2", "meta2")));

// Add the documents to PGVector
        pgVectorStore.add(documents);

// Retrieve documents similar to a query
        List<Document> results = this.pgVectorStore.similaritySearch(SearchRequest.builder().query("谁最有实力").topK(3).build());
        Assertions.assertNotNull(results);
    }
}