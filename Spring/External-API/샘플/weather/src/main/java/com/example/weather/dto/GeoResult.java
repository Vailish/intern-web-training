package com.example.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 지오코딩 결과 한 건 = 검색한 도시의 좌표와 이름.
 *
 * JSON의 필드 이름(latitude, longitude...)과 record 부품 이름을 똑같이 맞추면
 * Jackson이 알아서 채워 줍니다. (이름이 다르면 @JsonProperty로 연결 — Current.java 참고)
 *
 * - name    : 서버가 돌려주는 도시 이름 (language=ko 라서 "서울특별시" 처럼 한글로 옴)
 * - country : 나라 이름 ("대한민국")
 * - admin1  : 상위 행정구역 ("서울특별시", "경기도" 등)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeoResult(
		String name,
		double latitude,
		double longitude,
		String country,
		String admin1,
		String timezone
) {
}
