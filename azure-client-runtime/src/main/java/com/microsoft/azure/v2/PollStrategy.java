/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.v2.RestException;
import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.SwaggerMethodParser;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.protocol.SerializerAdapter.Encoding;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * An abstract class for the different strategies that an OperationStatus can use when checking the
 * status of a long running operation.
 */
abstract class PollStrategy {
    private final RestProxy restProxy;

    private long delayInMilliseconds;
    private String status;

    PollStrategy(RestProxy restProxy, long delayInMilliseconds) {
        this.restProxy = restProxy;
        this.delayInMilliseconds = delayInMilliseconds;
    }

    protected Object deserialize(String value, Type returnType) throws IOException {
        return restProxy.deserialize(value, returnType, null, Encoding.JSON);
    }

    /**
     * Set the delay in milliseconds to 0.
     */
    final void clearDelayInMilliseconds() {
        this.delayInMilliseconds = 0;
    }

    /**
     * Update the delay in milliseconds from the provided HTTP poll response.
     * @param httpPollResponse The HTTP poll response to update the delay in milliseconds from.
     */
    final void updateDelayInMillisecondsFrom(HttpResponse httpPollResponse) {
        final Long parsedDelayInMilliseconds = delayInMillisecondsFrom(httpPollResponse);
        if (parsedDelayInMilliseconds != null) {
            delayInMilliseconds = parsedDelayInMilliseconds;
        }
    }

    static Long delayInMillisecondsFrom(HttpResponse httpResponse) {
        Long result = null;

        final String retryAfterSecondsString = httpResponse.headerValue("Retry-After");
        if (retryAfterSecondsString != null && !retryAfterSecondsString.isEmpty()) {
            result = Long.valueOf(retryAfterSecondsString) * 1000;
        }

        return result;
    }

    /**
     * If this OperationStatus has a retryAfterSeconds value, return an Single that is delayed by the
     * number of seconds that are in the retryAfterSeconds value. If this OperationStatus doesn't have
     * a retryAfterSeconds value, then return an Single with no delay.
     * @return A Single with delay if this OperationStatus has a retryAfterSeconds value.
     */
    Single<Void> delayAsync() {
        Single<Void> result = Single.just(null);

        if (delayInMilliseconds > 0) {
            result = result.delay(delayInMilliseconds, TimeUnit.MILLISECONDS);
        }

        return result;
    }

    /**
     * @return the current status of the long running operation.
     */
    String status() {
        return status;
    }

    /**
     * Set the current status of the long running operation.
     * @param status The current status of the long running operation.
     */
    void setStatus(String status) {
        this.status = status;
    }

    /**
     * Create a new HTTP poll request.
     * @return A new HTTP poll request.
     */
    abstract HttpRequest createPollRequest();

    /**
     * Update the status of this PollStrategy from the provided HTTP poll response.
     * @param httpPollResponse The response of the most recent poll request.
     * @return A Completable that can be used to chain off of this operation.
     */
    abstract Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse);

    /**
     * Get whether or not this PollStrategy's long running operation is done.
     * @return Whether or not this PollStrategy's long running operation is done.
     */
    abstract boolean isDone();

    Observable<HttpResponse> sendPollRequestWithDelay() {
        return Observable.defer(new Func0<Observable<HttpResponse>>() {
            @Override
            public Observable<HttpResponse> call() {
                return delayAsync()
                        .flatMap(new Func1<Void, Single<HttpResponse>>() {
                            @Override
                            public Single<HttpResponse> call(Void ignored) {
                                final HttpRequest pollRequest = createPollRequest();
                                return restProxy.sendHttpRequestAsync(pollRequest);
                            }
                        })
                        .flatMap(new Func1<HttpResponse, Single<HttpResponse>>() {
                            @Override
                            public Single<HttpResponse> call(HttpResponse response) {
                                return updateFromAsync(response);
                            }
                        })
                        .toObservable();
            }
        });
    }

    Observable<OperationStatus<Object>> createOperationStatusObservable(HttpRequest httpRequest, HttpResponse httpResponse, SwaggerMethodParser methodParser, Type operationStatusResultType) {
        OperationStatus<Object> operationStatus;
        if (!isDone()) {
            operationStatus = new OperationStatus<>(this);
        }
        else {
            try {
                final Object resultObject = restProxy.handleAsyncReturnType(httpRequest, Single.just(httpResponse), methodParser, operationStatusResultType);
                operationStatus = new OperationStatus<>(resultObject, status());
            } catch (RestException e) {
                operationStatus = new OperationStatus<>(e, OperationState.FAILED);
            }
        }
        return Observable.just(operationStatus);
    }

    Observable<OperationStatus<Object>> pollUntilDoneWithStatusUpdates(final HttpRequest originalHttpRequest, final SwaggerMethodParser methodParser, final Type operationStatusResultType) {
        return sendPollRequestWithDelay()
                    .flatMap(new Func1<HttpResponse, Observable<OperationStatus<Object>>>() {
                        @Override
                        public Observable<OperationStatus<Object>> call(HttpResponse httpResponse) {
                            return createOperationStatusObservable(originalHttpRequest, httpResponse, methodParser, operationStatusResultType);
                        }
                    })
                    .repeat()
                    .takeUntil(new Func1<OperationStatus<Object>, Boolean>() {
                        @Override
                        public Boolean call(OperationStatus<Object> operationStatus) {
                            return isDone();
                        }
                    });
    }

    Single<HttpResponse> pollUntilDone() {
        return sendPollRequestWithDelay()
                .repeat()
                .takeUntil(new Func1<HttpResponse, Boolean>() {
                    @Override
                    public Boolean call(HttpResponse ignored) {
                        return isDone();
                    }
                })
                .last()
                .toSingle();
    }
}