package com.example.toongallery.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CategoryRequest {
    @NotBlank
    @Size(max = 10, message = "카테고리명은 10자 이내여야 합니다.")
    @Pattern(regexp = "^[A-Za-z]+$", message = "카테고리명은 영어 대소문자만 입력 가능합니다.")
    //@Pattern(regexp = "^[가-힣]+$", message = "카테고리명은 한글만 입력 가능합니다.")
    private String categoryName;
}
