package com.example.restful.controller;

import com.example.restful.dto.ApiResponse;
import com.example.restful.dto.PageResponse;
import com.example.restful.dto.UserCreateRequest;
import com.example.restful.entity.User;
import com.example.restful.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 관련 RESTful API 엔드포인트")
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "사용자 단일 조회", description = "사용자 ID를 통해 단일 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @Operation(summary = "사용자명으로 조회", description = "사용자명을 통해 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(
            @Parameter(description = "사용자명", required = true, example = "user1")
            @PathVariable String username) {
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @Operation(summary = "사용자 목록 조회", description = "페이징, 정렬을 지원하는 사용자 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<User>>> getUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 필드 및 방향", example = "id,asc")
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<User> userPage = userService.findAll(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(userPage)));
    }
    
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패 또는 중복)")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자 생성 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserCreateRequest.class),
                            examples = @ExampleObject(value = "{\"username\": \"newuser\", \"email\": \"newuser@example.com\", \"name\": \"신규 사용자\"}")
                    )
            )
            @Valid @RequestBody UserCreateRequest request) {
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("사용자가 생성되었습니다", user));
    }
    
    @Operation(summary = "사용자 수정", description = "사용자 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자 수정 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateRequest.class))
            )
            @Valid @RequestBody UserCreateRequest request) {
        User user = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("사용자가 수정되었습니다", user));
    }
    
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success("사용자가 삭제되었습니다", null));
    }
}

