## 1. 포인트
### 1-1. 포인트 충전
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant PointController as PointController(API)
    participant PointService as PointService
    participant UserPointRepository
    participant UserPoint as UserPoint 도메인
    participant UserPointHistoryRepository
    participant UserPointHistory as UserPointHistory 도메인

    %% 사용자 충전 요청
    User->>PointController: 잔액 충전 요청(userId, amount)

    %% 요청값 검증
    alt 최소 충전 금액 미만
        PointController-->>User: 실패 - 최소 충전 금액 미만 (10,000원 이상)
    else 유효성 검증 통과
        PointController->>PointService: 잔액 충전 처리 요청(userId, amount)
    end

    %% 현재 잔액 및 사용자 조회
        PointService->>UserPointRepository: UserPoint 조회
        UserPointRepository-->>PointService: UserPoint 도메인

        %% 도메인 검증
        PointService->>UserPoint: 잔액 충전 처리 요청
        activate UserPoint 
        UserPoint ->> UserPoint : 보유 한도 초과 여부 검증
        deactivate UserPoint
        alt 보유 한도 검증 실패
            UserPoint-->>PointService: 실패 - 보유 한도 초과
            PointService-->>PointController: 실패 - 보유 한도 초과
            PointController-->>User: 실패 - 보유 한도 초과
        else 보유 한도 검증 성공
            %% 잔액 증가 및 저장
            activate UserPoint
            UserPoint ->> UserPoint: 잔액증가(+amount)
            deactivate UserPoint
            UserPoint -->> PointService: 잔액  증가된 UserPoint 반환
            PointService->>UserPointRepository: 저장(UserPoint)
            UserPointRepository-->>PointService: 저장 완료

            %% 히스토리 기록
            PointService->>UserPointHistory: 생성(userId, amount, CHARGE)
            UserPointHistory -->> PointService: 반환
            PointService->>UserPointHistoryRepository: 저장(UserPointHistory)
            UserPointHistoryRepository-->>PointService: 저장 완료

            %% 성공 응답
            PointService-->>PointController: 충전 성공
            PointController-->>User: 충전 성공
        end
```
### 1-2. 포인트 조회
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UserPoint 

    User ->> UserPoint : 사용자의 잔액 정보 조회
    UserPoint -->> User: 사용자의 잔액 정보 전달
```

## 2. 쿠폰
### 2-1-1. 선착순 발급 (초기 설계:동기 처리)
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant CouponController
    User ->> CouponController: 쿠폰 발급 요청(userId, couponId)

    participant CouponService
    CouponController ->> CouponService: 쿠폰 발급 처리 요청(userId, couponId)

    %% 1. userId + couponId 로 UserCoupon 존재 여부 확인
    CouponService ->> UserCouponRepository: UserCoupon 조회
    UserCouponRepository -->> CouponService: UserCoupon 존재 여부
    alt 이미 발급된 쿠폰
        CouponService -->> CouponController: 실패 - 이미 발급된 쿠폰
        CouponController -->> User: 실패 - 이미 발급된 쿠폰
    else 발급 가능
        %% 2. couponId 로 Coupon 도메인 조회
        CouponService ->> CouponRepository: Coupon 조회
        CouponRepository -->> CouponService: Coupon 데이터
        CouponService ->> Coupon: 유효성 체크

        %% 3. Coupon 도메인 유효성 체크
        activate Coupon
        Coupon ->> Coupon: 만료 여부 확인
        alt 쿠폰 만료됨
            Coupon -->> CouponService: 실패 - 쿠폰 만료
            CouponService -->> CouponController: 실패 - 쿠폰 만료
            CouponController -->> User: 실패 - 쿠폰 만료
        else 쿠폰 유효
            Coupon ->> Coupon: 선착순 수량 확인
            alt 쿠폰 수량 부족
                Coupon -->> CouponService: 실패 - 수량 소진
                CouponService -->> CouponController: 실패 - 수량 소진
                CouponController -->> User: 실패 - 수량 소진
            else 수량 가능
                %% 4. 쿠폰 발급 처리
                Coupon ->> Coupon: issuedQuantity 증가
                deactivate Coupon
                Coupon -->> CouponService: 업데이트된 쿠폰 반환
                CouponService ->> CouponRepository: save(Coupon)
                CouponRepository -->> CouponService: 저장 완료

                %% 5. UserCoupon 도메인 생성
                CouponService ->> UserCoupon: UserCoupon 도메인 생성
                UserCoupon -->> CouponService: UserCoupon 도메인 반환
                CouponService ->> UserCouponRepository: save(UserCoupon)
                UserCouponRepository -->> CouponService: 저장 완료

                %% 성공 응답
                CouponService -->> CouponController: 발급 성공
                CouponController -->> User: 발급 성공
            end
        end
    end
