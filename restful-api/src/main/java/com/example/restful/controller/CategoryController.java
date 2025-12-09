package com.example.restful.controller;

import com.example.restful.dto.ApiResponse;
import com.example.restful.entity.Category;
import com.example.restful.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리 API", description = "카테고리 관련 RESTful API 엔드포인트")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "카테고리 단일 조회", description = "카테고리 ID를 통해 단일 카테고리 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategory(
            @Parameter(description = "카테고리 ID", required = true, example = "1")
            @PathVariable Long id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "카테고리 생성 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Category.class))
            )
            @RequestBody Category category) {
        Category created = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("카테고리가 생성되었습니다", created));
    }
    
    @Operation(summary = "카테고리 수정", description = "카테고리 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @Parameter(description = "카테고리 ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "카테고리 수정 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Category.class))
            )
            @RequestBody Category category) {
        Category updated = categoryService.update(id, category);
        return ResponseEntity.ok(ApiResponse.success("카테고리가 수정되었습니다", updated));
    }
    
    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "카테고리 ID", required = true, example = "1")
            @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success("카테고리가 삭제되었습니다", null));
    }
}

