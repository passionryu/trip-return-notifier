# trip-return-notifier

해외여행에서 돌아온 사용자를 감지하고 귀국 후 알림을 정확히 한 번 보내는 백엔드. 배경과 문제 정의는 [README.md](README.md).

## 규칙

@CONTRIBUTING.md

## 작업할 때

- 이슈 단위로 브랜치를 만들고, 커밋 메시지는 위 규칙(타입 + 한국어 설명 + `(#이슈번호)`)을 따른다.
- 커밋 전에 `./gradlew ktlintFormat`을 실행하고 `./gradlew check`가 통과하는지 확인한다.
- 문제 해결 티켓(`problem` 라벨)은 수치 측정이 핵심이다. 측정 방법과 결과를 PR 본문과 README 문제 해결 기록에 남긴다.
- README의 "의도적으로 쓰지 않는 것"(Kafka, Kubernetes, 분산 락 등)을 도입하지 않는다.
