package com.swpu.constructionsitesafety.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * 本项目中密钥为user_secret_key, 过期时间设置为1天
 */
public class JwtUtil {

	private static final String KEY = "user_secret_key";

	/**
	 * 生成jwt
	 * 使用Hs256算法, 私匙使用固定秘钥
	 *
	 * @param claims 设置的信息
	 * @return
	 */
	public static String createJWT(Map<String, Object> claims) {
		// 设置jwt的body
		return Jwts.builder()
				// claims声明是写在jwt中, 由jwt携带的信息
				.setClaims(claims)
				// 设置签名使用的签名算法和签名使用的秘钥
				.signWith(SignatureAlgorithm.HS256, KEY.getBytes(StandardCharsets.UTF_8))
				// 设置过期时间
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
				.compact();
	}

	/**
	 * Token解密
	 *
	 * @param token 加密后的token
	 * @return
	 */
	public static Claims parseJWT(String token) {
		return Jwts.parser()
				// 设置签名的秘钥
				.setSigningKey(KEY.getBytes(StandardCharsets.UTF_8))
				// 设置需要解析的jwt
				.parseClaimsJws(token).getBody();
	}
}
