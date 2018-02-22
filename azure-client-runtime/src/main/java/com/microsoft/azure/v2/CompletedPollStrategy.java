/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.SwaggerMethodParser;
import com.microsoft.rest.v2.http.BufferedHttpResponse;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * The "polling strategy" that is used when a request completes immediately and does not require any
 * further polling.
 */
public class CompletedPollStrategy extends PollStrategy {
    private final BufferedHttpResponse firstHttpResponse;
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

    public static class CompletedPollStrategyData extends PollStrategyData
    {
        HttpResponse firstHttpResponse;

        /**
         * Create a new CompletedPollStrategyData.
         * @param restProxy The RestProxy that created this PollStrategy.
         * @param methodParser The method parser that describes the service interface method that
         *                     initiated the long running operation.
         * @param firstHttpResponse The HTTP response to the original HTTP request.
         */
        public CompletedPollStrategyData(RestProxy restProxy, SwaggerMethodParser methodParser, HttpResponse firstHttpResponse) {
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

    public
    @Override
    HttpRequest createPollRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean isDone() {
        return true;
    }

    Observable<OperationStatus<Object>> pollUntilDoneWithStatusUpdates(final HttpRequest originalHttpRequest, final SwaggerMethodParser methodParser, final Type operationStatusResultType) {
        return createOperationStatusObservable(originalHttpRequest, firstHttpResponse, methodParser, operationStatusResultType);
    }

    Single<HttpResponse> pollUntilDone() {
        return Single.<HttpResponse>just(firstHttpResponse);
    }

    public Serializable strategyData() {
        return this.data;
    }
}
