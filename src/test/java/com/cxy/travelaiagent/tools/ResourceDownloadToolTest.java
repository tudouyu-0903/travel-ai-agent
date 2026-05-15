package com.cxy.travelaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



public class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.codefather.cn/favicon.ico";
        String fileName = "test_favicon.ico";
        String result = tool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}

