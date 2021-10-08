// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.iot.modelsrepository.implementation.models.FetchModelResult;

import java.util.Map;

/**
 * This type is used to unify the expand operation return types in the recursive function and has no other use cases.
 * Do not take any dependencies on this type.
 */
class IntermediateFetchModelResult {
    private final FetchModelResult fetchModelResult;
    private final Map<String, String> map;

    IntermediateFetchModelResult(FetchModelResult fetchModelResult, Map<String, String> map) {
        this.fetchModelResult = fetchModelResult;
        this.map = map;
    }

    public FetchModelResult getFetchModelResult() {
        return fetchModelResult;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
