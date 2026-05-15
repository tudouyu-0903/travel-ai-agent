package com.cxy.yuimagesearchmcpserver.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {

    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Value("${pexels.api-key:${PEXELS_API_KEY:}}")
    private String apiKey;

    @Tool(description = "Search travel images from web by Pexels")
    public String searchImage(@ToolParam(description = "Search query keyword, such as city name or attraction") String query) {
        if (apiKey == null || apiKey.isBlank()) {
            return "MCP image search is paused: please get a Pexels API Key and set PEXELS_API_KEY before using this tool.";
        }
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    public List<String> searchMediumImages(String query) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);

        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("per_page", 5);

        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
