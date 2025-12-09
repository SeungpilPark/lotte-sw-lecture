package com.example.restful.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이징 응답 형식")
public class PageResponse<T> {
    @Schema(description = "데이터 목록")
    private List<T> content;
    
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int page;
    
    @Schema(description = "페이지 크기", example = "10")
    private int size;
    
    @Schema(description = "전체 요소 개수", example = "100")
    private long totalElements;
    
    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;
    
    @Schema(description = "첫 페이지 여부", example = "true")
    private boolean first;
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}

