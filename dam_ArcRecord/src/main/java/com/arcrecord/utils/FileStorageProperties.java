package com.arcrecord.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储路径配置类
 * 前缀 "file.storage" 对应配置文件中的 file.storage 节点
 */
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    // 对应配置文件中的 file.storage.root-location
    private String locationPath;

    public String getLocationPath() {
        return locationPath;
    }

    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }
}
