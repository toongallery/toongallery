package com.example.toongallery.domain.comment;

import com.example.toongallery.domain.comment.dto.request.CommentSaveRequest;
import com.example.toongallery.domain.comment.dto.request.CommentUpdateRequest;
import com.example.toongallery.domain.comment.dto.response.CommentResponse;
import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.comment.service.CommentService;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.service.EpisodeService;
import com.example.toongallery.domain.like.service.LikeService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EpisodeService episodeService;

    @Mock
    private LikeService likeService;

    private User user;
    private AuthUser authUser;
    private Episode episode;
    private Comment parentComment;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "password", "Test User",
                LocalDate.of(1990, 1, 1), Gender.MALE, UserRole.ROLE_USER);
        user.setId(1L);

        // AuthUser 생성 시 UserRole 전달
        authUser = new AuthUser(user.getId(), user.getEmail(), UserRole.ROLE_USER);

        episode = Episode.of("Test Episode", 1, "test_thumbnail.jpg", null);
        ReflectionTestUtils.setField(episode, "id", 1L);

        parentComment = Comment.of(episode, user, "Parent Comment", null);
        ReflectionTestUtils.setField(parentComment, "Id", 1L); // ID 강제 설정

        comment = Comment.of(episode, user, "Child Comment", parentComment);
        ReflectionTestUtils.setField(comment, "Id", 2L); // ID 강제 설정
    }

    @Test
    void 댓글_저장_성공() {
        // given
        CommentSaveRequest request = new CommentSaveRequest("Test Comment", null);
        given(episodeService.getEpisode(episode.getId())).willReturn(episode);

        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            return savedComment;
        });

        // when
        CommentResponse result = commentService.createComment(authUser, episode.getId(), request);

        // then
        assertThat(result.getContent()).isEqualTo("Test Comment");
        verify(commentRepository).save(any(Comment.class));
    }


    @Test
    void 부모_댓글이_존재하지_않으면_예외_발생() {
        CommentSaveRequest request = new CommentSaveRequest("Test Comment", 999L);
        given(episodeService.getEpisode(episode.getId())).willReturn(episode);
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(authUser, episode.getId(), request))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    void 댓글_수정_성공() {
        CommentUpdateRequest request = new CommentUpdateRequest("Updated Comment");
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

        CommentResponse result = commentService.updateComment(authUser, comment.getId(), request);

        assertThat(result.getContent()).isEqualTo("Updated Comment");
        verify(commentRepository).findById(comment.getId());
    }

    @Test
    void 댓글_수정_시_사용자_불일치_예외() {
        AuthUser anotherAuthUser = new AuthUser(999L, "other@example.com", UserRole.ROLE_USER);
        CommentUpdateRequest request = new CommentUpdateRequest("Updated Comment");
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(anotherAuthUser, comment.getId(), request))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_MATCH_USER.getMessage());
    }

    @Test
    void 댓글_삭제_성공() {
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        willDoNothing().given(commentRepository).deleteByParentId(comment.getId());
        willDoNothing().given(commentRepository).delete(comment);

        commentService.deleteComment(authUser, comment.getId());

        verify(commentRepository).deleteByParentId(comment.getId());
        verify(commentRepository).delete(comment);
    }

    @Test
    void 댓글_삭제_시_사용자_불일치_예외() {
        AuthUser anotherAuthUser = new AuthUser(999L, "other@example.com", UserRole.ROLE_USER);
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(anotherAuthUser, comment.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_MATCH_USER.getMessage());
    }
    @Test
    void 댓글_목록_조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Comment> comments = List.of(parentComment, comment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        given(commentRepository.findByEpisodeIdAndParentIsNullOrderByCreatedAt(episode.getId(), pageable))
                .willReturn(commentPage);

        // when
        List<CommentResponse> responses = commentService.getComments(episode.getId(), pageable);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getContent()).isEqualTo(parentComment.getContent());
        assertThat(responses.get(1).getContent()).isEqualTo(comment.getContent());
    }

    @Test
    void 대댓글_조회_성공() {
        // given
        List<Comment> replies = List.of(comment);
        given(commentRepository.findByParentIdOrderByCreatedAt(parentComment.getId()))
                .willReturn(replies);

        // when
        List<CommentResponse> responses = commentService.getReplies(parentComment.getId());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContent()).isEqualTo(comment.getContent());
    }

}
