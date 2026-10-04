# 개발 규칙

사람이 기억해야 하는 규칙은 최소로 두고, 나머지는 도구가 검사합니다.

## 작업 흐름

1. 이슈를 하나 고른다. 이슈 하나가 작업 하나다.
2. `main`에서 이슈 브랜치를 만든다.
3. 작은 단위로 커밋한다.
4. 머지 전에 fixup 커밋을 정리하고 `main` 위로 rebase한다.
5. PR을 올리고 본문에 `Closes #이슈번호`를 적는다. CodeRabbit이 리뷰를 남긴다.
6. **Rebase merge**로 머지한다. 머지된 브랜치는 자동으로 삭제된다.

## 이슈와 PR 제목

- 작업 일차(`Day 01` 같은 표기)는 쓰지 않는다. 매일 하는 작업이 아니라서, 순서는 주 단위 마일스톤과 이슈 번호로 관리한다.
- PR 제목은 이슈 제목과 같게 쓴다. 노션 개발 일지 항목 제목도 PR 제목과 같다.

## 브랜치

`<타입>/<이슈번호>-<짧은-설명>` 형식을 쓴다. 타입은 커밋 타입과 같다.

```text
feat/6-event-api
chore/23-conventions
```

## 커밋 메시지

[Conventional Commits](https://www.conventionalcommits.org/ko/v1.0.0/) 타입을 쓰고, 설명은 한국어로 쓴다. 끝에 이슈 번호를 붙인다.

```text
<타입>: <무엇을 했는지> (#이슈번호)

<필요하면 본문: 왜 이렇게 했는지>
```

```text
feat: 이벤트 수신 API 추가 (#6)
fix: 같은 이벤트가 동시에 오면 두 번 저장되는 문제 수정 (#7)
test: 동시 요청 100개 중복 저장 테스트 추가 (#7)
```

| 타입 | 쓰는 경우 |
|---|---|
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 구조 개선 |
| `perf` | 성능 개선 |
| `test` | 테스트 추가·수정 |
| `docs` | 문서 |
| `build` | 빌드 설정, 의존성 |
| `ci` | CI 설정 |
| `chore` | 그 밖의 잡무 (규칙, 설정 파일 등) |

- 커밋 하나는 하나의 의도만 담는다. 커밋마다 `./gradlew check`가 통과하도록 한다.
- 설명은 "~ 추가", "~ 수정"처럼 명사형으로 끝낸다. 마침표는 찍지 않는다.

## fixup 커밋 정리

Rebase merge는 커밋을 그대로 `main`에 남기므로, "오타 수정"이나 "리뷰 반영" 같은 커밋은 머지 전에 원래 커밋에 합친다.

```bash
# 고칠 대상 커밋을 지정해 fixup 커밋을 만든다
git commit --fixup=<대상 커밋 해시>

# 머지 전에 한 번에 합친다
git fetch origin
git rebase -i --autosquash origin/main
git push --force-with-lease
```

## 코드 스타일

- `.editorconfig`와 [ktlint](https://pinterest.github.io/ktlint/)(`ktlint_official` 스타일)를 따른다.
- 커밋 전에 `./gradlew ktlintFormat`으로 자동 정렬한다.
- `./gradlew check`에 포맷 검사가 포함되어 있다. 어긋나면 빌드가 실패한다.
- 패키지 구조, 예외 처리, 테스트 규칙은 코드가 생기는 시점에 해당 이슈에서 정하고 이 문서에 추가한다.

## 코드 리뷰

- CodeRabbit이 PR마다 한국어로 리뷰한다. 설정은 [`.coderabbit.yaml`](.coderabbit.yaml)에 있다.
- 지적을 모두 받아들일 필요는 없다. 따르지 않을 때는 이유를 답글로 남긴다.
