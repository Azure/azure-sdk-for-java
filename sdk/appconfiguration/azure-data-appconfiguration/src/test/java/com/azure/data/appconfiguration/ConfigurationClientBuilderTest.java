// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.implementation.ClientConstants;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationClientBuilderTest extends TestBase {
    private static final String AZURE_APPCONFIG_CONNECTION_STRING = "AZURE_APPCONFIG_CONNECTION_STRING";
    private static final String DEFAULT_DOMAIN_NAME = ".azconfig.io";
    private static final String NAMESPACE_NAME = "dummyNamespaceName";

    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();
    static String connectionString;

    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.buildAsyncClient();
        });
    }

    @Test
    public void malformedURLExceptionForEndpoint() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.endpoint("htp://" + NAMESPACE_NAME + DEFAULT_DOMAIN_NAME).buildAsyncClient();
        });
    }

    @Test
    public void nullConnectionString() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString(null).buildAsyncClient();
        });
    }

    @Test
    public void emptyConnectionString() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("").buildAsyncClient();
        });
    }

    @Test
    public void missingSecretKey() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("endpoint=" + ENDPOINT + ";Id=aaa;Secret=").buildAsyncClient();
        });
    }

    @Test
    public void missingId() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("endpoint=" + ENDPOINT + ";Id=;Secret=aaa").buildAsyncClient();
        });
    }

    @Test
    public void invalidConnectionStringSegmentCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.connectionString("endpoint=" + ENDPOINT + ";Id=aaa").buildAsyncClient();
        });
    }

    @Test
    public void nullAADCredential() {
        assertThrows(NullPointerException.class, () -> {
            final ConfigurationClientBuilder builder = new ConfigurationClientBuilder();
            builder.endpoint(ENDPOINT).credential(null).buildAsyncClient();
        });
    }

    @Test
    public void timeoutPolicy() {
        final String key = "newKey";
        final String value = "newValue";

        connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw"
            : Configuration.getGlobalConfiguration().get(AZURE_APPCONFIG_CONNECTION_STRING);

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .addPolicy(new TimeoutPolicy(Duration.ofMillis(1))).buildClient();

        assertThrows(RuntimeException.class, () -> client.setConfigurationSetting(key, null, value));
    }

    @Test
    public void nullServiceVersion() {
        final String key = "newKey";
        final String value = "newValue";
        connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw"
            : Configuration.getGlobalConfiguration().get(AZURE_APPCONFIG_CONNECTION_STRING);

        Objects.requireNonNull(connectionString, "`AZURE_APPCONFIG_CONNECTION_STRING` expected to be set.");

        final ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .retryPolicy(new RetryPolicy())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(null);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        ConfigurationSetting addedSetting = clientBuilder.buildClient().setConfigurationSetting(key, null, value);
        Assertions.assertEquals(addedSetting.getKey(), key);
        Assertions.assertEquals(addedSetting.getValue(), value);
    }

    @Test
    public void defaultPipeline() {
        final String key = "newKey";
        final String value = "newValue";

        connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw"
            : Configuration.getGlobalConfiguration().get(AZURE_APPCONFIG_CONNECTION_STRING);

        Objects.requireNonNull(connectionString, "`AZURE_APPCONFIG_CONNECTION_STRING` expected to be set.");

        final ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .retryPolicy(new RetryPolicy())
            .configuration(Configuration.getGlobalConfiguration())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .pipeline(new HttpPipelineBuilder().build());

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());

            assertThrows(HttpResponseException.class,
                () -> clientBuilder.buildClient().setConfigurationSetting(key, null, value));
        }

        clientBuilder.pipeline(null);

        ConfigurationSetting addedSetting = clientBuilder.buildClient().setConfigurationSetting(key, null, value);
        Assertions.assertEquals(addedSetting.getKey(), key);
        Assertions.assertEquals(addedSetting.getValue(), value);
    }

    private static URI getURI(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }
}
