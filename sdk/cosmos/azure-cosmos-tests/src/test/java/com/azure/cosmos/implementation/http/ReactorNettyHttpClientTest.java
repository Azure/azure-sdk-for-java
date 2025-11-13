// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;


import com.azure.cosmos.CosmosNettyLeakDetectorFactory;
import com.azure.cosmos.TestNGLogListener;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that partition manager correctly resolves addresses for requests and does appropriate number of cache
 * refreshes.
 */
public class ReactorNettyHttpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyHttpClientTest.class);
    private HttpClient reactorNettyHttpClient;
    private volatile AutoCloseable disableNettyLeakDetectionScope;

    @BeforeClass(groups = "unit")
    public void before_ReactorNettyHttpClientTest() {
        this.reactorNettyHttpClient = HttpClient.createFixed(new HttpClientConfig(new Configs()));
        this.disableNettyLeakDetectionScope = CosmosNettyLeakDetectorFactory.createDisableLeakDetectionScope();
    }

    @AfterClass(groups = "unit", alwaysRun = true)
    public void after_ReactorNettyHttpClientTest() throws Exception {

        LifeCycleUtils.closeQuietly(reactorNettyHttpClient);
        if (this.disableNettyLeakDetectionScope != null) {
            this.disableNettyLeakDetectionScope.close();
        }
    }

    @Test(groups = "unit")
    public void httpClientWithMaxHeaderSize() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().maxHeaderSize()).isEqualTo(Configs.getMaxHttpHeaderSize());
    }

    @Test(groups = "unit")
    public void httpClientWithMaxChunkSize() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().maxChunkSize()).isEqualTo(Configs.getMaxHttpChunkSize());
    }

    @Test(groups = "unit")
    public void httpClientWithMaxInitialLineLength() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().maxInitialLineLength()).isEqualTo(Configs.getMaxHttpInitialLineLength());
    }

    @Test(groups = "unit")
    public void httpClientWithValidateHeaders() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().validateHeaders()).isTrue();
    }

    @Test(groups = "unit")
    public void httpClientWithConnectionAcquireTimeout() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        Integer connectionTimeoutInMillis =
            (Integer) httpClient.configuration().options().get(ChannelOption.CONNECT_TIMEOUT_MILLIS);
        assertThat(connectionTimeoutInMillis).isEqualTo((int) Configs.getConnectionAcquireTimeout().toMillis());
    }

    @Test(groups = "unit")
    public void httpClientWithMaxPoolSize() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        int maxConnectionPoolSize = httpClient.configuration().connectionProvider().maxConnections();
        assertThat(maxConnectionPoolSize).isEqualTo(Configs.getDefaultHttpPoolSize());
    }

    @Test(groups = "unit")
    //  We don't set any default response timeout to http client
    public void httpClientWithResponseTimeout() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        Duration responseTimeout = httpClient.configuration().responseTimeout();
        assertThat(responseTimeout).isNull();
    }

    @Test(groups = "unit")
    public void httpClientWithConnectionProviderName() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        String name = httpClient.configuration().connectionProvider().name();
        assertThat(name).isEqualTo(Configs.getReactorNettyConnectionPoolName());
    }
}
