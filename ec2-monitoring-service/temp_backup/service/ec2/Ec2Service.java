package com.aws.monitoring.service.ec2;

import com.aws.monitoring.dto.InstanceDto;
import com.aws.monitoring.service.common.AbstractAwsService;
import lombok.RequiredArgsConstructor;
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
@Service
@RequiredArgsConstructor
public class Ec2Service extends AbstractAwsService<InstanceDto> {

    private final Ec2Client ec2Client;

    @Override
    public String getServiceName() {
        return "EC2";
    }

    @Override
    public List<InstanceDto> listAllResources() {
        return listAllInstances();
    }

    @Override
    public List<InstanceDto> listActiveResources() {
        return listRunningInstances();
    }

    @Override
    public InstanceDto getResourceById(String resourceId) {
        return getInstanceById(resourceId);
    }

    /**
     * 계정 내 모든 EC2 인스턴스 목록 조회
     */
    public List<InstanceDto> listAllInstances() {
        logOperation("모든 인스턴스 목록 조회");
        
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            List<InstanceDto> instances = response.reservations().stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(this::convertToInstanceDto)
                    .collect(Collectors.toList());

            logResult("모든 인스턴스 목록 조회", instances.size());
            return instances;

        } catch (Exception e) {
            throw handleAwsException("인스턴스 목록 조회", e);
        }
    }

    /**
     * 특정 인스턴스 정보 조회
     */
    public InstanceDto getInstanceById(String instanceId) {
        logOperation("인스턴스 정보 조회", instanceId);
        
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
            throw handleAwsException("인스턴스 정보 조회", e);
        }
    }

    /**
     * 실행 중인 인스턴스만 조회
     */
    public List<InstanceDto> listRunningInstances() {
        logOperation("실행 중인 인스턴스 목록 조회");
        
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

            logResult("실행 중인 인스턴스 목록 조회", instances.size());
            return instances;

        } catch (Exception e) {
            throw handleAwsException("실행 중인 인스턴스 목록 조회", e);
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
