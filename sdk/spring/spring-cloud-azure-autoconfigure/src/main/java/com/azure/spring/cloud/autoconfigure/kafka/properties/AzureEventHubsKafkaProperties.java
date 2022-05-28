// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka.properties;

import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * {@link ConfigurationProperties} for configuring Azure Event Hubs Kafka.
 */
@ConfigurationProperties(prefix = AzureEventHubsKafkaProperties.PREFIX)
public class AzureEventHubsKafkaProperties implements TokenCredentialOptionsProvider, AzureProfileOptionsProvider {

    public static final String PREFIX = "spring.cloud.azure.kafka";

    protected boolean enabled = true;

    @NestedConfigurationProperty
    protected final TokenCredentialConfigurationProperties credential = new TokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    protected final AzureProfileConfigurationProperties profile = new AzureProfileConfigurationProperties();

    @Override
    public ProfileOptions getProfile() {
        return profile;
    }

    @Override
    public TokenCredentialOptions getCredential() {
        return credential;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
