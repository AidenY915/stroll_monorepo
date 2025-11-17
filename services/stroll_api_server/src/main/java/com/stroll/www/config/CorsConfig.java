package com.stroll.www.config;

import com.stroll.www.property.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Autowired
    Urls urls;
    @Override
    public void addCorsMappings(CorsRegistry reg) {
        System.out.println("frontendUrl = " + urls.getFrontendUrl());
        reg.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost", "http://localhost:*", "http://127.0.0.1:*", urls.getFrontendUrl())
                .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location","X-RateLimit-Remaining")
                .allowCredentials(true);
    }
}

