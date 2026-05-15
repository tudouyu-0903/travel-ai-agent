package com.cxy.travelaiagent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public class RagCustcomAdvisorFactory {

    public static Advisor createAdvisor(VectorStore vectorStore, String status) {
        VectorStoreDocumentRetriever.Builder retrieverBuilder = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)
                .topK(5);

        if (status != null && !status.isBlank()) {
            Filter.Expression expression = new FilterExpressionBuilder()
                    .eq("status", status)
                    .build();
            retrieverBuilder.filterExpression(expression);
        }

        DocumentRetriever documentRetriever = retrieverBuilder.build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(ContextualQueryAugmenterFactory.create())
                .build();
    }
}
