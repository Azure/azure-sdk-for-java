// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.HttpHeader;
import com.generic.core.http.HttpHeaders;
import com.generic.core.http.HttpPipelineCallContext;
import com.generic.core.http.HttpPipelineNextSyncPolicy;
import com.generic.core.http.HttpRequest;
import com.generic.core.http.HttpResponse;
import com.generic.core.util.CoreUtils;

import java.util.Objects;

/**
 * The pipeline policy that puts a UUID in the request header. Azure uses the request id as
 * the unique identifier for the request.
 *
 * <p>The default {@link HttpHeader} name can be overwritten as shown below
 * <p><strong>Code sample</strong></p>
 * <!-- src_embed com.azure.core.http.policy.RequestIdPolicy.constructor.overrideRequestIdHeaderName -->
 * <!-- end com.azure.core.http.policy.RequestIdPolicy.constructor.overrideRequestIdHeaderName -->
 */
public class RequestIdPolicy implements HttpPipelinePolicy {

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final String requestIdHeaderName;

    /**
     * Creates  {@link RequestIdPolicy} with provided {@code requestIdHeaderName}.
     * @param requestIdHeaderName to be used to set in {@link HttpRequest}.
     */
    public RequestIdPolicy(String requestIdHeaderName) {
        this.requestIdHeaderName = Objects.requireNonNull(requestIdHeaderName,
            "requestIdHeaderName can not be null.");
    }

    /**
     * Creates default {@link RequestIdPolicy} with default header name 'x-ms-client-request-id'.
     */
    public RequestIdPolicy() {
        this.requestIdHeaderName = REQUEST_ID_HEADER;
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        setRequestIdHeader(context.getHttpRequest(), requestIdHeaderName);
        return next.processSync();
    }

    private static void setRequestIdHeader(HttpRequest request, String requestIdHeaderName) {
//        HttpHeaders headers = request.getHeaders();
//        String requestId = headers.getValue(requestIdHeaderName);
//        if (requestId == null) {
//            headers.set(requestIdHeaderName, CoreUtils.randomUuid().toString());
//        }
    }
}

