package com.swpu.yosmart.exception;

public class TaskNotFoundException extends BaseException {
	public TaskNotFoundException(Long message) {
		super("未找到任务Id：" + message);
	}
}
