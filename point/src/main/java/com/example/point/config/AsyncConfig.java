package com.example.point.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int core = Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(core);             // 최소 스레드 수
        executor.setMaxPoolSize(core * 2);          // 최대 스레드 수 (늘어날 수 있는 스레드 수)
        executor.setQueueCapacity(500);             // 큐에 대기할 작업 개수
        executor.setKeepAliveSeconds(30);           // 최대 스레드 idle 유지 시간
        executor.setThreadNamePrefix("MyAsync-");   // 스레드 이름 prefix
        executor.initialize();
        return executor;
    }

    // 비동기 내부에서 exception 발생시 예외 처리
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            System.err.println("Uncaught async error in method: " + method.getName());
            ex.printStackTrace();
        };
    }
}
