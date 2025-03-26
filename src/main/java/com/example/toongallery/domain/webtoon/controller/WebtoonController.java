package com.example.toongallery.domain.webtoon.controller;

import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.service.WebtoonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webtoons")
public class WebtoonController {

    private final WebtoonService webtoonService;

    //웹툰 생성
    @PostMapping
    public ResponseEntity<String> createWebtoon(
            @RequestBody WebtoonSaveRequest webtoonSaveRequest,
            @AuthenticationPrincipal AuthUser authUser
            ){
        webtoonService.saveWebtoon(authUser, webtoonSaveRequest);
        return ResponseEntity.ok("웹툰 등록이 완료되었습니다!");
    }

    //웹툰 전체 조회
    @GetMapping
    public ResponseEntity<Page<WebtoonResponse>> getWebtoons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(webtoonService.getWebtoons(page, size));
    }
}
