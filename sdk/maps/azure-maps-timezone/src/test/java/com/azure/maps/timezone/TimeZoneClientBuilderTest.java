// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import org.junit.jupiter.api.Test;

public class TimeZoneClientBuilderTest {
    // Test for null timezoneClientId, the client ID value
    @Test
    public void missingtimezoneClientId() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.timezoneClientId(null);
        });
    }

    // Test for missing endpoint
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.endpoint(null);
        });
    }

    // Test for missing configuration
    @Test
    public void missingConfiguration() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.configuration(null);
        });
    }

    // Test for missing http log options
    @Test
    public void missingHttpLogOptions() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.httpLogOptions(null);
        });
    }

    // Test for missing retry policy
    @Test
    public void missingRetryPolicy() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.retryPolicy(null);
        });
    }

    // Test for missing client options
    @Test
    public void missingClientOptions() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.clientOptions(null);
        });
    }

    // Test for missing add policy
    @Test
    public void missingAddPolicy() {
        assertThrows(NullPointerException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.addPolicy(null);
        });
    }

    // Test for null timezone id, valid token credential
    @Test
    public void missingTimeZoneClientIdValidTokenCredential() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
            builder.credential(tokenCredential);
            builder.buildClient();
        });
    }

    // Test for null key credential and null token credential despite valid timezoneClientId
    @Test
    public void missingCredentials() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TimeZoneClientBuilder builder = new TimeZoneClientBuilder();
            builder.timezoneClientId("timezoneClientId");
            builder.buildClient();
        });
    }
}
