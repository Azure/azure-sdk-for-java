// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapsSearchClientBuilderTest {

    // Test for null mapsClientId, the client ID value
    @Test
    public void missingMapsClientId() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().mapsClientId(null));
    }

    // Test for missing endpoint
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().endpoint(null));
    }

    // Test for missing configuration
    @Test
    public void missingConfiguration() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().configuration(null));
    }

    // Test for missing http log options
    @Test
    public void missingHttpLogOptions() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().httpLogOptions(null));
    }

    // Test for missing retry policy
    @Test
    public void missingRetryPolicy() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().retryPolicy(null));
    }

    // Test for missing client options
    @Test
    public void missingClientOptions() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().clientOptions(null));
    }

    // Test for missing add policy
    @Test
    public void missingAddPolicy() {
        assertThrows(NullPointerException.class, () -> new MapsSearchClientBuilder().addPolicy(null));
    }

    // Test for null map id, valid token credential
    @Test
    public void missingMapsClientIdValidTokenCredential() {
        assertThrows(IllegalArgumentException.class,
            () -> new MapsSearchClientBuilder().credential(new MockTokenCredential()).buildClient());
    }

    // Test for null key credential and null token credential despite valid mapsClientId
    @Test
    public void missingCredentials() {
        assertThrows(IllegalArgumentException.class,
            () -> new MapsSearchClientBuilder().mapsClientId("mapsClientId").buildClient());
    }
}
