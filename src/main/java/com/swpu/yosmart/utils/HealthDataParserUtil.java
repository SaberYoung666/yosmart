package com.swpu.yosmart.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HealthDataParserUtil {

	// 数据类型映射
	private static final Map<String, String> DATA_TYPES = Map.of("HKQuantityTypeIdentifierStepCount", "StepCount", "HKCategoryTypeIdentifierSleepAnalysis", "SleepAnalysis", "HKQuantityTypeIdentifierHeartRate", "HeartRate");

	// 聚合方法
	private static final Map<String, String> AGGREGATION_METHODS = Map.of("HKQuantityTypeIdentifierStepCount", "sum", "HKCategoryTypeIdentifierSleepAnalysis", "duration", "HKQuantityTypeIdentifierHeartRate", "average");

	public static void parseHealthDataToDatabase(MultipartFile file, String dbUrl, String dbUser, String dbPassword, int userId) throws IOException, ParserConfigurationException, SAXException, SQLException {

		Map<String, Map<String, Map<String, DailyData>>> deviceData = parseXmlAndGroupByDevice(file);

		try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
			for (Map.Entry<String, Map<String, Map<String, DailyData>>> deviceEntry : deviceData.entrySet()) {
				String device = deviceEntry.getKey();
				for (Map.Entry<String, Map<String, DailyData>> dateEntry : deviceEntry.getValue().entrySet()) {
					String date = dateEntry.getKey();
					for (Map.Entry<String, DailyData> typeEntry : dateEntry.getValue().entrySet()) {
						insertDataToDatabase(conn, date, typeEntry.getKey(), typeEntry.getValue(), userId, device);
					}
				}
			}
		}
		System.out.println("数据处理完成，不同设备数据已独立存储");
	}

	private static Map<String, Map<String, Map<String, DailyData>>> parseXmlAndGroupByDevice(MultipartFile file) throws ParserConfigurationException, SAXException, IOException {
		// MultipartFile转File
		File tempFile = File.createTempFile("temp", null);
		FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);

		Map<String, Map<String, Map<String, DailyData>>> deviceData = new HashMap<>();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempFile);
		NodeList records = doc.getElementsByTagName("Record");

		ZoneId systemZone = ZoneId.systemDefault();
		LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

		for (int i = 0;i < records.getLength();i++) {
			Element record = (Element) records.item(i);
			try {
				String type = record.getAttribute("type");
				if (!DATA_TYPES.containsKey(type)) continue;

				// 解析时间并转换时区
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
				ZonedDateTime startZdt = ZonedDateTime.parse(record.getAttribute("startDate"), formatter).withZoneSameInstant(systemZone);
				LocalDate recordDate = startZdt.toLocalDate();

				if (recordDate.isBefore(thirtyDaysAgo)) continue;

				// 获取设备信息
				String device = record.getAttribute("sourceName");
				if (device.isEmpty()) {
					device = parseDeviceFromXml(record.getAttribute("device"));
				}

				// 初始化多层 Map 结构
				deviceData.computeIfAbsent(device, k -> new HashMap<>()).computeIfAbsent(recordDate.toString(), k -> new HashMap<>()).computeIfAbsent(type, k -> new DailyData());

				// 处理具体数据
				DailyData dailyData = deviceData.get(device).get(recordDate.toString()).get(type);
				if ("HKCategoryTypeIdentifierSleepAnalysis".equals(type)) {
					ZonedDateTime endZdt = ZonedDateTime.parse(record.getAttribute("endDate"), formatter).withZoneSameInstant(systemZone);
					double hours = ChronoUnit.SECONDS.between(startZdt, endZdt) / 3600.0;
					if (hours >= 0.01) { // 过滤无效数据
						dailyData.values.add(hours);
					}
				} else if ("HKQuantityTypeIdentifierHeartRate".equals(type)) {
					double value = Double.parseDouble(record.getAttribute("value"));
					// 可选：过滤异常心率值
					if (value >= 30 && value <= 200) {
						dailyData.values.add(value);
					} else {
						log.info("[WARN] 跳过异常心率值 %.1f%n", value);
					}
				} else {
					dailyData.values.add(Double.parseDouble(record.getAttribute("value")));
				}

			} catch (Exception e) {
				System.err.printf("解析记录 %d 失败: %s%n", i, e.getMessage());
			}
		}
		return deviceData;
	}

	private static void insertDataToDatabase(Connection conn, String date, String recordType, DailyData data, int userId, String device) throws SQLException {

		String table = DATA_TYPES.get(recordType);
		String checkSql = "SELECT COUNT(*) FROM " + table + " WHERE date = ? AND type = ? AND user_id = ? AND device = ?";
		String insertSql = "INSERT INTO " + table + " (date, type, value, user_id, device) VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
			checkStmt.setString(1, date);
			checkStmt.setString(2, recordType);
			checkStmt.setInt(3, userId);
			checkStmt.setString(4, device);
			ResultSet rs = checkStmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				/*  System.out.printf("跳过已存在记录：%s/%s/%s%n", date, recordType, device);*/
				return;
			}
		}

		try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
			double value = calculateAggregatedValue(recordType, data.values);
			value = Math.round(value * 100.0) / 100.0;

			insertStmt.setString(1, date);
			insertStmt.setString(2, recordType);
			insertStmt.setDouble(3, value);
			insertStmt.setInt(4, userId);
			insertStmt.setString(5, device);

			insertStmt.executeUpdate();
			/*          System.out.printf("成功插入数据：%s/%s/%s%n", date, recordType, device);*/
		}
	}

	// 从 device 属性中解析设备名称
	private static String parseDeviceFromXml(String deviceXml) {
		try {
			int start = deviceXml.indexOf("name:") + 5;
			int end = deviceXml.indexOf(",", start);
			return deviceXml.substring(start, end).trim();
		} catch (Exception e) {
			return "UnknownDevice";
		}
	}


	// 辅助方法：计算聚合值
	private static double calculateAggregatedValue(String recordType, List<Double> values) {
		if (values.isEmpty()) return 0.0;
		switch (AGGREGATION_METHODS.get(recordType)) {
			case "sum":
				return values.stream().mapToDouble(Double::doubleValue).sum();
			case "duration":
				return values.stream().mapToDouble(Double::doubleValue).sum();
			case "average":
				return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
			default:
				return 0.0;
		}
	}

	// 数据容器
	private static class DailyData {
		List<Double> values = new ArrayList<>();
	}
}