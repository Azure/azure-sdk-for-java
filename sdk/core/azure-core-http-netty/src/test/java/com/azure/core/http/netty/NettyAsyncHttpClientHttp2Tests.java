// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.util.Configuration;
import com.azure.core.validation.http.HttpProtocolVersionTests;
import io.netty.handler.ssl.SslContext;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.TrustManagerFactory;
import java.time.Duration;

/**
 * Tests HTTP-version negotiation with Reactor Netty.
 */
public class NettyAsyncHttpClientHttp2Tests extends HttpProtocolVersionTests {
    @Override
    protected HttpClient createHttpClient(HttpProtocolVersion version, boolean applySetting) throws Exception {
        TrustManagerFactory trustManagerFactory = getTrustManagerFactory();
        SslContext sslContext = version == HttpProtocolVersion.HTTP_2
            ? Http2SslContextSpec.forClient()
                .configure(builder -> builder.trustManager(trustManagerFactory))
                .sslContext()
            : Http11SslContextSpec.forClient()
                .configure(builder -> builder.trustManager(trustManagerFactory))
                .sslContext();
        reactor.netty.http.client.HttpClient nativeClient
            = reactor.netty.http.client.HttpClient.create(ConnectionProvider.newConnection())
                .secure(ssl -> ssl.sslContext(sslContext));
        NettyAsyncHttpClientBuilder builder
            = new NettyAsyncHttpClientBuilder(nativeClient).configuration(Configuration.NONE);
        if (applySetting) {
            builder.maximumHttpVersion(version);
        }
        return builder.build();
    }

    @Override
    protected HttpProtocolVersion getDefaultMaximumHttpVersion() {
        return HttpProtocolVersion.HTTP_1_1;
    }

    @Override
    protected void closeHttpClient(HttpClient client) {
        ((NettyAsyncHttpClient) client).nettyClient.configuration()
            .connectionProvider()
            .disposeLater()
            .block(Duration.ofSeconds(30));
    }
}
