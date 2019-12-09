// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.swagger.RestProxy;
import com.azure.core.http.swagger.SwaggerMethodParser;
import com.azure.core.management.CloudException;
import com.azure.core.management.OperationState;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;

/**
 * A PollStrategy that will continue to poll a resource's URL until the resource's provisioning
 * state property is in a completed state.
 */
public final class ProvisioningStatePollStrategy extends PollStrategy {
    private final ClientLogger logger = new ClientLogger(ProvisioningStatePollStrategy.class);

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

        /**Serial version id for this class*/
        private static final long serialVersionUID = 1L;

        HttpRequest originalRequest;
        String provisioningState;

        /**
         * Create a new ProvisioningStatePollStrategyData.
         * @param restProxy The RestProxy that created this PollStrategy.
         * @param methodParser The method parser that describes the service interface method that
         *     initiated the long running operation.
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
        return new HttpRequest(HttpMethod.GET, data.originalRequest.getUrl());
    }

    @Override
    Mono<HttpResponse> updateFromAsync(HttpResponse pollResponse) {
        return ensureExpectedStatus(pollResponse)
            .flatMap(response -> {
                final HttpResponse bufferedHttpPollResponse = response.buffer();
                return bufferedHttpPollResponse.getBodyAsString()
                    .map(responseBody -> {
                        ResourceWithProvisioningState resource = null;
                        try {
                            resource = deserialize(responseBody, ResourceWithProvisioningState.class);
                        } catch (IOException ignored) {
                        }

                        if (resource == null
                            || resource.getProperties() == null
                            || resource.getProperties().getProvisioningState() == null) {
                            throw logger.logExceptionAsError(new CloudException("The polling response does not "
                                + "contain a valid body", bufferedHttpPollResponse, null));
                        } else if (OperationState.isFailedOrCanceled(resource.getProperties().getProvisioningState())) {
                            throw logger.logExceptionAsError(new CloudException("Async operation failed with "
                                + "provisioning state: " + resource.getProperties().getProvisioningState(),
                                bufferedHttpPollResponse));
                        } else {
                            setStatus(resource.getProperties().getProvisioningState());
                        }
                        return bufferedHttpPollResponse;
                    });
            });
    }

    @Override
    boolean isDone() {
        return OperationState.isCompleted(getStatus());
    }

    @Override
    public Serializable getStrategyData() {
        return this.data;
    }
}
