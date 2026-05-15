package com.cxy.travelaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileOperationToolTest {

    @Test
    void readFile() {
        String fileName="编程导航.txt";
        FileOperationTool fileOperationTool = new FileOperationTool();
        String result = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        String fileName="编程导航.txt";
        FileOperationTool fileOperationTool = new FileOperationTool();
        String result = fileOperationTool.writeFile(fileName, "你好，这里是编程导航编程社区");
        Assertions.assertNotNull(result);
    }
}