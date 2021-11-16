// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis;

import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
@ConfigurationProperties("spring.cloud.azure.redis")
public class AzureRedisProperties {

    private String name;

    @NestedConfigurationProperty
    private final AzureResourceMetadata resource = new AzureResourceMetadata();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AzureResourceMetadata getResource() {
        return resource;
    }
}
