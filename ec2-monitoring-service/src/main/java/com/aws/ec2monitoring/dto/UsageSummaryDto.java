package com.aws.ec2monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 인스턴스 자원 사용량 요약 정보를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageSummaryDto {
    
    /**
     * 인스턴스 ID
     */
    private String instanceId;
    
    /**
     * 인스턴스 이름
     */
    private String instanceName;
    
    /**
     * 요약 기간 시작 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant startTime;
    
    /**
     * 요약 기간 종료 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant endTime;
    
    /**
     * CPU 사용률 요약
     */
    private ResourceUsage cpuUsage;
    
    /**
     * 네트워크 입력 트래픽 요약 (Bytes)
     */
    private ResourceUsage networkIn;
    
    /**
     * 네트워크 출력 트래픽 요약 (Bytes)
     */
    private ResourceUsage networkOut;
    
    /**
     * 디스크 읽기 요약 (Bytes)
     */
    private ResourceUsage diskRead;
    
    /**
     * 디스크 쓰기 요약 (Bytes)
     */
    private ResourceUsage diskWrite;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceUsage {
        
        /**
         * 평균값
         */
        private Double average;
        
        /**
         * 최대값
         */
        private Double maximum;
        
        /**
         * 최소값
         */
        private Double minimum;
        
        /**
         * 단위
         */
        private String unit;
        
        /**
         * 데이터 포인트 개수
         */
        private Integer dataPointCount;
    }
}
