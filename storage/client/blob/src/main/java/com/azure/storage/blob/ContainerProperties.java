package com.azure.storage.blob;

import com.azure.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.storage.blob.models.PublicAccessType;

public class ContainerProperties {

    private PublicAccessType blobPublicAccess;

    private boolean hasImmutabilityPolicy;

    private boolean hasLegalHold;

    //todo decide datetime representation for last modified time


    ContainerProperties(ContainerGetPropertiesHeaders generatedResponseHeaders) {
        this.blobPublicAccess = generatedResponseHeaders.blobPublicAccess();
        this.hasImmutabilityPolicy = generatedResponseHeaders.hasImmutabilityPolicy();
        this.hasLegalHold = generatedResponseHeaders.hasLegalHold();
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
}
