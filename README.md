# trip-return-notifier

해외여행에서 돌아온 사용자를 감지하고, 귀국 후 적절한 시점에 알림을 **정확히 한 번** 보내는 백엔드입니다.

문제 해결형 개인 프로젝트입니다. 기능을 많이 만드는 것보다, 실제 운영에서 생기는 문제(중복, 순서 뒤섞임, 유실, 피크)를 정의하고 해결한 과정을 수치로 남기는 것이 목표입니다.

## 왜 만드는가

여행 사진을 다루는 모바일 서비스에서 "여행이 끝난 사용자"는 가장 반응이 좋은 대상입니다. 하지만 사용자가 앱을 열 때까지 기다리면 이미 앱을 자주 쓰는 사람에게만 닿습니다. 그래서 기기가 보내는 신호로 여행과 귀국을 감지하고, 서버가 먼저 알림을 보냅니다.

## 전제와 제약

- **모바일 앱은 범위 밖입니다.** 기기 대신 [기기 시뮬레이터](#로드맵)가 이벤트를 보냅니다.
- 기기가 보내는 신호는 세 가지입니다.
  - 시간대 변경: 타임존 ID와 변경 시각
  - 통신망 국가 코드
  - 해외 사진 요약: 국가, 장수, 촬영 기간
- **좌표 원본은 보내지도, 저장하지도 않습니다.** 국가 코드와 날짜만 다룹니다.
- 해외에서 데이터를 꺼 둔 기기는 귀국 후 이벤트를 한꺼번에 보냅니다. 그래서 이벤트는 **중복될 수 있고, 순서가 뒤섞여 있고, 늦게 도착합니다.**
- 시차가 같은 나라(예: 한국과 일본, 둘 다 UTC+9)도 있으므로, 시차가 아니라 타임존 ID와 국가 코드로 판단합니다.

## 풀어야 할 문제

| # | 문제 | 무엇이 어려운가 |
|---|---|---|
| 1 | 이벤트 중복·순서 뒤섞임 | 재시도로 같은 이벤트가 여러 번 오고, 귀국 후 일괄 전송된 이벤트는 순서가 섞여 있습니다 |
| 2 | 알림은 정확히 한 번 | 워커가 여러 대이거나 발송 도중 죽어도 중복 발송이나 누락이 없어야 합니다 |
| 3 | 연휴 귀국 피크 | 연휴 직후 수십만 건의 알림이 같은 시각에 몰립니다 |
| 4 | 발송 규칙 | 야간(21~08시) 발송 금지, 1인당 빈도 제한, 수신 동의 철회를 지켜야 합니다 |
| 5 | 효과 측정 | 대조군을 두고 알림이 실제로 구매 전환을 늘렸는지 측정해야 합니다 |

## 목표 아키텍처

```text
[기기 시뮬레이터] ── POST /events ──▶ [API] ──▶ Postgres (이벤트 원본 저장)
                                          └──▶ Redis Stream
                                                    │
                                          [워커: 여행 판정] ──▶ 여행 상태 갱신
                                                    │  귀국이면 발송 예약
                                                    ▼
                                          Redis Sorted Set (발송 시각순 대기열)
                                                    │
                                          [워커: 발송] ──▶ 가짜 발송기 / FCM
```

현재는 빈 Spring Boot 프로젝트와 로컬 인프라만 있습니다. 나머지는 [이슈](https://github.com/passionryu/trip-return-notifier/issues)를 하나씩 처리하며 만들어 갑니다.

## 기술 스택

| 영역 | 선택 |
|---|---|
| 언어·프레임워크 | Kotlin, Spring Boot 4, Java 21 |
| DB | PostgreSQL 17 |
| 메시징·대기열·카운터 | Redis 8 (Stream, Sorted Set) |
| 로컬 환경 | Docker Compose |
| 테스트 | JUnit 5, Testcontainers, k6 |

**의도적으로 쓰지 않는 것**

| 기술 | 이유 |
|---|---|
| Kafka | 이 규모에서는 Redis Stream으로 충분하고, 운영할 구성 요소만 늘어납니다 |
| Kubernetes, MSA | API와 워커 두 프로세스면 충분합니다 |
| 분산 락 | 대부분 DB 제약 조건과 조건부 업데이트로 해결합니다 |

## 로컬 실행

필요한 것: Docker Desktop, JDK 21

```bash
docker compose up -d
```

```bash
./gradlew bootRun
```

```bash
curl localhost:8080/actuator/health
```

Postgres는 `localhost:5432`(DB·계정·비밀번호 모두 `trip`), Redis는 `localhost:6379`에 뜹니다. 포트가 겹치면 `POSTGRES_PORT`, `REDIS_PORT` 환경 변수로 바꿀 수 있습니다.

## 진행 방식

- 이슈 하나가 작업 하나입니다. 순서는 주 단위 마일스톤과 이슈 번호로 관리합니다.
- 브랜치는 이슈 단위로 만들고, PR 본문에 `Closes #이슈번호`를 적어 이슈를 닫습니다.
- 문제 해결 티켓을 끝내면 아래 [문제 해결 기록](#문제-해결-기록)에 한 줄을 추가합니다.
- 커밋, 브랜치, 머지, 코드 스타일 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)에 있습니다.

## 로드맵

| 주차 | 이슈 | 티켓 |
|---|---|---|
| 0주 · 준비 | #23 | [개발 규칙 및 자동화 초기 세팅](https://github.com/passionryu/trip-return-notifier/issues/23) |
| 1주 · 기반과 데이터 모델 | #1 | [로컬 개발 환경 구성과 앱 컨테이너화](https://github.com/passionryu/trip-return-notifier/issues/1) |
| | #2 | [CI 구축 (GitHub Actions)](https://github.com/passionryu/trip-return-notifier/issues/2) |
| | #3 | [도메인 정의: 이벤트·여행·판정 규칙 문서화](https://github.com/passionryu/trip-return-notifier/issues/3) |
| | #4 | [DB 스키마 설계 v1 (ERD)](https://github.com/passionryu/trip-return-notifier/issues/4) |
| | #5 | [DB 연결과 마이그레이션 도입](https://github.com/passionryu/trip-return-notifier/issues/5) |
| 2주 · 여행 판정과 비동기 처리 | #6 | [이벤트 수집 API](https://github.com/passionryu/trip-return-notifier/issues/6) |
| | #7 | [[문제 1] 이벤트 중복 저장 방지](https://github.com/passionryu/trip-return-notifier/issues/7) |
| | #8 | [여행 판정 로직과 시나리오 테스트](https://github.com/passionryu/trip-return-notifier/issues/8) |
| | #9 | [[문제 1] 늦게 도착한 이벤트와 순서 뒤섞임](https://github.com/passionryu/trip-return-notifier/issues/9) |
| | #10 | [기기 시뮬레이터](https://github.com/passionryu/trip-return-notifier/issues/10) |
| | #11 | [Testcontainers 통합 테스트 기반](https://github.com/passionryu/trip-return-notifier/issues/11) |
| | #12 | [Redis Stream으로 수집과 판정 분리](https://github.com/passionryu/trip-return-notifier/issues/12) |
| | #13 | [[문제] DB 저장과 Stream 발행 사이의 유실](https://github.com/passionryu/trip-return-notifier/issues/13) |
| 3주 · 예약 발송 | #14 | [귀국 시 발송 예약 생성](https://github.com/passionryu/trip-return-notifier/issues/14) |
| | #15 | [Redis Sorted Set 예약 발송 대기열과 발송 워커](https://github.com/passionryu/trip-return-notifier/issues/15) |
| | #16 | [[문제 2] 알림은 정확히 한 번만](https://github.com/passionryu/trip-return-notifier/issues/16) |
| | #17 | [[문제 4] 발송 규칙: 야간 금지·빈도 제한·수신 동의](https://github.com/passionryu/trip-return-notifier/issues/17) |
| | #18 | [[문제 5] 대조군 배정과 효과 측정](https://github.com/passionryu/trip-return-notifier/issues/18) |
| 4주 · 피크와 정리 | #19 | [[문제 3] 연휴 귀국 피크 재현](https://github.com/passionryu/trip-return-notifier/issues/19) |
| | #20 | [[문제 3] 피크 분산과 개선](https://github.com/passionryu/trip-return-notifier/issues/20) |
| | #21 | [(선택) 실제 푸시 발송과 모니터링](https://github.com/passionryu/trip-return-notifier/issues/21) |
| | #22 | [회고와 문제 해결 기록 정리](https://github.com/passionryu/trip-return-notifier/issues/22) |

## 문제 해결 기록

티켓을 끝낼 때마다 채웁니다.

| 문제 | 선택한 방법과 이유 | 측정 결과 | 티켓 |
|---|---|---|---|
| | | | |
