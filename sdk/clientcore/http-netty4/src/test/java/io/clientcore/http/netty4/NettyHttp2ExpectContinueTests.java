// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.shared.Http2ExpectContinueTests;
import io.clientcore.core.shared.InsecureTrustManager;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;

import java.security.SecureRandom;

public class NettyHttp2ExpectContinueTests extends Http2ExpectContinueTests {
    private final NettyHttpClient client
        = (NettyHttpClient) new NettyHttpClientBuilder().configuration(Configuration.none())
            .maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .sslContextModifier(
                builder -> builder.trustManager(new InsecureTrustManager()).secureRandom(new SecureRandom()))
            .build();

    @Override
    protected HttpClient getHttpClient() {
        return client;
    }

    @AfterEach
    public void closeClient() {
        client.close();
    }
}
