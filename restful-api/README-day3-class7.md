# GraphQL과 gRPC 구현 실습

3일차 7교시: GraphQL과 gRPC 구현 실습입니다.

## 강의 내용 (3일차 7교시)

### 학습 목표
- GraphQL과 gRPC를 실제로 구현
- 각 기술의 구현 방법과 특징 비교
- 동일한 기능을 REST, GraphQL, gRPC로 구현하여 비교

### 실습 내용

1. **GraphQL 구현 (25분)**
   - Spring Boot GraphQL 설정
   - Schema 정의
   - Resolver 구현
   - Query, Mutation 구현

2. **gRPC 구현 (20분)**
   - Protocol Buffer 정의
   - gRPC 서버 구현
   - 클라이언트 구현

3. **통신 패턴 비교 (5분)**
   - 동일한 기능을 REST, GraphQL, gRPC로 구현하여 비교
   - 각 기술의 장단점 분석

## Postman을 활용한 실습

### Postman 설정

1. **새 요청 생성**
   - Postman에서 새 요청 생성
   - 요청 타입을 GraphQL 또는 gRPC로 선택

2. **환경 변수 설정 (선택사항)**
   - `baseUrl`: `http://localhost:8080`
   - `grpcServer`: `localhost:9090`

## 실습 시나리오

### 실습 1: GraphQL Query 구현 (Postman)

1. **Postman에서 GraphQL 요청 생성**
   - 요청 타입: **GraphQL**
   - URL: `http://localhost:8080/graphql`
   - Method: **POST**

2. **제품 조회 쿼리 실행**
   - Body 탭에서 GraphQL 쿼리 작성:
   ```graphql
   query {
     product(id: 1) {
       id
       name
       price
       category {
         id
         name
       }
     }
   }
   ```
   - Send 버튼 클릭하여 실행

3. **필드 선택 테스트**
   - 필요한 필드만 선택하여 쿼리 실행
   - REST와 비교하여 오버페칭 해결 확인
   ```graphql
   query {
     product(id: 1) {
       id
       name
     }
   }
   ```

4. **관계 데이터 조회**
   ```graphql
   query {
     order(id: 1) {
       id
       orderNumber
       user {
         id
         username
       }
       items {
         id
         product {
           id
           name
         }
         quantity
         price
       }
     }
   }
   ```

### 실습 2: GraphQL Mutation 구현 (Postman)

1. **제품 생성**
   - Body에 다음 Mutation 작성:
   ```graphql
   mutation {
     createProduct(input: {
       name: "GraphQL 제품"
       price: 75000.0
       categoryId: 1
       description: "GraphQL로 생성된 제품"
     }) {
       id
       name
       price
     }
   }
   ```

2. **생성된 제품 조회**
   - 생성된 ID로 제품 조회 쿼리 실행

3. **제품 수정**
   ```graphql
   mutation {
     updateProduct(id: 12, input: {
       price: 80000.0
     }) {
       id
       name
       price
     }
   }
   ```

4. **제품 삭제**
   ```graphql
   mutation {
     deleteProduct(id: 12)
   }
   ```

### 실습 3: gRPC 서비스 테스트 (Postman)

1. **Postman에서 gRPC 요청 생성**
   - 요청 타입: **gRPC**
   - Server URL: `localhost:9090`
   - Method: 서비스와 메서드 선택

2. **서비스 목록 확인**
   - Server URL 입력 후 Connect
   - 사용 가능한 서비스 목록 확인:
     - `com.example.restful.grpc.ProductService`
     - `com.example.restful.grpc.UserService`

3. **제품 조회**
   - Service: `com.example.restful.grpc.ProductService`
   - Method: `GetProduct`
   - Message (JSON):
   ```json
   {
     "id": 1
   }
   ```

4. **제품 목록 조회**
   - Service: `com.example.restful.grpc.ProductService`
   - Method: `GetProducts`
   - Message (JSON):
   ```json
   {
     "page": 0,
     "size": 5,
     "sort": "id,asc"
   }
   ```

