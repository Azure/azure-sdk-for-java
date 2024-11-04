// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.properties;

import com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager.AzureResourceMetadataConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
@ConfigurationProperties("spring.cloud.azure.redis")
public class AzureRedisProperties {

    /**
     * Name of the Azure Cache for Redis.
     */
    private String name;

    @NestedConfigurationProperty
    private final AzureResourceMetadataConfigurationProperties resource = new AzureResourceMetadataConfigurationProperties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AzureResourceMetadataConfigurationProperties getResource() {
        return resource;
    }

}
