package com.cxy.travelaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class ContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter create() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                抱歉，当前旅游知识库中没有检索到足够相关的信息。
                你可以补充目的地、出行时间、预算、同行人或旅行偏好；如果问题涉及实时价格、签证、天气、门票或开放时间，建议使用联网搜索工具核验。
                """);

        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
