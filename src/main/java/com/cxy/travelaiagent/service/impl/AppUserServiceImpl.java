package com.cxy.travelaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxy.travelaiagent.constant.UserConstant;
import com.cxy.travelaiagent.dto.RegisterRequest;
import com.cxy.travelaiagent.entity.AppUser;
import com.cxy.travelaiagent.exception.BusinessException;
import com.cxy.travelaiagent.exception.ErrorCode;
import com.cxy.travelaiagent.exception.ThrowUtils;
import com.cxy.travelaiagent.mapper.AppUserMapper;
import com.cxy.travelaiagent.service.AppUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
* @author chenx
* @description 针对表【app_user】的数据库操作Service实现
* @createDate 2026-05-20 16:33:40
*/
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser>
    implements AppUserService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /***
     *  用户注册
     * @param registerRequest
     * @return
     */
    @Override
    public long userRegister(RegisterRequest registerRequest) {
        String userAccount = registerRequest.getUserAccount();
        String userName = registerRequest.getUsername();
        String passWord = registerRequest.getPassword();
        String phone = registerRequest.getPhone();
        String email = registerRequest.getEmail();

        // 1. 校验
        //用户名不能为空不能过长
        if (StrUtil.hasBlank(userAccount,userName,passWord)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");
        }
        if (passWord.length() < 8 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短小于8位");
        }

        //2.判断数据库是否已有数据
        QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        //获取数据库查询结果
        Long count = this.baseMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        //3.密码加密
        String encryptPassword = getEncryptPassword(passWord);
        //4. 插入数据
        AppUser appUser = new AppUser();
        appUser.setUsername(userName);
        appUser.setUserAccount(userAccount);
        appUser.setPasswordHash(encryptPassword);
        if (phone != null){
            appUser.setPhone(phone);
        }
        if (email != null){
            appUser.setEmail(email);
        }
        //当前时间
        appUser.setUpdated_at( new Date());
        appUser.setUpdated_at( new Date());
        boolean save = this.save(appUser);
        if(!save){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存用户失败");
        }
        return appUser.getId();
    }

    /***
     * 登录
     * @param userAccount 账号
     * @param passWord 密码
     * @return
     */
    @Override
    public AppUser login(String userAccount, String passWord, HttpServletRequest request) {
        //校验
        ThrowUtils.throwIf(userAccount==null,ErrorCode.PARAMS_ERROR,"账号不能为空");
        ThrowUtils.throwIf(passWord==null,ErrorCode.PARAMS_ERROR,"密码不能为空");
        //查询数据库
        QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
        //建立查询条件
        queryWrapper.eq("userAccount", userAccount);
        //查询
        AppUser appUser = this.getOne(queryWrapper);
        if (appUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        String passwordHash = appUser.getPasswordHash();
        if (!passwordHash.equals(getEncryptPassword(passWord))){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
        }
        //4.保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, appUser);

        return appUser;
    }

    @Override
    public AppUser getLoginUser(HttpServletRequest request) {
        //1.从session中获取当前登录用户ID
        AppUser user =(AppUser) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        AppUser appUser = this.getById(userId);
        if (appUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        return appUser;
    }
    /***
     * 登出
     * @param
     * @return
     */
    @Override
    public Boolean logout(HttpServletRequest request) {
        Object userId =request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /***
     * 密码加盐
     * @param userPassword 用户密码
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "yupi";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }



}




