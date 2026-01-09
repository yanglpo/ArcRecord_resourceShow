package com.arcrecord.utils;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    // ========== 保留跨域+特殊字符（必须） ==========
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 临时放开所有跨域（测试用）
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "[]{}#@"); // 增加更多特殊字符兼容
            connector.setProperty("relaxedQueryChars", "[]{}#@");
            connector.setProperty("allowEncodedSlash", "true");
        });
    }

    // ========== 3. 核心：本地资源映射配置（新增） ==========

    // 静态资源映射（仅兜底，动态路径走接口）
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 仅保留基础兜底映射（非必须，可根据需求删除）
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("file:C:/Users/tansun/Desktop/123/Assets/Game/")
                .setCachePeriod(0);
    }
}