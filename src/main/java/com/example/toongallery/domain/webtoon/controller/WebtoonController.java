package com.example.toongallery.domain.webtoon.controller;

import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonPopularResponse;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonPageResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.service.WebtoonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonController {

    private final WebtoonService webtoonService;

    //웹툰 생성
    @PostMapping
    public ResponseEntity<WebtoonResponse> createWebtoon(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestPart(value = "json") WebtoonSaveRequest webtoonSaveRequest,
            @RequestPart(value = "thumbnail")MultipartFile thumbnailFile
            ){
        WebtoonResponse webtoon = webtoonService.saveWebtoon(authUser, webtoonSaveRequest, thumbnailFile);
        return ResponseEntity.ok(webtoon);
    }

    //웹툰 전체 조회
    @GetMapping
   Mapping("/search")
    public ResponseEntity<Page<WebtoonResponse>> searchWebtoons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthUser authUser
    ){

        Page<WebtoonResponse> result = webtoonService.searchWebtoons(
                keyword, genres, author, page, size
        );

        if(authUser != null && result.getTotalElements()==1){
            Long webtoonId = result.getContent().get(0).getId();

            webtoonService.incrementWebtoonView( webtoonId, authUser.getUserId());
        }

        return ResponseEntity.ok(result);
    }

    //웹툰 검색(캐시 적용)
    @GetMapping("/v2/search")
    public ResponseEntity<Page<WebtoonResponse>> searchWebtoonsUserCache(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthUser authUser
            ){

        Page<WebtoonResponse> result = webtoonService.searchWebtoons(
                keyword, genres, author, page, size
        );

        if(authUser != null && result.getTotalElements()==1){
            Long webtoonId = result.getContent().get(0).getId();

            webtoonService.incrementWebtoonViewUseCache(webtoonId, authUser.getUserId());
        }

        return ResponseEntity.ok(result);
    }

    //인기 웹툰 조회
    @GetMapping("/popular")
    public ResponseEntity<List<WebtoonPopularResponse>> getPopularWebtoons(){
        List<WebtoonPopularResponse> popularWebtoons = webtoonService.getPopularWebtoons();
        return ResponseEntity.ok(popularWebtoons);
    }

    @GetMapping("/{webtoonId}/rate")
    public Double getAverageRate(@PathVariable Long webtoonId) {
        Webtoon webtoon = webtoonService.getWebtoonWithAverageRate(webtoonId);
        return webtoon.getRate();
    }
}
