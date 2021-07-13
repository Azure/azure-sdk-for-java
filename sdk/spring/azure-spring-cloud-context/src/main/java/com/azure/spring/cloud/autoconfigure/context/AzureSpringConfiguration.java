// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.util.Configuration;
import com.azure.spring.identity.CredentialPropertiesProvider;
import org.springframework.util.StringUtils;

/**
 * Azure Spring extension of the {@link Configuration}.
 */
public class AzureSpringConfiguration extends Configuration implements CredentialPropertiesProvider {

    private final Configuration configuration;

    public AzureSpringConfiguration(AzureContextProperties azureContextProperties) {
        this.configuration = Configuration.getGlobalConfiguration().clone();
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_TENANT_ID, azureContextProperties.getTenantId());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_CLIENT_ID, azureContextProperties.getClientId());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_CLIENT_SECRET, azureContextProperties.getClientSecret());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, azureContextProperties.getEnvironment().getAzureEnvironment().getActiveDirectoryEndpoint());
    }

    private void overridePropertyIfApplicable(String key, String value) {
        if (StringUtils.hasText(value)) {
            this.configuration.put(key, value);
        }
    }

    @Override
    public String getTenantId() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
    }

    @Override
    public String getClientId() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
    }

    @Override
    public String getClientSecret() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET);
    }

    @Override
    public String getClientCertificatePath() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
    }

    @Override
    public String getUsername() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_USERNAME);
    }

    @Override
    public String getPassword() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_PASSWORD);
    }

    @Override
    public String getAuthorityHost() {
        return this.configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
    }
}
