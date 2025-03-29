package com.example.toongallery.domain.comment.repository;

import com.example.toongallery.domain.comment.dto.response.CommentResponse;
import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.entity.QComment;
import com.example.toongallery.domain.like.entity.QLike;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;
    @Override
    public List<CommentResponse> findTop10CommentById(Long episodeId) {
        QComment comment = QComment.comment;
        QLike like = QLike.like;

        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .leftJoin(like).on(like.comment.Id.eq(comment.Id))
                .where(
                        comment.episode.id.eq(episodeId),
                        comment.parent.isNull()
                )
                .groupBy(comment.Id)
                .orderBy(like.id.count().desc())
                .limit(10)
                .fetch();

        return comments.stream()
                .map(CommentResponse::of)
                .collect(Collectors.toList());
    }
}
