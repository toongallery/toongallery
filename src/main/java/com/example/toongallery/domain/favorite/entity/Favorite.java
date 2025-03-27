package com.example.toongallery.domain.favorite.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "favorites")
@Builder
@AllArgsConstructor
public class Favorite extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webtoon_id")
    private Webtoon webtoon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


}
