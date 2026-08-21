// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Pipeline policy that, when an ideal endpoint has been set on the per-call {@link com.azure.core.util.Context}
 * under {@link #LAYOUT_ENDPOINT_KEY}, rewrites the outgoing request's host and port to that endpoint while
 * preserving the original host on the {@code Host} header.
 * <p>
 * This policy is a no-op for any request that does not opt in by setting the {@link #LAYOUT_ENDPOINT_KEY}
 * property on the call context, for example via
 * {@code context.addData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY, endpoint)} before issuing the request.
 */
public final class DataLocalityPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(DataLocalityPolicy.class);

    /**
     * The {@link com.azure.core.util.Context} data key used to opt a request into locality-aware routing.
     * When present and set to a non-null, non-empty absolute endpoint URL string, this policy rewrites the
     * outgoing request's host/port to that endpoint.
     */
    public static final String LAYOUT_ENDPOINT_KEY = "Azure.Storage.LayoutEndpoint";

    /**
     * Creates a new instance of {@link DataLocalityPolicy}.
     */
    public DataLocalityPolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        applyLayoutEndpoint(context);
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        applyLayoutEndpoint(context);
        return next.processSync();
    }

    private static void applyLayoutEndpoint(HttpPipelineCallContext context) {
        Optional<Object> endpointData = context.getData(LAYOUT_ENDPOINT_KEY);
        HttpRequest request = context.getHttpRequest();

        if (!endpointData.isPresent() || !(endpointData.get() instanceof String)) {
            return;
        }

        String endpoint = (String) endpointData.get();
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            return;
        }

        try {
            UrlBuilder requestUrlBuilder = UrlBuilder.parse(request.getUrl().toString());
            UrlBuilder endpointUrlBuilder = UrlBuilder.parse(endpoint);

            String originalAuthority = request.getUrl().getAuthority();
            requestUrlBuilder.setHost(endpointUrlBuilder.getHost());
            requestUrlBuilder.setPort(endpointUrlBuilder.getPort());

            request.setUrl(requestUrlBuilder.toString());
            request.setHeader(HttpHeaderName.HOST, originalAuthority);
        } catch (RuntimeException ex) {
            LOGGER.warning("Invalid data locality endpoint. Skipping request URL rewrite.", ex);
        }
    }

    /**
     * Gets the position to place the policy.
     *
     * @return The position to place the policy.
     */
    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_RETRY;
    }
}
