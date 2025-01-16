package com.swpu.constructionsitesafety.context;

public class BaseContext {

	public static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

	public static Integer getUserId() {
		return threadLocal.get();
	}

	public static void setUserId(Integer id) {
		threadLocal.set(id);
	}

	public static void removeUserId() {
		threadLocal.remove();
	}

}

