package com.example.toongallery.domain.comment.service;

import com.example.toongallery.domain.comment.dto.request.CommentSaveRequest;
import com.example.toongallery.domain.comment.dto.request.CommentUpdateRequest;
import com.example.toongallery.domain.comment.dto.response.CommentResponse;
import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.service.EpisodeService;
import com.example.toongallery.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final EpisodeService episodeService;

    @Transactional
    public CommentResponse createComment(AuthUser authUser, Long episodeId, CommentSaveRequest request) {
        User user = User.fromAuthUser(authUser);
        Episode episode = episodeService.getEpisode(episodeId);
        Comment parent = findParentComment(request.getParentId());
        Comment comment = commentRepository.save(new Comment(episode, user, request.getContent(), parent));
        return CommentResponse.of(comment);
    }
    private Comment findParentComment(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return commentRepository.findById(parentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND, null)
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getBestComments(Long episodeId) {
        // 좋아요 수에 따른 상위 10개의 댓글만 출력
        return null;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long episodeId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByEpisodeIdAndParentIsNullOrderByCreatedAt(episodeId, pageable);
        return comments.stream()
                .map(CommentResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long parentId) {
        List<Comment> comments = commentRepository.findByParentIdOrderByCreatedAt(parentId);
        return comments.stream()
                .map(CommentResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(AuthUser authUser, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND, null));

        User user = comment.getUser();
        if (!user.getId().equals(authUser.getUserId())) {
            throw new BaseException(ErrorCode.COMMENT_NOT_MATCH_USER, null);
        }
        comment.updateContent(request.getContent());

        return CommentResponse.of(comment);
    }

    @Transactional
    public void deleteComment(AuthUser authUser, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new BaseException(ErrorCode.COMMENT_NOT_FOUND, null));

        User user = comment.getUser();
        if (!user.getId().equals(authUser.getUserId())) {
            throw new BaseException(ErrorCode.COMMENT_NOT_MATCH_USER, null);
        }
        commentRepository.deleteByParentId(comment.getId());
        commentRepository.delete(comment);
    }
}
