// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.RequestOptions;

import java.util.UUID;

public class RequestOptionsIndexesConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.RequestOptions} to {@link RequestOptions}.
     */
    public static RequestOptions map(com.azure.search.documents.indexes.implementation.models.RequestOptions obj) {
        if (obj == null) {
            return null;
        }
        RequestOptions requestOptions = new RequestOptions();

        UUID xMsClientRequestId = obj.getXMsClientRequestId();
        requestOptions.setXMsClientRequestId(xMsClientRequestId);
        return requestOptions;
    }

    /**
     * Maps from {@link RequestOptions} to {@link com.azure.search.documents.indexes.implementation.models.RequestOptions}.
     */
    public static com.azure.search.documents.indexes.implementation.models.RequestOptions map(RequestOptions obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.RequestOptions requestOptions =
            new com.azure.search.documents.indexes.implementation.models.RequestOptions();

        UUID xMsClientRequestId = obj.getXMsClientRequestId();
        requestOptions.setXMsClientRequestId(xMsClientRequestId);
        return requestOptions;
    }
    private RequestOptionsIndexesConverter() {
    }
}
