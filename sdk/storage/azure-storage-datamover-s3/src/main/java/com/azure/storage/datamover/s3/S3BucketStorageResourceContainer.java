package com.azure.storage.datamover.s3;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class S3BucketStorageResourceContainer implements StorageResourceContainer {

    private final S3Client s3Client;
    private final String bucketName;

    S3BucketStorageResourceContainer(S3Client s3Client, String bucketName) {
        this.s3Client = Objects.requireNonNull(s3Client);
        this.bucketName = Objects.requireNonNull(bucketName);
    }

    @Override
    public Iterable<StorageResource> listResources() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .build();
        return s3Client.listObjectsV2Paginator(request)
            .stream()
            .flatMap(response -> response.contents()
                .stream()
                .map(s3Object -> new S3ObjectStorageResource(s3Client, bucketName, s3Object.key())))
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getPath() {
        return Collections.emptyList();
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        String objectKey = String.join("/", path);
        return new S3ObjectStorageResource(s3Client, bucketName, objectKey);
    }

    @Override
    public StorageResourceContainer getStorageResourceContainer(List<String> path) {
        throw new UnsupportedOperationException("Virtual directories not supported yet");
    }
}
