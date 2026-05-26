// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.apache.kafka.common.config.types.Password;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import reactor.core.publisher.Mono;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;

/**
 * {@link AuthenticateCallbackHandler} implementation for OAuth2 authentication with Azure Event Hubs.
 */
public class KafkaOAuth2AuthenticateCallbackHandler implements AuthenticateCallbackHandler {

    private static final Duration ACCESS_TOKEN_REQUEST_BLOCK_TIME = Duration.ofSeconds(30);
    private static final String TOKEN_AUDIENCE_FORMAT = "%s://%s/.default";

    private final AzurePasswordlessProperties properties;
    private final AzureCredentialResolver<TokenCredential> externalTokenCredentialResolver;

    private AzureCredentialResolver<TokenCredential> tokenCredentialResolver;
    private Function<TokenCredential, Mono<AzureOAuthBearerToken>> resolveToken;

    public KafkaOAuth2AuthenticateCallbackHandler() {
        this(null, null);
    }

    public KafkaOAuth2AuthenticateCallbackHandler(AzurePasswordlessProperties properties, AzureCredentialResolver<TokenCredential> externalTokenCredentialResolver) {
        this.properties = properties == null ? new AzurePasswordlessProperties() : properties;
        this.externalTokenCredentialResolver = externalTokenCredentialResolver == null ? new AzureTokenCredentialResolver() : externalTokenCredentialResolver;
    }

    @Override
    public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        if (configs.get(SASL_JAAS_CONFIG) instanceof Password) {
            AzureKafkaPropertiesUtils.copyJaasPropertyToAzureProperties(((Password) configs.get(SASL_JAAS_CONFIG)).value(), properties);
        }
        TokenRequestContext request = buildTokenRequestContext(configs);
        this.resolveToken = tokenCredential -> tokenCredential.getToken(request).map(AzureOAuthBearerToken::new);
        this.tokenCredentialResolver = new InternalCredentialResolver(externalTokenCredentialResolver, configs);
    }

    private TokenRequestContext buildTokenRequestContext(Map<String, ?> configs) {
        URI uri = buildEventHubsServerUri(configs);
        String tokenAudience = buildTokenAudience(uri);

        TokenRequestContext request = new TokenRequestContext();
        request.addScopes(tokenAudience);
        request.setTenantId(properties.getProfile().getTenantId());
        return request;
    }

    @SuppressWarnings("unchecked")
    private URI buildEventHubsServerUri(Map<String, ?> configs) {
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
        return uri;
    }

    private String buildTokenAudience(URI uri) {
        return String.format(TOKEN_AUDIENCE_FORMAT, uri.getScheme(), uri.getHost());
    }

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof OAuthBearerTokenCallback) {
                OAuthBearerTokenCallback oauthCallback = (OAuthBearerTokenCallback) callback;
                this.resolveToken
                    .apply(tokenCredentialResolver.resolve(properties))
                    .doOnNext(oauthCallback::token)
                    .doOnError(throwable -> oauthCallback.error("invalid_grant", throwable.getMessage(), null))
                    .block(ACCESS_TOKEN_REQUEST_BLOCK_TIME);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    @Override
    public void close() {
        // NOOP
    }

    private static class InternalCredentialResolver implements AzureCredentialResolver<TokenCredential> {
        private static final ClientLogger LOGGER = new ClientLogger(InternalCredentialResolver.class);

        private final AzureCredentialResolver<TokenCredential> delegated;
        private final Map<String, ?> configs;
        private TokenCredential credential;

        InternalCredentialResolver(AzureCredentialResolver<TokenCredential> delegated, Map<String, ?> configs) {
            this.delegated = delegated;
            this.configs = configs;
        }

        @Override
        public TokenCredential resolve(AzureProperties properties) {
            if (credential == null) {
                credential = (TokenCredential) configs.get(AZURE_TOKEN_CREDENTIAL);
                // Resolve the token credential when there is no credential passed from configs.
                if (credential == null) {
                    credential = delegated.resolve(properties);
                    if (credential == null) {
                        TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilderFactory(properties).build().build();
                        TokenCredential pipelinesCredential = tryBuildAzurePipelinesCredential(properties);
                        if (pipelinesCredential == null) {
                            credential = defaultAzureCredential;
                        } else {
                            credential = new ChainedTokenCredentialBuilder()
                                .addLast(pipelinesCredential)
                                .addLast(defaultAzureCredential)
                                .build();
                        }
                    }
                }
            }
            return credential;
        }

        @Override
        public boolean isResolvable(AzureProperties properties) {
            return true;
        }

        /**
         * Attempts to build an {@code AzurePipelinesCredential} from the Azure DevOps federated
         * workload-identity environment variables. Returns {@code null} when any of the four
         * caller-provided variables ({@code AZURESUBSCRIPTION_SERVICE_CONNECTION_ID},
         * {@code AZURESUBSCRIPTION_CLIENT_ID}, {@code AZURESUBSCRIPTION_TENANT_ID},
         * {@code SYSTEM_ACCESSTOKEN}) are missing, or when {@code AzurePipelinesCredentialBuilder#build()}
         * itself fails (e.g. {@code SYSTEM_OIDCREQUESTURI} is unavailable outside an Azure DevOps
         * job). The authority host is taken from the {@link AzureProperties} profile so that the
         * credential targets the correct cloud (public, China, US Gov).
         */
        private static TokenCredential tryBuildAzurePipelinesCredential(AzureProperties properties) {
            Configuration config = Configuration.getGlobalConfiguration();
            String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
            String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
            String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
            String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");
            if (isNullOrEmpty(serviceConnectionId)
                || isNullOrEmpty(clientId)
                || isNullOrEmpty(tenantId)
                || isNullOrEmpty(systemAccessToken)) {
                return null;
            }
            try {
                AzurePipelinesCredentialBuilder builder = new AzurePipelinesCredentialBuilder()
                    .systemAccessToken(systemAccessToken)
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .serviceConnectionId(serviceConnectionId);
                String authorityHost = resolveAuthorityHost(properties);
                if (!isNullOrEmpty(authorityHost)) {
                    builder.authorityHost(authorityHost);
                }
                return builder.build();
            } catch (RuntimeException e) {
                LOGGER.verbose("Failed to build AzurePipelinesCredential, will fall back to DefaultAzureCredential.", e);
                return null;
            }
        }

        private static String resolveAuthorityHost(AzureProperties properties) {
            if (properties == null || properties.getProfile() == null || properties.getProfile().getEnvironment() == null) {
                return null;
            }
            return properties.getProfile().getEnvironment().getActiveDirectoryEndpoint();
        }

        private static boolean isNullOrEmpty(String value) {
            return value == null || value.isEmpty();
        }
    }
}