5. **제품 생성**
   - Service: `com.example.restful.grpc.ProductService`
   - Method: `CreateProduct`
   - Message (JSON):
   ```json
   {
     "name": "gRPC 제품",
     "price": 90000.0,
     "category_id": 1,
     "description": "gRPC로 생성된 제품"
   }
   ```

6. **제품 수정**
   - Service: `com.example.restful.grpc.ProductService`
   - Method: `UpdateProduct`
   - Message (JSON):
   ```json
   {
     "id": 11,
     "name": "수정된 제품명",
     "price": 60000.0
   }
   ```

7. **제품 삭제**
   - Service: `com.example.restful.grpc.ProductService`
   - Method: `DeleteProduct`
   - Message (JSON):
   ```json
   {
     "id": 11
   }
   ```

8. **사용자 조회**
   - Service: `com.example.restful.grpc.UserService`
   - Method: `GetUser`
   - Message (JSON):
   ```json
   {
     "id": 1
   }
   ```



## GraphQL 구현

### GraphQL 엔드포인트

- **GraphQL API**: `http://localhost:8080/graphql`
- **GraphiQL (개발 도구)**: `http://localhost:8080/graphiql` (참고용)

### GraphQL Schema

Schema는 `src/main/resources/graphql/schema.graphqls`에 정의되어 있습니다.

주요 타입:
- `Query`: 데이터 조회
- `Mutation`: 데이터 변경
- `Product`, `User`, `Order`, `Category`: 도메인 모델

### Postman에서 GraphQL 사용 방법

1. **새 요청 생성**
   - 요청 타입: **GraphQL**
   - URL: `http://localhost:8080/graphql`
   - Method: **POST** (자동 설정)

2. **쿼리 작성**
   - Body 탭에서 GraphQL 쿼리 작성
   - Variables 탭에서 변수 정의 가능

3. **요청 실행**
   - Send 버튼 클릭
   - 응답 확인

### GraphQL Query 예제 (Postman)

#### 1. 제품 단일 조회
```graphql
query {
  product(id: 1) {
    id
    name
    price
    description
    category {
      id
      name
    }
  }
}
```

#### 2. 제품 목록 조회 (페이징)
```graphql
query {
  products(page: 0, size: 10, sort: "name,asc") {
    content {
      id
      name
      price
    }
    page
    size
    totalElements
    totalPages
  }
}
```

#### 3. 사용자 조회
```graphql
query {
  user(id: 1) {
    id
    username
    email
    name
  }
}
```

#### 4. 주문 조회 (관계 포함)
```graphql
query {
  order(id: 1) {
    id
    orderNumber
    totalAmount
    orderDate
    user {
      id
      username
      name
    }
    items {
      id
      quantity
      price
      product {
        id
        name
        price
      }
    }
  }
}
```

#### 5. 사용자별 주문 목록 조회
```graphql
query {
  ordersByUser(userId: 1, page: 0, size: 10) {
    content {
      id
      orderNumber
      totalAmount
      orderDate
    }
    totalElements
  }
}
```

### GraphQL Mutation 예제 (Postman)

#### 1. 제품 생성
```graphql
mutation {
  createProduct(input: {
    name: "새 제품"
    price: 50000.0
    categoryId: 1
    description: "테스트 제품"
  }) {
    id
    name
    price
  }
}
```

#### 2. 제품 수정
```graphql
mutation {
  updateProduct(id: 1, input: {
    name: "수정된 제품명"
    price: 60000.0
  }) {
    id
    name
    price
  }
}
```

#### 3. 제품 삭제
```graphql
mutation {
  deleteProduct(id: 1)
}
```

#### 4. 사용자 생성
```graphql
mutation {
  createUser(input: {
    username: "newuser"
    email: "newuser@example.com"
    name: "신규 사용자"
  }) {
    id
    username
    email
  }
}
```

### GraphQL의 장점

1. **오버페칭/언더페칭 해결**
   - 필요한 필드만 요청
   - 한 번의 요청으로 여러 리소스 조회

