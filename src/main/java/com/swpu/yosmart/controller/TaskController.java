package com.swpu.yosmart.controller;

import com.swpu.yosmart.constant.TaskStatusConstant;
import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.TaskEntity;
import com.swpu.yosmart.entity.User;
import com.swpu.yosmart.entity.UserEntity;
import com.swpu.yosmart.entity.dto.AddTaskDTO;
import com.swpu.yosmart.entity.dto.TaskEntityIdDTO;
import com.swpu.yosmart.entity.dto.TaskPromptDTO;
import com.swpu.yosmart.entity.dto.apidto.SubTaskDTO;
import com.swpu.yosmart.entity.dto.apidto.TaskContentDTO;
import com.swpu.yosmart.entity.vo.TaskVO;
import com.swpu.yosmart.exception.UserNotFoundException;
import com.swpu.yosmart.repository.TaskRepository;
import com.swpu.yosmart.repository.UserRepository;
import com.swpu.yosmart.service.IUserService;
import com.swpu.yosmart.utils.DeepSeekClient;
import com.swpu.yosmart.utils.JsonParserUtil;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.swpu.yosmart.utils.ReturnCode.RC404;
import static com.swpu.yosmart.utils.ReturnCode.RC500;

@RestController
@RequestMapping("/task")
@Slf4j
public class TaskController {
	// TODO 暂时所有的查询都不向用户展示依赖关系，因为若用户想调整依赖关系会比较麻烦
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IUserService userService;

	/**
	 * 添加单个任务（用户手动输入任务进行添加）
	 *
	 * @param addTaskDTO
	 * @return
	 */
	@PostMapping("/add")
	public ResultData<Boolean> addTask(@RequestBody AddTaskDTO addTaskDTO) {
		// 根据id确定用户
		User user = userService.getById(BaseContext.getUserId());
		Optional<UserEntity> userEntity = userRepository.findById(user.getName());

		// 将任务绑定在用户上
		List<UserEntity> assignedTo = new ArrayList<>();
		userEntity.ifPresent(assignedTo::add);
		if (assignedTo.isEmpty()) {
			return ResultData.fail(RC500.getCode(), "图数据库中不存在该用户信息，请确认该用户是否是合法注册");
		}

		LocalDateTime now = LocalDateTime.now();

		// 属性赋值
		TaskEntity task = new TaskEntity();
		task.setDescription(addTaskDTO.getDescription());
		task.setPriority(addTaskDTO.getPriority());
		task.setStatus(TaskStatusConstant.NOT_STARTED);
		task.setRepeat(addTaskDTO.getRepeat());

		// 允许用户不设置开始时间
		if (addTaskDTO.getStartTime() != null) {
			task.setStartTime(addTaskDTO.getStartTime());
		}

		task.setEndTime(addTaskDTO.getEndTime());
		task.setTags(addTaskDTO.getTags());
		task.setCreatedAt(now);
		task.setUpdatedAt(now);
		task.setAssignedTo(assignedTo);
		taskRepository.save(task);
		log.info("用户{}添加了任务{}", user.getName(), task.getId());
		return ResultData.success(Boolean.TRUE);
	}

	/**
	 * 调用大模型计划任务
	 *
	 * @param promptDTO
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/plan")
	public ResultData<Object> planTask(@RequestBody TaskPromptDTO promptDTO) throws IOException {
		User user = userService.getById(BaseContext.getUserId());
		Optional<UserEntity> userEntity = userRepository.findById(user.getName());
		// TODO 这里后续需要根据userId查询他近期的任务作为提示词输入
		String taskPrompt = promptDTO.getTaskPrompt();
		String existTasks = "";
		// TODO 加入重试机制，若输出不合法，重新请求
		String plannedTasks = DeepSeekClient.planTasksWithRetry(existTasks, taskPrompt);
		Object parse = JsonParserUtil.parse(plannedTasks);
		return ResultData.success(parse);
	}

	/**
	 * 插入计划过的任务
	 *
	 * @param taskContentDTO
	 * @return
	 */
	@PostMapping("/addPlanedTasks")
	public ResultData<Boolean> addPlanedTasks(@RequestBody TaskContentDTO taskContentDTO) {
		LocalDateTime now = LocalDateTime.now();

		// 根据id确定用户
		User user = userService.getById(BaseContext.getUserId());
		Optional<UserEntity> userEntity = userRepository.findById(user.getName());
		// 将任务绑定在用户上
		List<UserEntity> assignedTo = new ArrayList<>();
		userEntity.ifPresent(assignedTo::add);
		if (assignedTo.isEmpty()) {
			return ResultData.fail(RC500.getCode(), "图数据库中不存在该用户信息，请确认该用户是否是合法注册");
		}

		// 将DTO类型转化为Entity类型
		List<SubTaskDTO> subTasks = taskContentDTO.getSubTasks();

		// 子任务
		List<TaskEntity> subtaskEntities = new ArrayList<>();
		List<TaskEntity> dependsOn = new ArrayList<>();
		// 主任务
		TaskEntity mainTask = new TaskEntity();

		// 获取子任务的最早开始时间和最晚结束时间，作为主任务的开始和结束时间
		// 定义时间格式解析器
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
		// 解析为 LocalDateTime
		LocalDateTime earliest = LocalDateTime.parse(subTasks.get(0).getStartTime(), formatter);
		LocalDateTime latest = LocalDateTime.parse(subTasks.get(subTasks.size() - 1).getEndTime(), formatter);

		// 为主任务属性赋值
		mainTask.setDescription(taskContentDTO.getMainTask());
		mainTask.setStatus(TaskStatusConstant.NOT_STARTED);
		mainTask.setStartTime(earliest);
		mainTask.setEndTime(latest);
		mainTask.setCreatedAt(now);
		mainTask.setUpdatedAt(now);
		mainTask.setAssignedTo(assignedTo);

		// 为子任务属性赋值
		dependsOn.add(mainTask);
		// TODO 子任务之间也应当有依赖关系，后续和其他任务之间也应当有依赖关系
		subTasks.forEach(subTaskDTO -> {
			TaskEntity subtaskEntity = new TaskEntity();
			subtaskEntity.setDescription(subTaskDTO.getDescription());
			subtaskEntity.setPriority(subTaskDTO.getPriority());
			subtaskEntity.setStatus(TaskStatusConstant.NOT_STARTED);
			subtaskEntity.setRepeat(subTaskDTO.isRepeat());
			subtaskEntity.setStartTime(LocalDateTime.parse(subTaskDTO.getStartTime(), formatter));
			subtaskEntity.setEndTime(LocalDateTime.parse(subTaskDTO.getEndTime(), formatter));
			subtaskEntity.setTags(subTaskDTO.getTags());
			subtaskEntity.setCreatedAt(now);
			subtaskEntity.setUpdatedAt(now);
			// 子任务都依赖于主任务
			subtaskEntity.setDependsOn(dependsOn);
			subtaskEntities.add(subtaskEntity);
		});

		// 首先添加主任务
		taskRepository.save(mainTask);
		// 其次添加子任务
		taskRepository.saveAll(subtaskEntities);
		log.info("用户{}添加了计划任务，主任务编号为{}", user.getName(), mainTask.getId());

		return ResultData.success(Boolean.TRUE);
	}

