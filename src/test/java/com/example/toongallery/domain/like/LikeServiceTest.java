package com.example.toongallery.domain.like;

import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.like.entity.Like;
import com.example.toongallery.domain.like.repository.LikeRepository;
import com.example.toongallery.domain.like.service.LikeService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Comment comment;

    @BeforeEach
    public void setUp() {
        user1 = new User(
                "test@example.com",
                "password",
                "Test User",
                LocalDate.of(1990, 1, 1),
                Gender.MALE,
                UserRole.ROLE_USER
        );
        user1.setId(1L);
        user2 = new User(
                "user2@example.com",
                "password",
                "User Two",
                LocalDate.of(1991, 2, 2),
                Gender.FEMALE,
                UserRole.ROLE_USER
        );
        user2.setId(2L);

        comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test Comment");
    }

    @Test
    void 좋아요가_있으면_좋아요_취소() {
        // Given
        given(likeRepository.existsByUserIdAndCommentId(user1.getId(), comment.getId()))
                .willReturn(true);
        willDoNothing().given(likeRepository).deleteByUserIdAndCommentId(user1.getId(), comment.getId());

        // When
        boolean result = likeService.toggle(user1.getId(), comment.getId());

        // Then
        assertThat(result).isFalse(); // 좋아요가 취소되어야 함
        verify(likeRepository).deleteByUserIdAndCommentId(user1.getId(), comment.getId());
    }

    @Test
    void 좋아요가_없다면_좋아요추가() {
        // Given
        given(likeRepository.existsByUserIdAndCommentId(user1.getId(), comment.getId()))
                .willReturn(false);
        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(comment));
        given(userRepository.findById(user1.getId()))
                .willReturn(Optional.of(user1));

        // 수정된 부분: Like 객체를 builder로 생성하여 반환
        given(likeRepository.save(any(Like.class)))
                .willAnswer(invocation -> {
                    // Like 객체를 생성
                    Like like = Like.builder()
                            .user(user1)
                            .comment(comment)
                            .build();
                    return like; // like 객체 반환
                });

        // When
        boolean result = likeService.toggle(user1.getId(), comment.getId());

        // Then
        assertThat(result).isTrue(); // 좋아요가 추가되어야 함
        verify(likeRepository).save(any(Like.class)); // save가 호출되었는지 확인
    }

    @Test
    void 관심등록_중_댓글이_존재하지_않음() {
        // Given
        given(likeRepository.existsByUserIdAndCommentId(user1.getId(), comment.getId()))
                .willReturn(false);
        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> likeService.toggle(user1.getId(), comment.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_EXIST.getMessage());
    }

    @Test
    void 관심등록_중_유저가_존재하지_않음() {
        // Given
        given(likeRepository.existsByUserIdAndCommentId(user1.getId(), comment.getId()))
                .willReturn(false);
        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(comment));
        given(userRepository.findById(user1.getId()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> likeService.toggle(user1.getId(), comment.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_EXIST.getMessage());
    }

    @Test
    void 동시성_제어_테스트() throws InterruptedException {
        // Given
        given(likeRepository.existsByUserIdAndCommentId(user1.getId(), comment.getId()))
                .willReturn(false);
        given(likeRepository.existsByUserIdAndCommentId(user2.getId(), comment.getId()))
                .willReturn(false);
        given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(comment));
        given(userRepository.findById(user1.getId()))
                .willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId()))
                .willReturn(Optional.of(user2));

        // 버전 충돌을 유도
        given(likeRepository.save(any(Like.class)))
                .willAnswer(invocation -> {
                    throw new OptimisticLockingFailureException("동시 수정 충돌 발생");
                });

        // 두 스레드가 동시에 실행
        Thread thread1 = new Thread(() -> {
            assertThatThrownBy(() -> likeService.toggle(user1.getId(), comment.getId()))
                    .isInstanceOf(OptimisticLockingFailureException.class);
        });

        Thread thread2 = new Thread(() -> {
            assertThatThrownBy(() -> likeService.toggle(user2.getId(), comment.getId()))
                    .isInstanceOf(OptimisticLockingFailureException.class);
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

    @Test
    void 좋아요_개수_반환() {
        // Given
        long commentId = 1L;
        int expectedLikeCount = 5;
        given(likeRepository.countByCommentId(commentId)).willReturn(expectedLikeCount);

        // When
        int result = likeService.getLikeCount(commentId);

        // Then
        assertThat(result).isEqualTo(expectedLikeCount); // 예상한 좋아요 개수와 동일한지 확인
    }
}
