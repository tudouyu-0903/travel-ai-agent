package com.cxy.yuimagesearchmcpserver.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;
    @Test
    void searchImage() {
        String searchImage = imageSearchTool.searchImage("小猫");
        Assertions.assertNotNull(searchImage);
    }

    @Test
    void searchMediumImages() {
    }
}