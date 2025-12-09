# RESTful API 구현 실습 프로젝트

3일차 6교시: RESTful API 구현 실습을 위한 Spring Boot 프로젝트입니다.

## 강의 내용 (3일차 6교시)

### 학습 목표
- Spring Boot를 활용한 RESTful API 구현
- REST 원칙 적용 및 API 설계 실습
- CRUD 작업 구현
- 페이징, 정렬, 필터링 구현
- 에러 처리 및 HTTP 상태 코드 활용
- Postman을 활용한 API 테스트

### 실습 내용

1. **RESTful API 구현 (25분)**
   - Spring Boot REST API 구현
   - CRUD 작업 구현
   - 페이징, 정렬, 필터링
   - 에러 처리

2. **API 설계 적용 (15분)**
   - REST 원칙 적용
   - URI 설계
   - HTTP 메서드 선택
   - 상태 코드 활용

3. **API 테스트 (10분)**
   - Postman을 활용한 API 테스트
   - 에러 케이스 검증

## 프로젝트 구조

```
restful-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/restful/
│   │   │   ├── entity/               # JPA 엔티티 (Product, Category, User, Order)
│   │   │   ├── repository/           # Repository 인터페이스
│   │   │   ├── service/              # 서비스 레이어
│   │   │   ├── controller/           # REST API 컨트롤러
│   │   │   ├── dto/                  # 요청/응답 DTO
│   │   │   ├── exception/            # 예외 처리
│   │   │   └── RestfulApiApplication.java
│   │   └── resources/
│   │       ├── application.yml       # 애플리케이션 설정
│   │       └── data.sql              # 초기 데이터
│   └── test/
└── build.gradle
```

## 실행 방법

### 1. 프로젝트 빌드 및 실행

```bash
cd restful-api
./gradlew build
./gradlew bootRun
```

애플리케이션이 정상적으로 실행되면 기본 포트는 **8080**입니다.

### 2. H2 Console 접근

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비워두기)

## RESTful API 사용 가이드

### REST 원칙 적용 사항

1. **리소스 기반 URI 설계**
   - 명사 사용: `/api/products`, `/api/users`
   - 계층 구조: `/api/products/{id}`, `/api/orders/user/{userId}`

2. **HTTP 메서드 활용**
   - GET: 리소스 조회
   - POST: 리소스 생성
   - PUT: 리소스 전체 수정
   - PATCH: 리소스 부분 수정
   - DELETE: 리소스 삭제

3. **HTTP 상태 코드**
   - 200 OK: 성공
   - 201 Created: 생성 성공
   - 204 No Content: 삭제 성공
   - 400 Bad Request: 잘못된 요청
   - 404 Not Found: 리소스 없음
   - 500 Internal Server Error: 서버 오류

4. **일관된 응답 형식**
   - 모든 응답은 `ApiResponse<T>` 형식으로 통일
   - 페이징 응답은 `PageResponse<T>` 형식 사용


## 실습 시나리오

### 실습 1: REST 원칙 적용 확인

1. **리소스 기반 URI 확인**
   - 제품 조회: `GET /api/products/1`
   - 사용자 조회: `GET /api/users/1`
   - 주문 조회: `GET /api/orders/1`

2. **HTTP 메서드 활용 확인**
   - GET: 조회
   - POST: 생성 (201 Created 확인)
   - PUT: 전체 수정
   - PATCH: 부분 수정
   - DELETE: 삭제 (204 No Content 확인)

3. **상태 코드 확인**
   - 성공: 200 OK
   - 생성: 201 Created
   - 삭제: 204 No Content
   - 에러: 400, 404, 500

### 실습 2: 페이징, 정렬, 필터링

1. **페이징 테스트**
   ```http
   GET /api/products?page=0&size=5
   GET /api/products?page=1&size=5
   ```

2. **정렬 테스트**
   ```http
   GET /api/products?sort=name,asc
   GET /api/products?sort=price,desc
   GET /api/products?sort=id,desc
   ```

3. **필터링 테스트**
   ```http
   GET /api/products?name=노트북
   GET /api/products?categoryId=1
   GET /api/products/price-range?minPrice=10000&maxPrice=100000
   ```

