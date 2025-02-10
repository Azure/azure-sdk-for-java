// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.SharedExecutorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.validation.http.HttpValidatonUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests the functionality of {@link VertxRequestWriteSubscriber}.
 */
public class VertxRequestWriteSubscriberTests {
    private static final byte[] DATA = "Hello, world!".getBytes(StandardCharsets.UTF_8);

    /**
     * Tests that request writing operates as expected when the reactive stream has onNext followed by onError.
     * <p>
     * The result of this operation order should be that the request is failed.
     */
    @Test
    public void onNextThenOnErrorInSuccession() throws InterruptedException {
        RuntimeException reactiveError = new RuntimeException("Reactive error");
        AtomicReference<Throwable> onErrorDropped = new AtomicReference<>();
        Consumer<Throwable> onErrorDroppedConsumer = onErrorDropped::set;

        Flux<ByteBuffer> data = Flux.generate(sink -> {
            sink.next(ByteBuffer.wrap(DATA));
            sink.error(reactiveError);
        });

        Promise<HttpResponse> promise = Promise.promise();
        Buffer writtenContent = Buffer.buffer();
        HttpClientRequest httpClientRequest = new MockHttpClientRequest() {
            @Override
            public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
                writtenContent.appendBuffer(data);
                Promise<Void> promise = Promise.promise();
                promise.future().onComplete(handler);
                promise.complete();
            }
        };

        VertxRequestWriteSubscriber subscriber = new VertxRequestWriteSubscriber(httpClientRequest, promise, null,
            Context.of("reactor.onErrorDropped.local", onErrorDroppedConsumer));

        data.subscribe(subscriber);

