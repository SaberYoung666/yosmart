package com.swpu.constructionsitesafety.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultData<T> implements Serializable {

	private int status;
	private String message;
	private T data;

	/**
	 * 返回请求成功的数据
	 *
	 * @param data
	 * @param <T>
	 * @return
	 */
	public static <T> ResultData<T> success(T data) {
		ResultData<T> resultData = new ResultData<>();
		resultData.setStatus(ReturnCode.RC200.getCode());
		resultData.setMessage(ReturnCode.RC200.getMessage());
		resultData.setData(data);
		return resultData;
	}

	public static <T> ResultData<T> success() {
		ResultData<T> resultData = new ResultData<>();
		resultData.setStatus(ReturnCode.RC200.getCode());
		resultData.setMessage(ReturnCode.RC200.getMessage());
		return resultData;
	}

	/**
	 * 返回请求失败的数据
	 *
	 * @param code
	 * @param message
	 * @param <T>
	 * @return
	 */
	public static <T> ResultData<T> fail(int code, String message) {
		ResultData<T> resultData = new ResultData<>();
		resultData.setStatus(code);
		resultData.setMessage(message);
		return resultData;
	}
}
