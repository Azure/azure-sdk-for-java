// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Tests {@link ApacheHttpAsyncHttpClientBuilder}.
 */
public class ApacheHttpAsyncHttpClientBuilderTests {

    private static WireMockServer server;

    @BeforeAll
    public static void setupWireMock() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());

    }

    @AfterAll
    public static void shutdownWireMock() {

    }
}
