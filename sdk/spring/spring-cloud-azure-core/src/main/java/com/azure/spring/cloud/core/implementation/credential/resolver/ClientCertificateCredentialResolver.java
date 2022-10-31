// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.resolver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.ClientCertificateCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

public class ClientCertificateCredentialResolver implements AzureCredentialResolver<TokenCredential> {

    private final ClientCertificateCredentialBuilderFactory builderFactory;

    public ClientCertificateCredentialResolver(ClientCertificateCredentialBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    ClientCertificateCredentialResolver() {
        this(null);
    }


    @Override
    public boolean isResolvable(AzureProperties properties) {
        if (properties == null || properties.getCredential() == null || properties.getProfile() == null) {
            return false;
        }

        return StringUtils.hasText(properties.getProfile().getTenantId())
            && StringUtils.hasText(properties.getCredential().getClientId())
            && StringUtils.hasText(properties.getCredential().getClientCertificatePath());
    }

    @Override
    public TokenCredential resolve(AzureProperties properties) {
        String authorityHost = properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();

        if (authorityHost == null) {
            authorityHost = AzureEnvironment.AZURE.getActiveDirectoryEndpoint();
        }

        ClientCertificateCredentialBuilderFactory factory = this.builderFactory == null
            ? new ClientCertificateCredentialBuilderFactory(properties) : this.builderFactory;

        ClientCertificateCredentialBuilder builder = factory
            .build()
            .authorityHost(authorityHost)
            .clientId(properties.getCredential().getClientId())
            .tenantId(properties.getProfile().getTenantId());

        String clientCertificatePath = properties.getCredential().getClientCertificatePath();
        String clientCertificatePassword = properties.getCredential().getClientCertificatePassword();

        if (StringUtils.hasText(clientCertificatePassword)) {
            builder.pfxCertificate(clientCertificatePath, clientCertificatePassword);
        } else {
            builder.pemCertificate(clientCertificatePath);
        }

        return builder.build();
    }
}
