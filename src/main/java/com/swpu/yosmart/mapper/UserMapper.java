package com.swpu.yosmart.mapper;

import com.swpu.yosmart.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author saber
 * @since 2025-01-15
 */
@Repository
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
