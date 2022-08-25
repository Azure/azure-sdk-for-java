// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredential;

/**
 * Describe the cache key for ManagedIdentityCredential.
 */
public class ManagedIdentityCredentialCacheKeyDescriptor implements CacheKeyDescriptor {

    @Override
    public boolean support(TokenCredential tokenCredential) {
        return tokenCredential instanceof ManagedIdentityCredential;
    }

    @Override
    public Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.CLIENT_ID
        };
    }
}
