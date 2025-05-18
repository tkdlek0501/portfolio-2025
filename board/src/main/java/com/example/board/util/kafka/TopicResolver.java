package com.example.board.util.kafka;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TopicResolver {
    private static final Map<String, String> topicMap = Map.of(
            "POST:POST_CREATED", "post-created",
            "REPLY:REPLY_CREATED", "reply-created"
            // 필요한 조합을 계속 추가
    );

    public String resolve(String aggregateType, String eventType) {
        return topicMap.getOrDefault(aggregateType + ":" + eventType, "default-topic");
    }
}
