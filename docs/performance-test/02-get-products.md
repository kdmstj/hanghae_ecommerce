# 상품 목록 조회 성능 테스트

## 계획

상품 목록 조회는 시간대와 무관하게 지속적으로 발생하는 read-heavy 트래픽으로 서비스 성능을 검증하기 위해 부하 테스트가 적합하다고 판단하였습니다.

### 1) 목표(SLA)
- 응답 지연 시간 :  **p95 < 600ms** 
- 실패율 : **실패율 < 0.1%**

### 2) 부하 모델
본 테스트는 **ramping-arrival-rate** 방식을 적용하여 RPS 를 목표로 설정하였습니다.
- 방문 평균 RPS: **46 RPS**
- 피크 RPS: **460 RPS**
- 테스트 대상: `GET /api/v1/products`
- 상품은 총 1,000개 데이터를 대상으로 테스트하였습니다.

### 3) 스테이지 구성
부하를 점진적으로 증가시킨 후 피크 구간을 검증하고 종료하는 시나리오로 구성하였습니다.
- 워밍업 단계: **40 RPS × 1분**
- 평균 근접 단계: **46 RPS × 2분**
- 피크 검증 단계: **460 RPS × 2분**
- 쿨다운 단계: **0 RPS × 1분**

k6 설정 예시는 다음과 같습니다.
```javascript
stages: [
  { target: 40, duration: '1m'  },
  { target: 46, duration: '2m'  },
  { target: 460, duration: '2m'  },
  { target:   0, duration: '1m'  },
]
```
### 4) 실행 방법
테스트 실행은 아래 명령어로 수행합니다.
```
k6 run --out influxdb=http://localhost:8086/k6 ./k6/get-products.js
```

### 5) 관측 포인트
테스트 수행 시 다음 항목을 중점적으로 모니터링합니다.
- API 지표: RPS, p95 및 p99 응답 지연 시간, 실패율, 타임아웃 발생 여부
- DB 지표: 1초 이상 소요되는 슬로우 쿼리 발생 여부, 실행 계획 변화, 인덱스 사용률, 락 대기 상황
- JVM 및 풀 지표: 스레드풀 큐 길이, DB 커넥션풀 대기 시간, 메모리 사용량

## 결과
```
 execution: local
        script: ./k6/get-products.js
        output: InfluxDBv1 (http://localhost:8086)

     scenarios: (100.00%) 1 scenario, 1200 max VUs, 6m30s max duration (incl. graceful stop):
              * get_products: Up to 460.00 iterations/s for 6m0s over 4 stages (maxVUs: 400-1200, gracefulStop: 30s)

WARN[0218] Insufficient VUs, reached 1200 active VUs and cannot initialize more  executor=ramping-arrival-rate scenario=get_products

     ✗ status 200
      ↳  99% — ✓ 15892 / ✗ 4

     checks.........................: 99.97%  15892 out of 15896
     data_received..................: 1.2 GB  3.1 MB/s
     data_sent......................: 1.5 MB  4.0 kB/s
     dropped_iterations.............: 34623   91.709952/s
     errors.........................: 100.00% 4 out of 4
     http_req_blocked...............: avg=35.84µs  min=2µs      med=6µs   max=13.04ms p(90)=19µs   p(95)=311µs 
     http_req_connecting............: avg=24.05µs  min=0s       med=0s    max=12.93ms p(90)=0s     p(95)=261µs 
     http_req_duration..............: avg=12s      min=13.01ms  med=7.3s  max=39.35s  p(90)=32.65s p(95)=37.71s
     ✗ { ep:products_list }.........: avg=12s      min=13.01ms  med=7.3s  max=39.35s  p(90)=32.65s p(95)=37.71s
       { expected_response:true }...: avg=12s      min=13.01ms  med=7.3s  max=39.35s  p(90)=32.65s p(95)=37.71s
   ✓ http_req_failed................: 0.02%   4 out of 15896
     http_req_receiving.............: avg=448.77µs min=67µs     med=382µs max=25.94ms p(90)=638µs  p(95)=770µs 
     http_req_sending...............: avg=29.91µs  min=5µs      med=20µs  max=5.46ms  p(90)=42.5µs p(95)=59µs  
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s    max=0s      p(90)=0s     p(95)=0s    
     http_req_waiting...............: avg=12s      min=12.8ms   med=7.3s  max=39.35s  p(90)=32.65s p(95)=37.71s
     http_reqs......................: 15896   42.105577/s
     iteration_duration.............: avg=12.3s    min=114.54ms med=7.65s max=39.79s  p(90)=32.99s p(95)=37.99s
     iterations.....................: 15896   42.105577/s
     vus............................: 18      min=0              max=1200
     vus_max........................: 1200    min=400            max=1200


running (6m17.5s), 0000/1200 VUs, 15896 complete and 0 interrupted iterations
get_products ✓ [======================================] 0000/1200 VUs  6m0s  001.96 iters/s
ERRO[0377] thresholds on metrics 'http_req_duration{ep:products_list}' have been crossed

```

