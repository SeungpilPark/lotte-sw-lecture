# 도전과제: Swagger 에러 응답 문서화 추가

## 문제 상황

현재 Swagger 문서에는 성공 응답(200, 201, 204)은 잘 문서화되어 있지만, 에러 응답(400, 404, 500)에 대한 상세한 문서화가 부족합니다.

예를 들어, `ProductController`의 `getProduct` 메서드를 보면:
- 200 OK 응답은 예제와 함께 잘 문서화되어 있음
- 404 Not Found 응답은 간단한 설명만 있고, 실제 에러 응답 형식이 명시되지 않음
- 400 Bad Request, 500 Internal Server Error 등은 아예 문서화되지 않은 경우가 많음

클라이언트 개발자가 API를 사용할 때 어떤 에러가 발생할 수 있고, 그 에러 응답이 어떤 형식인지 미리 알 수 있어야 합니다.

## 도전과제

다음 API 엔드포인트들에 에러 응답 문서화를 추가하세요:

1. **ProductController의 주요 메서드들**
   - `GET /api/products/{id}`: 404 에러 응답 문서화
   - `POST /api/products`: 400 에러 응답 문서화 (검증 실패)
   - `PUT /api/products/{id}`: 400, 404 에러 응답 문서화
   - `DELETE /api/products/{id}`: 404 에러 응답 문서화

2. **에러 응답 형식 명시**
   - `ApiResponse` 형식의 에러 응답 예제 추가
   - 검증 실패 시 필드별 에러 메시지 예제 추가

## 구현 가이드

### 1단계: @ApiResponse 어노테이션 활용

Swagger의 `@ApiResponse` 어노테이션을 사용하여 에러 응답을 문서화하세요.

**기본 형식:**
```java
@ApiResponse(
    responseCode = "404",
    description = "제품을 찾을 수 없음",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ApiResponse.class),
        examples = @ExampleObject(value = "{\"success\": false, \"message\": \"제품를 찾을 수 없습니다. ID: 999\", \"data\": null}")
    )
)
```

### 2단계: 검증 실패 에러 응답 문서화

`@ApiResponse`의 `content`에 여러 예제를 추가하여 다양한 에러 케이스를 문서화하세요.

### 3단계: 일관된 에러 응답 형식

모든 에러 응답이 `ApiResponse` 형식을 따르도록 문서화하세요.

## 요구사항

1. **모든 주요 엔드포인트**에 에러 응답 문서화 추가
2. **에러 응답 예제**를 실제 응답 형식과 일치하도록 작성
3. **검증 실패 에러**는 필드별 에러 메시지 예제 포함
4. **일관된 형식** 유지

## 테스트 시나리오

Swagger UI에서 다음을 확인하세요:

1. **404 에러 문서화 확인**
   - `GET /api/products/{id}` 엔드포인트에서 404 응답 예제 확인
   - 실제로 존재하지 않는 ID로 요청하여 문서화된 형식과 일치하는지 확인

2. **400 에러 문서화 확인**
   - `POST /api/products` 엔드포인트에서 400 응답 예제 확인
   - 잘못된 데이터로 요청하여 문서화된 형식과 일치하는지 확인

3. **에러 응답 예제 확인**
   - Swagger UI에서 각 에러 응답의 예제가 표시되는지 확인
   - 예제가 실제 응답 형식과 일치하는지 확인

## 평가 기준

- [ ] 주요 엔드포인트에 에러 응답이 문서화되었는가?
- [ ] 404 에러 응답이 올바르게 문서화되었는가?
- [ ] 400 에러 응답(검증 실패)이 올바르게 문서화되었는가?
- [ ] 에러 응답 예제가 실제 응답 형식과 일치하는가?
- [ ] Swagger UI에서 에러 응답이 명확하게 표시되는가?

---

# 도전과제 풀이

## 풀이 1: ProductController - GET 메서드 에러 응답 문서화

`ProductController.java`의 `getProduct` 메서드를 다음과 같이 수정합니다:

```java
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
```

## 풀이 2: ProductController - POST 메서드 에러 응답 문서화

`createProduct` 메서드를 다음과 같이 수정합니다:

```java
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
```

## 풀이 3: ProductController - PUT 메서드 에러 응답 문서화

`updateProduct` 메서드를 다음과 같이 수정합니다:

