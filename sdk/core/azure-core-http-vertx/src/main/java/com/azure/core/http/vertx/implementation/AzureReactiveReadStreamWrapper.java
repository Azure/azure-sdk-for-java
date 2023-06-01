// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.MonoSink;

import java.io.InputStream;

/**
 * Wraps a Vertx {@link ReactiveReadStream} to handle propagating errors to both the handler of the read stream and the
 * reactive {@link MonoSink}. The {@link MonoSink} will never be completed successfully in this wrapper as that is
 * handled within {@code VertxAsyncHttpClient}.
 * <p>
 * For now this handles both {@link InputStream} and {@link Flux} {@link HttpRequest} bodies. In the future
 * {@link InputStream} should have its own {@link ReadStream} implementation instead of rely on mechanisms within
 * {@code azure-core} to convert the stream to a Flux as this adds unnecessary copies of the data represented by the
 * stream.
 */
public final class AzureReactiveReadStreamWrapper implements ReactiveReadStream<Buffer> {
    private final ReactiveReadStream<Buffer> wrapped;
    private final MonoSink<HttpResponse> sink;

    public AzureReactiveReadStreamWrapper(ReactiveReadStream<Buffer> wrapped, MonoSink<HttpResponse> sink) {
        this.wrapped = wrapped;
        this.sink = sink;
    }

    @Override
    public synchronized ReactiveReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        return wrapped.exceptionHandler(handler);
    }

    @Override
    public synchronized ReactiveReadStream<Buffer> handler(Handler<Buffer> handler) {
        return wrapped.handler(handler);
    }

    @Override
    public synchronized ReactiveReadStream<Buffer> pause() {
        return wrapped.pause();
    }

    @Override
    public synchronized ReactiveReadStream<Buffer> resume() {
        return wrapped.resume();
    }

    @Override
    public synchronized ReadStream<Buffer> fetch(long amount) {
        return wrapped.fetch(amount);
    }

    @Override
    public synchronized ReactiveReadStream<Buffer> endHandler(Handler<Void> endHandler) {
        return wrapped.endHandler(endHandler);
    }

    @Override
    public synchronized void onSubscribe(Subscription s) {
        wrapped.onSubscribe(s);
    }

    @Override
    public synchronized void onNext(Buffer buffer) {
        wrapped.onNext(buffer);
    }

    @Override
    public synchronized void onError(Throwable t) {
        wrapped.onError(t);
        sink.error(t);
    }

    @Override
    public synchronized void onComplete() {
        wrapped.onComplete();
    }
}
