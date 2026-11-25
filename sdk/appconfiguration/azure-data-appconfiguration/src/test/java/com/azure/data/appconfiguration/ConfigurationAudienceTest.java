// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.data.appconfiguration.models.ConfigurationAudience;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationAudienceTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final HttpClient HTTP_CLIENT = request -> Mono.just(new MockHttpResponse(request, 400));

    @ParameterizedTest
    @CsvSource({
        "https://example.appconfig.azure.com, https://appconfig.azure.com",
        "https://example.azconfig.azure.com, https://azconfig.azure.com",
        "https://example.azconfig.io, https://azconfig.io",
        "https://example.appconfig.azure.us, https://appconfig.azure.us",
        "https://example.azconfig.azure.us, https://azconfig.azure.us",
        "https://example.appconfig.azure.cn, https://appconfig.azure.cn",
        "https://example.azconfig.azure.cn, https://azconfig.azure.cn",
        "https://my-store-123.appconfig.azure.com, https://appconfig.azure.com",
        "https://example.appconfig.azure.com/, https://appconfig.azure.com",
        "https://example.appconfig.azure.us/, https://appconfig.azure.us",
        "https://example.appconfig.azure.cn/, https://appconfig.azure.cn",
        "https://example.azconfig.azure.us/, https://azconfig.azure.us",
        "https://example.appconfig-staging.azure.com, https://appconfig-staging.azure.com",
        "https://appconfig.example.appconfig-staging.azure.com, https://appconfig-staging.azure.com",
        "https://example.s1.westus2.appconfig-stage.azure.com, https://appconfig-staging.azure.com",
        "https://example.appconfig.sovereign.cloud, https://appconfig.sovereign.cloud",
        "https://example.azconfig.sovereign.cloud, https://azconfig.sovereign.cloud",
        "https://example.eastus.appconfig.sovereign.cloud, https://appconfig.sovereign.cloud",
        "https://appconfig-store.azconfig.example.appconfig.sovereign.cloud, https://appconfig.sovereign.cloud",
        "https://appconfig.example.azconfig.sovereign.cloud, https://azconfig.sovereign.cloud",
        "https://example.AZconfig.IO/, https://azconfig.io",
        "https://example.APPCONFIG-STAGING.AZURE.COM/, https://appconfig-staging.azure.com",
        "https://example.appconfig.azure.us:443/path?key=value#fragment, https://appconfig.azure.us",
        "https://example.azconfig.azure.cn/path/appconfig.azure.us, https://azconfig.azure.cn",
        "https://example.appconfig-test.azure.com, https://appconfig.azure.com",
        "https://example.fooappconfig.azure.us, https://appconfig.azure.com",
        "https://myazconfig.io, https://appconfig.azure.com",
        "https://example.fooappconfig-staging.azure.com, https://appconfig.azure.com",
        "https://other.custom.audience, https://appconfig.azure.com",
        "https://other.custom.audience/appconfig.azure.us, https://appconfig.azure.com",
        "https://other.custom.audience/?host=example.appconfig.azure.cn, https://appconfig.azure.com",
        "https://example.appconfig.azure.us@other.custom.audience, https://appconfig.azure.com",
        "https://127.0.0.1:443, https://appconfig.azure.com" })
    void infersAudienceFromHostname(String endpoint, String expectedAudience) {
        assertScopesForBothClients(endpoint, null, expectedAudience + "/.default");
    }

    @ParameterizedTest
    @ValueSource(strings = { "http://localhost:8483", "https://[::1]:443" })
    void infersAudienceForEndpointsThatCannotSendTokenRequests(String endpoint) throws ReflectiveOperationException {
        // The core pipeline disallows HTTP token authentication and cannot construct requests for IPv6 endpoints.
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder().endpoint(endpoint)
            .credential(credential(new ArrayList<>()))
            .httpClient(HTTP_CLIENT);
        builder.buildClient();
        builder.buildAsyncClient();
        Method method = ConfigurationClientBuilder.class.getDeclaredMethod("getDefaultScope", String.class);
        method.setAccessible(true);
        assertEquals("https://appconfig.azure.com/.default", method.invoke(builder, endpoint));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://appconfig.azure.com", "https://appconfig.azure.cn", "https://custom.audience" })
    void explicitAudienceOverridesInference(String audience) {
        assertScopesForBothClients("https://example.appconfig.sovereign.cloud",
            ConfigurationAudience.fromString(audience), audience + "/.default");
    }

    @Test
    void emptyAudienceRetainsInference() {
        assertScopesForBothClients("https://example.azconfig.io", ConfigurationAudience.fromString(""),
            "https://azconfig.io/.default");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void reusedBuilderInfersAudienceForEachEndpoint(boolean async) {
        List<String> scopes = new ArrayList<>();
        ConfigurationClientBuilder builder = new ConfigurationClientBuilder().credential(credential(scopes))
            .httpClient(HTTP_CLIENT)
            .endpoint("https://example.appconfig.azure.us");
        sendRequest(builder, async);
        builder.endpoint("https://example.azconfig.sovereign.cloud");
        sendRequest(builder, async);
        assertEquals(Arrays.asList("https://appconfig.azure.us/.default", "https://azconfig.sovereign.cloud/.default"),
            scopes);
    }

    private static void assertScopesForBothClients(String endpoint, ConfigurationAudience audience,
        String expectedScope) {
        for (boolean async : new boolean[] { false, true }) {
            List<String> scopes = new ArrayList<>();
            ConfigurationClientBuilder builder = new ConfigurationClientBuilder().endpoint(endpoint)
                .credential(credential(scopes))
                .audience(audience)
                .httpClient(HTTP_CLIENT);
            sendRequest(builder, async);
            assertEquals(Collections.singletonList(expectedScope), scopes);
        }
    }

    private static TokenCredential credential(List<String> scopes) {
        return request -> {
            scopes.addAll(request.getScopes());
            return Mono.just(new AccessToken("test-token", OffsetDateTime.MAX));
        };
    }

    private static void sendRequest(ConfigurationClientBuilder builder, boolean async) {
        assertThrows(HttpResponseException.class, () -> {
            if (async) {
                builder.buildAsyncClient().getConfigurationSetting("test", null).block(TIMEOUT);
            } else {
                builder.buildClient().getConfigurationSetting("test", null);
            }
        });
    }
}
