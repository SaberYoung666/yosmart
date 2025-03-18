package com.swpu.yosmart.service.impl;

import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.TaskEntity;
import com.swpu.yosmart.entity.UserEntity;
import com.swpu.yosmart.entity.vo.TaskVO;
import com.swpu.yosmart.exception.UserNotFoundException;
import com.swpu.yosmart.repository.TaskRepository;
import com.swpu.yosmart.repository.UserRepository;
import com.swpu.yosmart.service.ITaskService;
import com.swpu.yosmart.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 任务实现类
 */
@Slf4j
@Service
public class TaskServiceImpl implements ITaskService {

	@Autowired
	private IUserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TaskRepository taskRepository;

	@Override
	public List<TaskVO> getOneDayTaskVOS(LocalDate oneDay) {
		// 确认用户，确保查询的是当前用户的日程
		String userName = userService.getById(BaseContext.getUserId()).getName();
		Optional<UserEntity> userEntity = userRepository.findById(userName);
		UserEntity user = userEntity.orElseThrow(() -> new UserNotFoundException(userName));
		log.info("startTime:{},endTime:{}", oneDay.atStartOfDay(), oneDay.plusDays(1).atStartOfDay());
		// 查询与该用户有关的日程中，开始时间与结束时间包含该天的所有日程（应当是结束时间大于等于该天0点0分且开始时间小于等于该天23点59分）
		List<TaskEntity> taskEntities = taskRepository.getTasksFromTo(user.getName(), oneDay.atStartOfDay(), oneDay.plusDays(1).atStartOfDay());
		log.info("今日查询出了{}个任务", taskEntities.size());

		// 遍历，用VO类包装
		List<TaskVO> taskVOList = new ArrayList<>();
		taskEntities.forEach(taskEntity -> {
			TaskVO taskVO = new TaskVO();
			taskVO.setId(taskEntity.getId());
			taskVO.setDescription(taskEntity.getDescription());
			taskVO.setPriority(taskEntity.getPriority());
			taskVO.setStatus(taskEntity.getStatus());

			// 对于重复日程，把开始日期和结束日期都限定为今天，时间不变
			Boolean isRepeat = taskEntity.getRepeat();
			LocalDateTime originStartTime = taskEntity.getStartTime();
			LocalDateTime originEndTime = taskEntity.getEndTime();
			taskVO.setRepeat(isRepeat);
			if (isRepeat) {
				// 提取时间部分
				LocalTime startTimePart = originStartTime.toLocalTime();
				LocalTime endTimePart = originEndTime.toLocalTime();
				// 组合为新的日期时间
				taskVO.setStartTime(LocalDateTime.of(oneDay, startTimePart));
				taskVO.setEndTime(LocalDateTime.of(oneDay, endTimePart));
			} else {
				taskVO.setStartTime(originStartTime);
				taskVO.setEndTime(originEndTime);
			}

			taskVO.setTags(taskEntity.getTags());
			taskVO.setCreatedAt(taskEntity.getCreatedAt());
			taskVO.setUpdatedAt(taskEntity.getUpdatedAt());
			taskVOList.add(taskVO);
		});
		return taskVOList;
	}

	@Override
	public Boolean isRelatedTo(Long taskId) {
		String userName = userService.getById(BaseContext.getUserId()).getName();
		Optional<UserEntity> userEntity = userRepository.findById(userName);
		UserEntity user = userEntity.orElseThrow(() -> new UserNotFoundException(userName));
		return taskRepository.isTaskRelatedToPerson(user.getName(), taskId);
	}
}
