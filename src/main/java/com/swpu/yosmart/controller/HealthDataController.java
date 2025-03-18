package com.swpu.yosmart.controller;

import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.dto.AddHealthData;
import com.swpu.yosmart.service.IHealthDataService;
import com.swpu.yosmart.utils.HealthDataParserUtil;
import com.swpu.yosmart.utils.HealthDeepSeekAdvice;
import com.swpu.yosmart.utils.JsonParserUtil;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/HealthData")
@Slf4j
public class HealthDataController {
	@Autowired
	private IHealthDataService healthDataService;

	@PostMapping("/add")
	public ResultData<Boolean> addHealthData(@RequestBody AddHealthData addHealthData) throws SQLException, IOException, ParserConfigurationException, SAXException {

		String xmlFilePath = addHealthData.getXmlFilePath();
		String dbUrl = "jdbc:mysql://139.159.150.15:3306/yosmart?useUnicode=true&characterEncoding=utf8&useSSL=false";
		String dbUser = "root";
		String dbPassword = "Ysb040901!";
		int userId = BaseContext.getUserId(); // 用户 ID
		HealthDataParserUtil.parseHealthDataToDatabase(xmlFilePath, dbUrl, dbUser, dbPassword, userId);
		System.out.println("数据导入完成！");
		return ResultData.success(true);

	}

	@GetMapping("/getAllHealthData")
	public ResultData<List<Map<String, Object>>> getAllHealthData() {
		// 获取当前日期
		LocalDate endDate = LocalDate.now();

		// 查询数据
		List<Map<String, Object>> data = healthDataService.getAllHealthData(endDate);

		// 检查数据是否为空
		if (data.isEmpty()) {
			return ResultData.success("没有数据!");
		} else {
			return ResultData.success(data);
		}

	}

	@GetMapping("/getHealthDataToDay")
	public ResultData<List<Map<String, Object>>> getByToday() {
		// 获取当前日期
		LocalDate currentDate = LocalDate.now();
		// 查询数据
		List<Map<String, Object>> data = healthDataService.getHealthDataByToday(currentDate);

		// 检查数据是否为空
		if (data.isEmpty()) {
			return ResultData.success("请用户上传新的数据，当日数据为空！");
		} else {
			return ResultData.success(data);
		}
	}

	@GetMapping("/healthAdvice")
	public ResultData<String> healthAdvice() throws IOException {
		// 获取当前日期
		LocalDate currentDate = LocalDate.now();
		// 查询数据
		List<Map<String, Object>> healthData = healthDataService.getHealthDataByToday(currentDate);

		String advice = HealthDeepSeekAdvice.getHealthAdvice(healthData);

		return ResultData.success(JsonParserUtil.healthAdviceParse(advice));

	}
}