```

### 2-1-2 선착순 발급 (Redis 대기열)
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

### 2-1-3. 선착순 발급 (Redis + Kafka)
```mermaid
sequenceDiagram
  autonumber
  participant User as User
  participant CouponService as CouponService
  participant Kafka as Kafka Broker (topic: coupon.issue.requested)
  participant Consumer as CouponIssueConsumer
  participant Redis as Redis (발급 캐시)
  participant DB as Database (SOT)

  User ->> CouponService: 쿠폰 발급 요청
  CouponService ->> Kafka: CouponIssueEvent(userId, couponId) 발행
  CouponService -->> User: 발급 요청 접수 완료 (비동기 응답)

  loop 메시지 소비
    Kafka -->> Consumer: CouponIssueEvent 수신
    Consumer ->> DB: 쿠폰 조회 및 기간 검증
    alt 발급 가능 기간 아님
      Consumer ->> Consumer: 비즈니스 예외 처리
    else 기간 유효
      Consumer ->> Redis: existsIssuedUser(couponId, userId)?
      alt 이미 발급 사용자
        Consumer ->> Consumer: 비즈니스 예외 처리
      else 신규 사용자
        Consumer ->> Redis: limit/count 조회 (getCouponLimitQuantity / countIssuedUser)
        alt 수량 초과
          Consumer ->> Consumer: 비즈니스 예외 처리
        else 수량 여유 있음
          %% Redis로 중복/경합 1차 차단 (원자 연산)
          Consumer ->> Redis: saveIssuedUser(couponId, userId)
          %% DB에서 수량 확정 (배타락으로 직렬화)
          Consumer ->> DB: 쿠폰 수량 증가 (Pessimistic Lock)
          Consumer ->> DB: UserCoupon 저장 (발급 확정)
        end
      end
    end
  end
```

### 2-2. 보유 쿠폰 목록 조회
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UserCoupon
    User ->> UserCoupon : 사용자의 쿠폰 정보 조회
    UserCoupon -->> User : 사용자의 쿠폰 정보 전달
```

## 3. 상품
### 3-1. 상품 조회
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Product
    User ->> Product : 상품 정보 조회
    Product -->> User: 상품 정보 전달
```

### 3-2-1. 상위 상품 (초기 설계 : 스케줄러로 테이블 업데이트)
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    participant Scheduler as 스케줄러
    participant OrderProduct as ORDER_PRODUCT 테이블
    participant BestSeller as BEST_SELLER 테이블
    
    loop while(every 24hrs)

	    Scheduler->>OrderProduct: 매일 24시 최근 3일 주문상품 판매량 집계 쿼리 실행
	    OrderProduct-->>Scheduler: 집계 결과(상품ID, 판매수량)
	    Scheduler->>BestSeller: BEST_SELLER 테이블 초기화
	    BestSeller-->>Scheduler: 초기화 완료
	    loop 상위 5개 상품
	        Scheduler->>BestSeller: 상품ID, 이름, 단가, 판매수량 기록
	        BestSeller-->>Scheduler: 기록 완료
	    end
	end
```
```mermaid
sequenceDiagram
    autonumber
    actor User
    participant BestProduct
    User ->> BestProduct : 3일간 상위 상품 정보 조회
    BestProduct -->> User: 3일간 상위 상품 정보 전달
```

### 3-2-2. 상위 상품(Redis 캐싱, Application Event)
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

