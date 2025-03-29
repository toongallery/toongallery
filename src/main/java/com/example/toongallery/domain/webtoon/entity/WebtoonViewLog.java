package com.example.toongallery.domain.webtoon.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@NoArgsConstructor
@Table(name = "webtoon_view_logs", uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "webtoon_id", "view_date"}
))
public class WebtoonViewLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "webtoon_id", nullable = false)
    private Long webtoonId;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;
}
