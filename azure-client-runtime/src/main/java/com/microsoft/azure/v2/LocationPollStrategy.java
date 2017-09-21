/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

import java.io.IOException;

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

    private LocationPollStrategy(String fullyQualifiedMethodName, String locationUrl) {
        super(AzureProxy.defaultDelayInMilliseconds());

        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.locationUrl = locationUrl;
    }

    @Override
    public HttpRequest createPollRequest() {
        return new HttpRequest(fullyQualifiedMethodName, "GET", locationUrl);
    }

    @Override
    public void updateFrom(HttpResponse httpPollResponse) throws IOException {
        final int httpStatusCode = httpPollResponse.statusCode();
        if (httpStatusCode == 202) {
            locationUrl = httpPollResponse.headerValue(HEADER_NAME);
            updateDelayInMillisecondsFrom(httpPollResponse);
        }
        else {
            done = true;
        }
    }

    @Override
    public Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        Single<HttpResponse> result;
        try {
            updateFrom(httpPollResponse);
            result = Single.just(httpPollResponse);
        } catch (IOException e) {
            result = Single.error(e);
        }
        return result;
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
     */
    static LocationPollStrategy tryToCreate(String fullyQualifiedMethodName, HttpResponse httpResponse) {
        final String locationUrl = httpResponse.headerValue(HEADER_NAME);
        return locationUrl != null && !locationUrl.isEmpty()
                ? new LocationPollStrategy(fullyQualifiedMethodName, locationUrl)
                : null;
    }
}