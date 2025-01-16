package com.swpu.constructionsitesafety.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
public class LoginVO implements Serializable {
    /**
     * 用户姓名
     */
    private String name;

    /**
     * 学习进度
     */
    private Double progress;

    /**
     * token
     */
    private String token;
}