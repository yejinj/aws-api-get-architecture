package com.aws.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

/**
 * 간단한 AWS 설정 - 테스트용
 */
@Configuration
public class SimpleAwsConfig {

    @Bean
    public Ec2Client ec2Client() {
        return Ec2Client.builder()
                .region(Region.US_EAST_2)  // 기본 리전
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
