package com.swpu.yosmart.controller;

import com.swpu.yosmart.constant.TaskStatusConstant;
import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.TaskEntity;
import com.swpu.yosmart.entity.User;
import com.swpu.yosmart.entity.UserEntity;
import com.swpu.yosmart.entity.dto.AddTaskDTO;
import com.swpu.yosmart.entity.dto.TaskEntityIdDTO;
import com.swpu.yosmart.repository.TaskRepository;
import com.swpu.yosmart.repository.UserRepository;
import com.swpu.yosmart.service.IUserService;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.swpu.yosmart.utils.ReturnCode.RC500;

@RestController
@RequestMapping("/task")
@Slf4j
public class TaskController {
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IUserService userService;

	@PostMapping("/add")
	public ResultData<Boolean> addTask(@RequestBody AddTaskDTO addTaskDTO) {
		User user = userService.getById(BaseContext.getUserId());
		Optional<UserEntity> userEntity = userRepository.findById(user.getName());
		List<UserEntity> assignedTo = new ArrayList<>();
		userEntity.ifPresent(
				assignedTo::add
		);
		if (assignedTo.isEmpty()) {
			return ResultData.fail(RC500.getCode(), "图数据库中不存在该用户信息");
		}
		LocalDateTime now = LocalDateTime.now();

		TaskEntity task = new TaskEntity();
		task.setDescription(addTaskDTO.getDescription());
		task.setPriority(addTaskDTO.getPriority());
		task.setStatus(TaskStatusConstant.NOT_STARTED);
		task.setStartTime(addTaskDTO.getStartTime());
		task.setEndTime(addTaskDTO.getEndTime());
		task.setEstimatedTime(addTaskDTO.getEstimatedTime());
		task.setActualTime(-1);
		task.setTags(addTaskDTO.getTags());
		task.setCreatedAt(now);
		task.setUpdatedAt(now);
		task.setDependsOn(addTaskDTO.getDependsOn());
		task.setAssignedTo(assignedTo);
		task.setUses(addTaskDTO.getUses());
		task.setAffectedBy(addTaskDTO.getAffectedBy());
		taskRepository.save(task);
		log.info("用户{}添加了任务{}", user.getName(), task.getId());
		return ResultData.success(true);
	}

	@GetMapping("/list")
	public ResultData<List<TaskEntity>> listTask() {
		User user = userService.getById(BaseContext.getUserId());
		Optional<UserEntity> userEntity = userRepository.findById(user.getName());
		if (userEntity.isEmpty()) {
			return ResultData.fail(RC500.getCode(), "图数据库中不存在该用户信息");
		}
		List<TaskEntity> taskEntities = taskRepository.findByAssignedToUser(user.getName());
		return ResultData.success(taskEntities);
	}

	@DeleteMapping("/delete")
	public ResultData<Boolean> deleteTask(@RequestBody TaskEntityIdDTO taskEntityIdDTO) {

		return ResultData.success();
	}
}
