// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.kafka;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaConfigs;
import com.azure.spring.cloud.service.implementation.kafka.AzureOAuthBearerToken;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        String bootstrapServer = Arrays.asList(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).get(0).toString();
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
                OAuthBearerToken token = getOAuthBearerToken();
                oauthCallback.token(token);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private TokenCredential getTokenCredential() {
        if (credential == null) {
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
        }
        return credential;
    }

    private OAuthBearerToken getOAuthBearerToken() {
        if (accessToken == null || accessToken.isExpired()) {
            TokenCredential credential = getTokenCredential();
            TokenRequestContext request = new TokenRequestContext();
            request.addScopes(tokenAudience);
            request.setTenantId(tenantId);
            accessToken = Optional.ofNullable(credential.getToken(request).block(Duration.ofSeconds(30)))
                    .map(token -> convertToken(token))
                    .orElse(null);
        }

        return accessToken;
    }

    AzureOAuthBearerToken convertToken(AccessToken sourceToken) {
        String token = sourceToken.getToken();
        JWTClaimsSet claims;
        try {
            claims = JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException exception) {
            throw new RuntimeException("Unable to parse access token", exception);
        }
        long startTimeMs = claims.getIssueTime().getTime();
        long lifetimeMs = claims.getExpirationTime().getTime();
        // Referring to https://docs.microsoft.com/azure/active-directory/develop/access-tokens#payload-claims, the scp
        // claim is a String which is presented as a space separated list.
        Set<String> scope = Optional.ofNullable(claims.getClaim("scp"))
                .map(s -> Arrays.stream(((String) s).split(" ")).collect(Collectors.toSet()))
                .orElse(null);
        String principalName = (String) claims.getClaim("upn");
        return new AzureOAuthBearerToken(token, startTimeMs, lifetimeMs, scope, principalName);
    }

    @Override
    public void close() throws KafkaException {
        // NOOP
    }
}
