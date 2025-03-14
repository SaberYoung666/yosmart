package com.swpu.yosmart.controller;


import com.swpu.yosmart.utils.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping()
public class TryController {

    @GetMapping("/try")
    public ResultData tryit() {
        return ResultData.success();
    }
}
