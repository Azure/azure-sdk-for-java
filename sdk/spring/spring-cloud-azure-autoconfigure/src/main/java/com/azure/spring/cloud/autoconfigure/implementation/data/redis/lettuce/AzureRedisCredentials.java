// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.redis.lettuce;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import io.lettuce.core.RedisCredentials;

import java.util.Objects;
import java.util.Properties;

public class AzureRedisCredentials implements RedisCredentials {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;
    private final String username;

    /**
     * Create instance of Azure Redis Credentials
     * @param username the username to be used for authentication.
     */
    public AzureRedisCredentials(String username, PasswordlessProperties passwordlessProperties) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(passwordlessProperties, "PasswordlessProperties is required");
        this.username = username;
        azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        azureAuthenticationTemplate.init(passwordlessProperties.toPasswordlessProperties());
    }

    public AzureRedisCredentials(String username, PasswordlessProperties passwordlessProperties, TokenCredential tokenCredential) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(passwordlessProperties, "PasswordlessProperties is required");
        Objects.requireNonNull(tokenCredential, "TokenCredential is required");
        this.username = username;
        this.azureAuthenticationTemplate = new AzureAuthenticationTemplate(() -> tokenCredential, null);
        this.azureAuthenticationTemplate.init(passwordlessProperties.toPasswordlessProperties());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean hasUsername() {
        return username != null;
    }

    @Override
    public char[] getPassword() {
        return azureAuthenticationTemplate.getTokenAsPassword().toCharArray();
    }

    @Override
    public boolean hasPassword() {
        return true;
    }
}
