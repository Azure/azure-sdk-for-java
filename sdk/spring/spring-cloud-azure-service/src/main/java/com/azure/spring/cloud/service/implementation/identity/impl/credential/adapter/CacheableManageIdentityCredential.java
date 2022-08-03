// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;

public class CacheableManageIdentityCredential extends CacheableTokenCredentialAdapter<ManagedIdentityCredential> {

    public CacheableManageIdentityCredential(TokenCredentialProviderOptions options,
                                             ManagedIdentityCredential delegate) {
        super(options, delegate);
    }

    @Override
    protected Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[] {
                Descriptor.AUTHORITY_HOST,
                Descriptor.CLIENT_ID
        };
    }
}
