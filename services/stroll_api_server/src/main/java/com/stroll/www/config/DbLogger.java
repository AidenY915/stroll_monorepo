package com.stroll.www.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DbLogger {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void logDbUrl() {
        System.out.println("✅ Configured DB URL: " + dbUrl);
    }
}
