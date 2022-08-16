// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;

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
