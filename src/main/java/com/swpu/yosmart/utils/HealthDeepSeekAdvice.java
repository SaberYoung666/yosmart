package com.swpu.yosmart.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.swpu.yosmart.constant.DeepseekConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HealthDeepSeekAdvice {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json");

    // 配置自定义超时的 OkHttpClient
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)  // 连接服务器超时
            .readTimeout(60, TimeUnit.SECONDS)     // 服务器响应超时（关键调整项）
            .writeTimeout(30, TimeUnit.SECONDS)   // 请求发送超时
            .build();

    private static final String apiKey = DeepseekConstant.API_KEY;

    // Jackson 对象，用于 JSON 转换
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 获取健康建议
     *
     * @param healthData 健康数据（List<Map<String, Object>>，包含步数、睡眠、心率数据）
     * @return API 响应内容
     * @throws IOException 如果请求失败
     */
    public static String getHealthAdvice(List<Map<String, Object>> healthData) throws IOException {
        // 获取当前时间
        LocalDate currentTime = LocalDate.now();

        // 构建请求体
        Map<String, Object> requestBody = buildRequestBody(healthData, currentTime);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 打印请求体以调试
        log.debug("Request Body: {}", jsonBody);

        // 创建 RequestBody
        RequestBody body = RequestBody.create(JSON, jsonBody);

        // 创建 Request
        Request request = buildRequest(body);

        // 发送请求并获取响应
        return executeRequest(request);
    }

    /**
     * 构建请求体
     *
     * @param healthData  健康数据（List<Map<String, Object>>）
     * @param currentTime 当前时间
     * @return 请求体 Map
     */
    private static Map<String, Object> buildRequestBody(List<Map<String, Object>> healthData, LocalDate currentTime) {
        Map<String, Object> requestBody = new HashMap<>();

        List<Map<String, Object>> messages = new ArrayList<>();

        // 构建 system 消息
        Map<String, Object> systemMessage = new HashMap<>();
        String content = "你是一个专业的健康建议顾问，需要基于用户的步数、睡眠和心率数据提供健康建议。以下是具体要求：\n" +
                "1. 分析用户的健康数据，包括步数、睡眠时长和心率。\n" +
                "2. 根据数据分析结果，提供有针对性的健康建议。\n" +
                "3. 建议应包括但不限于：\n" +
                "   - 运动建议：基于步数数据，建议合适的运动量和运动强度。\n" +
                "   - 睡眠建议：基于睡眠数据，建议改善睡眠质量的方法。\n" +
                "   - 心率建议：基于心率数据，建议合适的心率范围和注意事项。\n" +
                "4. 严格检测数据异常，结合用户输入的健康数据进行分析\n";
        systemMessage.put("content", content);
        systemMessage.put("role", "system");
        messages.add(systemMessage);

        // 构建 user 消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("content", "我的健康数据是：" + objectMapper.valueToTree(healthData) + "，请分析并给我建议");
        userMessage.put("role", "user");
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("model", "deepseek-chat");
        requestBody.put("frequency_penalty", 0);
        requestBody.put("max_tokens", 2048);
        requestBody.put("presence_penalty", 0);
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "text");
        requestBody.put("response_format", responseFormat);
        requestBody.put("stop", null);
        requestBody.put("stream", false);
        requestBody.put("stream_options", null);
        requestBody.put("temperature", 1);
        requestBody.put("top_p", 1);
        requestBody.put("tools", null);
        requestBody.put("tool_choice", "none");
        requestBody.put("logprobs", false);
        requestBody.put("top_logprobs", null);

        return requestBody;
    }

    /**
     * 构建请求对象
     *
     * @param body 请求体
     * @return 请求对象
     */
    private static Request buildRequest(RequestBody body) {
        return new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * 执行请求并获取响应
     *
     * @param request 请求对象
     * @return API 响应内容
     * @throws IOException 如果请求失败
     */
    private static String executeRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("请求失败，状态码: " + response.code() + "，响应内容: " + (response.body() != null ? response.body().string() : "空响应"));
            }
        }
    }
}