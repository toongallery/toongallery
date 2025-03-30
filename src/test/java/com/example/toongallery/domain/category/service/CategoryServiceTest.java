package com.example.toongallery.domain.category.service;

import com.example.toongallery.domain.category.dto.request.CategoryRequest;
import com.example.toongallery.domain.category.dto.response.CategoryResponse;
import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.category.repository.CategoryRepository;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_성공() {
        //given
        String name = "comedy";
        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", name);


        // 대문자로 변환 후 DB에 있는지 검사
        when(categoryRepository.existsByCategoryName(name.toUpperCase())).thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //when
        CategoryResponse response = categoryService.createCategory(request);

        //then
        assertEquals("COMEDY", response.getCategoryName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_실패_중복이름일때(){
        //given
        String name = "comedy";
        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", name);

        // 대문자로 변환 후 DB에 있는지 검사
        when(categoryRepository.existsByCategoryName(name.toUpperCase())).thenReturn(true);

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> {
            categoryService.createCategory(request);
        });

        assertEquals(ErrorCode.DUPLICATE_CATEGORY_NAME, exception.getErrorCode());
        assertEquals(name, exception.getField()); // 예외 메시지에 들어간 값
        verify(categoryRepository, never()).save(any()); // 저장 시도되지 않아야 함
    }

    @Test
    void updateCategory_성공() {
        // given
        Long categoryId = 1L;
        String originalName = "COMEDY";
        String newName = "action";

        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", newName);
        Category category = Category.of(originalName);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryName(newName.toUpperCase())).thenReturn(false);

        // when
        CategoryResponse response = categoryService.updateCategory(categoryId, request);

        // then
        assertEquals("ACTION", response.getCategoryName());
        assertEquals("ACTION",category.getCategoryName());
    }

    @Test
    void updateCategory_성공_동일한이름이면변경안함() {
        // given
        Long categoryId = 1L;
        String name = "COMEDY";

        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", name);

        Category category = Category.of(name);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        CategoryResponse response = categoryService.updateCategory(categoryId, request);

        // then
        assertEquals("COMEDY", response.getCategoryName());
        verify(categoryRepository, never()).existsByCategoryName(any()); // 중복 검사 안 했는지 확인
    }

    @Test
    void updateCategory_실패_중복된이름() {
        // given
        Long categoryId = 1L;
        String originalName = "COMEDY";
        String newName = "action";

        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", newName);
        Category category = Category.of(originalName);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryName(newName.toUpperCase())).thenReturn(true);

        // when & then
        BaseException exception = assertThrows(BaseException.class, () ->
                categoryService.updateCategory(categoryId, request));

        assertEquals(ErrorCode.DUPLICATE_CATEGORY_NAME, exception.getErrorCode());
        assertEquals(newName, exception.getField());
    }

    @Test
    void updateCategory_실패_존재하지않는카테고리() {
        // given
        Long categoryId = 99L;
        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", "any");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> {
            categoryService.updateCategory(categoryId, request);
        });

        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void deleteCategory_성공() {
        //given
        Long categoryId = 1L;
        Category category = Category.of("COMEDY");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(categoryId);

        // then
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_실패_존재하지_않는_카테고리(){
        //given
        Long categoryId = 99L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> {
            categoryService.deleteCategory(categoryId);
        });

        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void getCategory_성공() {
        // given
        Long categoryId = 1L;
        Category category = Category.of("COMEDY");
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        CategoryResponse response = categoryService.getCategory(categoryId);

        // then
        assertEquals("COMEDY", response.getCategoryName());
    }

    @Test
    void getCategory_실패_존재하지않는카테고리() {
        // given
        Long categoryId = 99L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> {
            categoryService.getCategory(categoryId);
        });

        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void getAllCategories_성공() {
        // given
        List<Category> categories = List.of(
                Category.of("COMEDY"),
                Category.of("ACTION")
        );

        when(categoryRepository.findAll()).thenReturn(categories);

        // when
        List<CategoryResponse> responses = categoryService.getAllCategories();

        // then
        assertEquals(2, responses.size());
        assertEquals("COMEDY", responses.get(0).getCategoryName());
        assertEquals("ACTION", responses.get(1).getCategoryName());
    }

    @Test
    void getAllCategories_성공_빈리스트() {
        // given
        when(categoryRepository.findAll()).thenReturn(List.of());

        // when
        List<CategoryResponse> responses = categoryService.getAllCategories();

        // then
        assertEquals(0, responses.size());
    }

}