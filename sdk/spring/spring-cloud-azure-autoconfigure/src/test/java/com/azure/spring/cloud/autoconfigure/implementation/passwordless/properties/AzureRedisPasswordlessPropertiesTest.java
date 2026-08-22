// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AzureRedisPasswordlessPropertiesTest {

    private static final String REDIS_SCOPE_AZURE = "https://redis.azure.com/.default";

    @Test
    void defaultScopesShouldReturnAzureRedisScope() {
        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        assertEquals(REDIS_SCOPE_AZURE, properties.getScopes());
    }

    @Test
    void customScopesShouldOverrideDefault() {
        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        String customScope = "https://custom.redis.scope/.default";
        properties.setScopes(customScope);
        assertEquals(customScope, properties.getScopes());
    }

    @Test
    void defaultScopesShouldReturnAzureRedisScopeForAzureCloud() {
        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE);
        assertEquals(REDIS_SCOPE_AZURE, properties.getScopes());
    }

    @Test
    void defaultScopesShouldReturnAzureRedisScopeForAzureChinaCloud() {
        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        assertEquals(REDIS_SCOPE_AZURE, properties.getScopes());
    }

    @Test
    void defaultScopesShouldReturnAzureRedisScopeForAzureUsGovernmentCloud() {
        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        properties.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        assertEquals(REDIS_SCOPE_AZURE, properties.getScopes());
    }

    @Test
    void passwordlessEnabledShouldDefaultToFalse() {
        AzureRedisPasswordlessProperties properties = new AzureRedisPasswordlessProperties();
        assertFalse(properties.isPasswordlessEnabled());
    }
}

