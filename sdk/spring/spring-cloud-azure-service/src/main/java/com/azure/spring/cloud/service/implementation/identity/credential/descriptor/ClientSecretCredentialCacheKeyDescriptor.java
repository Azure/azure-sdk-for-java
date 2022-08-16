// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredential;

/**
 * Describe the cache key for ClientSecretCredential.
 */
public class ClientSecretCredentialCacheKeyDescriptor implements CacheKeyDescriptor {

    @Override
    public boolean support(TokenCredential tokenCredential) {
        return tokenCredential instanceof ClientSecretCredential;

    }

    @Override
    public Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.CLIENT_ID,
            Descriptor.CLIENT_SECRET};
    }
}
