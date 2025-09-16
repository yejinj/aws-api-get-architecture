package com.aws.ec2monitoring.config;

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

import java.time.Duration;

/**
 * AWS SDK 클라이언트 설정
 * - EC2 클라이언트: 인스턴스 메타데이터 조회용
 * - CloudWatch 클라이언트: 메트릭 데이터 조회용
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfig {

    private final AwsProperties awsProperties;

    /**
     * EC2 클라이언트 빈 생성
     * 인스턴스 정보, 상태, 태그 등을 조회하는데 사용
     */
    @Bean
    public Ec2Client ec2Client() {
        log.info("EC2 클라이언트 초기화 - 리전: {}, 프로필: {}", awsProperties.getRegion(), awsProperties.getProfile());
        log.info("테스트 인스턴스: {} ({})", awsProperties.getTestInstance().getName(), awsProperties.getTestInstance().getId());
        
        return Ec2Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(builder -> builder
                        .retryPolicy(RetryPolicy.builder()
                                .numRetries(3)
                                .build())
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(10))
                )
                .build();
    }

    /**
     * CloudWatch 클라이언트 빈 생성
     * CPU, 네트워크, 디스크 등의 메트릭을 조회하는데 사용
     */
    @Bean
    public CloudWatchClient cloudWatchClient() {
        log.info("CloudWatch 클라이언트 초기화 - 리전: {}, 프로필: {}", awsProperties.getRegion(), awsProperties.getProfile());
        
        return CloudWatchClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider())
                .overrideConfiguration(builder -> builder
                        .retryPolicy(RetryPolicy.builder()
                                .numRetries(3)
                                .build())
                        .apiCallTimeout(Duration.ofSeconds(60))
                        .apiCallAttemptTimeout(Duration.ofSeconds(20))
                )
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
