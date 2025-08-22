## 실시간 인기 상품 조회
### 1. 시나리오 흐름
1. 주문 완료 시 이벤트 발행
   - 주문이 완료되면 OrderFacade 에서 OrderCreatedEvent 를 발행합니다.
2. 메인 트랜잭션 커밋 이후 실시간 판매량 집계
    - 이벤트 리스너(OrderCreatedEventHandler)가 AFTER_COMMIT 단계에서 호출되어 트랜잭션의 일관성을 보장합니다.
    - ProductSalesRankingService 에서 Redis Sorted Set 을 사용해 실시간으로 판매량을 증가시킵니다.
3. 한 시간에 한 번, 스케줄러에 의한 캐싱 워밍
    - BestProductAggregateScheduler 가 실행되어 최근 3일(today, today-1, today-2)의 판매 데이터를 합산합니다.
    - Redis의 ZUNIONSTORE 명령어를 통해 집계된 랭킹을 캐싱하여 조회 성능을 최적화합니다.
4. 하루에 한 번, 스케줄러에 의한 DB 영속화
    - ProductSalesHistoryScheduler 가 매일 00:10에 실행됩니다.
    - Redis에 집계된 전날 판매 데이터를 SOT(Source of Truth)인 DB에 영속화하여 데이터 유실을 방지합니다.

### 2. 시퀀스 다이어그램
```mermaid
    sequenceDiagram
       autonumber
       participant OrderFacade as OrderFacade
       participant OrderCreatedEventHandler as OrderCreatedEventHandler
       participant ProductSalesRankingService as ProductSalesRankingService
       participant Redis as Redis (Cache)
       participant DB as Database (SOT)
       participant BestProductAggregateScheduler as BestProductAggregateScheduler
       participant ProductSalesHistoryScheduler as ProductSalesHistoryScheduler
    
       OrderFacade ->> OrderCreatedEventHandler: 주문 완료 이벤트 발행
       OrderCreatedEventHandler ->> ProductSalesRankingService: (AFTER_COMMIT) 이벤트 전달
       ProductSalesRankingService ->> Redis: 실시간 판매량 집계 업데이트
    
       loop 매 시간
       BestProductAggregateScheduler ->> Redis: today, today-1, today-2 랭킹 캐싱 워밍
       end
    
       loop 매일 00:10
       ProductSalesHistoryScheduler ->> DB: 전날 판매량 집계 영속화
       end
```

### 3. Redis 자료 구조 설계
#### 선택한 자료구조: Sorted Set (ZSET)
#### 이유
- member(상품 ID)에 대해 score(판매량)를 기반으로 자동 정렬이 가능
- 범위 기반 조회(ZRANGE, ZREVRANGE) 지원
#### Key 설계
- 일 단위 집계
    - Key: `product:ranking:day:{YYYY-MM-DD}`
    - Member: `productId`
    - Score: `quantity`
- 3일간 집계 
    - `ZUNIONSTORE product:ranking:3days product:ranking:day:today product:ranking:day:today-1 product:ranking:day:today-2`
    - 최근 3일간 집계 데이터를 캐싱해두어 실시간 조회 성능을 보장

### 4. 장점 및 고려사항

#### 장점
- 이벤트 기반 비동기 처리: 주문 트랜잭션과 분리되어 메인 로직 성능 저하 최소화
- Redis 활용한 고성능 집계: Sorted Set 사용으로 랭킹 연산 최적화
- 스케줄러 기반 캐싱 & 영속화: 캐싱 워밍으로 조회 지연 방지, DB 영속화로 데이터 유실 방지

#### 고려사항
- 3일 단위 vs 72시간 단위 집계
   - 단순 3일 단위(today, today-1, today-2)는 구현이 간단
   - 정확한 "72시간 기준 집계"를 위해서는 Lua Script + Sliding Window 방식 고려 필요
- DB 영속화 시 부하
   - 하루 단위 batch insert 시 부하 고려 필요

## 선착순 쿠폰 발급
### 1. 시나리오 흐름
1. 사용자가 쿠폰 발급 요청을 하면, 쿠폰 발급 가능 여부(만료, 중복, 수량 초과) 를 동기적으로 검증한다.
    - 만료 여부 → 쿠폰 메타데이터 확인
    - 중복 여부 → Redis Set(coupon:issue:{couponId}) 확인
    - 수량 초과 여부 → Redis Set 사이즈 확인
2. 검증을 통과하면, 실제 발급은 비동기적으로 처리한다.
   - 요청은 Redis 대기열(List)에 userId 를 등록하고,
   - 해당 쿠폰 ID 는 PENDING_COUPON_KEY(Set)에 넣어 스케줄러가 어떤 쿠폰을 처리할지 알 수 있도록 한다.
3. 스케줄러가 주기적으로 실행되어
   - PENDING_COUPON_KEY에서 대기 중인 쿠폰 ID들을 조회한다.
   - 각 쿠폰 ID에 대해 coupon:queue:{couponId} 큐에서 userId 들을 batchSize 만큼 꺼낸다.
   - 발급 처리(DB 반영) 후, 큐가 완전히 비었다면 PENDING_COUPON_KEY에서 해당 쿠폰 ID를 제거한다.

### 2. 시퀀스 다이어그램
```mermaid
sequenceDiagram
   autonumber
   participant User as User
   participant CouponService as CouponService
   participant Redis as Redis (대기열)
   participant Scheduler as Scheduler
   participant DB as Database (SOT)

   User ->> CouponService: 쿠폰 발급 요청
   CouponService ->> CouponService: 만료/중복/수량 초과 검증
   CouponService ->> Redis: 대기열 등록 (couponId, userId)
   CouponService -->> User: 발급 요청 접수 완료 (비동기 응답)

   loop 주기적 실행
   Scheduler ->> Redis: 대기열에서 userId 목록 Pop
   Scheduler ->> DB: 쿠폰 수량 증가 (Pessimistic Lock)
   Scheduler ->> DB: UserCoupon 저장 (발급 확정)
   end

```
   

## 3. Redis 자료구조 설계
#### 선택한 자료구조
- Set(userId) : 이미 발급된 사용자 체크 / 수량 체크
    - Key: `coupon:issued:{couponId}`
    - Value: userId
- List(userId) : 발급 대기열 관리
    - Key: `coupon:queue:{couponId}`
    - Value: userId
    - LPUSH + RPOP → FIFO 구조 보장
- Set(couponId) : 대기중 쿠폰 ID 관리
    - Key: `coupon:pending` 
    - Value: couponId

## 4. 장점 및 고려사항

#### 장점
- 동기 검증 → 비동기 발급 구조로 DB 부하 최소화
- Redis 자료구조 활용으로 중복 방지 및 선착순 보장
- 스케줄러를 통한 배치 처리로 대량 발급 상황에도 안정적 운영

#### 고려사항
- 스케줄러 주기 
    - 너무 짧으면 DB 부하가 집중될 수 있음
    - 너무 길면 사용자 입장에서 발급 확정이 늦어짐