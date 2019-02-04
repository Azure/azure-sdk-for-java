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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.pipeline.PipelineConfiguratorComposite;
import io.reactivex.netty.pipeline.ssl.SSLEngineFactory;
import io.reactivex.netty.pipeline.ssl.SslCompletionHandler;
import io.reactivex.netty.protocol.http.HttpObjectAggregationConfigurator;
import io.reactivex.netty.protocol.http.client.CompositeHttpClientBuilder;
import io.reactivex.netty.protocol.http.client.HttpClient.HttpClientConfig;
import io.reactivex.netty.protocol.http.client.HttpClientPipelineConfigurator;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.microsoft.azure.cosmosdb.internal.Constants;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Helper class internally used for instantiating rx http client.
 */
public class HttpClientFactory {
    private final static String NETWORK_LOG_CATEGORY = "com.microsoft.azure.cosmosdb.netty-network";

    private final Configs configs;
    private Integer maxPoolSize;
    private Integer maxIdleConnectionTimeoutInMillis;
    private Integer requestTimeoutInMillis;
    private InetSocketAddress proxy;

    public HttpClientFactory(Configs configs) {
        this.configs = configs;
    }

    public HttpClientFactory withPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public HttpClientFactory withHttpProxy(InetSocketAddress proxy) {
        this.proxy = proxy;
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

    class DefaultSSLEngineFactory implements SSLEngineFactory {
        private final SslContext sslContext;

        private DefaultSSLEngineFactory() {
            this.sslContext = configs.getSslContext();
        }

        @Override
        public SSLEngine createSSLEngine(ByteBufAllocator allocator) {
            return sslContext.newEngine(allocator);
        }
    }
    class SslPipelineConfiguratorUsedWithProxy<I, O> implements PipelineConfigurator<I, O> {

        private final SSLEngineFactory sslEngineFactory;

        private SslPipelineConfiguratorUsedWithProxy(SSLEngineFactory sslEngineFactory) {
            this.sslEngineFactory = sslEngineFactory;
        }

        @Override
        public void configureNewPipeline(ChannelPipeline pipeline) {
            final SslHandler sslHandler = new SslHandler(sslEngineFactory.createSSLEngine(pipeline.channel().alloc()));
            if(proxy != null) {
                pipeline.addAfter(Constants.Properties.HTTP_PROXY_HANDLER_NAME,Constants.Properties.SSL_HANDLER_NAME, sslHandler);
            } else {
                pipeline.addFirst(Constants.Properties.SSL_HANDLER_NAME, sslHandler);
            }
            pipeline.addAfter(Constants.Properties.SSL_HANDLER_NAME, Constants.Properties.SSL_COMPLETION_HANDLER_NAME,
                              new SslCompletionHandler(sslHandler.handshakeFuture()));
        }
    }

    public CompositeHttpClientBuilder<ByteBuf, ByteBuf> toHttpClientBuilder() {

        if (configs == null) {
            throw new IllegalArgumentException("configs is null");
        }

        DefaultSSLEngineFactory defaultSSLEngineFactory = new DefaultSSLEngineFactory();
        CompositeHttpClientBuilder<ByteBuf, ByteBuf> builder = new CompositeHttpClientBuilder<ByteBuf, ByteBuf>();
        if (maxPoolSize != null) {
            builder = builder.withMaxConnections(maxPoolSize);
        }

        if (maxIdleConnectionTimeoutInMillis != null) {
            builder = builder.withIdleConnectionsTimeoutMillis(maxIdleConnectionTimeoutInMillis);
        }

        builder = builder.pipelineConfigurator(pipeline -> {
            LoggingHandler loggingHandler = getLoggingHandler();

            if (loggingHandler != null) {
                pipeline.addFirst(Constants.Properties.LOGGING_HANDLER_NAME, loggingHandler);
            }

            if(proxy != null) {
                pipeline.addFirst(Constants.Properties.HTTP_PROXY_HANDLER_NAME, new HttpProxyHandler(proxy));
            }
        })
        .appendPipelineConfigurator(new SslPipelineConfiguratorUsedWithProxy<>(defaultSSLEngineFactory))
        .appendPipelineConfigurator(createClientPipelineConfigurator(configs));

        if (requestTimeoutInMillis != null) {
            HttpClientConfig.Builder clientConfigBuilder = new HttpClientConfig.Builder();
            clientConfigBuilder.readTimeout(requestTimeoutInMillis, TimeUnit.MILLISECONDS);
            return builder.config(clientConfigBuilder.build());
        }

        return builder;
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

    private static LoggingHandler getLoggingHandler() {
        if (LoggerFactory.getLogger(NETWORK_LOG_CATEGORY).isTraceEnabled()) {
            return new LoggingHandler(NETWORK_LOG_CATEGORY, LogLevel.TRACE);
        }

        return null;
    }
}
