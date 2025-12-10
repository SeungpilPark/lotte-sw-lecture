package com.example.restful.controller;

import com.example.restful.dto.ApiResponse;
import com.example.restful.dto.PageResponse;
import com.example.restful.dto.ProductCreateRequest;
import com.example.restful.dto.ProductUpdateRequest;
import com.example.restful.entity.Product;
import com.example.restful.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "제품 API", description = "제품 관련 RESTful API 엔드포인트")
public class ProductController {
    
    private final ProductService productService;
    
    @Operation(
            summary = "제품 단일 조회",
            description = "제품 ID를 통해 단일 제품 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"성공\", \"data\": {\"id\": 1, \"name\": \"노트북\", \"price\": 1200000.0}}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "제품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "제품 없음",
                                    value = "{\"success\": false, \"message\": \"제품를 찾을 수 없습니다. ID: 999\", \"data\": null, \"timestamp\": \"2024-01-01T10:00:00\"}"
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProduct(
            @Parameter(description = "제품 ID", required = true, example = "1")
            @PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    @Operation(
            summary = "제품 목록 조회",
            description = "페이징, 정렬, 필터링을 지원하는 제품 목록을 조회합니다. " +
                    "이름, 카테고리, 가격 범위로 필터링할 수 있습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Product>>> getProducts(
            @Parameter(description = "검색어 (제품명 또는 설명에서 검색)", example = "노트북")
            @RequestParam(required = false) String search,
            @Parameter(description = "제품명 검색 (부분 일치)", example = "노트북")
            @RequestParam(required = false) String name,
            @Parameter(description = "카테고리 ID로 필터링", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "최소 가격", example = "10000")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "최대 가격", example = "100000")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 필드 및 방향 (예: name,asc 또는 price,desc)", example = "name,asc")
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        // 정렬 파라미터 파싱 (예: "name,asc" 또는 "price,desc")
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Page<Product> productPage;
        
        // 검색어가 있으면 검색 우선
        if (search != null && !search.isEmpty()) {
            productPage = productService.searchByNameOrDescription(search, pageable);
        } else if (name != null && !name.isEmpty()) {
            // 이름으로 검색 (기존 기능 유지)
            productPage = productService.findByNameContaining(name, pageable);
        } else if (categoryId != null) {
            // 카테고리로 필터링
            productPage = productService.findByCategoryId(categoryId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            // 가격 범위로 필터링 (페이징 지원)
            productPage = productService.findByPriceRange(minPrice, maxPrice, pageable);
        } else {
            // 전체 조회
            productPage = productService.findAll(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(productPage)));
    }
    
    @Operation(
            summary = "카테고리별 제품 조회",
            description = "특정 카테고리에 속한 모든 제품을 조회합니다."
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(
            @Parameter(description = "카테고리 ID", required = true, example = "1")
            @PathVariable Long categoryId) {
        List<Product> products = productService.findByCategoryId(categoryId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @Operation(
            summary = "가격 범위별 제품 조회",
            description = "최소 가격과 최대 가격 사이의 제품을 조회합니다."
    )
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByPriceRange(
            @Parameter(description = "최소 가격", required = true, example = "10000")
            @RequestParam Double minPrice,
            @Parameter(description = "최대 가격", required = true, example = "100000")
            @RequestParam Double maxPrice) {
        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @Operation(
            summary = "제품 생성",
            description = "새로운 제품을 생성합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"제품이 생성되었습니다\", \"data\": {\"id\": 11, \"name\": \"새 제품\", \"price\": 50000.0}}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "필수 필드 누락",
                                            value = "{\"success\": false, \"message\": \"입력값 검증 실패\", \"data\": {\"name\": \"제품명은 필수입니다\", \"price\": \"가격은 필수입니다\"}, \"timestamp\": \"2024-01-01T10:00:00\"}"
                                    ),
                                    @ExampleObject(
                                            name = "잘못된 값",
                                            value = "{\"success\": false, \"message\": \"입력값 검증 실패\", \"data\": {\"name\": \"제품명은 필수입니다\", \"price\": \"가격은 양수여야 합니다\"}, \"timestamp\": \"2024-01-01T10:00:00\"}"
                                    )
                            }
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "제품 생성 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductCreateRequest.class),
                            examples = @ExampleObject(value = "{\"name\": \"새 제품\", \"price\": 50000.0, \"categoryId\": 1, \"description\": \"테스트 제품\"}")
                    )
            )
            @Valid @RequestBody ProductCreateRequest request) {
        Product product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("제품이 생성되었습니다", product));
    }
    
    @Operation(
            summary = "제품 수정 (전체 수정)",
            description = "제품의 모든 정보를 수정합니다. (PUT - 멱등성 보장)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"제품이 수정되었습니다\", \"data\": {\"id\": 1, \"name\": \"수정된 제품명\", \"price\": 60000.0}}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "제품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"제품를 찾을 수 없습니다. ID: 999\", \"data\": null, \"timestamp\": \"2024-01-01T10:00:00\"}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"입력값 검증 실패\", \"data\": {\"name\": \"제품명은 필수입니다\", \"price\": \"가격은 양수여야 합니다\"}, \"timestamp\": \"2024-01-01T10:00:00\"}")
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @Parameter(description = "제품 ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "제품 수정 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductUpdateRequest.class))
            )
            @Valid @RequestBody ProductUpdateRequest request) {
        Product product = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("제품이 수정되었습니다", product));
    }
    
    @Operation(
            summary = "제품 부분 수정",
            description = "제품의 일부 정보만 수정합니다. (PATCH)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "제품을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> patchProduct(
            @Parameter(description = "제품 ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "제품 부분 수정 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductUpdateRequest.class),
                            examples = @ExampleObject(value = "{\"price\": 55000.0}")
                    )
            )
            @RequestBody ProductUpdateRequest request) {
        Product product = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("제품이 수정되었습니다", product));
    }
    
    @Operation(
            summary = "제품 삭제",
            description = "제품을 삭제합니다. (멱등성 보장)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"제품이 삭제되었습니다\", \"data\": null, \"timestamp\": \"2024-01-01T10:00:00\"}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "제품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"제품를 찾을 수 없습니다. ID: 999\", \"data\": null, \"timestamp\": \"2024-01-01T10:00:00\"}")
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "제품 ID", required = true, example = "1")
            @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success("제품이 삭제되었습니다", null));
    }
}

