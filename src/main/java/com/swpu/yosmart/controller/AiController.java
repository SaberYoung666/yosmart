package com.swpu.yosmart.controller;

import com.swpu.yosmart.entity.dto.AiDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.swpu.yosmart.utils.DeepSeek.askQuestion;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {
    @PostMapping("/fuzzyInput")
    public String fuzzyInput(@RequestBody AiDTO aiDTO){
        JSONObject result = askQuestion(aiDTO.getQuestion());
        if (result != null) {
            return result.toString(4);
        } else {
            return  "用户提问解释错误";
        }
    }
}
