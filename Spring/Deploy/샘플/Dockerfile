# =========================================================
#  멀티스테이지(multi-stage) Dockerfile — 스프링부트 앱 배포용
#  intro-test / intro-jpa 처럼 Gradle 로 만든 스프링부트 앱을 기준으로 합니다.
#  (이 파일을 여러분의 프로젝트 "루트"(gradlew 가 있는 곳)에 두세요.)
# =========================================================

# ---------- 1단계: 빌드 스테이지 ----------
# JDK가 있는 이미지에서 소스를 컴파일해 실행 가능한 jar 를 만듭니다.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# 의존성 캐시 활용: 빌드 스크립트를 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# 소스 복사
COPY src src

# 실행 권한 부여 후 jar 빌드 (테스트는 CI에서 이미 돌리므로 -x test 로 생략 → 빌드 단축)
RUN chmod +x gradlew && ./gradlew clean bootJar -x test --no-daemon

# ---------- 2단계: 실행 스테이지 ----------
# 실행에는 JDK가 필요 없습니다. 가벼운 JRE 이미지만 사용 → 최종 이미지가 작아집니다.
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# 보안: root 가 아닌 전용 사용자로 실행합니다.
RUN useradd -r -s /bin/false spring

# 1단계에서 만든 jar 만 복사 (소스·그래들 캐시는 최종 이미지에 안 들어감)
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar

# ⚠️ 함정: /app 이 root 소유라 비루트(spring) 사용자가 로그/H2 파일을 못 만들어 앱이 죽습니다.
#          반드시 소유권을 넘겨줘야 합니다. (실측으로 확인된 문제)
RUN chown -R spring:spring /app
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
