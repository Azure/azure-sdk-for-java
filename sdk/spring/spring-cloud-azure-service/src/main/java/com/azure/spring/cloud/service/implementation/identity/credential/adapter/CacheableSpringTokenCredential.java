// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.adapter;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProviderOptions;

/**
 * TokenCredential that delegates the {@link TokenCredential} get from Spring context
 * and provides functionality to cache an access token.
 */
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
