package com.swpu.yosmart.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swpu.yosmart.entity.User;
import com.swpu.yosmart.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swpu.yosmart.service.IUserService;
import org.springframework.context.annotation.Primary;
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
