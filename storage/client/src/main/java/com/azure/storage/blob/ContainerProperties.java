package com.azure.storage.blob;

import com.azure.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.storage.blob.models.PublicAccessType;

import java.time.OffsetDateTime;

public class ContainerProperties {

    private PublicAccessType blobPublicAccess;

    private boolean hasImmutabilityPolicy;

    private boolean hasLegalHold;

    private OffsetDateTime lastModifiedTime;


    ContainerProperties(ContainerGetPropertiesHeaders generatedResponseHeaders) {
        this.blobPublicAccess = generatedResponseHeaders.blobPublicAccess();
        this.hasImmutabilityPolicy = generatedResponseHeaders.hasImmutabilityPolicy();
        this.hasLegalHold = generatedResponseHeaders.hasLegalHold();
        this.lastModifiedTime = generatedResponseHeaders.lastModified();
    }

    public PublicAccessType blobPublicAccess() {
        return blobPublicAccess;
    }

    public boolean hasImmutabilityPolicy() {
        return hasImmutabilityPolicy;
    }

    public boolean hasLegalHold() {
        return hasLegalHold;
    }

    public OffsetDateTime lastModifiedTime() {
        return lastModifiedTime;
    }
}
