// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeZoneClientBuilderTest {
    // Test for null timezoneClientId, the client ID value
    @Test
    public void missingtimezoneClientId() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().timezoneClientId(null));
    }

    // Test for missing endpoint
    @Test
    public void missingEndpoint() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().endpoint(null));
    }

    // Test for missing configuration
    @Test
    public void missingConfiguration() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().configuration(null));
    }

    // Test for missing http log options
    @Test
    public void missingHttpLogOptions() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().httpLogOptions(null));
    }

    // Test for missing retry policy
    @Test
    public void missingRetryPolicy() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().retryPolicy(null));
    }

    // Test for missing client options
    @Test
    public void missingClientOptions() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().clientOptions(null));
    }

    // Test for missing add policy
    @Test
    public void missingAddPolicy() {
        assertThrows(NullPointerException.class, () -> new TimeZoneClientBuilder().addPolicy(null));
    }

    // Test for null timezone id, valid token credential
    @Test
    public void missingTimeZoneClientIdValidTokenCredential() {
        assertThrows(IllegalArgumentException.class,
            () -> new TimeZoneClientBuilder().credential(new MockTokenCredential()).buildClient());
    }

    // Test for null key credential and null token credential despite valid timezoneClientId
    @Test
    public void missingCredentials() {
        assertThrows(IllegalArgumentException.class,
            () -> new TimeZoneClientBuilder().timezoneClientId("timezoneClientId").buildClient());
    }
}
