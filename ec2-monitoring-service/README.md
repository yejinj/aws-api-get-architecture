# AWS EC2 모니터링 서비스

Spring Boot를 사용하여 AWS EC2 인스턴스 정보와 CloudWatch 메트릭을 조회하는 REST API 서비스입니다.

## 주요 기능

- **EC2 인스턴스 정보 조회**: 계정 내 모든 EC2 인스턴스의 메타데이터 조회
- **CloudWatch 메트릭 조회**: CPU, 네트워크, 디스크 I/O 등의 성능 지표 조회  
- **자원 사용량 요약**: 최근 24시간 자원 사용량 통계 제공
- **환경변수 기반 설정**: `.env` 파일로 민감한 정보 안전 관리
- **타입 안전한 API**: DTO 기반의 구조화된 응답

## 아키텍처

```
[Client] → [Spring Boot API Server] → [AWS SDK for Java] → [AWS API (EC2/CloudWatch)]
```

- **Spring Boot**: REST API 서버 및 비즈니스 로직
- **AWS SDK for Java v2**: EC2 및 CloudWatch API 호출
- **EC2 API**: 인스턴스 메타데이터 (상태, 타입, 태그 등)
- **CloudWatch API**: 성능 메트릭 (CPU, 네트워크, 디스크 등)

## 시작하기

### 사전 요구사항

- **Java 17 이상** (OpenJDK 17 권장)
- **Maven 3.6 이상** (또는 IDE 내장 Maven)
- **AWS CLI** (자격 증명 설정용)
- **AWS 계정** (EC2 인스턴스 및 CloudWatch 접근 권한)

### 환경 설정

#### 1. 프로젝트 클론 및 설정
```bash
# 프로젝트 디렉토리로 이동
cd /path/to/aws-ec2-monitoring

# .env.example을 복사하여 .env 파일 생성
cp .env.example .env

#### 2. Java 및 Maven 설치 (macOS)
```bash
# Homebrew 설치 (없는 경우)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Java 17 설치
brew install openjdk@17

# Maven 설치  
brew install maven

# Java PATH 설정
export JAVA_HOME="/usr/local/opt/openjdk@17"
export PATH="/usr/local/opt/openjdk@17/bin:$PATH"
```

#### 3. AWS CLI 설치 및 자격 증명 설정

```bash
# AWS CLI 설치
brew install awscli

# AWS 자격 증명 설정 (실제 AWS 계정 정보 필요)
aws configure
# AWS Access Key ID: [실제 Access Key]
# AWS Secret Access Key: [실제 Secret Key]
# Default region name: ap-northeast-2
# Default output format: json

# 설정 확인
aws sts get-caller-identity
```

**필요한 IAM 권한:**
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:DescribeInstances",
                "cloudwatch:GetMetricStatistics",
                "cloudwatch:ListMetrics"
            ],
            "Resource": "*"
        }
    ]
}
```

### 애플리케이션 실행

#### 1. 프로젝트 빌드 및 실행
```bash
# 환경변수 설정 (매번 실행 시 필요)
export JAVA_HOME="/usr/local/opt/openjdk@17"
export PATH="/usr/local/opt/openjdk@17/bin:$PATH"

# 프로젝트 컴파일
mvn clean compile

# 애플리케이션 실행
mvn spring-boot:run
```

#### 2. IDE에서 실행 (권장)
- **IntelliJ IDEA**: `Ec2MonitoringApplication.java` 우클릭 → Run
- **VS Code**: Spring Boot Extension Pack 설치 후 실행

#### 3. 실행 확인
```bash
# 헬스체크 확인
curl http://localhost:8080/api/actuator/health

# 응답 예시:
# {"status":"UP","components":{"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

**서버 실행 주소**: `http://localhost:8080`    

## API 엔드포인트

### EC2 인스턴스 조회

#### 모든 인스턴스 목록
```http
GET /api/ec2/instances
```

#### 실행 중인 인스턴스만
```http
GET /api/ec2/instances/running
```

#### 특정 인스턴스 정보
```http
GET /api/ec2/instances/{instanceId}
```

### CloudWatch 메트릭 조회

#### CPU 사용률
```http
GET /api/ec2/instances/{instanceId}/metrics/cpu?period=300&hours=1
```

#### 네트워크 메트릭 (입력/출력)
```http
GET /api/ec2/instances/{instanceId}/metrics/network?period=300&hours=1
```

#### 디스크 I/O 메트릭
```http
GET /api/ec2/instances/{instanceId}/metrics/disk?period=300&hours=1
```

#### 모든 메트릭 통합 조회
```http
GET /api/ec2/instances/{instanceId}/metrics?period=300&hours=1
```

#### 24시간 사용량 요약
```http
GET /api/ec2/instances/{instanceId}/usage-summary
```

### 요청 파라미터

- `period`: 집계 기간 (초 단위, 기본값: 300 = 5분)
- `hours`: 조회할 시간 범위 (시간 단위, 기본값: 1)

## 설정

### .env 파일 설정 예시

```env
# AWS 설정
AWS_REGION=ap-northeast-2
AWS_PROFILE=default

# EC2 인스턴스 정보 (실제 값으로 변경 필요)
EC2_INSTANCE_ID=i-0da1b71ae6f874a24
EC2_INSTANCE_NAME=apitest
EC2_INSTANCE_TYPE=t3.micro
EC2_AMI_ID=ami-0634f3c109dcdc659
EC2_PLATFORM=Linux/UNIX
EC2_VCPU_COUNT=2

# CloudWatch 설정
CLOUDWATCH_DEFAULT_PERIOD=300
CLOUDWATCH_MAX_DATAPOINTS=1440

# 서버 설정
SERVER_PORT=8080
CONTEXT_PATH=/api

# 로깅 레벨
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
```

