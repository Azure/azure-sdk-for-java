// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

import com.azure.identity.ClientSecretCredential;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;

/**
 * TokenCredential that delegates the {@link ClientSecretCredential}
 * and provides functionality to cache an access token.
 */
public class CacheableClientSecretCredential extends CacheableTokenCredentialAdapter<ClientSecretCredential> {

    public CacheableClientSecretCredential(TokenCredentialProviderOptions options,
                                           ClientSecretCredential delegate) {
        super(options, delegate);
    }

    @Override
    protected Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.CLIENT_ID,
            Descriptor.CLIENT_SECRET
        };
    }

}
