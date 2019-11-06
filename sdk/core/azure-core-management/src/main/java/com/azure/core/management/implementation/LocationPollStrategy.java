// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.swagger.RestProxy;
import com.azure.core.http.swagger.SwaggerMethodParser;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * A PollStrategy type that uses the Location header value to check the status of a long running
 * operation.
 */
public final class LocationPollStrategy extends PollStrategy {
    private final ClientLogger logger = new ClientLogger(LocationPollStrategy.class);

    LocationPollStrategyData data;

    /**
     * The name of the header that indicates that a long running operation will use the Location
     * strategy.
     */
    public static final String HEADER_NAME = "Location";

    private LocationPollStrategy(LocationPollStrategyData data) {
        super(data);
        this.data = data;
    }

    /**
     * The LocationPollStrategy data.
     */
    public static class LocationPollStrategyData extends PollStrategyData {
        private static final long serialVersionUID = 1L;
        URL locationUrl;
        boolean done;

        /**
         * Create a new LocationPollStrategyData.
         */
        public LocationPollStrategyData() {
            super(null, null, 0);
            this.locationUrl = null;
        }

        /**
         * Create a new LocationPollStrategyData.
         * @param restProxy The RestProxy that created this PollStrategy.
         * @param methodParser The method parser that describes the service interface method that
         *     initiated the long running operation.
         * @param locationUrl The location url.
         * @param delayInMilliseconds The delay value.
         */
        public LocationPollStrategyData(RestProxy restProxy,
                                        SwaggerMethodParser methodParser,
                                        URL locationUrl,
                                        long delayInMilliseconds) {
            super(restProxy, methodParser, delayInMilliseconds);
            this.locationUrl = locationUrl;
        }

        PollStrategy initializeStrategy(RestProxy restProxy,
                                        SwaggerMethodParser methodParser) {
            this.restProxy = restProxy;
            this.methodParser = methodParser;
            return new LocationPollStrategy(this);
        }
    }

    @Override
    public HttpRequest createPollRequest() {
        return new HttpRequest(HttpMethod.GET, data.locationUrl);
    }

    @Override
    public Mono<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        return ensureExpectedStatus(httpPollResponse, new int[]{202})
            .map(response -> {
                final int httpStatusCode = response.getStatusCode();
                updateDelayInMillisecondsFrom(response);
                if (httpStatusCode == 202) {
                    String newLocationUrl = getHeader(response);
                    if (newLocationUrl != null) {
                        try {
                            data.locationUrl = new URL(newLocationUrl);
                        } catch (MalformedURLException mfue) {
                            throw logger.logExceptionAsError(Exceptions.propagate(mfue));
                        }
                    }
                } else {
                    data.done = true;
                }
                return response;
            });
    }

    @Override
    public boolean isDone() {
        return data.done;
    }

    /**
     * Try to create a new LocationOperationPollStrategy object that will poll the provided location
     * URL. If the provided HttpResponse doesn't have a Location header or the header is empty,
     * then null will be returned.
     * @param originalHttpRequest The original HTTP request.
     * @param methodParser The method parser that describes the service interface method that
     *     initiated the long running operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *     will be read from.
     * @param delayInMilliseconds The delay (in milliseconds) that the resulting pollStrategy will
     *     use when polling.
     */
    static PollStrategy tryToCreate(RestProxy restProxy, SwaggerMethodParser methodParser,
                                    HttpRequest originalHttpRequest, HttpResponse httpResponse,
                                    long delayInMilliseconds) {
        final String locationUrl = getHeader(httpResponse);

        URL pollUrl = null;
        if (locationUrl != null && !locationUrl.isEmpty()) {
            if (locationUrl.startsWith("/")) {
                try {
                    final URL originalRequestUrl = originalHttpRequest.getUrl();
                    pollUrl = new URL(originalRequestUrl, locationUrl);
                } catch (MalformedURLException ignored) {
                }
            } else {
                final String locationUrlLower = locationUrl.toLowerCase(Locale.ROOT);
                if (locationUrlLower.startsWith("http://") || locationUrlLower.startsWith("https://")) {
                    try {
                        pollUrl = new URL(locationUrl);
                    } catch (MalformedURLException ignored) {
                    }
                }
            }
        }

        return pollUrl == null
            ? null
            : new LocationPollStrategy(
            new LocationPollStrategyData(restProxy, methodParser, pollUrl, delayInMilliseconds));
    }

    static String getHeader(HttpResponse httpResponse) {
        return httpResponse.getHeaderValue(HEADER_NAME);
    }

    @Override
    public Serializable getStrategyData() {
        return this.data;
    }
}
