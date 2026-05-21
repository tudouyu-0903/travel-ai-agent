package com.cxy.travelaiagent.dto;

import lombok.Data;

@Data
public class LoginRequest {
    String password;   // 明文密码（必填）
    String userAccount;
}
