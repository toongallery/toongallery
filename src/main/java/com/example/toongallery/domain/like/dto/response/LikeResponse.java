package com.example.toongallery.domain.like.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponse {
    private boolean isLiked;
    private String message;
}
