// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ServiceBusServiceVersion}.
 */
public class ServiceBusServiceVersionTest {
    /**
     * The default admin api-version must be the newest (2024-05); a regression to an older default
     * silently stops the topic filter counts from being served.
     */
    @Test
    void getLatestReturnsNewestVersion() {
        assertEquals(ServiceBusServiceVersion.V2024_05, ServiceBusServiceVersion.getLatest());
    }

    /**
     * The enum -&gt; api-version string is what the client sends on every request; a wrong value
     * silently targets the wrong service API (2024-05 is what serves the topic filter counts).
     */
    @Test
    void versionMapsToApiVersionString() {
        assertEquals("2017-04", ServiceBusServiceVersion.V2017_04.getVersion());
        assertEquals("2021-05", ServiceBusServiceVersion.V2021_05.getVersion());
        assertEquals("2024-05", ServiceBusServiceVersion.V2024_05.getVersion());
    }
}
