# Swagger 문서화 실습

3일차 8교시: Swagger를 활용한 API 문서화 실습입니다.

## 강의 내용 (3일차 8교시)

### 학습 목표
- Swagger를 활용한 완전한 API 문서 작성
- 문서 기반 API 개발 워크플로우 이해
- Swagger UI에서 API 테스트
- 문서와 실제 구현 일치 확인
- 문서 배포 및 공유 방법

### 실습 내용

1. **Swagger 문서 작성 (25분)**
   - 구현한 RESTful API에 Swagger 적용
   - 모든 엔드포인트 문서화
   - 요청/응답 모델 정의
   - 예제 추가
   - 에러 응답 문서화

2. **문서 검증 및 테스트 (15분)**
   - Swagger UI에서 API 테스트
   - 문서와 실제 구현 일치 확인
   - 문서 품질 검토

3. **문서 배포 및 공유 (10분)**
   - 문서 정적 파일 생성
   - 문서 서버 배포
   - 팀 내 공유 방법

## Swagger UI 접근

애플리케이션 실행 후 다음 URL로 접근:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## 실습 시나리오

### 실습 1: Swagger UI 탐색

1. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

2. **Swagger UI 접근**
   - 브라우저에서 `http://localhost:8080/swagger-ui.html` 접근
   - API 그룹 확인 (제품 API, 사용자 API, 주문 API, 카테고리 API)

3. **API 엔드포인트 탐색**
   - 각 API 그룹을 펼쳐서 엔드포인트 목록 확인
   - 각 엔드포인트의 설명, 파라미터, 응답 형식 확인

4. **모델 스키마 확인**
   - "Schemas" 섹션에서 요청/응답 모델 확인
   - `ProductCreateRequest`, `UserCreateRequest`, `ApiResponse` 등 확인

### 실습 2: Swagger UI에서 API 테스트

1. **제품 조회 테스트**
   - "제품 API" 그룹의 `GET /api/products/{id}` 선택
   - "Try it out" 버튼 클릭
   - `id` 파라미터에 `1` 입력
   - "Execute" 버튼 클릭
   - 응답 확인 (200 OK, 응답 본문 확인)

2. **제품 목록 조회 테스트**
   - `GET /api/products` 선택
   - "Try it out" 클릭
   - 쿼리 파라미터 설정:
     - `page`: 0
     - `size`: 5
     - `sort`: name,asc
   - "Execute" 클릭
   - 페이징 응답 확인

3. **제품 생성 테스트**
   - `POST /api/products` 선택
   - "Try it out" 클릭
   - Request body에 예제 데이터 입력:
     ```json
     {
       "name": "Swagger 테스트 제품",
       "price": 75000.0,
       "categoryId": 1,
       "description": "Swagger UI에서 생성한 제품"
     }
     ```
   - "Execute" 클릭
   - 201 Created 응답 확인
   - 생성된 제품 정보 확인

4. **제품 수정 테스트 (PUT)**
   - `PUT /api/products/{id}` 선택
   - "Try it out" 클릭
   - `id`에 생성한 제품 ID 입력
   - Request body 입력:
     ```json
     {
       "name": "수정된 제품명",
       "price": 80000.0,
       "categoryId": 1,
       "description": "수정된 설명"
     }
     ```
   - "Execute" 클릭
   - 200 OK 응답 확인

5. **제품 부분 수정 테스트 (PATCH)**
   - `PATCH /api/products/{id}` 선택
   - "Try it out" 클릭
   - `id` 입력
   - Request body에 일부 필드만 입력:
     ```json
     {
       "price": 85000.0
     }
     ```
   - "Execute" 클릭
   - 200 OK 응답 확인

6. **제품 삭제 테스트**
   - `DELETE /api/products/{id}` 선택
   - "Try it out" 클릭
   - `id` 입력
   - "Execute" 클릭
   - 204 No Content 응답 확인

7. **에러 케이스 테스트**
   - 존재하지 않는 ID로 조회: `GET /api/products/999`
   - 404 Not Found 응답 확인
   - 잘못된 데이터로 생성 시도:
     ```json
     {
       "name": "",
       "price": -1000
     }
     ```
   - 400 Bad Request 응답 확인
   - 검증 에러 메시지 확인

### 실습 3: 문서 품질 검토

1. **문서 완성도 확인**
   - 모든 엔드포인트에 설명이 있는지 확인
   - 모든 파라미터에 설명과 예제가 있는지 확인
   - 모든 응답 코드에 설명이 있는지 확인
   - 요청/응답 모델에 필드 설명이 있는지 확인

2. **예제 데이터 확인**
   - 각 엔드포인트의 예제가 적절한지 확인
   - Request body 예제가 실제 사용 가능한 형식인지 확인

3. **에러 응답 문서화 확인**
   - 400, 404, 500 등 에러 응답이 문서화되어 있는지 확인
   - 에러 응답 형식이 명확한지 확인

### 실습 4: OpenAPI 스펙 확인

