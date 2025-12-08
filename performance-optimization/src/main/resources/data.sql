-- 초기 데이터 생성 (H2 Database)

-- Customers
INSERT INTO customers (id, name, email) VALUES (1, '홍길동', 'hong@example.com');
INSERT INTO customers (id, name, email) VALUES (2, '김철수', 'kim@example.com');
INSERT INTO customers (id, name, email) VALUES (3, '이영희', 'lee@example.com');

-- Products
INSERT INTO products (id, name, price) VALUES (1, '노트북', 1200000.0);
INSERT INTO products (id, name, price) VALUES (2, '마우스', 30000.0);
INSERT INTO products (id, name, price) VALUES (3, '키보드', 80000.0);
INSERT INTO products (id, name, price) VALUES (4, '모니터', 300000.0);

-- Orders
INSERT INTO orders (id, order_number, customer_id) VALUES (1, 'ORD-001', 1);
INSERT INTO orders (id, order_number, customer_id) VALUES (2, 'ORD-002', 1);
INSERT INTO orders (id, order_number, customer_id) VALUES (3, 'ORD-003', 2);

-- Order Items
INSERT INTO order_items (id, order_id, product_id, quantity) VALUES (1, 1, 1, 1);
INSERT INTO order_items (id, order_id, product_id, quantity) VALUES (2, 1, 2, 2);
INSERT INTO order_items (id, order_id, product_id, quantity) VALUES (3, 2, 3, 1);
INSERT INTO order_items (id, order_id, product_id, quantity) VALUES (4, 3, 4, 1);
INSERT INTO order_items (id, order_id, product_id, quantity) VALUES (5, 3, 2, 1);
