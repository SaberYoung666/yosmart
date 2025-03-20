package com.swpu.yosmart.service;

import com.swpu.yosmart.entity.vo.TaskVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务类
 */
public interface ITaskService {
	/**
	 * 提取出的方法，可以获取当前用户某日日程，并且用VO类包装
	 *
	 * @return
	 */
	List<TaskVO> getOneDayTaskVOS(LocalDate oneDay);

	/**
	 * 判断一个任务是否属于某个用户
	 *
	 * @param taskId
	 * @param taskId
	 * @return
	 */
	Boolean isRelatedTo(Long taskId);

	/**
	 * 模糊查询
	 * @param keyword
	 * @return
	 */
	List<TaskVO> fuzzySearch(String keyword);
}
