package com.example.toongallery.domain.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import static org.springframework.http.MediaType.*;
@Component
@RequiredArgsConstructor
public class StorageService {

    @Value("${spring.cloud.aws.s3.bucketName}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    private final S3Client s3Client;

    public String upload(MultipartFile file, String path, String filename) throws IOException {
        // 확장자 검사
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/png") && !contentType.startsWith("image/jpeg"))) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (png, jpeg만 허용)");
        }

        // 경로/파일명 조합
        String key = path.endsWith("/") ? path + filename : path + "/" + filename;

        // S3 업로드 요청 구성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                //.acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // S3 URL 반환
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

}
