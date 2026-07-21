package com.example.weather.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 지오코딩 서버의 응답 전체를 담는 그릇.
 *
 * 실제 응답 예:
 * {
 *   "results": [ { "name": "서울특별시", "latitude": 37.566, "longitude": 126.9784, ... } ],
 *   "generationtime_ms": 0.56
 * }
 *
 * @JsonIgnoreProperties(ignoreUnknown = true)
 *   → 외부 응답에는 우리가 안 쓰는 필드(generationtime_ms 등)가 잔뜩 들어 있습니다.
 *     이 어노테이션이 없으면 "모르는 필드"를 만났을 때 오류가 납니다.
 *     "필요한 것만 받고 나머지는 무시" 하겠다는 선언입니다. (외부 API 다룰 때 거의 필수)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(
		List<GeoResult> results
) {
}
