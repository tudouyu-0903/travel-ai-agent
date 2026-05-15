package com.cxy.travelaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetTimeToolTest {

    @Test
    void getTime() {
        String prompt = "现在几点";
        GetTimeTool getTimeTool = new GetTimeTool();
        String result = getTimeTool.getTime();
        assertNotNull(result);
    }
}