package com.swpu.yosmart.controller;

import com.swpu.yosmart.context.BaseContext;
import com.swpu.yosmart.entity.dto.AddHealthData;

import com.swpu.yosmart.utils.HealthDataParserUtil;
import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/HealthData")
@Slf4j
public class HealthDataController {
    @PostMapping("/add")
    public ResultData<Boolean> addHealthData(@RequestBody AddHealthData addHealthData) {
        String xmlFilePath = addHealthData.getXmlFilePath();
        String dbUrl = "jdbc:mysql://139.159.150.15:3306/yosmart?useUnicode=true&characterEncoding=utf8&useSSL=false";
        String dbUser = "root";
        String dbPassword = "Ysb040901!";
        int userId = BaseContext.getUserId(); // 用户 ID

        try {
            HealthDataParserUtil.parseHealthDataToDatabase(xmlFilePath, dbUrl, dbUser, dbPassword, userId);
            System.out.println("数据导入完成！");
            return ResultData.success(true);
        } catch (Exception e) {
            return ResultData.success(false);
        }
    }

  /*  @GetMapping("/gethealthdata")
    public ResultData<Boolean> getHealthData() {
        //当前日期
        LocalDate currentDate = LocalDate.now();

    }
*/
}
