package com.example.board.event.listener;

import com.example.board.domain.entity.Post;
import com.example.board.domain.entity.View;
import com.example.board.dto.event.PostViewIncreasedEvent;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventListener {

    private final ViewRepository viewRepository;
    private final PostRepository postRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleIncreasedPostViewEvent(PostViewIncreasedEvent event) {
        try {
            // 이미 조회를 했는지 확인
            viewRepository.findByPostIdAndUserId(event.getPostId(), event.getUserId())
                    .ifPresentOrElse(
                            view -> {
                                // 이미 조회를 한 경우
                                log.info("[board-service] 게시글 조회수 증가 이벤트 처리 :: 이미 조회수 증가 처리됨 :: postId: {}, userId: {}", event.getPostId(), event.getUserId());
                            },
                            () -> {
                                // 아직 조회를 하지 않은 경우
                                viewRepository.save(View.create(event.getUserId(), event.getPostId()));
                                postRepository.findById(event.getPostId())
                                        .ifPresent(Post::increaseViewCount);
                                log.info("[board-service] 게시글 조회수 증가 이벤트 처리 완료 :: postId: {}, userId: {}", event.getPostId(), event.getUserId());
                            }
                    );
        } catch (Exception e) {
            log.error("[board-service] 게시글 조회수 증가 처리 실패 :: postId: {}, userId: {}, {}", event.getPostId(), event.getUserId(), e.getMessage(), e);
        }
    }
}
