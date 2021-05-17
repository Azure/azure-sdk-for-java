// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 *
 */
public abstract class SpringCredentialBuilderBase<T extends SpringCredentialBuilderBase<T>> {

    protected Environment environment;

    public SpringCredentialBuilderBase() {

    }

    @SuppressWarnings("unchecked")
    public T environment(Environment environment) {
        this.environment = environment;
        return (T) this;
    }

    protected TokenCredential populateTokenCredential(String prefix) {
        return populateTokenCredential(prefix, true);
    }

    protected TokenCredential populateTokenCredentialBasedOnClientId(String prefix) {
        return populateTokenCredential(prefix, false);
    }

    private TokenCredential populateTokenCredential(String prefix, boolean createDefault) {
        String tenantId = getPropertyValue(prefix, "tenant-id");
        String clientId = getPropertyValue(prefix, "client-id");
        String clientSecret = getPropertyValue(prefix, "client-secret");
        AzureCloud azureCloud = getPropertyValue(AzureCloud.class, prefix, "cloud-name", AzureCloud.Azure);
        String authorityHost = azureCloud.getAuthorityHost();

        if (tenantId != null && clientId != null && clientSecret != null) {
            return new ClientSecretCredentialBuilder()
                .authorityHost(authorityHost)
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
        }

        String certPath = getPropertyValue(prefix, "client-certificate-path");

        if (tenantId != null && clientId != null && certPath != null) {
            return new ClientCertificateCredentialBuilder()
                .authorityHost(authorityHost)
                .tenantId(tenantId)
                .clientId(clientId)
                .pemCertificate(certPath)
                .build();
        }

        if (clientId != null) {
            return new ManagedIdentityCredentialBuilder().clientId(clientId).build();
        }

        return createDefault ? defaultManagedIdentityCredential() : null;
    }

    protected ManagedIdentityCredential defaultManagedIdentityCredential() {
        return new ManagedIdentityCredentialBuilder().build();
    }

    protected String getPropertyValue(String prefix, String propertyKey) {
        return Binder.get(this.environment)
                     .bind(prefix + propertyKey, String.class)
                     .orElse(null);
    }

    protected <T> T getPropertyValue(Class<T> type, String prefix, String propertyKey, T defaultValue) {
        return Binder.get(this.environment)
                     .bind(prefix + propertyKey, type)
                     .orElse(defaultValue);
    }

}
