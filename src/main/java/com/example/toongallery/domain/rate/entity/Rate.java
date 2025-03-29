package com.example.toongallery.domain.rate.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rates")
public class Rate extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rates;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
