package com.swpu.yosmart.entity.dto;

import lombok.Getter;
import lombok.Setter;

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
	 * 手机号
	 */
	private String telephone;

	/**
	 * 邮箱
	 */
	private String email;
}
