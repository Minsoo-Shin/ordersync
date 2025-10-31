# 아키텍처 구조

이 문서는 Order Sync 프로젝트의 아키텍처 구조를 설명합니다.

## 프로젝트 구조

```
order-sync-pjt/
├── apps/                          # 애플리케이션 레벨 코드
│   ├── order-core-kotlin/         # 주문 관리 서버
│   └── device-bridge-kotlin/      # 디바이스 브릿지 서버
├── libs/                          # 공통 라이브러리
│   └── kotlin/
│       └── common/                # 공통 코드 (Kafka 설정 등)
├── contracts/                     # 프로토콜 정의
│   └── proto/                     # Protocol Buffers
└── docs/                          # 문서
```

## 애플리케이션 레이어 구조 (order-core-kotlin)

각 애플리케이션은 Clean Architecture 원칙에 따라 다음과 같은 계층 구조를 가집니다:

```
com.example.ordersync/
├── domain/                        # 도메인 레이어 (비즈니스 로직)
│   └── order/
│       ├── Order.kt              # 엔티티
│       ├── OrderItem.kt          # 엔티티
│       └── OrderRepository.kt    # 리포지토리 인터페이스
│
├── application/                   # 애플리케이션 레이어 (유스케이스)
│   └── order/
│       ├── CreateOrderUseCase.kt
│       └── GetOrderUseCase.kt
│
├── infra/                         # 인프라 레이어 (외부 시스템 통신)
│   ├── persistence/              # 영속성 관련 (데이터베이스)
│   │   └── order/
│   │       ├── JpaOrderEntity.kt
│   │       ├── JpaOrderItemEntity.kt
│   │       ├── OrderMapper.kt
│   │       ├── OrderRepositoryAdapter.kt
│   │       └── SpringDataOrderJpaRepository.kt
│   │
│   └── messaging/                 # 메시징 관련
│       └── kafka/
│           └── OrderConsumer.kt
│
├── presentation/                  # 프레젠테이션 레이어 (API)
│   └── order/
│       ├── OrderController.kt
│       └── dto/
│           └── OrderDtos.kt
│
└── OrdersyncApplication.kt        # 애플리케이션 진입점
```

## 레이어 설명

### Domain Layer (도메인 레이어)

- **역할**: 비즈니스 로직과 도메인 규칙을 포함
- **특징**:
  - 프레임워크 독립적
  - 엔티티와 도메인 서비스 포함
  - 리포지토리 인터페이스 정의 (구현은 infra에)

### Application Layer (애플리케이션 레이어)

- **역할**: 유스케이스 구현 (비즈니스 흐름 조율)
- **특징**:
  - 도메인 레이어의 인터페이스를 사용
  - 트랜잭션 경계 설정
  - 각 유스케이스는 단일 책임을 가짐

### Infrastructure Layer (인프라 레이어)

외부 시스템과의 통신을 담당하며, 다음과 같이 구분됩니다:

#### `infra/persistence/`

- **역할**: 데이터 영속성 관련 구현
- **포함 내용**:
  - JPA Entity 클래스
  - Repository 구현 (Spring Data JPA 어댑터)
  - Domain ↔ Entity 변환 (Mapper)

#### `infra/messaging/`

- **역할**: 메시징 시스템 통신
- **포함 내용**:
  - Kafka Consumer/Producer
  - 메시지 처리 로직
  - 향후 RabbitMQ, Redis Pub/Sub 등 확장 가능

### Presentation Layer (프레젠테이션 레이어)

- **역할**: 외부와의 인터페이스 (REST API 등)
- **특징**:
  - HTTP 요청/응답 처리
  - DTO 변환
  - 입력 검증

## 공통 라이브러리 (libs/kotlin/common)

여러 애플리케이션에서 공유되는 코드를 포함합니다:

```
libs/kotlin/common/
└── src/main/kotlin/com/example/ordersync/
    └── kafka/
        └── KafkaConfig.kt        # Kafka 설정 클래스 (공통)
```

### 특징

- **KafkaConfig**: Spring Boot 자동 설정을 사용하여 각 애플리케이션의 `application.properties`에서 설정을 읽어옴
- 각 애플리케이션은 자신만의 `application.properties`에서 Kafka 설정 관리
- 비즈니스 로직이 포함된 Consumer는 각 애플리케이션에 위치 (`infra/messaging/kafka/`)

## 의존성 방향

```
presentation → application → domain
     ↓              ↓
   infra ──────────┘
```

- **도메인 레이어**는 어떤 외부 의존성도 가지지 않음
- **애플리케이션 레이어**는 도메인 레이어만 의존
- **인프라 레이어**는 도메인 레이어를 구현
- **프레젠테이션 레이어**는 애플리케이션 레이어를 호출

## 참고 사항

- 각 레이어는 명확한 책임을 가집니다
- 도메인 로직은 도메인 레이어에 집중되어 있어 테스트와 유지보수가 용이합니다
- 인프라 레이어의 변경이 도메인 로직에 영향을 주지 않도록 설계되었습니다

