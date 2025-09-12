# 주문 사용자 여정 성능 테스트

## 계획

상품 조회 -> 주문/결제까지 사용자 주요 흐름인 시나리오를 테스트하는 것이 적합하다고 판단하였습니다.

### 1) 목표(SLA)
- 응답 지연 시간
  - 상품 상세 조회 : **p(95) < 600ms**
  - 사용자 쿠폰 목록 조회 : **p(95) < 600ms**
  - 주문/결제: **p(95) < 800ms**
- 실패율 : **실패율 < 0.2%**
  - 여정 성공률과 각 API 에러율 기준 모두

### 2) 부하 모델
본 테스트는 **ramping-arrival-rate** 방식을 적용하여 RPS 를 목표로 설정하였습니다.
- 방문 평균 RPS: **50 RPS**
- 피크 RPS: **120 RPS**
- 여정 단게
  - GET /api/v1/products/{id}
  - GET /api/v1/users/{userId}/coupons
  - POST /api/v1/orders
- 상품은 총 1,000개 데이터를 대상으로 유저, 쿠폰, 결제 모의값을 준비하였습니다.

### 3) 스테이지 구성
부하를 점진적으로 증가시킨 후 피크 구간을 검증하고 종료하는 시나리오로 구성하였습니다.
(실제 intro 에서 계산한 RPS 는 로컬에서 테스트하기 너무 높아서 수정하였습니다.)

- 워밍업 단계: 15 RPS × 30초
- 평균 근접 단계: 50 RPS × 60초
- 피크 검증 단계: 120 RPS × 60초
- 쿨다운 단계: 0 RPS × 30초

k6 설정 예시는 다음과 같습니다.
```javascript
stages: [
        { target: 15,  duration: '30s' },
        { target: 50,  duration: '60s' },
        { target: 120, duration: '60s' },
        { target: 0,   duration: '30s' },
      ]
```
### 4) 실행 방법
테스트 실행은 아래 명령어로 수행합니다.
```
k6 run --out influxdb=http://localhost:8086/k6 ./k6/order-scenario.js
```


## 결과
```
✗ detail 2xx
      ↳  99% — ✓ 2648 / ✗ 4
 ✗ coupons 2xx
      ↳  99% — ✓ 2650 / ✗ 2
 ✗ order 2xx
      ↳  97% — ✓ 2574 / ✗ 78
 ✓ order body shape

 checks..........................: 99.20%  10524 out of 10608
 data_received...................: 1.6 MB   8.6 kB/s
 data_sent.......................: 1.3 MB   6.8 kB/s
 dropped_iterations..............: 6422     33.90/s
 errors..........................: 100.00%  84 out of 84
 http_req_blocked................: avg=20.62µs  min=1µs    med=5µs    max=3.67ms  p(90)=9µs    p(95)=21µs
 http_req_connecting.............: avg=11.55µs  min=0s     med=0s     max=3.02ms  p(90)=0s     p(95)=0s
 ✗ http_req_duration.............: avg=3.34s    min=5.50ms med=3.77s  max=12.91s  p(90)=5.41s  p(95)=6.00s
   ✗ { endpoint:order_place }....: avg=3.48s    min=27.71ms med=3.91s max=12.91s  p(90)=5.66s  p(95)=6.39s
   ✗ { endpoint:product_detail }.: avg=3.26s    min=7.39ms  med=3.71s max=10.00s  p(90)=5.39s  p(95)=5.96s
   ✗ { endpoint:user_coupons }...: avg=3.27s    min=5.50ms  med=3.71s max=10.00s  p(90)=5.21s  p(95)=5.68s
 ✓ http_req_failed...............: 1.05%    84 out of 7956
 http_req_waiting................: avg=3.34s    min=5.46ms  med=3.77s  max=12.91s  p(90)=5.41s  p(95)=6.00s
 http_reqs.......................: 7956     41.9999/s
 iteration_duration..............: avg=10.17s   min=113ms   med=12.37s max=27.05s  p(90)=15.15s p(95)=15.61s
 iterations......................: 2652     13.9999/s
 vus.............................: 10       min=0          max=200
 vus_max.........................: 200      min=80         max=200

ERRO thresholds on metrics 'http_req_duration{endpoint:order_place}, http_req_duration{endpoint:product_detail}, http_req_duration{endpoint:user_coupons}' have been crossed
```

### 1) 전반 요약

- 총 요청 수: **7,956건 (여정 2,652회 × 3요청/회)**
- 평균 응답 시간(전체): **3.34초**
- p95 응답 시간(전체): **6.00초**
  - 상품 상세 p95: **5.96초 (목표 0.6초 초과)**
  - 사용자 쿠폰 p95: **5.68초 (목표 0.6초 초과)**
  - 주문/결제 p95: **6.39초 (목표 0.8초 초과)**
- 실패율(전체): **1.05% (84/7,956)**

현재 SLA(각 API p95, 실패율 0.2%) 대비 성능/안정성 목표 모두 미충족. 또한 VU 부족 경고와 높은 dropped_iterations로 목표 RPS(피크 120)에 도달하지 못하였습니다.

### 2) 주요 지표 분석응답 시간(전체)

- **응답 시간**
  - 평균: **3.34초**
  - p95: **6.00초**
  - 최소: **5.50ms**, 최대: **12.91초**
  - 엔드포인트별 p95
    - product_detail: **5.96초**
    - user_coupons: **5.68초**
    - order_place: **6.39초** </br>
  → 각 API의 p95가 목표치(600ms/800ms)를 7~10배 초과.

- **처리량**
  - 요청 처리율: **~42 req/s** (http_reqs 7,956건 / 총 소요시간 기준)
  - dropped_iterations: **6,422건**</br>
 → 응답 지연으로 목표 도착률을 유지하지 못해 드롭 발생.

- **VU 활용**
  - maxVUs=200 도달, Insufficient VUs 경고 발생
  - iteration_duration 평균 10.17초(p95 15.61초) </br>
  → 단일 여정이 길어 VU 확장 대비 처리율 증가가 제한적.

- **실패율**
  - 전체: **1.05%** (84/7,956)
  - 체크 실패: order 2xx **78건**, coupons 2xx **2건**, detail 2xx **4건**
---

### 3) 결론
- 성능 목표 미달: SLA(p95 < 600ms, 실패율 < 0.2%) 불충족
- 안정성 저하: 체크 실패율 증가, 주문 응답 지연 심각
- 핵심 병목: 주문 시 동시 다발적 리소스 경합(DB row lock, Redis 핫키, 커넥션 풀 한계)
- 개선 필요: DB/Redis 최적화, 테스트 단계적 확장으로 재검증 필요
