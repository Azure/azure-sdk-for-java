// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * {@link AuthenticateCallbackHandler} implementation for OAuth2 authentication with Azure Event Hubs.
 */
public class KafkaOAuth2AuthenticateCallbackHandler implements AuthenticateCallbackHandler {
    private final AzureKafkaProperties properties = new AzureKafkaProperties();
    private final DefaultAzureCredentialBuilderFactory defaultAzureCredentialBuilderFactory =
        new DefaultAzureCredentialBuilderFactory(properties);
    private TokenCredential credential;
    private AzureOAuthBearerToken accessToken;
    private String tokenAudience;

    private final AzureTokenCredentialResolver tokenCredentialResolver = new AzureTokenCredentialResolver();

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        String bootstrapServer = Arrays.asList(configs.get(BOOTSTRAP_SERVERS_CONFIG)).get(0).toString();
        bootstrapServer = bootstrapServer.replaceAll("\\[|\\]", "");
        URI uri = URI.create("https://" + bootstrapServer);
        this.tokenAudience = uri.getScheme() + "://" + uri.getHost();
        credential = (TokenCredential) configs.get(AZURE_TOKEN_CREDENTIAL);
        AzureKafkaPropertiesUtils.convertConfigMapToAzureProperties(configs, properties);
    }

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
                credential = getTokenCredential();
                OAuthBearerToken token = getOAuthBearerToken();
                oauthCallback.token(token);
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
                credential = defaultAzureCredentialBuilderFactory.build().build();
            }
        }
        return credential;
    }

    private OAuthBearerToken getOAuthBearerToken() {
        if (accessToken == null || accessToken.isExpired()) {
            TokenRequestContext request = new TokenRequestContext();
            request.addScopes(tokenAudience);
            request.setTenantId(properties.getProfile().getTenantId());
            AccessToken accessToken = credential.getToken(request).block(Duration.ofSeconds(30));
            if (accessToken != null) {
                this.accessToken = new AzureOAuthBearerToken(accessToken);
            }
        }
        return accessToken;
    }

    @Override
    public void close() {
        // NOOP
    }
}
