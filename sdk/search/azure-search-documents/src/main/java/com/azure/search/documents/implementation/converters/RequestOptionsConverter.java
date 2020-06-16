// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.RequestOptions;

import java.util.UUID;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.RequestOptions} and
 * {@link RequestOptions}.
 */
public final class RequestOptionsConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.RequestOptions} to {@link RequestOptions}.
     */
    public static RequestOptions map(com.azure.search.documents.implementation.models.RequestOptions obj) {
        if (obj == null) {
            return null;
        }
        RequestOptions requestOptions = new RequestOptions();

        UUID xMsClientRequestId = obj.getXMsClientRequestId();
        requestOptions.setClientRequestId(xMsClientRequestId);
        return requestOptions;
    }

    /**
     * Maps from {@link RequestOptions} to {@link com.azure.search.documents.implementation.models.RequestOptions}.
     */
    public static com.azure.search.documents.implementation.models.RequestOptions map(RequestOptions obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.RequestOptions requestOptions =
            new com.azure.search.documents.implementation.models.RequestOptions();

        UUID xMsClientRequestId = obj.getClientRequestId();
        requestOptions.setXMsClientRequestId(xMsClientRequestId);
        return requestOptions;
    }

    private RequestOptionsConverter() {
    }
}
