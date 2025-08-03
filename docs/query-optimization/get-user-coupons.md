# 조회 성능 개선 보고서

## 대상 API
- **엔드포인트:** `GET /api/v1/users/{userId}/coupons`
- **목적:** 특정 사용자에게 발급된 상태(ISSUED)의 쿠폰 목록 조회

### 쿼리문
```sql
SELECT uc.id, uc.user_id, ucs.user_coupon_status 
FROM user_coupon AS uc 
JOIN user_coupon_state AS ucs    
    ON uc.id = ucs.user_coupon_id 
WHERE uc.user_id = 1   
  AND ucs.user_coupon_status = 'ISSUED';
```
### 문제점
#### 실행 계획
```sql
+----+-------------+-------+------------+--------+---------------+---------+---------+---------------------------+------+----------+-------------+
| id | select_type | table | partitions | type   | possible_keys | key     | key_len | ref                       | rows | filtered | Extra       |
+----+-------------+-------+------------+--------+---------------+---------+---------+---------------------------+------+----------+-------------+
|  1 | SIMPLE      | ucs   | NULL       | ALL    | NULL          | NULL    | NULL    | NULL                      | 1000 |    10.00 | Using where |
|  1 | SIMPLE      | uc    | NULL       | eq_ref | PRIMARY       | PRIMARY | 8       | hhplus.ucs.user_coupon_id |    1 |    10.00 | Using where |
+----+-------------+-------+------------+--------+---------------+---------+---------+---------------------------+------+----------+-------------+
```
- `user_coupon_state` 테이블에 대해 풀 테이블 스캔 발생
- 조인 순서 및 조건 필터링이 비효율적임

### 1차 인덱스 튜닝 : `user_coupon.user_id` 인덱스 추가
가설 : where 절에서 userId 기반으로 user_coupon 을 조회할 때 정렬된 user_id 에 대한 인덱스 스캔을 통해 효율적으로 탐색할 것으로 예상
#### 적용 DDL
```sql
CREATE INDEX idx_user_coupon_user_id ON user_coupon(user_id);
```

#### 실행 계획 변화
```sql
+----+-------------+-------+------------+------+---------------------------------+-------------------------+---------+-------+------+----------+--------------------------------------------+
| id | select_type | table | partitions | type | possible_keys                   | key                     | key_len | ref   | rows | filtered | Extra                                      |
+----+-------------+-------+------------+------+---------------------------------+-------------------------+---------+-------+------+----------+--------------------------------------------+
|  1 | SIMPLE      | uc    | NULL       | ref  | PRIMARY,idx_user_coupon_user_id | idx_user_coupon_user_id | 8       | const |    1 |   100.00 | Using index                                |
|  1 | SIMPLE      | ucs   | NULL       | ALL  | NULL                            | NULL                    | NULL    | NULL  | 1000 |     1.00 | Using where; Using join buffer (hash join) |
+----+-------------+-------+------------+------+---------------------------------+-------------------------+---------+-------+------+----------+--------------------------------------------+
```
- `user.user_coupon_id` 조건으로 인덱스 탐색 성공
- 하지만 여전히 `user_coupon_state` 는 풀 테이블 스캔

### 2차 인덱스 튜닝 : `user_coupon_state.(user_coupon_id + user_coupon_status)` 추가
가설 : `user_coupon_status` 는 cardinality 가 낮아서 단독 인덱스로는 비효율적이지만 조인 조건인 `user_coupon_id` 와 함께 복합 인덱스로 구성하면 조인 성능 향상 예상
#### 적용 DDL
```sql
CREATE INDEX idx_user_coupon_id_status ON user_coupon_state(user_coupon_id, user_coupon_status);
```

#### 실행 계획 변화
```sql
+----+-------------+-------+------------+------+---------------------------------+---------------------------+---------+--------------------+------+----------+-------------+
| id | select_type | table | partitions | type | possible_keys                   | key                       | key_len | ref                | rows | filtered | Extra       |
+----+-------------+-------+------------+------+---------------------------------+---------------------------+---------+--------------------+------+----------+-------------+
|  1 | SIMPLE      | uc    | NULL       | ref  | PRIMARY,idx_user_coupon_user_id | idx_user_coupon_user_id   | 8       | const              |    1 |   100.00 | Using index |
|  1 | SIMPLE      | ucs   | NULL       | ref  | idx_user_coupon_id_status       | idx_user_coupon_id_status | 91      | hhplus.uc.id,const |    1 |   100.00 | Using index |
+----+-------------+-------+------------+------+---------------------------------+---------------------------+---------+--------------------+------+----------+-------------+
```
- `user_coupon_state` 테이블도 복합 인덱스를 활용하여 탐색
- 조인 시 Join Buffer 제거됨 → 메모리 사용 최적화
