package com.cxy.travelaiagent.controller;

import com.cxy.travelaiagent.App.TravelApp;
import com.cxy.travelaiagent.agent.TravelAgent;
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
    public String doChatWithTravelAppSync(String message, String chatId) {
        return travelApp.doChat(message, chatId);
    }

    @GetMapping(value = "/travel_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithTravelAppSse(String message, String chatId) {
        return travelApp.doChatByStream(message, chatId);
    }

    @GetMapping("/travel_app/chat/rag")
    public String doChatWithTravelRag(String message, String chatId) {
        return travelApp.doChatWithRag(message, chatId);
    }

    @GetMapping("/travel_app/chat/tools")
    public String doChatWithTravelTools(String message, String chatId) {
        return travelApp.doChatWithTools(message, chatId);
    }

    @GetMapping("/travel_app/chat/mcp")
    public String doChatWithTravelMcp(String message, String chatId) {
        return travelApp.doChatWithMcp(message, chatId);
    }

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return doChatWithTravelAppSync(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message, String chatId) {
        return doChatWithTravelAppSse(message, chatId);
    }

    @GetMapping(value = "/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        return travelAgent.runStream(message);
    }
}
