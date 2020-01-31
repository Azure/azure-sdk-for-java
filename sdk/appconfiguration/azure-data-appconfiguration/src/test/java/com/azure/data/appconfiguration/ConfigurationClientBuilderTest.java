// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.implementation.ClientConstants;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigurationClientBuilderTest {
    private static final String DEFAULT_DOMAIN_NAME = ".azconfig.io";
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

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

    private static URI getURI(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }
}
