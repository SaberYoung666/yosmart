package com.swpu.yosmart.config;

import com.swpu.yosmart.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

// 注入适配器
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private JwtTokenInterceptor jwtTokenInterceptor;

	// 快捷建立
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 放行的接口路径
		List<String> excludePath = new ArrayList<String>();
		excludePath.add("/user/login");
		excludePath.add("/user/register");
		// 登录和注册接口不拦截
		registry.addInterceptor(jwtTokenInterceptor).excludePathPatterns(excludePath);
	}
}