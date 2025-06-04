package dev.bookmark.api.exception; // 또는 공통 DTO 패키지

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    // 일관된 오류 응답 형식을 위해, 오류 정보를 담을 간단한 DTO

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path; // 요청 경로 (선택 사항)

    public ErrorResponse(HttpStatus httpStatus, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    // 간단한 오류 메시지만 필요할 경우
    public ErrorResponse(HttpStatus httpStatus, String message) {
        this(httpStatus, message, null);
    }
}