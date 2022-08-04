// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;

public class CacheableSpringTokenCredential extends CacheableTokenCredentialAdapter<TokenCredential> {

    public CacheableSpringTokenCredential(TokenCredentialProviderOptions options,
                                          TokenCredential delegate) {
        super(options, delegate);
    }

    @Override
    protected Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.TOKEN_CREDENTIAL_BEAN_NAME
        };
    }

}
