package com.swpu.constructionsitesafety.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

	/**
	 * 用户姓名
	 */
	private String phone;

	/**
	 * 用户密码
	 */
	private String password;


}
