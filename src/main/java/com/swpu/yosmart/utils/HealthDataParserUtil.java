package com.swpu.yosmart.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * 健康数据解析工具类（修复版）
 * 完整支持步数和睡眠数据入库
 */
public class HealthDataParserUtil {

    // 数据类型映射
    private static final Map<String, String> DATA_TYPES = Map.of(
            "HKQuantityTypeIdentifierStepCount", "StepCount",
            "HKCategoryTypeIdentifierSleepAnalysis", "SleepAnalysis"  // 确保表名存在
    );

    // 聚合方法
    private static final Map<String, String> AGGREGATION_METHODS = Map.of(
            "HKQuantityTypeIdentifierStepCount", "sum",
            "HKCategoryTypeIdentifierSleepAnalysis", "duration"  // 与睡眠计算逻辑匹配
    );

    public static void parseHealthDataToDatabase(String xmlFilePath, String dbUrl,
                                                 String dbUser, String dbPassword, int userId)
            throws IOException, ParserConfigurationException, SAXException, SQLException {

        validateFilePath(xmlFilePath);
        Map<String, Map<String, DailyData>> dailyData = parseXmlAndGroupData(xmlFilePath);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            for (Map.Entry<String, Map<String, DailyData>> dateEntry : dailyData.entrySet()) {
                String date = dateEntry.getKey();
                for (Map.Entry<String, DailyData> typeEntry : dateEntry.getValue().entrySet()) {
                    String recordType = typeEntry.getKey();
                    DailyData data = typeEntry.getValue();
                    insertIntoDatabase(conn, date, recordType, data, userId);
                }
            }
        }
        System.out.println("处理完成！步数和睡眠数据均已入库");
    }

    private static Map<String, Map<String, DailyData>> parseXmlAndGroupData(String xmlFilePath)
            throws ParserConfigurationException, SAXException, IOException {

        Map<String, Map<String, DailyData>> dailyData = new HashMap<>();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(xmlFilePath));
        NodeList records = doc.getElementsByTagName("Record");

        System.out.println("发现记录数: " + records.getLength());

        // 使用系统时区处理日期
        ZoneId systemZone = ZoneId.systemDefault();
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        for (int i = 0; i < records.getLength(); i++) {
            Element record = (Element) records.item(i);
            try {
                String type = record.getAttribute("type");
                if (!DATA_TYPES.containsKey(type)) continue;

                // 带时区解析（修正跨时区问题）
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
                ZonedDateTime startZdt = ZonedDateTime.parse(
                                record.getAttribute("startDate"), formatter)
                        .withZoneSameInstant(systemZone);  // 转换为本地时区

                LocalDate recordDate = startZdt.toLocalDate();
                if (recordDate.isBefore(thirtyDaysAgo)) continue;

                String dateKey = recordDate.toString();
                DailyData daily = dailyData
                        .computeIfAbsent(dateKey, k -> new HashMap<>())
                        .computeIfAbsent(type, k -> new DailyData());

                // 特殊处理睡眠数据（精确计算持续时长）
                if ("HKCategoryTypeIdentifierSleepAnalysis".equals(type)) {
                    ZonedDateTime endZdt = ZonedDateTime.parse(
                                    record.getAttribute("endDate"), formatter)
                            .withZoneSameInstant(systemZone);

                    long seconds = ChronoUnit.SECONDS.between(startZdt, endZdt);
                    double hours = seconds / 3600.0;
                    // 保留两位小数
                    hours = Math.round(hours * 100.0) / 100.0;
                    // 特殊处理：如果睡眠时间小于0.01小时（约36秒），视为无效数据
                    if (hours < 0.01) {
                        System.out.printf("[WARN] 跳过无效睡眠记录 %s（时长 %.2f 小时）%n", dateKey, hours);
                        continue;
                    }
                    daily.values.add(hours);
                    System.out.printf("[DEBUG] 睡眠记录 %s 时长: %.2f 小时%n", dateKey, hours); // 调试日志
                } else {
                    double value = Double.parseDouble(record.getAttribute("value"));
                    daily.values.add(value);
                }

            } catch (DateTimeException | NumberFormatException e) {
                System.err.println("记录解析失败（索引 " + i + "）: " + e.getMessage());
            }
        }
        return dailyData;
    }

    private static void insertIntoDatabase(Connection conn, String date,
                                           String recordType, DailyData data, int userId) throws SQLException {

        String table = DATA_TYPES.get(recordType);
        String checkSql = String.format("SELECT COUNT(*) FROM %s WHERE date = ? AND type = ? AND user_id = ?", table);
        String insertSql = String.format(
                "INSERT INTO %s (date, type, value, user_id) " +
                        "VALUES (?, ?, ?, ?)", table);

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, date);
            checkStmt.setString(2, recordType);
            checkStmt.setInt(3, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.printf("[INFO] 跳过已存在记录 %s/%s/%d%n", table, date, userId);
                return;
            }
        }

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, date);
            insertStmt.setString(2, recordType);
            double value = calculateAggregatedValue(recordType, data.values);
            // 保留两位小数
            value = Math.round(value * 100.0) / 100.0;
            // 特殊处理：如果睡眠时间聚合值小于0.01小时，设置为0.5小时（可调整）
            if ("HKCategoryTypeIdentifierSleepAnalysis".equals(recordType) && value < 0.01) {
                value = 0.5; // 设置默认值
                System.out.printf("[INFO] 睡眠时间过短，设置为默认值 %.2f 小时 %n", value);
            }
            insertStmt.setDouble(3, value);
            insertStmt.setInt(4, userId);

            insertStmt.executeUpdate();
            System.out.printf("[SUCCESS] 插入 %s 表 %s 数据%n", table, date);
        } catch (SQLException e) {
            System.err.printf("[ERROR] 插入失败 %s/%s: %s%n", table, date, e.getMessage());
            throw e;
        }
    }

    // 辅助方法：计算聚合值
    private static double calculateAggregatedValue(String recordType, List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return AGGREGATION_METHODS.get(recordType).equals("sum") ?
                values.stream().mapToDouble(Double::doubleValue).sum() :
                values.stream().mapToDouble(Double::doubleValue).sum(); // 睡眠直接使用总时长
    }

    // 文件校验
    private static void validateFilePath(String path) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + path);
        }
        if (file.isDirectory()) {
            throw new FileNotFoundException("路径指向目录而非文件: " + path);
        }
    }

    // 数据容器
    private static class DailyData {
        List<Double> values = new ArrayList<>();

        public boolean isValid() {
            return !values.isEmpty();
        }
    }
}