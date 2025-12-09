package com.example.restful.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "제품 생성 요청")
public class ProductCreateRequest {
    
    @Schema(description = "제품명", example = "노트북", required = true)
    @NotBlank(message = "제품명은 필수입니다")
    private String name;
    
    @Schema(description = "가격", example = "1200000.0", required = true)
    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 양수여야 합니다")
    private Double price;
    
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "제품 설명", example = "고성능 노트북")
    private String description;
}

