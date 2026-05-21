package com.cxy.travelaiagent.aop;

import com.cxy.travelaiagent.anno.AuthCheck;
import com.cxy.travelaiagent.entity.AppUser;
import com.cxy.travelaiagent.exception.BusinessException;
import com.cxy.travelaiagent.exception.ErrorCode;
import com.cxy.travelaiagent.service.AppUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

@Aspect
@Component
public class AuthInterceptor  {
    @Resource
    private AppUserService userService;
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        //获取当前HttpServletRequest
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前用户
        AppUser loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return joinPoint.proceed();
    }
}
