# RDS Monitoring Service

AWS RDS 인스턴스 모니터링을 위한 Spring Boot REST API 서비스입니다.

## 🚀 기능

### RDS 모니터링
- **인스턴스 정보 조회**: 모든 RDS 인스턴스 또는 사용 가능한 인스턴스 목록
- **CPU 사용률 모니터링**: CloudWatch를 통한 CPU 메트릭 조회
- **데이터베이스 연결 수 모니터링**: 활성 연결 수 추적
- **헬스체크**: RDS 서비스 상태 확인

## 📋 API 엔드포인트

### 기본 정보
- **Base URL**: `http://localhost:8081/api`
- **포트**: 8081 (기본값)

### RDS 모니터링 API

#### 헬스체크
```http
GET /rds/health
```

#### 인스턴스 관리
```http
GET /rds/instances              # 모든 RDS 인스턴스 조회
GET /rds/instances/available    # 사용 가능한 인스턴스만 조회
```

#### 메트릭 조회
```http
GET /rds/instances/{instanceId}/cpu          # CPU 사용률
GET /rds/instances/{instanceId}/connections  # 데이터베이스 연결 수
```

**파라미터**:
- `hours`: 조회 시간 범위 (기본값: 1시간)
- `period`: 집계 간격 초 (기본값: 300초)

## 🔧 설정

### 환경변수 설정
1. `.env.example`을 복사하여 `.env` 파일 생성
2. 실제 RDS 인스턴스 정보로 수정

```bash
cp .env.example .env
```

### 필수 환경변수
```bash
# RDS 설정
RDS_INSTANCE_ID=your-database-instance
RDS_MASTER_USERNAME=admin
RDS_MASTER_PASSWORD=your-password
RDS_ENDPOINT=your-database.region.rds.amazonaws.com
```

## 🚀 실행 방법

### 개발 환경
```bash
mvn spring-boot:run
```

### 빌드
```bash
mvn clean package
java -jar target/rds-monitoring-0.0.1-SNAPSHOT.jar
```

## 📊 사용 예시

### RDS 인스턴스 목록 조회
```bash
curl http://localhost:8081/api/rds/instances
```

### CPU 메트릭 조회
```bash
curl "http://localhost:8081/api/rds/instances/database-1/cpu?hours=2"
```

### 연결 수 모니터링
```bash
curl "http://localhost:8081/api/rds/instances/database-1/connections"
```

## 🔒 보안

- `.env` 파일은 Git에서 제외됨
- AWS 자격증명은 환경변수 또는 IAM 역할 사용
- 민감한 정보는 환경변수로 관리

## 🛠 기술 스택

- **Java 17**
- **Spring Boot 3.2.0**
- **AWS SDK for Java v2**
- **Maven**
- **CloudWatch Metrics**

## 📝 참고사항

- 기본 포트는 8081 (EC2 서비스와 분리)
- CloudWatch 메트릭 수집을 위해 적절한 IAM 권한 필요
- RDS 인스턴스가 CloudWatch 모니터링을 활성화해야 메트릭 조회 가능
