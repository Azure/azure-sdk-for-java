// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.swagger.RestProxy;
import com.azure.core.http.swagger.SwaggerMethodParser;
import com.azure.core.management.OperationState;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * The "polling strategy" that is used when a request completes immediately and does not require any
 * further polling.
 */
public class CompletedPollStrategy extends PollStrategy {
    private final ClientLogger logger = new ClientLogger(CompletedPollStrategy.class);

    private final HttpResponse firstHttpResponse;
    private CompletedPollStrategyData data;

    /**
     * Create a new CompletedPollStrategy.
     * @param data The poll strategy data.
     */
    public CompletedPollStrategy(CompletedPollStrategyData data) {
        super(data);
        this.firstHttpResponse = data.firstHttpResponse.buffer();
        setStatus(OperationState.SUCCEEDED);
        this.data = data;
    }

    /**
     * The CompletedPollStrategy data.
     */
    public static class CompletedPollStrategyData extends PollStrategyData {
        HttpResponse firstHttpResponse;

        /**Serial version id for this class*/
        private static final long serialVersionUID = 1L;

        /**
         * Create a new CompletedPollStrategyData.
         * @param restProxy The RestProxy that created this PollStrategy.
         * @param methodParser The method parser that describes the service interface method that
         *     initiated the long running operation.
         * @param firstHttpResponse The HTTP response to the original HTTP request.
         */
        public CompletedPollStrategyData(RestProxy restProxy, SwaggerMethodParser methodParser,
                                         HttpResponse firstHttpResponse) {
            super(restProxy, methodParser, 0);
            this.firstHttpResponse = firstHttpResponse;
        }

        PollStrategy initializeStrategy(RestProxy restProxy,
                                        SwaggerMethodParser methodParser) {
            this.restProxy = restProxy;
            this.methodParser = methodParser;
            return new CompletedPollStrategy(this);
        }
    }

    @Override
    public HttpRequest createPollRequest() {
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    Mono<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    boolean isDone() {
        return true;
    }

    Flux<OperationStatus<Object>> pollUntilDoneWithStatusUpdates(final HttpRequest originalHttpRequest,
                                                                 final SwaggerMethodParser methodParser,
                                                                 final Type operationStatusResultType,
                                                                 Context context) {
        return createOperationStatusMono(originalHttpRequest, firstHttpResponse, methodParser,
            operationStatusResultType, context)
            .flatMapMany(cos -> Flux.just(cos));
    }

    Mono<HttpResponse> pollUntilDone() {
        return Mono.<HttpResponse>just(firstHttpResponse);
    }

    @Override
    public Serializable getStrategyData() {
        return this.data;
    }
}
