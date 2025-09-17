package com.aws.monitoring.service.s3;

import com.aws.monitoring.dto.BucketDto;
import com.aws.monitoring.service.common.AbstractAwsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * S3 버킷 관련 서비스
 * AWS S3 API를 통해 버킷 정보를 조회
 */
@Service
@RequiredArgsConstructor
public class S3Service extends AbstractAwsService<BucketDto> {

    private final S3Client s3Client;

    @Override
    public String getServiceName() {
        return "S3";
    }

    @Override
    public List<BucketDto> listAllResources() {
        return listAllBuckets();
    }

    @Override
    public List<BucketDto> listActiveResources() {
        // S3 버킷은 모두 활성 상태로 간주
        return listAllBuckets();
    }

    @Override
    public BucketDto getResourceById(String resourceId) {
        return getBucketInfo(resourceId);
    }

    /**
     * 모든 S3 버킷 목록 조회
     */
    public List<BucketDto> listAllBuckets() {
        logOperation("모든 버킷 목록 조회");
        
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            
            List<BucketDto> buckets = response.buckets().stream()
                    .map(this::convertToBucketDto)
                    .collect(Collectors.toList());

            logResult("모든 버킷 목록 조회", buckets.size());
            return buckets;

        } catch (Exception e) {
            throw handleAwsException("버킷 목록 조회", e);
        }
    }

    /**
     * 특정 버킷 정보 조회
     */
    public BucketDto getBucketInfo(String bucketName) {
        logOperation("버킷 정보 조회", bucketName);
        
        try {
            // 버킷 존재 확인
            HeadBucketRequest headRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headRequest);

            // 버킷 위치 조회
            GetBucketLocationRequest locationRequest = GetBucketLocationRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetBucketLocationResponse locationResponse = s3Client.getBucketLocation(locationRequest);

            return BucketDto.builder()
                    .name(bucketName)
                    .region(locationResponse.locationConstraintAsString())
                    .build();

        } catch (Exception e) {
            throw handleAwsException("버킷 정보 조회", e);
        }
    }

    /**
     * Bucket 객체를 BucketDto로 변환
     */
    private BucketDto convertToBucketDto(Bucket bucket) {
        return BucketDto.builder()
                .name(bucket.name())
                .creationDate(bucket.creationDate())
                .build();
    }
}
