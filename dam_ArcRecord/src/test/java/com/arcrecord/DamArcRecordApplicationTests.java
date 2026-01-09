package com.arcrecord;

import com.arcrecord.mapper.ImportDataToSqlMapper;
import com.arcrecord.service.HeroService;
import com.arcrecord.utils.DeleteHashFilesUtil;
import com.arcrecord.utils.FileStorageProperties;
import com.arcrecord.utils.ImportDataToSqlUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.arcrecord.utils.ImportDataToSqlUtils.*;


/*
* 首先执行测试中的方法初始化数据，当然 这里已经初始化好一份了。
* */
@SpringBootTest
class DamArcRecordApplicationTests {

    @Resource
    HeroService heroService;

    @Resource
    ImportDataToSqlMapper importDataToSqlMapper;

    //项目目录
    private String projectPath = "D:\\桌面\\ArcRecord\\ArcRecordExport";

    //文件目录
    private String filePath = "D:\\桌面\\ArcRecord\\ArcRecordExport\\Assets\\Game\\StaticData\\";
    private String chsTxtPath =filePath + "Text\\CHS.txt";
    private String roleTxtPath = filePath + "Role.txt";
    private String lineDialogPath = filePath + "LineDialog.txt";
    private String roleCollectionPath = filePath + "RoleCollection.txt";
    private String lineDramaPath = filePath + "LineDrama.txt";
    @Test
    @Rollback(false)
    @Transactional
    void contextLoads() throws Exception {

        deleteFile(projectPath);
        //renameFilesRecursively(projectPath);

        //insertChsMessage(chsTxtPath);
        //insertRoleMessage(roleTxtPath);
       // insertRoleCollection(roleCollectionPath);


        //insertLineDialog(lineDialogPath);
        //insertreadLineDrama(lineDramaPath);

    }


