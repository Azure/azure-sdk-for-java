// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import com.azure.data.appconfiguration.implementation.ClientConstants;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import static com.azure.data.appconfiguration.ConfigurationClientTestBase.FAKE_CONNECTION_STRING;
import static com.azure.data.appconfiguration.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.data.appconfiguration.TestHelper.getTokenCredential;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationClientBuilderTest extends TestProxyTestBase {
    private static final String DEFAULT_DOMAIN_NAME = ".azconfig.io";
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private final String key = "newKey";
    private final String value = "newValue";
    private static final String ENDPOINT
        = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    @Test
    @DoNotRecord
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void clientMissingEndpointButTokenCredentialProvided() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));
            builder.credential(credentials).buildClient();
        });
    }

    @Test
    @DoNotRecord
    public void malformedURLExceptionForEndpoint() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.endpoint("htp://" + NAMESPACE_NAME + DEFAULT_DOMAIN_NAME).buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void nullConnectionString() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString(null).buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void emptyConnectionString() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("").buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void nullCredentials() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.buildClient();
        });
    }

    @Test
    @DoNotRecord
    public void multipleCredentialsExist() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));
            builder.connectionString(FAKE_CONNECTION_STRING).credential(credentials).buildClient();
        });
    }

    @Test
    @DoNotRecord
    public void missingSecretKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("endpoint=" + ENDPOINT + ";Id=aaa;Secret=").buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void missingId() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("endpoint=" + ENDPOINT + ";Id=;Secret=aaa").buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void invalidConnectionStringSegmentCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("endpoint=" + ENDPOINT + ";Id=aaa").buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void nullAADCredential() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.endpoint(ENDPOINT).credential(null).buildAsyncClient();
        });
    }

    @Test
    @DoNotRecord
    public void timeoutPolicy() {
        final ConfigurationClient client = new ConfigurationClientBuilder().connectionString(FAKE_CONNECTION_STRING)
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(new TimeoutPolicy(Duration.ofMillis(1)))
            .buildClient();

        assertThrows(RuntimeException.class, () -> client.setConfigurationSetting(key, null, value));
    }

    @Test
    @DoNotRecord
    public void throwIfBothRetryOptionsAndRetryPolicyIsConfigured() {
        final ConfigurationClientBuilder clientBuilder
            = new ConfigurationClientBuilder().connectionString(FAKE_CONNECTION_STRING)
                .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
                .retryPolicy(new RetryPolicy())
                .addPolicy(new TimeoutPolicy(Duration.ofMillis(1)));

        assertThrows(IllegalStateException.class, clientBuilder::buildClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void nullServiceVersion(HttpClient httpClient) {
        TokenCredential tokenCredential = getTokenCredential(interceptorManager);

        String endpoint = interceptorManager.isPlaybackMode()
            ? new ConfigurationClientCredentials(FAKE_CONNECTION_STRING).getBaseUri()
            : Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");

        Objects.requireNonNull(tokenCredential, "tokenCredential expected to be set.");
        Objects.requireNonNull(endpoint, "endpoint expected to be set.");

        final ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder().credential(tokenCredential)
            .endpoint(endpoint)
            .retryPolicy(new RetryPolicy())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(null);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        }
        if (interceptorManager.isRecordMode()) {
            clientBuilder.httpClient(httpClient).addPolicy(interceptorManager.getRecordPolicy());
        }
        // Disable `("$.key")` sanitizer
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK3447");
        }

        ConfigurationSetting addedSetting = clientBuilder.buildClient().setConfigurationSetting(key, null, value);
        assertEquals(addedSetting.getKey(), key);
        assertEquals(addedSetting.getValue(), value);
    }

    @Test
    public void defaultPipeline() {
        TokenCredential tokenCredential = TestHelper.getTokenCredential(interceptorManager);

        String endpoint = interceptorManager.isPlaybackMode()
            ? new ConfigurationClientCredentials(FAKE_CONNECTION_STRING).getBaseUri()
            : Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");

        Objects.requireNonNull(tokenCredential, "tokenCredential expected to be set.");
        Objects.requireNonNull(endpoint, "endpoint expected to be set.");

        final ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder().credential(tokenCredential)
            .endpoint(endpoint)
            .retryPolicy(new RetryPolicy())
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isRecordMode()) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy()).httpClient(HttpClient.createDefault());
        }

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        }

        // Disable `("$.key")` sanitizer
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK3447");
        }

        ConfigurationSetting addedSetting = clientBuilder.buildClient().setConfigurationSetting(key, null, value);

        assertEquals(addedSetting.getKey(), key);
        assertEquals(addedSetting.getValue(), value);
    }

    @Test
    @DoNotRecord
    public void clientOptionsIsPreferredOverLogOptions() {
        ConfigurationClient configurationClient
            = new ConfigurationClientBuilder().connectionString(FAKE_CONNECTION_STRING)
                .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
                .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
                .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
                .httpClient(httpRequest -> {
                    assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                    return Mono.just(new MockHttpResponse(httpRequest, 400));
                })
                .buildClient();
        assertThrows(HttpResponseException.class, () -> configurationClient.setConfigurationSetting(key, null, value));
    }

    @Test
    @DoNotRecord
    public void clientOptionHeadersAreAddedLast() {
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(FAKE_CONNECTION_STRING)
            .clientOptions(new ClientOptions().setHeaders(Collections.singleton(new Header("User-Agent", "custom"))))
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.just(new MockHttpResponse(httpRequest, 400));
            })
            .buildClient();
        assertThrows(HttpResponseException.class, () -> configurationClient.setConfigurationSetting(key, null, value));
    }

    @Test
    @DoNotRecord
    public void getEndpointAtClientInstance() {
        ConfigurationClientBuilder configurationClientBuilder
            = new ConfigurationClientBuilder().connectionString(FAKE_CONNECTION_STRING);
        final ConfigurationClient client = configurationClientBuilder.buildClient();
        final ConfigurationAsyncClient asyncClient = configurationClientBuilder.buildAsyncClient();
        assertEquals("https://localhost:8080", client.getEndpoint());
        assertEquals("https://localhost:8080", asyncClient.getEndpoint());
    }

    private static URI getURI(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US, "Invalid namespace name: %s", namespace),
                exception);
        }
    }
}
