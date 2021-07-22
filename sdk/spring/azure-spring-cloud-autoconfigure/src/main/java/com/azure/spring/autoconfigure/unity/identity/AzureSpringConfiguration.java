// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity.identity;

import com.azure.core.util.Configuration;
import com.azure.spring.autoconfigure.unity.AzureProperties;
import com.azure.spring.identity.CredentialPropertiesProvider;
import org.springframework.util.StringUtils;

/**
 * Extends the {@link Configuration} to provide Azure Spring related configurations.
 */
public class AzureSpringConfiguration extends Configuration implements CredentialPropertiesProvider {

    private final Configuration configuration;

    public AzureSpringConfiguration(AzureProperties azureProperties) {
        this.configuration = Configuration.getGlobalConfiguration().clone();
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_TENANT_ID, azureProperties.getCredential().getTenantId());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_CLIENT_ID, azureProperties.getCredential().getClientId());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_CLIENT_SECRET, azureProperties.getCredential().getClientSecret());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, azureProperties.getCredential().getCertificatePath());
        overridePropertyIfApplicable(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, azureProperties.getEnvironment().getAuthorityHost());
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
