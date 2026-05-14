// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.util;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * Test-only {@link AuthenticateCallbackHandler} that authenticates against Azure Event Hubs Kafka endpoint
 * using the same {@link TokenCredential} chain as the rest of the integration tests
 * ({@link TestCredentialUtils#getIntegrationTestTokenCredential()}).
 *
 * <p>This bypasses the production {@code KafkaOAuth2AuthenticateCallbackHandler}, which on a CI agent ends
 * up using a {@code DefaultAzureCredential} that resolves to the agent's managed identity (which is not
 * granted the {@code Azure Event Hubs Data Owner} role by the test bicep). The federated
 * {@code AzurePipelinesCredential} used here corresponds to the {@code testApplicationOid} that the bicep
 * actually grants permissions to.</p>
 */
public class TestKafkaOAuth2CallbackHandler implements AuthenticateCallbackHandler {

    private static final Duration ACCESS_TOKEN_REQUEST_BLOCK_TIME = Duration.ofSeconds(30);
    private static final String TOKEN_AUDIENCE_FORMAT = "%s://%s/.default";

    private TokenCredential credential;
    private TokenRequestContext tokenRequestContext;

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        List<String> bootstrapServers = (List<String>) configs.get(BOOTSTRAP_SERVERS_CONFIG);
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            throw new IllegalArgumentException("bootstrap.servers must be configured for Azure Event Hubs.");
        }
        String bootstrap = bootstrapServers.get(0);
        URI uri = URI.create("https://" + bootstrap);
        String audience = String.format(TOKEN_AUDIENCE_FORMAT, uri.getScheme(), uri.getHost());

        this.tokenRequestContext = new TokenRequestContext();
        this.tokenRequestContext.addScopes(audience);
        this.credential = TestCredentialUtils.getIntegrationTestTokenCredential();
    }

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
                try {
                    AccessToken accessToken = credential.getToken(tokenRequestContext).block(ACCESS_TOKEN_REQUEST_BLOCK_TIME);
                    if (accessToken == null) {
                        oauthCallback.error("invalid_grant", "Failed to acquire token from credential chain.", null);
                    } else {
                        oauthCallback.token(new SimpleOAuthBearerToken(accessToken));
                    }
                } catch (RuntimeException e) {
                    oauthCallback.error("invalid_grant", e.getMessage(), null);
                }
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    @Override
    public void close() {
        // NOOP
    }

    /**
     * Minimal {@link OAuthBearerToken} that exposes only what the Kafka client needs for SASL handshake:
     * the bearer token string and its expiration time.
     */
    private static final class SimpleOAuthBearerToken implements OAuthBearerToken {
        private final AccessToken accessToken;

        SimpleOAuthBearerToken(AccessToken accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public String value() {
            return accessToken.getToken();
        }

        @Override
        public Long startTimeMs() {
            return null;
        }

        @Override
        public long lifetimeMs() {
            return accessToken.getExpiresAt().toInstant().toEpochMilli();
        }

        @Override
        public Set<String> scope() {
            return null;
        }

        @Override
        public String principalName() {
            return "azure-event-hubs-kafka-oauth-test";
        }
    }
}
