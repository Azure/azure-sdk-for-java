// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdEndpointTests {

    @Test(groups = { "unit" })
    public void configTest() {
        SslContext sslContextMock = Mockito.mock(SslContext.class);

        Duration connectionTimeoutDuration = Duration.ofSeconds(1);
        ConnectionPolicy connectionPolicy = ConnectionPolicy.getDefaultPolicy();
        connectionPolicy.setConnectTimeout(connectionTimeoutDuration);

        RntbdEndpoint.Config config = new RntbdEndpoint.Config(
            new RntbdTransportClient.Options.Builder(connectionPolicy).build(),
            sslContextMock,
            LogLevel.INFO);

        assertThat(config.connectionAcquisitionTimeoutInNanos()).isEqualTo(connectionTimeoutDuration.toNanos());

        Duration overrideDuration = Duration.ofSeconds(2);
        System.setProperty("COSMOS.TCP_CONNECTION_ACQUISITION_TIMEOUT_IN_MS", String.valueOf(overrideDuration.toMillis()));
        config = new RntbdEndpoint.Config(
            new RntbdTransportClient.Options.Builder(connectionPolicy).build(),
            sslContextMock,
            LogLevel.INFO);
        assertThat(config.connectionAcquisitionTimeoutInNanos()).isEqualTo(overrideDuration.toNanos());
    }
}
