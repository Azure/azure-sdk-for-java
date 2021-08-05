// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;


import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that partition manager correctly resolves addresses for requests and does appropriate number of cache refreshes.
 */
public class ReactorNettyHttpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyHttpClientTest.class);
    private Configs configs;
    private HttpClient reactorNettyHttpClient;

    @BeforeClass(groups = "unit")
    public void before_ReactorNettyHttpClientTest() {
        this.configs = new Configs();
        this.reactorNettyHttpClient = HttpClient.createFixed(new HttpClientConfig(this.configs));
    }

    @AfterClass(groups = "unit")
    public void after_ReactorNettyHttpClientTest() {
        LifeCycleUtils.closeQuietly(reactorNettyHttpClient);
    }

    @Test(groups = "unit")
    public void httpClientWithMaxHeaderSize() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().maxHeaderSize()).isEqualTo(this.configs.getMaxHttpHeaderSize());
    }

    @Test(groups = "unit")
    public void httpClientWithMaxChunkSize() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().maxChunkSize()).isEqualTo(this.configs.getMaxHttpChunkSize());
    }

    @Test(groups = "unit")
    public void httpClientWithMaxInitialLineLength() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().maxInitialLineLength()).isEqualTo(this.configs.getMaxHttpInitialLineLength());
    }

    @Test(groups = "unit")
    public void httpClientWithValidateHeaders() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        assertThat(httpClient.configuration().decoder().validateHeaders()).isTrue();
    }

    @Test(groups = "unit")
    public void httpClientWithOptions() {
        reactor.netty.http.client.HttpClient httpClient =
            ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, this.reactorNettyHttpClient, "httpClient");
        Integer connectionTimeoutInMillis = (Integer) httpClient.configuration().options().get(ChannelOption.CONNECT_TIMEOUT_MILLIS);
        assertThat(connectionTimeoutInMillis).isEqualTo((int) this.configs.getConnectionAcquireTimeout().toMillis());
    }
}
