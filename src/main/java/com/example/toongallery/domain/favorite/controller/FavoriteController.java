package com.example.toongallery.domain.favorite.controller;

import com.example.toongallery.domain.favorite.dto.response.FavoriteResponse;
import com.example.toongallery.domain.favorite.service.FavoriteService;
import com.example.toongallery.domain.like.dto.response.LikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{webtoonId}")
    public ResponseEntity<FavoriteResponse> toggle(@PathVariable Long webtoonId, @RequestParam Long userId) {
        boolean isFavorited = favoriteService.toggle(userId, webtoonId);
        String message = isFavorited ? "좋아요 완료" : "좋아요 취소 완료";
        return ResponseEntity.ok(new FavoriteResponse(isFavorited, message));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
}
