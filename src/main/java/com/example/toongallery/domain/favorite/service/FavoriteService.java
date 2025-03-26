package com.example.toongallery.domain.favorite.service;

import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.favorite.entity.Favorite;
import com.example.toongallery.domain.favorite.repository.FavoriteRepository;
import com.example.toongallery.domain.like.entity.Like;
import com.example.toongallery.domain.like.repository.LikeRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final WebtoonRepository webtoonRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggle(Long userId, Long webtoonId) {
        if (favoriteRepository.existsByUserIdAndWebtoonId(userId, webtoonId)) {
            favoriteRepository.deleteByUserIdAndWebtoonId(userId, webtoonId);
            return false;//좋아요가 true(이미 좋아요인 상태)이면 false(좋아요 취소)로 변경
        }

        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        Favorite favorite = new Favorite();
        favorite.setWebtoon(webtoon);
        favorite.setUser(user);

        favoriteRepository.save(favorite);
        return true;
    }

}
