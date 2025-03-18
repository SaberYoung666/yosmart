package com.swpu.yosmart.service;

import com.swpu.yosmart.entity.WorkTime;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swpu.yosmart.entity.vo.RecentWorkTimeVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author saber
 * @since 2025-03-16
 */
public interface IWorkTimeService extends IService<WorkTime> {

	/**
	 * 获取当前用户近七天的工作时长
	 * @return
	 */
	List<RecentWorkTimeVO> getRecentWorkTimes();
}
