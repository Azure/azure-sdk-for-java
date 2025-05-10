// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;

import java.util.List;

public class ItemOperationInvocationParameters {
    public CosmosPatchItemRequestOptions options;
    public CosmosAsyncContainer container;
    public Pair<String, String> idAndPkValuePair;

    public List<Pair<String, String>> otherDocumentIdAndPkValuePairs;
    public Boolean nonIdempotentWriteRetriesEnabled;
}