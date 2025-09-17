package com.aws.monitoring.service.common;

import com.aws.monitoring.config.AwsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AWS 서비스 추상 클래스
 * 공통 기능과 유틸리티 메서드 제공
 * 
 * @param <T> 서비스별 리소스 DTO 타입
 */
@Slf4j
public abstract class AbstractAwsService<T> implements AwsService<T> {
    
    @Autowired
    protected AwsProperties awsProperties;
    
    /**
     * 서비스 상태 확인 기본 구현
     * 각 서비스에서 필요에 따라 오버라이드 가능
     */
    @Override
    public boolean isServiceAvailable() {
        try {
            // 기본적인 연결 테스트
            listAllResources();
            log.info("{} 서비스 연결 상태: 정상", getServiceName());
            return true;
        } catch (Exception e) {
            log.error("{} 서비스 연결 상태: 오류 - {}", getServiceName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 공통 예외 처리 유틸리티
     */
    protected RuntimeException handleAwsException(String operation, Exception e) {
        String errorMessage = String.format("%s 서비스 %s 실패: %s", 
                getServiceName(), operation, e.getMessage());
        log.error(errorMessage, e);
        return new RuntimeException(errorMessage, e);
    }
    
    /**
     * 로깅 유틸리티
     */
    protected void logOperation(String operation, Object... params) {
        log.info("{} 서비스 {} 요청: {}", getServiceName(), operation, params);
    }
    
    /**
     * 결과 로깅 유틸리티
     */
    protected void logResult(String operation, int count) {
        log.info("{} 서비스 {} 완료: {}개 조회", getServiceName(), operation, count);
    }
}
