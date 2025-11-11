# Order Sync Project

주문 동기화 시스템으로, 주문 생성부터 디바이스 전송까지의 전체 프로세스를 관리하는 마이크로서비스 아키텍처 기반 프로젝트입니다.

## 📋 목차

- [개요](#개요)
- [아키텍처](#아키텍처)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [사전 요구사항](#사전-요구사항)
- [로컬 개발 환경 설정](#로컬-개발-환경-설정)
- [실행 방법](#실행-방법)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [개발 가이드](#개발-가이드)

## 개요

Order Sync는 다음과 같은 흐름으로 동작합니다:

1. **주문 생성** (order-core): 주문을 생성하고 Kafka로 이벤트 발행
2. **디바이스 동기화** (device-bridge): Kafka 이벤트를 수신하여 MQTT로 디바이스에 전송
3. **동기화 검증**: SQS를 통한 재시도 및 상태 검증 메커니즘

## 아키텍처

```
┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│   order-core     │────────▶│      Kafka       │────────▶│  device-bridge   │
│   (Port 8000)    │         │   (Port 9094)    │         │   (Port 8001)    │
└──────────────────┘         └──────────────────┘         └────────┬─────────┘
                                                                    │
                                                                    │ (1) Receive Order Event
                                                                    │ (2) Send to Device via MQTT
                                                                    │ (3) Publish Check Event
                                                                    │
                    ┌───────────────────────────────────────────────┴────────────────────────┐
                    │                                                                         │
                    ▼                                                                         ▼
        ┌──────────────────────────┐                                  ┌──────────────────────────┐
        │         MQTT             │                                  │         SQS              │
        │      (Port 1883)         │                                  │     (LocalStack)         │
        │                          │                                  │                          │
        │  Send Order to Device    │                                  │  Sync Status Check &     │
        │                          │                                  │  Retry Mechanism         │
        └──────────────────────────┘                                  └──────────┬───────────────┘
                                                                                │
                                                                                │ (4) Receive Delayed Message
                                                                                │ (5) Check Status
                                                                                │
                                                                                ▼
                                                                    ┌──────────────────────────┐
                                                                    │    device-bridge         │
                                                                    │  (Status Verification)   │
                                                                    └──────────────────────────┘
```

### 서비스 구성

- **order-core-kotlin**: 주문 관리 서비스
  - 주문 생성 및 조회
  - Kafka Producer로 주문 이벤트 발행

- **device-bridge-kotlin**: 디바이스 브릿지 서비스
  - Kafka Consumer로 주문 이벤트 수신
  - MQTT를 통해 디바이스로 주문 전송
  - MQTT 전송 후 SQS에 검증 이벤트 발행 (지연 메시지)
  - SQS Consumer로 동기화 상태 검증 및 필요시 재시도

## 기술 스택

### Backend
- **Kotlin** 1.9.25
- **Spring Boot** 3.5.7
- **Spring Data JPA**
- **H2 Database** (개발 환경)

### 메시징
- **Apache Kafka** (이벤트 스트리밍)
- **MQTT** (디바이스 통신)
- **AWS SQS** (동기화 상태 검증 및 재시도, LocalStack 사용)

### 인프라
- **Docker Compose** (로컬 개발 환경)
- **Gradle** (빌드 도구)

## 프로젝트 구조

```
order-sync-pjt/
├── apps/                          # 애플리케이션 레벨 코드
│   ├── order-core-kotlin/         # 주문 관리 서버 (Port 8000)
│   └── device-bridge-kotlin/      # 디바이스 브릿지 서버 (Port 8001)
├── libs/                          # 공통 라이브러리
│   └── kotlin/
│       └── common/                # 공통 코드 (이벤트, SQS 인터페이스 등)
├── contracts/                     # 프로토콜 정의
│   └── proto/                     # Protocol Buffers
├── docker/                        # Docker 설정 파일
│   └── mosquitto/                # MQTT 브로커 설정
├── scripts/                       # 유틸리티 스크립트
│   └── create-sqs-queue.sh       # SQS 큐 생성 스크립트
└── docs/                          # 문서
    ├── architecture.md           # 아키텍처 문서
    └── commit-convention.md      # 커밋 컨벤션
```

### 레이어 구조 (Clean Architecture)

각 애플리케이션은 다음과 같은 계층 구조를 가집니다:

```
domain/          # 도메인 레이어 (비즈니스 로직)
application/     # 애플리케이션 레이어 (유스케이스)
infra/           # 인프라 레이어 (외부 시스템 통신)
presentation/    # 프레젠테이션 레이어 (API)
```

## 사전 요구사항

- **Java** 21 이상
- **Docker** 및 **Docker Compose**
- **Gradle** (또는 Gradle Wrapper 사용)

## 로컬 개발 환경 설정

### 1. 저장소 클론

```bash
git clone <repository-url>
cd order-sync-pjt
```

### 2. Docker Compose로 인프라 서비스 실행

```bash
docker-compose up -d
```

다음 서비스들이 실행됩니다:
- **Zookeeper** (Port 2181)
- **Kafka** (Port 9092, 9094)
- **Kafka UI** (Port 8080) - http://localhost:8080
- **MQTT (Mosquitto)** (Port 1883, 9001)
- **LocalStack** (Port 4566) - AWS SQS 에뮬레이션

### 3. SQS 큐 생성

```bash
chmod +x scripts/create-sqs-queue.sh
./scripts/create-sqs-queue.sh
```

### 4. 애플리케이션 빌드

```bash
# 전체 프로젝트 빌드
./gradlew build

# 또는 각 서비스별 빌드
cd apps/order-core-kotlin && ./gradlew build
cd apps/device-bridge-kotlin && ./gradlew build
```

## 실행 방법

### 1. order-core 서비스 실행

```bash
cd apps/order-core-kotlin
./gradlew bootRun
```

서비스가 `http://localhost:8000`에서 실행됩니다.

### 2. device-bridge 서비스 실행

```bash
cd apps/device-bridge-kotlin
./gradlew bootRun
```

서비스가 `http://localhost:8001`에서 실행됩니다.

### 3. 서비스 확인

- **order-core**: http://localhost:8000
- **device-bridge**: http://localhost:8001
- **Kafka UI**: http://localhost:8080
- **H2 Console (order-core)**: http://localhost:8000/h2-console
- **H2 Console (device-bridge)**: http://localhost:8001/h2-console

## 주요 기능

### 주문 생성 및 이벤트 발행
- 주문 생성 시 Kafka로 `OrderCreated` 이벤트 발행
- 이벤트는 `orders` 토픽으로 전송

### 디바이스 동기화
- Kafka에서 주문 이벤트 수신
- MQTT를 통해 디바이스로 주문 정보 전송
- 동기화 상태 추적 (PENDING, SENT, CONFIRMED, FAILED)

### 재시도 및 검증 메커니즘
- MQTT 전송 후 SQS에 검증 이벤트 발행 (지연 메시지)
- SQS Consumer가 지연된 메시지를 수신하여 동기화 상태 검증
- 동기화 실패 시 자동 재시도 (MQTT 재전송)
- 최대 재시도 횟수 및 시간 제한 설정

## API 엔드포인트

### order-core

#### 주문 생성
```http
POST /api/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 10000
    }
  ]
}
```

#### 주문 조회
```http
GET /api/orders/{orderId}
```

### device-bridge

#### 웹훅 (디바이스 확인)
```http
POST /webhook/confirm
Content-Type: application/json

{
  "orderId": 1
}
```

## 개발 가이드

### 커밋 컨벤션

프로젝트는 커밋 컨벤션을 따릅니다. 자세한 내용은 [docs/commit-convention.md](docs/commit-convention.md)를 참고하세요.

기본 형식:
```
<type>(<scope>): <description>
```

예시:
- `feat(kafka): String deserializer 및 String payload 리스너로 consumer 통일`
- `refactor(sqs): SQS Producer를 범용 인터페이스로 리팩토링`

### 아키텍처 문서

더 자세한 아키텍처 정보는 [docs/architecture.md](docs/architecture.md)를 참고하세요.

### 공통 라이브러리 사용

공통 라이브러리(`libs/kotlin/common`)에는 다음이 포함됩니다:
- **이벤트 정의**: `OrderEvent`, `OrderCreated`, `OrderSyncCheckEvent`
- **SQS 인터페이스**: `SqsMessagePublisher` (Domain/Application 계층용)

각 서비스의 Infra 계층에서 인터페이스를 구현합니다.

## 환경 변수

### order-core
- `server.port`: 서버 포트 (기본값: 8000)
- `spring.kafka.bootstrap-servers`: Kafka 브로커 주소 (기본값: localhost:9094)

### device-bridge
- `server.port`: 서버 포트 (기본값: 8001)
- `mqtt.broker.url`: MQTT 브로커 주소 (기본값: tcp://localhost:1883)
- `aws.sqs.order-sync-check-queue-url`: SQS 큐 URL

## 문제 해결

### Kafka 연결 오류
- Docker Compose가 정상적으로 실행되었는지 확인
- `localhost:9094`로 접근 가능한지 확인

### MQTT 연결 오류
- Mosquitto 컨테이너가 실행 중인지 확인
- `docker/mosquitto/config/mosquitto.conf` 설정 확인

### SQS 연결 오류
- LocalStack이 실행 중인지 확인
- `scripts/create-sqs-queue.sh`로 큐가 생성되었는지 확인

## 라이선스

이 프로젝트는 개인 프로젝트입니다.

