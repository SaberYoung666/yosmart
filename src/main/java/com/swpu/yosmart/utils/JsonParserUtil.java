package com.swpu.yosmart.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swpu.yosmart.entity.dto.apidto.ApiResponseDTO;
import com.swpu.yosmart.entity.dto.apidto.TaskContentDTO;

/**
 * 这个工具类专用于将调用大模型得到的响应映射到任务对象
 */
public class JsonParserUtil {
    public static Object parse(String json) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponseDTO apiResponse = objectMapper.readValue(json, ApiResponseDTO.class);

        String contentStr = apiResponse.getChoices().get(0).getMessage().getContent();
        contentStr = contentStr.replace("'", "\"")
                .replace("False", "false")
                .replace("True", "true");

        return objectMapper.readValue(contentStr, TaskContentDTO.class);
    }

    public static String healthAdviceParse(String json) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponseDTO apiResponse = objectMapper.readValue(json, ApiResponseDTO.class);

        String contentStr = apiResponse.getChoices().get(0).getMessage().getContent();
        contentStr = contentStr.replace("'", "\"");

        return contentStr;
    }
}
