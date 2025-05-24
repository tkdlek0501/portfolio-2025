package com.example.board.event.listener;

import com.example.board.domain.entity.Post;
import com.example.board.domain.entity.View;
import com.example.board.dto.event.PostViewIncreasedEvent;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ViewRepository viewRepository;
    private final PostRepository postRepository;

    // Redis + Rua Script 를 사용하여 중복 조회수 증가 방지 - 1일 TTL
    private static final String VIEW_CACHE_KEY_PREFIX = "board:viewed:post:";
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local exists = redis.call("EXISTS", key)
        if exists == 0 then
            redis.call("SET", key, 1, "EX", 86400)  -- TTL 1일
            return 1
        else
            return 0
        end
        """;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleIncreasedPostViewEvent(PostViewIncreasedEvent event) {
        String cacheKey = VIEW_CACHE_KEY_PREFIX + event.getPostId() + ":" + event.getUserId();

        try {
            // Lua 스크립트를 실행하여 중복 조회 여부 확인
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(cacheKey)
            );

            if (result != null && result == 1L) {
                // Redis에 처음 저장된 경우만 실제 DB 처리
                viewRepository.save(View.create(event.getUserId(), event.getPostId()));
                postRepository.findById(event.getPostId())
                        .ifPresent(Post::increaseViewCount);
                log.info("[board-service] 게시글 조회수 증가 처리 완료 :: postId: {}, userId: {}", event.getPostId(), event.getUserId());
            } else {
                // 이미 조회 처리된 경우
                log.info("[board-service] 중복 조회 무시 :: postId: {}, userId: {}", event.getPostId(), event.getUserId());
            }
        } catch (Exception e) {
            log.error("[board-service] 게시글 조회수 증가 처리 실패 :: postId: {}, userId: {}, {}", event.getPostId(), event.getUserId(), e.getMessage(), e);
        }
    }
}
