package com.cxy.travelaiagent.controller;

import com.cxy.travelaiagent.App.TravelApp;
import com.cxy.travelaiagent.agent.TravelAgent;
import com.cxy.travelaiagent.anno.AuthCheck;
import com.cxy.travelaiagent.common.BaseResponse;
import com.cxy.travelaiagent.common.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private TravelApp travelApp;

    @Resource
    private TravelAgent travelAgent;

    @GetMapping("/travel_app/chat/sync")
    public BaseResponse<String> doChatWithTravelAppSync(String message, String chatId) {
        String result = travelApp.doChat(message, chatId);
        return ResultUtils.success(result);
    }

    @GetMapping(value = "/travel_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithTravelAppSse(String message, String chatId) {
        return travelApp.doChatByStream(message, chatId);
    }

    @GetMapping("/travel_app/chat/rag")
    public BaseResponse<String> doChatWithTravelRag(String message, String chatId) {
        String result = travelApp.doChatWithRag(message, chatId);
        return ResultUtils.success(result);
    }

    @GetMapping("/travel_app/chat/tools")
    public BaseResponse<String> doChatWithTravelTools(String message, String chatId) {
        String result = travelApp.doChatWithTools(message, chatId);
        return ResultUtils.success(result);
    }

    @GetMapping("/travel_app/chat/mcp")
    public BaseResponse<String> doChatWithTravelMcp(String message, String chatId) {
        String result = travelApp.doChatWithMcp(message, chatId);
        return ResultUtils.success(result);
    }

    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> doChatWithLoveAppSync(String message, String chatId) {
        String result = doChatWithTravelAppSync(message, chatId).getData();
        return ResultUtils.success(result);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message, String chatId) {
        return doChatWithTravelAppSse(message, chatId);
    }

    @AuthCheck
    @GetMapping(value = "/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        return travelAgent.runStream(message);
    }
}
