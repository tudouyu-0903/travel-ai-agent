package com.cxy.travelaiagent.dto;

import lombok.Data;

/***
 * 注册请求参数，包含创建用户所需的账号资料。
 */
@Data
public class RegisterRequest {
    String username;  // 用户名（必填）
    String password;   // 明文密码（必填）
    String userAccount;   // 账号（可选，必填）
    String phone;     // 手机号（可选，可为空）
    String email;       // 邮箱（可选，可为空
}
