// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builder for a policy to do validation of general response behavior.
 */
public class ResponseValidationPolicyBuilder {

    @FunctionalInterface
    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    private final List<TriConsumer<HttpResponse, ClientLogger, Context>> assertions = new ArrayList<>();

    /**
     * Creates a new instance of {@link ResponseValidationPolicyBuilder}.
     */
    public ResponseValidationPolicyBuilder() {
    }

    /**
     * Builds the policy described by this builder.
     *
     * @return The policy.
     */
    public HttpPipelinePolicy build() {
        return new ResponseValidationPolicy(assertions);
    }

    /**
     * Fluently applies an optional validation to this policy where, if the response contains the given header, asserts
     * its value is an echo of the value provided in the request.
     *
     * @param headerName The header to validate.
     * @return This policy.
     * @deprecated Use {@link  #addOptionalEcho(HttpHeaderName)} instead.
     */
    @Deprecated
    public ResponseValidationPolicyBuilder addOptionalEcho(String headerName) {
        assertions.add((httpResponse, logger, context) -> {
            HttpHeaderName httpHeaderName = HttpHeaderName.fromString(headerName);
            String requestHeaderValue = httpResponse.getRequest().getHeaders().getValue(httpHeaderName);
            String responseHeaderValue = httpResponse.getHeaders().getValue(httpHeaderName);
            List<HttpHeaderName> headersToSkip = getHeadersToSkip(context);

            if (responseHeaderValue != null
                && !responseHeaderValue.equals(requestHeaderValue)
                && !headersToSkip.contains(httpHeaderName)) {
                throw logger.logExceptionAsError(new RuntimeException(
                    String.format("Unexpected header value. Expected response to echo `%s: %s`. Got value `%s`.",
                        headerName, requestHeaderValue, responseHeaderValue)));
            }
        });

        return this;
    }

    /**
     * Fluently applies an optional validation to this policy where, if the response contains the given header, asserts
     * its value is an echo of the value provided in the request.
     *
     * @param headerName The header to validate.
     * @return This policy.
     */
    public ResponseValidationPolicyBuilder addOptionalEcho(HttpHeaderName headerName) {
        assertions.add((httpResponse, logger, context) -> {
            String requestHeaderValue = httpResponse.getRequest().getHeaders().getValue(headerName);
            String responseHeaderValue = httpResponse.getHeaders().getValue(headerName);
            List<HttpHeaderName> headersToSkip = getHeadersToSkip(context);

            if (responseHeaderValue != null
                && !responseHeaderValue.equals(requestHeaderValue)
                && !headersToSkip.contains(headerName)) {
                throw logger.logExceptionAsError(new RuntimeException(
                    String.format("Unexpected header value. Expected response to echo `%s: %s`. Got value `%s`.",
                        headerName, requestHeaderValue, responseHeaderValue)));
            }
        });

        return this;
    }

    @SuppressWarnings("unchecked")
    private List<HttpHeaderName> getHeadersToSkip(Context context) {
        List<HttpHeaderName> headersToSkip = new ArrayList<>();
        Optional<Object> contextAdjustment = context.getData(Constants.SKIP_ECHO_VALIDATION_KEY);
        if (contextAdjustment.isPresent()) {
            headersToSkip = (List<HttpHeaderName>) contextAdjustment.get();
        }
        return headersToSkip;
    }

    /**
     * Immutable policy for asserting validations on general responses.
     */
    public static class ResponseValidationPolicy implements HttpPipelinePolicy {

        private static final ClientLogger LOGGER = new ClientLogger(ResponseValidationPolicy.class);

        private final List<TriConsumer<HttpResponse, ClientLogger, Context>> assertions;

        /**
         * Creates a policy that executes each provided assertion on responses.
         *
         * @param assertions The assertions to apply.
         */
        ResponseValidationPolicy(List<TriConsumer<HttpResponse, ClientLogger, Context>> assertions) {
            this.assertions = new ArrayList<>(assertions);
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().map(response -> {
                assertions.forEach(assertion -> assertion.accept(response, LOGGER, context.getContext()));

                return response;
            });
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            HttpResponse response = next.processSync();

            assertions.forEach(assertion -> assertion.accept(response, LOGGER, context.getContext()));

            return response;
        }
    }
}
