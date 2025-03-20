package com.swpu.yosmart.controller;

import com.swpu.yosmart.constant.TaskStatusConstant;
import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.TaskEntity;
import com.swpu.yosmart.entity.User;
import com.swpu.yosmart.entity.UserEntity;
import com.swpu.yosmart.entity.dto.*;
import com.swpu.yosmart.entity.dto.apidto.SubTaskDTO;
import com.swpu.yosmart.entity.dto.apidto.TaskContentDTO;
import com.swpu.yosmart.entity.vo.TaskVO;
import com.swpu.yosmart.exception.AIIllegalOutputException;
import com.swpu.yosmart.exception.TaskNotFoundException;
import com.swpu.yosmart.exception.UserNotFoundException;
import com.swpu.yosmart.repository.TaskRepository;
import com.swpu.yosmart.repository.UserRepository;
import com.swpu.yosmart.service.ITaskService;
import com.swpu.yosmart.service.IUserService;
import com.swpu.yosmart.utils.DeepSeekClient;
import com.swpu.yosmart.utils.JsonParserUtil;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
	// TODO 暂时所有的查询任务接口都不查询存在分解任务的任务，只展示分解后的子任务
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IUserService userService;
	@Autowired
	private ITaskService taskService;

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
		// 查询用户将来五天的任务作为提示词输入
		StringBuilder existTasks = new StringBuilder();
		for (int i = 0;i < 5;i++) {
			List<TaskVO> oneDayTaskVOS = taskService.getOneDayTaskVOS(LocalDate.now().plusDays(i));
			if (!oneDayTaskVOS.isEmpty()) {
				oneDayTaskVOS.forEach(oneDayTaskVO -> {
					existTasks.append("{").append(oneDayTaskVO.getDescription()).append(",StartTime:").append(oneDayTaskVO.getStartTime()).append(",EndTime:").append(oneDayTaskVO.getEndTime()).append(",Priority:").append(oneDayTaskVO.getPriority()).append("}");
				});
			}

		}
		// 用户输入的将要解析的提示词
		String taskPrompt = promptDTO.getTaskPrompt();

		String plannedTasks = DeepSeekClient.planTasksWithRetry(existTasks.toString(), taskPrompt);
		// 当前重试次数和最大重试次数
		int attempts = 0;
		int maxAttempts = 3;
		Object parse = null;
		// 循环尝试执行
		while (attempts < maxAttempts) {
			try {
				parse = JsonParserUtil.parse(plannedTasks);
				break;
			} catch (Exception e) {
				attempts++;
				log.error("AI输出出现错误{}, 正在重试第{}/{}次", e.getMessage(), attempts, maxAttempts);
				if (attempts < maxAttempts) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		}

		// 如果所有尝试都失败，抛出异常
		if (attempts >= maxAttempts) {
			log.error("不合法的AI输出：{}", plannedTasks);
			throw new AIIllegalOutputException("重试3次后AI仍无法按规则输出，请调整大模型配置");
		}

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
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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

	@PostMapping("/delete")
	public ResultData<Boolean> deleteTask(@RequestBody TaskEntityIdDTO taskEntityIdDTO) {
		// 父任务应当是无法删除的，因为用户在执行插入之后将永远不会见到父任务，因此将不会产生自由任务节点
		log.info("要删除的任务的id是{}", taskEntityIdDTO.getTaskId());

		// 判断要删除的任务是否属于当前用户,以及任务是否存在
		String userName = userService.getById(BaseContext.getUserId()).getName();
		Optional<UserEntity> userEntity = userRepository.findById(userName);
		Optional<TaskEntity> taskEntity = taskRepository.findById(taskEntityIdDTO.getTaskId());

		UserEntity user = userEntity.orElseThrow(() -> new UserNotFoundException(userName));
		TaskEntity task = taskEntity.orElseThrow(() -> new TaskNotFoundException(taskEntityIdDTO.getTaskId()));
		if (taskRepository.isTaskRelatedToPerson(user.getName(), task.getId())) {
			taskRepository.deleteById(taskEntityIdDTO.getTaskId());
			// 删除可能产生的孤立节点（一般不会出现这种情况）
			taskRepository.clearTask();
			return ResultData.success("删除成功");
		} else {
			return ResultData.fail(RC404.getCode(), "任务不属于当前用户");
		}
	}

	/**
	 * 修改任务
	 *
	 * @param taskUpdateDTOS
	 * @return
	 */
	@PostMapping("/update")
	public ResultData<Boolean> updateTask(@RequestBody List<TaskUpdateDTO> taskUpdateDTOS) {
		// 先判断这个任务是否和当前用户相关
		String userName = userService.getById(BaseContext.getUserId()).getName();
		Optional<UserEntity> userEntity = userRepository.findById(userName);
		UserEntity user = userEntity.orElseThrow(() -> new UserNotFoundException(userName));
		for (TaskUpdateDTO taskUpdateDTO : taskUpdateDTOS) {
			String userEntityName = user.getName();
			Long id = taskUpdateDTO.getId();
			if (taskRepository.isTaskRelatedToPerson(userEntityName, id)) {
				// 修改任务
				taskRepository.updateTask(id, taskUpdateDTO.getDescription(), taskUpdateDTO.getPriority(), taskUpdateDTO.getRepeat(), taskUpdateDTO.getStartTime(), taskUpdateDTO.getEndTime(), taskUpdateDTO.getStatus(), taskUpdateDTO.getTags(), LocalDateTime.now());
			} else {
				return ResultData.fail(RC404.getCode(), "任务不属于当前用户");
			}
		}
		return ResultData.success(Boolean.TRUE);
	}

	/**
	 * 修改任务状态
	 *
	 * @param taskUpdateStatusDTO
	 * @return
	 */
	@PostMapping("/update/status")
	public ResultData<Boolean> updateTaskStatus(@RequestBody TaskUpdateStatusDTO taskUpdateStatusDTO) {

		if (taskService.isRelatedTo(taskUpdateStatusDTO.getTaskId())) {
			// 修改任务状态
			taskRepository.updateTaskStatus(taskUpdateStatusDTO.getTaskId(), taskUpdateStatusDTO.getStatus());
			return ResultData.success("更新成功");
		}
		return ResultData.fail(RC404.getCode(), "任务不属于当前用户");
	}

	/**
	 * 查询当日日程（完成、未完成、进行中）
	 *
	 * @return
	 */
	@GetMapping("/getToday")
	public ResultData<List<TaskVO>> getTodayTask() {
		return ResultData.success(taskService.getOneDayTaskVOS(LocalDate.now()));
	}

	/**
	 * 根据状态获取当日日程
	 *
	 * @param taskStatusDTO
	 * @return
	 */
	@GetMapping("/getTodayByStatus")
	public ResultData<List<TaskVO>> getTodayTaskByStatus(@RequestBody TaskStatusDTO taskStatusDTO) {
		Integer status = taskStatusDTO.getStatus();
		List<TaskVO> taskVOList = new ArrayList<>();
		taskService.getOneDayTaskVOS(LocalDate.now()).forEach(taskVO -> {
			if (taskVO.getStatus().equals(status)) {
				taskVOList.add(taskVO);
			}
		});
		return ResultData.success(taskVOList);
	}

	/**
	 * 获取某日日程
	 */
	@GetMapping("/getOneDay")
	public ResultData<List<TaskVO>> getOneDayTask(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResultData.success(taskService.getOneDayTaskVOS(date));
	}

	/**
	 * 模糊查询
	 *
	 * @param keyword
	 * @return
	 */
	@GetMapping("/fuzzySearch")
	public ResultData<List<TaskVO>> fuzzySearch(@RequestParam String keyword) {
		return ResultData.success(taskService.fuzzySearch(keyword));
	}
}