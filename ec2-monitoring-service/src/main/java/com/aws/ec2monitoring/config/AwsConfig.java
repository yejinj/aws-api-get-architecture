package com.aws.monitoring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.lambda.LambdaClient;

import java.time.Duration;

/**
 * AWS SDK 클라이언트 통합 설정
 * 지원 서비스: EC2, S3, RDS, Lambda, CloudWatch
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfig {

    private final AwsProperties awsProperties;

    /**
     * EC2 클라이언트 빈 생성
     */
    @Bean
    public Ec2Client ec2Client() {
        log.info("EC2 클라이언트 초기화 - 리전: {}", awsProperties.getRegion());
        return Ec2Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(getClientOverrideConfiguration(Duration.ofSeconds(30), Duration.ofSeconds(10)))
                .build();
    }

    /**
     * S3 클라이언트 빈 생성
     */
    @Bean
    public S3Client s3Client() {
        log.info("S3 클라이언트 초기화 - 리전: {}", awsProperties.getRegion());
        return S3Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(getClientOverrideConfiguration(Duration.ofSeconds(30), Duration.ofSeconds(10)))
                .build();
    }

    /**
     * RDS 클라이언트 빈 생성
     */
    @Bean
    public RdsClient rdsClient() {
        log.info("RDS 클라이언트 초기화 - 리전: {}", awsProperties.getRegion());
        return RdsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(getClientOverrideConfiguration(Duration.ofSeconds(30), Duration.ofSeconds(10)))
                .build();
    }

    /**
     * Lambda 클라이언트 빈 생성
     */
    @Bean
    public LambdaClient lambdaClient() {
        log.info("Lambda 클라이언트 초기화 - 리전: {}", awsProperties.getRegion());
        return LambdaClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(getClientOverrideConfiguration(Duration.ofSeconds(30), Duration.ofSeconds(10)))
                .build();
    }

    /**
     * CloudWatch 클라이언트 빈 생성
     */
    @Bean
    public CloudWatchClient cloudWatchClient() {
        log.info("CloudWatch 클라이언트 초기화 - 리전: {}", awsProperties.getRegion());
        return CloudWatchClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(getClientOverrideConfiguration(Duration.ofSeconds(60), Duration.ofSeconds(20)))
                .build();
    }

    /**
     * 공통 클라이언트 설정 생성
     */
    private software.amazon.awssdk.core.client.config.ClientOverrideConfiguration getClientOverrideConfiguration(
            Duration apiCallTimeout, Duration apiCallAttemptTimeout) {
        return software.amazon.awssdk.core.client.config.ClientOverrideConfiguration.builder()
                .retryPolicy(RetryPolicy.builder().numRetries(3).build())
                .apiCallTimeout(apiCallTimeout)
                .apiCallAttemptTimeout(apiCallAttemptTimeout)
                .build();
    }

    /**
     * AWS 자격 증명 제공자 설정
     * 로컬 개발: AWS CLI 프로필 사용
     * 서버 배포: IAM Role 또는 환경변수 사용
     */
    private DefaultCredentialsProvider getCredentialsProvider() {
        try {
            // 먼저 기본 자격 증명 체인을 시도 (IAM Role, 환경변수 등)
            return DefaultCredentialsProvider.builder()
                    .profileName(awsProperties.getProfile())
                    .build();
        } catch (Exception e) {
            log.warn("기본 자격 증명 실패, 프로필 자격 증명으로 전환: {}", e.getMessage());
            // 실패하면 명시적 프로필 사용
            return DefaultCredentialsProvider.builder()
                    .profileName(awsProperties.getProfile())
                    .build();
        }
    }
}
