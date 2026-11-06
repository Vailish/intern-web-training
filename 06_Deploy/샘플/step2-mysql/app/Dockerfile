# =========================================================
#  [STEP 2] 앱(스프링부트 + MySQL 드라이버) 이미지 — 멀티스테이지
#  STEP 1 심화(Dockerfile.multistage)와 같은 구조입니다. jar 빌드까지 Docker 안에서 합니다.
#
#  빌드:  docker build -t intro-app:1.0 .
#  ※ DB 접속 정보(DB_HOST, DB_PASSWORD 등)는 이미지에 넣지 않고 "실행할 때" -e 로 넘깁니다.
#     → 이미지 하나로 개발/운영 어디든 띄울 수 있고, 비밀번호가 이미지에 남지 않습니다.
# =========================================================

# ---------- 1단계: 빌드 ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew \
 && ./gradlew bootJar -x test --no-daemon

# ---------- 2단계: 실행 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# ⚠️ 함정: 컨테이너의 기본 시간대는 UTC 라서, 그대로 두면 작성 시각이 한국보다 9시간 느리게 저장·표시됩니다.
#    자바(JVM)는 TZ 환경변수를 읽어 기본 시간대를 정하므로 한국 시간으로 지정합니다.
ENV TZ=Asia/Seoul

RUN useradd -r -s /bin/false spring
COPY --from=build /workspace/build/libs/intro-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R spring:spring /app
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
