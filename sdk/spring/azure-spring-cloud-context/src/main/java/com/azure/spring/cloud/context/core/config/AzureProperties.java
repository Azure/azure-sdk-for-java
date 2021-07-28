// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.config;


import com.azure.core.util.CoreUtils;
import com.azure.spring.cloud.context.core.api.CredentialSupplier;
import com.azure.spring.cloud.context.core.enums.AzureEnvironments;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * Azure related properties.
 */
@Validated
@ConfigurationProperties(AzureProperties.PREFIX)
public class AzureProperties implements CredentialSupplier {

    public static final String PREFIX = "spring.cloud.azure";

    private String clientId;

    private String clientSecret;

    private String tenantId;

    private String resourceGroup;

    private AzureEnvironments environment = AzureEnvironments.Azure;

    private String region;

    private boolean autoCreateResources = false;

    private boolean msiEnabled = false;

    private String subscriptionId;

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (msiEnabled && CoreUtils.isNullOrEmpty(subscriptionId)) {
            Assert.hasText(this.subscriptionId, "When msi is enabled, "
                + "spring.cloud.azure.subscription-id must be provided");
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public AzureEnvironments getEnvironment() {
        return environment;
    }

    public void setEnvironment(AzureEnvironments environment) {
        this.environment = environment;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isAutoCreateResources() {
        return autoCreateResources;
    }

    public void setAutoCreateResources(boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }

    public boolean isMsiEnabled() {
        return msiEnabled;
    }

    public void setMsiEnabled(boolean msiEnabled) {
        this.msiEnabled = msiEnabled;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

}
