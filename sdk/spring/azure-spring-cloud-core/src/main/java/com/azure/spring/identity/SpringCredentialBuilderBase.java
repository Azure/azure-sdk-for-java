// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.core.env.Environment;

import java.util.Optional;


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
        String tenantId = getPropertyValue(prefix + "tenant-id");
        String clientId = getPropertyValue(prefix + "client-id");
        String clientSecret = getPropertyValue(prefix + "client-secret");
        String authorityHost = getAuthorityHost(prefix);

        if (tenantId != null && clientId != null && clientSecret != null) {
            return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorityHost(authorityHost)
                .build();
        }

        String certPath = getPropertyValue(prefix + "client-certificate-path");

        if (tenantId != null && clientId != null && certPath != null) {
            return new ClientCertificateCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .pemCertificate(certPath)
                .authorityHost(authorityHost)
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

    protected String getPropertyValue(String propertyName) {
        return environment.getProperty(propertyName);
    }

    protected String getPropertyValue(String propertyName, String defaultValue) {
        return environment.getProperty(propertyName, defaultValue);
    }

    protected String getAuthorityHost(String prefix) {
        return Optional.ofNullable(getPropertyValue(prefix + "authority-host"))
                       .orElse(Optional.ofNullable(getPropertyValue(prefix + "environment"))
                                       .filter(env -> !env.isEmpty())
                                       .map(SpringCredentialBuilderBase::toAuthorityHost)
                                       .orElse(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD));
    }

    public static String toAuthorityHost(String azureEnvironment) {
        switch (azureEnvironment) {
            case "AzureChina":
                return AzureAuthorityHosts.AZURE_CHINA;
            case "AzureGermany":
                return AzureAuthorityHosts.AZURE_GERMANY;
            case "AzureUSGovernment":
                return AzureAuthorityHosts.AZURE_GOVERNMENT;
            default:
                return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }
    }

}
