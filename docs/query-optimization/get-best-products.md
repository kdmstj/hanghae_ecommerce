# 조회 성능 개선 보고서

## 대상 API
- **엔드포인트:** `GET /api/v1/products/best`
- **목적:** 특정 사용자에게 발급된 상태(ISSUED)의 쿠폰 목록 조회

### 쿼리문
```sql
SELECT new kr.hhplus.be.server.product.domain.repository.dto.BestProductProjection(
                p.id, p.productName, SUM(pds.quantity))
            FROM ProductDailySales pds
            JOIN Product p ON p.id = pds.productId
            WHERE pds.salesDate >= :from
            GROUP BY p.id
            ORDER BY SUM(pds.quantity) DESC LIMIT 5;
```

### 문제점
#### 실행 계획
```sql
+----+-------------+-------+------------+--------+---------------+---------+---------+-----------------------+-------+----------+----------------------------------------------+
| id | select_type | table | partitions | type   | possible_keys | key     | key_len | ref                   | rows  | filtered | Extra                                        |
+----+-------------+-------+------------+--------+---------------+---------+---------+-----------------------+-------+----------+----------------------------------------------+
|  1 | SIMPLE      | pds   | NULL       | ALL    | NULL          | NULL    | NULL    | NULL                  | 29485 |    33.33 | Using where; Using temporary; Using filesort |
|  1 | SIMPLE      | p     | NULL       | eq_ref | PRIMARY       | PRIMARY | 8       | hhplus.pds.product_id |     1 |   100.00 | NULL                                         |
+----+-------------+-------+------------+--------+---------------+---------+---------+-----------------------+-------+----------+----------------------------------------------+
```
- `product_daily_sales` 테이블에 인덱스가 없어 풀 테이블 스캔 발생

### 인덱스 튜닝 방법1 : `product_daily_sales.idx_product_id_sales_date` 
#### 적용 DDL 
```
CREATE INDEX idx_product_id_sales_date ON product_daily_sales(product_id, sales_date);
```

#### 실행 계획
```
+----+-------------+-------+------------+--------+---------------------------+---------+---------+-----------------------+-------+----------+----------------------------------------------+
| id | select_type | table | partitions | type   | possible_keys             | key     | key_len | ref                   | rows  | filtered | Extra                                        |
+----+-------------+-------+------------+--------+---------------------------+---------+---------+-----------------------+-------+----------+----------------------------------------------+
|  1 | SIMPLE      | pds   | NULL       | ALL    | idx_product_id_sales_date | NULL    | NULL    | NULL                  | 30295 |    33.33 | Using where; Using temporary; Using filesort |
|  1 | SIMPLE      | p     | NULL       | eq_ref | PRIMARY                   | PRIMARY | 8       | hhplus.pds.product_id |     1 |   100.00 | NULL                                         |
+----+-------------+-------+------------+--------+---------------------------+---------+---------+-----------------------+-------+----------+----------------------------------------------+
```
- sales_date는 두 번째 컬럼이라 조건절에서 사용되지 못함
- 결국 인덱스 미사용 → 풀 스캔 유지

### 인덱스 튜닝 방법 : `product_daily_sales.idx_sales_date_product_id` 추가
#### 적용 DDL 
```
CREATE INDEX idx_sales_date_product_id ON product_daily_sales(sales_date, product_id);
```
#### 실행 계획
```mermaid
+----+-------------+-------+------------+--------+---------------------------+---------------------------+---------+-----------------------+------+----------+--------------------------------------------------------+
| id | select_type | table | partitions | type   | possible_keys             | key                       | key_len | ref                   | rows | filtered | Extra                                                  |
+----+-------------+-------+------------+--------+---------------------------+---------------------------+---------+-----------------------+------+----------+--------------------------------------------------------+
|  1 | SIMPLE      | pds   | NULL       | range  | idx_sales_date_product_id | idx_sales_date_product_id | 3       | NULL                  | 3000 |   100.00 | Using index condition; Using temporary; Using filesort |
|  1 | SIMPLE      | p     | NULL       | eq_ref | PRIMARY                   | PRIMARY                   | 8       | hhplus.pds.product_id |    1 |   100.00 | NULL                                                   |
+----+-------------+-------+------------+--------+---------------------------+---------------------------+---------+-----------------------+------+----------+--------------------------------------------------------+
```
- sales_date는 WHERE 절에 사용되므로 range scan 가능
- 3000 rows만 탐색 (Full Scan → Range Scan)
- product_id는 정렬/그룹화/조인 조건에 활용 가능

### 결론
- 쿼리 필터링 조건(sales_date)을 인덱스의 선행 컬럼으로 설정해야 MySQL이 효율적으로 range scan 수행 가능
- product_id는 join과 group by에 쓰이므로 후순위로 두어 복합 인덱스로 구성