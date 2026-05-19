package com.cxy.travelaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TravelAgentTest {
    @Resource
    private TravelAgent travelAgent;
    @Test
    void run() {
        String userPrompt = """  
                生成一份‘七夕约会计划’PDF文件，包含餐厅预订、活动流程和礼物清单，将生成的pdf文件链接给我 """;
        String answer = travelAgent.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}