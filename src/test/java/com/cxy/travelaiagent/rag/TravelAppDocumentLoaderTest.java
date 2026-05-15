package com.cxy.travelaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TravelAppDocumentLoaderTest {
    @Resource
    private TraverAppDocumentLoader traverAppDocumentLoader;
    @Test
    void loadMarkdowns() {
        traverAppDocumentLoader.loadMarkdowns();
    }

}