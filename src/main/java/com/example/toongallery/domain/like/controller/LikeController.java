package com.example.toongallery.domain.like.controller;

import com.example.toongallery.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{commentId}")
    public ResponseEntity<String> toggle(@PathVariable Long commentId, @RequestParam Long userId) {
        boolean isLiked = likeService.toggle(userId,commentId);
        if (isLiked) {
            return ResponseEntity.ok("좋아요");
        } else {
            return ResponseEntity.ok("좋아요 취소");
        }
    }

    @GetMapping("/{commentId}/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable Long commentId) {
        int count = likeService.getLikeCount(commentId);
        return ResponseEntity.ok(count);
    }
}
