/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The current state of polling for the result of a long running operation.
 * @param <T> The type of value that will be returned from the long running operation.
 */
public class OperationStatus<T> {
    private PollStrategy pollStrategy;
    private T result;

    /**
     * Create a new OperationStatus from the provided HTTP response.
     * @param originalHttpRequest The HttpRequest that initiated the long running operation.
     * @param originalHttpResponse The HttpResponse from the request that initiated the long running
     *                             operation.
     * @param serializer The serializer used to deserialize the response body.
     */
    OperationStatus(HttpRequest originalHttpRequest, HttpResponse originalHttpResponse, SerializerAdapter<?> serializer) {
        final int httpStatusCode = originalHttpResponse.statusCode();

        if (httpStatusCode != 200) {
            final String fullyQualifiedMethodName = originalHttpRequest.callerMethod();
            final String originalHttpRequestMethod = originalHttpRequest.httpMethod();
            final String originalHttpRequestUrl = originalHttpRequest.url();

            if (originalHttpRequestMethod.equalsIgnoreCase("PUT") || originalHttpRequestMethod.equalsIgnoreCase("PATCH")) {
                if (httpStatusCode == 201) {
                    pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(fullyQualifiedMethodName, originalHttpResponse, originalHttpRequestUrl, serializer);
                } else if (httpStatusCode == 202) {
                    pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(fullyQualifiedMethodName, originalHttpResponse, originalHttpRequestUrl, serializer);
                    if (pollStrategy == null) {
                        pollStrategy = LocationPollStrategy.tryToCreate(fullyQualifiedMethodName, originalHttpResponse);
                    }
                }
            } else /* if (originalRequestHttpMethod.equalsIgnoreCase("DELETE") || originalRequestHttpMethod.equalsIgnoreCase("POST") */ {
                if (httpStatusCode == 202) {
                    pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(fullyQualifiedMethodName, originalHttpResponse, originalHttpRequestUrl, serializer);
                    if (pollStrategy == null) {
                        pollStrategy = LocationPollStrategy.tryToCreate(fullyQualifiedMethodName, originalHttpResponse);
                    }
                }
            }
        }
    }

    /**
     * Update the properties of this OperationStatus from the provided response.
     * @param httpResponse The HttpResponse from the most recent request.
     */
    void updateFrom(HttpResponse httpResponse) throws IOException {
        pollStrategy.updateFrom(httpResponse);
    }

    /**
     * Update the properties of this OperationStatus from the provided HTTP poll response.
     * @param httpPollResponse The response of the most recent poll request.
     * @return A Single that can be used to chain off of this operation.
     */
    Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        return pollStrategy.updateFromAsync(httpPollResponse);
    }

    /**
     * Get whether or not the long running operation is done.
     * @return Whether or not the long running operation is done.
     */
    public boolean isDone() {
        return pollStrategy == null || pollStrategy.isDone();
    }

    /**
     * Create a HttpRequest that will get the next polling status update for the long running
     * operation.
     * @return A HttpRequest that will get the next polling status update for the long running
     * operation.
     */
    HttpRequest createPollRequest() {
        return pollStrategy.createPollRequest();
    }

    /**
     * If this OperationStatus has a retryAfterSeconds value, delay (and block) the current thread for
     * the number of seconds that are in the retryAfterSeconds value. If this OperationStatus doesn't
     * have a retryAfterSeconds value, then just return.
     */
    void delay() throws InterruptedException {
        final long delayInMilliseconds = pollStrategy.delayInMilliseconds();
        if (delayInMilliseconds > 0) {
            Thread.sleep(delayInMilliseconds);
        }
    }

    /**
     * If this OperationStatus has a retryAfterSeconds value, return an Single that is delayed by the
     * number of seconds that are in the retryAfterSeconds value. If this OperationStatus doesn't have
     * a retryAfterSeconds value, then return an Single with no delay.
     * @return A Single with delay if this OperationStatus has a retryAfterSeconds value.
     */
    Single<Void> delayAsync() {
        Single<Void> result = Single.just(null);

        final long delayInMilliseconds = pollStrategy.delayInMilliseconds();
        if (delayInMilliseconds > 0) {
            result = result.delay(delayInMilliseconds, TimeUnit.MILLISECONDS);
        }

        return result;
    }

    /**
     * If the long running operation is done, get the result of the operation. If the operation is
     * not done, then return null.
     * @return The result of the operation, or null if the operation isn't done yet.
     */
    public T result() {
        return result;
    }

    /**
     * Set the result of this OperationStatus.
     * @param result The result to assign to this OperationStatus.
     */
    void setResult(T result) {
        this.result = result;
    }
}
