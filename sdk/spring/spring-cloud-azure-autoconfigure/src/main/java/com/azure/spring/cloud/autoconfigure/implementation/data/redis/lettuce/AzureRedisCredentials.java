// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.redis.lettuce;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.lettuce.core.RedisCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Properties;

import static com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider.PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME;

public class AzureRedisCredentials implements RedisCredentials {

    public static final Logger LOGGER = LoggerFactory.getLogger(AzureRedisCredentials.class);
    private final AzureAuthenticationTemplate azureAuthenticationTemplate;
    private final String username;
    private static final String SPRING_CLOUD_AZURE_REDIS_PREFIX = "spring.data.redis.azure";
    private String passwordlessPropertiesPrefix = SPRING_CLOUD_AZURE_REDIS_PREFIX;
    private GenericApplicationContext applicationContext;

    /**
     * Create instance of Azure Redis Credentials
     */
    public AzureRedisCredentials(String username, PasswordlessProperties passwordlessProperties) {
        this(null, null, username, passwordlessProperties);
    }

    public AzureRedisCredentials(GenericApplicationContext applicationContext,
                                 String username,
                                 PasswordlessProperties passwordlessProperties) {
        this(null, applicationContext, username, passwordlessProperties);
    }

    public AzureRedisCredentials(String passwordlessPropertiesPrefix,
                                 GenericApplicationContext applicationContext,
                                 String username,
                                 PasswordlessProperties passwordlessProperties) {
        Objects.requireNonNull(passwordlessProperties, "PasswordlessProperties is required");
        this.applicationContext = applicationContext;
        azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        Properties properties = passwordlessProperties.toPasswordlessProperties();
        if (applicationContext == null) {
            LOGGER.debug("The applicationContext is not available, unable to obtain the Redis passwordless "
                + "token credential bean, will use default credential.");
        } else {
            enhancePasswordlessProperties(properties, passwordlessProperties);
        }
        if (StringUtils.hasText(passwordlessPropertiesPrefix)) {
            this.passwordlessPropertiesPrefix = passwordlessPropertiesPrefix;
        }
        azureAuthenticationTemplate.init(properties);
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

    private void enhancePasswordlessProperties(Properties properties, PasswordlessProperties passwordlessProperties) {
        if (!passwordlessProperties.isPasswordlessEnabled()) {
            if (!passwordlessProperties.isPasswordlessEnabled()) {
                LOGGER.debug("Feature passwordless authentication is not enabled({}.passwordless-enabled=false), "
                    + "skip enhancing Redis properties.", passwordlessPropertiesPrefix);
                return;
            }
        }

        String tokenCredentialBeanName = passwordlessProperties.getCredential().getTokenCredentialBeanName();
        if (StringUtils.hasText(tokenCredentialBeanName)) {
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
        } else {
            TokenCredentialProvider tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(properties));
            TokenCredential tokenCredential = tokenCredentialProvider.get();

            tokenCredentialBeanName = PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME + "." + passwordlessPropertiesPrefix;
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(properties, tokenCredentialBeanName);
            applicationContext.registerBean(tokenCredentialBeanName, TokenCredential.class, () -> tokenCredential);
        }

        AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(properties, SpringTokenCredentialProvider.class.getName());
        AuthProperty.AUTHORITY_HOST.setProperty(properties, passwordlessProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
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
