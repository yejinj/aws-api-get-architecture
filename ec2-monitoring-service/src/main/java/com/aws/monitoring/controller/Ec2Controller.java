package com.aws.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ec2")
public class Ec2Controller {

    @Autowired
    private Ec2Client ec2Client;
    
    @Autowired
    private CloudWatchClient cloudWatchClient;

    @GetMapping("/instances")
    public ResponseEntity<List<Map<String, Object>>> listAllInstances() {
        try {
            DescribeInstancesResponse response = ec2Client.describeInstances();
            
            List<Map<String, Object>> instances = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(this::toInstanceMap)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/instances/running")
    public ResponseEntity<List<Map<String, Object>>> listRunningInstances() {
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .filters(Filter.builder()
                            .name("instance-state-name")
                            .values("running")
                            .build())
                    .build();
                    
            DescribeInstancesResponse response = ec2Client.describeInstances(request);
            
            List<Map<String, Object>> instances = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
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
                    .namespace("AWS/EC2")
                    .metricName("CPUUtilization")
                    .dimensions(Dimension.builder()
                            .name("InstanceId")
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
            result.put("dataPoints", response.datapoints().stream()
                    .map(dp -> Map.of(
                            "timestamp", dp.timestamp(),
                            "average", dp.average(),
                            "maximum", dp.maximum()
                    ))
                    .sorted((p1, p2) -> ((Instant) p1.get("timestamp")).compareTo((Instant) p2.get("timestamp")))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/instances/{instanceId}/network")
    public ResponseEntity<Map<String, Object>> getNetworkMetrics(
            @PathVariable String instanceId,
            @RequestParam(defaultValue = "300") Integer period,
            @RequestParam(defaultValue = "1") Integer hours) {
        
        try {
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(hours, ChronoUnit.HOURS);

            Map<String, Object> result = new HashMap<>();
            result.put("instanceId", instanceId);
            result.put("period", period);
            
            // NetworkIn
            GetMetricStatisticsResponse networkInResponse = cloudWatchClient.getMetricStatistics(
                    GetMetricStatisticsRequest.builder()
                            .namespace("AWS/EC2")
                            .metricName("NetworkIn")
                            .dimensions(Dimension.builder().name("InstanceId").value(instanceId).build())
                            .statistics(Statistic.AVERAGE, Statistic.SUM)
                            .period(period)
                            .startTime(startTime)
                            .endTime(endTime)
                            .build());

            // NetworkOut
            GetMetricStatisticsResponse networkOutResponse = cloudWatchClient.getMetricStatistics(
                    GetMetricStatisticsRequest.builder()
                            .namespace("AWS/EC2")
                            .metricName("NetworkOut")
                            .dimensions(Dimension.builder().name("InstanceId").value(instanceId).build())
                            .statistics(Statistic.AVERAGE, Statistic.SUM)
                            .period(period)
                            .startTime(startTime)
                            .endTime(endTime)
                            .build());

            result.put("networkIn", networkInResponse.datapoints().stream()
                    .map(dp -> Map.of("timestamp", dp.timestamp(), "average", dp.average(), "sum", dp.sum()))
                    .sorted((p1, p2) -> ((Instant) p1.get("timestamp")).compareTo((Instant) p2.get("timestamp")))
                    .collect(Collectors.toList()));

            result.put("networkOut", networkOutResponse.datapoints().stream()
                    .map(dp -> Map.of("timestamp", dp.timestamp(), "average", dp.average(), "sum", dp.sum()))
                    .sorted((p1, p2) -> ((Instant) p1.get("timestamp")).compareTo((Instant) p2.get("timestamp")))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            ec2Client.describeInstances(DescribeInstancesRequest.builder().maxResults(5).build());
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "service", "EC2",
                    "timestamp", Instant.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "service", "EC2",
                    "error", e.getMessage(),
                    "timestamp", Instant.now()
            ));
        }
    }

    private Map<String, Object> toInstanceMap(Instance instance) {
        String name = instance.tags().stream()
                .filter(tag -> "Name".equals(tag.key()))
                .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                .findFirst()
                .orElse("");

        Map<String, Object> map = new HashMap<>();
        map.put("instanceId", instance.instanceId());
        map.put("instanceType", instance.instanceType().toString());
        map.put("state", instance.state().name().toString());
        map.put("publicIp", instance.publicIpAddress());
        map.put("privateIp", instance.privateIpAddress());
        map.put("launchTime", instance.launchTime());
        map.put("name", name);
        return map;
    }
}
