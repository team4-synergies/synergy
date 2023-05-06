package com.synergies.synergy.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    //bucketName
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    public void uploadFile(String fileName, boolean manager, MultipartFile file) {
        String contentType = file.getContentType();
        System.out.println("contentType: " +contentType);
        String uploadPath;

        if(manager) {
            uploadPath = bucket + "/admin/";
        }
        else {
            uploadPath = bucket + "/student/";
        }

        //String fileName = '/' + userId + '/' + "main";

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            metadata.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            amazonS3.putObject(new PutObjectRequest(bucket, uploadPath+fileName, byteArrayInputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            byteArrayInputStream.close();
        } catch (AmazonServiceException | IOException e) {
            e.printStackTrace();
        }
    }
}