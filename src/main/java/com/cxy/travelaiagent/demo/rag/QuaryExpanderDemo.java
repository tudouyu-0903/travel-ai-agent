package com.cxy.travelaiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * 基于ai的查询扩展
 */
@Component
public class QuaryExpanderDemo {
   private ChatClient.Builder chatClientBuilder;
   public QuaryExpanderDemo(ChatModel dashscopeChatModel) {
       this.chatClientBuilder = ChatClient.builder(dashscopeChatModel);
   }
    public List<Query> QuaryExpand(String query) {
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
        List<Query> queries = queryExpander.expand(new Query(query));
        return queries;
    }
}
