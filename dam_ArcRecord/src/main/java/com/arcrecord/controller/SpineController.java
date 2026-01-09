package com.arcrecord.controller;

import com.arcrecord.utils.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@RestController
@RequestMapping("/spine")
public class SpineController {

    // 注入配置类，获取 Path 对象（final 修饰，保证不可重新赋值）
    private String locationPath;


    // 构造器注入
    @Autowired
    public SpineController(FileStorageProperties fileStorageProperties) {
        this.locationPath = fileStorageProperties.getLocationPath();
    }

    // 基础路径（你的Spine文件根目录）
    private String BASE_PATH = locationPath;

    /**
     * 1. 获取指定角色的Spine文件路径（atlas/skel/png）
     */
    @PostMapping("/getRoleSpine")
    public ResponseEntity<Map<String, Object>> getRoleSpine(@RequestBody Map<String, String> spineObjectMap) {
        try {
            String spineObject = spineObjectMap.get("spineObject");
            Path targetDir = Paths.get(BASE_PATH + spineObject);
            File dir = targetDir.toFile();

            if (!dir.exists() || !dir.isDirectory()) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("atlas", "");
                emptyResult.put("png", new ArrayList<>());
                emptyResult.put("skel", "");
                return ResponseEntity.ok(emptyResult);
            }

            Map<String, Object> result = new HashMap<>();
            String atlas = "";
            String skel = "";
            List<String> pngList = new ArrayList<>();

            File[] files = dir.listFiles((dir1, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".atlas") || lowerName.endsWith(".skel") || lowerName.endsWith(".png");
            });

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    // 拼接相对路径（前端通过/getSingleSpineFile接口访问）
                    String relativePath = spineObject + "/" + fileName;

                    if (fileName.endsWith(".atlas")) {
                        atlas = relativePath;
                    } else if (fileName.endsWith(".skel")) {
                        skel = relativePath;
                    } else if (fileName.endsWith(".png")) {
                        pngList.add(relativePath);
                    }
                }
            }

            result.put("atlas", atlas);
            result.put("png", pngList);
            result.put("skel", skel);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("atlas", "");
            errorResult.put("png", new ArrayList<>());
            errorResult.put("skel", "");
            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 2. 提供单个Spine文件的HTTP访问（核心：前端通过此接口加载文件）
     */
    /**
     * 获取单个Spine文件（接收完整路径参数）
     * @param spinePath 完整的文件相对路径（如：Hero/H001/H001/CG_H001_a.atlas）
     * @return 文件资源响应
     */
    @GetMapping("/getSingleSpineFile")
    public ResponseEntity<byte[]> getSingleSpineFile(
            @RequestParam("spinePath") String spinePath,
            HttpServletRequest request
    ) {
        System.out.println("【Spine接口】接收到的spinePath：" + spinePath);
        try {
            // 1. 路径拼接（你的现有逻辑，已验证正确）
            String normalizedSpinePath = spinePath.replaceAll("/", Matcher.quoteReplacement(File.separator));
            Path fullPath = Paths.get(BASE_PATH, normalizedSpinePath);
            File targetFile = fullPath.toFile();
            System.out.println("【Spine接口】拼接后的完整路径：" + fullPath);

            // 2. 读取文件（你的现有逻辑，已验证成功）
            byte[] fileBytes = Files.readAllBytes(fullPath);
            System.out.println("【Spine接口】文件读取成功，大小：" + fileBytes.length + "字节");

            // 3. 构建响应头（核心修复：适配Spine Player的二进制解析）
            HttpHeaders headers = new HttpHeaders();
            String fileName = targetFile.getName();

            // ===== 关键修复：不同文件类型的响应头精准配置 =====
            if (fileName.endsWith(".atlas")) {
                // atlas是文本文件，需指定UTF-8编码
                headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
                headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());
            } else if (fileName.endsWith(".png")) {
                // PNG图片，内联加载
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());
            } else if (fileName.endsWith(".skel")) {
                // ===== .skel文件核心修复 =====
                // 1. 二进制流类型，禁用任何编码
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                // 2. 关键：移除Content-Encoding，避免前端解析二进制时乱码
                headers.remove("Content-Encoding");
                // 3. 禁用gzip压缩（很多框架会自动压缩，导致二进制损坏）
                headers.set("Content-Encoding", "identity");
                // 4. 明确是二进制流，内联加载（不触发下载）
                headers.setContentDisposition(ContentDisposition.inline()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build());
                // 5. 固定Content-Length，确保前端能完整接收
                headers.setContentLength(fileBytes.length);
                // 6. 禁用缓存，避免前端读取旧文件
                headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            }

            // 4. 跨域配置（确保前端5173端口能正常接收）
            String origin = request.getHeader("Origin");
            if (origin != null) {
                headers.add("Access-Control-Allow-Origin", origin);
                headers.add("Access-Control-Allow-Methods", "GET, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type, Accept");
                headers.add("Access-Control-Allow-Credentials", "true");
            }

            // 5. 返回响应：直接返回byte[]（而非ByteArrayResource，避免包装导致的格式问题）
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("【Spine接口】处理异常：");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * 动态判断文件的MediaType
     * @param fileName 文件名（含后缀）
     * @return 对应的MediaType
     */
    private MediaType getMediaType(String fileName) {
        if (fileName.endsWith(".atlas")) {
            // atlas文件：文本类型
            return MediaType.TEXT_PLAIN;
        } else if (fileName.endsWith(".png")) {
            // png图片：图片类型
            return MediaType.IMAGE_PNG;
        } else if (fileName.endsWith(".skel")) {
            // skel文件：二进制流
            return MediaType.APPLICATION_OCTET_STREAM;
        } else {
            // 默认二进制流
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}