2. **타입 안정성**
   - Schema로 타입 정의
   - 자동 검증

3. **개발 도구**
   - Postman에서 GraphQL 요청 타입 지원
   - Schema 탐색 및 자동완성 지원

## gRPC 구현

### gRPC 서버 포트

- **gRPC 서버**: `localhost:9090`

### Protocol Buffer 정의

Protocol Buffer 파일은 `src/main/proto/` 디렉토리에 정의되어 있습니다:
- `product.proto`: 제품 서비스 정의
- `user.proto`: 사용자 서비스 정의

### gRPC 서비스

구현된 서비스:
- `ProductService`: 제품 CRUD 작업
- `UserService`: 사용자 CRUD 작업

### Postman에서 gRPC 사용 방법

1. **새 요청 생성**
   - 요청 타입: **gRPC**
   - Server URL: `localhost:9090`
   - Connect 버튼 클릭

2. **서비스 및 메서드 선택**
   - Connect 후 사용 가능한 서비스 목록 표시
   - Service 선택: `com.example.restful.grpc.ProductService` 또는 `com.example.restful.grpc.UserService`
   - Method 선택: `GetProduct`, `GetProducts`, `CreateProduct` 등

3. **메시지 작성**
   - Message 탭에서 JSON 형식으로 메시지 작성
   - Postman이 자동으로 메시지 구조를 표시

4. **요청 실행**
   - Invoke 버튼 클릭
   - 응답 확인

### gRPC 요청 예제 (Postman)

#### 1. 제품 조회
- **Service**: `com.example.restful.grpc.ProductService`
- **Method**: `GetProduct`
- **Message**:
```json
{
  "id": 1
}
```

#### 2. 제품 목록 조회
- **Service**: `com.example.restful.grpc.ProductService`
- **Method**: `GetProducts`
- **Message**:
```json
{
  "page": 0,
  "size": 10,
  "sort": "id,asc"
}
```

#### 3. 제품 생성
- **Service**: `com.example.restful.grpc.ProductService`
- **Method**: `CreateProduct`
- **Message**:
```json
{
  "name": "새 제품",
  "price": 50000.0,
  "category_id": 1,
  "description": "테스트 제품"
}
```

#### 4. 제품 수정
- **Service**: `com.example.restful.grpc.ProductService`
- **Method**: `UpdateProduct`
- **Message**:
```json
{
  "id": 1,
  "name": "수정된 제품명",
  "price": 60000.0
}
```

#### 5. 제품 삭제
- **Service**: `com.example.restful.grpc.ProductService`
- **Method**: `DeleteProduct`
- **Message**:
```json
{
  "id": 1
}
```

#### 6. 사용자 조회
- **Service**: `com.example.restful.grpc.UserService`
- **Method**: `GetUser`
- **Message**:
```json
{
  "id": 1
}
```

#### 7. 사용자 목록 조회
- **Service**: `com.example.restful.grpc.UserService`
- **Method**: `GetUsers`
- **Message**:
```json
{
  "page": 0,
  "size": 10
}
```

#### 8. 사용자 생성
- **Service**: `com.example.restful.grpc.UserService`
- **Method**: `CreateUser`
- **Message**:
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "name": "신규 사용자"
}
```

### gRPC의 장점

1. **높은 성능**
   - Protocol Buffers로 직렬화 (JSON보다 빠름)
   - HTTP/2 기반 스트리밍 지원

2. **강타입**
   - Protocol Buffer로 타입 정의
   - 코드 생성으로 타입 안정성 보장

3. **다양한 통신 패턴**
   - Unary RPC
   - Server Streaming
   - Client Streaming
   - Bidirectional Streaming

## 통신 패턴 비교

### 동일한 기능 구현 비교

#### 제품 조회

**REST:**
```http
GET /api/products/1
```

**GraphQL:**
```graphql
query {
  product(id: 1) {
    id
    name
    price
  }
}
```

**gRPC (Postman):**
- Service: `com.example.restful.grpc.ProductService`
- Method: `GetProduct`
- Message: `{"id": 1}`

#### 제품 목록 조회

**REST:**
```http
GET /api/products?page=0&size=10&sort=name,asc
```

**GraphQL:**
```graphql
query {
  products(page: 0, size: 10, sort: "name,asc") {
    content {
      id
      name
      price
    }
  }
}
```

**gRPC (Postman):**
- Service: `com.example.restful.grpc.ProductService`
- Method: `GetProducts`
- Message: `{"page": 0, "size": 10, "sort": "name,asc"}`

#### 제품 생성

**REST:**
```http
POST /api/products
Content-Type: application/json

