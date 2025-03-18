package com.swpu.yosmart.mapper;

import com.swpu.yosmart.entity.WorkTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swpu.yosmart.entity.vo.RecentWorkTimeVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author saber
 * @since 2025-03-16
 */
public interface WorkTimeMapper extends BaseMapper<WorkTime> {

	@Select("SELECT DATE(start_time) AS work_date, SUM(duration_minutes) AS total_duration FROM work_time WHERE user_id = ${user_id} AND start_time >= CURDATE() - INTERVAL 6 DAY GROUP BY work_date ORDER BY work_date DESC;")
	List<RecentWorkTimeVO> getRecentWorkTimes(@Param("user_id") Integer userId);
}
