package com.arcrecord.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ImportDataToSqlUtils {


    /**
     * [读取标签文本信息 CHS]
     * @param fileName 文件路径
     * @return 每行对应一个 Map<Integer,String>
     * @throws Exception IO 异常直接抛出
     */
    public static Map<String, String> readChs(String fileName) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        Files.lines(Paths.get(fileName))
                .map(String::trim)
                .filter(line -> line.contains("@"))          // 确保有 @
                .forEach(line -> {
                    int pos = line.indexOf('@');             // 只找第一个 @
                    String key   = line.substring(0, pos);
                    String value = line.substring(pos + 1);
                    result.put(key, value);
                });
        return result;
    }

    /**
     * [读取英雄信息]
     * @param fileName 文件路径
     * @return 每行对应一个 Map<Integer,String>
     * @throws Exception IO 异常直接抛出
     */
    public static List<Map<String, String>> readHreo(String fileName) throws Exception {
        return Files.lines(Paths.get(fileName))
                .filter(l -> l.startsWith("H"))
                .map(l -> l.split("@", -1))
                .filter(arr -> arr.length > 65 && "HERO".equals(arr[65]))
                .map(arr -> {
                    Map<String,String> m = new LinkedHashMap<>();
                    for (int i = 0; i < arr.length; i++) m.put(String.valueOf(i), arr[i]);
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析LineDialog.txt文件
     * @param filePath 文件路径（如：D:/data/LineDialog.txt）
     * @return List<Map<String, String>> 解析后的数据列表
     */
    public static List<Map<String, String>> readLineDialog(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)) // 流式读取文件所有行
                // 步骤1：跳过第一行（通过索引过滤，index从0开始）
                .skip(1)
                // 步骤2：过滤空行（仅保留非空行，trim()排除仅含空格的行）
                .filter(line -> !line.trim().isEmpty())
                // 步骤3：按@分割行，-1保留末尾所有空字段
                .map(line -> line.split("@", -1))
                // 步骤4：将分割后的数组转为LinkedHashMap（保证key顺序）
                .map(arr -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (int i = 0; i < arr.length; i++) {
                        // key从1开始（1、2、3...），value为分割后的字段（空字符串保留）
                        map.put(String.valueOf(i), arr[i]);
                    }
                    return map;
                })
                // 步骤5：收集所有Map为List
                .collect(Collectors.toList());
    }


    /**
     * 解析LineDialog.txt文件
     * @param filePath 文件路径（如：D:/data/LineDialog.txt）
     * @return List<Map<String, String>> 解析后的数据列表
     */
    public static List<Map<String, String>> readRoleCollection(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)) // 流式读取文件所有行
                // 步骤1：跳过第一行（通过索引过滤，index从0开始）
                .skip(1)
                // 步骤2：过滤空行（仅保留非空行，trim()排除仅含空格的行）
                .filter(line -> !line.trim().isEmpty())
                // 步骤3：按@分割行，-1保留末尾所有空字段
                .map(line -> line.split("@", -1))
                // 步骤4：将分割后的数组转为LinkedHashMap（保证key顺序）
                .map(arr -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (int i = 0; i < arr.length; i++) {
                        // key从1开始（1、2、3...），value为分割后的字段（空字符串保留）
                        map.put(String.valueOf(i), arr[i]);
                    }
                    return map;
                })
                // 步骤5：收集所有Map为List
                .collect(Collectors.toList());
    }

    /**
     * 解析LineDialog.txt文件
     * @param filePath 文件路径（如：D:/data/LineDialog.txt）
     * @return List<Map<String, String>> 解析后的数据列表
     */
    public static List<Map<String, String>> readLineDrama(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)) // 流式读取文件所有行
                // 步骤1：跳过第一行（通过索引过滤，index从0开始）
                .skip(1)
                // 步骤2：过滤空行（仅保留非空行，trim()排除仅含空格的行）
                .filter(line -> !line.trim().isEmpty())
                // 步骤3：按@分割行，-1保留末尾所有空字段
                .map(line -> line.split("@", -1))
                // 步骤4：将分割后的数组转为LinkedHashMap（保证key顺序）
                .map(arr -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (int i = 0; i < arr.length; i++) {
                        // key从1开始（1、2、3...），value为分割后的字段（空字符串保留）
                        map.put(String.valueOf(i), arr[i]);
                    }
                    return map;
                })
                // 步骤5：收集所有Map为List
                .collect(Collectors.toList());
    }
}
