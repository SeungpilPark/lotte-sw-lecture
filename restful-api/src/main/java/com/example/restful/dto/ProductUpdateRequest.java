package com.example.restful.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "제품 수정 요청")
public class ProductUpdateRequest {
    
    @Schema(description = "제품명", example = "수정된 노트북")
    private String name;
    
    @Schema(description = "가격", example = "1300000.0")
    @Positive(message = "가격은 양수여야 합니다")
    private Double price;
    
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "제품 설명", example = "수정된 설명")
    private String description;
}

