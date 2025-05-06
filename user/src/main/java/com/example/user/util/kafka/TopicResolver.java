package com.example.user.util.kafka;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TopicResolver {
    private static final Map<String, String> topicMap = Map.of(
            "USER:USER_UPDATED", "user-updated"
            // 필요한 조합을 계속 추가
    );

    public String resolve(String aggregateType, String eventType) {
        return topicMap.getOrDefault(aggregateType + ":" + eventType, "default-topic");
    }
}
