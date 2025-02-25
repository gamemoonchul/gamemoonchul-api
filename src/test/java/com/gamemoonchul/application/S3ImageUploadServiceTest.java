package com.gamemoonchul.application;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.gamemoonchul.TestDataBase;
import com.gamemoonchul.common.exception.BadRequestException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class S3ImageUploadServiceTest extends TestDataBase {

    private S3Service s3Service = super.s3Service();

    private String fileName = "iu.png";

    @Test
    @Order(0)
    @DisplayName("S3에 잘못된 양식업로드 테스트")
    void uploadWrongType() throws IOException {
        // given
        MultipartFile file;
        byte[] content = Files.readAllBytes(Paths.get("src/test/resources/test.mp4"));
        file = new MockMultipartFile(fileName, fileName, "video/mp4", content);

        // when // then
        assertThrows(BadRequestException.class, () -> {
            s3Service.uploadImage(file);
        });
    }

    @Test
    @Order(1)
    @DisplayName("S3에 파일 업로드 테스트")
    void upload() {
        // given
        MultipartFile file;
        try {
            byte[] content = Files.readAllBytes(Paths.get("src/test/resources/" + fileName));
            file = new MockMultipartFile(fileName, fileName, "image/png", content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // when
        fileName = s3Service.uploadImage(file);

        //then
        assertNotNull(fileName);
    }

    @Test
    @Order(2)
    @DisplayName("S3에 파일 삭제 테스트")
    void delete() {
        // given

        // when
        s3Service.delete(fileName);

        //then
        assertFalse(s3Service.isValidFile(fileName));
    }

    @Test
    @Order(3)
    @DisplayName("S3에 파일 존재하는지 확인")
    void isValidFile() {
        // given // when
        boolean result = s3Service.isValidFile(fileName);

        //then
        assertFalse(result);
    }
}