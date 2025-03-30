package com.example.toongallery.domain.webtoon.service;

import com.example.toongallery.domain.author.service.AuthorService;
import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.category.repository.CategoryRepository;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.image.service.ImageService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonPopularResponse;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.webtoon.repository.WebtoonViewLogRepository;
import com.example.toongallery.domain.webtooncategory.service.WebtoonCategoryService;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WebtoonServiceTest {

    @Mock
    private WebtoonRepository webtoonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorService authorService;

    @Mock
    private WebtoonCategoryService webtoonCategoryService;

    @Mock
    private ImageService imageService;

    @Mock
    private WebtoonViewLogRepository webtoonViewLogRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    @Qualifier("webtoonViewCountCacheManager")
    private CacheManager webtoonViewCountCacheManager;

    @InjectMocks
    private WebtoonService webtoonService;

    private Webtoon testWebtoon;
    private Cache userViewCache;
    private Cache viewCountCache;

    @BeforeEach
    void setUp() {
        testWebtoon = Webtoon.of(
                "테스트 웹툰",
                "thumbnail.jpg",
                "테스트 설명",
                DayOfWeek.MON,
                WebtoonStatus.ONGOING
        );
        testWebtoon.setId(1L);
        testWebtoon.setViews(100);

        userViewCache = new ConcurrentMapCache("webtoonViews");

        CaffeineCache caffeineCache = new CaffeineCache("webtoonViewCounts",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.DAYS)
                        .maximumSize(1000)
                        .build(),
                true);

        viewCountCache = caffeineCache;

        when(cacheManager.getCache("webtoonViews")).thenReturn(userViewCache);
        when(webtoonViewCountCacheManager.getCache("webtoonViewCounts")).thenReturn(viewCountCache);

        when(webtoonRepository.findById(1L)).thenReturn(Optional.of(testWebtoon));

        when(webtoonViewLogRepository.existsByUserIdAndWebtoonIdAndViewDate(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(false);
    }

    @Test
    @DisplayName("대량 요청 성능 비교 테스트")
    void manyRequestTest() {
        //캐시 초기화
        userViewCache.clear();
        viewCountCache.clear();

        //캐시 미적용 테스트
        long startNoCache = System.currentTimeMillis();
        IntStream.range(0, 10000).parallel().forEach(i ->
                webtoonService.incrementWebtoonView(1L, (long) i));
        long endNoCache = System.currentTimeMillis();

        //캐시 적용 테스트 (캐시 초기화 후 진행)
        userViewCache.clear();
        viewCountCache.clear();
        viewCountCache.put(1L, 100); //초기 조회수 재설정

        long startWithCache = System.currentTimeMillis();
        IntStream.range(0, 10000).parallel().forEach(i ->
                webtoonService.incrementWebtoonViewUseCache(1L, (long) i));
        long endWithCache = System.currentTimeMillis();

        System.out.println("캐시 미적용 10,000회: " + (endNoCache - startNoCache) + "ms");
        System.out.println("캐시 적용 10,000회: " + (endWithCache - startWithCache) + "ms");

        assertTrue((endNoCache - startNoCache) > (endWithCache - startWithCache),
                "캐시 적용 버전이 더 빨라야 함");
    }

    @Test
    @DisplayName("대용량 웹툰 데이터 검색 성능 테스트")
    void searchPerformanceWithLargeData() {
        //5만건의 더미 웹툰 데이터 생성
        List<Webtoon> dummyWebtoons = IntStream.rangeClosed(1, 50000)
                .mapToObj(i -> {
                    Webtoon webtoon = Webtoon.of(
                            "웹툰제목_" + i,
                            "thumbnail_" + i + ".jpg",
                            "설명_" + i,
                            DayOfWeek.values()[i % 7],
                            WebtoonStatus.values()[i % 3]
                    );
                    webtoon.setId((long) i);
                    webtoon.setViews(i % 1000);
                    return webtoon;
                })
                .collect(Collectors.toList());

        //Mock 설정
        when(webtoonRepository.findBySearch(anyString(), anyList(), anyString(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    String keyword = invocation.getArgument(0);
                    List<String> genres = invocation.getArgument(1);
                    int page = ((Pageable) invocation.getArgument(3)).getPageNumber();
                    int size = ((Pageable) invocation.getArgument(3)).getPageSize();

                    List<Webtoon> filtered = dummyWebtoons.stream()
                            .filter(w -> w.getTitle().contains(keyword))
                            .skip(page * size)
                            .limit(size)
                            .collect(Collectors.toList());

                    return new PageImpl<>(filtered, PageRequest.of(page, size), dummyWebtoons.size());
                });

        // 성능 측정
        long start = System.currentTimeMillis();
        Page<WebtoonResponse> result = webtoonService.searchWebtoons("웹툰", List.of(), "", 1, 20);
        long duration = System.currentTimeMillis() - start;

        System.out.println("5만건 데이터에서 검색 소요 시간: " + duration + "ms");
        assertEquals(20, result.getContent().size());
    }

    @Test
    @DisplayName("인기 웹툰 조회 캐시 성능 테스트")
    void popularWebtoonsCacheTest(){
        //5만건 웹툰 생성 (상위 100개는 인기 웹툰)
        List<Webtoon> dummyWebtoons = IntStream.rangeClosed(1, 50000)
                .mapToObj(i->{
                    Webtoon webtoon = Webtoon.of("웹툰_"+i,"","",DayOfWeek.MON, WebtoonStatus.ONGOING);
                    webtoon.setId((long) i);
                    webtoon.setViews(i <= 100? 10000 : 100);//상위 100는 인기 웹툰
                    return webtoon;
                })
                .collect(Collectors.toList());

        //인기 웹툰 Mock 설정
        when(webtoonRepository.findPopularWebtoonsTop10())
                .thenAnswer(invocation -> dummyWebtoons.stream()
                        .sorted((a, b)->b.getViews() - a.getViews())
                        .limit(10)
                        .collect(Collectors.toList()));

        //캐시 적용 전 성능 측정
        long start = System.currentTimeMillis();
        List<WebtoonPopularResponse> result = webtoonService.getPopularWebtoons();
        long duration = System.currentTimeMillis() - start;

        System.out.println("인기 웹툰 조회 첫 요청: "+duration+"ms");
        assertEquals(10, result.size());

        //캐시 적용 후 성능 측정
        start = System.currentTimeMillis();
        result = webtoonService.getPopularWebtoons();
        duration = System.currentTimeMillis() - start;

        System.out.println("인기 웹툰 조회 캐시 사용 시: "+duration+"ms");
    }

    @Test
    @DisplayName("웹툰 저장 기능 테스트")
    void saveWebtoon(){
        //테스트 데이터 준비
        AuthUser authUser = new AuthUser(1L, "author@example.com", UserRole.ROLE_AUTHOR);
        WebtoonSaveRequest request = new WebtoonSaveRequest(
                "테스트 웹툰",
                List.of("작가1", "작가2"),
                List.of("로맨스", "코미디"),
                "테스트 설명",
                DayOfWeek.MON
        );
        MultipartFile thumbnailFile = mock(MultipartFile.class);

        //Mock 설정
        User authorUser = new User(
                "author@example.com",
                "encodedPassword",
                "작가1",
                LocalDate.of(1999, 11, 17),
                Gender.MALE,
                UserRole.ROLE_AUTHOR
        );

        User coAuthorUser = new User(
                "coauthor@example.com",
                "encodedPassword",
                "작가2",
                LocalDate.of(1997, 8, 25),
                Gender.FEMALE,
                UserRole.ROLE_AUTHOR
        );

        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(authorUser));

        when(userRepository.findByNameIn(List.of("작가1", "작가2")))
                .thenReturn(List.of(authorUser, coAuthorUser));

        //카테고리 Mock 설정
        Category romance = Category.of("로맨스");
        Category comedy = Category.of("코미디");
        when(categoryRepository.findByCategoryNameIn(List.of("로맨스", "코미디")))
                .thenReturn(List.of(romance, comedy));

        when(imageService.uploadWebtoonThumbnail(anyLong(), any()))
                .thenReturn("https://example.com/thumbnail.jpg");

        // 5. Mock 웹툰 저장 설정
        when(webtoonRepository.save(any(Webtoon.class)))
                .thenAnswer(invocation -> {
                    Webtoon webtoon = invocation.getArgument(0);
                    webtoon.setId(1L); // 저장 후 ID 설정
                    return webtoon;
                });

        //테스트 실행
        WebtoonResponse response = webtoonService.saveWebtoon(authUser, request, thumbnailFile);

        //검증
        assertNotNull(response);
        assertEquals("테스트 웹툰", response.getTitle());
        assertEquals(2, response.getAuthors().size());
        assertEquals(2, response.getGenres().size());
        assertEquals("https://example.com/thumbnail.jpg", response.getThumbnail());
        assertEquals("테스트 설명", response.getDescription());
        assertEquals(DayOfWeek.MON, response.getDayOfWeek());

        //상호작용 검증
        verify(webtoonRepository).save(any(Webtoon.class));
        verify(authorService).createAuthors(any(Webtoon.class), eq(List.of(authorUser, coAuthorUser)));
        verify(webtoonCategoryService).createWebtoonCategory(
                any(Webtoon.class),
                eq(List.of(romance, comedy))
        );
        verify(imageService).uploadWebtoonThumbnail(eq(1L), eq(thumbnailFile));
    }

    @DisplayName("웹툰 저장 실패 - 로그인한 유저가 작가가 아님")
    @Test
    void saveWebtoon_실패_작성자가아님() {
        // given
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ROLE_USER); // 작가 아님
        WebtoonSaveRequest request = new WebtoonSaveRequest(
                "웹툰 제목",
                List.of("작가1"),
                List.of("로맨스"),
                "설명입니다",
                DayOfWeek.MON
        );
        MultipartFile thumbnailFile = mock(MultipartFile.class);

        User user = new User(
                "user@example.com", "pw", "유저", LocalDate.now(), Gender.MALE, UserRole.ROLE_USER
        );
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // when & then
        BaseException ex = assertThrows(BaseException.class, () ->
                webtoonService.saveWebtoon(authUser, request, thumbnailFile));

        assertEquals(ErrorCode.INVALID_USER_ROLE, ex.getErrorCode());
        assertEquals("작가만 웹툰 등록 가능", ex.getField());
    }

    @DisplayName("웹툰 저장 실패 - 작가 목록에 존재하지 않는 유저 포함")
    @Test
    void saveWebtoon_실패_존재하지않는작가() {
        // given
        AuthUser authUser = new AuthUser(1L, "author@example.com", UserRole.ROLE_AUTHOR);
        WebtoonSaveRequest request = new WebtoonSaveRequest(
                "웹툰 제목",
                List.of("작가1", "작가2"),
                List.of("로맨스"),
                "설명입니다",
                DayOfWeek.MON
        );
        MultipartFile thumbnailFile = mock(MultipartFile.class);

        User authorUser = new User(
                "author@example.com", "pw", "작가1", LocalDate.now(), Gender.MALE, UserRole.ROLE_AUTHOR
        );

        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(authorUser));
        when(userRepository.findByNameIn(List.of("작가1", "작가2")))
                .thenReturn(List.of(authorUser)); // 작가2 없음

        // when & then
        BaseException ex = assertThrows(BaseException.class, () ->
                webtoonService.saveWebtoon(authUser, request, thumbnailFile));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getField().contains("작가2"), "2111"); // 누락된 작가 이름 포함 여부
    }


    @Test
    @DisplayName("전체 웹툰 조회 - 작가, 카테고리 포함")
    void getWebtoons_성공() {
        // given
        Webtoon webtoon1 = Webtoon.of("웹툰1", "thumb1.jpg", "설명1", DayOfWeek.MON, WebtoonStatus.ONGOING);
        ReflectionTestUtils.setField(webtoon1, "id", 1L);

        Webtoon webtoon2 = Webtoon.of("웹툰2", "thumb2.jpg", "설명2", DayOfWeek.TUE, WebtoonStatus.ONGOING);
        ReflectionTestUtils.setField(webtoon2, "id", 2L);

        Page<Webtoon> mockPage = new PageImpl<>(List.of(webtoon1, webtoon2));
        when(webtoonRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        when(authorService.getAuthorNamesByWebtoonId(1L)).thenReturn(List.of("작가A"));
        when(authorService.getAuthorNamesByWebtoonId(2L)).thenReturn(List.of("작가B", "작가C"));

        when(webtoonCategoryService.getCategoryNamesByWebtoonId(1L)).thenReturn(List.of("로맨스"));
        when(webtoonCategoryService.getCategoryNamesByWebtoonId(2L)).thenReturn(List.of("액션", "판타지"));

        // when
        Page<WebtoonResponse> result = webtoonService.getWebtoons(1, 10);

        // then
        assertEquals(2, result.getContent().size());

        WebtoonResponse w1 = result.getContent().get(0);
        assertEquals("웹툰1", w1.getTitle());
        assertEquals(List.of("작가A"), w1.getAuthors());
        assertEquals(List.of("로맨스"), w1.getGenres());

        WebtoonResponse w2 = result.getContent().get(1);
        assertEquals("웹툰2", w2.getTitle());
        assertEquals(List.of("작가B", "작가C"), w2.getAuthors());
        assertEquals(List.of("액션", "판타지"), w2.getGenres());
    }

    @DisplayName("전체 웹툰 조회 - 결과 없음 (빈 페이지)")
    @Test
    void getWebtoons_빈결과() {
        // given
        Page<Webtoon> emptyPage = new PageImpl<>(List.of());
        when(webtoonRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // when
        Page<WebtoonResponse> result = webtoonService.getWebtoons(1, 10);

        // then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }
}