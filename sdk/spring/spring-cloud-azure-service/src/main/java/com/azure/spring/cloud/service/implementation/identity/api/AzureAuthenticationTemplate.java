package com.azure.spring.cloud.service.implementation.identity.api;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;
import com.azure.spring.cloud.service.implementation.identity.api.token.AccessTokenResolver;
import com.azure.spring.cloud.service.implementation.identity.api.token.AccessTokenResolverOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 1. get a token credential
 * 2. get an access token from token credential
 * 3. get the password from access token
 */
public class AzureAuthenticationTemplate {

    private static final ClientLogger LOGGER = new ClientLogger(AzureAuthenticationTemplate.class);

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private TokenCredentialProvider tokenCredentialProvider;

    private AccessTokenResolver accessTokenResolver;

    private Configuration configuration;

    public AzureAuthenticationTemplate() {
        this.tokenCredentialProvider = null;
        this.accessTokenResolver = null;
        this.configuration = new ConfigurationBuilder().build();
    }

    public AzureAuthenticationTemplate(TokenCredentialProvider tokenCredentialProvider,
                                       AccessTokenResolver accessTokenResolver) {
        this.tokenCredentialProvider = tokenCredentialProvider;
        this.accessTokenResolver = accessTokenResolver;
    }

    public AzureAuthenticationTemplate(Configuration configuration) {
        this();
        this.configuration = configuration;
    }

    protected AccessTokenResolver getAccessTokenResolver() {
        return accessTokenResolver;
    }

    protected TokenCredentialProvider getTokenCredentialProvider() {
        return tokenCredentialProvider;
    }

    protected void init(Properties properties) {
        if (isInitialized.compareAndSet(false, true)) {
            LOGGER.info("Initializing AzureAuthenticationTemplate.");

            properties.entrySet().forEach(entry ->
                    configuration.put(entry.getKey().toString(), entry.getValue().toString()));

            if (getTokenCredentialProvider() == null) {
                this.tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(this.configuration));
            }

            if (getAccessTokenResolver() == null) {
                this.accessTokenResolver = AccessTokenResolver.createDefault(new AccessTokenResolverOptions(this.configuration));
            }
            LOGGER.info("Initialized AzureAuthenticationTemplate.");
        } else {
            LOGGER.info("AzureAuthenticationTemplate has already initialized.");
        }
    }

    protected Mono<String> getTokenAsPasswordAsync(){
        if (!isInitialized.get()) {
            throw new IllegalStateException("must call init() first");
        }
        return Mono.fromSupplier(getTokenCredentialProvider())
                   .flatMap(getAccessTokenResolver())
                   .filter(token -> !token.isExpired())
                   .map(AccessToken::getToken);
    }

    protected String getTokenAsPassword() {
        return getTokenAsPasswordAsync().block(getBlockTimeout());
    }

    protected Duration getBlockTimeout(){
        return Duration.ofSeconds(30);
    }

}
