package com.cxy.travelaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.cxy.travelaiagent.agent.model.AgentState;
import com.cxy.travelaiagent.tools.PDFGenerationTool;
import com.cxy.travelaiagent.tools.WebSearchTool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.stream.Collectors;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent {

    private ToolCallback[] availableTools;
    private ChatResponse toolChatResponse;
    private ToolCallingManager toolCallingManager;
    private final ChatOptions chatOptions;
    private PDFGenerationTool pdfGenerationTool;
    private WebSearchTool webSearchTool;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public boolean think() {
        String userText = getLatestUserText();
        if (shouldGeneratePdf(userText) && pdfGenerationTool != null) {
            String searchResult = "";
            if (shouldSearchWeb(userText) && webSearchTool != null) {
                log.info("{} tool call: searchWeb, query: {}", getName(), userText);
                searchResult = webSearchTool.searchWeb(userText);
                log.info("{} tool result: searchWeb, result: {}", getName(), searchResult);
            }

            String content = buildPdfContent(userText, searchResult);
            String fileName = "travel-plan-" + System.currentTimeMillis() + ".pdf";
            log.info("{} tool call: generateTravelPdf, fileName: {}", getName(), fileName);
            String pdfResult = pdfGenerationTool.generateTravelPdf(
                    fileName,
                    content
            );
            log.info("{} tool result: generateTravelPdf, result: {}", getName(), pdfResult);

            getMessageList().add(new AssistantMessage(formatPdfResult(pdfResult)));
            setState(AgentState.FINISHED);
            return false;
        }

        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        try {
            String system = getSystemPrompt();
            if (getNextStepPrompt() != null && !getNextStepPrompt().isBlank()) {
                system = system + "\n\n" + getNextStepPrompt();
            }

            ChatResponse chatResponse = getChatClient()
                    .prompt(prompt)
                    .system(system)
                    .tools(availableTools)
                    .call()
                    .chatResponse();

            this.toolChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            if (assistantMessage.getToolCalls().isEmpty()) {
                getMessageList().add(assistantMessage);
                setState(AgentState.FINISHED);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("{} failed while thinking", getName(), e);
            getMessageList().add(new AssistantMessage("处理失败：" + e.getMessage()));
            setState(AgentState.ERROR);
            return false;
        }
    }

    @Override
    public String act() {
        if (toolChatResponse == null) {
            setState(AgentState.FINISHED);
            return "已完成。";
        }

        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());

        ToolResponseMessage last = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = last.getResponses().stream()
                .map(response -> formatToolResult(response.name(), response.responseData()))
                .collect(Collectors.joining("\n\n"));

        setState(AgentState.FINISHED);
        log.info("{} tool execution result: {}", getName(), results);
        return results;
    }

    private String formatToolResult(String toolName, Object responseData) {
        String data = responseData == null ? "" : responseData.toString();
        if (toolName != null && toolName.toLowerCase().contains("pdf")) {
            return formatPdfResult(data);
        }
        return data;
    }

    private String getLatestUserText() {
        for (int i = getMessageList().size() - 1; i >= 0; i--) {
            if (getMessageList().get(i) instanceof UserMessage userMessage) {
                return userMessage.getText();
            }
        }
        return "";
    }

    private boolean shouldGeneratePdf(String text) {
        if (text == null) {
            return false;
        }
        String lowerText = text.toLowerCase();
        return lowerText.contains("pdf")
                || text.contains("导出")
                || text.contains("报告")
                || text.contains("文件")
                || text.contains("下载");
    }

    private boolean shouldSearchWeb(String text) {
        if (text == null) {
            return false;
        }
        return text.contains("联网")
                || text.contains("搜索")
                || text.contains("最新")
                || text.contains("攻略");
    }

    private String buildPdfContent(String userText, String searchResult) {
        StringBuilder content = new StringBuilder();
        content.append("智能旅游助手 PDF 报告\n\n");
        content.append("用户需求：\n").append(userText).append("\n\n");
        if (searchResult != null && !searchResult.isBlank()) {
            content.append("联网检索结果：\n").append(searchResult).append("\n\n");
        }
        content.append("规划建议：\n");
        content.append("1. 请根据预算、天数、出发地、同行人和旅行偏好进一步确认路线。\n");
        content.append("2. 景区、酒店、交通和演出价格会随季节波动，出行前请核验官方渠道。\n");
        content.append("3. 如果联网检索为空，本报告会基于用户需求生成，可补充信息后重新导出。\n");
        return content.toString();
    }

    private String formatPdfResult(String pdfResult) {
        if (pdfResult == null || pdfResult.isBlank()) {
            return "### PDF 生成失败\n\n工具没有返回结果，请稍后重试。";
        }
        if (pdfResult.startsWith("PDF generated successfully to:")) {
            String url = pdfResult.replace("PDF generated successfully to:", "").trim();
            return "### PDF 文件已生成\n\n[点击下载PDF报告](" + url + ")\n\n如需调整行程内容，可以继续告诉我。";
        }
        return "### PDF 生成失败\n\n" + pdfResult;
    }
}
