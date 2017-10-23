/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.RestProxy;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Single;
import rx.functions.Func1;

import java.io.IOException;

/**
 * A PollStrategy type that uses the Azure-AsyncOperation header value to check the status of a long
 * running operation.
 */
public final class AzureAsyncOperationPollStrategy extends PollStrategy {
    private final String fullyQualifiedMethodName;
    private final String operationResourceUrl;
    private final String originalResourceUrl;
    private final SerializerAdapter<?> serializer;
    private boolean pollingCompleted;
    private boolean pollingSucceeded;
    private boolean gotResourceResponse;

    /**
     * The name of the header that indicates that a long running operation will use the
     * Azure-AsyncOperation strategy.
     */
    public static final String HEADER_NAME = "Azure-AsyncOperation";

    /**
     * Create a new AzureAsyncOperationPollStrategy object that will poll the provided operation
     * resource URL.
     * @param fullyQualifiedMethodName The fully qualified name of the method that initiated the
     *                                 long running operation.
     * @param operationResourceUrl The URL of the operation resource this pollStrategy will poll.
     * @param originalResourceUrl The URL of the resource that the long running operation is
     *                            operating on.
     * @param serializer The serializer that will deserialize the operation resource and the
     *                   final operation result.
     * @param delayInMilliseconds The delay (in milliseconds) that the pollStrategy will use when
     *                            polling.
     */
    private AzureAsyncOperationPollStrategy(RestProxy restProxy, String fullyQualifiedMethodName, String operationResourceUrl, String originalResourceUrl, SerializerAdapter<?> serializer, long delayInMilliseconds) {
        super(restProxy, delayInMilliseconds);

        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.operationResourceUrl = operationResourceUrl;
        this.originalResourceUrl = originalResourceUrl;
        this.serializer = serializer;
    }

    @Override
    public HttpRequest createPollRequest() {
        String pollUrl;
        if (!pollingCompleted) {
            pollUrl = operationResourceUrl;
        }
        else if (pollingSucceeded) {
            pollUrl = originalResourceUrl;
        } else {
            throw new IllegalStateException("Polling is completed and did not succeed. Cannot create a polling request.");
        }

        return new HttpRequest(fullyQualifiedMethodName, "GET", pollUrl);
    }

    @Override
    public Single<HttpResponse> updateFromAsync(final HttpResponse httpPollResponse) {
        updateDelayInMillisecondsFrom(httpPollResponse);

        Single<HttpResponse> result;
        if (!pollingCompleted) {
            result = httpPollResponse.bodyAsStringAsync()
                    .map(new Func1<String, HttpResponse>() {
                        @Override
                        public HttpResponse call(String bodyString) {
                            ResourceWithProvisioningState operationResource = null;

                            try {
                                operationResource = serializer.deserialize(bodyString, ResourceWithProvisioningState.class, SerializerAdapter.Encoding.JSON);
                            }
                            catch (IOException ignored) {
                            }

                            if (operationResource == null) {
                                throw new CloudException("The polling response does not contain a valid body", httpPollResponse, null);
                            }
                            else {
                                final String resourceProvisioningState = provisioningState(operationResource);
                                setProvisioningState(resourceProvisioningState);

                                pollingCompleted = !ProvisioningState.IN_PROGRESS.equalsIgnoreCase(resourceProvisioningState);
                                if (pollingCompleted) {
                                    pollingSucceeded = ProvisioningState.SUCCEEDED.equalsIgnoreCase(resourceProvisioningState);
                                    clearDelayInMilliseconds();
                                }
                            }

                            return httpPollResponse;
                        }
                    });
        }
        else {
            if (pollingSucceeded) {
                gotResourceResponse = true;
            }

            result = Single.just(httpPollResponse);
        }

        return result;
    }

    private static String provisioningState(ResourceWithProvisioningState operationResource) {
        String provisioningState = null;

        final ResourceWithProvisioningState.Properties properties = operationResource.properties();
        if (properties != null) {
            provisioningState = properties.provisioningState();
        }

        return provisioningState;
    }

    @Override
    public boolean isDone() {
        return pollingCompleted && (!pollingSucceeded || gotResourceResponse);
    }

    /**
     * Try to create a new AzureAsyncOperationPollStrategy object that will poll the provided
     * operation resource URL. If the provided HttpResponse doesn't have an Azure-AsyncOperation
     * header or if the header is empty, then null will be returned.
     * @param originalHttpRequest The original HTTP request that initiated the long running
     *                            operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *                     will be read from.
     * @param delayInMilliseconds The delay (in milliseconds) that the resulting pollStrategy will
     *                            use when polling.
     */
    static PollStrategy tryToCreate(RestProxy restProxy, HttpRequest originalHttpRequest, HttpResponse httpResponse, SerializerAdapter<?> serializer, long delayInMilliseconds) {
        final String azureAsyncOperationUrl = httpResponse.headerValue(HEADER_NAME);
        return azureAsyncOperationUrl != null && !azureAsyncOperationUrl.isEmpty()
                ? new AzureAsyncOperationPollStrategy(restProxy, originalHttpRequest.callerMethod(), azureAsyncOperationUrl, originalHttpRequest.url(), serializer, delayInMilliseconds)
                : null;
    }
}