## 인기 상품 조회 캐시
### 1. 문제 분석
1. 문제 분석
- 인기 상품 조회 API는 매 요청마다 `product_daily_sales` 테이블을 조회합니다.
- 피크 트래픽 시 DB connection pool 이 고갈되면 응답 지연 증가 위험이 존재합니다.
- 해당 데이터는 하루 동안만 변동되며, 변경 시점을 예측할 수 있습니다.
- 이러한 특성으로 인해 캐시를 도입하면 DB 부하 절감과 응답 속도 개선 효과를 기대할 수 있습니다.

### 2. 해결 방안
- 캐싱 방식 : Cache Aside 패턴
    - 첫 요청 시 캐시 미스(Cache Miss) -> DB 조회 후 캐시에 저장.
    - 이후 요청 시 캐시 히트(Cache Hit) -> DB 조회 없이 캐시 데이터 반환.
- 캐시 스토리지 : Redis
- TTL : 1일(Duration.ofDays(1))
- Key : cache:best-products
- 적용 방식: Spring Cache 의 @Cacheable 어노테이션 사용
```
@Cacheable(value = "cache:best-products", key = "'cache:best-products'")
public List<BestProductResult> getBest() {
    return productDailySalesRepository.findTop5BestProducts(LocalDate.now().minusDays(3))
            .stream()
            .map(BestProductResult::fromProjection)
            .toList();
}
```

### 3. 테스트 결과
#### 3.1 테스트 시나리오
1. 동일한 API 를 연속 2회 호출
2. 첫번째 요청 -> Cache Miss -> DB 조회 쿼리 발생
3. 두번째 요청 -> Cache Hit -> DB 쿼리 미발생

#### 3.2 로그 분석
- 1차 호출
  - Redis GET -> MISS
  - DB SELECT 발생
  - Redis SET -> TTL 1일 설정
- 2차 호출
  - Redis GET -> HIT
  - DB SELECT 미발생

### 4. 결론
- 하루 주기 변동이라는 도메인 특성에 맞춰 Cache Aside + 1일 TTL을 적용, DB 부하를 줄이고 응답 시간을 개선하였습니다.
- 예상 가능한 값이기 때문에 추후에 스케줄러를 도입하여 Cache Warming 을 도입할 수 있을 것 같습니다.
