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
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.webtooncategory.service.WebtoonCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorService authorService;
    private final WebtoonCategoryService webtoonCategoryService;
    private final ImageService imageService;

    @Transactional
    public WebtoonResponse saveWebtoon(AuthUser authUser, WebtoonSaveRequest request, MultipartFile thumbnailFile) {
        // [1] 로그인 유저 확인 및 권한 체크
        System.out.println("[1] 웹툰 등록 요청자: " + authUser.getEmail());

        User currentUser = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> {
                    System.out.println("[ERROR] 유저 없음");
                    return new BaseException(ErrorCode.USER_NOT_EXIST, null);
                });

        System.out.println("[2] 현재 유저 역할: " + currentUser.getUserRole());
        if (currentUser.getUserRole() != UserRole.ROLE_ADMIN) {
            System.out.println("[ERROR] 권한 없음 - 관리자 아님");
            throw new BaseException(ErrorCode.INVALID_USER_ROLE, "작가만 웹툰 등록 가능");
        }

        // [2] 작가 이름으로 조회
        List<String> requestedAuthorNames = Optional.ofNullable(request.getAuthors())
                .orElse(Collections.emptyList());

        System.out.println("[3] 요청된 작가 목록: " + requestedAuthorNames);

        List<User> authors = userRepository.findByNameIn(requestedAuthorNames);
        List<String> foundAuthorNames = authors.stream().map(User::getName).toList();
        List<String> notFoundAuthors = requestedAuthorNames.stream()
                .filter(name -> !foundAuthorNames.contains(name))
                .toList();

        System.out.println("[4] 조회된 작가 수: " + authors.size());

        if (!notFoundAuthors.isEmpty()) {
            System.out.println("[ERROR] 존재하지 않는 작가: " + notFoundAuthors);
            throw new BaseException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 작가: " + notFoundAuthors);
        }

        boolean hasNonAuthor = authors.stream()
                .anyMatch(user -> user.getUserRole() != UserRole.ROLE_ADMIN);
        if (hasNonAuthor) {
            System.out.println("[ERROR] 작가가 아닌 유저 포함됨");
            throw new BaseException(ErrorCode.INVALID_USER_ROLE, "작가가 아닌 유저 포함됨");
        }

        // [3] 카테고리 이름으로 조회
        List<String> requestedCategoryNames = Optional.ofNullable(request.getCategory())
                .orElse(Collections.emptyList());

        System.out.println("[5] 요청된 카테고리: " + requestedCategoryNames);

        List<Category> categories = categoryRepository.findByCategoryNameIn(requestedCategoryNames);
        List<String> foundCategoryNames = categories.stream().map(Category::getCategoryName).toList();
        List<String> notFoundCategories = requestedCategoryNames.stream()
                .filter(name -> !foundCategoryNames.contains(name))
                .toList();

        System.out.println("[6] 조회된 카테고리 수: " + categories.size());

        if (!notFoundCategories.isEmpty()) {
            System.out.println("[ERROR] 존재하지 않는 카테고리: " + notFoundCategories);
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
        System.out.println("[7] 웹툰 저장 완료 - ID: " + webtoon.getId());

        // [5] 썸네일 업로드 후 웹툰에 반영
        String thumbnailUrl = imageService.uploadWebtoonThumbnail(webtoon.getId(), thumbnailFile);
        webtoon.updateThumbnail(thumbnailUrl); // 세터 없이 반영
        System.out.println("[7-1] 썸네일 업로드 완료 - URL: " + thumbnailUrl);

        // [6] 작가 매핑
        authorService.createAuthors(webtoon, authors);
        System.out.println("[8] 작가 매핑 저장 완료");

        // [7] 카테고리 매핑
        webtoonCategoryService.createWebtoonCategory(webtoon, categories);
        System.out.println("[9] 카테고리 매핑 저장 완료");

        // [8] 응답 반환
        System.out.println("[10] 웹툰 등록 완료. 응답 반환 시작");

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



//
//    @Transactional(readOnly = true)
//    public Page<WebtoonPageResponse> getWebtoons(int page, int size) {
//        Pageable pageable = PageRequest.of(page-1, size);
//
//        Page<Webtoon> webtoons = webtoonRepository.findAll(pageable);
//
//        return webtoons.map(webtoon -> {
//
//            List<String> authorNames = authorService.getAuthorNamesByWebtoonId(webtoon.getId());
//
//            List<String> genreList = Arrays.asList(webtoon.getGenres().split(","));
//
//            return new WebtoonPageResponse(
//                    webtoon.getTitle(),//제목
//                    authorNames, // 작가 이름
//                    genreList, //장르
//                    webtoon.getThumbnail(), // 나와야할거 같음
//                    webtoon.getDescription()
//            );
//        });
//    }
//
//    @Transactional(readOnly = true)
//    public Page<WebtoonResponse> searchWebtoons(
//            String keyword,
//            List<String> genres,
//            String authorName,
//            int page, int size
//    ){
//        Pageable pageable = PageRequest.of(page-1, size);
//
//        Page<Webtoon> webtoons = webtoonRepository.findBySearch(
//                keyword, genres, authorName, pageable
//        );
//        System.out.println("[조회 결과] 총 " + webtoons.getTotalElements() + "건");
//
//        return webtoons.map(webtoon->{
//            List<String> authorNames = authorService.getAuthorNamesByWebtoonId(webtoon.getId());
//            System.out.println("작가 목록: " + authorNames);
//
//            List<String> genreList = Arrays.asList(webtoon.getGenres().split(","));
//
//            return new WebtoonResponse(
//                    webtoon.getId(),
//                    webtoon.getTitle(),
//                    authorNames,
//                    genreList,
//                    webtoon.getThumbnail(),
//                    webtoon.getDescription(),
//                    webtoon.getDay_of_week(),
//                    webtoon.getStatus(),
//                    webtoon.getRate(),
//                    webtoon.getFavorite_count(),
//                    webtoon.getViews()
//            );
//        });
//    }
//
//    public WebtoonResponse getWebtoon(Long webtoonId) {
//
//        Webtoon webtoon = webtoonRepository.findById(webtoonId)
//                .orElseThrow(() -> new BaseException(ErrorCode.WEBTOON_NOT_FOUND, null));
//        List<String> authorNames = authorService.getAuthorNamesByWebtoonId(webtoon.getId());
//        return new WebtoonResponse(
//                webtoon.getId(),
//                webtoon.getTitle(),
//                webtoon.getGenres(),
//                authorNames,
//                webtoon.getThumbnail(),
//                webtoon.getDescription(),
//                webtoon.getDay_of_week(),
//                webtoon.getStatus(),
//                webtoon.getRate(),
//                webtoon.getFavorite_count(),
//                webtoon.getViews()
//        );
//    }
}
