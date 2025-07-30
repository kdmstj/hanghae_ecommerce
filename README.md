## Getting Started

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```

## 아키텍처
본 프로젝트는 **Layered Architecture** 를 기반으로 설계되었으며, 각 계층은 다음과 같은 책임을 가지고 있습니다.

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
- 비즈니스 규칙에 따라 상태를 변경하며, 스스로 유효성을 검증하는 책임을 가집니다.
- 주요 구성: `Entity`, `Enum`, `Repository`
