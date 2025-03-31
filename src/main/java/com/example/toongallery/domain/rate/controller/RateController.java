package com.example.toongallery.domain.rate.controller;

import com.example.toongallery.domain.rate.service.RateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rate")
@RequiredArgsConstructor
public class RateController {

    private final RateService rateService;

    @PostMapping("/{userId}/{episodeId}")
    public ResponseEntity<Void> rateEpisode(@PathVariable Long userId, @PathVariable Long episodeId, @RequestParam int rate) {
        rateService.rateEpisode(userId, episodeId, rate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public void deleteRate(@RequestParam Long userId, @RequestParam Long episodeId) {
        rateService.deleteRate(userId, episodeId);
    }

    @GetMapping("/average/{webtoonId}")
    public ResponseEntity<Double> getAverageRate(@PathVariable Long webtoonId) {
        Double averageRate = rateService.getAverageRateByWebtoonId(webtoonId);
        return ResponseEntity.ok(averageRate);
    }
}
