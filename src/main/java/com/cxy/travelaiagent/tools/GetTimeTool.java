package com.cxy.travelaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GetTimeTool {
    @Tool(description = "Get the current time")
    public String getTime() {
        return "现在时间是：" + LocalDateTime.now();
    }
}
