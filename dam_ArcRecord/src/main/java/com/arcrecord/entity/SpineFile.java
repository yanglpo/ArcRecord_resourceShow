package com.arcrecord.entity;

import java.util.List;

/**
 * Spine资源返回对象（支持多PNG纹理）
 * 兼容Java 1.8
 */

public class SpineFile {
    private String atlasFile;      // .atlas文件名称
    private String atlasFileUrl;   // .atlas文件访问路径
    private String skelFile;       // .skel文件名称
    private String skelFileUrl;    // .skel文件访问路径
    private List<SpineImageVO> pngFiles; // 多个.png文件列表

    // Getter & Setter
    public static class SpineImageVO {
        private String fileName;
        private String fileUrl;
        private long fileSize;

        // Getter & Setter

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
    }

    public String getAtlasFile() {
        return atlasFile;
    }

    public void setAtlasFile(String atlasFile) {
        this.atlasFile = atlasFile;
    }

    public String getAtlasFileUrl() {
        return atlasFileUrl;
    }

    public void setAtlasFileUrl(String atlasFileUrl) {
        this.atlasFileUrl = atlasFileUrl;
    }

    public String getSkelFile() {
        return skelFile;
    }

    public void setSkelFile(String skelFile) {
        this.skelFile = skelFile;
    }

    public String getSkelFileUrl() {
        return skelFileUrl;
    }

    public void setSkelFileUrl(String skelFileUrl) {
        this.skelFileUrl = skelFileUrl;
    }

    public List<SpineImageVO> getPngFiles() {
        return pngFiles;
    }

    public void setPngFiles(List<SpineImageVO> pngFiles) {
        this.pngFiles = pngFiles;
    }
}
