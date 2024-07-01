// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapsRenderClientBuilderTest {
    // Test for null RenderClientId, the client ID value
    @Test
    public void missingMapsClientId() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().mapsClientId(null));
    }

    // Test for missing endpoint
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().endpoint(null));
    }

    // Test for missing configuration
    @Test
    public void missingConfiguration() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().configuration(null));
    }

    // Test for missing http log options
    @Test
    public void missingHttpLogOptions() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().httpLogOptions(null));
    }

    // Test for missing retry policy
    @Test
    public void missingRetryPolicy() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().retryPolicy(null));
    }

    // Test for missing client options
    @Test
    public void missingClientOptions() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().clientOptions(null));
    }

    // Test for missing add policy
    @Test
    public void missingAddPolicy() {
        assertThrows(NullPointerException.class, () -> new MapsRenderClientBuilder().addPolicy(null));
    }

    // Test for null map id, valid token credential
    @Test
    public void missingMapsClientIdValidTokenCredential() {
        assertThrows(IllegalArgumentException.class,
            () -> new MapsRenderClientBuilder().credential(new MockTokenCredential()).buildClient());
    }

    // Test for null key credential and null token credential despite valid mapsClientId
    @Test
    public void missingCredentials() {
        assertThrows(IllegalArgumentException.class,
            () -> new MapsRenderClientBuilder().mapsClientId("mapsClientId").buildClient());
    }
}
