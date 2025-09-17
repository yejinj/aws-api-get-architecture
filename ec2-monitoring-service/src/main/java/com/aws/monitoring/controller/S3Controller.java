package com.aws.monitoring.controller;

import com.aws.monitoring.dto.BucketDto;
import com.aws.monitoring.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * S3 버킷 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    /**
     * 모든 S3 버킷 목록 조회
     */
    @GetMapping("/buckets")
    public ResponseEntity<List<BucketDto>> listAllBuckets() {
        log.info("모든 S3 버킷 목록 조회 요청");
        
        try {
            List<BucketDto> buckets = s3Service.listAllBuckets();
            return ResponseEntity.ok(buckets);
        } catch (Exception e) {
            log.error("S3 버킷 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 버킷 정보 조회
     */
    @GetMapping("/buckets/{bucketName}")
    public ResponseEntity<BucketDto> getBucketInfo(@PathVariable String bucketName) {
        log.info("버킷 정보 조회 요청: {}", bucketName);
        
        try {
            BucketDto bucket = s3Service.getBucketInfo(bucketName);
            return ResponseEntity.ok(bucket);
        } catch (Exception e) {
            log.error("버킷 정보 조회 실패: {}", bucketName, e);
            return ResponseEntity.notFound().build();
        }
    }
}
