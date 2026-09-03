// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.core.util.logging.ClientLogger;
import com.openai.core.http.Headers;
import com.openai.core.http.HttpResponseFor;
import com.openai.core.http.StreamResponse;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseStreamEvent;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Raw OpenAI streaming response that keeps a GenAI span open for the parsed stream lifecycle.
 */
final class TracedRawStreamingResponse implements HttpResponseFor<StreamResponse<ResponseStreamEvent>> {
    private static final ClientLogger LOGGER = new ClientLogger(TracedRawStreamingResponse.class);

    private final HttpResponseFor<StreamResponse<ResponseStreamEvent>> delegate;
    private final GenAiTracingScope scope;
    private final GenAiResponseTracing responseTracing;
    private final boolean invokeAgent;
    private final AtomicBoolean closed = new AtomicBoolean();
    private TracedStreamResponse parsed;

    TracedRawStreamingResponse(HttpResponseFor<StreamResponse<ResponseStreamEvent>> delegate, GenAiTracingScope scope,
        GenAiResponseTracing responseTracing, boolean invokeAgent) {
        this.delegate = delegate;
        this.scope = scope;
        this.responseTracing = responseTracing;
        this.invokeAgent = invokeAgent;
    }

    @Override
    public int statusCode() {
        return delegate.statusCode();
    }

    @Override
    public Headers headers() {
        return delegate.headers();
    }

    @Override
    public InputStream body() {
        return delegate.body();
    }

    @Override
    public synchronized StreamResponse<ResponseStreamEvent> parse() {
        if (parsed == null) {
            try {
                parsed = new TracedStreamResponse(delegate.parse());
            } catch (RuntimeException e) {
                scope.recordError(e);
                close();
                throw LOGGER.logThrowableAsError(e);
            }
        }
        return parsed;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                if (parsed != null) {
                    parsed.close();
                } else {
                    scope.close();
                }
            } finally {
                delegate.close();
            }
        }
    }

    private final class TracedStreamResponse implements StreamResponse<ResponseStreamEvent> {
        private final StreamResponse<ResponseStreamEvent> streamDelegate;
        private final ResponseAccumulator accumulator = ResponseAccumulator.create();
        private final AtomicBoolean streamCreated = new AtomicBoolean();
        private final AtomicBoolean streamClosed = new AtomicBoolean();

        private TracedStreamResponse(StreamResponse<ResponseStreamEvent> streamDelegate) {
            this.streamDelegate = streamDelegate;
        }

        @Override
        public Stream<ResponseStreamEvent> stream() {
            if (!streamCreated.compareAndSet(false, true)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("This raw streaming response has already been consumed."));
            }
            Iterator<ResponseStreamEvent> iterator;
            try {
                iterator = streamDelegate.stream().iterator();
            } catch (RuntimeException e) {
                fail(e);
                throw LOGGER.logThrowableAsError(e);
            }
            Iterator<ResponseStreamEvent> tracedIterator = new Iterator<ResponseStreamEvent>() {
                @Override
                public boolean hasNext() {
                    try {
                        boolean hasNext = iterator.hasNext();
                        if (!hasNext) {
                            close();
                        }
                        return hasNext;
                    } catch (RuntimeException e) {
                        fail(e);
                        throw LOGGER.logThrowableAsError(e);
                    }
                }

                @Override
                public ResponseStreamEvent next() {
                    try {
                        ResponseStreamEvent event = iterator.next();
                        accumulator.accumulate(event);
                        return event;
                    } catch (RuntimeException e) {
                        fail(e);
                        throw LOGGER.logThrowableAsError(e);
                    }
                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(tracedIterator, Spliterator.ORDERED), false)
                .onClose(this::close);
        }

        @Override
        public void close() {
            if (streamClosed.compareAndSet(false, true)) {
                try {
                    Response response = completedResponse();
                    if (response != null) {
                        responseTracing.recordResponseAttributes(scope, response, invokeAgent);
                    }
                    scope.close();
                } finally {
                    streamDelegate.close();
                }
            }
        }

        private void fail(RuntimeException error) {
            scope.recordError(error);
            close();
        }

        private Response completedResponse() {
            try {
                return accumulator.response();
            } catch (IllegalStateException ignored) {
                return null;
            }
        }
    }
}
