package com.example.toongallery.domain.webtoon.controller;

import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonPageResponse;
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
//    @GetMapping
//    public ResponseEntity<Page<WebtoonPageResponse>> getWebtoons(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size
//    ){
//        return ResponseEntity.ok(webtoonService.getWebtoons(page, size));
//    }
//
//    @GetMapping("/{webtoonId}")
//    public ResponseEntity<WebtoonPageResponse>getWebtoon(
//            @PathVariable Long webtoonId){
//        webtoonService.getWebtoon(webtoonId);
//        return ResponseEntity.ok().build();
//    }
//
//    //웹툰 검색
//    @GetMapping("/search")
//    public ResponseEntity<Page<WebtoonResponse>> searchWebtoons(
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) List<String> genres,
//            @RequestParam(required = false) String author,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size
//            ){
//        return ResponseEntity.ok(webtoonService.searchWebtoons(keyword, genres, author, page, size));
//    }
}
