package com.example.toongallery.domain.like.service;

import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.like.entity.Like;
import com.example.toongallery.domain.like.repository.LikeRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_EXIST, null));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new BaseException(ErrorCode.USER_NOT_EXIST, null));

        Like like = Like.builder()
                .comment(comment)
                .user(user)
                .build();

        likeRepository.save(like);
        return true;
    }

    public int getLikeCount(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }
}
