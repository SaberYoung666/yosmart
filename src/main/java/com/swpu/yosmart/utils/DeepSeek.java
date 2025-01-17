package com.swpu.yosmart.utils;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeepSeek {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = "sk-3e831120b97141af90c3ce5720393c94"; // 替换为你的 API Key

    /**
     * 解析任务描述并返回 JSON 格式的结果
     *
     * @param taskDescription 任务描述
     * @return JSON 格式的任务解析结果
     */
    public static JSONObject askQuestion(String taskDescription) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTime = now.format(formatter);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");

        // 用户输入，包含当前时间，并明确要求不要生成时间相关的标签
        String userPrompt = "当前时间是: " + startTime + "\n\n" + taskDescription + "\n\n请根据任务描述生成以下 JSON 格式的结果：\n" +
                "{\n" +
                "    \"description\": \"任务描述\",\n" +
                "    \"startTime\": \"当前时间（YYYY-MM-DDThh:mm:ss）\",\n" +
                "    \"endTime\": \"根据任务描述推断的截止时间（YYYY-MM-DDThh:mm:ss）\",\n" +
                "    \"estimatedTime\": \"根据任务描述推断的预计完成时间（以 30 分钟为单位）\",\n" +
                "    \"tags\": [\"根据任务描述推断的标签（不要包含时间相关的标签）\"]\n" +
                "}";

        JSONObject[] messages = new JSONObject[]{
                new JSONObject().put("role", "user").put("content", userPrompt)
        };
        requestBody.put("messages", messages);
        requestBody.put("response_format", new JSONObject().put("type", "json_object"));

        // 发送请求
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            // 解析响应
            JSONObject responseJson = new JSONObject(response.body());

            // 检查响应中是否包含 "choices" 字段
            if (!responseJson.has("choices")) {
                System.err.println("API 响应中缺少 'choices' 字段，完整响应如下：");
                System.err.println(responseJson.toString(4)); // 打印完整响应以便调试
                return null;
            }

            // 提取 "choices" 字段
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() == 0) {
                System.err.println("API 响应中 'choices' 字段为空，完整响应如下：");
                System.err.println(responseJson.toString(4)); // 打印完整响应以便调试
                return null;
            }

            // 提取第一个 choice 的 message 内容
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return new JSONObject(message.getString("content"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}