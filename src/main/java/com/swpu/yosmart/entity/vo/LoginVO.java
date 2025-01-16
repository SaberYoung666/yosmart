package com.swpu.yosmart.entity.vo;

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
     * token
     */
    private String token;
}