// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.TransactionalBatchImpl;
import com.azure.data.tables.models.TableTransactionActionResponse;

/**
 * Contains all response data for the
 * {@link TransactionalBatchImpl#submitTransactionalBatchWithRestResponseAsync(TransactionalBatchRequestBody, String, Context)}
 * operation.
 */
public final class TransactionalBatchResponse extends ResponseBase<TransactionalBatchSubmitBatchHeaders, TableTransactionActionResponse[]> {
    /**
     * Creates an instance of {@link TransactionalBatchResponse}.
     *
     * @param request The request which resulted in this {@link TransactionalBatchResponse}.
     * @param statusCode The status code of the HTTP response.
     * @param rawHeaders The raw headers of the HTTP response.
     * @param value The content stream.
     * @param headers The deserialized headers of the HTTP response.
     */
    public TransactionalBatchResponse(HttpRequest request, int statusCode, HttpHeaders rawHeaders,
                                      TableTransactionActionResponse[] value, TransactionalBatchSubmitBatchHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /**
     * @return The response content stream.
     */
    @Override
    public TableTransactionActionResponse[] getValue() {
        return super.getValue();
    }
}
