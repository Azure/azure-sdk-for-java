// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseUsage;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A wrapper around a streaming response iterable that records tracing attributes
 * and metrics as the stream is consumed.
 *
 * <p>The span remains open until the stream is fully consumed or an error occurs.
 * Token counts and response metadata are captured from the final stream event
 * using the OpenAI SDK's {@link ResponseAccumulator}.</p>
 */
public final class TracedStreamIterable implements Iterable<ResponseStreamEvent>, AutoCloseable {

    private final Iterable<ResponseStreamEvent> inner;
    private final GenAiTracingScope scope;
    private final boolean isInvokeAgent;
    private volatile boolean consumed;

    TracedStreamIterable(Iterable<ResponseStreamEvent> inner, GenAiTracingScope scope) {
        this(inner, scope, false);
    }

    TracedStreamIterable(Iterable<ResponseStreamEvent> inner, GenAiTracingScope scope, boolean isInvokeAgent) {
        this.inner = inner;
        this.scope = scope;
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

    private class TracedIterator implements Iterator<ResponseStreamEvent> {
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
            } catch (Throwable ex) {
                if (scope != null) {
                    scope.recordError(ex);
                    scope.close();
                    consumed = true;
                }
                throw ex;
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
                throw ex;
            } catch (Throwable ex) {
                if (scope != null) {
                    scope.recordError(ex);
                    scope.close();
                    consumed = true;
                }
                throw ex;
            }
        }

        private void finalizeStream() {
            if (scope != null && !consumed) {
                consumed = true;

                Response response = accumulator.response();
                if (response != null) {
                    String responseId = response.id();
                    String responseModel = GenAiResponseTracing.extractModelString(response.model());
                    Long inputTokens = null;
                    Long outputTokens = null;

                    Optional<ResponseUsage> usageOpt = response.usage();
                    if (usageOpt.isPresent()) {
                        ResponseUsage usage = usageOpt.get();
                        inputTokens = usage.inputTokens();
                        outputTokens = usage.outputTokens();
                    }

                    scope.setResponseAttributes(responseId, responseModel, inputTokens, outputTokens, null);

                    // Set request model only for chat operations (not invoke_agent)
                    if (responseModel != null && !isInvokeAgent) {
                        scope.setRequestModelAttributes(responseModel, null, null);
                    }

                    // Emit workflow action events for streaming responses
                    GenAiResponseTracing.emitWorkflowActionEventsIfPresent(scope, response);

                    // Format output messages from the accumulated response
                    String outputMessages = GenAiResponseTracing.formatOutputFromResponse(response);
                    scope.setOutputMessages(outputMessages);
                }

                scope.close();
            }
        }
    }
}
