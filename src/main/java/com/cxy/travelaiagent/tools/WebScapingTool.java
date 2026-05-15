package com.cxy.travelaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WebScapingTool {
    @Tool(description ="Scap the content of a web page")
    public String scrapPage(@ToolParam(description = "url of the web page") String url){

        try {
            Document document = Jsoup.connect(url).get();
            return  document.html().toString();

        } catch (Exception e) {
            return  "网页抓取失败"+e.getMessage();
        }
    }
}
