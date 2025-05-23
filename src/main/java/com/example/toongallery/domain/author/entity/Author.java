package com.example.toongallery.domain.author.entity;

import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "webtoon_id", nullable = false)
    private Webtoon webtoon;

    public Author(User user, Webtoon webtoon) {
        this.user = user;
        this.webtoon = webtoon;
    }

    public Long getUserId() {
        return user.getId();
    }
}
