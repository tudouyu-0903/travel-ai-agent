package com.cxy.travelaiagent.demo.invoke;

public final class TestApiKey {

    public static final String API_KEY = System.getenv().getOrDefault("DASHSCOPE_API_KEY", "");

    private TestApiKey() {
    }
}
