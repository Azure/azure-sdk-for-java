// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.UsernamePasswordCredential;

/**
 * Describe the cache key for UsernamePasswordCredential.
 */
public class UsernamePasswordCredentialCacheKeyDescriptor implements CacheKeyDescriptor {

    @Override
    public boolean support(TokenCredential tokenCredential) {
        return tokenCredential instanceof UsernamePasswordCredential;
    }

    @Override
    public Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.USERNAME,
            Descriptor.PASSWORD
        };
    }
}
