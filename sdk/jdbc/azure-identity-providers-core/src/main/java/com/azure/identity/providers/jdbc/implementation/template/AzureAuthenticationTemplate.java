// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.template;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.providers.jdbc.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.providers.jdbc.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.providers.jdbc.implementation.token.AccessTokenResolver;
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


    /**
     * Default constructor for AzureAuthenticationTemplate
     */
    public AzureAuthenticationTemplate() {
        this.tokenCredentialProvider = null;
        this.accessTokenResolver = null;
    }

    /**
     * AzureAuthenticationTemplate constructor.
     *
     * @param tokenCredentialProvider An TokenCredentialProvider class instance.
     * @param accessTokenResolver An AccessTokenResolver class instance.
     */
    public AzureAuthenticationTemplate(TokenCredentialProvider tokenCredentialProvider,
                                       AccessTokenResolver accessTokenResolver) {
        this.tokenCredentialProvider = tokenCredentialProvider;
        this.accessTokenResolver = accessTokenResolver;
    }

    /**
     * Initialize tokenCredentialProvider and accessTokenResolver with given properties.
     *
     * @param properties properties used to initialize AzureAuthenticationTemplate
     */
    public void init(Properties properties) {
        if (isInitialized.compareAndSet(false, true)) {
            LOGGER.verbose("Initializing AzureAuthenticationTemplate.");

            if (getTokenCredentialProvider() == null) {
                this.tokenCredentialProvider = TokenCredentialProvider.createDefault(
                    new TokenCredentialProviderOptions(properties));
            }

            if (getAccessTokenResolver() == null) {
                this.accessTokenResolver = AccessTokenResolver.createDefault(
                    new AccessTokenResolverOptions(properties));
            }

            LOGGER.verbose("Initialized AzureAuthenticationTemplate.");
        } else {
            LOGGER.info("AzureAuthenticationTemplate has already initialized.");
        }
    }

    /**
     * @return a Publisher emitting a String password response
     * @throws IllegalStateException {@link AzureAuthenticationTemplate#init(java.util.Properties)}
     *                               must be called before calling getTokenAsPasswordAsync().
     */
    public Mono<String> getTokenAsPasswordAsync() {
        if (!isInitialized.get()) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("must call init() first"));
        }
        return Mono.fromSupplier(getTokenCredentialProvider())
                   .flatMap(getAccessTokenResolver())
                   .filter(token -> !token.isExpired())
                   .map(AccessToken::getToken);
    }

    /**
     * Get a String token value represents a password.
     *
     * @return A String value represents a password.
     */
    public String getTokenAsPassword() {
        return getTokenAsPasswordAsync().block(getBlockTimeout());
    }

    AccessTokenResolver getAccessTokenResolver() {
        return accessTokenResolver;
    }

    TokenCredentialProvider getTokenCredentialProvider() {
        return tokenCredentialProvider;
    }

    Duration getBlockTimeout() {
        return Duration.ofSeconds(30);
    }

    AtomicBoolean getIsInitialized() {
        return isInitialized;
    }

}
