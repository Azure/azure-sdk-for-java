/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.RestProxy;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Single;

/**
 * A PollStrategy type that uses the Location header value to check the status of a long running
 * operation.
 */
public final class LocationPollStrategy extends PollStrategy {
    private final String fullyQualifiedMethodName;
    private String locationUrl;
    private boolean done;

    /**
     * The name of the header that indicates that a long running operation will use the Location
     * strategy.
     */
    public static final String HEADER_NAME = "Location";

    private LocationPollStrategy(RestProxy restProxy, String fullyQualifiedMethodName, String locationUrl, long delayInMilliseconds) {
        super(restProxy, delayInMilliseconds);

        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.locationUrl = locationUrl;
    }

    @Override
    public HttpRequest createPollRequest() {
        return new HttpRequest(fullyQualifiedMethodName, "GET", locationUrl);
    }

    @Override
    public Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        final int httpStatusCode = httpPollResponse.statusCode();
        if (httpStatusCode == 202) {
            String newLocationUrl = httpPollResponse.headerValue(HEADER_NAME);
            if (newLocationUrl != null) {
                locationUrl = newLocationUrl;
            }
            
            updateDelayInMillisecondsFrom(httpPollResponse);
        }
        else {
            done = true;
        }
        return Single.just(httpPollResponse);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Try to create a new LocationOperationPollStrategy object that will poll the provided location
     * URL. If the provided HttpResponse doesn't have a Location header or the header is empty,
     * then null will be returned.
     * @param fullyQualifiedMethodName The fully qualified name of the method that initiated the
     *                                 long running operation.
     * @param httpResponse The HTTP response that the required header values for this pollStrategy
     *                     will be read from.
     * @param delayInMilliseconds The delay (in milliseconds) that the resulting pollStrategy will
     *                            use when polling.
     */
    static PollStrategy tryToCreate(RestProxy restProxy, String fullyQualifiedMethodName, HttpResponse httpResponse, long delayInMilliseconds) {
        final String locationUrl = httpResponse.headerValue(HEADER_NAME);
        return locationUrl != null && !locationUrl.isEmpty()
                ? new LocationPollStrategy(restProxy, fullyQualifiedMethodName, locationUrl, delayInMilliseconds)
                : null;
    }
}