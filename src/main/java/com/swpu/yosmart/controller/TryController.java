package com.swpu.yosmart.controller;


import com.swpu.yosmart.service.ITaskService;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResultData tryit() {
		return ResultData.success();
	}
}
