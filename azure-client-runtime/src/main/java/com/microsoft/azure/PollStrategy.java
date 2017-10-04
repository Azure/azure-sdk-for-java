/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Single;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * An abstract class for the different strategies that an OperationStatus can use when checking the
 * status of a long running operation.
 */
abstract class PollStrategy {
    private long delayInMilliseconds;
    private String provisioningState;

    PollStrategy(long delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    /**
     * Get the number of milliseconds to delay before sending the next poll request.
     * @return The number of milliseconds to delay.
     */
    final long delayInMilliseconds() {
        return delayInMilliseconds;
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
        final String retryAfterSecondsString = httpPollResponse.headerValue("Retry-After");
        if (retryAfterSecondsString != null && !retryAfterSecondsString.isEmpty()) {
            try {
                delayInMilliseconds = Long.valueOf(retryAfterSecondsString) * 1000;
            }
            catch (NumberFormatException ignored) {
            }
        }
    }

    /**
     * If this PollStrategy has a retryAfterSeconds value, delay (and block) the current thread for
     * the number of seconds that are in the retryAfterSeconds value. If this PollStrategy doesn't
     * have a retryAfterSeconds value, then just return.
     */
    void delay() throws InterruptedException {
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

        if (delayInMilliseconds > 0) {
            result = result.delay(delayInMilliseconds, TimeUnit.MILLISECONDS);
        }

        return result;
    }

    /**
     * @return the current provisioning state of the long running operation.
     */
    String provisioningState() {
        return provisioningState;
    }

    /**
     * Set the current provisioning state of the long running operation.
     * @param provisioningState The current provisioning state of the long running operation.
     */
    protected void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * Create a new HTTP poll request.
     * @return A new HTTP poll request.
     */
    abstract HttpRequest createPollRequest();

    /**
     * Update the status of this PollStrategy from the provided HTTP poll response.
     * @param httpPollResponse The response of the most recent poll request.
     */
    abstract void updateFrom(HttpResponse httpPollResponse) throws IOException;

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
}