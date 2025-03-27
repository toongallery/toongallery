package com.example.toongallery.domain.comment.repository;

import com.example.toongallery.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    List<Comment> findByParentIdOrderByCreatedAt(Long parentId);

    Page<Comment> findByEpisodeIdAndParentIsNullOrderByCreatedAt(Long episodeId, Pageable pageable);

    void deleteByParentId(Long parentId);
}
