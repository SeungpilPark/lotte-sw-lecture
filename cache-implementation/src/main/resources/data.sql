-- 초기 데이터 생성 (H2 Database)

-- Categories
INSERT INTO categories (id, name, description) VALUES (1, '전자제품', '전자기기 및 액세서리');
INSERT INTO categories (id, name, description) VALUES (2, '의류', '의류 및 패션 아이템');
INSERT INTO categories (id, name, description) VALUES (3, '도서', '책 및 학습 자료');
INSERT INTO categories (id, name, description) VALUES (4, '식품', '음식 및 음료');

-- Products
INSERT INTO products (id, name, price, category_id, description) VALUES (1, '노트북', 1200000.0, 1, '고성능 노트북');
INSERT INTO products (id, name, price, category_id, description) VALUES (2, '마우스', 30000.0, 1, '무선 마우스');
INSERT INTO products (id, name, price, category_id, description) VALUES (3, '키보드', 80000.0, 1, '기계식 키보드');
INSERT INTO products (id, name, price, category_id, description) VALUES (4, '모니터', 300000.0, 1, '27인치 모니터');
INSERT INTO products (id, name, price, category_id, description) VALUES (5, '티셔츠', 25000.0, 2, '면 티셔츠');
INSERT INTO products (id, name, price, category_id, description) VALUES (6, '청바지', 60000.0, 2, '데님 청바지');
INSERT INTO products (id, name, price, category_id, description) VALUES (7, '자바 프로그래밍', 35000.0, 3, 'Java 학습서');
INSERT INTO products (id, name, price, category_id, description) VALUES (8, '스프링 부트 가이드', 40000.0, 3, 'Spring Boot 학습서');
INSERT INTO products (id, name, price, category_id, description) VALUES (9, '커피', 15000.0, 4, '원두 커피');
INSERT INTO products (id, name, price, category_id, description) VALUES (10, '과자', 5000.0, 4, '스낵 과자');

-- Users
INSERT INTO users (id, username, email, name) VALUES (1, 'user1', 'user1@example.com', '홍길동');
INSERT INTO users (id, username, email, name) VALUES (2, 'user2', 'user2@example.com', '김철수');
INSERT INTO users (id, username, email, name) VALUES (3, 'user3', 'user3@example.com', '이영희');

-- Orders
INSERT INTO orders (id, order_number, user_id, total_amount, order_date) VALUES (1, 'ORD-001', 1, 1230000.0, CURRENT_TIMESTAMP);
INSERT INTO orders (id, order_number, user_id, total_amount, order_date) VALUES (2, 'ORD-002', 1, 110000.0, CURRENT_TIMESTAMP);
INSERT INTO orders (id, order_number, user_id, total_amount, order_date) VALUES (3, 'ORD-003', 2, 300000.0, CURRENT_TIMESTAMP);
INSERT INTO orders (id, order_number, user_id, total_amount, order_date) VALUES (4, 'ORD-004', 3, 75000.0, CURRENT_TIMESTAMP);

-- Order Items
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (1, 1, 1, 1, 1200000.0);
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (2, 1, 2, 1, 30000.0);
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (3, 2, 3, 1, 80000.0);
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (4, 2, 2, 1, 30000.0);
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (5, 3, 4, 1, 300000.0);
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (6, 4, 5, 2, 25000.0);
INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES (7, 4, 6, 1, 60000.0);

-- H2 시퀀스 재설정 (ID를 명시적으로 지정했으므로 시퀀스를 다음 값으로 설정)
ALTER TABLE products ALTER COLUMN id RESTART WITH 11;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 5;
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 5;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 8;

