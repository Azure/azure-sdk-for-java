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
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import java.io.IOException;

/**
 * A PollStrategy that will continue to poll a resource's URL until the resource's provisioning
 * state property is in a completed state.
 */
public class ProvisioningStatePollStrategy extends PollStrategy {
    private final HttpRequest originalRequest;
    private HttpResponse latestHttpResponse;

    ProvisioningStatePollStrategy(RestProxy restProxy, HttpRequest originalRequest, String provisioningState, long delayInMilliseconds) {
        super(restProxy, delayInMilliseconds);

        this.originalRequest = originalRequest;
        setProvisioningState(provisioningState);
    }

    @Override
    HttpRequest createPollRequest() {
        return new HttpRequest(originalRequest.callerMethod(), "GET", originalRequest.url());
    }

    @Override
    Single<HttpResponse> updateFromAsync(final HttpResponse httpPollResponse) {
        latestHttpResponse = httpPollResponse.buffer();
        return latestHttpResponse.bodyAsStringAsync()
                .map(new Func1<String, HttpResponse>() {
                    @Override
                    public HttpResponse call(String responseBody) {
                        try {
                            final ResourceWithProvisioningState resource = deserialize(responseBody, ResourceWithProvisioningState.class);
                            if (resource == null || resource.properties() == null || resource.properties().provisioningState() == null) {
                                setProvisioningState(ProvisioningState.FAILED);
                            }
                            else {
                                setProvisioningState(resource.properties().provisioningState());
                            }
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                        return latestHttpResponse;
                    }
                });
    }

    @Override
    boolean isDone() {
        return ProvisioningState.isCompleted(provisioningState());
    }
}