{
  "name": "새 제품",
  "price": 50000.0,
  "categoryId": 1
}
```

**GraphQL:**
```graphql
mutation {
  createProduct(input: {
    name: "새 제품"
    price: 50000.0
    categoryId: 1
  }) {
    id
    name
    price
  }
}
```

**gRPC (Postman):**
- Service: `com.example.restful.grpc.ProductService`
- Method: `CreateProduct`
- Message: `{"name": "새 제품", "price": 50000.0, "category_id": 1}`

### 기술별 장단점 비교

| 특징 | REST | GraphQL | gRPC |
|------|------|---------|------|
| **데이터 페칭** | 오버페칭 가능 | 필요한 필드만 | 전체 객체 |
| **버전 관리** | URI 또는 헤더 | Schema 진화 | Protocol Buffer 진화 |
| **캐싱** | HTTP 캐싱 활용 가능 | 복잡함 | 제한적 |
| **성능** | 보통 | 보통 | 높음 |
| **학습 곡선** | 낮음 | 중간 | 높음 |
| **타입 안정성** | 낮음 | 높음 | 매우 높음 |
| **개발 도구** | Postman 등 | Postman (GraphQL) | Postman (gRPC) |
| **사용 사례** | 일반적인 웹 API | 복잡한 데이터 요구사항 | 마이크로서비스 간 통신 |

### 선택 가이드

**REST를 선택하는 경우:**
- 간단한 CRUD 작업
- HTTP 캐싱이 중요한 경우
- 널리 사용되는 표준이 필요한 경우

**GraphQL을 선택하는 경우:**
- 클라이언트가 필요한 데이터를 정확히 지정해야 하는 경우
- 오버페칭/언더페칭 문제가 있는 경우
- 모바일 앱처럼 네트워크 효율성이 중요한 경우

**gRPC를 선택하는 경우:**
- 마이크로서비스 간 통신
- 높은 성능이 필요한 경우
- 강타입이 중요한 경우
- 스트리밍이 필요한 경우

## 프로젝트 구조

```
restful-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/restful/
│   │   │   ├── graphql/              # GraphQL Resolver
│   │   │   │   ├── ProductResolver.java
│   │   │   │   ├── UserResolver.java
│   │   │   │   ├── OrderResolver.java
│   │   │   │   ├── CategoryResolver.java
│   │   │   │   └── *Page.java        # 페이징 타입
│   │   │   ├── grpc/                 # gRPC 서비스
│   │   │   │   ├── ProductGrpcService.java
│   │   │   │   └── UserGrpcService.java
│   │   │   └── ...
│   │   ├── proto/                    # Protocol Buffer 정의
│   │   │   ├── product.proto
│   │   │   └── user.proto
│   │   └── resources/
│   │       ├── graphql/
│   │       │   └── schema.graphqls  # GraphQL Schema
│   │       └── application.yml
│   └── test/
└── build.gradle
```

## 참고사항

- GraphQL은 `/graphql` 엔드포인트로 접근합니다.
- Postman에서 GraphQL 요청 타입을 사용하여 테스트합니다.
- gRPC 서버는 포트 9090에서 실행됩니다.
- Postman에서 gRPC 요청 타입을 사용하여 테스트합니다.
- Protocol Buffer 파일을 수정한 후에는 프로젝트를 다시 빌드해야 합니다.
- Postman의 gRPC 기능을 사용하려면 서버에 연결한 후 서비스와 메서드를 선택해야 합니다.

