package com.cxy.travelaiagent.controller;

import com.cxy.travelaiagent.common.BaseResponse;
import com.cxy.travelaiagent.common.ResultUtils;
import com.cxy.travelaiagent.dto.LoginRequest;
import com.cxy.travelaiagent.dto.RegisterRequest;
import com.cxy.travelaiagent.entity.AppUser;
import com.cxy.travelaiagent.exception.ErrorCode;
import com.cxy.travelaiagent.exception.ThrowUtils;
import com.cxy.travelaiagent.service.AppUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private AppUserService userService;

    /***
     * 注册用户
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody RegisterRequest registerRequest) {
        ThrowUtils.throwIf(registerRequest == null, ErrorCode.PARAMS_ERROR);
        long userId = userService.userRegister(registerRequest);
        return ResultUtils.success(userId);
    }
    /***
     *
     */
    @PostMapping("/login")
    public BaseResponse<AppUser> login(@RequestBody LoginRequest loginRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(loginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getPassword();
        AppUser login = userService.login(userAccount, userPassword,request);
        return ResultUtils.success(login);
    }

    /***
     * 登出请求
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResultUtils.success(true);
    }
}
