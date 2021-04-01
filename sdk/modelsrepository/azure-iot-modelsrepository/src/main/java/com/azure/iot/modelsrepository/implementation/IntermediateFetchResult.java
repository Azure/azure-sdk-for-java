// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.iot.modelsrepository.implementation.models.FetchResult;

import java.util.Map;

/**
 * This type is used to unify the expand operation return types in the recursive function and has no other use cases.
 * Do not take any dependencies on this type.
 */
class IntermediateFetchResult {
    private final FetchResult fetchResult;
    private final Map<String, String> map;

    IntermediateFetchResult(FetchResult fetchResult, Map<String, String> map) {
        this.fetchResult = fetchResult;
        this.map = map;
    }

    public FetchResult getFetchResult() {
        return fetchResult;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