	@GetMapping("/list")
	public ResultData<List<TaskEntity>> listTask() {
		String userName = userService.getById(BaseContext.getUserId()).getName();
		Optional<UserEntity> userEntity = userRepository.findById(userName);
		UserEntity user = userEntity.orElseThrow(() ->
				new UserNotFoundException(userName)
		);
		List<TaskEntity> taskEntities = taskRepository.findByAssignedToUser(user.getName());
		return ResultData.success(taskEntities);
	}

	@DeleteMapping("/delete")
	public ResultData<Boolean> deleteTask(@RequestBody TaskEntityIdDTO taskEntityIdDTO) {
		// TODO 理清删除逻辑
		log.info("要删除的任务的id是{}", taskEntityIdDTO.getElementId());

//		// 判断要删除的任务是否属于当前用户
//		String userName = userService.getById(BaseContext.getUserId()).getName();
//		Optional<UserEntity> userEntity = userRepository.findById(userName);
//		Optional<TaskEntity> taskEntity = taskRepository.findById(taskEntityIdDTO.getTaskId());
//
//		UserEntity user = userEntity.orElseThrow(() ->
//				new UserNotFoundException(userName)
//		);
//		TaskEntity task = taskEntity.orElseThrow(() ->
//				new TaskNotFoundException(taskEntityIdDTO.getTaskId())
//		);

//		if (task.getAssignedTo().contains(user)) {
//			taskRepository.deleteById(taskEntityIdDTO.getElementId());
//			return ResultData.success("删除成功");
		//}
//		return ResultData.fail(RC404.getCode(), "任务不属于当前用户");
		return ResultData.fail(RC404.getCode(), "接口暂停使用");
	}

	@PostMapping("/update")
	public ResultData<Object> updateTask(@RequestBody TaskEntity taskEntity) {
		return ResultData.success();
	}

	/**
	 * 查询当日日程（完成、未完成、进行中）
	 *
	 * @return
	 */
	@GetMapping("/getToday")
	public ResultData<List<TaskVO>> getTodayTask() {
		// 确认用户，确保查询的是当前用户的日程
		String userName = userService.getById(BaseContext.getUserId()).getName();
		Optional<UserEntity> userEntity = userRepository.findById(userName);
		UserEntity user = userEntity.orElseThrow(() ->
				new UserNotFoundException(userName)
		);
		// 再查询与该用户有关的日程中，开始时间与结束时间包含今天的所有日程（应当是结束时间大于等于今天0点0分且开始时间小于等于今天23点59分）
		LocalDateTime today = LocalDate.now().atStartOfDay();
		LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
		List<TaskEntity> taskEntities = taskRepository.todayTasks(today, tomorrow);

		// 遍历，用VO类包装
		List<TaskVO> taskVOList = new ArrayList<>();
		taskEntities.forEach(taskEntity -> {
			log.info(taskEntity.getDescription());
			TaskVO taskVO = new TaskVO();
			taskVO.setId(taskEntity.getId());
			taskVO.setDescription(taskEntity.getDescription());
			taskVO.setPriority(taskEntity.getPriority());
			taskVO.setRepeat(taskEntity.getRepeat());
			// 对于重复日程，把开始日期和结束日期都限定为今天，时间不变
			taskVO.setStartTime(taskEntity.getStartTime());
			taskVO.setEndTime(taskEntity.getEndTime());
			taskVO.setTags(taskEntity.getTags());
			taskVO.setCreatedAt(taskEntity.getCreatedAt());
			taskVO.setUpdatedAt(taskEntity.getUpdatedAt());
			taskVOList.add(taskVO);
		});
		return ResultData.success(taskVOList);
	}
}
