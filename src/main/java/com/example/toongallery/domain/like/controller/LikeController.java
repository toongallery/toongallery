package com.example.toongallery.domain.like.controller;

import com.example.toongallery.domain.like.dto.response.LikeCountResponse;
import com.example.toongallery.domain.like.dto.response.LikeResponse;
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
    public ResponseEntity<LikeResponse> toggle(@PathVariable Long commentId, @RequestParam Long userId) {
        boolean isLiked = likeService.toggle(userId,commentId);
        String message = isLiked ? "좋아요 완료" : "좋아요 취소 완료";
        return ResponseEntity.ok(new LikeResponse(isLiked,message));
    }

    @GetMapping("/{commentId}/count")
    public ResponseEntity<LikeCountResponse> getLikeCount(@PathVariable Long commentId) {
        int count = likeService.getLikeCount(commentId);
        LikeCountResponse response = new LikeCountResponse(count);
        return ResponseEntity.ok(response);
    }
}
