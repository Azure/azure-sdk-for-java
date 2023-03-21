// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import java.util.List;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.IterableStream;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.models.TableEntity;

public class EntityPaged<T extends TableEntity> implements PagedResponse<T> {
    private static final String DELIMITER_CONTINUATION_TOKEN = ";";
    private final Response<TableEntityQueryResponse> httpResponse;
    private final IterableStream<T> entityStream;
    private final String continuationToken;

    public EntityPaged(Response<TableEntityQueryResponse> httpResponse, List<T> entityList,
                String nextPartitionKey, String nextRowKey) {
        if (nextPartitionKey == null) {
            this.continuationToken = null;
        }
        else if (nextRowKey == null) {
            this.continuationToken = nextPartitionKey;
        }
        else {
            this.continuationToken = String.join(DELIMITER_CONTINUATION_TOKEN, nextPartitionKey, nextRowKey);
        }

        this.httpResponse = httpResponse;
        this.entityStream = IterableStream.of(entityList);
    }

    @Override
    public int getStatusCode() {
        return httpResponse.getStatusCode();
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpResponse.getHeaders();
    }

    @Override
    public HttpRequest getRequest() {
        return httpResponse.getRequest();
    }

    @Override
    public IterableStream<T> getElements() {
        return entityStream;
    }

    @Override
    public String getContinuationToken() {
        return continuationToken;
    }

    @Override
    public void close() {
    }
}