### 1) 전반 요약
- 총 요청 수: **15,896건**
- 평균 응답 시간: **12.0초**
- 95퍼센타일(p95) 응답 시간: **37.7초**
- 실패율: **0.02% (4건/15,896건)**

현재 SLA(p95 < 600ms, 실패율 < 0.1%) 대비 응답 지연 목표를 크게 초과하였으며, 성능 목표를 충족하지 못했습니다.
다만 기능적 안정성 측면에서는 요청 대부분이 성공(99.97%)했고, 오류는 소수였습니다.

### 2) 주요 지표 분석
- **응답 시간**
    - 평균: **12.0초**
    - p95: **37.7초**
    - 최소: **13ms**
    - 최대: **39.35초**  
      → 목표 SLA(600ms)를 약 60배 초과하였습니다.

- **처리량**
    - 초당 요청 수: **42 req/s**
    - dropped_iterations: **34,623건** 발생  
      → 응답 지연으로 인해 k6가 목표 RPS(피크 460RPS)를 달성하지 못하고 요청을 드롭하였습니다.

- **VU 활용**
    - 최대 **1,200 VU**까지 증가
    - 평균 iteration_duration: **12.3초**  
      → VU 확장이 충분히 이루어졌으나, 서버/DB의 응답 지연으로 인해 처리량이 증가하지 않았습니다.

---

### 3) 원인 추정
1. 쿼리 병목: 페이지네이션을 사용하지 않아 한 번의 요청으로 대량(1,000건)을 조회하고 있으며, 이로 인해 응답 시간이 비례적으로 증가하였습니다.
2. 커넥션 풀 부족 가능성: DB 커넥션 풀 제한으로 응답 지연이 발생했을 가능성이 있습니다.
3. 캐시 미사용: 모든 요청이 DB를 직접 타격하여 과부하 시 병목이 심화되었습니다.

---

### 4) 개선 사항
- 쿼리 및 API 최적화
  - 현재 한 번의 호출로 상품 전체를 조회하고 있어 병목이 발생하고 있습니다. 실제 서비스에서는 사용자가 한 번에 모든 상품을 조회하지 않으므로 페이지 단위 조회(페이지네이션) 를 적용하는 것이 적절합니다.
  - LIMIT/OFFSET 또는pagination을 도입하여 단일 요청 데이터 크기를 축소하면 응답 시간이 크게 단축될 수 있습니다.
- 캐싱 적용
  - 조회 결과를 TTL 기반 캐시로 관리하여 응답 시간을 단축 시킬 수 있습니다.
- DB 및 시스템 리소스 확장
  - 커넥션 풀 사이즈를 조정하고, 필요 시 읽기 전용 DB(Read Replica) 를 도입하여 조회 부하를 분산시킬 수 있습니다.
- 테스트 전략 보완
  - 로컬 환경에서는 600 RPS가 과부하일 수 있으므로 50~100 RPS부터 점진적으로 증가시키며 병목 구간을 탐색하는 방식으로 개선할 수 있습니다.
---

### 5) 결론
- 성능 목표 미달: SLA(p95 < 600ms)를 충족하지 못하였습니다.
- 안정성 확보: 실패율은 0.02%로 대부분 정상 처리합니다.
- 병목 구간 발견: DB 쿼리 성능 및 캐시 미비로 인한 응답 지연이 주요 원인으로 확인되었습니다. 따라서 캐시 적용 및 쿼리 최적화를 통해 성능 개선할 예정합니다.
