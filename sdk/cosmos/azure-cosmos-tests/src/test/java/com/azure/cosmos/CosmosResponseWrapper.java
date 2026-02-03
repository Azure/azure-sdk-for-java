// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.CosmosItemResponse;
import org.apache.commons.lang3.ArrayUtils;

public class CosmosResponseWrapper {
    private final CosmosDiagnosticsContext[] diagnosticsContexts;
    private final Integer statusCode;
    private final Integer subStatusCode;

    private final Long totalRecordCount;

    public CosmosResponseWrapper(CosmosItemResponse<?> itemResponse) {
        if (itemResponse.getDiagnostics() != null &&
            itemResponse.getDiagnostics().getDiagnosticsContext() != null) {
            System.out.println(itemResponse.getDiagnostics());

            this.diagnosticsContexts = ArrayUtils.toArray(itemResponse.getDiagnostics().getDiagnosticsContext());
        } else {
            this.diagnosticsContexts = null;
        }

        this.statusCode = itemResponse.getStatusCode();
        this.subStatusCode = null;
        this.totalRecordCount = itemResponse.getItem() != null ? 1L : 0L;
    }

    public CosmosResponseWrapper(CosmosDiagnosticsContext[] ctxs, int statusCode, Integer subStatusCode, Long totalRecordCount) {
        this.diagnosticsContexts = ctxs;
        this.statusCode = statusCode;
        this.subStatusCode = subStatusCode;
        this.totalRecordCount = totalRecordCount;
    }

    public CosmosDiagnosticsContext[] getDiagnosticsContexts() {
        return this.diagnosticsContexts;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

    public Integer getSubStatusCode() {
        return this.subStatusCode;
    }

    public Long getTotalRecordCount() {
        return this.totalRecordCount;
    }
}