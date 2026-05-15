package com.cxy.travelaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

@Component
public class QueryRewriter {
    private final QueryTransformer queryTransformer;
    public QueryRewriter(ChatModel dashscopeChatModel){
        ChatClient.Builder build = ChatClient.builder(dashscopeChatModel);
        queryTransformer=RewriteQueryTransformer.builder()
                .chatClientBuilder(build)
                .build();
    }

    public String rewrite(String prompt){
        Query query = new Query(prompt);
        Query transformQuery = queryTransformer.transform(query);
       return transformQuery.text();
    }
}
