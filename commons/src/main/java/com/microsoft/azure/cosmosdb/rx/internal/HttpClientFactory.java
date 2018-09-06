/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.rx.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.pipeline.PipelineConfiguratorComposite;
import io.reactivex.netty.pipeline.ssl.SSLEngineFactory;
import io.reactivex.netty.protocol.http.HttpObjectAggregationConfigurator;
import io.reactivex.netty.protocol.http.client.CompositeHttpClientBuilder;
import io.reactivex.netty.protocol.http.client.HttpClientPipelineConfigurator;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

/**
 * Helper class internally used for instantiating rx http client.
 */
public class HttpClientFactory {
    private Configs configs;
    private Integer maxPoolSize;
    private Integer maxIdleConnectionTimeoutInMillis;
    private Integer requestTimeoutInMillis;

    public HttpClientFactory() {
    }

    public HttpClientFactory withConfig(Configs configs) {
        this.configs = configs;
        return this;
    }

    public HttpClientFactory withPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public HttpClientFactory withMaxIdleConnectionTimeoutInMillis(int maxIdleConnectionTimeoutInMillis) {
        this.maxIdleConnectionTimeoutInMillis = maxIdleConnectionTimeoutInMillis;
        return this;
    }

    public HttpClientFactory withRequestTimeoutInMillis(int requestTimeoutInMillis) {
        this.requestTimeoutInMillis = requestTimeoutInMillis;
        return this;
    }

    static class DefaultSSLEngineFactory implements SSLEngineFactory {
        private final SslContext sslContext;

        private DefaultSSLEngineFactory() {
            try {
                SslProvider sslProvider = SslContext.defaultClientProvider();
                sslContext = SslContextBuilder.forClient().sslProvider(sslProvider).build();
            } catch (SSLException e) {
                throw new IllegalStateException("Failed to create default SSL context", e);
            }
        }

        @Override
        public SSLEngine createSSLEngine(ByteBufAllocator allocator) {
            return sslContext.newEngine(allocator);
        }
    }

    public CompositeHttpClientBuilder<ByteBuf, ByteBuf> toHttpClientBuilder() {

        if (configs == null) {
            throw new IllegalArgumentException("configs is null");
        }


        CompositeHttpClientBuilder<ByteBuf, ByteBuf> builder = new CompositeHttpClientBuilder<ByteBuf, ByteBuf>()
                .withSslEngineFactory(new DefaultSSLEngineFactory());
        builder = builder.pipelineConfigurator(createClientPipelineConfigurator(configs));


        builder = builder.enableWireLogging(LogLevel.TRACE);

        if (maxPoolSize != null) {
            builder = builder.withMaxConnections(maxPoolSize);
        }

        if (maxIdleConnectionTimeoutInMillis != null) {
            builder = builder.withIdleConnectionsTimeoutMillis(maxIdleConnectionTimeoutInMillis);
        }

        if (requestTimeoutInMillis != null) {
            RxClient.ClientConfig.Builder clientConfigBuilder = new RxClient.ClientConfig.Builder();
            clientConfigBuilder.readTimeout(requestTimeoutInMillis, TimeUnit.MILLISECONDS);
            return builder.config(clientConfigBuilder.build());
        }

        return builder;
    }

    public static CompositeHttpClientBuilder<ByteBuf, ByteBuf> httpClientBuilder(Configs configs,
                                                                                 int maxPoolSize,
                                                                                 int maxIdleConnectionTimeoutInMillis,
                                                                                 int requestTimeoutInMillis) {
        HttpClientFactory httpBuilder = new HttpClientFactory();

        return httpBuilder.withConfig(configs)
                .withPoolSize(maxPoolSize)
                .withMaxIdleConnectionTimeoutInMillis(maxIdleConnectionTimeoutInMillis)
                .withRequestTimeoutInMillis(requestTimeoutInMillis).toHttpClientBuilder();
    }

    private static PipelineConfigurator createClientPipelineConfigurator(Configs config) {
        PipelineConfigurator clientPipelineConfigurator = new PipelineConfiguratorComposite(
                new HttpClientPipelineConfigurator<ByteBuf, ByteBuf>(
                        config.getMaxHttpInitialLineLength(),
                        config.getMaxHttpHeaderSize(),
                        config.getMaxHttpChunkSize(),
                        true),
                new HttpObjectAggregationConfigurator(config.getMaxHttpBodyLength()));
        return clientPipelineConfigurator;
    }
}
