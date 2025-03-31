package com.example.toongallery.domain.favorite.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.favorite.entity.Favorite;
import com.example.toongallery.domain.favorite.repository.FavoriteRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final WebtoonRepository webtoonRepository;
    private final UserRepository userRepository;

    private static final int MAX_RETRIES = 3; // 최대 재시도 횟수
    private static final long RETRY_DELAY_MS = 100; // 재시도 간 대기 시간 (밀리초)

    @Transactional
    public boolean toggle(Long userId, Long webtoonId) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                // 이미 좋아요 상태라면 취소 처리
                if (favoriteRepository.existsByUserIdAndWebtoonId(userId, webtoonId)) {
                    favoriteRepository.deleteByUserIdAndWebtoonId(userId, webtoonId);
                    Webtoon webtoon = webtoonRepository.findById(webtoonId)
                            .orElseThrow(() -> new BaseException(ErrorCode.WEBTOON_NOT_FOUND, null));
                    webtoon.setFavorite_count(Optional.ofNullable(webtoon.getFavorite_count()).orElse(0));
                    webtoon.decreaseLikeCount();

                    webtoonRepository.save(webtoon);
                    return false;
                }

                // 웹툰, 유저 정보 조회
                Webtoon webtoon = webtoonRepository.findById(webtoonId)
                        .orElseThrow(() -> new BaseException(ErrorCode.WEBTOON_NOT_FOUND, null));
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

                // 새로운 좋아요 등록
                Favorite favorite = Favorite.builder()
                        .webtoon(webtoon)
                        .user(user)
                        .build();


                favoriteRepository.save(favorite);
                webtoon.setFavorite_count(Optional.ofNullable(webtoon.getFavorite_count()).orElse(0));
                webtoon.increaseLikeCount();
                webtoonRepository.save(webtoon);
                return true;

            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("동시 수정 충돌 발생, 최대 재시도 횟수 초과");
                }
                try {
                    long retryDelay = RETRY_DELAY_MS + ThreadLocalRandom.current().nextInt(200); // 100 ~ 200ms 랜덤 대기 시간 추가
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                    throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                }
            }
        }
        return false; // 도달하지 않음
    }

    // 좋아요 수 조회 메서드 추가 가능
    public int getFavoriteCount(Long webtoonId) {
        return favoriteRepository.countByWebtoonId(webtoonId);
    }
}
