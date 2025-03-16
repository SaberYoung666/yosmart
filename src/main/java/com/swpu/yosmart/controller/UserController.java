package com.swpu.yosmart.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swpu.yosmart.constant.UserStatusConstant;
import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.User;
import com.swpu.yosmart.entity.UserEntity;
import com.swpu.yosmart.entity.WorkTime;
import com.swpu.yosmart.entity.dto.*;
import com.swpu.yosmart.entity.vo.LoginVO;
import com.swpu.yosmart.repository.UserRepository;
import com.swpu.yosmart.service.IUserService;
import com.swpu.yosmart.service.IWorkTimeService;
import com.swpu.yosmart.utils.JwtUtil;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.swpu.yosmart.utils.ReturnCode.RC400;
import static com.swpu.yosmart.utils.ReturnCode.RC401;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author saber
 * @since 2024-12-21
 */

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
	@Autowired
	private IUserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IWorkTimeService workTimeService;

	@PostMapping("/login")
	public ResultData<LoginVO> login(@RequestBody LoginDTO loginDTO) {
		if (loginDTO.getName() == null || loginDTO.getName().isEmpty()) {
			return ResultData.fail(RC401.getCode(), "用户名为空");
		} else if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
			return ResultData.fail(RC401.getCode(), "密码为空");
		} else {
			QueryWrapper<User> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("name", loginDTO.getName());
			queryWrapper.eq("password", loginDTO.getPassword());
			User user = userService.getOne(queryWrapper);
			if (user == null) {
				return ResultData.fail(RC401.getCode(), "用户名或密码输入错误");
			} else {
				User loginUser = new User();
				loginUser.setId(user.getId());
				loginUser.setStatus(UserStatusConstant.ONLINE);
				loginUser.setLastLoginTime(LocalDateTime.now());
				userService.updateById(loginUser);

				LoginVO loginVO = new LoginVO();
				Map<String, Object> claim = new HashMap<>();
				claim.put("USER_ID", user.getId());
				String token = JwtUtil.createJWT(claim);
				loginVO.setToken(token);
				loginVO.setName(user.getName());
				log.info("用户{}登录", loginDTO.getName());
				return ResultData.success(loginVO);
			}
		}
	}

	@PostMapping("/register")
	public ResultData<Boolean> registerUser(@RequestBody RegisterDTO registerDTO) {
		User user = new User();
		boolean mysqlSave;
		if (registerDTO.getName() == null || registerDTO.getName().isEmpty()) {
			return ResultData.fail(RC400.getCode(), "用户名为空");
		} else if (registerDTO.getPassword() == null || registerDTO.getPassword().isEmpty()) {
			return ResultData.fail(RC400.getCode(), "密码为空");
		} else {
			QueryWrapper<User> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("name", registerDTO.getName());
			User one = userService.getOne(queryWrapper);
			if (one != null) {
				return ResultData.fail(RC400.getCode(), "该用户已经存在");
			}
			user.setName(registerDTO.getName());
			user.setPassword(registerDTO.getPassword());
			mysqlSave = userService.save(user);
			log.info("向mysql中添加用户:{}", registerDTO.getName());
		}

		// 同步插入到图数据库
		UserEntity userEntity = new UserEntity();
		LocalDateTime now = LocalDateTime.now();
		userEntity.setName(registerDTO.getName());
		userEntity.setCreatedAt(now);
		userEntity.setUpdatedAt(now);
		userRepository.save(userEntity);
		log.info("向neo4j中添加用户节点:{}", registerDTO.getName());
		return ResultData.success(mysqlSave);
	}

	@PostMapping("/updatePassword")
	public ResultData<Boolean> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
		String newPassword = updatePasswordDTO.getNewPassword();
		// 验证输入合法
		if (newPassword == null || newPassword.isEmpty()) {
			return ResultData.fail(RC400.getCode(), "新密码为空");
		} else {
			User user = userService.getById(BaseContext.getUserId());
			user.setPassword(newPassword);
			return ResultData.success(userService.updateById(user));
		}
	}

	@PostMapping("/deleteUser")
	public ResultData<Boolean> deleteUser(@RequestBody UserIdDTO userIdDTO) {
		return ResultData.success(userService.removeById(userIdDTO.getUserId()));
	}

	@GetMapping("/getInfo")
	public ResultData<User> getInfo() {
		return ResultData.success(userService.getById(BaseContext.getUserId()));
	}

	@PostMapping("/updateName")
	public ResultData<Boolean> updatePhone(@RequestBody UpdateNameDTO updateNameDTO) {
		User user = userService.getById(BaseContext.getUserId());
		user.setName(updateNameDTO.getNewName());
		return ResultData.success(userService.updateById(user));
	}

	@GetMapping("/get")
	public ResultData<Boolean> getUserByName(@RequestParam String userName) {
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("name", userName);
		User user = userService.getOne(queryWrapper);
		if (user == null) {
			log.info("不存在用户{}", userName);
			return ResultData.success(false);
		}
		log.info("用户{}已存在", userName);
		return ResultData.success(true);
	}

	/**
	 * 用户退出方法
	 *
	 * @return
	 */
	@PostMapping("/exit")
	public ResultData<Boolean> exit() {
		User user = userService.getById(BaseContext.getUserId());
		// 计算学习时长
		LocalDateTime startTime = user.getLastLoginTime();
		LocalDateTime endTime = LocalDateTime.now();
		Duration duration = Duration.between(startTime, endTime);
		Integer minutes = Math.toIntExact(duration.toMinutes());
		WorkTime workTime = new WorkTime();
		workTime.setUserId(user.getId());
		workTime.setStartTime(startTime);
		workTime.setEndTime(endTime);
		workTime.setDurationMinutes(minutes);
		workTimeService.save(workTime);
		// 修改状态
		User exitUser = new User();
		exitUser.setId(user.getId());
		exitUser.setStatus(UserStatusConstant.OFFLINE);
		userService.updateById(exitUser);
		return ResultData.success(true);
	}
}
