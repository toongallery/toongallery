package com.example.toongallery.domain.webtoon.service;

import com.example.toongallery.domain.author.service.AuthorService;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonPopularResponse;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.entity.WebtoonViewLog;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.webtoon.repository.WebtoonViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;
    private final UserRepository userRepository;
    private final AuthorService authorService;
    private final CacheManager cacheManager;
    private final WebtoonViewLogRepository webtoonViewLogRepository;

    @Transactional
    public Webtoon saveWebtoon(AuthUser authUser, WebtoonSaveRequest webtoonSaveRequest) {

        //현재 로그인한 사용자 가져오기
        User mainAuthor = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(()->new BaseException(ErrorCode.USER_NOT_EXIST,null));

        //사용자가 작가가 아닐 경우 예외처리
        if(authUser.getUserRole() != UserRole.ROLE_AUTHOR){
            throw new BaseException(ErrorCode.INVALID_USER_ROLE,null);
        }

        //추가할 작가 이름으로 사용자 조회(있는 경우에만 실행)
        List<User> authors = new ArrayList<>();

        if(webtoonSaveRequest.getAuthors() != null && !webtoonSaveRequest.getAuthors().isEmpty()) {
            authors = userRepository.findByNameIn(webtoonSaveRequest.getAuthors());

            if(authors.size() != webtoonSaveRequest.getAuthors().size()) {
                throw new BaseException(ErrorCode.USER_NOT_FOUND,null);
            }

            //작가가 아닌 사용자가 포함되었는지 체크
            List<User> nonAuthors = authors.stream()
                    .filter(user->user.getUserRole() != UserRole.ROLE_AUTHOR)
                    .collect(Collectors.toList());

            if(!nonAuthors.isEmpty()) {
                throw new BaseException(ErrorCode.INVALID_USER_ROLE,null);
            }
        }

        //작가 리스트에 본인 추가
        authors.add(mainAuthor);

        //List<String>을 콤마(,)로 연결하여 저장
        String genreString = String.join(",", webtoonSaveRequest.getGenres());

        Webtoon webtoon = new Webtoon(
                webtoonSaveRequest.getTitle(),
                genreString,
                webtoonSaveRequest.getThumbnail(),
                webtoonSaveRequest.getDescription(),
                webtoonSaveRequest.getDay_of_week(),
                webtoonSaveRequest.getStatus()
        );
        Webtoon savedWebtoon = webtoonRepository.save(webtoon);

        authorService.createAuthors(savedWebtoon, authors);

        return savedWebtoon;
    }

    //웹툰 전채 조회
    @Transactional(readOnly = true)
    public Page<WebtoonResponse> getWebtoons(int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size);

        Page<Webtoon> webtoons = webtoonRepository.findAll(pageable);

        return webtoons.map(webtoon -> {

            Integer cachedViews = getCurrentViewCount(webtoon.getId());

            System.out.println("cachedViews:"+cachedViews);

            List<String> authorNames = authorService.getAuthorNamesByWebtoonId(webtoon.getId());

            List<String> genreList = Arrays.asList(webtoon.getGenres().split(","));

            return new WebtoonResponse(
                    webtoon.getId(),
                    webtoon.getTitle(),
                    authorNames,
                    genreList,
                    webtoon.getThumbnail(),
                    webtoon.getDescription(),
                    webtoon.getDay_of_week(),
                    webtoon.getStatus(),
                    webtoon.getRate(),
                    webtoon.getFavorite_count(),
                    webtoon.getViews()
            );
        });
    }

    //웹툰 검색
    @Transactional(readOnly = true)
    public Page<WebtoonResponse> searchWebtoons(
            String keyword,
            List<String> genres,
            String authorName,
            int page, int size
    ){
        Pageable pageable = PageRequest.of(page-1, size);

        Page<Webtoon> webtoons = webtoonRepository.findBySearch(
                keyword, genres, authorName, pageable
        );

        return webtoons.map(webtoon->{

            List<String> authorNames = authorService.getAuthorNamesByWebtoonId(webtoon.getId());

            List<String> genreList = Arrays.asList(webtoon.getGenres().split(","));

            return new WebtoonResponse(
                    webtoon.getId(),
                    webtoon.getTitle(),
                    authorNames,
                    genreList,
                    webtoon.getThumbnail(),
                    webtoon.getDescription(),
                    webtoon.getDay_of_week(),
                    webtoon.getStatus(),
                    webtoon.getRate(),
                    webtoon.getFavorite_count(),
                    webtoon.getViews()
            );
        });
    }

    //웹툰 조회수 증가(캐시 미적용)
    @Transactional
    public void incrementWebtoonView(Long webtoonId , Long userId){
        LocalDate today = LocalDate.now();

        //오늘 조회한 적이 없는 경우만 처리
        if(!webtoonViewLogRepository.existsByUserIdAndWebtoonIdAndViewDate(userId, webtoonId, today)){
            Webtoon webtoon = webtoonRepository.findById(webtoonId)
                    .orElseThrow(()-> new BaseException(ErrorCode.WEBTOON_NOT_FOUND, null));

            webtoon.incrementView();

            //조회 기록 저장
            WebtoonViewLog webtoonViewLog = new WebtoonViewLog();
            webtoonViewLog.setUserId(userId);
            webtoonViewLog.setWebtoonId(webtoonId);
            webtoonViewLog.setViewDate(today);
            webtoonViewLogRepository.save(webtoonViewLog);
        }
    }

    //웹툰 조회수 증가(캐시 적용)
    @Transactional
    public void incrementWebtoonViewUseCache(Long webtoonId, Long userId) {
        String today = LocalDate.now().toString();
        Cache userViewCache = cacheManager.getCache("webtoonViews");

        //캐시 키 생성
        String cacheKey = String.format("%s_%s_%s", userId, today, webtoonId);

        //캐시에 없으면 조회수 증가
        if(userViewCache != null && userViewCache.get(cacheKey) == null) {
            Webtoon webtoon = webtoonRepository.findById(webtoonId)
                    .orElseThrow(()-> new BaseException(ErrorCode.WEBTOON_NOT_FOUND, null));

            userViewCache.put(cacheKey, "viewed");//캐시 저장
            //웹툰 DB에 있는 값을 가져와 저장
            webtoon.view(incrementWebtoonViewCount(webtoonId));
        }
    }

    //조회수 캐시
    @CachePut(value = "webtoonViewCounts", cacheManager = "webtoonViewCountCacheManager",key = "#webtoonId")
    public int incrementWebtoonViewCount(Long webtoonId){
        return getCurrentViewCount(webtoonId)+1;
    }

    @Cacheable(value = "webtoonViewCounts", cacheManager = "webtoonViewCountCacheManager", key = "#webtoonId")
    public int getCurrentViewCount(Long webtoonId){
        return webtoonRepository.findById(webtoonId)
                .map(Webtoon::getViews)
                .orElse(0);
    }

    //자정에 조회수 캐시 전체 초기화
    @Scheduled(cron = "0 0 0 * * ?")//매일 자정 실행
    @CacheEvict(cacheNames = "webtoonViewCounts",
    cacheManager = "webtoonViewCountCacheManager",
    allEntries = true)
    public void resetViewCountsAtMidnight(){
    }

    //자정에 조회 확인 캐시 전체 초기화
    @Scheduled(cron = "0 0 0 * * ?")//매일 자정 실행
    public void resetUserViewAtMidnight(){
        Cache cache = cacheManager.getCache("webtoonViews");
        if(cache != null){
            cache.clear();
        }
    }

    //인기 검색어 조회
    @Transactional(readOnly = true)
    public List<WebtoonPopularResponse> getPopularWebtoons(){
        List<Webtoon> webtoons = webtoonRepository.findPopularWebtoonsTop10();

        return IntStream.range(0, webtoons.size())
                .mapToObj(i->new WebtoonPopularResponse(
                        i+1, webtoons.get(i)
                ))
                .collect(Collectors.toList());
    }


}