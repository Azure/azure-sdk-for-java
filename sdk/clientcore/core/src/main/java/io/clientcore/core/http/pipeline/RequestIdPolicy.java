// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

import java.util.Objects;

/**
 * The {@link RequestIdPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to add a unique identifier to each {@link HttpRequest} in the form of a UUID in the request header.
 * <p>
 * The request id is set in the request header specified by {@code requestIdHeaderName}. If the request already
 * contains a value for the request id header, it will not be overwritten.
 * <p>
 * This class is useful when you need to track HTTP requests for debugging or auditing purposes. It allows you to
 * specify a custom header name for the request id.
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@link RequestIdPolicy} is created with a custom header name. Once added to the pipeline
 * requests will have their request id set in the 'request-id' header by the {@link RequestIdPolicy}.</p>
 *
 * <!-- src_embed io.clientcore.core.http.pipeline.SetRequestIdPolicy.constructor -->
 * <pre>
 * RequestIdPolicy policy = new RequestIdPolicy&#40;HttpHeaderName.fromString&#40;&quot;my-request-id&quot;&#41;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.pipeline.SetRequestIdPolicy.constructor -->
 *
 * @see io.clientcore.core.http.pipeline
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 * @see HttpHeaders
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class RequestIdPolicy implements HttpPipelinePolicy {
    private final HttpHeaderName requestIdHeaderName;

    /**
     * Creates {@link RequestIdPolicy} with provided {@code requestIdHeaderName}.
     *
     * @param requestIdHeaderName to be used to set in {@link HttpRequest}.
     * @throws NullPointerException if {@code requestIdHeaderName} is null.
     */
    public RequestIdPolicy(HttpHeaderName requestIdHeaderName) {
        this.requestIdHeaderName = Objects.requireNonNull(requestIdHeaderName, "requestIdHeaderName can not be null.");
    }

    private static void setRequestIdHeader(HttpRequest request, HttpHeaderName requestIdHeaderName) {
        HttpHeaders headers = request.getHeaders();
        String requestId = headers.getValue(requestIdHeaderName);
        if (requestId == null) {
            headers.set(requestIdHeaderName, CoreUtils.randomUuid().toString());
        }
    }

    @Override
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        setRequestIdHeader(httpRequest, requestIdHeaderName);
        return next.process();
    }

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.BEFORE_REDIRECT;
    }
}