4. **복합 조건 테스트**
   ```http
   GET /api/products?categoryId=1&page=0&size=5&sort=price,desc
   ```

### 실습 3: CRUD 작업

1. **제품 생성**
   ```http
   POST /api/products
   {
     "name": "새 제품",
     "price": 50000.0,
     "categoryId": 1,
     "description": "테스트 제품"
   }
   ```

2. **생성된 제품 조회**
   ```http
   GET /api/products/{생성된_ID}
   ```

3. **제품 수정 (PUT)**
   ```http
   PUT /api/products/{id}
   {
     "name": "수정된 제품명",
     "price": 60000.0,
     "categoryId": 1,
     "description": "수정된 설명"
   }
   ```

4. **제품 부분 수정 (PATCH)**
   ```http
   PATCH /api/products/{id}
   {
     "price": 55000.0
   }
   ```

5. **제품 삭제**
   ```http
   DELETE /api/products/{id}
   ```

6. **삭제 확인 (404 에러 확인)**
   ```http
   GET /api/products/{삭제된_ID}
   ```

### 실습 4: 에러 처리 검증

1. **존재하지 않는 리소스 조회**
   ```http
   GET /api/products/999
   ```
   - 404 Not Found 확인

2. **잘못된 입력값으로 생성 시도**
   ```http
   POST /api/products
   {
     "name": "",
     "price": -1000
   }
   ```
   - 400 Bad Request 확인
   - 검증 에러 메시지 확인

3. **중복 데이터 생성 시도**
   ```http
   POST /api/users
   {
     "username": "user1",
     "email": "user1@example.com",
     "name": "중복 사용자"
   }
   ```
   - 400 Bad Request 확인


## RESTful API 설계 체크리스트

### URI 설계
- [ ] 명사 사용 (동사 사용하지 않음)
- [ ] 계층 구조 명확
- [ ] 소문자 사용
- [ ] 하이픈(-) 사용 (언더스코어 사용하지 않음)
- [ ] 파일 확장자 포함하지 않음

### HTTP 메서드
- [ ] GET: 조회 (멱등성, 안전성)
- [ ] POST: 생성
- [ ] PUT: 전체 수정 (멱등성)
- [ ] PATCH: 부분 수정
- [ ] DELETE: 삭제 (멱등성)

### 상태 코드
- [ ] 200 OK: 성공
- [ ] 201 Created: 생성 성공
- [ ] 204 No Content: 삭제 성공
- [ ] 400 Bad Request: 잘못된 요청
- [ ] 404 Not Found: 리소스 없음
- [ ] 500 Internal Server Error: 서버 오류

### 응답 형식
- [ ] 일관된 응답 구조
- [ ] 에러 메시지 명확
- [ ] 페이징 정보 포함

### 기능
- [ ] 페이징 지원
- [ ] 정렬 지원
- [ ] 필터링 지원
- [ ] 입력값 검증
- [ ] 에러 처리

## 참고사항

- H2 Database는 인메모리 데이터베이스로, 애플리케이션 재시작 시 데이터가 초기화됩니다.
- 모든 API는 `ApiResponse<T>` 형식으로 응답합니다.
- 페이징 응답은 `PageResponse<T>` 형식으로 응답합니다.
- 입력값 검증은 Jakarta Validation을 사용합니다.
- 에러 처리는 `GlobalExceptionHandler`에서 통합 관리합니다.



## API 엔드포인트

### 제품 (Products) API

#### 1. 제품 단일 조회
```http
GET /api/products/{id}
```

