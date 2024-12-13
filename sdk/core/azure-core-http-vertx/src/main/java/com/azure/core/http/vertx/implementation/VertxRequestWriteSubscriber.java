// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.Unpooled;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.nio.ByteBuffer;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a {@link HttpClientRequest Vert.x request}.
 */
@SuppressWarnings("ReactiveStreamsSubscriberImplementation")
public final class VertxRequestWriteSubscriber implements Subscriber<ByteBuffer> {
    private static final ClientLogger LOGGER = new ClientLogger(VertxRequestWriteSubscriber.class);

    private final HttpClientRequest request;
    private final Promise<HttpResponse> promise;
    private final ProgressReporter progressReporter;
    private final ContextView contextView;

    // This subscriber is effectively synchronous so there is no need for these fields to be volatile.
    private volatile Subscription subscription;

    private volatile State state = State.UNINITIALIZED;
    private volatile Throwable error;

    /**
     * Creates a new {@link VertxRequestWriteSubscriber} that writes a stream of {@link ByteBuffer ByteBuffers} to a
     * {@link HttpClientRequest Vert.x request}.
     *
     * @param request The {@link HttpClientRequest Vert.x request} to write to.
     * @param promise The {@link MonoSink} to emit the {@link HttpResponse response} to.
     * @param progressReporter The {@link ProgressReporter} to report progress to.
     * @param contextView The {@link ContextView} to use when dropping errors.
     */
    public VertxRequestWriteSubscriber(HttpClientRequest request, Promise<HttpResponse> promise,
        ProgressReporter progressReporter, ContextView contextView) {
        this.request = request.exceptionHandler(this::onError).drainHandler(ignored -> requestNext());
        this.promise = promise;
        this.progressReporter = progressReporter;
        this.contextView = contextView;
    }

    @Override
    public void onSubscribe(Subscription s) {
        // Only set the Subscription if one has not been previously set.
        // Any additional Subscriptions will be cancelled.
        if (Operators.validate(subscription, s)) {
            subscription = s;

            s.request(1);
        }
    }

    @Override
    public void onNext(ByteBuffer bytes) {
        try {
            if (state == State.WRITING) {
                onErrorInternal(new IllegalStateException("Received onNext while processing another write operation."));
            } else {
                state = State.WRITING;
                write(bytes);
            }
        } catch (Exception ex) {
            // If writing has an error, and it isn't caught, there is a possibility for it to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            onErrorInternal(ex);
        }
    }

    @SuppressWarnings("deprecation")
    private void write(ByteBuffer bytes) {
        int remaining = bytes.remaining();
        request.write(Buffer.buffer(Unpooled.wrappedBuffer(bytes)), result -> {
            State state = this.state;
            if (state == State.WRITING) {
                this.state = State.UNINITIALIZED;
            }

            if (result.succeeded()) {
                if (progressReporter != null) {
                    progressReporter.reportProgress(remaining);
                }
                if (state == State.WRITING) {
                    if (!request.writeQueueFull()) {
                        requestNext();
                    }
                } else if (state == State.COMPLETE) {
                    endRequest();
                } else if (state == State.ERROR) {
                    resetRequest(error);
                }
            } else {
                this.state = State.ERROR;
                if (error != null) {
                    // Don't lose any reactive error that may have occurred while writing.
                    result.cause().addSuppressed(error);
                }
                resetRequest(result.cause());
            }
        });
    }

    private void requestNext() {
        if (state == State.UNINITIALIZED) {
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        onErrorInternal(throwable);
    }

    private void onErrorInternal(Throwable throwable) {
        State state = this.state;
        // code 2 and greater are completion states which means the error should be dropped as we already completed.
        if (state.code >= 2) {
            Operators.onErrorDropped(throwable, Context.of(contextView));

            // Also, even though Reactor may have an operator for the dropped error or will be logged by Operators
            // itself, we should log as well as at least this will help associate the error with this class.
            LOGGER.atInfo()
                .log(() -> "VertxRequestWriteSubscriber dropped an exception as it already reached a "
                    + "completion state.", throwable);
        }

        this.state = State.ERROR;
        if (state != State.WRITING) {
            resetRequest(throwable);
        } else {
            if (error != null) {
                // Already saw another error while writing, add this as a suppressed exception.
                error.addSuppressed(throwable);
            } else {
                // First error seen while writing, maintain it for future use.
                error = throwable;
            }
        }
    }

    private void resetRequest(Throwable throwable) {
        subscription.cancel();
        if (!promise.tryFail(throwable)) {
            // Seems the promise has already completed in some form.
            // Attempt to associate this error with the existing failure.
            Throwable cause = promise.future().cause();
            if (cause != null) {
                cause.addSuppressed(throwable);

                // Also, even though the exception was added as a suppressed exception to the failed Promise, we should
                // log as well as at least this will help associate the error with this class.
                LOGGER.atInfo()
                    .log(() -> "VertxRequestWriteSubscriber added an exception as a suppressed exception "
                        + "as the Promise already failed.", throwable);
            } else {
                // Turns out the future was completed as successfully externally, drop the error.
                Operators.onErrorDropped(LOGGER.logThrowableAsError(throwable), Context.of(contextView));

                // Also, even though Reactor may have an operator for the dropped error or will be logged by Operators
                // itself, we should log as well as at least this will help associate the error with this class.
                LOGGER.atInfo()
                    .log(() -> "VertxRequestWriteSubscriber dropped an exception as the Promise already "
                        + "completed successfully.", throwable);
            }
        }
        request.reset(0, throwable);
    }

    @Override
    public void onComplete() {
        State state = this.state;
        // code 2 and greater are completion states which means the erroneous complete should be dropped as we already
        // completed.
        if (state.code >= 2) {
            // Already completed, just return as there is no cleanup processing to do.
            return;
        }

        this.state = State.COMPLETE;
        if (state != State.WRITING) {
            endRequest();
        }
    }

    private void endRequest() {
        request.end().onFailure(promise::fail);
    }

    private enum State {
        UNINITIALIZED(0), WRITING(1), COMPLETE(2), ERROR(3);

        private final int code;

        State(int code) {
            this.code = code;
        }
    }
}
