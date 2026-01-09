package com.arcrecord.controller;

import com.arcrecord.entity.Role;
import com.arcrecord.entity.RoleCollection;
import com.arcrecord.service.HeroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private HeroService heroService;


    @GetMapping("/getRole")
    public List<Role> getRole() {
        List<Role> list = heroService.getHeroList();
        return list;
    }


    @GetMapping("/getRoleCollectionByHeroId")
    public List<RoleCollection> getRoleCollectionByHeroId(String heroId) {
        List<RoleCollection> roleCollectionList = heroService.getRoleCollectionByHeroId(heroId);
        RoleCollection roleCollection = new RoleCollection();
        return roleCollectionList;
    }

    @GetMapping("/getCGImages")
    public List<Map<String, String>> getCGImages(@RequestParam String roleMotions,@RequestParam String prefab, HttpServletRequest request) { // 注入HttpServletRequest获取请求URI

        List<Map<String, String>> result = new ArrayList<>();

        // 步骤1：解析roleMotions，分割出CG_1、CG_2等（注意分隔符是~不是,）
        String[] keyArray = roleMotions.split("~");

        // 步骤2：从请求URI中提取前缀（如CG_P0005）
        Map<String,String> pathMap  = extractPrefixAndBasePath(prefab);
        String prefix = pathMap.get("prefix");       // 提取前缀（如CG_P0005）
        String basePath = pathMap.get("basePath");   // 提取基础路径（如/CG/P0005/）
        // 步骤3：遍历生成完整的图片路径
        for (String key : keyArray) {
            // 过滤空值，避免异常
            if (key == null || key.trim().isEmpty()) {
                continue;
            }

            // 提取数字部分（如从CG_1中提取1）
            String num = key.replace("CG_", "").trim();

            // 合成最终图片名称和路径
            String imageName = prefix + "_" + num + ".png"; // CG_P0005_1.png
            String imagePath = basePath+"Image/" + imageName;

            Map<String, String> imageInfo = new HashMap<>();
            imageInfo.put("key", key); // 保留原key（如CG_1）
            imageInfo.put("url", imagePath); // 返回拼接后的完整相对路径
            result.add(imageInfo);
        }

        return result;
    }

    /**
     * 从请求URI中提取前缀（如从/CG/P0005/CG_P0005提取CG_P0005）
     */
    private String extractPrefixFromUri(String requestUri) {
        if (requestUri == null || requestUri.isEmpty()) {
            return "CG_DEFAULT"; // 默认前缀，避免空指针
        }

        // 按/分割URI，取最后一个非空部分作为前缀
        String[] uriParts = requestUri.split("/");
        String prefix = "CG_DEFAULT";
        for (int i = uriParts.length - 1; i >= 0; i--) {
            if (!uriParts[i].trim().isEmpty()) {
                prefix = uriParts[i].trim();
                break;
            }
        }

        return prefix;
    }

    /**
     * 从请求URI中同时提取前缀和不含前缀的基础路径（返回Map）
     * Map中键说明：
     *   - prefix: 提取的前缀（如CG_P0005）
     *   - basePath: 不含前缀的基础路径（如/CG/P0005/）
     * @param requestUri 原始请求路径（如/CG/P0005/CG_P0005）
     * @return 包含前缀和基础路径的Map
     */
    private Map<String, String> extractPrefixAndBasePath(String requestUri) {
        Map<String, String> resultMap = new HashMap<>();
        // 默认值，避免空指针
        String defaultPrefix = "CG_DEFAULT";
        String defaultBasePath = "";

        if (requestUri == null || requestUri.isEmpty()) {
            resultMap.put("prefix", defaultPrefix);
            resultMap.put("basePath", defaultBasePath);
            return resultMap;
        }

        // 按/分割URI，过滤空字符串，提取最后一个非空部分作为前缀
        String[] uriParts = requestUri.split("/");
        String prefix = defaultPrefix;
        for (int i = uriParts.length - 1; i >= 0; i--) {
            if (!uriParts[i].trim().isEmpty()) {
                prefix = uriParts[i].trim();
                break;
            }
        }

        // 提取不含前缀的基础路径（前缀之前的部分）
        String basePath = defaultBasePath;
        int prefixIndex = requestUri.lastIndexOf(prefix);
        if (prefixIndex > 0) {
            // 截取前缀之前的部分
            basePath = requestUri.substring(0, prefixIndex);
            // 确保基础路径以/结尾（避免拼接时路径错误）
            if (!basePath.endsWith("/") && !basePath.isEmpty()) {
                basePath += "/";
            }
        }

        // 存入Map
        resultMap.put("prefix", prefix);
        resultMap.put("basePath", basePath);
        return resultMap;
    }

}
