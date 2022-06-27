package com.azure.storage.datamover.s3;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import software.amazon.awssdk.services.s3.S3Client;

public final class S3Resources {

    private S3Resources() {
    }

    public static StorageResource s3Object(S3Client client, String bucketName, String key) {
        return new S3ObjectResource(client, bucketName, key);
    }

    public static StorageResourceContainer s3Bucket(S3Client client, String bucketName) {
        return new S3BucketResourceContainer(client, bucketName);
    }
}
