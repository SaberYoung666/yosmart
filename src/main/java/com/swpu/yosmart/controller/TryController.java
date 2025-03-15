package com.swpu.yosmart.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.swpu.yosmart.utils.JsonParserUtil;
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
    /*
    不要往这个类里面写功能！！仅作为测试用
     */

    @GetMapping("/try")
    public ResultData tryit() throws JsonProcessingException {
        String json = "{\"id\":\"28b56d53-1bf9-4e5f-b033-4ba2754bcb7d\",\"object\":\"chat.completion\",\"created\":1742004892,\"model\":\"deepseek-chat\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"{'main_task': '准备六级考试', 'sub_tasks': [{'description': '购买六级考试教材和真题', 'priority': 5, 'repeat': False, 'startTime': '2025-03-16-10-00', 'endTime': '2025-03-16-12-00', 'tags': ['学习', '准备']}, {'description': '制定学习计划，分配每天学习时间', 'priority': 5, 'repeat': False, 'startTime': '2025-03-16-14-00', 'endTime': '2025-03-16-16-00', 'tags': ['学习', '规划']}, {'description': '每天复习词汇和语法', 'priority': 4, 'repeat': True, 'startTime': '2025-03-17-09-00', 'endTime': '2025-04-15-10-00', 'tags': ['学习', '复习']}, {'description': '每周完成一套真题模拟', 'priority': 4, 'repeat': True, 'startTime': '2025-03-22-14-00', 'endTime': '2025-04-12-16-00', 'tags': ['学习', '模拟']}, {'description': '参加六级考试', 'priority': 5, 'repeat': False, 'startTime': '2025-04-19-09-00', 'endTime': '2025-04-19-11-00', 'tags': ['学习', '考试']}]}\"},\"logprobs\":null,\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":301,\"completion_tokens\":333,\"total_tokens\":634,\"prompt_tokens_details\":{\"cached_tokens\":192},\"prompt_cache_hit_tokens\":192,\"prompt_cache_miss_tokens\":109},\"system_fingerprint\":\"fp_3a5770e1b4_prod0225\"}";
        Object parse = JsonParserUtil.parse(json);
        return ResultData.success(parse);
    }
}
