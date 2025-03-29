package com.example.toongallery.domain.common.util;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class FileUpLoader {

    @Value("${spring.cloud.aws.s3.bucketName}")
    private String bucket;

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    private final S3Client s3Client;

    public String upload(MultipartFile file, String path, String filename) {
        try {
            // 확장자 검사
            String contentType = validateType(file);

            // 경로/파일명 조합
            String key = path.endsWith("/") ? path + filename : path + "/" + filename;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return cloudFrontDomain + "/" + key;
        } catch (Exception e) {
            throw new BaseException(ErrorCode.SERVER_NOT_WORK, "S3 업로드 실패: " + e.getMessage());
        }
    }

    public void delete(String pathWithFilename) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(pathWithFilename)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.SERVER_NOT_WORK, "S3 삭제 실패: " + e.getMessage());
        }
    }

    private static String validateType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/png") &&
                !contentType.startsWith("image/jpeg") &&
                !contentType.startsWith("image/webp"))) {
            throw new BaseException(ErrorCode.UNSUPPORTED_FILE_TYPE,contentType);

        }
        return contentType;
    }
}
