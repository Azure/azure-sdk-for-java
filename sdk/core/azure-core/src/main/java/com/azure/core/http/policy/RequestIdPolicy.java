// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.http.HttpHeadersHelper;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * The pipeline policy that puts a UUID in the request header. Azure uses the request id as
 * the unique identifier for the request.
 *
 * <p>The default {@link HttpHeader} name can be overwritten as shown below
 * <p><strong>Code sample</strong></p>
 * <!-- src_embed com.azure.core.http.policy.RequestIdPolicy.constructor.overrideRequestIdHeaderName -->
 * <pre>
 * new RequestIdPolicy&#40;&quot;x-ms-my-custom-request-id&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.RequestIdPolicy.constructor.overrideRequestIdHeaderName -->
 */
public class RequestIdPolicy implements HttpPipelinePolicy {

    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";
    private final String requestIdHeaderName;
    private final String requestIdHeaderNameLowerCase;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            HttpHeaders headers = context.getHttpRequest().getHeaders();
            String requestId = HttpHeadersHelper.getValue_noKeyFormatting(headers, requestIdHeaderNameLowerCase);
            if (requestId == null) {
                HttpHeadersHelper.set_noKeyFormatting(headers, requestIdHeaderNameLowerCase, requestIdHeaderName,
                    UUID.randomUUID().toString());
            }
        }
    };

    /**
     * Creates  {@link RequestIdPolicy} with provided {@code requestIdHeaderName}.
     * @param requestIdHeaderName to be used to set in {@link HttpRequest}.
     */
    public RequestIdPolicy(String requestIdHeaderName) {
        this.requestIdHeaderName = Objects.requireNonNull(requestIdHeaderName,
            "requestIdHeaderName can not be null.");
        this.requestIdHeaderNameLowerCase = requestIdHeaderName.toLowerCase(Locale.ROOT);
    }

    /**
     * Creates default {@link RequestIdPolicy} with default header name 'x-ms-client-request-id'.
     */
    public RequestIdPolicy() {
        this.requestIdHeaderName = REQUEST_ID_HEADER;
        this.requestIdHeaderNameLowerCase = REQUEST_ID_HEADER;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
    }
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}

