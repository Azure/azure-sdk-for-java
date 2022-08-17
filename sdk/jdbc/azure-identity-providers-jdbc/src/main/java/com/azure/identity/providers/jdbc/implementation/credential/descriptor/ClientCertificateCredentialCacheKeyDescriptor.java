// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.providers.jdbc.api.credential.descriptor.CacheKeyDescriptor;

/**
 * Describe the cache key for ClientCertificateCredential.
 */
public class ClientCertificateCredentialCacheKeyDescriptor implements CacheKeyDescriptor {

    @Override
    public boolean support(TokenCredential tokenCredential) {
        return tokenCredential instanceof ClientCertificateCredential;
    }

    @Override
    public Descriptor[] getTokenCredentialKeyDescriptors() {
        return new Descriptor[]{
            Descriptor.AUTHORITY_HOST,
            Descriptor.TENANT_ID,
            Descriptor.CLIENT_CERTIFICATE_PATH,
            Descriptor.CLIENT_CERTIFICATE_PASSWORD
        };
    }
}
