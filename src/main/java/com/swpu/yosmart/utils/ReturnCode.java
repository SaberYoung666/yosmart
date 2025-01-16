package com.swpu.yosmart.utils;

import lombok.Getter;

@Getter
public enum ReturnCode {
	/**
	 * 操作成功
	 **/
	RC100(100, "操作成功"),
	/**
	 * 操作失败
	 **/
	RC501(501, "操作失败"),
	/**
	 * 请求成功
	 **/
	RC200(200, "请求成功"),
	/**
	 * 服务降级
	 **/
	RC201(201, "服务开启降级保护,请稍后再试!"),
	/**
	 * 热点参数限流
	 **/
	RC202(202, "热点参数限流,请稍后再试!"),
	/**
	 * 系统规则不满足
	 **/
	RC203(203, "系统规则不满足要求,请稍后再试!"),
	/**
	 * 授权规则不通过
	 **/
	RC204(204, "授权规则不通过,请稍后再试!"),
	/**
	 * access_denied
	 **/
	RC403(403, "用户没有权限访问管理员资源"),
	/**
	 * access_denied
	 **/
	RC401(401, "匿名用户访问无权限资源时的异常"),
	/**
	 * 错误请求
	 */
	RC400(400, "服务器不理解请求的语法"),
	/**
	 * 未找到
	 */
	RC404(404, "找不到请求的资源"),
	/**
	 * 服务异常
	 **/
	RC500(500, "系统异常，请稍后重试");

	/**
	 * 自定义状态码
	 **/
	private final int code;
	/**
	 * 自定义描述
	 **/
	private final String message;

	ReturnCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

}