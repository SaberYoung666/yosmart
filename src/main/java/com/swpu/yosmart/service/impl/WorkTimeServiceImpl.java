package com.swpu.yosmart.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.WorkTime;
import com.swpu.yosmart.entity.vo.RecentWorkTimeVO;
import com.swpu.yosmart.mapper.WorkTimeMapper;
import com.swpu.yosmart.service.IWorkTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author saber
 * @since 2025-03-16
 */
@Service
public class WorkTimeServiceImpl extends ServiceImpl<WorkTimeMapper, WorkTime> implements IWorkTimeService {

	@Autowired
	private WorkTimeMapper workTimeMapper;

	@Override
	public List<RecentWorkTimeVO> getRecentWorkTimes() {
		return workTimeMapper.getRecentWorkTimes(BaseContext.getUserId());
	}
}
