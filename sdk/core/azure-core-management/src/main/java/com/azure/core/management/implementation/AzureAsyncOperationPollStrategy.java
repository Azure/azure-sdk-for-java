// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.swagger.RestProxy;
import com.azure.core.http.swagger.SwaggerMethodParser;
import com.azure.core.management.AsyncOperationResource;
import com.azure.core.management.CloudException;
import com.azure.core.management.OperationState;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A PollStrategy type that uses the Azure-AsyncOperation header value to check the status of a long
 * running operation.
 */
public final class AzureAsyncOperationPollStrategy extends PollStrategy {
    private final ClientLogger logger = new ClientLogger(AzureAsyncOperationPollStrategy.class);

    private AzureAsyncOperationPollStrategyData data;

    /**
     * The name of the header that indicates that a long running operation will use the
     * Azure-AsyncOperation strategy.
     */
    public static final String HEADER_NAME = "Azure-AsyncOperation";

    /**
     * Create a new AzureAsyncOperationPollStrategy object that will poll the provided operation
     * resource URL.
     * @param data The AzureAsyncOperationPollStrategyData data object.
     */
    private AzureAsyncOperationPollStrategy(AzureAsyncOperationPollStrategyData data) {
        super(data);
        this.data = data;
    }

    /**
     * The AzureAsyncOperationPollStrategy data.
     */
    private static class AzureAsyncOperationPollStrategyData extends PollStrategyData {
        private static final long serialVersionUID = 1L;
        private boolean pollingCompleted;
        private boolean pollingSucceeded;
        private boolean gotResourceResponse;
        private final HttpMethod initialHttpMethod;

        final URL operationResourceUrl;
        final URL originalResourceUrl;
        final URL locationUrl;

        /**
         * Create a new AzureAsyncOperationPollStrategyData object that will poll the provided operation
         * resource URL.
         * @param operationResourceUrl The URL of the operation resource this pollStrategy will poll.
         * @param originalResourceUrl The URL of the resource that the long running operation is
         *     operating on.
         * @param locationUrl The location uri received from service along with operationResourceUrl.
         * @param initialHttpMethod The http method used to initiate the long running operation
         * @param delayInMilliseconds The delay (in milliseconds) that the pollStrategy will use when
         *     polling.
         */
        AzureAsyncOperationPollStrategyData(RestProxy restProxy, SwaggerMethodParser methodParser,
                                            URL operationResourceUrl, URL originalResourceUrl, URL locationUrl,
                                            HttpMethod initialHttpMethod, long delayInMilliseconds) {
            super(restProxy, methodParser, delayInMilliseconds);
            this.operationResourceUrl = operationResourceUrl;
            this.originalResourceUrl = originalResourceUrl;
            this.locationUrl = locationUrl;
            this.initialHttpMethod = initialHttpMethod;
        }

        PollStrategy initializeStrategy(RestProxy restProxy,
                                        SwaggerMethodParser methodParser) {
            this.restProxy = restProxy;
            this.methodParser = methodParser;
            return new AzureAsyncOperationPollStrategy(this);
        }
    }

    @Override
    public HttpRequest createPollRequest() {
        URL pollUrl;
        if (!data.pollingCompleted) {
            pollUrl = data.operationResourceUrl;
        } else if (data.pollingSucceeded) {
            if (data.initialHttpMethod == HttpMethod.POST || data.initialHttpMethod == HttpMethod.DELETE) {
                if (data.locationUrl != null) {
                    pollUrl = data.locationUrl;
                } else {
                    pollUrl = data.operationResourceUrl;
                }
            } else {
                // For PUT|PATCH do a final get on the original resource uri.
                //
                pollUrl = data.originalResourceUrl;
            }
        } else {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Polling is completed and did not succeed. Cannot create a polling request."));
        }

        return new HttpRequest(HttpMethod.GET, pollUrl);
    }

    @Override
    public Mono<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        return ensureExpectedStatus(httpPollResponse)
            .flatMap(response -> {
                updateDelayInMillisecondsFrom(response);
                Mono<HttpResponse> result;
                if (!data.pollingCompleted) {
                    final HttpResponse bufferedHttpPollResponse = response.buffer();
                    result = bufferedHttpPollResponse.getBodyAsString()
                        .map(bodyString -> {
                            AsyncOperationResource operationResource = null;
                            try {
                                operationResource = deserialize(bodyString, AsyncOperationResource.class);
                            } catch (IOException ignored) {
                            }
                            //
                            if (operationResource == null || operationResource.getStatus() == null) {
                                throw logger.logExceptionAsError(new CloudException(
                                    "The polling response does not contain a valid body",
                                    bufferedHttpPollResponse,
                                    null));
                            } else {
                                final String status = operationResource.getStatus();
                                setStatus(status);

                                data.pollingCompleted = OperationState.isCompleted(status);
                                if (data.pollingCompleted) {
                                    data.pollingSucceeded = OperationState.SUCCEEDED.equalsIgnoreCase(status);
                                    clearDelayInMilliseconds();

                                    if (!data.pollingSucceeded) {
                                        throw logger.logExceptionAsError(new CloudException(
                                            "Async operation failed with provisioning state: " + status,
                                            bufferedHttpPollResponse));
                                    }

                                    if (operationResource.getId() != null) {
                                        data.gotResourceResponse = true;
                                    }
                                }
                                return bufferedHttpPollResponse;
                            }
                        });
                } else {
                    if (data.pollingSucceeded) {
                        data.gotResourceResponse = true;
                    }
                    result = Mono.just(response);
                }
                return result;
            });
    }

    @Override
    public boolean isDone() {
        return data.pollingCompleted
            && (!data.pollingSucceeded || !expectsResourceResponse() || data.gotResourceResponse);
    }

    /**
     * Try to create a new AzureAsyncOperationPollStrategy object that will poll the provided
     * operation resource URL. If the provided HttpResponse doesn't have an Azure-AsyncOperation
     * header or if the header is empty, then null will be returned.
     * @param restProxy The proxy object that is attempting to create a PollStrategy.
     * @param methodParser The method parser that describes the service interface method that
     *     initiated the long running operation.
     * @param originalHttpRequest The original HTTP request that initiated the long running
     *     operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *     will be read from.
     * @param delayInMilliseconds The delay (in milliseconds) that the resulting pollStrategy will
     *     use when polling.
     */
    static PollStrategy tryToCreate(RestProxy restProxy, SwaggerMethodParser methodParser,
                                    HttpRequest originalHttpRequest, HttpResponse httpResponse,
                                    long delayInMilliseconds) {
        String urlHeader = getHeader(httpResponse);
        URL azureAsyncOperationUrl = null;
        if (urlHeader != null) {
            try {
                azureAsyncOperationUrl = new URL(urlHeader);
            } catch (MalformedURLException ignored) {
            }
        }

        urlHeader = httpResponse.getHeaderValue("Location");
        URL locationUrl = null;
        if (urlHeader != null) {
            try {
                locationUrl = new URL(urlHeader);
            } catch (MalformedURLException ignored) {
            }
        }

        return azureAsyncOperationUrl != null
            ? new AzureAsyncOperationPollStrategy(
            new AzureAsyncOperationPollStrategyData(restProxy, methodParser, azureAsyncOperationUrl,
                originalHttpRequest.getUrl(), locationUrl, originalHttpRequest.getHttpMethod(), delayInMilliseconds))
            : null;
    }

    static String getHeader(HttpResponse httpResponse) {
        return httpResponse.getHeaderValue(HEADER_NAME);
    }

    @Override
    public Serializable getStrategyData() {
        return this.data;
    }
}
