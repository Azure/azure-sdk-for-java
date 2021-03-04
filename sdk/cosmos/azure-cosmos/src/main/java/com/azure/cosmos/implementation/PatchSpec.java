// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.Beta;
import com.azure.cosmos.CosmosPatchOperations;

import java.util.ArrayList;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class PatchSpec {
    private CosmosPatchOperations cosmosPatchOperations;
    private RequestOptions requestOptions;

    public PatchSpec(CosmosPatchOperations cosmosPatchOperations) {
        checkNotNull(cosmosPatchOperations, "expected non-null patch operations.");

        this.cosmosPatchOperations = cosmosPatchOperations;
        this.requestOptions = null;
    }

    public PatchSpec(CosmosPatchOperations cosmosPatchOperations, RequestOptions requestOptions) {
        checkNotNull(cosmosPatchOperations, "expected non-null patch operations.");

        this.cosmosPatchOperations = cosmosPatchOperations;
        this.requestOptions = requestOptions;
    }

    /**
     * Gets the CosmosPatchOperations associated with the patchSpec.
     *
     * @return the CosmosPatchOperations associated with the request.
     */
    public CosmosPatchOperations getCosmosPatchOperations() {
        return this.cosmosPatchOperations;
    }

    /**
     * Gets the RequestOptions associated with the patchSpec.
     *
     * @return the RequestOptions associated with the request.
     */
    public RequestOptions getRequestOptions() {
        return this.requestOptions;
    }
}
