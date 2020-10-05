// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.context.core.config;

import com.google.common.base.Strings;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.spring.cloud.context.core.api.CredentialSupplier;
import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("spring.cloud.azure")
public class AzureProperties implements CredentialSupplier {

    private String credentialFilePath;

    private String resourceGroup;

    private AzureEnvironment environment = AzureEnvironment.AZURE;

    private String region;

    private boolean autoCreateResources = false;

    private boolean msiEnabled = false;

    @NestedConfigurationProperty
    private AzureManagedIdentityProperties managedIdentity;

    private String subscriptionId;

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (msiEnabled && Strings.isNullOrEmpty(subscriptionId)) {
            Assert.hasText(this.subscriptionId, "When msi is enabled, spring.cloud.azure.subscription-id must be provided");
        }
    }

    @Override
    public String getCredentialFilePath() {
        return credentialFilePath;
    }

    public void setCredentialFilePath(String credentialFilePath) {
        this.credentialFilePath = credentialFilePath;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public AzureEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(AzureEnvironment environment) {
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

    public AzureManagedIdentityProperties getManagedIdentity() {
        return managedIdentity;
    }

    public void setManagedIdentity(AzureManagedIdentityProperties managedIdentity) {
        this.managedIdentity = managedIdentity;
    }
}
