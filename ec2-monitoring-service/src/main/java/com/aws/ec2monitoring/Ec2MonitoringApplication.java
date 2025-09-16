package com.aws.ec2monitoring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AWS EC2 모니터링 서비스 메인 애플리케이션
 * 
 * 주요 기능:
 * - EC2 인스턴스 정보 조회
 * - CloudWatch 메트릭 데이터 조회 (CPU, 네트워크, 디스크)
 * - 자원 사용량 요약 제공
 * 
 * API 엔드포인트:
 * - GET /api/ec2/instances - 모든 인스턴스 목록
 * - GET /api/ec2/instances/running - 실행 중인 인스턴스만
 * - GET /api/ec2/instances/{id} - 특정 인스턴스 정보
 * - GET /api/ec2/instances/{id}/metrics/cpu - CPU 메트릭
 * - GET /api/ec2/instances/{id}/metrics/network - 네트워크 메트릭
 * - GET /api/ec2/instances/{id}/metrics/disk - 디스크 메트릭
 * - GET /api/ec2/instances/{id}/metrics - 모든 메트릭
 * - GET /api/ec2/instances/{id}/usage-summary - 24시간 사용량 요약
 */
@SpringBootApplication
public class Ec2MonitoringApplication {

    public static void main(String[] args) {
        // .env 파일 로드 (개발 환경에서만)
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            
            // 환경변수로 설정
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            
            System.out.println("✅ .env 파일이 성공적으로 로드되었습니다.");
        } catch (Exception e) {
            System.out.println("⚠️  .env 파일을 찾을 수 없습니다. 환경변수나 application.yml의 기본값을 사용합니다.");
        }
        
        SpringApplication.run(Ec2MonitoringApplication.class, args);
    }
}
