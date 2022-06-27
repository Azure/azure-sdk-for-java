package com.azure.storage.datamover.s3;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class S3ObjectResource extends StorageResource {

    private final S3Client s3Client;
    private final String bucketName;
    private final String objectKey;

    S3ObjectResource(S3Client s3Client, String bucketName, String objectKey) {
        this.s3Client = Objects.requireNonNull(s3Client);
        this.bucketName = Objects.requireNonNull(bucketName);
        this.objectKey = Objects.requireNonNull(objectKey);
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected TransferCapabilities getOutgoingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected InputStream openInputStream() {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        return s3Client.getObject(getObjectRequest);
    }

    @Override
    protected long getLength() {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();
        return s3Client.headObject(headObjectRequest).contentLength();
    }

    @Override
    protected void consumeInputStream(InputStream inputStream, long length) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        s3Client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, length));
    }

    @Override
    protected List<String> getPath() {
        return Collections.singletonList(objectKey);
    }
}
