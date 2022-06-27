package com.azure.storage.datamover.s3;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferMethod;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

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
    protected Set<TransferMethod> getIncomingTransferMethods() {
        return Collections.singleton(TransferMethod.STREAMING);
    }

    @Override
    protected Set<TransferMethod> getOutgoingTransferMethods() {
        return Collections.singleton(TransferMethod.STREAMING);
    }
}
