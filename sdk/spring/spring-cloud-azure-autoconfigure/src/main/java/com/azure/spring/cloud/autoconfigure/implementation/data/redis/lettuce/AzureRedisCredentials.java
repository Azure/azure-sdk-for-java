// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.redis.lettuce;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.lettuce.core.RedisCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Objects;

public class AzureRedisCredentials implements RedisCredentials {

    public static final Logger LOGGER = LoggerFactory.getLogger(AzureRedisCredentials.class);
    private final AzureAuthenticationTemplate azureAuthenticationTemplate;
    private final String username;

    /**
     * Create instance of Azure Redis Credentials
     */
    public AzureRedisCredentials(String username, PasswordlessProperties passwordlessProperties) {
        Objects.requireNonNull(passwordlessProperties, "PasswordlessProperties is required");
        azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        azureAuthenticationTemplate.init(passwordlessProperties.toPasswordlessProperties());
        this.username = resolveUsername(azureAuthenticationTemplate, username);
    }

    public AzureRedisCredentials(String username, PasswordlessProperties passwordlessProperties, TokenCredential tokenCredential) {
        Objects.requireNonNull(passwordlessProperties, "PasswordlessProperties is required");
        Objects.requireNonNull(tokenCredential, "TokenCredential is required");
        this.azureAuthenticationTemplate = new AzureAuthenticationTemplate(() -> tokenCredential, null);
        this.azureAuthenticationTemplate.init(passwordlessProperties.toPasswordlessProperties());
        this.username = resolveUsername(azureAuthenticationTemplate, username);
    }

    private static String resolveUsername(AzureAuthenticationTemplate authenticationTemplate, String username) {
        if (StringUtils.hasText(username)) {
            LOGGER.debug("Username is set to {}, skipping retrieving it from the JWT", username);
            return username;
        }
        try {
            String tokenAsPassword = authenticationTemplate.getTokenAsPassword();
            JWT jwt = JWTParser.parse(tokenAsPassword);
            String oid = jwt.getJWTClaimsSet().getClaim("oid").toString();
            LOGGER.debug("Username is resolved to {}", oid);
            return oid;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse the token, can't get the username from the token", e);
        }
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
