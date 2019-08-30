// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Builder for a policy to do validation of general response behavior.
 */
public class ResponseValidationPolicyBuilder {

    private final Collection<BiConsumer<HttpResponse, ClientLogger>> assertions = new ArrayList<>();

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
     */
    public ResponseValidationPolicyBuilder optionalEcho(String headerName) {
        assertions.add((httpResponse, logger) -> {
            String responseHeaderValue = httpResponse.headerValue(headerName);
            if (responseHeaderValue != null && !responseHeaderValue.equals(httpResponse.request().headers().value(headerName))) {
                throw logger.logExceptionAsError(new RuntimeException());
            }
        });

        return this;
    }

    /**
     * Immutable policy for asserting validations on general responses.
     */
    public class ResponseValidationPolicy implements HttpPipelinePolicy {

        private final ClientLogger logger = new ClientLogger(ResponseValidationPolicy.class);

        private final Iterable<BiConsumer<HttpResponse, ClientLogger>> assertions;

        /**
         * Creates a policy that executes each provided assertion on responses.
         *
         * @param assertions The assertions to apply.
         */
        ResponseValidationPolicy(Iterable<BiConsumer<HttpResponse, ClientLogger>> assertions) {
            Collection<BiConsumer<HttpResponse, ClientLogger>> assertionsCopy = new ArrayList<>();
            assertions.forEach(assertionsCopy::add);
            this.assertions = assertionsCopy;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            Mono<HttpResponse> httpResponse = next.process();

            for (BiConsumer<HttpResponse, ClientLogger> assertion : assertions) {
                httpResponse = httpResponse.map(response -> {
                    assertion.accept(response, logger);
                    return response;
                });
            }

            return httpResponse;
        }
    }
}
