// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms;

import com.azure.communication.sms.implementation.models.SmsSendResponse;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;

/**
 * Async client for sending SMS messages with Azure Communication SMS Service.
 */
public final class SmsResponseMapSingle implements Response<SmsSendResult> {
    private final Response<SmsSendResponse> originalResponse;
    private final SmsSendResult value;



    SmsResponseMapSingle(Response<SmsSendResponse> originalResponse, SmsSendResult
        value) {
        this.originalResponse = originalResponse;
        this.value = value;
    }

    @Override
    public int getStatusCode() {
        return originalResponse.getStatusCode();
    }

    @Override
    public HttpHeaders getHeaders() {
        return originalResponse.getHeaders();
    }

    @Override
    public HttpRequest getRequest() {
        return originalResponse.getRequest();
    }

    @Override
    public SmsSendResult getValue() {
        return value;
    }
}
