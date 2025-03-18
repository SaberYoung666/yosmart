package com.swpu.yosmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.HeartRate;
import com.swpu.yosmart.entity.SleepAnalysis;
import com.swpu.yosmart.entity.StepCount;
import com.swpu.yosmart.mapper.HeartRateMapper;
import com.swpu.yosmart.mapper.SleepAnalysisMapper;
import com.swpu.yosmart.mapper.StepCountMapper;
import com.swpu.yosmart.service.IHealthDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class HealthDataServiceImpl implements IHealthDataService {

	private final StepCountMapper stepCountMapper;
	private final SleepAnalysisMapper sleepAnalysisMapper;
	private final HeartRateMapper heartRateMapper;

	public HealthDataServiceImpl(StepCountMapper stepCountMapper, SleepAnalysisMapper sleepAnalysisMapper, HeartRateMapper heartRateMapper) {
		this.stepCountMapper = stepCountMapper;
		this.sleepAnalysisMapper = sleepAnalysisMapper;
		this.heartRateMapper = heartRateMapper;
	}

	@Override
	public List<Map<String, Object>> getHealthDataByToday(LocalDate date) {
		Integer userId = BaseContext.getUserId();

		// 查询StepCount表中指定日期和用户的数据
		List<StepCount> stepCountList = stepCountMapper.selectList(new QueryWrapper<StepCount>().eq("date", date).eq("user_id", userId));

		// 查询SleepAnalysis表中指定日期和用户的数据
		List<SleepAnalysis> sleepAnalysisList = sleepAnalysisMapper.selectList(new QueryWrapper<SleepAnalysis>().eq("date", date).eq("user_id", userId));

		// 查询HeartRate表中指定日期和用户的数据
		List<HeartRate> heartRateList = heartRateMapper.selectList(new QueryWrapper<HeartRate>().eq("date", date).eq("user_id", userId));

		// 创建一个列表用于存储最终合并后的结果
		List<Map<String, Object>> result = new ArrayList<>();

		// 处理步数数据，只保留每个日期下值最大的记录
		Map<LocalDate, StepCount> maxStepCountMap = new HashMap<>();
		for (StepCount stepCount : stepCountList) {
			LocalDate currentDate = stepCount.getDate();
			if (!maxStepCountMap.containsKey(currentDate) || stepCount.getValue() > maxStepCountMap.get(currentDate).getValue()) {
				maxStepCountMap.put(currentDate, stepCount);
			}
		}

		// 处理睡眠数据，只保留每个日期下值最大的记录
		Map<LocalDate, SleepAnalysis> maxSleepAnalysisMap = new HashMap<>();
		for (SleepAnalysis sleepAnalysis : sleepAnalysisList) {
			LocalDate currentDate = sleepAnalysis.getDate();
			if (!maxSleepAnalysisMap.containsKey(currentDate) || sleepAnalysis.getValue() > maxSleepAnalysisMap.get(currentDate).getValue()) {
				maxSleepAnalysisMap.put(currentDate, sleepAnalysis);
			}
		}

		// 处理心率数据，只保留每个日期下值最大的记录
		Map<LocalDate, HeartRate> maxHeartRateMap = new HashMap<>();
		for (HeartRate heartRate : heartRateList) {
			LocalDate currentDate = heartRate.getDate();
			if (!maxHeartRateMap.containsKey(currentDate) || heartRate.getValue() > maxHeartRateMap.get(currentDate).getValue()) {
				maxHeartRateMap.put(currentDate, heartRate);
			}
		}

		// 合并所有日期
		Set<LocalDate> allDates = new HashSet<>(maxStepCountMap.keySet());
		allDates.addAll(maxSleepAnalysisMap.keySet());
		allDates.addAll(maxHeartRateMap.keySet());

		for (LocalDate currentDate : allDates) {
			Map<String, Object> map = new HashMap<>();
			map.put("date", currentDate);

			// 添加步数数据
			StepCount maxStepCount = maxStepCountMap.get(currentDate);
			if (maxStepCount != null) {
				map.put("stepCount", maxStepCount.getValue());
			}

			// 添加睡眠数据
			SleepAnalysis maxSleepAnalysis = maxSleepAnalysisMap.get(currentDate);
			if (maxSleepAnalysis != null) {
				map.put("sleepTime", maxSleepAnalysis.getValue());
			}

			// 添加心率数据
			HeartRate maxHeartRate = maxHeartRateMap.get(currentDate);
			if (maxHeartRate != null) {
				map.put("heartRate", maxHeartRate.getValue());
			} else {
				map.put("heartRate", null);
			}

			result.add(map);
		}

		return result;
	}

	@Override
	public List<Map<String, Object>> getAllHealthData(LocalDate endDate) {
		Integer userId = BaseContext.getUserId();
		LocalDate startDate = endDate.minusDays(30);

		// 查询StepCount表中指定日期范围内和用户的数据
		List<StepCount> stepCountList = stepCountMapper.selectList(new QueryWrapper<StepCount>().ge("date", startDate).le("date", endDate).eq("user_id", userId));

		// 查询SleepAnalysis表中指定日期范围内和用户的数据
		List<SleepAnalysis> sleepAnalysisList = sleepAnalysisMapper.selectList(new QueryWrapper<SleepAnalysis>().ge("date", startDate).le("date", endDate).eq("user_id", userId));

		// 查询HeartRate表中指定日期范围内和用户的数据
		List<HeartRate> heartRateList = heartRateMapper.selectList(new QueryWrapper<HeartRate>().ge("date", startDate).le("date", endDate).eq("user_id", userId));

		// 处理步数数据，保留每个日期下的最大值
		Map<LocalDate, StepCount> maxStepMap = new HashMap<>();
		for (StepCount step : stepCountList) {
			LocalDate date = step.getDate();
			if (!maxStepMap.containsKey(date) || step.getValue() > maxStepMap.get(date).getValue()) {
				maxStepMap.put(date, step);
			}
		}

		// 处理睡眠数据，保留每个日期下的最大值
		Map<LocalDate, SleepAnalysis> maxSleepMap = new HashMap<>();
		for (SleepAnalysis sleep : sleepAnalysisList) {
			LocalDate date = sleep.getDate();
			if (!maxSleepMap.containsKey(date) || sleep.getValue() > maxSleepMap.get(date).getValue()) {
				maxSleepMap.put(date, sleep);
			}
		}

		// 处理心率数据，保留每个日期下的最大值
		Map<LocalDate, HeartRate> maxHeartRateMap = new HashMap<>();
		for (HeartRate heartRate : heartRateList) {
			LocalDate date = heartRate.getDate();
			if (!maxHeartRateMap.containsKey(date) || heartRate.getValue() > maxHeartRateMap.get(date).getValue()) {
				maxHeartRateMap.put(date, heartRate);
			}
		}

		// 合并所有存在数据的日期
		Set<LocalDate> allDates = new HashSet<>(maxStepMap.keySet());
		allDates.addAll(maxSleepMap.keySet());
		allDates.addAll(maxHeartRateMap.keySet());

		// 格式化日期
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		List<Map<String, Object>> result = new ArrayList<>();
		for (LocalDate date : allDates) {
			Map<String, Object> map = new HashMap<>();
			map.put("date", date.format(formatter));

			// 添加最大步数
			StepCount maxStep = maxStepMap.get(date);
			if (maxStep != null) {
				map.put("stepCount", maxStep.getValue());
			}

			// 添加最长睡眠时间
			SleepAnalysis maxSleep = maxSleepMap.get(date);
			if (maxSleep != null) {
				map.put("sleepTime", maxSleep.getValue());
			}

			// 添加最高心率
			HeartRate maxHeartRate = maxHeartRateMap.get(date);
			if (maxHeartRate != null) {
				map.put("heartRate", maxHeartRate.getValue());
			} else {
				map.put("heartRate", null);
			}

			result.add(map);
		}

		return result;
	}

}