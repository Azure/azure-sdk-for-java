// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.apache;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactive.ReactiveEntityProducer;
import org.apache.hc.core5.reactive.ReactiveResponseConsumer;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReadmeSamples {

    public static void main(String[] args) throws Exception {
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                                                    .setSoTimeout(Timeout.ofSeconds(5))
                                                    .build();

        final MinimalHttpAsyncClient client = HttpAsyncClients.createMinimal(
            HttpVersionPolicy.NEGOTIATE,
            H2Config.DEFAULT,
            Http1Config.DEFAULT,
            ioReactorConfig);

        client.start();

        final URI requestUri = new URI("http://httpbin.org/post");
        final byte[] bs = "stuff".getBytes(StandardCharsets.UTF_8);


        final BasicRequestProducer requestProducer = new BasicRequestProducer(
            new SimpleHttpRequest("POST", requestUri),
            new ReactiveEntityProducer(Flux.just(ByteBuffer.wrap(bs)), bs.length, ContentType.TEXT_PLAIN, null));

        final ReactiveResponseConsumer consumer = new ReactiveResponseConsumer();
        final Future<Void> requestFuture = client.execute(requestProducer, consumer, null);

        final Message<HttpResponse, Publisher<ByteBuffer>> streamingResponse = consumer.getResponseFuture().get();

        Mono.from(streamingResponse.getBody())
            .subscribe(byteBuffer -> {
                final byte[] string = new byte[byteBuffer.remaining()];
                byteBuffer.get(string);
                System.out.println("====" + new String(string));
            });


        requestFuture.get(1, TimeUnit.MINUTES);

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);

    }
}
