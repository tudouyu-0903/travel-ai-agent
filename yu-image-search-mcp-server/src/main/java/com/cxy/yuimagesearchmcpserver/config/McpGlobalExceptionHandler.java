package com.cxy.yuimagesearchmcpserver.config;



import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@RestControllerAdvice
public class McpGlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncTimeout(AsyncRequestTimeoutException e) {
        log.debug("SSE 连接超时（正常现象）: {}", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public void handleIllegalState(IllegalStateException e) {
        if (e.getMessage() != null && e.getMessage().contains("OutputBuffer")) {
            log.debug("SSE 连接已关闭，忽略发送消息错误: {}", e.getMessage());
        } else {
            log.error("未处理的 IllegalStateException", e);
        }
    }

    @ExceptionHandler(Exception.class)
    public void handleGeneralException(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("OutputBuffer")) {
            log.debug("SSE 连接异常，忽略: {}", e.getMessage());
        } else {
            log.error("未处理的异常", e);
        }
    }
}