**응답 예시:**
```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 1,
    "name": "노트북",
    "price": 1200000.0,
    "description": "고성능 노트북",
    "category": {
      "id": 1,
      "name": "전자제품",
      "description": "전자기기 및 액세서리"
    }
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 2. 제품 목록 조회 (페이징, 정렬, 필터링)
```http
GET /api/products?page=0&size=10&sort=name,asc
GET /api/products?name=노트북&page=0&size=10
GET /api/products?categoryId=1&page=0&size=10&sort=price,desc
```

**쿼리 파라미터:**
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 필드 및 방향 (예: `name,asc`, `price,desc`)
- `name`: 제품명 검색 (부분 일치)
- `categoryId`: 카테고리 ID로 필터링
- `minPrice`, `maxPrice`: 가격 범위 필터링

**응답 예시:**
```json
{
  "success": true,
  "message": "성공",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "노트북",
        "price": 1200000.0,
        "description": "고성능 노트북"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 10,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 3. 카테고리별 제품 조회
```http
GET /api/products/category/{categoryId}
```

#### 4. 가격 범위별 제품 조회
```http
GET /api/products/price-range?minPrice=10000&maxPrice=100000
```

#### 5. 제품 생성
```http
POST /api/products
Content-Type: application/json

{
  "name": "새 제품",
  "price": 50000.0,
  "categoryId": 1,
  "description": "제품 설명"
}
```

**응답:** 201 Created

#### 6. 제품 수정 (전체 수정)
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "수정된 제품명",
  "price": 60000.0,
  "categoryId": 1,
  "description": "수정된 설명"
}
```

#### 7. 제품 부분 수정
```http
PATCH /api/products/{id}
Content-Type: application/json

{
  "price": 55000.0
}
```

#### 8. 제품 삭제
```http
DELETE /api/products/{id}
```

**응답:** 204 No Content

### 사용자 (Users) API

#### 1. 사용자 단일 조회
```http
GET /api/users/{id}
```

#### 2. 사용자명으로 조회
```http
GET /api/users/username/{username}
```

#### 3. 사용자 목록 조회 (페이징, 정렬)
```http
GET /api/users?page=0&size=10&sort=name,asc
```

#### 4. 사용자 생성
```http
POST /api/users
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "name": "신규 사용자"
}
```

#### 5. 사용자 수정
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "updateduser",
  "email": "updated@example.com",
  "name": "수정된 사용자"
}
```

#### 6. 사용자 삭제
```http
DELETE /api/users/{id}
```

### 주문 (Orders) API

#### 1. 주문 단일 조회
```http
GET /api/orders/{id}
```

#### 2. 주문번호로 조회
```http
GET /api/orders/number/{orderNumber}
```

#### 3. 주문 목록 조회 (페이징, 정렬)
```http
GET /api/orders?page=0&size=10&sort=orderDate,desc
```

#### 4. 사용자별 주문 목록 조회
```http
GET /api/orders/user/{userId}?page=0&size=10&sort=orderDate,desc
```

### 카테고리 (Categories) API

#### 1. 카테고리 단일 조회
```http
GET /api/categories/{id}
```

#### 2. 카테고리 목록 조회
```http
GET /api/categories
```

#### 3. 카테고리 생성
```http
POST /api/categories
Content-Type: application/json

{
  "name": "새 카테고리",
  "description": "카테고리 설명"
}
```

#### 4. 카테고리 수정
```http
PUT /api/categories/{id}
Content-Type: application/json

{
  "name": "수정된 카테고리",
  "description": "수정된 설명"
}
```

#### 5. 카테고리 삭제
```http
DELETE /api/categories/{id}
```

## 에러 처리

### 에러 응답 형식

모든 에러는 다음 형식으로 반환됩니다:

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

### 주요 에러 케이스

#### 1. 리소스 없음 (404 Not Found)
```http
GET /api/products/999
```

**응답:**
```json
{
  "success": false,
  "message": "제품를 찾을 수 없습니다. ID: 999",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 2. 잘못된 요청 (400 Bad Request)
```http
POST /api/products
Content-Type: application/json

{
  "name": "",
  "price": -1000
}
```

**응답:**
```json
{
  "success": false,
  "message": "입력값 검증 실패",
  "data": {
    "name": "제품명은 필수입니다",
    "price": "가격은 양수여야 합니다"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 3. 중복 데이터 (400 Bad Request)
```http
POST /api/users
Content-Type: application/json

{
  "username": "user1",
  "email": "user1@example.com",
  "name": "중복 사용자"
}
```

**응답:**
```json
{
  "success": false,
  "message": "이미 존재하는 사용자명입니다: user1",
  "data": null,
  "timestamp": "2024-01-01T10:00:00"
}
```

