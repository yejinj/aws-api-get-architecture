package com.aws.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 단순화된 EC2 컨트롤러 - 테스트용
 */
@RestController
@RequestMapping("/ec2")
public class SimpleEc2Controller {

    @Autowired
    private Ec2Client ec2Client;

    /**
     * 모든 EC2 인스턴스 목록 조회
     */
    @GetMapping("/instances")
    public ResponseEntity<List<Map<String, Object>>> listAllInstances() {
        try {
            DescribeInstancesResponse response = ec2Client.describeInstances();
            
            List<Map<String, Object>> instances = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(instance -> {
                        Map<String, Object> instanceMap = new HashMap<>();
                        instanceMap.put("instanceId", instance.instanceId());
                        instanceMap.put("instanceType", instance.instanceType().toString());
                        instanceMap.put("state", instance.state().name().toString());
                        instanceMap.put("publicIp", instance.publicIpAddress());
                        instanceMap.put("privateIp", instance.privateIpAddress());
                        instanceMap.put("launchTime", instance.launchTime());
                        
                        // Name 태그 찾기
                        String name = instance.tags().stream()
                                .filter(tag -> "Name".equals(tag.key()))
                                .map(Tag::value)
                                .findFirst()
                                .orElse("");
                        instanceMap.put("name", name);
                        
                        return instanceMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "EC2 인스턴스 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * 실행 중인 인스턴스만 조회
     */
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
                    .map(instance -> {
                        Map<String, Object> instanceMap = new HashMap<>();
                        instanceMap.put("instanceId", instance.instanceId());
                        instanceMap.put("instanceType", instance.instanceType().toString());
                        instanceMap.put("state", instance.state().name().toString());
                        instanceMap.put("publicIp", instance.publicIpAddress());
                        instanceMap.put("privateIp", instance.privateIpAddress());
                        instanceMap.put("launchTime", instance.launchTime());
                        
                        // Name 태그 찾기
                        String name = instance.tags().stream()
                                .filter(tag -> "Name".equals(tag.key()))
                                .map(Tag::value)
                                .findFirst()
                                .orElse("");
                        instanceMap.put("name", name);
                        
                        return instanceMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "실행 중인 EC2 인스턴스 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(List.of(error));
        }
    }

    /**
     * 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            // 간단한 EC2 연결 테스트
            ec2Client.describeInstances(DescribeInstancesRequest.builder().maxResults(5).build());
            health.put("status", "UP");
            health.put("service", "EC2");
            health.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("service", "EC2");
            health.put("error", e.getMessage());
            health.put("timestamp", java.time.Instant.now().toString());
            return ResponseEntity.status(503).body(health);
        }
    }
}
