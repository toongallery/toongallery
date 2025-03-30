package com.example.toongallery.domain.webtoon.service;

import com.example.toongallery.domain.author.service.AuthorService;
import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.category.repository.CategoryRepository;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.image.service.ImageService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonPopularResponse;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.entity.WebtoonViewLog;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.webtoon.repository.WebtoonViewLogRepository;
import com.example.toongallery.domain.webtooncategory.service.WebtoonCategoryService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorService authorService;
    private final WebtoonCategoryService webtoonCategoryService;
    private final ImageService imageService;
    private final CacheManager cacheManager;
    private final WebtoonViewLogRepository webtoonViewLogRepository;

    @Transactional
    public WebtoonResponse saveWebtoon(AuthUser authUser, WebtoonSaveRequest request, MultipartFile thumbnailFile) {
        User currentUser = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

        if (currentUser.getUserRole() != UserRole.ROLE_AUTHOR) {
            throw new BaseException(ErrorCode.INVALID_USER_ROLE, "작가만 웹툰 등록 가능");
        }

        // [2] 작가 이름으로 조회
        List<String> requestedAuthorNames = Optional.ofNullable(request.getAuthors())
                .orElse(Collections.emptyList());

        List<User> authors = userRepository.findByNameIn(requestedAuthorNames);
        List<String> foundAuthorNames = authors.stream().map(User::getName).toList();
        List<String> notFoundAuthors = requestedAuthorNames.stream()
                .filter(name -> !foundAuthorNames.contains(name))
                .toList();

            //작가가 아닌 사용자가 포함되었는지 체크
            List<User> nonAuthors = authors.stream()
                    .filter(user->user.getUserRole() != UserRole.ROLE_AUTHOR)
                    .toList();

        if (!notFoundAuthors.isEmpty()) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 작가: " + notFoundAuthors);
        }

        boolean hasNonAuthor = authors.stream()
                .anyMatch(user -> user.getUserRole() != UserRole.ROLE_AUTHOR);
        if (hasNonAuthor) {
            throw new BaseException(ErrorCode.INVALID_USER_ROLE, "작가가 아닌 유저 포함됨");
        }

        // [3] 카테고리 이름으로 조회
        List<String> requestedCategoryNames = Optional.ofNullable(request.getCategory())
                .orElse(Collections.emptyList());

        List<Category> categories = categoryRepository.findByCategoryNameIn(requestedCategoryNames);
        List<String> foundCategoryNames = categories.stream().map(Category::getCategoryName).toList();
        List<String> notFoundCategories = requestedCategoryNames.stream()
                .filter(name -> !foundCategoryNames.contains(name))
                .toList();

        if (!notFoundCategories.isEmpty()) {
            throw new BaseException(ErrorCode.CATEGORY_NOT_EXIST, "존재하지 않는 카테고리: " + notFoundCategories);
        }

        // [4] 웹툰 저장 (썸네일 제외)
        Webtoon webtoon = Webtoon.of(
                request.getTitle(),
                null,  // 썸네일은 아직
                request.getDescription(),
                request.getDay_of_week(),
                WebtoonStatus.ONGOING
        );
        webtoonRepository.save(webtoon);

        // [5] 썸네일 업로드 후 웹툰에 반영
        String thumbnailUrl = imageService.uploadWebtoonThumbnail(webtoon.getId(), thumbnailFile);
        webtoon.updateThumbnail(thumbnailUrl); // 세터 없이 반영

        // [6] 작가 매핑
        authorService.createAuthors(webtoon, authors);

        // [7] 카테고리 매핑
        webtoonCategoryService.createWebtoonCategory(webtoon, categories);

        return new WebtoonResponse(
                webtoon.getId(),
                webtoon.getTitle(),
                requestedAuthorNames,
                requestedCategoryNames,
                webtoon.getThumbnail(),
                webtoon.getDescription(),
                webtoon.getDay_of_week(),
                webtoon.getStatus(),
                webtoon.getRate(),
                webtoon.getFavorite_count(),
                webtoon.getViews()
        );
    }

    //웹툰 전채 조회
    @Transactional(readOnly = true)
    public Page<WebtoonResponse> getWebtoons(int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size);

        Page<Webtoon> webtoons = webtoonRepository.findAll(pageable);

        return webtoons.map(webtoon -> {

            List<String> authorNames = authorService.getAuthorNamesByWebtoonId(webtoon.getId());
            
            List<String> categoryList = webtoonCategoryService.getCategoryNamesByWebtoonId(webtoon.getId());

            return new WebtoonResponse(
                    webtoon.getId(),
                    webtoon.getTitle(),
                    authorNames,
                    categoryList,
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
            List<String> categoryList = webtoonCategoryService.getCategoryNamesByWebtoonId(webtoon.getId());
            return new WebtoonResponse(
                    webtoon.getId(),
                    webtoon.getTitle(),
                    authorNames,
                    categoryList,
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

    // 웹툰 제목 변경
    // 웹툰 작가
}