### application.yml 주요 설정

```yaml
# 환경변수 참조 방식으로 설정
aws:
  region: ${AWS_REGION:ap-northeast-2}
  profile: ${AWS_PROFILE:default}
  test-instance:
    id: ${EC2_INSTANCE_ID:}
    name: ${EC2_INSTANCE_NAME:}
    type: ${EC2_INSTANCE_TYPE:}
  cloudwatch:
    default-period: ${CLOUDWATCH_DEFAULT_PERIOD:300}
    max-datapoints: ${CLOUDWATCH_MAX_DATAPOINTS:1440}

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${CONTEXT_PATH:/api}
```

## 고려사항

### 보안 및 설정 관리
- **민감한 정보 관리**: 실제 인스턴스 정보는 `.env` 파일에서 관리하며, 이 파일은 `.gitignore`에 포함되어 git에 추적되지 않습니다
- **환경별 설정**: 개발/운영 환경에 따라 각각의 `.env` 파일을 생성하여 사용하세요
- **AWS 자격증명**: AWS CLI 프로필이나 IAM Role을 통한 안전한 자격증명 사용

### CloudWatch 과금 구조
- **기본 모니터링 (무료)**: 5분 간격 기본 지표
- **상세 모니터링 (유료)**: 1분 간격, 인스턴스당 약 $2.10/월
- **커스텀 지표**: $0.30/지표/월 (첫 10개 무료)
- **알람**: $0.10/알람/월

### 비용 최적화 팁
- 기본 지표만 사용하여 추가 비용 없이 모니터링
- 필요한 경우에만 상세 모니터링 활성화
- API 호출 빈도 조절로 요청 비용 관리

## 프로젝트 구조

```
aws/
├── .env                           # 실제 인스턴스 정보 (git 추적 안됨)
├── .env.example                   # 설정 템플릿
├── .gitignore                     # 민감한 파일 제외
├── pom.xml                        # Maven 의존성 설정
├── README.md                      # 프로젝트 문서
└── src/main/
    ├── java/com/aws/ec2monitoring/
    │   ├── Ec2MonitoringApplication.java    # 메인 애플리케이션 (.env 로드)
    │   ├── config/
    │   │   ├── AwsConfig.java              # AWS 클라이언트 설정
    │   │   └── AwsProperties.java          # 타입 안전한 설정 클래스
    │   ├── controller/
    │   │   └── Ec2Controller.java          # REST API 컨트롤러
    │   ├── service/
    │   │   ├── Ec2Service.java             # EC2 서비스 로직
    │   │   └── CloudWatchService.java      # CloudWatch 서비스 로직
    │   ├── dto/
    │   │   ├── InstanceDto.java            # 인스턴스 정보 DTO
    │   │   ├── MetricDto.java              # 메트릭 데이터 DTO
    │   │   └── UsageSummaryDto.java        # 사용량 요약 DTO
    │   └── exception/
    │       └── GlobalExceptionHandler.java # 전역 예외 처리
    └── resources/
        ├── application.yml                  # 메인 설정 (환경변수 참조)
        └── application-local.yml            # 개발용 설정
```

## 사용 예시

### 1. 헬스체크
```bash
curl -X GET "http://localhost:8080/api/actuator/health"
# 응답: {"status":"UP","components":{"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```

### 2. 모든 인스턴스 목록 조회
```bash
curl -X GET "http://localhost:8080/api/ec2/instances"
```

### 3. 실행 중인 인스턴스만 조회
```bash
curl -X GET "http://localhost:8080/api/ec2/instances/running"
```

### 4. 특정 인스턴스 정보 조회 (예시: apitest 인스턴스)
```bash
curl -X GET "http://localhost:8080/api/ec2/instances/i-0da1b71ae6f874a24"
```

### 5. CPU 메트릭 조회 (최근 2시간, 5분 간격)
```bash
curl -X GET "http://localhost:8080/api/ec2/instances/i-0da1b71ae6f874a24/metrics/cpu?period=300&hours=2"
```

### 6. 네트워크 메트릭 조회
```bash
curl -X GET "http://localhost:8080/api/ec2/instances/i-0da1b71ae6f874a24/metrics/network?period=300&hours=1"
```

### 7. 24시간 사용량 요약 조회
```bash
curl -X GET "http://localhost:8080/api/ec2/instances/i-0da1b71ae6f874a24/usage-summary"
```

### 8. 모든 메트릭 통합 조회
```bash
curl -X GET "http://localhost:8080/api/ec2/instances/i-0da1b71ae6f874a24/metrics?period=300&hours=1"
```

## 확장 계획

- **다른 AWS 서비스 지원**: RDS, Lambda 등
- **실시간 알림 기능**: 임계값 초과 시 알림
- **데이터 캐싱**: Redis를 통한 성능 최적화
- **배치 처리**: 주기적인 데이터 수집 및 저장
- **대시보드**: 웹 기반 모니터링 UI

## 기술 스택

### 백엔드
- **Spring Boot 3.2.0**: 웹 애플리케이션 프레임워크
- **Java 17**: OpenJDK 17 (LTS)
- **Maven 3.9+**: 빌드 및 의존성 관리

### AWS SDK
- **AWS SDK for Java 2.21.29**: EC2 및 CloudWatch API 클라이언트
- **AWS CLI**: 자격 증명 설정

### 라이브러리
- **Lombok**: 코드 간소화 (어노테이션 기반)
- **Jackson**: JSON 직렬화/역직렬화
- **dotenv-java 3.0.0**: .env 파일 로드
- **Spring Boot Actuator**: 헬스체크 및 모니터링

### 개발 환경
- **macOS**: Homebrew를 통한 패키지 관리