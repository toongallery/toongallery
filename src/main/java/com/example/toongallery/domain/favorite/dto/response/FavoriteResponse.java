package com.example.toongallery.domain.favorite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteResponse {
    private boolean isLiked;
    private String message;
}
