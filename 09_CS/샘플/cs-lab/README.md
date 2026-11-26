# cs-lab — CS 개념을 직접 재현해 보는 실습 코드

스프링 없이 **자바 표준 API만으로** 만든 실습 10개입니다. Gradle 빌드 없이 Java 11+ 의 "소스 파일 바로 실행" 기능으로 실행합니다.
모든 결과는 문서에 **실측값**으로 실려 있습니다(2026-11, Windows 11, Java 17.0.12). 시간(ms)은 PC마다 다르니 경향만 보세요.

| 파일 | 무엇을 재현하나 | 문서 |
|---|---|---|
| `CacheLab.java` | 같은 계산인데 메모리 읽는 순서만 바꿔 약 28배 차이 (캐시 지역성) | 01 |
| `NumberLab.java` | int 오버플로, `0.1+0.2`, `BigDecimal` 생성 함정, `Integer` 128 `==` 비교 | 02 |
| `EncodingLab.java` | UTF-8 vs MS949 바이트, 한글 깨짐 재현(`ì•ˆ`, `�`, `占쏙옙`), BOM | 02 |
| `ThreadPoolLab.java` | 스레드 풀이 부족하면 대기 시간이 쌓여 응답이 느려짐 (톰캣 흉내) | 03 |
| `RaceConditionLab.java` | `count++` 레이스 컨디션, `volatile` 은 해결책이 아님, `synchronized`·`AtomicInteger` | 04 |
| `SingletonStateBug.java` | 싱글톤 빈 필드에 요청 데이터를 저장하면 남의 이름이 응답됨 | 04 |
| `DeadlockLab.java` | 반대 순서 잠금으로 데드락 → 자동 감지 후 종료 / `fixed` 로 해결 버전 | 04 |
| `MemoryLab.java` | `StackOverflowError`, `OutOfMemoryError`, 문자열 `==` vs `equals` | 05 |
| `LostUpdateLab.java` | 트랜잭션을 써도 생기는 갱신 분실과 해결 2가지 (H2 필요) | 06 |
| `IndexLab.java` | 30만 건에서 인덱스 유무·실행 계획, 인덱스를 못 쓰는 패턴 (H2 필요) | 06 |

## 실행 (PowerShell, 이 폴더에서)

```powershell
java "-Dfile.encoding=UTF-8" CacheLab.java
java "-Dfile.encoding=UTF-8" NumberLab.java
java "-Dfile.encoding=UTF-8" EncodingLab.java
java "-Dfile.encoding=UTF-8" ThreadPoolLab.java          # 스레드 2개
java "-Dfile.encoding=UTF-8" ThreadPoolLab.java 6        # 스레드 6개로 비교
java "-Dfile.encoding=UTF-8" RaceConditionLab.java       # 스레드 4개 × 10만 번
java "-Dfile.encoding=UTF-8" RaceConditionLab.java 1 100000
java "-Dfile.encoding=UTF-8" SingletonStateBug.java
java "-Dfile.encoding=UTF-8" DeadlockLab.java
java "-Dfile.encoding=UTF-8" DeadlockLab.java fixed
java "-Dfile.encoding=UTF-8" MemoryLab.java                       # stack + string
java "-Xmx64m" "-Dfile.encoding=UTF-8" MemoryLab.java heap        # 반드시 -Xmx 로 힙 제한

# H2 드라이버가 필요한 실습 (03_Database 모듈의 jar 사용, 인메모리 DB라 파일이 남지 않음)
java -cp "../../../03_Database/샘플/jdbc/lib/h2-2.3.232.jar" "-Dfile.encoding=UTF-8" LostUpdateLab.java
java -cp "../../../03_Database/샘플/jdbc/lib/h2-2.3.232.jar" "-Dfile.encoding=UTF-8" IndexLab.java
```

- macOS·리눅스는 `"-Dfile.encoding=UTF-8"` 따옴표 없이 써도 되고, Java 18 이상이면 아예 빼도 한글이 깨지지 않습니다.
- IntelliJ에서 파일을 열고 `main` 옆 ▶ 버튼으로 실행해도 됩니다. H2 실습은 프로젝트 라이브러리에 위 jar를 추가해야 합니다.

## 주의

- **`"-Dfile.encoding=UTF-8"` 은 따옴표째** 붙입니다.
  - 빼면 한글 Windows + Java 17에서 소스를 MS949로 읽어 **한글이 깨집니다.** (일부러 빼고 실행해 보는 것이 02 문서의 실습입니다)
  - 따옴표가 없으면 PowerShell 5.1이 인자를 `.` 에서 잘라 `Could not find or load main class .encoding=UTF-8` 이 납니다.
- `MemoryLab heap` 은 힙을 제한하지 않으면 스스로 멈추고 안내합니다. 제한 없이 강제로 돌리면 PC 메모리의 1/4까지 써서 PC가 느려질 수 있습니다.
- `RaceConditionLab` 결과는 실행할 때마다 다릅니다. 그게 정상이고, 그 자체가 관찰 포인트입니다.
- `DeadlockLab` 은 최대 3초 안에 데드락을 감지하고 스스로 끝납니다. 드물게 타이밍상 감지가 안 되면 다시 실행하세요.
- `java X.java` 로 실행한 프로그램은 `jps -l` 에서 `jdk.compiler/com.sun.tools.javac.launcher.Main` 으로 보입니다.
- 학습용 코드라 재현에 필요한 최소한만 담았습니다. 실무 코드의 참고용이 아닙니다(예: `LostUpdateLab` 의 ❌ 패턴은 일부러 틀리게 짠 것).
