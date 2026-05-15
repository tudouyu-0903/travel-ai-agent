package com.cxy.travelaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey ;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for latest travel information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        if (apiKey == null || apiKey.isBlank()) {
            return "联网搜索未执行：请先配置 SEARCH_API_KEY。";
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");

        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);

            if (jsonObject.containsKey("error")) {
                return "联网搜索失败：" + jsonObject.getStr("error");
            }

            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "联网搜索没有返回可用结果，请换一个更具体的关键词，或检查 SearchAPI 配置。原始响应：" + response;
            }

            int limit = Math.min(5, organicResults.size());
            List<Object> objects = organicResults.subList(0, limit);
            return objects.stream()
                    .map(obj -> ((JSONObject) obj).toString())
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            return "联网搜索失败：" + e.getMessage();
        }
    }
}
