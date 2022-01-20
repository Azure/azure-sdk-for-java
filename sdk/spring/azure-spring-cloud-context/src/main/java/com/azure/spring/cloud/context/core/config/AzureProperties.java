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

    /**
     * The prefix.
     */
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

    /**
     * Gets the client ID.
     *
     * @return The client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client ID.
     *
     * @param clientId The client ID.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the client secret.
     *
     * @return The client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret The client secret.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Gets the tenant ID.
     *
     * @return The tenant ID.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Sets the tenant ID.
     *
     * @param tenantId The tenant ID.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Gets the resource group.
     *
     * @return The resource group.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    /**
     * Sets the resource group.
     *
     * @param resourceGroup The resource group.
     */
    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Gets the Azure environment.
     *
     * @return The Azure environment.
     */
    public AzureEnvironments getEnvironment() {
        return environment;
    }

    /**
     * Sets the Azure environment.
     *
     * @param environment The Azure environment.
     */
    public void setEnvironment(AzureEnvironments environment) {
        this.environment = environment;
    }

    /**
     * Gets the region.
     *
     * @return The region.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region.
     *
     * @param region The region.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Whether resources are automatically created.
     *
     * @return Whether resources are automatically created.
     */
    public boolean isAutoCreateResources() {
        return autoCreateResources;
    }

    /**
     * Sets whether resources are automatically created.
     *
     * @param autoCreateResources Whether resources are automatically created.
     */
    public void setAutoCreateResources(boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }

    /**
     * Whether MSI is enabled.
     *
     * @return Whether MSI is enabled.
     */
    public boolean isMsiEnabled() {
        return msiEnabled;
    }

    /**
     * Sets whether MSI is enabled.
     *
     * @param msiEnabled Whether MSI is enabeld.
     */
    public void setMsiEnabled(boolean msiEnabled) {
        this.msiEnabled = msiEnabled;
    }

    /**
     * Gets the subscription ID.
     *
     * @return The subscription ID.
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription ID.
     * @param subscriptionId The subscription ID.
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

}
