package com.aws.ec2monitoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS 관련 설정 Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    /**
     * AWS 리전
     */
    private String region = "us-east-2";

    /**
     * AWS CLI 프로필
     */
    private String profile = "default";

    /**
     * 테스트용 인스턴스 정보
     */
    private TestInstance testInstance = new TestInstance();

    /**
     * CloudWatch 설정
     */
    private CloudWatch cloudwatch = new CloudWatch();

    @Data
    public static class TestInstance {
        /**
         * 인스턴스 ID
         */
        private String id = "i-0da1b71ae6f874a24";

        /**
         * 인스턴스 이름
         */
        private String name = "apitest";

        /**
         * 인스턴스 타입
         */
        private String type = "t3.micro";

        /**
         * AMI ID
         */
        private String amiId = "ami-0634f3c109dcdc659";

        /**
         * 플랫폼
         */
        private String platform = "Linux/UNIX";

        /**
         * vCPU 수
         */
        private Integer vcpuCount = 2;
    }

    @Data
    public static class CloudWatch {
        /**
         * 기본 집계 기간 (초)
         */
        private Integer defaultPeriod = 300;

        /**
         * 최대 데이터 포인트 수
         */
        private Integer maxDatapoints = 1440;
    }
}
