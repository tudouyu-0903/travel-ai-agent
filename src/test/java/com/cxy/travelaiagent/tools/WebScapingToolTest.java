package com.cxy.travelaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebScapingToolTest {

    @Test
    void scrapPage() {
        WebScapingTool webScapingTool = new WebScapingTool();
        String url="https://www.codefather.cn";
        String result = webScapingTool.scrapPage(url);
        Assertions.assertNotNull(result);

    }
}