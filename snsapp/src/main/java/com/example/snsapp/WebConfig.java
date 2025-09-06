package com.example.snsapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/で始まるすべてのパスを対象にする
            .allowedOrigins("http://localhost:3000") // http://localhost:3000からのリクエストを許可
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 許可するHTTPメソッド
            .allowedHeaders("*") // すべてのヘッダーを許可
            .allowCredentials(true);
    }
}