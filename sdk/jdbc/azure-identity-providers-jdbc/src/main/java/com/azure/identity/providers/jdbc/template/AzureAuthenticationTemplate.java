// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.template;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.providers.jdbc.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.providers.jdbc.api.token.AccessTokenResolver;
import com.azure.identity.providers.jdbc.implementation.token.AccessTokenResolverOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Template class can be extended to get password from access token.
 */
public class AzureAuthenticationTemplate {

    private static final ClientLogger LOGGER = new ClientLogger(AzureAuthenticationTemplate.class);

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private TokenCredentialProvider tokenCredentialProvider;

    private AccessTokenResolver accessTokenResolver;

    private Properties properties = new Properties();

    public AzureAuthenticationTemplate() {
        this.tokenCredentialProvider = null;
        this.accessTokenResolver = null;
    }

    public AzureAuthenticationTemplate(TokenCredentialProvider tokenCredentialProvider,
                                       AccessTokenResolver accessTokenResolver) {
        this.tokenCredentialProvider = tokenCredentialProvider;
        this.accessTokenResolver = accessTokenResolver;
    }

    public AzureAuthenticationTemplate(Properties properties) {
        this();
        if (properties != null) {
            this.properties = properties;
        }
    }

    public void init(Properties properties) {
        if (isInitialized.compareAndSet(false, true)) {
            LOGGER.verbose("Initializing AzureAuthenticationTemplate.");

            if (properties != null) {
                this.properties.putAll(properties);
            }

            if (getTokenCredentialProvider() == null) {
                this.tokenCredentialProvider = TokenCredentialProvider.createDefault(
                    new TokenCredentialProviderOptions(this.properties));
            }

            if (getAccessTokenResolver() == null) {
                this.accessTokenResolver = AccessTokenResolver.createDefault(
                    new AccessTokenResolverOptions(this.properties));
            }

            LOGGER.verbose("Initialized AzureAuthenticationTemplate.");
        } else {
            LOGGER.info("AzureAuthenticationTemplate has already initialized.");
        }
    }

    public Mono<String> getTokenAsPasswordAsync() {
        if (!isInitialized.get()) {
            throw new IllegalStateException("must call init() first");
        }
        return Mono.fromSupplier(getTokenCredentialProvider())
                   .flatMap(getAccessTokenResolver())
                   .filter(token -> !token.isExpired())
                   .map(AccessToken::getToken);
    }

    public String getTokenAsPassword() {
        return getTokenAsPasswordAsync().block(getBlockTimeout());
    }

    protected AccessTokenResolver getAccessTokenResolver() {
        return accessTokenResolver;
    }

    protected TokenCredentialProvider getTokenCredentialProvider() {
        return tokenCredentialProvider;
    }

    protected Duration getBlockTimeout() {
        return Duration.ofSeconds(30);
    }

    protected AtomicBoolean getIsInitialized() {
        return isInitialized;
    }

    protected Properties getProperties() {
        return properties;
    }
}
