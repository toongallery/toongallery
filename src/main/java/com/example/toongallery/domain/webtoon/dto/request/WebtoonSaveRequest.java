package com.example.toongallery.domain.webtoon.dto.request;

import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonSaveRequest {

    @NotBlank
    private String title;

    //추가할 작가 이름 리스트
    @NotEmpty
    private List<String> authors;

    @NotEmpty
    private List<String> category;

//    @NotBlank
//    private String thumbnail;

    @NotBlank
    private String description;

    @NotNull
    private DayOfWeek day_of_week;

//    @NotBlank
//    private WebtoonStatus status;
}