        waitForCompletion(promise);
        assertTrue(promise.future().failed(), "The request should have failed.");
        assertSame(reactiveError, promise.future().cause());
        assertNull(onErrorDropped.get(), "No reactive error should be dropped.");
        assertArraysEqual(DATA, writtenContent.getBytes());
    }

    /**
     * Tests that request writing operates as expected when the reactive stream has onNext followed by onError.
     * <p>
     * The result of this operation order should be that the request is failed and there is an error dropped. An error
     * should be dropped as the error with the write failure should occur first.
     */
    @Test
    public void onNextWithWriteFailureThenOnError() throws InterruptedException {
        RuntimeException writeError = new RuntimeException("Write error");
        RuntimeException reactiveError = new RuntimeException("Reactive error");
        AtomicReference<Throwable> onErrorDropped = new AtomicReference<>();
        Consumer<Throwable> onErrorDroppedConsumer = onErrorDropped::set;

        Flux<ByteBuffer> data = Flux.generate(sink -> {
            sink.next(ByteBuffer.wrap(DATA));
            sink.error(reactiveError);
        });

        Promise<HttpResponse> promise = Promise.promise();
        HttpClientRequest httpClientRequest = new MockHttpClientRequest() {
            @Override
            public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
                Promise<Void> promise = Promise.promise();
                promise.future().onComplete(handler);
                promise.fail(writeError);
            }
        };

        VertxRequestWriteSubscriber subscriber = new VertxRequestWriteSubscriber(httpClientRequest, promise, null,
            Context.of("reactor.onErrorDropped.local", onErrorDroppedConsumer));

        data.subscribe(subscriber);

        waitForCompletion(promise);
        assertTrue(promise.future().failed(), "The request should have failed.");
        assertSame(writeError, promise.future().cause(), "The write error should be the cause of the failure.");
        assertSame(reactiveError, onErrorDropped.get(), "The reactive error should be dropped.");
    }

    /**
     * Tests that request writing operates as expected when the reactive stream has onNext followed by onComplete.
     * <p>
     * The result of this operation order should be that the request is successful. The onNext signal should be
     * processed and content written then the request should be completed due to the onComplete signal.
     */
    @Test
    public void onNextThenOnCompleteInSuccession() throws InterruptedException {
        Promise<HttpResponse> promise = Promise.promise();
        Flux<ByteBuffer> data = Flux.generate(sink -> {
            sink.next(ByteBuffer.wrap(DATA));
            sink.complete();
        });
        Buffer writtenContent = Buffer.buffer();
        HttpClientRequest httpClientRequest = new MockHttpClientRequest() {
            @Override
            public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
                writtenContent.appendBuffer(data);
                Promise<Void> promise = Promise.promise();
                promise.future().onComplete(handler);
                promise.complete();
            }

            @Override
            public Future<Void> end() {
                promise.complete();
                return super.end();
            }
        };

        VertxRequestWriteSubscriber subscriber
            = new VertxRequestWriteSubscriber(httpClientRequest, promise, null, Context.empty());

        data.subscribe(subscriber);

        waitForCompletion(promise);
        assertTrue(promise.future().succeeded(), "The request should have succeeded.");
        assertArraysEqual(DATA, writtenContent.getBytes());
    }

    /**
     * Tests that request writing operates as expected when the reactive stream has onNext followed by onComplete.
     * <p>
     * The result of this operation order should be that the request is failed and there are no errors dropped. While
     * the reactive stream completed successfully the writing of the request failed.
     */
    @Test
    public void onNextWithWriteFailureThenOnComplete() throws InterruptedException {
        RuntimeException writeError = new RuntimeException("Write error");
        AtomicReference<Throwable> onErrorDropped = new AtomicReference<>();
        Consumer<Throwable> onErrorDroppedConsumer = onErrorDropped::set;
        Promise<HttpResponse> promise = Promise.promise();
        Flux<ByteBuffer> data = Flux.generate(sink -> {
            sink.next(ByteBuffer.wrap(DATA));
            sink.complete();
        });
        HttpClientRequest httpClientRequest = new MockHttpClientRequest() {
            @Override
            public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
                Promise<Void> promise = Promise.promise();
                promise.future().onComplete(handler);
                promise.fail(writeError);
            }
        };

        VertxRequestWriteSubscriber subscriber = new VertxRequestWriteSubscriber(httpClientRequest, promise, null,
            Context.of("reactor.onErrorDropped.local", onErrorDroppedConsumer));

        data.subscribe(subscriber);

        waitForCompletion(promise);
        assertTrue(promise.future().failed(), "The request should have failed.");
        assertSame(writeError, promise.future().cause(), "The write error should be the cause of the failure.");
        assertNull(onErrorDropped.get(), "No reactive error should be dropped.");
    }

    /**
     * Tests that if the promise being used to manage the request-response state succeeds externally while writing the
     * request it is handled appropriately.
     * <p>
     * The exception(s) that occur during writing should be dropped as the promise doesn't have a failure cause.
     */
    @Test
    public void promiseIsSucceededExternalWhileRequestIsBeingSent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        RuntimeException writeError = new RuntimeException("Write error");
        RuntimeException reactiveError = new RuntimeException("Reactive error");
        AtomicReference<Throwable> onErrorDropped = new AtomicReference<>();
        Consumer<Throwable> onErrorDroppedConsumer = onErrorDropped::set;
        Promise<HttpResponse> promise = Promise.promise();
        Flux<ByteBuffer> data = Flux.generate(sink -> {
            sink.next(ByteBuffer.wrap(DATA));
            sink.error(reactiveError);
        });
        HttpClientRequest httpClientRequest = new MockHttpClientRequest() {
            @Override
            public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
                SharedExecutorService.getInstance().schedule(() -> {
                    Promise<Void> promise = Promise.promise();
                    promise.future().onComplete(handler);
                    promise.fail(writeError);
                    latch.countDown();
                }, 1000, TimeUnit.MILLISECONDS);
            }
        };

        VertxRequestWriteSubscriber subscriber = new VertxRequestWriteSubscriber(httpClientRequest, promise, null,
            Context.of("reactor.onErrorDropped.local", onErrorDroppedConsumer));

        data.subscribe(subscriber);
        promise.complete();

        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Timed out waiting for the request to be written.");
        }

        assertTrue(promise.future().succeeded(), "The request should have succeeded.");
        assertEquals(writeError, onErrorDropped.get(), "The write error should be dropped.");
        Throwable[] writeSuppressed = writeError.getSuppressed();
        assertEquals(1, writeSuppressed.length, "There should be one suppressed exception.");
        assertSame(reactiveError, writeSuppressed[0], "The reactive error should be the suppressed exception.");
    }

    private static void waitForCompletion(Promise<?> promise) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            if (promise.future().isComplete()) {
                return;
            }
        }

        fail("Timed out waiting for test to complete.");
    }
}
