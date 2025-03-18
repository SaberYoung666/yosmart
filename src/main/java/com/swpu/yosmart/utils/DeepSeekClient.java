package com.swpu.yosmart.utils;

import com.swpu.yosmart.constant.DeepseekConstant;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DeepSeekClient {

	private static final String API_URL = "https://api.deepseek.com/chat/completions";
	private static final MediaType JSON = MediaType.parse("application/json");

	// 配置自定义超时的 OkHttpClient
	private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)  // 连接服务器超时
			.readTimeout(60, TimeUnit.SECONDS)     // 服务器响应超时（关键调整项）
			.writeTimeout(30, TimeUnit.SECONDS)   // 请求发送超时
			.build();
	private static final String apiKey = DeepseekConstant.API_KEY;

	/**
	 * 发送任务规划请求
	 *
	 * @param existingTasks    现有任务列表（JSON 字符串）
	 * @param tasksGoingToPlan 待规划的任务（字符串）
	 * @return API 响应内容
	 * @throws IOException 如果请求失败
	 */
	public static String planTasks(String existingTasks, String tasksGoingToPlan) throws IOException {
		// 获取当前时间，并格式化为指定格式
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		String currentTime = dateFormat.format(new Date());

		// 构建请求体
		String jsonBody = buildRequestBody(existingTasks, tasksGoingToPlan, currentTime);

		// 创建 RequestBody
		RequestBody body = RequestBody.create(JSON, jsonBody);

		// 创建 Request
		Request request = new Request.Builder().url(API_URL).post(body).addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + apiKey).build();

		// 发送请求并获取响应
		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				return response.body().string();
			} else {
				throw new IOException("请求失败，状态码: " + response.code() + "，响应内容: " + (response.body() != null ? response.body().string() : "空响应"));
			}
		}
	}

	// 建议添加重试机制示例
	public static String planTasksWithRetry(String existingTasks, String tasksGoingToPlan) throws IOException {
		int maxRetries = 3;
		IOException exception = null;
		log.info("正在分解任务，任务提示词：{}", tasksGoingToPlan);
		for (int i = 0;i < maxRetries;i++) {
			try {
				return planTasks(existingTasks, tasksGoingToPlan);
			} catch (IOException e) {
				exception = e;
				if (e instanceof SocketTimeoutException) {
					log.info("超时重试 ({}/{})", i + 1, maxRetries);
				}
			}
		}
		throw exception;
	}

	/**
	 * 构建请求体 JSON 字符串
	 *
	 * @param existingTasks    现有任务列表（JSON 字符串）
	 * @param tasksGoingToPlan 待规划的任务（字符串）
	 * @param currentTime      当前时间（格式化后的字符串）
	 * @return 请求体 JSON 字符串
	 */
	private static String buildRequestBody(String existingTasks, String tasksGoingToPlan, String currentTime) {
		return "{\n" + "  \"messages\": [\n" + "    {\n" + "      \"content\": \"你是一个专业的、智能的任务分解、规划引擎，你需要严格遵守以下规则对user的输入进行处理：\\n1.自动识别任务领域（学习/生活/商务等）\\n2.通过联网搜索补充领域知识\\n3.解析用户模糊输入，结合联网搜索进行深度任务拆解\\n4.使用SMART原则拆解任务\\n5.生成带时间安排的子任务列表\\n6.时间安排需符合：\\n-总周期不超过30天\\n-单个任务周期1-7天\\n-包含周末缓冲期\\n7.依赖关系标注准确率需＞95%\\n8.解析用户需求时结合现有任务列表：{" + existingTasks + "}\\n9.严格检测时间冲突，新任务deadline不得与现有任务重叠\\n10.输出严格遵循JSON格式, 只输出任务规划，不输出额外的解释：{'main_task': string, 'sub_tasks': [{'description': string, 'priority': 1-5, 'repeat': bool, 'startTime': 'YYYY-MM-DDThh:mm:ss', 'endTime': 'YYYY-MM-DDThh:mm:ss', 'tags': [tag_names...]}, ...]}\\n11.时间应该从{" + currentTime + "}之后开始规划\\n12.若分解为重复任务,则startTime为'重复周期开始日期+每天开始时刻',endTime为'重复周期结束日期+每天结束时刻'\",\n" + "      \"role\": \"system\"\n" + "    },\n" + "    {\n" + "      \"content\": \"" + tasksGoingToPlan + "\",\n" + "      \"role\": \"user\"\n" + "    }\n" + "  ],\n" + "  \"model\": \"deepseek-chat\",\n" + "  \"frequency_penalty\": 0,\n" + "  \"max_tokens\": 2048,\n" + "  \"presence_penalty\": 0,\n" + "  \"response_format\": {\n" + "    \"type\": \"text\"\n" + "  },\n" + "  \"stop\": null,\n" + "  \"stream\": false,\n" + "  \"stream_options\": null,\n" + "  \"temperature\": 1,\n" + "  \"top_p\": 1,\n" + "  \"tools\": null,\n" + "  \"tool_choice\": \"none\",\n" + "  \"logprobs\": false,\n" + "  \"top_logprobs\": null\n" + "}";
	}
}