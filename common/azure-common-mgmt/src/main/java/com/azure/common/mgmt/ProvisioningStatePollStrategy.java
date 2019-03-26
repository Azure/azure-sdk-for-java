/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import com.azure.common.implementation.RestProxy;
import com.azure.common.implementation.SwaggerMethodParser;
import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;

/**
 * A PollStrategy that will continue to poll a resource's URL until the resource's provisioning
 * state property is in a completed state.
 */
public final class ProvisioningStatePollStrategy extends PollStrategy {
    private ProvisioningStatePollStrategyData data;
    ProvisioningStatePollStrategy(ProvisioningStatePollStrategyData data) {
        super(data);
        setStatus(data.provisioningState);
        this.data = data;
    }

    /**
     * The ProvisioningStatePollStrategy data.
     */
    public static class ProvisioningStatePollStrategyData extends PollStrategy.PollStrategyData {
        HttpRequest originalRequest;
        String provisioningState;

        /**
         * Create a new ProvisioningStatePollStrategyData.
         * @param restProxy The RestProxy that created this PollStrategy.
         * @param methodParser The method parser that describes the service interface method that
         *                     initiated the long running operation.
         * @param originalRequest The HTTP response to the original HTTP request.
         * @param provisioningState The provisioning state.
         * @param delayInMilliseconds The delay value.
         */
        public ProvisioningStatePollStrategyData(RestProxy restProxy,
                                                 SwaggerMethodParser methodParser,
                                                 HttpRequest originalRequest,
                                                 String provisioningState,
                                                 long delayInMilliseconds) {
            super(restProxy, methodParser, delayInMilliseconds);
            this.originalRequest = originalRequest;
            this.provisioningState = provisioningState;
        }

        PollStrategy initializeStrategy(RestProxy restProxy,
                                                 SwaggerMethodParser methodParser) {
            this.restProxy = restProxy;
            this.methodParser = methodParser;
            return new ProvisioningStatePollStrategy(this);
        }

    }

    @Override
    HttpRequest createPollRequest() {
        return new HttpRequest(HttpMethod.GET, data.originalRequest.url());
    }

    @Override
    Mono<HttpResponse> updateFromAsync(HttpResponse pollResponse) {
        return ensureExpectedStatus(pollResponse)
                .flatMap(response -> {
                        final HttpResponse bufferedHttpPollResponse = response.buffer();
                        return bufferedHttpPollResponse.bodyAsString()
                                .map(responseBody -> {
                                        ResourceWithProvisioningState resource = null;
                                        try {
                                            resource = deserialize(responseBody, ResourceWithProvisioningState.class);
                                        } catch (IOException ignored) {
                                        }

                                        if (resource == null || resource.properties() == null || resource.properties().provisioningState() == null) {
                                            throw new CloudException("The polling response does not contain a valid body", bufferedHttpPollResponse, null);
                                        }
                                        else if (OperationState.isFailedOrCanceled(resource.properties().provisioningState())) {
                                            throw new CloudException("Async operation failed with provisioning state: " + resource.properties().provisioningState(), bufferedHttpPollResponse);
                                        }
                                        else {
                                            setStatus(resource.properties().provisioningState());
                                        }
                                        return bufferedHttpPollResponse;
                                });
                });
    }

    @Override
    boolean isDone() {
        return OperationState.isCompleted(status());
    }

    @Override
    public Serializable strategyData() {
        return this.data;
    }
}