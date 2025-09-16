package com.aws.ec2monitoring.service;

import com.aws.ec2monitoring.dto.InstanceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EC2 인스턴스 관련 서비스
 * AWS EC2 API를 통해 인스턴스 정보를 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Ec2Service {

    private final Ec2Client ec2Client;

    /**
     * 계정 내 모든 EC2 인스턴스 목록 조회
     * 
     * @return 인스턴스 정보 목록
     */
    public List<InstanceDto> listAllInstances() {
        log.info("모든 EC2 인스턴스 목록 조회 시작");
        
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            List<InstanceDto> instances = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(this::convertToInstanceDto)
                    .collect(Collectors.toList());

            log.info("EC2 인스턴스 {}개 조회 완료", instances.size());
            return instances;

        } catch (Exception e) {
            log.error("EC2 인스턴스 목록 조회 실패", e);
            throw new RuntimeException("EC2 인스턴스 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 인스턴스 정보 조회
     * 
     * @param instanceId 인스턴스 ID
     * @return 인스턴스 정보
     */
    public InstanceDto getInstanceById(String instanceId) {
        log.info("인스턴스 정보 조회: {}", instanceId);
        
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            
            DescribeInstancesResponse response = ec2Client.describeInstances(request);
            
            return response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .findFirst()
                    .map(this::convertToInstanceDto)
                    .orElseThrow(() -> new RuntimeException("인스턴스를 찾을 수 없습니다: " + instanceId));
                    
        } catch (Exception e) {
            log.error("인스턴스 정보 조회 실패: {}", instanceId, e);
            throw new RuntimeException("인스턴스 정보 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 실행 중인 인스턴스만 조회
     * 
     * @return 실행 중인 인스턴스 목록
     */
    public List<InstanceDto> listRunningInstances() {
        log.info("실행 중인 EC2 인스턴스 목록 조회 시작");
        
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .filters(Filter.builder()
                            .name("instance-state-name")
                            .values("running")
                            .build())
                    .build();
                    
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            List<InstanceDto> instances = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(this::convertToInstanceDto)
                    .collect(Collectors.toList());

            log.info("실행 중인 EC2 인스턴스 {}개 조회 완료", instances.size());
            return instances;

        } catch (Exception e) {
            log.error("실행 중인 EC2 인스턴스 목록 조회 실패", e);
            throw new RuntimeException("실행 중인 EC2 인스턴스 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Instance 객체를 InstanceDto로 변환
     * 
     * @param instance AWS SDK Instance 객체
     * @return InstanceDto 객체
     */
    private InstanceDto convertToInstanceDto(Instance instance) {
        // 태그를 Map으로 변환
        Map<String, String> tags = instance.tags().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));
        
        // Name 태그에서 인스턴스 이름 추출
        String instanceName = tags.getOrDefault("Name", "");
        
        // 보안 그룹 정보 변환
        List<InstanceDto.SecurityGroupDto> securityGroups = instance.securityGroups().stream()
                .map(sg -> InstanceDto.SecurityGroupDto.builder()
                        .groupId(sg.groupId())
                        .groupName(sg.groupName())
                        .build())
                .collect(Collectors.toList());

        return InstanceDto.builder()
                .instanceId(instance.instanceId())
                .instanceType(instance.instanceType().toString())
                .state(instance.state().name().toString())
                .availabilityZone(instance.placement().availabilityZone())
                .publicIpAddress(instance.publicIpAddress())
                .privateIpAddress(instance.privateIpAddress())
                .launchTime(instance.launchTime())
                .platform(instance.platform() != null ? instance.platform().toString() : "Linux")
                .vpcId(instance.vpcId())
                .subnetId(instance.subnetId())
                .securityGroups(securityGroups)
                .tags(tags)
                .name(instanceName)
                .monitoring(instance.monitoring().state().toString())
                .build();
    }
}
