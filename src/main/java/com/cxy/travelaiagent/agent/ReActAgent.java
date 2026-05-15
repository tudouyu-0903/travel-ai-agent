package com.cxy.travelaiagent.agent;

import com.cxy.travelaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();

    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                setState(AgentState.FINISHED);
                Message lastMessage = getMessageList().isEmpty() ? null : getMessageList().get(getMessageList().size() - 1);
                if (lastMessage instanceof AssistantMessage assistantMessage) {
                    return assistantMessage.getText();
                }
                return "已完成。";
            }
            return act();
        } catch (Exception e) {
            log.error("Agent step failed", e);
            setState(AgentState.ERROR);
            return "执行失败：" + e.getMessage();
        }
    }
}
