package com.aws.monitoring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AWS 통합 모니터링 서비스 메인 애플리케이션
 * 
 * 지원 서비스:
 * - EC2: 인스턴스 정보 및 CloudWatch 메트릭 조회
 * - S3: 버킷 정보 및 사용량 통계 조회
 * - RDS: 데이터베이스 인스턴스 및 성능 메트릭 조회
 * - Lambda: 함수 정보 및 실행 통계 조회
 * 
 * API 구조:
 * - GET /api/{service}/* - 각 AWS 서비스별 엔드포인트
 * - GET /api/health - 서비스 상태 확인
 */
@SpringBootApplication
public class AwsMonitoringApplication {

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
        
        SpringApplication.run(AwsMonitoringApplication.class, args);
    }
}