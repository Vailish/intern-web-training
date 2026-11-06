# =========================================================
#  [STEP 2] DB(MySQL) 이미지
#  공식 mysql 이미지를 그대로 써도 되지만, "우리 서비스용 설정"을 이미지에 담아 두면
#  누가 띄워도 같은 문자셋·시간대로 동작합니다. → 이것이 DB 이미지를 직접 빌드하는 이유입니다.
#
#  빌드:  docker build -t intro-db:1.0 .
# =========================================================

# MySQL 8.4 = 장기 지원(LTS) 버전
FROM mysql:8.4

# 컨테이너가 처음 시작될 때 자동으로 만들 데이터베이스 이름 (공식 이미지 기능)
# ※ 비밀번호(MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD)는 절대 여기 적지 않습니다! 실행 시 -e 로 넘깁니다.
ENV MYSQL_DATABASE=introdb

# 우리 서비스용 MySQL 설정 파일(한글 utf8mb4, 한국 시간)을 설정 폴더에 넣습니다.
COPY conf/intro.cnf /etc/mysql/conf.d/intro.cnf

# 3306 = MySQL 기본 포트 (문서용 표시)
EXPOSE 3306
