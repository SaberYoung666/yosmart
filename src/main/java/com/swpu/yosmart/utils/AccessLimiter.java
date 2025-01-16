package com.swpu.constructionsitesafety.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AccessLimiter {
    private final ConcurrentHashMap<String, Long> accessMap = new ConcurrentHashMap<>();

    // 增加一个方法参数moduleId，用于区分不同的模块
    public void incrementAccess(Integer userId, Integer moduleId) {
        // 使用 userId 和 moduleId 的组合作为键
        String key = userId + ":" + moduleId;
        accessMap.compute(key, (k, value) -> {
            long currentTime = System.currentTimeMillis();
            if (value == null || (currentTime - value) > TimeUnit.HOURS.toMillis(2)) {
                return currentTime; // 允许访问，重置时间
            } else {
                return value; // 不允许访问，保持原时间
            }
        });
    }

    // 同样，增加一个参数moduleId
    public boolean isAccessAllowed(Integer userId, Integer moduleId) {
        // 使用 userId 和 moduleId 的组合作为键
        String key = userId + ":" + moduleId;
        Long lastAccessTime = accessMap.get(key);
        long currentTime = System.currentTimeMillis();
        if (lastAccessTime == null || (currentTime - lastAccessTime) > TimeUnit.HOURS.toMillis(2)) {
            return true; // 允许访问
        }
        return false; // 不允许访问
    }
}