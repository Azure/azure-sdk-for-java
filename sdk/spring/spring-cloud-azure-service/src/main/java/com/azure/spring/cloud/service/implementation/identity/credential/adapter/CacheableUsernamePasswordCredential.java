// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.adapter;

import com.azure.identity.UsernamePasswordCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProviderOptions;

/**
 * TokenCredential that delegates {@link UsernamePasswordCredential}
 * and can cache an access token.
 */
public class CacheableUsernamePasswordCredential extends CacheableTokenCredentialAdapter<UsernamePasswordCredential> {

    public CacheableUsernamePasswordCredential(TokenCredentialProviderOptions options,
                                               UsernamePasswordCredential delegate) {
        super(options, delegate);
    }

    @Override
    protected Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.USERNAME,
            Descriptor.PASSWORD
        };
    }
}
