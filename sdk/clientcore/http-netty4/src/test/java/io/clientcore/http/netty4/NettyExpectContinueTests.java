// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.shared.ExpectContinueTests;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class NettyExpectContinueTests extends ExpectContinueTests {
    private final NettyHttpClient client
        = (NettyHttpClient) new NettyHttpClientBuilder().configuration(Configuration.none()).build();

    @Override
    protected HttpClient getHttpClient() {
        return client;
    }

    @Test
    public void sendsAfterContinueFallback() throws Exception {
        verifyContinueFallback(client);
    }

    @Test
    public void timesOutWhileWaitingForContinue() throws Exception {
        NettyHttpClient timedClient = (NettyHttpClient) new NettyHttpClientBuilder().configuration(Configuration.none())
            .responseTimeout(Duration.ofMillis(200))
            .build();
        try {
            verifyContinueTimeout(timedClient);
        } finally {
            timedClient.close();
        }
    }

    @Test
    public void interruptionClosesWaitingRequest() throws Exception {
        verifyContinueInterruption(client);
    }

    @Test
    public void doesNotReuseConnectionAfterEarlyRejection() throws Exception {
        verifyConnectionAfterEarlyRejection(client);
    }

    @AfterEach
    public void closeClient() {
        client.close();
    }
}
