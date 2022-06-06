// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.core.implementation.credential.provider.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.kafka.AzureOAuthBearerToken;
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

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * {@link AuthenticateCallbackHandler} implementation for OAuth2 authentication with Azure Event Hubs.
 */
public class KafkaOAuth2AuthenticateCallbackHandler implements AuthenticateCallbackHandler {
    private TokenCredential credential;
    private AzureOAuthBearerToken accessToken;
    private String clientId;
    private String tenantId;
    private String authorityHost;
    private String tokenAudience;

    public static TokenCredentialProvider tokenCredentialProvider;

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        String bootstrapServer = Arrays.asList(configs.get(BOOTSTRAP_SERVERS_CONFIG)).get(0).toString();
        bootstrapServer = bootstrapServer.replaceAll("\\[|\\]", "");
        URI uri = URI.create("https://" + bootstrapServer);
        this.tokenAudience = uri.getScheme() + "://" + uri.getHost();
        this.clientId = (String) configs.getOrDefault(AzureKafkaConfigs.CLIENT_ID_CONFIG, null);
        this.tenantId = (String) configs.getOrDefault(AzureKafkaConfigs.TENANT_ID_CONFIG, null);
        this.authorityHost = (String) configs.getOrDefault(AzureKafkaConfigs.AAD_ENDPOINT_CONFIG, null);
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
            if (tokenCredentialProvider == null) {
                DefaultAzureCredentialBuilder credentialBuilder = new DefaultAzureCredentialBuilder();
                if (clientId != null && !clientId.isEmpty()) {
                    credentialBuilder.managedIdentityClientId(clientId);
                }
                if (tenantId != null && !tenantId.isEmpty()) {
                    credentialBuilder.tenantId(tenantId);
                }
                if (authorityHost != null && !authorityHost.isEmpty()) {
                    credentialBuilder.authorityHost(authorityHost);
                }
                credential = credentialBuilder.build();
            } else {
                credential = tokenCredentialProvider.getTokenCredential();
            }
        }
        return credential;
    }

    private OAuthBearerToken getOAuthBearerToken() {
        if (accessToken == null || accessToken.isExpired()) {
            TokenRequestContext request = new TokenRequestContext();
            request.addScopes(tokenAudience);
            request.setTenantId(tenantId);
            accessToken = new AzureOAuthBearerToken(credential.getToken(request).block(Duration.ofSeconds(30)));
        }

        return accessToken;
    }

    @Override
    public void close() {
        // NOOP
    }
}
