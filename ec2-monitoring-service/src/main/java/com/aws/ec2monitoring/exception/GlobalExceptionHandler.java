package com.aws.monitoring.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.rds.model.RdsException;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 통합 AWS 서비스 글로벌 예외 처리 핸들러
 * 모든 AWS SDK 예외 및 일반적인 예외를 처리하여 적절한 HTTP 응답을 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * EC2 관련 예외 처리
     */
    @ExceptionHandler(Ec2Exception.class)
    public ResponseEntity<Map<String, Object>> handleEc2Exception(Ec2Exception e) {
        log.error("EC2 API 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "EC2_API_ERROR",
                "EC2 서비스 호출 중 오류가 발생했습니다: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * CloudWatch 관련 예외 처리
     */
    @ExceptionHandler(CloudWatchException.class)
    public ResponseEntity<Map<String, Object>> handleCloudWatchException(CloudWatchException e) {
        log.error("CloudWatch API 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "CLOUDWATCH_API_ERROR",
                "CloudWatch 서비스 호출 중 오류가 발생했습니다: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * S3 관련 예외 처리
     */
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<Map<String, Object>> handleS3Exception(S3Exception e) {
        log.error("S3 API 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "S3_API_ERROR",
                "S3 서비스 호출 중 오류가 발생했습니다: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * RDS 관련 예외 처리
     */
    @ExceptionHandler(RdsException.class)
    public ResponseEntity<Map<String, Object>> handleRdsException(RdsException e) {
        log.error("RDS API 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "RDS_API_ERROR",
                "RDS 서비스 호출 중 오류가 발생했습니다: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Lambda 관련 예외 처리
     */
    @ExceptionHandler(LambdaException.class)
    public ResponseEntity<Map<String, Object>> handleLambdaException(LambdaException e) {
        log.error("Lambda API 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "LAMBDA_API_ERROR",
                "Lambda 서비스 호출 중 오류가 발생했습니다: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 일반적인 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("런타임 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_ERROR",
                "내부 서버 오류가 발생했습니다: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 잘못된 인자 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 인자 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "INVALID_ARGUMENT",
                "잘못된 요청 파라미터입니다: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 모든 예외에 대한 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        
        Map<String, Object> errorResponse = createErrorResponse(
                "UNKNOWN_ERROR",
                "예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 에러 응답 객체 생성 유틸리티 메서드
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, int status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
