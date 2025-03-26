package com.example.toongallery.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter

public class CommentSaveRequest {
    @NotBlank
    @Size(max = 30)
    private String content;

    private Long parentId;

    public CommentSaveRequest(String content, Long parentId) {
        this.content = content;
        this.parentId = parentId;
    }
}
