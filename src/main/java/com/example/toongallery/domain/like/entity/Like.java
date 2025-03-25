package com.example.toongallery.domain.like.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Likes")
public class Like extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

/*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commnet_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
*/

    public Like (LocalDateTime createdAt,LocalDateTime modifiedAt) {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }


}

