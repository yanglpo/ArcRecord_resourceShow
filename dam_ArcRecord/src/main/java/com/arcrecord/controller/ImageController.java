package com.arcrecord.controller;

import com.arcrecord.utils.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    // 注入配置类，获取 Path 对象（final 修饰，保证不可重新赋值）
    private String locationPath;


    // 构造器注入
    @Autowired
    public ImageController(FileStorageProperties fileStorageProperties) {
        this.locationPath = fileStorageProperties.getLocationPath();
    }

    // 图片根路径（请求路径对应的基础目录）
    private final Path rootLocation = Paths.get(locationPath);
    // 搜索根目录（文件不存在时，在此目录下递归搜索）
    private final Path searchRoot = Paths.get(locationPath);

    // 支持多级路径的请求（比如 /images/Hero/H607/Img/xxx.png）
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {

        String requestUri = request.getRequestURI();
        String filename = requestUri.replace("/images/", "");
        // 1. 获取请求路径中 /images 之后的部分（比如 Hero/H607/Img/Icon_Head_M_H607.png）
        if (filename.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            // 2. 拼接完整的本地文件路径
            Path targetFile = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(targetFile.toUri());

            // 3. 打印路径日志，方便排查（关键！）
            System.out.println("===== 图片请求 =====");
            System.out.println("请求路径：" + requestUri);
            System.out.println("目标文件路径：" + targetFile);
            System.out.println("目标文件是否存在：" + resource.exists());

            // 4. 验证文件是否存在且可读 → 不存在则搜索+复制
            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("文件不存在或不可读，开始递归搜索...");

                // 提取纯文件名（比如从 Hero/H607/Img/xxx.png 中提取 xxx.png）
                String pureFileName = new File(filename).getName();
                // 递归搜索同名文件
                Path foundFile = searchFileRecursive(searchRoot, pureFileName);

                if (foundFile != null) {
                    System.out.println("找到匹配文件：" + foundFile);
                    // 创建目标文件的父目录（避免目录不存在导致复制失败）
                    Files.createDirectories(targetFile.getParent());
                    // 复制文件到目标路径（覆盖已存在的文件）
                    Files.copy(foundFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("文件已复制到目标路径：" + targetFile);
                    // 重新加载复制后的文件资源
                    resource = new UrlResource(targetFile.toUri());
                } else {
                    System.out.println("搜索目录中未找到文件：" + pureFileName);
                    return ResponseEntity.notFound().build();
                }
            }

            // 5. 验证（复制后）文件是否存在且可读
            if (resource.exists() && resource.isReadable()) {
                // 自动识别文件类型（图片/jpeg、png等）
                String contentType = Files.probeContentType(targetFile);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                System.out.println("文件复制后仍不存在/不可读！");
                return ResponseEntity.notFound().build();
            }

        } catch (MalformedURLException e) {
            System.err.println("URL格式错误：" + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            System.err.println("文件操作失败：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 递归搜索指定目录下的同名文件（含所有子目录）
     * @param searchDir 搜索根目录
     * @param fileName  要搜索的纯文件名（如 Icon_Head_M_H607.png）
     * @return 找到的文件路径，未找到返回null
     */
    private Path searchFileRecursive(Path searchDir, String fileName) {
        File dir = searchDir.toFile();

        // 搜索目录不存在，直接返回null
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("搜索目录不存在：" + searchDir);
            return null;
        }

        // 遍历目录下的所有文件/子目录
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            // 找到同名文件，返回路径
            if (file.isFile() && file.getName().equals(fileName)) {
                return file.toPath();
            }
            // 递归搜索子目录
            Path found = searchFileRecursive(file.toPath(), fileName);
            if (found != null) {
                return found;
            }
        }

        // 未找到匹配文件
        return null;
    }
}