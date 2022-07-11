package com.azure.storage.datamover.s3;

import com.azure.storage.common.resource.StorageResource;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class S3ObjectStorageResource implements StorageResource {

    private final S3Client s3Client;
    private final String bucketName;
    private final String objectKey;

    S3ObjectStorageResource(S3Client s3Client, String bucketName, String objectKey) {
        this.s3Client = Objects.requireNonNull(s3Client);
        this.bucketName = Objects.requireNonNull(bucketName);
        this.objectKey = Objects.requireNonNull(objectKey);
    }

    @Override
    public InputStream openInputStream() {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        return s3Client.getObject(getObjectRequest);
    }

    @Override
    public long getLength() {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();
        return s3Client.headObject(headObjectRequest).contentLength();
    }

    @Override
    public void consumeInputStream(InputStream inputStream, long length) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        s3Client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, length));
    }

    @Override
    public String getUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void consumeUri(String sasUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getPath() {
        return Collections.singletonList(objectKey);
    }

    @Override
    public boolean canConsumeStream() {
        return true;
    }

    @Override
    public boolean canProduceStream() {
        return true;
    }

    @Override
    public boolean canConsumeUri() {
        return false;
    }

    @Override
    public boolean canProduceUri() {
        return false;
    }
}
