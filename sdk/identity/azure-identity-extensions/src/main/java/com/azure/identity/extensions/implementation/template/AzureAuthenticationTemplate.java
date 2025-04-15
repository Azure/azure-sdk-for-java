// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.template;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.extensions.implementation.credential.provider.CachingTokenCredentialProvider;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.identity.extensions.implementation.token.AccessTokenResolver;
import com.azure.identity.extensions.implementation.token.AccessTokenResolverOptions;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import reactor.core.publisher.Mono;
import static com.azure.identity.extensions.implementation.enums.AuthProperty.GET_TOKEN_TIMEOUT;

/**
 * Template class can be extended to get password from access token.
 */
public class AzureAuthenticationTemplate {

    private static final ClientLogger LOGGER = new ClientLogger(AzureAuthenticationTemplate.class);

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private TokenCredentialProvider tokenCredentialProvider;

    private AccessTokenResolver accessTokenResolver;

    private long accessTokenTimeoutInSeconds;

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
     * @param tokenCredentialProvider A TokenCredentialProvider class instance.
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
                TokenCredentialProviderOptions options = new TokenCredentialProviderOptions(properties);
                this.tokenCredentialProvider = TokenCredentialProvider.createDefault(options);

                if (Boolean.TRUE.equals(AuthProperty.TOKEN_CREDENTIAL_CACHE_ENABLED.getBoolean(properties))) {
                    this.tokenCredentialProvider
                        = new CachingTokenCredentialProvider(options, this.tokenCredentialProvider);
                }
            }

            if (getAccessTokenResolver() == null) {
                this.accessTokenResolver
                    = AccessTokenResolver.createDefault(new AccessTokenResolverOptions(properties));
            }

            if (properties.containsKey(GET_TOKEN_TIMEOUT.getPropertyKey())) {
                accessTokenTimeoutInSeconds = Long.parseLong(GET_TOKEN_TIMEOUT.get(properties));
            } else {
                accessTokenTimeoutInSeconds = 30;
                LOGGER.verbose("Use default access token timeout: {} seconds.", accessTokenTimeoutInSeconds);
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
     * Return a password which is a Microsoft Entra token that can be used to authenticate.
     *
     * Always return a valid value, and the value won't expire in a threshold.
     *
     * To reduce the underlying auth method calling(like managed identity endpoint has a rate limit), as well as to improve the performance, multiple calls to
     * this method may return cached value.
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
        return Duration.ofSeconds(accessTokenTimeoutInSeconds);
    }

    AtomicBoolean getIsInitialized() {
        return isInitialized;
    }
}
