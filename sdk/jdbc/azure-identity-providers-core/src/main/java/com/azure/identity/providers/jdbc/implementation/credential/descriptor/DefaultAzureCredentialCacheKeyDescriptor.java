// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;

/**
 * Describe the cache key for DefaultAzureCredential.
 */
public class DefaultAzureCredentialCacheKeyDescriptor implements CacheKeyDescriptor {

    @Override
    public boolean support(TokenCredential tokenCredential) {
        return tokenCredential instanceof DefaultAzureCredential;
    }

    @Override
    public Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.CLIENT_ID
        };
    }
}
