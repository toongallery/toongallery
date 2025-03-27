package com.example.toongallery.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentUpdateRequest {

    @NotBlank
    private String content;

    public CommentUpdateRequest(String content) {
        this.content = content;
    }
}
