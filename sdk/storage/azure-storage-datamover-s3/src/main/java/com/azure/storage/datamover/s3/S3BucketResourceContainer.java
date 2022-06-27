package com.azure.storage.datamover.s3;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class S3BucketResourceContainer extends StorageResourceContainer {

    private final S3Client s3Client;
    private final String bucketName;

    S3BucketResourceContainer(S3Client s3Client, String bucketName) {
        this.s3Client = Objects.requireNonNull(s3Client);
        this.bucketName = Objects.requireNonNull(bucketName);
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .build();
        return s3Client.listObjectsV2Paginator(request)
            .stream()
            .flatMap(response -> response.contents()
                .stream()
                .map(s3Object -> new S3ObjectResource(s3Client, bucketName, s3Object.key())))
            .collect(Collectors.toList());
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected List<String> getPath() {
        return Collections.emptyList();
    }

    @Override
    protected StorageResource getStorageResource(List<String> path) {
        String objectKey = String.join("/", path);
        return new S3ObjectResource(s3Client, bucketName, objectKey);
    }
}
