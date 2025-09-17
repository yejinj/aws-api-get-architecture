package com.aws.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * S3 버킷 정보를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketDto {
    
    /**
     * 버킷 이름
     */
    private String name;
    
    /**
     * 버킷 생성 날짜
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant creationDate;
    
    /**
     * 버킷 리전
     */
    private String region;
    
    /**
     * 버킷 크기 (바이트)
     */
    private Long size;
    
    /**
     * 객체 개수
     */
    private Long objectCount;
}
