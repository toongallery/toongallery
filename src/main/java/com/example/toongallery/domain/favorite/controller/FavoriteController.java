package com.example.toongallery.domain.favorite.controller;

import com.example.toongallery.domain.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    @PostMapping("/{webtoonId}")
    public ResponseEntity<String> toggle(@PathVariable Long webtoonId, @RequestParam Long userId) {
        boolean isfavorited = favoriteService.toggle(userId,webtoonId);
        if (isfavorited) {
            return ResponseEntity.ok("관심 등록");
        } else {
            return ResponseEntity.ok("관심 등록 해제");
        }
    }
}
