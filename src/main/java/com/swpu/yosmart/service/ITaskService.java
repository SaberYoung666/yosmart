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
	 * @return
	 */
	List<TaskVO> getOneDayTaskVOS(LocalDate oneDay);

	/**
	 * 更新指定任务的任务状态
	 * @param taskId
	 */
	void updateTaskStatus(Long taskId, Integer status);

	/**
	 * 判断一个任务是否属于某个用户
	 * @param userName
	 * @param taskId
	 * @return
	 */
	Boolean isRelatedTo(String userName, Long taskId);
}
