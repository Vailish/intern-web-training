# =========================================================
#  [STEP 1] 가장 기본적인 Dockerfile (단일 스테이지)
#  - 내 PC에서 먼저 jar를 만든 뒤(.\gradlew.bat bootJar), 그 jar를 이미지에 "복사"만 합니다.
#  - DB 설정은 원본 프로젝트와 100% 동일 (H2 파일 모드: jdbc:h2:./data/introdb)
#
#  빌드:  docker build -t intro-h2:1.0 .
#  실행:  docker run -d --name intro-h2 -p 8080:8080 intro-h2:1.0
# =========================================================

# ① 베이스 이미지: 자바 17 "실행 환경(JRE)"만 들어 있는 리눅스 이미지
#    (컴파일은 이미 내 PC에서 끝났으니 JDK가 아니라 JRE면 충분합니다)
FROM eclipse-temurin:17-jre

# ② 컨테이너 안의 작업 폴더. 이후 명령은 모두 /app 에서 실행됩니다.
#    H2 URL이 ./data/introdb (상대경로) 이므로 DB 파일은 /app/data/introdb.mv.db 에 생깁니다.
WORKDIR /app

# ⚠️ 함정: 컨테이너의 기본 시간대는 UTC 라서, 그대로 두면 작성 시각이 한국보다 9시간 느리게 저장·표시됩니다.
#    자바(JVM)는 TZ 환경변수를 읽어 기본 시간대를 정하므로 한국 시간으로 지정합니다.
ENV TZ=Asia/Seoul

# ③ 보안: root 대신 전용 사용자(spring)로 앱을 실행합니다.
RUN useradd -r -s /bin/false spring

# ④ 내 PC에서 만든 jar를 이미지 안으로 복사합니다. (이름은 app.jar 로 단순하게)
#    주의: .\gradlew.bat build 로 빌드하면 build/libs 에 intro-0.0.1-SNAPSHOT-plain.jar(실행 불가)도 생깁니다.
#          *.jar 로 적으면 파일이 2개 잡혀 빌드가 실패하므로 정확한 파일 이름을 적었습니다.
COPY build/libs/intro-0.0.1-SNAPSHOT.jar app.jar

# ⑤ ⚠️ 함정: /app 은 root 소유라서 spring 사용자가 H2 파일(data/)을 만들 수 없어 앱이 죽습니다.
#    data 폴더를 미리 만들고 소유권을 spring 에게 넘겨 줍니다.
RUN mkdir -p /app/data && chown -R spring:spring /app
USER spring

# ⑥ "이 컨테이너는 8080 포트를 쓴다"는 문서용 표시 (실제 포트 개방은 docker run -p 가 합니다)
EXPOSE 8080

# ⑦ 컨테이너가 시작될 때 실행할 명령 = java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
