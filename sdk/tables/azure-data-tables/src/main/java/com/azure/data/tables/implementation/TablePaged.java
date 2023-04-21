// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import java.util.List;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.IterableStream;
import com.azure.data.tables.implementation.models.TableQueryResponse;
import com.azure.data.tables.models.TableItem;

public class TablePaged implements PagedResponse<TableItem> {
    final Response<TableQueryResponse> httpResponse;
    final IterableStream<TableItem> tableStream;
    final String continuationToken;

    public TablePaged(Response<TableQueryResponse> httpResponse, List<TableItem> tableList, String continuationToken) {
        this.httpResponse = httpResponse;
        this.tableStream = IterableStream.of(tableList);
        this.continuationToken = continuationToken;
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
    public IterableStream<TableItem> getElements() {
        return tableStream;
    }

    @Override
    public String getContinuationToken() {
        return continuationToken;
    }

    @Override
    public void close() {
    }
}
