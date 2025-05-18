package com.example.point.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // TODO: 추후 비동기 스레드풀 설정 필요하면 AsyncConfigurer 구현
}
