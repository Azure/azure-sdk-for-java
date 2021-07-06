// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * Implements a Location polling strategy.
 */
public class LocationPollingStrategy implements PollingStrategy {
    private static final String LOCATION = "Location";
    private static final String REQUEST_URL = "requestURL";
    private static final String HTTP_METHOD = "httpMethod";

    private final JacksonAdapter serializer = new JacksonAdapter();

    @Override
    public boolean canPoll(Response<?> activationResponse) {
        HttpHeader locationHeader = activationResponse.getHeaders().get(LOCATION);
        return locationHeader != null;
    }

    @Override
    public String getPollingUrl(PollingContext<BinaryData> context) {
        return context.getData(LOCATION).replace("http://", "https://");
    }

    @Override
    public String getFinalGetUrl(PollingContext<BinaryData> context) {
        String finalGetUrl;
        String httpMethod = context.getData(HTTP_METHOD);
        if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
            finalGetUrl = context.getData(REQUEST_URL);
        } else if ("POST".equalsIgnoreCase(httpMethod) && context.getData(LOCATION) != null) {
            finalGetUrl = context.getData(LOCATION);
        } else {
            throw new RuntimeException("Cannot get final result");
        }

        return finalGetUrl;
    }

    @Override
    public Mono<LongRunningOperationStatus> onActivationResponse(Response<?> response, PollingContext<BinaryData> context) {
        HttpHeader locationHeader = response.getHeaders().get(LOCATION);
        if (locationHeader != null) {
            context.setData(LOCATION, locationHeader.getValue());
        }
        context.setData(HTTP_METHOD, response.getRequest().getHttpMethod().name());
        context.setData(REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            return Mono.just(LongRunningOperationStatus.IN_PROGRESS);
        } else {
            throw new RuntimeException("Operation failed or cancelled: " + response.getStatusCode());
        }
    }

    @Override
    public Mono<LongRunningOperationStatus> onPollingResponse(HttpResponse response, PollingContext<BinaryData> context) {
        HttpHeader locationHeader = response.getHeaders().get(LOCATION);
        if (locationHeader != null) {
            context.setData(LOCATION, locationHeader.getValue());
        }

        LongRunningOperationStatus status;
        if (response.getStatusCode() == 202) {
            status = LongRunningOperationStatus.IN_PROGRESS;
        } else if (response.getStatusCode() >= 200 && response.getStatusCode() <= 204) {
            status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else {
            status = LongRunningOperationStatus.FAILED;
        }

        return Mono.just(status);
    }

    @Override
    public <U> Mono<U> getFinalResult(HttpResponse response, PollingContext<BinaryData> context, Type resultType) {
        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType)) {
            return (Mono<U>) BinaryData.fromFlux(response.getBody());
        } else {
            return response.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                    serializer.deserialize(body, resultType, SerializerEncoding.JSON)));
        }
    }
}
