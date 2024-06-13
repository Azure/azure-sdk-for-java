// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CosmosCommonRequestOptions;
import com.azure.cosmos.models.ReadOnlyCommonRequestOptions;

public interface OverridableRequestOptions extends ReadOnlyCommonRequestOptions {
    void override(CosmosCommonRequestOptions cosmosCommonRequestOptions);

    default <T> T overrideOption(T source, T target) {
        if (source != null) {
            target = source;
        }
        return target;
    }
}
