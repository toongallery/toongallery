//package com.example.toongallery.domain.comment;
//
//import com.example.toongallery.domain.comment.dto.request.CommentSaveRequest;
//import com.example.toongallery.domain.comment.dto.request.CommentUpdateRequest;
//import com.example.toongallery.domain.comment.dto.response.CommentResponse;
//import com.example.toongallery.domain.comment.entity.Comment;
//import com.example.toongallery.domain.comment.repository.CommentRepository;
//import com.example.toongallery.domain.comment.service.CommentService;
//import com.example.toongallery.domain.common.dto.AuthUser;
//import com.example.toongallery.domain.common.exception.BaseException;
//import com.example.toongallery.domain.common.exception.ErrorCode;
//import com.example.toongallery.domain.episode.entity.Episode;
//import com.example.toongallery.domain.episode.service.EpisodeService;
//import com.example.toongallery.domain.user.entity.User;
//import com.example.toongallery.domain.user.enums.Gender;
//import com.example.toongallery.domain.user.enums.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CommentServiceTest {
//
//    @InjectMocks
//    private CommentService commentService;
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private EpisodeService episodeService;
//
//    private User user;
//    private AuthUser authUser;
//    private Episode episode;
//    private Comment parentComment;
//    private Comment comment;
//
//    @BeforeEach
//    void setUp() {
//        user = new User(
//                "test@example.com",
//                "password",
//                "Test User",
//                LocalDate.of(1990, 1, 1),
//                Gender.MALE,
//                UserRole.ROLE_USER
//        );
//        user.setId(1L);
//
//        authUser = new AuthUser(user.getId(), user.getEmail());
//
//        episode = new Episode();
//        episode.setId(1L);
//        episode.setTitle("Test Episode");
//
//        parentComment = new Comment();
//        parentComment.setId(1L);
//        parentComment.setContent("Parent Comment");
//        parentComment.setUser(user);
//        parentComment.setEpisode(episode);
//
//        comment = new Comment();
//        comment.setId(2L);
//        comment.setContent("Child Comment");
//        comment.setUser(user);
//        comment.setEpisode(episode);
//        comment.setParent(parentComment);
//    }
//
//    @Test
//    void 댓글_저장_성공() {
//        // Given
//        CommentSaveRequest request = new CommentSaveRequest("Test Comment", null);
//        given(episodeService.getEpisode(episode.getId())).willReturn(episode);
//        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
//            Comment c = invocation.getArgument(0);
//            c.setId(1L);
//            return c;
//        });
//
//        // When
//        CommentResponse result = commentService.createComment(authUser, episode.getId(), request);
//
//        // Then
//        assertThat(result.getContent()).isEqualTo("Test Comment");
//        verify(commentRepository).save(any(Comment.class));
//    }
//
//    @Test
//    void 부모_댓글이_존재하지_않으면_예외_발생() {
//        // Given
//        CommentSaveRequest request = new CommentSaveRequest("Test Comment", 999L);
//        given(episodeService.getEpisode(episode.getId())).willReturn(episode);
//        given(commentRepository.findById(999L)).willReturn(Optional.empty());
//
//        // When & Then
//        assertThatThrownBy(() -> commentService.createComment(authUser, episode.getId(), request))
//                .isInstanceOf(BaseException.class)
//                .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    void 댓글_수정_성공() {
//        // Given
//        CommentUpdateRequest request = new CommentUpdateRequest("Updated Comment");
//        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
//
//        // When
//        CommentResponse result = commentService.updateComment(authUser, comment.getId(), request);
//
//        // Then
//        assertThat(result.getContent()).isEqualTo("Updated Comment");
//        verify(commentRepository).findById(comment.getId());
//    }
//
//    @Test
//    void 댓글_수정_시_사용자_불일치_예외() {
//        // Given
//        AuthUser anotherAuthUser = new AuthUser(999L, "other@example.com");
//        CommentUpdateRequest request = new CommentUpdateRequest("Updated Comment");
//        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
//
//        // When & Then
//        assertThatThrownBy(() -> commentService.updateComment(anotherAuthUser, comment.getId(), request))
//                .isInstanceOf(BaseException.class)
//                .hasMessage(ErrorCode.COMMENT_NOT_MATCH_USER.getMessage());
//    }
//
//    @Test
//    void 댓글_삭제_성공() {
//        // Given
//        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
//        willDoNothing().given(commentRepository).deleteByParentId(comment.getId());
//        willDoNothing().given(commentRepository).delete(comment);
//
//        // When
//        commentService.deleteComment(authUser, comment.getId());
//
//        // Then
//        verify(commentRepository).deleteByParentId(comment.getId());
//        verify(commentRepository).delete(comment);
//    }
//
//    @Test
//    void 댓글_삭제_시_사용자_불일치_예외() {
//        // Given
//        AuthUser anotherAuthUser = new AuthUser(999L, "other@example.com");
//        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
//
//        // When & Then
//        assertThatThrownBy(() -> commentService.deleteComment(anotherAuthUser, comment.getId()))
//                .isInstanceOf(BaseException.class)
//                .hasMessage(ErrorCode.COMMENT_NOT_MATCH_USER.getMessage());
//    }
//}
