/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

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
    private AzureAsyncOperationPollStrategy(String fullyQualifiedMethodName, String operationResourceUrl, String originalResourceUrl, SerializerAdapter<?> serializer, long delayInMilliseconds) {
        super(delayInMilliseconds);

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
                    .flatMap(new Func1<String, Single<HttpResponse>>() {
                        @Override
                        public Single<HttpResponse> call(String bodyString) {
                            Single<HttpResponse> result;
                            try {
                                final ResourceWithProvisioningState operationResource = serializer.deserialize(bodyString, ResourceWithProvisioningState.class);
                                if (operationResource != null) {
                                    final String resourceProvisioningState = provisioningState(operationResource);
                                    setProvisioningState(resourceProvisioningState);

                                    pollingCompleted = !ProvisioningState.IN_PROGRESS.equalsIgnoreCase(resourceProvisioningState);
                                    if (pollingCompleted) {
                                        pollingSucceeded = ProvisioningState.SUCCEEDED.equalsIgnoreCase(resourceProvisioningState);
                                        clearDelayInMilliseconds();
                                    }
                                }

                                if (httpPollResponse.statusCode() == 200 && operationResource == null) {
                                    result = Single.error(new CloudException("Response does not contain a valid body", httpPollResponse, null));
                                } else {
                                    result = Single.just(httpPollResponse);
                                }
                            } catch (IOException e) {
                                result = Single.error(e);
                            }
                            return result;
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
     * @param fullyQualifiedMethodName The fully qualified name of the method that initiated the
     *                                 long running operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *                     will be read from.
     * @param delayInMilliseconds The delay (in milliseconds) that the resulting pollStrategy will
     *                            use when polling.
     */
    static PollStrategy tryToCreate(String fullyQualifiedMethodName, HttpResponse httpResponse, String originalResourceUrl, SerializerAdapter<?> serializer, long delayInMilliseconds) {
        final String azureAsyncOperationUrl = httpResponse.headerValue(HEADER_NAME);
        return azureAsyncOperationUrl != null && !azureAsyncOperationUrl.isEmpty()
                ? new AzureAsyncOperationPollStrategy(fullyQualifiedMethodName, azureAsyncOperationUrl, originalResourceUrl, serializer, delayInMilliseconds)
                : null;
    }
}