package com.example.weather;

/**
 * "그런 도시는 없어요" 를 뜻하는 예외.
 *
 * 서비스가 이 예외를 던지면, 컨트롤러가 받아서 화면에 친절한 안내 문구로 바꿔 보여 줍니다.
 * (외부 API 호출 실패 = 서버 고장 이 아니라, "입력이 잘못됨" 일 수 있다는 것을 구분하는 연습)
 */
public class CityNotFoundException extends RuntimeException {

	public CityNotFoundException(String city) {
		super("도시를 찾을 수 없습니다: '" + city + "' (영문 도시명으로 다시 시도해 보세요. 예: Seoul, Busan)");
	}
}
