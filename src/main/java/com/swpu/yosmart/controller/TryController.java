package com.swpu.yosmart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping()
public class TryController {

	@GetMapping("/try")
	public String tryit() {
		log.info("get try");
		return "try success";
	}
}
