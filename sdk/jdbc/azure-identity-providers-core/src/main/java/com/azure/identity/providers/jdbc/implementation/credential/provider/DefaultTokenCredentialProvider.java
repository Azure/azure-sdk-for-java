// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import reactor.util.annotation.Nullable;

/**
 * Default tokenCredentialProvider implementation that provides tokenCredential instance.
 */
public class DefaultTokenCredentialProvider implements TokenCredentialProvider {

    private final TokenCredentialProviderOptions options;

    DefaultTokenCredentialProvider() {
        this.options = new TokenCredentialProviderOptions();
    }

    DefaultTokenCredentialProvider(TokenCredentialProviderOptions options) {
        this.options = options;
    }

    @Override
    public TokenCredential get() {
        return get(this.options);
    }

    @Override
    public TokenCredential get(TokenCredentialProviderOptions options) {
        if (options == null) {
            return new DefaultAzureCredentialBuilder().build();
        }
        return resolveTokenCredential(options);
    }

    private TokenCredential resolveTokenCredential(TokenCredentialProviderOptions options) {
        final String tenantId = options.getTenantId();
        final String clientId = options.getClientId();
        final boolean isClientIdSet = hasText(clientId);
        final String authorityHost = options.getAuthorityHost();
        if (hasText(tenantId)) {
            String clientSecret = options.getClientSecret();
            if (isClientIdSet && hasText(clientSecret)) {
                return new ClientSecretCredentialBuilder().clientId(clientId)
                        .authorityHost(authorityHost)
                        .clientSecret(clientSecret)
                        .tenantId(tenantId)
                        .build();
            }

            String clientCertificatePath = options.getClientCertificatePath();
            if (isClientIdSet && hasText(clientCertificatePath)) {
                ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder()
                        .authorityHost(authorityHost)
                        .tenantId(tenantId)
                        .clientId(clientId);

                if (hasText(options.getClientCertificatePassword())) {
                    builder.pfxCertificate(clientCertificatePath, options.getClientCertificatePassword());
                } else {
                    builder.pemCertificate(clientCertificatePath);
                }

                return builder.build();
            }
        }

        if (isClientIdSet && hasText(options.getUsername())
                && hasText(options.getPassword())) {
            return new UsernamePasswordCredentialBuilder().username(options.getUsername())
                    .authorityHost(authorityHost)
                    .password(options.getPassword())
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .build();
        }

        if (options.isManagedIdentityEnabled()) {
            ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
            if (isClientIdSet) {
                builder.clientId(clientId);
            }
            return builder.build();
        }

        return new DefaultAzureCredentialBuilder()
                .authorityHost(authorityHost)
                .tenantId(tenantId)
                .managedIdentityClientId(clientId)
                .build();
    }

    private boolean hasText(@Nullable String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
