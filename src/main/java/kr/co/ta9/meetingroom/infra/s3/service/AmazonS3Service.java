package kr.co.ta9.meetingroom.infra.s3.service;

import kr.co.ta9.meetingroom.global.error.code.AmazonS3ErrorCode;
import kr.co.ta9.meetingroom.infra.s3.exception.AwsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AmazonS3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 단일 파일 업로드
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();

            String key = originalFilename + "-" + UUID.randomUUID();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .toExternalForm();
        } catch (S3Exception | IOException e) {
            throw new AwsException(AmazonS3ErrorCode.FAILED_TO_UPLOAD_FILE);
        }
    }

    // 파일 다중 업로드
    public List<String> uploadFiles(List<MultipartFile> files) {
        ArrayList<String> publicUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            publicUrls.add(uploadFile(file));
        }

        return publicUrls;
    }

    // 파일 삭제
    public void deleteFile(String publicUrl) {
        try {
            String key = extractObjectKeyFromPublicUrl(publicUrl);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            throw new AwsException(AmazonS3ErrorCode.FAILED_TO_DELETE_OBJECT);
        }
    }

    // 파일 다중 삭제
    public void deleteFiles(List<String> publicUrls) {
        for (String publicUrl : publicUrls) {
            deleteFile(publicUrl);
        }
    }

    // URL에서 객체 키 추출
    private String extractObjectKeyFromPublicUrl(String publicUrl) {
        URI uri;

        try {
            uri = URI.create(publicUrl);
        } catch (IllegalArgumentException e) {
            throw new AwsException(AmazonS3ErrorCode.FAILED_TO_DELETE_OBJECT);
        }

        String host = uri.getHost();
        String path = uri.getPath();

        if (host == null || path == null || path.isEmpty() || "/".equals(path)) {
            throw new AwsException(AmazonS3ErrorCode.FAILED_TO_DELETE_OBJECT);
        }

        String rawKey = path.startsWith("/") ? path.substring(1) : path;
        String hostLower = host.toLowerCase(Locale.ROOT);
        String bucketLower = bucketName.toLowerCase(Locale.ROOT);

        if (hostLower.startsWith(bucketLower + ".s3.")) {
            return URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
        }

        if (hostLower.startsWith("s3.") && hostLower.contains("amazonaws.com")) {
            int slash = rawKey.indexOf('/');
            if (slash > 0
                    && rawKey.substring(0, slash).equalsIgnoreCase(bucketName)
                    && slash < rawKey.length() - 1) {
                return URLDecoder.decode(rawKey.substring(slash + 1), StandardCharsets.UTF_8);
            }
        }

        throw new AwsException(AmazonS3ErrorCode.FAILED_TO_DELETE_OBJECT);
    }
}
