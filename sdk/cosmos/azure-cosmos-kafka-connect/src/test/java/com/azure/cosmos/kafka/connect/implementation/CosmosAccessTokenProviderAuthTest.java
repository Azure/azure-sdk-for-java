// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.kafka.connect.CosmosAccessToken;
import com.azure.cosmos.kafka.connect.CosmosAccessTokenProvider;
import com.azure.cosmos.kafka.connect.CosmosSinkConnector;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigValue;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the AccessTokenProvider auth type.
 */
public class CosmosAccessTokenProviderAuthTest {
    private static final String PROVIDER_CLASS_CONFIG = "azure.cosmos.auth.accessTokenProvider.class";
    private static final OffsetDateTime EXPIRES_AT = OffsetDateTime.of(2030, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test(groups = "unit")
    public void parsesAccessTokenProviderAuthConfig() {
        Map<String, String> configs = sinkConfigWithProvider(TestTokenProvider.class.getName());
        configs.put("my.provider.setting", "value");

        CosmosAuthConfig authConfig = new CosmosSinkConfig(configs).getAccountConfig().getCosmosAuthConfig();

        assertThat(authConfig).isInstanceOf(CosmosAccessTokenProviderAuthConfig.class);
        CosmosAccessTokenProviderAuthConfig providerAuthConfig = (CosmosAccessTokenProviderAuthConfig) authConfig;
        assertThat(providerAuthConfig.getProviderClassName()).isEqualTo(TestTokenProvider.class.getName());
        assertThat(providerAuthConfig.getProviderConfigs().get("my.provider.setting")).isEqualTo("value");
        assertThat(providerAuthConfig.toString()).doesNotContain("my.provider.setting");
    }

    @Test(groups = "unit")
    public void cacheKeyIsStableForSameProviderAndConfigs() {
        Map<String, String> configs = Collections.singletonMap("k", "v");

        CosmosAccessTokenProviderAuthConfig first = new CosmosAccessTokenProviderAuthConfig(TestTokenProvider.class.getName(), configs);
        CosmosAccessTokenProviderAuthConfig second = new CosmosAccessTokenProviderAuthConfig(TestTokenProvider.class.getName(), configs);
        CosmosAccessTokenProviderAuthConfig different = new CosmosAccessTokenProviderAuthConfig(
            TestTokenProvider.class.getName(), Collections.singletonMap("k", "other"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
    }

    @Test(groups = "unit")
    public void credentialReturnsProviderTokenForRequestedScopesAndClosesProvider() {
        CosmosAccessTokenProviderAuthConfig authConfig = new CosmosAccessTokenProviderAuthConfig(
            TestTokenProvider.class.getName(), Collections.singletonMap("k", "v"));
        TestTokenProvider provider = (TestTokenProvider) authConfig.createProvider();
        assertThat(provider.configs.get("k")).isEqualTo("v");

        CosmosAccessTokenProviderCredential credential = new CosmosAccessTokenProviderCredential(provider);
        TokenRequestContext request = new TokenRequestContext().addScopes("https://cosmos.azure.com/.default");

        AccessToken syncToken = credential.getTokenSync(request);
        AccessToken asyncToken = credential.getToken(request).block();

        assertThat(syncToken.getToken()).isEqualTo("token-for-https://cosmos.azure.com/.default");
        assertThat(syncToken.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(asyncToken.getToken()).isEqualTo(syncToken.getToken());

        credential.close();
        assertThat(provider.closed).isTrue();
    }

    @Test(groups = "unit")
    public void credentialRejectsNullToken() {
        CosmosAccessTokenProviderCredential credential = new CosmosAccessTokenProviderCredential(new NullTokenProvider());

        assertThatThrownBy(() -> credential.getTokenSync(new TokenRequestContext().addScopes("scope")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("returned a null token");
    }

    @Test(groups = "unit")
    public void accessTokenDoesNotExposeTokenValue() {
        assertThat(new CosmosAccessToken("secret-token", EXPIRES_AT).toString()).doesNotContain("secret-token");
        assertThatThrownBy(() -> new CosmosAccessToken("", EXPIRES_AT)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CosmosAccessToken("token", null)).isInstanceOf(NullPointerException.class);
    }

    @Test(groups = "unit")
    public void validationRequiresLoadableProviderClass() {
        assertThat(providerClassErrors(TestTokenProvider.class.getName())).isEmpty();
        assertThat(providerClassErrors("")).anyMatch(error -> error.contains("is required"));
        assertThat(providerClassErrors("com.example.DoesNotExist")).anyMatch(error -> error.contains("can not be found"));
        assertThat(providerClassErrors(NotAProvider.class.getName())).anyMatch(error -> error.contains("must implement"));
    }

    @Test(groups = "unit")
    public void throughputControlThresholdIsRejectedWithAccessTokenProvider() {
        Map<String, String> configs = sinkConfigWithProvider(TestTokenProvider.class.getName());
        configs.put("azure.cosmos.throughputControl.enabled", "true");
        configs.put("azure.cosmos.throughputControl.group.name", "group");
        configs.put("azure.cosmos.throughputControl.targetThroughputThreshold", "0.5");
        configs.put("azure.cosmos.throughputControl.globalControl.database.name", "db");
        configs.put("azure.cosmos.throughputControl.globalControl.container.name", "container");

        assertThat(errors(configs).get("azure.cosmos.throughputControl.targetThroughputThreshold"))
            .anyMatch(error -> error.contains("not supported"));
    }

    private static List<String> providerClassErrors(String providerClass) {
        return errors(sinkConfigWithProvider(providerClass)).get(PROVIDER_CLASS_CONFIG);
    }

    private static Map<String, List<String>> errors(Map<String, String> configs) {
        Config config = new CosmosSinkConnector().validate(configs);
        return config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
    }

    private static Map<String, String> sinkConfigWithProvider(String providerClass) {
        Map<String, String> configs = new HashMap<>();
        configs.put("azure.cosmos.account.endpoint", "https://test.documents.azure.com:443/");
        configs.put("azure.cosmos.auth.type", CosmosAuthType.ACCESS_TOKEN_PROVIDER.getName());
        configs.put(PROVIDER_CLASS_CONFIG, providerClass);
        configs.put("azure.cosmos.sink.database.name", "db");
        configs.put("azure.cosmos.sink.containers.topicMap", "topic#container");
        return configs;
    }

    public static class TestTokenProvider implements CosmosAccessTokenProvider {
        private Map<String, ?> configs;
        private volatile boolean closed;

        @Override
        public void configure(Map<String, ?> configs) {
            this.configs = configs;
        }

        @Override
        public CosmosAccessToken getAccessToken(List<String> scopes) {
            return new CosmosAccessToken("token-for-" + String.join(",", scopes), EXPIRES_AT);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    public static class NullTokenProvider implements CosmosAccessTokenProvider {
        @Override
        public void configure(Map<String, ?> configs) {
        }

        @Override
        public CosmosAccessToken getAccessToken(List<String> scopes) {
            return null;
        }
    }

    public static class NotAProvider {
    }
}
