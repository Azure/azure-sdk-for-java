// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.resolver;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.spring.core.credential.provider.AzureTokenCredentialProvider;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import org.springframework.util.StringUtils;

/**
 * Resolve the token credential according to the azure properties.
 */
public class AzureTokenCredentialResolver implements AzureCredentialResolver<AzureTokenCredentialProvider> {

    @Override
    public AzureTokenCredentialProvider resolve(AzureProperties properties) {
        // TODO (xiada): the token credential logic
        final TokenCredentialProperties credential = properties.getCredential();
        if (credential == null) {
            return null;
        }
        if (StringUtils.hasText(credential.getTenantId()) && StringUtils.hasText(
            credential.getClientId()) && StringUtils.hasText(credential.getClientSecret())) {
            final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(credential.getClientId())
                .clientSecret(credential.getClientSecret())
                .tenantId(credential.getTenantId())
                .build();
            return new AzureTokenCredentialProvider(clientSecretCredential);
        }

        if (StringUtils.hasText(credential.getTenantId()) && StringUtils.hasText(credential.getClientCertificatePath())) {
            final ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId(credential.getClientId())
                .pemCertificate(credential.getClientCertificatePath())
                .tenantId(credential.getTenantId())
                .build();
            return new AzureTokenCredentialProvider(clientCertificateCredential);
        }

        if (credential.getManagedIdentityClientId() != null) {
            final ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
                .clientId(credential.getManagedIdentityClientId())
                .build();
            return new AzureTokenCredentialProvider(managedIdentityCredential);
        }
        return null;
    }

    /**
     * All SDKs will support this type.
     *
     * @param properties Azure properties
     * @return Resolvable or not
     */
    @Override
    public boolean isResolvable(AzureProperties properties) {
        return true;
    }
}
