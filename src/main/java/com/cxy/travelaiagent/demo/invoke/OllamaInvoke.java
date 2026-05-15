package com.cxy.travelaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
//spring ai框架调用  ollama ai大模型
public class OllamaInvoke implements CommandLineRunner {
    @Resource
    private ChatModel ollamaChatModel;


    @Override
    public void run(String... args) throws Exception {

        AssistantMessage assistantMessage = ollamaChatModel.call(new Prompt("你好我是cxy"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
