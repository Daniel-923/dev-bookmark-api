package dev.bookmark.api.exception;

import jakarta.servlet.http.HttpServletRequest; // 요청 경로를 가져오기 위해
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // @Valid 검증 실패 시 발생
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice // 1. 모든 @RestController에서 발생하는 예외를 처리함을 선언
public class GlobalExceptionHandler {

    // 2. IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST, // 400 Bad Request
                exception.getMessage(), // 서비스에서 던진 예외 메시지 사용
                request.getRequestURI() // 요청 경로
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 3. IllegalStateException 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException exception, HttpServletRequest request) {
        // 예를 들어, "하위 폴더가 있어서 삭제 불가" 같은 경우는 409 Conflict가 더 적절할 수 있습니다.
        // 여기서는 예시로 400 Bad Request를 사용합니다. 상황에 맞게 상태 코드를 선택하세요.
        HttpStatus status = HttpStatus.BAD_REQUEST; // 또는 HttpStatus.CONFLICT
        if (exception.getMessage() != null && exception.getMessage().contains("삭제할 수 없습니다")) {
            status = HttpStatus.CONFLICT; // 메시지 내용에 따라 상태 코드 변경 가능
        }
        ErrorResponse errorResponse = new ErrorResponse(
                status,
                exception.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    // 4. @Valid 어노테이션으로 인한 유효성 검증 실패 시 (MethodArgumentNotValidException) 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        // 여러 유효성 검증 오류 메시지를 하나로 합치거나, 첫 번째 오류만 보여줄 수 있습니다.
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                errorMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 5. (선택) 가장 일반적인 최상위 예외 처리 (위에서 잡지 못한 모든 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        // 실제 운영 환경에서는 여기서 에러 로깅을 철저히 해야 합니다.
        // ex.printStackTrace(); // 개발 중에만 사용, 실제 운영에서는 로깅 프레임워크 사용
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, // 500 Internal Server Error
                "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.", // 사용자에게는 구체적인 예외 메시지 대신 일반적인 메시지
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // TODO: 나중에 ResourceNotFoundException, DuplicateResourceException 등
    //       커스텀 예외를 만들고, 각 예외에 맞는 핸들러 메소드를 추가하면 더욱 좋습니다.
    //       예: @ExceptionHandler(ResourceNotFoundException.class) -> HttpStatus.NOT_FOUND (404)
}