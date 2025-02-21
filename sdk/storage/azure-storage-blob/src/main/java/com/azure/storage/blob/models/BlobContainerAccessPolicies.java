// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * This class contains values which correlate to the access polices set on a specific container.
 */
@Immutable
public class BlobContainerAccessPolicies {
    private final PublicAccessType blobAccessType;
    private final List<BlobSignedIdentifier> identifiers;

    /**
     * Constructs a {@link BlobContainerAccessPolicies}.
     *
     * @param blobAccessType Level of public access the container allows.
     * @param identifiers {@link BlobSignedIdentifier BlobSignedIdentifiers} associated with the container.
     */
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
     * @return the {@link BlobSignedIdentifier BlobSignedIdentifiers} associated with the container.
     */
    public List<BlobSignedIdentifier> getIdentifiers() {
        return this.identifiers;
    }
}
