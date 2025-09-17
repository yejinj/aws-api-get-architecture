package com.aws.monitoring.controller;

import com.aws.monitoring.service.common.AwsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 서비스 상태 확인 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final List<AwsService<?>> awsServices;

    /**
     * 모든 AWS 서비스 상태 확인
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        log.info("서비스 상태 확인 요청");
        
        Map<String, Object> healthStatus = new HashMap<>();
        Map<String, Boolean> serviceStatus = new HashMap<>();
        
        boolean allHealthy = true;
        
        for (AwsService<?> service : awsServices) {
            try {
                boolean isHealthy = service.isServiceAvailable();
                serviceStatus.put(service.getServiceName(), isHealthy);
                if (!isHealthy) {
                    allHealthy = false;
                }
            } catch (Exception e) {
                log.error("{} 서비스 상태 확인 실패", service.getServiceName(), e);
                serviceStatus.put(service.getServiceName(), false);
                allHealthy = false;
            }
        }
        
        healthStatus.put("status", allHealthy ? "UP" : "DOWN");
        healthStatus.put("services", serviceStatus);
        healthStatus.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.ok(healthStatus);
    }
}
