// src/main/java/com/example/snsapp/WebConfig.java

package com.example.snsapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // <-- この行をインポートする
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) { // <-- ここに @NonNull を追加
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}