// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.validation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.HttpClientOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Shared tests of HTTP protocol configuration through providers and {@link HttpClient#createDefault(HttpClientOptions)}.
 */
@Execution(ExecutionMode.SAME_THREAD)
public abstract class HttpClientOptionsProviderTests {
    @ParameterizedTest
    @MethodSource("maximumHttpVersionArguments")
    public void optionsSelectMaximumHttpVersion(HttpProtocolVersion version, boolean useSpi) throws Exception {
        HttpClientOptions options
            = new HttpClientOptions().setConfiguration(Configuration.NONE).setMaximumHttpVersion(version);
        HttpClient client = createClient(options, useSpi);
        try {
            assertMaximumHttpVersion(client, version);
        } finally {
            closeHttpClient(client);
        }
    }

    protected static Stream<Arguments> maximumHttpVersionArguments() {
        return Stream.of(Arguments.of(HttpProtocolVersion.HTTP_1_1, false),
            Arguments.of(HttpProtocolVersion.HTTP_1_1, true), Arguments.of(HttpProtocolVersion.HTTP_2, false),
            Arguments.of(HttpProtocolVersion.HTTP_2, true), Arguments.of(null, false), Arguments.of(null, true));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void defaultOptionsPreserveMaximumHttpVersion(boolean useSpi) throws Exception {
        HttpClient client = createClient(new HttpClientOptions().setConfiguration(Configuration.NONE), useSpi);
        try {
            assertMaximumHttpVersion(client, null);
        } finally {
            closeHttpClient(client);
        }
    }

    @ParameterizedTest
    @MethodSource("maximumHttpVersionArguments")
    public void changingMaximumHttpVersionDoesNotChangeExistingClient(HttpProtocolVersion version, boolean useSpi)
        throws Exception {
        HttpClientOptions options
            = new HttpClientOptions().setConfiguration(Configuration.NONE).setMaximumHttpVersion(version);
        HttpClient first = createClient(options, useSpi);
        try {
            HttpProtocolVersion nextVersion
                = version == HttpProtocolVersion.HTTP_2 ? HttpProtocolVersion.HTTP_1_1 : HttpProtocolVersion.HTTP_2;
            options.setMaximumHttpVersion(nextVersion);
            HttpClient updated = createClient(options, useSpi);
            try {
                assertMaximumHttpVersion(updated, nextVersion);
                assertMaximumHttpVersion(first, version);
            } finally {
                closeHttpClient(updated);
            }

            options.setMaximumHttpVersion(null);
            HttpClient cleared = createClient(options, useSpi);
            try {
                assertMaximumHttpVersion(cleared, null);
                assertMaximumHttpVersion(first, version);
            } finally {
                closeHttpClient(cleared);
            }
        } finally {
            closeHttpClient(first);
        }
    }

    @ParameterizedTest
    @EnumSource(HttpProtocolVersion.class)
    public void maximumHttpVersionIsAppliedWhenClientSharingIsEnabled(HttpProtocolVersion version) throws Exception {
        HttpClientProvider provider = createProvider(
            new ConfigurationBuilder().putProperty("AZURE_ENABLE_HTTP_CLIENT_SHARING", "true").build());
        HttpClientOptions options
            = new HttpClientOptions().setConfiguration(Configuration.NONE).setMaximumHttpVersion(version);
        HttpClient first = provider.createInstance(options);
        try {
            HttpClient second = provider.createInstance(options);
            try {
                assertNotSame(first, second);
                assertMaximumHttpVersion(first, version);
                assertMaximumHttpVersion(second, version);
            } finally {
                closeHttpClient(second);
            }
        } finally {
            closeHttpClient(first);
        }
    }

    @Test
    public void nullOptionsPreserveClientSharing() throws Exception {
        HttpClientProvider provider = createProvider(
            new ConfigurationBuilder().putProperty("AZURE_ENABLE_HTTP_CLIENT_SHARING", "true").build());
        HttpClient client = provider.createInstance();
        assertSame(client, provider.createInstance(null));
        assertMaximumHttpVersion(client, null);
    }

    private HttpClient createClient(HttpClientOptions options, boolean useSpi) {
        HttpClientProvider provider = createProvider(Configuration.NONE);
        return useSpi
            ? HttpClient.createDefault(options.setHttpClientProvider(provider.getClass()))
            : provider.createInstance(options);
    }

    protected abstract HttpClientProvider createProvider(Configuration configuration);

    protected abstract void assertMaximumHttpVersion(HttpClient client, HttpProtocolVersion version) throws Exception;

    protected abstract void closeHttpClient(HttpClient client) throws Exception;
}
