// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class TrafficClientBuilderTest {
    // Test for null RenderClientId, the client ID value
    @Test
    public void missingMapsClientId() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.trafficClientId(null);
        });
    }

    // Test for missing endpoint
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.endpoint(null);
        });
    }

    // Test for missing configuration
    @Test
    public void missingConfiguration() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.configuration(null);
        });
    }

    // Test for missing http log options
    @Test
    public void missingHttpLogOptions() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.httpLogOptions(null);
        });
    }

    // Test for missing retry policy
    @Test
    public void missingRetryPolicy() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.retryPolicy(null);
        });
    }

    // Test for missing client options
    @Test
    public void missingClientOptions() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.clientOptions(null);
        });
    }

    // Test for missing add policy
    @Test
    public void missingAddPolicy() {
        assertThrows(NullPointerException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.addPolicy(null);
        });
    }

    // Test for null map id, valid token credential
    @Test
    public void missingMapsClientIdValidTokenCredential() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
            builder.credential(tokenCredential);
            builder.buildClient();
        });
    }

    // Test for null key credential and null token credential despite valid mapsClientId
    @Test
    public void missingCredentials() {
        assertThrows(IllegalArgumentException.class, () -> {
            final TrafficClientBuilder builder = new TrafficClientBuilder();
            builder.trafficClientId("mapsClientId");
            builder.buildClient();
        });
    }
}
