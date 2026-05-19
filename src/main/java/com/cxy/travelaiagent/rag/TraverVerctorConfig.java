package com.cxy.travelaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.util.List;

/***
 * 向量数据库初始化，基于内存的向量数据库bean
 */
@Configuration
@Profile("!pg-vector")
public class TraverVerctorConfig {
    @Resource
    private TraverAppDocumentLoader traverAppDocumentLoader;
    @Resource
    private  MyKeyWordEnricher myKeyWordEnricher;
    @Bean("pgVectorStore")
    @Lazy
    VectorStore simpleVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
//        调用对应大模型的embedding模型用于创建向量
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
//        获取加载的的文件列表
        List<Document> documents = traverAppDocumentLoader.loadMarkdowns();

//        自动补充关键词元信息
        List<Document> documents1 = documents;

//        将加载的文件添加到向量数据库中
        simpleVectorStore.add(documents1);
        return simpleVectorStore;

    }
}