### 3-2-3. 상위 상품 조회 (Kafka 이벤트)
```mermaid
sequenceDiagram
  autonumber
  participant Order as OrderFacade
  participant P as Kafka Producer
  participant K as Kafka (topic: order.created)
  participant Rank as ProductSalesRanking(Consumer)
  participant Redis as Redis(Cache)

  Order ->> P: publish OrderCreatedEvent(orderId, items)
  P ->> K: send to order.created
  K -->> Rank: consume OrderCreatedEvent
  Rank ->> Redis: 실시간 집계 업데이트(상품별 INCR)
```


## 4. 주문/결제
### 4-1. 주문/결제 (초기 설계)
```mermaid
sequenceDiagram
    autonumber
    actor user as User
    participant order as 주문
    participant stock as 재고
    participant coupon as 쿠폰
    participant point as 포인트
    participant data_platform as 데이터 플랫폼
    user ->> order: (상품ID, 수량)목록으로 주문 요청
    order ->> stock: 재고 차감 요청
    alt 재고가 부족한 경우
        stock -->> order: 재고 차감 실패
        order -->> user : 주문 실패
    else 재고가 있는 경우
        activate stock
        stock ->> stock: 재고 차감 처리
        deactivate stock
        stock -->> order: 재고 차감 완료
    end
    opt 적용하는 쿠폰이 있는 경우
        order ->> coupon: 쿠폰 적용 요청
        alt 쿠폰이 정상적으로 적용되지 않는 경우
            coupon -->> order: 쿠폰 적용 실패
            order ->> stock : 재고 롤백 요청
                activate stock
                stock ->> stock: 재고 롤백 처리
                deactivate stock
            stock -->> order : 재고 롤백 완료
            order -->> user : 주문 실패
        else 쿠폰이 정상적으로 적용되는 경우
            activate coupon
            coupon ->> coupon: 쿠폰 사용 처리
            deactivate coupon
            coupon -->> order : 쿠폰 적용 완료
        end
    end
    order ->> point: 포인트 차감 요청
    alt 포인트 차감에 실패한 경우
        point -->> order : 포인트 차감 실패
        opt 쿠폰을 적용한 경우
            order -->> coupon: 쿠폰 롤백 처리 요청
            activate coupon
            coupon ->> coupon : 쿠폰 롤백 처리
            deactivate coupon
            coupon ->> order: 쿠폰 롤백 완료
        end
        order ->> stock : 재고 롤백 요청
            activate stock
            stock ->> stock: 재고 롤백 처리
            deactivate stock
        stock -->> order : 재고 롤백 완료
        order -->> user : 주문 실패
    else 포인트 차감에 성공한 경우
        activate point
        point -->> point : 포인트 처리
        deactivate point
        point -->> order : 포인트 차감 완료
        order -->> user : 주문 완료
    end
    opt 주문에 성공한 경우
        order ->> data_platform: 주문 정보 전송 (비동기)
    end

```

### 4-2. 주문 완료 이후 Outbox + Kafka
```mermaid
sequenceDiagram
    autonumber
    participant order as 주문(Facade/App)
    participant db as Database(RDBMS)
    participant outbox as Outbox 테이블
    participant sch as OutboxScheduler
    participant producer as Kafka Producer
    participant kafka as Kafka (topic: order.created)
    participant consumer as ProductSalesRanking(Consumer)

    %% 주문 트랜잭션 내 기록
    order ->> outbox: Outbox INSERT (status=INIT, topic=order.created, payload=JSON)
    order ->> db: 트랜잭션 커밋
    db -->> order: 커밋 성공 (주문 완료)

    %% 비동기 이벤트 전파
    sch -->> outbox: 주기적 폴링(STATUS=INIT)
    sch ->> outbox: INIT 이벤트 배치 조회
    sch ->> producer: publish OrderCreatedEvent (key=orderId)
    producer ->> kafka: send(topic=order.created)
    kafka -->> producer: ACK(acks=all)
    sch ->> outbox: STATUS=PUBLISHED 업데이트

    %% 소비 및 후속 처리
    kafka -->> consumer: OrderCreatedEvent 전달
    consumer ->> consumer: 판매량 집계, 데이터 플랫폼 정보 전달
    consumer -->> kafka: 오프셋 커밋(자동/수동)
```
