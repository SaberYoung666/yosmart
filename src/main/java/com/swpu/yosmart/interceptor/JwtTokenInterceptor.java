package com.swpu.constructionsitesafety.interceptor;

import com.swpu.constructionsitesafety.context.BaseContext;
import com.swpu.constructionsitesafety.utils.JwtUtil;
import com.swpu.constructionsitesafety.utils.ReturnCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

	/**
	 * 校验jwt
	 *
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 从请求头中获取令牌
		String token = request.getHeader("Authorization");

		// 校验令牌
		try {
			log.info("jwt校验:{}", token);
			Claims claims = JwtUtil.parseJWT(token);
			log.info(String.valueOf(claims));
			Integer userId = Integer.valueOf((claims.get("USER_ID").toString()));
			log.info("当前用户的id:{}", userId);
			BaseContext.setUserId(userId);
			// 通过，放行
			return true;
		} catch (Exception ex) {
			// 不通过，响应401状态码
			log.info("请求不通过");
			response.setStatus(ReturnCode.RC401.getCode());
			return false;
		}
	}

	/**
	 * 清除token
	 *
	 * @param request
	 * @param response
	 * @param handler
	 * @param ex
	 * @throws Exception
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
		// 清空threadLocal中的数据
		BaseContext.removeUserId();
	}
}
