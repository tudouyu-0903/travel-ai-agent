package com.cxy.travelaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cxy.travelaiagent.dto.RegisterRequest;
import com.cxy.travelaiagent.entity.AppUser;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author chenx
* @description 针对表【app_user】的数据库操作Service
* @createDate 2026-05-20 16:33:40
*/
public interface AppUserService extends IService<AppUser> {
    /***
     *用户注册
     * @param registerRequest
     * @return
     */
    long userRegister(RegisterRequest registerRequest);

    /***
     * 密码加盐
     * @param userPassword 用户密码
     * @return
     */
    String getEncryptPassword(String userPassword);

    /***
     * 登录
     * @param userAccount 账号
     * @param passWord 密码
     * @return
     */
    AppUser login(String userAccount, String passWord, HttpServletRequest request);

    /***
     * 获取当前登录用户
     * @param request
     * @return
     */
    AppUser getLoginUser(HttpServletRequest request);

    /***
     * 登出
     * @param
     * @return
     */
    Boolean logout(HttpServletRequest request);
}
