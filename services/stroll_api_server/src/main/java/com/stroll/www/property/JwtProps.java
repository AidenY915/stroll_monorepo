package com.stroll.www.property;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProps {
    private String secret;
    private Long expiration;

    @PostConstruct
    void afterBind()
    {
        System.out.println("scret: "+ secret);
    }
}
