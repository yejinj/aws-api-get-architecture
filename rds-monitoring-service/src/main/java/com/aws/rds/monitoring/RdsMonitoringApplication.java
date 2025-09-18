package com.aws.rds.monitoring;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RdsMonitoringApplication {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            System.out.println("✅ .env 파일이 성공적으로 로드되었습니다.");
        } catch (Exception e) {
            System.out.println("⚠️  .env 파일을 찾을 수 없습니다. 환경변수나 application.yml의 기본값을 사용합니다.");
        }
        SpringApplication.run(RdsMonitoringApplication.class, args);
    }
}
