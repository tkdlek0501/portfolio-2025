package com.example.board.repository.query;

import com.example.board.domain.entity.ChildReply;
import com.example.board.domain.entity.QChildReply;
import com.example.board.domain.entity.Reply;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChildReplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QChildReply childReply = QChildReply.childReply;

    public ChildReplyQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 첫 페이지 조회 (오프셋 기반)
    public List<ChildReply> findFirstPage(Long replyId, int size) {
        return queryFactory
                .selectFrom(childReply)
                .where(childReply.replyId.eq(replyId)) // 해당 게시글의 댓글만 조회
                .limit(size)
                .fetch();
    }

    // 다음 페이지 조회 (커서 기반)
    public List<ChildReply> findNextPage(Long replyId, Long cursor, int size) {
        return queryFactory
                .selectFrom(childReply)
                .where(
                        childReply.replyId.eq(replyId), // 해당 게시글의 댓글만 조회
                        childReply.id.gt(cursor) // 커서 이후 데이터 조회
                )
                .limit(size)
                .fetch();
    }
}
