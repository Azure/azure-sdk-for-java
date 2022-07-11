package com.azure.storage.datamover.s3;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import software.amazon.awssdk.services.s3.S3Client;

public final class S3StorageResources {

    private S3StorageResources() {
    }

    public static StorageResource s3Object(S3Client client, String bucketName, String key) {
        return new S3ObjectStorageResource(client, bucketName, key);
    }

    public static StorageResourceContainer s3Bucket(S3Client client, String bucketName) {
        return new S3BucketStorageResourceContainer(client, bucketName);
    }
}
