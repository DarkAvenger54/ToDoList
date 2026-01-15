package pl.edu.wit.todolist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String signKey, Integer expirationMs, String issuer){ }