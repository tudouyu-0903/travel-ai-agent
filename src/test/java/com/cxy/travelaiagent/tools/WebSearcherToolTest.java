package com.cxy.travelaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebSearcherToolTest {

    @Value("${search-api.api-key}")
    private String apiKey;


    @Test
    void searchWeb() {
        WebSearchTool webSearcherTool = new WebSearchTool(apiKey);
        String query = "谁是华晨宇";
        String result = webSearcherTool.searchWeb(query);
        Assertions.assertNotNull(result);
        System.out.println("Search result: " + result);
    }
}
