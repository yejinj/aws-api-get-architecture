package com.aws.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * EC2 인스턴스 정보를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceDto {
    
    /**
     * 인스턴스 ID (예: i-1234567890abcdef0)
     */
    private String instanceId;
    
    /**
     * 인스턴스 타입 (예: t3.micro, t3.small)
     */
    private String instanceType;
    
    /**
     * 인스턴스 상태 (running, stopped, pending 등)
     */
    private String state;
    
    /**
     * 가용 영역 (예: ap-northeast-2a)
     */
    private String availabilityZone;
    
    /**
     * 퍼블릭 IP 주소
     */
    private String publicIpAddress;
    
    /**
     * 프라이빗 IP 주소
     */
    private String privateIpAddress;
    
    /**
     * 인스턴스 시작 시간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant launchTime;
    
    /**
     * 플랫폼 (Linux, Windows 등)
     */
    private String platform;
    
    /**
     * VPC ID
     */
    private String vpcId;
    
    /**
     * 서브넷 ID
     */
    private String subnetId;
    
    /**
     * 보안 그룹 목록
     */
    private List<SecurityGroupDto> securityGroups;
    
    /**
     * 인스턴스 태그 (Key-Value 형태)
     */
    private Map<String, String> tags;
    
    /**
     * 인스턴스 이름 (Name 태그에서 추출)
     */
    private String name;
    
    /**
     * 모니터링 상태 (enabled/disabled)
     */
    private String monitoring;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityGroupDto {
        private String groupId;
        private String groupName;
    }
}
