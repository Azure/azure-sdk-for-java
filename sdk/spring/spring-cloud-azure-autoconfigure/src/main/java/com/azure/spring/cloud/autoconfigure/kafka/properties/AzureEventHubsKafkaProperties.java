// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.kafka.properties;

import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * {@link ConfigurationProperties} for configuring Azure Event Hubs Kafka.
 */
@ConfigurationProperties(prefix = AzureEventHubsKafkaProperties.PREFIX)
public class AzureEventHubsKafkaProperties implements AzureProperties {

    public static final String PREFIX = "spring.cloud.azure.kafka";

    protected boolean enabled = true;

    @NestedConfigurationProperty
    protected final TokenCredentialConfigurationProperties credential = new TokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    protected final AzureProfileConfigurationProperties profile = new AzureProfileConfigurationProperties();

    @Override
    public AzureProfileConfigurationProperties getProfile() {
        return profile;
    }

    @Override
    public TokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    @Override
    public ClientOptions getClient() {
        return null;
    }

    @Override
    public ProxyOptions getProxy() {
        return null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
