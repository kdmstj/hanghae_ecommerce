## 프로젝트 개요
- **도메인**: `e-커머스 상품 주문 서비스`
- **주제**: TDD, 서버 구축(프로젝트/DB 설계), 대용량 트래픽 및 데이터 처리, 장애 대응
- **목표**: 실제 이커머스 환경에서 발생할 수 있는 **동시성, 성능, 장애 대응**을 경험하며 안정적인 서버 아키텍처를 설계/구현합니다.

<br/>

## 요구사항 요약
| 영역 | 주요 기능 |
|---|---|
| 포인트 | 충전, 조회, 사용 |
| 쿠폰 | 선착순 발급, 보유 목록 조회 |
| 상품 | 목록/상세 조회, 실시간 인기 상품 조회 |
| 주문 | 재고 차감, 포인트 사용, 쿠폰 사용, 주문 처리 |

- **🔗 [상세 요구사항 분석 문서](./docs/requirement.md)**

<br/>


## 기술적 특징
- 테스트/품질 보증
  - TDD: 단위/통합 테스트 기준 수립 및 검증
  - Testcontainers로 DB/Redis/Kafka 자동 구동
- 아키텍처 설계
  - Layered Architecture 기반, 도메인 주도 설계 원칙 일부 적용
- DB 설계 및 최적화
  - 인덱싱 최적화(보유 쿠폰 목록 조회 등), 정규화/역정규화
- 동시성 제어
  - **DB 락**: 비즈니스 요구에 맞춰 낙관적/비관적 락으로 정합성 보장
  - **분산 락(Redis)**: DB 락을 대체하여 정합성과 성능을 확보
- 실시간 상품 조회 성능 최적화
  - Cache Warming으로 DB 부하 감소
  - Redis **Sorted Set**으로 판매량 랭킹 집계/캐싱
  - Kafka 이벤트로 판매량 집계 비동기 처리
- 선착순 쿠폰 발급 성능 최적화
  - Redis 분산 락과 원자 연산으로 1차 경합 차단
  - Kafka 토픽(키 기반 파티셔닝)으로 순차 처리하여 정합성과 처리량 확보
- 주문/결제 비동기 이벤트 아키텍처
  - Application Event로 트랜잭션 분리
  - Outbox + Kafka로 트랜잭션 일관성과 최종적 전달 보장
- 운영 안정성
  - 장애 **탐지 → 대응 → 복구 → 재발 방지** 프로세스 문서화
 
<br/>

## 문서 허브

### 1. 다이어그램
- **[ERD 문서](./docs/diagrams/erd.md)** — Mermaid 기반 데이터 모델과 테이블 관계
- **[시퀀스 다이어그램 문서](./docs/diagrams/sequence_diagram.md)** — 유스케이스별 흐름도

### 2. DB
- **[쿼리 최적화](./docs/query-optimization/)** — 인덱스/쿼리 플랜/튜닝 사례

### 3. 동시성 제어
- **[DB 락 문서](./docs/concurrency/db-locking.md)** — 낙관/비관 락 선택 기준과 사례
- **[분산 락 문서](./docs/concurrency/distributed-locking.md)** — Redis 기반 분산 락/원자 연산 전략

### 4. Redis
- **[Redis 캐싱 전략](./docs/redis/cache.md)** — 캐시 워밍/만료 전략으로 DB 부하 감소
- **[실시간 베스트 상품(Sorted Set)](./docs/redis/get-best-products.md)** — 실시간 랭킹 집계
- **[선착순 쿠폰 발급(Queue)](./docs/redis/issue-coupons.md)** — 분산 락·SADD로 1차 경합 차단

### 5. Kafka
- **[주문/결제 이벤트](./docs/kafka/02-order.md)** — Outbox 패턴 + Kafka로 최종적 전달 보장
- **[쿠폰 발급 이벤트](./docs/kafka/03-issue-coupons.md)** — 토픽 기반 순차 처리(정합성/처리량)

### 6. 성능 테스트
- **[성능 테스트 디렉터리](./docs/performance-test/)** — K6 + InfluxDB + Grafana로 부하 시나리오/지표/대시보드 구성

### 7. 장애 대응
- **[장애 대응 문서](./docs/error-report.md)** — 가상 장애 시나리오 기반 대응·복구·재발 방지

<br/>

## 아키텍처
본 프로젝트는 **Layered Architecture** 를 기반으로 설계되었으며, 각 계층은 다음과 같은 책임을 가지고 있습니다.
추상화를 통해서 의존성 역전을 위배하지 않도록 하였습니다. 이를 통해서 유지보수성을 높이는데 목적을 두었습니다.

### Presentation Layer
- 사용자의 요청(Request)을 수신하고 직렬화합니다.
    - 요청에 대한 유효성 검증을 수행합니다.
    - 요청을 Application Layer에서 사용할 수 있는 형태(Command 등)로 변환합니다.
- 처리 결과를 응답(Response) 객체로 역직렬화하여 반환합니다.
- 예외 발생 시 적절한 HTTP 상태 코드와 메시지를 제공합니다.
- 주요 구성: `Controller`, `Request/Response DTO`

### Application Layer
- 유스케이스를 조합하고 실행합니다.
- 트랜잭션 경계를 정의하고, 하나의 작업 흐름을 완성하는 책임을 가집니다.
- 외부 요청을 처리하기 위한 `Command`, 처리 결과를 전달하기 위한 `Result` 객체 등을 정의합니다.
- 외부 시스템 연동 순서, 조건 분기, 복수 도메인 간 협업 로직 등을 담당합니다.
- 주요 구성: `Facade`, `Service`, `Command`, `Result`

### Domain Layer
- 시스템의 핵심 비즈니스 규칙과 로직을 담는 계층입니다.
- 도메인의 상태를 표현하는 `Entity`, 도메인 규칙을 보조하는 `Enum`, 상태를 저장·조회하는 `Repository 인터페이스` 등을 포함합니다.

### Infra Layer
- 실제 인프라를 활용하는 경우 (Redis, Kafka 등) 구현체를 작성하기 위함입니다.
- 비즈니스 규칙에 따라 상태를 변경하며, 스스로 유효성을 검증하는 책임을 가집니다.
- 주요 구성: `Redis Repository`, `Kafka Producer/Consumer`

<br/>

## 프로젝트 블로그
프로젝트를 진행 중 작성한 회고입니다.
- **🔗 [회고 블로그](https://kangkangsulae.tistory.com/category/%ED%9A%8C%EA%B3%A0%20%EB%B0%8F%20%ED%9B%84%EA%B8%B0/%ED%95%AD%ED%95%B4%20%ED%94%8C%EB%9F%AC%EC%8A%A4%20%EB%B0%B1%EC%97%94%EB%93%9C)**

