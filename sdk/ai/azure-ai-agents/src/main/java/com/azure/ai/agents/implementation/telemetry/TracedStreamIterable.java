// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.core.util.logging.ClientLogger;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseStreamEvent;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A wrapper around a streaming response iterable that records tracing attributes and metrics as the stream is
 * consumed.
 *
 * <p>The span remains open until the stream is fully consumed or an error occurs. Token counts and response
 * metadata are captured from the accumulated stream using the OpenAI SDK's {@link ResponseAccumulator}.</p>
 */
public final class TracedStreamIterable implements Iterable<ResponseStreamEvent>, AutoCloseable {

    private static final ClientLogger LOGGER = new ClientLogger(TracedStreamIterable.class);

    private final Iterable<ResponseStreamEvent> inner;
    private final GenAiTracingScope scope;
    private final GenAiResponseTracing responseTracing;
    private final boolean isInvokeAgent;
    private volatile boolean consumed;

    TracedStreamIterable(Iterable<ResponseStreamEvent> inner, GenAiTracingScope scope,
        GenAiResponseTracing responseTracing, boolean isInvokeAgent) {
        this.inner = inner;
        this.scope = scope;
        this.responseTracing = responseTracing;
        this.isInvokeAgent = isInvokeAgent;
    }

    @Override
    public Iterator<ResponseStreamEvent> iterator() {
        return new TracedIterator(inner.iterator());
    }

    @Override
    public void close() {
        if (!consumed && scope != null) {
            consumed = true;
            scope.close();
        }
    }

    private final class TracedIterator implements Iterator<ResponseStreamEvent> {
        private final Iterator<ResponseStreamEvent> innerIterator;
        private final ResponseAccumulator accumulator;

        TracedIterator(Iterator<ResponseStreamEvent> innerIterator) {
            this.innerIterator = innerIterator;
            this.accumulator = ResponseAccumulator.create();
        }

        @Override
        public boolean hasNext() {
            try {
                boolean hasNext = innerIterator.hasNext();
                if (!hasNext) {
                    finalizeStream();
                }
                return hasNext;
            } catch (RuntimeException ex) {
                recordErrorAndClose(ex);
                throw LOGGER.logExceptionAsError(ex);
            }
        }

        @Override
        public ResponseStreamEvent next() {
            try {
                ResponseStreamEvent event = innerIterator.next();
                accumulator.accumulate(event);
                return event;
            } catch (NoSuchElementException ex) {
                finalizeStream();
                throw LOGGER.logExceptionAsError(ex);
            } catch (RuntimeException ex) {
                recordErrorAndClose(ex);
                throw LOGGER.logExceptionAsError(ex);
            }
        }

        private void recordErrorAndClose(Throwable ex) {
            if (scope != null && !consumed) {
                scope.recordError(ex);
                scope.close();
                consumed = true;
            }
        }

        private void finalizeStream() {
            if (scope == null || consumed) {
                return;
            }
            consumed = true;

            Response response = accumulator.response();
            if (response != null) {
                responseTracing.recordResponseAttributes(scope, response, isInvokeAgent);
            }

            scope.close();
        }
    }
}
