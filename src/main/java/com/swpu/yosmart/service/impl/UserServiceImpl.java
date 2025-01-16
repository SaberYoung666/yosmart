package com.swpu.yosmart.service.impl;

import com.swpu.yosmart.entity.User;
import com.swpu.yosmart.mapper.UserMapper;
import com.swpu.yosmart.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author saber
 * @since 2025-01-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
