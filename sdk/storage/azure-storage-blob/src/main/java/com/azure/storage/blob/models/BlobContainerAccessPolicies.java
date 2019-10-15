// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.List;

/**
 * This class contains values which correlate to the access polices set on a specific container.
 */
public class BlobContainerAccessPolicies {
    private final PublicAccessType blobAccessType;
    private final List<BlobSignedIdentifier> identifiers;

    public BlobContainerAccessPolicies(PublicAccessType blobAccessType, List<BlobSignedIdentifier> identifiers) {
        this.blobAccessType = blobAccessType;
        this.identifiers = identifiers;
    }

    /**
     * @return the level of public access the container allows.
     */
    public PublicAccessType getBlobAccessType() {
        return blobAccessType;
    }

    /**
     * @return the {@link BlobSignedIdentifier SignedIdentifiers} associates with the container.
     */
    public List<BlobSignedIdentifier> getIdentifiers() {
        return this.identifiers;
    }
}
