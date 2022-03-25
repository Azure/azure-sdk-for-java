// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.implementation.simple.SimpleChannelPoolMap;
import com.azure.core.http.netty.implementation.simple.SimpleRequestContext;
import com.azure.core.http.netty.implementation.simple.SimpleRequestSender;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * TODO (kasobol-msft) add docs.
 */
public class SimpleNettyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleNettyHttpClient.class);

    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";

    private final ChannelPoolMap<URI, ChannelPool> channelPoolMap;

    /**
     * TODO (kasobol-msft) add docs.
     */
    public SimpleNettyHttpClient() {
        // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(r -> {
            Thread t = new Thread(r);
            // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
            t.setDaemon(true);
            return t;
        });

        channelPoolMap = new SimpleChannelPoolMap(eventLoopGroup);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        Mono<HttpResponse> responseMono = Mono.fromFuture(() -> sendInternal(request, context, eagerlyReadResponse));
        if (!eagerlyReadResponse) {
            // TODO (kasobol-msft) maybe replace with dedicated reactor friendly collector later.
            // Otherwise there's deadlock in channel handler.
            responseMono = responseMono.publishOn(Schedulers.boundedElastic());
        }
        return responseMono;
    }

    @Override
    public HttpResponse sendSynchronously(HttpRequest request, Context context) {
        try {
            boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
            return sendInternal(request, context, eagerlyReadResponse).get();
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private CompletableFuture<HttpResponse> sendInternal(
        HttpRequest request, Context context, boolean eagerlyReadResponse) {
        URL url = request.getUrl();

        // Configure the client.
        CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();
        // Make the connection attempt.
        URI channelPoolKey;
        try {
            channelPoolKey = new URI(url.getProtocol(), null, url.getHost(),
                url.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
        ChannelPool channelPool = channelPoolMap.get(channelPoolKey);

        SimpleRequestContext requestContext = new SimpleRequestContext(
            channelPool, request, responseFuture, eagerlyReadResponse);
        channelPool.acquire()
            .addListener(new SimpleRequestSender(requestContext));

        return responseFuture;
    }
}