1. **OpenAPI JSON 확인**
   - 브라우저에서 `http://localhost:8080/v3/api-docs` 접근
   - JSON 형식의 OpenAPI 스펙 확인
   - 전체 API 구조 확인

2. **OpenAPI YAML 확인**
   - 브라우저에서 `http://localhost:8080/v3/api-docs.yaml` 접근
   - YAML 형식의 OpenAPI 스펙 확인

3. **스펙 구조 분석**
   - `info`: API 정보
   - `servers`: 서버 목록
   - `paths`: API 엔드포인트 정의
   - `components`: 공통 스키마 정의

### 실습 5: 문서 정적 파일 생성 (선택사항)

1. **OpenAPI 스펙 다운로드**
   - `http://localhost:8080/v3/api-docs` 접근
   - JSON 파일로 저장 (예: `openapi.json`)

2. **Swagger Editor에서 확인**
   - https://editor.swagger.io/ 접근
   - 다운로드한 JSON 파일 업로드
   - 문서 확인 및 편집

3. **문서 서버 배포 (선택사항)**
   - 정적 파일 서버에 배포
   - 또는 Swagger UI를 별도 서버에 배포

## Swagger 어노테이션 활용

### Controller 어노테이션

#### @Tag
- API 그룹을 태그로 분류
- 예: `@Tag(name = "제품 API", description = "제품 관련 RESTful API 엔드포인트")`

#### @Operation
- 각 엔드포인트의 설명 추가
- 예:
  ```java
  @Operation(
      summary = "제품 단일 조회",
      description = "제품 ID를 통해 단일 제품 정보를 조회합니다."
  )
  ```

#### @Parameter
- 파라미터 설명 및 예제 추가
- 예:
  ```java
  @Parameter(description = "제품 ID", required = true, example = "1")
  @PathVariable Long id
  ```

#### @ApiResponses / @ApiResponse
- 응답 코드별 설명 추가
- 예:
  ```java
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "제품을 찾을 수 없음")
  })
  ```

#### @RequestBody
- 요청 본문 설명 및 예제 추가
- 예:
  ```java
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "제품 생성 요청",
      required = true,
      content = @Content(
          schema = @Schema(implementation = ProductCreateRequest.class),
          examples = @ExampleObject(value = "{\"name\": \"새 제품\", \"price\": 50000.0}")
      )
  )
  ```

### DTO 어노테이션

#### @Schema
- 모델 및 필드 설명 추가
- 예:
  ```java
  @Schema(description = "제품 생성 요청")
  public class ProductCreateRequest {
      @Schema(description = "제품명", example = "노트북", required = true)
      private String name;
  }
  ```


## Swagger 설정

### OpenApiConfig 클래스

`OpenApiConfig.java`에서 다음을 설정할 수 있습니다:

- API 정보 (제목, 버전, 설명, 연락처, 라이선스)
- 서버 목록 (로컬, 개발, 프로덕션)
- 보안 스키마 (인증/인가)
- 공통 응답/요청 스키마

### application.yml 설정 (선택사항)

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
```

## Swagger UI 기능

### 1. API 탐색
- 태그별로 API 그룹화
- 각 엔드포인트의 상세 정보 확인
- 요청/응답 스키마 확인

### 2. API 테스트
- "Try it out" 버튼으로 직접 API 호출
- 파라미터 입력 및 요청 본문 작성
- 응답 확인 및 검증

### 3. 스키마 확인
- "Schemas" 섹션에서 모델 확인
- 필드 타입, 설명, 예제 확인
- 모델 간 관계 확인

### 4. 인증 설정 (선택사항)
- "Authorize" 버튼으로 인증 토큰 설정
- Bearer Token, API Key 등 설정 가능

## 문서화 모범 사례

### 1. 명확한 설명
- 각 엔드포인트의 목적과 사용 사례를 명확히 설명
- 기술 용어보다는 비즈니스 용어 사용

### 2. 적절한 예제
- 실제 사용 가능한 예제 데이터 제공
- 다양한 시나리오의 예제 제공

### 3. 에러 처리 문서화
- 모든 가능한 에러 응답 문서화
- 에러 메시지 형식 명시

### 4. 버전 관리
- API 버전 정보 명시
- 변경 이력 관리

### 5. 일관성 유지
- 네이밍 컨벤션 일관성 유지
- 응답 형식 통일
- 에러 처리 방식 통일

## 참고사항

- Swagger UI는 개발 환경에서만 활성화하는 것을 권장합니다.
- 프로덕션 환경에서는 Swagger UI를 비활성화하거나 인증을 추가하세요.
- OpenAPI 스펙은 API 버전 관리와 함께 관리하는 것이 좋습니다.
- 문서는 코드와 함께 유지보수되어야 합니다.

## 추가 학습 자료

- [SpringDoc OpenAPI 공식 문서](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger Editor](https://editor.swagger.io/)
- [OpenAPI Generator](https://openapi-generator.tech/)

