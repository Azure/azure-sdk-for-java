// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.tracing.TracerProxy;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import com.azure.core.models.CloudEvent;
/**
 * This pipeline policy should be added after OpenTelemetryPolicy in the http pipeline.
 *
 * It checks whether the {@link HttpRequest} headers have "traceparent" or "tracestate" and whether the serialized
 * http body json string for a list of {@link CloudEvent} instances has place holders
 * {@link Constants#TRACE_PARENT_PLACEHOLDER} or {@link Constants#TRACE_STATE_PLACEHOLDER}.
 * The place holders will be replaced by the value from headers if the headers have "traceparent" or "tracestate",
 * or be removed if the headers don't have.
 *
 * The place holders won't exist in the json string if the {@link TracerProxy#isTracingEnabled()} returns false.
 */
public final class CloudEventTracingPipelinePolicy implements HttpPipelinePolicy {
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final HttpRequest request = context.getHttpRequest();
        final HttpHeader contentType = request.getHeaders().get(Constants.CONTENT_TYPE);
        StringBuilder bodyStringBuilder = new StringBuilder();
        if (TracerProxy.isTracingEnabled() && contentType != null
            && Constants.CLOUD_EVENT_CONTENT_TYPE.equals(contentType.getValue())) {
            return request.getBody().map(byteBuffer -> bodyStringBuilder.append(new String(byteBuffer.array(),
                StandardCharsets.UTF_8)))
                .then(Mono.fromCallable(() -> replaceTracingPlaceHolder(request, bodyStringBuilder)))
                .then(next.process());
        } else {
            return next.process();
        }
    }

    /**
     *
     * @param request The {@link HttpRequest}, whose body will be mutated by replacing traceparent and tracestate
     *                placeholders.
     * @param bodyStringBuilder The {@link StringBuilder} that contains the full HttpRequest body string.
     * @return The new body string with the place holders replaced (if header has tracing)
     * or removed (if header no tracing).
     */
    static String replaceTracingPlaceHolder(HttpRequest request, StringBuilder bodyStringBuilder) {
        final int traceParentPlaceHolderIndex = bodyStringBuilder.indexOf(Constants.TRACE_PARENT_PLACEHOLDER);
        if (traceParentPlaceHolderIndex >= 0) { // There is "traceparent" placeholder in body, replace it.
            final HttpHeader traceparentHeader = request.getHeaders().get(Constants.TRACE_PARENT);
            bodyStringBuilder.replace(traceParentPlaceHolderIndex,
                Constants.TRACE_PARENT_PLACEHOLDER.length() + traceParentPlaceHolderIndex,
                traceparentHeader != null
                    ? String.format(",\"%s\":\"%s\"", Constants.TRACE_PARENT, traceparentHeader.getValue())
                    : "");
        }
        final int traceStatePlaceHolderIndex = bodyStringBuilder.indexOf(Constants.TRACE_STATE_PLACEHOLDER);
        if (traceStatePlaceHolderIndex >= 0) { // There is "tracestate" placeholder in body, replace it.
            final HttpHeader tracestateHeader = request.getHeaders().get(Constants.TRACE_STATE);
            bodyStringBuilder.replace(traceStatePlaceHolderIndex,
                Constants.TRACE_STATE_PLACEHOLDER.length() + traceStatePlaceHolderIndex,
                tracestateHeader != null
                    ? String.format(",\"%s\":\"%s\"", Constants.TRACE_STATE, tracestateHeader.getValue())
                    : "");
        }
        String newBodyString = bodyStringBuilder.toString();
        request.setHeader(Constants.CONTENT_LENGTH, String.valueOf(newBodyString.length()));
        request.setBody(newBodyString);
        return newBodyString;
    }
}
