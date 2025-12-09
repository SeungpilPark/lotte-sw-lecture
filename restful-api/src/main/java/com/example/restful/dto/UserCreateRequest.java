package com.example.restful.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 생성 요청")
public class UserCreateRequest {
    
    @Schema(description = "사용자명", example = "user1", required = true)
    @NotBlank(message = "사용자명은 필수입니다")
    private String username;
    
    @Schema(description = "이메일", example = "user1@example.com", required = true)
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @Schema(description = "이름", example = "홍길동", required = true)
    @NotBlank(message = "이름은 필수입니다")
    private String name;
}

