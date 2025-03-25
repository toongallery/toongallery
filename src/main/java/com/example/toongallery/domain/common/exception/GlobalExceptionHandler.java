package com.example.toongallery.domain.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ErrorDetail errorDetail = new ErrorDetail(
                ex.getField(),
                ex.getErrorCode().getMessage(),
                ex.getErrorCode().name()
        );
        return new ResponseEntity<>(ErrorResponse.of(errorDetail), ex.getErrorCode().getHttpStatus());
    }

    // Valid 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetail(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getCode() // NotBlank, Size 등
                ))
                .collect(Collectors.toList());
        return new ResponseEntity<>(ErrorResponse.of(errorDetails), HttpStatus.BAD_REQUEST);
    }

    // @PathVariable이 잘못된 타입일 때 발생
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String requiredType = (ex.getRequiredType() != null)
                ? ex.getRequiredType().getSimpleName()
                : "Unknown";
        ErrorDetail errorDetail = new ErrorDetail(
                ex.getName(), // 파라미터 이름 (예: "id")
                String.format("'%s'은(는) 잘못된 타입입니다. %s 타입이 필요합니다.", ex.getValue(), requiredType),
                "TYPE_MISMATCH"
        );
        return new ResponseEntity<>(ErrorResponse.of(errorDetail), HttpStatus.BAD_REQUEST);
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail(
                "unknown",
                ex.getMessage(),
                "INTERNAL_SERVER_ERROR"
        );
        return new ResponseEntity<>(ErrorResponse.of(errorDetail), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}