# AWS API Get Architecture

AWS 서비스들을 활용한 다양한 API 아키텍처 프로젝트들의 모음입니다.

## 프로젝트 목록

### 1. EC2 모니터링 서비스 (`ec2-monitoring-service/`)

Spring Boot를 사용하여 AWS EC2 인스턴스 정보와 CloudWatch 메트릭을 조회하는 REST API 서비스입니다.

#### 주요 기능
- **EC2 인스턴스 정보 조회**: 계정 내 모든 EC2 인스턴스의 메타데이터 조회
- **CloudWatch 메트릭 조회**: CPU, 네트워크, 디스크 I/O 등의 성능 지표 조회  
- **자원 사용량 요약**: 최근 24시간 자원 사용량 통계 제공
- **환경변수 기반 설정**: `.env` 파일로 민감한 정보 안전 관리
- **타입 안전한 API**: DTO 기반의 구조화된 응답

#### 기술 스택
- **Spring Boot 3.2.0**: 웹 애플리케이션 프레임워크
- **Java 17**: OpenJDK 17 (LTS)
- **AWS SDK for Java 2.21.29**: EC2 및 CloudWatch API 클라이언트
- **Maven**: 빌드 및 의존성 관리

#### 아키텍처
```
[Client] → [Spring Boot API Server] → [AWS SDK for Java] → [AWS API (EC2/CloudWatch)]
```

#### API 엔드포인트
- `GET /api/ec2/instances` - 모든 인스턴스 목록
- `GET /api/ec2/instances/running` - 실행 중인 인스턴스만
- `GET /api/ec2/instances/{id}` - 특정 인스턴스 정보
- `GET /api/ec2/instances/{id}/metrics/cpu` - CPU 메트릭
- `GET /api/ec2/instances/{id}/metrics/network` - 네트워크 메트릭
- `GET /api/ec2/instances/{id}/metrics/disk` - 디스크 메트릭
- `GET /api/ec2/instances/{id}/usage-summary` - 24시간 사용량 요약

자세한 내용은 [`ec2-monitoring-service/README.md`](./ec2-monitoring-service/README.md)를 참고하세요.

## 시작하기

각 프로젝트별로 개별적인 설정 및 실행 방법이 있습니다. 각 프로젝트 폴더의 README.md 파일을 참고하여 진행하세요.

### 공통 사전 요구사항
- **Java 17 이상**
- **Maven 3.6 이상** 
- **AWS CLI** (자격 증명 설정용)
- **AWS 계정** (해당 서비스 접근 권한)

## 보안

모든 프로젝트는 민감한 정보(AWS 자격증명, 인스턴스 ID 등)를 `.env` 파일로 관리합니다. 
- `.env` 파일은 `.gitignore`에 포함되어 Git에 추적되지 않습니다
- `.env.example` 파일을 참고하여 본인의 환경에 맞게 설정하세요