    //删除解包出来的多于文件LINEDRAMA
    public void deleteFile(String projectPath){
        File rootDir = new File(projectPath);
        // 安全校验：目标目录必须存在，且是目录
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.err.println("错误：目标目录不存在或不是有效目录 → " + projectPath);
            return;
        }
        // 递归删除含#的文件/文件夹
        DeleteHashFilesUtil.deleteHashFiles(rootDir);
        System.out.println("清理完成！已删除 " + projectPath + " 下所有名称含#的文件/文件夹");
    }

    /**
     * 递归遍历目录并修改文件后缀（入参改为String类型路径） CG_H002_a.atlas.asset  -> CG_H002_a.atlas
     * @param dirPath 当前遍历目录的字符串路径
     */
    private static void renameFilesRecursively(String dirPath) {
        // 将String路径转换为File对象
        File currentDir = new File(dirPath);

        // 1. 校验当前路径是否为有效目录
        if (!currentDir.exists()) {
            System.out.println("警告：路径不存在 -> " + dirPath);
            return;
        }
        if (!currentDir.isDirectory()) {
            System.out.println("警告：不是目录 -> " + dirPath);
            return;
        }

        // 2. 获取目录下的所有文件/子目录
        File[] files = currentDir.listFiles();
        if (files == null) {
            System.out.println("警告：无法访问目录（可能权限不足）-> " + dirPath);
            return;
        }

        // 3. 遍历处理每个文件/子目录
        for (File file : files) {
            if (file.isDirectory()) {
                // 若是子目录，递归调用（传入子目录的字符串路径）
                renameFilesRecursively(file.getAbsolutePath());
            } else if (file.isFile()) {
                // 若是文件，执行后缀修改
                renameFileBySuffix(file);
            }
        }
    }

    /**
     * 单个文件的后缀修改逻辑
     * @param file 待处理的文件
     */
    private static void renameFileBySuffix(File file) {
        String oldFileName = file.getName();
        String newFileName = null;

        // 判断并替换后缀
        if (oldFileName.endsWith(".atlas.asset")) {
            newFileName = oldFileName.replace(".atlas.asset", ".atlas");
        } else if (oldFileName.endsWith(".skel.asset")) {
            newFileName = oldFileName.replace(".skel.asset", ".skel");
        }

        // 无需改名则直接返回
        if (newFileName == null) {
            return;
        }

        // 构建新文件对象（与原文件同目录）
        File newFile = new File(file.getParentFile(), newFileName);

        // 检查新文件是否已存在，避免覆盖
        if (newFile.exists()) {
            System.out.println("跳过（文件已存在）：" + file.getAbsolutePath() + " -> " + newFile.getName());
            return;
        }

        // 执行重命名
        boolean renamed = file.renameTo(newFile);
        if (renamed) {
            System.out.println("成功：" + file.getAbsolutePath() + " -> " + newFileName);
        } else {
            System.out.println("失败：无法重命名 " + file.getAbsolutePath());
        }
    }




    /*将CHS信息导入导入数据库*/
    public void insertChsMessage(String chsTxtPath) throws Exception {
            Map<String, String> map = ImportDataToSqlUtils.readChs(chsTxtPath);
            List<Map<String,String>> all = map.entrySet()
                    .stream()
                    .map(e -> {
                        Map<String,String> m = new HashMap<>();
                        m.put("deskey", e.getKey());
                        m.put("description", e.getValue());
                        return m;
                    })
                .collect(Collectors.toList());
            int half = 50_000;
            List<Map<String,String>> part1 = all.subList(0, half);
            importDataToSqlMapper.insertChs(part1);
            List<Map<String,String>> part2 = all.subList(half, all.size());
            importDataToSqlMapper.insertChs(part2);
    }

    /*将Role信息对应表格后导入数据库*/
    public void insertRoleMessage(String roleTxtPath) throws Exception {
        List<Map<String, String>> roleList = ImportDataToSqlUtils.readHreo(roleTxtPath);

        for (int row = 0; row < roleList.size(); row++) {
            Map<String, String> r = roleList.get(row);

            // 把角色中文名塞到 68 列
            String realName = importDataToSqlMapper.getRoleName(r.get("2"));
            r.put("68", realName);

            // 去掉 21 列尾部下划线
            String spinePath = r.get("21");
            if (spinePath != null && spinePath.endsWith("_")) {
                r.put("21", spinePath.substring(0, spinePath.length() - 1));
            }

            //41 -44 图片
            String png41 = convert(r.get("41"));
            r.put("41",png41);
            String png42 = convert(r.get("42"));
            r.put("42",png42);
            String png43 = convert(r.get("43"));
            r.put("43",png43);
            String png44 = convert(r.get("44"));
            r.put("44",png44);
        }
        importDataToSqlMapper.insertRole(roleList);
    }

    /*将图鉴信息对应表格后导入数据库*/
    public void insertRoleCollection(String roleCollectionPath) throws IOException {
        List<Map<String, String>> roleCollectionlist = readRoleCollection(roleCollectionPath);
        for (int row = 0; row < roleCollectionlist.size(); row++) {
            Map<String, String> r = roleCollectionlist.get(row);
            String prefabicon = r.get("9");
            if(StringUtils.isNotBlank(prefabicon)){
                prefabicon = convertRoleCollection(prefabicon);
                r.put("9", prefabicon+".png");
            }

            String prefab = r.get("10");
            String dramaids = r.get("11");
            String rolemotions = r.get("12");
            if(StringUtils.isNotBlank(prefab)){
                String lowerSrc = prefab.toLowerCase();
                // 2. 不包含 _LoveTalk，原样返回并拼接.png
                if (lowerSrc.contains("_lovetalk")) {
                    prefab = truncateLoveTalkPath(prefab);
                }
                if(lowerSrc.contains("sex")){
                    prefab = truncateSexPath(prefab);
                }
                if(StringUtils.isNotBlank(dramaids)){
                    prefab =prefab.replaceFirst("\\.png$", "");
                }
                if(rolemotions.contains("CG")){
                    prefab =prefab.replaceFirst("\\.png$", "");
                }
            }
            r.put("10", prefab);
        }
        importDataToSqlMapper.insertroleCollection(roleCollectionlist);
    }


    /*将对话信息导入数据库*/
    public void insertreadLineDrama(String lineDramaPath) throws IOException {
        List<Map<String, String>> roleLineDramalist = readLineDrama(lineDramaPath);

        importDataToSqlMapper.insertreadLineDrama(roleLineDramalist);
    }
    public void insertLineDialog(String lineDialogPath) throws IOException {
        List<Map<String, String>> lineDialoglist = readLineDialog(lineDialogPath);
//        for (int row = 0; row < lineDialoglist.size(); row++) {
//            Map<String, String> r = lineDialoglist.get(row);
//
//            //增加第13行 id
//            // 1. 获取第12个字段的值（key="12"）
//            String field12 = r.get("12");
//
//            // 2. 判断第12个字段是否非空（排除null和空字符串）
//            if (field12 != null && !field12.trim().isEmpty()) {
//                // 3. 处理字段值：截取_前的部分（如H612_01 → H612）
//                String processedValue = processFieldValue(field12);
//                // 4. 将处理后的值放入第13个字段（key="13"）
//                r.put("13", processedValue);
//            } else {
//                // 可选：第12个字段为空时，第13个字段设为空字符串（或不处理）
//                r.put("13", "");
//            }
//        }
        importDataToSqlMapper.insertLineDialog(lineDialoglist);
    }



    /*处理图片尾缀名*/
    public static String convert(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }
        String[] seg = src.split("/");
        if (seg.length != 4) {
            return src;          // 格式不对，原样返回
        }

        // 最后一段：H012_Img[Icon_Head_B_H012]
        String last = seg[3];
        // 正则：分组1=编号，分组2=中括号里的名字
        java.util.regex.Pattern p =
                java.util.regex.Pattern.compile("(.+?)_Img\\[(.+?)\\]");
        java.util.regex.Matcher m = p.matcher(last);
        if (!m.matches()) {
            return src;          // 格式不对，原样返回
        }

        String newName = m.group(2);          // Icon_Head_B_H012
        return seg[0] + "/" + seg[1] + "/" + seg[2] + "/" + newName + ".png";
    }

    /**
     * 仅处理包含 [] 的字符串，提取 [] 内的内容并重构路径
     * 匹配格式：xxx_Img[xxx].png（支持任意路径分段，仅聚焦文件名部分）
     * @param src 原始字符串（如 H004_Img[H004_Sex_LoveTalk].png 或 /Icon_P0022_LoveTalk.png）
     * @return 处理后的字符串（无 [] 则原样返回）
     */
    public static String convertRoleCollection(String src) {

        // 预编译正则：匹配 xxx_Img[xxx] 格式（无后缀），忽略大小写
        final Pattern IMG_PATTERN = Pattern.compile("(.+?)_Img\\[(.+?)\\]", Pattern.CASE_INSENSITIVE);
        // 1. 空值/无[]，直接返回
        if (src == null || src.isEmpty() || !src.contains("[") || !src.contains("]")) {
            return src;
        }

        // 2. 拆分路径：分离目录和文件名（兼容任意分段）
        String directory = "";
        String fileName = src;
        int lastSlashIndex = src.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            directory = src.substring(0, lastSlashIndex + 1); // 保留末尾的/
            fileName = src.substring(lastSlashIndex + 1);
        }

        // 3. 匹配文件名中的 xxx_Img[xxx] 格式（无后缀）
        Matcher m = IMG_PATTERN.matcher(fileName);
        if (!m.matches()) {
            return src; // 格式不匹配，原样返回
        }

        // 4. 提取 [] 内的内容，重构路径（补充.png后缀）
        String newFileName = m.group(2); // 中括号内的内容
        return directory + newFileName;
    }


    // 方法1：专门处理包含 Sex 关键词的路径截断（如 Hero/H015/H015_Sex → Hero/H015/H015_S）
    public static String truncateSexPath(String src) {
        final Pattern SEX_PATTERN = Pattern.compile("(.*?)([Ss][Ee][Xx])", Pattern.CASE_INSENSITIVE);
        // 1. 空值/空白字符串，直接返回
        if (src == null || src.trim().isEmpty()) {
            return src;
        }

        // 2. 不包含 Sex（大小写不敏感），原样返回
        if (!src.toLowerCase().contains("sex")) {
            return src;
        }

        // 3. 匹配 Sex 关键词并截断（保留到 S，移除 ex）
        Matcher matcher = SEX_PATTERN.matcher(src);
        if (matcher.find()) {
            // 分组1：Sex 前的所有字符，分组2：Sex（大小写）
            String prefix = matcher.group(1);
            // 保留 Sex 的第一个字符（S/s），拼接剩余路径（Sex 后的内容）
            String truncated = prefix + matcher.group(2).charAt(0) + src.substring(matcher.end());
            return truncated;
        }

        // 兜底：未匹配到则返回原路径（理论上不会走到这里）
        return src;
    }

    // 方法2：专门处理包含 _LoveTalk 关键词的路径截断
    public static String truncateLoveTalkPath(String src) {
        // 1. 空值/空白字符串，直接返回
        if (src == null || src.trim().isEmpty()) {
            return src;
        }

        String lowerSrc = src.toLowerCase();
        // 2. 不包含 _LoveTalk，原样返回并拼接.png
        if (!lowerSrc.contains("_lovetalk")) {
            return src + ".png";
        }

        // 3. 拆分路径为目录 + 文件名
        PathParts pathParts = splitPath(src);
        String directory = pathParts.directory;
        String fileName = pathParts.fileName;

        // 4. 匹配 _LoveTalk 关键词位置
        String lowerFileName = fileName.toLowerCase();
        int targetIndex = lowerFileName.indexOf("_lovetalk");

        // 5. 关键词在文件名开头：仅保留目录（移除末尾/）+ 拼接.png
        if (targetIndex <= 0) {
            return directory.replaceAll("/$", "") + ".png";
        }

        // 6. 截断关键词 + 移除末尾下划线
        String truncatedFileName = truncateFileName(fileName, targetIndex);

        // 7. 重构最终路径
        return directory + truncatedFileName + ".png";
    }

    // 私有工具方法：拆分路径为目录和文件名
    private static PathParts splitPath(String src) {
        String directory = "";
        String fileName = src;
        int lastSlashIndex = src.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            directory = src.substring(0, lastSlashIndex + 1); // 保留末尾的/
            fileName = src.substring(lastSlashIndex + 1);
        }
        return new PathParts(directory, fileName);
    }

    // 私有工具方法：截断文件名 + 移除末尾所有下划线
    private static String truncateFileName(String fileName, int targetIndex) {
        // 截取到关键词起始位置
        String truncated = fileName.substring(0, targetIndex);
        // 移除末尾所有下划线（如 H004___ → H004、H004_ → H004）
        return truncated.replaceAll("_+$", "");
    }

    // 内部静态类：封装路径拆分结果
    private static class PathParts {
        String directory;
        String fileName;

        PathParts(String directory, String fileName) {
            this.directory = directory;
            this.fileName = fileName;
        }
    }


}