```java
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
```

## 풀이 4: ProductController - DELETE 메서드 에러 응답 문서화

`deleteProduct` 메서드를 다음과 같이 수정합니다:

```java
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
```

## 풀이 5: UserController 에러 응답 문서화 (참고)

`UserController`의 주요 메서드들도 동일한 방식으로 문서화할 수 있습니다:

```java
@Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class)
                )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (입력값 검증 실패 또는 중복)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = com.example.restful.dto.ApiResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "검증 실패",
                                        value = "{\"success\": false, \"message\": \"입력값 검증 실패\", \"data\": {\"username\": \"사용자명은 필수입니다\", \"email\": \"이메일 형식이 올바르지 않습니다\"}, \"timestamp\": \"2024-01-01T10:00:00\"}"
                                ),
                                @ExampleObject(
                                        name = "중복 데이터",
                                        value = "{\"success\": false, \"message\": \"이미 존재하는 사용자명입니다: user1\", \"data\": null, \"timestamp\": \"2024-01-01T10:00:00\"}"
                                )
                        }
                )
        )
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
```

## 풀이 6: 공통 에러 응답 스키마 정의 (고급, 선택사항)

더 체계적으로 관리하려면 `OpenApiConfig`에서 공통 에러 응답 스키마를 정의할 수 있습니다:

```java
package com.example.restful.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RESTful API 문서")
                        .version("1.0.0")
                        .description("소프트웨어 아키텍처 강의 - RESTful API 실습 프로젝트")
                        .contact(new Contact()
                                .name("강의 지원")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.example.com")
                                .description("프로덕션 서버")
                ))
                .components(new io.swagger.v3.oas.models.Components()
                        .addResponses("NotFound", new ApiResponse()
                                .description("리소스를 찾을 수 없음")
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new Schema<>()
                                                        .$ref("#/components/schemas/ApiResponse")))))
                        .addResponses("BadRequest", new ApiResponse()
                                .description("잘못된 요청")
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new Schema<>()
                                                        .$ref("#/components/schemas/ApiResponse")))))
                        .addResponses("InternalServerError", new ApiResponse()
                                .description("서버 오류")
                                .content(new Content()
                                        .addMediaType("application/json",
                                                new MediaType().schema(new Schema<>()
                                                        .$ref("#/components/schemas/ApiResponse"))))));
    }
}
```

그리고 Controller에서 공통 응답을 참조할 수 있습니다:

```java
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "제품을 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(ref = "#/components/responses/NotFound")
                )
        )
})
```

## 테스트 방법

### 1. Swagger UI에서 확인

1. 애플리케이션 실행 후 `http://localhost:8080/swagger-ui.html` 접근
2. 각 API 엔드포인트를 펼쳐서 에러 응답이 표시되는지 확인
3. 에러 응답의 예제가 실제 응답 형식과 일치하는지 확인

### 2. 실제 API 테스트

1. **404 에러 테스트**
   ```bash
   curl http://localhost:8080/api/products/999
   ```
   - Swagger 문서에 명시된 형식과 일치하는지 확인

2. **400 에러 테스트**
   ```bash
   curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{"name": "", "price": -1000}'
   ```
   - Swagger 문서에 명시된 검증 에러 형식과 일치하는지 확인

### 3. Postman 테스트

Postman에서도 동일한 요청을 보내서 실제 응답이 문서화된 형식과 일치하는지 확인하세요.

## 핵심 학습 포인트

1. **@ApiResponse 어노테이션**: 다양한 HTTP 상태 코드에 대한 응답 문서화
2. **@ExampleObject**: 실제 응답 예제를 문서에 포함
3. **에러 응답 일관성**: 모든 에러 응답이 `ApiResponse` 형식을 따르도록 문서화
4. **클라이언트 개발자 경험**: API 문서를 통해 에러 케이스를 미리 파악할 수 있도록 개선
5. **문서와 실제 구현 일치**: 문서화된 내용이 실제 응답과 일치하도록 주의

## 추가 개선 사항

1. **공통 에러 응답 컴포넌트**: OpenAPI Components를 활용한 재사용 가능한 에러 응답 정의
2. **에러 코드 체계**: 표준화된 에러 코드 정의 및 문서화
3. **에러 응답 상세화**: 에러 발생 시 해결 방법 가이드 포함

