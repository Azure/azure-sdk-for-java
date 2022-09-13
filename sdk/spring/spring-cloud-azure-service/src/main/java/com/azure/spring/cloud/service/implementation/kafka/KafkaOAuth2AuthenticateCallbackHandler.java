// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import reactor.core.publisher.Mono;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * {@link AuthenticateCallbackHandler} implementation for OAuth2 authentication with Azure Event Hubs.
 */
public class KafkaOAuth2AuthenticateCallbackHandler implements AuthenticateCallbackHandler {

    private static final Duration ACCESS_TOKEN_REQUEST_BLOCK_TIME = Duration.ofSeconds(30);
    private static final String TOKEN_AUDIENCE_FORMAT = "%s://%s/.default";

    private final AzureKafkaProperties properties;
    private final AzureTokenCredentialResolver tokenCredentialResolver;

    private TokenCredential credential;
    private String tokenAudience;
    private Function<TokenCredential, Mono<AzureOAuthBearerToken>> resolveToken;

    public KafkaOAuth2AuthenticateCallbackHandler() {
        this(null, null);
    }

    public KafkaOAuth2AuthenticateCallbackHandler(AzureKafkaProperties properties, AzureTokenCredentialResolver tokenCredentialResolver) {
        this.properties = properties == null ? new AzureKafkaProperties() : properties;
        this.tokenCredentialResolver = tokenCredentialResolver == null ? new AzureTokenCredentialResolver() : tokenCredentialResolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        List<String> bootstrapServers = (List<String>) configs.get(BOOTSTRAP_SERVERS_CONFIG);
        if (bootstrapServers == null || bootstrapServers.size() != 1) {
            throw new IllegalArgumentException("Invalid bootstrap servers configured for Azure Event Hubs for Kafka! Must supply exactly 1 non-null bootstrap server configuration,"
                    + " with the format as {YOUR.EVENTHUBS.FQDN}:9093.");
        }
        String bootstrapServer = bootstrapServers.get(0);
        if (bootstrapServer == null || !bootstrapServer.endsWith(":9093")) {
            throw new IllegalArgumentException("Invalid bootstrap server configured for Azure Event Hubs for Kafka! The format should be {YOUR.EVENTHUBS.FQDN}:9093.");
        }
        URI uri = URI.create("https://" + bootstrapServer);
        this.tokenAudience = String.format(TOKEN_AUDIENCE_FORMAT, uri.getScheme(), uri.getHost());
        this.credential = (TokenCredential) configs.get(AZURE_TOKEN_CREDENTIAL);
        AzureKafkaPropertiesUtils.convertConfigMapToAzureProperties(configs, properties);

        TokenRequestContext request = buildTokenRequestContext();
        this.resolveToken = tokenCredential -> tokenCredential.getToken(request).map(AzureOAuthBearerToken::new);
    }

    private TokenRequestContext buildTokenRequestContext() {
        TokenRequestContext request = new TokenRequestContext();
        request.addScopes(tokenAudience);
        request.setTenantId(properties.getProfile().getTenantId());
        return request;
    }

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
                this.resolveToken
                    .apply(getTokenCredential())
                    .doOnNext(oauthCallback::token)
                    .doOnError(throwable -> oauthCallback.error("invalid_grant", throwable.getMessage(), null))
                    .block(ACCESS_TOKEN_REQUEST_BLOCK_TIME);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private TokenCredential getTokenCredential() {
        if (credential == null) {
            // Resolve the token credential when there is no credential passed from configs.
            credential = tokenCredentialResolver.resolve(properties);
            if (credential == null) {
                // Create DefaultAzureCredential when no credential can be resolved from configs.
                credential = new DefaultAzureCredentialBuilderFactory(properties).build().build();
            }
        }
        return credential;
    }

    @Override
    public void close() {
        // NOOP
    }
}
