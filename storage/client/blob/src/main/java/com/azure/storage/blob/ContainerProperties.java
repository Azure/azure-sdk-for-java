// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.storage.blob.models.PublicAccessType;

public class ContainerProperties {

    private final PublicAccessType blobPublicAccess;

    private final boolean hasImmutabilityPolicy;

    private final boolean hasLegalHold;

    //todo decide datetime representation for last modified time

    ContainerProperties(ContainerGetPropertiesHeaders generatedResponseHeaders) {
        this.blobPublicAccess = generatedResponseHeaders.blobPublicAccess();
        this.hasImmutabilityPolicy = generatedResponseHeaders.hasImmutabilityPolicy();
        this.hasLegalHold = generatedResponseHeaders.hasLegalHold();
    }

    /**
     * @return the access type for the container
     */
    public PublicAccessType blobPublicAccess() {
        return blobPublicAccess;
    }

    /**
     * @return the immutability status for the container
     */
    public boolean hasImmutabilityPolicy() {
        return hasImmutabilityPolicy;
    }

    /**
     * @return the legal hold status for the container
     */
    public boolean hasLegalHold() {
        return hasLegalHold;
    }
}
