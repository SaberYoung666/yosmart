package com.swpu.yosmart.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String message) {
		super("未找到用户名：" + message);
	}
}
