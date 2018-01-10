/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.SwaggerMethodParser;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A PollStrategy type that uses the Azure-AsyncOperation header value to check the status of a long
 * running operation.
 */
public final class AzureAsyncOperationPollStrategy extends PollStrategy {
    private final URL operationResourceUrl;
    private final URL originalResourceUrl;

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
     * @param operationResourceUrl The URL of the operation resource this pollStrategy will poll.
     * @param originalResourceUrl The URL of the resource that the long running operation is
     *                            operating on.
     * @param delayInMilliseconds The delay (in milliseconds) that the pollStrategy will use when
     *                            polling.
     */
    private AzureAsyncOperationPollStrategy(RestProxy restProxy, SwaggerMethodParser methodParser, URL operationResourceUrl, URL originalResourceUrl, long delayInMilliseconds) {
        super(restProxy, methodParser, delayInMilliseconds);

        this.operationResourceUrl = operationResourceUrl;
        this.originalResourceUrl = originalResourceUrl;
    }

    @Override
    public HttpRequest createPollRequest() {
        URL pollUrl;
        if (!pollingCompleted) {
            pollUrl = operationResourceUrl;
        }
        else if (pollingSucceeded) {
            pollUrl = originalResourceUrl;
        } else {
            throw new IllegalStateException("Polling is completed and did not succeed. Cannot create a polling request.");
        }

        return new HttpRequest(fullyQualifiedMethodName(), HttpMethod.GET, pollUrl);
    }

    @Override
    public Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        return ensureExpectedStatus(httpPollResponse)
                .flatMap(new Function<HttpResponse, Single<HttpResponse>>() {
                    @Override
                    public Single<HttpResponse> apply(HttpResponse response) {
                        updateDelayInMillisecondsFrom(response);

                        Single<HttpResponse> result;
                        if (!pollingCompleted) {
                            final HttpResponse bufferedHttpPollResponse = response.buffer();
                            result = bufferedHttpPollResponse.bodyAsStringAsync()
                                    .map(new Function<String, HttpResponse>() {
                                        @Override
                                        public HttpResponse apply(String bodyString) {
                                            AsyncOperationResource operationResource = null;

                                            try {
                                                operationResource = deserialize(bodyString, AsyncOperationResource.class);
                                            }
                                            catch (IOException ignored) {
                                            }

                                            if (operationResource == null || operationResource.status() == null) {
                                                throw new CloudException("The polling response does not contain a valid body", bufferedHttpPollResponse, null);
                                            }
                                            else {
                                                final String status = operationResource.status();
                                                setStatus(status);

                                                pollingCompleted = OperationState.isCompleted(status);
                                                if (pollingCompleted) {
                                                    pollingSucceeded = OperationState.SUCCEEDED.equalsIgnoreCase(status);
                                                    clearDelayInMilliseconds();

                                                    if (!pollingSucceeded) {
                                                        throw new CloudException("Async operation failed with provisioning state: " + status, bufferedHttpPollResponse);
                                                    }

                                                    if (operationResource.id() != null) {
                                                        gotResourceResponse = true;
                                                    }
                                                }
                                            }

                                            return bufferedHttpPollResponse;
                                        }
                                    });
                        }
                        else {
                            if (pollingSucceeded) {
                                gotResourceResponse = true;
                            }

                            result = Single.just(response);
                        }

                        return result;
                    }
                });
    }

    @Override
    public boolean isDone() {
        return pollingCompleted && (!pollingSucceeded || !expectsResourceResponse() || gotResourceResponse);
    }

    /**
     * Try to create a new AzureAsyncOperationPollStrategy object that will poll the provided
     * operation resource URL. If the provided HttpResponse doesn't have an Azure-AsyncOperation
     * header or if the header is empty, then null will be returned.
     * @param restProxy The proxy object that is attempting to create a PollStrategy.
     * @param methodParser The method parser that describes the service interface method that
     *                     initiated the long running operation.
     * @param originalHttpRequest The original HTTP request that initiated the long running
     *                            operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *                     will be read from.
     * @param delayInMilliseconds The delay (in milliseconds) that the resulting pollStrategy will
     *                            use when polling.
     */
    static PollStrategy tryToCreate(RestProxy restProxy, SwaggerMethodParser methodParser, HttpRequest originalHttpRequest, HttpResponse httpResponse, long delayInMilliseconds) {
        URL azureAsyncOperationUrl = null;

        try {
            azureAsyncOperationUrl = new URL(getHeader(httpResponse));
        } catch (MalformedURLException ignored) {
        }

        return azureAsyncOperationUrl != null
                ? new AzureAsyncOperationPollStrategy(restProxy, methodParser, azureAsyncOperationUrl, originalHttpRequest.url(), delayInMilliseconds)
                : null;
    }

    static String getHeader(HttpResponse httpResponse) {
        return httpResponse.headerValue(HEADER_NAME);
    }
}