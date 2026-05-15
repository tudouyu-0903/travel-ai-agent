package com.cxy.travelaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * 集中工具工作类
 */
@Configuration
public class ToolRegistation {
    @Value("${search-api.api-key}")
    private String apiKey;

    @Bean
    public ToolCallback[] allTools(
            FileOperationTool fileOperationTool,
            ResourceDownloadTool resourceDownloadTool,
            TerminalOperationTool terminalOperationTool,
            PDFGenerationTool pdfGenerationTool,
            TerminateTool terminateTool
    ) {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebScapingTool webScrapingTool = new WebScapingTool();

        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
        );
    }
}
