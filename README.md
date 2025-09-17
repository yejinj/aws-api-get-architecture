# AWS API Get Architecture

AWS 서비스들을 활용한 다양한 API 아키텍처 프로젝트들의 모음입니다.

## 프로젝트 목록

### 1. AWS 통합 모니터링 서비스 (`ec2-monitoring-service/`)

Spring Boot를 사용하여 다양한 AWS 서비스의 리소스 정보와 메트릭을 조회하는 통합 REST API 서비스입니다.

#### 지원 서비스
- **EC2**: 인스턴스 정보 및 CloudWatch 메트릭 조회
- **S3**: 버킷 정보 및 사용량 통계 조회
- **RDS**: 데이터베이스 인스턴스 및 성능 메트릭 조회
- **Lambda**: 함수 정보 및 실행 통계 조회

#### 주요 기능
- **다중 AWS 서비스 지원**: 확장 가능한 서비스 아키텍처
- **공통 인터페이스**: 모든 서비스에 일관된 API 구조 제공
- **CloudWatch 메트릭**: CPU, 네트워크, 디스크 I/O 등의 성능 지표 조회  
- **자원 사용량 요약**: 최근 24시간 자원 사용량 통계 제공
- **환경변수 기반 설정**: `.env` 파일로 민감한 정보 안전 관리
- **타입 안전한 API**: DTO 기반의 구조화된 응답
- **통합 헬스체크**: 모든 AWS 서비스 상태 확인

#### 기술 스택
- **Spring Boot 3.2.0**: 웹 애플리케이션 프레임워크
- **Java 17**: OpenJDK 17 (LTS)
- **AWS SDK for Java 2.21.29**: 다중 AWS 서비스 API 클라이언트
- **Maven**: 빌드 및 의존성 관리

#### 아키텍처
```
[Client] → [Spring Boot API Server] → [AWS SDK for Java] → [AWS APIs (EC2/S3/RDS/Lambda/CloudWatch)]
```

#### API 엔드포인트
**공통**
- `GET /api/health` - 모든 AWS 서비스 상태 확인

**EC2**
- `GET /api/ec2/instances` - 모든 인스턴스 목록
- `GET /api/ec2/instances/running` - 실행 중인 인스턴스만
- `GET /api/ec2/instances/{id}` - 특정 인스턴스 정보
- `GET /api/ec2/instances/{id}/metrics/*` - 메트릭 조회

**S3**
- `GET /api/s3/buckets` - 모든 버킷 목록
- `GET /api/s3/buckets/{name}` - 특정 버킷 정보

**RDS** (예정)
- `GET /api/rds/instances` - 모든 DB 인스턴스 목록
- `GET /api/rds/instances/{id}` - 특정 DB 인스턴스 정보

**Lambda** (예정)
- `GET /api/lambda/functions` - 모든 함수 목록
- `GET /api/lambda/functions/{name}` - 특정 함수 정보

자세한 내용은 [`ec2-monitoring-service/README.md`](./ec2-monitoring-service/README.md)를 참고하세요.

## 시작하기

각 프로젝트별로 개별적인 설정 및 실행 방법이 있습니다. 각 프로젝트 폴더의 README.md 파일을 참고하여 진행하세요.

### 공통 사전 요구사항
- **Java 17 이상**
- **Maven 3.6 이상** 
- **AWS CLI** (자격 증명 설정용)
- **AWS 계정** (EC2, S3, RDS, Lambda, CloudWatch 접근 권한)

## 보안

모든 프로젝트는 민감한 정보(AWS 자격증명, 인스턴스 ID 등)를 `.env` 파일로 관리합니다. 
- `.env` 파일은 `.gitignore`에 포함되어 Git에 추적되지 않습니다
- `.env.example` 파일을 참고하여 본인의 환경에 맞게 설정하세요