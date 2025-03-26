package com.example.toongallery.domain.comment.controller;

import com.example.toongallery.domain.comment.dto.request.CommentSaveRequest;
import com.example.toongallery.domain.comment.dto.request.CommentUpdateRequest;
import com.example.toongallery.domain.comment.dto.response.CommentResponse;
import com.example.toongallery.domain.comment.service.CommentService;
import com.example.toongallery.domain.common.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons/{episodeId}/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long episodeId,
            @RequestBody CommentSaveRequest request
    ) {
        return ResponseEntity.ok(commentService.createComment(authUser, episodeId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long episodeId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        Pageable pageable = PageRequest.of(page-1, size);
        return ResponseEntity.ok(commentService.getComments(episodeId, pageable));
    }

    @GetMapping("/{parentId}")
    public ResponseEntity<List<CommentResponse>> getReplies(
            @PathVariable Long parentId
    ) {
        return ResponseEntity.ok(commentService.getReplies(parentId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(authUser, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(authUser, commentId);
        return ResponseEntity.ok().build();
    }
}
