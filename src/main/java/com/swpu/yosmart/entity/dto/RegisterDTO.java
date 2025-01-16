package com.swpu.constructionsitesafety.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
@Getter
@Setter
public class RegisterDTO {
    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户权限(0为工人，1为管理员)
     */
    private Integer authority;

    /**
     * 性别(0为男，1为女)
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 电话号码
     */
    private String phone;
}
