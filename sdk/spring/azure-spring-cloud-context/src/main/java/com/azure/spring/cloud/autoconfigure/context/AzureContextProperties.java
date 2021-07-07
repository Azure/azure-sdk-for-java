// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.spring.core.SpringPropertyPrefix;
import com.azure.spring.core.CredentialProperties;
import com.google.common.base.Strings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

/**
 * Azure Context related properties for resource management.
 */
@Validated
@ConfigurationProperties(SpringPropertyPrefix.PREFIX)
public class AzureContextProperties implements InitializingBean {

    private CredentialProperties credential;

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

    public CredentialProperties getCredential() {
        return credential;
    }

    public void setCredential(CredentialProperties credential) {
        this.credential = credential;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (credential.isMsiEnabled() && Strings.isNullOrEmpty(getSubscriptionId())) {
            Assert.hasText(getSubscriptionId(), "When msi is enabled, "
                + "spring.cloud.azure.subscription-id must be provided");
        }
    }
}
