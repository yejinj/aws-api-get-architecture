package com.aws.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rds")
public class RdsController {

    @Autowired
    private RdsClient rdsClient;
    
    @Autowired
    private CloudWatchClient cloudWatchClient;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            rdsClient.describeDBInstances(DescribeDbInstancesRequest.builder().maxRecords(20).build());
            health.put("status", "UP");
            health.put("service", "RDS");
            health.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("service", "RDS");
            health.put("error", e.getMessage());
            health.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(503).body(health);
        }
    }

    @GetMapping("/instances")
    public ResponseEntity<List<Map<String, Object>>> listAllInstances() {
        try {
            DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder().build();
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);

            List<Map<String, Object>> instances = response.dbInstances().stream()
                    .map(this::toInstanceMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/instances/available")
    public ResponseEntity<List<Map<String, Object>>> listAvailableInstances() {
        try {
            DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder().build();
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);

            List<Map<String, Object>> instances = response.dbInstances().stream()
                    .filter(instance -> "available".equals(instance.dbInstanceStatus()))
                    .map(this::toInstanceMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/instances/{instanceId}/cpu")
    public ResponseEntity<Map<String, Object>> getCpuMetrics(
            @PathVariable String instanceId,
            @RequestParam(defaultValue = "300") Integer period,
            @RequestParam(defaultValue = "1") Integer hours) {

        try {
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/RDS")
                    .metricName("CPUUtilization")
                    .dimensions(Dimension.builder()
                            .name("DBInstanceIdentifier")
                            .value(instanceId)
                            .build())
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM)
                    .period(period)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

            Map<String, Object> result = new HashMap<>();
            result.put("instanceId", instanceId);
            result.put("metricName", "CPUUtilization");
            result.put("unit", "Percent");
            result.put("period", period);
            result.put("dataPointCount", response.datapoints().size());

            response.datapoints().stream()
                    .max(java.util.Comparator.comparing(software.amazon.awssdk.services.cloudwatch.model.Datapoint::timestamp))
                    .ifPresent(dp -> {
                        Map<String, Object> latestCpu = new HashMap<>();
                        latestCpu.put("timestamp", dp.timestamp());
                        latestCpu.put("average", dp.average());
                        latestCpu.put("maximum", dp.maximum());
                        result.put("latestCpu", latestCpu);
                    });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "RDS CPU 메트릭 조회 실패: " + e.getMessage());
            error.put("instanceId", instanceId);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/instances/{instanceId}/connections")
    public ResponseEntity<Map<String, Object>> getConnectionMetrics(
            @PathVariable String instanceId,
            @RequestParam(defaultValue = "300") Integer period,
            @RequestParam(defaultValue = "1") Integer hours) {

        try {
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

            GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/RDS")
                    .metricName("DatabaseConnections")
                    .dimensions(Dimension.builder()
                            .name("DBInstanceIdentifier")
                            .value(instanceId)
                            .build())
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM)
                    .period(period)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);

            Map<String, Object> result = new HashMap<>();
            result.put("instanceId", instanceId);
            result.put("metricName", "DatabaseConnections");
            result.put("unit", "Count");
            result.put("period", period);
            result.put("dataPointCount", response.datapoints().size());

            response.datapoints().stream()
                    .max(java.util.Comparator.comparing(software.amazon.awssdk.services.cloudwatch.model.Datapoint::timestamp))
                    .ifPresent(dp -> {
                        Map<String, Object> latestConnections = new HashMap<>();
                        latestConnections.put("timestamp", dp.timestamp());
                        latestConnections.put("average", dp.average());
                        latestConnections.put("maximum", dp.maximum());
                        result.put("latestConnections", latestConnections);
                    });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "RDS 연결 메트릭 조회 실패: " + e.getMessage());
            error.put("instanceId", instanceId);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/instances/{instanceId}/iops")
    public ResponseEntity<Map<String, Object>> getIopsMetrics(
            @PathVariable String instanceId,
            @RequestParam(defaultValue = "300") Integer period,
            @RequestParam(defaultValue = "1") Integer hours) {

        try {
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

            // Read IOPS
            GetMetricStatisticsRequest readIopsRequest = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/RDS")
                    .metricName("ReadIOPS")
                    .dimensions(Dimension.builder()
                            .name("DBInstanceIdentifier")
                            .value(instanceId)
                            .build())
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM)
                    .period(period)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            // Write IOPS
            GetMetricStatisticsRequest writeIopsRequest = GetMetricStatisticsRequest.builder()
                    .namespace("AWS/RDS")
                    .metricName("WriteIOPS")
                    .dimensions(Dimension.builder()
                            .name("DBInstanceIdentifier")
                            .value(instanceId)
                            .build())
                    .statistics(Statistic.AVERAGE, Statistic.MAXIMUM)
                    .period(period)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            GetMetricStatisticsResponse readIopsResponse = cloudWatchClient.getMetricStatistics(readIopsRequest);
            GetMetricStatisticsResponse writeIopsResponse = cloudWatchClient.getMetricStatistics(writeIopsRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("instanceId", instanceId);
            result.put("period", period);
            result.put("readIopsCount", readIopsResponse.datapoints().size());
            result.put("writeIopsCount", writeIopsResponse.datapoints().size());

            readIopsResponse.datapoints().stream()
                    .max(java.util.Comparator.comparing(software.amazon.awssdk.services.cloudwatch.model.Datapoint::timestamp))
                    .ifPresent(dp -> {
                        Map<String, Object> latestReadIops = new HashMap<>();
                        latestReadIops.put("timestamp", dp.timestamp());
                        latestReadIops.put("average", dp.average());
                        latestReadIops.put("maximum", dp.maximum());
                        result.put("latestReadIops", latestReadIops);
                    });

            writeIopsResponse.datapoints().stream()
                    .max(java.util.Comparator.comparing(software.amazon.awssdk.services.cloudwatch.model.Datapoint::timestamp))
                    .ifPresent(dp -> {
                        Map<String, Object> latestWriteIops = new HashMap<>();
                        latestWriteIops.put("timestamp", dp.timestamp());
                        latestWriteIops.put("average", dp.average());
                        latestWriteIops.put("maximum", dp.maximum());
                        result.put("latestWriteIops", latestWriteIops);
                    });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "RDS IOPS 메트릭 조회 실패: " + e.getMessage());
            error.put("instanceId", instanceId);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    private Map<String, Object> toInstanceMap(DBInstance instance) {
        Map<String, Object> map = new HashMap<>();
        map.put("instanceId", instance.dbInstanceIdentifier());
        map.put("instanceClass", instance.dbInstanceClass());
        map.put("engine", instance.engine());
        map.put("engineVersion", instance.engineVersion());
        map.put("status", instance.dbInstanceStatus());
        map.put("endpoint", instance.endpoint() != null ? instance.endpoint().address() : null);
        map.put("port", instance.endpoint() != null ? instance.endpoint().port() : null);
        map.put("masterUsername", instance.masterUsername());
        map.put("availabilityZone", instance.availabilityZone());
        map.put("multiAZ", instance.multiAZ());
        map.put("publiclyAccessible", instance.publiclyAccessible());
        map.put("storageType", instance.storageType());
        map.put("allocatedStorage", instance.allocatedStorage());
        map.put("instanceCreateTime", instance.instanceCreateTime());
        return map;
    }
}
