package com.azure.storage.blob.models;

import java.util.List;

public class ContainerAccessPolicies {
    private final PublicAccessType blobAccessType;
    private final List<SignedIdentifier> identifiers;

    public ContainerAccessPolicies(PublicAccessType blobAccessType, List<SignedIdentifier> identifiers) {
        this.blobAccessType = blobAccessType;
        this.identifiers = identifiers;
    }

    public PublicAccessType getBlobAccessType() {
        return blobAccessType;
    }

    public List<SignedIdentifier> getIdentifiers() {
        return this.identifiers;
    }
}
