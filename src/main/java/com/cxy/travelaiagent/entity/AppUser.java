package com.cxy.travelaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName app_user
 */
@TableName(value = "app_user")
@Data
public class AppUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("username")
    private String username;
    @TableField("userAccount")
    private String userAccount;
    @TableField("phone")
    private String phone;
    @TableField("email")
    private String email;
    @TableField("password_hash")
    private String passwordHash;
    @TableField("created_at")
    private Date created_at;
    @TableField("updated_at")
    private Date updated_at;
}
