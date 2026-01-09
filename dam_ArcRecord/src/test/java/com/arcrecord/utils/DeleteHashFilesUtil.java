package com.arcrecord.utils;

import java.io.File;

//删除解包出来的多于文件
public class DeleteHashFilesUtil {

    /**
     * 递归删除指定目录下名称含#的文件/文件夹
     * @param file 待处理的文件/目录
     */
    public static void deleteHashFiles(File file) {
        // 如果是目录，先递归处理子文件/子目录
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteHashFiles(child); // 递归处理子项
                }
            }
        }

        // 检查名称是否含#，含则删除
        String fileName = file.getName();
        if (fileName.contains("#")) {
            deleteFileOrDir(file);
        }
    }

    /**
     * 删除单个文件/目录（目录会先清空再删除）
     * @param file 待删除的文件/目录
     */
    private static void deleteFileOrDir(File file) {
        // 如果是目录，先清空所有子项
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileOrDir(child); // 递归清空目录
                }
            }
        }

        // 删除文件/空目录
        boolean deleted = file.delete();
        if (deleted) {
            System.out.println("已删除：" + file.getAbsolutePath());
        } else {
            System.err.println("删除失败：" + file.getAbsolutePath() + "（可能是权限不足或文件被占用）");
        }
    }
}
