package com.aws.ec2monitoring.service;

import com.aws.ec2monitoring.config.AwsProperties;
import com.aws.ec2monitoring.dto.MetricDto;
import com.aws.ec2monitoring.dto.UsageSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CloudWatch 메트릭 관련 서비스
 * AWS CloudWatch API를 통해 EC2 인스턴스의 메트릭 데이터를 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudWatchService {

    private final CloudWatchClient cloudWatchClient;
    private final AwsProperties awsProperties;

    /**
     * CPU 사용률 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초, 예: 300 = 5분)
     * @param hours 조회할 시간 범위 (시간)
     * @return CPU 사용률 메트릭 데이터
     */
    public MetricDto getCpuUtilization(String instanceId, Integer period, Integer hours) {
        log.info("CPU 사용률 메트릭 조회: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        return getMetric(instanceId, "CPUUtilization", "Percent", period, hours);
    }

    /**
     * 네트워크 입력 트래픽 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초)
     * @param hours 조회할 시간 범위 (시간)
     * @return 네트워크 입력 메트릭 데이터
     */
    public MetricDto getNetworkIn(String instanceId, Integer period, Integer hours) {
        log.info("네트워크 입력 메트릭 조회: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        return getMetric(instanceId, "NetworkIn", "Bytes", period, hours);
    }

    /**
     * 네트워크 출력 트래픽 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초)
     * @param hours 조회할 시간 범위 (시간)
     * @return 네트워크 출력 메트릭 데이터
     */
    public MetricDto getNetworkOut(String instanceId, Integer period, Integer hours) {
        log.info("네트워크 출력 메트릭 조회: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        return getMetric(instanceId, "NetworkOut", "Bytes", period, hours);
    }

    /**
     * 디스크 읽기 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초)
     * @param hours 조회할 시간 범위 (시간)
     * @return 디스크 읽기 메트릭 데이터
     */
    public MetricDto getDiskReadBytes(String instanceId, Integer period, Integer hours) {
        log.info("디스크 읽기 메트릭 조회: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        return getMetric(instanceId, "DiskReadBytes", "Bytes", period, hours);
    }

    /**
     * 디스크 쓰기 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초)
     * @param hours 조회할 시간 범위 (시간)
     * @return 디스크 쓰기 메트릭 데이터
     */
    public MetricDto getDiskWriteBytes(String instanceId, Integer period, Integer hours) {
        log.info("디스크 쓰기 메트릭 조회: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        return getMetric(instanceId, "DiskWriteBytes", "Bytes", period, hours);
    }

    /**
     * 인스턴스의 최근 24시간 자원 사용량 요약 조회
     * 
     * @param instanceId 인스턴스 ID
     * @return 자원 사용량 요약 정보
     */
    public UsageSummaryDto getUsageSummary(String instanceId) {
        log.info("자원 사용량 요약 조회: 인스턴스={}", instanceId);
        
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(24, ChronoUnit.HOURS);
        
        try {
            // 각 메트릭별로 요약 통계 조회
            UsageSummaryDto.ResourceUsage cpuUsage = getResourceUsageSummary(instanceId, "CPUUtilization", "Percent", startTime, endTime);
            UsageSummaryDto.ResourceUsage networkIn = getResourceUsageSummary(instanceId, "NetworkIn", "Bytes", startTime, endTime);
            UsageSummaryDto.ResourceUsage networkOut = getResourceUsageSummary(instanceId, "NetworkOut", "Bytes", startTime, endTime);
            UsageSummaryDto.ResourceUsage diskRead = getResourceUsageSummary(instanceId, "DiskReadBytes", "Bytes", startTime, endTime);
            UsageSummaryDto.ResourceUsage diskWrite = getResourceUsageSummary(instanceId, "DiskWriteBytes", "Bytes", startTime, endTime);

            return UsageSummaryDto.builder()
                    .instanceId(instanceId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .cpuUsage(cpuUsage)
                    .networkIn(networkIn)
                    .networkOut(networkOut)
                    .diskRead(diskRead)
                    .diskWrite(diskWrite)
                    .build();

        } catch (Exception e) {
            log.error("자원 사용량 요약 조회 실패: 인스턴스={}", instanceId, e);
            throw new RuntimeException("자원 사용량 요약 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 공통 메트릭 조회 메서드
     * 
     * @param instanceId 인스턴스 ID
     * @param metricName 메트릭 이름
     * @param unit 단위
     * @param period 집계 기간 (초)
     * @param hours 조회할 시간 범위 (시간)
     * @return 메트릭 데이터
     */
    private MetricDto getMetric(String instanceId, String metricName, String unit, Integer period, Integer hours) {
        // 기본값 설정
        if (period == null) period = awsProperties.getCloudwatch().getDefaultPeriod();
        if (hours == null) hours = 1;
        
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

        try {
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/EC2")
                    .metricName(metricName)
                    .dimensions(Dimension.builder()
                            .name("InstanceId")
                            .value(instanceId)
                            .build())
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM, Statistic.MINIMUM, Statistic.SUM, Statistic.SAMPLE_COUNT)
                    .period(period)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

            List<MetricDto.DataPointDto> dataPoints = response.datapoints().stream()
                    .map(dp -> MetricDto.DataPointDto.builder()
                            .timestamp(dp.timestamp())
                            .average(dp.average())
                            .maximum(dp.maximum())
                            .minimum(dp.minimum())
                            .sum(dp.sum())
                            .sampleCount(dp.sampleCount())
                            .unit(dp.unit() != null ? dp.unit().toString() : unit)
                            .build())
                    .sorted((dp1, dp2) -> dp1.getTimestamp().compareTo(dp2.getTimestamp()))
                    .collect(Collectors.toList());

            return MetricDto.builder()
                    .metricName(metricName)
                    .namespace("AWS/EC2")
                    .instanceId(instanceId)
                    .unit(unit)
                    .dataPoints(dataPoints)
                    .startTime(startTime)
                    .endTime(endTime)
                    .period(period)
                    .build();

        } catch (Exception e) {
            log.error("메트릭 조회 실패: 인스턴스={}, 메트릭={}", instanceId, metricName, e);
            throw new RuntimeException("메트릭 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 메트릭의 요약 통계 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param metricName 메트릭 이름
     * @param unit 단위
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 자원 사용량 요약
     */
    private UsageSummaryDto.ResourceUsage getResourceUsageSummary(String instanceId, String metricName, String unit, Instant startTime, Instant endTime) {
        try {
            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/EC2")
                    .metricName(metricName)
                    .dimensions(Dimension.builder()
                            .name("InstanceId")
                            .value(instanceId)
                            .build())
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM, Statistic.MINIMUM)
                    .period(3600) // 1시간 단위로 집계
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
            List<Datapoint> datapoints = response.datapoints();

            if (datapoints.isEmpty()) {
                return UsageSummaryDto.ResourceUsage.builder()
                        .average(0.0)
                        .maximum(0.0)
                        .minimum(0.0)
                        .unit(unit)
                        .dataPointCount(0)
                        .build();
            }

            double avgSum = datapoints.stream().mapToDouble(Datapoint::average).sum();
            double avgAverage = avgSum / datapoints.size();
            double maximum = datapoints.stream().mapToDouble(Datapoint::maximum).max().orElse(0.0);
            double minimum = datapoints.stream().mapToDouble(Datapoint::minimum).min().orElse(0.0);

            return UsageSummaryDto.ResourceUsage.builder()
                    .average(avgAverage)
                    .maximum(maximum)
                    .minimum(minimum)
                    .unit(unit)
                    .dataPointCount(datapoints.size())
                    .build();

        } catch (Exception e) {
            log.warn("메트릭 요약 조회 실패: 인스턴스={}, 메트릭={}", instanceId, metricName, e);
            return UsageSummaryDto.ResourceUsage.builder()
                    .average(0.0)
                    .maximum(0.0)
                    .minimum(0.0)
                    .unit(unit)
                    .dataPointCount(0)
                    .build();
        }
    }
}
