package com.example.toongallery.domain.rate.controller;

import com.example.toongallery.domain.rate.service.RateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rate")
@RequiredArgsConstructor
public class RateController {

    private final RateService rateService;

    @PostMapping
    public void rateEpisode(@RequestParam Long userId, @RequestParam Long episodeId, @RequestParam int rate) {
        rateService.rateEpisode(userId, episodeId, rate);
    }

    @DeleteMapping
    public void deleteRate(@RequestParam Long userId, @RequestParam Long episodeId) {
        rateService.deleteRate(userId, episodeId);
    }

    @GetMapping("/average")
    public Double getAverageRate(@RequestParam Long episodeId) {
        return rateService.getAverageRate(episodeId);
    }
}
