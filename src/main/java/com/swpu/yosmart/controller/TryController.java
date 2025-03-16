package com.swpu.yosmart.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.swpu.yosmart.entity.vo.TaskVO;
import com.swpu.yosmart.service.ITaskService;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping()
public class TryController {

	@Autowired
	ITaskService taskService;

    /*
    不要往这个类里面写功能！！仅作为测试用
     */

	@GetMapping("/try")
	public ResultData tryit() throws JsonProcessingException {
//		StringBuilder existTasks = new StringBuilder();
//		for (int i = 0;i < 5;i++) {
//			List<TaskVO> oneDayTaskVOS = taskService.getOneDayTaskVOS(LocalDate.now().plusDays(i));
//			oneDayTaskVOS.forEach(oneDayTaskVO -> {
//				existTasks.append("{").append(oneDayTaskVO.getDescription()).append(",StartTime:").append(oneDayTaskVO.getStartTime()).append(",EndTime:").append(oneDayTaskVO.getEndTime()).append(",Priority:").append(oneDayTaskVO.getPriority()).append("}\n");
//			});
//		}
		return ResultData.success();
	}
}
