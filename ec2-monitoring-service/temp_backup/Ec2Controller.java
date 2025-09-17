package com.aws.monitoring.controller;

import com.aws.monitoring.dto.InstanceDto;
import com.aws.monitoring.dto.MetricDto;
import com.aws.monitoring.dto.UsageSummaryDto;
import com.aws.monitoring.service.ec2.CloudWatchService;
import com.aws.monitoring.service.ec2.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EC2 모니터링 REST API 컨트롤러
 * EC2 인스턴스 정보 및 CloudWatch 메트릭 조회 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/ec2")
@RequiredArgsConstructor
public class Ec2Controller {

    private final Ec2Service ec2Service;
    private final CloudWatchService cloudWatchService;

    /**
     * 모든 EC2 인스턴스 목록 조회
     * 
     * @return 인스턴스 목록
     */
    @GetMapping("/instances")
    public ResponseEntity<List<InstanceDto>> listAllInstances() {
        log.info("모든 EC2 인스턴스 목록 조회 요청");
        
        try {
            List<InstanceDto> instances = ec2Service.listAllInstances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            log.error("EC2 인스턴스 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 실행 중인 EC2 인스턴스만 조회
     * 
     * @return 실행 중인 인스턴스 목록
     */
    @GetMapping("/instances/running")
    public ResponseEntity<List<InstanceDto>> listRunningInstances() {
        log.info("실행 중인 EC2 인스턴스 목록 조회 요청");
        
        try {
            List<InstanceDto> instances = ec2Service.listRunningInstances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            log.error("실행 중인 EC2 인스턴스 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 인스턴스 정보 조회
     * 
     * @param instanceId 인스턴스 ID
     * @return 인스턴스 정보
     */
    @GetMapping("/instances/{instanceId}")
    public ResponseEntity<InstanceDto> getInstanceById(@PathVariable String instanceId) {
        log.info("인스턴스 정보 조회 요청: {}", instanceId);
        
        try {
            InstanceDto instance = ec2Service.getInstanceById(instanceId);
            return ResponseEntity.ok(instance);
        } catch (Exception e) {
            log.error("인스턴스 정보 조회 실패: {}", instanceId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * CPU 사용률 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초, 기본값: 300)
     * @param hours 조회 시간 범위 (시간, 기본값: 1)
     * @return CPU 사용률 메트릭 데이터
     */
    @GetMapping("/instances/{instanceId}/metrics/cpu")
    public ResponseEntity<MetricDto> getCpuMetrics(
            @PathVariable String instanceId,
            @RequestParam(required = false, defaultValue = "300") Integer period,
            @RequestParam(required = false, defaultValue = "1") Integer hours) {
        
        log.info("CPU 메트릭 조회 요청: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        try {
            MetricDto metrics = cloudWatchService.getCpuUtilization(instanceId, period, hours);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("CPU 메트릭 조회 실패: {}", instanceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 네트워크 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초, 기본값: 300)
     * @param hours 조회 시간 범위 (시간, 기본값: 1)
     * @return 네트워크 메트릭 데이터 (입력/출력)
     */
    @GetMapping("/instances/{instanceId}/metrics/network")
    public ResponseEntity<List<MetricDto>> getNetworkMetrics(
            @PathVariable String instanceId,
            @RequestParam(required = false, defaultValue = "300") Integer period,
            @RequestParam(required = false, defaultValue = "1") Integer hours) {
        
        log.info("네트워크 메트릭 조회 요청: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        try {
            MetricDto networkIn = cloudWatchService.getNetworkIn(instanceId, period, hours);
            MetricDto networkOut = cloudWatchService.getNetworkOut(instanceId, period, hours);
            
            return ResponseEntity.ok(List.of(networkIn, networkOut));
        } catch (Exception e) {
            log.error("네트워크 메트릭 조회 실패: {}", instanceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 디스크 I/O 메트릭 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초, 기본값: 300)
     * @param hours 조회 시간 범위 (시간, 기본값: 1)
     * @return 디스크 I/O 메트릭 데이터 (읽기/쓰기)
     */
    @GetMapping("/instances/{instanceId}/metrics/disk")
    public ResponseEntity<List<MetricDto>> getDiskMetrics(
            @PathVariable String instanceId,
            @RequestParam(required = false, defaultValue = "300") Integer period,
            @RequestParam(required = false, defaultValue = "1") Integer hours) {
        
        log.info("디스크 메트릭 조회 요청: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        try {
            MetricDto diskRead = cloudWatchService.getDiskReadBytes(instanceId, period, hours);
            MetricDto diskWrite = cloudWatchService.getDiskWriteBytes(instanceId, period, hours);
            
            return ResponseEntity.ok(List.of(diskRead, diskWrite));
        } catch (Exception e) {
            log.error("디스크 메트릭 조회 실패: {}", instanceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 모든 메트릭 통합 조회
     * 
     * @param instanceId 인스턴스 ID
     * @param period 집계 기간 (초, 기본값: 300)
     * @param hours 조회 시간 범위 (시간, 기본값: 1)
     * @return 모든 메트릭 데이터
     */
    @GetMapping("/instances/{instanceId}/metrics")
    public ResponseEntity<List<MetricDto>> getAllMetrics(
            @PathVariable String instanceId,
            @RequestParam(required = false, defaultValue = "300") Integer period,
            @RequestParam(required = false, defaultValue = "1") Integer hours) {
        
        log.info("모든 메트릭 조회 요청: 인스턴스={}, 기간={}초, 범위={}시간", instanceId, period, hours);
        
        try {
            MetricDto cpu = cloudWatchService.getCpuUtilization(instanceId, period, hours);
            MetricDto networkIn = cloudWatchService.getNetworkIn(instanceId, period, hours);
            MetricDto networkOut = cloudWatchService.getNetworkOut(instanceId, period, hours);
            MetricDto diskRead = cloudWatchService.getDiskReadBytes(instanceId, period, hours);
            MetricDto diskWrite = cloudWatchService.getDiskWriteBytes(instanceId, period, hours);
            
            return ResponseEntity.ok(List.of(cpu, networkIn, networkOut, diskRead, diskWrite));
        } catch (Exception e) {
            log.error("모든 메트릭 조회 실패: {}", instanceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 최근 24시간 자원 사용량 요약 조회
     * 
     * @param instanceId 인스턴스 ID
     * @return 자원 사용량 요약 정보
     */
    @GetMapping("/instances/{instanceId}/usage-summary")
    public ResponseEntity<UsageSummaryDto> getUsageSummary(@PathVariable String instanceId) {
        log.info("자원 사용량 요약 조회 요청: {}", instanceId);
        
        try {
            UsageSummaryDto summary = cloudWatchService.getUsageSummary(instanceId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("자원 사용량 요약 조회 실패: {}", instanceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
