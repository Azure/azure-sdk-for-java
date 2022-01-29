// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.core.aware.authentication.TokenCredentialAware;
import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.credential.provider.AzureTokenCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

import java.util.function.Function;

/**
 * Resolve the token credential according to the azure properties.
 */
public class AzureTokenCredentialResolver implements AzureCredentialResolver<AzureTokenCredentialProvider> {

    private final Function<AzureProperties, TokenCredential> resolveFunction;

    public AzureTokenCredentialResolver(Function<AzureProperties, TokenCredential> resolveFunction) {
        this.resolveFunction = resolveFunction;
    }

    public AzureTokenCredentialResolver() {
        this.resolveFunction = AzureTokenCredentialResolver::resolveTokenCredential;
    }

    @Override
    public AzureTokenCredentialProvider resolve(AzureProperties properties) {
        TokenCredential tokenCredential = this.resolveFunction.apply(properties);
        return tokenCredential == null ? null : new AzureTokenCredentialProvider(tokenCredential);
    }

    private static TokenCredential resolveTokenCredential(AzureProperties properties) {
        if (properties.getCredential() == null) {
            return null;
        }

        TokenCredential result = null;

        final TokenCredentialAware.TokenCredential credentialProperties = properties.getCredential();
        final String tenantId = properties.getProfile().getTenantId();
        if (StringUtils.hasText(tenantId)
            && StringUtils.hasText(credentialProperties.getClientId())
            && StringUtils.hasText(credentialProperties.getClientSecret())) {
            result = new ClientSecretCredentialBuilder()
                .clientId(credentialProperties.getClientId())
                .clientSecret(credentialProperties.getClientSecret())
                .tenantId(tenantId)
                .build();
        }

        if (StringUtils.hasText(tenantId)
            && StringUtils.hasText(credentialProperties.getClientCertificatePath())) {
            ClientCertificateCredentialBuilder builder =
                new ClientCertificateCredentialBuilder().tenantId(tenantId)
                                                        .clientId(credentialProperties.getClientId());
            if (StringUtils.hasText(credentialProperties.getClientCertificatePassword())) {
                builder.pfxCertificate(credentialProperties.getClientCertificatePath(),
                    credentialProperties.getClientCertificatePassword());
            } else {
                builder.pemCertificate(credentialProperties.getClientCertificatePath());
            }

            result = builder.build();
        }

        if (credentialProperties.getManagedIdentityClientId() != null) {
            result = new ManagedIdentityCredentialBuilder()
                .clientId(credentialProperties.getManagedIdentityClientId())
                .build();
        }
        return result;
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return true;
    }

}
