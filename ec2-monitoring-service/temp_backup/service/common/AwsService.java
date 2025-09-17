package com.aws.monitoring.service.common;

import java.util.List;

/**
 * AWS 서비스 공통 인터페이스
 * 모든 AWS 서비스 구현체가 따라야 하는 기본 계약 정의
 * 
 * @param <T> 서비스별 리소스 DTO 타입
 */
public interface AwsService<T> {
    
    /**
     * 서비스명 반환
     * @return AWS 서비스 이름 (예: "EC2", "S3", "RDS", "Lambda")
     */
    String getServiceName();
    
    /**
     * 서비스 상태 확인
     * @return 서비스 연결 및 권한 상태
     */
    boolean isServiceAvailable();
    
    /**
     * 모든 리소스 목록 조회
     * @return 서비스별 리소스 목록
     */
    List<T> listAllResources();
    
    /**
     * 활성 상태의 리소스만 조회
     * @return 활성 리소스 목록
     */
    List<T> listActiveResources();
    
    /**
     * 특정 리소스 상세 정보 조회
     * @param resourceId 리소스 식별자
     * @return 리소스 상세 정보
     */
    T getResourceById(String resourceId);
}
