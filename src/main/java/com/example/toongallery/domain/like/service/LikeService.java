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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private static final int MAX_RETRIES = 3; // 최대 재시도 횟수
    private static final Random random = new Random(); // 랜덤 객체 생성

    private static final long RETRY_DELAY_MS = 100; // 재시도 간 대기 시간 (밀리초)

    @Transactional
    public boolean toggle(Long userId, Long commentId) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                if (likeRepository.existsByUserIdAndCommentId(userId, commentId)) {
                    likeRepository.deleteByUserIdAndCommentId(userId, commentId);
                    return false; // 좋아요가 true(이미 좋아요인 상태)이면 false(좋아요 취소)로 변경
                }

                Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_EXIST, null));
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

                Like like = Like.builder()
                        .comment(comment)
                        .user(user)
                        .build();

                likeRepository.save(like);
                return true;
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("동시 수정 충돌 발생, 최대 재시도 횟수 초과");
                }
                try {
                    long retryDelay = 100 + random.nextInt(51); // 0에서 50까지의 랜덤 값 추가
                    Thread.sleep(retryDelay); // 재시도 간 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                    throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                }
            }
        }
        return false; // 이 줄은 도달하지 않음
    }

    public int getLikeCount(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }
}
