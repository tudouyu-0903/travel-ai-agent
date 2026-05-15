package com.cxy.travelaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;

public class HttpAiInvoke {

    public static void main(String[] args) {
        String apiKey =TestApiKey.API_KEY;
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus");

        // 构建 input 对象
        JSONObject input = new JSONObject();
        JSONArray messages = new JSONArray();

        // 添加 system 消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // 添加 user 消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");
        messages.add(userMessage);

        input.set("messages", messages);
        requestBody.set("input", input);

        // 构建 parameters 对象
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 发送 POST 请求
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", ContentType.JSON.getValue())
                    .body(requestBody.toString())
                    .execute();

            // 获取响应内容
            String responseBody = response.body();
            System.out.println("响应状态码: " + response.getStatus());
            System.out.println("响应内容: " + responseBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
