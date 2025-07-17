## 1. 포인트
### 포인트 충전 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor User as 사용자
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
        PointController-->>User: 실패 - 최소 충전 금액 미만
    else 충전 단위 불일치
        PointController-->>User: 실패 - 1,000원 단위만 허용
    else 유효성 검증 통과
        PointController->>PointService: 잔액 충전 처리 요청(userId, amount)
    end

    %% 하루 충전 한도 검증
    PointService->>UserPointHistoryRepository: 오늘 누적 충전액 집계(userId)
    UserPointHistoryRepository-->>PointService: 오늘 누적 충전액
    alt 하루 한도 초과
        PointService-->>PointController: 실패 - 하루 한도 초과
        PointController-->>User: 실패 - 하루 한도 초과
    else 하루 한도 이내
        %% 현재 잔액 및 사용자 조회
        PointService->>UserPointRepository: UserPoint 조회
        UserPointRepository-->>PointService: UserPoint 도메인

        %% 도메인 검증
        PointService->>UserPoint: 잔액 충전 처리 요청
        activate UserPoint 
        UserPoint ->> UserPoint : 보유 한도 초과 여부 검증
        deactivate UserPoint
        alt 보유 한도 검증 통과
            UserPoint-->>PointService: 실패 - 보유 한도 초과
            PointService-->>PointController: 실패 - 보유 한도 초과
            PointController-->>User: 실패 - 보유 한도 초과
        else 검증 통과
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
    end
```
### 포인트 조회 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor User 사용자
    participant UserPoint 

    User ->> UserPoint : 사용자의 잔액 정보 조회
    UserPoint -->> User: 사용자의 잔액 정보 전달
```

## 2. 쿠폰
### 선착순 쿠폰 발급 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor User as 사용자
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

### 보유 쿠폰 조회 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor User as 사용자
    participant UserCoupon
    User ->> UserCoupon : 사용자의 쿠폰 정보 조회
    UserCoupon -->> User : 사용자의 쿠폰 정보 전달
```

## 3. 쿠폰
### 상품 조회 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor User as 사용자
    participant Product
    User ->> Product : 상품 정보 조회
    Product -->> User: 상품 정보 전달
```

## 4. 주문/결제
### 주문/결제 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor user as 사용자
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

## 5. 상위 상품
### 상위 상품 조회 시퀀스 다이어그램
```mermaid
---
config:
  theme: redux
---
sequenceDiagram
    autonumber
    actor User as 사용자
    participant BestProduct
    User ->> BestProduct : 3일간 상위 상품 정보 조회
    BestProduct -->> User: 3일간 상위 상품 정보 전달
```

### 상위 상품 스케줄러 시퀀스 다이어그램
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

    Scheduler->>OrderProduct: 매일 24시 최근 3일 주문상품 판매량 집계 쿼리 실행
    OrderProduct-->>Scheduler: 집계 결과(상품ID, 판매수량)
    Scheduler->>BestSeller: BEST_SELLER 테이블 초기화
    BestSeller-->>Scheduler: 초기화 완료
    loop 상위 5개 상품
        Scheduler->>BestSeller: 상품ID, 이름, 단가, 판매수량 기록
        BestSeller-->>Scheduler: 기록 완료
    end
```