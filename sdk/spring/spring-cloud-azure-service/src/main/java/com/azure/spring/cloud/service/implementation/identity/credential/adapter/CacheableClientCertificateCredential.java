// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.adapter;

import com.azure.identity.ClientCertificateCredential;
import com.azure.spring.cloud.service.implementation.identity.credential.TokenCredentialProviderOptions;

/**
 * TokenCredential that delegates the {@link ClientCertificateCredential}
 * and provides functionality to cache an access token.
 */
public class CacheableClientCertificateCredential extends CacheableTokenCredentialAdapter<ClientCertificateCredential> {

    public CacheableClientCertificateCredential(TokenCredentialProviderOptions options,
                                                ClientCertificateCredential delegate) {
        super(options, delegate);
    }

    @Override
    protected Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.CLIENT_CERTIFICATE_PATH,
            Descriptor.CLIENT_CERTIFICATE_PASSWORD
        };
    }
}
