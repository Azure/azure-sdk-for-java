// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.api.credential.descriptor;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;

import java.util.function.Function;


/**
 * Describe the cache key.
 */
public interface CacheKeyDescriptor {

    boolean support(TokenCredential tokenCredential);

    /**
     * Get the cache key.
     *
     * @return The cache key for caching.
     */
    Descriptor[] getTokenCredentialKeyDescriptors();

    enum Descriptor {
        AUTHORITY_HOST(TokenCredentialProviderOptions::getAuthorityHost),
        CLIENT_ID(TokenCredentialProviderOptions::getClientId),
        CLIENT_SECRET(TokenCredentialProviderOptions::getClientSecret),
        CLIENT_CERTIFICATE_PATH(TokenCredentialProviderOptions::getClientCertificatePath),
        CLIENT_CERTIFICATE_PASSWORD(TokenCredentialProviderOptions::getClientCertificatePassword),
        TENANT_ID(TokenCredentialProviderOptions::getTenantId),
        USERNAME(TokenCredentialProviderOptions::getUsername),
        PASSWORD(TokenCredentialProviderOptions::getPassword),
        TOKEN_CREDENTIAL_BEAN_NAME(TokenCredentialProviderOptions::getTokenCredentialBeanName);

        Function<TokenCredentialProviderOptions, String> getter;

        Descriptor(Function<TokenCredentialProviderOptions, String> getter) {
            this.getter = getter;
        }

        public Function<TokenCredentialProviderOptions, String> getGetter() {
            return getter;
        }
    }
}
