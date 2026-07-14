-- 애플리케이션이 시작될 때 실행되는 DDL입니다. (spring.sql.init.mode=always)
-- JPA의 ddl-auto=update가 하던 "테이블 만들기"를 MyBatis에서는 이렇게 직접 합니다.
-- DB 교육(03_SQL_기초)에서 만든 intro 테이블과 완전히 동일합니다. (H2·MySQL 공용)
CREATE TABLE IF NOT EXISTS intro (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    VARCHAR(4000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
