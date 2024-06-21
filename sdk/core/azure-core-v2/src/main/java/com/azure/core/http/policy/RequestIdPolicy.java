// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The {@code RequestIdPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to add a unique identifier to each {@link HttpRequest} in the form of a UUID in the request header. Azure
 * uses the request id as the unique identifier for the request.
 *
 * <p>This class is useful when you need to track HTTP requests for debugging or auditing purposes. It allows you to
 * specify a custom header name for the request id, or use the default header name 'x-ms-client-request-id'.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code RequestIdPolicy} is created with a custom header name. Once added to the pipeline
 * requests will have their request id set in the 'x-ms-my-custom-request-id' header by the {@code RequestIdPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.RequestIdPolicy.constructor -->
 * <pre>
 * &#47;&#47; Using the default header name
 * RequestIdPolicy defaultPolicy = new RequestIdPolicy&#40;&#41;;
 * &#47;&#47; Using a custom header name
 * RequestIdPolicy customRequestIdPolicy = new RequestIdPolicy&#40;&quot;x-ms-my-custom-request-id&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.RequestIdPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.HttpHeaders
 */
public class RequestIdPolicy implements HttpPipelinePolicy {

    private static final HttpHeaderName REQUEST_ID_HEADER = HttpHeaderName.fromString("x-ms-client-request-id");
    private final HttpHeaderName requestIdHeaderName;

    /**
     * Creates  {@link RequestIdPolicy} with provided {@code requestIdHeaderName}.
     * @param requestIdHeaderName to be used to set in {@link HttpRequest}.
     */
    public RequestIdPolicy(String requestIdHeaderName) {
        this.requestIdHeaderName = HttpHeaderName
            .fromString(Objects.requireNonNull(requestIdHeaderName, "requestIdHeaderName can not be null."));
    }

    /**
     * Creates default {@link RequestIdPolicy} with default header name 'x-ms-client-request-id'.
     */
    public RequestIdPolicy() {
        this.requestIdHeaderName = REQUEST_ID_HEADER;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        setRequestIdHeader(context.getHttpRequest(), requestIdHeaderName);
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        setRequestIdHeader(context.getHttpRequest(), requestIdHeaderName);
        return next.processSync();
    }

    private static void setRequestIdHeader(HttpRequest request, HttpHeaderName requestIdHeaderName) {
        HttpHeaders headers = request.getHeaders();
        String requestId = headers.getValue(requestIdHeaderName);
        if (requestId == null) {
            headers.set(requestIdHeaderName, CoreUtils.randomUuid().toString());
        }
    }
}
