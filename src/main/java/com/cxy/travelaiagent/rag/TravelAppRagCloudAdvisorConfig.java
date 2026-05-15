package com.cxy.travelaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Deleted:import org.springframework.stereotype.Component;


/***
 * 基于阿里云服务的 RAG 云知识库
 */
// Deleted://@Component
@Configuration
@Slf4j
public class TravelAppRagCloudAdvisorConfig  {
    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeAgentApiKey;

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeAgentApiKey);
        DocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder().withIndexName("spring-ai 知识库")
                        .build());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(dashScopeDocumentRetriever)
                .build();
    }
}
