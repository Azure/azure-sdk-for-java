// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollResult;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import java.io.IOException;

public class OperationResourcePollingStrategy implements PollingStrategy {
    private static final String OPERATION_LOCATION = "Operation-Location";
    private static final String LOCATION = "Location";
    private static final String REQUEST_URL = "requestURL";
    private static final String HTTP_METHOD = "httpMethod";

    private final JacksonAdapter serializer = new JacksonAdapter();

    @Override
    public boolean canPoll(Response<?> activationResponse) {
        HttpHeader operationLocationHeader = activationResponse.getHeaders().get(OPERATION_LOCATION);
        return operationLocationHeader != null;
    }

    @Override
    public String getPollingUrl(PollingContext<PollResult> ctx) {
        return ctx.getData(OPERATION_LOCATION);
    }

    @Override
    public String getFinalResultUrl(PollingContext<PollResult> ctx) {
        PollResponse<PollResult> lastResponse = ctx.getLatestResponse();
        String finalGetUrl = lastResponse.getValue().getResourceLocation();
        if (finalGetUrl == null) {
            String httpMethod = ctx.getData(HTTP_METHOD);
            if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
                finalGetUrl = ctx.getData(REQUEST_URL);
            } else if ("POST".equalsIgnoreCase(httpMethod) && ctx.getData(LOCATION) != null) {
                finalGetUrl = ctx.getData(LOCATION);
            } else {
                throw new RuntimeException("Cannot get final result");
            }
        }

        return finalGetUrl;
    }

    @Override
    public PollResult parseInitialResponse(Response<?> response, PollingContext<PollResult> ctx) {
        HttpHeader operationLocationHeader = response.getHeaders().get(OPERATION_LOCATION);
        HttpHeader locationHeader = response.getHeaders().get(LOCATION);
        if (operationLocationHeader != null) {
            ctx.setData(OPERATION_LOCATION, operationLocationHeader.getValue());
        }
        if (locationHeader != null) {
            ctx.setData(LOCATION, locationHeader.getValue());
        }
        ctx.setData(HTTP_METHOD, response.getRequest().getHttpMethod().name());
        ctx.setData(REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            return new PollResult().setStatus(LongRunningOperationStatus.IN_PROGRESS);
        } else {
            throw new RuntimeException("Operation failed or cancelled: " + response.getStatusCode());
        }
    }

    @Override
    public PollResult parsePollingResponse(HttpResponse response, String responseBody, PollingContext<PollResult> ctx) {
        try {
            return serializer.deserialize(responseBody, PollResult.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
