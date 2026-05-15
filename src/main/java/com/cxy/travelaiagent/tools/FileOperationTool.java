package com.cxy.travelaiagent.tools;

import cn.hutool.core.io.FileUtil;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import static com.cxy.travelaiagent.constant.FileConstant.FILE_SAVE_DIR;

@Component
public class FileOperationTool {
    private static final String FILE_PATH =FILE_SAVE_DIR+"file/";
    @Tool
    public String readFile(@ToolParam(description = "name of filename") String filename) {
        String filePath=FILE_PATH+"/"+filename;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "ERROR reading file"+filename;
        }
    }

    public String writeFile(@ToolParam(description = "name of filename") String filename,
                            @ToolParam(description = "content of file") String content) {
        String filePath=FILE_PATH+"/"+filename;
        try {
            FileUtil.mkdir(FILE_PATH);
            FileUtil.writeUtf8String(content,filePath);
            return "SUCCESS writing file"+filename;
        } catch (Exception e) {
            return "ERROR writing file"+filename;
        }
    }
}
