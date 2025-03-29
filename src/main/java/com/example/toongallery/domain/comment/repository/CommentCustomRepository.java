package com.example.toongallery.domain.comment.repository;

import com.example.toongallery.domain.comment.dto.response.CommentResponse;

import java.util.List;

public interface CommentCustomRepository {
        List<CommentResponse> findTop10CommentById(Long episodeId);
}
