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
    private Boolean pollingSucceeded;
    private boolean gotResourceResponse;

    /**
     * The name of the header that indicates that a long running operation will use the
     * Azure-AsyncOperation strategy.
     */
    public static final String HEADER_NAME = "Azure-AsyncOperation";

    /**
     * The provisioning state of the operation resource if the operation is still in progress.
     */
    public static final String IN_PROGRESS = "InProgress";

    /**
     * The provisioning state of the operation resource if the operation is successful.
     */
    public static final String SUCCEEDED = "Succeeded";

    /**
     * Create a new AzureAsyncOperationPollStrategy object that will poll the provided operation
     * resource URL.
     * @param fullyQualifiedMethodName The fully qualified name of the method that initiated the
     *                                 long running operation.
     * @param operationResourceUrl The URL of the operation resource this pollStrategy will poll.
     * @param serializer The serializer that will deserialize the operation resource and the
     *                   final operation result.
     */
    private AzureAsyncOperationPollStrategy(String fullyQualifiedMethodName, String operationResourceUrl, String originalResourceUrl, SerializerAdapter<?> serializer) {
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.operationResourceUrl = operationResourceUrl;
        this.originalResourceUrl = originalResourceUrl;
        this.serializer = serializer;
    }

    @Override
    public HttpRequest createPollRequest() {
        String pollUrl = null;
        if (pollingSucceeded == null) {
            pollUrl = operationResourceUrl;
        }
        else if (pollingSucceeded) {
            pollUrl = originalResourceUrl;
        }
        return new HttpRequest(fullyQualifiedMethodName, "GET", pollUrl);
    }

    @Override
    public void updateFrom(HttpResponse httpPollResponse) throws IOException {
        updateDelayInMillisecondsFrom(httpPollResponse);

        if (pollingSucceeded == null) {
            final String bodyString = httpPollResponse.bodyAsString();
            updateFromResponseBodyString(bodyString);
        }
        else if (pollingSucceeded) {
            gotResourceResponse = true;
        }
    }

    @Override
    public Single<HttpResponse> updateFromAsync(final HttpResponse httpPollResponse) {
        Single<HttpResponse> result;

        if (pollingSucceeded == null) {
            updateDelayInMillisecondsFrom(httpPollResponse);

            result = httpPollResponse.bodyAsStringAsync()
                    .flatMap(new Func1<String, Single<HttpResponse>>() {
                        @Override
                        public Single<HttpResponse> call(String bodyString) {
                            Single<HttpResponse> result = Single.just(httpPollResponse);
                            try {
                                updateFromResponseBodyString(bodyString);
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

    private void updateFromResponseBodyString(String httpResponseBodyString) throws IOException {
        final OperationResource operationResource = serializer.deserialize(httpResponseBodyString, OperationResource.class);
        if (operationResource != null) {
            final String provisioningState = provisioningState(operationResource);
            if (!IN_PROGRESS.equalsIgnoreCase(provisioningState)) {
                pollingSucceeded = SUCCEEDED.equalsIgnoreCase(provisioningState);
                clearDelayInMilliseconds();
            }
        }
    }

    private static String provisioningState(OperationResource operationResource) {
        String provisioningState = null;

        final OperationResource.Properties properties = operationResource.properties();
        if (properties != null) {
            provisioningState = properties.provisioningState();
        }

        return provisioningState;
    }

    @Override
    public boolean isDone() {
        return pollingSucceeded != null && (!pollingSucceeded || gotResourceResponse);
    }

    /**
     * Try to create a new AzureAsyncOperationPollStrategy object that will poll the provided
     * operation resource URL. If the provided HttpResponse doesn't have an Azure-AsyncOperation
     * header or if the header is empty, then null will be returned.
     * @param fullyQualifiedMethodName The fully qualified name of the method that initiated the
     *                                 long running operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *                     will be read from.
     */
    static AzureAsyncOperationPollStrategy tryToCreate(String fullyQualifiedMethodName, HttpResponse httpResponse, String originalResourceUrl, SerializerAdapter<?> serializer) {
        final String azureAsyncOperationUrl = httpResponse.headerValue(HEADER_NAME);
        return azureAsyncOperationUrl != null && !azureAsyncOperationUrl.isEmpty()
                ? new AzureAsyncOperationPollStrategy(fullyQualifiedMethodName, azureAsyncOperationUrl, originalResourceUrl, serializer)
                : null;
    }
}