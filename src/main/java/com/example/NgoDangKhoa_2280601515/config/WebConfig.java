package com.example.NgoDangKhoa_2280601515.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình để serve static files từ thư mục uploads
        registry.addResourceHandler("/" + uploadDir + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cho phép load resources từ external domains (CDN, APIs, etc.)
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
