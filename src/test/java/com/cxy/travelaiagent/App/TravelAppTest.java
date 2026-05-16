package com.cxy.travelaiagent.App;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest

class TravelAppTest {
    @Resource
    private TravelApp travelApp;
//    @Test
//    void testChat() {
//        String chatId= UUID.randomUUID().toString();
////        第一轮
//        String userMessage="我是cxy";
//        String result = loveApp.doChat(userMessage, chatId);
//
////        第二轮
//         userMessage="我想另一半（小鱼）更爱我";
//         result = loveApp.doChat(userMessage, chatId);
//         Assertions.assertNotNull( result);
////        第一轮
//         userMessage="我的另一半是谁";
//         result = loveApp.doChat(userMessage, chatId);
//        Assertions.assertNotNull( result);
//
//    }



    @Test
    void doChatWithResport() {
        String chatId= UUID.randomUUID().toString();
        String userMessage="我是cxy,我想让另一半(小鱼）更爱我，但我不知道怎么做";
        TravelApp.LoveReport loveReport = travelApp.doChatWithResport(userMessage, chatId);
        Assertions.assertNotNull( loveReport);
    }


    @Test
    void doChatWithRag() {
        String chatId= UUID.randomUUID().toString();
        String userMessage="我已经结婚了，但是婚后关系不亲密怎么办";
        String answear = travelApp.doChatWithRag(userMessage, chatId);
        Assertions.assertNotNull(answear);
    }

    @Test

    void doChatWithTools() {
        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF文件，包含餐厅预订、活动流程和礼物清单，将生成的pdf文件链接给我");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = travelApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我生成一些哄另一半开心的照片";
        String answer = travelApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithMapMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点";
        String answer = travelApp.doChatWithMapMcp(message, chatId);
        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isEmpty());
        System.out.println("地图 MCP 响应: " + answer);
    }



}