package com.swpu.constructionsitesafety.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swpu.constructionsitesafety.context.BaseContext;
import com.swpu.constructionsitesafety.entity.User;
import com.swpu.constructionsitesafety.entity.dto.*;
import com.swpu.constructionsitesafety.entity.vo.LoginVO;
import com.swpu.constructionsitesafety.entity.vo.UserPageVO;
import com.swpu.constructionsitesafety.service.IUserService;
import com.swpu.constructionsitesafety.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.swpu.constructionsitesafety.utils.ReturnCode.*;

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

	@PostMapping("/login")
	public ResultData<LoginVO> login(@RequestBody LoginDTO loginDTO) {
		if (loginDTO.getPhone() == null || loginDTO.getPhone().isEmpty()) {
			return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "手机号为空！");
		} else if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
			return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "密码为空！");
		} else {
			LoginVO loginVO = userService.userLogin(loginDTO.getPhone(), loginDTO.getPassword());
			if (loginVO == null) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "手机号或密码输入错误！");
			} else {
				return ResultData.success(loginVO);
			}
		}
	}
	@PostMapping("/register")
	public ResultData registerUser(@RequestBody RegisterDTO registerDTO ) {
		User addUser = new User();
		User user = userService.getById(BaseContext.getUserId());
		if (user.getAuthority() == 1) {
			if (registerDTO.getName() == null || registerDTO.getName().isEmpty()) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "用户名为空！");
			} else if (registerDTO.getAuthority() == null) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "权限设置为空！");
			}
			else if (registerDTO.getGender() == null) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "性别为空！");
			}else if (registerDTO.getAge() == null) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "年龄为空！");
			}else if (registerDTO.getPhone() == null || registerDTO.getPhone().isEmpty()) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "电话号码为空！");
			} else if (userService.getUserPhone(registerDTO.getPhone()) == 0) {
				return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "该用户已经存在！");
			} else {
				addUser.setName(registerDTO.getName());
				addUser.setPassword("123456");
				addUser.setAge(registerDTO.getAge());
				addUser.setGender(registerDTO.getGender());
		        addUser.setAuthority(registerDTO.getAuthority());
				addUser.setPhone(registerDTO.getPhone());
				addUser.setModule1(0);
				addUser.setModule2(0);
				addUser.setModule3(0);
				addUser.setModule4(0);
				userService.save(addUser);
				return ResultData.success(true);
			}
		}
		return ResultData.fail(RC403.getCode(), RC403.getMessage());
	}
	@PostMapping("/updatePassword")
	public ResultData<Boolean> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
		if (updatePasswordDTO.getNewPassword() == null || updatePasswordDTO.getNewPassword().isEmpty()) {
			return ResultData.fail(USERNAME_OR_PASSWORD_ERROR.getCode(), "用户名为空！");
		} else {
			Integer userId = BaseContext.getUserId();
			Integer i = userService.resetPassword(userId, updatePasswordDTO.getNewPassword());
			if (i == 1) {
				return ResultData.success(true);
			}
			return ResultData.success(false);
		}
	}

	@PostMapping("/resetPassword")
	public ResultData<Boolean> resetPassword(@RequestBody UserIdDTO userIdDTO) {
		User user = userService.getById(BaseContext.getUserId());
		if (user.getAuthority() == 1) {
			Integer i = userService.resetPassword(userIdDTO.getUserId(), "123456");
			if (i == 1) {
				return ResultData.success(true);
			}
			return ResultData.fail(RC501.getCode(), RC501.getMessage());
		}
		return ResultData.fail(RC403.getCode(), RC403.getMessage());
	}

	@PostMapping("/deleteUser")
	public ResultData<Boolean> deleteUser(@RequestBody UserIdDTO userIdDTO) {
		User user = userService.getById(BaseContext.getUserId());
		if (user.getAuthority() == 1) {
			boolean result = userService.removeById(userIdDTO.getUserId());
			return ResultData.success(result);
		}
		return ResultData.fail(RC403.getCode(), RC403.getMessage());
	}

	@GetMapping("/getInfo")
	public ResultData<User> getInfo() {
		return ResultData.success(userService.getById(BaseContext.getUserId()));
	}

	@GetMapping("/getAllUsersInfo")
	public ResultData<UserPageVO> getAllUsersInfo(@RequestParam Integer pageId) {
		User user = userService.getById(BaseContext.getUserId());
		if (user.getAuthority() == 1) {
			IPage<User> users = userService.getAllUsersInfo(pageId);
			UserPageVO userPageVO = new UserPageVO();
			userPageVO.setPages(users.getPages());
			userPageVO.setUsers(users.getRecords());
			return ResultData.success(userPageVO);
		}
		return ResultData.fail(RC403.getCode(), RC403.getMessage());
	}

	@PostMapping("/updatePhone")
	public ResultData<Boolean> updatePhone(@RequestBody UpdatePhoneDTO updatePhoneDTO) {
		User user = new User();
		user.setId(BaseContext.getUserId());
		user.setPhone(updatePhoneDTO.getNewPhone());
		return ResultData.success(userService.updateById(user));
	}

	@GetMapping("/selectUser")
	public ResultData<UserPageVO> selectUser(@RequestParam String likeName) {
		User user = userService.getById(BaseContext.getUserId());
		if (user.getAuthority() == 1) {
			IPage<User> users = userService.selectUser(likeName);
			UserPageVO userPageVO = new UserPageVO();
			userPageVO.setPages(users.getPages());
			userPageVO.setUsers(users.getRecords());
			return ResultData.success(userPageVO);
		}
		return ResultData.fail(RC403.getCode(), RC403.getMessage());
	}
	@PostMapping("/updateUserModule")
	public ResultData<Boolean> updateUserModule(@RequestBody ModuleIdDTO moduleIdDTO){
		User user = userService.getById(BaseContext.getUserId());
		switch (moduleIdDTO.getModuleId()){
			case 1:
				user.setModule1(1);
				break;
			case 2:
				user.setModule2(1);
				break;
			case 3:
				user.setModule3(1);
				break;
			case 4:
				user.setModule4(1);
				break;
			default:
				return ResultData.fail(RC203.getCode(),RC203.getMessage());
		}
		return ResultData.success(userService.updateById(user));
	}
}
