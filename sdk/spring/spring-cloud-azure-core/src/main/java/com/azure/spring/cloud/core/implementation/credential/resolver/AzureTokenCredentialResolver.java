// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientSecretCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.ManagedIdentityCredentialBuilderFactory;
import com.azure.spring.cloud.core.implementation.factory.credential.UsernamePasswordCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;
import org.springframework.util.StringUtils;

import java.util.function.Function;

/**
 * Resolve the token credential according to the azure properties.
 */
public class AzureTokenCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final Function<AzureProperties, TokenCredential> resolveFunction;

    public AzureTokenCredentialResolver(Function<AzureProperties, TokenCredential> resolveFunction) {
        this.resolveFunction = resolveFunction;
    }

    public AzureTokenCredentialResolver() {
        this.resolveFunction = AzureTokenCredentialResolver::resolveTokenCredential;
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        return this.resolveFunction.apply(properties);
    }

    private static TokenCredential resolveTokenCredential(AzureProperties azureProperties) {
        if (azureProperties.getCredential() == null) {
            return null;
        }

        final TokenCredentialOptionsProvider.TokenCredentialOptions properties = azureProperties.getCredential();
        final String tenantId = azureProperties.getProfile().getTenantId();
        final String clientId = properties.getClientId();
        final boolean isClientIdSet = StringUtils.hasText(clientId);
        if (StringUtils.hasText(tenantId)) {

            if (isClientIdSet && StringUtils.hasText(properties.getClientSecret())) {
                return new ClientSecretCredentialBuilderFactory(azureProperties)
                    .build()
                    .clientId(clientId)
                    .clientSecret(properties.getClientSecret())
                    .tenantId(tenantId)
                    .build();
            }

            String clientCertificatePath = properties.getClientCertificatePath();
            if (StringUtils.hasText(clientCertificatePath)) {
                ClientCertificateCredentialBuilder builder =
                    new ClientCertificateCredentialBuilderFactory(azureProperties)
                        .build()
                        .tenantId(tenantId)
                        .clientId(clientId);

                if (StringUtils.hasText(properties.getClientCertificatePassword())) {
                    builder.pfxCertificate(clientCertificatePath, properties.getClientCertificatePassword());
                } else {
                    builder.pemCertificate(clientCertificatePath);
                }

                return builder.build();
            }
        }

        if (isClientIdSet && StringUtils.hasText(properties.getUsername())
            && StringUtils.hasText(properties.getPassword())) {
            return new UsernamePasswordCredentialBuilderFactory(azureProperties)
                .build()
                .username(properties.getUsername())
                .password(properties.getPassword())
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }

        if (properties.isManagedIdentityEnabled()) {
            ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilderFactory(azureProperties).build();
            if (isClientIdSet) {
                builder.clientId(clientId);
            }
            return builder.build();
        }
        return null;
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return true;
    }

}
