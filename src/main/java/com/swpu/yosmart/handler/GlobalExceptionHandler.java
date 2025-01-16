package com.swpu.constructionsitesafety.handler;

import com.swpu.constructionsitesafety.utils.ResultData;
import com.swpu.constructionsitesafety.utils.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * 捕获业务异常
	 *
	 * @param ex
	 * @param <T>
	 * @return
	 */
//	@ExceptionHandler
//	public <T> ResultData<T> exceptionHandler(Exception ex) {
//		log.error("异常信息：{}", ex.getMessage());
//		return ResultData.fail(ReturnCode.RC500.getCode(), ex.getMessage());
//	}

}
