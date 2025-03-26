package com.example.toongallery.domain.like.service;

import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.like.entity.Like;
import com.example.toongallery.domain.like.repository.LikeRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggle(Long userId, Long commentId) {
        if (likeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            likeRepository.deleteByUserIdAndCommentId(userId, commentId);
            return false;//좋아요가 true(이미 좋아요인 상태)이면 false(좋아요 취소)로 변경
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        Like like = new Like();
        like.setComment(comment);
        like.setUser(user);

        likeRepository.save(like);
        return true;
    }

    public int getLikeCount(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }
}
