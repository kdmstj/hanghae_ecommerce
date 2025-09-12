# 선착순 쿠폰 발급 성능 테스트

## 계획

상품 목록 조회는 특정 시간대에 트래픽이 급격하게 증가하므로 피크 테스트가 적합하다고 판단하였습니다.

### 1) 목표(SLA)
- 응답 지연 시간 :  **p95 < 600ms**
- 실패율 : **실패율 < 0.2%**

### 2) 부하 모델
본 테스트는 **ramping-arrival-rate** 방식을 적용하여 RPS 를 목표로 설정하였습니다.
- 방문 평균 RPS: **46 RPS**
- 피크 RPS: **120 RPS** (로컬 환경에 따라 줄여서 돌렸습니다.)
- 테스트 대상: `PUT /api/v1/users/{userId}/coupons/{couponId}/issue`
- 데이터 전제
  - 쿠폰 1종 : couponId = 100
  - 총 수량: 1,465장

### 3) 스테이지 구성
부하를 점진적으로 증가시킨 후 피크 구간을 검증하고 종료하는 시나리오로 구성하였습니다.
- 워밍업: **10 RPS × 1분** 
- 평균 근접: **46 RPS × 2분**
- 피크 검증: **120 RPS × 2분**
- 쿨다운: **0 RPS × 1분**

k6 설정 예시는 다음과 같습니다.
```javascript
stages: [
  { target: 10,  duration: '1m'  }
  { target: 46, duration: '2m'  },
  { target: 120, duration: '2m'  }, 
  { target: 0,   duration: '1m'  },
]
```
### 4) 실행 방법
테스트 실행은 아래 명령어로 수행합니다.
```
k6 run --out influxdb=http://localhost:8086/k6 ./k6/issue-coupon.js
```

## 결과
```
 ✓ status 200

     checks.........................: 100.00% 17219 out of 17219
     data_received..................: 2.7 MB  7.4 kB/s
     data_sent......................: 2.3 MB  6.4 kB/s
     http_req_blocked...............: avg=14.95µs  min=2µs     med=5µs      max=4.05ms   p(90)=9µs      p(95)=14µs    
     http_req_connecting............: avg=7.74µs   min=0s      med=0s       max=3.07ms   p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=550.85µs min=264µs   med=463µs    max=78.43ms  p(90)=692µs    p(95)=851µs   
     ✓ { ep:coupon_issue }..........: avg=550.85µs min=264µs   med=463µs    max=78.43ms  p(90)=692µs    p(95)=851µs   
       { expected_response:true }...: avg=550.85µs min=264µs   med=463µs    max=78.43ms  p(90)=692µs    p(95)=851µs   
   ✓ http_req_failed................: 0.00%   0 out of 17219
     http_req_receiving.............: avg=53.54µs  min=15µs    med=37µs     max=25.02ms  p(90)=86µs     p(95)=104µs   
     http_req_sending...............: avg=19.62µs  min=6µs     med=16µs     max=3.18ms   p(90)=25µs     p(95)=32µs    
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=477.68µs min=222µs   med=401µs    max=72.2ms   p(90)=597µs    p(95)=741µs   
     http_reqs......................: 17219   47.83111/s
     iteration_duration.............: avg=300.53ms min=100.9ms med=301.75ms max=502.39ms p(90)=459.44ms p(95)=480.04ms
     iterations.....................: 17219   47.83111/s
     vus............................: 0       min=0              max=40 
     vus_max........................: 400     min=400            max=400


running (6m00.0s), 0000/0400 VUs, 17219 complete and 0 interrupted iterations
issue_coupon ✓ [======================================] 0000/0400 VUs  6m0s  001.00 iters/s
```

### 1) 전반 요약
- 총 요청 수: **17,219건**
- 평균 응답 시간: **0.551ms**
- 95퍼센타일(p95) 응답 시간: **0.851ms**
- 실패율: 0.00%

현재 SLA(p95 < 600ms, 실패율 < 0.2%) 대비 성능 목표를 크게 초과하였으며, 안정성 목표도 충족하지 못함.

### 2) 주요 지표 분석
- 응답 시간
  - 평균: 0.551ms
  - p95: 0.851ms
  - 최소: 0.264ms
  - 최대: 78.43ms</br>
→ 목표 SLA(600ms) 대비 여유 충분.

- 처리량
  - 초당 요청 수: ~47.83 req/s
  - http_req_waiting p95: 0.741ms (서버 대기 지연도 낮음)

- VU 활용
  - 활성 VU 최대: 40
  - iteration_duration 평균: 300.53ms (p95 480.04ms)
→ VU/리소스 여유가 커서 더 높은 RPS로 확장 여지 있음.
  
---

### 3) 결론
- 성능 목표 달성: p95와 실패율 모두 SLA 이내로 달성하였습니다.
- 안정성 확보: 실패율은 거의 0%로 기능적 오류는 발생하지 않았습니다.
