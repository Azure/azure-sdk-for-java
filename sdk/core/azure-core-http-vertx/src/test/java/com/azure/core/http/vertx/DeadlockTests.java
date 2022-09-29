// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.security.SecureRandom;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

public class DeadlockTests {
    private static final String GET_ENDPOINT = "/get";

    private WireMockServer server;
    private byte[] expectedGetBytes;

    @BeforeEach
    public void configureWireMockServer() {
        expectedGetBytes = new byte[10 * 1024 * 1024];
        new SecureRandom().nextBytes(expectedGetBytes);

        server = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        server.stubFor(get(GET_ENDPOINT).willReturn(aResponse().withBody(expectedGetBytes)));

        server.start();
    }

    @AfterEach
    public void shutdownWireMockServer() {
        if (server != null) {
            server.shutdown();
        }
    }
}
