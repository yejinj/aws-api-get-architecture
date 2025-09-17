package com.aws.monitoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS 통합 서비스 설정 Properties
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
     * EC2 관련 설정
     */
    private Ec2Config ec2 = new Ec2Config();

    /**
     * S3 관련 설정
     */
    private S3Config s3 = new S3Config();

    /**
     * RDS 관련 설정
     */
    private RdsConfig rds = new RdsConfig();

    /**
     * Lambda 관련 설정
     */
    private LambdaConfig lambda = new LambdaConfig();

    /**
     * CloudWatch 설정
     */
    private CloudWatch cloudwatch = new CloudWatch();

    @Data
    public static class Ec2Config {
        private TestInstance testInstance = new TestInstance();
        
        @Data
        public static class TestInstance {
            private String id = "";
            private String name = "";
            private String type = "t3.micro";
            private String amiId = "";
            private String platform = "Linux/UNIX";
            private Integer vcpuCount = 2;
        }
    }

    @Data
    public static class S3Config {
        private String defaultBucket = "";
        private Integer maxKeys = 1000;
    }

    @Data
    public static class RdsConfig {
        private String defaultDbInstanceId = "";
        private Integer maxRecords = 100;
    }

    @Data
    public static class LambdaConfig {
        private String defaultFunctionName = "";
        private Integer maxItems = 50;
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
