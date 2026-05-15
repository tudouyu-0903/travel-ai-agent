package com.cxy.travelaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class QuaryExpanderDemoTest {
    @Resource
    private QuaryExpanderDemo quaryExpanderDemo;
    @Test
   void testQuaryExpand(){
       List<Query> queries = quaryExpanderDemo.QuaryExpand("谁是歌手华晨宇啊啊啊啊啊啊啊，告诉我哈哈哈哈哈哈");
       Assertions.assertNotNull(queries);
   }
}