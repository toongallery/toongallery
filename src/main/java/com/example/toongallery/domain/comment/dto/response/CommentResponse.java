package com.example.toongallery.domain.comment.dto.response;

import com.example.toongallery.domain.comment.entity.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class CommentResponse {
    private final Long id;
    private final String content;
    private final Long episodeId;
    private final Long userId;
    private final Long parentId;

    private CommentResponse(Long id, String content, Long episodeId, Long userId, Long parentId) {
        this.id = id;
        this.content = content;
        this.episodeId = episodeId;
        this.userId = userId;
        this.parentId = parentId;
    }
    public static CommentResponse of(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getEpisode().getId(),
                comment.getUser().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null
        );
    }
}
