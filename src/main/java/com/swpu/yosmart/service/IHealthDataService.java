package com.swpu.yosmart.service;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 健康数据服务接口
 */
public interface IHealthDataService {

    /**
     * 根据日期和用户ID查询健康数据
     *
     * @param date 查询的日期
     * @return 健康数据列表
     */
    List<Map<String, Object>> getHealthDataByToday(LocalDate date);
    List<Map<String, Object>> getAllHealthData(LocalDate endDate);
}