package com.azure.messaging.eventgrid.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.tracing.TracerProxy;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class CloudEventTracingPipelinePolicy implements HttpPipelinePolicy {
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final HttpRequest request = context.getHttpRequest();
        final HttpHeader contentType = request.getHeaders().get(Constants.CONTENT_TYPE);
        if (TracerProxy.isTracingEnabled() && contentType != null &&
            Constants.CLOUD_EVENT_CONTENT_TYPE.equals(contentType.getValue())) {
            return request.getBody().map(byteBuffer ->
                replaceTracingPlaceHolder(request, byteBuffer)).then(next.process());
        }
        else {
            return next.process();
        }
    }

    static String replaceTracingPlaceHolder(HttpRequest request, ByteBuffer byteBuffer) {
        String bodyString = new String(byteBuffer.array(), StandardCharsets.UTF_8);
        final HttpHeader traceparentHeader = request.getHeaders().get(Constants.TRACE_PARENT);
        final HttpHeader tracestateHeader = request.getHeaders().get(Constants.TRACE_STATE);
        bodyString = bodyString.replace(Constants.TRACE_PARENT_REPLACE,
            traceparentHeader != null
                ? String.format(",\"%s\":\"%s\"", Constants.TRACE_PARENT,
                traceparentHeader.getValue()) : "");
        bodyString = bodyString.replace(Constants.TRACE_STATE_REPLACE,
            tracestateHeader != null
                ? String.format(",\"%s\":\"%s\"", Constants.TRACE_STATE, tracestateHeader.getValue()) : "");
        request.setHeader(Constants.CONTENT_LENGTH, String.valueOf(bodyString.length()));
        request.setBody(bodyString);
        return bodyString;
    }
}
