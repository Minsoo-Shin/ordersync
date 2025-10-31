# Git 커밋 메시지 컨벤션

이 문서는 이 프로젝트에서 사용하는 Git 커밋 메시지 컨벤션을 정의합니다.

## 기본 형식

```
<type>(<scope>): <description>
```

**예시:**
- `feat(kafka): String deserializer 및 String payload 리스너로 consumer 통일`
- `chore(repo): 모노레포 구조 생성 (apps/, libs/kotlin/common, contracts/proto, scripts, docs)`
- `infra: 단일 노드 Kafka (ZK) docker-compose 추가`

## 타입 (Type)

커밋의 성격을 나타냅니다. **영어로 작성**합니다.

- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **chore**: 빌드 과정 또는 보조 도구에 대한 변경사항 (설정, 리팩토링 등)
- **infra**: 인프라 관련 변경사항 (Docker, CI/CD, 배포 등)
- **docs**: 문서만 수정하는 경우
- **style**: 코드 포맷팅, 세미콜론 누락 등 (코드 변경이 없는 경우)
- **refactor**: 코드 리팩토링
- **test**: 테스트 코드 추가 또는 수정
- **perf**: 성능 개선

## 스코프 (Scope)

변경사항이 영향을 주는 범위를 나타냅니다. 선택사항이며, 타입 뒤에 괄호로 묶어서 작성합니다.

일반적으로 사용하는 스코프:
- **repo**: 저장소 전체 구조에 대한 변경
- **apps**: 애플리케이션 레벨 변경 (여러 앱에 영향을 주는 경우)
- **server**: 특정 서버 애플리케이션
- **config**: 설정 파일 변경
- **kafka**: Kafka 관련 변경
- **kotlin**: Kotlin 프로젝트 전반에 대한 변경

스코프가 명확하지 않거나 여러 영역에 걸치는 경우, 스코프를 생략할 수 있습니다.

## 설명 (Description)

변경사항을 간결하고 명확하게 설명합니다. **한국어로 작성**합니다.

### 작성 규칙

1. **간결성**: 50자 이내로 간결하게 작성
2. **명확성**: 무엇을 변경했는지 명확하게 표현
3. **명령형**: "추가", "수정", "제거" 등의 명령형으로 작성
4. **현재형 사용**: "추가했습니다" 보다는 "추가" 사용

**좋은 예시:**
- `feat(kafka): String deserializer 및 String payload 리스너로 consumer 통일`
- `chore(config): kafka bootstrap을 localhost:9094로 설정하고 server.port=8000으로 설정`

**나쁜 예시:**
- `feat: kafka consumer 수정` (너무 모호함)
- `fix(kafka): 버그 수정` (무엇을 수정했는지 불명확)
- `feat(kafka): String deserializer 및 String payload 리스너로 consumer 통일했습니다` (불필요한 존댓말)

## 본문 (Body)

필요한 경우, 커밋 메시지 본문에 추가 설명을 작성할 수 있습니다. 본문은 빈 줄로 구분합니다.

```
<type>(<scope>): <description>

- 구체적인 변경사항 1
- 구체적인 변경사항 2
- 구체적인 변경사항 3
```

**예시:**
```
feat(device-bridge): device-bridge-kotlin 프로젝트 초기 설정 및 데이터소스 설정 추가

- device-bridge-kotlin 프로젝트 구조 및 Gradle 설정 추가
- DeviceBridgeApplication 및 테스트 클래스 추가
- H2 데이터베이스 의존성 및 설정 추가 (JPA 사용을 위한 필수 설정)
- order-core-kotlin 설정 정리 (.gitattributes 제거, settings.gradle 업데이트)
```

## 요약

- **타입과 스코프는 영어**로 작성
- **설명은 한국어**로 작성
- 형식: `<type>(<scope>): <description>`
- 간결하고 명확하게 작성
- 본문에 여러 변경사항이 있을 경우 불릿 포인트로 나열
