package com.cxy.travelaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.cxy.travelaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class BaseAgent {

    private String name;
    private AgentState state = AgentState.IDLE;
    private String systemPrompt;
    private String nextStepPrompt;
    private int currentStep = 0;
    private int maxStep = 3;
    private ChatClient chatClient;
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {
        if (state != AgentState.IDLE) {
            throw new RuntimeException("cannot run agent from state " + state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent from empty userPrompt");
        }

        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        String lastResult = "";

        try {
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                lastResult = step();
            }
            if (StrUtil.isBlank(lastResult)) {
                lastResult = "已完成。";
            }
            return lastResult;
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Agent execution failed", e);
            return "执行失败：" + e.getMessage();
        } finally {
            clean();
        }
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(180000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("当前任务仍在处理中，请稍后再试。");
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("请输入有效的旅行需求。");
                    emitter.complete();
                    return;
                }

                state = AgentState.RUNNING;
                messageList.add(new UserMessage(userPrompt));

                String lastResult = "";
                for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                    currentStep = i + 1;
                    log.info("Executing agent step {}/{}", currentStep, maxStep);
                    lastResult = step();
                }

                if (StrUtil.isBlank(lastResult)) {
                    lastResult = "已完成。";
                }
                emitter.send(lastResult);
                emitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Streaming agent execution failed", e);
                try {
                    emitter.send("执行失败：" + e.getMessage());
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                this.clean();
            }
        });

        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.clean();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.clean();
            log.info("SSE connection completed");
        });

        return emitter;
    }

    public abstract String step();

    protected void clean() {
        state = AgentState.IDLE;
        currentStep = 0;
        messageList = new ArrayList<>();
        cleanInternal();
    }

    protected void cleanInternal() {
    }
}
