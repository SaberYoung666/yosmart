package com.swpu.constructionsitesafety.config;

import com.swpu.constructionsitesafety.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 注入适配器
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private JwtTokenInterceptor jwtTokenInterceptor;

	// 快捷建立
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 登录和注册接口不拦截
		registry.addInterceptor(jwtTokenInterceptor).excludePathPatterns("/user/login");
	}
}