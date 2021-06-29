// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.spring.core.AzureSpringProperties;
import com.azure.spring.core.CredentialProperties;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * Azure Context related properties for resource management.
 */
@Validated
@ConfigurationProperties(AzureContextProperties.PREFIX)
@Import(AzureSpringProperties.class)
public class AzureContextProperties {

    public static final String PREFIX = "spring.cloud.azure";

    @Autowired
    private CredentialProperties credentialProperties;

    /**
     * Flag to automatically create resources.
     */
    private boolean autoCreateResources = false;

    /**
     * Name of the region where resources would be automatically created.
     */
    private String region;

    /**
     * Name of the Azure resource group.
     */
    private String resourceGroup;

    private String subscriptionId;

    public CredentialProperties getCredentialProperties() {
        return credentialProperties;
    }

    public void setCredentialProperties(CredentialProperties credentialProperties) {
        this.credentialProperties = credentialProperties;
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

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (credentialProperties.isMsiEnabled() && Strings.isNullOrEmpty(getSubscriptionId())) {
            Assert.hasText(getSubscriptionId(), "When msi is enabled, "
                + "spring.cloud.azure.subscription-id must be provided");
        }
    }
}
