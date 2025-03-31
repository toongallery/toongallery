package com.example.toongallery.domain.comment.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "comments")
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long Id;

    private String content;

    @ManyToOne
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private int likeCount;

    public Comment(Episode episode, User user, String content, Comment parent) {
        this.episode = episode;
        this.user = user;
        this.content = content;
        this.parent = parent;
        this.likeCount = 0;
    }

    public static Comment of(Episode episode, User user, String content, Comment parent) {
        return new Comment(episode, user, content, parent);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
