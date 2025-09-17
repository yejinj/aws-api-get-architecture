package com.aws.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * CloudWatch 메트릭 데이터를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDto {
    
    /**
     * 메트릭 이름 (예: CPUUtilization, NetworkIn)
     */
    private String metricName;
    
    /**
     * 네임스페이스 (예: AWS/EC2)
     */
    private String namespace;
    
    /**
     * 인스턴스 ID
     */
    private String instanceId;
    
    /**
     * 단위 (예: Percent, Bytes, Count)
     */
    private String unit;
    
    /**
     * 데이터 포인트 목록
     */
    private List<DataPointDto> dataPoints;
    
    /**
     * 조회 시작 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant startTime;
    
    /**
     * 조회 종료 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant endTime;
    
    /**
     * 집계 기간 (초)
     */
    private Integer period;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPointDto {
        
        /**
         * 타임스탬프
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant timestamp;
        
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
         * 합계
         */
        private Double sum;
        
        /**
         * 샘플 개수
         */
        private Double sampleCount;
        
        /**
         * 단위
         */
        private String unit;
    }
}
