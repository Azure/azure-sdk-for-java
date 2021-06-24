// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.spring.core.AzureProperties;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * Azure Context related properties for resource management.
 */
@Validated
@ConfigurationProperties(AzureContextProperties.PREFIX)
public class AzureContextProperties {

    public static final String PREFIX = "spring.cloud.azure";

    @Autowired
    private AzureProperties azureProperties;

    /**
     * Flag to automatically create resources.
     */
    private boolean autoCreateResources = false;

    /**
     * Name of the region where resources would be automatically created.
     */
    private String region;


    public AzureProperties getAzureProperties() {
        return azureProperties;
    }

    public void setAzureProperties(AzureProperties azureProperties) {
        this.azureProperties = azureProperties;
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

    @PostConstruct
    private void validate() {
        if (autoCreateResources) {
            Assert.hasText(this.region,
                "When auto create resources is enabled, spring.cloud.azure.region must be provided");
        }

        if (azureProperties.isMsiEnabled() && Strings.isNullOrEmpty(azureProperties.getSubscriptionId())) {
            Assert.hasText(azureProperties.getSubscriptionId(), "When msi is enabled, "
                + "spring.cloud.azure.subscription-id must be provided");
        }
    }
}
