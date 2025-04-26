package com.example.board.repository.query;

import com.example.board.domain.entity.QReply;
import com.example.board.domain.entity.Reply;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QReply reply = QReply.reply;

    public ReplyQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 첫 페이지 조회 (오프셋 기반)
    public List<Reply> findFirstPage(Long postId, int size) {
        return queryFactory
                .selectFrom(reply)
                .where(reply.postId.eq(postId)) // 해당 게시글의 댓글만 조회
//                .orderBy(reply.id.asc())
                .limit(size)
                .fetch();
    }

    // 다음 페이지 조회 (커서 기반)
    public List<Reply> findNextPage(Long postId, Long cursor, int size) {
        return queryFactory
                .selectFrom(reply)
                .where(
                        reply.postId.eq(postId), // 해당 게시글의 댓글만 조회
                        reply.id.gt(cursor) // 커서 이후 데이터 조회
                )
//                .orderBy(reply.id.asc())
                .limit(size)
                .fetch();
    }
}
