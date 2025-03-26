package com.example.toongallery.domain.like.entity;

import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Setter
@NoArgsConstructor
@Table(name = "Likes")
public class Like extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

