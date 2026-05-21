package com.cxy.travelaiagent.exception;

import com.cxy.travelaiagent.common.BaseResponse;
import com.cxy.travelaiagent.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/***
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExcptionHandler {
    @ExceptionHandler()
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("businessException: ", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
