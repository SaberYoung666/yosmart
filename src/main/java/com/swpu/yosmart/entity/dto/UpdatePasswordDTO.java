package com.swpu.yosmart.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDTO {

	/**
	 * 用户旧密码
	 */
	private String oldPassword;

	/**
	 * 用户新密码
	 */
	private String newPassword;

}
